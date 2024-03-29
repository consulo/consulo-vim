/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2016 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.ui;

import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.key.ShortcutOwner;
import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.ConfigurationException;
import consulo.configurable.StandardConfigurableIds;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.KeyboardShortcut;
import consulo.ui.ex.awt.IdeBorderFactory;
import consulo.ui.ex.awt.JBScrollPane;
import consulo.ui.ex.awt.UIUtil;
import consulo.ui.ex.awt.table.ComboBoxTableRenderer;
import consulo.ui.ex.awt.table.StripeTable;
import consulo.ui.ex.keymap.util.KeymapUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author vlan
 */
@ExtensionImpl
public class VimEmulationConfigurable implements ApplicationConfigurable {
  @NotNull
  private final VimShortcutConflictsTable.Model myConflictsTableModel = new VimShortcutConflictsTable.Model();
  @NotNull
  private final VimSettingsPanel myPanel = new VimSettingsPanel(myConflictsTableModel);

  @Nonnull
  @Override
  public String getId() {
    return "editor.vim";
  }

  @Nullable
  @Override
  public String getParentId() {
    return StandardConfigurableIds.EDITOR_GROUP;
  }

  @NotNull
  @Nls
  @Override
  public String getDisplayName() {
    return "Vim Emulation";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myPanel;
  }

  @Override
  public boolean isModified() {
    return myConflictsTableModel.isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    myConflictsTableModel.apply();
  }

  @Override
  public void reset() {
    myConflictsTableModel.reset();
  }

  private static final class VimSettingsPanel extends JPanel {
    @NotNull
    private final VimShortcutConflictsTable myShortcutConflictsTable;

    public VimSettingsPanel(@NotNull VimShortcutConflictsTable.Model model) {
      myShortcutConflictsTable = new VimShortcutConflictsTable(model);
      setLayout(new BorderLayout());
      final JScrollPane scrollPane = new JBScrollPane(myShortcutConflictsTable);
      scrollPane.setBorder(new LineBorder(UIUtil.getBorderColor()));
      final JPanel conflictsPanel = new JPanel(new BorderLayout());
      final String title = String.format("Shortcut Conflicts for Active Keymap");
      conflictsPanel.setBorder(IdeBorderFactory.createTitledBorder(title, false));
      conflictsPanel.add(scrollPane);
      add(conflictsPanel, BorderLayout.CENTER);
    }
  }

  private static final class VimShortcutConflictsTable extends StripeTable {
    public VimShortcutConflictsTable(@NotNull Model model) {
      super(model);
      getTableColumn(Column.KEYSTROKE).setPreferredWidth(100);
      getTableColumn(Column.IDE_ACTION).setPreferredWidth(400);
      final TableColumn ownerColumn = getTableColumn(Column.OWNER);
      final ComboBoxTableRenderer<ShortcutOwner> renderer = new ShortcutOwnerRenderer();
      ownerColumn.setPreferredWidth(150);
      ownerColumn.setCellRenderer(renderer);
      ownerColumn.setCellEditor(renderer);
    }

    @NotNull
    @Override
    public Dimension getMinimumSize() {
      return calcSize(super.getMinimumSize());
    }

    @NotNull
    @Override
    public Dimension getPreferredSize() {
      return calcSize(super.getPreferredSize());
    }

    @NotNull
    private Dimension calcSize(@NotNull Dimension dimension) {
      final Container container = getParent();
      if (container != null) {
        final Dimension size = container.getSize();
        return new Dimension(size.width, dimension.height);
      }
      return dimension;
    }

    @NotNull
    private TableColumn getTableColumn(@NotNull Column column) {
      return getColumnModel().getColumn(column.getIndex());
    }

    private static final class ShortcutOwnerRenderer extends ComboBoxTableRenderer<ShortcutOwner> {
      public ShortcutOwnerRenderer() {
        super(ShortcutOwner.values());
      }

      @Override
      protected void customizeComponent(ShortcutOwner owner, JTable table, boolean isSelected) {
        super.customizeComponent(owner, table, isSelected);
        if (owner == ShortcutOwner.UNDEFINED) {
          setForeground(UIUtil.getComboBoxDisabledForeground());
        }
      }

      @Override
      public boolean isCellEditable(EventObject event) {
        return true;
      }
    }

    private static final class Model extends AbstractTableModel {
      @NotNull
      private final List<Row> myRows = new ArrayList<Row>();

      public Model() {
        reset();
      }

      @Override
      public int getRowCount() {
        return myRows.size();
      }

      @Override
      public int getColumnCount() {
        return Column.values().length;
      }

      @Nullable
      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
        final Column column = Column.fromIndex(columnIndex);
        if (column != null && rowIndex >= 0 && rowIndex < myRows.size()) {
          final Row row = myRows.get(rowIndex);
          switch (column) {
            case KEYSTROKE:
              return KeymapUtil.getShortcutText(new KeyboardShortcut(row.getKeyStroke(), null));
            case IDE_ACTION:
              return row.getAction().getTemplatePresentation().getText();
            case OWNER:
              return row.getOwner();
          }
        }
        return null;
      }

      @Override
      public void setValueAt(Object object, int rowIndex, int columnIndex) {
        final Column column = Column.fromIndex(columnIndex);
        if (column != null && rowIndex >= 0 && rowIndex < myRows.size() && object instanceof ShortcutOwner) {
          final Row row = myRows.get(rowIndex);
          row.setOwner((ShortcutOwner)object);
        }
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return Column.fromIndex(columnIndex) == Column.OWNER;
      }

      @Nullable
      @Override
      public String getColumnName(int index) {
        final Column column = Column.fromIndex(index);
        return column != null ? column.getTitle() : null;
      }

      public boolean isModified() {
        return !VimPlugin.getKey().getShortcutConflicts().equals(getCurrentData());
      }

      public void apply() {
        VimPlugin.getKey().getSavedShortcutConflicts().putAll(getCurrentData());
      }

      public void reset() {
        myRows.clear();
        for (Map.Entry<KeyStroke, ShortcutOwner> entry : VimPlugin.getKey().getShortcutConflicts().entrySet()) {
          final KeyStroke keyStroke = entry.getKey();
          final List<AnAction> actions = VimPlugin.getKey().getKeymapConflicts(keyStroke);
          if (!actions.isEmpty()) {
            myRows.add(new Row(keyStroke, actions.get(0), entry.getValue()));
          }
        }
        Collections.sort(myRows);
      }

      @NotNull
      private Map<KeyStroke, ShortcutOwner> getCurrentData() {
        final Map<KeyStroke, ShortcutOwner> result = new HashMap<KeyStroke, ShortcutOwner>();
        for (Row row : myRows) {
          result.put(row.getKeyStroke(), row.getOwner());
        }
        return result;
      }
    }

    private static final class Row implements Comparable<Row> {
      @NotNull
      private final KeyStroke myKeyStroke;
      @NotNull
      private final AnAction myAction;
      @NotNull
      private ShortcutOwner myOwner;

      private Row(@NotNull KeyStroke keyStroke, @NotNull AnAction action, @NotNull ShortcutOwner owner) {
        myKeyStroke = keyStroke;
        myAction = action;
        myOwner = owner;
      }

      @NotNull
      public KeyStroke getKeyStroke() {
        return myKeyStroke;
      }

      @NotNull
      public AnAction getAction() {
        return myAction;
      }

      @NotNull
      public ShortcutOwner getOwner() {
        return myOwner;
      }

      @Override
      public int compareTo(@NotNull Row row) {
        final KeyStroke otherKeyStroke = row.getKeyStroke();
        final int keyCodeDiff = myKeyStroke.getKeyCode() - otherKeyStroke.getKeyCode();
        return keyCodeDiff != 0 ? keyCodeDiff : myKeyStroke.getModifiers() - otherKeyStroke.getModifiers();
      }

      public void setOwner(@NotNull ShortcutOwner owner) {
        myOwner = owner;
      }
    }

    private static enum Column {
      KEYSTROKE(0, "Shortcut"),
      IDE_ACTION(1, "IDE Action"),
      OWNER(2, "Handler");

      @NotNull
      private static final Map<Integer, Column> ourMembers = new HashMap<Integer, Column>();

      static {
        for (Column column : values()) {
          ourMembers.put(column.myIndex, column);
        }
      }

      private final int myIndex;
      @NotNull
      private final String myTitle;

      Column(int index, @NotNull String title) {
        myIndex = index;
        myTitle = title;
      }

      @Nullable
      public static Column fromIndex(int index) {
        return ourMembers.get(index);
      }

      public int getIndex() {
        return myIndex;
      }

      @NotNull
      public String getTitle() {
        return myTitle;
      }
    }
  }
}

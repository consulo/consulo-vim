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

import consulo.codeEditor.Editor;
import consulo.ui.ex.awt.JBScrollPane;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.helper.EditorData;
import com.maddyhome.idea.vim.helper.EditorDataContext;
import com.maddyhome.idea.vim.helper.UiHelper;
import com.maddyhome.idea.vim.option.Options;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This panel displays text in a <code>more</code> like window.
 */
public class ExOutputPanel extends JPanel {
  @NotNull private final Editor myEditor;

  @NotNull private final JLabel myLabel = new JLabel("more");
  @NotNull private final JTextArea myText = new JTextArea();
  @NotNull private final JScrollPane myScrollPane =
    new JBScrollPane(myText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  @NotNull private final ComponentAdapter myAdapter;
  private boolean myAtEnd = false;
  private int myLineHeight = 0;

  @Nullable private JComponent myOldGlass = null;
  @Nullable private LayoutManager myOldLayout = null;
  private boolean myWasOpaque = false;

  private boolean myActive = false;

  private ExOutputPanel(@NotNull Editor editor) {
    myEditor = editor;

    // Create a text editor for the text and a label for the prompt
    BorderLayout layout = new BorderLayout(0, 0);
    setLayout(layout);
    add(myScrollPane, BorderLayout.CENTER);
    add(myLabel, BorderLayout.SOUTH);

    setFontForElements();

    myText.setBorder(null);
    myScrollPane.setBorder(null);

    myLabel.setForeground(myText.getForeground());
    myLabel.setBackground(myText.getBackground());
    setForeground(myText.getForeground());
    setBackground(myText.getBackground());

    myText.setEditable(false);

    setBorder(BorderFactory.createEtchedBorder());

    myAdapter = new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        positionPanel();
      }
    };

    // Setup some listeners to handle keystrokes
    MoreKeyListener moreKeyListener = new MoreKeyListener(this);
    addKeyListener(moreKeyListener);
    myText.addKeyListener(moreKeyListener);
  }

  @NotNull
  public static ExOutputPanel getInstance(@NotNull Editor editor) {
    ExOutputPanel panel = EditorData.getMorePanel(editor);
    if (panel == null) {
      panel = new ExOutputPanel(editor);
      EditorData.setMorePanel(editor, panel);
    }
    return panel;
  }

  public void setText(@NotNull String data) {
    if (data.length() > 0 && data.charAt(data.length() - 1) == '\n') {
      data = data.substring(0, data.length() - 1);
    }

    myText.setText(data);
    myText.setCaretPosition(0);
    if (data.length() > 0) {
      activate();
    }
  }

  /**
   * Turns off the ex entry field and optionally puts the focus back to the original component
   */
  public void deactivate(boolean refocusOwningEditor) {
    if (!myActive) return;
    myActive = false;
    myText.setText("");
    if (refocusOwningEditor) {
      UiHelper.requestFocus(myEditor.getContentComponent());
    }
    if (myOldGlass != null) {
      myOldGlass.removeComponentListener(myAdapter);
      myOldGlass.setVisible(false);
      myOldGlass.remove(this);
      myOldGlass.setOpaque(myWasOpaque);
      myOldGlass.setLayout(myOldLayout);
    }
  }

  /**
   * Turns on the more window for the given editor
   */
  private void activate() {
    JRootPane root = SwingUtilities.getRootPane(myEditor.getContentComponent());
    myOldGlass = (JComponent)root.getGlassPane();
    if (myOldGlass != null) {
      myOldLayout = myOldGlass.getLayout();
      myWasOpaque = myOldGlass.isOpaque();
      myOldGlass.setLayout(null);
      myOldGlass.setOpaque(false);
      myOldGlass.add(this);
      myOldGlass.addComponentListener(myAdapter);
    }

    setFontForElements();
    positionPanel();

    if (myOldGlass != null) {
      myOldGlass.setVisible(true);
    }

    myActive = true;
    UiHelper.requestFocus(myText);
  }

  private void setFontForElements() {
    final Font font = UiHelper.getEditorFont();
    myText.setFont(font);
    myLabel.setFont(font);
  }

  private static int countLines(@NotNull String text) {
    if (text.length() == 0) {
      return 0;
    }

    int count = 0;
    int pos = -1;
    while ((pos = text.indexOf('\n', pos + 1)) != -1) {
      count++;
    }

    if (text.charAt(text.length() - 1) != '\n') {
      count++;
    }

    return count;
  }

  private void scrollLine() {
    scrollOffset(myLineHeight);
  }

  private void scrollPage() {
    scrollOffset(myScrollPane.getVerticalScrollBar().getVisibleAmount());
  }

  private void scrollHalfPage() {
    double sa = myScrollPane.getVerticalScrollBar().getVisibleAmount() / 2.0;
    double offset = Math.ceil(sa / myLineHeight) * myLineHeight;
    scrollOffset((int)offset);
  }

  private void handleEnter() {
    if (myAtEnd) {
      close();
    }
    else {
      scrollLine();
    }
  }

  private void badKey() {
    myLabel.setText("-- MORE -- (RET: line, SPACE: page, d: half page, q: quit)");
  }

  private void scrollOffset(int more) {
    myAtEnd = false;
    int val = myScrollPane.getVerticalScrollBar().getValue();
    myScrollPane.getVerticalScrollBar().setValue(val + more);
    myScrollPane.getHorizontalScrollBar().setValue(0);
    if (val + more >=
        myScrollPane.getVerticalScrollBar().getMaximum() - myScrollPane.getVerticalScrollBar().getVisibleAmount()) {
      myAtEnd = true;
      myLabel.setText("Hit ENTER or type command to continue");
    }
    else {
      myLabel.setText("-- MORE --");
    }
  }

  private void positionPanel() {
    final JComponent contentComponent = myEditor.getContentComponent();
    Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, contentComponent);
    setSize(scroll.getSize());

    myLineHeight = myText.getFontMetrics(myText.getFont()).getHeight();
    int count = countLines(myText.getText());
    int visLines = getSize().height / myLineHeight - 1;
    int lines = Math.min(count, visLines);
    setSize(getSize().width, lines * myLineHeight + myLabel.getPreferredSize().height +
                             getBorder().getBorderInsets(this).top * 2);

    int height = getSize().height;
    Rectangle bounds = scroll.getBounds();
    bounds.translate(0, scroll.getHeight() - height);
    bounds.height = height;
    Point pos = SwingUtilities.convertPoint(scroll.getParent(), bounds.getLocation(),
                                            SwingUtilities.getRootPane(contentComponent).getGlassPane());
    bounds.setLocation(pos);
    setBounds(bounds);

    myScrollPane.getVerticalScrollBar().setValue(0);
    if (!Options.getInstance().isSet("more")) {
      // FIX
      scrollOffset(100000);
    }
    else {
      scrollOffset(0);
    }
  }

  private void close() {
    close(null);
  }

  private void close(@Nullable final KeyEvent e) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        deactivate(true);

        final Project project = myEditor.getProject();

        if (project != null && e != null && e.getKeyChar() != '\n') {
          final KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
          final List<KeyStroke> keys = new ArrayList<KeyStroke>(1);
          keys.add(key);
          VimPlugin.getMacro().playbackKeys(myEditor, new EditorDataContext(myEditor), project, keys, 0, 0, 1);
        }
      }
    });
  }

  private static class MoreKeyListener extends KeyAdapter {
    private final ExOutputPanel myExOutputPanel;

    public MoreKeyListener(ExOutputPanel panel) {
      this.myExOutputPanel = panel;
    }

    /**
     * Invoked when a key has been pressed.
     */
    public void keyTyped(@NotNull KeyEvent e) {
      if (myExOutputPanel.myAtEnd) {
        myExOutputPanel.close(e);
      }
      else {
        switch (e.getKeyChar()) {
          case ' ':
            myExOutputPanel.scrollPage();
            break;
          case 'd':
            myExOutputPanel.scrollHalfPage();
            break;
          case 'q':
            myExOutputPanel.close();
            break;
          case '\n':
            myExOutputPanel.handleEnter();
            break;
          case '\u001b':
            myExOutputPanel.close();
            break;
          case KeyEvent.CHAR_UNDEFINED: {
            switch (e.getKeyCode()) {
              case KeyEvent.VK_ENTER:
                myExOutputPanel.handleEnter();
                break;
              case KeyEvent.VK_ESCAPE:
                myExOutputPanel.close();
                break;
              default:
                myExOutputPanel.badKey();
            }
          }
          default:
            myExOutputPanel.badKey();
        }
      }
    }
  }
}

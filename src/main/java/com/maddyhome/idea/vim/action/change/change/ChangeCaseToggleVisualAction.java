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

package com.maddyhome.idea.vim.action.change.change;

import consulo.dataContext.DataContext;
import consulo.codeEditor.Editor;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.action.VimCommandAction;
import com.maddyhome.idea.vim.command.Command;
import com.maddyhome.idea.vim.command.MappingMode;
import com.maddyhome.idea.vim.common.TextRange;
import com.maddyhome.idea.vim.handler.VisualOperatorActionHandler;
import com.maddyhome.idea.vim.helper.CharacterHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Set;

/**
 * @author vlan
 */
public class ChangeCaseToggleVisualAction extends VimCommandAction {
  public ChangeCaseToggleVisualAction() {
    super(new VisualOperatorActionHandler() {
      protected boolean execute(@NotNull Editor editor, @NotNull DataContext context, @NotNull Command cmd,
                                @NotNull TextRange range) {
        return VimPlugin.getChange().changeCaseRange(editor, range, CharacterHelper.CASE_TOGGLE);
      }
    });
  }

  @NotNull
  @Override
  public Set<MappingMode> getMappingModes() {
    return MappingMode.V;
  }

  @NotNull
  @Override
  public Set<List<KeyStroke>> getKeyStrokesSet() {
    return parseKeysSet("~");
  }

  @NotNull
  @Override
  public Command.Type getType() {
    return Command.Type.CHANGE;
  }

  @Override
  public int getFlags() {
    return Command.FLAG_EXIT_VISUAL;
  }
}

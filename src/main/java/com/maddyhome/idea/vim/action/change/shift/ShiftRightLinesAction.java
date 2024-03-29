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

package com.maddyhome.idea.vim.action.change.shift;

import consulo.dataContext.DataContext;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.EditorAction;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.command.Argument;
import com.maddyhome.idea.vim.handler.ChangeEditorActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public class ShiftRightLinesAction extends EditorAction {
  public ShiftRightLinesAction() {
    super(new Handler());
  }

  private static class Handler extends ChangeEditorActionHandler {
    public boolean execute(@NotNull Editor editor, @NotNull DataContext context, int count, int rawCount, @Nullable Argument argument) {
      VimPlugin.getChange().indentLines(editor, context, count, 1);

      return true;
    }
  }
}

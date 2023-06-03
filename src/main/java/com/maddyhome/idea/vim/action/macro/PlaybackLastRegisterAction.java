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

package com.maddyhome.idea.vim.action.macro;

import consulo.language.editor.PlatformDataKeys;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;
import consulo.dataContext.DataContext;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.EditorAction;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.command.Command;
import com.maddyhome.idea.vim.handler.EditorActionHandlerBase;

/**
 */
public class PlaybackLastRegisterAction extends EditorAction {
  public PlaybackLastRegisterAction() {
    super(new Handler());
  }

  private static class Handler extends EditorActionHandlerBase {
    protected boolean execute(@NotNull Editor editor, @NotNull DataContext context, @NotNull Command cmd) {
      final Project project = context.getData(PlatformDataKeys.PROJECT);
      return VimPlugin.getMacro().playbackLastRegister(editor, context, project, cmd.getCount());
    }
  }
}

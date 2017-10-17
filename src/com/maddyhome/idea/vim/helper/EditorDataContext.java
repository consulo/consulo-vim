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

package com.maddyhome.idea.vim.helper;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;

public class EditorDataContext implements DataContext {
  public EditorDataContext(Editor editor) {
    this.editor = editor;
  }

  /**
   * Returns the object corresponding to the specified data identifier. Some of the supported data identifiers are
   * defined in the {@link PlatformDataKeys} class.
   *
   * @param dataId the data identifier for which the value is requested.
   * @return the value, or null if no value is available in the current context for this identifier.
   */
  @SuppressWarnings("unchecked")
  public <T> T getData(@NotNull Key<T> dataId) {
    if (PlatformDataKeys.EDITOR == dataId) {
      return (T) editor;
    }
    else if (PlatformDataKeys.PROJECT == dataId) {
      return (T) editor.getProject();
    }
    else if (PlatformDataKeys.VIRTUAL_FILE == dataId) {
      return (T) EditorData.getVirtualFile(editor);
    }

    return null;
  }

  private final Editor editor;
}

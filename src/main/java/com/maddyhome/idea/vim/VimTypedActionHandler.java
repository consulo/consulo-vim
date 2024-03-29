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

package com.maddyhome.idea.vim;

import com.maddyhome.idea.vim.action.VimShortcutKeyAction;
import consulo.language.editor.completion.lookup.Lookup;
import consulo.language.editor.completion.lookup.LookupManager;
import consulo.dataContext.DataContext;
import consulo.logging.Logger;
import consulo.codeEditor.Editor;
import consulo.codeEditor.action.TypedActionHandler;
import com.maddyhome.idea.vim.helper.EditorDataContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Accepts all regular keystrokes and passes them on to the Vim key handler.
 *
 * IDE shortcut keys used by Vim commands are handled by {@link VimShortcutKeyAction}.
 */
public class VimTypedActionHandler implements TypedActionHandler {
  private static final Logger logger = Logger.getInstance(VimTypedActionHandler.class.getName());

  private final TypedActionHandler origHandler;
  @NotNull private final KeyHandler handler;

  public VimTypedActionHandler(TypedActionHandler origHandler) {
    this.origHandler = origHandler;
    handler = KeyHandler.getInstance();
    handler.setOriginalHandler(origHandler);
  }

  @Override
  public void execute(@NotNull final Editor editor, final char charTyped, @NotNull final DataContext context) {
    if (isEnabled(editor)) {
      // Run key handler outside of the key typed command for creating our own undoable commands
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            handler.handleKey(editor, KeyStroke.getKeyStroke(charTyped), new EditorDataContext(editor));
          }
          catch (Throwable e) {
            logger.error(e);
          }
        }
      });
    }
    else {
      origHandler.execute(editor, charTyped, context);
    }
  }

  private boolean isEnabled(@NotNull Editor editor) {
    if (VimPlugin.isEnabled()) {
      final Lookup lookup = LookupManager.getActiveLookup(editor);
      return lookup == null || !lookup.isFocused();
    }
    return false;
  }
}

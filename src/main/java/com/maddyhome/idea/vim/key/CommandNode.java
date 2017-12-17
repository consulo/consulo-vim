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

package com.maddyhome.idea.vim.key;

import com.intellij.openapi.actionSystem.AnAction;
import com.maddyhome.idea.vim.command.Command;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * This node represents a command in the key/action tree
 */
public class CommandNode implements Node {
  /**
   * Creates a command node for the key and action
   *
   * @param key     The final keystroke in this command
   * @param actName The name of the action
   * @param action  The action that executes this command
   * @param cmdType The type of the command
   * @param flags   Any special flags needs by the command
   */
  public CommandNode(KeyStroke key, String actName, AnAction action, @NotNull Command.Type cmdType, int flags) {
    this.key = key;
    this.actionId = actName;
    this.action = action;
    this.type = cmdType;
    this.flags = flags;
  }

  public String getActionId() {
    return actionId;
  }

  /**
   * Gets the command's action
   *
   * @return The command's action
   */
  public AnAction getAction() {
    return action;
  }

  /**
   * Gets the command's keystroke
   *
   * @return The command's keystroke
   */
  public KeyStroke getKey() {
    return key;
  }

  /**
   * Gets the command's type
   *
   * @return The command's type
   */
  @NotNull
  public Command.Type getCmdType() {
    return type;
  }

  /**
   * Gets the command's flags
   *
   * @return The command's flags
   */
  public int getFlags() {
    return flags;
  }

  @NotNull
  public String toString() {

    return "CommandNode[key=" + key + ", actionId=" + actionId + ", action=" + action + ", argType=" + type + "]";
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommandNode)) return false;

    final CommandNode node = (CommandNode)o;

    return flags == node.flags &&
           type == node.type &&
           actionId.equals(node.actionId) &&
           action.equals(node.action) &&
           key.equals(node.key);
  }

  public int hashCode() {
    int result;
    result = key.hashCode();
    result = 29 * result + actionId.hashCode();
    result = 29 * result + action.hashCode();
    result = 29 * result + type.hashCode();
    result = 29 * result + flags;
    return result;
  }

  protected final KeyStroke key;
  protected final AnAction action;
  protected final String actionId;
  @NotNull protected final Command.Type type;
  protected final int flags;
}

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
import com.maddyhome.idea.vim.command.Argument;
import com.maddyhome.idea.vim.command.Command;
import org.jetbrains.annotations.NotNull;

/**
 * This represents a command argument node in the key/action tree. Currently arguments of argType character
 * and motion command are used.
 */
public class ArgumentNode implements Node {
  /**
   * Creates a node for the given action.
   *
   * @param actionId The id of the action.
   * @param action   The action this arguments is mapped to.
   * @param cmdType  The type of the command this argument is for.
   * @param argType  The type of the argument.
   * @param flags    Any special flags associated with this argument.
   */
  public ArgumentNode(String actionId, AnAction action, @NotNull Command.Type cmdType, @NotNull Argument.Type argType,
                      int flags) {
    this.actionId = actionId;
    this.action = action;
    this.argType = argType;
    this.cmdType = cmdType;
    this.flags = flags;
  }

  public String getActionId() {
    return actionId;
  }

  /**
   * Gets the action of the argument
   *
   * @return The argument's action
   */
  public AnAction getAction() {
    return action;
  }

  /**
   * Gets the argument type
   *
   * @return The argument's type
   */
  @NotNull
  public Argument.Type getArgType() {
    return argType;
  }

  /**
   * Gets the type of the command this arguments is for
   *
   * @return The argument's command type
   */
  @NotNull
  public Command.Type getCmdType() {
    return cmdType;
  }

  /**
   * Gets the argument flags
   *
   * @return The argument's flags
   */
  public int getFlags() {
    return flags;
  }

  @NotNull
  public String toString() {

    return "ArgumentNode[" +
           "actionId=" +
           actionId +
           ", action=" +
           action +
           ", argType=" +
           argType +
           ", flags=" +
           flags +
           "]";
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArgumentNode)) return false;

    final ArgumentNode node = (ArgumentNode)o;

    return argType == node.argType &&
           cmdType == node.cmdType &&
           flags == node.flags &&
           actionId.equals(node.actionId) &&
           action.equals(node.action);
  }

  public int hashCode() {
    int result;
    result = action.hashCode();
    result = 29 * result + actionId.hashCode();
    result = 29 * result + argType.ordinal();
    result = 29 * result + cmdType.hashCode();
    result = 29 * result + flags;
    return result;
  }

  protected final String actionId;
  protected final AnAction action;
  @NotNull protected final Argument.Type argType;
  @NotNull protected final Command.Type cmdType;
  protected final int flags;
}

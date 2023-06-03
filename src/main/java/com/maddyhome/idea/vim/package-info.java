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

/**
 * IdeaVim command index.
 *
 *
 * 1. Insert mode
 *
 * tag                      action
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * |i_<Esc>|                {@link InsertExitModeAction}
 * |i_CTRL-[|               ...
 * |i_CTRL-C|               ...
 * |i_CTRL-\_CTRL-N|        ...
 *
 *
 * 2. Normal mode
 *
 * tag                      action
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * |i|                      {@link InsertBeforeCursorAction}
 * |<Insert>|               ...
 * |v|                      {@link VisualToggleCharacterModeAction}
 * |gv|                     {@link VisualSelectPreviousAction}
 *
 *
 * 2.2. Window commands
 *
 * tag                      action
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * |CTRL-W_+|               TODO
 * |CTRL-W_-|               TODO
 * |CTRL-W_<|               TODO
 * |CTRL-W_=|               TODO
 * |CTRL-W_>|               TODO
 * |CTRL-W_H|               TODO
 * |CTRL-W_J|               TODO
 * |CTRL-W_K|               TODO
 * |CTRL-W_L|               TODO
 * |CTRL-W_R|               TODO
 * |CTRL-W_W|               {@link WindowPrevAction}
 * |CTRL-W_]|               TODO
 * |CTRL-W_^|               TODO
 * |CTRL-W__|               TODO
 * |CTRL-W_b|               TODO
 * |CTRL-W_c|               {@link CloseWindowAction}
 * |CTRL-W_h|               {@link WindowLeftAction}
 * |CTRL-W_<Left>|          ...
 * |CTRL-W_j|               {@link WindowDownAction}
 * |CTRL-W_<Down>|          ...
 * |CTRL-W_k|               {@link WindowUpAction}
 * |CTRL-W_<Up>|            ...
 * |CTRL-W_l|               {@link WindowRightAction}
 * |CTRL-W_<Right>|         ...
 * |CTRL-W_n|               TODO
 * |CTRL-W_o|               {@link WindowOnlyAction}
 * |CTRL-W_CTRL-O|          ...
 * |CTRL-W_p|               TODO
 * |CTRL-W_q|               TODO
 * |CTRL-W_r|               TODO
 * |CTRL-W_s|               {@link HorizontalSplitAction}
 * |CTRL-W_S|               ...
 * |CTRL-W_CTRL-S|          ...
 * |CTRL-W_t|               TODO
 * |CTRL-W_v|               {@link VerticalSplitAction}
 * |CTRL-W_CTRL-V|          ...
 * |CTRL-W_w|               {@link WindowNextAction}
 * |CTRL-W_CTRL-W|          ...
 * |CTRL-W_z|               TODO
 * |CTRL-W_bar|             TODO
 *
 *
 * 2.4. Commands starting with 'g'
 *
 * tag                      action
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * |g@|                     {@link OperatorAction}
 *
 *
 * 3. Visual mode
 *
 * tag                      action
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * |v_<Esc>|                {@link VisualExitModeAction}
 * |v_CTRL-C|               ...
 * |v_CTRL-\_CTRL-N|        ...
 * |v_<BS>|                 NVO mapping
 * |v_CTRL-H|               ...
 * |v_CTRL-V|               NVO mapping
 * |v_!|                    {@link FilterVisualLinesAction}
 * |v_:|                    NVO mapping
 * |v_<|                    {@link ShiftLeftVisualAction}
 * |v_=|                    {@link AutoIndentLinesVisualAction}
 * |v_>|                    {@link ShiftRightVisualAction}
 * |v_[p|                   {@link PutVisualTextNoIndentAction}
 * |v_]p|                   ...
 * |v_[P|                   ...
 * |v_]P|                   ...
 * |v_A|                    {@link VisualBlockAppendAction}
 * |v_C|                    {@link ChangeVisualLinesEndAction}
 * |v_D|                    {@link DeleteVisualLinesEndAction}
 * |v_I|                    {@link VisualBlockInsertAction}
 * |v_J|                    {@link DeleteJoinVisualLinesSpacesAction}
 * |v_K|                    TODO
 * |v_O|                    {@link VisualSwapEndsBlockAction}
 * |v_R|                    {@link ChangeVisualLinesAction}
 * |v_S|                    ...
 * |v_U|                    {@link ChangeCaseUpperVisualAction}
 * |v_V|                    NV mapping
 * |v_X|                    {@link DeleteVisualLinesAction}
 * |v_Y|                    {@link YankVisualLinesAction}
 * |v_a"|                   VO mapping
 * |v_a'|                   VO mapping
 * |v_ab|                   VO mapping
 * |v_a(|                   ...
 * |v_a)|                   ...
 * |v_at|                   {@link MotionOuterBlockTagAction}
 * |v_a<|                   ...
 * |v_a>|                   ...
 * |v_aB|                   VO mapping
 * |v_a{|                   ...
 * |v_a}|                   ...
 * |v_aW|                   VO mapping
 * |v_a[|                   VO mapping
 * |v_a]|                   ...
 * |v_a`|                   VO mapping
 * |v_ap|                   VO mapping
 * |v_as|                   VO mapping
 * |v_aw|                   VO mapping
 * |v_c|                    {@link ChangeVisualAction}
 * |v_s|                    ...
 * |v_d|                    {@link DeleteVisualAction}
 * |v_x|                    ...
 * |v_<Del>|                ...
 * |v_gJ|                   {@link DeleteJoinVisualLinesAction}
 * |v_gp|                   {@link PutVisualTextMoveCursorAction}
 * |v_gP|                   ...
 * |v_gq|                   {@link ReformatCodeVisualAction}
 * |v_gv|                   {@link VisualSwapSelectionsAction}
 * |v_i"|                   VO mapping
 * |v_i'|                   VO mapping
 * |v_ib|                   VO mapping
 * |v_i(|                   ...
 * |v_i)|                   ...
 * |v_it|                   {@link MotionInnerBlockTagAction}
 * |v_i<|                   ...
 * |v_i>|                   ...
 * |v_iB|                   VO mapping
 * |v_i{|                   ...
 * |v_i}|                   ...
 * |v_iW|                   VO mapping
 * |v_i[|                   VO mapping
 * |v_i]|                   ...
 * |v_i`|                   VO mapping
 * |v_ip|                   VO mapping
 * |v_is|                   VO mapping
 * |v_iw|                   VO mapping
 * |v_o|                    {@link VisualSwapEndsAction}
 * |v_p|                    {@link PutVisualTextAction}
 * |v_P|                    ...
 * |v_r|                    {@link ChangeVisualCharacterAction}
 * |v_u|                    {@link ChangeCaseLowerVisualAction}
 * |v_v|                    NV mapping
 * |v_y|                    {@link YankVisualAction}
 * |v_~|                    {@link ChangeCaseToggleVisualAction}
 *
 * TODO: Support Select mode and commands associated with it, such as |v_CTRL-G|, |v_CTRL-O|
 *
 *
 * 5. Ex commands
 *
 * tag                      handler
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * |:map|                   {@link MapHandler}
 * |:nmap|                  ...
 * |:vmap|                  ...
 * |:omap|                  ...
 * |:imap|                  ...
 * |:cmap|                  ...
 * |:noremap|               ...
 * |:nnoremap|              ...
 * |:vnoremap|              ...
 * |:onoremap|              ...
 * |:inoremap|              ...
 * |:cnoremap|              ...
 * |:sort|                  {@link SortHandler}
 * |:source|                {@link SourceHandler}
 *
 * @see :help index.
 *
 * @author vlan
 */
package com.maddyhome.idea.vim;

import com.maddyhome.idea.vim.action.change.OperatorAction;
import com.maddyhome.idea.vim.action.change.change.*;
import com.maddyhome.idea.vim.action.change.delete.*;
import com.maddyhome.idea.vim.action.change.insert.InsertBeforeCursorAction;
import com.maddyhome.idea.vim.action.change.insert.InsertExitModeAction;
import com.maddyhome.idea.vim.action.change.insert.VisualBlockAppendAction;
import com.maddyhome.idea.vim.action.change.insert.VisualBlockInsertAction;
import com.maddyhome.idea.vim.action.change.shift.ShiftLeftVisualAction;
import com.maddyhome.idea.vim.action.change.shift.ShiftRightVisualAction;
import com.maddyhome.idea.vim.action.copy.*;
import com.maddyhome.idea.vim.action.motion.object.MotionInnerBlockTagAction;
import com.maddyhome.idea.vim.action.motion.object.MotionOuterBlockTagAction;
import com.maddyhome.idea.vim.action.motion.visual.*;
import com.maddyhome.idea.vim.action.window.*;
import com.maddyhome.idea.vim.ex.handler.MapHandler;
import com.maddyhome.idea.vim.ex.handler.SortHandler;
import com.maddyhome.idea.vim.ex.handler.SourceHandler;
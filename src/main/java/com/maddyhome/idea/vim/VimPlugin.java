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

import com.maddyhome.idea.vim.ex.CommandParser;
import com.maddyhome.idea.vim.ex.vimscript.VimScriptParser;
import com.maddyhome.idea.vim.group.*;
import com.maddyhome.idea.vim.helper.DocumentManager;
import com.maddyhome.idea.vim.helper.MacKeyRepeat;
import com.maddyhome.idea.vim.option.Options;
import com.maddyhome.idea.vim.ui.VimEmulationConfigurable;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.application.ApplicationManager;
import consulo.application.util.SystemInfo;
import consulo.codeEditor.action.EditorActionManager;
import consulo.codeEditor.action.TypedAction;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.disposer.Disposable;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.project.event.ProjectManagerAdapter;
import consulo.project.ui.notification.Notification;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.notification.event.NotificationListener;
import consulo.project.ui.wm.StatusBar;
import consulo.project.ui.wm.WindowManager;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.keymap.Keymap;
import consulo.ui.ex.keymap.KeymapManager;
import jakarta.inject.Singleton;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;

/**
 * This plugin attempts to emulate the key binding and general functionality of Vim and gVim. See the supplied
 * documentation for a complete list of supported and unsupported Vim emulation. The code base contains some debugging
 * output that can be enabled in necessary.
 * <p/>
 * This is an application level plugin meaning that all open projects will share a common instance of the plugin.
 * Registers and marks are shared across open projects so you can copy and paste between files of different projects.
 *
 * @version 0.1
 */
@ServiceAPI(value = ComponentScope.APPLICATION, lazy = false)
@ServiceImpl
@Singleton
@State(name = "VimSettings", storages = @Storage("vim_settings.xml"))
public class VimPlugin implements PersistentStateComponent<Element>, Disposable {
    public static final NotificationGroup IDEAVIM_NOTIFICATION_ID = NotificationGroup.balloonGroup("vim");
    public static final NotificationGroup IDEAVIM_STICKY_NOTIFICATION_ID = NotificationGroup.balloonGroup("vim-sticky");
    public static final String IDEAVIM_NOTIFICATION_TITLE = "Vim Emulator";
    public static final int STATE_VERSION = 4;

    private boolean error = false;

    private int previousStateVersion = 0;
    private String previousKeyMap = "";

    // It is enabled by default to avoid any special configuration after plugin installation
    private boolean enabled = true;

    private static final Logger LOG = Logger.getInstance(VimPlugin.class);

    @NotNull
    private final MotionGroup motion;
    @NotNull
    private final ChangeGroup change;
    @NotNull
    private final CopyGroup copy;
    @NotNull
    private final MarkGroup mark;
    @NotNull
    private final RegisterGroup register;
    @NotNull
    private final FileGroup file;
    @NotNull
    private final SearchGroup search;
    @NotNull
    private final ProcessGroup process;
    @NotNull
    private final MacroGroup macro;
    @NotNull
    private final DigraphGroup digraph;
    @NotNull
    private final HistoryGroup history;
    @NotNull
    private final KeyGroup key;
    @NotNull
    private final WindowGroup window;
    @NotNull
    private final EditorGroup editor;

    public VimPlugin() {
        motion = new MotionGroup();
        change = new ChangeGroup();
        copy = new CopyGroup();
        mark = new MarkGroup();
        register = new RegisterGroup();
        file = new FileGroup();
        search = new SearchGroup();
        process = new ProcessGroup();
        macro = new MacroGroup();
        digraph = new DigraphGroup();
        history = new HistoryGroup();
        key = new KeyGroup();
        window = new WindowGroup();
        editor = new EditorGroup();

        LOG.debug("VimPlugin ctr");
    }

    @Override
    public void afterLoadState() {
        LOG.debug("initComponent");

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                updateState();
            }
        });

        final TypedAction typedAction = EditorActionManager.getInstance().getTypedAction();
        EventFacade.getInstance().setupTypedActionHandler(new VimTypedActionHandler(typedAction.getRawHandler()));

        // Register vim actions in command mode
        RegisterActions.registerActions();

        // Add some listeners so we can handle special events
        setupListeners(this);

        // Register ex handlers
        CommandParser.getInstance().registerHandlers();

        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            final File ideaVimRc = VimScriptParser.findIdeaVimRc();
            if (ideaVimRc != null) {
                VimScriptParser.executeFile(ideaVimRc);
            }
        }

        LOG.debug("done");
    }

    @Override
    public void dispose() {
        LOG.debug("disposeComponent");
        turnOffPlugin();
        EventFacade.getInstance().restoreTypedActionHandler();
        LOG.debug("done");
    }

    @Override
    public Element getState() {
        LOG.debug("Saving state");

        final Element element = new Element("ideavim");
        // Save whether the plugin is enabled or not
        final Element state = new Element("state");
        state.setAttribute("version", Integer.toString(STATE_VERSION));
        state.setAttribute("enabled", Boolean.toString(enabled));
        element.addContent(state);

        mark.saveData(element);
        register.saveData(element);
        search.saveData(element);
        history.saveData(element);
        key.saveData(element);
        editor.saveData(element);

        return element;
    }

    @Override
    public void loadState(@NotNull final Element element) {
        LOG.debug("Loading state");

        // Restore whether the plugin is enabled or not
        Element state = element.getChild("state");
        if (state != null) {
            try {
                previousStateVersion = Integer.valueOf(state.getAttributeValue("version"));
            }
            catch (NumberFormatException ignored) {
            }
            enabled = Boolean.valueOf(state.getAttributeValue("enabled"));
            previousKeyMap = state.getAttributeValue("keymap");
        }

        mark.readData(element);
        register.readData(element);
        search.readData(element);
        history.readData(element);
        key.readData(element);
        editor.readData(element);
    }

    @NotNull
    public static MotionGroup getMotion() {
        return getInstance().motion;
    }

    @NotNull
    public static ChangeGroup getChange() {
        return getInstance().change;
    }

    @NotNull
    public static CopyGroup getCopy() {
        return getInstance().copy;
    }

    @NotNull
    public static MarkGroup getMark() {
        return getInstance().mark;
    }

    @NotNull
    public static RegisterGroup getRegister() {
        return getInstance().register;
    }

    @NotNull
    public static FileGroup getFile() {
        return getInstance().file;
    }

    @NotNull
    public static SearchGroup getSearch() {
        return getInstance().search;
    }

    @NotNull
    public static ProcessGroup getProcess() {
        return getInstance().process;
    }

    @NotNull
    public static MacroGroup getMacro() {
        return getInstance().macro;
    }

    @NotNull
    public static DigraphGroup getDigraph() {
        return getInstance().digraph;
    }

    @NotNull
    public static HistoryGroup getHistory() {
        return getInstance().history;
    }

    @NotNull
    public static KeyGroup getKey() {
        return getInstance().key;
    }

    @NotNull
    public static WindowGroup getWindow() {
        return getInstance().window;
    }

    @NotNull
    public static EditorGroup getEditor() {
        return getInstance().editor;
    }

    public static boolean isEnabled() {
        return getInstance().enabled;
    }

    public static void setEnabled(final boolean enabled) {
        if (!enabled) {
            getInstance().turnOffPlugin();
        }

        getInstance().enabled = enabled;

        if (enabled) {
            getInstance().turnOnPlugin();
        }
    }

    public static boolean isError() {
        return getInstance().error;
    }

    /**
     * Indicate to the user that an error has occurred. Just beep.
     */
    public static void indicateError() {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            getInstance().error = true;
        }
        else if (!Options.getInstance().isSet("visualbell")) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public static void clearError() {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            getInstance().error = false;
        }
    }

    public static void showMode(String msg) {
        showMessage(msg);
    }

    public static void showMessage(@Nullable String msg) {
        ProjectManager pm = ProjectManager.getInstance();
        Project[] projects = pm.getOpenProjects();
        for (Project project : projects) {
            StatusBar bar = WindowManager.getInstance().getStatusBar(project);
            if (bar != null) {
                if (msg == null || msg.length() == 0) {
                    bar.setInfo("");
                }
                else {
                    bar.setInfo("VIM - " + msg);
                }
            }
        }
    }

    @NotNull
    private static VimPlugin getInstance() {
        return Application.get().getComponent(VimPlugin.class);
    }

    private void turnOnPlugin() {
        KeyHandler.getInstance().fullReset(null);

        getEditor().turnOn();
        getMotion().turnOn();
    }

    private void turnOffPlugin() {
        KeyHandler.getInstance().fullReset(null);

        getEditor().turnOff();
        getMotion().turnOff();
    }

    private void updateState() {
        if (isEnabled()) {
            if (SystemInfo.isMac) {
                final MacKeyRepeat keyRepeat = MacKeyRepeat.getInstance();
                final Boolean enabled = keyRepeat.isEnabled();
                final Boolean isKeyRepeat = editor.isKeyRepeat();
                if ((enabled == null || !enabled) && (isKeyRepeat == null || isKeyRepeat)) {
                    if (Messages.showYesNoDialog("Do you want to enable repeating keys in Mac OS X on press and hold?\n\n" +
                                                   "(You can do it manually by running 'defaults write -g " +
                                                   "ApplePressAndHoldEnabled 0' in the console).", IDEAVIM_NOTIFICATION_TITLE,
                                                 Messages.getQuestionIcon()) == Messages.YES) {
                        editor.setKeyRepeat(true);
                        keyRepeat.setEnabled(true);
                    }
                    else {
                        editor.setKeyRepeat(false);
                    }
                }
            }
            if (previousStateVersion > 0 && previousStateVersion < 3) {
                final KeymapManager manager = KeymapManager.getInstance();
                Keymap keymap = null;
                if (previousKeyMap != null) {
                    keymap = manager.getKeymap(previousKeyMap);
                }
                if (keymap == null) {
                    keymap = manager.getKeymap(manager.getDefaultKeymap().getName());
                }
                assert keymap != null : "Default keymap not found";
                new Notification(
                  VimPlugin.IDEAVIM_STICKY_NOTIFICATION_ID,
                  VimPlugin.IDEAVIM_NOTIFICATION_TITLE,
                  String.format("Vim Emulator plugin doesn't use the special \"Vim\" keymap any longer. " +
                                  "Switching to \"%s\" keymap.<br/><br/>" +
                                  "Now it is possible to set up:<br/>" +
                                  "<ul>" +
                                  "<li>Vim keys in your ~/.consulovimrc file using key mapping commands</li>" +
                                  "<li>IDE action shortcuts in \"File | " + ShowSettingsUtil.getSettingsMenuName() + " | Keymap\"</li>" +
                                  "<li>Vim or IDE handlers for conflicting shortcuts in <a href='#settings'>Vim Emulation</a> settings</li>" +
                                  "</ul>", keymap.getPresentableName()),
                  NotificationType.INFORMATION,
                  new NotificationListener.Adapter() {
                      @Override
                      @RequiredUIAccess
                      protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                          ShowSettingsUtil.getInstance().showAndSelect(null, VimEmulationConfigurable.class);
                      }
                  }).notify(null);
                manager.setActiveKeymap(keymap);
            }
            if (previousStateVersion > 0 && previousStateVersion < 4) {
                new Notification(
                  VimPlugin.IDEAVIM_STICKY_NOTIFICATION_ID,
                  VimPlugin.IDEAVIM_NOTIFICATION_TITLE,
                  "The ~/.vimrc file is no longer read by default, use ~/.consulovimrc instead. You can read it from your " +
                    "~/.consulovimrc using this command:<br/><br/>" +
                    "<code>source ~/.vimrc</code>",
                  NotificationType.INFORMATION).notify(null);
            }
        }
    }

    /**
     * This sets up some listeners so we can handle various events that occur
     */
    private void setupListeners(Disposable disposable) {
        final EventFacade eventFacade = EventFacade.getInstance();

        DocumentManager.getInstance().addDocumentListener(new MarkGroup.MarkUpdater());
        DocumentManager.getInstance().addDocumentListener(new SearchGroup.DocumentSearchListener());

        eventFacade.addProjectManagerListener(new ProjectManagerAdapter() {
            @Override
            public void projectOpened(@NotNull final Project project) {
                eventFacade.addFileEditorManagerListener(project, new MotionGroup.MotionEditorChange());
                eventFacade.addFileEditorManagerListener(project, new FileGroup.SelectionCheck());
                eventFacade.addFileEditorManagerListener(project, new SearchGroup.EditorSelectionCheck());
            }
        }, disposable);
    }
}

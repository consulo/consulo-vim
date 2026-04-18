/**
 * @author VISTALL
 * @since 02/06/2023
 */
// open for actions - maybe later remove it?
open module consulo.vim {
    requires consulo.application.api;
    requires consulo.base.localize.library;
    requires consulo.code.editor.api;
    requires consulo.color.scheme.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.container.api;
    requires consulo.datacontext.api;
    requires consulo.disposer.api;
    requires consulo.document.api;
    requires consulo.file.editor.api;
    requires consulo.ide.api;
    requires consulo.language.api;
    requires consulo.language.code.style.api;
    requires consulo.language.editor.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.module.content.api;
    requires consulo.platform.api;
    requires consulo.project.api;
    requires consulo.project.ui.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.undo.redo.api;
    requires consulo.util.collection;
    requires consulo.util.dataholder;
    requires consulo.util.io;
    requires consulo.util.lang;
    requires consulo.virtual.file.system.api;

    requires com.google.common;

    // TODO remove in future
    requires java.desktop;
}

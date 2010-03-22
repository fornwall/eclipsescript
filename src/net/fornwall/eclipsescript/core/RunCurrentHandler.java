package net.fornwall.eclipsescript.core;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.scripts.ScriptMetadata;
import net.fornwall.eclipsescript.scripts.ScriptStore;
import net.fornwall.eclipsescript.ui.ErrorHandlingHandler;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class RunCurrentHandler extends ErrorHandlingHandler {

	@Override
	protected void doExecute(ExecutionEvent event) throws Exception {
		IEditorInput editorInput = EclipseUtils.getCurrentEditorInput();
		if (editorInput instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) editorInput;
			IFile editedFile = fileInput.getFile();
			boolean isScriptFile = ScriptFilesChangeListener.isEclipseScript(editedFile);
			if (isScriptFile) {
				ScriptMetadata m = new ScriptMetadata(fileInput.getFile());
				ScriptStore.executeScript(m);
				return;
			}
		}
		MessageDialog.openInformation(EclipseUtils.getWindowShell(), Messages.cannotRunCurrentScriptTitle,
				Messages.cannotRunCurrentScriptText);
	}

}

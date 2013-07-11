package org.eclipsescript.core;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.scripts.ScriptLanguageHandler;
import org.eclipsescript.scripts.ScriptMetadata;
import org.eclipsescript.scripts.ScriptStore;
import org.eclipsescript.ui.ErrorHandlingHandler;
import org.eclipsescript.util.EclipseUtils;

public class RunCurrentHandler extends ErrorHandlingHandler {

	@Override
	protected void doExecute(ExecutionEvent event) throws Exception {
		IEditorInput editorInput = EclipseUtils.getCurrentEditorInput();
		if (editorInput instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) editorInput;
			IFile editedFile = fileInput.getFile();
			boolean isScriptFile = ScriptLanguageHandler.isEclipseScriptFile(editedFile);
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

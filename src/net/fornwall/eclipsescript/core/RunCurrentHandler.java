package net.fornwall.eclipsescript.core;

import net.fornwall.eclipsescript.scripts.ScriptMetadata;
import net.fornwall.eclipsescript.scripts.ScriptStore;
import net.fornwall.eclipsescript.ui.ErrorHandlingHandler;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class RunCurrentHandler extends ErrorHandlingHandler {

	@Override
	protected void doExecute(ExecutionEvent event) throws Exception {
		IEditorInput editorInput = EclipseUtils.getCurrentEditorInput();
		if (editorInput instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) editorInput;
			boolean isScriptFile = ScriptFilesChangeListener.iEclipseScript(fileInput.getFile());
			if (isScriptFile) {
				for (ScriptMetadata m : ScriptStore.getAllMetadatas()) {
					if (m.getFile().equals(fileInput.getFile())) {
						ScriptStore.executeScript(m);
						return;
					}
				}
				Activator.logError(new RuntimeException("Could not find current file in script store"));
				return;
			}
		}
		MessageDialog.openInformation(EclipseUtils.getWindowShell(), "Cannot run",
				"Cannot run the currently edited script");
	}

}

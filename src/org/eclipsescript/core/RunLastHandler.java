package org.eclipsescript.core;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.scripts.ScriptMetadata;
import org.eclipsescript.scripts.ScriptStore;
import org.eclipsescript.ui.ErrorHandlingHandler;
import org.eclipsescript.util.EclipseUtils;

public class RunLastHandler extends ErrorHandlingHandler {

	public static ScriptMetadata lastRun = null;

	@Override
	public void doExecute(ExecutionEvent event) throws ExecutionException {
		if (lastRun == null) {
			MessageDialog.openWarning(EclipseUtils.activeWindow().getShell(), "No script to run", //$NON-NLS-1$
					Messages.runScriptBeforeRunningLast);
		} else {
			ScriptStore.executeScript(lastRun);
		}
	}
}

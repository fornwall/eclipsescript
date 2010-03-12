package net.fornwall.eclipsescript.core;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.scripts.ScriptMetadata;
import net.fornwall.eclipsescript.scripts.ScriptStore;
import net.fornwall.eclipsescript.ui.ErrorHandlingHandler;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;

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

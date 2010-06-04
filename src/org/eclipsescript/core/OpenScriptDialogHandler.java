package org.eclipsescript.core;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipsescript.ui.ErrorHandlingHandler;
import org.eclipsescript.ui.QuickScriptDialog;
import org.eclipsescript.util.EclipseUtils;

public class OpenScriptDialogHandler extends ErrorHandlingHandler {

	@Override
	public void doExecute(ExecutionEvent event) throws ExecutionException {
		QuickScriptDialog d = new QuickScriptDialog(EclipseUtils.activeWindow());
		d.open();
	}

}

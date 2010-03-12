package net.fornwall.eclipsescript.core;

import net.fornwall.eclipsescript.ui.ErrorHandlingHandler;
import net.fornwall.eclipsescript.ui.QuickScriptDialog;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class OpenScriptDialogHandler extends ErrorHandlingHandler {

	@Override
	public void doExecute(ExecutionEvent event) throws ExecutionException {
		QuickScriptDialog d = new QuickScriptDialog(EclipseUtils.activeWindow(), event.getCommand());
		d.open();
	}

}

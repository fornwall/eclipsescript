package org.eclipsescript.ui;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipsescript.core.Activator;

public abstract class ErrorHandlingHandler extends AbstractHandler {

	protected abstract void doExecute(ExecutionEvent event) throws Exception;

	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			doExecute(event);
		} catch (LinkageError e) {
			Activator.logError(e);
		} catch (Exception e) {
			Activator.logError(e);
		}
		return null;
	}

}

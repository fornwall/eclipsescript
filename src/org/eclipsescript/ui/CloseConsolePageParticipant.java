package org.eclipsescript.ui;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

public class CloseConsolePageParticipant implements IConsolePageParticipant {

	@Override
	public void activated() {
		// do nothing
	}

	@Override
	public void deactivated() {
		// do nothing
	}

	@Override
	public void dispose() {
		// do nothing
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return adapter.isInstance(this) ? this : null;
	}

	/** Method overridden to add close console action to the console toolbar. */
	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		CloseConsoleAction action = new CloseConsoleAction(console);
		IPageSite site = page.getSite();
		IActionBars actionBars = site.getActionBars();
		IToolBarManager manager = actionBars.getToolBarManager();
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, action);
	}

}

package org.eclipsescript.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipsescript.core.Activator;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.scriptobjects.Console;

public class CloseConsolePageParticipant implements IConsolePageParticipant {

	public static class RemoveAllTerminatedAction extends Action {

		public RemoveAllTerminatedAction() {
			super(Messages.removeAllTerminatedConsoles, Activator.getImageDescriptor(Activator.IMG_REMOVE_ALL));
			setToolTipText(Messages.removeAllTerminatedConsoles);
		}

		@Override
		public void run() {
			// ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { fConsole });
			ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
			IConsoleManager consoleManager = consolePlugin.getConsoleManager();
			List<IConsole> consolesToRemove = new ArrayList<IConsole>();
			for (IConsole console : consoleManager.getConsoles()) {
				if (console instanceof Console.ConsoleClass) {
					((Console.ConsoleClass) console).isDisposed = true;
					consolesToRemove.add(console);
				}
			}
			consoleManager.removeConsoles(consolesToRemove.toArray(new IConsole[consolesToRemove.size()]));
		}

	}

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
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, new RemoveAllTerminatedAction());
	}
}

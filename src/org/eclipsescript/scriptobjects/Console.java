package org.eclipsescript.scriptobjects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.scripts.IScriptRuntime;
import org.eclipsescript.util.EclipseUtils;
import org.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;

public class Console {

	// just a marker superclass for enablement in console closer, see plugin.xml
	public static class ConsoleClass extends MessageConsole {
		public boolean isDisposed = false;

		public ConsoleClass(String name, ImageDescriptor imageDescriptor) {
			super(name, imageDescriptor);
		}

		@Override
		protected void dispose() {
			isDisposed = true;
			super.dispose();
		}

	}

	private ConsoleClass console;
	private final String name;
	MessageConsoleStream out;

	public Console(IScriptRuntime runtime) {
		this.name = NLS.bind(Messages.scriptConsoleName, runtime.getExecutingFile().getName());
	}

	void init() {
		// Open a console for the first time or re-open
		if (console == null || console.isDisposed) {
			console = new ConsoleClass(name, null);
			out = console.newMessageStream();
			ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
			IConsoleManager consoleManager = consolePlugin.getConsoleManager();
			consoleManager.addConsoles(new IConsole[] { console });
			consoleManager.showConsoleView(console);
		}
	}

	public void print(final String msg) throws Exception {
		EclipseUtils.runInDisplayThreadAsync(new DisplayThreadRunnable() {

			@Override
			public void runWithDisplay(Display display) throws Exception {
				init();
				out.print(msg);
			}

		});
	}

	public void println(final String msg) throws Exception {
		EclipseUtils.runInDisplayThreadAsync(new DisplayThreadRunnable() {

			@Override
			public void runWithDisplay(Display display) throws Exception {
				init();
				out.println(msg);
			}

		});
	}

	public void printStackTrace(final Throwable t) throws Exception {
		EclipseUtils.runInDisplayThreadAsync(new DisplayThreadRunnable() {

			@Override
			public void runWithDisplay(Display display) throws Exception {
				init();
				out.println(t.getClass().getName());
				for (StackTraceElement stack : t.getStackTrace()) {
					out.println("\t at " + stack.toString()); //$NON-NLS-1$
				}
			}

		});
	}

}

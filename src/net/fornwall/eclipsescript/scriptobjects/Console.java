package net.fornwall.eclipsescript.scriptobjects;

import java.io.IOException;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.scripts.IScriptRuntime;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console {

	// just a marker superclass for enablement in console closer, see plugin.xml
	public static class ConsoleClass extends MessageConsole {
		public ConsoleClass(String name, ImageDescriptor imageDescriptor) {
			super(name, imageDescriptor);
		}
	}

	private MessageConsoleStream out;

	private MessageConsole console;
	private String name;

	public Console(IScriptRuntime runtime) {
		this.name = NLS.bind(Messages.scriptConsoleName, runtime.getStartingScript().getName());
	}

	public void clear() {
		if (console == null)
			return;

		console.clearConsole();
		try {
			out.close();
		} catch (final IOException e) {
			// not so interesting
		}
		out = console.newMessageStream();
	}

	private void init() {
		if (console == null) {
			console = new ConsoleClass(name, null);
			out = console.newMessageStream();
			ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
			IConsoleManager consoleManager = consolePlugin.getConsoleManager();
			consoleManager.addConsoles(new IConsole[] { console });
			consoleManager.showConsoleView(console);
		}
	}

	public void print(final String msg) {
		init();
		out.print(msg);
	}

	public void println(final String msg) {
		init();
		out.println(msg);
	}
}

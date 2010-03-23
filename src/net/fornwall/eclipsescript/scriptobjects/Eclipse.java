package net.fornwall.eclipsescript.scriptobjects;

import net.fornwall.eclipsescript.scripts.IScriptRuntime;

public class Eclipse {

	public static final String VARIABLE_NAME = "eclipse"; //$NON-NLS-1$

	private final Console console;
	private final Editors editors;
	private final Resources resources;
	private final Runtime runtime;
	private final Window window;

	public Eclipse(IScriptRuntime scriptRuntime) {
		this.console = new Console(scriptRuntime);
		this.editors = new Editors();
		this.resources = new Resources(scriptRuntime);
		this.runtime = new Runtime(scriptRuntime);
		this.window = new Window();
	}

	public Console getConsole() {
		return console;
	}

	public Editors getEditors() {
		return editors;
	}

	public Resources getResources() {
		return resources;
	}

	public Runtime getRuntime() {
		return runtime;
	}

	public Window getWindow() {
		return window;
	}

}

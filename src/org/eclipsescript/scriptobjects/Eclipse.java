package org.eclipsescript.scriptobjects;

import org.eclipsescript.scripts.IScriptRuntime;

public class Eclipse {

	public static final String VARIABLE_NAME = "eclipse"; //$NON-NLS-1$

	private final Console console;
	private final Editors editors;
	private final Resources resources;
	private final Runtime runtime;
	private final Window window;
	private final Xml xml;

	public Eclipse(IScriptRuntime scriptRuntime) {
		this.console = new Console(scriptRuntime);
		this.editors = new Editors();
		this.resources = new Resources(scriptRuntime);
		this.runtime = new Runtime(scriptRuntime);
		this.window = new Window();
		this.xml = new Xml(resources);
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

	public Xml getXml() {
		return xml;
	}
}

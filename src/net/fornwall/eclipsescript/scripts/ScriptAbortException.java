package net.fornwall.eclipsescript.scripts;

public class ScriptAbortException extends ScriptException {

	private static final long serialVersionUID = 1L;

	public ScriptAbortException(String message, Exception cause, int lineNumber) {
		super(message, cause, lineNumber, false);
	}

}

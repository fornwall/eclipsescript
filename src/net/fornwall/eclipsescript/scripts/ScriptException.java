package net.fornwall.eclipsescript.scripts;

public class ScriptException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int lineNumber;

	/**
	 * @param showStackTrace
	 *            not used at the moment, it could perhaps always be useful with stack trace?
	 */
	public ScriptException(String message, Throwable cause, int lineNumber, boolean showStackTrace) {
		super(message, cause);
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isShowStackTrace() {
		return true;
	}
}

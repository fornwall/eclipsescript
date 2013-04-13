package org.eclipsescript.scripts;

public class ScriptException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int lineNumber;
	private final String scriptStackTrace;
	private final String sourceName;

	/**
	 * @param scriptStackTrace
	 * @param showStackTrace
	 *            not used at the moment, it could perhaps always be useful with stack trace?
	 */
	public ScriptException(String message, Throwable cause, String sourceName, int lineNumber, String scriptStackTrace,
			boolean showStackTrace) {
		super(message, cause);
		this.lineNumber = lineNumber;
		this.sourceName = sourceName;
		this.scriptStackTrace = scriptStackTrace;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getScriptStackTrace() {
		return scriptStackTrace;
	}

	public String getSourceName() {
		return sourceName;
	}

	public boolean isShowStackTrace() {
		return true;
	}
}

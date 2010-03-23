package net.fornwall.eclipsescript.javascript;

import java.io.IOException;
import java.io.Reader;

import net.fornwall.eclipsescript.javascript.CustomContextFactory.CustomContext;
import net.fornwall.eclipsescript.scripts.IScriptRuntime;
import net.fornwall.eclipsescript.scripts.ScriptClassLoader;

import org.eclipse.core.resources.IFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

class JavascriptRuntime implements IScriptRuntime {

	/**
	 * Throw to indicate abnormal exception of script. Needs to extend error to prevent script from being able to catch
	 * exception.
	 */
	@SuppressWarnings("serial")
	static class DieError extends Error {
		public EvaluatorException evalException;

		public DieError(String dieMessage) {
			super(dieMessage);
			try {
				Context.reportError(dieMessage);
			} catch (EvaluatorException e) {
				evalException = e;
			}
		}
	}

	/**
	 * Throw to indicate normal exit of script. Needs to extend error to prevent script from being able to catch
	 * exception.
	 */
	@SuppressWarnings("serial")
	static class ExitError extends Error {
		// just a marker class
	}

	private final Context context;
	private final Scriptable topLevelScope;
	private final IFile startingScript;

	public JavascriptRuntime(CustomContext context, Scriptable topLevelScope, IFile startingScript) {
		this.context = context;
		this.topLevelScope = topLevelScope;
		this.startingScript = startingScript;

		context.jsRuntime = this;
	}

	@Override
	public void abortRunningScript(String errorMessage) {
		throw new DieError(errorMessage);
	}

	@Override
	public void evaluate(Reader reader, String sourceName) throws IOException {
		Scriptable fileScope = context.newObject(topLevelScope);
		context.evaluateReader(fileScope, reader, sourceName, 1, null);
	}

	@Override
	public void exitRunningScript() {
		throw new ExitError();
	}

	@Override
	public ScriptClassLoader getScriptClassLoader() {
		return (ScriptClassLoader) context.getApplicationClassLoader();
	}

	@Override
	public IFile getStartingScript() {
		return startingScript;
	}

}

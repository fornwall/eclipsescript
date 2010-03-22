package net.fornwall.eclipsescript.javascript;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.fornwall.eclipsescript.scriptobjects.Eclipse;
import net.fornwall.eclipsescript.scripts.ScriptAbortException;
import net.fornwall.eclipsescript.scripts.ScriptException;
import net.fornwall.eclipsescript.scripts.ScriptLanguageSupport;
import net.fornwall.eclipsescript.scripts.ScriptMetadata;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class JavascriptHandler implements ScriptLanguageSupport {

	static EvaluatorException getCurrentScriptInfo(String message) {
		try {
			Context.reportError(message);
			return null;
		} catch (EvaluatorException e) {
			return e;
		}
	}

	@Override
	public void executeScript(ScriptMetadata script) {
		CustomContextFactory.init();

		Context context = Context.enter();
		try {
			ScriptableObject scope = new ImporterTopLevel(context);
			JavascriptRuntime jsRuntime = new JavascriptRuntime(context, scope, script.getFile());

			Eclipse eclipseJavaObject = new Eclipse(jsRuntime);
			Object eclipseJsObject = Context.javaToJS(eclipseJavaObject, scope);
			ScriptableObject.putConstProperty(scope, Eclipse.VARIABLE_NAME, eclipseJsObject);

			Reader reader = null;
			try {
				reader = new InputStreamReader(script.getFile().getContents(), script.getFile().getCharset());
				jsRuntime.evaluate(reader, script.getFile().getName());
			} catch (ExitException e) {
				// do nothing
			} catch (DieException e) {
				throw new ScriptAbortException(e.getMessage(), e.evalException, e.evalException.lineNumber());
			} catch (RhinoException e) {
				boolean showStackTrace = (e instanceof WrappedException);
				Throwable cause = showStackTrace ? ((WrappedException) e).getCause() : e;
				throw new ScriptException(e.getMessage(), cause, e.lineNumber(), showStackTrace);
			} catch (Exception e) {
				throw (e instanceof RuntimeException) ? ((RuntimeException) e) : new RuntimeException(e);
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		} finally {
			Context.exit();
		}
	}

	/**
	 * Throw to indicate normal exit of script. Needs to extend error to prevent script from being able to catch
	 * exception.
	 */
	@SuppressWarnings("serial")
	static class ExitException extends Error {
		// just a marker class
	}

	/**
	 * Throw to indicate abnormal exception of script. Needs to extend error to prevent script from being able to catch
	 * exception.
	 */
	@SuppressWarnings("serial")
	private static class DieException extends Error {
		public EvaluatorException evalException;

		public DieException(String dieMessage) {
			super(dieMessage);
			evalException = JavascriptHandler.getCurrentScriptInfo(dieMessage);
		}
	}

	public static void exitRunningScript() {
		throw new ExitException();
	}

	public static void dieRunningScript(String message) {
		throw new DieException(message);
	}

}

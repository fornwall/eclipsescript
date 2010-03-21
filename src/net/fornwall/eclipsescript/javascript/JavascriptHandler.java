package net.fornwall.eclipsescript.javascript;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.fornwall.eclipsescript.scriptobjects.Console;
import net.fornwall.eclipsescript.scriptobjects.Editors;
import net.fornwall.eclipsescript.scriptobjects.Resources;
import net.fornwall.eclipsescript.scriptobjects.Runtime;
import net.fornwall.eclipsescript.scriptobjects.Utils;
import net.fornwall.eclipsescript.scriptobjects.Window;
import net.fornwall.eclipsescript.scripts.ScriptAbortException;
import net.fornwall.eclipsescript.scripts.ScriptException;
import net.fornwall.eclipsescript.scripts.ScriptLanguageSupport;
import net.fornwall.eclipsescript.scripts.ScriptMetadata;

import org.eclipse.core.resources.IProject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
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

			Scriptable eclipseObject = context.newObject(scope);
			ScriptableObject.putConstProperty(scope, "eclipse", eclipseObject);

			Console console = new Console(jsRuntime);
			ScriptableObject.putProperty(eclipseObject, "console", Context.javaToJS(console, scope));

			Window window = new Window();
			ScriptableObject.putProperty(eclipseObject, "window", Context.javaToJS(window, scope));

			Resources resources = new Resources(script.getFile().getProject());
			ScriptableObject.putProperty(eclipseObject, "resources", Context.javaToJS(resources, scope));

			Editors editors = new Editors();
			ScriptableObject.putProperty(eclipseObject, "editors", Context.javaToJS(editors, scope));

			Utils utils = new Utils();
			ScriptableObject.putProperty(eclipseObject, "utils", Context.javaToJS(utils, scope));

			Runtime runtime = new Runtime(jsRuntime);
			ScriptableObject.putProperty(eclipseObject, "runtime", Context.javaToJS(runtime, scope));

			Reader reader = null;
			try {
				reader = new InputStreamReader(script.getFile().getContents(), script.getFile().getCharset());
				IProject scriptProject = script.getFile().getProject();
				jsRuntime.evaluate(reader, scriptProject, script.getFile().getName());
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

	@Override
	public String scriptFileExtension() {
		return "eclipse.js";
	}

}

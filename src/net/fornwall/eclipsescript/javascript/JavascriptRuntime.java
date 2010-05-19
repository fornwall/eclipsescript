package net.fornwall.eclipsescript.javascript;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Callable;

import net.fornwall.eclipsescript.javascript.CustomContextFactory.CustomContext;
import net.fornwall.eclipsescript.scripts.IScriptRuntime;
import net.fornwall.eclipsescript.scripts.ScriptClassLoader;
import net.fornwall.eclipsescript.scripts.ScriptException;
import net.fornwall.eclipsescript.scripts.ScriptMetadata;
import net.fornwall.eclipsescript.scripts.ScriptStore;
import net.fornwall.eclipsescript.util.JavaUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IJobRunnable;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;

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

	final Context context;
	final ScriptMetadata script;
	final Scriptable topLevelScope;

	public JavascriptRuntime(CustomContext context, Scriptable topLevelScope, ScriptMetadata script) {
		this.context = context;
		this.topLevelScope = topLevelScope;
		this.script = script;

		context.jsRuntime = this;
	}

	@Override
	public void abortRunningScript(String errorMessage) {
		throw new DieError(errorMessage);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T adaptTo(Object object, Class<T> clazz) {
		if (object == null)
			return null;
		if (clazz.isInstance(object))
			return (T) object;
		if (clazz == IJobRunnable.class) {
			if (!(object instanceof BaseFunction))
				return null;

			final BaseFunction function = (BaseFunction) object;
			return (T) new IJobRunnable() {
				@Override
				public IStatus run(final IProgressMonitor monitor) {
					Object functionReturnValue = context.getFactory().call(new ContextAction() {
						@Override
						public Object run(final Context cx) {
							return ScriptStore.executeRunnableWhichMayThrowScriptException(script,
									new Callable<Object>() {
										@Override
										public Object call() throws Exception {
											final Object[] arguments = new Object[] { monitor };
											try {
												return function.call(cx, topLevelScope, topLevelScope, arguments);
											} catch (Throwable t) {
												handleExceptionFromScriptRuntime(t);
												return null;
											}
										}
									});
						}
					});
					if (functionReturnValue instanceof IStatus) {
						return (IStatus) functionReturnValue;
					} else if (functionReturnValue instanceof Boolean) {
						return (Boolean.TRUE.equals(functionReturnValue)) ? Status.OK_STATUS : Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
		}
		return null;
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
		return script.getFile();
	}

	public void handleExceptionFromScriptRuntime(Throwable err) {
		if (err instanceof ExitError) {
			// do nothing, just exit quietly due to eclipse.runtime.exit() call
		} else if (err instanceof DieError) {
			DieError e = (DieError) err;
			throw new ScriptException(e.getMessage(), e.evalException, e.evalException.lineNumber(), false);
		} else if (err instanceof RhinoException) {
			RhinoException e = (RhinoException) err;
			boolean showStackTrace = (e instanceof WrappedException);
			Throwable cause = showStackTrace ? ((WrappedException) e).getCause() : e;
			throw new ScriptException(e.getMessage(), cause, e.lineNumber(), showStackTrace);
		} else {
			if (err instanceof Error) {
				throw (Error) err;
			}
			throw JavaUtils.asRuntime(err);
		}
	}
}

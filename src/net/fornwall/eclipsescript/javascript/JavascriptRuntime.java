package net.fornwall.eclipsescript.javascript;

import java.io.IOException;
import java.io.Reader;

import net.fornwall.eclipsescript.javascript.CustomContextFactory.CustomContext;
import net.fornwall.eclipsescript.scripts.IScriptRuntime;
import net.fornwall.eclipsescript.scripts.ScriptClassLoader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IJobRunnable;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
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

	final Context context;
	private final IFile startingScript;
	final Scriptable topLevelScope;

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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T adaptTo(Object object, Class<T> clazz) {
		if (object == null)
			return null;
		if (clazz.isInstance(object))
			return (T) object;
		if (clazz == IJobRunnable.class) {
			if (object instanceof BaseFunction) {
				final BaseFunction function = (BaseFunction) object;
				return (T) new IJobRunnable() {
					@Override
					public IStatus run(final IProgressMonitor monitor) {
						Object functionReturnValue = context.getFactory().call(new ContextAction() {
							@Override
							public Object run(Context cx) {
								Object[] arguments = new Object[] { monitor };
								// FIXME: topLevelScope correct here?
								return function.call(cx, topLevelScope, topLevelScope, arguments);
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
		return startingScript;
	}

}

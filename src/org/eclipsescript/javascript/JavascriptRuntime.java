package org.eclipsescript.javascript;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IJobRunnable;
import org.eclipsescript.javascript.CustomContextFactory.CustomContext;
import org.eclipsescript.rhino.javascript.BaseFunction;
import org.eclipsescript.rhino.javascript.Context;
import org.eclipsescript.rhino.javascript.ContextAction;
import org.eclipsescript.rhino.javascript.EvaluatorException;
import org.eclipsescript.rhino.javascript.RhinoException;
import org.eclipsescript.rhino.javascript.Scriptable;
import org.eclipsescript.rhino.javascript.WrappedException;
import org.eclipsescript.scripts.IScriptRuntime;
import org.eclipsescript.scripts.MarkerManager;
import org.eclipsescript.scripts.ScriptClassLoader;
import org.eclipsescript.scripts.ScriptException;
import org.eclipsescript.scripts.ScriptMetadata;
import org.eclipsescript.scripts.ScriptStore;
import org.eclipsescript.util.JavaUtils;

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

	final CustomContext context;
	final InheritableThreadLocal<IFile> currentFile = new InheritableThreadLocal<IFile>();
	final ScriptMetadata script;
	final Scriptable topLevelScope;

	public JavascriptRuntime(CustomContext context, Scriptable topLevelScope, ScriptMetadata script) {
		this.context = context;
		this.topLevelScope = topLevelScope;
		this.script = script;
		setExecutingFile(script.getFile());

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
	public void disableTimeout() {
		context.useTimeout = false;
	}

	@Override
	public void evaluate(IFile file, boolean nested) throws IOException {
		// Cleanup eventual error markers from last run of this file since it may now be fixed - if an error remains it
		// will be re-added later when it fails again:
		MarkerManager.clearMarkers(file);

		InputStreamReader reader = null;
		IFile previousFile = getExecutingFile();
		try {
			setExecutingFile(file);
			reader = new InputStreamReader(file.getContents(true), file.getCharset());
			String sourceName = file.getFullPath().toPortableString();
			Scriptable fileScope = nested ? topLevelScope : context.newObject(topLevelScope);
			context.evaluateReader(fileScope, reader, sourceName, 1, null);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		} finally {
			setExecutingFile(previousFile);
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	public void exitRunningScript() {
		throw new ExitError();
	}

	@Override
	public IFile getExecutingFile() {
		return currentFile.get();
	}

	@Override
	public ScriptClassLoader getScriptClassLoader() {
		return (ScriptClassLoader) context.getApplicationClassLoader();
	}

	public void handleExceptionFromScriptRuntime(Throwable err) {
		if (err instanceof ExitError) {
			// do nothing, just exit quietly due to eclipse.runtime.exit() call
		} else if (err instanceof DieError) {
			DieError e = (DieError) err;
			RhinoException re = e.evalException;
			throw new ScriptException(e.getMessage(), re, re.sourceName(), re.lineNumber(), re.getScriptStackTrace(),
					false);
		} else if (err instanceof RhinoException) {
			RhinoException re = (RhinoException) err;
			boolean showStackTrace = (re instanceof WrappedException);
			Throwable cause = showStackTrace ? ((WrappedException) re).getCause() : re;
			throw new ScriptException(re.getMessage(), cause, re.sourceName(), re.lineNumber(),
					re.getScriptStackTrace(), showStackTrace);
		} else {
			if (err instanceof Error) {
				throw (Error) err;
			}
			throw JavaUtils.asRuntime(err);
		}
	}

	@Override
	public void setExecutingFile(IFile file) {
		currentFile.set(file);
	}
}

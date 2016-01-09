package org.eclipsescript.scriptobjects;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IJobRunnable;
import org.eclipsescript.core.Activator;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.scripts.IScriptRuntime;
import org.eclipsescript.util.EclipseUtils;

/**
 * Object which is put under <code>eclipse.runtime</code> in the script scope containing methods for manipulating the
 * currently executing scripts runtime.
 */
public class Runtime {

	private static final Map<String, Object> globals = new HashMap<String, Object>();
	private final IScriptRuntime scriptRuntime;

	public Runtime(IScriptRuntime scriptRuntime) {
		this.scriptRuntime = scriptRuntime;
	}

	public void asyncExec(Runnable runnable) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(runnable);
	}

	public void die(String message) {
		scriptRuntime.abortRunningScript(message);
	}

	public void disableTimeout() {
		scriptRuntime.disableTimeout();
	}

	public void exec(String command) {
		try {
			java.lang.Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			Activator.logError(e);
		}
	}

	public void exit() {
		scriptRuntime.exitRunningScript();
	}

	public synchronized Object getGlobal(String key) {
		return globals.get(key);
	}

	public Shell getShell() {
		return EclipseUtils.getWindowShell();
	}

	public void include(Object... includes) throws Exception {
		for (Object includeObject : includes) {
			IFile fileToInclude;
			if (includeObject instanceof IFile) {
				fileToInclude = (IFile) includeObject;
			} else {
				String includeStringPath = (String) includeObject;
				Path includePath = new Path(includeStringPath);
				IFile executingScriptFile = scriptRuntime.getExecutingFile();
				IContainer startingScriptContainer = executingScriptFile.getParent();
				if (includePath.isAbsolute()) {
					fileToInclude = startingScriptContainer.getWorkspace().getRoot().getFile(includePath);
				} else {
					fileToInclude = startingScriptContainer.getFile(includePath);
				}
			}
			if (!fileToInclude.exists())
				scriptRuntime.abortRunningScript(
						Messages.fileToIncludeDoesNotExist + fileToInclude.getFullPath().toOSString());
			scriptRuntime.evaluate(fileToInclude, true);
		}
	}

	public synchronized void putGlobal(String key, Object value) {
		globals.put(key, value);
	}

	public void schedule(final Object objectToSchedule) {
		final IJobRunnable runnable = scriptRuntime.adaptTo(objectToSchedule, IJobRunnable.class);
		if (runnable == null)
			throw new IllegalArgumentException(Messages.notPossibleToScheduleObject + objectToSchedule);
		String jobName = NLS.bind(Messages.scriptBackgroundJobName, scriptRuntime.getExecutingFile().getName());
		final IScriptRuntime runtime = scriptRuntime;
		final IFile executingFile = scriptRuntime.getExecutingFile();
		Job job = new WorkspaceJob(jobName) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				runtime.setExecutingFile(executingFile);
				return runnable.run(monitor);
			}
		};
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}

	public void syncExec(Runnable runnable) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().syncExec(runnable);
	}
}

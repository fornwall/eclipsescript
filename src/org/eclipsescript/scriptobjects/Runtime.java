package org.eclipsescript.scriptobjects;

import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IJobRunnable;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.scripts.IScriptRuntime;
import org.eclipsescript.util.EclipseUtils;

/**
 * Object which is put under <code>eclipse.runtime</code> in the script scope containing methods for manipulating the
 * currently executing scripts runtime.
 */
public class Runtime {

	private final IScriptRuntime scriptRuntime;

	public Runtime(IScriptRuntime scriptRuntime) {
		this.scriptRuntime = scriptRuntime;
	}

	public void die(String message) {
		scriptRuntime.abortRunningScript(message);
	}

	public void exit() {
		scriptRuntime.exitRunningScript();
	}

	public Shell getShell() {
		return EclipseUtils.getWindowShell();
	}

	public void include(Object... includes) throws Exception {
		for (Object includeObject : includes) {
			String includeStringPath = (String) includeObject;
			Path includePath = new Path(includeStringPath);
			IFile startingScript = scriptRuntime.getStartingScript();
			IContainer startingScriptCounter = startingScript.getParent();
			IFile fileToInclude;
			if (includePath.isAbsolute()) {
				fileToInclude = startingScriptCounter.getWorkspace().getRoot().getFile(includePath);
			} else {
				fileToInclude = startingScriptCounter.getFile(includePath);
			}
			if (!fileToInclude.exists())
				scriptRuntime.abortRunningScript(Messages.fileToIncludeDoesNotExist
						+ fileToInclude.getFullPath().toOSString());

			Reader reader = new InputStreamReader(fileToInclude.getContents(), fileToInclude.getCharset());
			try {
				String name = fileToInclude.getFullPath().toPortableString();
				scriptRuntime.evaluate(reader, name, true);
			} finally {
				reader.close();
			}
		}
	}

	public void schedule(final Object objectToSchedule) {
		final IJobRunnable runnable = scriptRuntime.adaptTo(objectToSchedule, IJobRunnable.class);
		if (runnable == null)
			throw new IllegalArgumentException(Messages.notPossibleToScheduleObject + objectToSchedule);
		String jobName = NLS.bind(Messages.scriptBackgroundJobName, scriptRuntime.getStartingScript().getName());
		Job job = new WorkspaceJob(jobName) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				return runnable.run(monitor);
			}
		};
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}
}

package net.fornwall.eclipsescript.scriptobjects;

import java.io.InputStreamReader;
import java.io.Reader;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.scripts.IScriptRuntime;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IJobRunnable;

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
			IFile fileToInclude = startingScriptCounter.getFile(includePath);
			if (!fileToInclude.exists())
				scriptRuntime.abortRunningScript(Messages.fileToIncludeDoesNotExist
						+ fileToInclude.getFullPath().toOSString());

			Reader reader = new InputStreamReader(fileToInclude.getContents(), fileToInclude.getCharset());
			try {
				scriptRuntime.evaluate(reader, fileToInclude.getName());
			} finally {
				reader.close();
			}
		}
	}

	public void schedule(final IJobRunnable runnable) {
		String jobName = NLS.bind(Messages.scriptBackgroundJobName, scriptRuntime.getStartingScript().getName());
		Job job = new Job(jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return runnable.run(monitor);
			}
		};
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}

}

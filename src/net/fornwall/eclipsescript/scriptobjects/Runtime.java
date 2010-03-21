package net.fornwall.eclipsescript.scriptobjects;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import net.fornwall.eclipsescript.core.Activator;
import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.scripts.IScriptRuntime;
import net.fornwall.eclipsescript.scripts.ScriptClassLoader;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IJobRunnable;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Object which is put under <code>eclipse.runtime</code> in the script scope containing methods for manipulating the
 * currently executing scripts runtime.
 */
public class Runtime {

	private final IScriptRuntime scriptRuntime;

	public Runtime(IScriptRuntime scriptRuntime) {
		this.scriptRuntime = scriptRuntime;
	}

	private void addClassLoader(ClassLoader loader) {
		ScriptClassLoader cl = scriptRuntime.getScriptClassLoader();
		cl.addLoader(loader);
	}

	public void include(Object... includes) throws Exception {
		for (Object includeObject : includes) {
			String includeStringPath = (String) includeObject;
			Path includePath = new Path(includeStringPath);
			IFile startingScript = scriptRuntime.getStartingScript();
			IContainer startingScriptCounter = startingScript.getParent();
			IFile fileToInclude = startingScriptCounter.getFile(includePath);
			if (!fileToInclude.exists())
				scriptRuntime.abortRunningScript("File does not exist: " + fileToInclude.getFullPath().toOSString());

			Reader reader = new InputStreamReader(fileToInclude.getContents(), fileToInclude.getCharset());
			try {
				scriptRuntime.evaluate(reader, fileToInclude.getName());
			} finally {
				reader.close();
			}
		}
	}

	public void addClassLoader(Object o) throws MalformedURLException {
		ScriptClassLoader cl = scriptRuntime.getScriptClassLoader();

		ClassLoader loader = null;
		if (o instanceof IFile) {
			IFile file = (IFile) o;
			if (!file.exists())
				throw new IllegalArgumentException("File " + file.getProjectRelativePath() + " does not exist");
			URL url = new URL(file.getLocationURI().toURL().toString());
			loader = new URLClassLoader(new URL[] { url }, cl);
		} else if (o instanceof IFolder) {
			IFolder folder = (IFolder) o;
			if (!folder.exists())
				throw new IllegalArgumentException("Folder " + folder.getProjectRelativePath() + " does not exist");
			URL url = new URL(folder.getLocationURI().toURL().toString() + "/");
			loader = new URLClassLoader(new URL[] { url }, cl);
		} else if (o instanceof File) {
			File file = (File) o;
			if (!file.exists())
				throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
			if (file.isDirectory()) {
				loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, cl);
			} else {
				if (!file.getName().endsWith(".jar"))
					throw new IllegalArgumentException(file.getAbsolutePath() + " is not a jar file");
				loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, cl);
			}
		} else if (o instanceof ClassLoader) {
			loader = (ClassLoader) o;
		} else {
			throw new IllegalArgumentException("Invalid object to add as classloader: "
					+ (o == null ? "null" : (o + " of class " + o.getClass())));
		}

		addClassLoader(loader);
	}

	public void exit() {
		scriptRuntime.exitRunningScript();
	}

	public void die(String message) {
		scriptRuntime.abortRunningScript(message);
	}

	public void require(String name) {
		boolean pluginLoaded = load(name) != null;
		if (!pluginLoaded) {
			scriptRuntime.abortRunningScript("Cannot load required plugin '" + name + "'");
		}
	}

	public Bundle load(String name) {
		for (Bundle bundle : Activator.getDefault().getBundle().getBundleContext().getBundles()) {
			if (bundle.getSymbolicName().equals(name)) {
				ScriptClassLoader cl = scriptRuntime.getScriptClassLoader();
				cl.addBundle(bundle);

				return bundle;
			}
		}
		return null;
	}

	public Version version(String bundleName) {
		Bundle bundle = load(bundleName);
		if (bundle == null)
			return null;
		String versionString = (String) bundle.getHeaders().get("Bundle-Version");
		return Version.parseVersion(versionString);
	}

	public Shell getShell() {
		return EclipseUtils.getWindowShell();
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

package net.fornwall.eclipsescript.core;

import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.CHANGED;
import static org.eclipse.core.resources.IResourceDelta.CONTENT;
import static org.eclipse.core.resources.IResourceDelta.MOVED_FROM;
import static org.eclipse.core.resources.IResourceDelta.MOVED_TO;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;
import static org.eclipse.core.resources.IResourceDelta.REPLACED;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import net.fornwall.eclipsescript.scripts.ScriptStore;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;

/**
 * Use as:
 * 
 * <pre>
 * final ScriptFilesChangeListener listener = new ScriptFilesChangeListener();
 * getWorkspace().addResourceChangeListener(listener, POST_CHANGE);
 * </pre>
 */
public class ScriptFilesChangeListener implements IResourceChangeListener {

	public static String FILE_SUFFIX = ".eclipse.js";

	public static boolean isEclipseScript(IFile file) {
		return file.getFullPath().toString().endsWith(FILE_SUFFIX);
	}

	void processNewOrChangedScript(final IFile file) {
		ScriptStore.addScript(file);
	}

	public void rescanAllFiles() {
		ScriptStore.clearScripts();
		final IWorkspace workspace = getWorkspace();
		for (final IProject project : workspace.getRoot().getProjects()) {
			final IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(final IResource resource) throws CoreException {
					if (!(resource instanceof IFile))
						return true;
					if (resource.isDerived())
						return false;
					final IFile file = (IFile) resource;
					if (isEclipseScript(file))
						processNewOrChangedScript(file);
					return true;
				}

			};
			if (project.isOpen()) {
				try {
					project.accept(visitor);
				} catch (final CoreException x) {
					// ignore folders we cannot access
					Activator.logError(x);
				}
			}
		}
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

			public boolean visit(final IResourceDelta delta) {
				if (!(delta.getResource() instanceof IFile))
					return true;
				final IFile file = (IFile) delta.getResource();
				if (file.isDerived())
					return false;

				final String fullPath = delta.getFullPath().toString();
				if (isEclipseScript(file)) {
					switch (delta.getKind()) {
					case ADDED:
						processNewOrChangedScript(file);
						break;
					case REMOVED:
						ScriptStore.removeScript(fullPath);
						break;
					case CHANGED:
						if ((delta.getFlags() & MOVED_FROM) != 0) {
							ScriptStore.removeScript(delta.getMovedFromPath().toString());
							processNewOrChangedScript(file);
						}
						if ((delta.getFlags() & MOVED_TO) != 0) {
							processNewOrChangedScript(file);
						}
						if ((delta.getFlags() & REPLACED) != 0) {
							processNewOrChangedScript(file);
						}
						if ((delta.getFlags() & CONTENT) != 0) {
							processNewOrChangedScript(file);
						}
						break;
					}
				}
				return true;
			}

		};
		try {
			event.getDelta().accept(visitor);
		} catch (final CoreException x) {
			Activator.logError(x);
		}
	}
}

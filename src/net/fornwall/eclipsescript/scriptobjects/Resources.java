package net.fornwall.eclipsescript.scriptobjects;

import static java.util.regex.Pattern.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fornwall.eclipsescript.util.JavaUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Resources {

	private final IProject scriptProject;

	public Resources(IProject scriptProject) {
		this.scriptProject = scriptProject;
	}

	public IFile[] filesMatching(final String patternString, IResource startingPoint) {
		final Pattern pattern = compile(patternString);
		final List<IFile> result = new ArrayList<IFile>();
		try {
			walk(startingPoint, pattern, result);
		} catch (final CoreException x) {
			// ignore Eclipse internal errors
		}
		return result.toArray(new IFile[result.size()]);
	}

	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	// note that exists() should be called to determine existence
	public IProject getProject(String projectName) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	/** Get the currently selected project. */
	public IProject getCurrentProject() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();
		if (activePage == null)
			return null;
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null)
			return null;
		IEditorInput editorInput = activeEditor.getEditorInput();
		if (editorInput == null)
			return null;
		IResource resource = (IResource) editorInput.getAdapter(IResource.class);
		if (resource == null)
			return null;
		return resource.getProject();
	}

	/** Get the project of the currently executing script. */
	public IProject getScriptProject() {
		return scriptProject;
	}

	public String readFile(IFile file) throws CoreException {
		return JavaUtils.readAllToStringAndClose(file.getContents());
	}

	private void walk(final IResource resource, final Pattern pattern, final Collection<IFile> result)
			throws CoreException {
		if (resource instanceof IProject) {
			final IProject project = (IProject) resource;
			if (!project.isOpen())
				return;
			final IResource[] children = project.members();
			for (final IResource resource2 : children)
				walk(resource2, pattern, result);
		} else if (resource instanceof IContainer) {
			final IResource[] children = ((IContainer) resource).members();
			for (final IResource resource2 : children) {
				walk(resource2, pattern, result);
			}
		} else if (resource instanceof IFile) {
			final String path = resource.getFullPath().toString();
			final Matcher match = pattern.matcher(path);
			if (match.matches())
				result.add((IFile) resource);
		}
	}
}

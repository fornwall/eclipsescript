package org.eclipsescript.scripts;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipsescript.core.Activator;

public class MarkerManager {

	private static final String SCRIPT_PROBLEM_MARKER_TYPE = "org.eclipsescript.scriptproblemmarker"; //$NON-NLS-1$

	public static void addMarker(IFile file, ScriptException error) {
		try {
			IMarker m = file.createMarker(SCRIPT_PROBLEM_MARKER_TYPE);
			if (error.getLineNumber() > 0) {
				m.setAttribute(IMarker.LINE_NUMBER, error.getLineNumber());
			}
			m.setAttribute(IMarker.MESSAGE, error.getMessage());
			m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			// TODO: ? "If we indicated additional information in the marker for IMarker.CHAR_START and
			// IMarker.CHAR_END, the
			// editor will also draw a red squiggly line under the offending problem"
		} catch (CoreException e) {
			Activator.logError(e);
		}
	}

	public static void clearMarkers(IFile file) {
		try {
			file.deleteMarkers(SCRIPT_PROBLEM_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			Activator.logError(e);
		}
	}

}

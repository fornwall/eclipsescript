package net.fornwall.eclipsescript.scripts;

import java.util.Collections;
import java.util.Map;

import net.fornwall.eclipsescript.javascript.JavaScriptLanguageSupport;

import org.eclipse.core.resources.IFile;

public class ScriptLanguageHandler {

	public static Map<String, ? extends IScriptLanguageSupport> getLanguageSupports() {
		return Collections.singletonMap("js", new JavaScriptLanguageSupport()); //$NON-NLS-1$
	}

	static IScriptLanguageSupport getScriptSupport(IFile file) {
		String fileName = file.getName();
		int index = fileName.lastIndexOf(".eclipse."); //$NON-NLS-1$
		if (index == -1)
			return null;
		String fileExtension = fileName.substring(index + 9);
		return getLanguageSupports().get(fileExtension);
	}

	public static boolean isEclipseScriptFile(IFile file) {
		return getScriptSupport(file) != null;
	}
}

package net.fornwall.eclipsescript.scripts;

import net.fornwall.eclipsescript.javascript.JavaScriptLanguageSupport;

import org.eclipse.core.resources.IFile;

public class ScriptLanguageHandler {

	public static IScriptLanguageSupport getScriptSupport(@SuppressWarnings("unused") IFile file) {
		// hard-coded to javascript support:
		IScriptLanguageSupport result = new JavaScriptLanguageSupport();
		return result;
	}

}

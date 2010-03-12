package net.fornwall.eclipsescript.scripts;

import net.fornwall.eclipsescript.javascript.JavascriptHandler;

import org.eclipse.core.resources.IFile;

public class ScriptLanguageHandler {

	public static ScriptLanguageSupport getScriptSupport(@SuppressWarnings("unused") IFile file) {
		// hard-coded to javascript support:
		ScriptLanguageSupport result = new JavascriptHandler();
		return result;
	}

}

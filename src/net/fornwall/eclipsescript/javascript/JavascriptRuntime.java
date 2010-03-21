package net.fornwall.eclipsescript.javascript;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import net.fornwall.eclipsescript.scripts.IScriptRuntime;
import net.fornwall.eclipsescript.scripts.ScriptClassLoader;

class JavascriptRuntime implements IScriptRuntime {

	private final Context context;
	private final Scriptable topLevelScope;
	private final IFile startingScript;

	public JavascriptRuntime(Context context, Scriptable topLevelScope, IFile startingScript) {
		this.context = context;
		this.topLevelScope = topLevelScope;
		this.startingScript = startingScript;
	}

	@Override
	public void evaluate(Reader reader, Object script, String sourceName) throws IOException {
		Scriptable fileScope = context.newObject(topLevelScope);
		context.evaluateReader(fileScope, reader, sourceName, 1, null);
	}

	@Override
	public IFile getStartingScript() {
		return startingScript;
	}

	@Override
	public ScriptClassLoader getScriptClassLoader() {
		return (ScriptClassLoader) context.getApplicationClassLoader();
	}

	@Override
	public void abortRunningScript(String errorMessage) {
		JavascriptHandler.dieRunningScript(errorMessage);
	}

	@Override
	public void exitRunningScript() {
		JavascriptHandler.exitRunningScript();
	}

}

package net.fornwall.eclipsescript.scripts;

import java.io.IOException;
import java.io.Reader;


import org.eclipse.core.resources.IFile;

public interface IScriptRuntime {

	public void evaluate(Reader reader, String sourceName) throws IOException;

	public IFile getStartingScript();

	public ScriptClassLoader getScriptClassLoader();

	public void abortRunningScript(String errorMessage);

	public void exitRunningScript();

}

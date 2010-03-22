package net.fornwall.eclipsescript.scripts;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;

public interface IScriptRuntime {

	public void abortRunningScript(String errorMessage);

	public void evaluate(Reader reader, String sourceName) throws IOException;

	public void exitRunningScript();

	public ScriptClassLoader getScriptClassLoader();

	public IFile getStartingScript();

}

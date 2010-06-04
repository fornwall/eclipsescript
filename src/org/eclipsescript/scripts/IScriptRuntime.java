package org.eclipsescript.scripts;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;

public interface IScriptRuntime {

	public void abortRunningScript(String errorMessage);

	public <T> T adaptTo(Object object, Class<T> clazz);

	public void evaluate(Reader reader, String sourceName) throws IOException;

	public void exitRunningScript();

	public ScriptClassLoader getScriptClassLoader();

	public IFile getStartingScript();
}

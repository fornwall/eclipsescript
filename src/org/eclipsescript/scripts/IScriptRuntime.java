package org.eclipsescript.scripts;

import java.io.IOException;

import org.eclipse.core.resources.IFile;

public interface IScriptRuntime {

	public void abortRunningScript(String errorMessage);

	public <T> T adaptTo(Object object, Class<T> clazz);

	public void evaluate(IFile file, boolean nested) throws IOException;

	public void exitRunningScript();

	public IFile getExecutingFile();

	public ScriptClassLoader getScriptClassLoader();

	public void setExecutingFile(IFile file);

}

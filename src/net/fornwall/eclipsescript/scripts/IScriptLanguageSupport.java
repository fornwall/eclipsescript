package net.fornwall.eclipsescript.scripts;

public interface IScriptLanguageSupport {

	/**
	 * Execute all scripts in the same contexts.
	 */
	public void executeScript(ScriptMetadata script);

}

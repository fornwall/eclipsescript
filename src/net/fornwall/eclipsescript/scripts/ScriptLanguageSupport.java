package net.fornwall.eclipsescript.scripts;


public interface ScriptLanguageSupport {

	/**
	 * Execute all scripts in the same contexts.
	 */
	public void executeScript(ScriptMetadata script);

	/**
	 * Return the file extension that this is a script handler for.
	 */
	public String scriptFileExtension();

}

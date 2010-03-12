package net.fornwall.eclipsescript.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Localization messages using the {@link NLS} system. The static initializer block will initialize the fields of this
 * class with values loaded from the messages properties file.
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "net.fornwall.eclipsescript.messages.messages"; //$NON-NLS-1$

	public static String internalErrorDialogDetails;
	public static String internalErrorDialogText;
	public static String internalErrorDialogTitle;
	public static String internalErrorWhenRunningScriptDialogTitle;
	public static String quickAccessPressKeyToShowAllMatches;
	public static String quickAccessStartTypingToFindMatches;
	public static String runScriptBeforeRunningLast;
	public static String scriptErrorWhenRunningScriptDialogText;
	public static String scriptErrorWhenRunningScriptDialogTitle;
	public static String scriptErrorWhenRunningScriptJumpToScriptButton;
	public static String scriptErrorWhenRunningScriptOkButton;
	public static String scriptShortcutConflictsWithExisting;
	public static String scriptShortcutConflictsWithExistingScript;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}

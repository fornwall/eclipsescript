package org.eclipsescript.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Localization messages using the {@link NLS} system. The static initializer block will initialize the fields of this
 * class with values loaded from the messages properties file.
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

	public static String cannotRunCurrentScriptText;
	public static String cannotRunCurrentScriptTitle;
	public static String clearMarkersJobName;
	public static String fileToIncludeDoesNotExist;
	public static String fileToReadDoesNotExist;
	public static String internalErrorDialogDetails;
	public static String internalErrorDialogText;
	public static String internalErrorDialogTitle;
	public static String noDocumentSelected;
	public static String noSelectionSelected;
	public static String noTextEditorSelected;
	public static String notPossibleToScheduleObject;
	public static String Resources_cannotReadFromObject;
	public static String runScriptBeforeRunningLast;
	public static String scriptAlertDialogTitle;
	public static String scriptBackgroundJobName;
	public static String scriptConfirmDialogTitle;
	public static String scriptConsoleName;
	public static String scriptErrorWhenRunningScriptDialogText;
	public static String scriptErrorWhenRunningScriptDialogTitle;
	public static String scriptErrorWhenRunningScriptJumpToScriptButton;
	public static String scriptErrorWhenRunningScriptOkButton;
	public static String scriptPromptDialogTitle;
	public static String scriptTimeout;
	public static String windowOpenArgumentNull;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}

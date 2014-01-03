package org.eclipsescript.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipsescript.preferences.messages"; //$NON-NLS-1$
	public static String EclipseScriptPreferencePage_0;
	public static String EclipseScriptPreferencePage_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

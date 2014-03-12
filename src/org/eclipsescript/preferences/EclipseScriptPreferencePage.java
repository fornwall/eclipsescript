package org.eclipsescript.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipsescript.core.Activator;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class EclipseScriptPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public EclipseScriptPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.EclipseScriptPreferencePage_0);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
	 * types of preferences. Each field editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new IntegerFieldEditor(PreferenceConstants.P_TIMEOUT, Messages.EclipseScriptPreferencePage_1,
				getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// nothing to do here
	}

}
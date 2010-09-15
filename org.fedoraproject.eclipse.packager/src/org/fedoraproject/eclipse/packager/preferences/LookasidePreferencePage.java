/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/

package org.fedoraproject.eclipse.packager.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.fedoraproject.eclipse.packager.PackagerPlugin;


/**
 * Specfile editor main preference page class.
 *
 */
public class LookasidePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	/**
	 * default constructor
	 */
	public LookasidePreferencePage() {
		super(GRID);
		setPreferenceStore(PackagerPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.LookasidePreferencesPageDescription);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public synchronized void init(IWorkbench workbench) {
		IPreferenceStore store = getPreferenceStore();
		// make sure we have defaults defined properly at any time
		String currDefault = store.getDefaultString(PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
		if (currDefault.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			store.setDefault(PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL, PreferencesConstants.DEFAULT_LOOKASIDE_DOWNLOAD_URL);
		}
		currDefault = store.getDefaultString(PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL);
		if (currDefault.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			store.setDefault(PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL, PreferencesConstants.DEFAULT_LOOKASIDE_UPLOAD_URL);
		}
		// If empty fill with defaults
		if (store.getString(PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL).equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			store.setValue(PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL, PreferencesConstants.DEFAULT_LOOKASIDE_DOWNLOAD_URL);
		}
		if (store.getString(PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL).equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			store.setValue(PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL, PreferencesConstants.DEFAULT_LOOKASIDE_UPLOAD_URL);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		/* Preference for setting the lookaside urls */
		StringFieldEditor lookasideUploadURLEditor = new StringFieldEditor(
				PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL, Messages.LookasideUploadURLLabel,
				parent);
		StringFieldEditor lookasideDownloadURLEditor = new StringFieldEditor(
				PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL, Messages.LookasideDownloadURLLabel,
				parent);
		addField(lookasideUploadURLEditor);
		addField(lookasideDownloadURLEditor);
	}
	
}

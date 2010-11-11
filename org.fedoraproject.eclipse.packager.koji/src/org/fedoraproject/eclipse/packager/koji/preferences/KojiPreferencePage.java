/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.fedoraproject.eclipse.packager.koji.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.fedoraproject.eclipse.packager.PackagerPlugin;


/**
 * Specfile editor main preference page class.
 *
 */
public class KojiPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	private StringFieldEditor kojiWebURLEditor;
	private StringFieldEditor kojiHubURLEditor;
	
	/**
	 * default constructor
	 */
	public KojiPreferencePage() {
		super(GRID);
		setPreferenceStore(PackagerPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.kojiPreferencesPage_description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public synchronized void init(IWorkbench workbench) {
		IPreferenceStore store = getPreferenceStore();
		// make sure we have defaults defined properly at any time
		String currDefault = store.getDefaultString(PreferencesConstants.PREF_KOJI_WEB_URL);
		if (currDefault.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			store.setDefault(PreferencesConstants.PREF_KOJI_WEB_URL, PreferencesConstants.DEFAULT_KOJI_WEB_URL);
		}
		currDefault = store.getDefaultString(PreferencesConstants.PREF_KOJI_HUB_URL);
		if (currDefault.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			store.setDefault(PreferencesConstants.PREF_KOJI_HUB_URL, PreferencesConstants.DEFAULT_KOJI_HUB_URL);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		/* Preference for setting the koji host */
		kojiWebURLEditor = new StringFieldEditor(
				PreferencesConstants.PREF_KOJI_WEB_URL, Messages.kojiPreferencesPage_kojiWebURLLabel,
				parent);
		kojiHubURLEditor = new StringFieldEditor(
				PreferencesConstants.PREF_KOJI_HUB_URL, Messages.kojiPreferencesPage_kojiHubURLLabel,
				parent);
		addField(kojiWebURLEditor);
		addField(kojiHubURLEditor);
	}
	
	/**
	 * Validate fields for sane values.
	 */
	@Override
	public void checkState() {
		super.checkState();
		if (kojiWebURLEditor.getStringValue() != null
				&& !kojiWebURLEditor.getStringValue().startsWith("http")) {
			setErrorMessage(Messages.kojiPreferencesPage_kojiWebURLInvalidMsg);
			setValid(false);
		} else if (kojiHubURLEditor.getStringValue() != null && !kojiHubURLEditor.getStringValue().startsWith("https://")) { // urls seem to be valid
			setErrorMessage(Messages.kojiPreferencesPage_kojiHubURLInvalidMsg);
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}
	
	/**
	 * Register validation listener for field editors.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
		}
	}

	
}

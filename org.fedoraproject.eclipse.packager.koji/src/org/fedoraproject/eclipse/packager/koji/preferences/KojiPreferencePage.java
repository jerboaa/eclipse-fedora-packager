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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
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
	
	/**
	 * default constructor
	 */
	public KojiPreferencePage() {
		super(GRID);
		setPreferenceStore(PackagerPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.KojiPreferencesPageDescription);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		/* Preference for setting the koji host */
		StringFieldEditor kojiWebURLEditor = new StringFieldEditor(
				PreferencesConstants.PREF_KOJI_WEB_URL, Messages.KojiWebURLLabel,
				parent);
		StringFieldEditor kojiHubURLEditor = new StringFieldEditor(
				PreferencesConstants.PREF_KOJI_HUB_URL, Messages.KojiHubURLLabel,
				parent);
		addField(kojiWebURLEditor);
		addField(kojiHubURLEditor);
	}
	
}

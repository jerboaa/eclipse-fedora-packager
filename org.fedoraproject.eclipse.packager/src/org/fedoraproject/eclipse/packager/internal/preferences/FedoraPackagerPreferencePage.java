/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/

package org.fedoraproject.eclipse.packager.internal.preferences;


import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

/**
 * Eclipse Fedora Packager main preference page.
 */
public class FedoraPackagerPreferencePage extends FieldEditorPreferencePage implements
	IWorkbenchPreferencePage {
	
	private static final int GROUP_SPAN = 2;
	private static final String HTTP_PREFIX = "http"; //$NON-NLS-1$
	private static final String HTTPS_PREFIX = "https://"; //$NON-NLS-1$
	 
	// Lookaside cache
	private StringFieldEditor lookasideUploadURLEditor;
	private StringFieldEditor lookasideDownloadURLEditor;
	// Koji
	private StringFieldEditor kojiWebURLEditor;
	private StringFieldEditor kojiHubURLEditor;
	
	/**
	 * default constructor
	 */
	public FedoraPackagerPreferencePage() {
		super(GRID);
		setPreferenceStore(PackagerPlugin.getDefault().getPreferenceStore());
		setDescription(FedoraPackagerText.FedoraPackagerPreferencePage_description);
	}
	
	/**
	 * Validate fields for sane values.
	 */
	@Override
	public void checkState() {
		super.checkState();
		// Upload URL has to be https
		if (lookasideUploadURLEditor.getStringValue() != null
				&& !lookasideUploadURLEditor.getStringValue().startsWith(HTTP_PREFIX)) {
			setErrorMessage(FedoraPackagerText.FedoraPackagerPreferencePage_invalidUploadURLMsg);
			setValid(false);
		} else if (lookasideDownloadURLEditor.getStringValue() != null &&
				!lookasideDownloadURLEditor.getStringValue().startsWith(HTTP_PREFIX)) {
			setErrorMessage(FedoraPackagerText.FedoraPackagerPreferencePage_invalidDownloadURLMsg);
			setValid(false);
		} else if (kojiWebURLEditor.getStringValue() != null
				&& !kojiWebURLEditor.getStringValue().startsWith(HTTP_PREFIX)) {
			setErrorMessage(FedoraPackagerText.FedoraPackagerPreferencePage_kojiWebURLInvalidMsg);
			setValid(false);
		} else if (kojiHubURLEditor.getStringValue() != null
				&& !kojiHubURLEditor.getStringValue().startsWith(HTTPS_PREFIX)) {
			setErrorMessage(FedoraPackagerText.FedoraPackagerPreferencePage_kojiHubURLInvalidMsg);
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
		}
	}
	
	@Override
	protected void createFieldEditors() {
		Composite composite = getFieldEditorParent();
		
		// General prefs
		Group generalGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		generalGroup.setText(FedoraPackagerText.FedoraPackagerPreferencePage_generalGroupName);
		addField(new BooleanFieldEditor(
				FedoraPackagerPreferencesConstants.PREF_DEBUG_MODE,
				FedoraPackagerText.FedoraPackagerPreferencePage_debugSwitchLabel,
				generalGroup));
		updateMargins(generalGroup);
		
		Group lookasideGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		lookasideGroup.setText(FedoraPackagerText.FedoraPackagerPreferencePage_lookasideGroupName);
		GridDataFactory.fillDefaults().grab(true, false).span(GROUP_SPAN, 1)
		.applyTo(lookasideGroup);
		/* Preference for setting the lookaside urls */
		lookasideUploadURLEditor = new StringFieldEditor(
				FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL,
				FedoraPackagerText.FedoraPackagerPreferencePage_lookasideUploadURLLabel,
				lookasideGroup);
		lookasideDownloadURLEditor = new StringFieldEditor(
				FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL,
				FedoraPackagerText.FedoraPackagerPreferencePage_lookasideDownloadURLLabel,
				lookasideGroup);
		// register change listener
		lookasideDownloadURLEditor.setPropertyChangeListener(this);
		lookasideUploadURLEditor.setPropertyChangeListener(this);
		// load defaults and/or set values
		lookasideDownloadURLEditor.load();
		lookasideUploadURLEditor.load();
		addField(lookasideUploadURLEditor);
		addField(lookasideDownloadURLEditor);
		updateMargins(lookasideGroup);
		
		Group kojiGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		kojiGroup.setText(FedoraPackagerText.FedoraPackagerPreferencePage_buildSystemGroupName);
		GridDataFactory.fillDefaults().grab(true, false).span(GROUP_SPAN, 1)
		.applyTo(kojiGroup);
		/* Preference for setting the koji host */
		kojiWebURLEditor = new StringFieldEditor(
				FedoraPackagerPreferencesConstants.PREF_KOJI_WEB_URL, FedoraPackagerText.FedoraPackagerPreferencePage_kojiWebURLLabel,
				kojiGroup);
		kojiHubURLEditor = new StringFieldEditor(
				FedoraPackagerPreferencesConstants.PREF_KOJI_HUB_URL, FedoraPackagerText.FedoraPackagerPreferencePage_kojiHubURLLabel,
				kojiGroup);
		kojiWebURLEditor.setPropertyChangeListener(this);
		kojiHubURLEditor.setPropertyChangeListener(this);
		kojiWebURLEditor.load();
		kojiHubURLEditor.load();
		addField(kojiWebURLEditor);
		addField(kojiHubURLEditor);
		updateMargins(kojiGroup);
	}
	
	@Override
	public boolean performOk() {
		super.performOk();
		// reload packager logger config
		FedoraPackagerLogger.getInstance().refreshConfig();
		return true;
	}
	
	@Override
	protected void performApply() {
		super.performApply();
		// reload packager logger config
		FedoraPackagerLogger.getInstance().refreshConfig();
	}

	private void updateMargins(Group group) {
		// make sure there is some room between the group border
		// and the controls in the group
		GridLayout layout = (GridLayout) group.getLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
	}
	
}

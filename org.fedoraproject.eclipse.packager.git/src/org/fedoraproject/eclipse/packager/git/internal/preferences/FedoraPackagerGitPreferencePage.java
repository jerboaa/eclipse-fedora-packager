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
package org.fedoraproject.eclipse.packager.git.internal.preferences;

import org.eclipse.jface.layout.GridDataFactory;
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
import org.fedoraproject.eclipse.packager.git.Activator;
import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitText;
import org.fedoraproject.eclipse.packager.git.GitPreferencesConstants;

/**
 * Add Git preferences to Fedora packager preferences.
 *
 */
public class FedoraPackagerGitPreferencePage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	protected static final int GROUP_SPAN = 2;
	private StringFieldEditor gitCloneURLEditor;
	
	/**
	 * default constructor
	 */
	public FedoraPackagerGitPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(FedoraPackagerGitText.FedoraPackagerGitPreferencePage_description);
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
	public void checkState() {
		super.checkState();
		// base URL should end with "/"
		if (!gitCloneURLEditor.getStringValue().endsWith("/")) { //$NON-NLS-1$
			setErrorMessage(FedoraPackagerGitText.FedoraPackagerGitPreferencePage_invalidBaseURLMsg);
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}
	
	@Override
	public void createFieldEditors() {
		Composite composite = getFieldEditorParent();
		Group gitGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		gitGroup.setText(FedoraPackagerGitText.FedoraPackagerGitPreferencePage_gitGroupName);
		GridDataFactory.fillDefaults().grab(true, false).span(GROUP_SPAN, 1)
		.applyTo(gitGroup);
		/* Preference for git clone base url */
		gitCloneURLEditor = new StringFieldEditor(
				GitPreferencesConstants.PREF_CLONE_BASE_URL,
				FedoraPackagerGitText.FedoraPackagerGitPreferencePage_cloneBaseURLLabel,
				gitGroup);
		addField(gitCloneURLEditor);
		updateMargins(gitGroup);
	}
	
	private void updateMargins(Group group) {
		// make sure there is some room between the group border
		// and the controls in the group
		GridLayout layout = (GridLayout) group.getLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// empty
	}
}

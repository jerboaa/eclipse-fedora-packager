/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.local.internal.ui;

import org.eclipse.linuxtools.rpm.ui.editor.wizards.Messages;
import org.eclipse.linuxtools.rpm.ui.editor.wizards.SpecfileNewWizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.fedoraproject.eclipse.packager.local.LocalFedoraPackagerPlugin;
import org.fedoraproject.eclipse.packager.local.LocalFedoraPackagerText;

public class LocalFedoraPackagerPageFour extends SpecfileNewWizardPage {
	private String projectName;

	// widgets from super class
	private Text packageNameText;
	private Text projectText;
	private Text versionText;
	private Text summaryText;
	private Text licenseText;
	private Text urlText;
	private Text source0Text;

	/**
	 * Create the wizard.
	 */
	public LocalFedoraPackagerPageFour(String pageName, String projectName) {
		super(null);
		LocalFedoraPackagerPlugin
				.getImageDescriptor(LocalFedoraPackagerText.LocalFedoraPackagerWizardPage_image);
		this.projectName = projectName;
	}

	/**
	 * Create contents of the wizard.
	 *
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		// Gets the Text widgets from super to change
		//   the default values and add listeners
		packageNameText = getNameText();
		packageNameText.setEnabled(false);

		projectText = getProjectText();
		projectText.setEnabled(false);

		versionText = getVersionText();
		addListener(versionText);

		summaryText = getSummaryText();
		addListener(summaryText);

		licenseText = getLicenseText();
		addListener(licenseText);

		urlText = getUrlText();
		addListener(urlText);

		source0Text = getSourceText();
		addListener(source0Text);

		setDefaultValues();
		dialogChanged();
	}

	private void addListener(Text text) {
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
	}

	/**
	 * Set default value of package name and project name
	 *
	 * @see org.eclipse.linuxtools.rpm.ui.editor.wizards.SpecfileNewWizardPage#setDefaultValues()
	 *
	 */
	private void setDefaultValues() {
		packageNameText.setText(projectName);
		projectText.setText("/" + projectName);
	}

	/**
	 * In Local Fedora Packager at this point,
	 * project is not created. To avoid getting error
	 * in the specfile wizard and let the user to finish the project
	 * we set the project name to the project name retrieved from
	 * LocalFedoraPackagerPageOne and also
	 * we set the already created error messages to null
	 *
	 * @see org.eclipse.linuxtools.rpm.ui.editor.wizards.SpecfileNewWizardPage#dialogChanged()
	 */
	private void dialogChanged() {
		String fileName = getFileName();
		if (getProjectName().length() == 0) {
			updateStatus(Messages.SpecfileNewWizardPage_22);
			return;
		}
		if (fileName.length() == 0) {
			updateStatus(Messages.SpecfileNewWizardPage_25);
			return;
		}
		if (getVersionText().getText().indexOf("-") > -1) { //$NON-NLS-1$
			updateStatus(Messages.SpecfileNewWizardPage_28);
			return;
		}
		updateStatus(null);
	}

}
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
package org.fedoraproject.eclipse.packager.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;

/**
 * Start a plain project
 * 
 */
public class LocalFedoraPackagerPageOne extends WizardNewProjectCreationPage {
	private Label lblNoteGit;

	/**
	 * Create the wizard.
	 * 
	 * @param pageName
	 */
	public LocalFedoraPackagerPageOne(String pageName) {
		super(pageName);
		setTitle(FedoraPackagerText.LocalFedoraPackagerWizardPage_title);
		setDescription(FedoraPackagerText.LocalFedoraPackagerWizardPage_description);
		setImageDescriptor(ImageDescriptor.createFromFile(getClass(),
				FedoraPackagerText.LocalFedoraPackagerWizardPage_image));
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite container = (Composite) getControl();

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		lblNoteGit = new Label(container, SWT.NONE);
		lblNoteGit
				.setText(FedoraPackagerText.LocalFedoraPackagerPageOne_lblNoteGit);
		lblNoteGit.setForeground(Display.getDefault().getSystemColor(
				SWT.COLOR_BLUE));
		GridData layoutData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		lblNoteGit.setLayoutData(layoutData);

		setControl(container);
	}
}

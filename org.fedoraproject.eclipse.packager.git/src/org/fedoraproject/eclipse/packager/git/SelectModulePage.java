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
package org.fedoraproject.eclipse.packager.git;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SelectModulePage extends WizardPage {

	private Text projectText;

	protected SelectModulePage() {
		super("Package selection");
		setDescription("Select a package you want to work on");
		this.setImageDescriptor(ImageDescriptor.createFromFile(getClass(),
				"/icons/import_obj.gif")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		Label label = new Label(composite, SWT.NONE);
		label.setText("Package name:"); // TODO: externalize
		projectText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		projectText.setLayoutData(gd);
		setControl(composite);
	}

	/**
	 * @return location the user wants to store this repository.
	 */
	public String getPackageName() {
		return projectText.getText();
	}

}

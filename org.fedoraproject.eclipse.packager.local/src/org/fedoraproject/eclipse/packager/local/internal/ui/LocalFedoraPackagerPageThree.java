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

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpmstubby.InputType;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.fedoraproject.eclipse.packager.local.LocalFedoraPackagerText;
import org.fedoraproject.eclipse.packager.local.LocalProjectType;
import org.fedoraproject.eclipse.packager.api.FileDialogRunable;

public class LocalFedoraPackagerPageThree extends WizardPage {

	private Group grpSpec;
	private Button btnCheckStubby;
	private Button btnStubbyBrowse;
	private Button btnCheckSrpm;
	private Button btnSrpmBrowse;
	private Button btnCheckPlain;
	private Label lblSrpm;
	private Text textStubby;
	private Text textSrpm;
	private ComboViewer comboStubby;

	private InputType inputType;
	private LocalProjectType projectType;
	private File externalFile = null;
	private boolean pageCanFinish;

	/**
	 * Create the wizard.
	 */
	public LocalFedoraPackagerPageThree(String pageName) {
		super(pageName);
		setTitle(LocalFedoraPackagerText.LocalFedoraPackagerWizardPage_title);
		setDescription(LocalFedoraPackagerText.LocalFedoraPackagerWizardPage_description);
		setImageDescriptor(ImageDescriptor.createFromFile(getClass(),
				LocalFedoraPackagerText.LocalFedoraPackagerWizardPage_image));
	}

	/**
	 * Create contents of the wizard.
	 *
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		grpSpec = new Group(container, SWT.NONE);
		grpSpec.setLayout(new GridLayout(3, false));
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		grpSpec.setLayoutData(layoutData);
		grpSpec.setText(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_grpSpec);

		btnCheckStubby = new Button(grpSpec, SWT.RADIO);
		btnCheckStubby
				.setText(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_btnCheckStubby);
		layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		btnCheckStubby.setLayoutData(layoutData);

		btnCheckStubby.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectControl();
				setPageStatus(false, false);
			}
		});
		
		comboStubby = new ComboViewer(grpSpec, SWT.READ_ONLY);
		comboStubby.getControl().setLayoutData(layoutData);
		comboStubby.setContentProvider(ArrayContentProvider.getInstance());
		comboStubby.setInput(InputType.values());
		comboStubby.getCombo().select(0);
		layoutData = new GridData();
		layoutData.horizontalIndent = 25;
		comboStubby.getCombo().setLayoutData(layoutData);

		textStubby = new Text(grpSpec, SWT.BORDER | SWT.SINGLE);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		textStubby.setLayoutData(layoutData);

		btnStubbyBrowse = new Button(grpSpec, SWT.PUSH);
		btnStubbyBrowse
				.setText(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_btnStubbyBrowse);

		btnStubbyBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				projectType = LocalProjectType.STUBBY;
				int comboIndex = comboStubby.getCombo().getSelectionIndex();
				inputType = InputType.valueOf(comboStubby.getCombo().getItem(comboIndex));
				String filter = null;
				switch(inputType) {
				case ECLIPSE_FEATURE:
					filter = InputType.ECLIPSE_FEATURE.getFileNamePattern();
					break;
				case MAVEN_POM:
					filter = InputType.MAVEN_POM.getFileNamePattern();
					break;
				}
				if (filter != null) {
					fileDialog(filter, textStubby);
				}

				if (textStubby.getText() != null) {
					setPageStatus(true, true);
				}
			}
		});

		btnCheckSrpm = new Button(grpSpec, SWT.RADIO);
		btnCheckSrpm
				.setText(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_btnCheckSrpm);
		layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		btnCheckSrpm.setLayoutData(layoutData);

		btnCheckSrpm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectControl();
				setPageStatus(false, false);
			}
		});

		lblSrpm = new Label(grpSpec, SWT.NONE);
		lblSrpm.setText(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_lblSrpm);
		layoutData = new GridData();
		layoutData.horizontalIndent = 25;
		lblSrpm.setLayoutData(layoutData);

		textSrpm = new Text(grpSpec, SWT.BORDER | SWT.SINGLE);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		textSrpm.setLayoutData(layoutData);

		btnSrpmBrowse = new Button(grpSpec, SWT.PUSH);
		btnSrpmBrowse
				.setText(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_btnSrpmBrowse);

		btnSrpmBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				projectType = LocalProjectType.SRPM;
				fileDialog(	"*.src.rpm", textSrpm); //$NON-NLS-1$
				if (textSrpm.getText() != null) {
					setPageStatus(true, true);
				}
			}
		});

		btnCheckPlain = new Button(grpSpec, SWT.RADIO);
		btnCheckPlain
				.setText(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_btnCheckPlain);
		layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		btnCheckPlain.setLayoutData(layoutData);

		btnCheckPlain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectControl();
				projectType = LocalProjectType.PLAIN;
				externalFile = null;
				setPageStatus(true, false);
			}
		});
		selectControl();
		setPageStatus(false, false);
		setControl(container);
	}

	/**
	 * Runs the filaDialog and sets the project type and externalFile to be
	 * passed to project creator
	 *
	 * @param String
	 *            filter for the fileDialog
	 * @param Text
	 *            text box for file location
	 * @param String
	 *            type of the project that user selected
	 */
	private void fileDialog(String filter, Text text) {
		FileDialogRunable fdr = new FileDialogRunable(filter,
				NLS.bind(LocalFedoraPackagerText.LocalFedoraPackagerPageThree_fileDialog, filter));
		getShell().getDisplay().syncExec(fdr);
		String filePath = fdr.getFile();
		if (filePath != null) {
			text.setText(filePath);
			this.externalFile = new File(filePath);
		}
	}

	/**
	 * Return the external file to the user's selected file
	 *
	 * @return File
	 */
	public File getExternalFile() {
		return externalFile;
	}
	
	/**
	 * Return the type of the populated project 
	 * based on the user's selection
	 *
	 * @return LocalProjectType 
	 */
	public LocalProjectType getProjectType() {
		return projectType;
	}
	
	/**
	 * Returns the input type of the stubby_project
	 *
	 * @return InputType
	 */
	public InputType getInputType() {
		return inputType;
	}

	/**
	 * If Finish button can be enabled, return true
	 *
	 * @return pageCanFinish
	 */
	public boolean pageCanFinish() {
		return pageCanFinish;
	}

	/**
	 * Sets the status of page
	 *
	 * @param pageIsComplete
	 *            next or finish can be enabled
	 * @param pageCanFinish
	 *            finish can be enabled
	 */
	private void setPageStatus(boolean pageIsComplete, boolean pageCanFinish) {
		this.pageCanFinish = pageCanFinish;
		setPageComplete(pageIsComplete);
	}

	/**
	 * Sets the enabled properties based on the selected button
	 */
	protected void selectControl() {
		if (btnCheckStubby.getSelection()) {
			comboStubby.getCombo().setEnabled(true);
			textStubby.setEnabled(true);
			btnStubbyBrowse.setEnabled(true);
			lblSrpm.setEnabled(false);
			textSrpm.setEnabled(false);
			btnSrpmBrowse.setEnabled(false);
			textSrpm.setText("");
		} else if (btnCheckSrpm.getSelection()) {
			lblSrpm.setEnabled(true);
			textSrpm.setEnabled(true);
			btnSrpmBrowse.setEnabled(true);
			comboStubby.getCombo().setEnabled(false);
			textStubby.setEnabled(false);
			btnStubbyBrowse.setEnabled(false);
			textStubby.setText("");
		} else {
			comboStubby.getCombo().setEnabled(false);
			textStubby.setEnabled(false);
			btnStubbyBrowse.setEnabled(false);
			lblSrpm.setEnabled(false);
			textSrpm.setEnabled(false);
			btnSrpmBrowse.setEnabled(false);
			textStubby.setText("");
			textSrpm.setText("");
		}
	}

}

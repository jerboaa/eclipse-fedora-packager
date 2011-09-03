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
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.LocalProjectType;
import org.fedoraproject.eclipse.packager.api.FileDialogRunable;

/**
 * Adding functions to enable user to chose a way to populate the project
 *
 */
public class LocalFedoraPackagerPageThree extends WizardPage {

	private Button btnCheckStubby;
	private Button btnCheckSrpm;
	private Button btnCheckPlain;
	private Button btnSpecTemplate;
	private Button btnStubbyBrowse;
	private Button btnSrpmBrowse;
	private Button btnSpecPlainBrowse;
	private Label lblSrpm;
	private Label lblSpecPlain;
	private Text textStubby;
	private Text textSrpm;
	private Text textSpecPlain;
	private ComboViewer comboStubby;

	private InputType inputType;
	private LocalProjectType projectType;
	private File externalFile = null;
	private boolean pageCanFinish;

	/**
	 * Create the wizard.
	 *
	 * @param pageName
	 */
	public LocalFedoraPackagerPageThree(String pageName) {
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

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		Group grpSpec = new Group(container, SWT.NONE);
		grpSpec.setLayout(new GridLayout(3, false));
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		grpSpec.setLayoutData(layoutData);
		grpSpec.setText(FedoraPackagerText.LocalFedoraPackagerPageThree_grpSpec);

		btnCheckPlain = createRadioButton(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_btnCheckPlain);
		btnCheckPlain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				projectType = LocalProjectType.PLAIN;
				selectControl();
				btnSpecTemplate.setEnabled(true);
				setPageStatus(true, false);
			}
		});

		btnSpecTemplate = new Button(grpSpec, SWT.CHECK);
		btnSpecTemplate
				.setText(FedoraPackagerText.LocalFedoraPackagerPageThree_btnTemplateSpec);
		layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		layoutData.horizontalIndent = 25;
		btnSpecTemplate.setLayoutData(layoutData);
		btnSpecTemplate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnSpecTemplate.getSelection()) {
					setPlainControl(false);
					setPageStatus(true, false);
				} else {
					setPlainControl(true);
					setPageStatus(false, false);
				}
			}
		});

		lblSpecPlain = createLabel(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_lblSpecPlain);
		textSpecPlain = createText(grpSpec);
		btnSpecPlainBrowse = createPushButton(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_btnBrowse);
		btnSpecPlainBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fileDialog("*.spec", textSpecPlain); //$NON-NLS-1$
				if (textSpecPlain.getText().length() != 0) {
					setPageStatus(true, true);
				}
			}
		});

		btnCheckSrpm = createRadioButton(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_btnCheckSrpm);
		btnCheckSrpm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				projectType = LocalProjectType.SRPM;
				selectControl();
			}
		});

		lblSrpm = createLabel(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_lblSrpm);
		textSrpm = createText(grpSpec);
		btnSrpmBrowse = createPushButton(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_btnBrowse);
		btnSrpmBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fileDialog("*.src.rpm", textSrpm); //$NON-NLS-1$
				if (textSrpm.getText().length() != 0) {
					setPageStatus(true, true);
				}
			}
		});


		btnCheckStubby = createRadioButton(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_btnCheckStubby);
		btnCheckStubby.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				projectType = LocalProjectType.STUBBY;
				selectControl();
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

		textStubby = createText(grpSpec);
		btnStubbyBrowse = createPushButton(grpSpec,
				FedoraPackagerText.LocalFedoraPackagerPageThree_btnBrowse);
		btnStubbyBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int comboIndex = comboStubby.getCombo().getSelectionIndex();
				inputType = InputType.valueOf(comboStubby.getCombo().getItem(
						comboIndex));
				String filter = null;
				switch (inputType) {
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

				if (textStubby.getText().length() != 0) {
					setPageStatus(true, true);
				}
			}
		});

		selectControl();
		setPageStatus(false, false);
		setControl(container);
	}

	/**
	 * Return created Text widget
	 *
	 * @param Group
	 * @return Text
	 */
	private Text createText(Group grp) {
		Text text = new Text(grp, SWT.BORDER | SWT.SINGLE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(layoutData);
		return text;
	}

	/**
	 * Return the created Label widget
	 *
	 * @param Group
	 * @param String
	 *            label's text
	 * @return Button
	 */
	private Label createLabel(Group grp, String text) {
		Label label = new Label(grp, SWT.NONE);
		label.setText(text);
		GridData layoutData = new GridData();
		layoutData.horizontalIndent = 25;
		label.setLayoutData(layoutData);
		return label;
	}

	/**
	 * Return the created push button widget
	 *
	 * @param Group
	 * @param String
	 *            push button's text
	 * @return Button
	 */
	private Button createPushButton(Group grp, String text) {
		Button button = new Button(grp, SWT.PUSH);
		button.setText(text);
		return button;
	}

	/**
	 * Return the created radio button widget
	 *
	 * @param Group
	 * @param String
	 *            radio button's text
	 * @return Button
	 */
	private Button createRadioButton(Group grp, String text) {
		Button button = new Button(grp, SWT.RADIO);
		button.setText(text);
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		button.setLayoutData(layoutData);
		return button;
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
		FileDialogRunable fdr = new FileDialogRunable(filter, NLS.bind(
				FedoraPackagerText.LocalFedoraPackagerPageThree_fileDialog,
				filter));
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
	 * Return the type of the populated project based on the user's selection
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
	 * Returns the button for later reference in main wizard
	 *
	 * @return Button
	 */
	public Button btnSpecTemplate() {
		return btnSpecTemplate;
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
		setPageStatus(false, false);
		btnSpecTemplate.setSelection(true);
		btnSpecTemplate.setEnabled(false);
		if (btnCheckStubby.getSelection()) {
			setStubbyControl(true);
			setSrpmControl(false);
			setPlainControl(false);
		} else if (btnCheckSrpm.getSelection()) {
			setStubbyControl(false);
			setSrpmControl(true);
			setPlainControl(false);
		} else if (btnCheckPlain.getSelection()) {
			btnSpecTemplate.setEnabled(true);
			setStubbyControl(false);
			setSrpmControl(false);
			setPlainControl(false);
		} else {
			setStubbyControl(false);
			setSrpmControl(false);
			setPlainControl(false);
		}
	}

	private void setStubbyControl(boolean bool) {
		comboStubby.getCombo().setEnabled(bool);
		textStubby.setEnabled(bool);
		btnStubbyBrowse.setEnabled(bool);
	}

	private void setSrpmControl(boolean bool) {
		lblSrpm.setEnabled(bool);
		textSrpm.setEnabled(bool);
		btnSrpmBrowse.setEnabled(bool);
		textStubby.setText(""); //$NON-NLS-1$
		textSrpm.setText(""); //$NON-NLS-1$
	}

	private void setPlainControl(boolean bool) {
		lblSpecPlain.setEnabled(bool);
		textSpecPlain.setEnabled(bool);
		btnSpecPlainBrowse.setEnabled(bool);
		textSpecPlain.setText(""); //$NON-NLS-1$
	}
}

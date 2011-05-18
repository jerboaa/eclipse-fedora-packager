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
package org.fedoraproject.eclipse.packager.bodhi.internal.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;
import org.fedoraproject.eclipse.packager.bodhi.api.IBodhiNewDialog;

/**
 * Class implementing Bodhi updates dialog.
 */
public class BodhiNewDialog extends Dialog implements IBodhiNewDialog {
	protected static final String DIALOG_TITLE = BodhiText.BodhiNewDialog_dialogTitle; 
	protected String buildName;
	protected String release;
	protected String bugs;
	protected String notes;
	protected String type;
	protected String request;

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiNewDialog#getBuildName()
	 */
	@Override
	public String getBuildName() {
		return buildName;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiNewDialog#getRelease()
	 */
	@Override
	public String getRelease() {
		return release;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiNewDialog#getBugs()
	 */
	@Override
	public String getBugs() {
		return bugs;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiNewDialog#getNotes()
	 */
	@Override
	public String getNotes() {
		return notes;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiNewDialog#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiNewDialog#getRequest()
	 */
	@Override
	public String getRequest() {
		return request;
	}

	protected String suggestedBugs;
	protected String suggestedNotes;

	protected Button[] typeButtons;
	protected Button[] requestButtons;
	protected Text bugText;
	protected Text notesText;

	/**
	 * @param parentShell
	 * @param buildName
	 * @param release
	 * @param bugs
	 * @param notes
	 */
	public BodhiNewDialog(Shell parentShell, String buildName,
			String release, String bugs, String notes) {
		super(parentShell);
		this.buildName = buildName;
		this.release = release;
		this.suggestedBugs = bugs;
		this.suggestedNotes = notes;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DIALOG_TITLE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label buildLabel = new Label(top, SWT.NONE);
		buildLabel.setText(BodhiText.BodhiNewDialog_build);

		Label buildText = new Label(top, SWT.NONE);
		buildText.setText(buildName);

		Label releaseLabel = new Label(top, SWT.NONE);
		releaseLabel.setText(BodhiText.BodhiNewDialog_release);

		Label releaseText = new Label(top, SWT.NONE);
		releaseText.setText(release);

		Label typeLabel = new Label(top, SWT.NONE);
		typeLabel.setText(BodhiText.BodhiNewDialog_type);

		Composite typeComposite = new Composite(top, SWT.BORDER);
		typeComposite.setLayout(new GridLayout(3, true));
		typeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		typeButtons = new Button[3];
		typeButtons[0] = new Button(typeComposite, SWT.RADIO);
		typeButtons[0].setText(BodhiText.BodhiNewDialog_security);
		typeButtons[0].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		typeButtons[0].setSelection(true); // DEFAULT
		typeButtons[1] = new Button(typeComposite, SWT.RADIO);
		typeButtons[1].setText(BodhiText.BodhiNewDialog_bugfix);
		typeButtons[1].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		typeButtons[2] = new Button(typeComposite, SWT.RADIO);
		typeButtons[2].setText(BodhiText.BodhiNewDialog_enhancement);
		typeButtons[2].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label requestLabel = new Label(top, SWT.NONE);
		requestLabel.setText(BodhiText.BodhiNewDialog_request);

		Composite requestComposite = new Composite(top, SWT.BORDER);
		requestComposite.setLayout(new GridLayout(3, true));
		requestComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		requestButtons = new Button[2];
		requestButtons[0] = new Button(requestComposite, SWT.RADIO);
		requestButtons[0].setText(BodhiText.BodhiNewDialog_testing);
		requestButtons[0].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		requestButtons[0].setSelection(true); // DEFAULT
		requestButtons[1] = new Button(requestComposite, SWT.RADIO);
		requestButtons[1].setText(BodhiText.BodhiNewDialog_stable);
		requestButtons[1].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label bugLabel = new Label(top, SWT.NONE);
		bugLabel.setText(BodhiText.BodhiNewDialog_bugIds);

		bugText = new Text(top, SWT.BORDER);
		bugText.setToolTipText(BodhiText.BodhiNewDialog_bugIdsMsg);
		bugText.setText(suggestedBugs);
		bugText.setLayoutData(new GridData(convertWidthInCharsToPixels(30),
				SWT.DEFAULT));

		Label notesLabel = new Label(top, SWT.NONE);
		notesLabel.setText(BodhiText.BodhiNewDialog_notes); //$NON-NLS-1$

		notesText = new Text(top, SWT.WRAP | SWT.BORDER);
		notesText.setText(suggestedNotes);
		GridData notesData = new GridData(SWT.DEFAULT,
				convertHeightInCharsToPixels(4));
		notesData.horizontalAlignment = GridData.FILL;
		notesData.grabExcessHorizontalSpace = true;
		notesData.horizontalSpan = 2;
		notesText.setLayoutData(notesData);

		return top;
	}

	@Override
	protected void okPressed() {
		bugs = bugText.getText();
		if (!bugs.equals("") && !bugs.matches("[0-9]+(,[0-9]+)*")) { //$NON-NLS-1$ //$NON-NLS-2$
			MessageDialog.openError(getShell(), BodhiText.BodhiNewDialog_invalidBugIds,
					BodhiText.BodhiNewDialog_invalidBugIdsMsg);
			return;
		}
		
		notes = notesText.getText();
		type = getSelectedType();
		request = getSelectedRequest();
		// OK to dispose of our dialog
		super.okPressed();
	}

	private String getSelectedType() {
		String ret = ""; //$NON-NLS-1$
		if (typeButtons[0].getSelection()) {
			ret = "security"; //$NON-NLS-1$
		}
		else if (typeButtons[1].getSelection()) {
			ret = "bugfix"; //$NON-NLS-1$
		}
		else {
			ret = "enhancement"; //$NON-NLS-1$
		}
		return ret;
	}
	
	private String getSelectedRequest() {
		String ret = ""; //$NON-NLS-1$
		if (requestButtons[0].getSelection()) {
			ret = "testing"; //$NON-NLS-1$
		}
		else {
			ret = "stable"; //$NON-NLS-1$
		}
		return ret;
	}
}

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
package org.fedoraproject.eclipse.packager.bodhi.internal.ui;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;


/**
 * Dialog to add additional builds to the fixed list
 * for an update.
 *
 */
public class AddNewBuildDialog extends AbstractBodhiDialog {
	
	private static final String BUILDS_REGEX = "^(?:([^, ]+)[, ]?)+$"; //$NON-NLS-1$

	private Text txtBuilds;
	private String[] buildsData;

	/**
	 * Create the dialog.
	 * @param parent
	 */
	public AddNewBuildDialog(Shell parent) {
		super(parent, SWT.MODELESS);
		setText(BodhiText.AddNewBuildDialog_dialogTitle);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public int open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		shell.setSize(480, 154);
		shell.setText(BodhiText.AddNewBuildDialog_addAnotherBuild);
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBackground(getColor(SWT.COLOR_WHITE));
		composite.setBounds(0, 0, 478, 122);
		
		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setLocation(248, 79);
		btnCancel.setSize(110, 33);
		btnCancel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// nothing
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				performCancel();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// nothing
			}
		});
		btnCancel.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				// Cancel only if return key is pressed on the cancel button
				if (e.character == SWT.CR) {
					performCancel();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// nothing, this event does not seem to get triggered
			}
		});
		btnCancel.setText(BodhiText.AddNewBuildDialog_cancelBtn);
		
		Button btnAddBuilds = new Button(composite, SWT.NONE);
		btnAddBuilds.setLocation(131, 79);
		btnAddBuilds.setSize(110, 33);
		btnAddBuilds.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// nothing
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				handleFormInput();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// nothing
			}
		});
		btnAddBuilds.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {
					handleFormInput();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// nothing, this event does not seem to get triggered
			}
		});
		btnAddBuilds.setText(BodhiText.AddNewBuildDialog_addBtn);
		
		txtBuilds = new Text(composite, SWT.BORDER);
		txtBuilds.setLocation(131, 38);
		txtBuilds.setSize(338, 31);
		txtBuilds.setToolTipText(BodhiText.AddNewBuildDialog_buildsToolTip);
		txtBuilds.setText(""); //$NON-NLS-1$
		txtBuilds.forceFocus();
		
		Label lblBuildsToAdd = new Label(composite, SWT.NONE);
		lblBuildsToAdd.setBackground(getColor(SWT.COLOR_WHITE));
		lblBuildsToAdd.setLocation(10, 43);
		lblBuildsToAdd.setSize(110, 21);
		lblBuildsToAdd.setText(BodhiText.AddNewBuildDialog_packageBuildsLbl);
		
		lblError = new Label(composite, SWT.NONE);
		lblError.setBackground(getColor(SWT.COLOR_WHITE));
		lblError.setBounds(131, 10, 337, 21);
		composite
				.setTabList(new Control[] { txtBuilds, btnAddBuilds, btnCancel });
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.bodhi.internal.ui.AbstractBodhiDialog#validateForm()
	 */
	@Override
	protected boolean validateForm() {
		Pattern pattern = Pattern.compile(BUILDS_REGEX);
		Matcher matcher = pattern.matcher(txtBuilds.getText());
		if (!matcher.matches()) {
			setValidationError(BodhiText.AddNewBuildDialog_buildsFormatErrorMsg);
			return false;
		}
		// need to set builds data this way, since we cannot
		// access UI widgets after they have been disposed.
		setBuilds();
		return true;
	}
	
	private void setBuilds() {
		Pattern pattern = Pattern.compile(BUILDS_REGEX);
		Matcher matcher = pattern.matcher(txtBuilds.getText());
		ArrayList<String> builds = new ArrayList<String>();
		if (matcher.matches()) {
			for (int i = 0; i < matcher.groupCount(); i++) {
				for (String item: matcher.group(i).split("[, ]")) { //$NON-NLS-1$
					builds.add(item);
				}
			}
		}
		this.buildsData = builds.toArray(new String[]{});
	}
	
	/**
	 * @return The list of builds to add or {@code null}.
	 */
	public String[] getBuilds() {
		return this.buildsData;
	}

}

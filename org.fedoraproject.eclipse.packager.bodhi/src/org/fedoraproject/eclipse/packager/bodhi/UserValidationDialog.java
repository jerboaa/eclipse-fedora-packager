/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.bodhi;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog for prompting for a user name and password
 */
public class UserValidationDialog extends TrayDialog implements IUserValidationDialog {
	// widgets
	protected Text usernameField;
	protected Text passwordField;
	protected Button allowCachingButton;

	protected String domain;
	protected String defaultUsername;
	protected String defaultPassword;
	protected String password = null;
	protected boolean allowCaching = false;
	protected Image keyLockImage;

	// whether or not the user name can be changed
	protected boolean isUsernameMutable = true;
	protected String username = null;
	protected String message = null;
	boolean cachingCheckbox = true;
	protected String pathToImage;

	/**
	 * Creates a new UserValidationDialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param location
	 *            the location
	 * @param defaultName
	 *            the default user name
	 * @param message
	 *            a message to display to the user
	 */
	public UserValidationDialog(Shell parentShell, String location,
			String defaultName, String defaultPassword, String message,
			String pathToImage) {
		this(parentShell, location, defaultName, defaultPassword, message,
				pathToImage, true);
	}

	/**
	 * Creates a new UserValidationDialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param location
	 *            the location
	 * @param defaultName
	 *            the default user name
	 * @param message
	 *            a message to display to the user
	 * @param cachingCheckbox
	 *            a flag to show the allowCachingButton
	 */
	public UserValidationDialog(Shell parentShell, String location,
			String defaultName, String defaultPassword, String message,
			String pathToImage, boolean cachingCheckbox) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setHelpAvailable(false);
		this.defaultUsername = defaultName;
		this.defaultPassword = defaultPassword;
		this.domain = location;
		this.message = message;
		this.cachingCheckbox = cachingCheckbox;
		this.pathToImage = pathToImage;
	}

	/**
	 * @see Window#configureShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("UserValidationDialog.0")); //$NON-NLS-1$
	}

	/**
	 * @see Window#create
	 */
	@Override
	public void create() {
		super.create();
		// add some default values
		usernameField.setText(defaultUsername);
		passwordField.setText(defaultPassword);

		if (isUsernameMutable) {
			// give focus to user name field
			usernameField.selectAll();
			usernameField.setFocus();
		} else {
			usernameField.setEditable(false);
			passwordField.setFocus();
		}
	}

	/**
	 * @see Dialog#createDialogArea
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite imageComposite = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		imageComposite.setLayout(layout);
		imageComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Composite main = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		main.setLayout(layout);
		main.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label imageLabel = new Label(imageComposite, SWT.NONE);

		if (pathToImage != null) {
			keyLockImage = BodhiPlugin.getImageDescriptor(pathToImage)
					.createImage();
			imageLabel.setImage(keyLockImage);
		}

		GridData data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL);
		imageLabel.setLayoutData(data);

		if (message != null) {
			Label messageLabel = new Label(main, SWT.WRAP);
			messageLabel.setText(message);
			data = new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 3;
			data.widthHint = 300;
			messageLabel.setLayoutData(data);
		}
		if (domain != null) {
			Label d = new Label(main, SWT.WRAP);
			d.setText(Messages.getString("UserValidationDialog.1")); //$NON-NLS-1$
			data = new GridData();
			d.setLayoutData(data);
			Label label = new Label(main, SWT.WRAP);
			if (isUsernameMutable) {
				label.setText(domain);
			} else {
				label.setText(domain);
			}
			data = new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 2;
			data.widthHint = 300;
			label.setLayoutData(data);
		}
		createUsernameFields(main);
		createPasswordFields(main);

		if (cachingCheckbox && domain != null) {
			allowCachingButton = new Button(main, SWT.CHECK);
			allowCachingButton
					.setText(Messages.getString("UserValidationDialog.2")); //$NON-NLS-1$
			data = new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 3;
			allowCachingButton.setLayoutData(data);
			allowCachingButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					allowCaching = allowCachingButton.getSelection();
				}
			});
		}

		Dialog.applyDialogFont(parent);

		return main;
	}

	/**
	 * Creates the three widgets that represent the password entry area.
	 * 
	 * @param parent
	 *            the parent of the widgets
	 */
	protected void createPasswordFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(Messages.getString("UserValidationDialog.3")); //$NON-NLS-1$

		passwordField = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		passwordField.setLayoutData(data);
	}

	/**
	 * Creates the three widgets that represent the user name entry area.
	 * 
	 * @param parent
	 *            the parent of the widgets
	 */
	protected void createUsernameFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(Messages.getString("UserValidationDialog.4")); //$NON-NLS-1$

		usernameField = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		usernameField.setLayoutData(data);
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IUserValidationDialog#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IUserValidationDialog#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IUserValidationDialog#getAllowCaching()
	 */
	public boolean getAllowCaching() {
		return allowCaching;
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The default implementation of this framework method sets this dialog's
	 * return code to <code>Window.OK</code> and closes the dialog. Subclasses
	 * may override.
	 * </p>
	 */
	@Override
	protected void okPressed() {
		password = passwordField.getText();
		username = usernameField.getText();

		super.okPressed();
	}

	/**
	 * Sets whether or not the user name field should be mutable. This method
	 * must be called before create(), otherwise it will be ignored.
	 * 
	 * @param value
	 *            whether the user name is mutable
	 */
	public void setUsernameMutable(boolean value) {
		isUsernameMutable = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		if (keyLockImage != null) {
			keyLockImage.dispose();
		}
		return super.close();
	}
}

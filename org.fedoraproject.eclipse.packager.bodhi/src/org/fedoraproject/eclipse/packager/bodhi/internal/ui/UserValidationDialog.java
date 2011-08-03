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

import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.fedoraproject.eclipse.packager.bodhi.BodhiPlugin;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;

/**
 * A dialog for user name and password prompting.
 */
public class UserValidationDialog extends TrayDialog {
	// widgets
	protected Text usernameField;
	protected Text passwordField;
	protected Button allowCachingButton;

	protected URL bodhiInstanceUrl;
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
	protected String errorMessage;

	/**
	 * Creates a new UserValidationDialog.
	 * 
	 * @param parentShell
	 *            The parent shell
	 * @param bodhiInstanceUrl
	 *            The URL to the bodhi hinstance
	 * @param defaultUserName
	 *            The default user name
	 * @param defaultPassword
	 *            The default password. 
	 * @param message
	 *            A message to display to the user.
	 * @param pathToImage 
	 * 	          Path to image icon.
	 * @param errorMessage An error message to be shown to the user.
	 */
	public UserValidationDialog(Shell parentShell, URL bodhiInstanceUrl,
			String defaultUserName, String defaultPassword, String message,
			String pathToImage, String errorMessage) {
		this(parentShell, bodhiInstanceUrl, defaultUserName, defaultPassword, message,
				pathToImage, errorMessage, true);
	}

	/**
	 * Creates a new UserValidationDialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param bodhiInstanceURL
	 *            the URL to the bodhi instance
	 * @param defaultName
	 *            the default user name
	 * @param defaultPassword
	 * @param message
	 *            a message to display to the user
	 * @param pathToImage
	 *            Path to the image icon.
	 * @param errorMessage An error message to be shown to the user.
	 * @param cachingCheckbox
	 *            a flag to show the allowCachingButton
	 */
	public UserValidationDialog(Shell parentShell, URL bodhiInstanceURL,
			String defaultName, String defaultPassword, String message,
			String pathToImage, String errorMessage, boolean cachingCheckbox) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setHelpAvailable(false);
		this.defaultUsername = defaultName;
		this.defaultPassword = defaultPassword;
		this.bodhiInstanceUrl = bodhiInstanceURL;
		this.message = message;
		this.cachingCheckbox = cachingCheckbox;
		this.pathToImage = pathToImage;
		this.errorMessage = errorMessage;
	}

	/**
	 * @see Window#configureShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(BodhiText.UserValidationDialog_passwordRequired);
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

		Label lblErrorMessage = new Label(main, SWT.WRAP);
		lblErrorMessage.setText(errorMessage);
		lblErrorMessage.setForeground(getColor(SWT.COLOR_RED));
		data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL);
		data.horizontalSpan = 3;
		data.widthHint = 300;
		lblErrorMessage.setLayoutData(data);
		if (message != null) {
			Label messageLabel = new Label(main, SWT.WRAP);
			messageLabel.setText(message);
			data = new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 3;
			data.widthHint = 300;
			messageLabel.setLayoutData(data);
		}
		if (bodhiInstanceUrl != null) {
			Label d = new Label(main, SWT.WRAP);
			d.setText(BodhiText.UserValidationDialog_server);
			data = new GridData();
			d.setLayoutData(data);
			Label label = new Label(main, SWT.WRAP);
			label.setText(bodhiInstanceUrl.toString());
			data = new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 2;
			data.widthHint = 300;
			label.setLayoutData(data);
		}
		createUsernameFields(main);
		createPasswordFields(main);

		if (cachingCheckbox && bodhiInstanceUrl != null) {
			allowCachingButton = new Button(main, SWT.CHECK);
			allowCachingButton
					.setText(BodhiText.UserValidationDialog_savePassword);
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
		new Label(parent, SWT.NONE).setText(BodhiText.UserValidationDialog_password);

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
		new Label(parent, SWT.NONE).setText(BodhiText.UserValidationDialog_username);

		usernameField = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		usernameField.setLayoutData(data);
	}

	/**
	 * Get the password, which was entered.
	 * 
	 * @return The password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Get the entered username.
	 * 
	 * @return The username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the user selection for password caching.
	 * 
	 * @return {@code true} if caching is desired. {@code false} otherwise.
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
	
	protected Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}
}

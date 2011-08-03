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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Super class for bodhi related validating dialogs
 *
 */
public abstract class AbstractBodhiDialog extends Dialog {

	protected int result;
	protected Label lblError;
	protected Shell shell; // The dialog's shell
	
	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public AbstractBodhiDialog(Shell parent, int style) {
		super(parent, style);
	}
	
	protected Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}
	
	/**
	 * Handler for a cancel action. Closes the dialog.
	 */
	protected void performCancel() {
		this.shell.close();
		result = Window.CANCEL;
	}
	
	/**
	 * Handler which is called when "Save Update" button was pressed.
	 */
	protected void handleFormInput() {
		if (validateForm()) {
			// set form data
			this.shell.close();
			result = Window.OK;
		}
	}
	
	/**
	 * Provide an inline error message feedback if some field is invalid.
	 */
	protected void setValidationError(String error) {
		this.lblError.setText(error);
		this.lblError.setForeground(getColor(SWT.COLOR_RED));
		this.lblError.redraw();
	}
	
	/**
	 * Validates the form and may use {@link this#setValidationError(String)} to
	 * report errors in the form to the user.
	 * 
	 * @return  @return {@code true} if the form was valid, {@code false} otherwise.
	 */
	protected abstract boolean validateForm();

}

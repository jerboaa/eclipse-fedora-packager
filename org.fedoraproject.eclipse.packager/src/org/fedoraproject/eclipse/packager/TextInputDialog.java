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
package org.fedoraproject.eclipse.packager;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * An Eclipse forms based text input dialog.
 *
 */
public class TextInputDialog extends InputDialog implements Runnable {

	private boolean cancelled = false;
	
	/**
	 * 
	 * @param shell
	 * @param dialogTitle 
	 * @param dialogMessage 
	 */
	public TextInputDialog(Shell shell, String dialogTitle, String dialogMessage) {
		super(shell, dialogTitle, dialogMessage, "", null); //$NON-NLS-1$
	}

	@Override
	public void run() {
		int returnCode = open();
		if (returnCode == Window.CANCEL) {
			cancelled = true;
		}
	}
	
	/**
	 * @return {@code true} if and only if cancelled was pressed.
	 */
	public boolean isCancelled() {
		return cancelled;
	}
}

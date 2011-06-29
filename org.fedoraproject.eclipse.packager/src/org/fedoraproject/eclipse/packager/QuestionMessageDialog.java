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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Wrapper class for question based (yes/no) message dialogs.
 *
 */
public class QuestionMessageDialog implements Runnable {

	private final String question;
	private boolean okPressed;
	private Shell shell;
	private IProjectRoot root;
	private String title;

	/**
	 * 
	 * @param question
	 * @param shell
	 * @param fpRoot 
	 */
	public QuestionMessageDialog(String question, Shell shell, IProjectRoot fpRoot) {
		this.question = question;
		this.shell = shell;
		this.root = fpRoot;
	}
	
	/**
	 * 
	 * @param question
	 * @param shell
	 * @param title The title of the question dialog.
	 */
	public QuestionMessageDialog(String title, String question, Shell shell) {
		this.question = question;
		this.shell = shell;
		this.title = title;
	}
	

	@Override
	public void run() {
		if (root != null) {
			okPressed = MessageDialog.openQuestion(shell,
					root.getProductStrings().getProductName(), question);
		} else if (title != null) {
			okPressed = MessageDialog.openQuestion(shell,
					title, question);
		} else {
			// either need a title, or project root.
			throw new IllegalStateException();
		}
	}

	/**
	 * @return {@code true} if and only if yes was pressed by the user.
	 */
	public boolean isOkPressed() {
		return okPressed;
	}
}

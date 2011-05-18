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
package org.fedoraproject.eclipse.packager.tests.utils;

import org.eclipse.jface.window.Window;
import org.fedoraproject.eclipse.packager.bodhi.api.IUserValidationDialog;

public class UserValidationDialogStub implements IUserValidationDialog {

	public boolean getAllowCaching() {
		return false;
	}

	public String getPassword() {
		return "pass";
	}

	public int getReturnCode() {
		return Window.OK;
	}

	public String getUsername() {
		return "user";
	}

	public int open() {
		return Window.OK;
	}

}

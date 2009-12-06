package org.fedoraproject.eclipse.packager.tests;

import org.eclipse.jface.window.Window;
import org.fedoraproject.eclipse.packager.bodhi.IUserValidationDialog;

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

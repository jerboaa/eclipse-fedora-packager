package org.fedoraproject.eclipse.packager.tests;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.fedoraproject.eclipse.packager.bodhi.BodhiNewHandler;
import org.fedoraproject.eclipse.packager.bodhi.IBodhiNewDialog;

public class BodhiNewDialogStub implements IBodhiNewDialog {
	private BodhiNewHandler handler;
	
	public BodhiNewDialogStub(BodhiNewHandler handler) {
		this.handler = handler;
	}
	
	public String getBugs() {
		return "";
	}

	public String getBuildName() {
		try {
			return handler.getBuildName();
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNotes() {
		try {
			return handler.getClog();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getRelease() {
		try {
			return handler.getReleaseName();
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getRequest() {
		return "testing";
	}

	public String getType() {
		return "bugfix";
	}

	public int getReturnCode() {
		return Window.OK;
	}

	public int open() {
		return Window.OK;
	}

}

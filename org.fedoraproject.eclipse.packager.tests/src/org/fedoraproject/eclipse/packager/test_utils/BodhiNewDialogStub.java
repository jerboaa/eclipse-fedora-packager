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
package org.fedoraproject.eclipse.packager.test_utils;

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

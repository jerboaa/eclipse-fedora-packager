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
package org.fedoraproject.eclipse.packager.tests.cvs;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.cvs.handlers.TagHandler;
import org.fedoraproject.eclipse.packager.tests.AbstractTest;

public class CVSTagTest extends AbstractTest {
	private IStatus result;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		handler = new TagHandler();
		handler.setDebug(true);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);

		result = handler.waitForJob();
	}
	
	public void testNeedWriteAccess() throws Exception {
		String errMsg = "\"tag\" requires write access to the repository";
		assertTrue(result.getChildren()[0].getMessage().contains(errMsg));
	}
	
}

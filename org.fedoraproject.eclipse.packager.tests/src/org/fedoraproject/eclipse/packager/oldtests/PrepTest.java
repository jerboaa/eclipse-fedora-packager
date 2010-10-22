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
package org.fedoraproject.eclipse.packager.oldtests;


import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.rpm.PrepHandler;

public class PrepTest extends AbstractTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		handler = new PrepHandler();
		handler.setDebug(true);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);		
		handler.waitForJob();
	}
	
	public void testSourceFolder() throws Exception {
		IContainer folder = (IContainer) branch.findMember("ed-1.1");
		assertNotNull(folder);
		assertTrue(folder.members().length > 0);
	}
}

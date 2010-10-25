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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.bodhi.BodhiNewHandler;
import org.fedoraproject.eclipse.packager.oldtests.utils.AbstractTest;
import org.fedoraproject.eclipse.packager.oldtests.utils.BodhiClientStub;
import org.fedoraproject.eclipse.packager.oldtests.utils.BodhiNewDialogStub;
import org.fedoraproject.eclipse.packager.oldtests.utils.UserValidationDialogStub;

public class BodhiNewTest extends AbstractTest {
	private BodhiClientStub bodhi;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		handler = new BodhiNewHandler();
		bodhi = new BodhiClientStub();
		handler.setDebug(true);
		((BodhiNewHandler) handler).setBodhi(bodhi);
		((BodhiNewHandler) handler).setDialog(new BodhiNewDialogStub((BodhiNewHandler) handler));
		((BodhiNewHandler) handler).setAuthDialog(new UserValidationDialogStub());
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();

	}

	public void testUpdate() throws Exception {
		IStatus result = handler.waitForJob();
		String tgflash = result.getMessage();
		String update = result.getChildren()[0].getMessage();
		assertEquals("1337", tgflash);
		assertEquals("7331", update);
	}
	
	public void testAuth() throws Exception {
		assertEquals("user", bodhi.username);
		assertEquals("pass", bodhi.password);
	}
	
	public void testArgs() throws Exception {
		String clog = "* Wed Oct 29 2008 Karsten Hopp <karsten@redhat.com> 1.1-1\n"+
			 "- update to lastest version, fixes CVE-2008-3916";
		assertEquals("ed-1.1-1.fc10", bodhi.buildName);
		assertEquals("F10", bodhi.release);
		assertEquals("", bodhi.bugs);
		assertEquals("bugfix", bodhi.type);
		assertEquals("testing", bodhi.request);
		assertEquals(clog, bodhi.notes);
	}
}

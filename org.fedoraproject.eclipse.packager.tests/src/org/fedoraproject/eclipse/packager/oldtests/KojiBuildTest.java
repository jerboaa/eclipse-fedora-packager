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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.koji.KojiBuildHandler;
import org.fedoraproject.eclipse.packager.oldtests.utils.AbstractTest;
import org.fedoraproject.eclipse.packager.oldtests.utils.KojiHubClientStub;

public class KojiBuildTest extends AbstractTest {
	private KojiHubClientStub koji;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		handler = new KojiBuildHandler();
		handler.setDebug(true);
		koji = new KojiHubClientStub();
		((KojiBuildHandler) handler).setKoji(koji);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();
	}

	public void testBuild() throws Exception {
		assertEquals("1337", handler.waitForJob().getMessage());
	}
	
	public void testSCMURL() throws Exception {
		assertEquals("cvs://cvs.fedoraproject.org/cvs/pkgs?rpms/ed/F-10#ed-1_1-1_fc10", koji.scmURL);
	}
	
	public void testTarget() throws Exception {
		assertEquals("dist-f10-updates-candidate", koji.target);
	}
}

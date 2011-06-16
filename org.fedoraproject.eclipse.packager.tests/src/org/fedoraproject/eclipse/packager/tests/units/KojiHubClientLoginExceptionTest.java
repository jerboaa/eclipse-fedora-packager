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
package org.fedoraproject.eclipse.packager.tests.units;


import static org.junit.Assert.*;

import java.security.GeneralSecurityException;

import org.junit.Before;
import org.junit.Test;
import org.fedoraproject.eclipse.packager.koji.api.errors.*;

public class KojiHubClientLoginExceptionTest {

	private KojiHubClientLoginException exceptionUnderTest;
	
	@Before
	public void setUp() throws Exception {
		exceptionUnderTest = new KojiHubClientLoginException(new GeneralSecurityException());
	}

	/**
	 * We should be able to determine if login exception was due to an expired
	 * certificate. This test requires a valid ~/.fedora.cert
	 * 
	 */
	@Test
	public void testIsCertificateExpired() {
		assertFalse(exceptionUnderTest.isCertificateExpired());
	}

}

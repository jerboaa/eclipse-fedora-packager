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

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	KojiSSLHubClientTest.class,
	UploadFileValidityTest.class,
	AssertionsEnabledTest.class,
	KojiBuildInfoTest.class,
	KojiHubClientLoginExceptionTest.class,
	BodhiClientTest.class
})

public class AllUnitTests {
	// empty
}

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
package org.fedoraproject.eclipse.packager.tests;

import org.fedoraproject.eclipse.packager.tests.commands.AllCommandsTests;
import org.fedoraproject.eclipse.packager.tests.units.AllUnitTests;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	// Unit tests
	AllUnitTests.class,
	// Command tests
	AllCommandsTests.class,
	// Auxiliary plug-in tests
	CVSProjectTypeTest.class,
	GitFpBitsTest.class,
	GitProjectTypeTest.class,
	SourcesFileTest.class,
	SourcesFileUpdaterTest.class,
	VCSIgnoreFileUpdaterTest.class,
	FedoraSSLTest.class,
	FedoraProjectRootTest.class,
	FedoraPackagerUtilsTest.class,
	FedoraPackagerLoggerTest.class,
	FedoraPackagerTest.class
})

public class AllTests {
	// empty
}

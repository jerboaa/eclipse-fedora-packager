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
package org.fedoraproject.eclipse.packager.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.fedoraproject.eclipse.packager.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TagTest.class);
		suite.addTestSuite(NewSourcesTest.class);
		suite.addTestSuite(BodhiNewTest.class);
		//suite.addTestSuite(MockTest.class);
		suite.addTestSuite(UploadTest.class);
		suite.addTestSuite(DownloadTest.class);
		suite.addTestSuite(LocalBuildTest.class);
		suite.addTestSuite(PrepTest.class);
		suite.addTestSuite(KojiBuildTest.class);
		suite.addTestSuite(SRPMTest.class);
		//$JUnit-END$
		return suite;
	}

}

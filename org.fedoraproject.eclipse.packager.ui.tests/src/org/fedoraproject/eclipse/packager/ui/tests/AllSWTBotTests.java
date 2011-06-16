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
package org.fedoraproject.eclipse.packager.ui.tests;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AddSourcesSWTBotTest.class,
	DistGitImportSWTBotTest.class,
	BodhiUpdateSWTBotTest.class,
	DownloadSourcesSWTBotTest.class,
	KojiBuildSWTBotTest.class,
	LocalBuildSWTBotTest.class,
	MockSWTBotTest.class,
	CreateSRPMSWTBotTest.class,
	PrepSourcesSWTBotTest.class,
	ReplaceSourcesSWTBotTest.class}
)

public class AllSWTBotTests {

}

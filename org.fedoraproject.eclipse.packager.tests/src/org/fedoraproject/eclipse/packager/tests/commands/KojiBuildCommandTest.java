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
/**
 * 
 */
package org.fedoraproject.eclipse.packager.tests.commands;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.koji.api.KojiBuildCommand;
import org.fedoraproject.eclipse.packager.tests.utils.KojiGenericHubClientStub;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Koji build command.
 *
 */
public class KojiBuildCommandTest {

	// project under test
	private GitTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	// Fedora packager root
	private IProjectRoot fpRoot;
	
	/**
	 * Clone a test project to be used for testing.
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse-fedorapackager");
		this.fpRoot = FedoraPackagerUtils.getProjectRoot((this.testProject
				.getProject()));
		this.packager = new FedoraPackager(fpRoot);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}

	/**
	 * Test method for 
	 * {@link org.fedoraproject.eclipse.packager.koji.api.KojiBuildCommand#checkConfiguration()}.
	 */
	@Test
	public void testCheckConfiguration() throws Exception {
		KojiBuildCommand buildCommand = (KojiBuildCommand) packager
				.getCommandInstance(KojiBuildCommand.ID);
		try {
			buildCommand.call(new NullProgressMonitor());
			fail("Should have thrown an exception. Command is not properly configured.");
		} catch (CommandMisconfiguredException e) {
			// pass
		}
	}

	/**
	 *  This illustrates proper usage of {@link KojiBuildCommand}. Since
	 *  it's using a stubbed client it does not actually push a build to koji.
	 */
	@Test
	public void canPushFakeScratchBuild() throws Exception {
		KojiBuildCommand build = (KojiBuildCommand) packager
				.getCommandInstance(KojiBuildCommand.ID);
		build.setKojiClient(new KojiGenericHubClientStub());
		build.buildTarget("dist-rawhide").nvr("eclipse-fedorapackager-0.1.12-1.fc15");
		build.sourceLocation("git://pkgs.stg.fedoraproject.org/eclipse-fedorapackager.git?#7526fb6c2c150dcc3480a9838540426a501d0553");
		try {
			build.isScratchBuild(true).call(new NullProgressMonitor());
		} catch (Exception e) {
			fail("Shouldn't have thrown any exception.");
		}
	}

}

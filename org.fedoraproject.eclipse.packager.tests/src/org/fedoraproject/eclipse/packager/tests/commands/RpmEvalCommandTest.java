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
package org.fedoraproject.eclipse.packager.tests.commands;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.rpm.api.EvalResult;
import org.fedoraproject.eclipse.packager.rpm.api.RpmEvalCommand;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the RPM eval command.
 */
public class RpmEvalCommandTest {

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
	 * {@link org.fedoraproject.eclipse.packager.rpm.api.RpmEvalCommand#checkConfiguration()}.
	 */
	@Test
	public void testCheckConfiguration() throws Exception {
		RpmEvalCommand eval = (RpmEvalCommand) packager
				.getCommandInstance(RpmEvalCommand.ID);
		try {
			eval.call(new NullProgressMonitor());
			fail("Should have thrown an exception. Command is not properly configured.");
		} catch (CommandMisconfiguredException e) {
			// pass
		}
	}

	/**
	 *  This illustrates proper usage of {@link RpmEvalCommand}.
	 */
	@Test
	public void canEvalArchitecture() throws Exception {
		RpmEvalCommand eval = (RpmEvalCommand) packager
				.getCommandInstance(RpmEvalCommand.ID);
		EvalResult result;
		try {
			result = eval.variable(RpmEvalCommand.ARCH).call(new NullProgressMonitor());
		} catch (Exception e) {
			fail("Shouldn't have thrown any exception.");
			return;
		}
		assertTrue(result.wasSuccessful());
	}

}

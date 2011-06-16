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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.tests.utils.DummyPostExecCmdListener;
import org.fedoraproject.eclipse.packager.tests.utils.DummyPreExecCmdListener;
import org.fedoraproject.eclipse.packager.tests.utils.DummyResult;
import org.fedoraproject.eclipse.packager.tests.utils.FedoraPackagerCommandDummyImpl;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for functionality provided by {@link FedoraPackagerCommand}. This test
 * uses a dummy implementation, {@link FedoraPackagerCommandDummyImpl}, of a
 * Fedora packager command.
 * 
 */
public class FedoraPackagerCommandTest {

	// Fedora packager root
	private static IProjectRoot fpRoot;
	private static GitTestProject testProject;
	
	/**
	 * Set up a Fedora project to be able to use commands. We only require this
	 * to be run once for all tests in this class.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		testProject = new GitTestProject("eclipse-fedorapackager");
		fpRoot = FedoraPackagerUtils.getProjectRoot(testProject.getProject());
	}

	/**
	 * This is static intentionally.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
		testProject.dispose();
	}
	
	@Test
	public void shouldThrowConfigurationException() throws Exception {
		FedoraPackagerCommandDummyImpl fpCmd = new FedoraPackagerCommandDummyImpl();
		fpCmd.initialize(fpRoot);
		// FedoraPackagerCommandDummyImpl requires setConfiguration(true) to be
		// called, i.e. this should throw an exception.
		try {
			fpCmd.call(new NullProgressMonitor());
		} catch (CommandMisconfiguredException e) {
			// pass
			return;
		}
		fail("Command was NOT properly configured, yet did not throw exception!");
	}
	
	@Test
	public void cannotCallACommandInstanceTwice() throws Exception {
		FedoraPackagerCommandDummyImpl fpCmd = new FedoraPackagerCommandDummyImpl();
		fpCmd.initialize(fpRoot);
		fpCmd.setConfiguration(true); // configure
		fpCmd.call(new NullProgressMonitor()); // should work
		try {
			fpCmd.call(new NullProgressMonitor());
			fail("Command should have thrown IllegalStateException, since callable == false");
		} catch (IllegalStateException e) {
			// pass
		}
	}
	
	@Test
	public void canRegisterCommandListeners() throws Exception {
		FedoraPackagerCommandDummyImpl fpCmd = new FedoraPackagerCommandDummyImpl();
		fpCmd.initialize(fpRoot);
		DummyPreExecCmdListener preExecListener = new DummyPreExecCmdListener();
		fpCmd.setConfiguration(true);
		fpCmd.addCommandListener(preExecListener);
		// test pre-exec hook
		try {
			fpCmd.call(new NullProgressMonitor());
			fail("Should have thrown CommandListenerException!");
		} catch(CommandListenerException e) {
			if (e.getCause() instanceof IllegalStateException) {
				// exception thrown in pre-exec listener
				IllegalStateException exp = (IllegalStateException)e.getCause();
				assertEquals(DummyPreExecCmdListener.EXCEPTION_MSG, exp.getMessage());
			}
		}
		// get a fresh instance
		fpCmd = new FedoraPackagerCommandDummyImpl();
		DummyPostExecCmdListener postExecListener = new DummyPostExecCmdListener();
		fpCmd.setConfiguration(true);
		fpCmd.addCommandListener(postExecListener);
		// test post-exec hook
		try {
			fpCmd.call(new NullProgressMonitor());
			fail("Should have thrown CommandListenerException!");
		} catch(CommandListenerException e) {
			if (e.getCause() instanceof IllegalStateException) {
				// exception thrown in post-exec listener
				IllegalStateException exp = (IllegalStateException)e.getCause();
				assertEquals(DummyPostExecCmdListener.EXCEPTION_MSG, exp.getMessage());
			}
		}
	}

	/**
	 * Positive test case. If command succeeds, should get an ICommandResult.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canGetCommandResult() throws Exception {
		FedoraPackagerCommandDummyImpl fpCmd = new FedoraPackagerCommandDummyImpl();
		fpCmd.initialize(fpRoot);
		fpCmd.setConfiguration(true);
		DummyResult result = fpCmd.call(new NullProgressMonitor());
		assertTrue(result.wasSuccessful());
	}
}

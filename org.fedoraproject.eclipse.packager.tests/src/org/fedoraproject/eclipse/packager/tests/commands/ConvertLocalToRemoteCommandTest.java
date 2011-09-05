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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.git.api.ConvertLocalToRemoteCommand;
import org.fedoraproject.eclipse.packager.git.api.errors.LocalProjectConversionFailedException;
import org.fedoraproject.eclipse.packager.git.api.errors.RemoteAlreadyExistsException;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitConvertTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConvertLocalToRemoteCommandTest {

	private static final String PROJECT = "alchemist"; //$NON-NLS-1$
	private static final String SPEC = "alchemist.spec"; //$NON-NLS-1$
	private static final String URI = "git://pkgs.fedoraproject.org/eclipse-fedorapackager.git"; //$NON-NLS-1$

	// project under test
	private GitConvertTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	// Fedora packager root
	private IProjectRoot lfpRoot;
	private IFpProjectBits projectBits;
	private Git git;

	/**
	 * Set up a Fedora project and run the command.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitConvertTestProject(PROJECT, SPEC);
		// create a fedoraprojectRoot for this project
		lfpRoot = FedoraPackagerUtils.getProjectRoot(this.testProject
				.getProject());
		packager = new FedoraPackager(lfpRoot);
		projectBits = FedoraPackagerUtils.getVcsHandler(lfpRoot);
	}

	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}

	/**
	 * Test if the command works properly
	 * when there is no remote repository added manually
	 *
	 * @throws Exception
	 */
	@Test
	public void testConvertCommand() throws Exception {
		boolean localRefsOk = false;

		ConvertLocalToRemoteCommand convertCmd;
		convertCmd = (ConvertLocalToRemoteCommand) packager
				.getCommandInstance(ConvertLocalToRemoteCommand.ID);
		convertCmd.call(new NullProgressMonitor());

		// Make sure the property of the project changed
		// from local to main fedorapackager
		assertTrue(lfpRoot.getProject().getPersistentProperty(
				PackagerPlugin.PROJECT_PROP).equals("true")); //$NON-NLS-1$
		assertNull(lfpRoot.getProject().getPersistentProperty(
				PackagerPlugin.PROJECT_LOCAL_PROP));

		// Make sure the url for remote repository is correct
		assertTrue(projectBits.getScmUrl().contains(
				"pkgs.fedoraproject.org/alchemist.git")); //$NON-NLS-1$

		// Find the local repository in the project location
		findGitRepository();

		// Make sure the current checked out branch is master
		assertTrue(git.getRepository().getBranch().equals("master")); //$NON-NLS-1$

		// Check a random branch (f10),
		// and make sure both remote and local version of it exists
		List<Ref> remoteRefs = git.branchList().setListMode(ListMode.REMOTE)
				.call();
		assertTrue(remoteRefs
				.toString()
				.contains(
						"refs/remotes/origin/f10=2f12e50b9860dc05cf3ac09c1cd82b45fae28637")); //$NON-NLS-1$

		Map<String, Ref> localRefs = git.getRepository().getRefDatabase()
				.getRefs(Constants.R_REFS);
		for (Ref refValue : localRefs.values()) {
			if (refValue.toString().contains(
					"refs/heads/f10=2f12e50b9860dc05cf3ac09c1cd82b45fae28637")) { //$NON-NLS-1$
				localRefsOk = true;
			}
		}
		assertTrue(localRefsOk);
	}

	/**
	 * Test if no exception is thrown when a remote origin
	 * with expected url has been added to the project location
	 *
	 * @throws Exception
	 */
	@Test
	public void testShouldNotThrowExceptionWhenExistingExpectedRemote()
			throws Exception {

		// Find the local repository in the project location
		findGitRepository();

		String uri = projectBits.getScmUrl();
		testProject.addRemoteRepository(uri, git);
		ConvertLocalToRemoteCommand convertCmd;
		convertCmd = (ConvertLocalToRemoteCommand) packager
				.getCommandInstance(ConvertLocalToRemoteCommand.ID);
		try {
			convertCmd.call(new NullProgressMonitor());
		} catch (CommandMisconfiguredException e) {
			e.printStackTrace();
		} catch (CommandListenerException e) {
			e.printStackTrace();
		} catch (LocalProjectConversionFailedException e) {
			fail("Should not throw a LocalProjectConversionFailedException");
		} catch (RemoteAlreadyExistsException e) {
			fail("Should not throw a RemoteAlreadyExistsException");
		}
	}

	/**
	 * Test if exception is thrown when a remote origin
	 * with a non expected url has been added to the project location
	 *
	 * @throws Exception
	 */
	@Test
	public void testShouldThrowExceptionWhenExistingNonExpectedRemote()
			throws Exception {

		// Find the local repository in the project location
		findGitRepository();

		testProject.addRemoteRepository(URI, git);
		ConvertLocalToRemoteCommand convertCmd;
		convertCmd = (ConvertLocalToRemoteCommand) packager
				.getCommandInstance(ConvertLocalToRemoteCommand.ID);
		try {
			convertCmd.call(new NullProgressMonitor());
			fail("Should have thrown a RemoteAlreadyExistsException");
		} catch (CommandMisconfiguredException e) {
			e.printStackTrace();
		} catch (CommandListenerException e) {
			e.printStackTrace();
		} catch (RemoteAlreadyExistsException e) {
			// pass
		} catch (LocalProjectConversionFailedException e) {
			fail("Should not get to this point");
		}
	}

	/**
	 * Finds the Git repository in the project location
	 *
	 * @throws IOException
	 */
	private void findGitRepository() throws IOException {
		// Find the local repository
		RepositoryCache repoCache = org.eclipse.egit.core.Activator
				.getDefault().getRepositoryCache();

		git = new Git(repoCache.lookupRepository(lfpRoot.getProject()
					.getFile(".git").getLocation().toFile())); //$NON-NLS-1$
	}
}

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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildResult;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand.BuildType;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildResult;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Some basic tests for the mock build command.
 */
public class MockBuildCommandTest {

	// project under test
	private GitTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	// Fedora packager root
	private IProjectRoot fpRoot;
	// Path to SRPM
	private String srpmPath;
	private BranchConfigInstance bci;
	
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse-fedorapackager");
		// switch to F15
		testProject.checkoutBranch("f15");
		testProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		this.fpRoot = FedoraPackagerUtils.getProjectRoot((this.testProject
				.getProject()));
		this.packager = new FedoraPackager(fpRoot);
		// need to have sources ready
		DownloadSourceCommand download = (DownloadSourceCommand) packager
				.getCommandInstance(DownloadSourceCommand.ID);
		download.call(new NullProgressMonitor());
		this.bci = FedoraPackagerUtils.getVcsHandler(fpRoot).getBranchConfig();
		// build fresh SRPM
		RpmBuildResult srpmBuildResult = createSRPM();
		this.srpmPath = srpmBuildResult.getAbsoluteSRPMFilePath();
	}

	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}

	/**
	 * This test may take >= 15 mins to run. Be patient :)
	 * 
	 * @throws Exception
	 */
	@Test
	public void canCreateF15MockBuild() throws Exception {
		MockBuildCommand mockBuild = (MockBuildCommand) packager
				.getCommandInstance(MockBuildCommand.ID);
		MockBuildResult result = mockBuild.pathToSRPM(srpmPath)
				.branchConfig(bci).call(new NullProgressMonitor());
		assertTrue(result.wasSuccessful());
		String resultDirectoryPath = result.getResultDirectoryPath();
		assertNotNull(resultDirectoryPath);
		// should have created RPMs in the result directory
		boolean found = false;
		this.testProject.getProject().refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
		File resultPath = new File(resultDirectoryPath);
		IContainer container = (IContainer) this.testProject.getProject()
				.findMember(new Path(resultPath.getName()));
		for (IResource file: container.members()) {
			if (file.getName().endsWith(".rpm")) {
				// not interested in source RPMs
				if (!file.getName().endsWith(".src.rpm")) {
					found = true;
				}
			}
		}
		assertTrue(found);
	}
	
	/**
	 * Helper to create an SRPM which we can use for a mock build.
	 * 
	 * @return the built result.
	 * @throws Exception
	 */
	private RpmBuildResult createSRPM() throws Exception {
		List<String> nodeps = new ArrayList<String>(1);
		nodeps.add(RpmBuildCommand.NO_DEPS);
		// get RPM build command in order to produce an SRPM
		RpmBuildCommand srpmBuild = (RpmBuildCommand) packager
				.getCommandInstance(RpmBuildCommand.ID);
		// want SRPM build
		srpmBuild.buildType(BuildType.SOURCE).flags(nodeps);
		// set branch config
		srpmBuild.branchConfig(bci);
		return srpmBuild.call(new NullProgressMonitor());
	}
}

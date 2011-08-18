/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.tests.local;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.local.LocalProjectType;
import org.fedoraproject.eclipse.packager.local.api.LocalFedoraPackagerProjectCreator;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class WizardSRPMProjectTest {
	private static final String PROJECT = "helloworld";
	private static final String SRPM = "helloworld-2-2.src.rpm";

	static IWorkspace workspace;
	static IWorkspaceRoot root;
	static NullProgressMonitor monitor;
	static IProject baseProject;
	static LocalFedoraPackagerProjectCreator testMainProject;
	private static File externalFile;
	String pluginRoot;

	@BeforeClass
	public static void setUp() throws Exception {
		// Create a base project for the test
		baseProject = ResourcesPlugin.getWorkspace().getRoot().getProject("helloworld");
		baseProject.create(null);
		baseProject.open(null);

		testMainProject = new
				LocalFedoraPackagerProjectCreator(baseProject, null);

		// Find the test SRPM and install it
		URL url = FileLocator.find(FrameworkUtil
				.getBundle(WizardSRPMProjectTest.class), new Path(
				"resources" + IPath.SEPARATOR + PROJECT + IPath.SEPARATOR + //$NON-NLS-1$
						SRPM), null);
		if (url == null) {
			fail("Unable to find resource" + IPath.SEPARATOR + PROJECT + IPath.SEPARATOR
					+ SRPM);
		}
		externalFile = new File(FileLocator.toFileURL(url).getPath());
	}

	@Test
	public void testPopulateSrpm() throws Exception {
		// poulate project using imported SRPM
		testMainProject.create(externalFile, LocalProjectType.SRPM);

		// create the local git repository inside the project
		// add the contents and do the initial commit
		testMainProject.createProjectStructure();

		// Make sure the original SRPM got copied into the workspace
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
		ProjectType projectType = FedoraPackagerUtils.getProjectType(resource);
		assertTrue(projectType.equals(ProjectType.GIT));

		// Make sure the original SRPM got copied into the workspace
		IFile srpm = baseProject.getFile(new Path(SRPM));
		assertTrue(srpm.exists());

		// Make sure everything got installed properly
		IFile specFile = baseProject.getFile(new Path("helloworld.spec")); //$NON-NLS-1$
		assertTrue(specFile.exists());

		// Check if the generated .spec file contains the correct information
		boolean packageNameOK = false;
		if (specFile.exists()) {
			InputStream is = specFile.getContents();
			String line = null;
			Scanner scan = new Scanner(is);
			while(scan.hasNext() && !packageNameOK) {
				line = scan.nextLine();
				if (line.contains("Name: helloworld")) { //$NON-NLS-1$
					packageNameOK = true;
				}
			}
			scan.close();
		}
		assertTrue(packageNameOK);

		IFile sourceBall = baseProject.getFile(new Path("helloworld-2.tar.bz2")); //$NON-NLS-1$
		assertTrue(sourceBall.exists());
	}

	@After
	public void tearDown() throws CoreException {
		baseProject.delete(true, false, null);
	}
}

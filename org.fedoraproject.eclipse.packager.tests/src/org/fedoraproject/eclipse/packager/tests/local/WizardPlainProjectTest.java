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
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class WizardPlainProjectTest {
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
		baseProject = ResourcesPlugin.getWorkspace().getRoot().getProject("eclipse-packager");
		baseProject.create(null);
		baseProject.open(null);		
		
		testMainProject = new
				LocalFedoraPackagerProjectCreator(baseProject, null);
		
		// Find the test .spec file and install it
		URL url = FileLocator.find(FrameworkUtil
				.getBundle(WizardPlainProjectTest.class), new Path(
				"resources" + IPath.SEPARATOR + "helloworld" + IPath.SEPARATOR + //$NON-NLS-1$ //$NON-NLS-2$
						"helloworld.spec"), null);
		if (url == null) {
			fail("Unable to find resource" + IPath.SEPARATOR + "helloworld" + IPath.SEPARATOR
					+ "helloworld.spec");
		}
		externalFile = new File(FileLocator.toFileURL(url).getPath());
	}

	@Test
	public void testPopulatePlain() throws Exception {
		// Start a plain project using imported .spec file
		testMainProject.create(externalFile, LocalProjectType.PLAIN);

		// Make sure the original .spec file got copied into the workspace
		IFile featureFile = baseProject.getFile(new Path("helloworld.spec"));
		assertTrue(featureFile.exists());
	}

	@After
	public void tearDown() throws CoreException {
		baseProject.delete(true, false, null);
	}
}
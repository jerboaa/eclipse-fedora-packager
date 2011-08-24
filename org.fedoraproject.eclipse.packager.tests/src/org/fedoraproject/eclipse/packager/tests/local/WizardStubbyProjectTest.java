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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpmstubby.InputType;
import org.eclipse.ui.ide.IDE;
import org.fedoraproject.eclipse.packager.api.LocalFedoraPackagerProjectCreator;
import org.fedoraproject.eclipse.packager.tests.utils.LocalSearchString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class WizardStubbyProjectTest {
	private static final String PROJECT = "eclipse-packager";
	private static final String FEATURE = "feature.xml";
	private static final String SPEC = "eclipse-packager.spec";

	private IProject baseProject;
	private LocalFedoraPackagerProjectCreator testMainProject;
	private File externalFile;

	@Before
	public void setUp() throws Exception {
		// Create a base project for the test
		baseProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(PROJECT);
		baseProject.create(null);
		baseProject.open(null);

		testMainProject = new LocalFedoraPackagerProjectCreator(baseProject, null);
		
		// Find the test feature.xml file and install it
		URL url = FileLocator.find(FrameworkUtil
				.getBundle(WizardStubbyProjectTest.class), new Path(
				"resources" + IPath.SEPARATOR + PROJECT + IPath.SEPARATOR + //$NON-NLS-1$
						FEATURE), null);
		if (url == null) {
			fail("Unable to find resource" + IPath.SEPARATOR + PROJECT
					+ IPath.SEPARATOR + FEATURE);
		}
		externalFile = new File(FileLocator.toFileURL(url).getPath());
	}

	@Test
	public void testPopulateStubby() throws Exception {
		// poulate project using imported feature.xml
		testMainProject.create(InputType.ECLIPSE_FEATURE, externalFile);

		// Make sure the original feature.xml got copied into the workspace
		IFile featureFile = baseProject.getFile(new Path(FEATURE));
		assertTrue(featureFile.exists());

		// Make sure the proper .spec file is generated
		IFile specFile = baseProject.getFile(new Path(SPEC));
		IDE.openEditor(Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), specFile);
		assertTrue(specFile.exists());

		// Check if the generated .spec file contains the correct information
		LocalSearchString localSearch = new LocalSearchString();
		assertTrue(localSearch.searchString("Name:           eclipse-packager", specFile)); //$NON-NLS-1$	
	}

	@After
	public void tearDown() throws CoreException {
		baseProject.delete(true, true, null);
	}
}
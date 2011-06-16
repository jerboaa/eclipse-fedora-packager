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
package org.fedoraproject.eclipse.packager.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.SourcesFileUpdater;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.tests.utils.TestsUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@code sources} file updater, {@link SourcesFileUpdater}.
 *
 */
public class SourcesFileUpdaterTest {
	
	private IProjectRoot fpRoot;
	private File uploadedFile;
	private IProject testProject;
	
	private static final String EXAMPLE_FEDORA_PROJECT_ROOT = 
		"resources/example-fedora-project"; // $NON-NLS-1$
	private static final String EXAMPLE_UPLOAD_FILE =
		"resources/callgraph-factorial.zip"; // $NON-NLS-1$
	
	@Before
	public void setUp() throws Exception {
		String dirName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_FEDORA_PROJECT_ROOT), null)).getFile();
		File copySource = new File(dirName);
		
		testProject = TestsUtils.createProjectFromTemplate(copySource);
		fpRoot = FedoraPackagerUtils.getProjectRoot(testProject);
		assertNotNull(fpRoot);
		
		String fileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_UPLOAD_FILE), null)).getFile();
		uploadedFile = new File(fileName);
		assertNotNull(uploadedFile);
	}

	@After
	public void tearDown() throws Exception {
		try {
			this.testProject.delete(true, null);
		} catch (CoreException e) { /* ignore */ }
	}

	@Test
	public void canReplaceSourcesFile() throws Exception {
		// sources file pre-update
		File sourcesFile = new File(testProject.getLocation().toFile().getAbsolutePath()
				+ File.separatorChar + SourcesFile.SOURCES_FILENAME);
		final String sourcesFileContentPre = TestsUtils.readContents(sourcesFile);
		// sanity check
		assertEquals("20a16942e761f9281591891834997fe5  project_sources.zip",
				sourcesFileContentPre);
		SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(fpRoot,
				uploadedFile);
		// want to replace :)
		sourcesUpdater.setShouldReplace(true);
		try {
			// this should update the sources file
			sourcesUpdater.postExecution();
		} catch (CommandListenerException e) {
			fail("Should not throw any exception!");
		}
		final String sourcesFileContentPost = TestsUtils.readContents(sourcesFile);
		assertNotSame(sourcesFileContentPre, sourcesFileContentPost);
		assertEquals(SourcesFile.calculateChecksum(uploadedFile) + "  "
				+ uploadedFile.getName(), sourcesFileContentPost);
	}

	@Test
	public void canUpdateSourcesFile() throws Exception {
		// sources file pre-update
		File sourcesFile = new File(testProject.getLocation().toFile().getAbsolutePath()
				+ File.separatorChar + SourcesFile.SOURCES_FILENAME);
		final String sourcesFileContentPre = TestsUtils.readContents(sourcesFile);
		// sanity check
		assertEquals("20a16942e761f9281591891834997fe5  project_sources.zip",
				sourcesFileContentPre);
		SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(fpRoot,
				uploadedFile);
		try {
			// this should update the sources file
			sourcesUpdater.postExecution();
		} catch (CommandListenerException e) {
			fail("Should not throw any exception!");
		}
		final String sourcesFileContentPost = TestsUtils.readContents(sourcesFile);
		assertNotSame(sourcesFileContentPre, sourcesFileContentPost);
		final String expectedSourcesFileContentPost = sourcesFileContentPre + "\n" +
			SourcesFile.calculateChecksum(uploadedFile) + "  "
			+ uploadedFile.getName();
		assertEquals(expectedSourcesFileContentPost, sourcesFileContentPost);
	}

}

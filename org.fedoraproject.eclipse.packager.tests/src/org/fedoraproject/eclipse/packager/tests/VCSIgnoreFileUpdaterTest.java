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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.VCSIgnoreFileUpdater;
import org.fedoraproject.eclipse.packager.tests.utils.TestsUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for VCS ignore file updater, {@link VCSIgnoreFileUpdater}.
 *
 */
public class VCSIgnoreFileUpdaterTest {

	private IProjectRoot fpRoot;
	private IFile vcsIgnoreFile; // The .gitignore abstraction
	private IProject tempProject;
	private Stack<File> tempDirsAndFiles = new Stack<File>();
	
	private static final String EXAMPLE_FEDORA_PROJECT_ROOT = 
		"resources/example-fedora-project"; // $NON-NLS-1$
	private static final String GITIGNORE_FILE_NAME = ".gitignore"; //$NON-NLS-1$
	
	@Before
	public void setUp() throws Exception {
		String dirName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_FEDORA_PROJECT_ROOT), null)).getFile();
		File copySource = new File(dirName);
				
		tempProject = TestsUtils.createProjectFromTemplate(copySource);
		fpRoot = new FedoraProjectRoot();
		fpRoot.initialize(tempProject, ProjectType.GIT);
		assertNotNull(fpRoot);
		
		// Get an IFile handle it shouldn't matter if it exists or not.
		// VCSIgnoreFileUpdater should do the right thing.
		vcsIgnoreFile = fpRoot.getIgnoreFile();
	}

	@After
	public void tearDown() throws Exception {
		while (!tempDirsAndFiles.isEmpty()) {
			File file = tempDirsAndFiles.pop();
			if (file.isDirectory()) {
				for (File f: file.listFiles()) {
					f.delete();
				}
			}
			file.delete();
		}
		try {
			this.tempProject.delete(true, null);
		} catch (CoreException e) { /* ignore */ }
	}
	
	@Test
	public void canCreateNewVCSIgnoreFileIfNotExistent() throws Exception {
		// should not exist as of yet
		assertTrue(!vcsIgnoreFile.exists());
		File underlyingFileInFs = vcsIgnoreFile.getLocation().toFile();
		assertTrue(!underlyingFileInFs.exists());
		final String preContent = "";
		final File newIgnoredFile = File.createTempFile("IGNORE_ME-", ".txt");
		tempDirsAndFiles.push(newIgnoredFile); // add to cleanup stack
		VCSIgnoreFileUpdater ignoreUpdater = new VCSIgnoreFileUpdater(
				newIgnoredFile, vcsIgnoreFile);
		// do the update
		ignoreUpdater.postExecution();
		// File should have been created
		assertTrue(vcsIgnoreFile.exists());
		System.out.println(this.fpRoot.getProject().getLocationURI().toURL().getPath());
		assertTrue(underlyingFileInFs.exists());
		final String postContent = TestsUtils.readContents(underlyingFileInFs);
		assertNotSame(preContent, postContent);
		assertEquals(newIgnoredFile.getName(), postContent);
	}
	

	@Test
	public void canReplaceContentWithNewIgnoreContent() throws Exception {
		final String initialContent = "somefile.txt\n";
		File gitignore = createGitIgnoreWithContent(initialContent);
		// sanity check
		assertTrue(vcsIgnoreFile.exists());
		assertTrue(gitignore.exists());
		final File newIgnoredFile = File.createTempFile("IGNORE_ME-", ".txt");
		tempDirsAndFiles.push(newIgnoredFile); // add to cleanup stack
		VCSIgnoreFileUpdater ignoreUpdater = new VCSIgnoreFileUpdater(
				newIgnoredFile, vcsIgnoreFile);
		ignoreUpdater.setShouldReplace(true); // want to replace
		// do the update
		ignoreUpdater.postExecution();
		final String contentPost = TestsUtils.readContents(gitignore);
		assertNotSame(initialContent, contentPost);
		assertEquals(newIgnoredFile.getName(), contentPost);
	}

	/**
	 * New to-be-ignored filename does not exist in ignore file yet. It should
	 * simply append the new filename.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canAppendToIgnoreFileContentIfNewIgnoredFileDoesNOTExistInIgnoreFile()
			throws Exception {
		final String initialContent = "somefile.txt\n";
		File gitignore = createGitIgnoreWithContent(initialContent);
		// sanity check
		assertTrue(vcsIgnoreFile.exists());
		assertTrue(gitignore.exists());
		final File newIgnoredFile = File.createTempFile("IGNORE_ME-", ".txt");
		tempDirsAndFiles.push(newIgnoredFile); // add to cleanup stack
		VCSIgnoreFileUpdater ignoreUpdater = new VCSIgnoreFileUpdater(
				newIgnoredFile, vcsIgnoreFile);
		// do the update
		ignoreUpdater.postExecution();
		final String contentPost = TestsUtils.readContents(gitignore);
		assertNotSame(initialContent.trim(), contentPost);
		final String expectedContentPost = initialContent
				+ newIgnoredFile.getName();
		assertEquals(expectedContentPost, contentPost);
	}
	
	/**
	 * New to-be-ignored filename DOES exist in ignore file. It should
	 * keep ignore file as is.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canAppendToIgnoreFileContentIfNewIgnoredFileDOESExistInIgnoreFile()
			throws Exception {
		final String existingFileName = "somefile.txt";
		File gitignore = createGitIgnoreWithContent(existingFileName + "\n");
		// sanity check
		assertTrue(vcsIgnoreFile.exists());
		assertTrue(gitignore.exists());
		final File newIgnoredFileCandidate = new File(tempProject.getLocation().toFile().getAbsolutePath()
				+ File.separatorChar + existingFileName);
		VCSIgnoreFileUpdater ignoreUpdater = new VCSIgnoreFileUpdater(
				newIgnoredFileCandidate, vcsIgnoreFile);
		// do the update
		ignoreUpdater.postExecution();
		final String contentPost = TestsUtils.readContents(gitignore);
		assertEquals(existingFileName, contentPost);
	}

	/**
	 * Create .gitignore file within the project root. Set content to
	 * {@code content}.
	 * 
	 * @param content
	 *            The initial content.
	 * @return A handle to the newly created .gitignore file.
	 */
	private File createGitIgnoreWithContent(String content) throws IOException, CoreException {
		// create underlying .gitignore file first
		File gitignore = new File(tempProject.getLocation().toFile().getAbsolutePath()
				+ File.separatorChar + GITIGNORE_FILE_NAME);
		if (gitignore.exists()) {
			// refuse to continue
			fail("Huh? .gitignore exists and you want to create it? Uh-uh!");
		}
		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(gitignore);
			int bytesRead;
			byte[] buf = new byte[512];
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		// now link to make Eclipse happy
		if (!vcsIgnoreFile.exists()) {
			vcsIgnoreFile.createLink(new Path(gitignore.getAbsolutePath()),
					IResource.REPLACE /* ignore */, null);
		}
		return gitignore;
	}
}

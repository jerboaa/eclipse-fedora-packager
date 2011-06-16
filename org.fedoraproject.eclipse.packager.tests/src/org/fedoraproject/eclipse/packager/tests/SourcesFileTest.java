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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.tests.utils.TestsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SourcesFileTest {

	private SourcesFile sourcesFile;
	private Stack<File> tempDirsAndFiles = new Stack<File>();
	private IProject tempProject;
	private static final String NEW_SOURCE_ARCHIVE =
		"resources/callgraph-factorial.zip"; //$NON-NLS-1$
	private static final String EXAMPLE_FEDORA_PROJECT_ROOT =
		"resources/example-fedora-project"; //$NON-NLS-1$	
	private static final String ORIG_SOURCE =
		"project_sources.zip"; //$NON-NLS-1$
	private static final String ORIG_CHECKSUM =
		"20a16942e761f9281591891834997fe5"; //$NON-NLS-1$
	
	@Before
	public void setUp() throws Exception {
		String dirName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_FEDORA_PROJECT_ROOT), null)).getFile();
		File copySource = new File(dirName);
		
		// convert it to an external eclipse project
		tempProject = TestsUtils.createProjectFromTemplate(copySource);
		IFile s = (IFile) tempProject.findMember(new Path(
				SourcesFile.SOURCES_FILENAME));
		assertNotNull(s);
		sourcesFile = new SourcesFile(s);
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
			tempProject.delete(true, null);
		} catch (CoreException e) { /* ignore */ }
	}

	@Test
	public void testGetName() {
		assertEquals(SourcesFile.SOURCES_FILENAME, sourcesFile.getName());
	}

	@Test
	public void testGetSources() {
		Map<String, String> sourcesMap = sourcesFile.getSources();
		assertEquals(1, sourcesMap.size());
		for (String filename: sourcesMap.keySet()) {
			assertEquals(ORIG_SOURCE, filename);
			assertEquals(ORIG_CHECKSUM, sourcesMap.get(filename));
		}
	}

	@Test
	public void testSetSources() {
		Map<String, String> sourcesMap = sourcesFile.getSources();
		assertEquals(1, sourcesMap.size());
		for (String filename: sourcesMap.keySet()) {
			assertEquals(ORIG_SOURCE, filename);
			assertEquals(ORIG_CHECKSUM, sourcesMap.get(filename));
		}
		Map<String, String> newSourcesMap = new HashMap<String, String>();
		newSourcesMap.put("newSource.tar", "52e7ac2eb10d1494ceb96d0cd73d33e0");
		newSourcesMap.put("newSource2.tar", "52e6ac2eb10d1494ceb96d0cd73d33e0");
		sourcesFile.setSources(newSourcesMap);
		assertEquals(newSourcesMap, sourcesFile.getSources());
		assertEquals(2, sourcesFile.getSources().size());
		// newSource.tar
		assertTrue(sourcesFile.getSources().keySet().contains("newSource.tar"));
		assertEquals("52e7ac2eb10d1494ceb96d0cd73d33e0", sourcesFile.getSources().get("newSource.tar"));
		// newSource2.tar
		assertTrue(sourcesFile.getSources().keySet().contains("newSource2.tar"));
		assertEquals("52e6ac2eb10d1494ceb96d0cd73d33e0", sourcesFile.getSources().get("newSource2.tar"));
	}

	@Test
	public void testGetCheckSum() {
		assertEquals(ORIG_CHECKSUM, sourcesFile.getCheckSum(ORIG_SOURCE));
	}

	@Test
	public void testGetMissingSources() throws Exception {
		assertTrue(sourcesFile.getMissingSources().isEmpty());
		// remove source file in order to get non-empty missing sources set
		IFile sourceToDelete = (IFile) tempProject.findMember(new Path(
				ORIG_SOURCE));
		sourceToDelete.delete(true, null);
		tempProject.refreshLocal(IResource.DEPTH_ONE, null);
		assertEquals(1, sourcesFile.getMissingSources().size());
		assertTrue(sourcesFile.getMissingSources().contains(ORIG_SOURCE));
	}

	@Test
	public void testGetAllSources() {
		// it's not missing, but should show up in all sources.
		assertEquals(1, sourcesFile.getAllSources().size());
	}

	@Test
	public void testDeleteSource() throws Exception {
		// it's not missing, but should show up in all sources.
		assertEquals(1, sourcesFile.getAllSources().size());
		sourcesFile.deleteSource(ORIG_SOURCE);
		assertEquals(1, sourcesFile.getMissingSources().size());
	}

	@Test
	public void testCalculateChecksum() throws Exception {
		IFile projectSources = (IFile) tempProject.findMember(new Path(ORIG_SOURCE));
		assertNotNull(projectSources);
		assertEquals(ORIG_CHECKSUM,
				SourcesFile.calculateChecksum(projectSources.getLocation()
						.toFile()));
		File tempFile = File.createTempFile("eclipse-fedorapackager-sourcesfiletest", "");
		// schedule tempfile for deletion after test
		tempDirsAndFiles.push(tempFile);
		ByteArrayInputStream in = new ByteArrayInputStream("Test Checksum\n".getBytes());
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(tempFile);
			int bytesRead;
			byte[] buf = new byte[1024];
			while ((bytesRead = in.read(buf)) != -1 ) {
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
		// MD5sum of "Test Checksum\n" == c4f94c2fe892ee0fa41f91352b64adf5
		assertEquals("c4f94c2fe892ee0fa41f91352b64adf5",
				SourcesFile.calculateChecksum(tempFile));
	}

	/**
	 * SourceFile works. Why isn't this test?
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSave() throws Exception {
		// sources file pre-update
		File s = new File(tempProject.getLocation().toFile().getAbsolutePath()
				+ File.separatorChar + SourcesFile.SOURCES_FILENAME);
		final String sourcesFileContentPre = TestsUtils.readContents(s);
		String newFileLoc = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(NEW_SOURCE_ARCHIVE), null)).getFile();
		File fileToAdd = new File(newFileLoc);
		Map<String, String> sources = sourcesFile.getSources();
		final String newFileChecksum = SourcesFile.calculateChecksum(fileToAdd);
		final String newFileName = "newFile.zip";
		String preString = "";
		for (String key: sources.keySet()) {
			preString += sources.get(key) + "  ";
			preString += key + "\n";
		}
		final String expectedContent = preString + newFileChecksum + "  " + newFileName; 
	
		// Add new content
		sources.put(newFileName, newFileChecksum);
		sourcesFile.setSources(sources);
		sourcesFile.save();
		
		final String actualAfterSave = TestsUtils.readContents(s);
		assertNotSame(sourcesFileContentPre, actualAfterSave);
		assertEquals(expectedContent, actualAfterSave);
	}

}

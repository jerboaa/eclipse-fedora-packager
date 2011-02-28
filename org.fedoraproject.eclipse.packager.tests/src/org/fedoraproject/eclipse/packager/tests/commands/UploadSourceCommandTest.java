package org.fedoraproject.eclipse.packager.tests.commands;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.SourcesFileUpdater;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.api.VCSIgnoreFileUpdater;
import org.fedoraproject.eclipse.packager.api.errors.FileAvailableInLookasideCacheException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidUploadFileException;
import org.fedoraproject.eclipse.packager.tests.SourcesFileUpdaterTest;
import org.fedoraproject.eclipse.packager.tests.TestsPlugin;
import org.fedoraproject.eclipse.packager.tests.VCSIgnoreFileUpdaterTest;
import org.fedoraproject.eclipse.packager.tests.units.UploadFileValidityTest;
import org.fedoraproject.eclipse.packager.tests.utils.TestsUtils;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Eclipse plug-in test for UploadSourceCommand.
 */
public class UploadSourceCommandTest {

	// project under test
	private GitTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	private static final String EXAMPLE_UPLOAD_FILE =
		"resources/callgraph-factorial.zip"; // $NON-NLS-1$
	private static final String INVALID_UPLOAD_FILE =
		"resources/invalid_upload_file.exe"; // $NON-NLS-1$
	private static final String LOOKASIDE_CACHE_URL_FOR_TESTING =
		"http://upload-cgi/cgi-bin/upload.cgi"; //$NON-NLS-1$
	private static final String EXAMPLE_FEDORA_PROJECT_ROOT = 
		"resources/example-fedora-project"; // $NON-NLS-1$
	
	// List of temporary resources which should get deleted after test runs
	private Stack<File> tempDirectories = new Stack<File>();
	
	/**
	 * Set up a Fedora project and run the command.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse-fedorapackager");
		FedoraProjectRoot fpRoot = new FedoraProjectRoot(this.testProject.getProject());
		this.packager = new FedoraPackager(fpRoot);
	}

	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
		while (!tempDirectories.isEmpty()) {
			File dir = tempDirectories.pop();
			for (File file: dir.listFiles()) {
				file.delete();
			}
			dir.delete();
		}
	}
	
	@Test
	public void shouldThrowMalformedURLException() throws Exception {
		UploadSourceCommand uploadCmd = packager.uploadSources();
		try {
			uploadCmd.setUploadURL("very bad url");
			fail("UploadSourceCommand.setUploadURL should not accept invalid URLs!");
		} catch (MalformedURLException e) {
			// pass
		}
	}

	/**
	 * Uploading sources in Fedora entails two requests. First a POST is fired
	 * with filename and MD5 as parameters and the server returns if the
	 * resource is "missing" or "available". Should sources be already
	 * available, an FileAvailableInLookasideCacheException should be thrown.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canDetermineIfSourceIsAvailable() throws Exception {
		String fileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_UPLOAD_FILE), null)).getFile();
		File file = new File(fileName);
		UploadSourceCommand uploadCmd = packager.uploadSources();
		uploadCmd.setUploadURL(LOOKASIDE_CACHE_URL_FOR_TESTING)
			.setFileToUpload(file).call(new NullProgressMonitor());
		uploadCmd = packager.uploadSources();
		try {
			uploadCmd.setUploadURL(LOOKASIDE_CACHE_URL_FOR_TESTING)
				.setFileToUpload(file).call(new NullProgressMonitor());
			// File already available
			fail("File should be present in lookaside cache.");
		} catch (FileAvailableInLookasideCacheException e) {
			//pass
		}
	}
	
	@Test
	public void canUploadSources() throws Exception {
		UploadSourceCommand uploadCmd = packager.uploadSources();
		String fileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_UPLOAD_FILE), null)).getFile();
		File file = new File(fileName);
		try {
			uploadCmd.setUploadURL(LOOKASIDE_CACHE_URL_FOR_TESTING).setFileToUpload(file)
				.call(new NullProgressMonitor());
		} catch (FileAvailableInLookasideCacheException e) {
			// File should not be available
			fail("File should have been missing!");
		}
	}
	
	/**
	 * After a file is uploaded, the {@code sources} file should be updated with
	 * the new checksum/filename. This test checks for this.
	 * 
	 * @see SourcesFileUpdaterTest
	 * 
	 * @throws Exception
	 */
	@Test
	public void canUpdateSourcesFile() throws Exception {
		String dirName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_FEDORA_PROJECT_ROOT), null)).getFile();
		File copySource = new File(dirName);
		File destTmpDir = TestsUtils.copyFolderContentsToTemp(copySource, null);
		tempDirectories.push(destTmpDir); // add to stack for later removal
		// sources file pre-update
		File sourcesFile = new File(destTmpDir.getAbsolutePath()
				+ File.separatorChar + SourcesFile.SOURCES_FILENAME);
		String sourcesFileContentPre = TestsUtils.readContents(sourcesFile);
		// sanity check
		assertEquals("4fd81a8fe53239a664f933707569d671  project_sources.zip",
				sourcesFileContentPre);
		// convert it to an external eclipse project
		IProject dummyProject = TestsUtils.adaptFolderToProject(destTmpDir);
		FedoraProjectRoot root = new FedoraProjectRoot(dummyProject);
		String fileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_UPLOAD_FILE), null)).getFile();
		File fileToAdd = new File(fileName);
		
		// create listener
		SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(root, fileToAdd);
		// want to replace sources
		sourcesUpdater.setShouldReplace(true);
		UploadSourceCommand uploadCmd = packager.uploadSources();
		uploadCmd.setFileToUpload(fileToAdd);
		uploadCmd.addCommandListener(sourcesUpdater);
		uploadCmd.call(new NullProgressMonitor());
		
		// assert sources file has been updated as expected
		String sourcesFileContentPost = TestsUtils.readContents(sourcesFile);
		assertEquals( SourcesFile.calculateChecksum(fileToAdd) + "  " + fileToAdd.getName(),
				sourcesFileContentPost);
	}
	
	/**
	 * When setting the upload file it should throw InvalidUploadFileException
	 * if the file name is not valid. Other upload file validity test are
	 * tested in {@link UploadFileValidityTest}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canDetermineValidUploadFiles() throws Exception {
		UploadSourceCommand uploadCmd = packager.uploadSources();
		String invalidUploadFileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(INVALID_UPLOAD_FILE), null)).getFile();
		File invalidUploadFile = new File(invalidUploadFileName);
		try {
			uploadCmd.setFileToUpload(invalidUploadFile);
			fail("Upload file was invalid and no exception thrown.");
		} catch (InvalidUploadFileException e) {
			// pass
		}
	}
	
	/**
	 * After a file is uploaded, the VCS ignore file should be updated.
	 * 
	 * @see VCSIgnoreFileUpdaterTest
	 * 
	 * @throws Exception
	 */
	@Test
	public void canUpdateIgnoreFile() throws Exception {
		String dirName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_FEDORA_PROJECT_ROOT), null)).getFile();
		File copySource = new File(dirName);
		File destTmpDir = TestsUtils.copyFolderContentsToTemp(copySource, null);
		tempDirectories.push(destTmpDir);
		// VCS ignore file pre-update
		File vcsIgnoreFile = new File(destTmpDir.getAbsolutePath()
				+ File.separatorChar + ".gitignore");
		String vcsIgnoreFileContentPre = "";
		if (vcsIgnoreFile.exists()) {
			vcsIgnoreFileContentPre = TestsUtils.readContents(vcsIgnoreFile);
		}
		assertEquals("", vcsIgnoreFileContentPre);
		
		String fileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_UPLOAD_FILE), null)).getFile();
		File fileToAdd = new File(fileName);
		
		UploadSourceCommand uploadCmd = packager.uploadSources();
		uploadCmd.setFileToUpload(fileToAdd);
		VCSIgnoreFileUpdater vcsUpdater = new VCSIgnoreFileUpdater(fileToAdd, vcsIgnoreFile);
		uploadCmd.addCommandListener(vcsUpdater);
		uploadCmd.call(new NullProgressMonitor());
		
		assertTrue(vcsIgnoreFile.exists()); // should have been created
		String ignoreFileContentPost = TestsUtils.readContents(vcsIgnoreFile);
		assertEquals(fileToAdd.getName(), ignoreFileContentPost);
	}
}

package org.fedoraproject.eclipse.packager.tests.commands;


import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.ChecksumValidListener;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Eclipse plug-in test for DownloadSourceCommand.
 */
public class DownloadSourceCommandTest {

	// project under test
	private GitTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	// Fedora packager root
	private FedoraProjectRoot fpRoot;
	
	/**
	 * Set up a Fedora project and run the command.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse");
		this.fpRoot = new FedoraProjectRoot(this.testProject.getProject());
		this.packager = new FedoraPackager(fpRoot);
	}

	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}
	
	@Test
	public void shouldThrowMalformedURLException() throws Exception {
		DownloadSourceCommand downloadCmd = packager.downloadSources();
		try {
			downloadCmd.setDownloadURL("very bad url");
			fail("DownloadSourceCommand.setUploadURL should not accept invalid URLs!");
		} catch (MalformedURLException e) {
			// pass
		}
	}
	
	@Test
	public void canDownloadSeveralFilesWithoutErrors() throws Exception {
		// The eclipse package usually has 2 source files. That's why we
		// use the eclipse package for testing
		DownloadSourceCommand downloadCmd = packager.downloadSources();
		ChecksumValidListener md5sumListener = new ChecksumValidListener(fpRoot);
		downloadCmd.addCommandListener(md5sumListener); // want md5sum checking
		try {
			downloadCmd.call(new NullProgressMonitor());
		} catch (SourcesUpToDateException e) {
			fail("sources for eclipse should not be present");
		} catch (CommandMisconfiguredException e) {
			fail("Cmd should be properly configured");
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof InvalidCheckSumException) {
				fail("Checksums should be OK");
			}
		}
		// pass
	}

	

}

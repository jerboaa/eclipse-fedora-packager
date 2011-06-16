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


import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.ChecksumValidListener;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.tests.utils.CorruptDownload;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
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
	private IProjectRoot fpRoot;
	
	/**
	 * Set up a Fedora project and run the command.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse-fedorapackager");
		this.fpRoot = FedoraPackagerUtils.getProjectRoot((this.testProject
				.getProject()));
		this.packager = new FedoraPackager(fpRoot);
	}

	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}
	
	@Test
	public void shouldThrowMalformedURLException() throws Exception {
		DownloadSourceCommand downloadCmd = (DownloadSourceCommand) packager
				.getCommandInstance(DownloadSourceCommand.ID);
		try {
			downloadCmd.setDownloadURL("very bad url");
			fail("DownloadSourceCommand.setUploadURL should not accept invalid URLs!");
		} catch (MalformedURLException e) {
			// pass
		}
	}

	/**
	 * Positive results test. Should work fine. Since this is downloading
	 * Eclipse sources it might take a while.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canDownloadSeveralFilesWithoutErrors() throws Exception {
		// not using eclipse-fedorapackager for this test
		this.testProject.dispose();
		// The eclipse package usually has 2 source files. That's why we
		// use the eclipse package for testing
		this.testProject = new GitTestProject("eclipse");
		this.fpRoot = FedoraPackagerUtils.getProjectRoot(this.testProject
				.getProject());
		this.packager = new FedoraPackager(fpRoot);
		DownloadSourceCommand downloadCmd = (DownloadSourceCommand) packager
				.getCommandInstance(DownloadSourceCommand.ID);
		ChecksumValidListener md5sumListener = new ChecksumValidListener(fpRoot);
		downloadCmd.addCommandListener(md5sumListener); // want md5sum checking
		try {
			downloadCmd.call(new NullProgressMonitor());
		} catch (SourcesUpToDateException e) {
			fail("sources for " + testProject.getProject().getName() + " should not be present");
		} catch (CommandMisconfiguredException e) {
			fail("Cmd should be properly configured");
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof InvalidCheckSumException) {
				fail("Checksums should be OK");
			}
		}
		// pass
	}
	
	/**
	 * Test checksums of source files.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canDetectChecksumErrors() throws Exception {
		DownloadSourceCommand downloadCmd = (DownloadSourceCommand) packager
				.getCommandInstance(DownloadSourceCommand.ID);
		CorruptDownload checksumDestroyer = new CorruptDownload(fpRoot);
		ChecksumValidListener md5sumListener = new ChecksumValidListener(fpRoot);
		// Add checksum destroyer first, checksum checker after.
		downloadCmd.addCommandListener(checksumDestroyer); // should corrupt MD5
		downloadCmd.addCommandListener(md5sumListener); // want md5sum checking
		try {
			downloadCmd.call(new NullProgressMonitor());
		} catch (SourcesUpToDateException e) {
			fail("sources for " + testProject.getProject().getName() + " should not be present");
		} catch (CommandMisconfiguredException e) {
			fail("Cmd should be properly configured");
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof InvalidCheckSumException) {
				// pass
				return;
			}
		}
		fail("Checksum should be invalid for all source files!");
	}

}

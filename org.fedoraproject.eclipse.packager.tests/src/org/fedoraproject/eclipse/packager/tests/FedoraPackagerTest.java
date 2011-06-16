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
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.tests.utils.TestsUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FedoraPackagerTest {

	private static final String EXAMPLE_FEDORA_PROJECT_ROOT = 
		"resources/example-fedora-project"; // $NON-NLS-1$
	private FedoraPackager packager;
	private IProject packagerProject;
	private FedoraProjectRoot fpRoot;
	
	@Before
	public void setUp() throws Exception {
		String dirName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_FEDORA_PROJECT_ROOT), null)).getFile();
		File origSourceDir = new File(dirName);
				
		packagerProject = TestsUtils.createProjectFromTemplate(origSourceDir);
		fpRoot = new FedoraProjectRoot();
		fpRoot.initialize(packagerProject, ProjectType.GIT);
		assertNotNull(fpRoot);
		packager = new FedoraPackager(fpRoot);
		assertNotNull(packager);
	}

	@After
	public void tearDown() throws Exception {
		try {
			this.packagerProject.delete(true, null);
		} catch (CoreException e) { /* ignore */ }
	}

	@Test
	public void testGetFedoraProjectRoot() {
		assertNotNull(fpRoot);
		assertSame(fpRoot, packager.getFedoraProjectRoot());
	}

	/**
	 * FedoraPackager should know about all registered commands from various
	 * plug-ins such as Koji and Bodhi. The core plug-in contributes
	 * {@link DownloadSourceCommand} and {@link UploadSourceCommand}, so those
	 * two should be there in any case.
	 */
	@Test
	public void canGetRegisteredCommandIDs() {
		String[] regCommands = packager.getRegisteredCommandIDs();
		int downloadUploadThereCounter = 2;
		for (String commandId: regCommands) {
			if (commandId.equals(DownloadSourceCommand.ID)
					|| commandId.equals(UploadSourceCommand.ID)) {
				downloadUploadThereCounter--;
			}
		}
		assertTrue(downloadUploadThereCounter == 0);
	}

	/**
	 * Should be able to successfully instantiate a
	 * {@link DownloadSourceCommand} object.
	 */
	@Test
	public void canGetCommandInstance() {
		DownloadSourceCommand download;
		try {
			download = (DownloadSourceCommand) packager
					.getCommandInstance(DownloadSourceCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			fail(DownloadSourceCommand.ID + " should be available!");
			return;
		} catch (FedoraPackagerCommandInitializationException e) {
			fail(DownloadSourceCommand.ID + " should be properly initialized!");
			return;
		}
		assertNotNull(download);
	}
	
	/**
	 * Should throw {@link FedoraPackagerCommandNotFoundException} for an
	 * unknown command.
	 * @throws FedoraPackagerCommandNotFoundException 
	 */
	@Test(expected=FedoraPackagerCommandNotFoundException.class) 
	public void shouldThrowFedoraPackagerCommandNotFoundException()
			throws FedoraPackagerCommandNotFoundException {
		try {
			packager
					.getCommandInstance("SomeCommandWhichShouldNotBeThere");
		} catch (FedoraPackagerCommandInitializationException e) {
			fail("Command shouldn't be there");
			return;
		}
	}
	
	/**
	 * 
	 */
	@Test(expected=FedoraPackagerCommandInitializationException.class)
	public void shouldThrowInitializationException() throws FedoraPackagerCommandInitializationException {
		DownloadSourceCommand download;
		try {
			download = (DownloadSourceCommand) packager
					.getCommandInstance(DownloadSourceCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			fail(DownloadSourceCommand.ID + " should be available!");
			return;
		}
		// this should throw initialization exception
		download.initialize(fpRoot);
	}

}

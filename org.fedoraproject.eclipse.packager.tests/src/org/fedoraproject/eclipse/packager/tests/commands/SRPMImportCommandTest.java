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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.lib.Repository;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.SourcesFileUpdater;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.api.VCSIgnoreFileUpdater;
import org.fedoraproject.eclipse.packager.rpm.api.SRPMImportCommand;
import org.fedoraproject.eclipse.packager.tests.TestsPlugin;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SRPMImportCommandTest {
	// project under test
	private IProject testProject;
	// main interface class
	private static final String UPLOAD_URL_PROP = "org.fedoraproject.eclipse.packager.tests.LookasideUploadUrl"; //$NON-NLS-1$
	private String uploadURLForTesting;
	private String srpmPath;
	private Git git;

	@Before
	public void setup() throws Exception {
		String uploadURL = System.getProperty(UPLOAD_URL_PROP);
		if (uploadURL == null) {
			fail(UPLOAD_URL_PROP + " not set");
		}
		srpmPath = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path("resources/eclipse-3.7.0-new.fc17.src.rpm"),
						null)).getFile();
		this.uploadURLForTesting = uploadURL;
		testProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("eclipse");
		testProject.create(null);
		testProject.open(null);
		testProject
				.setPersistentProperty(PackagerPlugin.PROJECT_PROP, "true" /* unused value */); //$NON-NLS-1$
		InitCommand ic = new InitCommand();
		ic.setDirectory(testProject.getLocation().toFile());
		ic.setBare(false);
		Repository repository = ic.call().getRepository();
		git = new Git(repository);
		ConnectProviderOperation connect = new ConnectProviderOperation(
				testProject);
		connect.execute(null);
	}

	@After
	public void tearDown() throws Exception {
		this.testProject.delete(true, null);
	}

	@Test
	public void canImportSRPM() throws Exception {
		Set<String> stageSet = new HashSet<String>();
		Set<String> uploadedFiles = new HashSet<String>();
		SRPMImportCommand srpmImport = new SRPMImportCommand(srpmPath,
				testProject);
		srpmImport.call(new NullProgressMonitor());
		testProject
				.getFile(new Path("sources")).getLocation().toFile().createNewFile(); //$NON-NLS-1$
		testProject.getProject().refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
		IProjectRoot fpr;
		fpr = FedoraPackagerUtils.getProjectRoot(testProject);
		FedoraPackager packager = new FedoraPackager(fpr);
		// get RPM build command in order to produce an SRPM
		UploadSourceCommand upload = (UploadSourceCommand) packager
				.getCommandInstance(UploadSourceCommand.ID);
		boolean firstUpload = true;
		for (String file : srpmImport.getUploadFiles()) {
			if (FedoraPackagerUtils.isValidUploadFile(testProject
					.getFile(new Path(file)).getLocation().toFile())) {
				File newUploadFile = testProject.getFile(new Path(file))
						.getLocation().toFile();
				SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(fpr,
						newUploadFile);
				sourcesUpdater.setShouldReplace(firstUpload);
				if (firstUpload) {
					firstUpload = false;
				}
				// Note that ignore file may not exist, yet
				IFile gitIgnore = fpr.getIgnoreFile();
				VCSIgnoreFileUpdater vcsIgnoreFileUpdater = new VCSIgnoreFileUpdater(
						newUploadFile, gitIgnore);
				String uploadUrl = uploadURLForTesting;
				if (uploadUrl != null) {
					upload.setUploadURL(uploadUrl);
				}
				upload.setFileToUpload(newUploadFile);
				// enable SLL authentication
				upload.setFedoraSSLEnabled(true);
				upload.addCommandListener(sourcesUpdater);
				upload.addCommandListener(vcsIgnoreFileUpdater);
				upload.call(new NullProgressMonitor());
				uploadedFiles.add(file);
			} else {
				stageSet.add(file);
			}
		}
		// Refresh project
		IProject project = packager.getFedoraProjectRoot().getProject();
		if (project != null) {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		// stage changes
		stageSet.add(packager.getFedoraProjectRoot().getSourcesFile().getName());
		stageSet.add(packager.getFedoraProjectRoot().getIgnoreFile().getName());
		FedoraPackagerUtils.getVcsHandler(fpr).stageChanges(
				stageSet.toArray(new String[0]));
		// ensure files are extracted
		assertTrue(packager.getFedoraProjectRoot().getContainer().getLocation()
				.append("/eclipse-build-new.tar.xz").toFile().exists());
		assertTrue(packager.getFedoraProjectRoot().getContainer().getLocation()
				.append("/eclipse-3.7.0-new-src.tar.bz2").toFile().exists());
		assertTrue(packager.getFedoraProjectRoot().getContainer().getLocation()
				.append("/eclipse-3.7.0-new.fc17.src.rpm").toFile().exists());
		// ensure files uploaded
		fpr.getSourcesFile().deleteSource("eclipse-build-new.tar.xz");
		fpr.getSourcesFile().deleteSource("eclipse-3.7.0-new-src.tar.bz2");
		assertTrue(!packager.getFedoraProjectRoot().getContainer().getLocation()
				.append("/eclipse-build-new.tar.xz").toFile().exists());
		assertTrue(!packager.getFedoraProjectRoot().getContainer().getLocation()
				.append("/eclipse-3.7.0-new-src.tar.bz2").toFile().exists());
		DownloadSourceCommand download = (DownloadSourceCommand) packager
				.getCommandInstance(DownloadSourceCommand.ID);
		download.setDownloadURL(uploadURLForTesting);
		download.call(new NullProgressMonitor());
		assertTrue(packager.getFedoraProjectRoot().getContainer().getLocation()
				.append("/eclipse-build-new.tar.xz").toFile().exists());
		assertTrue(packager.getFedoraProjectRoot().getContainer().getLocation()
				.append("/eclipse-3.7.0-new-src.tar.bz2").toFile().exists());
		// ensure files are added to git
		Set<String> addedSet = git.status().call().getAdded();
		assertTrue(addedSet.contains(packager.getFedoraProjectRoot()
				.getSourcesFile().getName()));
		assertTrue(addedSet.contains(packager.getFedoraProjectRoot()
				.getSpecFile().getName()));
		assertTrue(addedSet.contains(packager.getFedoraProjectRoot()
				.getIgnoreFile().getName()));
		addedSet.containsAll(stageSet);
		// ensure files are tracked
		for (String file : uploadedFiles) {
			assertTrue(packager.getFedoraProjectRoot().getSourcesFile()
					.getSources().keySet().contains(file));
		}
		assertTrue(packager.getFedoraProjectRoot().getSourcesFile()
				.getSources().keySet().contains("eclipse-build-new.tar.xz"));
		assertTrue(packager.getFedoraProjectRoot().getSourcesFile()
				.getSources().keySet()
				.contains("eclipse-3.7.0-new-src.tar.bz2"));
	}
}

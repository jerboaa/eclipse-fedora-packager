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
package org.fedoraproject.eclipse.packager.rpm.api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.rpm.ui.SRPMImportOperation;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.SourcesFileUpdater;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.api.VCSIgnoreFileUpdater;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FileAvailableInLookasideCacheException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.errors.SRPMImportCommandException;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Command for importing SRPM to a project.
 * 
 */
public class SRPMImportCommand {

	/**
	 * @param srpm
	 *            The srpm being imported.
	 * @param project
	 *            The project that is receiving the import.
	 * @param fprContainer
	 *            The container containing the folder to which the SRPM is being
	 *            imported.
	 * @param uploadUrl
	 *            The url of the lookaside cache.
	 */
	public SRPMImportCommand(String srpm, IProject project,
			IContainer fprContainer, String uploadUrl) {
		this.srpm = srpm;
		this.project = project;
		this.fprContainer = fprContainer;
		this.uploadUrl = uploadUrl;
	}

	private String srpm = null;
	private IProject project = null;
	private IContainer fprContainer;
	private String uploadUrl;

	protected void checkConfiguration() throws CommandMisconfiguredException {
		if (srpm == null) {
			throw new CommandMisconfiguredException(
					RpmText.SRPMImportCommand_PathNotSet);
		} else if (!new File(srpm).exists()) {
			throw new CommandMisconfiguredException(NLS.bind(
					RpmText.SRPMImportCommand_SRPMNotFound, srpm));
		}
		if (project == null) {
			throw new CommandMisconfiguredException(
					RpmText.SRPMImportCommand_ProjectNotSet);
		}
	}

	/**
	 * Calling method for this command.
	 * 
	 * @param monitor
	 *            Monitor for this command's runtime.
	 * @return The result of calling this command.
	 * @throws SRPMImportCommandException
	 */
	public SRPMImportResult call(IProgressMonitor monitor)
			throws SRPMImportCommandException {
		Set<String> stageSet = new HashSet<String>();
		Set<String> uploadedFiles = new HashSet<String>();
		Set<String> skippedUploads = new HashSet<String>();
		// install rpm to the project folder
		SRPMImportOperation sio;
		try {
			sio = new SRPMImportOperation(project, new File(srpm),
					RPMProjectLayout.FLAT);
			sio.run(new NullProgressMonitor());
		} catch (InvocationTargetException e) {
			throw new SRPMImportCommandException(e.getMessage(), e);
		}
		if (!sio.getStatus().isOK()) {
			Throwable e = sio.getStatus().getException();
			if (e != null) {
				throw new SRPMImportCommandException(e.getMessage(), e);
			} else {
				throw new SRPMImportCommandException(NLS.bind(
						RpmText.SRPMImportCommand_ImportError, sio.getStatus()
								.getMessage()));
			}
		}
		String[] cmdList = null;
		List<String> uploadList = new ArrayList<String>();
		String[] moveFiles;
		String[] uploadFiles;
		try {
			// get files in the srpm
			cmdList = new String[] { "rpm", "-qpl", srpm }; //$NON-NLS-1$ //$NON-NLS-2$
			ProcessBuilder pBuilder = new ProcessBuilder(cmdList);
			Process child = pBuilder.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new BufferedInputStream(child.getInputStream())));
			try {
				if (child.waitFor() != 0) {
					throw new SRPMImportCommandException(NLS.bind(
							RpmText.SRPMImportCommand_NonZeroQueryExit,
							child.exitValue()));
				}
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}

			while (br.ready()) {
				uploadList.add(br.readLine());
			}
			// SRPM needs to be moved but not uploaded, use existing list to
			// build array of files that need to be moved
			uploadList.add((new File(srpm)).getName());
			moveFiles = uploadList.toArray(new String[0]);
			uploadList.remove((new File(srpm)).getName());
		} catch (IOException e) {
			throw new SRPMImportCommandException(NLS.bind(
					RpmText.SRPMImportCommand_IOError, srpm), e);
		}
		SRPMImportResult result = new SRPMImportResult(cmdList);
		// Move imported files to packager root as workaround for
		// rpm editor only building to a limited number of project
		// layouts; flat layout may not be suitable to for CVS repositories or
		// Git repositories with subfolders
		if (!fprContainer.getProject().getFullPath().toOSString()
				.equals(fprContainer.getFullPath().toOSString())) {
			for (String file : moveFiles) {
				IResource source = fprContainer.getProject().findMember(file);
				if (file.endsWith(".spec")) { //$NON-NLS-1$
					file = fprContainer.getProject().getName().concat(".spec"); //$NON-NLS-1$
				}
				IFile resultFile = fprContainer.getFile(new Path(file));
				// no way to force overwrite workaround
				if (resultFile.exists()) {
					try {
						resultFile.delete(true, monitor);
					} catch (CoreException e) {
						throw new SRPMImportCommandException(NLS.bind(
								RpmText.SRPMImportCommand_OverwriteError,
								resultFile.getProjectRelativePath()), e);
					}
				}
				try {
					source.move(resultFile.getProjectRelativePath(), true,
							monitor);
				} catch (CoreException e) {
					throw new SRPMImportCommandException(NLS.bind(
							RpmText.SRPMImportCommand_MoveError, file), e);
				}
			}
		} else {
			for (String file : moveFiles) {
				if (file.endsWith(".spec") && !file.startsWith(fprContainer.getProject().getName())) { //$NON-NLS-1$
					IResource source = fprContainer.getProject().findMember(file);
					file = fprContainer.getProject().getName().concat(".spec"); //$NON-NLS-1$
					IFile resultFile = fprContainer.getFile(new Path(file));
					try {
						source.move(resultFile.getProjectRelativePath(), true,
								monitor);
					} catch (CoreException e) {
						throw new SRPMImportCommandException(NLS.bind(
								RpmText.SRPMImportCommand_MoveError, file), e);
					}
				}
			}
		}
		uploadFiles = uploadList.toArray(new String[0]);
		try {
			// create sources file if one does not exist
			fprContainer
					.getFile(new Path("sources")).getLocation().toFile().createNewFile(); //$NON-NLS-1$
			fprContainer.getProject().refreshLocal(IResource.DEPTH_INFINITE,
					monitor);
			// Should now have a valid project root, so get it.
			IProjectRoot fpr = FedoraPackagerUtils.getProjectRoot(fprContainer);

			// Make sure if the imported SRPM makes sense
			if (!fpr.getSpecfileModel().getName()
					.equals(fprContainer.getProject().getName())) {
				String errorMsg = NLS.bind(
						RpmText.SRPMImportCommand_PackageNameSpecNameMismatchError,
						fprContainer.getProject().getName(), fpr
								.getSpecfileModel().getName());
				throw new SRPMImportCommandException(errorMsg);
			}
			FedoraPackager fp = new FedoraPackager(fpr);

			// get upload source command
			UploadSourceCommand upload = (UploadSourceCommand) fp
					.getCommandInstance(UploadSourceCommand.ID);
			boolean firstUpload = true;
			for (String file : uploadFiles) {
				// This won't find the .spec file since it has been removed
				// above.
				// We don't care, since it shouldn't get uploaded anyway.
				IResource candidate = fprContainer.findMember(new Path(file));
				if (candidate != null
						&& FedoraPackagerUtils.isValidUploadFile(candidate
								.getLocation().toFile())) {
					File newUploadFile = candidate.getLocation().toFile();
					SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(
							fpr, newUploadFile);
					// replace existing source, but not other files from this
					// SRPM
					sourcesUpdater.setShouldReplace(firstUpload);
					// Note that ignore file may not exist, yet
					IFile gitIgnore = fpr.getIgnoreFile();
					VCSIgnoreFileUpdater vcsIgnoreFileUpdater = new VCSIgnoreFileUpdater(
							newUploadFile, gitIgnore);
					if (uploadUrl != null) {
						upload.setUploadURL(uploadUrl);
					}
					upload.setFileToUpload(newUploadFile);
					// enable SLL authentication
					upload.setFedoraSSLEnabled(true);
					upload.addCommandListener(sourcesUpdater);
					upload.addCommandListener(vcsIgnoreFileUpdater);
					try {
						upload.call(new NullProgressMonitor());
						uploadedFiles.add(file);
						if (firstUpload) {
							firstUpload = false;
						}
					} catch (FileAvailableInLookasideCacheException e) {
						// ignore, imports that update an existing repo can have
						// identical files in an update, but these files don't
						// really need to be uploaded
						skippedUploads.add(file);
					}

				} else {
					stageSet.add(file);
				}
			}
			result.setUploaded(uploadedFiles.toArray(new String[0]));
			result.setSkipped(skippedUploads.toArray(new String[0]));
			monitor.subTask(RpmText.SRPMImportCommand_StagingChanges);
			// Do VCS update
			if (FedoraPackagerUtils.getVcsHandler(fpr).updateVCS(fpr, monitor)
					.isOK()) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
			// stage changes
			stageSet.add(fpr.getSourcesFile().getName());
			stageSet.add(fpr.getIgnoreFile().getName());
			FedoraPackagerUtils.getVcsHandler(fpr).stageChanges(
					stageSet.toArray(new String[0]));
			result.setAddedToGit(stageSet.toArray(new String[0]));
		} catch (Exception e) {
			throw new SRPMImportCommandException(e.getMessage(), e);
		}
		result.setSuccess(true);
		return result;
	}

	/**
	 * Set the SRPM this command imports.
	 * 
	 * @param path
	 *            The path to the SRPM.
	 * @return This command.
	 */
	public SRPMImportCommand setSRPM(String path) {
		this.srpm = path;
		return this;
	}

	/**
	 * Set the project to import the SRPM into.
	 * 
	 * @param project
	 *            The project being imported to.
	 * @return This command.
	 */
	public SRPMImportCommand setProject(IProject project) {
		this.project = project;
		return this;
	}
}

/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.handlers;


import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.SourcesFileUpdater;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.api.UploadSourceResult;
import org.fedoraproject.eclipse.packager.api.VCSIgnoreFileUpdater;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FileAvailableInLookasideCacheException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidUploadFileException;
import org.fedoraproject.eclipse.packager.api.errors.UploadFailedException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Class responsible for uploading source files.
 * 
 * @see UploadSourceCommand
 * @see VCSIgnoreFileUpdater
 * @see SourcesFileUpdater
 */
public class UploadHandler extends AbstractHandler {

	@SuppressWarnings("static-access")
	@Override
	/**
	 *  Performs upload of sources (independent of VCS used), updates "sources"
	 *  file and performs necessary CVS operations to bring branch in sync.
	 *  Checks if sources have changed.
	 *  
	 */
	public Object execute(final ExecutionEvent e) throws ExecutionException {

		final IResource resource = FedoraHandlerUtils.getResource(e);
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			fedoraProjectRoot = FedoraPackagerUtils.getValidRoot(resource);
		} catch (InvalidProjectRootException e1) {
			// TODO handle appropriately
			e1.printStackTrace();
			return null;
		}
		final FedoraPackager packager = new FedoraPackager(fedoraProjectRoot);
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
		// do tasks as job
		Job job = new Job(FedoraPackagerText.get().uploadHandler_taskName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(FedoraPackagerText.get().uploadHandler_taskName, 1);

				if (sourceFile.getMissingSources().contains(resource.getName())) {
					// file already in sources and up-to-date
					return FedoraHandlerUtils
							.handleOK(
									MessageFormat.format(
											FedoraPackagerText.get().uploadHandler_versionExists,
											resource.getName())
							, true);
				}

				File newUploadFile = resource.getLocation().toFile();
				SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(fedoraProjectRoot,
						newUploadFile);
				sourcesUpdater.setShouldReplace(shouldReplaceSources());
				IFile gitIgnore = (IFile) fedoraProjectRoot.getContainer().findMember(new Path(".gitignore")); //$NON-NLS-1$
				if (gitIgnore == null) {
					gitIgnore = fedoraProjectRoot.getContainer().getFile(new Path(".gitignore")); //$NON-NLS-1$
				}
				VCSIgnoreFileUpdater vcsIgnoreFileUpdater = new VCSIgnoreFileUpdater(newUploadFile, gitIgnore);
				UploadSourceCommand uploadCmd = packager.uploadSources();
				
				try {
					uploadCmd.setUploadURL("http://upload-cgi/cgi-bin/upload.cgi");
				} catch (MalformedURLException e2) {
					// TODO Handle appropriately
					e2.printStackTrace();
				}
				try {
					uploadCmd.setFileToUpload(newUploadFile);
				} catch (InvalidUploadFileException e1) {
					// TODO: handle appropriately
					e1.printStackTrace();
				}
				uploadCmd.addCommandListener(sourcesUpdater);
				uploadCmd.addCommandListener(vcsIgnoreFileUpdater);
				UploadSourceResult result = null;
				try {
					result = uploadCmd.call(new SubProgressMonitor(monitor, 1));
				} catch (FileAvailableInLookasideCacheException e) {
					// TODO: handle appropriately
					e.printStackTrace();
				} catch (CommandListenerException e) {
					// TODO: handle appropriately
					e.printStackTrace();
				} catch (CommandMisconfiguredException e) {
					// TODO handle appropriately
					e.printStackTrace();
				} catch (UploadFailedException e) {
					// TODO handle appropriately
					e.printStackTrace();
				}

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				
				IStatus res = Status.OK_STATUS;
				if (!result.wasSuccessful()) {
					res = new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID, result.getErrorString());
				}

				// Do VCS update
				res = projectBits.updateVCS(fedoraProjectRoot, monitor);
				if (res.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
				
				// Refresh project
				IProject project = fedoraProjectRoot.getProject();
				if (project != null) {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						return FedoraHandlerUtils.handleError(e);
					}
				}
				
				return res;
			}

		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	/**
	 * Determines if {@code sources} file should be replaced or not.
	 * 
	 * @return {@code true} if and only if {@code sources} file should be
	 *         replaced with new content.
	 * @see NewSourcesHandler
	 */
	protected boolean shouldReplaceSources() {
		return false;
	}
}

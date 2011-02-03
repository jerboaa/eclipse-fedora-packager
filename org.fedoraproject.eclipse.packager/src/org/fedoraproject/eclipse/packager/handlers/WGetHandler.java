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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.fedoraproject.eclipse.packager.DownloadJob;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;

/**
 * Utility class for Upload/DownloadHandlers.
 *
 */
public abstract class WGetHandler extends CommonHandler {
	
	protected IProject project;
	
	protected IStatus retrieveSources(FedoraProjectRoot fedoraProjectRoot, IProgressMonitor monitor) {
		SourcesFile sourcesFile = fedoraProjectRoot.getSourcesFile();
		project = fedoraProjectRoot.getContainer().getProject();
	
		// check md5sum of any local sources
		Set<String> sourcesToGet = sourcesFile.getSourcesToDownload();
	
		if (sourcesToGet.isEmpty()) {
			return FedoraHandlerUtils.handleOK(FedoraPackagerText.get().wGetHandler_nothingToDownload, false);
		}
	
		// Need to download remaining sources from repo
		IStatus status = null;
		// get download URL from lookaside cache 
		String downloadBaseUrl = fedoraProjectRoot.getLookAsideCache().getDownloadUrl();
		for (final String source : sourcesToGet) {
			final String url = downloadBaseUrl
					+ "/" + project.getName() //$NON-NLS-1$
					+ "/" + source + "/" + sourcesFile.getSource(source) + "/" + source; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			status = download(fedoraProjectRoot, url, source, monitor);
			if (!status.isOK()) {
				// download failed
				try {
					sourcesFile.deleteSource(source);
				} catch (CoreException e) {
					e.printStackTrace();
					return FedoraHandlerUtils.handleError(e);
				}
				break;
			}
		}
	
		if (!status.isOK()) {
			return FedoraHandlerUtils.handleError(status.getMessage());
		}
	
		// sources downloaded successfully, check MD5
		sourcesToGet = sourcesFile.getSourcesToDownload();
	
		// if all checks pass we should have an empty list
		if (!sourcesToGet.isEmpty()) {
			String failedSources = ""; //$NON-NLS-1$
			for (String source : sourcesToGet) {
				failedSources += source + '\n';
			}
			return FedoraHandlerUtils.handleError(MessageFormat.format(FedoraPackagerText.get().wGetHandler_badMd5sum, failedSources));
		} else {
			return Status.OK_STATUS;
		}
	}
	
	private IStatus download(FedoraProjectRoot fedoraProjectRoot, String location, String fileName,
			IProgressMonitor monitor) {
		IFile file = null;
		try {
			URL url = new URL(location);
			file = fedoraProjectRoot.getContainer().getFile(new Path(fileName));

			// connect to repo
			URLConnection conn = url.openConnection();

			if (file.exists()) {
				return new DownloadJob(file, conn, true).run(monitor);
			} else {
				return new DownloadJob(file, conn).run(monitor);
			}
		} catch (MalformedURLException e) {
			// Url doesn't seem right
			return FedoraHandlerUtils.handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
			return FedoraHandlerUtils.handleError(MessageFormat.format(
					FedoraPackagerText.get().wGetHandler_couldNotCreate, fileName));

		}
		finally {
			// refresh folder in resource tree
			try {
				project.refreshLocal(IResource.DEPTH_ONE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
				return FedoraHandlerUtils.handleError(FedoraPackagerText.get().wGetHandler_couldNotRefresh);
			}
		}
	}
	
	protected IStatus updateSources(SourcesFile sources, File toAdd) {
		return updateSources(sources, toAdd, false);
	}

	protected IStatus updateSources(SourcesFile sourceFile, File toAdd,
			boolean forceOverwrite) {
		String filename = toAdd.getName();
		if (forceOverwrite) {
			sourceFile.getSources().clear();
		}
		sourceFile.getSources().put(filename, SourcesFile.getMD5(toAdd));

		try {
			sourceFile.save();
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

}

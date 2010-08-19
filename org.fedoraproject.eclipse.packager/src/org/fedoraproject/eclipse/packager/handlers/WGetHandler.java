package org.fedoraproject.eclipse.packager.handlers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.DownloadJob;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.Messages;
import org.fedoraproject.eclipse.packager.SourcesFile;

public abstract class WGetHandler extends CommonHandler {
	
	protected static final String uploadURL = "http://cvs.fedoraproject.org/repo/pkgs"; //$NON-NLS-1$
	
	protected IProject project;
	
	protected IStatus retrieveSources(FedoraProjectRoot fedoraProjectRoot, IProgressMonitor monitor) {
		SourcesFile sourcesFile = fedoraProjectRoot.getSourcesFile();
		project = fedoraProjectRoot.getContainer().getProject();
	
		// check md5sum of any local sources
		Set<String> sourcesToGet = sourcesFile.getSourcesToDownload();
	
		if (sourcesToGet.isEmpty()) {
			return handleOK(Messages.getString("WGetHandler.nothingToDownload"), false); //$NON-NLS-1$
		}
	
		// Need to download remaining sources from repo
		IStatus status = null;
		for (final String source : sourcesToGet) {
			final String url = uploadURL
					+ "/" + project.getName() //$NON-NLS-1$
					+ "/" + source + "/" + sourcesFile.getSource(source) + "/" + source; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			status = download(url, source, monitor);
			if (!status.isOK()) {
				// download failed
				try {
					sourcesFile.deleteSource(source);
				} catch (CoreException e) {
					e.printStackTrace();
					handleError(e);
				}
				break;
			}
		}
	
		if (!status.isOK()) {
			return handleError(status.getMessage());
		}
	
		// sources downloaded successfully, check MD5
		sourcesToGet = sourcesFile.getSourcesToDownload();
	
		// if all checks pass we should have an empty list
		if (!sourcesToGet.isEmpty()) {
			String failedSources = ""; //$NON-NLS-1$
			for (String source : sourcesToGet) {
				failedSources += source + '\n';
			}
			return handleError(Messages.getString("WGetHandler.badMd5sum") //$NON-NLS-1$
					+ failedSources);
		} else {
			return Status.OK_STATUS;
		}
	}
	
	private IStatus download(String location, String fileName,
			IProgressMonitor monitor) {
		IFile file = null;
		try {
			URL url = new URL(location);
			file = project.getFile(new Path(fileName));

			// connect to repo
			URLConnection conn = url.openConnection();

			if (file.exists()) {
				return new DownloadJob(file, conn, true).run(monitor);
			} else {
				return new DownloadJob(file, conn).run(monitor);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return handleError(NLS.bind(
					Messages.getString("WGetHandler.couldNotCreate"), fileName)); //$NON-NLS-1$

		} finally {
			// refresh folder in resource tree
			try {
				project.refreshLocal(IResource.DEPTH_ONE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
				return handleError(Messages.getString("WGetHandler.couldNotRefresh")); //$NON-NLS-1$
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

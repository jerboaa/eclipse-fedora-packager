package org.fedoraproject.eclipse.packager.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.LookasideCache;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;

/**
 * A class used to execute a {@code download sources} command. It has setters
 * for all supported options and arguments of this command and a
 * {@link #call(IProgressMonitor)} method to finally execute the command. Each
 * instance of this class should only be used for one invocation of the command
 * (means: one call to {@link #call(IProgressMonitor)})
 * 
 */
public class DownloadSourceCommand extends
		FedoraPackagerCommand<DownloadSourceResult> {

	private SourcesFile sources;
	private LookasideCache lookasideCache;
	
	/**
	 * @param projectRoot The project root abstraction.
	 */
	public DownloadSourceCommand(FedoraProjectRoot projectRoot) {
		super(projectRoot);
		this.sources = projectRoot.getSourcesFile();
		this.lookasideCache = projectRoot.getLookAsideCache();
	}
	
	/**
	 * @param downloadURL The URL to the download resource
	 * @return this instance.
	 * @throws MalformedURLException If an invalid URL has been provided.
	 */
	public DownloadSourceCommand setDownloadURL(String downloadURL) throws MalformedURLException {
		this.lookasideCache.setDownloadUrl(downloadURL);
		return this;
	}

	/**
	 * Implementation of the {@code DownloadSourcesCommand}.
	 * 
	 * @throws SourcesUpToDateException
	 *             If the source files are already up-to-date.
	 */
	@Override
	protected DownloadSourceResult doCall(IProgressMonitor monitor)
		throws SourcesUpToDateException, DownloadFailedException, InvalidCheckSumException {
		// Check if there are any sources to download (i.e. md5 does not match or
		// files are not present in the current Fedora project root).
		Set<String> sourcesToGet = sources.getSourcesToDownload();
		if (sourcesToGet.isEmpty()) {
			throw new SourcesUpToDateException(
					FedoraPackagerText.get().downloadSourceCommand_nothingToDownload);
		}
		// Need to download the rest of the files in the set from the lookaside
		// cache
		DownloadSourceResult result = new DownloadSourceResult();
		for (final String source : sourcesToGet) {
			final String url = lookasideCache.getDownloadUrl().toString()
					+ "/" + projectRoot.getProject().getName() //$NON-NLS-1$
					+ "/" + //$NON-NLS-1$
					source
					+ "/" + //$NON-NLS-1$
					sources.getSource(source)
					+ "/" + //$NON-NLS-1$
					source;
			
			IFile file = projectRoot.getContainer().getFile(new Path(source));
			URL sourceUrl = null; 
			try {
				sourceUrl = new URL(url);
			} catch (MalformedURLException e) {
				// TODO: Externalize
				throw new DownloadFailedException("Invalid URL", e);
			}
			
			try {
				download(monitor, file, sourceUrl);
			} catch (IOException e) {
				// clean-up and bail.
				try {
					sources.deleteSource(source);
				} catch (CoreException coreEx) { /* ignore */ }
				// TODO: Externalize
				throw new DownloadFailedException("Failed to download file: x", e);
			} catch (CoreException e) {
				// TODO: Externalize
				throw new DownloadFailedException("Ooops something unexpected happened", e);
			}
			// Make sure checksums are good
			checkMD5Sums();
			result.setSuccessful(true);
		}
		return result;
	}

	@Override
	protected void checkConfiguration() throws IllegalStateException {
		// We are good to go with the defaults. No-Op.
	}
	
	/**
	 * After sources have been downloaded, check MD5 sums of all of them.
	 * 
	 * Pre: Downloading sources finished successfully.
	 */
	private void checkMD5Sums() throws InvalidCheckSumException {
		// get 
		Set<String> sourcesToGet = sources.getSourcesToDownload();
	
		// if all checks pass we should have an empty list
		if (!sourcesToGet.isEmpty()) {
			StringBuilder failedSources = new StringBuilder(""); //$NON-NLS-1$ 
			for (String source : sourcesToGet) {
				failedSources.append("'" + source + "', "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			int end = -1;
			String badFiles = ""; //$NON-NLS-1$ 
			if ( (end = failedSources.lastIndexOf(", ")) > 0) { //$NON-NLS-1$ 
				badFiles = failedSources.substring(0, end);
			}
			// FIXME: String externalization
			throw new InvalidCheckSumException(MessageFormat.format(FedoraPackagerText.get().wGetHandler_badMd5sum, badFiles));
		}
	}
	
	/**
	 * Carry out the download for a given IFile.
	 * 
	 * @param monitor
	 * @param fileToDownload
	 * @param fileConnection
	 * @throws IOException If download failed.
	 * @throws CoreException Something else failed, unrecoverable error.
	 */
	@SuppressWarnings("static-access")
	private void download(IProgressMonitor monitor, IFile fileToDownload,
			URL fileURL) throws IOException, CoreException {
		URLConnection fileConnection = fileURL.openConnection();
		fileConnection = fileURL.openConnection();
		// FIXME: beginTask() may only be called once. This is not the case here.
		monitor.beginTask(
				MessageFormat.format(FedoraPackagerText.get().downloadJob_name,
						fileToDownload.getName()), fileConnection.getContentLength());
		File tempFile = File.createTempFile(fileToDownload.getName(), ""); //$NON-NLS-1$
		FileOutputStream fos = new FileOutputStream(tempFile);
		InputStream is = new BufferedInputStream(fileConnection.getInputStream());
		int bytesRead;
		boolean canceled = false;
		byte buf[] = new byte[5 * 1024]; // 5k buffer
		while ((bytesRead = is.read(buf)) != -1) {
			if (monitor.isCanceled()) {
				canceled = true;
				break;
			}
			fos.write(buf, 0, bytesRead);
			monitor.worked(bytesRead);
		}
		is.close();
		fos.close();
		if (!canceled) {
			if (fileToDownload.exists()) {
				// replace file
				fileToDownload.setContents(new FileInputStream(tempFile), true,
						false, monitor);
			} else {
				// create new file
				fileToDownload.create(new FileInputStream(tempFile), true, monitor);
			}
		}
		tempFile.delete();
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

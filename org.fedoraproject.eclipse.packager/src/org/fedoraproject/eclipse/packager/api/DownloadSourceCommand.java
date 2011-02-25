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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.LookasideCache;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
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
	 * @param monitor
	 *            The main progress monitor. Each file to download is executed
	 *            as a subtask.
	 * @throws SourcesUpToDateException
	 *             If the source files are already downloaded and up-to-date.
	 * @throws CommandMisconfiguredException
	 *             If the command was not properly configured when it was
	 *             called.
	 * @throws DownloadFailedException
	 *             If the download of some source failed.
	 * @throws CommandListenerException
	 *             If some listener detected a problem.
	 * @return The result of this command.
	 */
	@Override
	public DownloadSourceResult call(IProgressMonitor monitor)
			throws SourcesUpToDateException, DownloadFailedException,
			CommandMisconfiguredException,
			CommandListenerException {
		// Make sure listeners are properly called
		try {
			callPreExecListeners();
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof CommandMisconfiguredException) {
				// explicitly throw the specific exception
				throw (CommandMisconfiguredException)e.getCause();
			}
			throw e;
		}
		// Check if there are any sources to download (i.e. md5 does not match or
		// files are not present in the current Fedora project root).
		Set<String> sourcesToGet = sources.getMissingSources();
		if (sourcesToGet.isEmpty()) {
			throw new SourcesUpToDateException(
					FedoraPackagerText.get().downloadSourceCommand_nothingToDownload);
		}
		// Need to download the rest of the files in the set from the lookaside
		// cache
		DownloadSourceResult result = new DownloadSourceResult();
		int fileNumber = 1;
		for (final String source : sourcesToGet) {
			final String url = lookasideCache.getDownloadUrl().toString()
					+ "/" + projectRoot.getProject().getName() //$NON-NLS-1$
					+ "/" + //$NON-NLS-1$
					source
					+ "/" + //$NON-NLS-1$
					sources.getCheckSum(source)
					+ "/" + //$NON-NLS-1$
					source;
			
			IFile file = projectRoot.getContainer().getFile(new Path(source));
			URL sourceUrl = null; 
			try {
				sourceUrl = new URL(url);
			} catch (MalformedURLException e) {
				throw new DownloadFailedException(
						MessageFormat.format(
								FedoraPackagerText.get().downloadSourceCommand_invalidURL,
						url), e);
			}
			// indicate some progress
			monitor.subTask(MessageFormat.format(
								FedoraPackagerText.get().downloadSourceCommand_downloadingFileXofY,
						fileNumber, sourcesToGet.size()));
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
			try {
				download(subMonitor, file, sourceUrl);
			} catch (IOException e) {
				// clean-up and bail.
				try {
					sources.deleteSource(source);
				} catch (CoreException coreEx) { /* ignore */ }
				throw new DownloadFailedException(
						MessageFormat.format(
								FedoraPackagerText.get().downloadSourceCommand_downloadFile,
						file.getName()), e);
			} catch (CoreException e) {
				throw new DownloadFailedException(
						FedoraPackagerText.get().downloadSourceCommand_downloadFile, e);
			}
			fileNumber++;
		}
		// Call post-exec listeners
		callPostExecListeners();
		result.setSuccessful(true);
		setCallable(false);
		return result;
	}

	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// We are good to go with the defaults. No-Op.
	}

	/**
	 * Carry out the download for a given IFile.
	 * 
	 * @param subMonitor
	 *            A sub progress monitor to indicate progress for this file
	 *            only.
	 * @param fileToDownload
	 * @param fileConnection
	 * @throws IOException
	 *             If download failed.
	 * @throws CoreException
	 *             Something else failed, unrecoverable error.
	 */
	private void download(IProgressMonitor subMonitor, IFile fileToDownload,
			URL fileURL) throws IOException, CoreException {
		URLConnection fileConnection = fileURL.openConnection();
		fileConnection = fileURL.openConnection();
		subMonitor.beginTask(
				MessageFormat.format(FedoraPackagerText.get().downloadSourceCommand_downloadFile,
						fileToDownload.getName()), fileConnection.getContentLength());
		File tempFile = File.createTempFile(fileToDownload.getName(), ""); //$NON-NLS-1$
		FileOutputStream fos = new FileOutputStream(tempFile);
		InputStream is = new BufferedInputStream(fileConnection.getInputStream());
		int bytesRead;
		boolean canceled = false;
		byte buf[] = new byte[5 * 1024]; // 5k buffer
		while ((bytesRead = is.read(buf)) != -1) {
			if (subMonitor.isCanceled()) {
				canceled = true;
				break;
			}
			fos.write(buf, 0, bytesRead);
			subMonitor.worked(bytesRead);
		}
		is.close();
		fos.close();
		if (!canceled) {
			if (fileToDownload.exists()) {
				// replace file
				fileToDownload.setContents(new FileInputStream(tempFile), true,
						false, subMonitor);
			} else {
				// create new file
				fileToDownload.create(new FileInputStream(tempFile), true, subMonitor);
			}
		}
		tempFile.delete();
		subMonitor.done();
	}
}

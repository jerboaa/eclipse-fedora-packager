package org.fedoraproject.eclipse.packager.api;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.LookasideCache;
import org.fedoraproject.eclipse.packager.LookasideCache.CacheType;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * A class used to execute a {@code download sources} command. It has setters
 * for all supported options and arguments of this command and a {@link #call()}
 * method to finally execute the command. Each instance of this class should
 * only be used for one invocation of the command (means: one call to
 * {@link #call()})
 * 
 */
public class DownloadSourceCommand extends
		FedoraPackagerCommand<HttpResponse> {

	private File fileToDownload; // should probably be a list
	private HttpResponse response;
	private SourcesFile sources;
	private LookasideCache lookasideCache;
	
	/**
	 * @param projectRoot The project root abstraction.
	 * @param sources The sources file abstraction.
	 * @param lookasideCache The lookaside cache abstraction.
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
	 * @param fileToUpload the fileToUpload to set. Required.
	 * @return this instance.
	 */
	public DownloadSourceCommand setFileToUpload(File fileToUpload) {
		this.fileToDownload = fileToUpload;
		return this;
	}

	/**
	 * Executes the {@code UploadSources} command. Each instance of this class
	 * should only be used for one invocation of the command. Don't call this
	 * method twice on an instance.
	 * 
	 */
	@Override
	public HttpResponse call(IProgressMonitor monitor) {
		// Don't allow this very same instance to be called twice.
		checkCallable();
		// TODO Implement
		setCallable(false);
		return null;
	}

}

package org.fedoraproject.eclipse.packager.api;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.LookasideCache;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.api.errors.FileAvailableInLookasideCacheException;

/**
 * A class used to execute a {@code upload sources} command. It has setters for
 * all supported options and arguments of this command and a {@link #call()}
 * method to finally execute the command. Each instance of this class should
 * only be used for one invocation of the command (means: one call to
 * {@link #call()})
 * 
 */
public class UploadSourceCommand extends
		FedoraPackagerCommand<HttpResponse> {

	private final LookasideCache lookasideCache;
	private final SourcesFile sources;
	private File fileToUpload;
	private HttpResponse response;
	private boolean replaceSource = false;
	
	/**
	 * @param projectRoot The project root.
	 * 
	 */
	public UploadSourceCommand(FedoraProjectRoot projectRoot) {
		super(projectRoot);
		this.sources = projectRoot.getSourcesFile();
		this.lookasideCache = projectRoot.getLookAsideCache();
	}
	
	/**
	 * @param uploadURL the uploadURL to set. Optional.
	 * @return this instance.
	 * @throws MalformedURLException If the provided URL was not well formed.
	 */
	public UploadSourceCommand setUploadURL(String uploadURL) throws MalformedURLException {
		this.lookasideCache.setUploadUrl(uploadURL);
		return this;
	}

	/**
	 * @param fileToUpload the fileToUpload to set. Required.
	 * @return this instance.
	 */
	public UploadSourceCommand setFileToUpload(File fileToUpload) {
		this.fileToUpload = fileToUpload;
		return this;
	}

	/**
	 * @param replaceSource Set to true if sources should be replaced in
	 * sources file.
	 * @return this instance.
	 */
	public UploadSourceCommand setReplaceSource(boolean replaceSource) {
		this.replaceSource = replaceSource;
		return this;
	}
	

	/**
	 * Implementation of the {@code UploadSources} command.
	 * 
	 * @throws FileAvailableInLookasideCacheException If the to-be-uploaded file
	 * is still missing from the lookaside cache.
	 */
	@Override
	public HttpResponse call(IProgressMonitor monitor)
		throws FileAvailableInLookasideCacheException {
		//TODO implement
		return null;
	}
	
	@SuppressWarnings("static-access")
	@Override
	protected void checkConfiguration() throws IllegalStateException {
		if (this.fileToUpload == null) {
			throw new IllegalStateException(
					FedoraPackagerText.get().uploadSourceCommand_uploadFileUnspecified);
		}
	}

}

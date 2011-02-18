package org.fedoraproject.eclipse.packager.api;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.LookasideCache;
import org.fedoraproject.eclipse.packager.SourcesFile;

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
	 * @param cache The lookaside instance.
	 * @param sources The sources file instance.
	 * 
	 */
	public UploadSourceCommand(FedoraProjectRoot projectRoot,
			SourcesFile sources, LookasideCache cache) {
		super(projectRoot);
		this.sources = sources;
		this.lookasideCache = cache;
	}
	
	/**
	 * @param uploadURL the uploadURL to set. Optional.
	 * @return this instance.
	 */
	public UploadSourceCommand setUploadURL(String uploadURL) {
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
	 * Executes the {@code UploadSources} command. Each instance of this class
	 * should only be used for one invocation of the command. Don't call this
	 * method twice on an instance.
	 */
	@Override
	public HttpResponse call() throws Exception {
		// Don't allow this very same instance to be called twice.
		checkCallable();
		// TODO Implement
		// don't allow this instance to be called twice
		this.setCallable(false); 
		return null;
	}

}

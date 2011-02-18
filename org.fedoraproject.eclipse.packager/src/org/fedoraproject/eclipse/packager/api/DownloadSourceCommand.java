package org.fedoraproject.eclipse.packager.api;

import java.io.File;

import org.apache.http.HttpResponse;
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
	private CacheType lookasideType;
	
	/**
	 * @param projectRoot The project root abstraction.
	 * @param sources The sources file abstraction.
	 * @param lookasideCache The lookaside cache abstraction.
	 */
	public DownloadSourceCommand(FedoraProjectRoot projectRoot, SourcesFile sources,
			LookasideCache lookasideCache) {
		super(projectRoot);
		this.sources = sources;
		this.lookasideCache = lookasideCache;
		// Default to Fedora lookaside cache type
		this.lookasideType = CacheType.FEDORA;
	}
	
	/**
	 * @param downloadURL The URL to the download resource
	 * @return this instance.
	 */
	public DownloadSourceCommand setDownloadURL(String downloadURL) {
		this.lookasideCache.setDownloadUrl(downloadURL);
		return this;
	}
	
	/**
	 * Specify the lookaside cache type.
	 * 
	 * @param type Usually FEDORA, may be EPEL or something else.
	 * @return this instance
	 */
	public DownloadSourceCommand setLookasideType(CacheType type) {
		this.lookasideType = type;
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
	public HttpResponse call() {
		// Don't allow this very same instance to be called twice.
		checkCallable();
		// TODO Implement
		return null;
	}

}

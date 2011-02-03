package org.fedoraproject.eclipse.packager.api;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;

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

	/**
	 * Default upload URL if everything else fails.
	 */
	public static final String DEFAULT_UPLOAD_URL = 
		"https://pkgs.fedoraproject.org/repo/pkgs/upload.cgi"; //$NON-NLS-1$
	private URL uploadURL;
	private File fileToUpload;
	private HttpResponse response;
	
	/**
	 * @param projectRoot
	 */
	public UploadSourceCommand(FedoraProjectRoot projectRoot) {
		super(projectRoot);
		try {
			this.uploadURL = new URL(DEFAULT_UPLOAD_URL);
		} catch (MalformedURLException e) {
			// ignore
		}
	}
	
	/**
	 * @param uploadURL the uploadURL to set. Optional.
	 */
	public void setUploadURL(URL uploadURL) {
		this.uploadURL = uploadURL;
	}

	/**
	 * @param fileToUpload the fileToUpload to set. Required.
	 */
	public void setFileToUpload(File fileToUpload) {
		this.fileToUpload = fileToUpload;
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
		return null;
	}

}

package org.fedoraproject.eclipse.packager.api;

import java.io.File;
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

	public static final String DEFAULT_UPLOAD_URL = 
		;
	private URL uploadURL;
	private File fileToUpload;
	private HttpResponse response;
	
	/**
	 * @param projectRoot
	 */
	protected UploadSourceCommand(FedoraProjectRoot projectRoot) {
		super(projectRoot);
		setCallable(false);
	}
	
	/**
	 * @param uploadURL the uploadURL to set
	 */
	public void setUploadURL(URL uploadURL) {
		this.uploadURL = uploadURL;
	}

	/**
	 * @return the uploadURL
	 */
	public URL getUploadURL() {
		return uploadURL;
	}

	/**
	 * @param fileToUpload the fileToUpload to set
	 */
	public void setFileToUpload(File fileToUpload) {
		this.fileToUpload = fileToUpload;
	}

	/**
	 * @return the fileToUpload
	 */
	public File getFileToUpload() {
		return fileToUpload;
	}

	public HttpResponse call() throws Exception {
		// TODO Implement
		return null;
	}

}

package org.fedoraproject.eclipse.packager.bodhi.api.errors;

import org.apache.http.HttpResponse;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if an error occurred during bodhi interaction.
 *
 */
public class BodhiClientException extends FedoraPackagerAPIException {

	private HttpResponse response;
	
	private static final long serialVersionUID = -2076679215232309371L;
	
	/**
	 * @param msg
	 * @param cause
	 */
	public BodhiClientException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * @param message
	 * @param response
	 *            The HTTP response. Pass if some HTTP error occured. I.e.
	 *            status code != 200.
	 */
	public BodhiClientException(String message, HttpResponse response) {
		super(message);
		this.response = response;
	}

	/**
	 * @return The HTTP response if available, {@code null} otherwise.
	 */
	public HttpResponse getHttpResponse() {
		return this.response;
	}

}

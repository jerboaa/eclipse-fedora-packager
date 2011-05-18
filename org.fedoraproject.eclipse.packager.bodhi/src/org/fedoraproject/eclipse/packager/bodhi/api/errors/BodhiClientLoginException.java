package org.fedoraproject.eclipse.packager.bodhi.api.errors;

import org.apache.http.HttpResponse;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if an error related to bodhi client logins occurred.
 *
 */
public class BodhiClientLoginException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 8396679855266934702L;
	private HttpResponse response;
	
	/**
	 * @param msg
	 * @param cause
	 */
	public BodhiClientLoginException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * @param message
	 * @param response
	 *            The HTTP response. Pass if some HTTP error occured. I.e.
	 *            status code != 200.
	 */
	public BodhiClientLoginException(String message, HttpResponse response) {
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

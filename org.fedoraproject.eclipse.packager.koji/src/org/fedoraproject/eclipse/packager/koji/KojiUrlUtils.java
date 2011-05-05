package org.fedoraproject.eclipse.packager.koji;

import java.net.URL;

/**
 * Helper dealing with task URLs.
 *
 */
public class KojiUrlUtils {
	
	/**
	 * Construct the correct URL to a task on koji.
	 * 
	 * @param taskId
	 * @param kojiWebUrl
	 * @return The URL as a string.
	 */
	public static String constructTaskUrl(int taskId, URL kojiWebUrl) {
		return kojiWebUrl.toString() + "/taskinfo?taskID=" + taskId; //$NON-NLS-1$
	}

}

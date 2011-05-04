package org.fedoraproject.eclipse.packager.koji.api;

import java.util.Map;

/**
 * Class representing build info as returned by
 * getBuild XMLRPC call.  
 *
 */
public class KojiBuildInfo {

	private String state;
	private int taskId;
	/**
	 * Construct the build info from the map
	 * returned by the API call.
	 * 
	 * @param buildInfo
	 */
	public KojiBuildInfo(Map<String, Object> buildInfo) {
		parseBuildInfo(buildInfo);
	}

	/**
	 * @return {@code true} if the associated build of this build info is
	 *         complete.
	 */
	public boolean isComplete() {
		return state.equals(Integer.valueOf(1));
	}
	
	/**
	 * @return The task id this build.
	 */
	public int getTaskId() {
		return this.taskId;
	}
	
	private void parseBuildInfo(Map<String, Object> buildInfo) {
		// TODO: Use constants for keys
		this.state = (String)buildInfo.get("state"); //$NON-NLS-1$
		try {
			this.taskId = Integer.parseInt((String) buildInfo.get("task_id")); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
}

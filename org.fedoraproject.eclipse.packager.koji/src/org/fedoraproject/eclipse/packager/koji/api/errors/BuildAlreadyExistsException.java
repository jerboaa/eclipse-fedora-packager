package org.fedoraproject.eclipse.packager.koji.api.errors;

import org.fedoraproject.eclipse.packager.koji.api.KojiClientException;

/**
 * Thrown if some build already existed, when another one was attempted to be
 * pushed to koji.
 * 
 */
public class BuildAlreadyExistsException extends KojiClientException {

	private static final long serialVersionUID = 5322603068319243734L;
	
	/* existing task ID */
	private int taskId;
	
	/**
	 * @param taskId The task ID, which already existed.
	 */
	public BuildAlreadyExistsException(int taskId) {
		this.taskId = taskId;
	}
	
	@Override
	public String getMessage() {
		// TODO externalize
		return "Build already exists (id=" + this.taskId + ", state=COMPLETE)";
	}
	
	/**
	 * @return The task ID of the existing build.
	 */
	public int getTaskId() {
		return taskId;
	}

}

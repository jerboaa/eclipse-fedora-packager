/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.koji.api.errors;

import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.koji.KojiText;

/**
 * Thrown if some build already existed, when another one was attempted to be
 * pushed to koji.
 * 
 */
public class BuildAlreadyExistsException extends KojiHubClientException {

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
		return NLS.bind(KojiText.BuildAlreadyExistsException_msg, this.taskId);
	}
	
	/**
	 * @return The task ID of the existing build.
	 */
	public int getTaskId() {
		return taskId;
	}

}

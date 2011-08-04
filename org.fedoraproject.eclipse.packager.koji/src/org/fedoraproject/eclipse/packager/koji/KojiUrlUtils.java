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

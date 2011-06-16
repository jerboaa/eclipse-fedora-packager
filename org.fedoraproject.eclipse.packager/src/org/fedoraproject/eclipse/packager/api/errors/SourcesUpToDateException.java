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
package org.fedoraproject.eclipse.packager.api.errors;


/**
 * Thrown if sources are already present and up-to-date when a {@code DownloadSourcesCommand}
 * is triggered.
 *
 */
public class SourcesUpToDateException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = -4530245999844216895L;
	/**
	 * @param message
	 * @param cause
	 */
	public SourcesUpToDateException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public SourcesUpToDateException(String message) {
		super(message);
	}
}

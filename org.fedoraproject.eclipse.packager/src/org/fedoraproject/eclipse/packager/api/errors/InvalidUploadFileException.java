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

import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Thrown if an upload file is invalid as determined by
 * {@link FedoraPackagerUtils#isValidUploadFile(java.io.File)}.
 */
public class InvalidUploadFileException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = -7299319057739321380L;
	
	/**
	 * @param message
	 * @param cause
	 */
	public InvalidUploadFileException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public InvalidUploadFileException(String message) {
		super(message);
	}
}

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


import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;

/**
 * Thrown if a file with a specific MD5 is already available in
 * the lookaside cache.
 */
public class FileAvailableInLookasideCacheException extends
		FedoraPackagerAPIException {

	private static final long serialVersionUID = -1682359355021803771L;

	/**
	 * @param fileName The available filename.
	 */
	public FileAvailableInLookasideCacheException(String fileName) {
		super(
				NLS.bind(FedoraPackagerText.FileAvailableInLookasideCacheException_message,
								fileName));
	}
}

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
package org.fedoraproject.eclipse.packager;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class responsible for management of the
 * lookaside cache.
 *
 */
public class LookasideCache implements ILookasideCache {
	
	/**
	 * Default upload URL for the Fedora lookaside cache
	 */
	public static final String DEFAULT_FEDORA_UPLOAD_URL = "https://pkgs.fedoraproject.org/repo/pkgs/upload.cgi"; //$NON-NLS-1$
	/**
	 * Default download URL for the Fedora lookaside cache
	 */
	public static final String DEFAULT_FEDORA_DOWNLOAD_URL = "http://pkgs.fedoraproject.org/repo/pkgs"; //$NON-NLS-1$
	
	
	private URL downloadUrl;
	private URL uploadUrl;
	
	/**
	 * Create lookaside cache abstraction.
	 * 
	 */
	LookasideCache() {
		try {
			this.downloadUrl = new URL(DEFAULT_FEDORA_DOWNLOAD_URL);
			this.uploadUrl = new URL(DEFAULT_FEDORA_UPLOAD_URL);
		} catch (MalformedURLException e) {
			// ignore
		}
	}

	/**
	 * @return the proper download URL for this lookaside cache type.
	 */
	@Override
	public URL getDownloadUrl() {
		return this.downloadUrl;
	}

	/**
	 * Set the downloadUrl for this lookaside cache type.
	 * 
	 * @param downloadUrl The new download URL. 
	 * @throws MalformedURLException If the new URL is invalid.
	 */
	@Override
	public void setDownloadUrl(String downloadUrl) throws MalformedURLException {
		this.downloadUrl = new URL(downloadUrl);
	}

	/**
	 * @return the uploadUrl for this lookaside cache type.
	 */
	@Override
	public URL getUploadUrl() {
		return this.uploadUrl;
	}

	/**
	 * Set the uploadUrl for this lookaside cache type.
	 * 
	 * @param uploadUrl The new upload URL. 
	 * @throws MalformedURLException If the new URL is invalid.
	 */
	@Override
	public void setUploadUrl(String uploadUrl) throws MalformedURLException {
		this.uploadUrl = new URL(uploadUrl);
	}
}

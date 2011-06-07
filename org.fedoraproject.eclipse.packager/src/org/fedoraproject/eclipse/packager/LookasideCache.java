/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *      Red Hat - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for management of the
 * lookaside cache.
 *
 */
public class LookasideCache {
	
	/**
	 * Default upload URL for the Fedora lookaside cache
	 */
	public static final String DEFAULT_FEDORA_UPLOAD_URL = "https://pkgs.fedoraproject.org/repo/pkgs/upload.cgi"; //$NON-NLS-1$
	/**
	 * Default download URL for the Fedora lookaside cache
	 */
	public static final String DEFAULT_FEDORA_DOWNLOAD_URL = "http://pkgs.fedoraproject.org/repo/pkgs"; //$NON-NLS-1$
	
	private Map<CacheType, URL[]> urlMap;
	private static final int UPLOAD_URL_INDEX = 0;
	private static final int DOWNLOAD_URL_INDEX = 1;
	private CacheType type;
	
	/**
	 * Allow for various types of lookaside caches. E.g. Fedora, EPEL, etc.
	 * For now only FEDORA is supported.
	 */
	public static enum CacheType {
		/**
		 * Fedora lookaside cache type.
		 */
		FEDORA
	}
	
	/**
	 * Create lookaside cache of the requested type.
	 * 
	 * @param type The cache type to create.
	 */
	public LookasideCache(CacheType type) {
		this.urlMap = new HashMap<CacheType, URL[]>();
		URL[] urls = new URL[2];
		switch (type) {
			case FEDORA:
				try {
					urls[UPLOAD_URL_INDEX] = new URL(DEFAULT_FEDORA_UPLOAD_URL);
					urls[DOWNLOAD_URL_INDEX] = new URL(DEFAULT_FEDORA_DOWNLOAD_URL);
				} catch (MalformedURLException e) {
					//ignore
				}
				break;
			default:
				// default to Fedora
				try {
					urls[UPLOAD_URL_INDEX] = new URL(DEFAULT_FEDORA_UPLOAD_URL);
					urls[DOWNLOAD_URL_INDEX] = new URL(DEFAULT_FEDORA_DOWNLOAD_URL);
				} catch (MalformedURLException e) {
					//ignore
				}
				break;
		}
		this.urlMap.put(type, urls);
		this.type = type;
	}

	/**
	 * @return the proper download URL for this lookaside cache type.
	 */
	public URL getDownloadUrl() {
		return this.urlMap.get(this.type)[DOWNLOAD_URL_INDEX];
	}

	/**
	 * Set the downloadUrl for this lookaside cache type.
	 * 
	 * @param downloadUrl The new download URL. 
	 * @throws MalformedURLException If the new URL is invalid.
	 */
	public void setDownloadUrl(String downloadUrl) throws MalformedURLException {
		this.urlMap.get(this.type)[DOWNLOAD_URL_INDEX] = new URL(downloadUrl);
	}

	/**
	 * @return the uploadUrl for this lookaside cache type.
	 */
	public URL getUploadUrl() {
		return this.urlMap.get(this.type)[UPLOAD_URL_INDEX];
	}

	/**
	 * Set the uploadUrl for this lookaside cache type.
	 * 
	 * @param uploadUrl The new upload URL. 
	 * @throws MalformedURLException If the new URL is invalid.
	 */
	public void setUploadUrl(String uploadUrl) throws MalformedURLException {
		this.urlMap.get(this.type)[UPLOAD_URL_INDEX] = new URL(uploadUrl);
	}
}

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
 * Interface for Lookaside cache implementations. Users are free to implement
 * this interface and use its implementation via the
 * {@code lookasideCacheProvider} extension point.
 * 
 */
public interface ILookasideCache {

	/**
	 * Allow for various types of lookaside caches. E.g. Fedora, EPEL, etc.
	 * For now only FEDORA is supported.
	 */
	public static enum CacheType {
		/**
		 * Fedora lookaside cache type.
		 */
		FEDORA,
		/**
		 * EPEL lookaside cache type.
		 */
		EPEL
	}
	
	/**
	 * @return the proper download URL for this lookaside cache type.
	 */
	public URL getDownloadUrl();

	/**
	 * Set the downloadUrl for this lookaside cache type.
	 * 
	 * @param downloadUrl The new download URL. 
	 * @throws MalformedURLException If the new URL is invalid.
	 */
	public void setDownloadUrl(String downloadUrl) throws MalformedURLException;

	/**
	 * @return the uploadUrl for this lookaside cache type.
	 */
	public URL getUploadUrl();

	/**
	 * Set the uploadUrl for this lookaside cache type.
	 * 
	 * @param uploadUrl The new upload URL. 
	 * @throws MalformedURLException If the new URL is invalid.
	 */
	public void setUploadUrl(String uploadUrl) throws MalformedURLException;
}

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
	 * Initialize an ILookasideCache object to the requested type. This method
	 * is called on object creation.
	 * 
	 * @param type
	 *            The cache type to create.
	 */
	public void initialize(CacheType type);
	
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

package org.fedoraproject.eclipse.packager;

/**
 * Interface for the Lookaside cache which should
 * know where to upload/download things.
 */
public interface ILookasideCache {
	
	/**
	 * Common command prefix for Eclipse Fedorapackager command IDs.
	 */
	public static final String FEDORA_PACKAGER_CMD_PREFIX = "org.fedoraproject.eclipse.packager"; //$NON-NLS-1$
	
	/**
	 * @return The upload URL of the lookaside cache.
	 */
	public String getUploadUrl();
	
	/**
	 * @return The download URL of the lookaside cache. 
	 */
	public String getDownloadUrl();
}

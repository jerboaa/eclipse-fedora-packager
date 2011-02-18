package org.fedoraproject.eclipse.packager.api;

import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.LookasideCache;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;

/**
 * 
 * @author Severin Gehwolf
 *
 */
public class FedoraPackager {
	private final FedoraProjectRoot root;
	private final SourcesFile sources;
	private final LookasideCache lookasideCache;
	
	/**
	 * @param root
	 */
	public FedoraPackager(FedoraProjectRoot root) {
		if (root == null)
			throw new NullPointerException();
		this.root = root;
		this.sources = null;
		this.lookasideCache = null;
	}
	
	/**
	 * @return An upload source command.
	 */
	public UploadSourceCommand uploadSources() {
		return new UploadSourceCommand(root, sources, lookasideCache);
	}
	
	/**
	 * @return An download source command.
	 */
	public DownloadSourceCommand downloadSources() {
		return new DownloadSourceCommand(root, sources, lookasideCache);
	}
}

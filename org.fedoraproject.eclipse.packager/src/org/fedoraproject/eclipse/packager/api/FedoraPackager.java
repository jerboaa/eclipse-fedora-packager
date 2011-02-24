package org.fedoraproject.eclipse.packager.api;

import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;

/**
 * Eclipse Fedora Packager main interface for commands.
 */
public class FedoraPackager {
	private final FedoraProjectRoot root;
	
	/**
	 * @param root
	 */
	public FedoraPackager(FedoraProjectRoot root) {
		if (root == null)
			throw new NullPointerException();
		this.root = root;
	}
	
	/**
	 * @return An upload source command.
	 */
	public UploadSourceCommand uploadSources() {
		return new UploadSourceCommand(root);
	}
	
	/**
	 * @return An download source command.
	 */
	public DownloadSourceCommand downloadSources() {
		return new DownloadSourceCommand(root);
	}
	
	/**
	 * Get the underlying Fedora project root
	 * 
	 * @return The Fedora project root.
	 */
	public FedoraProjectRoot getFedoraProjectRoot() {
		return this.root;
	}
}

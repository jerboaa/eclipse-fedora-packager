package org.fedoraproject.eclipse.packager.api;

import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.handlers.UploadHandler;

/**
 * 
 * @author sgehwolf
 *
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
	public UploadSourceCommand upload() {
		return new UploadSourceCommand(root);
	}
}

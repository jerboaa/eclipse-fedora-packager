package org.fedoraproject.eclipse.packager.koji.api;

import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.ICommandListener;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * A listener for post sources download MD5 checking.
 */
public class UnpushedChangesListener implements ICommandListener {

	/**
	 * The fedora project root to work with.
	 */
	private FedoraProjectRoot projectRoot;
	
	/**
	 * Create a MD5Sum checker
	 * 
	 * @param root The Fedora project root.
	 */
	public UnpushedChangesListener(FedoraProjectRoot root) {
		this.projectRoot = root;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#preExecution()
	 */
	@Override
	public void preExecution() throws CommandListenerException {
		// TODO: implement
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#postExecution()
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		// nothing
	}

}

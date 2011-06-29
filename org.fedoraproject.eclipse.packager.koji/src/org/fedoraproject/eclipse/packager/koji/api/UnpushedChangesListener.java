package org.fedoraproject.eclipse.packager.koji.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.ICommandListener;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.api.errors.UnpushedChangesException;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * A listener which checks if there have been unpushed changes on the current
 * branch. This is useful to check prior pushing a build to Koji.
 */
public class UnpushedChangesListener implements ICommandListener {

	/**
	 * The fedora project root to work with.
	 */
	private IProjectRoot projectRoot;
	private IProgressMonitor mainMonitor;
	
	/**
	 * Create a MD5Sum checker
	 * 
	 * @param root The Fedora project root.
	 * @param monitor The main monitor to create a submonitor from.
	 */
	public UnpushedChangesListener(IProjectRoot root, IProgressMonitor monitor) {
		this.projectRoot = root;
		this.mainMonitor = monitor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#preExecution()
	 */
	@Override
	public void preExecution() throws CommandListenerException {
		// indicate some progress, by creating a subtask
		FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		logger.logInfo(KojiText.UnpushedChangesListener_checkUnpushedChangesMsg);
		mainMonitor.subTask(KojiText.UnpushedChangesListener_checkUnpushedChangesMsg);
		IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(projectRoot);
		if (projectBits.hasLocalChanges(projectRoot)) {
			throw new CommandListenerException(new UnpushedChangesException(KojiText.UnpushedChangesListener_unpushedChangesError));
		}
		mainMonitor.worked(15);
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

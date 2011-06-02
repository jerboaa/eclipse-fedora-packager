package org.fedoraproject.eclipse.packager.koji.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.QuestionMessageDialog;
import org.fedoraproject.eclipse.packager.api.ICommandListener;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * A listener for post sources download MD5 checking.
 */
public class TagSourcesListener implements ICommandListener {

	/**
	 * The fedora project root to work with.
	 */
	private FedoraProjectRoot projectRoot;
	private IProgressMonitor mainMonitor;
	private Shell shell;
	
	/**
	 * Create a MD5Sum checker
	 * 
	 * @param root The Fedora project root.
	 * @param monitor The main monitor to create a submonitor from.
	 * @param shell The shell to be used for message dialog prompting
	 */
	public TagSourcesListener(FedoraProjectRoot root, IProgressMonitor monitor, Shell shell) {
		this.projectRoot = root;
		this.mainMonitor = monitor;
		this.shell = shell;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#preExecution()
	 */
	@Override
	public void preExecution() throws CommandListenerException {
		// indicate some progress, by creating a subtask
		mainMonitor.subTask(KojiText.TagSourcesListener_tagSourcesMsg);
		IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(projectRoot);
		if (projectBits.needsTag()) {
			// Do VCS tagging if so requested.
			if (askIfShouldTag()) {
				FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
				logger.logInfo(KojiText.TagSourcesListener_tagSourcesMsg);
				projectBits.tagVcs(projectRoot, mainMonitor);
			}
		}
		mainMonitor.worked(20);
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#postExecution()
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		// nothing
	}
	
	/**
	 * Ask for tagging of sources. This is only necessary for CVS based source
	 * control.
	 * 
	 * @param shell
	 * @return {@code true} if the user requested a tag of sources.
	 */
	private boolean askIfShouldTag() {
		QuestionMessageDialog op = new QuestionMessageDialog(
				KojiText.KojiBuildHandler_tagBeforeSendingBuild, shell, this.projectRoot);
		Display.getDefault().syncExec(op);
		return op.isOkPressed();
	}

}

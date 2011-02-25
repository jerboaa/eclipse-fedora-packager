package org.fedoraproject.eclipse.packager.api;

import java.text.MessageFormat;
import java.util.Set;

import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;

/**
 * A listener for post sources download MD5 checking.
 */
public class ChecksumValidListener implements ICommandListener {

	/**
	 * The fedora project root to work with.
	 */
	private FedoraProjectRoot projectRoot;
	
	/**
	 * Create a MD5Sum checker
	 * 
	 * @param root The Fedora project root.
	 */
	public ChecksumValidListener(FedoraProjectRoot root) {
		this.projectRoot = root;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#preExecution()
	 */
	@Override
	public void preExecution() throws CommandListenerException {
		// Nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandListener#postExecution()
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		// do the MD5 check
		Set<String> sourcesToGet = projectRoot.getSourcesFile()
				.getMissingSources();
	
		// if all checks pass we should have an empty list
		if (!sourcesToGet.isEmpty()) {
			StringBuilder failedSources = new StringBuilder(""); //$NON-NLS-1$ 
			for (String source : sourcesToGet) {
				failedSources.append("'" + source + "', "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			int end = -1;
			String badFiles = ""; //$NON-NLS-1$ 
			if ( (end = failedSources.lastIndexOf(", ")) > 0) { //$NON-NLS-1$ 
				badFiles = failedSources.substring(0, end);
			}
			// FIXME: String externalization
			throw new CommandListenerException(new InvalidCheckSumException(MessageFormat.format(FedoraPackagerText.get().wGetHandler_badMd5sum, badFiles)));
		}
	}

}

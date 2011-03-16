package org.fedoraproject.eclipse.packager.api;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesFileUpdateException;

/**
 * Post exec hook for {@link UploadSourceCommand}, responsible for updating the
 * {@code sources} file.
 *
 */
public class SourcesFileUpdater implements ICommandListener {
	
	private FedoraProjectRoot fpRoot;
	private File fileToAdd;
	private boolean shouldReplace = false;

	/**
	 * Create a SourcesFileUpdater for this project root.
	 * 
	 * @param fpRoot
	 *            The Fedora project root for which to update sources for.
	 * @param fileToAdd
	 *            The file which should get added to the {@code sources} file.
	 */
	public SourcesFileUpdater(FedoraProjectRoot fpRoot, File fileToAdd) {
		this.fpRoot = fpRoot;
		this.fileToAdd = fileToAdd;
	}

	/**
	 * Setter for state info if {@code sources} file should be replaced or not.
	 * 
	 * @param newValue
	 */
	public void setShouldReplace(boolean newValue) {
		this.shouldReplace = newValue;
	}

	@Override
	public void preExecution() throws CommandListenerException {
		// nothing
	}

	/**
	 * Updates the {@code sources} file for the Fedora project root of this
	 * instance and add the new file as required.
	 * 
	 * @throws CommandListenerException
	 *             If an error occurred during updating the {@code sources}
	 *             file. Use {@link Throwable#getCause()} to determine the
	 *             actual cause of the exception.
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		String filename = fileToAdd.getName();
		Map<String, String> sources = fpRoot.getSourcesFile().getSources();
		if (shouldReplace) {
			sources.clear();
		}
		sources.put(filename, SourcesFile.calculateChecksum(fileToAdd));
		fpRoot.getSourcesFile().setSources(sources);

		try {
			fpRoot.getSourcesFile().save();
		} catch (CoreException e) {
			throw new CommandListenerException(new SourcesFileUpdateException(
					FedoraPackagerText.SourcesFileUpdater_errorSavingFile, e));
		}
	}
}

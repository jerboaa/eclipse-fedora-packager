package org.fedoraproject.eclipse.packager.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Post exec hook for {@link UploadSourceCommand}, responsible for updating VCS
 * ignore file (such as .gitignore). TODO: This should probably use VCS project
 * bits in future.
 * 
 */
public class VCSIgnoreFileUpdater implements ICommandListener {
	
	private File ignoredFile;
	private boolean shouldReplace = false;
	private File vcsIgnoreFile;

	/**
	 * Create a VCSIgnoreFileUpdater for this project root.
	 * 
	 * @param ignoredFile
	 *            The file which should get added to the {@code sources} file.
	 * @param vcsIgnoreFile
	 *            The file of the VCS which is responsible for it to not track
	 *            some source file.
	 */
	public VCSIgnoreFileUpdater(File ignoredFile,
			File vcsIgnoreFile) {
		this.ignoredFile = ignoredFile;
		this.vcsIgnoreFile = vcsIgnoreFile;
	}

	/**
	 * Setter for state info if content of VCS ignore file should be replaced or
	 * not.
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
	 * Updates the VCS ignore file for the Fedora project root of this instance
	 * as required.
	 * 
	 * @throws CommandListenerException
	 *             If an error occurred during the update. Use
	 *             {@link Throwable#getCause()} to determine the actual cause of
	 *             the exception.
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		String filename = ignoredFile.getName();
		ArrayList<String> ignoreFiles = new ArrayList<String>();
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			if (shouldReplace) {
				pw = new PrintWriter(new FileWriter(vcsIgnoreFile, false));
				pw.println(filename);
			} else {
				// only append to file if not already present
				br = new BufferedReader(new FileReader(vcsIgnoreFile));

				String line = br.readLine();
				while (line != null) {
					ignoreFiles.add(line);
					line = br.readLine();
				}

				if (!ignoreFiles.contains(filename)) {
					pw = new PrintWriter(new FileWriter(vcsIgnoreFile, true));
					pw.println(filename);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}

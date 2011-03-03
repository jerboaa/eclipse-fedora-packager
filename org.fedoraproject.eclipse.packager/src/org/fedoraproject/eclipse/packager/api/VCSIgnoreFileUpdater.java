package org.fedoraproject.eclipse.packager.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Post exec hook for {@link UploadSourceCommand}, responsible for updating VCS
 * ignore file (such as {@code .gitignore}). TODO: This should probably use VCS
 * project bits in future.
 * 
 */
public class VCSIgnoreFileUpdater implements ICommandListener {
	
	private File newIgnoredFileCandidate;
	private boolean shouldReplace = false;
	private IFile vcsIgnoreFile; // may not exist

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
			IFile vcsIgnoreFile) {
		this.newIgnoredFileCandidate = ignoredFile;
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
	 * Updates the VCS ignore file for the Fedora project root of this instance.
	 * 
	 * @throws CommandListenerException
	 *             If an error occurred during the update. Use
	 *             {@link Throwable#getCause()} to determine the actual cause of
	 *             the exception.
	 */
	@Override
	public void postExecution() throws CommandListenerException {
		try {
			createVCSFileIfNotExistent();
		} catch (CoreException e) {
			throw new CommandListenerException(e);
		}
		String filename = newIgnoredFileCandidate.getName();
		ArrayList<String> ignoreFiles = new ArrayList<String>();
		final PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = null;
		PrintWriter pw = null;
		final IFile file = vcsIgnoreFile;
		try {
			out = new PipedOutputStream(in);
			pw = new PrintWriter(out);
			if (shouldReplace) {
				pw.println(filename);
			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader(vcsIgnoreFile.getContents()));
				String line = br.readLine();
				while (line != null) {
					ignoreFiles.add(line);
					pw.println(line);
					line = br.readLine();
				}
				// only append to file if not already present
				if (!ignoreFiles.contains(filename)) {
					pw.println(filename);
				}
			}
			// Close output end of pipe
			pw.close();
			out.close();
			
			Job job = new Job(FedoraPackagerText.get().sourcesFile_saveJob) {

				@Override
				public IStatus run(IProgressMonitor monitor) {
					try {
						// Potentially long running so do as job.
						file.setContents(in, false, true, monitor);
						file.getParent().refreshLocal(IResource.DEPTH_ONE, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			throw new CommandListenerException(e);
		} catch (CoreException e) {
			throw new CommandListenerException(e);
		}
		finally {
			if (pw != null) {
				pw.close();
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * Creates the VCS ignore file if it does not exist.
	 * 
	 * @throws CoreException If creation of VCS ignore file fails.
	 */
	private void createVCSFileIfNotExistent() throws CoreException {
		if (vcsIgnoreFile.exists()) {
			return; // done
		}
		// Start out empty
		ByteArrayInputStream in = new ByteArrayInputStream("".getBytes()); //$NON-NLS-1$
		vcsIgnoreFile.create(in, false, null);
	}
}

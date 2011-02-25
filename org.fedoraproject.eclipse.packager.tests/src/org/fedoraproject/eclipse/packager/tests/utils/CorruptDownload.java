package org.fedoraproject.eclipse.packager.tests.utils;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.ICommandListener;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;

/**
 * Action to intentionally alter the MD5sum of a downloaded
 * source file. This should be used for testing purposes only.
 *
 */
public class CorruptDownload implements ICommandListener {
	
	private FedoraProjectRoot fedoraProjectRoot;
	
	public CorruptDownload(FedoraProjectRoot fpRoot) {
		this.fedoraProjectRoot = fpRoot;
	}

	public void preExecution() throws CommandListenerException {
		// nothing
	}

	/**
	 * Intentionally destroy MD5sums of sources files.
	 */
	public void postExecution() throws CommandListenerException {
		String extraContents = "0xbeef";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(extraContents.getBytes());
		SourcesFile sources = fedoraProjectRoot.getSourcesFile();
		for (String filename: sources.getAllSources()) {
			IFile sourceFile;
				sourceFile = (IFile)fedoraProjectRoot.getContainer().findMember(new Path(filename));
			if (sourceFile != null) {
				try {
					sourceFile.appendContents(inputStream, IResource.FORCE, new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			fedoraProjectRoot.getContainer().refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e) {
			// ignore
		}

	}

}

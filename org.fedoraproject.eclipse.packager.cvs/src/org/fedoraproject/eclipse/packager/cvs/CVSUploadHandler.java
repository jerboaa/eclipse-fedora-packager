package org.fedoraproject.eclipse.packager.cvs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.handlers.UploadHandler;

/**
 * Handles upload commands (CSV implementation)
 * 
 * @author Red Hat Inc.
 *
 */
@SuppressWarnings("restriction")
public class CVSUploadHandler extends UploadHandler {
	
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
			throws ExecutionException {
		// do nothing. Why?
		return Status.OK_STATUS;
	}
	
	@Override
	/**
	 *  Performs upload of sources (independent of VCS used), updates "sources"
	 *  file and performs necessary CVS operations to bring branch in sync.
	 */
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		// Do the file uploading
		IStatus status = (IStatus)super.execute(e);
		
		final IResource resource = getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = getValidRoot(resource);
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
		
		// Do rest of work if uploading was Ok.
		if (status.isOK()) {
			
			//Update sources file
			final File toAdd = resource.getLocation().toFile();
			status = updateSources(sourceFile, toAdd);
			if (!status.isOK()) {
				// fail updating sources file
			}

			// Handle CVS specific stuff; Update .cvsignore
			final File cvsignore = new File(fedoraProjectRoot
					.getContainer().getLocation().toString()
					+ IPath.SEPARATOR + ".cvsignore"); //$NON-NLS-1$
			status = updateCVSIgnore(cvsignore, toAdd);
			if (!status.isOK()) {
				// fail updating sources file
			}
			
			// Do CVS update
			job = new Job(org.fedoraproject.eclipse.packager.Messages.getString("FedoraPackager.jobName")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor
							.beginTask(
									Messages.getString("CVSUploadHandler.doCvsOps"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

					final IStatus result = updateCVS(sourceFile, cvsignore, monitor);
					if (result.isOK()) {
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
					return result;
				}
			};
			job.setUser(true);
			job.schedule();
		}
		return null;
	}
	
	/**
	 * Update .cvsignore file. Appends to file.
	 * 
	 * @param cvsignore
	 * @param toAdd
	 * @return
	 */
	protected IStatus updateCVSIgnore(File cvsignore, File toAdd) {
		return updateCVSIgnore(cvsignore, toAdd, false);
	}
	
	/**
	 * Run CVS update/add on sources, and .cvsignore file
	 * 
	 * @param sources
	 * @param cvsignore
	 * @param monitor
	 * @return	Status of the operation performed.
	 */
	protected IStatus updateCVS(SourcesFile sources, File cvsignore,
			IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		// get CVSProvider
		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
				.getProvider(specfile.getProject(),
						CVSProviderPlugin.getTypeId());

		try {
			ICVSRepositoryLocation location = provider.getRemoteLocation();

			// get CVSROOT
			CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
			ICVSFolder rootFolder = cvsRoot.getLocalRoot();

			// get Branch
			ICVSFolder branchFolder = rootFolder.getFolder(specfile.getParent()
					.getName());
			if (branchFolder != null) {
				ICVSFile cvsSources = branchFolder.getFile(sources.getName());
				if (cvsSources != null) {
					// if 'sources' is not shared with CVS, add it
					Session session = new Session(location, branchFolder, true);
					session.open(monitor, true);
					if (!cvsSources.isManaged()) {
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						String[] arguments = new String[] { sources.getName() };
						status = Command.ADD.execute(session,
								Command.NO_GLOBAL_OPTIONS,
								Command.NO_LOCAL_OPTIONS, arguments, null,
								monitor);
					}
					if (status.isOK()) {
						// everything has passed so far
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						// perform update on sources and .cvsignore
						String[] arguments = new String[] { sources.getName(),
								cvsignore.getName() };
						status = Command.UPDATE.execute(session,
								Command.NO_GLOBAL_OPTIONS,
								Command.NO_LOCAL_OPTIONS, arguments, null,
								monitor);
					}
				} else {
					status = handleError(org.fedoraproject.eclipse.packager.Messages.getString("UploadHandler.22")); //$NON-NLS-1$
				}
			} else {
				status = handleError(org.fedoraproject.eclipse.packager.Messages.getString("UploadHandler.23")); //$NON-NLS-1$
			}

		} catch (CVSException e) {
			e.printStackTrace();
			status = handleError(e.getMessage());
		}
		return status;
	}

	/**
	 * Actually writes to .cvsignore. ATM this method is never called with
	 * <code>forceOverwrite</code> set to true.
	 * 
	 * @param cvsignore
	 * @param toAdd
	 * @param forceOverwrite
	 * @return Status of the performed operation.
	 */
	private IStatus updateCVSIgnore(File cvsignore, File toAdd,
			boolean forceOverwrite) {
		IStatus status;
		String filename = toAdd.getName();
		ArrayList<String> ignoreFiles = new ArrayList<String>();
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			if (forceOverwrite) {
				pw = new PrintWriter(new FileWriter(cvsignore, false));
				pw.println(filename);
				status = Status.OK_STATUS;
			} else {
				// only append to file if not already present
				br = new BufferedReader(new FileReader(cvsignore));

				String line = br.readLine();
				while (line != null) {
					ignoreFiles.add(line);
					line = br.readLine();
				}

				if (!ignoreFiles.contains(filename)) {
					pw = new PrintWriter(new FileWriter(cvsignore, true));
					pw.println(filename);
				}
				status = Status.OK_STATUS;
			}
		} catch (IOException e) {
			e.printStackTrace();
			status = handleError(e);
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					status = handleError(e);
				}
			}
		}
		return status;
	}
	
	@Override
	protected String getTaskName() {
		return Messages.getString("CVSUploadHandler.taskName"); //$NON-NLS-1$
	}

}

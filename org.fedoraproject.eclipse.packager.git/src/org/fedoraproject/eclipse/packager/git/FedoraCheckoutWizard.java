/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.git;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

/**
 * Wizard to checkout package content from Fedora Git.
 *
 */
public class FedoraCheckoutWizard extends Wizard implements IImportWizard {

	private SelectModulePage page;
	private Git git;

	private IStructuredSelection selection;

	/**
	 * Creates the wizards and sets that it needs progress monitor.
	 */
	public FedoraCheckoutWizard() {
		super();
		// Set title of wizard window
		setWindowTitle(Messages.fedoraCheckoutWizard_wizardTitle);
		// required to show progress info of clone job
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		// get Fedora username from cert
		page = new SelectModulePage();
		addPage(page);
		page.init(selection);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private String getGitURL() {
		String username = FedoraHandlerUtils.getUsernameFromCert();
		String packageName = page.getPackageName();
		if (username.equals("anonymous")) { //$NON-NLS-1$
			return "git://pkgs.fedoraproject.org/" + packageName + ".git"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return "ssh://" + username + "@pkgs.fedoraproject.org/" //$NON-NLS-1$ //$NON-NLS-2$
					+ packageName + ".git"; //$NON-NLS-1$
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	@Override
	public boolean performFinish() {
		try {
			final URIish uri = new URIish(getGitURL());
			final CloneOperation2 clone = new CloneOperation2(uri, true,
					new ArrayList<Ref>(), new File(ResourcesPlugin
							.getWorkspace().getRoot().getLocation().toFile(),
							page.getPackageName()), Constants.R_HEADS + Constants.MASTER,
					"origin", 0); //$NON-NLS-1$

			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			// Bail out if project already exists
			IResource project = wsRoot.findMember(new Path(page.getPackageName()));
			if (project != null && project.exists()) {
				final String errorMessage = NLS.bind(
						Messages.fedoraCheckoutWizard_projectExists, project.getName());
				cloneFailChecked(errorMessage);
				// let's give user a chance to fix this minor problem
				return false;
			}
			// Make sure to be created directory does not exist or is
			// empty
			File newDir = new File(wsRoot.getLocation().toOSString() +
			IPath.SEPARATOR + page.getPackageName() );
			if (newDir.exists() && newDir.isDirectory()) {
				String contents[] = newDir.list();
				if (contents.length != 0) {
					// Refuse to clone, give user a chance to correct
					final String errorMessage = NLS.bind(
							Messages.fedoraCheckoutWizard_filesystemResourceExists, page.getPackageName());
					cloneFailChecked(errorMessage);
					return false;
				}
			}

			// Make sure we report a nice error if repo not found
			try {
				// Perform clone in ModalContext thread with progress
				// reporting on the wizard.
				getContainer().run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {

						clone.run(monitor);
						if (monitor.isCanceled())
							throw new InterruptedException();
					}
				});
			} catch (InvocationTargetException e) {
				//e.printStackTrace();
				// if repo wasn't found make this apparent
				if (e.getCause() instanceof NoRemoteRepositoryException) {
					// Refuse to clone, give user a chance to correct
					final String errorMessage = NLS.bind(
							Messages.fedoraCheckoutWizard_repositoryNotFound, page.getPackageName());
					cloneFailChecked(errorMessage);
					return false; // let user correct
				} else {
					throw e;
				}
			}
			// Add cloned repository to the list of Git repositories so that it
			// shows up in the Git repositories view.
			final RepositoryUtil config = org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
			config.addConfiguredRepository(clone.getGitDir());
			IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(page.getPackageName());
			newProject.create(null);
			newProject.open(null);
			ConnectProviderOperation connect = new ConnectProviderOperation(
					newProject);
			connect.execute(null);

			// Add new project to working sets, if requested
			IWorkingSet[] workingSets = page.getWorkingSets();
			if (workingSets.length > 0) {
				PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(newProject, workingSets);
			}

			// Find repo we've just created and set gitRepo
			RepositoryCache repoCache = org.eclipse.egit.core.Activator
					.getDefault().getRepositoryCache();
			try {
				this.git = new Git(repoCache.lookupRepository(clone.getGitDir()));
			} catch (IOException ex) {
				// Repo lookup failed, no way we can continue.
				cloneFailChecked(ex.getMessage());
				return false;
			}
			
			// Create local branches
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						createLocalBranches(monitor);
					} catch (CoreException e) {
						cloneFailChecked(e.getMessage());
						return;
					}
					if (monitor.isCanceled())
						throw new InterruptedException();
				}
			});
			
			// Finally show the Git Repositories view for convenience
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(
							"org.eclipse.egit.ui.RepositoriesView"); //$NON-NLS-1$
			return true;
		} catch (InterruptedException e) {
			MessageDialog.openInformation(getShell(), Messages.fedoraCheckoutWizard_cloneFail, 
					Messages.fedoraCheckoutWizard_cloneCancel);
			return false;
		} catch (Exception e) {
			org.fedoraproject.eclipse.packager.git.Activator.handleError(
					Messages.fedoraCheckoutWizard_cloneFail, e, true);
			return false;
		}
	}
	
	/**
	 * Create local branches based on existing remotes (uses the JGit API).
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	private void createLocalBranches(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.fedoraCheckoutWizard_createLocalBranchesJob,
				IProgressMonitor.UNKNOWN);

		try {
			// get a list of remote branches
			ListBranchCommand branchList = git.branchList();
			branchList.setListMode(ListMode.REMOTE); // want all remote branches
			List<Ref> remoteRefs = branchList.call();
			for (Ref remoteRef: remoteRefs) {
				String name = remoteRef.getName();
				int index = (Constants.R_REMOTES + "origin/").length(); //$NON-NLS-1$
				// Remove "refs/remotes/origin/" part in branch name
				name = name.substring(index);
				// Use "f14"-like branch naming
				if (name.endsWith("/" + Constants.MASTER)) { //$NON-NLS-1$
					index = name.indexOf("/" + Constants.MASTER); //$NON-NLS-1$
					name = name.substring(0, index);
				}
				// Create all remote branches, except "master"
				if (!name.equals(Constants.MASTER)) {
					CreateBranchCommand branchCreateCmd = git.branchCreate();
					branchCreateCmd.setName(name);
					// Need to set starting point this way in order for tracking
					// to work properly. See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=333899
					branchCreateCmd.setStartPoint(remoteRef.getName());
					// Add remote tracking config in order to not confuse
					// fedpkg
					branchCreateCmd.setUpstreamMode(SetupUpstreamMode.TRACK);
					branchCreateCmd.call();
				}
			}
		} catch (JGitInternalException e) {
			e.printStackTrace();
		} catch (RefAlreadyExistsException e) {
			e.printStackTrace();
		} catch (RefNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidRefNameException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens error dialog with provided reason in error message.
	 * 
	 * @param errorMsg The error message to use.
	 */
	private void cloneFailChecked(String errorMsg) {
		ErrorDialog
		.openError(
				getShell(),
				getWindowTitle() + Messages.fedoraCheckoutWizard_problem,
				Messages.fedoraCheckoutWizard_cloneFail,
				new Status(
						IStatus.ERROR,
						org.fedoraproject.eclipse.packager.git.Activator.PLUGIN_ID,
						0, errorMsg, null));
	}
}

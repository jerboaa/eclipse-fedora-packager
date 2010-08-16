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
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.ssl.Certificates;
import org.apache.commons.ssl.KeyMaterial;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.ui.RepositoryUtil;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class FedoraCheckoutWizard extends Wizard implements IImportWizard {

	private SelectModulePage page;

	public FedoraCheckoutWizard() {
		super();
		// required to show progress info of clone job
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		// get Fedora username from cert
		page = new SelectModulePage();
		addPage(page);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private String getUsername() {
		String file = System.getProperty("user.home") + IPath.SEPARATOR //$NON-NLS-1$
				+ ".fedora.cert"; //$NON-NLS-1$
		File cert = new File(file);
		if (cert.exists()) {
			KeyMaterial kmat;
			try {
				kmat = new KeyMaterial(cert, cert, new char[0]);
				List<?> chains = kmat.getAssociatedCertificateChains();
				Iterator<?> it = chains.iterator();
				ArrayList<String> cns = new ArrayList<String>();
				while (it.hasNext()) {
					X509Certificate[] certs = (X509Certificate[]) it.next();
					if (certs != null) {
						for (int i = 0; i < certs.length; i++) {
							cns.add(Certificates.getCN(certs[i]));
						}
					}
				}
				return cns.get(0);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "anonymous";
	}

	private String getGitURL() {
		String username = getUsername();
		String packageName = page.getPackageName();
		if (username.equals("anonymous")) {
			return "git://pkgs.stg.fedoraproject.org/" + packageName + ".git";
		} else {
			return "ssh://" + username + "@pkgs.stg.fedoraproject.org/"
					+ packageName + ".git";
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// nothing
	}

	@Override
	public boolean performFinish() {
		try {
			final URIish uri = new URIish(getGitURL());
			final CloneOperation clone = new CloneOperation(uri, true,
					new ArrayList<Ref>(), new File(ResourcesPlugin
							.getWorkspace().getRoot().getLocation().toFile(),
							page.getPackageName()), "refs/heads/master", // TODO:
																			// use
																			// constants
					"origin");

			// Bail out if project already exists
			IResource project = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(new Path(page.getPackageName()));
			if (project != null && project.exists()) {
				final String errorMessage = NLS.bind(
						"Project already exists", project.getName());
				ErrorDialog
						.openError(
								getShell(),
								getWindowTitle(),
								"Clone failed!",
								new Status(
										IStatus.ERROR,
										org.fedoraproject.eclipse.packager.git.Activator.PLUGIN_ID,
										0, errorMessage, null));
				// let's give user a chance to fix this minor problem
				return false;
			}

			// Perform clone in ModalContext thread with progress
			// reporting on the wizard.
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					clone.run(monitor);
					if (monitor.isCanceled())
						throw new InterruptedException();
				}
			});
			// Add cloned repository to the list of Git repositories so that it
			// shows up in the Git repositories view.
			final RepositoryUtil config = org.eclipse.egit.ui.Activator.getDefault().getRepositoryUtil();
			config.addConfiguredRepository(clone.getGitDir());
			IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(page.getPackageName());
			newProject.create(null);
			newProject.open(null);
			ConnectProviderOperation connect = new ConnectProviderOperation(
					newProject);
			connect.execute(null);
			// Finally show the Git Repositories view for convenience
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(
							"org.eclipse.egit.ui.RepositoriesView"); //$NON-NLS-1$
			return true;
		} catch (InterruptedException e) {
			MessageDialog.openInformation(getShell(), "Clone Failed", // TODO: externalize
					"Clone cancelled by user");
			return false;
		} catch (Exception e) {
			org.fedoraproject.eclipse.packager.git.Activator.handleError(
					"Clone Failed", e, true);
			return false;
		}
	}
}

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
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.ssl.Certificates;
import org.apache.commons.ssl.KeyMaterial;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class FedoraCheckoutWizard extends Wizard implements IImportWizard {

	private SelectModulePage page;

	public FedoraCheckoutWizard() {
		super();
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
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		try {
			CloneOperation clone = new CloneOperation(new URIish(getGitURL()),
					true, new ArrayList<Ref>(), new File(ResourcesPlugin
							.getWorkspace().getRoot().getLocation().toFile(),
							page.getPackageName()), "refs/heads/master",
					"origin");
			clone.run(new NullProgressMonitor());
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(page.getPackageName());
			project.create(null);
			project.open(null);
			ConnectProviderOperation connect = new ConnectProviderOperation(project);
			connect.execute(null);

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
}

/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.internal.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.fedoraproject.eclipse.packager.LocalProjectType;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.LocalFedoraPackagerProjectCreator;
import org.fedoraproject.eclipse.packager.utils.UiUtils;

/**
 * wizard to ease the process of creating fedora packages
 *
 */
public class LocalFedoraPackagerProjectWizard extends Wizard implements
		INewWizard {

	private static final String PAGE_ONE = "PageOne"; //$NON-NLS-1$
	private static final String PAGE_TWO = "PageTwo"; //$NON-NLS-1$
	private static final String PAGE_THREE = "PageThree"; //$NON-NLS-1$
	private static final String PAGE_FOUR = "PageFour"; //$NON-NLS-1$

	private LocalFedoraPackagerPageOne pageOne;
	private LocalFedoraPackagerPageTwo pageTwo;
	private LocalFedoraPackagerPageThree pageThree;
	private LocalFedoraPackagerPageFour pageFour;

	private IWorkspaceRoot root;
	private IProject project;
	private IProjectDescription description;

	private LocalProjectType projectType;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		pageOne = new LocalFedoraPackagerPageOne(PAGE_ONE);
		addPage(pageOne);
		pageTwo = new LocalFedoraPackagerPageTwo(PAGE_TWO);
		addPage(pageTwo);
		pageThree = new LocalFedoraPackagerPageThree(PAGE_THREE);
		addPage(pageThree);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) {
					try {
						createBaseProject(monitor != null ? monitor
								: new NullProgressMonitor());
						createMainProject(monitor != null ? monitor
								: new NullProgressMonitor());

						// Set persistent property so that we know when to show the context
						// menu item.
						project.setPersistentProperty(PackagerPlugin.PROJECT_LOCAL_PROP, "true" /* unused value */); //$NON-NLS-1$

						ConnectProviderOperation connect = new ConnectProviderOperation(project);
						connect.execute(null);

						// Finally ask if the Fedora Packaging perspective should be opened
						// if not already open.
						UiUtils.openPerspective(getShell());

					} catch (NoHeadException e) {
						e.printStackTrace();
					} catch (NoMessageException e) {
						e.printStackTrace();
					} catch (ConcurrentRefUpdateException e) {
						e.printStackTrace();
					} catch (JGitInternalException e) {
						e.printStackTrace();
					} catch (WrongRepositoryStateException e) {
						e.printStackTrace();
					} catch (NoFilepatternException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			};
			getContainer().run(false, true, op);
		} catch (InvocationTargetException x) {
			return false;
		} catch (InterruptedException x) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		return (getContainer().getCurrentPage() == pageThree && pageThree
				.pageCanFinish())
				|| getContainer().getCurrentPage() == pageFour;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof LocalFedoraPackagerPageThree) {
			// check if user chooses to use the Specfile template
			if (!((LocalFedoraPackagerPageThree) page).pageCanFinish()) {
				pageFour = new LocalFedoraPackagerPageFour(PAGE_FOUR,
						this.pageOne.getProjectName());
				addPage(pageFour);
				return pageFour;
			} else {
				return null;
			}
		}
		return super.getNextPage(page);
	}

	/**
	 * Creates the base of the project.
	 *
	 * @param IProgressMonitor
	 *            Progress monitor to report back status
	 */
	protected void createBaseProject(IProgressMonitor monitor) {
		root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(pageOne.getProjectName());
		description = ResourcesPlugin.getWorkspace().newProjectDescription(
				pageOne.getProjectName());
		if (!Platform.getLocation().equals(pageOne.getLocationPath())) {
			description.setLocation(pageOne.getLocationPath());
		}
		try {
			project.create(description, monitor);
			project.open(monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new instance of the FedoraRPM project.
	 *
	 * @param IProgressMonitor
	 *            Progress monitor to report back status
	 * @throws WrongRepositoryStateException
	 * @throws JGitInternalException
	 * @throws ConcurrentRefUpdateException
	 * @throws NoMessageException
	 * @throws NoHeadException
	 * @throws IOException
	 * @throws NoFilepatternException
	 * @throws CoreException
	 */
	protected void createMainProject(IProgressMonitor monitor)
			throws NoHeadException, NoMessageException,
			ConcurrentRefUpdateException, JGitInternalException,
			WrongRepositoryStateException, NoFilepatternException, IOException,
			CoreException {

		LocalFedoraPackagerProjectCreator fedoraRPMProjectCreator = new LocalFedoraPackagerProjectCreator(
				project, monitor);
		projectType = pageThree.getProjectType();
		switch (projectType) {
		case PLAIN:
			if (pageThree.btnSpecTemplate().getSelection()) {
				fedoraRPMProjectCreator.create(pageFour.getContent());
			} else {
				fedoraRPMProjectCreator.create(pageThree.getExternalFile(),
						projectType);
			}
			break;
		case SRPM:
			fedoraRPMProjectCreator.create(pageThree.getExternalFile(),
					projectType);
			break;
		case STUBBY:
			fedoraRPMProjectCreator.create(pageThree.getInputType(),
					pageThree.getExternalFile());
			break;
		}
	}
}

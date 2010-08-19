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
package org.fedoraproject.eclipse.packager.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

/**
 * Utility class used to determine fedora specific properties from the
 * information in the ExecutionEvent.
 */
public class FedoraHandlerUtils {

	private static final String GIT_REPOSITORY = "org.eclipse.egit.core.GitProvider"; //$NON-NLS-1$
	private static final String CVS_REPOSITORY = "org.eclipse.team.cvs.core.cvsnature"; //$NON-NLS-1$

	/**
	 * Represents the Git, Cvs or unknown project type.
	 * 
	 */
	public static enum ProjectType {
		/** Git project */
		GIT,
		/** Cvs project */
		CVS,
		/** Unknown */
		UNKNOWN
	}

	/**
	 * Returns a FedoraProjectRoot from the ExecutionEvent.
	 * 
	 * @param event
	 *            The execution event.
	 * @return The retrieved FedoraProjectRoot.
	 */
	public static FedoraProjectRoot getValidRoot(ExecutionEvent event) {
		IResource resource = getResource(event);
		return getValidRoot(resource);
	}

	/**
	 * Returns a FedoraProjectRoot from the given resource. It works by finding
	 * the closest IContainer containing sources file.
	 * 
	 * @param resource
	 *            The resource to use for determining the base folder.
	 * @return The calculated FedoraProjectRoot.
	 */
	public static FedoraProjectRoot getValidRoot(IResource resource) {
		if (resource instanceof IFolder || resource instanceof IProject) {
			// TODO check that spec file and sources file are present
			if (validateFedorapackageRoot((IContainer) resource)) {
				return new FedoraProjectRoot((IContainer) resource);
			}
		} else if (resource instanceof IFile) {
			if (validateFedorapackageRoot(resource.getParent())) {
				return new FedoraProjectRoot(resource.getParent());
			}
		}
		return null;
	}

	private static boolean validateFedorapackageRoot(IContainer resource) {
		IFile file = resource.getFile(new Path("sources")); //$NON-NLS-1$
		if (file.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the IResource that was selected when the event was fired.
	 * @param event The fired execution event. 
	 * @return The resource that was selected.
	 */
	public static IResource getResource(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part == null) {
			return null;
		}
		if (part instanceof EditorPart) {
			IEditorInput input = ((EditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput) input).getFile();
			} else {
				return null;
			}
		}
		IWorkbenchSite site = part.getSite();
		if (site == null) {
			return null;
		}
		ISelectionProvider provider = site.getSelectionProvider();
		if (provider == null) {
			return null;
		}
		ISelection selection = provider.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			if (element instanceof IResource) {
				return (IResource) element;
			} else if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				Object adapted = adaptable.getAdapter(IResource.class);
				return (IResource) adapted;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Creates a list of rpm defines to use the given directory as a base directory.
	 * @param dir The base directory.
	 * @return Defines to instruct rpmbuild to use given directory.
	 */
	public static List<String> getRPMDefines(String dir) {
		ArrayList<String> rpmDefines = new ArrayList<String>();
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_sourcedir " + dir); //$NON-NLS-1$
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_builddir " + dir); //$NON-NLS-1$
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_srcrpmdir " + dir); //$NON-NLS-1$
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_rpmdir " + dir); //$NON-NLS-1$

		return rpmDefines;
	}

	/**
	 * Returns the project type determined from the given IResource.
	 * @param resource The base for determining the project type.
	 * @return The project type.
	 */
	public static ProjectType getProjectType(IResource resource) {

		Map<?,?> persistentProperties = null;
		try {
			persistentProperties = resource.getProject()
					.getPersistentProperties();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		QualifiedName name = new QualifiedName("org.eclipse.team.core", //$NON-NLS-1$
				"repository"); //$NON-NLS-1$
		String repository = (String) persistentProperties.get(name);
		if (GIT_REPOSITORY.equals(repository)) {
			return ProjectType.GIT;
		} else if (CVS_REPOSITORY.equals(repository)) {
			return ProjectType.CVS;
		}
		return ProjectType.UNKNOWN;
	}

	/**
	 * Returns the IFpProjectBits used to abstract vcs specific things.
	 * 
	 * @param project The project for which to get the VCS specific parts.
	 * @return The needed IFpProjectBits.
	 */
	public static IFpProjectBits getVcsHandler(IResource project) {
		ProjectType type = FedoraHandlerUtils.getProjectType(project);
		IExtensionPoint vcsExtensions = Platform.getExtensionRegistry()
				.getExtensionPoint(PackagerPlugin.PLUGIN_ID, "vcsContribution"); //$NON-NLS-1$
		if (vcsExtensions != null) {
			IConfigurationElement[] elements = vcsExtensions
					.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("vcs") //$NON-NLS-1$
						&& (elements[i].getAttribute("type") //$NON-NLS-1$
								.equals(type.name()))) {
					try {
						IConfigurationElement bob = elements[i];
						IFpProjectBits vcsContributor = (IFpProjectBits) bob
								.createExecutableExtension("class");  //$NON-NLS-1$
						// Do initialization
						if (vcsContributor != null) {
							vcsContributor.initialize(project);
						}
						return vcsContributor;
					} catch (CoreException e) {
						e.printStackTrace();
					}

				}
			}
		}
		return null;
	}
}

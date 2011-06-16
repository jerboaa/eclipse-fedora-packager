/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.ILookasideCache.CacheType;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerExtensionPointException;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType;

/**
 * This class is representing a root directory for a Fedora package in a given
 * branch. It can be a folder in the cvs case or a project in the git case.
 * 
 */
public class FedoraProjectRoot implements IProjectRoot {
	
	private static final String LOOKASIDE_CACHE_EXTENSIONPOINT_NAME =
		"lookasideCacheProvider"; //$NON-NLS-1$
	private static final String LOOKASIDE_CACHE_ELEMENT_NAME = "cache"; //$NON-NLS-1$
	private static final String LOOKASIDE_CACHE_CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
	private static final String PR_STRING_EXTENSIONPOINT_NAME =
		"productNamesProvider"; //$NON-NLS-1$
	private static final String PR_STRING_ELEMENT_NAME = "provider"; //$NON-NLS-1$
	private static final String PR_STRING_CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
	
	private IContainer rootContainer;
	private SourcesFile sourcesFile;
	private ProjectType type;
	private ILookasideCache lookAsideCache; // The lookaside cache abstraction
	private IProductStrings productStrings;

	/**
	 * Default no-arg constructor. Required for instance creation via
	 * reflections.
	 */
	public FedoraProjectRoot() {
		// nothing
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#initialize(org.eclipse.core.resources.IContainer, org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType)
	 */
	@Override
	public void initialize(IContainer container, ProjectType type) throws FedoraPackagerExtensionPointException {
		this.rootContainer = container;
		this.sourcesFile = new SourcesFile(rootContainer.getFile(new Path(
				SourcesFile.SOURCES_FILENAME)));
		assert type != null;
		this.type = type;
		// statically pass Fedora type
		this.lookAsideCache = createNewLookasideCacheObject(CacheType.FEDORA);
		this.productStrings = createNewNonTranslatableStringsObject(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getContainer()
	 */
	@Override
	public IContainer getContainer() {
		return rootContainer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getProject()
	 */
	@Override
	public IProject getProject() {
		return this.rootContainer.getProject();
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getSourcesFile()
	 */
	@Override
	public SourcesFile getSourcesFile() {
		return sourcesFile;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getPackageName()
	 */
	@Override
	public String getPackageName() {
		return this.getSpecfileModel().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getSpecFile()
	 */
	@Override
	public IFile getSpecFile() {
		try {
			for (IResource resource : rootContainer.members()) {
				if (resource instanceof IFile) {
					String ext = ((IFile) resource).getFileExtension();
					if (ext != null && ext.equals("spec")) //$NON-NLS-1$
						return (IFile) resource;
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getSpecfileModel()
	 */
	@Override
	public Specfile getSpecfileModel() {
		SpecfileParser parser = new SpecfileParser();
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getSpecFile().getContents()));
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Specfile specfile = parser.parse(sb.toString());
		return specfile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getProjectType()
	 */
	@Override
	public ProjectType getProjectType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getIgnoreFile()
	 */
	@Override
	public IFile getIgnoreFile() {
		String ignoreFileName = null;
		switch (type) {
		case GIT:
			ignoreFileName = ".gitignore"; //$NON-NLS-1$
			break;
		case CVS:
			ignoreFileName = ".cvsignore"; //$NON-NLS-1$
			break;
		default:
			break;
		}
		assert ignoreFileName != null;
		IFile ignoreFile = getFileMember(ignoreFileName);
		// If not existent, return a IFile handle from the container
		if (ignoreFile == null) {
			return this.rootContainer.getFile(new Path(ignoreFileName));
		}
		assert ignoreFile != null;
		return ignoreFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getLookAsideCache()
	 */
	@Override
	public ILookasideCache getLookAsideCache() {
		return lookAsideCache;
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getProductStrings()
	 */
	@Override
	public IProductStrings getProductStrings() {
		return this.productStrings;
	}

	/**
	 * Instantiate a new lookaside cache object using the lookasideCacheProvider
	 * extension point.
	 * @param cacheType 
	 * 
	 * @return the newly created and initialized instance.
	 * @throws FedoraPackagerExtensionPointException 
	 */
	private ILookasideCache createNewLookasideCacheObject(CacheType cacheType)
			throws FedoraPackagerExtensionPointException {
		IExtensionPoint lookasideCacheExtension = Platform
				.getExtensionRegistry().getExtensionPoint(
						PackagerPlugin.PLUGIN_ID,
						LOOKASIDE_CACHE_EXTENSIONPOINT_NAME);
		if (lookasideCacheExtension != null) {
			for (IConfigurationElement lookasideCacheElement : lookasideCacheExtension
					.getConfigurationElements()) {
				if (lookasideCacheElement.getName().equals(
						LOOKASIDE_CACHE_ELEMENT_NAME)) {
					// found extension point element
					try {
						ILookasideCache cache = (ILookasideCache) lookasideCacheElement
								.createExecutableExtension(LOOKASIDE_CACHE_CLASS_ATTRIBUTE_NAME);
						assert cache != null;
						// Do initialization
						cache.initialize(cacheType);
						return cache;
					} catch (IllegalStateException e) {
						throw new FedoraPackagerExtensionPointException(
								e.getMessage(), e);
					} catch (CoreException e) {
						throw new FedoraPackagerExtensionPointException(
								e.getMessage(), e);
					}
				}
			}
		}
		throw new FedoraPackagerExtensionPointException(NLS.bind(
				FedoraPackagerText.extensionNotFoundError,
				LOOKASIDE_CACHE_EXTENSIONPOINT_NAME));
	}

	/**
	 * Instantiate a new non translatable strings object using the nonTranslatableStringsProvider
	 * extension point.
	 * @param fedoraProjectRoot 
	 * 
	 * @return the newly created and initialized instance.
	 * @throws FedoraPackagerExtensionPointException 
	 */
	private IProductStrings createNewNonTranslatableStringsObject(
			IProjectRoot fedoraProjectRoot)
			throws FedoraPackagerExtensionPointException {
		IExtensionPoint productStringsExtension = Platform
				.getExtensionRegistry()
				.getExtensionPoint(PackagerPlugin.PLUGIN_ID,
						PR_STRING_EXTENSIONPOINT_NAME);
		if (productStringsExtension != null) {
			for (IConfigurationElement providerElement : productStringsExtension
					.getConfigurationElements()) {
				if (providerElement.getName().equals(
						PR_STRING_ELEMENT_NAME)) {
					// found extension point element
					try {
						IProductStrings productStrings = (IProductStrings) providerElement
								.createExecutableExtension(PR_STRING_CLASS_ATTRIBUTE_NAME);
						assert productStrings != null;
						// Do initialization
						productStrings.initialize(fedoraProjectRoot);
						return productStrings;
					} catch (IllegalStateException e) {
						throw new FedoraPackagerExtensionPointException(
								e.getMessage(), e);
					} catch (CoreException e) {
						throw new FedoraPackagerExtensionPointException(
								e.getMessage(), e);
					}
				}
			}
		}
		throw new FedoraPackagerExtensionPointException(NLS.bind(
				FedoraPackagerText.extensionNotFoundError,
				PR_STRING_EXTENSIONPOINT_NAME));
	}

	/**
	 * Find the ignore file in the root container if any.
	 * 
	 * @param ignoreFileName The name of the VCS ignore file.
	 * 
	 * @return The file handle if found. {@code null} otherwise.
	 */
	private IFile getFileMember(String ignoreFileName) {
		IResource resource = rootContainer.findMember(ignoreFileName);
		if (resource instanceof IFile) {
			return ((IFile) resource);
		}
		return null;
	}
}

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
package org.fedoraproject.eclipse.packager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

public class FedoraProjectRoot {

	private IContainer rootContainer;
	private SourcesFile sourcesFile;

	public FedoraProjectRoot(IContainer container) {
		this.rootContainer = container;
		this.sourcesFile = new SourcesFile(rootContainer.getFile(new Path(
				"sources"))); //$NON-NLS-1$
	}

	public IContainer getContainer() {
		return rootContainer;
	}

	public SourcesFile getSourcesFile() {
		return sourcesFile;
	}

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
	
	public String makeTagName() {
		Specfile specfile = getSpecfileModel();
		String name = specfile.getName().replaceAll("^[0-9]+", "");
		String version = specfile.getVersion();
		String release = specfile.getRelease();
		return (name + "-" + version + "-" + release).replaceAll("\\.", "_");
	}
}

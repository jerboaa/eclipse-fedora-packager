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
package org.fedoraproject.eclipse.packager.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.linuxtools.rpmstubby.Generator;
import org.eclipse.linuxtools.rpmstubby.InputType;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.LocalProjectType;

/**
 * Utility class to create to enable existing and
 *   new maintainers work with fedora packages locally
 *
 */
public class LocalFedoraPackagerProjectCreator {

	private IProject project;
	private IProgressMonitor monitor;
	private Repository repository;
	private Git git;

	/**
	 * Construct the local fedora packager project
	 *   based on the created project in main wizard
	 * @param project
	 *            the base of the project
	 * @param monitor
	 *            Progress monitor to report back status
	 *
	 */
	public LocalFedoraPackagerProjectCreator(IProject project, IProgressMonitor monitor) {
		this.project = project;
		this.monitor = monitor;
	}

	/**
	 * Starts a plain project using the specfile template
	 *
	 * @param content
	 * 		contents of the spec template
	 * @throws CoreException
	 * @throws IOException
	 * @throws WrongRepositoryStateException
	 * @throws JGitInternalException
	 * @throws ConcurrentRefUpdateException
	 * @throws NoMessageException
	 * @throws NoHeadException
	 * @throws NoFilepatternException
	 *
	 */
	public void create(String content) throws CoreException,
			NoFilepatternException, NoHeadException, NoMessageException,
			ConcurrentRefUpdateException, JGitInternalException,
			WrongRepositoryStateException, IOException {
		final String projectName = project.getName();
		final String fileName = projectName + ".spec"; //$NON-NLS-1$

		final InputStream contentInputStream =
				new ByteArrayInputStream(content.getBytes());
		final IFile specfile = project.getFile(new Path(fileName));
		try {
			InputStream stream = contentInputStream;
			if (specfile.exists()) {
				specfile.setContents(stream, true, true, monitor);
			} else {
				specfile.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		createProjectStructure();
	}

	/**
	 * Populate the project based on the imported SRPM or .spec file
	 *
	 * @param externalFile
	 *            the xml file uploaded from file system
	 * @param projectType
	 * @throws CoreException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws WrongRepositoryStateException
	 * @throws JGitInternalException
	 * @throws ConcurrentRefUpdateException
	 * @throws NoMessageException
	 * @throws NoHeadException
	 * @throws NoFilepatternException
	 */
	public void create(File externalFile, LocalProjectType projectType)
			throws CoreException, NoFilepatternException, NoHeadException,
			NoMessageException, ConcurrentRefUpdateException,
			JGitInternalException, WrongRepositoryStateException, IOException {
		switch(projectType) {
		case PLAIN:
			IFile specFile = project.getFile(externalFile.getName());
			specFile.create(new FileInputStream(externalFile), false, monitor);
			break;
		case SRPM:
			RPMProject rpmProject = new RPMProject(project, RPMProjectLayout.FLAT);
			rpmProject.importSourceRPM(externalFile);
			break;
		}

		createProjectStructure();
	}

	/**
	 * Populate the project using rpmstubby based on the
	 *  eclipse-feature or maven-pom choice of user
	 * @param inputType
	 *		type of the stubby project
	 * @param stubby
	 * 		the external xml file uploaded from file system
	 * @throws CoreException
	 * @throws FileNotFoundException
	 * @throws WrongRepositoryStateException
	 * @throws JGitInternalException
	 * @throws ConcurrentRefUpdateException
	 * @throws NoMessageException
	 * @throws NoHeadException
	 * @throws NoFilepatternException
	 * @throws IOException
	 *
	 */
	public void create(InputType inputType, File stubby) throws CoreException,
			NoFilepatternException, NoHeadException, NoMessageException,
			ConcurrentRefUpdateException, JGitInternalException,
			WrongRepositoryStateException, IOException {
			IFile stubbyFile = project.getFile(stubby.getName());
			stubbyFile.create(new FileInputStream(stubby), false, monitor);

			Generator specfilegGenerator = new Generator(inputType);
			specfilegGenerator.generate(stubbyFile);

			createProjectStructure();
	}

	/**
	 * Creates project structure inside the base project
	 *
	 * @throws CoreException
	 * @throws IOException
	 * @throws WrongRepositoryStateException
	 * @throws JGitInternalException
	 * @throws ConcurrentRefUpdateException
	 * @throws NoMessageException
	 * @throws NoHeadException
	 * @throws NoFilepatternException
	 *
	 */
	public void createProjectStructure() throws NoFilepatternException,
			NoHeadException, NoMessageException, ConcurrentRefUpdateException,
			JGitInternalException, WrongRepositoryStateException, IOException,
			CoreException {

		File directory = new File(project.getLocation().toString());
		FileUtils.mkdirs(directory, true);
		directory.getCanonicalFile();

		InitCommand command = new InitCommand();
		command.setDirectory(directory);
		command.setBare(false);
		repository = command.call().getRepository();

		git = new Git(repository);

		for (File file : directory.listFiles()) {
			String name = file.getName();

			if (name.contains(".spec")) { //$NON-NLS-1$
				git.add().addFilepattern(name).call();
			}

			if (name.equals(".gitignore")) { //$NON-NLS-1$
				git.add().addFilepattern(name).call();
			}
		}

		// do the first commit
		git.commit().setMessage(FedoraPackagerText.LocalFedoraPackagerProjectCreator_FirstCommit)
				.call();

		// Add created repository to the list of Git repositories so that it
		// shows up in the Git repositories view.
		final RepositoryUtil config = org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
		config.addConfiguredRepository(repository.getDirectory());
	}
}
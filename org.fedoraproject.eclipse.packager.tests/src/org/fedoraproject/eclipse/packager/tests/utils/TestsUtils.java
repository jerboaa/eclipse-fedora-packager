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
package org.fedoraproject.eclipse.packager.tests.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility class for Fedora Packager tests.
 *
 */
public class TestsUtils {

	/**
	 * Prefix for temporary directories created by EFP tests.
	 */
	private static final String TMP_DIRECTORY_PREFIX =
										"eclipse-fedorapackager-tests-temp";
	private static final String TMP_PROJECT_PREFIX =
		"eclipse-fedorapackager-tests-";
	
	/**
	 * @return A unique name.
	 */
	public static String getRandomUniqueName() {
		return TMP_PROJECT_PREFIX + Long.toString(System.nanoTime());
	}
	
	/**
	 * Create a temporary directory (attempts to delete existing directories
	 * with the same name).
	 * 
	 * @return A file handle to the temporary directory.
	 * 
	 * @throws IOException If a problem occurred.
	 */
	public static File createTempDirectory() throws IOException {
		final File tempDir;

		tempDir = File.createTempFile(TMP_DIRECTORY_PREFIX,
				Long.toString(System.nanoTime()));
		if (!(tempDir.delete())) {
			throw new IOException("Could not delete temp file: "
					+ tempDir.getAbsolutePath());
		}
		if (!(tempDir.mkdir())) {
			throw new IOException("Could not create temp directory: "
					+ tempDir.getAbsolutePath());
		}
		return tempDir;
	}
	
	/**
	 * Copy a file into temporary storage.
	 * 
	 * @param fromFile The source file.
	 * @return A temporary file with contents as in {@code fromFile}.
	 * @throws IOException
	 */
	public static File copyContentsToTempFile(File fromFile) throws IOException {
		File destination = createTempDirectory();
		return copyFileContents(fromFile, destination, true);
	}

	/**
	 * Copy contents of folder {@code from} into a temporary directory. This
	 * function works recursively (1 level depth).
	 * 
	 * @param fromDir
	 *            The directory which should be mirrored into a directory in a
	 *            temp location.
	 * @param fileFilter
	 *            An optional file filter to filter files in {@code fromDir}.
	 * @return A file handle to the directory in temporary storage.
	 * 
	 */
	public static File copyFolderContentsToTemp(File fromDir,
			FileFilter fileFilter) throws IOException {
		File destination = createTempDirectory();
		File[] files = null;
		if (fileFilter == null) {
			files = fromDir.listFiles();
		} else {
			files = fromDir.listFiles(fileFilter);
		}
		for (File file: files) {
			copyFileContents(file, destination, false);
		}
		return destination;
	}

	/**
	 * Create a project in the current workspace with contents (i.e. files) the
	 * same as in the template folder.
	 * 
	 * @param folder
	 *            The directory to convert.
	 * @return A handle to the external project.
	 * @throws CoreException
	 */
	public static IProject createProjectFromTemplate(File folder) throws CoreException, IOException {
		// Create external project
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject newProject = root.getProject(getRandomUniqueName());
		newProject.create(null);
		newProject.open(null);
		
		// Add content, handle spec file appropriately
		for (File file: folder.listFiles()) {
			IFile newFile;
			if (file.getName().endsWith(".spec")) {
				newFile = newProject.getFile(newProject.getName() + ".spec");
			} else {
				newFile = newProject.getFile(file.getName());
			}
			if (!newFile.exists()) {
				FileInputStream in = new FileInputStream(file);
				newFile.create(in, false, null);
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		newProject.refreshLocal(IResource.DEPTH_ONE, null);
		
		return newProject;
	}
	
	/**
	 * Read entire content of a file into a string, stripping off any leading or
	 * trailing whitespace.
	 * 
	 * @param source The file to read from.
	 * @return The entire file content.
	 * @throws IOException
	 */
	public static String readContents(File source) throws IOException {
		StringBuffer result = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(source));
			String line;
			do {
				line = br.readLine();
				if (line != null) {
					result.append(line + "\n");
				}
			} while (line != null);
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return result.toString().trim();
	}

	/**
	 * Copy a file into destination {@code destination}. If {@code useTempFile}
	 * is set to true, the a random temporary file in destination will get
	 * created. Otherwise the filename of {@code fromFile} will be used.
	 * 
	 * @param fromFile
	 * @param destination
	 * @param useTempFilenames
	 * @return
	 * @throws IOException
	 */
	private static File copyFileContents(File fromFile, File destination,
			boolean useTempFilenames) throws IOException {
		FileInputStream from = null;
		FileOutputStream to = null;
		File toFile = null;
		if (useTempFilenames) {
			toFile = File.createTempFile(TMP_DIRECTORY_PREFIX, "", destination);
		} else {
			toFile = new File(destination.getAbsolutePath()
					+ File.separatorChar + fromFile.getName());
		}
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = from.read(buffer)) != -1) {
				to.write(buffer, 0, bytesRead); // write
			}
		} finally {
			if (from != null) {
				try {
					from.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (to != null) {
				try {
					to.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return toFile;
	}
}

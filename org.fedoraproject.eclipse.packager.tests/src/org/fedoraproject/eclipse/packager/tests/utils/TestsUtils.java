package org.fedoraproject.eclipse.packager.tests.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
	public static final String TMP_DIRECTORY_PREFIX =
										"eclipse-fedorapackager-tests-temp";
	
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
		FileInputStream from = null;
		FileOutputStream to = null;
		File[] files = null;
		if (fileFilter == null) {
			files = fromDir.listFiles();
		} else {
			files = fromDir.listFiles(fileFilter);
		}
		for (File file: files) {
			try {
				from = new FileInputStream(file);
				File toFile = new File(destination.getAbsolutePath()
						+ File.separatorChar + file.getName());
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
		}
		return destination;
	}

	/**
	 * Convert a directory represented as an abstract {@link java.io.File} to an
	 * Eclipse external project. Accounts for 1 level of files only.
	 * 
	 * @param folder The directory to convert.
	 * @return A handle to the external project.
	 * @throws CoreException 
	 */
	public static IProject adaptFolderToProject(File folder) throws CoreException {
		// Create external project
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject externalProject = root.getProject(folder.getName());
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.newProjectDescription(folder.getName());
		URI fileProjectURL = null;
		try {
			fileProjectURL = new URI("file://" + folder.getAbsolutePath());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		description.setLocationURI(fileProjectURL);
		externalProject.create(null);
		externalProject.open(null);
		
		// Add content
		for (File file: folder.listFiles()) {
			IFile newFile = externalProject.getFile(file.getName());
			if (!newFile.exists()) {
				try {
					newFile.create(new FileInputStream(file), true, null);
				} catch (FileNotFoundException e) {
					// ignore
				}
			}
		}
		
		externalProject.refreshLocal(IResource.DEPTH_ONE, null);
		
		return externalProject;
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
		File file = source;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
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
}

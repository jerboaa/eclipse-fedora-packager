package org.fedoraproject.eclipse.packager.tests.utils;

import java.io.File;
import java.io.IOException;

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

}

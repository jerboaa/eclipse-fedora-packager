package org.fedoraproject.eclipse.packager.tests.utils;

import java.io.InputStream;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class LocalSearchString {

	/**
	 * Look through the file for the specified string
	 * 
	 * @param String
	 * @param IFile
	 * @return boolean
	 */
	public boolean searchString(String match, IFile file) throws CoreException {
		boolean foundMatch = false;
		if (file.exists()) {
			InputStream is = file.getContents();
			String line = null;
			Scanner scan = new Scanner(is);
			while(scan.hasNext() && !foundMatch) {
				line = scan.nextLine();
				if (line.contains(match)) {
					foundMatch = true;
				}
			}
			scan.close();
		}
		return foundMatch;
	}
}

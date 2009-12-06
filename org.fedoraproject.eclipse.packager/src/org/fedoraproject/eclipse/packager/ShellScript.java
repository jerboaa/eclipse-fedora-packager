/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.fedoraproject.eclipse.packager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A utility class for constructing and executing shell scripts on the system.
 *
 */
public class ShellScript {
	
	private File script;
	private String scriptContents;
	private int successCode;
	private File shellScriptFile;
	
	/**
	 * Constructs a new shell script object.
	 * @param command the command to execute
	 * @param successCode the return code that indicated command execution was successful
	 */
	public ShellScript(String command, int successCode)  {
		scriptContents = "#!/bin/sh" + "\n" + command; //$NON-NLS-1$
		this.successCode = successCode;
	}

	/**
	 * Executes the shell script without logging standard output.
	 * @return standard output from execution
	 * @throws CoreException if the operation fails
	 */
	public String execNoLog() throws CoreException {
		byte[] buf = scriptContents.getBytes();
		File file = null;
		try {
			file = getShellScriptFile();
			BufferedOutputStream os = 
				new BufferedOutputStream(new FileOutputStream(file));
			for(int i = 0; i < buf.length; i++) {
				os.write(buf[i]);
		}
		os.close();
		} catch (IOException e) {
			String throw_message = "Error trying to write to " +
			  file.getAbsolutePath();
			IStatus error = new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID, 1,
					throw_message, null);
			throw new CoreException(error);
		}
        	script = file;
		Command.exec("chmod +x " + script.getAbsolutePath(), 0); //$NON-NLS-1$
        return Command.exec("sh " + script.getAbsolutePath(), successCode); //$NON-NLS-1$
	}
	
	public File getShellScriptFile() throws CoreException {
		if (shellScriptFile == null) {
			try {
				shellScriptFile = File.createTempFile(PackagerPlugin.PLUGIN_ID, ".sh"); //$NON-NLS-1$
			} catch(IOException e) {
				String throw_message = "Error creating " +
				  shellScriptFile.getAbsolutePath();
				IStatus error = new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		}
		return shellScriptFile;
	}
	
}

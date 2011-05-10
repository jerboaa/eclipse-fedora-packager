package org.fedoraproject.eclipse.packager.rpm.api;

import org.fedoraproject.eclipse.packager.api.ICommandResult;

/**
 * Super class for results of this plug-in.
 *
 */
public abstract class Result implements ICommandResult {

	private String[] cmdList;
	
	/**
	 * 
	 * @param cmdList
	 */
	public Result(String[] cmdList) {
		this.cmdList = cmdList;
	}
	
	/**
	 * Get the build command for which this is the result
	 * of.
	 * @return The command.
	 */
	public String getBuildCommand() {
		String cmd = new String();
		for (String token: cmdList) {
			cmd += token + " "; //$NON-NLS-1$
		}
		return cmd;
	}
	
	@Override
	abstract public boolean wasSuccessful();

}

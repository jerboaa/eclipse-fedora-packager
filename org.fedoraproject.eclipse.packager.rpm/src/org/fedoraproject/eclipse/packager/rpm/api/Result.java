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

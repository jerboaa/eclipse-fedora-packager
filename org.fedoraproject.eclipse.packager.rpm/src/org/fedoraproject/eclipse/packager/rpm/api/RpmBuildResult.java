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

import java.util.HashSet;
import java.util.Set;

import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand.BuildType;

/**
 * Result of a call to {@link RpmBuildCommand}.
 */
public class RpmBuildResult extends Result {

	private boolean success;
	private BuildType buildType;
	private Set<String> srpms;
	private Set<String> rpms;
	
	/**
	 * 
	 * @param cmdList
	 * @param type 
	 */
	public RpmBuildResult(String[] cmdList, BuildType type) {
		super(cmdList);
		this.buildType = type;
		this.srpms = new HashSet<String>();
		this.rpms = new HashSet<String>();
	}
	
	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	/**
	 * @return The build type of the build, which has this as a result.
	 */
	public BuildType getBuildType() {
		return this.buildType;
	}
	
	/**
	 * Collect SRPM related output in this result
	 * 
	 * @param line
	 */
	public void addSrpm(String line) {
		// of the form "Wrote: path/to/src.rpm
		this.srpms.add(line.split("\\s+")[1]); //$NON-NLS-1$
	}
	
	/**
	 * Collect RPM related output in this result
	 * 
	 * @param line
	 */
	public void addRpm(String line) {
		// of the form "Wrote: path/to/rpm
		this.rpms.add(line.split("\\s+")[1]); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.ICommandResult#wasSuccessful()
	 */
	@Override
	public boolean wasSuccessful() {
		return this.success;
	}

	/**
	 * Returns the path to the written SRPM if applicable.
	 * 
	 * @return The absolute path to the source RPM or {@code null} if there are
	 *         none.
	 */
	public String getAbsoluteSRPMFilePath() {
		for (String srpm: srpms) {
			return srpm; // there really should only be one
		}
		return null;
	}

	/**
	 * @return a list of absolute paths to RPMs produced by the build or
	 *         {@code null} if there are none.
	 */
	public Set<String> getAbsoluteRpmFilePaths() {
		return this.rpms;
	}
}

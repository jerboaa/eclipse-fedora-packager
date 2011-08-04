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
package org.fedoraproject.eclipse.packager.koji.api;

import java.util.Map;

/**
 * Class representing build info as returned by
 * getBuild XMLRPC call.
 *
 */


/*
 * getBuild(buildInfo, strict=False)
 *  description: Return information about a build.  buildID may be either
    a int ID, a string NVR, or a map containing 'name', 'version'
    and 'release.  A map will be returned containing the following
    keys:
      id: build ID
      package_id: ID of the package built
      package_name: name of the package built
      version
      release
      epoch
      nvr
      state
      task_id: ID of the task that kicked off the build
      owner_id: ID of the user who kicked off the build
      owner_name: name of the user who kicked off the build
      creation_event_id: id of the create_event
      creation_time: time the build was created (text)
      creation_ts: time the build was created (epoch)
      completion_time: time the build was completed (may be null)
      completion_ts: time the build was completed (epoch, may be null)

    If there is no build matching the buildInfo given, and strict is specified,
    raise an error.  Otherwise return None.
 */
@SuppressWarnings("unused")
public class KojiBuildInfo {

	/* relevant keys of the returned map */ 
	private static final String KEY_ID = "id"; //$NON-NLS-1$
	private static final String KEY_PACKAGE_ID = "package_id"; //$NON-NLS-1$
	private static final String KEY_PACKAGE_NAME = "package_name"; //$NON-NLS-1$
	private static final String KEY_VERSION = "version"; //$NON-NLS-1$
	private static final String KEY_RELEASE = "release"; //$NON-NLS-1$
	private static final String KEY_EPOCH = "epoch"; //$NON-NLS-1$
	private static final String KEY_NVR = "nvr"; //$NON-NLS-1$
	private static final String KEY_STATE = "state"; //$NON-NLS-1$
	private static final String KEY_TASK_ID = "task_id"; //$NON-NLS-1$
	
	private int state;
	private int taskId;
	private String release;
	private int epoch;
	private String nvr;
	private String version;
	private String packageName;
	private int packageId;
	
	/**
	 * Construct the build info from the map
	 * returned by the API call.
	 * 
	 * @param buildInfo
	 */
	public KojiBuildInfo(Map<String, Object> buildInfo) {
		parseBuildInfo(buildInfo);
	}

	/**
	 * @return {@code true} if the associated build of this build info is
	 *         complete.
	 */
	public boolean isComplete() {
		return state == 1;
	}
	
	/**
	 * @return The task id this build.
	 */
	public int getTaskId() {
		return this.taskId;
	}
	
	/**
	 * @return The release associated with this build info.
	 */
	public String getRelease() {
		return release;
	}
	
	/**
	 * @return The version associated with this build info.
	 */
	public String getVersion() {
		return this.version;
	}
	
	/**
	 * @return The name of the package associated with this build info.
	 */
	public String getPackageName() {
		return this.packageName;
	}
	
	/**
	 * @return the state of the associated build.
	 */
	public int getState() {
		return state;
	}

	/**
	 * @return the epoch of the associated build.
	 */
	public int getEpoch() {
		return epoch;
	}

	/**
	 * @return the name-version-release of the associated build
	 */
	public String getNvr() {
		return nvr;
	}

	/**
	 * @return the packageId
	 */
	public int getPackageId() {
		return packageId;
	}

	/* does the heavy lifting :) */
	private void parseBuildInfo(Map<String, Object> buildInfo) {
		this.state = (Integer)buildInfo.get(KEY_STATE); // auto-un-boxed
		this.taskId = (Integer)buildInfo.get(KEY_TASK_ID);
		this.release = (String)buildInfo.get(KEY_RELEASE);
		this.version = (String)buildInfo.get(KEY_VERSION);
		this.packageName = (String)buildInfo.get(KEY_PACKAGE_NAME);
		this.nvr = (String)buildInfo.get(KEY_NVR);
		this.packageId = (Integer)buildInfo.get(KEY_PACKAGE_ID);
		Object epoch = buildInfo.get(KEY_EPOCH);
		if (epoch != null && epoch instanceof Integer) {
			this.epoch = (Integer)buildInfo.get(KEY_EPOCH);
		}
	}
}

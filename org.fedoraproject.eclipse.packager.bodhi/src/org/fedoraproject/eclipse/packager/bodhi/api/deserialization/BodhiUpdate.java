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
package org.fedoraproject.eclipse.packager.bodhi.api.deserialization;

import com.google.gson.JsonObject;

/**
 * Class which represents a pushed Bodhi update. Deserialized from
 * a JSON String by google.gson.
 *
 */
public class BodhiUpdate {

	private String status;
	private boolean close_bugs;
	private String request;
	private String date_submitted;
	private int unstable_karma;
	private String submitter;
	private boolean critpath;
	private JsonObject approved;
	private int stable_karma;
	private String date_pushed;
	private Build[] builds;
	// nvr of the update
	private String title;
	// update comment
	private String notes;
	private String date_modified;
	private JsonObject nagged;
	private int[] bugs;
	private JsonObject[] comments;
	private boolean critpath_approved;
	private int updateid;
	private int karma;
	// TODO: Model release appropriately
	private JsonObject release;
	private String type;
	
	/**
	 * Google GSON wants this
	 */
	public BodhiUpdate() {
		// nothing
	}
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @return the close_bugs
	 */
	public boolean isCloseBugs() {
		return close_bugs;
	}
	/**
	 * @return the request
	 */
	public String getRequest() {
		return request;
	}
	/**
	 * @return the date_submitted
	 */
	public String getDateSubmitted() {
		return date_submitted;
	}
	/**
	 * @return the unstable_karma
	 */
	public int getUnstableKarma() {
		return unstable_karma;
	}
	/**
	 * @return the submitter
	 */
	public String getSubmitter() {
		return submitter;
	}
	/**
	 * @return the critpath
	 */
	public boolean isCritpath() {
		return critpath;
	}
	/**
	 * @return the approved
	 */
	public JsonObject getApproved() {
		return approved;
	}
	/**
	 * @return the stable_karma
	 */
	public int getStable_karma() {
		return stable_karma;
	}
	/**
	 * @return the date_pushed
	 */
	public String getDatePushed() {
		return date_pushed;
	}
	/**
	 * @return the builds
	 */
	public Build[] getBuilds() {
		return builds;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @return the date_modified
	 */
	public String getDateModified() {
		return date_modified;
	}
	/**
	 * @return the nagged
	 */
	public JsonObject getNagged() {
		return nagged;
	}
	/**
	 * @return the bugs
	 */
	public int[] getBugs() {
		return bugs;
	}
	/**
	 * @return the comments
	 */
	public JsonObject[] getComments() {
		return comments;
	}
	/**
	 * @return the critpath_approved
	 */
	public boolean isCritpathApproved() {
		return critpath_approved;
	}
	/**
	 * @return the updateid
	 */
	public int getUpdateid() {
		return updateid;
	}
	/**
	 * @return the karma
	 */
	public int getKarma() {
		return karma;
	}
	/**
	 * @return the release
	 */
	public JsonObject getRelease() {
		return release;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}

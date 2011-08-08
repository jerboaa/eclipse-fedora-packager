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

/**
 * Implementation of Result for SRPMImport
 *
 */
public class SRPMImportResult extends Result {

	private boolean success;
	private String[] uploaded;
	private String[] addedToGit;
	
	/**
	 * @param cmdList The command called for the file query.
	 */
	public SRPMImportResult(String[] cmdList) {
		super(cmdList);
	}

	@Override
	public boolean wasSuccessful() {
		return success;
	}
	
	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	/**
	 * Set the files successfully uploaded to lookaside cache.
	 * @param files The names of the files uploaded.
	 */
	public void setUploaded(String[] files){
		uploaded = files;
	}
	
	/**
	 * Set the files added to the project's git.
	 * @param files The files uploaded.
	 */
	public void setAddedToGit(String[] files){
		addedToGit = files;
	}
	
	/**
	 * @return The names of the successfully uploaded files.
	 */
	public String[] getUploaded(){
		return uploaded;
	}
	
	/**
	 * @return The names of the files added to the project's git.
	 */
	public String[] getAddedToGit(){
		return addedToGit;
	}

}

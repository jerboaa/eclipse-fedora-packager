package org.fedoraproject.eclipse.packager;

import org.eclipse.core.resources.IResource;

/**
 * Enriched IResource with type (use adapters to get instance)
 * 
 * @author Red Hat Inc.
 *
 */
public class FpProject {
	
	private IResource project;
	private ProjectType myType;
	public static enum ProjectType { GIT, CVS } // type of project
	
	/**
	 * Constructs a FpProject from an IResource
	 * @param res
	 */
	public FpProject(IResource res) {
		this.project = res;
		// TODO: do the intelligent cast
	}
	
	/**
	 * Get project type of this FpProject instance
	 * @param project
	 * @return The type of this instance.
	 */
	public ProjectType getProjectType(IResource project) {
		return this.myType;
	}
}

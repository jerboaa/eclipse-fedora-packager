package org.fedoraproject.eclipse.packager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;

/**
 * Enriched IResource with type. Use adapters to get instance. See
 * IResourceAdapterFactory.
 * 
 * @author Red Hat Inc.
 *
 */
public class FpProject {
	
	private IResource project;
	private ProjectType myType;
	public static enum ProjectType { GIT, CVS, UNKNOWN } // type of project
	
	private static final String GIT_REPOSITORY = "org.eclipse.egit.core.GitProvider"; //$NON-NLS-1$
	private static final String CVS_REPOSITORY = "org.eclipse.team.cvs.core.cvsnature"; //$NON-NLS-1$
	
	/**
	 * Constructs a FpProject
	 * @param res
	 */
	public FpProject(IResource res, ProjectType type) {
		this.project = res;
		this.myType = type;
	}
	
	/**
	 * @return the project
	 */
	public IResource getProject() {
		return project;
	}

	/**
	 * Get project type of this FpProject instance
	 * 
	 * @return The type of this instance.
	 */
	public ProjectType getProjectType() {
		return this.myType;
	}
	
	/**
	 * Try to adapt an IResource to FpProject. Return null if it fails.
	 * 
	 * @param res The resource to adapt
	 * @return The adapted resource or null.
	 */
	public static FpProject doAdapt(IResource res) throws CoreException {
		if (res instanceof IProject) {
			IProject test = (IProject)res;
			// open project if not already open
			if (!test.isOpen()) {
				test.open(null);
			}
			RepositoryProvider provider = RepositoryProvider.getProvider(test);
			if (provider != null) {
				if (provider.getID().equals(GIT_REPOSITORY)) {
					// Git project, adapt to FpProject with type GIT
					return new FpProject(res, ProjectType.GIT);
				}
				if (provider.getID().equals(CVS_REPOSITORY)) {
					// Git project, adapt to FpProject with type GIT
					return new FpProject(res, ProjectType.CVS);
				}
			}
		}
		return null;
	}
}

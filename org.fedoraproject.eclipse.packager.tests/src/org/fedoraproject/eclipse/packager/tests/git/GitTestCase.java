package org.fedoraproject.eclipse.packager.tests.git;

import org.eclipse.core.resources.IProject;
import org.fedoraproject.eclipse.packager.test_utils.GitTestProject;

import junit.framework.TestCase;

public class GitTestCase extends TestCase {
	
	private GitTestProject project;
	private IProject iProject;
	
	@Override
	protected void setUp() throws Exception {
		project = new GitTestProject("eclipse-rpm-editor");
		iProject = project.getProject();
	}

	@Override
	protected void tearDown() throws Exception {
		project.dispose();
	}

	/**
	 * @return the project
	 */
	public GitTestProject getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(GitTestProject project) {
		this.project = project;
	}

	/**
	 * @return the iProject
	 */
	public IProject getiProject() {
		return iProject;
	}

	/**
	 * @param iProject the iProject to set
	 */
	public void setiProject(IProject iProject) {
		this.iProject = iProject;
	}
}

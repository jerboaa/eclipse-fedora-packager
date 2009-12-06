package org.fedoraproject.eclipse.packager.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.fedoraproject.eclipse.packager.CommonHandler;

public abstract class AbstractTest extends TestCase {

	private CVSTestProject project;
	protected CommonHandler handler;
	protected IContainer branch;

	public AbstractTest() {
		super();
	}

	public AbstractTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		project = new CVSTestProject("ed", "ed-0_8-1_fc8");
		
		IProject testProj = project.getProject();
		branch = (IContainer) testProj.findMember("F-8");
	}

	@Override
	protected void tearDown() throws Exception {
		project.dispose();
	}

}
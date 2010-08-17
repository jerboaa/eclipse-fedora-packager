/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.tests.cvs;

import junit.framework.TestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.fedoraproject.eclipse.packager.FpProject;
import org.fedoraproject.eclipse.packager.test_utils.CVSTestProject;

public class CVSProjectAdapterTest extends TestCase {
	private CVSTestProject project;
	private IProject iProject;
	
	@Override
	protected void setUp() throws Exception {
		project = new CVSTestProject("ed", "ed-1_1-1_fc10");
		iProject = project.getProject();
	}

	@Override
	protected void tearDown() throws Exception {
		project.dispose();
	}
	
	// resource set in AbstractTest
	public void testAdaptToFpProject() throws Exception {
		IAdaptable adaptable = this.iProject;
		// Should return FpProject instance with CVS type
		Object adapted = adaptable.getAdapter(FpProject.class);
		assertNotNull(adapted);
		assertTrue(adapted instanceof FpProject);
		FpProject adaptedProject = (FpProject)adapted;
		assertTrue(adaptedProject.getProjectType() == FpProject.ProjectType.CVS);
	}

}

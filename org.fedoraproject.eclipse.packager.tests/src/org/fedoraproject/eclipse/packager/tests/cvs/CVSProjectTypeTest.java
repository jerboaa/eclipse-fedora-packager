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
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.tests.cvs.utils.CVSTestProject;

public class CVSProjectTypeTest extends TestCase {
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
	
	public void testAdaptToFpProject() throws Exception {
		// Should return FpProject instance with CVS type
		assertTrue(FedoraHandlerUtils.getProjectType(this.iProject) == FedoraHandlerUtils.ProjectType.CVS);
	}

}

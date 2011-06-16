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
package org.fedoraproject.eclipse.packager.tests;

import junit.framework.TestCase;
import org.eclipse.core.resources.IProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.tests.utils.cvs.CVSTestProject;

public class CVSProjectTypeTest extends TestCase {
	private CVSTestProject project;
	private IProject iProject;
	
	@Override
	protected void setUp() throws Exception {
		project = new CVSTestProject();
		project.checkoutModule("ed");
		iProject = project.getProject();
	}

	@Override
	protected void tearDown() throws Exception {
		project.dispose();
	}
	
	public void testAdaptToFpProject() throws Exception {
		// Should return FpProject instance with CVS type
		assertTrue(FedoraPackagerUtils.getProjectType(this.iProject) == FedoraPackagerUtils.ProjectType.CVS);
	}

}

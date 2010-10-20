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
package org.fedoraproject.eclipse.packager.tests.git;

import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestCase;

public class GitProjectTypeTest extends GitTestCase {
	
	public void testAdaptToFpProject() throws Exception {
		// Should return FpProject instance with GIT type
		assertTrue(FedoraHandlerUtils.getProjectType(getiProject()) == FedoraHandlerUtils.ProjectType.GIT);
	}

}

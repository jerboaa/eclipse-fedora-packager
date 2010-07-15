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
package org.fedoraproject.eclipse.packager.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.fedoraproject.eclipse.packager.CommonHandler;
import org.fedoraproject.eclipse.packager.test_utils.CVSTestProject;

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
		project = new CVSTestProject("ed", "ed-1_1-1_fc10");
		
		IProject testProj = project.getProject();
		branch = (IContainer) testProj.findMember("F-10");
	}

	@Override
	protected void tearDown() throws Exception {
		project.dispose();
	}

}
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
package org.fedoraproject.eclipse.packager.rpm.internal.core;

import java.util.Observable;
import java.util.Observer;

import org.fedoraproject.eclipse.packager.rpm.api.MockBuildResult;

/**
 * Observer which determines if a mock build succeeded or failed.
 *
 */
public class MockBuildCommandSuccessObserver implements Observer {

	private static final String MOCK_ERROR_OUTPUT_STRING = "error"; //$NON-NLS-1$
	private MockBuildResult mockBuildResult;
	
	/**
	 * 
	 * @param result
	 */
	public MockBuildCommandSuccessObserver(MockBuildResult result) {
		this.mockBuildResult = result;
	}
	
	@Override
	public void update(Observable object, Object arg) {
		if (arg instanceof String) {
			String line = (String) arg; 
			// if Error shows up in the console there was likely an error
			// so set status accordingly.
			if (line.toLowerCase().contains(MOCK_ERROR_OUTPUT_STRING)) {
				mockBuildResult.setFailure();
			}
		}
	}

}

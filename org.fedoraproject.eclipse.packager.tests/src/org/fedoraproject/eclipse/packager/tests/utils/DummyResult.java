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
package org.fedoraproject.eclipse.packager.tests.utils;

import org.fedoraproject.eclipse.packager.api.ICommandResult;

/**
 * Fixture for {@link FedoraPackagerCommandDummyImpl}.
 *
 */
public class DummyResult implements ICommandResult {

	private boolean overallStatus;
	
	public DummyResult(boolean status) {
		this.overallStatus = status;
	}
	
	@Override
	public boolean wasSuccessful() {
		return overallStatus;
	}

}

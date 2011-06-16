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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Observes status prints on the console and updates the progress monitor.
 *
 */
public class MockBuildStatusObserver implements Observer {

	private IProgressMonitor monitor;
	
	/**
	 * @param monitor
	 */
	public MockBuildStatusObserver(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void update(Observable object, Object arg) {
		if (arg instanceof String) {
			// update the subtask
			monitor.subTask((String)arg);
		}
	}

}

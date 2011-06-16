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

import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildResult;

/**
 * Observes, what is being printed and filters lines ending with .src.rpm. At
 * the moment this observer is only registered for SRPM builds. A similar thing
 * could be used for more interesting things, but keep in mind that rpmbuild
 * output may produce A LOT of output. Doing some analysis on every line of
 * output may slow down builds quite a bit.
 * 
 * For SRPM builds there isn't really a lot of output (one line), so it should
 * be OK to use it for the SRPM build case.
 * 
 * @see ConsoleWriter
 * 
 */
public class RpmConsoleFilterObserver implements Observer {
	
	
	/**
	 * The suffix of source RPMs.
	 */
	public static final String SRPM_SUFFIX = ".src.rpm"; //$NON-NLS-1$

	private RpmBuildResult result;
	
	/**
	 * @param result The result to store the console msg into
	 */
	public RpmConsoleFilterObserver(RpmBuildResult result) {
		this.result = result;
	}
	
	/**
	 * Does the filtering of relevant lines.
	 */
	@Override
	public void update(Observable obj, Object arg) {
		if (arg instanceof String) {
			String line = (String) arg;
			if (line.endsWith(SRPM_SUFFIX)) {
				result.addSrpm(line);
			}
        }

	}

}

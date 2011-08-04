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
package org.fedoraproject.eclipse.packager.koji.internal.handlers;

/**
 * Handler to push a scratch build to Koji.
 */
public class KojiScratchBuildHandler extends KojiBuildHandler {

	/**
	 * Since, this is the handler for a scratch build always return true.
	 * 
	 * @return {@code true}
	 */
	@Override
	protected boolean isScratchBuild() {
		return true;
	}
}

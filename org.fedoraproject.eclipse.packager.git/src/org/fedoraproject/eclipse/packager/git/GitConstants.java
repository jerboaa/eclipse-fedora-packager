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
package org.fedoraproject.eclipse.packager.git;

/**
 * Utility class for Git constants.
 *
 */
public final class GitConstants {
	
	/**
	 * Protocol prefix for anonymous Git.
	 */
	public static final String ANONYMOUS_PROTOCOL = "git://"; //$NON-NLS-1$
	/**
	 * Protocol prefix for authenticated Git clone operations.
	 */
	public static final String AUTHENTICATED_PROTOCOL = "ssh://"; //$NON-NLS-1$
	/**
	 * Token which separates username from Git URL
	 */
	public static final String USERNAME_SEPARATOR = "@"; //$NON-NLS-1$
	/**
	 * Commonly used suffix for Git repositories
	 */
	public static final String GIT_REPO_SUFFIX = ".git"; //$NON-NLS-1$
}

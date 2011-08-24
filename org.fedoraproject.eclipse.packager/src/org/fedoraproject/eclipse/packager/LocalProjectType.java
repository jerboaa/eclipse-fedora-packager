/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager;

/**
 * Type of the populated Local Fedora project based on the selected type by
 * user.
 */
public enum LocalProjectType {
	/** Start plain project using specfile template */
	PLAIN,
	/** using Source Rpm */
	SRPM,
	/** using rpmstubby */
	STUBBY
}

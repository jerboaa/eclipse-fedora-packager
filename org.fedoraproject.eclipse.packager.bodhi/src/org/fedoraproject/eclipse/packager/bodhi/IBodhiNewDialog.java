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
package org.fedoraproject.eclipse.packager.bodhi;

public interface IBodhiNewDialog {

	public abstract String getBuildName();

	public abstract String getRelease();

	public abstract String getBugs();

	public abstract String getNotes();

	public abstract String getType();

	public abstract String getRequest();

	public abstract int open();

	public abstract int getReturnCode();

}
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
package org.fedoraproject.eclipse.packager.internal.utils.httpclient;

/**
 * Listener interface for hc-httpclient upload progress reporting. *
 */
public interface IRequestProgressListener {

	/**
	 * Called each time x bytes of the total entity have been
	 * written to the relevant output stream.
	 * 
	 * @param num The number of bytes written so far.
	 */
	void transferred(long num);

}
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
package org.fedoraproject.eclipse.packager.internal.handlers;

/**
 * Class responsible for uploading source files. The only difference
 * between this handler and {@link UploadHandler} is that this handler
 * will replace contents in {@code sources} files.
 * 
 * @see UploadHandler
 */
public class NewSourcesHandler extends UploadHandler {

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.handlers.UploadHandler#shouldReplaceSources()
	 */
	@Override
	protected boolean shouldReplaceSources() {
		return true;
	}
}

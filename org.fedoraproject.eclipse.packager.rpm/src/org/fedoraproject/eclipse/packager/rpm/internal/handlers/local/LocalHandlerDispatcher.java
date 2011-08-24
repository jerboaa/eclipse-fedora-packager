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
package org.fedoraproject.eclipse.packager.rpm.internal.handlers.local;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * 
 * Abstract super class for dispatched keyboard shortcut handlers.
 *
 */
public abstract class LocalHandlerDispatcher extends
		FedoraPackagerAbstractHandler {

	/**
	 * Check if the underlying resource of the event has property
	 * {@link PackagerPlugin#PROJECT_PROP}. If the project has that property,
	 * dispatch to the passed in handler, otherwise do nothing.
	 * 
	 * @param event
	 *            The passed in event to execute();
	 * @param handler
	 *            The handler to potentially dispatch to.
	 * @throws ExecutionException
	 * @return {@code true} if dispatched to passed handler, {@code false} otherwise.
	 */
	protected boolean checkDispatch(ExecutionEvent event, FedoraPackagerAbstractHandler handler) throws ExecutionException {
		IResource eventResource = FedoraHandlerUtils.getResource(event);
		FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		String nonLocalProperty;
		try {
			nonLocalProperty = eventResource.getProject().getPersistentProperty(PackagerPlugin.PROJECT_PROP);
		} catch (CoreException e) {
			logger.logDebug(e.getMessage(), e);
			return false; // can't continue
		}
		if (nonLocalProperty != null) {
			// dispatch to non-local handler
			logger.logDebug(NLS.bind(FedoraPackagerText.LocalHandlerDispatcher_dispatchToHandlerMsg, handler.getClass().getName()));
			// must always return null, so discard return value
			handler.execute(event);
			return true;
		}
		// no-op
		return false;
	}
}

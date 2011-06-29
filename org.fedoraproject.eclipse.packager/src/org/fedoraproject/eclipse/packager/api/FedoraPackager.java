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
package org.fedoraproject.eclipse.packager.api;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;

/**
 * Eclipse Fedora Packager main interface for commands.
 */
public class FedoraPackager {
	
	private static final String CMD_ID_ATTRIBUTE_NAME = "id"; //$NON-NLS-1$
	private static final String CMD_EXTENSIONPOINT_NAME =
		"packagerCommandContribution"; //$NON-NLS-1$
	private static final String CMD_ELEMENT_NAME = "command"; //$NON-NLS-1$
	private static final String CMD_CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
	
	private final IProjectRoot root;
	
	/**
	 * @param root
	 */
	public FedoraPackager(IProjectRoot root) {
		if (root == null)
			throw new NullPointerException();
		this.root = root;
	}
	
	/**
	 * Get the underlying Fedora project root
	 * 
	 * @return The Fedora project root.
	 */
	public IProjectRoot getFedoraProjectRoot() {
		return this.root;
	}
	
	/**
	 * Get a list of all registered Fedora packager command IDs. Each command id
	 * may be used to get the desired command instance from the registry using
	 * {@link FedoraPackager#getCommandInstance(String)}.
	 * 
	 * @return A list of registered command ids.
	 */
	public String[] getRegisteredCommandIDs() {
		ArrayList<String> cmdIdList = new ArrayList<String>();
		IExtensionPoint packagerCommandExtension = Platform
				.getExtensionRegistry().getExtensionPoint(
						PackagerPlugin.PLUGIN_ID, CMD_EXTENSIONPOINT_NAME);
		if (packagerCommandExtension != null) {
			for (IConfigurationElement command : packagerCommandExtension
					.getConfigurationElements()) {
				if (command.getName().equals(CMD_ELEMENT_NAME)) {
					String cmdId = command.getAttribute(CMD_ID_ATTRIBUTE_NAME);
					if (cmdId != null) {
						cmdIdList.add(cmdId);
					}
				}
			}
		}
		String[] result = new String[cmdIdList.size()];
		return cmdIdList.toArray(result);
	}
	
	/**
	 * Get a command instance from the Eclipse Fedora Packager command registry
	 * if available.
	 * 
	 * @param commandId
	 *            The unique identifier of the command to be instantiated.
	 * @return The initialized instance of a FedoraPackagerCommand with the
	 *         given Id.
	 * @throws FedoraPackagerCommandInitializationException
	 *             If command initialization failed.
	 * @throws FedoraPackagerCommandNotFoundException
	 *             If the command with the given id was not found.
	 */
	public FedoraPackagerCommand<?> getCommandInstance(String commandId)
			throws FedoraPackagerCommandInitializationException,
			FedoraPackagerCommandNotFoundException {
		IExtensionPoint packagerCommandExtension = Platform
				.getExtensionRegistry().getExtensionPoint(
						PackagerPlugin.PLUGIN_ID, CMD_EXTENSIONPOINT_NAME);
		if (packagerCommandExtension != null) {
			for (IConfigurationElement command : packagerCommandExtension
					.getConfigurationElements()) {
				if (command.getName().equals(CMD_ELEMENT_NAME)) {
					String cmdId = command.getAttribute(CMD_ID_ATTRIBUTE_NAME);
					if (cmdId.equals(commandId)) {
						// found extension point element with desired commandId
						try {
							FedoraPackagerCommand<?> commandContributor = (FedoraPackagerCommand<?>) command
									.createExecutableExtension(CMD_CLASS_ATTRIBUTE_NAME);
							assert commandContributor != null;
							// Do initialization
							commandContributor.initialize(this.root);
							return commandContributor;
						} catch (IllegalStateException e) {
							throw new FedoraPackagerCommandInitializationException(
									e.getMessage(), e);
						} catch (CoreException e) {
							throw new FedoraPackagerCommandInitializationException(
									e.getMessage(), e);
						}
					}
				}
			}
		}
		throw new FedoraPackagerCommandNotFoundException(NLS.bind(
				FedoraPackagerText.FedoraPackager_commandNotFoundError,
				root.getProductStrings().getDistributionName(), commandId));
	}
}

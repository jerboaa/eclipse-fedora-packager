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
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;

/**
 * Common superclass of all commands in the package
 * {@code org.fedoraproject.eclipse.packager.api}
 * <p>
 * This class ensures that all commands fulfill the {@link Callable} interface.
 * It also has a property {@link #projectRoot} holding a reference to the
 * {@link FedoraProjectRoot} this command should work with.
 * <p>
 * Finally this class stores a state telling whether it is allowed to call
 * {@link #call(IProgressMonitor)} on this instance. Instances of
 * {@link FedoraPackagerCommand} can only be used for one single successful call
 * to {@link #call(IProgressMonitor)}. Afterwards this instance may not be used
 * anymore to set/modify any properties or to call
 * {@link #call(IProgressMonitor)} again. This is achieved by setting the
 * {@link #callable} property to false after the successful execution of
 * {@link #call(IProgressMonitor)} and to check the state (by calling
 * {@link #checkCallable()}) before setting of properties and inside
 * {@link #call(IProgressMonitor)}.
 * 
 * Implementers of this class are responsible for calling listeners
 * appropriately.
 * 
 * @param <T>
 *            the return type which is expected from
 *            {@link #call(IProgressMonitor)}
 */
public abstract class FedoraPackagerCommand<T> {
	
	/**
	 * The project root to work with.
	 */
	protected IProjectRoot projectRoot;
	
	/**
	 * A list of command listeners to call
	 */
	private final ArrayList<ICommandListener> cmdListeners = new ArrayList<ICommandListener>();
	
	/**
	 * a state which tells whether it is allowed to call {@link #call()} on this
	 * instance.
	 */
	private boolean callable = true;

	/**
	 * Default 0-arg constructor.
	 */
	protected FedoraPackagerCommand() {
		// nothing
	}
	
	/**
	 * Initialize a FedoraPackagerCommand instance with the
	 * {@link IProjectRoot} to work with. Called on object creation.
	 * 
	 * @param projectRoot
	 * @throws FedoraPackagerCommandInitializationException
	 *             If the project root was already set. I.e. this method has
	 *             been called more than once.
	 */
	public void initialize(IProjectRoot projectRoot) throws FedoraPackagerCommandInitializationException {
		try {
			setProjectRoot(projectRoot);
		} catch (IllegalStateException e) {
			throw new FedoraPackagerCommandInitializationException(e.getMessage(), e);
		}
		// per default add config and state checker
		this.cmdListeners.add(new CheckConfigListener(this));
	}

	/**
	 * 
	 * @param projectRoot
	 *            the projectRoot to set
	 */
	private void setProjectRoot(IProjectRoot projectRoot)
			throws IllegalStateException {
		if (this.projectRoot != null) {
			throw new IllegalStateException(
					NLS.bind(
							FedoraPackagerText.FedoraPackagerCommand_projectRootSetTwiceError,
							projectRoot.getProductStrings().getDistributionName()));
		}
		this.projectRoot = projectRoot;
	}

	/**
	 * Checks that the property {@link #callable} is {@code true}. If not then
	 * an {@link IllegalStateException} is thrown.
	 *
	 * @throws IllegalStateException
	 *             when this method is called and the property {@link #callable}
	 *             is {@code false}
	 */
	protected void checkCallable() throws IllegalStateException {
		if (!callable)
			throw new IllegalStateException(NLS.bind(
					FedoraPackagerText.commandWasCalledInTheWrongState,
					this.getClass().getName()));
	}

	/**
	 * @param callable the callable to set
	 */
	protected void setCallable(boolean callable) {
		this.callable = callable;
	}
	
	/**
	 * Call pre-exec command listeners in order they have been
	 * added.
	 * 
	 * @throws CommandListenerException If any listener detected a problem.
	 * 
	 */
	protected void callPreExecListeners() throws CommandListenerException {
		for (ICommandListener listener: cmdListeners) {
			listener.preExecution();
		}
	}
	
	/**
	 * Call pre-exec command listeners in order they have been
	 * added.
	 * 
	 * @throws CommandListenerException If any listener detected a problem.
	 */
	protected void callPostExecListeners() throws CommandListenerException {
		for (ICommandListener listener: cmdListeners) {
			listener.postExecution();
		}
	}
	
	/**
	 * Configuration checking routine. This should check if all required
	 * parameters in order to successfully execute the command is present.
	 * 
	 * @throws IllegalStateException
	 */
	protected abstract void checkConfiguration() throws CommandMisconfiguredException;

	/**
	 * Add/register a command listener.
	 * 
	 * @param listener
	 */
	public void addCommandListener(ICommandListener listener) {
		this.cmdListeners.add(listener);
	}

	/**
	 * Executes the specific command. Each instance of this
	 * class should only be used for one invocation of the command. Don't call
	 * this method twice on an instance.
	 * 
	 * @param monitor
	 * @return The result of executing the command.
	 * @throws FedoraPackagerAPIException indicating the error which occurred.
	 */
	public abstract T call(IProgressMonitor monitor) throws FedoraPackagerAPIException;
}

/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Severin Gehwolf <sgehwolf@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.api;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;

/**
 * Common superclass of all commands in the package 
 * {@code org.fedoraproject.eclipse.packager.api}
 * <p>
 * This class ensures that all commands fulfill the {@link Callable} interface.
 * It also has a property {@link #projectRoot} holding a reference to the
 * {@link FedoraProjectRoot} this command should work with.
 * <p>
 * Finally this class stores a state telling whether it is allowed to call
 * {@link #call(IProgressMonitor)} on this instance. Instances of {@link FedoraPackagerCommand}
 * can only be used for one single successful call to {@link #call(IProgressMonitor)}.
 * Afterwards this instance may not be used anymore to set/modify any properties
 * or to call {@link #call(IProgressMonitor)} again. This is achieved by setting the 
 * {@link #callable} property to false after the successful execution of
 * {@link #call(IProgressMonitor)} and to check the state (by calling {@link #checkCallable()})
 * before setting of properties and inside {@link #call(IProgressMonitor)}.
 *
 * @param <T>
 *            the return type which is expected from {@link #call(IProgressMonitor)}
 */
public abstract class FedoraPackagerCommand<T> {
	
	/**
	 * The project root to work with.
	 */
	final protected FedoraProjectRoot projectRoot;
	
	/**
	 * a state which tells whether it is allowed to call {@link #call()} on this
	 * instance.
	 */
	private boolean callable = true;
	
	/**
	 * Creates a new command which interacts with a single repository
	 *
	 * @param repo
	 *            the {@link Repository} this command should interact with
	 */
	protected FedoraPackagerCommand(FedoraProjectRoot projectRoot) {
		this.projectRoot = projectRoot;
	}
	
	/**
	 * Checks that the property {@link #callable} is {@code true}. If not then
	 * an {@link IllegalStateException} is thrown
	 *
	 * @throws IllegalStateException
	 *             when this method is called and the property {@link #callable}
	 *             is {@code false}
	 */
	protected void checkCallable() throws IllegalStateException {
		if (!callable)
			throw new IllegalStateException(MessageFormat.format(
					FedoraPackagerText.get().commandWasCalledInTheWrongState,
					this.getClass().getName()));
	}

	/**
	 * @param callable the callable to set
	 */
	protected void setCallable(boolean callable) {
		this.callable = callable;
	}
	
	/**
	 * The main method for each FedoraPackager command. Calling this method
	 * starts commands execution.
	 * 
	 * @param monitor The Eclipse progress monitor.
	 * @return The generic return type.
	 * @throws Exception If an error occurs carrying out the command.
	 * 
	 */
	public abstract T call(IProgressMonitor monitor) throws Exception;
}

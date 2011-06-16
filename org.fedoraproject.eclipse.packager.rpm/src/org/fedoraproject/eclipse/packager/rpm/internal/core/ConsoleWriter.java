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
package org.fedoraproject.eclipse.packager.rpm.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Observable;

import org.eclipse.ui.console.MessageConsoleStream;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;

/**
 * Observable event source for writing to the Eclipse Fedora Packager console.
 * 
 */
public class ConsoleWriter extends Observable implements Runnable {
	
	boolean terminated;
	BufferedReader in;
	MessageConsoleStream out;

	/**
	 * Create a new console writer thread.
	 * 
	 * @param in
	 * 		The InputStream to read from.
	 * @param out
	 * 		The Eclipse MessageConsoleStream to write output to.
	 */
	public ConsoleWriter(InputStream in, MessageConsoleStream out) {
		this.out = out;
		this.in = new BufferedReader(new InputStreamReader(in));
		this.terminated = false;
	}

	@Override
	public void run() {
		try {
			long count = 0;
			long maxChars = Runtime.getRuntime().freeMemory() / 32;
			String line = null;
			// Use line based IO. Fixes Trac #42 (localized language problem).
			while (!terminated && (line = in.readLine()) != null) {
				out.write(line + '\n');
				count += line.length() + 1;
				
				// notify observers
				setChanged();
                notifyObservers(line);
                
				// Clear console every X characters.
				// X = available Memory / 32 (char == 16 bit); So 1/16 of available
				// memory seem to be good. May adjust as needed. Fixes Trac #55.
				if (count > maxChars) {
					count = 0;
					out.getConsole().clearConsole();
				}
			}
		} catch (IOException e) {
			//Log error, but do nothing about it
			FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
			logger.logError(FedoraPackagerText.ConsoleWriterThread_ioFail, e);
		}
	}
	
	/**
	 * Terminate any ongoing reading/writing.
	 */
	public void stop() {
		this.terminated = true;
	}

}

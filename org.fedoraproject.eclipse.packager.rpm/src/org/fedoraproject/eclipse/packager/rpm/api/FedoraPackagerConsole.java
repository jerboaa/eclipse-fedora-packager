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
package org.fedoraproject.eclipse.packager.rpm.api;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;

/**
 * MessageConsole related code for Eclipse Fedora Packager
 *
 */
public class FedoraPackagerConsole {
	
	private static final String CONSOLE_NAME = "Packager Console"; //$NON-NLS-1$
	
	/**
	 * @return A console instance.
	 */
	public static MessageConsole getConsole() {
		MessageConsole ret = null;
		for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles()) {
			if (cons.getName().equals(CONSOLE_NAME)) {
				ret = (MessageConsole) cons;
			}
		}
		// no existing console, create new one
		if (ret == null) {
			ret = new MessageConsole(CONSOLE_NAME,
					RPMPlugin.getImageDescriptor("icons/rpm.gif")); //$NON-NLS-1$
		}
		ret.clearConsole();
		return ret;
	}
}

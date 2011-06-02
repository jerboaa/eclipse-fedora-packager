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

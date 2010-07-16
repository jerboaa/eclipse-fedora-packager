package org.fedoraproject.eclipse.packager;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.messages"; //$NON-NLS-1$
	public static String ConsoleWriterThread_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}

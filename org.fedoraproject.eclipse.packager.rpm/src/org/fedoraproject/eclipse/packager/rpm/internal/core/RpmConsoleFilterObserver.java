package org.fedoraproject.eclipse.packager.rpm.internal.core;

import java.util.Observable;
import java.util.Observer;

import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildResult;

/**
 * Observes, what is being printed and filters lines ending
 * with .srpm and .rpm. This is useful for parsing which RPMs/SRPMs
 * have been produced by a build command.
 * 
 * @see ConsoleWriter
 *
 */
public class RpmConsoleFilterObserver implements Observer {
	
	
	/**
	 * The suffix of binary RPMs.
	 */
	public static final String RPM_SUFFIX = ".rpm"; //$NON-NLS-1$
	/**
	 * The suffix of source RPMs.
	 */
	public static final String SRPM_SUFFIX = ".src.rpm"; //$NON-NLS-1$

	private RpmBuildResult result;
	
	/**
	 * @param result The result to store the console msg into
	 */
	public RpmConsoleFilterObserver(RpmBuildResult result) {
		this.result = result;
	}
	
	/**
	 * Does the filtering of relevant lines.
	 */
	@Override
	public void update(Observable obj, Object arg) {
		if (arg instanceof String) {
			String line = (String) arg;
			if (line.endsWith(SRPM_SUFFIX)) {
				result.addSrpm(line);
			} else if (line.endsWith(RPM_SUFFIX)) {
				result.addRpm(line);
			}
        }

	}

}

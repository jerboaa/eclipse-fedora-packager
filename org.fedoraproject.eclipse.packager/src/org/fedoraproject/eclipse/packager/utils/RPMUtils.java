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
package org.fedoraproject.eclipse.packager.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
import org.fedoraproject.eclipse.packager.IProjectRoot;

/**
 * Utility class for RPM related things.
 * 
 */
public class RPMUtils {

	/**
	 * Creates a list of rpm defines to use the given directory as a base
	 * directory.
	 * 
	 * @param dir
	 *            The base directory.
	 * @return Defines to instruct rpmbuild to use given directory.
	 */
	public static List<String> getRPMDefines(String dir) {
		ArrayList<String> rpmDefines = new ArrayList<String>();
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_sourcedir " + dir); //$NON-NLS-1$
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_builddir " + dir); //$NON-NLS-1$
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_srcrpmdir " + dir); //$NON-NLS-1$
		rpmDefines.add("--define"); //$NON-NLS-1$
		rpmDefines.add("_rpmdir " + dir); //$NON-NLS-1$

		return rpmDefines;
	}

	/**
	 * Submit a query to RPM. Uses org.eclipse.linuxtools.rpm.Utils.
	 * 
	 * @param projectRoot
	 * @param format
	 * @param bci
	 * @return The result of the query.
	 * @throws IOException
	 *             If rpm command failed.
	 */
	public static String rpmQuery(IProjectRoot projectRoot, String format,
			BranchConfigInstance bci) throws IOException {
		IResource parent = projectRoot.getSpecFile().getParent();
		String dir = parent.getLocation().toString();
		List<String> defines = getRPMDefines(dir);
		List<String> distDefines = getDistDefines(bci);

		String result = null;
		defines.add(0, "rpm"); //$NON-NLS-1$
		defines.addAll(distDefines);
		defines.add("-q"); //$NON-NLS-1$
		defines.add("--qf"); //$NON-NLS-1$
		defines.add("%{" + format + "}\\n"); //$NON-NLS-1$//$NON-NLS-2$
		defines.add("--specfile"); //$NON-NLS-1$
		defines.add(projectRoot.getSpecFile().getLocation().toString());

		result = Utils.runCommandToString(defines.toArray(new String[0]));

		return result.substring(0, result.indexOf('\n'));
	}

	/**
	 * Creates a tag name as expected from Fedora infrastructure based on the
	 * N-V-R and return its string representation.
	 * 
	 * @param projectRoot
	 *            Container used for retrieving needed data.
	 * @param bci
	 *            Current branch configuration.
	 * @return The tag name.
	 * @throws IOException
	 */
	public static String makeTagName(IProjectRoot projectRoot,
			BranchConfigInstance bci) throws IOException {
		return getNVR(projectRoot, bci).replaceAll("\\.", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the N-V-R retrieved from the .spec file in the project root.
	 * 
	 * @param projectRoot
	 *            Container used to retrieve the needed data.
	 * @param bci
	 *            Current branch configuration.
	 * @return N-V-R (Name-Version-Release) retrieved.
	 * @throws IOException
	 *             if RPM query failed.
	 */
	public static String getNVR(IProjectRoot projectRoot,
			BranchConfigInstance bci) throws IOException {
		String name = rpmQuery(projectRoot, "NAME", bci).replaceAll("^[0-9]+", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String version = rpmQuery(projectRoot, "VERSION", bci); //$NON-NLS-1$
		String release = rpmQuery(projectRoot, "RELEASE", bci); //$NON-NLS-1$
		return (name + "-" + version + "-" + release); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Get distribution definitions required for RPM build.
	 * 
	 * @param bci
	 *            The BranchConfigInstance on which to base the definitions.
	 * @return A list of required dist-defines.
	 */
	public static List<String> getDistDefines(BranchConfigInstance bci) {
		// substitution for rhel
		ArrayList<String> distDefines = new ArrayList<String>();
		String distvar = bci.getDistVariable().equals("epel") ? "rhel" //$NON-NLS-1$//$NON-NLS-2$ 
				: bci.getDistVariable();
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add("dist " + bci.getDist()); //$NON-NLS-1$
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add(distvar + ' ' + bci.getDistVal());
		return distDefines;
	}
}

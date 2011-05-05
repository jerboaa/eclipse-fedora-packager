package org.fedoraproject.eclipse.packager.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;

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
	 * @return The result of the query.
	 * @throws IOException If rpm command failed.
	 */
	public static String rpmQuery(FedoraProjectRoot projectRoot, String format)
			throws IOException {
		IResource parent = projectRoot.getSpecFile().getParent();
		String dir = parent.getLocation().toString();
		List<String> defines = getRPMDefines(dir);
		IFpProjectBits projectBits = FedoraPackagerUtils
				.getVcsHandler(projectRoot);
		List<String> distDefines = getDistDefines(projectBits, parent.getName());

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
	 * @return The tag name.
	 * @throws IOException
	 */
	public static String makeTagName(FedoraProjectRoot projectRoot)
			throws IOException {
		return getNVR(projectRoot).replaceAll("\\.", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the N-V-R retrieved from the .spec file in the project root.
	 * 
	 * @param projectRoot
	 *            Container used to retrieve the needed data.
	 * @return N-V-R (Name-Version-Release) retrieved.
	 * @throws IOException if RPM query failed.
	 */
	public static String getNVR(FedoraProjectRoot projectRoot)
			throws IOException {
		String name = rpmQuery(projectRoot, "NAME").replaceAll("^[0-9]+", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String version = rpmQuery(projectRoot, "VERSION"); //$NON-NLS-1$
		String release = rpmQuery(projectRoot, "RELEASE"); //$NON-NLS-1$
		return (name + "-" + version + "-" + release); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Get distribution definitions required for RPM build.
	 * 
	 * @param projectBits
	 * @param parentName
	 * @return A list of required dist-defines.
	 */
	public static List<String> getDistDefines(IFpProjectBits projectBits,
			String parentName) {
		// substitution for rhel
		ArrayList<String> distDefines = new ArrayList<String>();
		String distvar = projectBits.getDistVariable().equals("epel") ? "rhel" //$NON-NLS-1$//$NON-NLS-2$ 
				: projectBits.getDistVariable();
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add("dist " + projectBits.getDist()); //$NON-NLS-1$
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add(distvar + ' ' + projectBits.getDistVal());
		return distDefines;
	}
}

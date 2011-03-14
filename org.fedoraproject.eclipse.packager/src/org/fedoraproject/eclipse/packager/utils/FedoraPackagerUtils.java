package org.fedoraproject.eclipse.packager.utils;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;

/**
 * Utility class for Fedora Packager. Put commonly used code in here as long
 * as it's not RPM related. If it's RPM related, RPMUtils is the better choice.
 */
public class FedoraPackagerUtils {

	private static final String GIT_REPOSITORY = "org.eclipse.egit.core.GitProvider"; //$NON-NLS-1$
	private static final String CVS_REPOSITORY = "org.eclipse.team.cvs.core.cvsnature"; //$NON-NLS-1$

	/**
	 * Type of the Fedora project root based on the underlying VCS system. 
	 */
	public static enum ProjectType {
		/** Git project */
		GIT,
		/** Cvs project */
		CVS,
		/** Unknown */
		UNKNOWN
	}

	/**
	 * A valid project root contains a {@code .spec} file and a {@code sources}
	 * file. The RPM spec-file must be of the form {@code package-name.spec}.
	 * 
	 * @param resource
	 * @return True if the project root looks right.
	 */
	private static boolean isValidFedoraProjectRoot(IContainer resource) {
		IFile sourceFile = resource.getFile(new Path("sources")); //$NON-NLS-1$
		// FIXME: Determine rpm package name from a persistent property. In
		// future the project name might not be equal to the RPM package name.
		IFile specFile = resource.getFile(new Path(resource.getProject()
				.getName() + ".spec")); //$NON-NLS-1$
		if (sourceFile.exists() && specFile.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns a FedoraProjectRoot from the given resource after performing some
	 * validations.
	 * 
	 * @param resource
	 *            The container for this Fedora project root or a resource
	 *            within it.
	 * @throws InvalidProjectRootException
	 *             If the project root does not contain a .spec with the proper
	 *             name or doesn't contain a sources file.
	 * 
	 * @return The retrieved FedoraProjectRoot.
	 */
	public static FedoraProjectRoot getProjectRoot(IResource resource)
			throws InvalidProjectRootException {
		IContainer canditate = null;
		if (resource instanceof IFolder || resource instanceof IProject) {
			canditate = (IContainer) resource;
		} else if (resource instanceof IFile) {
			canditate = resource.getParent();
		}
		if (canditate != null && isValidFedoraProjectRoot(canditate)) {
			return new FedoraProjectRoot(canditate);
		} else {
			throw new InvalidProjectRootException(FedoraPackagerText.FedoraPackagerUtils_invalidProjectRootError);
		}
	}
	
	/**
	 * Returns the project type determined from the given IResource.
	 * @param resource The base for determining the project type.
	 * @return The project type.
	 */
	public static ProjectType getProjectType(IResource resource) {

		Map<?,?> persistentProperties = null;
		try {
			persistentProperties = resource.getProject()
					.getPersistentProperties();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		QualifiedName name = new QualifiedName("org.eclipse.team.core", //$NON-NLS-1$
				"repository"); //$NON-NLS-1$
		String repository = (String) persistentProperties.get(name);
		if (GIT_REPOSITORY.equals(repository)) {
			return ProjectType.GIT;
		} else if (CVS_REPOSITORY.equals(repository)) {
			return ProjectType.CVS;
		}
		return ProjectType.UNKNOWN;
	}

	/**
	 * Returns the IFpProjectBits used to abstract vcs specific things.
	 * 
	 * @param fedoraprojectRoot The project for which to get the VCS specific parts.
	 * @return The needed IFpProjectBits.
	 */
	public static IFpProjectBits getVcsHandler(FedoraProjectRoot fedoraprojectRoot) {
		IResource project = fedoraprojectRoot.getProject();
		ProjectType type = getProjectType(project);
		IExtensionPoint vcsExtensions = Platform.getExtensionRegistry()
				.getExtensionPoint(PackagerPlugin.PLUGIN_ID, "vcsContribution"); //$NON-NLS-1$
		if (vcsExtensions != null) {
			IConfigurationElement[] elements = vcsExtensions
					.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("vcs") //$NON-NLS-1$
						&& (elements[i].getAttribute("type") //$NON-NLS-1$
								.equals(type.name()))) {
					try {
						IConfigurationElement bob = elements[i];
						IFpProjectBits vcsContributor = (IFpProjectBits) bob
								.createExecutableExtension("class");  //$NON-NLS-1$
						// Do initialization
						if (vcsContributor != null) {
							vcsContributor.initialize(fedoraprojectRoot);
						}
						return vcsContributor;
					} catch (CoreException e) {
						e.printStackTrace();
					}

				}
			}
		}
		return null;
	}
	
	/**
	 * Checks if <code>candidate</code> is a valid file for uploading.
	 * I.e. is non-empty and has a valid file extension. Valid file extensions
	 * are: <code>'tar', 'gz', 'bz2', 'lzma', 'xz', 'Z', 'zip', 'tff', 'bin',
     *            'tbz', 'tbz2', 'tlz', 'txz', 'pdf', 'rpm', 'jar', 'war', 'db',
     *            'cpio', 'jisp', 'egg', 'gem'</code>
	 * 
	 * @param candidate
	 * @return <code>true</code> if <code>candidate</code> is a valid file for uploading
	 * 		   <code>false</code> otherwise.
	 */
	public static boolean isValidUploadFile(File candidate) {
		if (candidate.length() != 0) {
			Pattern extensionPattern = Pattern.compile("^.*\\.(?:tar|gz|bz2|lzma|xz|Z|zip|tff|bin|tbz|tbz2|tlz|txz|pdf|rpm|jar|war|db|cpio|jisp|egg|gem)$"); //$NON-NLS-1$
			Matcher extMatcher = extensionPattern.matcher(candidate.getName());
			if (extMatcher.matches()) {
				// file extension seems to be good
				return true;
			}
		}
		return false;
	}
}

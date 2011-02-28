package org.fedoraproject.eclipse.packager.utils;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.ssl.Certificates;
import org.apache.commons.ssl.KeyMaterial;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;
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
	 * Do some sanity checks to make sure we have a properly structured Fedora
	 * project root. At the moment this only checks if a sources file is
	 * present.
	 * 
	 * @param resource
	 * @return True if the project root looks right.
	 */
	private static boolean validateFedorapackageRoot(IContainer resource) {
		IFile file = resource.getFile(new Path("sources")); //$NON-NLS-1$
		if (file.exists()) {
			return true;
		}
		// TODO: Add check if .spec file is present
		return false;
	}

	/**
	 * Returns a FedoraProjectRoot from the given resource.
	 * 
	 * @param resource
	 *            The underlying resource of the Fedora project root or
	 *            a resource within it.
	 * @throws InvalidProjectRootException
	 *             If the project root does not contain a .spec with the proper
	 *             name or doesn't contain a sources file.
	 * 
	 * @return The retrieved FedoraProjectRoot.
	 */
	public static FedoraProjectRoot getValidRoot(IResource resource) throws InvalidProjectRootException {
		IContainer canditate = null;
		if (resource instanceof IFolder || resource instanceof IProject) {
			canditate = (IContainer) resource;
		} else if (resource instanceof IFile) {
			canditate = resource.getParent();
		}
		if (canditate != null && validateFedorapackageRoot(canditate)) {
			return new FedoraProjectRoot(canditate);
		} else {
			// TODO: Externalize
			throw new InvalidProjectRootException("Invalid Fedora project root");
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
	
	/**
	 * Determine FAS username from <code>.fedora.cert</code>.
	 * 
	 * @return Username if retrieval is successful. <code>"anonymous"</code> otherwise.
	 */
	//TODO: Move this into Handler independent utility class?
	public static String getUsernameFromCert() {
		String file = System.getProperty("user.home") + IPath.SEPARATOR //$NON-NLS-1$
				+ ".fedora.cert"; //$NON-NLS-1$
		File cert = new File(file);
		if (cert.exists()) {
			KeyMaterial kmat;
			try {
				kmat = new KeyMaterial(cert, cert, new char[0]);
				List<?> chains = kmat.getAssociatedCertificateChains();
				Iterator<?> it = chains.iterator();
				ArrayList<String> cns = new ArrayList<String>();
				while (it.hasNext()) {
					X509Certificate[] certs = (X509Certificate[]) it.next();
					if (certs != null) {
						for (int i = 0; i < certs.length; i++) {
							cns.add(Certificates.getCN(certs[i]));
						}
					}
				}
				return cns.get(0);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "anonymous"; //$NON-NLS-1$
	}
}

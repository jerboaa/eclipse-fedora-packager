package org.fedoraproject.eclipse.packager.rpm.api;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.errors.ArchitectureNotSupportedException;
import org.fedoraproject.eclipse.packager.rpm.api.errors.InvalidMockConfigurationException;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Command for building a package in a chroot'ed environment
 * using {@code mock}.
 *
 */
public class MockBuildCommand extends FedoraPackagerCommand<MockBuildResult> {

	private String localArchitecture; // set in initialize()
	private String buildArchitecture;
	private String mockConfig; // user may set this explicitly

	/**
	 * Set the build architecture for which to build.
	 * 
	 * @param candidate
	 * @return This instance.
	 * @throws ArchitectureNotSupportedException
	 *             If the build architecture does not make sense.
	 */
	public MockBuildCommand buildArch(String candidate)
			throws ArchitectureNotSupportedException {
		if (!isArchitectureSupported(candidate)) {
			throw new ArchitectureNotSupportedException(NLS.bind(
					RpmText.MockBuildCommand_archException, candidate,
					localArchitecture));
		}
		this.buildArchitecture = candidate;
		return this;
	}
	
	/**
	 * Set the mock config.
	 * 
	 * @param mockConfig
	 * @return This instance.
	 * @throws InvalidMockConfigurationException If the config was invalid.
	 */
	public MockBuildCommand mockConfig(String mockConfig) throws InvalidMockConfigurationException {
		if (!isSupportedMockConfig(mockConfig)) {
			throw new InvalidMockConfigurationException(NLS.bind(
					RpmText.MockBuildCommand_invalidMockConfigError, mockConfig));
		}
		this.mockConfig = mockConfig;
		return this;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand#
	 * checkConfiguration()
	 */
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// require build arch to be set
		if (buildArchitecture == null) {
			throw new CommandMisconfiguredException(
					RpmText.MockBuildCommand_buildArchNullError);
		}
	}

	/**
	 * 
	 */
	@Override
	public MockBuildResult call(IProgressMonitor monitor)
			throws FedoraPackagerAPIException {
		// TODO Implement
		
		// 1. Create SRPM
		// 2. 
		return null;
	}
	
	/**
	 * Get a default mock config for the configured build architecture.
	 * 
	 * @param projectRoot
	 * @param buildarch
	 * @return
	 */
	private String getMockcfg() {
		assert this.buildArchitecture != null && this.mockConfig == null;
		IFpProjectBits projectBits =  FedoraPackagerUtils.getVcsHandler(projectRoot);
		String distvar = projectBits.getDistVariable(); 
		String distval = projectBits.getDistVal(); 
		String mockcfg = null;
		if (distvar.equals("rhel")) { //$NON-NLS-1$
			mockcfg = "epel-" + distval + "-" + this.buildArchitecture; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			mockcfg = "fedora-" + distval + "-" + this.buildArchitecture; //$NON-NLS-1$ //$NON-NLS-2$
			if (distval.equals("4") || distval.equals("5") //$NON-NLS-1$ //$NON-NLS-2$
					|| distval.equals("6")) { //$NON-NLS-1$
				mockcfg += "-core"; //$NON-NLS-1$
			}
			
			if (projectBits.getCurrentBranchName().equals("devel")) { //$NON-NLS-1$
				mockcfg = "fedora-devel-" + this.buildArchitecture; //$NON-NLS-1$
			}
			
			if (projectBits.getCurrentBranchName().equals("devel")) { //$NON-NLS-1$
				if (!isSupportedMockConfig(mockcfg)) {
					// If the mockcfg as determined from above does not exist,
					// do something reasonable.
					mockcfg = "fedora-devel-" + this.buildArchitecture;  //$NON-NLS-1$
				}
			}
		}
		return mockcfg;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand#initialize(org.fedoraproject.eclipse.packager.FedoraProjectRoot)
	 */
	@Override
	public void initialize(FedoraProjectRoot fp) throws FedoraPackagerCommandInitializationException {
		super.initialize(fp);
		// set the local architecture
		EvalResult archResult;
		try {
			FedoraPackager packager = new FedoraPackager(this.projectRoot);
			RpmEvalCommand eval = (RpmEvalCommand) packager.getCommandInstance(RpmEvalCommand.ID);
			archResult = eval.variable(RpmEvalCommand.ARCH).call(new NullProgressMonitor());
		} catch (FedoraPackagerAPIException e) {
			throw new FedoraPackagerCommandInitializationException(e.getMessage(), e);
		}
		this.localArchitecture = archResult.getEvalResult();
	}

	/**
	 * Determine if candidate works in theory. I.e. i386 should work on x86_64.
	 * @param candidate
	 * @return {@code true} if the candidate makes sense. {@code false} otherwise.
	 */
	private boolean isArchitectureSupported(String candidate) {
		if (!localArchitecture.equals(candidate)) {
			// For now only handle x86_64 => i386
			if (localArchitecture.equals("x86_64") && //$NON-NLS-1$ 
					candidate.equals("i386")) { //$NON-NLS-1$
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Determine if mock program is available
	 * 
	 * @return {@code true} if mock is available, {@code false} otherwise.
	 */
	private boolean isMockInstalled() {
		if (Utils.fileExist("/usr/bin/mock")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	/**
	 * Determine if the given mock config is valid. I.e. a config file
	 * exists in /etc/mock
	 * 
	 * @param candidate
	 * @return {@code true} if the mock config exists on the local system for
	 *         the given string, {@code false} otherwise.
	 */
	private boolean isSupportedMockConfig(String candidate) {
		File file = new File("/etc/mock/" + candidate + ".cfg"); //$NON-NLS-1$ //$NON-NLS-2$
		if (file.exists()) {
			return true;
		}
		return false;
	}
}

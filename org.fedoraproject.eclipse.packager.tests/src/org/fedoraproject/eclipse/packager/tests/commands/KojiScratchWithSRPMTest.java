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
package org.fedoraproject.eclipse.packager.tests.commands;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient;
import org.fedoraproject.eclipse.packager.koji.api.KojiBuildCommand;
import org.fedoraproject.eclipse.packager.koji.api.KojiSSLHubClient;
import org.fedoraproject.eclipse.packager.koji.api.KojiUploadSRPMCommand;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildResult;
import org.fedoraproject.eclipse.packager.rpm.api.SRPMBuildJob;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KojiScratchWithSRPMTest {
	
	private static final String KOJI_TEST_INSTANCE_URL_PROP = "org.fedoraproject.eclipse.packager.tests.koji.testInstanceURL"; //$NON-NLS-1$
	// project under test
	private GitTestProject testProject;
	// Fedora packager root
	private IProjectRoot fpRoot;
	// main interface class
	private FedoraPackager packager;
	// srpm build command command
	private RpmBuildCommand srpmBuild;
	//result of building srpm
	RpmBuildResult srpmBuildResult;
	
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("ed");
		this.fpRoot = FedoraPackagerUtils.getProjectRoot(this.testProject
				.getProject());
		testProject.checkoutBranch("f15");
		this.packager = new FedoraPackager(fpRoot);
		srpmBuild = (RpmBuildCommand) packager
		.getCommandInstance(RpmBuildCommand.ID);
		DownloadSourceCommand download = (DownloadSourceCommand) packager
			.getCommandInstance(DownloadSourceCommand.ID);
		download.call(new NullProgressMonitor());
		SRPMBuildJob srpmBuildJob = new SRPMBuildJob(NLS.bind(
				RpmText.MockBuildHandler_creatingSRPMForMockBuild,
				fpRoot.getPackageName()), srpmBuild,
				fpRoot);
		srpmBuildJob.setUser(false);
		srpmBuildJob.schedule();
		srpmBuildJob.join();
		srpmBuildResult = srpmBuildJob.getSRPMBuildResult();
	}
	
	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}
	
	/**
	 * In order for this test to work, koji test certificates need to be at
	 * location: ~/.eclipse-fedorapackager/testing/koji-certs
	 * 
	 * Also, it is required to set Java System property
	 * "org.fedoraproject.eclipse.packager.tests.koji.testInstanceURL" to point
	 * to the koji test instance.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canUploadSRPMAndRequestBuild() 
	throws Exception{
		KojiUploadSRPMCommand uploadSRPMCommand = 
			(KojiUploadSRPMCommand) packager
			.getCommandInstance(KojiUploadSRPMCommand.ID);
		final String uploadPath = "cli-build/" + 
			FedoraPackagerUtils.getUniqueIdentifier();  //$NON-NLS-1$
		String kojiURL = System.getProperty(KOJI_TEST_INSTANCE_URL_PROP);
		if (kojiURL == null){
			fail("System property testing.koji.url not set.");
		}
		try {
			IKojiHubClient kojiClient = new KojiSSLHubClient(kojiURL) {
				@Override
				protected void initSSLConnection() 
				throws FileNotFoundException, GeneralSecurityException, 
				IOException {
					// Create empty HostnameVerifier
					HostnameVerifier hv = new HostnameVerifier() {
						@Override
						public boolean verify(String arg0, SSLSession arg1) {
							return true;
						}
					};
					String certDir = System.getProperty("user.home") + 
						File.separatorChar + ".eclipse-fedorapackager" + 
						File.separatorChar + "testing" + File.separatorChar + 
						"koji-certs";
					FedoraSSL fedoraSSL = FedoraSSLFactory.getInstance(
							new File(certDir + File.separatorChar + 
									"client.crt"),
							new File(certDir + File.separatorChar + 
									"clientca.crt"),
							new File(certDir + File.separatorChar + 
									"serverca.crt"));
					SSLContext ctxt = null;
					// may throw exceptions (dealt with in login())
			 	    ctxt = fedoraSSL.getInitializedSSLContext();
					// set up the proper socket
					HttpsURLConnection.setDefaultSSLSocketFactory(
							ctxt.getSocketFactory());
					HttpsURLConnection.setDefaultHostnameVerifier(hv);
				}
			};
		kojiClient.login();
		IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fpRoot);
		assertTrue(uploadSRPMCommand.setKojiClient(kojiClient)
				.setRemotePath(uploadPath).setSRPM(
						srpmBuildResult.getAbsoluteSRPMFilePath())
			.call(new NullProgressMonitor()).wasSuccessful());
		KojiBuildCommand kojiBuildCmd = (KojiBuildCommand) packager
			.getCommandInstance(KojiBuildCommand.ID);
		kojiBuildCmd.setKojiClient(kojiClient);
		kojiBuildCmd.sourceLocation(uploadPath + "/" + 
				new File(srpmBuildResult.getAbsoluteSRPMFilePath()).getName()); //$NON-NLS-1$
		String nvr = RPMUtils.getNVR(fpRoot);
		kojiBuildCmd.buildTarget(projectBits.getTarget()).nvr(nvr)
			.isScratchBuild(true);
		assertTrue(kojiBuildCmd.call(
				new NullProgressMonitor()).wasSuccessful());
		} catch (Exception e){
			fail(e.getMessage());
			throw e;
		}
	}
}

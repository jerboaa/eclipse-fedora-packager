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
package org.fedoraproject.eclipse.packager.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.ILookasideCache;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.LookasideCache;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.tests.utils.TestsUtils;
import org.fedoraproject.eclipse.packager.tests.utils.cvs.CVSTestProject;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils.ProjectType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FedoraProjectRootTest {

	private IProject projectResource;
	private IProjectRoot fpRoot;
	private GitTestProject gitTestProject;
	private CVSTestProject cvsTestProject;

	private static final String SOURCE_FILE_NAME = "project_sources.zip";
	private static final String PACKAGE_NAME = "example-fedora-project";
	private static final String GIT_IGNOREFILE_NAME = ".gitignore";
	@SuppressWarnings("unused")
	private static final String CVS_IGNOREFILE_NAME = ".cvsignore";
	private static final String EXAMPLE_FEDORA_PROJECT_ROOT = "resources/example-fedora-project"; // $NON-NLS-1$

	@Before
	public void setUp() throws Exception {
		String dirName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(EXAMPLE_FEDORA_PROJECT_ROOT), null)).getFile();
		File copySource = new File(dirName);

		projectResource = TestsUtils.createProjectFromTemplate(copySource);
		// Users should really use FedoraPackagerUtils.getProjectRoot(), but
		// this
		// doesn't work for this case.
		fpRoot = new FedoraProjectRoot();
		fpRoot.initialize(projectResource, ProjectType.GIT);
		assertNotNull(fpRoot);
	}

	@After
	public void tearDown() throws Exception {
		projectResource.delete(true, null);
		fpRoot = null;
		if (gitTestProject != null) {
			gitTestProject.dispose();
		}
		if (cvsTestProject != null) {
			cvsTestProject.dispose();
		}
	}

	@Test
	public void canCreateFedoraProjectRoot() throws Exception {
		// Dummy Fedora project root
		fpRoot = null;
		assertNull(fpRoot);
		fpRoot = new FedoraProjectRoot();
		fpRoot.initialize(projectResource, ProjectType.GIT);
		assertNotNull(fpRoot);

		// Git case
		fpRoot = null;
		gitTestProject = new GitTestProject("eclipse-fedorapackager");
		fpRoot = FedoraPackagerUtils
				.getProjectRoot(gitTestProject.getProject());
		assertNotNull(fpRoot);
		assertTrue(fpRoot.getProjectType() == ProjectType.GIT);

		// CVS case is turned off.
		// fpRoot = null;
		// cvsTestProject = new CVSTestProject();
		// cvsTestProject.checkoutModule("eclipse-rpm-editor");
		// IContainer cvsF12Container = (IContainer) cvsTestProject.getProject()
		// .findMember(new Path("F-12"));
		// fpRoot = FedoraPackagerUtils.getProjectRoot(cvsF12Container);
		// assertNotNull(fpRoot);
		// assertTrue(fpRoot.getProjectType() == ProjectType.CVS);
	}

	@Test
	public void testGetContainer() throws Exception {
		assertNotNull(fpRoot.getContainer());
		assertSame(projectResource, fpRoot.getContainer());

		// Git case
		gitTestProject = new GitTestProject("eclipse-fedorapackager");
		fpRoot = FedoraPackagerUtils
				.getProjectRoot(gitTestProject.getProject());
		assertNotNull(fpRoot);
		// Project is container
		assertSame(gitTestProject.getProject(), fpRoot.getContainer());

		// // CVS case
		// cvsTestProject = new CVSTestProject();
		// cvsTestProject.checkoutModule("eclipse-rpm-editor");
		// IContainer cvsF12Container = (IContainer) cvsTestProject.getProject()
		// .findMember(new Path("F-12"));
		// assertNotNull(cvsF12Container);
		// fpRoot = FedoraPackagerUtils.getProjectRoot(cvsF12Container);
		// assertNotNull(fpRoot);
		// // Branch folder is container
		// assertSame(cvsF12Container, fpRoot.getContainer());
	}

	@Test
	public void testGetProject() throws Exception {
		// Git case
		fpRoot = null;
		gitTestProject = new GitTestProject("eclipse-fedorapackager");
		fpRoot = FedoraPackagerUtils
				.getProjectRoot(gitTestProject.getProject());
		assertNotNull(fpRoot);
		// Project is container
		assertSame(gitTestProject.getProject(), fpRoot.getProject());

		// CVS case
		// cvsTestProject = new CVSTestProject();
		// cvsTestProject.checkoutModule("eclipse-rpm-editor");
		// IContainer cvsF12Container = (IContainer) cvsTestProject.getProject()
		// .findMember(new Path("F-12"));
		// assertNotNull(cvsF12Container);
		// fpRoot = null;
		// fpRoot = FedoraPackagerUtils.getProjectRoot(cvsF12Container);
		// assertNotNull(fpRoot);
		// // Branch folder is container
		// assertSame(cvsTestProject.getProject(), fpRoot.getProject());
	}

	@Test
	public void testGetSourcesFile() {
		SourcesFile sourcesFile = fpRoot.getSourcesFile();
		assertNotNull(sourcesFile.getCheckSum(SOURCE_FILE_NAME));
	}

	@Test
	public void testGetSpecFile() {
		IFile specFile = fpRoot.getSpecFile();
		assertNotNull(specFile);
		assertEquals("spec", specFile.getFileExtension());
	}

	@Test
	public void testGetSpecfileModel() {
		Specfile specModel = fpRoot.getSpecfileModel();
		assertEquals(PACKAGE_NAME, specModel.getName());
	}

	@Test
	public void testGetProjectType() throws Exception {
		// Git case
		fpRoot = null;
		gitTestProject = new GitTestProject("eclipse-fedorapackager");
		fpRoot = FedoraPackagerUtils
				.getProjectRoot(gitTestProject.getProject());
		assertNotNull(fpRoot);
		assertEquals(ProjectType.GIT, fpRoot.getProjectType());

		// // CVS case
		// cvsTestProject = new CVSTestProject();
		// cvsTestProject.checkoutModule("eclipse-rpm-editor");
		// IContainer cvsF12Container = (IContainer) cvsTestProject.getProject()
		// .findMember(new Path("F-12"));
		// assertNotNull(cvsF12Container);
		// fpRoot = FedoraPackagerUtils.getProjectRoot(cvsF12Container);
		// assertNotNull(fpRoot);
		// assertEquals(ProjectType.CVS, fpRoot.getProjectType());
	}

	@Test
	public void testGetIgnoreFile() throws Exception {
		IFile ignoreFile = fpRoot.getIgnoreFile();
		// we don't have an ignore file
		assertTrue(!ignoreFile.exists());

		// Git case
		fpRoot = null;
		gitTestProject = new GitTestProject("eclipse-fedorapackager");
		fpRoot = FedoraPackagerUtils
				.getProjectRoot(gitTestProject.getProject());
		assertNotNull(fpRoot);
		ignoreFile = fpRoot.getIgnoreFile();
		assertNotNull(ignoreFile);
		assertEquals(GIT_IGNOREFILE_NAME, ignoreFile.getName());

		// CVS case
		// cvsTestProject = new CVSTestProject();
		// cvsTestProject.checkoutModule("eclipse-rpm-editor");
		// IContainer cvsF12Container = (IContainer) cvsTestProject.getProject()
		// .findMember(new Path("F-12"));
		// assertNotNull(cvsF12Container);
		// fpRoot = FedoraPackagerUtils.getProjectRoot(cvsF12Container);
		// assertNotNull(fpRoot);
		// ignoreFile = fpRoot.getIgnoreFile();
		// assertNotNull(ignoreFile);
		// assertEquals(CVS_IGNOREFILE_NAME, ignoreFile.getName());
	}

	@Test
	public void testGetLookAsideCache() {
		ILookasideCache lookasideCache = fpRoot.getLookAsideCache();
		assertNotNull(lookasideCache);
		// should be initialized with default values
		assertEquals(LookasideCache.DEFAULT_FEDORA_DOWNLOAD_URL, lookasideCache
				.getDownloadUrl().toString());
		assertEquals(LookasideCache.DEFAULT_FEDORA_UPLOAD_URL, lookasideCache
				.getUploadUrl().toString());
	}

	@Test
	public void canRetrieveNVRs() throws Exception {
		fpRoot = null;
		gitTestProject = new GitTestProject("eclipse-mylyn-tasks");
		gitTestProject.checkoutBranch("f15");
		fpRoot = FedoraPackagerUtils
				.getProjectRoot(gitTestProject.getProject());
		assertNotNull(fpRoot);
		String[] nvrs = fpRoot.getPackageNVRs(FedoraPackagerUtils
				.getVcsHandler(fpRoot).getBranchConfig());
		// expected list
		String[] expectedNvrs = new String[] {
				"eclipse-mylyn-tasks-3.5.1-3.fc15",
				"eclipse-mylyn-tasks-bugzilla-3.5.1-3.fc15",
				"eclipse-mylyn-tasks-trac-3.5.1-3.fc15",
				"eclipse-mylyn-tasks-web-3.5.1-3.fc15" };
		assertEquals(4, nvrs.length);
		for (int i = 1; i < expectedNvrs.length; i++) {
			assertEquals(expectedNvrs[i], nvrs[i]);
		}
	}

}

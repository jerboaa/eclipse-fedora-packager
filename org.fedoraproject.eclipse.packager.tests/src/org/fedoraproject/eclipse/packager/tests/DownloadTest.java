package org.fedoraproject.eclipse.packager.tests;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.cvs.DownloadHandler;

public class DownloadTest extends AbstractTest {
	@Override
	protected void setUp() throws Exception {
		System.out.println("SETUP1 START");
		super.setUp();
		System.out.println("SETUP1 END");
		
		System.out.println("SETUP2 START");
		handler = new DownloadHandler();
		handler.setDebug(true);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();
		System.out.println("SETUP2 END");
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println("TEARDOWN START");
		super.tearDown();
		System.out.println("TEARDOWN END");
	}

	public void testNoChange() throws Exception {
		System.out.println("TESTNOCHANGE START");
		handler.execute(null);
		assertTrue(handler.waitForJob().isOK());
		assertNotNull(branch.findMember("ed-0.8.tar.bz2"));
		System.out.println("TESTNOCHANGE END");
	}

	public void testNeedDownload() throws Exception {
		System.out.println("TESTNEEDDOWNLOAD START");
		assertTrue(handler.waitForJob().isOK());
		assertNotNull(branch.findMember("ed-0.8.tar.bz2"));
		System.out.println("TESTNEEDDOWNLOAD END");
	}
	
	public void testMD5Mismatch() throws Exception {
		System.out.println("TESTMD5MISMATCH START");
		assertTrue(handler.waitForJob().isOK());
		IFile source = (IFile) branch.findMember("ed-0.8.tar.bz2");
		FileWriter out = new FileWriter(source.getLocation().toFile());
		out.write(0x90);
		out.close();
		handler.execute(null);
		assertTrue(handler.waitForJob().isOK());
		assertNotNull(branch.findMember("ed-0.8.tar.bz2"));
		System.out.println("TESTMD5MISMATCH END");
	}
	
	public void testBadMD5() throws Exception {
		System.out.println("TESTBADMD5 START");
		IFile source = (IFile) branch.findMember("sources");
		PrintWriter out = new PrintWriter(new FileWriter(source.getLocation().toFile()));
		// Incorrect MD5sum
		out.println("a359451fb32097974484b5ba7c19f5fb  ed-0.8.tar.bz2");
		out.close();
		handler.execute(null);
		assertFalse(handler.waitForJob().isOK());
		assertNull(branch.findMember("ed-0.8.tar.bz2"));
		System.out.println("TESTBADMD5 END");
	}
}

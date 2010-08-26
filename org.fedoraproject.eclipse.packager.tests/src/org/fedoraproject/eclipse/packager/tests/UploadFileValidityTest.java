package org.fedoraproject.eclipse.packager.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

import junit.framework.TestCase;

public class UploadFileValidityTest extends TestCase {

	private File emptyFileRegular;
	private File emptyFileInvalid;
	private File regularFileNonEmpty;
	private File regularFileNonEmptyInvalid;
	
	@Override
	public void setUp() {
		try {
			// Create files
			emptyFileRegular = File.createTempFile("eclipse-fed-packager-test", ".rpm");
			emptyFileInvalid = File.createTempFile("eclipse-fed-packager-test", ".exe");
			regularFileNonEmpty = File.createTempFile("eclipse-fed-packager-test", ".cpio");
			regularFileNonEmptyInvalid = File.createTempFile("eclipse-fed-packager-test", ".invalid");
			
			// Make it empty
			BufferedWriter out = new BufferedWriter(new FileWriter(emptyFileRegular));
			out.write("");
			out.close();
			// Make it empty, again
			out = new BufferedWriter(new FileWriter(emptyFileInvalid));
			out.write("");
			out.close();
			// Put text into file
			out = new BufferedWriter(new FileWriter(regularFileNonEmpty));
			out.write("I'm not empty!");
			out.close();
			// write something into file
			out = new BufferedWriter(new FileWriter(regularFileNonEmptyInvalid));
			out.write("I'm not empty!");
			out.close();
		} catch (IOException e) { }
	}
	
	@Override
	public void tearDown() {
		// tear down the house
		if (emptyFileRegular.exists()) {
			emptyFileRegular.delete();
		}
		if (emptyFileInvalid.exists()) {
			emptyFileInvalid.delete();
		}
		if (regularFileNonEmpty.exists()) {
			regularFileNonEmpty.delete();
		}
		if (regularFileNonEmptyInvalid.exists()) {
			regularFileNonEmptyInvalid.delete();
		}
	}
	
	/**
	 * Test FedoraHandlerUtils.isValidUploadFile().
	 */
	public void testIsValidUploadFile() {
		assertFalse(FedoraHandlerUtils.isValidUploadFile(emptyFileRegular));
		assertFalse(FedoraHandlerUtils.isValidUploadFile(emptyFileInvalid));
		assertTrue(FedoraHandlerUtils.isValidUploadFile(regularFileNonEmpty));
		assertFalse(FedoraHandlerUtils.isValidUploadFile(regularFileNonEmptyInvalid));
	}

}

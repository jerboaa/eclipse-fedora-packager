package org.fedoraproject.eclipse.packager.tests;

import static org.junit.Assert.*;

import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.junit.Test;

public class FedoraPackagerLoggerTest {

	@Test
	public void shouldBeASingleton() {
		FedoraPackagerLogger log = FedoraPackagerLogger.getInstance();
		assertNotNull(log);
		// should return the very same reference
		assertTrue(log == FedoraPackagerLogger.getInstance());
	}

}

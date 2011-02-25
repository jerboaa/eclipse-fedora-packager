package org.fedoraproject.eclipse.packager.tests.units;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	FedoraSSLTest.class,
	KojiHubClientTest.class,
	UploadFileValidityTest.class
})

public class AllUnitTests {
	// empty
}

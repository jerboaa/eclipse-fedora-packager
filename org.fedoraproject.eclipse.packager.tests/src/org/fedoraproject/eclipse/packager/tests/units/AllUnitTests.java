package org.fedoraproject.eclipse.packager.tests.units;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	KojiSSLHubClientTest.class,
	UploadFileValidityTest.class,
	AssertionsEnabledTest.class,
	KojiBuildInfoTest.class,
	KojiHubClientLoginExceptionTest.class,
})

public class AllUnitTests {
	// empty
}

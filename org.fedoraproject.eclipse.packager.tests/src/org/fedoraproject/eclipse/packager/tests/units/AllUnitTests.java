package org.fedoraproject.eclipse.packager.tests.units;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	KojiHubClientTest.class,
	UploadFileValidityTest.class,
	AssertionsEnabledTest.class
})

public class AllUnitTests {
	// empty
}

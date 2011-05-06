package org.fedoraproject.eclipse.packager.tests.commands;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	FedoraPackagerCommandTest.class,
	DownloadSourceCommandTest.class,
	UploadSourceCommandTest.class,
	KojiBuildCommandTest.class
})

public class AllCommandsTests {
	// empty
}

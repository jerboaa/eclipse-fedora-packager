package org.fedoraproject.eclipse.packager.tests;

import org.fedoraproject.eclipse.packager.tests.commands.AllCommandsTests;
import org.fedoraproject.eclipse.packager.tests.units.AllUnitTests;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	// Unit tests
	AllUnitTests.class,
	// Command tests
	AllCommandsTests.class,
	// Auxiliary plug-in tests
	CVSProjectTypeTest.class,
	GitFpBitsTest.class,
	GitProjectTypeTest.class,
	SourcesFileUpdaterTest.class,
	VCSIgnoreFileUpdaterTest.class
})

public class AllTests {
	// empty
}

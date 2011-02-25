package org.fedoraproject.eclipse.packager.ui.tests;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AddSourcesSWTBotTest.class,
	DistGitImportSWTBotTest.class,
	BodhiUpdateSWTBotTest.class,
	DownloadSourcesSWTBotTest.class,
	KojiBuildSWTBotTest.class,
	LocalBuildSWTBotTest.class,
	MockSWTBotTest.class,
	CreateSRPMSWTBotTest.class,
	PrepSourcesSWTBotTest.class,
	ReplaceSourcesSWTBotTest.class}
)

public class AllSWTBotTests {

}

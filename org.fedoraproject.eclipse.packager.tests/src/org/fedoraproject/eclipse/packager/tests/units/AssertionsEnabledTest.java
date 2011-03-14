package org.fedoraproject.eclipse.packager.tests.units;

import static org.junit.Assert.fail;

import org.junit.Test;

public class AssertionsEnabledTest {

	@Test
	public void assertionsEnabledForTests() {
		String nullString = null;
		try {
			assert nullString != null;
		} catch (AssertionError e) {
			// pass
			return;
		}
		fail("Please enable assertions for tests!" +
				" I.e. add the '-ea' VM switch.");
	}
}

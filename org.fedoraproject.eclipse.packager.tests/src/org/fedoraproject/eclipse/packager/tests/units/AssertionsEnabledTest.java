/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
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

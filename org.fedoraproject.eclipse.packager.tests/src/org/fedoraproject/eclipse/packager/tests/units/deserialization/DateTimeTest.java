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
package org.fedoraproject.eclipse.packager.tests.units.deserialization;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.fedoraproject.eclipse.packager.bodhi.fas.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests parsing of JSON serialized date/times.
 *
 */
public class DateTimeTest {

	private DateTime dateTime;
	
	@Before
	public void setUp() throws Exception {
		dateTime = new DateTime("2010-06-17 15:42:05.553330+00:00");
	}

	@Test
	public void testGetTime() {
		assertTrue(dateTime.getTime().after(Timestamp.valueOf("2010-06-16 15:42:05")));
	}
	
	@Test
	public void testGetTimeZone() {
		assertNotNull(dateTime.getTimeZone().getID());
		assertEquals("GMT", dateTime.getTimeZone().getID());
	}

}

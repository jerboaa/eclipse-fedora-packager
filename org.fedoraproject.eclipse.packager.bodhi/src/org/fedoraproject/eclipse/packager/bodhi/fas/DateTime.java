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
package org.fedoraproject.eclipse.packager.bodhi.fas;

import java.sql.Timestamp;
import java.util.TimeZone;

/**
 * Date time class used for JSON deserialization.
 *
 */
public class DateTime {
	
	private Timestamp time;
	private String originalString;
	private TimeZone timezone;
	
	/**
	 * @param serializedJSON A the JSON string to parse from.
	 */
	public DateTime(String serializedJSON) {
		this.originalString = serializedJSON;
		// String looks like: 2010-06-17 15:42:05.553330+00:00
		parseFromJson(serializedJSON);
	}

	/**
	 * Set the values from the String.
	 * 
	 * @param serializedJSON
	 */
	private void parseFromJson(String serializedJSON) {
		int plusIndex = serializedJSON.indexOf("+");
		String time = serializedJSON.substring(0, plusIndex);
		String timeShift = serializedJSON.substring(plusIndex + 1);
		timezone = TimeZone.getTimeZone("UTC+"  + timeShift);
		this.time = Timestamp.valueOf(time);
	}

	/**
	 * @return the originalString
	 */
	public String getOriginalString() {
		return originalString;
	}

	/**
	 * @return the time
	 */
	public Timestamp getTime() {
		return time;
	}
	
	/**
	 * @return the time zone.
	 */
	public TimeZone getTimeZone() {
		return this.timezone;
	}
}

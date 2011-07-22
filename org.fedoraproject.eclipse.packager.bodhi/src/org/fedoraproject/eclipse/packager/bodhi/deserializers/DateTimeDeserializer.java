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
package org.fedoraproject.eclipse.packager.bodhi.deserializers;

import java.lang.reflect.Type;

import org.fedoraproject.eclipse.packager.bodhi.fas.DateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom JSON deserializer for Date/Time values, such as:
 * {@code 2010-06-17 15:42:05.553330+00:00}
 *
 */
public class DateTimeDeserializer implements JsonDeserializer<DateTime> {
	@Override
	public DateTime deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		return new DateTime(json.getAsJsonPrimitive().getAsString());
	}
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.fedoraproject.eclipse.packager.bodhi.api.BodhiUpdateResponse;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BodhiUpdateResponseTest {

	private static final String EXAMPLE_UPDATE_JSON_RESPONSE =
			"{"+
					" \"tg_flash\": \"Update successfully created\","+
					" \"updates\":"+
					"    [ "+
					"       { \"status\": \"pending\","+
					"         \"close_bugs\": false,"+
					"	 \"request\": \"testing\","+
					"	 \"date_submitted\": \"2011-07-22 19:06:59\","+
					"	 \"unstable_karma\": -3,"+
					"	 \"submitter\": \"guest\","+
					"	 \"critpath\": false,"+
					"	 \"approved\": null,"+
					"	 \"stable_karma\": 3,"+
					"	 \"date_pushed\": null,"+
					"	 \"builds\":"+
					"	    ["+
					"	      { \"nvr\": \"ed-1.5-3.fc15\","+
					"	        \"package\":"+
					"		  { \"suggest_reboot\": false,"+
					"		    \"committers\": [\"guest\"],"+
					"		    \"name\": \"ed\""+
					"		  }"+
					"              }"+
					"	    ],"+
					"	 \"title\": \"ed-1.5-3.fc15\","+
					"	 \"notes\": \"This is a test. Please disregard\","+
					"	 \"date_modified\": null,"+
					"	 \"nagged\": null,"+
					"	 \"bugs\": [],"+
					"	 \"comments\": "+
					"	    [ {"+
					"	        \"group\": null,"+
					"		\"karma\": 0,"+
					"		\"anonymous\": false,"+
					"		\"author\": \"bodhi\","+
					"		\"timestamp\": \"2011-07-22 19:07:02\","+
					"		\"text\": \"This update has been submitted for testing by guest.\""+
					"	      } ],"+
					"	 \"critpath_approved\": false,"+
					"	 \"updateid\": null,"+
					"	 \"karma\": 0,"+
					"	 \"release\":"+
					"	   { "+
					"	     \"dist_tag\": \"dist-f15\","+
					"	     \"id_prefix\": \"FEDORA\","+
					"	     \"locked\": true,"+
					"	     \"name\": \"F15\","+
					"	     \"long_name\": \"Fedora 15\""+
					"	   },"+
					"	 \"type\": \"enhancement\""+
					"       }"+
					"   ]"+
					"}";
	
	@Test
	public void canDeserializeUpdateJSONResponse() throws Exception {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		BodhiUpdateResponse result = gson.fromJson(EXAMPLE_UPDATE_JSON_RESPONSE, BodhiUpdateResponse.class);
		assertNotNull(result);
		assertEquals("Update successfully created", result.getFlashMsg());
		assertEquals("ed", result.getUpdates()[0].getBuilds()[0].getPkg().getName());
	}
}

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

import org.fedoraproject.eclipse.packager.bodhi.api.BodhiLoginResponse;
import org.fedoraproject.eclipse.packager.bodhi.fas.DateTime;
import org.fedoraproject.eclipse.packager.bodhi.deserializers.DateTimeDeserializer;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BodhiLoginResponseTest {

	private static final String EXAMPLE_LOGIN_JSON_RESPONSE =
			"{"+
					""+
					" \"tg_flash\": \"Welcome, ...\","+
					" \"_csrf_token\": \"14f7a7ee9cdb692691cd34372921042e7a77e8c4\","+
					" \"user\":"+
					" {"+
					"	\"status\": \"active\","+
					"	\"certificate_serial\": 1,"+
					"	\"locale\": \"en\","+
					"	\"creation\": \"2010-06-17 15:42:05.553330+00:00\","+
					"	\"telephone\": \"131212\","+
					"	\"affiliation\": null,"+
					"	\"country_code\": \"CA\","+
					"	\"timezone\": \"UTC\","+
					"	\"status_change\": \"2010-06-17 15:42:05.553330+00:00\","+
					"	\"id\": 2,"+
					"	\"password_changed\": \"2010-06-17 15:43:37.431477+00:00\","+
					"	\"privacy\": true,"+
					"	\"alias_enabled\": null,"+
					"	\"comments\": \"\","+
					"	\"latitude\": null,"+
					"	\"email\": \"anonymous@example.com\","+
					"	\"username\": \"anonymous\","+
					"	\"gpg_keyid\": \"\","+
					"	\"internal_comments\": null,"+
					"	\"postal_address\": \"\","+
					"	\"unverified_email\": \"\","+
					"	\"ssh_key\": \"\","+
					"	\"approved_memberships\": "+
					"	[ "+
					"		{\"display_name\": \"Fedora CLA Group\","+
					"			\"name\": \"cla_fedora\","+
					"			\"url\": \"\","+
					"			\"creation\": \"2008-03-12 02:00:57.941258+00:00\","+
					"			\"irc_network\": \"\","+
					"			\"needs_sponsor\": false,"+
					"			\"irc_channel\": \"\","+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"\","+
					"			\"group_type\": \"cla\","+
					"			\"invite_only\": true,"+
					"			\"id\": 101441,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": null,"+
					"			\"mailing_list\": \"\","+
					"			\"owner_id\": 100001 },"+
					"		{\"display_name\": \"Signed CLA Group\","+
					"			\"name\": \"cla_done\","+
					"			\"url\": \"\","+
					"			\"creation\": \"2008-03-12 01:58:48.745240+00:00\","+
					"			\"irc_network\": \"\","+
					"			\"needs_sponsor\": false,"+
					"			\"irc_channel\": \"\","+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"\","+
					"			\"group_type\": \"cla\","+
					"			\"invite_only\": true,"+
					"			\"id\": 100002,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": null,"+
					"			\"mailing_list\": \"\","+
					"			\"owner_id\": 100001},"+
					"		{\"display_name\": \"Red Hat Employees CLA Group\","+
					"			\"name\": \"cla_redhat\","+
					"			\"url\": \"\","+
					"			\"creation\": \"2008-03-12 02:00:57.985941+00:00\","+
					"			\"irc_network\": \"\","+
					"			\"needs_sponsor\": false,"+
					"			\"irc_channel\": \"\","+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"\","+
					"			\"group_type\": \"cla\","+
					"			\"invite_only\": true,"+
					"			\"id\": 101440,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": null,"+
					"			\"mailing_list\": \"\","+
					"			\"owner_id\": 100001},"+
					"		{\"display_name\": \"Fedora Packager GIT Commit Group\","+
					"			\"name\": \"packager\","+
					"			\"url\": \"https://fedoraproject.org/wiki/PackageMaintainers\","+
					"			\"creation\": \"2008-03-12 02:00:58.145046+00:00\","+
					"			\"irc_network\": \"irc.freenode.net\","+
					"			\"needs_sponsor\": true,"+
					"			\"irc_channel\": \"#fedora-devel\","+
					"			\"apply_rules\": \"Please do not apply to this group.  Your sponsor will add you after you've completed http://fedoraproject.org/wiki/Join_the_package_collection_maintainers.\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"https://lists.fedoraproject.org/mailman/listinfo/devel\","+
					"			\"group_type\": \"cvs\","+
					"			\"invite_only\": true,"+
					"			\"id\": 100300,"+
					"			\"joinmsg\": \"Welcome to the Fedora packager group. Please continue the process from: https://fedoraproject.org/wiki/PackageMaintainers/Join#Add_Package_to_Source_Code_Management_.28SCM.29_system_and_Set_Owner\","+
					"			\"prerequisite_id\": 100002,"+
					"			\"mailing_list\": \"devel@lists.fedoraproject.org\","+
					"			\"owner_id\": 100001},"+
					"		{\"display_name\": \"Fedora Bugs Group\","+
					"			\"name\": \"fedorabugs\","+
					"			\"url\": \"https://fedoraproject.org/wiki/BugZappers\","+
					"			\"creation\": \"2008-03-12 01:58:48.763123+00:00\","+
					"			\"irc_network\": \"irc.freenode.net\","+
					"			\"needs_sponsor\": false,"+
					"			\"irc_channel\": \"fedora-bugzappers\","+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"https://www.redhat.com/mailman/listinfo/fedora-test-list\","+
					"			\"group_type\": \"tracking\","+
					"			\"invite_only\": true,"+
					"			\"id\": 100148,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": null,"+
					"			\"mailing_list\": \"fedora-test-list@redhat.com\","+
					"			\"owner_id\": 100001},"+
					"		{\"display_name\": \"Eclipse Fedorapackager\","+
					"			\"name\": \"giteclipse-fedorapackager\","+
					"			\"url\": null,"+
					"			\"creation\": \"2009-12-06 16:17:28.973638+00:00\","+
					"			\"irc_network\": null,"+
					"			\"needs_sponsor\": true,"+
					"			\"irc_channel\": null,"+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": null,"+
					"			\"group_type\": \"git\","+
					"			\"invite_only\": false,"+
					"			\"id\": 141675,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": 100002,"+
					"			\"mailing_list\": null,"+
					"			\"owner_id\": 100615}"+
					"	],"+
					"	\"unapproved_memberships\": [],"+
					"	\"passwordtoken\": null,"+
					"	\"ircnick\": \"anonymous\","+
					"	\"password\": \"...\","+
					"	\"group_roles\":"+
					"		{"+
					"		\"fedorabugs\":"+
					"			{\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-23 18:36:52.470144+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 100615,"+
					"			\"approval\": \"2010-06-23 18:36:52.563303+00:00\","+
					"			\"group_id\": 100148,"+
					"			\"role_type\": \"user\"},"+
					"		\"cla_done\": {\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-17 15:46:18.031406+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 148899,"+
					"			\"approval\": \"2010-06-17 15:46:18.139294+00:00\","+
					"			\"group_id\": 100002,"+
					"			\"role_type\": \"user\"},"+
					"		\"giteclipse-fedorapackager\": {\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-29 20:11:05.466351+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 100615,"+
					"			\"approval\": \"2010-06-29 20:40:02.021650+00:00\","+
					"			\"group_id\": 141675,"+
					"			\"role_type\": \"user\"},"+
					"		\"packager\": {\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-23 15:56:24.872873+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 100615,"+
					"			\"approval\": \"2010-06-23 18:36:52.549348+00:00\","+
					"			\"group_id\": 100300,"+
					"			\"role_type\": \"user\"},"+
					"		\"cla_fedora\": {\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-17 15:46:18.031406+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 148899,"+
					"			\"approval\": \"2010-06-17 15:46:18.125494+00:00\","+
					"			\"group_id\": 101441,"+
					"			\"role_type\": \"user\"}"+
					"		},"+
					"	\"emailtoken\": null,"+
					"	\"roles\": ["+
					"		{"+
					"			\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-17 15:46:18.031406+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 148899,"+
					"			\"approval\": \"2010-06-17 15:46:18.125494+00:00\","+
					"			\"group_id\": 101441,"+
					"			\"role_type\": \"user\" },"+
					"		{"+
					"			\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-17 15:46:18.031406+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 148899,"+
					"			\"approval\": \"2010-06-17 15:46:18.139294+00:00\","+
					"			\"group_id\": 100002,"+
					"			\"role_type\": \"user\" },"+
					"		{"+
					"			\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-21 20:19:11.599401+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 100061,"+
					"			\"approval\": \"2010-06-21 20:59:57.588493+00:00\","+
					"			\"group_id\": 101440,"+
					"			\"role_type\": \"user\"},"+
					"		{"+
					"			\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-23 15:56:24.872873+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 100615,"+
					"			\"approval\": \"2010-06-23 18:36:52.549348+00:00\","+
					"			\"group_id\": 100300,"+
					"			\"role_type\": \"user\"},"+
					"		{"+
					"			\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-23 18:36:52.470144+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 100615,"+
					"			\"approval\": \"2010-06-23 18:36:52.563303+00:00\","+
					"			\"group_id\": 100148,"+
					"			\"role_type\": \"user\"},"+
					"		{"+
					"			\"internal_comments\": null,"+
					"			\"role_status\": \"approved\","+
					"			\"creation\": \"2010-06-29 20:11:05.466351+00:00\","+
					"			\"person_id\": 148899,"+
					"			\"sponsor_id\": 100615,"+
					"			\"approval\": \"2010-06-29 20:40:02.021650+00:00\","+
					"			\"group_id\": 141675,"+
					"			\"role_type\": \"user\"}"+
					"	],"+
					"	\"longitude\": null,"+
					"	\"memberships\":"+
					"	["+
					"		{"+
					"			\"display_name\": \"Fedora CLA Group\","+
					"			\"name\": \"cla_fedora\","+
					"			\"url\": \"\","+
					"			\"creation\": \"2008-03-12 02:00:57.941258+00:00\","+
					"			\"irc_network\": \"\","+
					"			\"needs_sponsor\": false,"+
					"			\"irc_channel\": \"\","+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"\","+
					"			\"group_type\": \"cla\","+
					"			\"invite_only\": true,"+
					"			\"id\": 101441,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": null,"+
					"			\"mailing_list\": \"\","+
					"			\"owner_id\": 100001},"+
					"		{"+
					"			\"display_name\": \"Signed CLA Group\","+
					"			\"name\": \"cla_done\","+
					"			\"url\": \"\","+
					"			\"creation\": \"2008-03-12 01:58:48.745240+00:00\","+
					"			\"irc_network\": \"\","+
					"			\"needs_sponsor\": false,"+
					"			\"irc_channel\": \"\","+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"\","+
					"			\"group_type\": \"cla\","+
					"			\"invite_only\": true,"+
					"			\"id\": 100002,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": null,"+
					"			\"mailing_list\": \"\","+
					"			\"owner_id\": 100001},"+
					"		{"+
					"			\"display_name\": \"Fedora Packager GIT Commit Group\","+
					"			\"name\": \"packager\","+
					"			\"url\": \"https://fedoraproject.org/wiki/PackageMaintainers\","+
					"			\"creation\": \"2008-03-12 02:00:58.145046+00:00\","+
					"			\"irc_network\": \"irc.freenode.net\","+
					"			\"needs_sponsor\": true,"+
					"			\"irc_channel\": \"#fedora-devel\","+
					"			\"apply_rules\": \"Please do not apply to this group.  Your sponsor will add you after you've completed http://fedoraproject.org/wiki/Join_the_package_collection_maintainers.\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"https://lists.fedoraproject.org/mailman/listinfo/devel\","+
					"			\"group_type\": \"cvs\","+
					"			\"invite_only\": true,"+
					"			\"id\": 100300,"+
					"			\"joinmsg\": \"Welcome to the Fedora packager group. Please continue the process from: https://fedoraproject.org/wiki/PackageMaintainers/Join#Add_Package_to_Source_Code_Management_.28SCM.29_system_and_Set_Owner\","+
					"			\"prerequisite_id\": 100002,"+
					"			\"mailing_list\": \"devel@lists.fedoraproject.org\","+
					"			\"owner_id\": 100001},"+
					"		{"+
					"			\"display_name\": \"Fedora Bugs Group\","+
					"			\"name\": \"fedorabugs\","+
					"			\"url\": \"https://fedoraproject.org/wiki/BugZappers\","+
					"			\"creation\": \"2008-03-12 01:58:48.763123+00:00\","+
					"			\"irc_network\": \"irc.freenode.net\","+
					"			\"needs_sponsor\": false,"+
					"			\"irc_channel\": \"fedora-bugzappers\","+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": \"https://www.redhat.com/mailman/listinfo/fedora-test-list\","+
					"			\"group_type\": \"tracking\","+
					"			\"invite_only\": true,"+
					"			\"id\": 100148,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": null,"+
					"			\"mailing_list\": \"fedora-test-list@redhat.com\","+
					"			\"owner_id\": 100001},"+
					"		{"+
					"			\"display_name\": \"Eclipse Fedorapackager\","+
					"			\"name\": \"giteclipse-fedorapackager\","+
					"			\"url\": null,"+
					"			\"creation\": \"2009-12-06 16:17:28.973638+00:00\","+
					"			\"irc_network\": null,"+
					"			\"needs_sponsor\": true,"+
					"			\"irc_channel\": null,"+
					"			\"apply_rules\": \"\","+
					"			\"user_can_remove\": true,"+
					"			\"mailing_list_url\": null,"+
					"			\"group_type\": \"git\","+
					"			\"invite_only\": false,"+
					"			\"id\": 141675,"+
					"			\"joinmsg\": \"\","+
					"			\"prerequisite_id\": 100002,"+
					"			\"mailing_list\": null,"+
					"			\"owner_id\": 100615}"+
					"	],"+
					"	\"facsimile\": null,"+
					"	\"human_name\": \"Harry Holle\","+
					"	\"old_password\": \"...\","+
					"	\"last_seen\": \"2011-05-19 13:20:13.015314+00:00\""+
					"	}"+
					"}";
	
	@Test
	public void canDeserializeLoginJSONResponse() throws Exception {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
		Gson gson = gsonBuilder.create();
		BodhiLoginResponse result = gson.fromJson(EXAMPLE_LOGIN_JSON_RESPONSE, BodhiLoginResponse.class);
		assertNotNull(result);
		assertEquals("anonymous", result.getUser().getUsername());
	}
}

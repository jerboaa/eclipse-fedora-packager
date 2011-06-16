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

import com.google.gson.JsonObject;

/**
 * FAS user class. Used for JSON deserialization. 
 *
 */
public class FASUser {
	
	private String status;
	private int certificate_serial;
	private String locale;
	private DateTime creation;
	private String telephone;
	private String affiliation;
	private String country_code;
	private String timezone;
	private DateTime status_change;
	private int id;
	private DateTime password_changed;
	// privacy may be boolean
	private String privacy;
	private String alias_enabled;
	private String comments;
	private String latitude;
	private String email;
	private String username;
	private String gpg_keyid;
	private String internal_comments;
	private String postal_address;
	private String unverified_email;
	private String ssh_key;
	// FIXME: Model this as FASMembership objects
	private JsonObject[] approved_memberships;
	// FIXME: Model this as FASMembership objects
	private JsonObject[] unapproved_memberships;
	private String passwordtoken;
	private String ircnick;
	private String password;
	// FIXME: Model this as FASRole objects
	private JsonObject group_roles;
	private String emailtoken;
	// FIXME: Model this as FASRole objects
	private JsonObject[] roles;
	private String longitude;
	// FIXME: Model this as FASMembership objects
	private JsonObject[] memberships;
	private String facsimile;
	private String human_name;
	private String old_password;
	private DateTime last_seen;
	
	/**
	 *  no-args constructor used by GSON
	 */
	public FASUser() {
		// empty
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the certificate_serial
	 */
	public int getCertificateSerial() {
		return certificate_serial;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @return the creation
	 */
	public DateTime getCreation() {
		return creation;
	}

	/**
	 * @return the telephone
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * @return the affiliation
	 */
	public String getAffiliation() {
		return affiliation;
	}

	/**
	 * @return the country_code
	 */
	public String getCountryCode() {
		return country_code;
	}

	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return timezone;
	}

	/**
	 * @return the status_change
	 */
	public DateTime getStatusChange() {
		return status_change;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the password_changed
	 */
	public DateTime getPasswordChanged() {
		return password_changed;
	}

	/**
	 * @return the privacy
	 */
	public String getPrivacy() {
		return privacy;
	}

	/**
	 * @return the alias_enabled
	 */
	public String getAliasEnabled() {
		return alias_enabled;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the gpg_keyid
	 */
	public String getGpgKeyId() {
		return gpg_keyid;
	}

	/**
	 * @return the internal_comments
	 */
	public String getInternalComments() {
		return internal_comments;
	}

	/**
	 * @return the postal_address
	 */
	public String getPostalAddress() {
		return postal_address;
	}

	/**
	 * @return the unverified_email
	 */
	public String getUnverifiedEmail() {
		return unverified_email;
	}

	/**
	 * @return the ssh_key
	 */
	public String getSshKey() {
		return ssh_key;
	}

	/**
	 * @return the approved_memberships
	 */
	public JsonObject[] getApprovedMemberships() {
		return approved_memberships;
	}

	/**
	 * @return the unapproved_memberships
	 */
	public JsonObject[] getUnapprovedMemberships() {
		return unapproved_memberships;
	}

	/**
	 * @return the passwordtoken
	 */
	public String getPasswordToken() {
		return passwordtoken;
	}

	/**
	 * @return the ircnick
	 */
	public String getIrcNick() {
		return ircnick;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the group_roles
	 */
	public JsonObject getGroupRoles() {
		return group_roles;
	}

	/**
	 * @return the emailtoken
	 */
	public String getEmailToken() {
		return emailtoken;
	}

	/**
	 * @return the roles
	 */
	public JsonObject[] getRoles() {
		return roles;
	}

	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}

	/**
	 * @return the memberships
	 */
	public JsonObject[] getMemberships() {
		return memberships;
	}

	/**
	 * @return the facsimile
	 */
	public String getFacsimile() {
		return facsimile;
	}

	/**
	 * @return the human_name
	 */
	public String getHumanName() {
		return human_name;
	}

	/**
	 * @return the old_password
	 */
	public String getOldPassword() {
		return old_password;
	}

	/**
	 * @return the last_seen
	 */
	public DateTime getLastSeen() {
		return last_seen;
	}
}

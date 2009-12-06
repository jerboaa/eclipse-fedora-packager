package org.fedoraproject.eclipse.packager.bodhi;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

public interface IBodhiClient {

	public abstract JSONObject login(String username, String password)
			throws IOException, HttpException, ParseException, JSONException;

	public abstract JSONObject newUpdate(String buildName, String release,
			String type, String request, String bugs, String notes)
			throws IOException, HttpException, ParseException, JSONException;

	public abstract void logout() throws IOException, HttpException, ParseException;

}
package org.fedoraproject.eclipse.packager.bodhi.api.deserialization;

import com.google.gson.annotations.SerializedName;

/**
 * Class which represents a Koji build. Deserialized from
 * a JSON String by google.gson.
 */
public class Build {
	
	private String nvr;
	// serialized name in the JSON string is "package", which
	// is not a valid Java identifier.
	@SerializedName("package") private FedoraPackage pkg;
	
	/**
	 * Google GSON requires this
	 */
	public Build() {
		// nothing
	}
	
	/**
	 * @return the nvr
	 */
	public String getNvr() {
		return nvr;
	}
	/**
	 * @return the pkg
	 */
	public FedoraPackage getPkg() {
		return pkg;
	}
	
}

package org.fedoraproject.eclipse.packager;

/**
 * A disposable configuration for a single operation on a branch.
 * 
 */
public class BranchConfigInstance {
	private String dist;
	private String distVal;
	private String distVariable;
	private String buildTarget;
	private String equivalentBranch;

	/**
	 * @param dist
	 * @param distVal
	 * @param distVariable
	 * @param buildTarget
	 * @param equivalentBranch
	 */
	public BranchConfigInstance(String dist, String distVal,
			String distVariable, String buildTarget, String equivalentBranch) {
		this.dist = dist;
		this.distVal = distVal;
		this.distVariable = distVariable;
		this.buildTarget = buildTarget;
		this.equivalentBranch = equivalentBranch;
	}

	/**
	 * @return The build target.
	 */
	public String getBuildTarget() {
		return buildTarget;
	}

	/**
	 * @param buildTarget
	 */
	public void setBuildTarget(String buildTarget) {
		this.buildTarget = buildTarget;
	}

	/**
	 * @return The dist.
	 */
	public String getDist() {
		return dist;
	}

	/**
	 * @param dist
	 */
	public void setDist(String dist) {
		this.dist = dist;
	}

	/**
	 * @return The dist-value.
	 */
	public String getDistVal() {
		return distVal;
	}

	/**
	 * @param distVal
	 */
	public void setDistVal(String distVal) {
		this.distVal = distVal;
	}

	/**
	 * @return The dist-variable.
	 */
	public String getDistVariable() {
		return distVariable;
	}

	/**
	 * @param distVariable
	 */
	public void setDistVariable(String distVariable) {
		this.distVariable = distVariable;
	}

	/**
	 * @return A mapped branch name that corresponds to this configuration,
	 *         irrespective of the actual name of the branch.
	 */
	public String getEquivalentBranch() {
		return equivalentBranch;
	}

	/**
	 * @param equivalentBranch
	 */
	public void setEquivalentBranch(String equivalentBranch) {
		this.equivalentBranch = equivalentBranch;
	}
}

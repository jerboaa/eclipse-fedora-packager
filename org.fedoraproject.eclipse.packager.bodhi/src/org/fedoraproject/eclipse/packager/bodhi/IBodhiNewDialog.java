package org.fedoraproject.eclipse.packager.bodhi;

public interface IBodhiNewDialog {

	public abstract String getBuildName();

	public abstract String getRelease();

	public abstract String getBugs();

	public abstract String getNotes();

	public abstract String getType();

	public abstract String getRequest();

	public abstract int open();

	public abstract int getReturnCode();

}
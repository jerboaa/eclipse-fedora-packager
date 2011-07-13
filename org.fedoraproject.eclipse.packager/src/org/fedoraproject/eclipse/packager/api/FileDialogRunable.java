package org.fedoraproject.eclipse.packager.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Runnable for prompting the user for a file from the file system.
 *
 */
public class FileDialogRunable implements Runnable {
	
	/**
	 * @param filter The qurey to use when filtering files.
	 * @param title The title of the file dialog.
	 */
	public FileDialogRunable(String filter, String title){
		super();
		this.filter = filter;
		this.title = title;
	}
	private String title;
	private String filter;
	Shell threadShell;
	private String srpm = null;
	private FileDialog fd;
	
	@Override
	public void run() {
		threadShell = new Shell();
		fd = new FileDialog(threadShell, SWT.OPEN);
		fd.setFilterExtensions(new String[]{filter});
		fd.setText(title);
		srpm = fd.open();
	}
	
	/**
	 * @return 
	 * 	The name of the file selected by the user. If no file has been 
	 *  selected, returns null.
	 */
	public String getFile(){
		return srpm;
	}
}

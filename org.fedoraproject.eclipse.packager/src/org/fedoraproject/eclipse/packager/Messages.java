package org.fedoraproject.eclipse.packager;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Utility class for String externalization. See messages.properties for
 * more info.
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	// ConsoleWriterThread
	public static String consoleWriterThread_ioFail;
	// DownloadJob
	public static String downloadJob_name;
	// CommonHandler Strings
	public static String commonHandler_fedoraPackagerName;
	public static String commonHandler_branchAlreadyTaggedMessage;
	// DownloadHandler Strings
	public static String downloadHandler_jobName;
	public static String downloadHandler_downloadSourceTask;
	// NewSourcesHandler Strings
	public static String newSourcesHandler_jobName;
	public static String newSourcesHandler_taskName;
	public static String newSourcesHandler_invalidFile;
	public static String newSourcesHandler_failUpdateSourceFile;
	public static String newSourcesHandler_failVCSUpdate;
	// UploadHandler Strings
	public static String uploadHandler_taskName;
	public static String uploadHandler_versionExists;
	public static String uploadHandler_invalidFile;
	public static String uploadHandler_failUpdatSourceFile;
	public static String uploadHandler_failVCSUpdate;
	public static String uploadHandler_checkingRemoteStatus;
	public static String uploadHandler_uploadFail;
	public static String uploadHandler_fileAlreadyUploaded;
	public static String uploadHandler_progressMsg;
	// WGetHandler Strings
	public static String wGetHandler_nothingToDownload;
	public static String wGetHandler_badMd5sum;
	public static String wGetHandler_couldNotCreate;
	public static String wGetHandler_couldNotRefresh;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}

package org.fedoraproject.eclipse.packager;

import org.eclipse.osgi.util.NLS;


/**
 * Translation bundle for FedoraPackager core.
 */
public class FedoraPackagerText extends NLS {

	/**
	 * Do not in-line this into the static initializer as the
	 * "Find Broken Externalized Strings" tool will not be
	 * able to find the corresponding bundle file.
	 * 
	 * This is the path to the file containing externalized strings.
	 */
	private static final String BUNDLE_NAME = "org.fedoraproject.eclipse.packager.fedorapackagertext"; //$NON-NLS-1$

	// ConsoleWriterThread
	/****/ public static String ConsoleWriterThread_ioFail;
	// DownloadHandler Strings
	/****/ public static String DownloadHandler_downloadSourceTask;
	// UploadHandler Strings
	/****/ public static String UploadHandler_taskName;
	/****/ public static String UploadHandler_versionExists;
	/****/ public static String UploadHandler_invalidFile;
	/****/ public static String UploadHandler_failUpdatSourceFile;
	/****/ public static String UploadHandler_failVCSUpdate;
	/****/ public static String UploadHandler_checkingRemoteStatus;
	/****/ public static String UploadHandler_uploadFail;
	/****/ public static String UploadHandler_fileAlreadyUploaded;
	/****/ public static String UploadHandler_progressMsg;
	/****/ public static String UploadHandler_invalidUrlError;
	// UploadCommand
	/****/ public static String UploadSourceCommand_uploadFileUnspecified;
	/****/ public static String UploadSourceCommand_uploadFileInvalid;
	/****/ public static String UploadSourceCommand_uploadingFileSubTaskName;
	// FileAvailableInLookasideCacheException
	/****/ public static String FileAvailableInLookasideCacheException_message;
	// DownloadSourceCommand
	/****/ public static String DownloadSourceCommand_nothingToDownload;
	/****/ public static String DownloadSourceCommand_downloadFile;
	/****/ public static String DownloadSourceCommand_downloadFileError;
	/****/ public static String DownloadSourceCommand_invalidURL;
	/****/ public static String DownloadSourceCommand_downloadingFileXofY;
	// SourcesFile
	/****/ public static String SourcesFile_saveFailedMsg;
	/****/ public static String SourcesFile_saveJob;
	// FedoraPackagerUtils
	/****/ public static String FedoraPackagerUtils_invalidProjectRootError;
	// ChecksumValidListener
	/****/ public static String ChecksumValidListener_badChecksum;
	// FedoraPackagerPreferencesPage
	/****/ public static String FedoraPackagerPreferencePage_lookasideUploadURLLabel;
	/****/ public static String FedoraPackagerPreferencePage_lookasideDownloadURLLabel;
	/****/ public static String FedoraPackagerPreferencePage_description;
	/****/ public static String FedoraPackagerPreferencePage_invalidDownloadURLMsg;
	/****/ public static String FedoraPackagerPreferencePage_invalidUploadURLMsg;
	/****/ public static String FedoraPackagerPreferencePage_kojiWebURLLabel;
	/****/ public static String FedoraPackagerPreferencePage_kojiHubURLLabel;
	/****/ public static String FedoraPackagerPreferencePage_kojiWebURLInvalidMsg;
	/****/ public static String FedoraPackagerPreferencePage_kojiHubURLInvalidMsg;
	/****/ public static String FedoraPackagerPreferencePage_buildSystemGroupName;
	/****/ public static String FedoraPackagerPreferencePage_lookasideGroupName;
	// Generic strings
	/****/ public static String somethingUnexpectedHappenedError;
	/****/ public static String commandWasCalledInTheWrongState;
	
	static {
		initializeMessages(BUNDLE_NAME,	FedoraPackagerText.class);
	}
}

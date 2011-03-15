package org.fedoraproject.eclipse.packager.internal.handlers;

/**
 * Class responsible for uploading source files.
 * 
 * @see UploadHandler
 */
public class NewSourcesHandler extends UploadHandler {

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.handlers.UploadHandler#shouldReplaceSources()
	 */
	@Override
	protected boolean shouldReplaceSources() {
		return true;
	}
}

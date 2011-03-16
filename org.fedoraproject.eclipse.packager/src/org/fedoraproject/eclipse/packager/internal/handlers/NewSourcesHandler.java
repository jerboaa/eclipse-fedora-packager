package org.fedoraproject.eclipse.packager.internal.handlers;

/**
 * Class responsible for uploading source files. The only difference
 * between this handler and {@link UploadHandler} is that this handler
 * will replace contents in {@code sources} files.
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

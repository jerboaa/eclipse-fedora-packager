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
package org.fedoraproject.eclipse.packager.internal.utils.httpclient;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * Requires hc-httpclient >= 4.0
 * 
 * Recall, that in hc-httpclient terminology an Entity is
 * anything that should be transported as the payload to
 * the server.
 * 
 * CountingRequestEntity is a wrapper for any entity which
 * requires upload progress reporting. Every time writing of
 * a undefined amount of bytes occurs, a ProgressListener is
 * invoked. By implementing this listener and wrapping any
 * HttpEntity with a CountingRequestentity allows you to
 * provide feedback on upload progres.
 *
 */
public class CoutingRequestEntity implements HttpEntity {

	private final HttpEntity entity;
	private final IRequestProgressListener listener;

	/**
	 * @param entity The HttpEntity which gets wrapped.
	 * @param listener The listener to notify.
	 */
	public CoutingRequestEntity(final HttpEntity entity,
			final IRequestProgressListener listener) {
		super();
		this.entity = entity;
		this.listener = listener;
	}


	@Override
	@SuppressWarnings("deprecation")
	public void consumeContent() throws IOException {
		this.entity.consumeContent();
	}

	@Override
	public long getContentLength() {
		return this.entity.getContentLength();
	}

	@Override
	public boolean isRepeatable() {
		return this.entity.isRepeatable();
	}

	@Override
	public void writeTo(final OutputStream out) throws IOException {
		this.entity.writeTo(new CountingOutputStream(out, this.listener));
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return this.entity.getContent();
	}

	@Override
	public Header getContentEncoding() {
		return this.entity.getContentEncoding();
	}

	@Override
	public Header getContentType() {
		return this.entity.getContentType();
	}

	@Override
	public boolean isChunked() {
		return this.entity.isChunked();
	}

	@Override
	public boolean isStreaming() {
		return this.entity.isStreaming();
	}

	/**
	 * Simple wrapper to allow for progress reporting when
	 * writing to the OutputStream.
	 */
	public static class CountingOutputStream extends FilterOutputStream {

		private final IRequestProgressListener listener;
		private long transferred;

		/**
		 * @param out
		 * @param listener
		 */
		public CountingOutputStream(final OutputStream out,
				final IRequestProgressListener listener) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
		}

		@Override
		public void write(final byte[] b, final int off, final int len) throws IOException {
			super.write(b, off, len);
			this.transferred += len;
			this.listener.transferred(this.transferred);
		}

		@Override
		public void write(int b) throws IOException {
			super.write(b);
			this.transferred++;
			this.listener.transferred(this.transferred);
		}

	}

}
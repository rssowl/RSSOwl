/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2009 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **                                                                          **
 **   This program and the accompanying materials are made available under   **
 **   the terms of the Eclipse Public License v1.0 which accompanies this    **
 **   distribution, and is available at:                                     **
 **   http://www.rssowl.org/legal/epl-v10.html                               **
 **                                                                          **
 **   A copy is found in the file epl-v10.html and important notices to the  **
 **   license from the team is found in the textfile LICENSE.txt distributed **
 **   in this package.                                                       **
 **                                                                          **
 **   This copyright notice MUST APPEAR in all copies of the file!           **
 **                                                                          **
 **   Contributors:                                                          **
 **     RSSOwl Development Team - initial API and implementation             **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.core.connection;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.core.runtime.IProgressMonitor;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>
 * This kind of FilterInputStream makes sure that the HTTP Method responsible
 * for the given InputStream is releasing its connection after the stream has
 * been closed. This class is also keeping two important headers to be used for
 * the Conditional GET Mechanism of HTTP.
 * </p>
 * <p>
 * Passing an instance of <code>IProgressMonitor</code> into the class allows
 * for early cancelation by throwing an Exception from the various
 * stream-methods as soon as the monitor is canceled.
 * </p>
 *
 * @author bpasero
 */
public class HttpConnectionInputStream extends FilterInputStream implements IConditionalGetCompatible, IAbortable {

  /* Response Header */
  private static final String HEADER_RESPONSE_ETAG = "ETag"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_CONTENT_LENGTH = "Content-Length"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_CONTENT_DISPOSITION = "Content-Disposition"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_CONTENT_ENCODING = "Content-Encoding"; //$NON-NLS-1$

  private final HttpRequestBase fMethod;
  private final CloseableHttpResponse fResponse;
  private final IProgressMonitor fMonitor;
  private String fIfModifiedSince;
  private String fIfNoneMatch;
  private final URI fLink;

  /**
   * Creates a <code>HttpConnectionInputStream</code> by assigning the argument
   * <code>inS</code> to the field <code>this.in</code> so as to remember it for
   * later use.
   *
   * @param link the {@link URI} that was used to create the Stream.
   * @param method The Method holding the connection of the given Stream.
   * @param response the http response
   * @param monitor A ProgressMonitor to support early cancelation, or
   * <code>NULL</code> if no monitor is being used.
   * @param inS the underlying input Stream.
   */
  public HttpConnectionInputStream(URI link, HttpRequestBase method, CloseableHttpResponse response, IProgressMonitor monitor, InputStream inS) {
    super(inS);
    fLink = link;
    fMethod = method;
    fResponse = response;
    fMonitor = monitor;

    /* Keep some important Headers */
    Header headerLastModified = response.getFirstHeader(HEADER_RESPONSE_LAST_MODIFIED);
    if (headerLastModified != null)
      setIfModifiedSince(headerLastModified.getValue());

    Header headerETag = response.getFirstHeader(HEADER_RESPONSE_ETAG);
    if (headerETag != null)
      setIfNoneMatch(headerETag.getValue());
  }

  /**
   * @return the actual {@link URI} used to open this stream.
   */
  public URI getLink() {
    try {
      return new URI(fMethod.getURI().toString());
    } catch (URISyntaxException e) {
      return fLink;
    }
  }

  /*
   * @seeorg.rssowl.core.connection.internal.http.IConditionalGetCompatible#
   * getIfModifiedSince()
   */
  @Override
  public String getIfModifiedSince() {
    return fIfModifiedSince;
  }

  /*
   * @seeorg.rssowl.core.connection.internal.http.IConditionalGetCompatible#
   * getIfNoneMatch()
   */
  @Override
  public String getIfNoneMatch() {
    return fIfNoneMatch;
  }

  /*
   * @seeorg.rssowl.core.connection.internal.http.IConditionalGetCompatible#
   * setIfModifiedSince(java.lang.String)
   */
  @Override
  public void setIfModifiedSince(String ifModifiedSince) {
    fIfModifiedSince = ifModifiedSince;
  }

  /*
   * @seeorg.rssowl.core.connection.internal.http.IConditionalGetCompatible#
   * setIfNoneMatch(java.lang.String)
   */
  @Override
  public void setIfNoneMatch(String ifNoneMatch) {
    fIfNoneMatch = ifNoneMatch;
  }

  /*
   * @see org.rssowl.core.connection.IAbortable#abort()
   */
  @Override
  public void abort() {
    fMethod.abort();
    fMethod.releaseConnection();
  }

  /*
   * @see java.io.FilterInputStream#close()
   */
  @Override
  public void close() throws IOException {
    super.close();
    fMethod.releaseConnection();
  }

  /*
   * @see java.io.FilterInputStream#read()
   */
  @Override
  public int read() throws IOException {

    /* Support early cancelation */
    if (fMonitor != null && fMonitor.isCanceled())
      throw new MonitorCanceledException(Messages.HttpConnectionInputStream_ERROR_CONNECTION_CANCELED);

    return super.read();
  }

  /*
   * @see java.io.FilterInputStream#read(byte[], int, int)
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {

    /* Support early cancelation */
    if (fMonitor != null && fMonitor.isCanceled())
      throw new MonitorCanceledException(Messages.HttpConnectionInputStream_ERROR_CONNECTION_CANCELED);

    return super.read(b, off, len);
  }

  /*
   * @see java.io.FilterInputStream#available()
   */
  @Override
  public int available() throws IOException {

    /* Support early cancelation */
    if (fMonitor != null && fMonitor.isCanceled())
      throw new MonitorCanceledException(Messages.HttpConnectionInputStream_ERROR_CONNECTION_CANCELED);

    return super.available();
  }

  /**
   * @return the content length of the content served by this stream or -1 if
   * not available.
   */
  public int getContentLength() {
    Header header = fResponse.getFirstHeader(HEADER_RESPONSE_CONTENT_LENGTH);
    if (header != null) {
      String value = header.getValue();
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return -1;
      }
    }

    return -1;
  }

  /**
   * @return the content type of the content served by this stream or null if
   * not available.
   */
  public String getContentType() {
    Header header = fResponse.getFirstHeader(HEADER_RESPONSE_CONTENT_TYPE);
    if (header != null)
      return header.getValue();

    return null;
  }

  /**
   * @return the content disposition of the content served by this stream or
   * null if not available.
   */
  public String getContentDisposition() {
    Header header = fResponse.getFirstHeader(HEADER_RESPONSE_CONTENT_DISPOSITION);
    if (header != null)
      return header.getValue();

    return null;
  }

  /**
   * @return the content encoding of the content served by this stream or
   * null if not available.
   */
  public String getContentEncoding() {
    Header header = fResponse.getFirstHeader(HEADER_RESPONSE_CONTENT_ENCODING);
    if (header != null)
      return header.getValue();

    return null;
  }
}
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.url.URLStreamHandlerService;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.util.Triple;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * This interface allows to contribute Handlers for Protocols. The application
 * is calling the contributor that is matching the URL's protocol.
 * <p>
 * Contributed via <code>org.rssowl.core.ProtocolHandler</code> Extension Point.
 * </p>
 *
 * @author bpasero
 */
public interface IProtocolHandler {

  /**
   * Reloads a <code>IFeed</code> with its News from the given <code>URL</code>
   * and returns it.
   *
   * @param link The Link to the Feed as <code>URI</code>.
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @param properties A Map of properties that can be used to transport custom
   * information
   * @return Returns the <code>IFeed</code> from the given URL including
   * {@link IConditionalGet} information and the actual {@link URI} that was
   * loaded, supporting redirects.
   * @throws CoreException In case of an Exception while loading the Feed from
   * the URL.
   * @see {@link IConnectionPropertyConstants}
   */
  Triple<IFeed, IConditionalGet, URI> reload(URI link, IProgressMonitor monitor, Map<Object, Object> properties) throws CoreException;

  /**
   * Opens a connection to the given {@link URI} and returns a
   * {@link InputStream} to read from.
   *
   * @param link the link to open a connection to.
   * @param monitor The Progress-Monitor used from the callee.
   * @param properties A Map of properties that can be used to transport custom
   * information
   * @return an {@link InputStream} for the given {@link URI}.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   * @see {@link IConnectionPropertyConstants}
   */
  InputStream openStream(URI link, IProgressMonitor monitor, Map<Object, Object> properties) throws ConnectionException;

  /**
   * Get the Feed Icon for the given Link. For instance, this could be the
   * favicon associated with the host providing the Feed. Will return
   * <code>NULL</code> if no icon can be found.
   *
   * @param link The Link to the Feed as <code>URI</code>.
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @return Returns an Icon for the given Link as byte-array or
   * <code>NULL</code> if none.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   */
  byte[] getFeedIcon(URI link, IProgressMonitor monitor) throws ConnectionException;

  /**
   * Returns a Label that can be used to present the resource identified by the
   * given <code>URI</code>. For instance, if the resource is a feed, this
   * method should return the Title of the feed.
   *
   * @param link The <code>URI</code> identifying the resource.
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @return Returns a Label that can be used to present the resource identified
   * by the given <code>URI</code>.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   */
  String getLabel(URI link, IProgressMonitor monitor) throws ConnectionException;

  /**
   * Returns the {@link URI} of the Feed that is available from the given
   * website or <code>null</code> if none. If the given {@link URI} already is a
   * feed, it is up to the implementation whether to return the same object.
   *
   * @param website the website to look for a valid feed.
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @return the {@link URI} of the Feed that is available from the given
   * website or <code>null</code> if none.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   */
  URI getFeed(URI website, IProgressMonitor monitor) throws ConnectionException;

  /**
   * <p>
   * When contributing a Protocol it is required to supply an instance of
   * URLStreamHandler in order to create instances of <code>java.net.URL</code>
   * without getting a MalformedURLException.
   * </p>
   * <p>
   * <strong>Note: </strong> This method is never called by the application. For
   * retrieving the InputStream it is always calling
   * <code>IProtocolhandler#load(Feed feed)</code>
   * </p>
   * <strong>Example of a implementation where the default Handler should not be
   * used or is not present:</strong>
   *
   * <pre>
   *   public URLStreamHandler getURLStreamHandler() throws ConnectionException {
   *     return new AbstractURLStreamHandlerService() {
   *     // Implementation of the openConnection-Method
   *     }
   *   }
   * </pre>
   *
   * Or as an alternative:
   *
   * <pre>
   *   public URLStreamHandler getURLStreamHandler() throws ConnectionException {
   *     return new URLStreamHandlerService() {
   *     // Implementation of the entire Interface
   *     }
   *   }
   * </pre>
   *
   * <strong>Example of a implementation where the default Handler should be
   * used:</strong>
   *
   * <pre>
   *   public URLStreamHandler getURLStreamHandler() throws ConnectionException {
   *     return null;
   *   }
   * </pre>
   *
   * @return The handler used by <code>java.net.URL</code> for the contributed
   * Protocol or NULL if the default Handler of the JDK shall be used. Note that
   * you have to supply a Handler in case the contributed Protocol does not have
   * any default Handler.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   */
  URLStreamHandlerService getURLStreamHandler() throws ConnectionException;
}
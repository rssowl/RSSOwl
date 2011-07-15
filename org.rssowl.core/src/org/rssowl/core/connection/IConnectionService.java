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

import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.util.Triple;

import java.net.URI;
import java.util.Map;

/**
 * Provides access to the connection service of RSSOwl. This service provides
 * API to load data from the internet (e.g. loading the contents of a feed). It
 * is also the central place to ask for credentials if a resource requires
 * authentication. Several extension points allow to customize the behavor of
 * this service, including the ability to register <code>IProtocolHandler</code>
 * to define the lookup process on per protocol basis or contributing
 * <code>ICredentialsProvider</code> to define how credentials should be stored
 * and retrieved.
 *
 * @author bpasero
 * @see IProtocolHandler
 * @see ICredentialsProvider
 */
public interface IConnectionService {

  /**
   * Notify the service about being shut down.
   */
  void shutdown();

  /**
   * Returns the responsible {@link IProtocolHandler} for the link or
   * <code>null</code> if none.
   *
   * @param link the link to find a responsible {@link IProtocolHandler} for.
   * @return the responsible {@link IProtocolHandler} for the link or
   * <code>null</code> if none.
   * @throws ConnectionException In case of an Exception while loading
   * {@link IProtocolHandler}
   */
  IProtocolHandler getHandler(URI link) throws ConnectionException;

  /**
   * Reloads a <code>IFeed</code> with its News from the given <code>URL</code>
   * and returns it.
   *
   * @param link The Link to the Feed as <code>URL</code>.
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @param properties A Map of properties that can be used to transport custom
   * information
   * @return Returns the <code>IFeed</code> from the given URL including
   * {@link IConditionalGet} information and the actual {@link URI} that was
   * loaded, supporting redirects.
   * @throws CoreException In case of an Exception while loading the Feed from
   * the URL.
   * @see IConnectionPropertyConstants
   * @see UnknownProtocolException
   */
  Triple<IFeed, IConditionalGet, URI> reload(URI link, IProgressMonitor monitor, Map<Object, Object> properties) throws CoreException;

  /**
   * Returns the Feed Icon for the given Link. For instance, this could be the
   * favicon associated with the host providing the Feed.
   *
   * @param link The Link to the Feed as <code>URI</code>.
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @return Returns an Icon for the given Link as byte-array.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   * @see UnknownProtocolException
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
   * website or <code>null</code> if none.
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
   * Returns the Credentials-Provider capable of returning Credentials for
   * protected URLs and Proxy-Server.
   *
   * @param link The Link for which to retrieve the Credentials-Provider.
   * @return The Credentials-Provider.
   */
  ICredentialsProvider getCredentialsProvider(URI link);

  /**
   * Returns the contributed or default Factory for Secure Socket Connections.
   *
   * @return the contributed or default Factory for Secure Socket Connections.
   */
  SecureProtocolSocketFactory getSecureProtocolSocketFactory();

  /**
   * Return the Authentication Credentials for the given Feed or NULL if none.
   *
   * @param link The Link to check present Authentication Credentials.
   * @param realm The Realm to get credentials for or <code>null</code> if none.
   * @return the Authentication Credentials for the given Feed or NULL if none.
   * @throws CredentialsException In case of an error while retrieving
   * Credentials for the Feed.
   */
  ICredentials getAuthCredentials(URI link, String realm) throws CredentialsException;

  /**
   * Return the Proxy Credentials for the given Feed or NULL if none.
   *
   * @param link The Link to check present Proxy Credentials.
   * @return the Proxy Credentials for the given Feed or NULL if none.
   * @throws CredentialsException In case of an error while retrieving Proxy
   * Credentials.
   */
  IProxyCredentials getProxyCredentials(URI link) throws CredentialsException;
}
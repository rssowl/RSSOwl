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

import java.net.URI;

/**
 * The {@link ICredentialsProvider} interface allows to contribute a provider
 * that knows how to provide and store authentication and proxy credentials for
 * a given link.
 * <p>
 * Contributed via <code>org.rssowl.core.CredentialsProvider</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 */
public interface ICredentialsProvider {

  /**
   * Get the Credentials to be used to authenticate on the given Feed. This
   * includes stored credentials and in-memory credentials.
   *
   * @param link The Link to supply authentication Credentials for
   * @param realm The Realm to get credentials for or <code>null</code> if none.
   * @return Credentials to use or NULL in case none are to be used for the
   * Feed.
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  ICredentials getAuthCredentials(URI link, String realm) throws CredentialsException;

  /**
   * Get the Credentials to be used to authenticate on the given Feed from the
   * persisted store excluding in-memory credentials.
   *
   * @param link The Link to supply authentication Credentials for
   * @param realm The Realm to get credentials for or <code>null</code> if none.
   * @param persistedOnly if <code>true</code> will return only those
   * credentials that have been persisted and <code>false</code> to also include
   * credentials that are stored in memory.
   * @return Credentials to use or NULL in case none are to be used for the
   * Feed.
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  ICredentials getPersistedAuthCredentials(URI link, String realm) throws CredentialsException;

  /**
   * Get the Proxy-Credentials to be used to connect on the given Feed using a
   * Proxy Server.
   *
   * @param link The Link to supply proxy-authentication Credentials for
   * @return Credentials to use or NULL in case none are to be used for the
   * Feed.
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  IProxyCredentials getProxyCredentials(URI link) throws CredentialsException;

  /**
   * Set the Credentials to be used to authenticate on the given Feed.
   *
   * @param credentials The Credentials to use for the given Link
   * @param link The Link to supply authentication Credentials for
   * @param realm The Realm to set credentials for or <code>null</code> if none.
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  void setAuthCredentials(ICredentials credentials, URI link, String realm) throws CredentialsException;

  /**
   * Set the Credentials to be used to authenticate on the given Feed in memory
   * only. The credentials are lost after restart of the application.
   *
   * @param credentials The Credentials to use for the given Link
   * @param link The Link to supply authentication Credentials for
   * @param realm The Realm to set credentials for or <code>null</code> if none.
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  void setInMemoryAuthCredentials(ICredentials credentials, URI link, String realm) throws CredentialsException;

  /**
   * Set the Proxy-Credentials to be used to connect on the given Feed using a
   * Proxy Server.
   *
   * @param credentials The Credentials to use for the given Link
   * @param link The Link to supply proxy-authentication Credentials for
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  void setProxyCredentials(IProxyCredentials credentials, URI link) throws CredentialsException;

  /**
   * Deletes the credentials for the given Link from the provider.
   *
   * @param link The Link pointing to authentication Credentials
   * @param realm The Realm to delete the credentials from.
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  void deleteAuthCredentials(URI link, String realm) throws CredentialsException;

  /**
   * Deletes the proxy-credentials for the given Link from the provider.
   *
   * @param link The Link pointing to proxy-authentication Credentials
   * @throws CredentialsException Checked Exception to be used in case of any
   * Exception.
   */
  void deleteProxyCredentials(URI link) throws CredentialsException;
}
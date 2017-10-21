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

package org.rssowl.core.tests.connection;

import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.IProxyCredentials;
import org.rssowl.core.connection.PlatformCredentialsProvider;
import org.rssowl.core.util.SyncUtils;

import java.net.URI;

/**
 * Part of the Connection Plugin TestCase.
 *
 * @author bpasero
 */
public class MyCredentialsProvider extends PlatformCredentialsProvider {
  private boolean fAuthDeleted;
  private boolean fProxyDeleted;

  /*
   * @see org.rssowl.core.connection.auth.ICredentialsProvider#getAuthCredentials(java.net.URI)
   */
  @Override
  @SuppressWarnings( { "nls", "unused" })
  public ICredentials getAuthCredentials(URI link, String realm) throws CredentialsException {
    if (!fAuthDeleted && link.toString().equals("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rdf.xml")) {
      return new ICredentials() {
        @Override
        public String getUsername() {
          return "bpasero";
        }

        @Override
        public String getPassword() {
          return "admin";
        }

        @Override
        public String getDomain() {
          return "";
        }
      };
    }

    if (SyncUtils.fromGoogle(link.toString())) {
      return new ICredentials() {
        @Override
        public String getUsername() {
          return "rssowl@mailinator.com";
        }

        @Override
        public String getPassword() {
          return "rssowl.org";
        }

        @Override
        public String getDomain() {
          return "";
        }
      };
    }

    return super.getAuthCredentials(link, realm);
  }

  /*
   * @see org.rssowl.core.connection.auth.ICredentialsProvider#getProxyCredentials(java.net.URI)
   */
  @Override
  @SuppressWarnings( { "unused", "nls" })
  public IProxyCredentials getProxyCredentials(URI link) {
    if (!fProxyDeleted && link.toString().equals("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rdf.xml")) {
      return new IProxyCredentials() {
        @Override
        public String getHost() {
          return "127.0.0.1";
        }

        @Override
        public int getPort() {
          return 0;
        }

        @Override
        public String getUsername() {
          return "bpasero";
        }

        @Override
        public String getPassword() {
          return "admin";
        }

        @Override
        public String getDomain() {
          return "";
        }
      };
    }

    return null;
  }

  /*
   * @see org.rssowl.core.connection.PlatformCredentialsProvider#deleteAuthCredentials(java.net.URI,
   * java.lang.String)
   */
  @Override
  public void deleteAuthCredentials(URI link, String realm) throws CredentialsException {
    fAuthDeleted = true;
    super.deleteAuthCredentials(link, realm);
  }

  /*
   * @see org.rssowl.core.connection.auth.DefaultCredentialsProvider#deleteProxyCredentials(java.net.URI)
   */
  @Override
  public void deleteProxyCredentials(URI link) {
    fProxyDeleted = true;
    super.deleteProxyCredentials(link);
  }
}
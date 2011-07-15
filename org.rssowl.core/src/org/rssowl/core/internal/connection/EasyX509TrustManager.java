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

package org.rssowl.core.internal.connection;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * EasyX509TrustManager unlike default {@link X509TrustManager} accepts
 * self-signed certificates.
 *
 * @author <a href="mailto:adrian.sutton@ephox.com">Adrian Sutton </a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski </a>
 */
public class EasyX509TrustManager implements X509TrustManager {
  private X509TrustManager standardTrustManager = null;

  /**
   * Constructor for EasyX509TrustManager.
   *
   * @param keystore In-memory collection of keys and certificates
   * @throws NoSuchAlgorithmException In case of an error
   * @throws KeyStoreException In case of an error
   */
  public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
    super();
    TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    factory.init(keystore);
    TrustManager[] trustmanagers = factory.getTrustManagers();
    if (trustmanagers.length == 0)
      throw new NoSuchAlgorithmException("No trust manager found"); //$NON-NLS-1$

    this.standardTrustManager = (X509TrustManager) trustmanagers[0];
  }

  /*
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
   * java.lang.String)
   */
  public void checkClientTrusted(X509Certificate[] chain, String authType) {}

  /*
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
   * java.lang.String)
   */
  public void checkServerTrusted(X509Certificate[] chain, String authType) {}

  /*
   * @see X509TrustManager#getAcceptedIssuers()
   */
  public X509Certificate[] getAcceptedIssuers() {
    if (standardTrustManager != null)
      return standardTrustManager.getAcceptedIssuers();
    return new X509Certificate[] { };
  }
}
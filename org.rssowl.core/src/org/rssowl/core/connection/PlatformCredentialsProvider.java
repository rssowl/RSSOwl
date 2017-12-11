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

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.security.storage.friends.InternalExchangeUtils;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.equinox.security.storage.provider.IProviderHints;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The default implementation of the ICredentialsProvider retrieves
 * authentication Credentials from the Equinox Security Storage.
 *
 * @author bpasero
 */
@SuppressWarnings("restriction")
public class PlatformCredentialsProvider implements ICredentialsProvider {

  /* Node for feed related security preferences */
  private static final String SECURE_FEED_NODE = "rssowl/feeds"; //$NON-NLS-1$

  /* File with credentials stored */
  private static final String SECURE_STORAGE_FILE = ".credentials"; //$NON-NLS-1$

  /* ID of the Win32 dependent password provider (win32) */
  private static final String WIN_PW_PROVIDER_ID = "org.eclipse.equinox.security.WindowsPasswordProvider"; //$NON-NLS-1$

  /* ID of the RSSOwl password provider (Dialog asking for Master Password) */
  private static final String RSSOWL_PW_PROVIDER_ID = "org.rssowl.ui.RSSOwlPasswordProvider"; //$NON-NLS-1$

  /* ID of the MacOS dependent password provider */
  private static final String MACOS_PW_PROVIDER_ID = "org.eclipse.equinox.security.OSXKeystoreIntegration"; //$NON-NLS-1$

  /* Unique Key to store Usernames */
  private static final String USERNAME = "org.rssowl.core.connection.auth.Username"; //$NON-NLS-1$

  /* Unique Key to store Passwords */
  private static final String PASSWORD = "org.rssowl.core.connection.auth.Password"; //$NON-NLS-1$

  /* Unique Key to store Domains */
  private static final String DOMAIN = "org.rssowl.core.connection.auth.Domain"; //$NON-NLS-1$

  /* Separator between Domain and Username */
  private static final String DOMAIN_SEPARATOR = "\\"; //$NON-NLS-1$

  /* System Property to enable NTLM Proxy support */
  private static final String ENABLE_NTLM_PROXY = "enableNtlmProxy"; //$NON-NLS-1$

  /* Flag for NTLM Proxy Support controlled through System Property */
  private static final boolean NTLM_PROXY_ENABLED = (System.getProperty(ENABLE_NTLM_PROXY) != null);

  /* Default Realm being used to store credentials */
  private static final String REALM = ""; //$NON-NLS-1$

  /* A cache of non-protected Links (in the form Link + Realm) */
  private final Set<String> fUnprotectedLinksCache = Collections.synchronizedSet(new HashSet<String>());

  /* The In-Memory credentials store if the user chooses to not store passwords permanently */
  private final Map<String, ICredentials> fInMemoryStore = Collections.synchronizedMap(new HashMap<String, ICredentials>());

  /* Simple POJO Implementation of ICredentials */
  private static class Credentials implements ICredentials {
    private String fUsername;
    private String fPassword;
    private String fDomain;

    Credentials(String username, String password, String domain) {
      fUsername = username;
      fPassword = password;
      fDomain = domain;
    }

    @Override
    public String getUsername() {
      return fUsername;
    }

    @Override
    public String getPassword() {
      return fPassword;
    }

    @Override
    public String getDomain() {
      return fDomain;
    }
  }

  /*
   * @see
   * org.rssowl.core.connection.ICredentialsProvider#getPersistedAuthCredentials
   * (java.net.URI, java.lang.String)
   */
  @Override
  public ICredentials getPersistedAuthCredentials(URI link, String realm) throws CredentialsException {
    return internalGetAuthCredentials(link, realm, true);
  }

  /*
   * @see
   * org.rssowl.core.connection.ICredentialsProvider#getAuthCredentials(java
   * .net.URI, java.lang.String)
   */
  @Override
  public synchronized ICredentials getAuthCredentials(URI link, String realm) throws CredentialsException {
    return internalGetAuthCredentials(link, realm, false);
  }

  private synchronized ICredentials internalGetAuthCredentials(URI link, String realm, boolean persistedOnly) throws CredentialsException {

    /* Check Cache first */
    if (isUnprotected(link, realm))
      return null;

    /* Check In-Memory Store */
    if (!persistedOnly) {
      ICredentials inMemoryCredentials = fInMemoryStore.get(toCacheKey(link, realm));
      if (inMemoryCredentials != null)
        return inMemoryCredentials;
    }

    /* Retrieve Credentials */
    ICredentials authorizationInfo = getAuthorizationInfo(link, realm);

    /* Credentials Provided */
    if (authorizationInfo != null)
      return authorizationInfo;

    /* Cache as unprotected (but check memory store as necessary) */
    if (!persistedOnly || !fInMemoryStore.containsKey(toCacheKey(link, realm)))
      addUnprotected(link, realm);

    /* Credentials not provided */
    return null;
  }

  private ISecurePreferences getSecurePreferences() {
    if (!InternalOwl.IS_ECLIPSE) {
      IPreferenceScope prefs = Owl.getPreferenceService().getGlobalScope();
      boolean useOSPasswordProvider = prefs.getBoolean(DefaultPreferences.USE_OS_PASSWORD);

      /* Disable OS Password if Master Password shall be used */
      if (prefs.getBoolean(DefaultPreferences.USE_MASTER_PASSWORD))
        useOSPasswordProvider = false;

      /* Try storing credentials in profile folder */
      try {
        Activator activator = Activator.getDefault();

        /* Check if Bundle is Stopped */
        if (activator == null)
          return null;

        IPath stateLocation = activator.getStateLocation();
        stateLocation = stateLocation.append(SECURE_STORAGE_FILE);
        URL location = stateLocation.toFile().toURL();
        Map<String, String> options = null;

        /* Use OS dependent password provider if available */
        if (useOSPasswordProvider) {
          if (Platform.OS_WIN32.equals(Platform.getOS())) {
            options = new HashMap<String, String>();
            options.put(IProviderHints.REQUIRED_MODULE_ID, WIN_PW_PROVIDER_ID);
          } else if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            options = new HashMap<String, String>();
            options.put(IProviderHints.REQUIRED_MODULE_ID, MACOS_PW_PROVIDER_ID);
          }
        }

        /* Use RSSOwl password provider */
        else {
          options = new HashMap<String, String>();
          options.put(IProviderHints.REQUIRED_MODULE_ID, RSSOWL_PW_PROVIDER_ID);
        }

        return SecurePreferencesFactory.open(location, options);
      } catch (MalformedURLException e) {
        Activator.safeLogError(e.getMessage(), e);
      } catch (IllegalStateException e1) {
        Activator.safeLogError(e1.getMessage(), e1);
      } catch (IOException e2) {
        Activator.safeLogError(e2.getMessage(), e2);
      }
    }

    /* Fallback to default location */
    return SecurePreferencesFactory.getDefault();
  }

  private ICredentials getAuthorizationInfo(URI link, String realm) throws CredentialsException {
    ISecurePreferences securePreferences = getSecurePreferences();

    /* Check if Bundle is Stopped */
    if (securePreferences == null)
      return null;

    /* Return from Equinox Security Storage */
    if (securePreferences.nodeExists(SECURE_FEED_NODE)) { // Global Feed Node
      ISecurePreferences allFeedsPreferences = securePreferences.node(SECURE_FEED_NODE);
      if (allFeedsPreferences.nodeExists(EncodingUtils.encodeSlashes(link.toString()))) { // Feed Node
        ISecurePreferences feedPreferences = allFeedsPreferences.node(EncodingUtils.encodeSlashes(link.toString()));
        if (feedPreferences.nodeExists(EncodingUtils.encodeSlashes(realm != null ? realm : REALM))) { // Realm Node
          ISecurePreferences realmPreferences = feedPreferences.node(EncodingUtils.encodeSlashes(realm != null ? realm : REALM));

          try {
            String username = realmPreferences.get(USERNAME, null);
            String password = realmPreferences.get(PASSWORD, null);
            String domain = realmPreferences.get(DOMAIN, null);

            if (username != null && password != null)
              return new Credentials(username, password, domain);
          } catch (StorageException e) {
            throw new CredentialsException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
          }
        }
      }
    }

    return null;
  }

  /*
   * @see
   * org.rssowl.core.connection.auth.ICredentialsProvider#getProxyCredentials
   * (java.net.URI)
   */
  @Override
  public IProxyCredentials getProxyCredentials(URI link) {
    Activator activator = Activator.getDefault();

    /* Check if Bundle is Stopped */
    if (activator == null)
      return null;

    IProxyService proxyService = activator.getProxyService();

    /* Check if Proxy is enabled */
    if (!proxyService.isProxiesEnabled())
      return null;

    String host = URIUtils.safeGetHost(link);
    boolean isSSL = URIUtils.HTTPS_SCHEME.equals(link.getScheme());

    /* Retrieve Proxy Data */
    final IProxyData proxyData = proxyService.getProxyDataForHost(host, isSSL ? IProxyData.HTTPS_PROXY_TYPE : IProxyData.HTTP_PROXY_TYPE);
    if (proxyData != null) {

      /* Look for Domain as part of Username to support NTLM Proxy */
      final String proxyHost = proxyData.getHost();
      final int proxyPort = proxyData.getPort();
      final Pair<String /* Username */, String /* Domain */> proxyUserAndDomain = splitUserAndDomain(proxyData.getUserId());
      final String proxyPassword = proxyData.getPassword();

      /* Return as IProxyCredentials Object */
      return new IProxyCredentials() {
        @Override
        public String getHost() {
          return proxyHost;
        }

        @Override
        public int getPort() {
          return proxyPort;
        }

        @Override
        public String getUsername() {
          return proxyUserAndDomain.getFirst();
        }

        @Override
        public String getPassword() {
          return proxyPassword;
        }

        @Override
        public String getDomain() {
          return proxyUserAndDomain.getSecond();
        }
      };
    }

    /* Feed does not require Proxy or Credentials not supplied */
    return null;
  }

  private Pair<String /* Username */, String /* Domain */> splitUserAndDomain(String username) {
    if (NTLM_PROXY_ENABLED && StringUtils.isSet(username) && username.contains(DOMAIN_SEPARATOR)) {
      String user = null;
      String domain = null;

      StringTokenizer tokenizer = new StringTokenizer(username, DOMAIN_SEPARATOR);
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        if (StringUtils.isSet(token)) {
          if (domain == null)
            domain = token;
          else if (user == null)
            user = token;
        }
      }

      if (StringUtils.isSet(user) && StringUtils.isSet(domain))
        return Pair.create(user, domain);
    }

    return Pair.create(username, null);
  }

  /*
   * @see
   * org.rssowl.core.connection.ICredentialsProvider#setAuthCredentials(org.
   * rssowl.core.connection.ICredentials, java.net.URI, java.lang.String)
   */
  @Override
  public void setAuthCredentials(ICredentials credentials, URI link, String realm) throws CredentialsException {
    internalSetAuthCredentials(credentials, link, realm, true);
  }

  /*
   * @see
   * org.rssowl.core.connection.ICredentialsProvider#setInMemoryAuthCredentials
   * (org.rssowl.core.connection.ICredentials, java.net.URI, java.lang.String)
   */
  @Override
  public void setInMemoryAuthCredentials(ICredentials credentials, URI link, String realm) throws CredentialsException {
    internalSetAuthCredentials(credentials, link, realm, false);
  }

  private void internalSetAuthCredentials(ICredentials credentials, URI link, String realm, boolean persist) throws CredentialsException {

    /* Store Credentials in In-Memory Store */
    if (!persist) {
      fInMemoryStore.put(toCacheKey(link, realm), credentials);
    }

    /* Store Credentials in secure Storage */
    else {
      ISecurePreferences securePreferences = getSecurePreferences();

      /* Check if Bundle is Stopped */
      if (securePreferences == null)
        return;

      /* Store in Equinox Security Storage */
      ISecurePreferences allFeedsPreferences = securePreferences.node(SECURE_FEED_NODE);
      ISecurePreferences feedPreferences = allFeedsPreferences.node(EncodingUtils.encodeSlashes(link.toString()));
      ISecurePreferences realmPreference = feedPreferences.node(EncodingUtils.encodeSlashes(realm != null ? realm : REALM));

      IPreferenceScope globalScope = Owl.getPreferenceService().getGlobalScope();

      /* OS Password is only supported on Windows and Mac */
      boolean useOSPassword = globalScope.getBoolean(DefaultPreferences.USE_OS_PASSWORD);
      if (!Platform.OS_WIN32.equals(Platform.getOS()) && !Platform.OS_MACOSX.equals(Platform.getOS()))
        useOSPassword = false;

      boolean encryptPW = useOSPassword || globalScope.getBoolean(DefaultPreferences.USE_MASTER_PASSWORD);
      try {
        if (credentials.getUsername() != null)
          realmPreference.put(USERNAME, credentials.getUsername(), encryptPW);

        if (credentials.getPassword() != null)
          realmPreference.put(PASSWORD, credentials.getPassword(), encryptPW);

        if (credentials.getDomain() != null)
          realmPreference.put(DOMAIN, credentials.getDomain(), encryptPW);

        realmPreference.flush(); // Flush to disk early
      } catch (StorageException e) {
        throw new CredentialsException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
      } catch (IOException e) {
        throw new CredentialsException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
      }
    }

    /* Uncache */
    removeUnprotected(link, realm);
  }

  /*
   * @see
   * org.rssowl.core.connection.auth.ICredentialsProvider#setProxyCredentials
   * (org.rssowl.core.connection.auth.IProxyCredentials, java.net.URI)
   */
  @Override
  public void setProxyCredentials(IProxyCredentials credentials, URI link) {
    IProxyService proxyService = Activator.getDefault().getProxyService();
    proxyService.setProxiesEnabled(true);
    boolean isSSL = URIUtils.HTTPS_SCHEME.equals(link.getScheme());

    /* Retrieve Proxy Data */
    final IProxyData proxyData = proxyService.getProxyData(isSSL ? IProxyData.HTTPS_PROXY_TYPE : IProxyData.HTTP_PROXY_TYPE);
    if (proxyData != null) { //TODO What if Data is NULL?
      proxyData.setHost(credentials.getHost());
      proxyData.setPort(credentials.getPort());
      proxyData.setUserid(credentials.getUsername());
      proxyData.setPassword(credentials.getPassword());
    }
  }

  /*
   * @see
   * org.rssowl.core.connection.ICredentialsProvider#deleteAuthCredentials(java
   * .net.URI, java.lang.String)
   */
  @Override
  public synchronized void deleteAuthCredentials(URI link, String realm) throws CredentialsException {

    /* Delete from In-Memory Store if present */
    fInMemoryStore.remove(toCacheKey(link, realm));

    /* Delete from Cache */
    removeUnprotected(link, realm);

    /* Check if Bundle is Stopped */
    ISecurePreferences securePreferences = getSecurePreferences();
    if (securePreferences == null)
      return;

    /* Remove from Equinox Security Storage */
    if (securePreferences.nodeExists(SECURE_FEED_NODE)) { // Global Feed Node
      ISecurePreferences allFeedsPreferences = securePreferences.node(SECURE_FEED_NODE);
      if (allFeedsPreferences.nodeExists(EncodingUtils.encodeSlashes(link.toString()))) { // Feed Node
        ISecurePreferences feedPreferences = allFeedsPreferences.node(EncodingUtils.encodeSlashes(link.toString()));
        if (feedPreferences.nodeExists(EncodingUtils.encodeSlashes(realm != null ? realm : REALM))) { // Realm Node
          ISecurePreferences realmPreferences = feedPreferences.node(EncodingUtils.encodeSlashes(realm != null ? realm : REALM));
          realmPreferences.clear();
          realmPreferences.removeNode();
          try {
            feedPreferences.flush();
          } catch (IOException e) {
            throw new CredentialsException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
          }
        }
      }
    }
  }

  /*
   * @see
   * org.rssowl.core.connection.auth.ICredentialsProvider#deleteProxyCredentials
   * (java.net.URI)
   */
  @Override
  public void deleteProxyCredentials(URI link) {
    IProxyService proxyService = Activator.getDefault().getProxyService();
    proxyService.setProxiesEnabled(false);
    //TODO System Properties are still set?
  }

  /**
   * An internal method only available for the
   * {@link PlatformCredentialsProvider} to clear all secure preferences nodes.
   * This method is called e.g. when the master password is to be changed or
   * disabled.
   */
  public void clear() {

    /* Clear In-Memory Store */
    fInMemoryStore.clear();

    /* Clear unprotected links cache */
    fUnprotectedLinksCache.clear();

    /* Clear cached info */
    InternalExchangeUtils.passwordProvidersReset();

    /* Remove all Nodes */
    ISecurePreferences secureRoot = getSecurePreferences();

    /* Check if Bundle is Stopped */
    if (secureRoot == null)
      return;

    String[] childrenNames = secureRoot.childrenNames();
    for (String child : childrenNames) {
      secureRoot.node(child).removeNode();
    }

    /* Flush to Disk */
    try {
      secureRoot.flush();
    } catch (IOException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }
  }

  private boolean isUnprotected(URI link, String realm) {
    return fUnprotectedLinksCache.contains(toCacheKey(link, realm));
  }

  private void addUnprotected(URI link, String realm) {
    fUnprotectedLinksCache.add(toCacheKey(link, realm));
  }

  private void removeUnprotected(URI link, String realm) {
    fUnprotectedLinksCache.remove(toCacheKey(link, realm));
  }

  private String toCacheKey(URI link, String realm) {
    if (realm == null)
      realm = REALM;

    return link.toString() + realm;
  }
}
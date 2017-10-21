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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.url.URLStreamHandlerService;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.AuthenticationRequiredException;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.HttpConnectionInputStream;
import org.rssowl.core.connection.IAbortable;
import org.rssowl.core.connection.IConditionalGetCompatible;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.ICredentialsProvider;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.connection.IProxyCredentials;
import org.rssowl.core.connection.MonitorCanceledException;
import org.rssowl.core.connection.NotModifiedException;
import org.rssowl.core.connection.ProxyAuthenticationRequiredException;
import org.rssowl.core.connection.SyncConnectionException;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.interpreter.EncodingException;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.Triple;
import org.rssowl.core.util.URIUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * The <code>DefaultFeedHandler</code> is an implementation of
 * <code>IProtocolHandler</code> that works on HTTP, HTTPS and the FILE
 * Protocol. After loading the Inputstream of the given URL, the stream is
 * passed to the Interpreter-Component to interpret it as one of the supported
 * XML-Formats for Newsfeeds.
 *
 * @author bpasero
 */
public class DefaultProtocolHandler implements IProtocolHandler {

  /* Http Status Codes */
  private static final int HTTP_ERRORS = 400;
  private static final int HTTP_STATUS_NOT_MODIFIED = 304;
  private static final int HTTP_ERROR_AUTH_REQUIRED = 401;
  private static final int HTTP_ERROR_FORBIDDEN = 403;
  private static final int HTTP_ERROR_PROXY_AUTH_REQUIRED = 407;

  /* Header Constants */
  private static final String HEADER_REQUEST_COOKIE = "Cookie"; //$NON-NLS-1$
  private static final String HEADER_REQUEST_ACCEPT_LANGUAGE = "Accept-Language"; //$NON-NLS-1$
  private static final String HEADER_REQUEST_USER_AGENT = "User-Agent"; //$NON-NLS-1$
  private static final String HEADER_REQUEST_ACCEPT_ENCODING = "Accept-Encoding"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_CONTENT_ENCODING = "Content-Encoding"; //$NON-NLS-1$

  /* Google Error Response Codes */
  private static final String HEADER_RESPONSE_ERROR = "Error"; //$NON-NLS-1$
  private static final String HEADER_RESPONSE_URL = "Url"; //$NON-NLS-1$
  private static final String ERROR_BAD_AUTH = "BadAuthentication"; //$NON-NLS-1$
  private static final String ERROR_NOT_VERIFIED = "NotVerified"; //$NON-NLS-1$
  private static final String ERROR_NO_TERMS = "TermsNotAgreed"; //$NON-NLS-1$
  private static final String ERROR_CAPTCHA_REQUIRED = "CaptchaRequired"; //$NON-NLS-1$
  private static final String ERROR_UNKNOWN = "Unknown"; //$NON-NLS-1$
  private static final String ERROR_ACCOUNT_DELETED = "AccountDeleted"; //$NON-NLS-1$
  private static final String ERROR_ACCOUNT_DISABLED = "AccountDisabled"; //$NON-NLS-1$
  private static final String ERROR_SERVICE_DISABLED = "ServiceDisabled"; //$NON-NLS-1$
  private static final String ERROR_SERVICE_UNAVAILABLE = "ServiceUnavailable"; //$NON-NLS-1$

  /** Property to tell the XML parser to use platform encoding */
  public static final String USE_PLATFORM_ENCODING = "org.rssowl.core.internal.connection.DefaultProtocolHandler.UsePlatformEncoding"; //$NON-NLS-1$

  /* The Default Connection Timeout */
  private static final int DEFAULT_CON_TIMEOUT = 30000;

  /* Timeout for loading a Feed or Label for a Feed */
  private static final int FEED_LABEL_CON_TIMEOUT = 10000;

  /* Timeout for loading a Favicon */
  private static final int FAVICON_CON_TIMEOUT = 5000;

  /* Set a limit for titles that are looked up from feeds */
  private static final int MAX_DETECTED_TITLE_LENGTH = 1024;

  private static final String USER_AGENT = CoreUtils.getUserAgent();
  private static boolean fgSSLInitialized;
  private static boolean fgFeedProtocolInitialized;

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#reload(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor, java.util.Map)
   */
  @Override
  public Triple<IFeed, IConditionalGet, URI> reload(URI link, IProgressMonitor monitor, Map<Object, Object> properties) throws CoreException {
    IModelFactory typesFactory = Owl.getModelFactory();

    /* Create a new empty feed from the existing one */
    IFeed feed = typesFactory.createFeed(null, link);

    /* Add Monitor to support early cancelation */
    if (properties == null)
      properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.PROGRESS_MONITOR, monitor);

    /* Retrieve the InputStream out of the Feed's Link */
    InputStream inS = openStream(link, properties);

    /* Retrieve Conditional Get if present */
    IConditionalGet conditionalGet = getConditionalGet(link, inS);

    /* Return on Cancelation or Shutdown */
    if (monitor.isCanceled()) {
      closeStream(inS, true);
      return null;
    }

    /* Pass the Stream to the Interpreter */
    try {
      Owl.getInterpreter().interpret(inS, feed, null);
    } catch (EncodingException e) {

      /* Return on Cancelation or Shutdown */
      if (monitor.isCanceled()) {
        closeStream(inS, true);
        return null;
      }

      /* Re-retrieve InputStream from the Feed's Link */
      inS = openStream(link, properties);

      /* Re-retrieve Conditional Get if present */
      conditionalGet = getConditionalGet(link, inS);

      /* Return on Cancelation or Shutdown */
      if (monitor.isCanceled()) {
        closeStream(inS, true);
        return null;
      }

      /* Second try: Use platform encoding */
      Owl.getInterpreter().interpret(inS, feed, Collections.singletonMap((Object) USE_PLATFORM_ENCODING, (Object) Boolean.TRUE));
    }

    /* Return actual URI that was connected to (supporting redirects) */
    if (inS instanceof HttpConnectionInputStream)
      return Triple.create(feed, conditionalGet, ((HttpConnectionInputStream) inS).getLink());

    /* Otherwise just use input URI */
    return Triple.create(feed, conditionalGet, link);
  }

  protected IConditionalGet getConditionalGet(URI link, InputStream inS) {
    IModelFactory typesFactory = Owl.getModelFactory();

    if (inS instanceof IConditionalGetCompatible) {
      String ifModifiedSince = ((IConditionalGetCompatible) inS).getIfModifiedSince();
      String ifNoneMatch = ((IConditionalGetCompatible) inS).getIfNoneMatch();

      if (ifModifiedSince != null || ifNoneMatch != null)
        return typesFactory.createConditionalGet(ifModifiedSince, link, ifNoneMatch);
    }

    return null;
  }

  protected void closeStream(InputStream inS, boolean abort) {
    try {
      if (abort && inS instanceof IAbortable)
        ((IAbortable) inS).abort();
      else if (inS != null)
        inS.close();
    } catch (IOException ex) {
      /* Ignore */
    }
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#getFeedIcon(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public byte[] getFeedIcon(URI link, IProgressMonitor monitor) {

    /* Try to load the Favicon directly from the supplied Link */
    byte[] favicon = loadFavicon(link, false, false, monitor);

    /* Fallback: Scan the Homepage of the Link for a Favicon entry */
    if (favicon == null || favicon.length == 0) {
      try {
        URI topLevelUri = URIUtils.toTopLevel(link);
        if (topLevelUri != null) {
          URI faviconUri = getFavicon(topLevelUri, monitor);
          if (faviconUri != null && faviconUri.isAbsolute())
            return loadFavicon(faviconUri, true, false, monitor);
        }
      } catch (ConnectionException e) {
      } catch (URISyntaxException e) {
      } catch (Throwable t) {
        Activator.getDefault().logError(t.getMessage(), t);
      }
    }

    return favicon;
  }

  /* Load a possible Favicon from the given Feed */
  byte[] loadFavicon(URI link, boolean isFavicon, boolean rewriteHost, IProgressMonitor monitor) {
    InputStream inS = null;
    boolean isError = false;
    try {

      /* Define Properties for Connection */
      Map<Object, Object> properties = new HashMap<Object, Object>();
      properties.put(IConnectionPropertyConstants.CON_TIMEOUT, FAVICON_CON_TIMEOUT);
      properties.put(IConnectionPropertyConstants.PROGRESS_MONITOR, monitor);

      /* Load Favicon */
      URI faviconLink = isFavicon ? link : URIUtils.toFaviconUrl(link, rewriteHost);
      if (faviconLink == null)
        return null;

      inS = openStream(faviconLink, properties);

      ByteArrayOutputStream fos = new ByteArrayOutputStream();
      byte buffer[] = new byte[0xffff];
      int nbytes;

      while ((nbytes = inS.read(buffer)) != -1)
        fos.write(buffer, 0, nbytes);

      return fos.toByteArray();
    } catch (URISyntaxException e) {
      /* Ignore */
    } catch (ConnectionException e) {
      isError = true;

      /* Try rewriting the Host to obtain the Favicon */
      if (!rewriteHost && !isFavicon) {
        String exceptionName = e.getClass().getName();

        /* Only retry in case this is a generic ConnectionException */
        if (ConnectionException.class.getName().equals(exceptionName))
          return loadFavicon(link, false, true, monitor);
      }
    } catch (IOException e) {
      /* Ignore */
    } finally {
      closeStream(inS, isError);
    }

    return null;
  }

  private URI getFavicon(URI link, IProgressMonitor monitor) throws ConnectionException {

    /* Define Properties for Connection */
    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.PROGRESS_MONITOR, monitor);
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, FAVICON_CON_TIMEOUT);

    /* Open Stream */
    InputStream inS = openStream(link, properties);
    BufferedInputStream bufIns = new BufferedInputStream(inS);
    BufferedReader reader = new BufferedReader(new InputStreamReader(bufIns));
    try {

      /* Use real Base if possible */
      if (inS instanceof HttpConnectionInputStream)
        return CoreUtils.findFavicon(reader, ((HttpConnectionInputStream) inS).getLink(), monitor);

      /* Otherwise use request URI */
      return CoreUtils.findFavicon(reader, link, monitor);
    }

    /* Finally close the Stream */
    finally {
      closeStream(inS, true); //Abort the stream to avoid downloading the full content
    }
  }

  /**
   * Do not override default URLStreamHandler of HTTP/HTTPS and therefor return
   * NULL.
   *
   * @see org.rssowl.core.connection.IProtocolHandler#getURLStreamHandler()
   */
  @Override
  public URLStreamHandlerService getURLStreamHandler() {
    return null;
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#openStream(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor, java.util.Map)
   */
  @Override
  public InputStream openStream(URI link, IProgressMonitor monitor, Map<Object, Object> properties) throws ConnectionException {

    /* Add Monitor to support early cancelation */
    if (monitor != null) {
      if (properties == null)
        properties = new HashMap<Object, Object>();
      properties.put(IConnectionPropertyConstants.PROGRESS_MONITOR, monitor);
    }

    return openStream(link, properties);
  }

  /**
   * Load the Contents of the given URL by connecting to it. The additional
   * properties may be used in conjunction with the
   * <code>IConnectionPropertyConstants</code> to define connection related
   * properties..
   *
   * @param link The URL to load.
   * @param properties Connection related properties as defined in
   * <code>IConnectionPropertyConstants</code> for example, or <code>NULL</code>
   * if none.
   * @return The Content of the URL as InputStream.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   * @see AuthenticationRequiredException
   * @see NotModifiedException
   */
  protected InputStream openStream(URI link, Map<Object, Object> properties) throws ConnectionException {

    /* Retrieve the InputStream out of the Link */
    try {
      return internalOpenStream(link, link, null, properties);
    }

    /* Handle Authentication Required */
    catch (AuthenticationRequiredException e) {

      /* Realm required from here on */
      if (e.getRealm() == null)
        throw e;

      /* Try to load credentials using Host / Port / Realm */
      URI normalizedUri = URIUtils.normalizeUri(link, true);
      ICredentials authCredentials = Owl.getConnectionService().getAuthCredentials(normalizedUri, e.getRealm());

      /* Credentials based on Host / Port / Realm provided */
      if (authCredentials != null) {

        /* Store for plain URI too */
        ICredentialsProvider credProvider = Owl.getConnectionService().getCredentialsProvider(link);
        if (credProvider.getPersistedAuthCredentials(normalizedUri, e.getRealm()) != null)
          credProvider.setAuthCredentials(authCredentials, link, null);
        else
          credProvider.setInMemoryAuthCredentials(authCredentials, link, null);

        /* Reopen Stream */
        try {
          return internalOpenStream(link, normalizedUri, e.getRealm(), properties);
        } catch (AuthenticationRequiredException ex) {
          Owl.getConnectionService().getCredentialsProvider(normalizedUri).deleteAuthCredentials(normalizedUri, e.getRealm());
          throw ex;
        }
      }

      /* Otherwise throw exception to callee */
      throw e;
    }
  }

  private InputStream internalOpenStream(URI link, URI authLink, String authRealm, Map<Object, Object> properties) throws ConnectionException {

    /* Handle File Protocol at first */
    if (URIUtils.FILE_SCHEME.equals(link.getScheme()))
      return loadFileProtocol(link);

    /* SSL Support */
    if (URIUtils.HTTPS_SCHEME.equals(link.getScheme()))
      initSSLProtocol();

    /* Feed Support */
    if (URIUtils.FEED_SCHEME.equals(link.getScheme()))
      initFeedProtocol();

    /* Init Client */
    HttpClient client = initClient(properties);

    /* Init the connection */
    HttpMethodBase method = null;
    InputStream inS = null;
    try {

      /* Create Method (GET or POST) */
      method = initConnection(link, properties);

      /* Proxy if required */
      setupProxy(link, client);

      /* Authentication if required */
      setupAuthentication(authLink, authRealm, client, method);

      /* Open the connection */
      inS = openConnection(client, method);

      /* Try to pipe the resulting stream into a GZipInputStream */
      if (inS != null)
        inS = pipeStream(inS, method);
    } catch (IOException e) {
      abortAndRelease(method);
      throw new ConnectionException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    }

    /* In case authentication required */
    int statusCode = method.getStatusCode();
    if (statusCode == HTTP_ERROR_AUTH_REQUIRED) {
      AuthState hostAuthState = method.getHostAuthState();
      String realm = hostAuthState != null ? hostAuthState.getRealm() : null;
      abortAndRelease(method);

      throw new AuthenticationRequiredException(realm, Activator.getDefault().createErrorStatus(Messages.DefaultProtocolHandler_ERROR_AUTHENTICATION_REQUIRED, null));
    }

    /* In case sync authentication failed (Forbidden) */
    else if (isSyncAuthenticationIssue(method, authLink)) {
      abortAndRelease(method);

      throw new AuthenticationRequiredException(null, Activator.getDefault().createErrorStatus(Messages.DefaultProtocolHandler_GR_ERROR_BAD_AUTH, null));
    }

    /* In case of Forbidden Status with Error Code (Google Reader) */
    else if (statusCode == HTTP_ERROR_FORBIDDEN && method.getResponseHeader(HEADER_RESPONSE_ERROR) != null)
      handleForbidden(method);

    /* In case proxy-authentication required / failed */
    else if (statusCode == HTTP_ERROR_PROXY_AUTH_REQUIRED) {
      abortAndRelease(method);

      throw new ProxyAuthenticationRequiredException(Activator.getDefault().createErrorStatus(Messages.DefaultProtocolHandler_ERROR_PROXY_AUTHENTICATION_REQUIRED, null));
    }

    /* If status code is 4xx, throw an IOException with the status code included */
    else if (statusCode >= HTTP_ERRORS) {
      String error = getError(statusCode);
      abortAndRelease(method);

      if (error != null)
        throw new ConnectionException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.DefaultProtocolHandler_ERROR_HTTP_STATUS_MSG, String.valueOf(statusCode), error), null));

      throw new ConnectionException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.DefaultProtocolHandler_ERROR_HTTP_STATUS, String.valueOf(statusCode)), null));
    }

    /* In case the Feed has not been modified since */
    else if (statusCode == HTTP_STATUS_NOT_MODIFIED) {
      abortAndRelease(method);

      throw new NotModifiedException(Activator.getDefault().createInfoStatus(Messages.DefaultProtocolHandler_INFO_NOT_MODIFIED_SINCE, null));
    }

    /* In case response body is not available */
    if (inS == null) {
      abortAndRelease(method);

      throw new ConnectionException(Activator.getDefault().createErrorStatus(Messages.DefaultProtocolHandler_ERROR_STREAM_UNAVAILABLE, null));
    }

    /* Check wether a Progress Monitor is provided to support early cancelation */
    IProgressMonitor monitor = null;
    if (properties != null && properties.containsKey(IConnectionPropertyConstants.PROGRESS_MONITOR))
      monitor = (IProgressMonitor) properties.get(IConnectionPropertyConstants.PROGRESS_MONITOR);

    /* Return a Stream that releases the connection once closed */
    return new HttpConnectionInputStream(link, method, monitor, inS);
  }

  private void abortAndRelease(HttpMethodBase method) {
    if (method != null) {
      method.abort();
      method.releaseConnection();
    }
  }

  private boolean isSyncAuthenticationIssue(HttpMethodBase method, URI link) {

    /* Handle Google Error Response "Forbidden" for synced connections */
    if (method.getStatusCode() == HTTP_ERROR_FORBIDDEN && SyncUtils.fromGoogle(link.toString())) {
      Header errorHeader = method.getResponseHeader(HEADER_RESPONSE_ERROR);
      if (errorHeader == null || ERROR_BAD_AUTH.equals(errorHeader.getValue()))
        return true;
    }

    return false;
  }

  protected void handleForbidden(HttpMethodBase method) throws ConnectionException {
    String errorMsg = null;
    String errorUrl = null;

    /* Lookup Google Error if present */
    Header errorHeader = method.getResponseHeader(HEADER_RESPONSE_ERROR);
    if (errorHeader != null && StringUtils.isSet(errorHeader.getValue())) {
      String errorCode = errorHeader.getValue();
      if (ERROR_BAD_AUTH.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_BAD_AUTH;
      else if (ERROR_NOT_VERIFIED.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_NOT_VERIFIED;
      else if (ERROR_NO_TERMS.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_NO_TERMS;
      else if (ERROR_UNKNOWN.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_UNKNOWN;
      else if (ERROR_ACCOUNT_DELETED.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_ACCOUNT_DELETED;
      else if (ERROR_ACCOUNT_DISABLED.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_ACCOUNT_DISABLED;
      else if (ERROR_SERVICE_DISABLED.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_SERVICE_DISABLED;
      else if (ERROR_SERVICE_UNAVAILABLE.equals(errorCode))
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_SERVICE_UNAVAILABLE;
      else if (ERROR_CAPTCHA_REQUIRED.equals(errorCode)) {
        errorMsg = Messages.DefaultProtocolHandler_GR_ERROR_CAPTCHA_REQUIRED;
        errorUrl = SyncUtils.CAPTCHA_UNLOCK_URL;
      }

      /* Also look up specified Error URL as necessary */
      if (errorUrl == null) {
        Header urlHeader = method.getResponseHeader(HEADER_RESPONSE_URL);
        if (urlHeader != null && StringUtils.isSet(urlHeader.getValue()))
          errorUrl = urlHeader.getValue();
      }
    }

    /* Otherwise throw generic Forbidden Exception */
    if (errorMsg == null)
      errorMsg = Messages.DefaultProtocolHandler_ERROR_FORBIDDEN;

    abortAndRelease(method);

    if (errorUrl != null)
      throw new SyncConnectionException(errorUrl, Activator.getDefault().createErrorStatus(errorMsg, null));

    throw new ConnectionException(Activator.getDefault().createErrorStatus(errorMsg, null));
  }

  /* Some HTTP Error Messages */
  private String getError(int errorCode) {
    switch (errorCode) {
      case 400:
        return "Bad Request"; //$NON-NLS-1$
      case 403:
        return "Forbidden"; //$NON-NLS-1$
      case 404:
        return "Not Found"; //$NON-NLS-1$
      case 408:
        return "Request Timeout"; //$NON-NLS-1$
      case 500:
        return "Internal Server Error"; //$NON-NLS-1$
      case 502:
        return "Bad Gateway"; //$NON-NLS-1$
      case 503:
        return "Service Unavailable"; //$NON-NLS-1$
    }

    return null;
  }

  private InputStream loadFileProtocol(URI link) throws ConnectionException {
    try {
      File file = new File(link);
      return new BufferedInputStream(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      throw new ConnectionException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    }
  }

  private void setupAuthentication(URI link, String realm, HttpClient client, HttpMethodBase method) throws URIException, CredentialsException {
    ICredentials authCredentials = Owl.getConnectionService().getAuthCredentials(link, realm);
    if (authCredentials != null) {
      client.getParams().setAuthenticationPreemptive(true);

      /* Require Host */
      String host = method.getURI().getHost();

      /* Create the UsernamePasswordCredentials */
      NTCredentials userPwCreds = new NTCredentials(authCredentials.getUsername(), authCredentials.getPassword(), host, (authCredentials.getDomain() != null) ? authCredentials.getDomain() : ""); //$NON-NLS-1$

      /* Authenticate to the Server */
      client.getState().setCredentials(AuthScope.ANY, userPwCreds);
      method.setDoAuthentication(true);
    }
  }

  private void setupProxy(URI link, HttpClient client) throws CredentialsException {
    IProxyCredentials creds = Owl.getConnectionService().getProxyCredentials(link);
    if (creds != null) {

      /* Apply Proxy Config to HTTPClient */
      client.getParams().setAuthenticationPreemptive(true);
      client.getHostConfiguration().setProxy(creds.getHost(), creds.getPort());

      /* Authenticate if required */
      if (creds.getUsername() != null || creds.getPassword() != null) {
        String user = StringUtils.isSet(creds.getUsername()) ? creds.getUsername() : ""; //$NON-NLS-1$
        String pw = StringUtils.isSet(creds.getPassword()) ? creds.getPassword() : ""; //$NON-NLS-1$

        AuthScope proxyAuthScope = new AuthScope(creds.getHost(), creds.getPort());

        /* Use NTLM Credentials if Domain is set */
        if (creds.getDomain() != null)
          client.getState().setProxyCredentials(proxyAuthScope, new NTCredentials(user, pw, creds.getHost(), creds.getDomain()));

        /* Use normal Credentials if Domain is not set */
        else
          client.getState().setProxyCredentials(proxyAuthScope, new UsernamePasswordCredentials(user, pw));
      }
    }
  }

  private synchronized void initSSLProtocol() {
    if (fgSSLInitialized)
      return;

    /* Register Easy Protocol Socket Factory with HTTPS */
    Protocol easyHttpsProtocol = new Protocol(URIUtils.HTTPS_SCHEME, (ProtocolSocketFactory) Owl.getConnectionService().getSecureProtocolSocketFactory(), 443);
    Protocol.registerProtocol(URIUtils.HTTPS_SCHEME, easyHttpsProtocol);

    fgSSLInitialized = true;
  }

  private synchronized void initFeedProtocol() {
    if (fgFeedProtocolInitialized)
      return;

    Protocol feed = new Protocol(URIUtils.FEED_SCHEME, new DefaultProtocolSocketFactory(), 80);
    Protocol.registerProtocol(URIUtils.FEED_SCHEME, feed);

    fgFeedProtocolInitialized = true;
  }

  private void setHeaders(Map<Object, Object> properties, HttpMethodBase method) {
    method.setRequestHeader(HEADER_REQUEST_ACCEPT_ENCODING, "gzip"); //$NON-NLS-1$
    method.setRequestHeader(HEADER_REQUEST_USER_AGENT, USER_AGENT);

    /* Add Conditional GET Headers if present */
    if (properties != null) {
      String ifModifiedSince = (String) properties.get(IConnectionPropertyConstants.IF_MODIFIED_SINCE);
      String ifNoneMatch = (String) properties.get(IConnectionPropertyConstants.IF_NONE_MATCH);

      if (ifModifiedSince != null)
        method.setRequestHeader(HEADER_RESPONSE_IF_MODIFIED_SINCE, ifModifiedSince);

      if (ifNoneMatch != null)
        method.setRequestHeader(HEADER_RESPONSE_IF_NONE_MATCH, ifNoneMatch);
    }

    /* Add Accept-Language Header if present */
    if (properties != null && properties.containsKey(IConnectionPropertyConstants.ACCEPT_LANGUAGE))
      method.setRequestHeader(HEADER_REQUEST_ACCEPT_LANGUAGE, (String) properties.get(IConnectionPropertyConstants.ACCEPT_LANGUAGE));

    /* Add Cookie Header if present */
    if (properties != null && properties.containsKey(IConnectionPropertyConstants.COOKIE))
      method.setRequestHeader(HEADER_REQUEST_COOKIE, (String) properties.get(IConnectionPropertyConstants.COOKIE));

    /* Add more Headers */
    if (properties != null && properties.containsKey(IConnectionPropertyConstants.HEADERS)) {
      Map<?, ?> headers = (Map<?, ?>) properties.get(IConnectionPropertyConstants.HEADERS);
      Set<?> entries = headers.entrySet();
      for (Object obj : entries) {
        Entry<?, ?> entry = (Entry<?, ?>) obj;
        method.setRequestHeader((String) entry.getKey(), (String) entry.getValue());
      }
    }
  }

  private HttpClient initClient(Map<Object, Object> properties) {

    /* Retrieve Connection Timeout from Properties if set */
    int conTimeout = DEFAULT_CON_TIMEOUT;
    if (properties != null && properties.containsKey(IConnectionPropertyConstants.CON_TIMEOUT))
      conTimeout = (Integer) properties.get(IConnectionPropertyConstants.CON_TIMEOUT);

    /* Create a new HttpClient */
    HttpClient client = new HttpClient(new SimpleHttpConnectionManager(true));

    /* Socket Timeout - Max. time to wait for an answer */
    client.getHttpConnectionManager().getParams().setSoTimeout(conTimeout);

    /* Connection Timeout - Max. time to wait for a connection */
    client.getHttpConnectionManager().getParams().setConnectionTimeout(conTimeout);

    return client;
  }

  private HttpMethodBase initConnection(URI link, Map<Object, Object> properties) throws IOException {

    /* Create the Method. Wrap any RuntimeException into an IOException */
    HttpMethodBase method = null;
    try {
      if (properties != null && properties.containsKey(IConnectionPropertyConstants.POST))
        method = new PostMethod(link.toString());
      else
        method = new GetMethod(link.toString());
    } catch (RuntimeException e) {
      throw new IOException(e.getMessage());
    }

    /* Ignore Cookies */
    if (method instanceof GetMethod)
      method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

    /* Set Headers */
    setHeaders(properties, method);

    /* Set Parameters (POST only) */
    if (method instanceof PostMethod && properties != null && properties.containsKey(IConnectionPropertyConstants.PARAMETERS)) {
      Map<?, ?> parameters = (Map<?, ?>) properties.get(IConnectionPropertyConstants.PARAMETERS);
      Set<?> entries = parameters.entrySet();
      for (Object obj : entries) {
        Entry<?, ?> entry = (Entry<?, ?>) obj;
        String key = (String) entry.getKey();
        if (entry.getValue() instanceof String)
          ((PostMethod) method).addParameter(key, (String) entry.getValue());
        else if (entry.getValue() instanceof String[]) {
          String[] parameterValues = (String[]) entry.getValue();
          for (String value : parameterValues) {
            ((PostMethod) method).addParameter(key, value);
          }
        }
      }
    }

    /* Follow Redirects */
    if (method instanceof GetMethod)
      method.setFollowRedirects(true);

    return method;
  }

  private InputStream openConnection(HttpClient client, HttpMethodBase method) throws HttpException, IOException {

    /* Execute the Method */
    client.executeMethod(method);

    /* Finally retrieve the InputStream from the respond body */
    return method.getResponseBodyAsStream();
  }

  private InputStream pipeStream(InputStream inputStream, HttpMethodBase method) throws IOException {
    Assert.isNotNull(inputStream);

    /* Retrieve the Content Encoding */
    String contentEncoding = method.getResponseHeader(HEADER_RESPONSE_CONTENT_ENCODING) != null ? method.getResponseHeader(HEADER_RESPONSE_CONTENT_ENCODING).getValue() : null;
    boolean isGzipStream = false;

    /*
     * Return in case the Content Encoding is not given and the InputStream does
     * not support mark() and reset()
     */
    if ((contentEncoding == null || !contentEncoding.equals("gzip")) && !inputStream.markSupported()) //$NON-NLS-1$
      return inputStream;

    /* Content Encoding is set to gzip, so use the GZipInputStream */
    if (contentEncoding != null && contentEncoding.equals("gzip")) { //$NON-NLS-1$
      isGzipStream = true;
    }

    /* Detect if the Stream is gzip encoded */
    else if (inputStream.markSupported()) {
      inputStream.mark(2);
      int id1 = inputStream.read();
      int id2 = inputStream.read();
      inputStream.reset();

      /* Check for GZip Magic Numbers (See RFC 1952) */
      if (id1 == 0x1F && id2 == 0x8B)
        isGzipStream = true;
    }

    /* Create the GZipInputStream then */
    if (isGzipStream) {
      try {
        return new GZIPInputStream(inputStream);
      } catch (IOException e) {
        return inputStream;
      }
    }
    return inputStream;
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#getLabel(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public String getLabel(URI link, IProgressMonitor monitor) throws ConnectionException {
    String title = ""; //$NON-NLS-1$

    /* Define Properties for Connection */
    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.PROGRESS_MONITOR, monitor);
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, FEED_LABEL_CON_TIMEOUT);

    /* Open Stream */
    InputStream inS = openStream(link, properties);
    try {

      /* Return on Cancelation or Shutdown */
      if (monitor.isCanceled())
        return null;

      /* Buffered Stream to support mark and reset */
      BufferedInputStream bufIns = new BufferedInputStream(inS);
      bufIns.mark(8192);

      /* Try to read Encoding out of XML Document */
      String encoding = getEncodingFromXML(new InputStreamReader(bufIns), monitor);

      /* Avoid lowercase UTF-8 notation */
      if ("utf-8".equalsIgnoreCase(encoding)) //$NON-NLS-1$
        encoding = "UTF-8"; //$NON-NLS-1$

      /* Reset the Stream to its beginning */
      bufIns.reset();

      /* Grab Title using supplied Encoding */
      if (StringUtils.isSet(encoding) && Charset.isSupported(encoding))
        title = getTitleFromFeed(new BufferedReader(new InputStreamReader(bufIns, encoding)), monitor);

      /* Grab Title using Default Encoding */
      else
        title = getTitleFromFeed(new BufferedReader(new InputStreamReader(bufIns)), monitor);

      /* Remove the title tags (also delete attributes in title tag) */
      title = title.replaceAll("<title[^>]*>", ""); //$NON-NLS-1$ //$NON-NLS-2$
      title = title.replaceAll("</title>", ""); //$NON-NLS-1$ //$NON-NLS-2$

      /* Remove potential CDATA Tags */
      title = title.replaceAll(Pattern.quote("<![CDATA["), ""); //$NON-NLS-1$ //$NON-NLS-2$
      title = title.replaceAll(Pattern.quote("]]>"), ""); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (IOException e) {
      if (!(e instanceof MonitorCanceledException))
        Activator.safeLogError(e.getMessage(), e);
    }

    /* Finally close the Stream */
    finally {
      closeStream(inS, true); //Abort the stream to avoid downloading the full content
    }

    // Have an upper maximum of title length to protect against issues
    String result = StringUtils.stripTags(title.trim(), true);
    if (result.length() > MAX_DETECTED_TITLE_LENGTH)
      result = result.substring(0, MAX_DETECTED_TITLE_LENGTH);

    return result;
  }

  /* Tries to read the encoding information from the given InputReader */
  private String getEncodingFromXML(InputStreamReader inputReader, IProgressMonitor monitor) throws IOException {
    String encoding = null;

    /* Read the first line or until the Tag is closed */
    StringBuilder strBuf = new StringBuilder();
    int c;
    while (!monitor.isCanceled() && (c = inputReader.read()) != -1) {
      char character = (char) c;

      /* Append all Characters, except for closing Tag or CR */
      if (character != '>' && character != '\n' && character != '\r')
        strBuf.append(character);

      /* Closing Tag is the last one to append */
      else if (character == '>') {
        strBuf.append(character);
        break;
      }

      /* End of Line or Tag reached */
      else
        break;
    }

    /* Save the first Line */
    String firstLine = strBuf.toString();

    /* Look if Encoding is supplied */
    if (firstLine.indexOf("encoding") >= 0) { //$NON-NLS-1$

      /* Extract the Encoding Value */
      String regEx = "<\\?.*encoding=[\"']([^\\s]*)[\"'].*\\?>"; //$NON-NLS-1$
      Pattern pattern = Pattern.compile(regEx);
      Matcher match = pattern.matcher(firstLine);

      /* Get first matching String */
      if (match.find())
        return match.group(1);
    }

    return encoding;
  }

  /* Tries to find the title information from the given Reader */
  private String getTitleFromFeed(BufferedReader inputReader, IProgressMonitor monitor) throws IOException {
    String title = ""; //$NON-NLS-1$
    String firstLine;
    boolean titleFound = false;

    /* Read the file until the Title is found or EOF is reached */
    while (true) {

      /* Return on Cancelation or Shutdown */
      if (monitor.isCanceled())
        return null;

      /* Will throw an IOException on EOF reached */
      firstLine = inputReader.readLine();

      /* EOF reached */
      if (firstLine == null)
        break;

      /* If the line contains the title, break loop */
      if (firstLine.indexOf("<title") >= 0 && firstLine.indexOf("</title>") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
        title = firstLine.trim();
        titleFound = true;
        break;
      }
    }

    /* Return if no title was found */
    if (!titleFound)
      return title;

    /* Extract the title String */
    String regEx = "<title[^>]*>[^<]*</title>"; //$NON-NLS-1$
    Pattern pattern = Pattern.compile(regEx);
    Matcher match = pattern.matcher(title);

    /* Get first matching String */
    if (match.find())
      title = match.group();

    return title;
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#getFeed(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public URI getFeed(final URI website, IProgressMonitor monitor) throws ConnectionException {

    /* Define Properties for Connection */
    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.PROGRESS_MONITOR, monitor);
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, FEED_LABEL_CON_TIMEOUT);

    /* Open Stream */
    InputStream inS = openStream(website, properties);
    BufferedInputStream bufIns = new BufferedInputStream(inS);
    BufferedReader reader = new BufferedReader(new InputStreamReader(bufIns));
    try {

      /* Our HttpConnectionInputStream */
      if (inS instanceof HttpConnectionInputStream) {

        /* Check the content type and return early if already a feed */
        String contentType = ((HttpConnectionInputStream) inS).getContentType();
        if (contentType != null) {
          for (String feedContentType : CoreUtils.FEED_MIME_TYPES) {
            if (contentType.toLowerCase().contains(feedContentType))
              return website;
          }
        }

        /* Use real Base if possible */
        return CoreUtils.findFeed(reader, ((HttpConnectionInputStream) inS).getLink(), monitor);
      }

      /* Normal Stream (use request URI) */
      return CoreUtils.findFeed(reader, website, monitor);
    }

    /* Finally close the Stream */
    finally {
      closeStream(inS, true); //Abort the stream to avoid downloading the full content
    }
  }
}
/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.core.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.AuthenticationRequiredException;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.connection.SyncConnectionException;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Some tools to synchronize with online services like Google Reader.
 *
 * @author bpasero
 */
public class SyncUtils {

  /** Flag to control enablement of Google Reader Sync */
  public static final boolean ENABLED = false;

  /** Google Client Login Site */
  public static final String GOOGLE_LOGIN_URL = "https://www.google.com/accounts/ClientLogin"; //$NON-NLS-1$

  /** Google Stream Service */
  public static final String GOOGLE_API_URL = "http://www.google.com/reader/api/0/"; //$NON-NLS-1$

  /** Google API Token Service */
  public static final String GOOGLE_API_TOKEN_URL = GOOGLE_API_URL + "token"; //$NON-NLS-1$

  /** Google Edit-Tag Service */
  public static final String GOOGLE_EDIT_TAG_URL = GOOGLE_API_URL + "edit-tag?client=scroll"; //$NON-NLS-1$

  /** Google Feed Service */
  public static final String GOOGLE_FEED_URL = GOOGLE_API_URL + "stream/contents/feed/"; //$NON-NLS-1$

  /** Google Stream Contents Service */
  public static final String GOOGLE_STREAM_CONTENTS_URL = GOOGLE_API_URL + "stream/contents/"; //$NON-NLS-1$

  /** Google Unread Count Service */
  public static final String GOOGLE_UNREAD_COUNT_URL = GOOGLE_API_URL + "unread-count"; //$NON-NLS-1$

  /** Google Account Creation URL (follows to Google Reader after signup) */
  public static final String GOOGLE_NEW_ACCOUNT_URL = "https://www.google.com/accounts/NewAccount?continue=http%3A%2F%2Fwww.google.com%2Freader%2F&followup=http%3A%2F%2Fwww.google.com%2Freader%2F&service=reader"; //$NON-NLS-1$

  /** Google Reader URL */
  public static final String GOOGLE_READER_URL = "https://reader.google.com"; //$NON-NLS-1$

  /** URL to export from Google Reader */
  public static final String GOOGLE_READER_OPML_URI = "https://www.google.com/reader/subscriptions/export"; //$NON-NLS-1$

  /** URL to unlock the Google Account using a captcha */
  public static final String CAPTCHA_UNLOCK_URL = "https://www.google.com/accounts/DisplayUnlockCaptcha"; //$NON-NLS-1$

  /** Schemes to use for synced feeds */
  public static final String READER_HTTP_SCHEME = "reader"; //$NON-NLS-1$
  public static final String READER_HTTPS_SCHEME = "readers"; //$NON-NLS-1$

  /** Special Google Reader Feeds */
  public static final String GOOGLE_READER_ALL_ITEMS_FEED = "reader://readinglist"; //$NON-NLS-1$
  public static final String GOOGLE_READER_STARRED_FEED = "reader://starred"; //$NON-NLS-1$
  public static final String GOOGLE_READER_SHARED_ITEMS_FEED = "reader://shared"; //$NON-NLS-1$
  public static final String GOOGLE_READER_RECOMMENDED_ITEMS_FEED = "reader://recommended"; //$NON-NLS-1$
  public static final String GOOGLE_READER_NOTES_FEED = "reader://notes"; //$NON-NLS-1$

  /** Some special preferences a news can have after parsed from the JSONInterpreter */
  public static final String GOOGLE_MARKED_UNREAD = "org.rssowl.pref.GoogleMarkedUnRead"; //$NON-NLS-1$
  public static final String GOOGLE_MARKED_READ = "org.rssowl.pref.GoogleMarkedRead"; //$NON-NLS-1$
  public static final String GOOGLE_LABELS = "org.rssowl.pref.GoogleLabels"; //$NON-NLS-1$

  /** Google Categories */
  public static final String CATEGORY_STARRED = "user/-/state/com.google/starred"; //$NON-NLS-1$
  public static final String CATEGORY_READ = "user/-/state/com.google/read"; //$NON-NLS-1$
  public static final String CATEGORY_UNREAD = "user/-/state/com.google/kept-unread"; //$NON-NLS-1$
  public static final String CATEGORY_TRACKING_UNREAD = "user/-/state/com.google/tracking-kept-unread "; //$NON-NLS-1$
  public static final String CATEGORY_LABEL_PREFIX = "user/-/label/"; //$NON-NLS-1$

  /** Google API Parameters */
  public static final String API_PARAM_TOKEN = "T"; //$NON-NLS-1$
  public static final String API_PARAM_STREAM = "s"; //$NON-NLS-1$
  public static final String API_PARAM_IDENTIFIER = "i"; //$NON-NLS-1$
  public static final String API_PARAM_TAG_TO_ADD = "a"; //$NON-NLS-1$
  public static final String API_PARAM_TAG_TO_REMOVE = "r"; //$NON-NLS-1$

  /** Default Connection Timeouts in MS */
  public static final int DEFAULT_CON_TIMEOUT = 30000;

  /** Short Connection Timeouts in MS */
  public static final int SHORT_CON_TIMEOUT = 5000;

  /* Google Auth Identifier */
  private static final String AUTH_IDENTIFIER = "Auth="; //$NON-NLS-1$

  /* Google Auth Header */
  private static final String GOOGLE_LOGIN_HEADER_VALUE = "GoogleLogin auth="; //$NON-NLS-1$

  /* Google Authentication Token can be shared during the session */
  private static String fgSharedAuthToken;
  private static final Object AUTH_TOKEN_LOCK= new Object();

  /* Google URL Prefixes */
  private static final String GOOGLE_HTTP_URL_PREFIX = "http://www.google.com"; //$NON-NLS-1$
  private static final String GOOGLE_HTTPS_URL_PREFIX = "https://www.google.com"; //$NON-NLS-1$

  /**
   * Obtains the Google Auth Token to perform REST operations for Google
   * Services.
   *
   * @param email the user account for google
   * @param pw the password for the user account
   * @param refresh if <code>true</code> causes a fresh authentication token to
   * be obtained and a shared one to be picked up otherwise.
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @return the google Auth Token for the given account or <code>null</code> if
   * none.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   */
  public static String getGoogleAuthToken(String email, String pw, boolean refresh, IProgressMonitor monitor) throws ConnectionException {

    /*
     * Return the shared token if existing or even null if not willing to
     * refresh. Clients have to force refresh to get the token then.
     */
    if (!refresh)
      return fgSharedAuthToken;

    /* Clear Shared Token */
    fgSharedAuthToken = null;

    /* Return on cancellation */
    if (monitor.isCanceled())
      return null;

    /* Obtain a new token (only 1 Thread permitted) */
    synchronized (AUTH_TOKEN_LOCK) {

      /* Another thread might have won the race */
      if (fgSharedAuthToken != null)
        return fgSharedAuthToken;

      /* Return on cancellation */
      if (monitor.isCanceled())
        return null;

      /* Now Connect to Google */
      try {
        fgSharedAuthToken = internalGetGoogleAuthToken(email, pw, monitor);
      } catch (URISyntaxException e) {
        throw new ConnectionException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
      } catch (IOException e) {
        throw new ConnectionException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
      }
    }

    return fgSharedAuthToken;
  }

  private static String internalGetGoogleAuthToken(String email, String pw, IProgressMonitor monitor) throws ConnectionException, URISyntaxException, IOException {
    URI uri = new URI(GOOGLE_LOGIN_URL);
    IProtocolHandler handler = Owl.getConnectionService().getHandler(uri);
    if (handler != null) {

      /* Google Specific Parameters */
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("accountType", "GOOGLE"); //$NON-NLS-1$ //$NON-NLS-2$
      parameters.put("Email", email); //$NON-NLS-1$
      parameters.put("Passwd", pw); //$NON-NLS-1$
      parameters.put("service", "reader"); //$NON-NLS-1$ //$NON-NLS-2$
      parameters.put("source", "RSSOwl.org-RSSOwl-" + Activator.getDefault().getVersion()); //$NON-NLS-1$ //$NON-NLS-2$

      Map<Object, Object> properties = new HashMap<Object, Object>();
      properties.put(IConnectionPropertyConstants.PARAMETERS, parameters);
      properties.put(IConnectionPropertyConstants.POST, Boolean.TRUE);
      properties.put(IConnectionPropertyConstants.CON_TIMEOUT, getConnectionTimeout());

      BufferedReader reader = null;
      try {
        InputStream inS = handler.openStream(uri, monitor, properties);
        reader = new BufferedReader(new InputStreamReader(inS));
        String line;
        while (!monitor.isCanceled() && (line = reader.readLine()) != null) {
          if (line.startsWith(AUTH_IDENTIFIER))
            return line.substring(AUTH_IDENTIFIER.length());
        }
      } finally {
        try {
          if (reader != null)
            reader.close();
        } catch (IOException e) {
          /* Ignore */
        }
      }
    }

    return null;
  }

  /**
   * Returns the header value to authenticate against any Google REST services.
   *
   * @param authToken the authorization token that can be obtained from
   * {@link SyncUtils#getGoogleAuthToken(String, String, boolean, IProgressMonitor)}
   * @return a header value that can be used inside <code>Authorization</code>
   * to get access to Google Services.
   */
  public static String getGoogleAuthorizationHeader(String authToken) {
    return GOOGLE_LOGIN_HEADER_VALUE + authToken;
  }

  /**
   * Obtains the Google API Token to perform REST operations for Google
   * Services.
   *
   * @param email the user account for google
   * @param pw the password for the user account
   * @param monitor an instance of {@link IProgressMonitor} that can be used to
   * cancel the operation and report progress.
   * @return the Google API Token to perform REST operations for Google
   * Services.
   * @throws ConnectionException Checked Exception to be used in case of any
   * Exception.
   */
  public static String getGoogleApiToken(String email, String pw, IProgressMonitor monitor) throws ConnectionException {
    try {

      /* First try to use shared authentication token */
      try {
        return internalGetGoogleApiToken(email, pw, false, monitor);
      } catch (ConnectionException e) {

        /* Rethrow if this exception is not about Authentication issues */
        if (!(e instanceof AuthenticationRequiredException) && !(e instanceof SyncConnectionException))
          throw e;

        /* Second try with up to date authentication token */
        return internalGetGoogleApiToken(email, pw, true, monitor);
      }

    } catch (URISyntaxException e) {
      throw new ConnectionException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    } catch (IOException e) {
      throw new ConnectionException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    }
  }

  private static String internalGetGoogleApiToken(String email, String pw, boolean refresh, IProgressMonitor monitor) throws ConnectionException, IOException, URISyntaxException {
    URI uri = new URI(GOOGLE_API_TOKEN_URL);
    IProtocolHandler handler = Owl.getConnectionService().getHandler(uri);
    if (handler != null) {

      String token = SyncUtils.getGoogleAuthToken(email, pw, refresh, monitor);

      Map<String, String> headers = new HashMap<String, String>();
      headers.put("Authorization", SyncUtils.getGoogleAuthorizationHeader(token)); //$NON-NLS-1$

      Map<Object, Object> properties = new HashMap<Object, Object>();
      properties.put(IConnectionPropertyConstants.HEADERS, headers);
      properties.put(IConnectionPropertyConstants.CON_TIMEOUT, getConnectionTimeout());

      BufferedReader reader = null;
      try {
        InputStream inS = handler.openStream(uri, monitor, properties);
        reader = new BufferedReader(new InputStreamReader(inS));
        String line;
        while (!monitor.isCanceled() && (line = reader.readLine()) != null) {
          return line;
        }
      } finally {
        try {
          if (reader != null)
            reader.close();
        } catch (IOException e) {
          /* Ignore */
        }
      }
    }

    return null;
  }

  /**
   * @param news the {@link INews} to check for synchronization.
   * @return <code>true</code> if the news is under synchronization control and
   * <code>false</code> otherwise.
   */
  public static boolean isSynchronized(INews news) {
    return news != null && news.getParentId() == 0 && isSynchronized(news.getFeedLinkAsText());
  }

  /**
   * @param bm the {@link IBookMark} to check for synchronization.
   * @return <code>true</code> if the bookmark is under synchronization control
   * and <code>false</code> otherwise.
   */
  public static boolean isSynchronized(IBookMark bm) {
    return isSynchronized(bm.getFeedLinkReference().getLinkAsText());
  }

  /**
   * @param link the link to check for synchronization.
   * @return <code>true</code> if the link is under synchronization control and
   * <code>false</code> otherwise.
   */
  public static boolean isSynchronized(String link) {
    return link != null && link.startsWith(READER_HTTP_SCHEME);
  }

  /**
   * @param link the link to check for belonging to google sync services.
   * @return <code>true</code> if the link is from google sync services and
   * <code>false</code> otherwise.
   */
  public static boolean fromGoogle(String link) {
    return isSynchronized(link) || link.startsWith(GOOGLE_HTTP_URL_PREFIX) || link.startsWith(GOOGLE_HTTPS_URL_PREFIX);
  }

  /**
   * @return <code>true</code> if the user has stored credentials for Google
   * Reader synchronization and <code>false</code> otherwise.
   */
  public static boolean hasSyncCredentials() {
    try {
      return Owl.getConnectionService().getAuthCredentials(URI.create(SyncUtils.GOOGLE_LOGIN_URL), null) != null;
    } catch (CredentialsException e) {
      return false;
    }
  }

  private static int getConnectionTimeout() {
    return Owl.isShuttingDown() ? SyncUtils.SHORT_CON_TIMEOUT : SyncUtils.DEFAULT_CON_TIMEOUT;
  }
}
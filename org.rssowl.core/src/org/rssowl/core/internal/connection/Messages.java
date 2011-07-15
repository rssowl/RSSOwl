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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.core.internal.connection.messages"; //$NON-NLS-1$
  public static String ConnectionServiceImpl_ERROR_NO_CREDENTIAL_PROVIDER;
  public static String ConnectionServiceImpl_ERROR_NO_PROTOCOL_HANDLER;
  public static String ConnectionServiceImpl_ERROR_UNKNOWN_PROTOCOL;
  public static String DefaultProtocolHandler_ERROR_AUTHENTICATION_REQUIRED;
  public static String DefaultProtocolHandler_ERROR_FORBIDDEN;
  public static String DefaultProtocolHandler_ERROR_HTTP_STATUS;
  public static String DefaultProtocolHandler_ERROR_HTTP_STATUS_MSG;
  public static String DefaultProtocolHandler_ERROR_PROXY_AUTHENTICATION_REQUIRED;
  public static String DefaultProtocolHandler_ERROR_STREAM_UNAVAILABLE;
  public static String DefaultProtocolHandler_GR_ERROR_ACCOUNT_DELETED;
  public static String DefaultProtocolHandler_GR_ERROR_ACCOUNT_DISABLED;
  public static String DefaultProtocolHandler_GR_ERROR_BAD_AUTH;
  public static String DefaultProtocolHandler_GR_ERROR_CAPTCHA_REQUIRED;
  public static String DefaultProtocolHandler_GR_ERROR_NO_TERMS;
  public static String DefaultProtocolHandler_GR_ERROR_NOT_VERIFIED;
  public static String DefaultProtocolHandler_GR_ERROR_SERVICE_DISABLED;
  public static String DefaultProtocolHandler_GR_ERROR_SERVICE_UNAVAILABLE;
  public static String DefaultProtocolHandler_GR_ERROR_UNKNOWN;
  public static String DefaultProtocolHandler_INFO_NOT_MODIFIED_SINCE;
  public static String ReaderProtocolHandler_GR_ALL_ITEMS;
  public static String ReaderProtocolHandler_GR_NOTES;
  public static String ReaderProtocolHandler_GR_RECOMMENDED_ITEMS;

  public static String ReaderProtocolHandler_GR_SHARED_ITEMS;
  public static String ReaderProtocolHandler_GR_STARRED_ITEMS;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

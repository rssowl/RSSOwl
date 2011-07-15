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

package org.rssowl.core.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.core.util.messages"; //$NON-NLS-1$
  public static String CoreUtils_ALL_IN_N;
  public static String CoreUtils_AND;
  public static String CoreUtils_CONNECTION_TIMEOUT;
  public static String CoreUtils_IN_N;
  public static String CoreUtils_INVALID_FEED;
  public static String CoreUtils_LABEL_ASSIGNED;
  public static String CoreUtils_N_DAY;
  public static String CoreUtils_N_DAYS;
  public static String CoreUtils_N_HOUR;
  public static String CoreUtils_N_HOURS;
  public static String CoreUtils_N_MINUTE;
  public static String CoreUtils_N_MINUTES;
  public static String CoreUtils_NO_HEADLINE;
  public static String CoreUtils_OR;
  public static String CoreUtils_UNABLE_CONNECT;
  public static String CoreUtils_UNABLE_RESOLVE_HOST;
  public static String CoreUtils_UNNABLE_CONNECT;
  public static String CoreUtils_UNSUPPORTED_FORMAT;
  public static String CoreUtils_UNSUPPORTED_PROTOCOL;
  public static String JobQueue_TASK_NAME;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

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

package org.rssowl.ui.internal.notifier;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.notifier.messages"; //$NON-NLS-1$
  public static String NotificationPopup_INCOMING_NEWS;
  public static String NotificationPopup_N_INCOMING;
  public static String NotificationPopup_N_SEARCH_MATCH;
  public static String NotificationPopup_N_SEARCH_MATCHES;
  public static String NotificationPopup_NO_CONTENT;
  public static String NotificationPopup_PAGE_N_OF_M;
  public static String NotificationPopup_RECENT_NEWS;
  public static String SearchNotificationItem_NAME_UNREAD_COUNT;
  public static String SearchNotificationItem_NEW_RESULTS;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

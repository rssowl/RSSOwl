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

package org.rssowl.ui.internal.dialogs.bookmark;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.bookmark.messages"; //$NON-NLS-1$
  public static String BookmarkDefinitionPage_CREATE_BOOKMARK;
  public static String BookmarkDefinitionPage_LOCATION;
  public static String BookmarkDefinitionPage_NAME;
  public static String BookmarkDefinitionPage_UNABLE_LOAD_TITLE;
  public static String CreateBookmarkWizard_BOOKMARK;
  public static String CreateBookmarkWizard_ENTER_VALID_LINK;
  public static String CreateBookmarkWizard_LOADING_TITLE;
  public static String CreateBookmarkWizard_NEW_BOOKMARK;
  public static String FeedDefinitionPage_BOOKMARK_EXISTS;
  public static String FeedDefinitionPage_CREATE_BOOKMARK;
  public static String FeedDefinitionPage_CREATE_FEED;
  public static String FeedDefinitionPage_CREATE_FEED_DIRECT;
  public static String FeedDefinitionPage_CREATE_KEYWORD_FEED;
  public static String FeedDefinitionPage_IMPORT_WIZARD_TIP;
  public static String FeedDefinitionPage_USE_TITLE_OF_FEED;
  public static String KeywordSubscriptionPage_CREATE_BOOKMARK;
  public static String KeywordSubscriptionPage_N_ON_M;
  public static String KeywordSubscriptionPage_SELECT_SEARCH_ENGINE;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

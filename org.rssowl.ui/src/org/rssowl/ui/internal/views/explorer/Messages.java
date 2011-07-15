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

package org.rssowl.ui.internal.views.explorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.views.explorer.messages"; //$NON-NLS-1$
  public static String BookMarkExplorer_ALWAYS_SHOW;
  public static String BookMarkExplorer_BEGIN_WHEN_TYPING;
  public static String BookMarkExplorer_BOOKMARK;
  public static String BookMarkExplorer_COLLAPSE_ALL;
  public static String BookMarkExplorer_CONFIGURE;
  public static String BookMarkExplorer_ERROR_NO_SET_FOUND;
  public static String BookMarkExplorer_FILTER_ELEMENTS;
  public static String BookMarkExplorer_FIND;
  public static String BookMarkExplorer_FOLDER;
  public static String BookMarkExplorer_GROUP_BY_LAST_VISIT;
  public static String BookMarkExplorer_GROUP_BY_POPULARITY;
  public static String BookMarkExplorer_GROUP_BY_STATE;
  public static String BookMarkExplorer_GROUP_BY_TYPE;
  public static String BookMarkExplorer_GROUP_ELEMENTS;
  public static String BookMarkExplorer_LINKING;
  public static String BookMarkExplorer_MANAGE_SETS;
  public static String BookMarkExplorer_NEW;
  public static String BookMarkExplorer_NEWSBIN;
  public static String BookMarkExplorer_NEXT_SET;
  public static String BookMarkExplorer_NO_GROUPING;
  public static String BookMarkExplorer_PREVIOUS_SET;
  public static String BookMarkExplorer_SAVED_SEARCH;
  public static String BookMarkExplorer_SHARING;
  public static String BookMarkExplorer_SHOW_ALL;
  public static String BookMarkExplorer_SHOW_ERROR;
  public static String BookMarkExplorer_SHOW_FAVICONS;
  public static String BookMarkExplorer_SHOW_NEVER_VISITED;
  public static String BookMarkExplorer_SHOW_NEW;
  public static String BookMarkExplorer_SHOW_STICKY;
  public static String BookMarkExplorer_SHOW_UNREAD;
  public static String BookMarkExplorer_SORT_BY_NAME;
  public static String BookMarkGrouping_BOOKMARKS;
  public static String BookMarkGrouping_EARLIER_WEEK;
  public static String BookMarkGrouping_FAILRY_POPULAR;
  public static String BookMarkGrouping_LAST_WEEK;
  public static String BookMarkGrouping_MORE_THAN_WEEK;
  public static String BookMarkGrouping_NEVER;
  public static String BookMarkGrouping_NEW;
  public static String BookMarkGrouping_NEWS_BINS;
  public static String BookMarkGrouping_OTHER;
  public static String BookMarkGrouping_POPULAR;
  public static String BookMarkGrouping_SAVED_SEARCHES;
  public static String BookMarkGrouping_STICKY;
  public static String BookMarkGrouping_TODAY;
  public static String BookMarkGrouping_UNPOPULAR;
  public static String BookMarkGrouping_UNREAD;
  public static String BookMarkGrouping_VERY_POPULAR;
  public static String BookMarkGrouping_YESTERDAY;
  public static String BookMarkLabelProvider_NAME_UNREAD;
  public static String BookMarkSearchbar_CLEAR;
  public static String BookMarkSearchbar_LINK;
  public static String BookMarkSearchbar_NAME;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

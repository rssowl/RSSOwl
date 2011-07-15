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

package org.rssowl.ui.internal.dialogs.properties;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.properties.messages"; //$NON-NLS-1$
  public static String DisplayPropertyPage_DISPLAY_NEWS_CONTENT;
  public static String DisplayPropertyPage_FILTER;
  public static String DisplayPropertyPage_GROUP;
  public static String DisplayPropertyPage_LAYOUT;
  public static String DisplayPropertyPage_LOAD_IMAGES;
  public static String DisplayPropertyPage_LOAD_MEDIA;
  public static String DisplayPropertyPage_ONLY_EMPTY_CONTENT;
  public static String DisplayPropertyPage_OPEN_NEWS_LINK;
  public static String DisplayPropertyPage_USE_LINK_TRANSFORMER;
  public static String EntityPropertyDialog_PROPERTIES;
  public static String EntityPropertyDialog_PROPERTIES_FOR_N;
  public static String GeneralPropertyPage_AUTO_UPDATE_FEED_STARTUP;
  public static String GeneralPropertyPage_AUTO_UPDATE_FEEDS_STARTUP;
  public static String GeneralPropertyPage_CHANGE_LINK_WARNING;
  public static String GeneralPropertyPage_DAYS;
  public static String GeneralPropertyPage_DISPLAY_BIN_ONSTARTUP;
  public static String GeneralPropertyPage_DISPLAY_BINS_ONSTARTUP;
  public static String GeneralPropertyPage_DISPLAY_ELEMENTS_ONSTARTUP;
  public static String GeneralPropertyPage_DISPLAY_FEED_ONSTARTUP;
  public static String GeneralPropertyPage_DISPLAY_FEEDS_ONSTARTUP;
  public static String GeneralPropertyPage_ENTER_LINK;
  public static String GeneralPropertyPage_ENTER_NAME_BIN;
  public static String GeneralPropertyPage_ENTER_NAME_BOOKMARK;
  public static String GeneralPropertyPage_ENTER_NAME_FOLDER;
  public static String GeneralPropertyPage_HOURS;
  public static String GeneralPropertyPage_INVALID_LINK;
  public static String GeneralPropertyPage_LINK;
  public static String GeneralPropertyPage_LOAD_NAME;
  public static String GeneralPropertyPage_LOCATION;
  public static String GeneralPropertyPage_MINUTES;
  public static String GeneralPropertyPage_NAME;
  public static String GeneralPropertyPage_SECONDS;
  public static String GeneralPropertyPage_UPDATE_FEED;
  public static String GeneralPropertyPage_UPDATE_FEEDS;
  public static String GeneralPropertyPage_WARNING;
  public static String InformationPropertyPage_BOOKMARKS;
  public static String InformationPropertyPage_CREATED;
  public static String InformationPropertyPage_DESCRIPTION;
  public static String InformationPropertyPage_FIND_OUT_MORE;
  public static String InformationPropertyPage_FOLDERS;
  public static String InformationPropertyPage_HOMEPAGE;
  public static String InformationPropertyPage_LAST_VISITED;
  public static String InformationPropertyPage_LOAD_FAILED_REASON;
  public static String InformationPropertyPage_LOAD_FAILED_REASON_SYNCED;
  public static String InformationPropertyPage_LOAD_FAILED_UNKNOWN;
  public static String InformationPropertyPage_LOAD_FAILED_UNKNOWN_SYNCED;
  public static String InformationPropertyPage_LOADED_OK;
  public static String InformationPropertyPage_N_NEW;
  public static String InformationPropertyPage_N_NEW_UNREAD;
  public static String InformationPropertyPage_N_NEW_UNREAD_UPDATED;
  public static String InformationPropertyPage_N_NEW_UPDATED;
  public static String InformationPropertyPage_N_UNREAD;
  public static String InformationPropertyPage_N_UNREAD_UPDATED;
  public static String InformationPropertyPage_N_UPDATED;
  public static String InformationPropertyPage_NEVER;
  public static String InformationPropertyPage_NEWS_COUNT;
  public static String InformationPropertyPage_NEWSBINS;
  public static String InformationPropertyPage_NONE;
  public static String InformationPropertyPage_NOT_LOADED;
  public static String InformationPropertyPage_NOT_SYNCED;
  public static String InformationPropertyPage_SEARCHES;
  public static String InformationPropertyPage_STATUS;
  public static String InformationPropertyPage_SYNCED_OK;
  public static String ReadingPropertyPage_MARK_READ_AFTER;
  public static String ReadingPropertyPage_MARK_READ_ON_CLOSE;
  public static String ReadingPropertyPage_MARK_READ_ON_MINIMIZE;
  public static String ReadingPropertyPage_MARK_READ_ON_SCROLLING;
  public static String ReadingPropertyPage_MARK_READ_ON_SWITCH;
  public static String ReadingPropertyPage_SECONDS;
  public static String RetentionPropertyPage_CLEANUP_INFO;
  public static String RetentionPropertyPage_CLEANUP_NOTE;
  public static String RetentionPropertyPage_DELETE_READ;
  public static String RetentionPropertyPage_DELETE_UNREAD;
  public static String RetentionPropertyPage_MAX_AGE;
  public static String RetentionPropertyPage_MAX_AGE_SYNCHRONIZED;
  public static String RetentionPropertyPage_MAX_NUMBER;
  public static String RetentionPropertyPage_MAX_NUMBER_SYNCHRONIZED;
  public static String RetentionPropertyPage_NEVER_DELETE_LABELED;
  public static String RetentionPropertyPage_PERFORMING_CLEANUP;
  public static String SearchMarkPropertyPage_ADD_TO_ALL;
  public static String SearchMarkPropertyPage_ALL_NEWS;
  public static String SearchMarkPropertyPage_DEFINE_SEARCH;
  public static String SearchMarkPropertyPage_LOCATION;
  public static String SearchMarkPropertyPage_LOCATION_WARNING;
  public static String SearchMarkPropertyPage_MATCH_ALL;
  public static String SearchMarkPropertyPage_MATCH_ANY;
  public static String SearchMarkPropertyPage_NAME;
  public static String SearchMarkPropertyPage_NAME_FROM_CONDITION;
  public static String SearchMarkPropertyPage_SEARCH_IN;
  public static String SearchMarkPropertyPage_SEARCH_NAME;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

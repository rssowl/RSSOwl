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

package org.rssowl.ui.internal.search;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.search.messages"; //$NON-NLS-1$
  public static String LocationControl_CHOOSE_BINS;
  public static String LocationControl_CHOOSE_BINS_LABEL;
  public static String LocationControl_CHOOSE_BINS_MSG;
  public static String LocationControl_CHOOSE_LOCATION;
  public static String LocationControl_CHOOSE_LOCATION_LABEL;
  public static String LocationControl_CHOOSE_LOCATION_MSG;
  public static String LocationControl_DESELECT_ALL;
  public static String LocationControl_FILTER_BINS;
  public static String LocationControl_FILTER_LOCATIONS;
  public static String LocationControl_NEW_NEWSBIN;
  public static String LocationControl_SELECT_ALL;
  public static String SearchConditionItem_CONTAINS_ANY;
  public static String SearchConditionItem_CONTENT_ASSIST_INFO;
  public static String SearchConditionItem_DAYS;
  public static String SearchConditionItem_ERROR_PHRASE_AND_WILDCARD_SEARCH;
  public static String SearchConditionItem_FALSE;
  public static String SearchConditionItem_HOURS;
  public static String SearchConditionItem_MINUTES;
  public static String SearchConditionItem_SEARCH_HELP;
  public static String SearchConditionItem_SEARCH_VALUE_FIELD;
  public static String SearchConditionItem_TRUE;
  public static String SearchConditionItem_WARNING_PHRASE_SEARCH_UNSUPPORTED;
  public static String SearchConditionItem_WARNING_WILDCARD_SPECIAL_CHAR_SEARCH;
  public static String SearchConditionList_ADD_CONDITION;
  public static String SearchConditionList_AGE;
  public static String SearchConditionList_ATTACHMENT;
  public static String SearchConditionList_AUTHOR;
  public static String SearchConditionList_CATEGORY;
  public static String SearchConditionList_DATE;
  public static String SearchConditionList_DATE_MODIFIED;
  public static String SearchConditionList_DATE_PUBLISHED;
  public static String SearchConditionList_DATE_RECEIVED;
  public static String SearchConditionList_DELETE_CONDITION;
  public static String SearchConditionList_DESCRIPTION;
  public static String SearchConditionList_ENTIRE_NEWS;
  public static String SearchConditionList_FEED;
  public static String SearchConditionList_HAS_ATTACHMENTS;
  public static String SearchConditionList_IS_STICKY;
  public static String SearchConditionList_LABEL;
  public static String SearchConditionList_LINK;
  public static String SearchConditionList_LOCATION;
  public static String SearchConditionList_OTHER;
  public static String SearchConditionList_SOURCE;
  public static String SearchConditionList_STATE;
  public static String SearchConditionList_TITLE;
  public static String StateConditionControl_NEW;
  public static String StateConditionControl_NEW_HINT;
  public static String StateConditionControl_NEW_INFO;
  public static String StateConditionControl_READ;
  public static String StateConditionControl_READ_INFO;
  public static String StateConditionControl_UNREAD;
  public static String StateConditionControl_UNREAD_HINT;
  public static String StateConditionControl_UNREAD_INFO;
  public static String StateConditionControl_UPDATED;
  public static String StateConditionControl_UPDATED_INFO;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

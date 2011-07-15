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

package org.rssowl.ui.internal.dialogs.cleanup;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.cleanup.messages"; //$NON-NLS-1$
  public static String BookMarkTask_DELETE_N;
  public static String CleanUpModel_DELETE_BY_AGE;
  public static String CleanUpModel_DELETE_BY_COUNT;
  public static String CleanUpModel_DELETE_BY_UPDATE;
  public static String CleanUpModel_DELETE_BY_VISIT;
  public static String CleanUpModel_DELETE_CON_ERROR;
  public static String CleanUpModel_DELETE_DUPLICATES;
  public static String CleanUpModel_DELETE_UNSUBSCRIBED_FEEDS;
  public static String CleanUpModel_READ_NEWS;
  public static String CleanUpModel_RECOMMENDED_OPS;
  public static String CleanUpOptionsPage_CHOOSE_OPS;
  public static String CleanUpOptionsPage_CLEANUP_BOOKMARKS;
  public static String CleanUpOptionsPage_CLEANUP_INFO;
  public static String CleanUpOptionsPage_CLEANUP_NEWS;
  public static String CleanUpOptionsPage_DAYS;
  public static String CleanUpOptionsPage_DELETE_BY_AGE;
  public static String CleanUpOptionsPage_DELETE_BY_COUNT;
  public static String CleanUpOptionsPage_DELETE_BY_UPDATE;
  public static String CleanUpOptionsPage_DELETE_CON_ERROR;
  public static String CleanUpOptionsPage_DELETE_DUPLICATES;
  public static String CleanUpOptionsPage_DELETE_NEWS_BY_AGE;
  public static String CleanUpOptionsPage_DELETE_READ;
  public static String CleanUpOptionsPage_DELETE_UNSUBSCRIBED_FEEDS;
  public static String CleanUpOptionsPage_DONT_DELETE_LABELED;
  public static String CleanUpOptionsPage_DONT_DELETE_UNREAD;
  public static String CleanUpSummaryPage_DESELECT_ALL;
  public static String CleanUpSummaryPage_DISPLAY;
  public static String CleanUpSummaryPage_DISPLAY_FEED;
  public static String CleanUpSummaryPage_REVIEW_OPS;
  public static String CleanUpSummaryPage_SELECT_ALL;
  public static String CleanUpSummaryPage_WAIT_GENERATE_PREVIEW;
  public static String CleanUpWizard_CHOOSE_BOOKMARKS;
  public static String CleanUpWizard_CLEAN_UP;
  public static String CleanUpWizard_CLEANUP_OPS;
  public static String CleanUpWizard_CONFIRM_DELETE;
  public static String CleanUpWizard_N_FEEDS;
  public static String CleanUpWizard_N_NEWS;
  public static String CleanUpWizard_N_SEARCHES;
  public static String CleanUpWizard_NO_UNDO;
  public static String CleanUpWizard_ONE_FEED;
  public static String CleanUpWizard_ONE_SEARCH;
  public static String CleanUpWizard_PLEASE_CONFIRM_DELETE;
  public static String CleanUpWizard_RESTART_RSSOWL;
  public static String CleanUpWizard_RESTART_TO_CLEANUP;
  public static String CleanUpWizard_SUMMARY;
  public static String CleanUpWizard_WAIT_CLEANUP;
  public static String DefragDatabaseTask_IMPROVE_DB_PERFORMANCE;
  public static String DeleteOrphanedSearchMarkTask_DELETE_ORPHANED_SEARCH_MARKS;
  public static String DeleteOrphanedSearchMarkTask_DELETED_ORPHANED_SEARCH_MARK;
  public static String DisableOrphanedNewsFiltersTask_DISABLE_ORPHANED_NEWS_FILTER;
  public static String DisableOrphanedNewsFiltersTask_DISABLE_ORPHANED_NEWS_FILTERS;
  public static String FeedSelectionPage_CHOOSE_BOOKMARKS;
  public static String FeedSelectionPage_DESELECT_ALL;
  public static String FeedSelectionPage_DISPLAY;
  public static String FeedSelectionPage_DISPLAY_FEEDS;
  public static String FeedSelectionPage_SELECT_ALL;
  public static String NewsTask_DELETE_N_NEWS_FROM_M;
  public static String OptimizeSearchTask_IMPROVE_PERFORMANCE;
  public static String ReindexTask_REPAIR_INDEX;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

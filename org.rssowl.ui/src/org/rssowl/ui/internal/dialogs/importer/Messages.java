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

package org.rssowl.ui.internal.dialogs.importer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.importer.messages"; //$NON-NLS-1$
  public static String ImportElementsPage_BOOKMARKS;
  public static String ImportElementsPage_CANCEL_SEARCH;
  public static String ImportElementsPage_CHOOSE_ELEMENTS;
  public static String ImportElementsPage_CHOOSE_ELEMENTS_MESSAGE;
  public static String ImportElementsPage_CONNECTING;
  public static String ImportElementsPage_DESELECT_ALL;
  public static String ImportElementsPage_ERROR_IMPORT_GR;
  public static String ImportElementsPage_ERROR_IMPORT_GR_DETAILS;
  public static String ImportElementsPage_FEED_TITLE;
  public static String ImportElementsPage_FETCHING_RESULTS;
  public static String ImportElementsPage_FLATTEN;
  public static String ImportElementsPage_GR_NOTES;
  public static String ImportElementsPage_GR_RECOMMENDED_ITEMS;
  public static String ImportElementsPage_GR_SHARED_ITEMS;
  public static String ImportElementsPage_HIDDEN_ELEMENTS_INFO;
  public static String ImportElementsPage_HIDE_EXISTING;
  public static String ImportElementsPage_IMPORT_GOOGLE_READER;
  public static String ImportElementsPage_INVALID_OPML_WARNING;
  public static String ImportElementsPage_MISSING_ACCOUNT;
  public static String ImportElementsPage_N_RESULTS;
  public static String ImportElementsPage_NO_FEEDS_FOUND;
  public static String ImportElementsPage_PREVIEW;
  public static String ImportElementsPage_SEARCHING_FOR_FEEDS;
  public static String ImportElementsPage_SELECT_ALL;
  public static String ImportElementsPage_SHOW_PREVIEW;
  public static String ImportElementsPage_SINGLE_RESULT;
  public static String ImportElementsPage_UNABLE_TO_IMPORT;
  public static String ImportElementsPage_UNABLE_TO_IMPORT_REASON;
  public static String ImportOptionsPage_IMPORT_FILTERS;
  public static String ImportOptionsPage_IMPORT_LABELS;
  public static String ImportOptionsPage_IMPORT_N_FILTERS;
  public static String ImportOptionsPage_IMPORT_N_LABELS;
  public static String ImportOptionsPage_IMPORT_OPTIONS;
  public static String ImportOptionsPage_IMPORT_PREFERENCES_UNAVAILABLE;
  public static String ImportOptionsPage_IMPORT_PREFRENCES;
  public static String ImportOptionsPage_LABELS_INFO;
  public static String ImportOptionsPage_OPTIONS_INFO;
  public static String ImportOptionsPage_SELECT_OPTIONS;
  public static String ImportSourcePage_BROWSE;
  public static String ImportSourcePage_CHOOSE_EXISTING_FILE;
  public static String ImportSourcePage_CHOOSE_FILE;
  public static String ImportSourcePage_CHOOSE_SOURCE;
  public static String ImportSourcePage_CHOOSE_SOURCE_FOR_IMPORT;
  public static String ImportSourcePage_IMPORT_BY_KEYWORD;
  public static String ImportSourcePage_IMPORT_FROM_FILE_OR_WEBSITE;
  public static String ImportSourcePage_IMPORT_GOOGLE_READER;
  public static String ImportSourcePage_IMPORT_RECOMMENDED;
  public static String ImportSourcePage_MATCH_LANGUAGE;
  public static String ImportSourcePage_MATCH_LANGUAGE_N;
  public static String ImportSourcePage_NO_MPORT;
  public static String ImportSourcePage_WELCOME;
  public static String ImportSourcePage_WELCOME_INFO;
  public static String ImportTargetPage_BOOKMARK_EXISTS;
  public static String ImportTargetPage_CHOOSE_TARGET;
  public static String ImportTargetPage_CHOOSE_TARGET_FOLDER;
  public static String ImportTargetPage_DIRECT_IMPORT;
  public static String ImportTargetPage_IMPORT_TO_EXISTING;
  public static String ImportTargetPage_IMPORT_TO_NEW_SET;
  public static String ImportTargetPage_NAME;
  public static String ImportWizard_ATTENTION;
  public static String ImportWizard_IMPORT;
  public static String ImportWizard_LABELED_NEWS;
  public static String ImportWizard_NEW_UPDATED_NEWS;
  public static String ImportWizard_NEWS_WITH_ATTACHMENTS;
  public static String ImportWizard_PREFERENCE_OVERWRITE;
  public static String ImportWizard_TODAYS_NEWS;
  public static String ImportWizard_RESTART_RSSOWL;
  public static String ImportWizard_RESTART_RSSOWL_INFO;
  public static String ImportWizard_STICKY_NEWS;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

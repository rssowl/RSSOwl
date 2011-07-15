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

package org.rssowl.ui.internal.dialogs.welcome;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.welcome.messages"; //$NON-NLS-1$
  public static String TutorialPage_CONFIGURE_TITLE;
  public static String TutorialPage_CONTACT;
  public static String TutorialPage_FAQ;
  public static String TutorialPage_FINISH;
  public static String TutorialPage_FINISH_TEXT;
  public static String TutorialPage_FINISH_TITLE;
  public static String TutorialPage_FORUMS;
  public static String TutorialPage_IMPORT_EXPORT;
  public static String TutorialPage_IMPORT_EXPORT_TEXT;
  public static String TutorialPage_IMPORT_EXPORT_TITLE;
  public static String TutorialPage_INTRO;
  public static String TutorialPage_LAYOUT_TEXT;
  public static String TutorialPage_NEWS;
  public static String TutorialPage_NEWS_BIN_TEXT;
  public static String TutorialPage_NEWS_BINS;
  public static String TutorialPage_NEWS_FILTER_POWER;
  public static String TutorialPage_NEWS_FILTER_TEXT;
  public static String TutorialPage_NEWS_FILTERS;
  public static String TutorialPage_NEWS_TEXT;
  public static String TutorialPage_NOTIFICATIONS;
  public static String TutorialPage_NOTIFIER_TEXT;
  public static String TutorialPage_NOTIFIER_TITLE;
  public static String TutorialPage_OVERVIEW;
  public static String TutorialPage_OVERVIEW_TITLE;
  public static String TutorialPage_PREFERENCES;
  public static String TutorialPage_PREFERENCES_TEXT;
  public static String TutorialPage_REPORT_BUGS;
  public static String TutorialPage_SAVED_SEARCHES;
  public static String TutorialPage_SAVED_SEARCHES_TEXT;
  public static String TutorialPage_SAVING_SEARCH_RESULTS;
  public static String TutorialPage_SHARE_FEEDS_TITLE;
  public static String TutorialPage_SHARING;
  public static String TutorialPage_SHARING_TEXT;
  public static String TutorialPage_STORING_NEWS_BINS;
  public static String TutorialPage_SYNCHRONIZATION;
  public static String TutorialPage_SYNCHRONIZATION_TEXT;
  public static String TutorialPage_SYNCHRONIZATION_TITLE;
  public static String TutorialPage_TIPS_AND_TRICKS;
  public static String TutorialPage_TIPS_TEXT;
  public static String TutorialPage_TIPS_TITLE;
  public static String TutorialPage_WEBSITE;
  public static String TutorialPage_WELCOME_TEXT;
  public static String TutorialPage_WELCOME_TUTORIAL;
  public static String TutorialPage_WORKING_WITH_NEWS;
  public static String TutorialWizard_RSSOWL_TUTORIAL;
  public static String WelcomeWizard_WELCOME;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

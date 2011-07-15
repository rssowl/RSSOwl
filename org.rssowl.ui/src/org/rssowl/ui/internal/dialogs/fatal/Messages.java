/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.dialogs.fatal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.fatal.messages"; //$NON-NLS-1$
  public static String CleanProfilePage_CAUTION;
  public static String CleanProfilePage_CAUTION_TEXT_RESTORE;
  public static String CleanProfilePage_CAUTION_TEXT_START_OVER;
  public static String CleanProfilePage_CLEAN_TEXT_OK;
  public static String CleanProfilePage_CLEAN_TEXT_OPML_OK;
  public static String CleanProfilePage_CONFIRM_TEXT;
  public static String CleanProfilePage_NO_BACKUPS;
  public static String CleanProfilePage_OPML_BACKUP_INFO;
  public static String CleanProfilePage_RESTORE_SUBSCRIPTIONS_SETTINGS;
  public static String CleanProfilePage_RESTORING_SUBSCRIPTIONS_SETTINGS;
  public static String CleanProfilePage_RSSOWL_CRASH;
  public static String CleanProfilePage_START_OVER;
  public static String CleanProfilePage_STARTING_OVER;
  public static String ErrorInfoPage_COPY;
  public static String ErrorInfoPage_ERROR_DETAILS;
  public static String ErrorInfoPage_FURTHER_STEPS;
  public static String ErrorInfoPage_GENERAL_ERROR_ADVISE;
  public static String ErrorInfoPage_LET_US_KNOW;
  public static String ErrorInfoPage_LOCKED_ERROR;
  public static String ErrorInfoPage_LOCKED_ERROR_N;
  public static String ErrorInfoPage_LOCKED_PROFILE_ADVISE;
  public static String ErrorInfoPage_NEXT_PAGE_ADVISE;
  public static String ErrorInfoPage_OOM_ERROR;
  public static String ErrorInfoPage_OOM_ERROR_N;
  public static String ErrorInfoPage_RSSOWL_CRASH;
  public static String ErrorInfoPage_SEND_LOGS_ADVISE;
  public static String ErrorInfoPage_STARTUP_ERROR;
  public static String ErrorInfoPage_STARTUP_ERROR_N;
  public static String FatalErrorWizard_CRASH_REPORTER;
  public static String FatalErrorWizard_PROFILE_RECOVERY;
  public static String FatalErrorWizard_RECREATE_SEARCH_INDEX;
  public static String FatalErrorWizard_RESTORE_BACKUP;
  public static String FatalErrorWizard_RESTORE_ERROR;
  public static String FatalErrorWizard_RESTORE_ERROR_N;
  public static String FatalErrorWizard_RESTORE_SUBSCRIPTIONS_SETTINGS;
  public static String FatalErrorWizard_START_OVER;
  public static String FatalErrorWizard_WE_ARE_SORRY;
  public static String RecreateSearchIndexPage_RSSOWL_CRASH;
  public static String RecreateSearchPage_INFORMATION;
  public static String RecreateSearchPage_RECREATING_DETAILS_RESTART;
  public static String RecreateSearchPage_RECREATING_DETAILS_QUIT;
  public static String RecreateSearchPage_RECREATING_INFORMATION;
  public static String RecreateSearchPage_RECREATING_SEARCH_INDEX;
  public static String RestoreBackupPage_BACKUP_INFO_QUIT;
  public static String RestoreBackupPage_BACKUP_INFO_RESTART;
  public static String RestoreBackupPage_BACKUP_LABEL;
  public static String RestoreBackupPage_CAUTION;
  public static String RestoreBackupPage_CHOOSE_BACKUP;
  public static String RestoreBackupPage_CONFIRM_RESTORE;
  public static String RestoreBackupPage_CURRENT_PROFILE;
  public static String RestoreBackupPage_LAST_MODIFIED;
  public static String RestoreBackupPage_RESTORE_TEXT_OK;
  public static String RestoreBackupPage_RESTORE_WARNING;
  public static String RestoreBackupPage_RESTORING_A_BACKUP;
  public static String RestoreBackupPage_RSSOWL_CRASH;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}
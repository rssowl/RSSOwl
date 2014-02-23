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

package org.rssowl.core.internal.persist.service;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.core.internal.persist.service.messages"; //$NON-NLS-1$
  public static String DBManager_CREATING_DB_BACKUP;
  public static String DBManager_DIRECTORY_PERMISSION_ERROR;
  public static String DBManager_DISK_FULL_ERROR;
  public static String DBManager_FILE_PERMISSION_ERROR;
  public static String DBManager_IMPROVING_APP_PERFORMANCE;
  public static String DBManager_OPTIMIZING_DESCRIPTIONS;
  public static String DBManager_OPTIMIZING_CONDITIONAL_GETS;
  public static String DBManager_OPTIMIZING_FOLDERS;
  public static String DBManager_OPTIMIZING_LABELS;
  public static String DBManager_OPTIMIZING_NEWSBINS;
  public static String DBManager_OPTIMIZING_NEWSFEEDS;
  public static String DBManager_OPTIMIZING_NEWSFILTERS;
  public static String DBManager_OPTIMIZING_PREFERENCES;
  public static String DBManager_PROGRESS_WAIT;
  public static String DBManager_RSSOWL_MIGRATION;
  public static String DBManager_UNABLE_TO_OPEN_PROFILE;
  public static String DBManager_WAIT_TASK_COMPLETION;
  public static String DBManager_UPDATING_NEWS_COUNTERS;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

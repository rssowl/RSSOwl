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

class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.cleanup.messages"; //$NON-NLS-1$

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

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

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

package org.rssowl.ui.internal.dialogs.cleanup.operations;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.cleanup.operations.messages"; //$NON-NLS-1$

  public static String CleanUpModel_DELETE_BY_AGE;
  public static String CleanUpModel_DELETE_BY_COUNT;
  public static String CleanUpModel_DELETE_BY_UPDATE;
  public static String CleanUpModel_DELETE_BY_VISIT;
  public static String CleanUpModel_DELETE_CON_ERROR;
  public static String CleanUpModel_DELETE_DUPLICATES;
  public static String CleanUpModel_DELETE_UNSUBSCRIBED_FEEDS;
  public static String CleanUpModel_DELETE_BROKEN_SEARCHES;
  public static String CleanUpModel_READ_NEWS;
  public static String CleanUpModel_RECOMMENDED_DEFRAGMENT;
  public static String CleanUpModel_RECOMMENDED_SEARCH_INDEX;


  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

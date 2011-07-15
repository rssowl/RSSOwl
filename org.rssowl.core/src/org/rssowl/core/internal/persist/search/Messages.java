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

package org.rssowl.core.internal.persist.search;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.core.internal.persist.search.messages"; //$NON-NLS-1$
  public static String Indexer_INDEX_FROM_SHUTDOWN;
  public static String Indexer_SAVE_INDEXER;
  public static String Indexer_UPDATE_SAVED_SEARCHES;
  public static String IndexingTask_INDEXING_FEED;
  public static String ModelSearchImpl_ERROR_SEARCH;
  public static String ModelSearchImpl_ERROR_WILDCARDS;
  public static String ModelSearchImpl_PROGRESS_WAIT;
  public static String ModelSearchImpl_REINDEX_SEARCH_INDEX;
  public static String ModelSearchImpl_CLEANUP_SEARCH_INDEX;
  public static String ModelSearchImpl_WAIT_TASK_COMPLETION;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

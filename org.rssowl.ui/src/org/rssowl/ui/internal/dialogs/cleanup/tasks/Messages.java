
package org.rssowl.ui.internal.dialogs.cleanup.tasks;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.cleanup.tasks.messages"; //$NON-NLS-1$

  public static String TASK_LABEL_DATABASE_DEFRAGMENT;
  public static String TASK_LABEL_SEARCH_IMPROVE_PERFORMANCE;
  public static String TASK_LABEL_SEARCH_CLEANUP;
  public static String TASK_LABEL_SEARCH_REINDEX;
  public static String TASK_LABEL_DELETE_ORPHANED_SEARCH_MARK;
  public static String TASK_LABEL_DELETE_ORPHANED_SEARCH_MARKS;
  public static String TASK_LABEL_DISABLE_ORPHANED_NEWS_FILTER;
  public static String TASK_LABEL_DISABLE_ORPHANED_NEWS_FILTERS;
  public static String TASK_LABEL_DELETE_N_NEWS_FROM_M;
  public static String TASK_LABEL_DELETE_BOOKMARK;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

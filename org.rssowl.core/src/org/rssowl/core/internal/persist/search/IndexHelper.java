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

package org.rssowl.core.internal.persist.search;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.service.Messages;
import org.rssowl.core.internal.persist.service.ProfileFileManager;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.util.LongOperationMonitor;

import java.io.File;

public class IndexHelper {

  private static final String CLEANUP_INDEX_MARKER = "cleanupindex"; //$NON-NLS-1$
  private static final String REINDEX_MARKER = "reindex"; //$NON-NLS-1$
  private static final String REINDEX_RUNNING_MARKER = "reindexmarker"; //$NON-NLS-1$

  // TODO: remove since it's never used
//  private final IDBManager fDBManager;

  public IndexHelper() {
//    this.fDBManager = fDBManager;
  }

  public static void processIndex(LongOperationMonitor progressMonitor) {

    /* Log previously failing Reindexing */
    try {
      if (getReindexMarkerFile().exists()) {
        Activator.safeLogInfo("Detected a Search Re-Indexing that did not complete"); //$NON-NLS-1$
        ProfileFileManager.safeDelete(getReindexMarkerFile());
      }
    } catch (Exception e) {
      /* Ignore */
    }

    /* Re-Index Search Index if necessary */
    String monitorText = Messages.DBManager_PROGRESS_WAIT;
    int REINDEX_TOTAL_WORK = 10000000;
    SubMonitor subMonitor = SubMonitor.convert(progressMonitor, monitorText, REINDEX_TOTAL_WORK);
    boolean reindexed = false;
    {
      boolean shouldReindex = shouldReindex();
      if (shouldReindex) {
        progressMonitor.beginLongOperation(false);
        subMonitor = SubMonitor.convert(progressMonitor, Messages.DBManager_PROGRESS_WAIT, 20);
      }

      IModelSearch modelSearch = InternalOwl.getDefault().getPersistenceService().getModelSearch();
      if (!progressMonitor.isCanceled() && shouldReindex) {

        /* Reindex */
        if (shouldReindex && !progressMonitor.isCanceled()) {
          Activator.safeLogInfo("Start: Search Re-Indexing"); //$NON-NLS-1$

          File marker = getReindexMarkerFile();
          File reIndexFile = getReIndexFile();
          try {

            /* Create Marker that Reindexing is Performed */
            if (!marker.exists()) {
              ProfileFileManager.safeCreate(marker);
            }

            /* Reindex Search Index */
            reindexed = true;
            modelSearch.reindexAll(subMonitor != null ? subMonitor.newChild(20) : new NullProgressMonitor());

            /*
             * Make sure to delete the reindex file if existing only after the
             * operation has completed without issues to ensure that upon next
             * start the reindexing is started again if it failed prior.
             */
            if (reIndexFile.exists()) {
              ProfileFileManager.safeDelete(reIndexFile);
            }
          } finally {
            ProfileFileManager.safeDelete(marker);
          }

          /* Log Status */
          Activator.safeLogInfo("Finished: Search Re-Indexing"); //$NON-NLS-1$
        }
      }
    }

    /* Clean-Up Search Index if necessary */
    File cleanUpIndexFile = getCleanUpIndexFile();
    if (!reindexed && cleanUpIndexFile.exists() && !progressMonitor.isCanceled()) {

      /* Report Progress */
      progressMonitor.beginLongOperation(false);
      subMonitor = SubMonitor.convert(progressMonitor, Messages.DBManager_PROGRESS_WAIT, 20);

      /* Startup Model Search to perform operation */
      IModelSearch modelSearch = InternalOwl.getDefault().getPersistenceService().getModelSearch();
      modelSearch.startup();

      /* Trigger Clean Up */
      if (!progressMonitor.isCanceled()) {
        Activator.safeLogInfo("Start: Search Clean-Up"); //$NON-NLS-1$
        modelSearch.cleanUp(subMonitor != null ? subMonitor.newChild(20) : new NullProgressMonitor());

        /* Delete the Marker */
        ProfileFileManager.safeDelete(cleanUpIndexFile);

        /* Log Status */
        Activator.safeLogInfo("Finished: Search Clean-Up"); //$NON-NLS-1$
      }
    }
  }

  private static boolean shouldReindex() {

    if (getReIndexFile().exists()) { //Need to set system property as model search relies on it
      System.setProperty("rssowl.reindex", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      return true;
    }

    /* Finally ask System Property */
    return Boolean.getBoolean("rssowl.reindex"); //$NON-NLS-1$
  }

  /**
   * @return the File indicating whether reindexing should be run or not.
   */
  public static File getReIndexFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, REINDEX_MARKER);
  }

  /**
   * @return the File indicating whether the reindexing of news terminated
   * normally or not.
   */
  public static File getReindexMarkerFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, REINDEX_RUNNING_MARKER);
  }

  public static void reindexOnNextStartUp() {
    File reIndexFile = getReIndexFile();
    if (!reIndexFile.exists()) {
      ProfileFileManager.safeCreate(reIndexFile);
    }
  }

  /**
   * @return the File indicating whether cleanup index should be run or not.
   */
  public static File getCleanUpIndexFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, CLEANUP_INDEX_MARKER);
  }

  public static void cleanUpIndexOnNextStartUp() {
    File cleanUpIndexFile = getCleanUpIndexFile();
    if (!cleanUpIndexFile.exists()) {
      ProfileFileManager.safeCreate(cleanUpIndexFile);
    }
  }

}

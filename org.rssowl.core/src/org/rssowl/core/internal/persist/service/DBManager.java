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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.AbstractEntity;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.ConditionalGet;
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.Label;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.NewsBin;
import org.rssowl.core.internal.persist.Preference;
import org.rssowl.core.internal.persist.SearchFilter;
import org.rssowl.core.internal.persist.migration.MigrationResult;
import org.rssowl.core.internal.persist.migration.Migrations;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.NewsCounterItem;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.DiskFullException;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.persist.service.InsufficientFilePermissionException;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.persist.service.ProfileLockedException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.LongOperationMonitor;
import org.rssowl.core.util.Pair;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.config.ObjectClass;
import com.db4o.config.ObjectField;
import com.db4o.config.QueryEvaluationMode;
import com.db4o.diagnostic.Diagnostic;
import com.db4o.diagnostic.DiagnosticListener;
import com.db4o.diagnostic.NativeQueryNotOptimized;
import com.db4o.ext.DatabaseFileLockedException;
import com.db4o.query.Query;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The central class to configure the underlying database of RSSOwl.
 */
public class DBManager {
  private static final String FORMAT_FILE_NAME = "format2"; //$NON-NLS-1$
  private static final String DB_NAME = "rssowl.db"; //$NON-NLS-1$
  private static final String DB_RESTORE_NAME = "rssowl.db.restore"; //$NON-NLS-1$
  private static DBManager fInstance;

  /* Some constants used when defragmenting to a larger block size */
  private static final long LARGE_DB_STARTING_SIZE = 1610612736; //1.5 GB in Bytes
  private static final int LARGE_DB_BLOCK_SIZE = 8;

  /* Migration Related */
  private static final boolean ENABLE_MIGRATION = false; //Turned off as of RSSOwl 2.1

  /* Files created and used in profile directory */
  private static final String TMP_BACKUP_NAME = "tmp.bak"; //$NON-NLS-1$
  private static final String DEFRAGMENT_MARKER = "defragment"; //$NON-NLS-1$
  private static final String LARGE_BLOCK_SIZE_MARKER = "largeblocksize"; //$NON-NLS-1$
  private static final String ONLINE_RUNNING_BACKUP_MARKER = "onlinebakmarker"; //$NON-NLS-1$
  private static final String CLEANUP_INDEX_MARKER = "cleanupindex"; //$NON-NLS-1$
  private static final String REINDEX_MARKER = "reindex"; //$NON-NLS-1$
  private static final String REINDEX_RUNNING_MARKER = "reindexmarker"; //$NON-NLS-1$

  /* Backup Settings */
  private static final boolean PERFORM_SCHEDULED_BACKUPS = false; //Disabled in favor of online backups
  private static final int MAX_OFFLINE_BACKUPS_COUNT = 1; //Only used for backups from defragment
  private static final int MAX_ONLINE_BACKUPS_COUNT = 1; //Will keep 1 Current + 1 Weekly
  private static final int MAX_ONLINE_BACKUP_AGE = 1000 * 60 * 60 * 24 * 7; //7 Days
  private static final String RESTORE_BACKUP_NAME = ".restorebak"; //$NON-NLS-1$
  private static final String ONLINE_BACKUP_NAME = ".onlinebak"; //$NON-NLS-1$
  private static final String OFFLINE_BACKUP_NAME = ".backup"; //$NON-NLS-1$
  private static final int OFFLINE_BACKUP_INTERVAL = 1000 * 60 * 60 * 24 * 7; //7 Days
  private static final int ONLINE_BACKUP_SCHEDULE_INTERVAL = 1000 * 60 * 5; //5 Minutes
  private static final int ONLINE_BACKUP_DELAY_THRESHOLD = 1000 * 60 * 30; //30 Minutes
  private static final int ONLINE_BACKUP_SHORT_INTERVAL = 1000 * 60 * 55; //55 Minutes
  private static final int ONLINE_BACKUP_LONG_INTERVAL = 1000 * 60 * 60 * 10; //10 Hours

  /* Defrag Tasks Work Ticks */
  private static final int DEFRAG_TOTAL_WORK = 10000000; //100% (but don't fill to 100% to leave room for backup)
  private static final int DEFRAG_SUB_WORK_LABELS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_FOLDERS = 500000; //5%
  private static final int DEFRAG_SUB_WORK_BINS = 1000000; //10%
  private static final int DEFRAG_SUB_WORK_FEEDS = 3000000; //30%
  private static final int DEFRAG_SUB_WORK_DESCRIPTIONS = 3000000; //30%
  private static final int DEFRAG_SUB_WORK_PREFERENCES = 100000; //1%
  private static final int DEFRAG_SUB_WORK_FILTERS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_CONDITIONAL_GET = 100000; //1%
  private static final int DEFRAG_SUB_WORK_COUNTERS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_EVENTS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_COMMITT_DESTINATION = 300000; //3%
  private static final int DEFRAG_SUB_WORK_CLOSE_DESTINATION = 100000; //1%
  private static final int DEFRAG_SUB_WORK_CLOSE_SOURCE = 100000; //1%
  private static final int DEFRAG_SUB_WORK_FINISH = 100000; //1%

  private ObjectContainer fObjectContainer;
  private final AtomicLong fNextOnlineBackup = new AtomicLong();
  private final ReadWriteLock fLock = new ReentrantReadWriteLock();
  private final List<DatabaseListener> fEntityStoreListeners = new CopyOnWriteArrayList<DatabaseListener>();

  /**
   * @return The Singleton Instance.
   */
  public static DBManager getDefault() {
    if (fInstance == null)
      fInstance = new DBManager();

    return fInstance;
  }

  /**
   * Load and initialize the contributed DataBase.
   *
   * @param monitor the {@link LongOperationMonitor} is used to react on
   * cancellation and report accurate startup progress.
   * @param emergency if <code>true</code> indicates this startup method is
   * called from an emergency situation like restoring a backup.
   * @param forRestore if <code>true</code> will open the restore DB as profile
   * and <code>false</code> to open the default profile location.
   * @throws PersistenceException In case of an error while initializing and
   * loading the contributed DataBase.
   */
  public void startup(LongOperationMonitor monitor, boolean emergency, boolean forRestore) throws PersistenceException {

    /* Trigger Singleton EventManager */
    EventManager.getInstance();

    /* Handle Restore Case */
    if (!forRestore) {
      File restoreDBFile = new File(getDBRestoreFilePath());
      if (restoreDBFile.exists()) {
        Activator.safeLogInfo("Start: Restoring a Backup (renaming rssowl.db.restore to rssowl.db"); //$NON-NLS-1$

        /* Backup and Delete current profile */
        backupAndDeleteProfile();

        /* Atomic Rename Restore to Profile */
        DBHelper.rename(restoreDBFile, new File(getDBFilePath()));

        Activator.safeLogInfo("End: Restoring a Backup (renaming rssowl.db.restore to rssowl.db"); //$NON-NLS-1$
      }
    }

    /* Create Database */
    createDatabase(monitor, emergency, forRestore);
  }

  @SuppressWarnings("unused")
  private void createDatabase(LongOperationMonitor progressMonitor, boolean emergency, boolean forRestore) throws PersistenceException {

    /* Assert File Permissions */
    checkDirPermissions();

    SubMonitor subMonitor = null;
    MigrationResult migrationResult = new MigrationResult(false, false, false);
    try {

      /* Migration and Defragment only apply to non-emergency situations */
      if (!emergency) {

        /* Check for Migration */
        int workspaceVersion = getWorkspaceFormatVersion();

        /* Log previously failing Online Backup */
        try {
          if (getOnlineBackupMarkerFile().exists()) {
            Activator.safeLogInfo("Detected an Online Backup that did not complete"); //$NON-NLS-1$
            safeDelete(getOnlineBackupMarkerFile());
          }
        } catch (Exception e) {
          /* Ignore */
        }

        /* Log previously failing Reindexing */
        try {
          if (getReindexMarkerFile().exists()) {
            Activator.safeLogInfo("Detected a Search Re-Indexing that did not complete"); //$NON-NLS-1$
            safeDelete(getReindexMarkerFile());
          }
        } catch (Exception e) {
          /* Ignore */
        }

        /* Perform Migration if necessary */
        if (ENABLE_MIGRATION && workspaceVersion != getCurrentFormatVersion()) {
          progressMonitor.beginLongOperation(false);
          subMonitor = SubMonitor.convert(progressMonitor, Messages.DBManager_RSSOWL_MIGRATION, 100);
          migrationResult = migrate(workspaceVersion, getCurrentFormatVersion(), subMonitor.newChild(70));
        }

        /* Perform Defrag if necessary */
        if (!defragmentIfNecessary(progressMonitor, subMonitor)) {

          /* Defragment */
          if (migrationResult.isDefragmentDatabase())
            defragment(false, progressMonitor, subMonitor);

          /*
           * We only run the time-based back-up if a defragment has not taken
           * place because we always back-up during defragment.
           */
          else if (PERFORM_SCHEDULED_BACKUPS)
            scheduledBackup(progressMonitor);
        }
      }

      /* Open the DB */
      Configuration config = createConfiguration(false);
      createObjectContainer(config, forRestore);

      /* Notify Listeners that DB is opened */
      fireDatabaseEvent(new DatabaseEvent(fObjectContainer, fLock), true);

      /*
       * Model Search Reindex or Cleanup only applies to non-emergency
       * situations
       */
      if (!emergency) {

        /* Re-Index Search Index if necessary */
        boolean reindexed = false;
        {
          boolean shouldReindex = shouldReindex(migrationResult);
          if (shouldReindex) {
            progressMonitor.beginLongOperation(false);
            subMonitor = SubMonitor.convert(progressMonitor, Messages.DBManager_PROGRESS_WAIT, 20);
          }

          IModelSearch modelSearch = InternalOwl.getDefault().getPersistenceService().getModelSearch();
          if (!progressMonitor.isCanceled() && (shouldReindex || migrationResult.isOptimizeIndex())) {

            /* Reindex */
            if (shouldReindex && !progressMonitor.isCanceled()) {
              Activator.safeLogInfo("Start: Search Re-Indexing"); //$NON-NLS-1$

              File marker = getReindexMarkerFile();
              File reIndexFile = getReIndexFile();
              try {

                /* Create Marker that Reindexing is Performed */
                if (!marker.exists())
                  safeCreate(marker);

                /* Reindex Search Index */
                reindexed = true;
                modelSearch.reindexAll(subMonitor != null ? subMonitor.newChild(20) : new NullProgressMonitor());

                /*
                 * Make sure to delete the reindex file if existing only after
                 * the operation has completed without issues to ensure that
                 * upon next start the reindexing is started again if it failed
                 * prior.
                 */
                if (reIndexFile.exists())
                  safeDelete(reIndexFile);
              } finally {
                safeDelete(marker);
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
            safeDelete(cleanUpIndexFile);

            /* Log Status */
            Activator.safeLogInfo("Finished: Search Clean-Up"); //$NON-NLS-1$
          }
        }

        /* Start the periodic online backup service */
        final BackupService backupService = createOnlineBackupService();
        Job job = new Job("Online Backup Service") { //$NON-NLS-1$
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            if (!Owl.isShuttingDown() && !monitor.isCanceled()) {

              /* Perform Backup and store next online backup time unless delayed */
              synchronized (fNextOnlineBackup) {
                if (shouldOnlineBackup() && !delayOnlineBackup()) {
                  try {
                    backupService.backup(true, monitor);
                    fNextOnlineBackup.set(System.currentTimeMillis() + getOnlineBackupDelay(false));
                  } catch (PersistenceException e) {
                    Activator.safeLogError(e.getMessage(), e);
                  }
                }
              }

              /* Store Next Backup Time */
              if (!Owl.isShuttingDown() && !monitor.isCanceled())
                schedule(ONLINE_BACKUP_SCHEDULE_INTERVAL);
            }

            return Status.OK_STATUS;
          }
        };
        job.setSystem(true);
        job.schedule(ONLINE_BACKUP_SCHEDULE_INTERVAL);

        /* Store next online backup time */
        fNextOnlineBackup.set(System.currentTimeMillis() + getOnlineBackupDelay(true));
      }
    } finally {
      if (subMonitor != null) //If we perform the migration, the subMonitor is not null. Otherwise we don't show progress.
        progressMonitor.done();
    }
  }

  private boolean shouldOnlineBackup() {
    return System.currentTimeMillis() >= fNextOnlineBackup.get();
  }

  private boolean delayOnlineBackup() {
    boolean delay = System.currentTimeMillis() >= (fNextOnlineBackup.get() + ONLINE_BACKUP_DELAY_THRESHOLD);

    /* Re-Schedule to the future if delay threshold is hit */
    if (delay)
      fNextOnlineBackup.set(System.currentTimeMillis() + ONLINE_BACKUP_SHORT_INTERVAL);

    return delay;
  }

  private long getOnlineBackupDelay(boolean initial) {
    if (initial)
      return ONLINE_BACKUP_SHORT_INTERVAL;

    return getLongProperty("rssowl.onlinebackup.interval", ONLINE_BACKUP_LONG_INTERVAL); //$NON-NLS-1$
  }

  private long getLongProperty(String propertyName, long defaultValue) {
    String propertyValue = System.getProperty(propertyName);

    if (propertyValue != null) {
      try {
        long longProperty = Long.parseLong(propertyValue);
        if (longProperty > 0) {
          return longProperty;
        }
      } catch (NumberFormatException e) {
        /* Let it fall through and use default */
      }
    }
    return defaultValue;
  }

  public void addEntityStoreListener(DatabaseListener listener) {
    if (listener instanceof EventManager)
      fEntityStoreListeners.add(0, listener);
    else if (listener instanceof DB4OIDGenerator) {
      if (!fEntityStoreListeners.isEmpty() && fEntityStoreListeners.get(0) instanceof EventManager)
        fEntityStoreListeners.add(1, listener);
      else
        fEntityStoreListeners.add(0, listener);
    } else
      fEntityStoreListeners.add(listener);
  }

  private void fireDatabaseEvent(DatabaseEvent event, boolean storeOpened) {
    for (DatabaseListener listener : fEntityStoreListeners) {
      if (storeOpened) {
        listener.databaseOpened(event);
      } else {
        listener.databaseClosed(event);
      }
    }
  }

  private void createObjectContainer(Configuration config, boolean forRestore) throws PersistenceException {
    try {

      /* Open DB */
      fObjectContainer = Db4o.openFile(config, forRestore ? getDBRestoreFilePath() : getDBFilePath());

      /* Handle Fatal Error while opening DB */
      if (fObjectContainer == null)
        throw new PersistenceException(Messages.DBManager_UNABLE_TO_OPEN_PROFILE);

      /* Keep date of last successfull profile opened */
      storeProfileLastUsed();

      // #xyrio for debug, show when query optimizations failed
      fObjectContainer.ext().configure().diagnostic().addListener(new DiagnosticListener() {
        @Override
        public void onDiagnostic(Diagnostic d) {
          if (d instanceof NativeQueryNotOptimized) {
            System.out.println("Native query failed optimization!"); //$NON-NLS-1$
          }
        }
      });
    }

    /* Error opening the DB */
    catch (Throwable e) {

      /* Generic Error */
      if (e instanceof Error)
        throw (Error) e;

      /* Persistence Exception */
      if (e instanceof PersistenceException)
        throw (PersistenceException) e;

      /* Profile locked by another running instance */
      if (e instanceof DatabaseFileLockedException)
        throw new ProfileLockedException(e.getMessage(), e);

      File file = new File(getDBFilePath());

      /* Disk Full Error */
      if (!file.exists())
        throw new DiskFullException(Messages.DBManager_DISK_FULL_ERROR, e);

      /* Permission Error */
      if (!file.canRead() || (!file.canWrite()))
        throw new InsufficientFilePermissionException(NLS.bind(Messages.DBManager_FILE_PERMISSION_ERROR, file), null);

      /* Any other Error */
      throw new PersistenceException(e);
    }
  }

  private void checkDirPermissions() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    if (!dir.canRead() || (!dir.canWrite()))
      throw new InsufficientFilePermissionException(NLS.bind(Messages.DBManager_DIRECTORY_PERMISSION_ERROR, dir), null);
  }

  private boolean shouldReindex(MigrationResult migrationResult) {

    /* First ask migration result */
    boolean shouldReindex = migrationResult.isReindex();

    /* Second look if the reindex file exists */
    if (!shouldReindex && getReIndexFile().exists())
      shouldReindex = true;

    if (shouldReindex) { //Need to set system property as model search relies on it
      System.setProperty("rssowl.reindex", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      return true;
    }

    /* Finally ask System Property */
    return Boolean.getBoolean("rssowl.reindex"); //$NON-NLS-1$
  }

  private BackupService createOnlineBackupService() {
    File file = new File(getDBFilePath());

    /* No database file exists, so no back-up can exist */
    if (!file.exists())
      return null;

    final BackupService onlineBackupService = new BackupService(file, ONLINE_BACKUP_NAME, MAX_ONLINE_BACKUPS_COUNT);
    onlineBackupService.setBackupStrategy(new BackupService.BackupStrategy() {
      @Override
      public void backup(File originFile, File backupFile, IProgressMonitor monitor) {
        File marker = getOnlineBackupMarkerFile();
        File tmpBackupFile = null;
        try {

          /* Handle Shutdown and Cancellation */
          if (Owl.isShuttingDown() || monitor.isCanceled())
            return;

          /* Create Marker that Onlinebackup is Performed */
          if (!marker.exists())
            safeCreate(marker);

          /* Use a tmp file to guard against RSSOwl shutdown while backing up */
          tmpBackupFile = new File(backupFile.getParentFile(), TMP_BACKUP_NAME);
          if (tmpBackupFile.exists() && !tmpBackupFile.delete())
            throw new PersistenceException("Failed to delete file: " + tmpBackupFile); //$NON-NLS-1$

          /* Relies on fObjectContainer being set before calling backup */
          fObjectContainer.ext().backup(tmpBackupFile.getAbsolutePath());

          /* Store Backup as Weekly Backup if necessary */
          File weeklyBackup = onlineBackupService.getWeeklyBackupFile();
          boolean renameToWeekly = false;
          if (!weeklyBackup.exists()) //First Weekly
            renameToWeekly = true;
          else if (weeklyBackup.lastModified() < (System.currentTimeMillis() - MAX_ONLINE_BACKUP_AGE)) //Weekly older 1 Week
            renameToWeekly = true;

          /* Atomic Rename */
          DBHelper.rename(tmpBackupFile, renameToWeekly ? weeklyBackup : backupFile);
        } catch (IOException e) {
          throw new PersistenceException(e);
        } finally {
          safeDelete(marker);
          if (tmpBackupFile != null && tmpBackupFile.exists()) //Cleanup if something went wrong
            safeDelete(tmpBackupFile);
        }
      }
    });

    return onlineBackupService;
  }

  private void safeCreate(File file) {
    try {
      file.createNewFile();
    } catch (Exception e) {
      /* Ignore */
    }
  }

  private void safeDelete(File file) {
    try {
      file.delete();
    } catch (Exception e) {
      /* Ignore */
    }
  }

  /**
   * @return the File indicating whether defragment should be run or not.
   */
  public File getDefragmentFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, DEFRAGMENT_MARKER);
  }

  /**
   * @return the File indicating whether the DB was defragmented to a larger
   * block size.
   */
  public static File getLargeBlockSizeMarkerFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, LARGE_BLOCK_SIZE_MARKER);
  }

  /**
   * @return the File indicating whether the online backup terminated normally
   * or not.
   */
  public File getOnlineBackupMarkerFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, ONLINE_RUNNING_BACKUP_MARKER);
  }

  /**
   * @return the File indicating whether cleanup index should be run or not.
   */
  public File getCleanUpIndexFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, CLEANUP_INDEX_MARKER);
  }

  /**
   * @return the File indicating whether reindexing should be run or not.
   */
  public File getReIndexFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, REINDEX_MARKER);
  }

  /**
   * @return the File indicating whether the reindexing of news terminated
   * normally or not.
   */
  public File getReindexMarkerFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, REINDEX_RUNNING_MARKER);
  }

  /**
   * Internal method, exposed for tests only.
   *
   * @return the path to the db file.
   */
  public static final String getDBFilePath() {
    String filePath = Activator.getDefault().getStateLocation().toOSString() + File.separator + DB_NAME;
    return filePath;
  }

  /**
   * Internal method, exposed for tests only.
   *
   * @return the path to the db file.
   */
  public static final String getDBRestoreFilePath() {
    String filePath = Activator.getDefault().getStateLocation().toOSString() + File.separator + DB_RESTORE_NAME;
    return filePath;
  }

  private File getDBFormatFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File formatFile = new File(dir, FORMAT_FILE_NAME);
    return formatFile;
  }

  public void removeEntityStoreListener(DatabaseListener listener) {
    fEntityStoreListeners.remove(listener);
  }

  private BackupService createScheduledBackupService(Long backupFrequency) {
    return new BackupService(new File(getDBFilePath()), OFFLINE_BACKUP_NAME, MAX_OFFLINE_BACKUPS_COUNT, getDBLastBackUpFile(), backupFrequency);
  }

  private void scheduledBackup(IProgressMonitor monitor) {
    if (!new File(getDBFilePath()).exists())
      return;

    long sevenDays = getLongProperty("rssowl.offlinebackup.interval", OFFLINE_BACKUP_INTERVAL); //$NON-NLS-1$
    try {
      createScheduledBackupService(sevenDays).backup(false, monitor);
    } catch (PersistenceException e) {
      Activator.safeLogError(e.getMessage(), e);
    }
  }

  public File getDBLastBackUpFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File lastBackUpFile = new File(dir, "lastbackup"); //$NON-NLS-1$
    return lastBackUpFile;
  }

  private File getDBLastUsedFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File lastDBUseFile = new File(dir, "lastused"); //$NON-NLS-1$
    return lastDBUseFile;
  }

  Long getProfileLastUsed() {
    File file = getDBLastUsedFile();
    if (file.exists()) {
      try {
        return Long.parseLong(DBHelper.readFirstLineFromFile(file));
      } catch (Exception e) {
        /* Ignore */
      }
    }

    return null;
  }

  private void storeProfileLastUsed() {
    File file = getDBLastUsedFile();
    try {
      if (!file.exists())
        file.createNewFile();
      DBHelper.writeToFile(file, String.valueOf(System.currentTimeMillis()));
    } catch (Exception e) {
      /* Ignore */
    }
  }

  private MigrationResult migrate(final int workspaceFormat, int currentFormat, IProgressMonitor progressMonitor) {
    Activator.safeLogInfo(NLS.bind("Migrating RSSOwl (from version {0} to version {1}", workspaceFormat, currentFormat)); //$NON-NLS-1$

    ConfigurationFactory configFactory = new ConfigurationFactory() {
      @Override
      public Configuration createConfiguration() {
        return DBManager.createConfiguration(false);
      }
    };
    Migration migration = new Migrations().getMigration(workspaceFormat, currentFormat);
    if (migration == null) {
      throw new PersistenceException("It was not possible to migrate your data to the current version of RSSOwl. Migrations are supported between final versions and between consecutive milestones. In other words, 2.0M7 to 2.0M8 and 2.0 to 2.1 are supported but 2.0M6 to 2.0M8 is not supported. In the latter case, you would need to launch 2.0M7 and then 2.0M8 to be able to use that version. Migration was attempted from originFormat: " + workspaceFormat + " to destinationFormat: " + currentFormat); //$NON-NLS-1$ //$NON-NLS-2$
    }

    final File dbFile = new File(getDBFilePath());
    final String backupFileSuffix = ".mig."; //$NON-NLS-1$

    /*
     * Copy the db file to a permanent back-up where the file name includes the
     * workspaceFormat number. This will only be deleted after another
     * migration.
     */
    final BackupService backupService = new BackupService(dbFile, backupFileSuffix + workspaceFormat, 1);
    backupService.setLayoutStrategy(new BackupService.BackupLayoutStrategy() {
      @Override
      public List<File> findBackupFiles() {
        List<File> backupFiles = new ArrayList<File>(3);
        for (int i = workspaceFormat; i >= 0; --i) {
          File file = new File(dbFile.getAbsoluteFile() + backupFileSuffix + i);
          if (file.exists())
            backupFiles.add(file);
        }
        return backupFiles;
      }

      @Override
      public void rotateBackups(List<File> backupFiles) {
        throw new UnsupportedOperationException("No rotation supported because maxBackupCount is 1"); //$NON-NLS-1$
      }
    });
    backupService.backup(true, new NullProgressMonitor());

    /* Create a copy of the db file to use for the migration */
    File migDbFile = backupService.getTempBackupFile();
    DBHelper.copyFileNIO(dbFile, migDbFile);

    /* Migrate the copy */
    MigrationResult migrationResult = migration.migrate(configFactory, migDbFile.getAbsolutePath(), progressMonitor);

    File dbFormatFile = getDBFormatFile();
    File migFormatFile = new File(dbFormatFile.getAbsolutePath() + ".mig.temp"); //$NON-NLS-1$
    try {
      if (!migFormatFile.exists()) {
        migFormatFile.createNewFile();
      }
      if (!dbFormatFile.exists()) {
        dbFormatFile.createNewFile();
      }
    } catch (IOException ioe) {
      throw new PersistenceException("Failed to migrate data", ioe); //$NON-NLS-1$
    }
    setFormatVersion(migFormatFile);

    DBHelper.rename(migFormatFile, dbFormatFile);

    /* Finally, rename the actual db file */
    DBHelper.rename(migDbFile, dbFile);

    /* Pre 2.0 M9 (inclusive) Code Path */
    if (getOldDBFormatFile().exists())
      getOldDBFormatFile().delete();

    return migrationResult;
  }

  private File getOldDBFormatFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File formatFile = new File(dir, "format"); //$NON-NLS-1$
    return formatFile;
  }

  private int getWorkspaceFormatVersion() {
    boolean dbFileExists = new File(getDBFilePath()).exists();
    File formatFile = getDBFormatFile();
    boolean formatFileExists = formatFile.exists();

    /* Pre 2.0 M9 (inclusive) Code Path */
    if (!formatFileExists && getOldDBFormatFile().exists()) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(getOldDBFormatFile()));
        String text = reader.readLine();
        DBHelper.writeToFile(formatFile, text);
        formatFileExists = true;
      } catch (IOException e) {
        throw new PersistenceException(e);
      } finally {
        DBHelper.closeQuietly(reader);
      }
    }

    if (dbFileExists) {
      /* Assume that it's M5a if no format file exists, but a db file exists */
      if (!formatFileExists)
        return 0;

      String versionText = DBHelper.readFirstLineFromFile(formatFile);
      try {
        int version = Integer.parseInt(versionText);
        return version;
      } catch (NumberFormatException e) {
        throw new PersistenceException("Format file does not contain a number as the version", e); //$NON-NLS-1$
      }
    }
    /*
     * In case there is no database file, we just set the version as the current
     * version.
     */
    if (!formatFileExists) {
      try {
        formatFile.createNewFile();
      } catch (IOException ioe) {
        throw new PersistenceException("Error creating database", ioe); //$NON-NLS-1$
      }
    }
    setFormatVersion(formatFile);
    return getCurrentFormatVersion();
  }

  private void setFormatVersion(File formatFile) {
    DBHelper.writeToFile(formatFile, String.valueOf(getCurrentFormatVersion()));
  }

  private int getCurrentFormatVersion() {
    return 5;
  }

  private boolean defragmentIfNecessary(LongOperationMonitor progressMonitor, SubMonitor subMonitor) {
    boolean defragmentToLargeBlockSize = false;

    /* First: Check if a defragmentation to a larger block size is required */
    if (defragmentToLargerBlockSize()) {
      defragmentToLargeBlockSize = true;

      /*
       * Should also clean up any defragment file to avoid duplicate
       * defragmentation
       */
      File defragmentFile = getDefragmentFile();
      if (defragmentFile.exists())
        defragmentFile.delete();
    }

    /* Second: Check if the user asked for defragmentation */
    else {
      File defragmentFile = getDefragmentFile();
      if (!defragmentFile.exists())
        return false;

      if (!defragmentFile.delete())
        Activator.getDefault().logError("Failed to delete defragment file", null); //$NON-NLS-1$
    }

    defragment(defragmentToLargeBlockSize, progressMonitor, subMonitor);
    return true;
  }

  private boolean defragmentToLargerBlockSize() {
    if (getLargeBlockSizeMarkerFile().exists())
      return false;

    File database = new File(getDBFilePath());
    long length = database.exists() ? database.length() : 0;
    return length > LARGE_DB_STARTING_SIZE;
  }

  private void defragment(boolean useLargeBlockSize, LongOperationMonitor progressMonitor, SubMonitor subMonitor) {
    SubMonitor monitor;
    if (subMonitor == null) {
      progressMonitor.beginLongOperation(true);
      String monitorText = Messages.DBManager_PROGRESS_WAIT;
      subMonitor = SubMonitor.convert(progressMonitor, monitorText, DEFRAG_TOTAL_WORK);
      monitor = subMonitor.newChild(DEFRAG_TOTAL_WORK);

      /*
       * This should not be needed, but things don't work properly when it's not
       * called.
       */
      monitor.beginTask(monitorText, DEFRAG_TOTAL_WORK);
    } else {
      monitor = subMonitor.newChild(10);
      monitor.setWorkRemaining(100);
    }

    Activator.safeLogInfo("Start: Database Defragmentation"); //$NON-NLS-1$

    BackupService backupService = createScheduledBackupService(null);
    File database = new File(getDBFilePath());
    File defragmentedDatabase = new File(database.getParentFile(), TMP_BACKUP_NAME);
    if (defragmentedDatabase.exists() && !defragmentedDatabase.delete())
      throw new PersistenceException("Failed to delete file: " + defragmentedDatabase); //$NON-NLS-1$

    /* User might have cancelled the operation */
    if (!useLargeBlockSize && monitor.isCanceled()) {
      Activator.safeLogInfo("Cancelled: Database Defragmentation"); //$NON-NLS-1$
      return;
    }

    /* Defrag */
    monitor.subTask(Messages.DBManager_IMPROVING_APP_PERFORMANCE);
    copyDatabase(database, defragmentedDatabase, useLargeBlockSize, monitor);

    /* User might have cancelled the operation */
    if (!useLargeBlockSize && monitor.isCanceled()) {
      Activator.safeLogInfo("Cancelled: Database Defragmentation"); //$NON-NLS-1$
      defragmentedDatabase.delete();
      return;
    }

    /* Backup */
    monitor.subTask(Messages.DBManager_CREATING_DB_BACKUP);
    backupService.backup(true, monitor);

    /* User might have cancelled the operation */
    if (!useLargeBlockSize && monitor.isCanceled()) {
      Activator.safeLogInfo("Cancelled: Database Defragmentation"); //$NON-NLS-1$
      defragmentedDatabase.delete();
      return;
    }

    /* Rename Defragmented DB to real DB */
    DBHelper.rename(defragmentedDatabase, database);

    /*
     * Create the marker file in case the DB has been migrated to a larger block
     * size
     */
    File largeBlockSizeMarkerFile = getLargeBlockSizeMarkerFile();
    if (useLargeBlockSize && !largeBlockSizeMarkerFile.exists()) {
      try {
        if (!largeBlockSizeMarkerFile.createNewFile())
          Activator.getDefault().logError("Failed to create large blocksize marker file", null); //$NON-NLS-1$
      } catch (IOException e) {
        Activator.getDefault().logError("Failed to create large blocksize marker file", e); //$NON-NLS-1$
      }
    }

    /* Finished */
    monitor.done();
    Activator.safeLogInfo("Finished: Database Defragmentation"); //$NON-NLS-1$
  }

  /**
   * Internal method. Made public for testing. Creates a copy of the database
   * that has all essential data structures. At the moment, this means not
   * copying NewsCounter and IConditionalGets since they will be re-populated
   * eventually.
   *
   * @param source
   * @param destination
   * @param useLargeBlockSize
   * @param monitor
   */
  public final static void copyDatabase(File source, File destination, boolean useLargeBlockSize, IProgressMonitor monitor) {
    ObjectContainer sourceDb = null;
    ObjectContainer destinationDb = null;
    try {

      /* Open Source DB */
      sourceDb = Db4o.openFile(createConfiguration(true), source.getAbsolutePath());

      Configuration destinationDbConfiguration = createConfiguration(true);
      if (useLargeBlockSize)
        destinationDbConfiguration.blockSize(LARGE_DB_BLOCK_SIZE);

      /* Open Destination DB */
      destinationDb = Db4o.openFile(destinationDbConfiguration, destination.getAbsolutePath());

      /* Copy (Defragment) */
      internalCopyDatabase(sourceDb, destinationDb, useLargeBlockSize, monitor);
    } finally {
      if (sourceDb != null)
        sourceDb.close();

      if (destinationDb != null)
        destinationDb.close();
    }
  }

  public final static void internalCopyDatabase(ObjectContainer sourceDb, ObjectContainer destinationDb, boolean useLargeBlockSize, IProgressMonitor monitor) {

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Labels (keep in memory to avoid duplicate copies when cascading feed) */
    List<Label> labels = new ArrayList<Label>();
    ObjectSet<Label> allLabels = sourceDb.query(Label.class);
    int available = DEFRAG_SUB_WORK_LABELS;
    if (!allLabels.isEmpty()) {
      int chunk = available / allLabels.size();
      for (Label label : allLabels) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        labels.add(label);
        sourceDb.activate(label, Integer.MAX_VALUE);
        destinationDb.ext().set(label, Integer.MAX_VALUE);
        monitor.worked(chunk);
      }
      allLabels = null;
    } else
      monitor.worked(available);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Folders */
    ObjectSet<Folder> allFolders = sourceDb.query(Folder.class);
    available = DEFRAG_SUB_WORK_FOLDERS;
    if (!allFolders.isEmpty()) {
      int chunk = available / allFolders.size();
      for (Folder type : allFolders) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        sourceDb.activate(type, Integer.MAX_VALUE);
        if (type.getParent() == null)
          destinationDb.ext().set(type, Integer.MAX_VALUE);

        monitor.worked(chunk);
      }
      allFolders = null;
    } else
      monitor.worked(available);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /*
     * We use destinationDb for the query here because we have already copied
     * the NewsBins at this stage and we may need to fix the NewsBin in case it
     * contains stale news refs.
     */
    ObjectSet<NewsBin> allBins = destinationDb.query(NewsBin.class);
    available = DEFRAG_SUB_WORK_BINS;
    if (!allBins.isEmpty()) {
      int chunk = available / allBins.size();
      for (NewsBin newsBin : allBins) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        destinationDb.activate(newsBin, Integer.MAX_VALUE);
        List<NewsReference> staleNewsRefs = new ArrayList<NewsReference>(0);
        for (NewsReference newsRef : newsBin.getNewsRefs()) {
          if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
            return;

          Query query = sourceDb.query();
          query.constrain(News.class);
          query.descend("fId").constrain(newsRef.getId()); //$NON-NLS-1$
          Iterator<?> newsIt = query.execute().iterator();
          if (!newsIt.hasNext()) {
            Activator.getDefault().logError("NewsBin " + newsBin + " has reference to news with id: " + newsRef.getId() + ", but that news does not exist.", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            staleNewsRefs.add(newsRef);
            continue;
          }
          Object news = newsIt.next();
          sourceDb.activate(news, Integer.MAX_VALUE);
          destinationDb.ext().set(news, Integer.MAX_VALUE);
        }

        if (!staleNewsRefs.isEmpty()) {
          if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
            return;

          newsBin.removeNewsRefs(staleNewsRefs);
          destinationDb.ext().set(newsBin, Integer.MAX_VALUE);
        }

        monitor.worked(chunk);
      }
      allBins = null;
    } else
      monitor.worked(available);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Feeds */
    available = DEFRAG_SUB_WORK_FEEDS;
    int feedCounter = 0;
    NewsCounter newsCounter = new NewsCounter();
    ObjectSet<Feed> allFeeds = sourceDb.query(Feed.class);
    if (!allFeeds.isEmpty()) {
      int allFeedsSize = allFeeds.size();
      int chunk = available / allFeedsSize;

      int i = 1;
      for (Feed feed : allFeeds) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        /* Introduce own label as feed copying can be very time consuming */
        monitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_NEWSFEEDS, i, allFeedsSize));
        i++;

        sourceDb.activate(feed, Integer.MAX_VALUE);
        addNewsCounterItem(newsCounter, feed);
        destinationDb.ext().set(feed, Integer.MAX_VALUE);

        ++feedCounter;
        if (feedCounter % 40 == 0) {
          destinationDb.commit();
          System.gc();
        }

        monitor.worked(chunk);
      }
      allFeeds = null;
      destinationDb.commit();
      System.gc();
    } else
      monitor.worked(available);

    /* Back to normal subtask label */
    monitor.subTask(Messages.DBManager_IMPROVING_APP_PERFORMANCE);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    destinationDb.ext().set(newsCounter, Integer.MAX_VALUE);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Description */
    available = DEFRAG_SUB_WORK_DESCRIPTIONS;
    int descriptionCounter = 0;
    ObjectSet<Description> allDescriptions = sourceDb.query(Description.class);
    if (!allDescriptions.isEmpty()) {
      int chunk = Math.max(1, available / allDescriptions.size());

      for (Description description : allDescriptions) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        sourceDb.activate(description, Integer.MAX_VALUE);
        destinationDb.ext().set(description, Integer.MAX_VALUE);

        ++descriptionCounter;
        if (descriptionCounter % 600 == 0) {
          destinationDb.commit();
          System.gc();
        }

        monitor.worked(chunk);
      }

      allDescriptions = null;
      destinationDb.commit();
      System.gc();
    } else
      monitor.worked(available);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Preferences */
    available = DEFRAG_SUB_WORK_PREFERENCES;
    ObjectSet<Preference> allPreferences = sourceDb.query(Preference.class);
    if (!allPreferences.isEmpty()) {
      int chunk = available / allPreferences.size();

      for (Preference pref : allPreferences) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        sourceDb.activate(pref, Integer.MAX_VALUE);
        destinationDb.ext().set(pref, Integer.MAX_VALUE);

        monitor.worked(chunk);
      }

      allPreferences = null;
    } else
      monitor.worked(available);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Filter */
    available = DEFRAG_SUB_WORK_FILTERS;
    ObjectSet<SearchFilter> allFilters = sourceDb.query(SearchFilter.class);
    if (!allFilters.isEmpty()) {
      int chunk = available / allFilters.size();

      for (ISearchFilter filter : allFilters) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        sourceDb.activate(filter, Integer.MAX_VALUE);
        destinationDb.ext().set(filter, Integer.MAX_VALUE);
        monitor.worked(chunk);
      }

      allFilters = null;
    } else
      monitor.worked(available);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Counter */
    List<Counter> counterSet = sourceDb.query(Counter.class);
    Counter counter = counterSet.iterator().next();
    sourceDb.activate(counter, Integer.MAX_VALUE);
    destinationDb.ext().set(counter, Integer.MAX_VALUE);

    monitor.worked(DEFRAG_SUB_WORK_COUNTERS);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Entity Id By Event Type */
    EntityIdsByEventType entityIdsByEventType = sourceDb.query(EntityIdsByEventType.class).iterator().next();
    sourceDb.activate(entityIdsByEventType, Integer.MAX_VALUE);
    destinationDb.ext().set(entityIdsByEventType, Integer.MAX_VALUE);

    monitor.worked(DEFRAG_SUB_WORK_EVENTS);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    /* Conditional Get */
    available = DEFRAG_SUB_WORK_CONDITIONAL_GET;
    ObjectSet<ConditionalGet> allConditionalGets = sourceDb.query(ConditionalGet.class);
    if (!allConditionalGets.isEmpty()) {
      int chunk = available / allConditionalGets.size();
      for (ConditionalGet conditionalGet : allConditionalGets) {
        if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
          return;

        sourceDb.activate(conditionalGet, Integer.MAX_VALUE);
        destinationDb.ext().set(conditionalGet, Integer.MAX_VALUE);
        monitor.worked(chunk);
      }
      allConditionalGets = null;
    } else
      monitor.worked(available);

    /* User might have cancelled the operation */
    if (isCanceled(monitor, useLargeBlockSize, sourceDb, destinationDb))
      return;

    sourceDb.close();
    monitor.worked(DEFRAG_SUB_WORK_CLOSE_SOURCE);

    /* User might have cancelled the operation */
    if (monitor.isCanceled()) {
      destinationDb.close();
      return;
    }

    destinationDb.commit();
    monitor.worked(DEFRAG_SUB_WORK_COMMITT_DESTINATION);

    /* User might have cancelled the operation */
    if (monitor.isCanceled()) {
      destinationDb.close();
      return;
    }

    destinationDb.close();
    monitor.worked(DEFRAG_SUB_WORK_CLOSE_DESTINATION);

    /* User might have cancelled the operation */
    if (monitor.isCanceled())
      return;

    System.gc();
    monitor.worked(DEFRAG_SUB_WORK_FINISH);
  }

  private static boolean isCanceled(IProgressMonitor monitor, boolean useLargeBlockSize, ObjectContainer source, ObjectContainer dest) {
    if (monitor.isCanceled()) {
      if (useLargeBlockSize) { //Must not allow cancellation when migrating from small DB to 2 GB DB
        monitor.setTaskName(Messages.DBManager_WAIT_TASK_COMPLETION);
        return false;
      }

      /* Otherwise close Object Containers */
      source.close();
      dest.close();
      return true;
    }

    return false;
  }

  private static void addNewsCounterItem(NewsCounter newsCounter, Feed feed) {
    Map<State, Integer> stateToCountMap = feed.getNewsCount();
    int unreadCount = getCount(stateToCountMap, EnumSet.of(State.NEW, State.UNREAD, State.UPDATED));
    Integer newCount = stateToCountMap.get(INews.State.NEW);
    newsCounter.put(feed.getLink().toString(), new NewsCounterItem(newCount, unreadCount, feed.getStickyCount()));
  }

  private static int getCount(Map<State, Integer> stateToCountMap, Set<State> states) {
    int count = 0;
    for (State state : states) {
      count += stateToCountMap.get(state);
    }
    return count;
  }

  /**
   * Internal method, exposed for tests only.
   *
   * @param forDefrag if <code>true</code> the configuration will be improved
   * for the defrag process and <code>false</code> otherwise to return a normal
   * configuration suitable for the application.
   * @return Configuration
   */
  public static final Configuration createConfiguration(boolean forDefrag) {
    Configuration config = Db4o.newConfiguration();

    //TODO We can use dbExists to configure our parameters for a more
    //efficient startup. For example, the following could be used. We'd have
    //to include a file when we need to evolve the schema or something similar
    //config.detectSchemaChanges(false)

    if (getLargeBlockSizeMarkerFile().exists())
      config.blockSize(LARGE_DB_BLOCK_SIZE); //The DB has been migrated to a larger block size

    config.setOut(new PrintStream(new ByteArrayOutputStream()) {
      @Override
      public void write(byte[] buf, int off, int len) {
        if (buf != null && len >= 0 && off >= 0 && off <= buf.length - len)
          CoreUtils.appendLogMessage(new String(buf, off, len));
      }
    });

    config.lockDatabaseFile(true);
    config.queries().evaluationMode(forDefrag ? QueryEvaluationMode.LAZY : QueryEvaluationMode.IMMEDIATE);
    config.automaticShutDown(false);
    config.callbacks(false);
    config.activationDepth(2);
    config.flushFileBuffers(false);
    config.callConstructors(true);
    config.exceptionsOnNotStorable(true);
    configureAbstractEntity(config);
    config.objectClass(BookMark.class).objectField("fFeedLink").indexed(true); //$NON-NLS-1$
    config.objectClass(ConditionalGet.class).objectField("fLink").indexed(true); //$NON-NLS-1$
    configureFeed(config);
    configureNews(config);
    configureFolder(config);
    config.objectClass(Description.class).objectField("fNewsId").indexed(true); //$NON-NLS-1$
    config.objectClass(NewsCounter.class).cascadeOnDelete(true);
    config.objectClass(Preference.class).cascadeOnDelete(true);
    config.objectClass(Preference.class).objectField("fKey").indexed(true); //$NON-NLS-1$
    config.objectClass(SearchFilter.class).objectField("fActions").cascadeOnDelete(true); //$NON-NLS-1$

    if (isIBM_VM_1_6()) //See defect 733
      config.objectClass("java.util.MiniEnumSet").translate(new com.db4o.config.TSerializable()); //$NON-NLS-1$

    return config;
  }

  private static boolean isIBM_VM_1_6() {
    String javaVendor = System.getProperty("java.vendor"); //$NON-NLS-1$
    String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
    return javaVendor != null && javaVendor.contains("IBM") && javaVersion != null && javaVersion.contains("1.6"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private static void configureAbstractEntity(Configuration config) {
    ObjectClass abstractEntityClass = config.objectClass(AbstractEntity.class);
    ObjectField idField = abstractEntityClass.objectField("fId"); //$NON-NLS-1$
    idField.indexed(true);
    idField.cascadeOnActivate(true);
    abstractEntityClass.objectField("fProperties").cascadeOnUpdate(true); //$NON-NLS-1$
  }

  private static void configureFolder(Configuration config) {
    ObjectClass oc = config.objectClass(Folder.class);
    oc.objectField("fChildren").cascadeOnUpdate(true); //$NON-NLS-1$
  }

  private static void configureNews(Configuration config) {
    ObjectClass oc = config.objectClass(News.class);

    /* Indexes */
    oc.objectField("fParentId").indexed(true); //$NON-NLS-1$
    oc.objectField("fFeedLink").indexed(true); //$NON-NLS-1$
    oc.objectField("fStateOrdinal").indexed(true); //$NON-NLS-1$
  }

  private static void configureFeed(Configuration config) {
    ObjectClass oc = config.objectClass(Feed.class);

    ObjectField linkText = oc.objectField("fLinkText"); //$NON-NLS-1$
    linkText.indexed(true);
    linkText.cascadeOnActivate(true);

    oc.objectField("fTitle").cascadeOnActivate(true); //$NON-NLS-1$
  }

  /**
   * Shutdown the contributed Database.
   *
   * @throws PersistenceException In case of an error while shutting down the
   * contributed DataBase.
   */
  public void shutdown() throws PersistenceException {
    fLock.writeLock().lock();
    try {
      fireDatabaseEvent(new DatabaseEvent(fObjectContainer, fLock), false);
      if (fObjectContainer != null)
        while (!fObjectContainer.close());
    } finally {
      fLock.writeLock().unlock();
    }
  }

  public final ObjectContainer getObjectContainer() {
    return fObjectContainer;
  }

  Pair<File, Long> getProfile() {
    File profile = new File(getDBFilePath());
    Long timestamp = getProfileLastUsed();

    return Pair.create(profile, timestamp);
  }

  List<File> getProfileBackups() {
    List<File> backups = new ArrayList<File>();
    File backupDir = new File(Activator.getDefault().getStateLocation().toOSString());

    /* Locate Online Backups */
    File onlineWeeklyBackup = new File(backupDir, DB_NAME + ONLINE_BACKUP_NAME + ".weekly"); //$NON-NLS-1$
    if (onlineWeeklyBackup.exists())
      backups.add(onlineWeeklyBackup);

    File onlineDailyBackup = new File(backupDir, DB_NAME + ONLINE_BACKUP_NAME);
    if (onlineDailyBackup.exists())
      backups.add(onlineDailyBackup);

    File onlineDailyBackupOlder = new File(backupDir, DB_NAME + ONLINE_BACKUP_NAME + ".0"); //$NON-NLS-1$
    if (onlineDailyBackupOlder.exists())
      backups.add(onlineDailyBackupOlder);

    /* Locate Offline Backups */
    File offlineBackup = new File(backupDir, DB_NAME + OFFLINE_BACKUP_NAME);
    if (offlineBackup.exists())
      backups.add(offlineBackup);

    File offlineBackupOlder = new File(backupDir, DB_NAME + OFFLINE_BACKUP_NAME + ".0"); //$NON-NLS-1$
    if (offlineBackupOlder.exists())
      backups.add(offlineBackupOlder);

    Collections.sort(backups, new Comparator<File>() {
      @Override
      public int compare(File f1, File f2) {
        return f1.lastModified() > f2.lastModified() ? -1 : 1;
      };
    });

    return backups;
  }

  void restoreProfile(File backup) throws PersistenceException {
    Activator.safeLogInfo(NLS.bind("Start: Database Restore from Backup ({0})", backup.getName())); //$NON-NLS-1$

    /* Atomic Rename to "rssowl.db.restore" */
    File db = new File(getDBRestoreFilePath());
    DBHelper.rename(backup, db);

    /* Handle Large Block Size properly */
    try {
      File largeBlockSizeMarkerFile = getLargeBlockSizeMarkerFile();
      if (largeBlockSizeMarkerFile.exists() && db.length() < LARGE_DB_STARTING_SIZE)
        largeBlockSizeMarkerFile.delete();
      else if (!largeBlockSizeMarkerFile.exists() && db.length() > LARGE_DB_STARTING_SIZE)
        largeBlockSizeMarkerFile.createNewFile();
    } catch (IOException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }

    Activator.safeLogInfo("End: Database Restore from Backup"); //$NON-NLS-1$
  }

  /* Delete the Profile by Moving it to a Backup */
  private void backupAndDeleteProfile() {
    File db = new File(getDBFilePath());
    if (db.exists()) {
      Activator.safeLogInfo("Start: Backup and Delete Profile"); //$NON-NLS-1$

      /*
       * Object Container might be opened, so try to close (only for testing,
       * not in production)
       */
      if (InternalOwl.TESTING)
        shutdown();

      /* Find Suitable Backup Name */
      int i = 0;
      File backupDir = new File(Activator.getDefault().getStateLocation().toOSString());
      File backupCandidate = new File(backupDir, DB_NAME + RESTORE_BACKUP_NAME);
      while (backupCandidate.exists()) {
        backupCandidate = new File(backupDir, DB_NAME + RESTORE_BACKUP_NAME + "." + i++); //$NON-NLS-1$
      }

      /* Atomic Rename */
      DBHelper.rename(db, backupCandidate);

      Activator.safeLogInfo(NLS.bind("End: Backup and Delete Profile ({0})", backupCandidate.getName())); //$NON-NLS-1$
    }
  }

  void dropDatabaseForTests() throws PersistenceException {
    SafeRunner.run(new LoggingSafeRunnable() {
      @Override
      public void run() throws Exception {

        /* Shutdown DB */
        shutdown();

        /* Delete DB */
        File dbFile = new File(getDBFilePath());
        if (dbFile.exists() && !dbFile.delete())
          Activator.getDefault().logError("Failed to delete db file", null); //$NON-NLS-1$

        /* Delete other marker files */
        delete(getDBFormatFile(), getDefragmentFile(), getReIndexFile(), getCleanUpIndexFile());
      }
    });
  }

  private void delete(File... files) {
    for (File file : files) {
      if (file.exists())
        file.delete();
    }
  }
}
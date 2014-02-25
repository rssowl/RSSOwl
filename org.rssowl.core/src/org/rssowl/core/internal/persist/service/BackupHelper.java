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

package org.rssowl.core.internal.persist.service;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.persist.service.PersistenceException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class BackupHelper {

  private static final String ONLINE_RUNNING_BACKUP_MARKER = "onlinebakmarker"; //$NON-NLS-1$

  /* Backup Settings */
//  private static final boolean PERFORM_SCHEDULED_BACKUPS = false; //Disabled in favor of online backups
  private static final int MAX_OFFLINE_BACKUPS_COUNT = 1; //Only used for backups from defragment
  private static final int MAX_ONLINE_BACKUPS_COUNT = 1; //Will keep 1 Current + 1 Weekly
  private static final int MAX_ONLINE_BACKUP_AGE = 1000 * 60 * 60 * 24 * 7; //7 Days
  private static final String RESTORE_BACKUP_NAME = ".restorebak"; //$NON-NLS-1$
  private static final String ONLINE_BACKUP_NAME = ".onlinebak"; //$NON-NLS-1$
  private static final String OFFLINE_BACKUP_NAME = ".backup"; //$NON-NLS-1$
  private static final int ONLINE_BACKUP_SCHEDULE_INTERVAL = 1000 * 60 * 5; //5 Minutes
  private static final int ONLINE_BACKUP_DELAY_THRESHOLD = 1000 * 60 * 30; //30 Minutes
  private static final int ONLINE_BACKUP_SHORT_INTERVAL = 1000 * 60 * 55; //55 Minutes
  private static final int ONLINE_BACKUP_LONG_INTERVAL = 1000 * 60 * 60 * 10; //10 Hours

  private static final AtomicLong fNextOnlineBackup = new AtomicLong();

  /**
   * @return the File indicating whether the online backup terminated normally
   * or not.
   */
  public static File getOnlineBackupMarkerFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, ONLINE_RUNNING_BACKUP_MARKER);
  }

//  private void scheduledBackup(IProgressMonitor monitor) {
//    if (!new File(getDBFilePath()).exists()) {
//      return;
//    }
//
//    long sevenDays = getLongProperty("rssowl.offlinebackup.interval", OFFLINE_BACKUP_INTERVAL); //$NON-NLS-1$
//    try {
//      createScheduledBackupService(sevenDays).backup(false, monitor);
//    } catch (PersistenceException e) {
//      Activator.safeLogError(e.getMessage(), e);
//    }
//  }
//
//  public BackupService createScheduledBackupService(Long backupFrequency) {
//    return new BackupService(new File(fDBManager.getDBFilePath()), OFFLINE_BACKUP_NAME, MAX_OFFLINE_BACKUPS_COUNT, getDBLastBackUpFile(), backupFrequency);
//  }

  public static File getDBLastBackUpFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File lastBackUpFile = new File(dir, "lastbackup"); //$NON-NLS-1$
    return lastBackUpFile;
  }

  public static void createBackupService() {

    /* Log previously failing Online Backup */
    try {
      if (getOnlineBackupMarkerFile().exists()) {
        Activator.safeLogInfo("Detected an Online Backup that did not complete"); //$NON-NLS-1$
        ProfileFileManager.safeDelete(getOnlineBackupMarkerFile());
      }
    } catch (Exception e) {
      /* Ignore */
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
          if (!Owl.isShuttingDown() && !monitor.isCanceled()) {
            schedule(ONLINE_BACKUP_SCHEDULE_INTERVAL);
          }
        }

        return Status.OK_STATUS;
      }
    };
    job.setSystem(true);
    job.schedule(ONLINE_BACKUP_SCHEDULE_INTERVAL);

    /* Store next online backup time */
    fNextOnlineBackup.set(System.currentTimeMillis() + getOnlineBackupDelay(true));
  }

  public static BackupService createScheduledBackupService(Long backupFrequency) {
    return new BackupService(new File(ProfileFileManager.getDBFilePath()), OFFLINE_BACKUP_NAME, MAX_OFFLINE_BACKUPS_COUNT, getDBLastBackUpFile(), backupFrequency);
  }

  private static BackupService createOnlineBackupService() {
    File file = new File(ProfileFileManager.getDBFilePath());

    /* No database file exists, so no back-up can exist */
    if (!file.exists()) {
      return null;
    }

    final BackupService onlineBackupService = new BackupService(file, ONLINE_BACKUP_NAME, MAX_ONLINE_BACKUPS_COUNT);
    onlineBackupService.setBackupStrategy(new BackupService.BackupStrategy() {
      public void backup(File originFile, File backupFile, IProgressMonitor monitor) {
        File marker = getOnlineBackupMarkerFile();
        File tmpBackupFile = null;
        try {

          /* Handle Shutdown and Cancellation */
          if (Owl.isShuttingDown() || monitor.isCanceled()) {
            return;
          }

          /* Create Marker that Onlinebackup is Performed */
          if (!marker.exists()) {
            ProfileFileManager.safeCreate(marker);
          }

          /* Use a tmp file to guard against RSSOwl shutdown while backing up */
          tmpBackupFile = new File(backupFile.getParentFile(), ProfileFileManager.getTempFile());
          if (tmpBackupFile.exists() && !tmpBackupFile.delete()) {
            throw new PersistenceException("Failed to delete file: " + tmpBackupFile); //$NON-NLS-1$
          }

          /* Relies on fObjectContainer being set before calling backup */
          DBManager.getDefault().getObjectContainer().ext().backup(tmpBackupFile.getAbsolutePath());

          /* Store Backup as Weekly Backup if necessary */
          File weeklyBackup = onlineBackupService.getWeeklyBackupFile();
          boolean renameToWeekly = false;
          if (!weeklyBackup.exists()) {
            renameToWeekly = true;
          } else if (weeklyBackup.lastModified() < (System.currentTimeMillis() - MAX_ONLINE_BACKUP_AGE)) {
            renameToWeekly = true;
          }

          /* Atomic Rename */
          DBHelper.rename(tmpBackupFile, renameToWeekly ? weeklyBackup : backupFile);
        } catch (IOException e) {
          throw new PersistenceException(e);
        } finally {
          ProfileFileManager.safeDelete(marker);
          if (tmpBackupFile != null && tmpBackupFile.exists()) {
            ProfileFileManager.safeDelete(tmpBackupFile);
          }
        }
      }
    });

    return onlineBackupService;
  }

  private static boolean shouldOnlineBackup() {
    return System.currentTimeMillis() >= fNextOnlineBackup.get();
  }

  private static boolean delayOnlineBackup() {
    boolean delay = System.currentTimeMillis() >= (fNextOnlineBackup.get() + ONLINE_BACKUP_DELAY_THRESHOLD);

    /* Re-Schedule to the future if delay threshold is hit */
    if (delay) {
      fNextOnlineBackup.set(System.currentTimeMillis() + ONLINE_BACKUP_SHORT_INTERVAL);
    }

    return delay;
  }

  public static long getOnlineBackupDelay(boolean initial) {
    if (initial) {
      return ONLINE_BACKUP_SHORT_INTERVAL;
    }
    return getLongProperty("rssowl.onlinebackup.interval", ONLINE_BACKUP_LONG_INTERVAL); //$NON-NLS-1$
  }

  private static long getLongProperty(String propertyName, long defaultValue) {
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

  public static List<File> getProfileBackups() {
    List<File> backups = new ArrayList<File>();
    File backupDir = new File(Activator.getDefault().getStateLocation().toOSString());

    /* Locate Online Backups */
    String dbName = ProfileFileManager.getDBName();
    File onlineWeeklyBackup = new File(backupDir, dbName + ONLINE_BACKUP_NAME + ".weekly"); //$NON-NLS-1$
    if (onlineWeeklyBackup.exists()) {
      backups.add(onlineWeeklyBackup);
    }

    File onlineDailyBackup = new File(backupDir, dbName + ONLINE_BACKUP_NAME);
    if (onlineDailyBackup.exists()) {
      backups.add(onlineDailyBackup);
    }

    File onlineDailyBackupOlder = new File(backupDir, dbName + ONLINE_BACKUP_NAME + ".0"); //$NON-NLS-1$
    if (onlineDailyBackupOlder.exists()) {
      backups.add(onlineDailyBackupOlder);
    }

    /* Locate Offline Backups */
    File offlineBackup = new File(backupDir, dbName + OFFLINE_BACKUP_NAME);
    if (offlineBackup.exists()) {
      backups.add(offlineBackup);
    }

    File offlineBackupOlder = new File(backupDir, dbName + OFFLINE_BACKUP_NAME + ".0"); //$NON-NLS-1$
    if (offlineBackupOlder.exists()) {
      backups.add(offlineBackupOlder);
    }

    Collections.sort(backups, new Comparator<File>() {
      public int compare(File f1, File f2) {
        return f1.lastModified() > f2.lastModified() ? -1 : 1;
      };
    });

    return backups;
  }

  public static void restoreProfile(File backup) throws PersistenceException {
    Activator.safeLogInfo(NLS.bind("Start: Database Restore from Backup ({0})", backup.getName())); //$NON-NLS-1$

    /* Atomic Rename to "rssowl.db.restore" */
    File db = new File(ProfileFileManager.getDBRestoreFilePath());
    DBHelper.rename(backup, db);

    /* Handle Large Block Size properly */
      ConfigurationHelper.updateLargeBlockSizemarker(db.length());

      Activator.safeLogInfo("End: Database Restore from Backup"); //$NON-NLS-1$
  }

  /* Delete the Profile by Moving it to a Backup */
  public static void backupAndDeleteProfile() {
    File db = new File(ProfileFileManager.getDBFilePath());
    if (!db.exists()) {
      return;
    }

    Activator.safeLogInfo("Start: Backup and Delete Profile"); //$NON-NLS-1$

//    /*
//     * Object Container might be opened, so try to close (only for testing, not
//     * in production)
//     */
//    if (InternalOwl.TESTING) {
//      fDBManager.shutdown();
//    }

    /* Find Suitable Backup Name */
    int i = 0;
    String dbName = ProfileFileManager.getDBName();
    File backupDir = new File(Activator.getDefault().getStateLocation().toOSString());
    File backupCandidate = new File(backupDir, dbName + RESTORE_BACKUP_NAME);
    while (backupCandidate.exists()) {
      backupCandidate = new File(backupDir, dbName + RESTORE_BACKUP_NAME + "." + i++); //$NON-NLS-1$
    }

    /* Atomic Rename */
    DBHelper.rename(db, backupCandidate);

    Activator.safeLogInfo(NLS.bind("End: Backup and Delete Profile ({0})", backupCandidate.getName())); //$NON-NLS-1$
  }
}

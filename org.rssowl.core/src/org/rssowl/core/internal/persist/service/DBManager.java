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

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.persist.migration.MigrationHelper;
import org.rssowl.core.internal.persist.search.IndexHelper;
import org.rssowl.core.persist.service.DiskFullException;
import org.rssowl.core.persist.service.InsufficientFilePermissionException;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.persist.service.ProfileLockedException;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.LongOperationMonitor;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;
import com.db4o.ext.DatabaseFileLockedException;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The central class to configure the underlying database of RSSOwl.
 */
public class DBManager {
  private static DBManager fInstance;
  private ObjectContainer fObjectContainer;
  private final ReadWriteLock fLock = new ReentrantReadWriteLock();
  private final List<DatabaseListener> fEntityStoreListeners = new CopyOnWriteArrayList<DatabaseListener>();

  /**
   * @return The Singleton Instance.
   */
  public static DBManager getDefault() {
    if (fInstance == null) {
      fInstance = new DBManager();
    }
    return fInstance;
  }

  public final ObjectContainer getObjectContainer() {
    return fObjectContainer;
  }

  /*
   * @see org.rssowl.core.internal.persist.service.IDBManager#shutdown()
   */
  public void shutdown() throws PersistenceException {
    fLock.writeLock().lock();
    try {
      fireDatabaseEvent(new DatabaseEvent(fObjectContainer, fLock), false);
      if (fObjectContainer != null) {
        while (!fObjectContainer.close()) {
          ;
        }
      }
    } finally {
      fLock.writeLock().unlock();
    }
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
    File restoreDBFile = new File(ProfileFileManager.getDBRestoreFilePath());
    if (!forRestore && restoreDBFile.exists()) {
      Activator.safeLogInfo("Start: Restoring a Backup (renaming rssowl.db.restore to rssowl.db"); //$NON-NLS-1$

      /* Backup and Delete current profile */
      BackupHelper.backupAndDeleteProfile();

      /* Atomic Rename Restore to Profile */
      DBHelper.rename(restoreDBFile, new File(ProfileFileManager.getDBFilePath()));

      Activator.safeLogInfo("End: Restoring a Backup (renaming rssowl.db.restore to rssowl.db"); //$NON-NLS-1$
    }

    /* Create Database */
    createDatabase(monitor, emergency, forRestore);
  }

  @SuppressWarnings("unused")
  private void createDatabase(LongOperationMonitor progressMonitor, boolean emergency, boolean forRestore) throws PersistenceException {

    /* Assert File Permissions */
    ProfileFileManager.checkDirPermissions();

    try {

      /* Migration and Defragment only apply to non-emergency situations */
      if (!emergency) {
        MigrationHelper.migrateIfNecessary(progressMonitor);
        DefragmentHelper.defragmentIfNecessary(progressMonitor);
      }

      /* Open the DB */
      Configuration config = ConfigurationHelper.createConfiguration(false, false);
      createObjectContainer(config, forRestore);

      /* Notify Listeners that DB is opened */
      fireDatabaseEvent(new DatabaseEvent(fObjectContainer, fLock), true);

      /*
       * Model Search Reindex or Cleanup only applies to non-emergency
       * situations
       */
      if (!emergency) {
        IndexHelper.processIndex(progressMonitor);
        BackupHelper.createBackupService();
      }
    } finally {
      progressMonitor.done();
    }
  }

  private void createObjectContainer(Configuration config, boolean forRestore) throws PersistenceException {
    try {

      /* Open DB */
      String dbName = forRestore ? ProfileFileManager.getDBRestoreFilePath() : ProfileFileManager.getDBFilePath();
      fObjectContainer = Db4o.openFile(config, dbName);

      /* Handle Fatal Error while opening DB */
      if (fObjectContainer == null) {
        throw new PersistenceException(Messages.DBManager_UNABLE_TO_OPEN_PROFILE);
      }

      /* Keep date of last successfull profile opened */
      storeProfileLastUsed();
    }

    /* Error opening the DB */
    catch (Throwable e) {

      /* Generic Error */
      if (e instanceof Error) {
        throw (Error) e;
      }

      /* Persistence Exception */
      if (e instanceof PersistenceException) {
        throw (PersistenceException) e;
      }

      /* Profile locked by another running instance */
      if (e instanceof DatabaseFileLockedException) {
        throw new ProfileLockedException(e.getMessage(), e);
      }

      File file = new File(ProfileFileManager.getDBFilePath());

      /* Disk Full Error */
      if (!file.exists()) {
        throw new DiskFullException(Messages.DBManager_DISK_FULL_ERROR, e);
      }

      /* Permission Error */
      if (!file.canRead() || (!file.canWrite())) {
        throw new InsufficientFilePermissionException(NLS.bind(Messages.DBManager_FILE_PERMISSION_ERROR, file), null);
      }

      /* Any other Error */
      throw new PersistenceException(e);
    }
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

  public void addEntityStoreListener(DatabaseListener listener) {
    if (listener instanceof EventManager) {
      fEntityStoreListeners.add(0, listener);
    } else if (listener instanceof DB4OIDGenerator) {
      if (!fEntityStoreListeners.isEmpty() && fEntityStoreListeners.get(0) instanceof EventManager) {
        fEntityStoreListeners.add(1, listener);
      } else {
        fEntityStoreListeners.add(0, listener);
      }
    } else {
      fEntityStoreListeners.add(listener);
    }
  }

  public void removeEntityStoreListener(DatabaseListener listener) {
    fEntityStoreListeners.remove(listener);
  }

  private void storeProfileLastUsed() {
    File file = ProfileFileManager.getDBLastUsedFile();
    try {
      if (!file.exists()) {
        ProfileFileManager.safeCreate(file);
      }
      DBHelper.writeToFile(file, String.valueOf(System.currentTimeMillis()));
    } catch (Exception e) {
      /* Ignore */
    }
  }

  void dropDatabaseForTests() throws PersistenceException {
    SafeRunner.run(new LoggingSafeRunnable() {
      public void run() throws Exception {

        /* Shutdown DB */
        shutdown();

        /* Delete DB */
        File dbFile = new File(ProfileFileManager.getDBFilePath());
        if (dbFile.exists() && !dbFile.delete()) {
          Activator.getDefault().logError("Failed to delete db file", null); //$NON-NLS-1$
        }

        /* Delete other marker files */
//        FileManager.delete(getDefragmentFile(), getCleanUpIndexFile(), getReIndexFile(), getReindexMarkerFile(), getDBFormatFile());
      }
    });
  }
}
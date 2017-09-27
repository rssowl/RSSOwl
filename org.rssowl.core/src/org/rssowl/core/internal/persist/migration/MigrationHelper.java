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

package org.rssowl.core.internal.persist.migration;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.persist.service.BackupService;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;
import org.rssowl.core.internal.persist.service.ConfigurationHelper;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.internal.persist.service.ProfileFileManager;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.LongOperationMonitor;

import com.db4o.config.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Some helpers around Migrations.
 */
public class MigrationHelper {

  /* Migration Related */
  private static final boolean ENABLE_MIGRATION = false; //Turned off as of RSSOwl 2.1
  private static final String FORMAT_FILE_NAME = "format2"; //$NON-NLS-1$

  public static void migrateIfNecessary(LongOperationMonitor progressMonitor) {

    /* Check for Migration */
    int workspaceVersion = getWorkspaceFormatVersion();

    /* Perform Migration if necessary */
    if (ENABLE_MIGRATION && workspaceVersion != getCurrentFormatVersion()) {
      progressMonitor.beginLongOperation(false);
      migrate(progressMonitor, workspaceVersion, getCurrentFormatVersion());
    }
  }

  private static void migrate(LongOperationMonitor progressMonitor, final int workspaceFormat, int currentFormat) {
    Activator.safeLogInfo(NLS.bind("Migrating RSSOwl (from version {0} to version {1}", workspaceFormat, currentFormat)); //$NON-NLS-1$

    ConfigurationFactory configFactory = new ConfigurationFactory() {
      public Configuration createConfiguration() {
        return ConfigurationHelper.createConfiguration(false, false);
      }
    };
    IMigration migration = new Migrations().getMigration(workspaceFormat, currentFormat);
    if (migration == null) {
      throw new PersistenceException("It was not possible to migrate your data to the current version of RSSOwl. Migrations are supported between final versions and between consecutive milestones. In other words, 2.0M7 to 2.0M8 and 2.0 to 2.1 are supported but 2.0M6 to 2.0M8 is not supported. In the latter case, you would need to launch 2.0M7 and then 2.0M8 to be able to use that version. Migration was attempted from originFormat: " + workspaceFormat + " to destinationFormat: " + currentFormat); //$NON-NLS-1$ //$NON-NLS-2$
    }

    final File dbFile = new File(ProfileFileManager.getDBFilePath());
    final String backupFileSuffix = ".mig."; //$NON-NLS-1$

    /*
     * Copy the db file to a permanent back-up where the file name includes the
     * workspaceFormat number. This will only be deleted after another
     * migration.
     */
    final BackupService backupService = new BackupService(dbFile, backupFileSuffix + workspaceFormat, 1);
    backupService.setLayoutStrategy(new BackupService.BackupLayoutStrategy() {
      public List<File> findBackupFiles() {
        List<File> backupFiles = new ArrayList<File>(3);
        for (int i = workspaceFormat; i >= 0; --i) {
          File file = new File(dbFile.getAbsoluteFile() + backupFileSuffix + i);
          if (file.exists()) {
            backupFiles.add(file);
          }
        }
        return backupFiles;
      }

      public void rotateBackups(List<File> backupFiles) {
        throw new UnsupportedOperationException("No rotation supported because maxBackupCount is 1"); //$NON-NLS-1$
      }
    });
    backupService.backup(true, new NullProgressMonitor());

    /* Create a copy of the db file to use for the migration */
    File migDbFile = backupService.getTempBackupFile();
    DBHelper.copyFileNIO(dbFile, migDbFile);

    /* Migrate the copy */
    migration.migrate(configFactory, migDbFile.getAbsolutePath(), progressMonitor);

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
    if (getOldDBFormatFile().exists()) {
      getOldDBFormatFile().delete();
    }
  }

  private static File getOldDBFormatFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File formatFile = new File(dir, "format"); //$NON-NLS-1$
    return formatFile;
  }

  private static int getWorkspaceFormatVersion() {
    boolean dbFileExists = new File(ProfileFileManager.getDBFilePath()).exists();
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
      if (!formatFileExists) {
        return 0;
      }

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

  private static void setFormatVersion(File formatFile) {
    DBHelper.writeToFile(formatFile, String.valueOf(getCurrentFormatVersion()));
  }

  private static int getCurrentFormatVersion() {
    return 5;
  }

  public static File getDBFormatFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File formatFile = new File(dir, FORMAT_FILE_NAME);
    return formatFile;
  }

  static Object getFieldValue(Object object, String fieldName) {
    try {
      return getField(object, fieldName).get(object);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }

  static Field getField(Object object, String fieldName) {
    Class<?> klass = object.getClass();
    while (klass != Object.class) {
      for (Field field : klass.getDeclaredFields()) {
        if (field.getName().equals(fieldName)) {
          field.setAccessible(true);
          return field;
        }
      }
      klass = klass.getSuperclass();
    }
    throw new IllegalArgumentException("No field with name: " + fieldName); //$NON-NLS-1$
  }

  static void setField(Object object, String fieldName, Object value) {
    try {
      Field field = getField(object, fieldName);
      field.set(object, value);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
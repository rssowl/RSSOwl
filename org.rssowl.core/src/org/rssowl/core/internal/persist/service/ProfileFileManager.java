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

import org.eclipse.osgi.util.NLS;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.persist.service.InsufficientFilePermissionException;
import org.rssowl.core.util.Pair;

import java.io.File;

/**
 * This class stores information about all files in profile directory: database,
 * backups, metadata, action files (like reindex and defragment)
 *
 * @author MuxaJIbI4
 */
public final class ProfileFileManager {

  private static final String DB_NAME = "rssowl.db"; //$NON-NLS-1$
  private static final String DB_RESTORE_NAME = "rssowl.db.restore"; //$NON-NLS-1$
  private static final String TMP_BACKUP_NAME = "tmp.bak"; //$NON-NLS-1$

  public static void checkDirPermissions() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    if (!dir.canRead() || (!dir.canWrite())) {
      throw new InsufficientFilePermissionException(NLS.bind(Messages.DBManager_DIRECTORY_PERMISSION_ERROR, dir), null);
    }
  }

  /**
   * Internal method, exposed for tests only.
   *
   * @return the path to the db file.
   */
  public static String getDBFilePath() {
    String filePath = Activator.getDefault().getStateLocation().toOSString() + File.separator + DB_NAME;
    return filePath;
  }

  public static String getDBName() {
    return DB_NAME;
  }

  public static String getTempFile() {
    return TMP_BACKUP_NAME;
  }

  /**
   * Internal method, exposed for tests only.
   *
   * @return the path to the db file.
   */
  public static String getDBRestoreFilePath() {
    String filePath = Activator.getDefault().getStateLocation().toOSString() + File.separator + DB_RESTORE_NAME;
    return filePath;
  }

  public static File getDBLastUsedFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    File lastDBUseFile = new File(dir, "lastused"); //$NON-NLS-1$
    return lastDBUseFile;
  }

  public static Pair<File, Long> getProfile() {
    File profile = new File(ProfileFileManager.getDBFilePath());
    Long timestamp = getProfileLastUsed();

    return Pair.create(profile, timestamp);
  }

  static Long getProfileLastUsed() {
    File file = ProfileFileManager.getDBLastUsedFile();
    if (file.exists()) {
      try {
        return Long.parseLong(DBHelper.readFirstLineFromFile(file));
      } catch (Exception e) {
        /* Ignore */
      }
    }

    return null;
  }

  public static void delete(File... files) {
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.exists()) {
        safeDelete(file);
      }
    }
  }

  public static void safeCreate(File file) {
    try {
      file.createNewFile();
    } catch (Exception e) {
      /* Ignore */
    }
  }

  public static void safeDelete(File file) {
    try {
      file.delete();
    } catch (Exception e) {
      /* Ignore */
    }
  }

}

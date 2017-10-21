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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.rssowl.core.Owl.StartLevel;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.persist.service.AbstractPersistenceService;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.LongOperationMonitor;
import org.rssowl.core.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The persistence service controls the lifecycle of the underlying database.
 *
 * @author bpasero
 */
public class PersistenceServiceImpl extends AbstractPersistenceService {

  /** Default Constructor */
  public PersistenceServiceImpl() {}

  /*
   * @see
   * org.rssowl.core.persist.service.AbstractPersistenceService#startup(org.
   * rssowl.core.util.LongOperationMonitor, boolean, boolean)
   */
  @Override
  public void startup(LongOperationMonitor monitor, boolean emergency, boolean forRestore) {
    super.startup(monitor, emergency, forRestore);

    /* Startup DB */
    DBManager.getDefault().startup(monitor, emergency, forRestore);
    InternalOwl.getDefault().setStartLevel(StartLevel.DB_OPENED);

    /* Startup Model Search (not in case of emergency) */
    if (!emergency) {
      getModelSearch().startup();
      InternalOwl.getDefault().setStartLevel(StartLevel.SEARCH_INDEX_OPENED);
    }
  }

  /*
   * @see org.rssowl.core.persist.service.IPersistenceService#shutdown(boolean)
   */
  @Override
  public void shutdown(boolean emergency) throws PersistenceException {

    /* Shutdown ID Generator, Search and DB */
    if (!emergency) {

      /* ID Generator (safely) */
      try {
        getIDGenerator().shutdown();
      } catch (Exception e) {
        Activator.safeLogError(e.getMessage(), e);
      }

      /* Search (safely) */
      try {
        getModelSearch().shutdown(emergency);
      } catch (Exception e) {
        Activator.safeLogError(e.getMessage(), e);
      }

      /* DB */
      DBManager.getDefault().shutdown();
    }

    /* Emergent Exit: Shutdown DB and Search */
    else {

      /* DB (safely) */
      try {
        DBManager.getDefault().shutdown();
      } catch (Exception e) {
        Activator.safeLogError(e.getMessage(), e);
      }

      /* Search */
      getModelSearch().shutdown(emergency);
    }
  }

  /**
   * Instructs the persistence service to schedule an optimization run during
   * the next time the application is started. The actual optimization type is
   * dependent on the persistence system being used and implementors are free to
   * leave this as a no-op in case the the persistence system tunes itself
   * automatically during runtime.
   *
   * @throws PersistenceException in case a problem occurs while trying to
   * schedule this operation.
   */
  @Override
  public void defragmentOnNextStartup() throws PersistenceException {
    try {
      DBManager.getDefault().getDefragmentFile().createNewFile();
    } catch (IOException e) {
      throw new PersistenceException(e);
    }
  }

  /**
   * Returns the profile {@link File} that contains all data and the
   * {@link Long} timestamp when it was last successfully used.
   *
   * @return the profile {@link File} and the {@link Long} timestamp when it was
   * last successfully used.
   */
  public Pair<File, Long> getProfile() {
    return DBManager.getDefault().getProfile();
  }

  /**
   * Provides a list of available backups for the user to restore from in case
   * of an unrecoverable error.
   *
   * @return a list of available backups for the user to restore from in case of
   * an unrecoverable error.
   */
  public List<File> getProfileBackups() {
    return DBManager.getDefault().getProfileBackups();
  }

  /**
   * Will rename the provided backup file to the operational RSSOwl profile
   * database.
   *
   * @param backup the backup {@link File} to restore from.
   * @throws PersistenceException in case a problem occurs while trying to
   * execute this operation.
   */
  public void restoreProfile(File backup) throws PersistenceException {
    DBManager.getDefault().restoreProfile(backup);
  }

  /**
   * Recreate the Profile of the persistence layer. In case of a Database, this
   * would drop relations and create them again.
   *
   * @param needsEmergencyStartup if <code>true</code> causes this method to
   * also trigger an emergency startup so that other operations can be normally
   * done afterwards like importing from a OPML backup.
   * @throws PersistenceException In case of an error while starting up the
   * persistence layer.
   */
  public void recreateProfile(boolean needsEmergencyStartup) throws PersistenceException {
    Activator.safeLogInfo(needsEmergencyStartup ? "Start: Recreate Profile with OPML Import" : "Start: Start Over with Fresh Profile"); //$NON-NLS-1$ //$NON-NLS-2$

    /* First check to delete the "rssowl.db.restore" file that is being used */
    File restoreDBFile = new File(DBManager.getDBRestoreFilePath());
    if (restoreDBFile.exists())
      restoreDBFile.delete();

    /* Delete the large blocksize marker if present because we start with an empty profile again */
    File largeBlockSizeMarkerFile = DBManager.getLargeBlockSizeMarkerFile();
    if (largeBlockSizeMarkerFile.exists())
      largeBlockSizeMarkerFile.delete();

    /* Communicate shutdown to listeners (mandatory before reopening for restore) */
    DBManager.getDefault().shutdown();

    /* Open new empty DB for restore */
    if (!needsEmergencyStartup)
      DBManager.getDefault().startup(new LongOperationMonitor(new NullProgressMonitor()) {}, true, true);

    /* Otherwise if startup is needed, startup with empty DB */
    else
      InternalOwl.getDefault().startup(new LongOperationMonitor(new NullProgressMonitor()) {}, true, true);

    /* Reindex on next startup */
    InternalOwl.getDefault().getPersistenceService().getModelSearch().reIndexOnNextStartup();

    Activator.safeLogInfo(needsEmergencyStartup ? "End: Recreate Profile with OPML Import" : "End: Start Over with Fresh Profile"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Only used from tests to get a clean start for each test case.
   *
   * @throws PersistenceException
   */
  public void recreateSchemaForTests() throws PersistenceException {
    DBManager.getDefault().dropDatabaseForTests();
    DBManager.getDefault().startup(new LongOperationMonitor(new NullProgressMonitor()) {}, true, false);

    getModelSearch().clearIndex();
  }

}
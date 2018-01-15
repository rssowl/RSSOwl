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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;
import org.rssowl.core.internal.persist.service.Migration;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This object is responsible for holding the currently supported migrations and
 * making them available on request.
 */
public final class Migrations {

  public static class ChainedMigration implements Migration {
    private final List<Migration> fMigrations;
    private final int fDestinationFormat;
    private final int fOriginFormat;

    public ChainedMigration(int originFormat, int destinationFormat, List<Migration> migrations) {
      fOriginFormat = originFormat;
      fDestinationFormat = destinationFormat;
      fMigrations = migrations;
    }

    /*
     * @see
     * org.rssowl.core.internal.persist.service.Migration#getDestinationFormat()
     */
    @Override
    public int getDestinationFormat() {
      return fDestinationFormat;
    }

    /*
     * @see org.rssowl.core.internal.persist.service.Migration#getOriginFormat()
     */
    @Override
    public int getOriginFormat() {
      return fOriginFormat;
    }

    public List<Migration> getMigrations() {
      return Collections.unmodifiableList(fMigrations);
    }

    /*
     * @see
     * org.rssowl.core.internal.persist.service.Migration#migrate(org.rssowl
     * .core.internal.persist.service.ConfigurationFactory, java.lang.String,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public MigrationResult migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor) {
      boolean reindex = false;
      boolean optimize = false;
      boolean defragment = false;
      for (Migration migration : fMigrations) {
        MigrationResult migrationResult = migration.migrate(configFactory, dbFileName, progressMonitor);
        reindex |= migrationResult.isReindex();
        optimize |= migrationResult.isOptimizeIndex();
        defragment |= migrationResult.isDefragmentDatabase();
      }
      return new MigrationResult(reindex, optimize, defragment);
    }

  }

  private final List<Migration> fMigrations;

  /**
   * Creates an instance of this object.
   */
  public Migrations() {
    fMigrations = Arrays.<Migration> asList(new Migration2To3(), new Migration3To4(), new Migration4To5(), new Migration2To5());
  }

  public Migrations(Migration... migrations) {
    fMigrations = Arrays.asList(migrations);
  }

  /**
   * @return the {@link List} of migrations.
   */
  public List<Migration> getMigrations() {
    return Collections.unmodifiableList(fMigrations);
  }

  /**
   * Returns a Migration that satisfies {@code originFormat} and
   * {@code destinationFormat} or {@code null} if none can be found.
   *
   * @param originFormat The current format of the database.
   * @param destinationFormat The desired format of the database.
   * @return a Migration or {@code} null.
   */
  public final Migration getMigration(int originFormat, int destinationFormat) {
    Assert.isLegal(originFormat < destinationFormat, "Only forward migrations supported currently, originFormat: " + originFormat + ", destinationFormat: " + destinationFormat); //$NON-NLS-1$ //$NON-NLS-2$
    Migration migration = doGetMigration(originFormat, destinationFormat);
    if (migration != null)
      return migration;

    List<Migration> chainedMigrations = findChainedMigrations(originFormat, destinationFormat);
    if (chainedMigrations.isEmpty())
      return null;

    return new ChainedMigration(originFormat, destinationFormat, chainedMigrations);
  }

  private Migration doGetMigration(int originFormat, int destinationFormat) {
    for (Migration migration : fMigrations) {
      if (migration.getOriginFormat() == originFormat && migration.getDestinationFormat() == destinationFormat) {
        return migration;
      }
    }
    return null;
  }

  private List<Migration> findChainedMigrations(int originFormat, int destinationFormat) {
    LinkedList<LinkedList<Migration>> migrationsQueues = createMigrationsQueues(originFormat, destinationFormat);

    List<Migration> smallestQueue = Collections.emptyList();
    for (LinkedList<Migration> migrationQueue : migrationsQueues) {
      if (smallestQueue.isEmpty() || migrationQueue.size() < smallestQueue.size())
        smallestQueue = migrationQueue;
    }

    return smallestQueue;
  }

  private LinkedList<LinkedList<Migration>> createMigrationsQueues(int originFormat, int destinationFormat) {
    LinkedList<LinkedList<Migration>> migrationQueues = new LinkedList<LinkedList<Migration>>();
    for (Migration migration : fMigrations) {
      if (migration.getOriginFormat() == originFormat) {
        LinkedList<Migration> migrationQueue = new LinkedList<Migration>();
        migrationQueue.add(migration);
        migrationQueues.add(migrationQueue);
      }
    }
    return findMigrationQueues(migrationQueues, destinationFormat);
  }

  private LinkedList<LinkedList<Migration>> findMigrationQueues(LinkedList<LinkedList<Migration>> migrationQueues, int destinationFormat) {
    boolean changed = false;
    LinkedList<LinkedList<Migration>> migrationQueuesCopy = new LinkedList<LinkedList<Migration>>(migrationQueues);
    for (ListIterator<LinkedList<Migration>> it = migrationQueuesCopy.listIterator(); it.hasNext();) {
      LinkedList<Migration> migrationQueue = it.next();
      Migration migration = migrationQueue.getLast();
      if (migration.getDestinationFormat() == destinationFormat)
        continue;

      it.remove();

      for (Migration innerMigration : fMigrations) {
        if (migration.equals(innerMigration))
          continue;

        if (migration.getDestinationFormat() == innerMigration.getOriginFormat()) {
          changed = true;
          LinkedList<Migration> newMigrationQueue = new LinkedList<Migration>(migrationQueue);
          newMigrationQueue.add(innerMigration);
          it.add(newMigrationQueue);
        }

      }
    }

    if (changed)
      return findMigrationQueues(migrationQueuesCopy, destinationFormat);

    return migrationQueuesCopy;
  }
}
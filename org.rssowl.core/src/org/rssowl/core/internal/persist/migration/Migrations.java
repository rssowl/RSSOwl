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

  private final List<IMigration> fMigrations;

  /**
   * Creates an instance of this object.
   */
  public Migrations() {
    fMigrations = Arrays.<IMigration> asList(new Migration2To3(), new Migration3To4(), new Migration4To5(), new Migration2To5());
  }

  public Migrations(IMigration... migrations) {
    fMigrations = Arrays.asList(migrations);
  }

  /**
   * @return the {@link List} of migrations.
   */
  public List<IMigration> getMigrations() {
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
  public final IMigration getMigration(int originFormat, int destinationFormat) {
    Assert.isLegal(originFormat < destinationFormat, "Only forward migrations supported currently, originFormat: " + originFormat + ", destinationFormat: " + destinationFormat); //$NON-NLS-1$ //$NON-NLS-2$
    IMigration migration = doGetMigration(originFormat, destinationFormat);
    if (migration != null)
      return migration;

    List<IMigration> chainedMigrations = findChainedMigrations(originFormat, destinationFormat);
    if (chainedMigrations.isEmpty())
      return null;

    return new ChainedMigration(originFormat, destinationFormat, chainedMigrations);
  }

  private IMigration doGetMigration(int originFormat, int destinationFormat) {
    for (IMigration migration : fMigrations) {
      if (migration.getOriginFormat() == originFormat && migration.getDestinationFormat() == destinationFormat) {
        return migration;
      }
    }
    return null;
  }

  private List<IMigration> findChainedMigrations(int originFormat, int destinationFormat) {
    LinkedList<LinkedList<IMigration>> migrationsQueues = createMigrationsQueues(originFormat, destinationFormat);

    List<IMigration> smallestQueue = Collections.emptyList();
    for (LinkedList<IMigration> migrationQueue : migrationsQueues) {
      if (smallestQueue.isEmpty() || migrationQueue.size() < smallestQueue.size())
        smallestQueue = migrationQueue;
    }

    return smallestQueue;
  }

  private LinkedList<LinkedList<IMigration>> createMigrationsQueues(int originFormat, int destinationFormat) {
    LinkedList<LinkedList<IMigration>> migrationQueues = new LinkedList<LinkedList<IMigration>>();
    for (IMigration migration : fMigrations) {
      if (migration.getOriginFormat() == originFormat) {
        LinkedList<IMigration> migrationQueue = new LinkedList<IMigration>();
        migrationQueue.add(migration);
        migrationQueues.add(migrationQueue);
      }
    }
    return findMigrationQueues(migrationQueues, destinationFormat);
  }

  private LinkedList<LinkedList<IMigration>> findMigrationQueues(LinkedList<LinkedList<IMigration>> migrationQueues, int destinationFormat) {
    boolean changed = false;
    LinkedList<LinkedList<IMigration>> migrationQueuesCopy = new LinkedList<LinkedList<IMigration>>(migrationQueues);
    for (ListIterator<LinkedList<IMigration>> it = migrationQueuesCopy.listIterator(); it.hasNext();) {
      LinkedList<IMigration> migrationQueue = it.next();
      IMigration migration = migrationQueue.getLast();
      if (migration.getDestinationFormat() == destinationFormat)
        continue;

      it.remove();

      for (IMigration innerMigration : fMigrations) {
        if (migration.equals(innerMigration))
          continue;

        if (migration.getDestinationFormat() == innerMigration.getOriginFormat()) {
          changed = true;
          LinkedList<IMigration> newMigrationQueue = new LinkedList<IMigration>(migrationQueue);
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
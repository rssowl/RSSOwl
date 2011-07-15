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
package org.rssowl.core.tests.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Test;
import org.rssowl.core.internal.persist.migration.MigrationResult;
import org.rssowl.core.internal.persist.migration.Migrations;
import org.rssowl.core.internal.persist.migration.Migrations.ChainedMigration;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;
import org.rssowl.core.internal.persist.service.Migration;

/**
 * Tests for Migrations.
 */
public class MigrationsTest {

  private static class MigrationImpl implements Migration   {
    private final int fOrigin;
    private final int fDestination;

    MigrationImpl(int origin, int destination)  {
      fOrigin = origin;
      fDestination = destination;
    }

    public int getDestinationFormat() {
      return fDestination;
    }

    public int getOriginFormat() {
      return fOrigin;
    }

    public MigrationResult migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return "(" + fOrigin + ", " + fDestination + ")";
    }
  }

  /**
   * Test if no migration is possible
   */
  @Test
  public void testNoMigrationPossible() {
    Migrations migrations = new Migrations(new MigrationImpl(2, 3), new MigrationImpl(3, 4));
    Migration migration = migrations.getMigration(1, 4);
    assertNull(migration);
  }

  /**
   * Tests simple migration scenarios.
   */
  @Test
  public void testGetMigration() {
    Migrations migrations = new Migrations(new MigrationImpl(1, 2), new MigrationImpl(2, 3), new MigrationImpl(3, 4));
    Migration migration = migrations.getMigration(2, 4);
    assertEquals(ChainedMigration.class, migration.getClass());
    ChainedMigration chainedMigration = (ChainedMigration) migration;

    assertEquals(2, chainedMigration.getMigrations().size());
    assertEquals(migrations.getMigrations().get(1), chainedMigration.getMigrations().get(0));

    migration = migrations.getMigration(2, 3);
    assertFalse(migration instanceof ChainedMigration);
    assertEquals(migrations.getMigrations().get(1), migration);
  }

  /**
   * Tests a more complex example of migration where there are multiple possible
   * paths to the goal to make sure we choose the shortest one.
   */
  @Test
  public void testGetMigrationComplex() {
    /* More complex example */
    Migrations migrations = new Migrations(new MigrationImpl(1, 2), new MigrationImpl(2, 3),
        new MigrationImpl(3, 4), new MigrationImpl(4, 6), new MigrationImpl(3, 5),
        new MigrationImpl(2, 4), new MigrationImpl(5, 6), new MigrationImpl(7, 9),
        new MigrationImpl(7, 8), new MigrationImpl(8, 9), new MigrationImpl(6, 8));

    Migration migration = migrations.getMigration(2, 8);

    assertEquals(ChainedMigration.class, migration.getClass());
    ChainedMigration chainedMigration = (ChainedMigration) migration;
    assertEquals(3, chainedMigration.getMigrations().size());
    assertEquals(migrations.getMigrations().get(5), chainedMigration.getMigrations().get(0));
    assertEquals(migrations.getMigrations().get(3), chainedMigration.getMigrations().get(1));
    assertEquals(migrations.getMigrations().get(10), chainedMigration.getMigrations().get(2));
  }
}

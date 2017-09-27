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

package org.rssowl.core.internal.persist.migration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;

import java.util.Collections;
import java.util.List;

/**
 * Aggregates list of <code>IMigration</code> migrations that will be performed
 * one by another
 */
public class ChainedMigration implements IMigration {
  private final List<IMigration> fMigrations;
  private final int fDestinationFormat;
  private final int fOriginFormat;

  public ChainedMigration(int originFormat, int destinationFormat, List<IMigration> migrations) {
    fOriginFormat = originFormat;
    fDestinationFormat = destinationFormat;
    fMigrations = migrations;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.migration.IMigration#getDestinationFormat
   * ()
   */
  public int getDestinationFormat() {
    return fDestinationFormat;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.migration.IMigration#getOriginFormat()
   */
  public int getOriginFormat() {
    return fOriginFormat;
  }

  /*
   * @see org.rssowl.core.internal.persist.service.Migration#migrate(org.rssowl
   * .core.internal.persist.service.ConfigurationFactory, java.lang.String,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public void migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor) {
    for (IMigration migration : fMigrations) {
      migration.migrate(configFactory, dbFileName, progressMonitor);
    }
  }

  public List<IMigration> getMigrations() {
    return Collections.unmodifiableList(fMigrations);
  }
}

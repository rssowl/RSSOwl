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
import org.rssowl.core.internal.persist.migration.MigrationResult;

/**
 * Implementations of this interface are able to migrate the database data from
 * one format version to another.
 */
public interface Migration {
  /**
   * @return the format version that the implementation can migrate from.
   */
  int getOriginFormat();

  /**
   * @return the format version that the implementation can migrate to.
   */
  int getDestinationFormat();

  /**
   * Perform the migration. Implementations are responsible for making sure that
   * all object containers are closed at the end of the script.
   *
   * @param configFactory
   * @param dbFileName
   * @param progressMonitor
   * @return MigrationResult indicating actions that should be performed at some
   * point after the migration.
   */
  MigrationResult migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor);
}
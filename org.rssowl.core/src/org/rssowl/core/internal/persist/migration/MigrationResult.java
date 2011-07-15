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

/**
 * Some flags to control what should happen after Migration.
 */
public final class MigrationResult {
  private final boolean reindex;
  private final boolean optimizeIndex;
  private final boolean defragmentDatabase;

  public MigrationResult(boolean reindex, boolean optimizeIndex, boolean defragmentDatabase) {
    this.reindex = reindex;
    this.optimizeIndex = optimizeIndex;
    this.defragmentDatabase = defragmentDatabase;
  }

  /**
   * @return <code>true</code> if the lucene index should be reindexed.
   */
  public final boolean isReindex() {
    return reindex;
  }

  /**
   * @return <code>true</code> if the lucene index should be optimized.
   */
  public final boolean isOptimizeIndex() {
    return optimizeIndex;
  }

  /**
   * @return <code>true</code> if the database should be defragmented.
   */
  public final boolean isDefragmentDatabase() {
    return defragmentDatabase;
  }
}
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.Migration;

import java.io.File;

/**
 * Migration from version 4 (nightly of 17-Jan-2008) to version 5 (builds from
 * nightly of 03-Feb-2008 to 2.0M8).
 */
public class Migration4To5 implements Migration {

  /*
   * @see
   * org.rssowl.core.internal.persist.service.Migration#getDestinationFormat()
   */
  @Override
  public int getDestinationFormat() {
    return 5;
  }

  /*
   * @see org.rssowl.core.internal.persist.service.Migration#getOriginFormat()
   */
  @Override
  public int getOriginFormat() {
    return 4;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.service.Migration#migrate(org.rssowl.core
   * .internal.persist.service.ConfigurationFactory, java.lang.String,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public MigrationResult migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor) {
    File dbLastBackUpFile = DBManager.getDefault().getDBLastBackUpFile();
    dbLastBackUpFile.delete();

    return new MigrationResult(true, false, true);
  }
}
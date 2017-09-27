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
import org.rssowl.core.internal.persist.service.BackupHelper;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;

import java.io.File;

/**
 * Migration from version 2 (2.0M7) to version 5 (builds from nightly of
 * 03-Feb-2008 to 2.0M8).
 */
public class Migration2To5 implements IMigration {

  /*
   * @see
   * org.rssowl.core.internal.persist.migration.IMigration#getDestinationFormat
   * ()
   */
  public int getDestinationFormat() {
    return 5;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.migration.IMigration#getOriginFormat()
   */
  public int getOriginFormat() {
    return 2;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.migration.IMigration#migrate(org.rssowl
   * .core.internal.persist.service.ConfigurationFactory, java.lang.String,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public void migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor) {
    final int totalProgress = 100;
    int totalProgressIncremented = 0;
    progressMonitor.beginTask(Messages.Migration2To5_MIGRATING_DATA, totalProgress);

    ObjectContainer oc = Db4o.openFile(configFactory.createConfiguration(), dbFileName);

    totalProgressIncremented = Migration2To3.migrate(progressMonitor, totalProgress, totalProgressIncremented, oc);
    oc.commit();
    oc.close();

    File dbLastBackUpFile = BackupHelper.getDBLastBackUpFile();
    dbLastBackUpFile.delete();

    progressMonitor.worked(totalProgress - totalProgressIncremented);
  }
}
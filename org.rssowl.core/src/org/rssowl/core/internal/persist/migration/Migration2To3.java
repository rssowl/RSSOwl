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
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;
import org.rssowl.core.internal.persist.service.Migration;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;

import java.util.List;

/**
 * Migration from version 2 (2.0M7) to version 3 (nightly from 13-Jan-2008).
 */
public class Migration2To3 implements Migration {

  /*
   * @see
   * org.rssowl.core.internal.persist.service.Migration#getDestinationFormat()
   */
  @Override
  public int getDestinationFormat() {
    return 3;
  }

  /*
   * @see org.rssowl.core.internal.persist.service.Migration#getOriginFormat()
   */
  @Override
  public int getOriginFormat() {
    return 2;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.service.Migration#migrate(org.rssowl.core
   * .internal.persist.service.ConfigurationFactory, java.lang.String,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public MigrationResult migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor) {
    final int totalProgress = 100;
    int totalProgressIncremented = 0;
    progressMonitor.beginTask(Messages.Migration2To3_MIGRATING_DATA, totalProgress);

    ObjectContainer oc = Db4o.openFile(configFactory.createConfiguration(), dbFileName);

    totalProgressIncremented = migrate(progressMonitor, totalProgress, totalProgressIncremented, oc);
    oc.commit();
    oc.close();

    progressMonitor.worked(totalProgress - totalProgressIncremented);

    return new MigrationResult(false, false, false);
  }

  static int migrate(IProgressMonitor progressMonitor, final int totalProgress, int totalProgressIncremented, ObjectContainer oc) {
    List<News> newsList = oc.query(News.class);
    int newsCountPerIncrement = newsList.size() / totalProgress;

    int i = 0;
    for (News news : newsList) {
      oc.activate(news, Integer.MAX_VALUE);
      String descriptionFieldName = "fDescription"; //$NON-NLS-1$
      String descriptionValue = (String) MigrationHelper.getFieldValue(news, descriptionFieldName);
      if (descriptionValue != null) {
        MigrationHelper.setField(news, descriptionFieldName, null);
        Description description = new Description(news, descriptionValue);
        oc.ext().set(description, Integer.MAX_VALUE);
        oc.ext().set(news, Integer.MAX_VALUE);
      }

      ++i;

      if (newsCountPerIncrement == 0) {
        int progressIncrement = totalProgress / newsList.size();
        totalProgressIncremented += progressIncrement;
        progressMonitor.worked(progressIncrement);
      } else if (i % newsCountPerIncrement == 0) {
        totalProgressIncremented++;
        progressMonitor.worked(1);
      }
    }

    return totalProgressIncremented;
  }
}
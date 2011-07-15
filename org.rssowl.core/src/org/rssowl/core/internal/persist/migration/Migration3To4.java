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
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.NewsBin;
import org.rssowl.core.internal.persist.service.ConfigurationFactory;
import org.rssowl.core.internal.persist.service.Migration;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.reference.NewsReference;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.query.Query;

import java.util.List;

/**
 * Migration from version 3 (nightly from 13-Jan-2008) to 4 (nightly of
 * 17-Jan-2008).
 */
public class Migration3To4 implements Migration {

  /*
   * @see
   * org.rssowl.core.internal.persist.service.Migration#getDestinationFormat()
   */
  public int getDestinationFormat() {
    return 4;
  }

  /*
   * @see org.rssowl.core.internal.persist.service.Migration#getOriginFormat()
   */
  public int getOriginFormat() {
    return 3;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.service.Migration#migrate(org.rssowl.core
   * .internal.persist.service.ConfigurationFactory, java.lang.String,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public MigrationResult migrate(ConfigurationFactory configFactory, String dbFileName, IProgressMonitor progressMonitor) {
    final int totalProgress = 100;
    int totalProgressIncremented = 0;
    progressMonitor.beginTask(Messages.Migration3To4_MIGRATING_DATA, totalProgress);

    ObjectContainer oc = Db4o.openFile(configFactory.createConfiguration(), dbFileName);

    List<NewsBin> newsBins = oc.query(NewsBin.class);

    for (INewsBin newsBin : newsBins) {
      oc.activate(newsBin, Integer.MAX_VALUE);
      for (NewsReference newsRef : newsBin.getNewsRefs()) {
        Query query = oc.query();
        query.constrain(News.class);
        query.descend("fId").constrain(newsRef.getId()); //$NON-NLS-1$
        News news = (News) query.execute().iterator().next();
        oc.activate(news, Integer.MAX_VALUE);
        String parentIdFieldName = "fParentId"; //$NON-NLS-1$
        MigrationHelper.setField(news, parentIdFieldName, newsBin.getId().longValue());
        oc.ext().set(news, Integer.MAX_VALUE);
      }
    }

    oc.commit();
    oc.close();

    progressMonitor.worked(totalProgress - totalProgressIncremented);

    return new MigrationResult(true, false, true);
  }
}
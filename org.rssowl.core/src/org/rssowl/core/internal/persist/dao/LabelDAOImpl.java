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

package org.rssowl.core.internal.persist.dao;

import org.rssowl.core.internal.persist.Label;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.event.LabelEvent;
import org.rssowl.core.persist.event.LabelListener;

import com.db4o.query.Query;

import java.util.List;

/**
 * A data-access-object for <code>ILabel</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class LabelDAOImpl extends AbstractEntityDAO<ILabel, LabelListener, LabelEvent> implements ILabelDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public LabelDAOImpl() {
    super(Label.class, false);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final LabelEvent createDeleteEventTemplate(ILabel entity) {
    return new LabelEvent(null, entity, true);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#doDelete(org
   * .rssowl.core.persist.IPersistable)
   */
  @Override
  protected void doDelete(ILabel entity) {
    Query query = fDb.query();
    query.constrain(News.class);
    query.descend("fLabels").constrain(entity); //$NON-NLS-1$
    @SuppressWarnings("unchecked")
    List<INews> news = query.execute();
    if (!news.isEmpty()) {
      activateAll(news);
      for (INews newsItem : news) {
        newsItem.removeLabel(entity);
      }
      DynamicDAO.saveAll(news);
    }

    super.doDelete(entity);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final LabelEvent createSaveEventTemplate(ILabel entity) {
    ILabel oldLabel = fDb.ext().peekPersisted(entity, Integer.MAX_VALUE, true);
    return new LabelEvent(oldLabel, entity, true);
  }
}
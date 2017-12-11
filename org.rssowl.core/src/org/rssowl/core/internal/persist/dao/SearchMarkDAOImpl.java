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

import org.rssowl.core.internal.persist.SearchMark;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.ISearchMarkDAO;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.persist.service.PersistenceException;

import com.db4o.ext.Db4oException;
import com.db4o.query.Query;

import java.util.Date;
import java.util.Set;

/**
 * A data-access-object for <code>ISearchMark</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class SearchMarkDAOImpl extends AbstractEntityDAO<ISearchMark, SearchMarkListener, SearchMarkEvent> implements ISearchMarkDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public SearchMarkDAOImpl() {
    super(SearchMark.class, true);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final SearchMarkEvent createDeleteEventTemplate(ISearchMark entity) {
    return createSaveEventTemplate(entity);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final SearchMarkEvent createSaveEventTemplate(ISearchMark entity) {
    return new SearchMarkEvent(entity, null, true);
  }

  /*
   * @see org.rssowl.core.persist.dao.ISearchMarkDAO#fireNewsChanged(java.util.Set)
   */
  @Override
  public void fireNewsChanged(Set<SearchMarkEvent> events) {
    for (SearchMarkListener listener : fEntityListeners) {
      listener.newsChanged(events);
    }
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.ISearchMarkDAO#load(org.rssowl.core.persist
   * .ISearchCondition)
   */
  @Override
  public ISearchMark load(ISearchCondition searchCondition) {
    Query query = fDb.query();
    query.constrain(fEntityClass);
    query.descend("fSearchConditions").constrain(searchCondition); //$NON-NLS-1$
    return getSingleResult(query);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.ISearchMarkDAO#visited(org.rssowl.core.persist
   * .ISearchMark)
   */
  @Override
  public void visited(ISearchMark mark) {
    fWriteLock.lock();
    try {
      mark.setLastVisitDate(new Date());
      mark.setPopularity(mark.getPopularity() + 1);
      preSave(mark);
      fDb.ext().set(mark, 1);
      fDb.commit();
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    } finally {
      fWriteLock.unlock();
    }
    DBHelper.cleanUpAndFireEvents();
  }
}
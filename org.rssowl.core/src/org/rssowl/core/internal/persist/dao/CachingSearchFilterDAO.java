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

import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.dao.ISearchFilterDAO;
import org.rssowl.core.persist.event.SearchFilterEvent;
import org.rssowl.core.persist.event.SearchFilterListener;

import java.util.Collection;
import java.util.Set;

/**
 * Caching DAO for {@link ISearchFilter}.
 */
public class CachingSearchFilterDAO extends CachingDAO<SearchFilterDAOImpl, ISearchFilter, SearchFilterListener, SearchFilterEvent> implements ISearchFilterDAO {

  public CachingSearchFilterDAO() {
    super(new SearchFilterDAOImpl());
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.CachingDAO#createEntityListener()
   */
  @Override
  protected SearchFilterListener createEntityListener() {
    return new SearchFilterListener() {
      @Override
      public void entitiesAdded(Set<SearchFilterEvent> events) {
        putAll(events);
      }

      @Override
      public void entitiesDeleted(Set<SearchFilterEvent> events) {
        removeAll(events);
      }

      @Override
      public void entitiesUpdated(Set<SearchFilterEvent> events) {
        putAll(events);
      }

      @Override
      public void filterApplied(ISearchFilter filter, Collection<INews> news) {
        /* No action needed */
      }
    };
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.ISearchFilterDAO#fireFilterApplied(org.rssowl
   * .core.persist.ISearchFilter, java.util.Collection)
   */
  @Override
  public void fireFilterApplied(ISearchFilter filter, Collection<INews> news) {
    getDAO().fireFilterApplied(filter, news);
  }
}
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

import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.dao.ISearchDAO;
import org.rssowl.core.persist.event.SearchEvent;
import org.rssowl.core.persist.event.SearchListener;

import java.util.Set;

/**
 * Caching DAO for {@link ISearch}.
 */
public class CachingSearchDAO extends CachingDAO<SearchDAOImpl, ISearch, SearchListener, SearchEvent> implements ISearchDAO {

  public CachingSearchDAO() {
    super(new SearchDAOImpl());
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.CachingDAO#createEntityListener()
   */
  @Override
  protected SearchListener createEntityListener() {
    return new SearchListener() {

      public void entitiesAdded(Set<SearchEvent> events) {
        for (SearchEvent event : events)
          getCache().put(event.getEntity().getId(), event.getEntity());
      }

      public void entitiesDeleted(Set<SearchEvent> events) {
        for (SearchEvent event : events)
          getCache().remove(event.getEntity().getId(), event.getEntity());
      }

      public void entitiesUpdated(Set<SearchEvent> events) {
      /* No action needed */
      }
    };
  }
}
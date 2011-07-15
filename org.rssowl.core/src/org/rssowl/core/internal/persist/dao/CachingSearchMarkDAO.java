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

import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.service.DatabaseEvent;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.dao.ISearchMarkDAO;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.util.CoreUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Caching DAO for {@link ISearchMark}.
 */
public class CachingSearchMarkDAO extends CachingDAO<SearchMarkDAOImpl, ISearchMark, SearchMarkListener, SearchMarkEvent> implements ISearchMarkDAO {

  public CachingSearchMarkDAO() {
    super(new SearchMarkDAOImpl());
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.CachingDAO#onDatabaseOpened(org.rssowl
   * .core.internal.persist.service.DatabaseEvent)
   */
  @Override
  protected void onDatabaseOpened(DatabaseEvent event) {
    if (USE_LEGACY_CACHE_ACTIVATION)
      super.onDatabaseOpened(event);
    else {
      IFolderDAO folderDAO = InternalOwl.getDefault().getPersistenceService().getDAOService().getFolderDAO();
      Collection<IFolder> roots = folderDAO.loadRoots();
      Set<ISearchMark> searchmarks = new HashSet<ISearchMark>();
      CoreUtils.fillSearchMarks(searchmarks, roots);
      for (ISearchMark searchmark : searchmarks) {
        getCache().put(searchmark.getId(), searchmark);
      }
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.CachingDAO#createEntityListener()
   */
  @Override
  protected SearchMarkListener createEntityListener() {
    return new SearchMarkListener() {
      public void entitiesAdded(Set<SearchMarkEvent> events) {
        for (SearchMarkEvent event : events)
          getCache().put(event.getEntity().getId(), event.getEntity());
      }

      public void entitiesDeleted(Set<SearchMarkEvent> events) {
        for (SearchMarkEvent event : events)
          getCache().remove(event.getEntity().getId(), event.getEntity());
      }

      public void entitiesUpdated(Set<SearchMarkEvent> events) {
      /* No action needed */
      }

      public void newsChanged(Set<SearchMarkEvent> events) {
      /* No action needed */
      }
    };
  }

  /*
   * @see org.rssowl.core.persist.dao.ISearchMarkDAO#fireNewsChanged(java.util.Set)
   */
  public void fireNewsChanged(Set<SearchMarkEvent> events) {
    getDAO().fireNewsChanged(events);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.ISearchMarkDAO#load(org.rssowl.core.persist
   * .ISearchCondition)
   */
  public ISearchMark load(ISearchCondition searchCondition) {
    for (ISearchMark mark : getCache().values()) {
      if (mark.containsSearchCondition(searchCondition))
        return mark;
    }
    return null;
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.ISearchMarkDAO#visited(org.rssowl.core.persist
   * .ISearchMark)
   */
  public void visited(ISearchMark mark) {
    getDAO().visited(mark);
  }
}
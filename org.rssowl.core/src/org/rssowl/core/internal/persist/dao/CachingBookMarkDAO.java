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
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.CoreUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Caching DAO for {@link IBookMark}.
 */
public final class CachingBookMarkDAO extends CachingDAO<BookMarkDAOImpl, IBookMark, BookMarkListener, BookMarkEvent> implements IBookMarkDAO {

  public CachingBookMarkDAO() {
    super(new BookMarkDAOImpl());
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
      Set<IBookMark> bookmarks = new HashSet<IBookMark>();
      CoreUtils.fillBookMarks(bookmarks, roots);
      for (IBookMark bookmark : bookmarks) {
        getCache().put(bookmark.getId(), bookmark);
      }
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.CachingDAO#createEntityListener()
   */
  @Override
  protected BookMarkListener createEntityListener() {
    return new BookMarkListener() {
      @Override
      public void entitiesAdded(Set<BookMarkEvent> events) {
        for (BookMarkEvent event : events)
          getCache().put(event.getEntity().getId(), event.getEntity());
      }

      @Override
      public void entitiesDeleted(Set<BookMarkEvent> events) {
        for (BookMarkEvent event : events)
          getCache().remove(event.getEntity().getId(), event.getEntity());
      }

      @Override
      public void entitiesUpdated(Set<BookMarkEvent> events) {
        /* No action needed */
      }
    };
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IBookMarkDAO#loadAll(org.rssowl.core.persist
   * .reference.FeedLinkReference)
   */
  @Override
  public Collection<IBookMark> loadAll(FeedLinkReference feedRef) {
    Set<IBookMark> marks = new HashSet<IBookMark>(1);
    for (IBookMark mark : getCache().values()) {
      if (mark.getFeedLinkReference().equals(feedRef))
        marks.add(mark);
    }

    return marks;
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IBookMarkDAO#exists(org.rssowl.core.persist
   * .reference.FeedLinkReference)
   */
  @Override
  public boolean exists(FeedLinkReference feedRef) {
    for (IBookMark mark : getCache().values()) {
      if (mark.getFeedLinkReference().equals(feedRef))
        return true;
    }

    return false;
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IBookMarkDAO#visited(org.rssowl.core.persist
   * .IBookMark)
   */
  @Override
  public void visited(IBookMark mark) {
    getDAO().visited(mark);
  }
}
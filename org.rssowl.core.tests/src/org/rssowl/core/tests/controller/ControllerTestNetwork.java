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

package org.rssowl.core.tests.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.Controller.BookMarkLoadListener;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This TestCase covers use-cases for the Controller (network only).
 *
 * @author bpasero
 */
public class ControllerTestNetwork {

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * Reload a Feed.
   *
   * @throws Exception
   */
  @Test
  public void testReloadFeed() throws Exception {
    BookMarkLoadListener listener = null;
    try {
      final IFeed feed = DynamicDAO.save(new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/manager/rss_2_0.xml"))); //$NON-NLS-1$
      IBookMark bookmark = createBookMark(feed);

      final AtomicBoolean bool1 = new AtomicBoolean(false);
      final AtomicBoolean bool2 = new AtomicBoolean(false);

      assertTrue(bookmark.getNewsRefs().isEmpty());
      assertTrue(bookmark.getNewsRefs(INews.State.getVisible()).isEmpty());
      assertTrue(bookmark.getNews().isEmpty());
      assertTrue(bookmark.getNews(INews.State.getVisible()).isEmpty());
      assertEquals(0, bookmark.getNewsCount(INews.State.getVisible()));

      listener = new BookMarkLoadListener() {
        public void bookMarkDoneLoading(IBookMark bookmark) {
          if (bookmark.getFeedLinkReference().references(feed))
            bool1.set(true);
        }

        public void bookMarkAboutToLoad(IBookMark bookmark) {
          if (bookmark.getFeedLinkReference().references(feed))
            bool2.set(true);
        }
      };
      Controller.getDefault().addBookMarkLoadListener(listener);

      Controller.getDefault().reload(bookmark, null, new NullProgressMonitor());

      assertEquals(new FeedReference(feed.getId()).resolve().getFormat(), "RSS 2.0"); //$NON-NLS-1$

      assertEquals(15, bookmark.getNewsRefs().size());
      assertEquals(15, bookmark.getNewsRefs(INews.State.getVisible()).size());
      assertEquals(15, bookmark.getNews().size());
      assertEquals(15, bookmark.getNews(INews.State.getVisible()).size());
      assertEquals(15, bookmark.getNewsCount(INews.State.getVisible()));

      assertTrue(bool1.get());
      assertTrue(bool2.get());
    } finally {
      if (listener != null)
        Controller.getDefault().removeBookMarkLoadListener(listener);
    }
  }

  /**
   * @throws Exception
   * See http://dev.rssowl.org/show_bug.cgi?id=1107
   */
  @Test
  public void testDeleteFolderHierarchyWithBin() throws Exception {
    IModelFactory factory = Owl.getModelFactory();
    IFolder root = factory.createFolder(null, null, "Root");
    IFolder folder = factory.createFolder(null, root, "Folder");
    IFolder childFolder = factory.createFolder(null, folder, "Child Folder");

    IFeed feed = factory.createFeed(null, new URI("http://www.rssowl.org/rssowl2dg/tests/manager/rss_2_0.xml"));

    DynamicDAO.save(feed);

    IBookMark mark = factory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Bookmark");
    INewsBin bin = factory.createNewsBin(null, folder, "Bin");

    DynamicDAO.save(root);

    Controller.getDefault().reload(mark, null, new NullProgressMonitor());

    /* Move News to Bin */
    INews copiedNews = factory.createNews(feed.getNews().get(0), bin);
    DynamicDAO.save(copiedNews);
    DynamicDAO.save(bin);

    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(feed.getNews().get(0)), INews.State.HIDDEN, false, false);

    /* Delete Folder */
    DynamicDAO.deleteAll(Collections.singleton(folder));

    assertNull(Owl.getPersistenceService().getDAOService().getNewsCounterDAO().load().get(feed.getLink().toString()));
  }

  /**
   * Reload a large Feed with invalid encoding.
   *
   * @throws Exception
   */
  @Test
  public void testReloadLargeFeedWithInvalidEncoding() throws Exception {
    IFeed feed = new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/manager/invalid_utf8_large.xml")); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);
    Controller.getDefault().reload(createBookMark(feed), null, new NullProgressMonitor());

    assertEquals("RDF", new FeedReference(feed.getId()).resolve().getFormat()); //$NON-NLS-1$
  }

  /**
   * Reload a small Feed with invalid encoding.
   *
   * @throws Exception
   */
  @Test
  public void testReloadSmallFeedWithInvalidEncoding() throws Exception {
    IFeed feed = new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/manager/invalid_utf8_small.xml")); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);
    Controller.getDefault().reload(createBookMark(feed), null, new NullProgressMonitor());

    assertEquals("RDF", new FeedReference(feed.getId()).resolve().getFormat()); //$NON-NLS-1$
  }

  /**
   * Reload a BookMark.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testReloadBookMark() throws Exception {
    IFeed feed = new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/manager/rss_2_0.xml"));
    feed = DynamicDAO.save(feed);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Folder");
    folder = DynamicDAO.save(folder);
    IBookMark bookmark = Owl.getModelFactory().createBookMark(1L, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    Controller.getDefault().reload(bookmark, null, new NullProgressMonitor());

    assertEquals(new FeedReference(feed.getId()).resolve().getFormat(), "RSS 2.0"); //$NON-NLS-1$
  }

  /**
   * Reload a BookMark that points to an unavailable Feed.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testReloadBookMarkWithError() throws Exception {
    IFeed feed = new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/not_existing.xml"));
    feed = DynamicDAO.save(feed);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Folder");
    folder = DynamicDAO.save(folder);
    IBookMark bookmark = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    Controller.getDefault().reload(bookmark, null, new NullProgressMonitor());

    assertEquals(true, new BookMarkReference(bookmark.getId()).resolve().isErrorLoading());
  }

  private NewsCounter loadNewsCounter() {
    return DynamicDAO.getDAO(INewsCounterDAO.class).load();
  }

  private int getNewCount(IFeed feed) {
    return loadNewsCounter().getNewCount(feed.getLink().toString());
  }

  private int getUnreadCount(IFeed feed) {
    return loadNewsCounter().getUnreadCount(feed.getLink().toString());
  }

  private int getStickyCount(IFeed feed) {
    return loadNewsCounter().getStickyCount(feed.getLink().toString());
  }

  /**
   * Test the News-Service with Reload.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testNewsServiceWithReloadBookMark() throws Exception {
    IFeed feed = new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/manager/rss_2_0.xml"));
    feed = DynamicDAO.save(feed);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Folder");
    folder = DynamicDAO.save(folder);
    IBookMark bookmark = Owl.getModelFactory().createBookMark(1L, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    Controller.getDefault().reload(bookmark, null, new NullProgressMonitor());

    feed.getNews().get(0).setFlagged(true);
    DynamicDAO.save(feed);

    int unreadCounter = getUnreadCount(feed);
    int newCounter = getNewCount(feed);
    int stickyCounter = getStickyCount(feed);

    Controller.getDefault().reload(bookmark, null, new NullProgressMonitor());

    assertEquals(unreadCounter, getUnreadCount(feed));
    assertEquals(newCounter, getNewCount(feed));
    assertEquals(stickyCounter, getStickyCount(feed));
  }

  private IBookMark createBookMark(IFeed feed) throws PersistenceException {
    IFolder folder = DynamicDAO.save(Owl.getModelFactory().createFolder(null, null, "Root"));

    return DynamicDAO.save(Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark"));
  }
}
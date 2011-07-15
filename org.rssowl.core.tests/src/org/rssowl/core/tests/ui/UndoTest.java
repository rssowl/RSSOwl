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

package org.rssowl.core.tests.ui;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.ui.internal.undo.CopyOperation;
import org.rssowl.ui.internal.undo.MoveOperation;
import org.rssowl.ui.internal.undo.NewsStateOperation;
import org.rssowl.ui.internal.undo.StickyOperation;
import org.rssowl.ui.internal.undo.UndoStack;

import java.net.URI;
import java.util.Collections;
import java.util.Date;

/**
 * Tests for the {@link UndoStack}.
 *
 * @author bpasero
 */
public class UndoTest {
  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    fFactory = Owl.getModelFactory();
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    UndoStack.getInstance().clear();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoCopy() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    IFolder folder = fFactory.createFolder(null, null, "Root");
    INewsBin bin = fFactory.createNewsBin(null, folder, "Bin");

    DynamicDAO.save(folder);

    INews copiedNews = fFactory.createNews(news, bin);
    DynamicDAO.save(copiedNews);
    DynamicDAO.save(bin);

    UndoStack.getInstance().addOperation(new CopyOperation(Collections.singletonList(copiedNews)));

    assertTrue(bin.containsNews(copiedNews));

    UndoStack.getInstance().undo();

    assertEquals(0, bin.getNewsCount(INews.State.getVisible()));

    UndoStack.getInstance().redo();

    assertTrue(bin.containsNews(copiedNews));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoMove() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    IFolder folder = fFactory.createFolder(null, null, "Root");
    IBookMark bookmark = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");
    INewsBin bin = fFactory.createNewsBin(null, folder, "Bin");

    DynamicDAO.save(folder);

    INews copiedNews = fFactory.createNews(news, bin);
    DynamicDAO.save(copiedNews);
    DynamicDAO.save(bin);

    UndoStack.getInstance().addOperation(new MoveOperation(Collections.singletonList(news), Collections.singletonList(copiedNews), false));

    /* Remove (it's a move!) */
    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), INews.State.HIDDEN, false, false);

    assertTrue(bin.containsNews(copiedNews));
    assertTrue(bookmark.getNewsCount(INews.State.getVisible()) == 0);

    UndoStack.getInstance().undo();

    assertTrue(bin.getNewsCount(INews.State.getVisible()) == 0);

    assertTrue(bookmark.containsNews(news));
    assertTrue(bookmark.getNewsCount(INews.State.getVisible()) > 0);

    UndoStack.getInstance().redo();

    assertTrue(bin.containsNews(copiedNews));
    assertTrue(bookmark.getNewsCount(INews.State.getVisible()) == 0);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoStateChange() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news), INews.State.READ, false));

    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), INews.State.READ, false, false);

    assertEquals(INews.State.READ, news.getState());

    UndoStack.getInstance().undo();
    assertEquals(INews.State.NEW, news.getState());

    UndoStack.getInstance().redo();
    assertEquals(INews.State.READ, news.getState());

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news), INews.State.HIDDEN, false));
    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), INews.State.HIDDEN, false, false);

    assertEquals(INews.State.HIDDEN, news.getState());

    UndoStack.getInstance().undo();
    assertEquals(INews.State.READ, news.getState());

    UndoStack.getInstance().redo();
    assertEquals(INews.State.HIDDEN, news.getState());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoStateChangeAffectEquivalent() throws Exception {
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setLink(new URI("http://www.news.com"));

    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.foo2.com"));
    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news1), INews.State.READ, true));

    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news1), INews.State.READ, true, false);

    assertEquals(INews.State.READ, news1.getState());
    assertEquals(INews.State.READ, news2.getState());

    UndoStack.getInstance().undo();
    assertEquals(INews.State.NEW, news1.getState());
    assertEquals(INews.State.NEW, news2.getState());

    UndoStack.getInstance().redo();
    assertEquals(INews.State.READ, news1.getState());
    assertEquals(INews.State.READ, news2.getState());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoSticky() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);
    UndoStack.getInstance().addOperation(new StickyOperation(Collections.singletonList(news), true));

    news.setFlagged(true);
    DynamicDAO.save(news);

    UndoStack.getInstance().undo();

    assertEquals(false, news.isFlagged());

    UndoStack.getInstance().redo();

    assertEquals(true, news.isFlagged());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoDeleteAndSticky() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));
    news.setFlagged(true);

    DynamicDAO.save(feed);

    IBookMark bookmark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");
    DynamicDAO.save(root);

    assertEquals(1, bookmark.getStickyNewsCount());
    assertEquals(1, bookmark.getNewsCount(INews.State.getVisible()));

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singleton(news), INews.State.HIDDEN, false));
    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singleton(news), INews.State.HIDDEN, false, false);

    assertEquals(0, bookmark.getStickyNewsCount());
    assertEquals(0, bookmark.getNewsCount(INews.State.getVisible()));

    UndoStack.getInstance().undo();

    assertEquals(1, bookmark.getStickyNewsCount());
    assertEquals(1, bookmark.getNewsCount(INews.State.getVisible()));

    UndoStack.getInstance().redo();

    assertEquals(0, bookmark.getStickyNewsCount());
    assertEquals(0, bookmark.getNewsCount(INews.State.getVisible()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewActionFromUndoActionDeletesAllFollowingRedoActions() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);
    UndoStack.getInstance().addOperation(new StickyOperation(Collections.singletonList(news), true));

    news.setFlagged(true);
    DynamicDAO.save(news);

    UndoStack.getInstance().undo();

    assertEquals(false, news.isFlagged());
    assertEquals(true, UndoStack.getInstance().isRedoSupported());

    UndoStack.getInstance().addOperation(new StickyOperation(Collections.singletonList(news), true));

    assertEquals(false, UndoStack.getInstance().isRedoSupported());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoRedoNewsState() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));
    news.setState(INews.State.READ);

    DynamicDAO.save(feed);

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news), INews.State.UNREAD, false));
    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), INews.State.UNREAD, false, false);
    assertEquals(INews.State.UNREAD, news.getState());

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news), INews.State.READ, false));
    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), INews.State.READ, false, false);
    assertEquals(INews.State.READ, news.getState());

    UndoStack.getInstance().undo();
    UndoStack.getInstance().undo();
    assertEquals(INews.State.READ, news.getState());

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news), INews.State.UNREAD, false));
    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), INews.State.UNREAD, false, false);
    assertEquals(INews.State.UNREAD, news.getState());

    UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news), INews.State.READ, false));
    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), INews.State.READ, false, false);
    assertEquals(INews.State.READ, news.getState());

    UndoStack.getInstance().undo();
    UndoStack.getInstance().undo();
    assertEquals(INews.State.READ, news.getState());
    assertEquals(false, UndoStack.getInstance().isUndoSupported());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUndoRedoExceedsMaxLimit() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));
    news.setState(INews.State.READ);

    DynamicDAO.save(feed);

    UndoStack.getInstance().addOperation(new StickyOperation(Collections.singletonList(news), true));
    news.setFlagged(true);
    DynamicDAO.save(news);

    for (int i = 0; i < 20; i++) {
      UndoStack.getInstance().addOperation(new NewsStateOperation(Collections.singletonList(news), i % 2 == 0 ? INews.State.UNREAD : INews.State.READ, false));
      DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singletonList(news), i % 2 == 0 ? INews.State.UNREAD : INews.State.READ, false, false);
    }

    int undos = 0;
    while (UndoStack.getInstance().isUndoSupported()) {
      UndoStack.getInstance().undo();
      undos++;
    }

    assertEquals(true, news.toReference().resolve().isFlagged());
    assertEquals(20, undos);

    assertEquals(true, UndoStack.getInstance().isRedoSupported());

    int redos = 0;
    while (UndoStack.getInstance().isRedoSupported()) {
      UndoStack.getInstance().redo();
      redos++;
    }

    assertEquals(20, redos);

    while (UndoStack.getInstance().isUndoSupported()) {
      UndoStack.getInstance().undo();
    }

    UndoStack.getInstance().addOperation(new StickyOperation(Collections.singletonList(news), false));
    news.setFlagged(false);
    DynamicDAO.save(news);

    undos = 0;
    while (UndoStack.getInstance().isUndoSupported()) {
      UndoStack.getInstance().undo();
      undos++;
    }

    assertEquals(1, undos);
    assertEquals(true, UndoStack.getInstance().isRedoSupported());
    assertEquals(true, news.toReference().resolve().isFlagged());
  }
}
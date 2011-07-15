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

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.tests.model.LargeBlockSizeTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This TestCase covers use-cases for the Controller (local only).
 *
 * @author bpasero
 */
public class ControllerTestLocal extends LargeBlockSizeTest {

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
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
   * Test the News-Service.
   *
   * @throws Exception
   */
  @Test
  public void testNewsService() throws Exception {
    INewsDAO newsDao = Owl.getPersistenceService().getDAOService().getNewsDAO();

    IFeed feed = new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/manager/rss_2_0.xml")); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));

    Owl.getModelFactory().createNews(null, feed, new Date()); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(1, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));

    newsDao.setState(feed.getNews(), INews.State.READ, true, false);
    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));

    newsDao.setState(feed.getNews(), INews.State.UNREAD, true, false);
    feed.getNews().get(0).setFlagged(true);
    DynamicDAO.save(feed.getNews().get(0));

    assertEquals(1, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));

    newsDao.setState(feed.getNews(), INews.State.READ, true, false);
    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));

    newsDao.setState(feed.getNews(), INews.State.UPDATED, true, false);
    assertEquals(1, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));

    feed.getNews().get(0).setFlagged(false);
    DynamicDAO.save(feed.getNews().get(0));
    newsDao.setState(feed.getNews(), INews.State.READ, true, false);

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));

    Owl.getModelFactory().createNews(null, feed, new Date()); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);

    Owl.getModelFactory().createNews(null, feed, new Date()); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);

    assertEquals(2, getUnreadCount(feed));
    assertEquals(2, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));

    newsDao.setState(feed.getNews(), INews.State.READ, true, false);
    feed.getNews().get(0).setFlagged(true);
    feed.getNews().get(1).setFlagged(true);
    DynamicDAO.save(feed);

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(2, getStickyCount(feed));

    newsDao.setState(feed.getNews(), INews.State.UNREAD, true, false);

    assertEquals(3, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(2, getStickyCount(feed));

    /* Simulate Dirty Shutdown */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();

    feed = new Feed(new URI("http://www.rssowl.org/rssowl2dg/tests/manager/rss_2_0.xml")); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);

    Owl.getModelFactory().createNews(null, feed, new Date()); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);

    Owl.getModelFactory().createNews(null, feed, new Date()); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);

    feed.getNews().get(0).setFlagged(true);
    feed.getNews().get(1).setFlagged(true);
    DynamicDAO.save(feed);

    assertEquals(2, getUnreadCount(feed));
    assertEquals(2, getNewCount(feed));
    assertEquals(2, getStickyCount(feed));
  }

  /**
   * Test the News-Service with an Updated News.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testNewsServiceWithUpdatedNews() throws Exception {
    IFeed feed = new Feed(new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News Title #1");
    news1.setLink(new URI("http://www.link.com"));
    news1.setFlagged(true);

    feed = DynamicDAO.save(feed);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(1, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));

    feed.getNews().get(0).setTitle("News Title Updated #1");
    feed = DynamicDAO.save(feed);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(1, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));
  }

  /**
   * Test the News-Service with an Updated News.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testNewsServiceWithUpdatedNews2() throws Exception {
    IFeed feed = new Feed(new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News Title #1");
    news1.setLink(new URI("http://www.link.com"));
    news1.setState(INews.State.READ);

    feed = DynamicDAO.save(feed);

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));

    feed.getNews().get(0).setTitle("News Title Updated #1");
    feed.getNews().get(0).setState(INews.State.UPDATED);
    feed.getNews().get(0).setFlagged(true);
    feed = DynamicDAO.save(feed);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(INews.State.UPDATED, feed.getNews().get(0).getState());
    assertEquals(1, getStickyCount(feed));

    feed.getNews().get(0).setState(INews.State.READ);
    feed = DynamicDAO.save(feed);

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));
  }

  /**
   * Test the News-Service with an Deleted News.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testNewsServiceWithDeletedNews() throws Exception {
    IFeed feed = new Feed(new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News Title #1");
    news1.setLink(new URI("http://www.link.com"));
    news1.setFlagged(true);

    feed = DynamicDAO.save(feed);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(1, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));

    DynamicDAO.delete(feed.getNews().get(0));

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));
  }

  /**
   * Test the News-Service with an Updated News.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testNewsServiceWithDeletedNews2() throws Exception {
    IFeed feed = new Feed(new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News Title #1");
    news1.setLink(new URI("http://www.link.com"));
    news1.setState(INews.State.READ);

    feed = DynamicDAO.save(feed);

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));

    feed.getNews().get(0).setTitle("News Title Updated #1");
    feed.getNews().get(0).setState(INews.State.UPDATED);
    feed.getNews().get(0).setFlagged(true);
    feed = DynamicDAO.save(feed);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));
    assertEquals(INews.State.UPDATED, feed.getNews().get(0).getState());

    feed.getNews().get(0).setState(INews.State.READ);
    feed = DynamicDAO.save(feed);

    DynamicDAO.delete(feed.getNews().get(0));

    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(0, getStickyCount(feed));
  }

  /**
   * Test the News-Service with an Unread News.
   *
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testNewsServiceWithApplicationLayerSaveNews() throws Exception {
    IFeed feed = new Feed(new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News Title #1");
    news1.setLink(new URI("http://www.link.com"));
    news1.setState(INews.State.UNREAD);
    news1.setFlagged(true);

    feed = DynamicDAO.save(feed);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));

    feed.getNews().get(0).setTitle("News Title Updated #1");

    List<INews> news = new ArrayList<INews>();
    news.add(feed.getNews().get(0));

    DynamicDAO.saveAll(news);

    assertEquals(1, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));
    assertEquals(1, getStickyCount(feed));
  }
}
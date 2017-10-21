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
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.event.AttachmentEvent;
import org.rssowl.core.persist.event.AttachmentListener;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.tests.model.LargeBlockSizeTest;
import org.rssowl.ui.internal.Controller;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

/**
 * This Test-Case tries to be very close to a real-world-example by modifying a
 * Feed in memory and using Controller#reload() on it to test the results.
 * Summmary of Tests:
 * <ul>
 * <li>Title: testTitle()</li>
 * <li>Link: testLink()</li>
 * <li>Guid: testGuid()</li>
 * <li>Title, Link: testTitleLink()</li>
 * <li>Title, Guid: testTitleGuid()</li>
 * <li>Title, PubDate: testTitlePubDate()</li>
 * <li>Title, Link, PubDate: testTitleLinkPubdate()</li>
 * <li>Title, Guid, PubDate: testTitleGuidPubDate()</li>
 * <li>Title, Link, Guid: testTitleLinkGuid()</li>
 * <li>Title, Link, Guid, PubDate: testTitleLinkGuidPubDate()</li>
 * </ul>
 *
 * @author bpasero
 */
public class ReloadTestLocal extends LargeBlockSizeTest {
  private Controller fController;
  private SimpleDateFormat fDateFormat;
  private Random fRand = new Random();
  private INewsDAO fNewsDao;

  {
    fDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"); //$NON-NLS-1$
    fDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    InMemoryProtocolHandler.FEED = null;

    fController = Controller.getDefault();
    fNewsDao = Owl.getPersistenceService().getDAOService().getNewsDAO();
  }

  @Test
  @SuppressWarnings("all")
  public void testInMemoryFeed() throws Exception {
    IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);
    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));

    FeedReference feedRef = new FeedReference(feed.getId());

    Date d = new Date();

    InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", "http://www.guid.de", fDateFormat.format(d));
    fController.reload(createBookMark(feed), null, new NullProgressMonitor());

    assertEquals(1, feedRef.resolve().getNews().size());

    INews news = feedRef.resolve().getNews().get(0);
    assertEquals("Title", news.getTitle());
    assertEquals("http://www.link.de", news.getLink().toString());
    assertEquals("http://www.guid.de", news.getGuid().getValue());
    assertEquals(d.toString(), news.getPublishDate().toString());
  }

  @Test
  @SuppressWarnings("all")
  public void testTitle() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", null, null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());
      assertEquals(1, addedCounter[0]);

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);
      assertEquals(1, updatedCounter[0]);

      /* Second Reload with different Title */
      InMemoryProtocolHandler.FEED = generateFeed("Title *new*", null, null, null);

      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(2, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(1, updatedCounter[0]);
      assertEquals(2, addedCounter[0]);

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("Title".equals(newsItem.getTitle()))
          assertEquals(INews.State.UNREAD, newsItem.getState());
        else if ("Title *new*".equals(newsItem.getTitle()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      assertEquals(3, updatedCounter[0]);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());
      assertEquals(3, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testLink() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed(null, "http://www.link.de", null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with different Link */
      InMemoryProtocolHandler.FEED = generateFeed(null, "http://www.link_other.de", null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(2, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.link.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.UNREAD, newsItem.getState());
        else if ("http://www.link_other.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(3, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testGuid() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed(null, null, "http://www.guid.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with different Guid */
      InMemoryProtocolHandler.FEED = generateFeed(null, null, "http://www.guid_other.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(2, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.guid.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.UNREAD, newsItem.getState());
        else if ("http://www.guid_other.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(3, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testTitleLink() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with updated Title */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link.de", null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fourth Reload with added News */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link_other.de", null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.link.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.READ, newsItem.getState());
        else if ("http://www.link_other.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(4, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testTitleGuid() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", null, "http://www.guid.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with updated Title */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", null, "http://www.guid.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fourth Reload with added News */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", null, "http://www.guid_other.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.guid.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.READ, newsItem.getState());
        else if ("http://www.guid_other.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(4, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests that news with the same title will not be updated if their
   * description changes.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("all")
  public void testTitleDescription() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      long now = System.currentTimeMillis();

      String description = "Initial description";
      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", null, null, fDateFormat.format(now), description);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with updated description */
      long ms = now + 100000;
      String updatedDescription = description + "updated";
      InMemoryProtocolHandler.FEED = generateFeed("Title", null, null, fDateFormat.format(new Date(ms)), updatedDescription);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UNREAD, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fourth Reload with added News */
      ms = System.currentTimeMillis() + 1000000;
      InMemoryProtocolHandler.FEED = generateFeed("Title Other", null, null, fDateFormat.format(ms));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("Title".equals(newsItem.getTitle()))
          assertEquals(INews.State.READ, newsItem.getState());
        else if ("Title Other".equals(newsItem.getTitle()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(4, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testTitleLinkPubdate() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      long now = System.currentTimeMillis();

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with updated Title */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link.de", null, fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fourth Reload with updated Publish Date */
      long ms = now + 100000;
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link.de", null, fDateFormat.format(ms));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fifth Reload with updated Title and Publish Date */
      ms = now + 1000000;
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2*", "http://www.link.de", null, fDateFormat.format(ms));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Sixth Reload with added News */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2*", "http://www.link_other.de", null, fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.link.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.READ, newsItem.getState());
        else if ("http://www.link_other.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(7, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testTitleGuidPubDate() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      long now = System.currentTimeMillis();

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", null, "http://www.guid.de", fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with updated Title */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", null, "http://www.guid.de", fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fourth Reload with updated Publish Date */
      long ms = now + 100000;
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", null, "http://www.guid.de", fDateFormat.format(ms));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fifth Reload with updated Title and Publish Date */
      ms = now + 1000000;
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2*", null, "http://www.guid.de", fDateFormat.format(ms));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Sixth Reload with added News */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2*", null, "http://www.guid_other.de", fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.guid.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.READ, newsItem.getState());
        else if ("http://www.guid_other.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(7, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testTitleLinkGuid() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", "http://www.guid.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with updated Title */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link.de", "http://www.guid.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fourth Reload with updated Link */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link_updated.de", "http://www.guid.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fifth Reload with updated Title and Link */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2*", "http://www.link_updated_again.de", "http://www.guid.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Sixth Reload with added News */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2*", "http://www.link_updated_again.de", "http://www.guid_other.de", null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.guid.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.READ, newsItem.getState());
        else if ("http://www.guid_other.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(7, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testTitleLinkGuidPubDate() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      long now = System.currentTimeMillis();

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", "http://www.guid.de", fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);

      /* Second Reload with updated Title */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link.de", "http://www.guid.de", fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fourth Reload with updated Link */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link_updated.de", "http://www.guid.de", fDateFormat.format(now));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Reload with updated description */
      String updatedDescription = "updatedDescription";
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated*", "http://www.link_updated.de", "http://www.guid.de", fDateFormat.format(now + 1000), updatedDescription);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Fifth Reload with updated Title and Link */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2*", "http://www.link_updated_again.de", "http://www.guid.de", fDateFormat.format(now), updatedDescription);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Reload with updated Title and Publish Date */
      long ms2 = now + 200000;
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2 #3*", "http://www.link_updated_again.de", "http://www.guid.de", fDateFormat.format(ms2), updatedDescription + " again");
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Reload with updated Title, Link and Publish Date */
      long ms3 = now + 500000;
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2 #3 #4*", "http://www.link_updated_again_again.de", "http://www.guid.de", fDateFormat.format(ms3));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

      /* Set to Read and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());

      /* Sixth Reload with added News */
      InMemoryProtocolHandler.FEED = generateFeed("Title *updated #2 #3 #4*", "http://www.link_updated_again_again.de", "http://www.guid_other.de", fDateFormat.format(ms3));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.guid.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.READ, newsItem.getState());
        else if ("http://www.guid_other.de".equals(newsItem.getGuid().getValue()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Set to Read */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.READ, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      assertEquals(INews.State.READ, feedRef.resolve().getNews().get(1).getState());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(12, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testNewsDeleted() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      final int removedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          removedCounter[0] += events.size();
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, fDateFormat.format(System.currentTimeMillis()));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

      /* Delete News (set to Hidden) */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.HIDDEN, true, false);
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      /* Reload unchanged Feed */
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      /* Second Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link_other.de", null, fDateFormat.format(System.currentTimeMillis()));
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(2, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));

      List<INews> news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.link.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.HIDDEN, newsItem.getState());
        else if ("http://www.link_other.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Really Delete News */
      news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.link.de".equals(newsItem.getLink().toString()))
          fNewsDao.setState(new ArrayList<INews>(Arrays.asList(newsItem)), INews.State.DELETED, false, false);
      }

      news = feedRef.resolve().getNews();
      for (INews newsItem : news) {
        if ("http://www.link.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.DELETED, newsItem.getState());
        else if ("http://www.link_other.de".equals(newsItem.getLink().toString()))
          assertEquals(INews.State.NEW, newsItem.getState());
      }

      /* Reload unchanged Feed */
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());

      /* Test Event Counter */
      assertEquals(2, addedCounter[0]);
      assertEquals(2, updatedCounter[0]);
      assertEquals(1, removedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testNewsUpdatedOnOtherFields() throws Exception {
    NewsListener newsListener = null;
    NewsListener oldNewsListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);
      assertEquals(0, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          addedCounter[0] += events.size();
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Did not expect this Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          updatedCounter[0] += events.size();
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, null, "Hello World", "bpasero", null, "mp3");
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(1, getNewCount(feed));
      assertEquals(INews.State.NEW, feedRef.resolve().getNews().get(0).getState());
      assertEquals("Hello World", feedRef.resolve().getNews().get(0).getDescription());
      assertEquals("bpasero", feedRef.resolve().getNews().get(0).getAuthor().getName());

      /* Set to Unread */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);
      assertEquals(1, updatedCounter[0]);

      /* Second Reload - changed Description */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, fDateFormat.format(new Date()), "Hello World Changed", "bpasero", null, "mp3");
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UNREAD, feedRef.resolve().getNews().get(0).getState());
      assertEquals("Hello World Changed", feedRef.resolve().getNews().get(0).getDescription());
      assertEquals(2, updatedCounter[0]);

      /* Set to Unread and Reload */
      fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);
      fController.reload(bookmark, null, new NullProgressMonitor());

      /* This Reload - added Enclosure */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, null, "Hello World Changed", "bpasero", "http://www.download.de", "mp3");
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feed.getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UNREAD, feedRef.resolve().getNews().get(0).getState());
      assertEquals(1, feedRef.resolve().getNews().get(0).getAttachments().size());
      assertEquals("http://www.download.de", feedRef.resolve().getNews().get(0).getAttachments().get(0).getLink().toString());
      assertEquals(3, updatedCounter[0]);

      /* This Reload - Changed Author */
      InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, null, "Hello World Changed", "ijuma", "http://www.download.de", "mp3");
      oldNewsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          assertEquals(1, events.size());
          int attachmentsSize = events.iterator().next().getOldNews().getAttachments().size();
          assertEquals(1, attachmentsSize);
        }
      };
      DynamicDAO.addEntityListener(INews.class, oldNewsListener);
      fController.reload(bookmark, null, new NullProgressMonitor());
      DynamicDAO.removeEntityListener(INews.class, oldNewsListener);
      assertEquals(1, feedRef.resolve().getNews().size());
      assertEquals(1, getUnreadCount(feed));
      assertEquals(0, getNewCount(feed));
      assertEquals(INews.State.UNREAD, feedRef.resolve().getNews().get(0).getState());
      assertEquals("ijuma", feedRef.resolve().getNews().get(0).getAuthor().getName());
      assertEquals(3, updatedCounter[0]);

      /* Test Event Counter */
      assertEquals(1, addedCounter[0]);
      assertEquals(3, updatedCounter[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      if (oldNewsListener != null)
        DynamicDAO.removeEntityListener(INews.class, oldNewsListener);
    }
  }

  @Test
  @SuppressWarnings("all")
  public void testAttachmentAddedUpdatedEvent() throws Exception {
    AttachmentListener attachmentListener = null;
    try {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);

      IBookMark bookmark = createBookMark(feed);

      final int addedCounter[] = new int[] { 0 };
      final int updatedCounter[] = new int[] { 0 };
      attachmentListener = new AttachmentListener() {
        public void entitiesAdded(Set<AttachmentEvent> events) {
          addedCounter[0]++;
        }

        public void entitiesDeleted(Set<AttachmentEvent> events) {}

        public void entitiesUpdated(Set<AttachmentEvent> events) {
          updatedCounter[0]++;
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);

      /* First Reload */
      InMemoryProtocolHandler.FEED = generateFeedWithEnclosure("Title", null, "http://www.mp3.com/me.mp3", "wav");
      fController.reload(bookmark, null, new NullProgressMonitor());

      /* Second Reload with different attachment type */
      InMemoryProtocolHandler.FEED = generateFeedWithEnclosure("Title", null, "http://www.mp3.com/me.mp3", "mp3");
      fController.reload(bookmark, null, new NullProgressMonitor());

      assertEquals(1, addedCounter[0]);
      assertEquals(1, updatedCounter[0]);
    } finally {
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
    }
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

  @Test
  @SuppressWarnings("all")
  public void testNewsServiceWithReloadBookMarkAndCleanup() throws Exception {
    IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
    feed = DynamicDAO.save(feed);
    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));

    FeedReference feedRef = new FeedReference(feed.getId());

    IBookMark bookmark = createBookMark(feed);
    IPreferenceScope preferences = Owl.getPreferenceService().getEntityScope(bookmark);
    preferences.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 0);
    preferences.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    preferences.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, false);

    /* First Reload */
    InMemoryProtocolHandler.FEED = generateFeed("Title", "http://www.link.de", null, null);
    fController.reload(bookmark, null, new NullProgressMonitor());
    assertEquals(1, feedRef.resolve().getNews().size());
    assertEquals(1, getUnreadCount(feed));
    assertEquals(1, getNewCount(feed));

    /* Set to UNREAD */
    fNewsDao.setState(feedRef.resolve().getNews(), INews.State.UNREAD, true, false);
    assertEquals(0, getNewCount(feed));

    /* Second Reload */
    fController.reload(bookmark, null, new NullProgressMonitor());
    assertEquals(0, getUnreadCount(feed));
    assertEquals(0, getNewCount(feed));

    /* Third Reload */
    InMemoryProtocolHandler.FEED = generateEmptyFeed();
    fController.reload(bookmark, null, new NullProgressMonitor());
    assertEquals(0, feedRef.resolve().getNews().size());
  }

  @Test
  @SuppressWarnings("all")
  public void testReloadFeedWithDuplicateNews() throws Exception {

    /* Duplicate News with: Title */
    {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      InMemoryProtocolHandler.FEED = generateFeedWithDuplicateNews("News Title", null, null, null, "Description", null, null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      DynamicDAO.delete(feedRef.resolve());
    }

    /* Duplicate News with: Title, Link */
    {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      InMemoryProtocolHandler.FEED = generateFeedWithDuplicateNews("News Title", "http://www.link.com", null, null, "Description", null, null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      DynamicDAO.delete(feedRef.resolve());
    }

    /* Duplicate News with: Title, Guid */
    {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      InMemoryProtocolHandler.FEED = generateFeedWithDuplicateNews("News Title", null, "http://www.link.com", null, "Description", null, null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      DynamicDAO.delete(feedRef.resolve());
    }

    /* Duplicate News with: Title, Link, Guid */
    {
      IFeed feed = new Feed(new URI("inmemory://rss_2_0.xml")); //$NON-NLS-1$
      feed = DynamicDAO.save(feed);

      FeedReference feedRef = new FeedReference(feed.getId());

      IBookMark bookmark = createBookMark(feed);

      InMemoryProtocolHandler.FEED = generateFeedWithDuplicateNews("News Title", "http://www.link.com", "http://www.guid.com", null, "Description", null, null, null);
      fController.reload(bookmark, null, new NullProgressMonitor());
      assertEquals(1, feedRef.resolve().getNews().size());
      DynamicDAO.delete(feedRef.resolve());
    }
  }

  @SuppressWarnings("nls")
  private String generateFeed(String title, String link, String guid, String pubDate) {
    return generateFeed(title, link, guid, pubDate, null, null, null, "mp3");
  }

  @SuppressWarnings("nls")
  private String generateFeed(String title, String link, String guid, String pubDate, String description) {
    return generateFeed(title, link, guid, pubDate, description, null, null, "mp3");
  }

  @SuppressWarnings("nls")
  private String generateFeedWithEnclosure(String title, String link, String enclosure, String type) {
    return generateFeed(title, link, null, null, null, null, enclosure, type);
  }

  @SuppressWarnings("nls")
  private String generateFeed(String title, String link, String guid, String pubDate, String description, String author, String enclosure, String type) {
    StringBuilder str = new StringBuilder();

    str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    str.append("<rss version=\"2.0\">\n");
    str.append("<channel>\n");
    str.append("<title>In Memory Feed</title>\n");
    str.append("<lastBuildDate>");
    str.append(fDateFormat.format(new Date(System.currentTimeMillis() + fRand.nextInt(100000))));
    str.append("</lastBuildDate>\n");

    generateNews(title, link, guid, pubDate, description, author, enclosure, type, str);

    str.append("</channel>\n");
    str.append("</rss>");

    return str.toString();
  }

  @SuppressWarnings("nls")
  private String generateFeedWithDuplicateNews(String title, String link, String guid, String pubDate, String description, String author, String enclosure, String type) {
    StringBuilder str = new StringBuilder();

    str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    str.append("<rss version=\"2.0\">\n");
    str.append("<channel>\n");
    str.append("<title>In Memory Feed</title>\n");
    str.append("<lastBuildDate>");
    str.append(fDateFormat.format(new Date(System.currentTimeMillis() + fRand.nextInt(100000))));
    str.append("</lastBuildDate>\n");

    generateNews(title, link, guid, pubDate, description, author, enclosure, type, str);
    generateNews(title, link, guid, pubDate, description, author, enclosure, type, str);
    generateNews(title, link, guid, pubDate, description, author, enclosure, type, str);

    str.append("</channel>\n");
    str.append("</rss>");

    return str.toString();
  }

  private void generateNews(String title, String link, String guid, String pubDate, String description, String author, String enclosure, String type, StringBuilder str) {
    str.append("<item>\n");

    if (title != null)
      str.append("<title>").append(title).append("</title>\n");

    if (link != null)
      str.append("<link>").append(link).append("</link>\n");

    if (guid != null)
      str.append("<guid>").append(guid).append("</guid>\n");

    if (pubDate != null)
      str.append("<pubDate>").append(pubDate).append("</pubDate>\n");

    if (description != null)
      str.append("<description>").append(description).append("</description>\n");

    if (author != null)
      str.append("<author>").append(author).append("</author>\n");

    if (enclosure != null)
      str.append("<enclosure url=\"" + enclosure + "\" type=\"" + type + "\" />\n");

    str.append("</item>\n");
  }

  @SuppressWarnings("nls")
  private String generateEmptyFeed() {
    StringBuilder str = new StringBuilder();

    str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    str.append("<rss version=\"2.0\">\n");
    str.append("<channel>\n");
    str.append("<title>In Memory Feed</title>\n");
    str.append("<lastBuildDate>");
    str.append(fDateFormat.format(new Date(System.currentTimeMillis() + fRand.nextInt(100000))));
    str.append("</lastBuildDate>\n");

    str.append("</channel>\n");
    str.append("</rss>");

    return str.toString();
  }

  private IBookMark createBookMark(IFeed feed) throws PersistenceException {
    IFolder folder = DynamicDAO.save(Owl.getModelFactory().createFolder(null, null, "Root"));

    return DynamicDAO.save(Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark"));
  }
}
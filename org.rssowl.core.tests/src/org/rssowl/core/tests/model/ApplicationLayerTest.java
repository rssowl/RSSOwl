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

package org.rssowl.core.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.IApplicationService;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.MergeResult;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.tests.TestUtils;
import org.rssowl.core.util.ReparentInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * This TestCase is for testing the IApplicationLayer.
 *
 * @author bpasero
 */
@SuppressWarnings("nls")
public class ApplicationLayerTest extends LargeBlockSizeTest {
  private IModelFactory fFactory;
  private IApplicationService fAppService;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
    fAppService = Owl.getApplicationService();
  }

  /**
   * See bug #184 : State change and async loading of the equivalent news in different
   * feeds can lead to incorrect behaviour.
   *
   * @throws Exception
   */
  @Test
  public void testHandleReloadedWithAsyncLoadingOfEquivalentNews() throws Exception {
    IFeed feed0 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    URI newsLink = new URI("http://www.news.com");
    INews news0 = fFactory.createNews(null, feed0, new Date());
    news0.setLink(newsLink);
    news0.setState(INews.State.READ);
    DynamicDAO.save(feed0);

    IFolder folder = fFactory.createFolder(null, null, "Folder");
    DynamicDAO.save(folder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    DynamicDAO.save(feed1);
    IBookMark mark1 = fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "Mark1");
    DynamicDAO.save(folder);

    feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setLink(newsLink);
    fAppService.handleFeedReload(mark1, feed1, null, false, true, new NullProgressMonitor());

    assertEquals(INews.State.READ, DynamicDAO.load(INews.class, news1.getId()).getState());
  }

  /**
   * Tests that {@link IBookMark#getMostRecentNewsDate()} is set correctly during
   * {@link IApplicationService#handleFeedReload(IBookMark, IFeed, org.rssowl.core.persist.IConditionalGet, boolean)}
   * @throws Exception
   */
  @Test
  public void testBookMarkLastNewNewsDateIsSetDuringReload() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Folder");

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    DynamicDAO.save(feed1);
    IBookMark mark1 = fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "Mark1");
    DynamicDAO.save(folder);
    assertNull(mark1.getMostRecentNewsDate());

    long time = System.currentTimeMillis();
    feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    fFactory.createNews(null, feed1, new Date());
    fAppService.handleFeedReload(mark1, feed1, null, false, true, new NullProgressMonitor());
    assertNotNull(mark1.getMostRecentNewsDate());
    long lastUpdatedDate = mark1.getMostRecentNewsDate().getTime();
    assertTrue(time <= lastUpdatedDate);

    feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    fAppService.handleFeedReload(mark1, feed1, null, false, true, new NullProgressMonitor());
    assertEquals(lastUpdatedDate, mark1.getMostRecentNewsDate().getTime());
  }

  /**
   * See bug #317 : Retention strategy works incorrectly if news is deleted
   * before being saved.
   *
   * @throws Exception
   */
  @Test
  public void testHandleFeedReloadWithRetentionStrategy() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.rssowl.org"));
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    IBookMark mark = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark");
    DynamicDAO.save(feed);
    FeedReference feedRef = new FeedReference(feed.getId());
    DynamicDAO.save(folder);

    IFeed emptyFeed = fFactory.createFeed(null, feed.getLink());
    INews news = fFactory.createNews(null, emptyFeed, new Date());
    news.setState(INews.State.READ);

    Owl.getPreferenceService().getEntityScope(mark).putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    fAppService.handleFeedReload(mark, emptyFeed, null, false, true, new NullProgressMonitor());

    feed = null;
    System.gc();

    feed = feedRef.resolve();
    assertEquals(1, feed.getNews().size());
    assertEquals(0, feed.getVisibleNews().size());
    assertEquals(INews.State.DELETED, DynamicDAO.load(INews.class, news.getId()).getState());
  }

  @Test
  public void testHandleFeedReloadWithoutRetentionStrategy() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.rssowl.org"));
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    IBookMark mark = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark");
    DynamicDAO.save(feed);
    FeedReference feedRef = new FeedReference(feed.getId());
    DynamicDAO.save(folder);

    IFeed emptyFeed = fFactory.createFeed(null, feed.getLink());
    INews news = fFactory.createNews(null, emptyFeed, new Date());
    news.setState(INews.State.READ);

    Owl.getPreferenceService().getEntityScope(mark).putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    fAppService.handleFeedReload(mark, emptyFeed, null, false, false, new NullProgressMonitor());

    feed = null;
    System.gc();

    feed = feedRef.resolve();
    assertEquals(1, feed.getNews().size());
    assertEquals(1, feed.getVisibleNews().size());
    assertEquals(INews.State.READ, DynamicDAO.load(INews.class, news.getId()).getState());
  }

  /**
   * Tests that Feed#mergeNews(List, boolean) works properly during
   * handleFeedReload in the fast path when there are many news to be removed.
   *
   * @throws Exception
   */
  @Test
  public void testHandleFeedReloadWithNewsToCleanUpBiggerThanTwenty() throws Exception  {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.rssowl.org"));
    int totalNews = 100;
    for (int i = 0; i < totalNews; ++i) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setGuid(fFactory.createGuid(news, String.valueOf(i), true));
    }
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    IBookMark mark = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark");
    DynamicDAO.save(feed);
    FeedReference feedRef = new FeedReference(feed.getId());
    DynamicDAO.save(folder);

    int i = 0;
    int deletedNewsCount = 40;
    for (INews news : feed.getNews()) {
      if (i == deletedNewsCount)
        break;

      news.setState(State.DELETED);
      ++i;
    }

    IFeed emptyFeed = fFactory.createFeed(null, feed.getLink());

    fAppService.handleFeedReload(mark, emptyFeed, null, false, true, new NullProgressMonitor());

    feed = null;
    System.gc();

    feed = feedRef.resolve();
    int expectedNewsCount = totalNews - deletedNewsCount;
    assertEquals(expectedNewsCount, feed.getNews().size());
    assertEquals(expectedNewsCount, DynamicDAO.loadAll(INews.class).size());
  }

  /**
   * See bug #318 : If attachments are deleted as part of a reload, oldNews are
   * not filled in NewsEvent.
   *
   * @throws Exception
   */
  @Test
  public void testHandleFeedReloadFillsOldNewsWithAttachmentDeleted() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = fFactory.createFeed(null, new URI("http://www.rssowl.org"));
      INews news = fFactory.createNews(null, feed, new Date());
      fFactory.createGuid(news, "newsguid", null);
      fFactory.createAttachment(null, news);
      IFolder folder = fFactory.createFolder(null, null, "Folder");
      IBookMark mark = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark");
      DynamicDAO.save(feed);
      DynamicDAO.save(folder);

      IFeed emptyFeed = fFactory.createFeed(null, feed.getLink());
      INews emptyNews = fFactory.createNews(null, emptyFeed, new Date());
      fFactory.createGuid(emptyNews, news.getGuid().getValue(), null);

      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          assertEquals(1, events.size());
          assertNotNull(events.iterator().next().getOldNews());
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      fAppService.handleFeedReload(mark, emptyFeed, null, false, true, new NullProgressMonitor());
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests that calling setNewsState with force = true fires both events even
   * though the news state has not changed.
   *
   * @throws Exception
   */
  @Test
  public void testSetNewsStateWithEquivalentNewsAndForce() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com"));
      IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));

      INews news1 = fFactory.createNews(null, feed1, new Date());
      news1.setLink(new URI("www.link.com"));

      INews news2 = fFactory.createNews(null, feed2, new Date());
      news2.setLink(new URI("www.link.com"));

      fFactory.createNews(null, feed1, new Date());
      fFactory.createNews(null, feed2, new Date());

      DynamicDAO.save(feed1);
      DynamicDAO.save(feed2);
      List<INews> newsList = new ArrayList<INews>(1);
      newsList.add(news2);
      final boolean[] newsUpdatedCalled = new boolean[1];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          newsUpdatedCalled[0] = true;
          assertEquals(2, events.size());
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, INews.State.NEW, true, true);
      assertEquals(true, newsUpdatedCalled[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests that news with different Guid (permaLink == true) and same link
   * are not considered equivalent during setState.
   * @throws Exception
   */
  @Test
  public void testSetNewsStateWithAffectEquivalentNewsAndGuidDifferentAndLinkEqual() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com"));
      IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));

      INews news1 = fFactory.createNews(null, feed1, new Date());
      fFactory.createGuid(news1, "guid1", true);
      news1.setLink(new URI("www.link.com"));

      final INews news2 = fFactory.createNews(null, feed2, new Date());
      fFactory.createGuid(news2, "guid2", true);
      news2.setLink(new URI("www.link.com"));

      fFactory.createNews(null, feed1, new Date());
      fFactory.createNews(null, feed2, new Date());

      DynamicDAO.save(feed1);
      DynamicDAO.save(feed2);
      List<INews> newsList = new ArrayList<INews>(1);
      newsList.add(news2);
      final boolean[] newsUpdatedCalled = new boolean[1];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          newsUpdatedCalled[0] = true;
          assertEquals(1, events.size());
          assertEquals(news2, events.iterator().next().getEntity());
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, INews.State.NEW, true, true);
      assertEquals(true, newsUpdatedCalled[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests that the all NewsEvents issued after a call to setNewsState are fully
   * activated even if there was an equivalent news that was not in memory.
   *
   * @throws Exception
   */
  @Test
  public void testSetNewsStateWithEquivalentNewsHasNewsEventEntityActivated() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com"));
      IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));

      INews news1 = fFactory.createNews(null, feed1, new Date());
      news1.setLink(new URI("www.link.com"));

      INews news2 = fFactory.createNews(null, feed2, new Date());
      news2.setLink(new URI("www.link.com"));

      fFactory.createNews(null, feed1, new Date());
      fFactory.createNews(null, feed2, new Date());

      DynamicDAO.save(feed1);
      feed2 = DynamicDAO.save(feed2);
      feed1 = null;
      feed2 = null;
      news1 = null;
      System.gc();

      List<INews> newsList = Collections.singletonList(news2);
      final boolean[] newsUpdatedCalled = new boolean[1];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          newsUpdatedCalled[0] = true;
          assertEquals(2, events.size());
          for (NewsEvent event : events) {
            IFeed feed = event.getEntity().getFeedReference().resolve();

            /* This should be enough to verify that the news is fully activated */
            assertNotNull(feed.getId());
            assertNotNull(feed.getNews());
            assertNotNull(feed.getNews().get(0));
          }
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, INews.State.READ, true, false);
      assertEquals(true, newsUpdatedCalled[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests that calling ApplicationLayerImpl#saveFeed(MergeResult) does not
   * cause news to be lost in the feed. See bug #276.
   *
   * @throws Exception
   */
  @Test
  public void testSaveFeedNewsLost() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news0 = fFactory.createNews(null, feed, new Date());
    news0.setLink(new URI("http://www.feed.com/news1"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    URI news1Link = new URI("http://www.feed.com/news2");
    news1.setLink(news1Link);
    DynamicDAO.save(feed);
    long feedId = feed.getId();
    INews news = feed.getNews().get(0);
    feed.removeNews(news);
    MergeResult mergeResult = new MergeResult();
    mergeResult.addRemovedObject(news);
    mergeResult.addUpdatedObject(feed);
    TestUtils.saveFeed(mergeResult);
    feed = null;
    news0 = null;
    news1 = null;
    mergeResult = null;
    news = null;
    System.gc();
    feed = DynamicDAO.load(IFeed.class, feedId);
    assertEquals(1, feed.getNews().size());
    assertEquals(news1Link, feed.getNews().get(0).getLink());
  }

  /**
   * Tests that {@link DynamicDAO#saveAll(Collection)} sets the current and old
   * state correctly when firing a newsUpdated event.
   *
   * @throws Exception
   */
  @Test
  public void testSaveNewsSetsCurrentAndOldState() throws Exception {
    IFeed feed = new Feed(new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news = fFactory.createNews(null, feed, new Date());
    news.setTitle("News Title #1");
    news.setLink(new URI("http://www.link.com"));
    news.setState(INews.State.UNREAD);

    final ICategory category = fFactory.createCategory(null, news);
    category.setName("Category name");

    final IAttachment attachment = fFactory.createAttachment(null, news);
    attachment.setLink(new URI("http://attachment.com"));

    feed = DynamicDAO.save(feed);

    final INews savedNews = feed.getNews().get(0);
    savedNews.setTitle("News Title Updated #1");

    Collection<INews> newsList = new ArrayList<INews>();
    newsList.add(savedNews);

    NewsListener newsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        NewsEvent event = events.iterator().next();
        assertEquals(true, event.getEntity().equals(savedNews));
        INews oldNews = event.getOldNews();
        assertEquals(State.UNREAD, oldNews.getState());
        assertEquals(State.UNREAD, event.getEntity().getState());
        assertEquals(category.getName(), oldNews.getCategories().get(0).getName());
        IAttachment oldAttachment = oldNews.getAttachments().get(0);
        assertEquals(attachment.getLink(), oldAttachment.getLink());
        assertNull(oldAttachment.getNews());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    try {
      DynamicDAO.saveAll(newsList);
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
    newsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        NewsEvent event = events.iterator().next();
        assertEquals(savedNews.getId().longValue(), event.getEntity().getId().longValue());
        assertEquals(State.UNREAD, event.getOldNews().getState());
        assertEquals(State.UPDATED, event.getEntity().getState());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    newsList.iterator().next().setState(State.UPDATED);
    try {
      DynamicDAO.saveAll(newsList);
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests {@link IFeedDAO#loadReference(URI)}.
   *
   * @throws Exception
   */
  @Test
  public void testLoadFeedReference() throws Exception {
    URI feed1Url = new URI("http://www.feed1.com");
    IFeed feed1 = new Feed(feed1Url);
    feed1 = DynamicDAO.save(feed1);

    URI feed2Url = new URI("http://www.feed2.com");
    IFeed feed2 = new Feed(feed2Url);
    feed2 = DynamicDAO.save(feed2);

    assertEquals(true, Owl.getPersistenceService().getDAOService().getFeedDAO().exists(feed1Url));

    assertEquals(true, Owl.getPersistenceService().getDAOService().getFeedDAO().exists(feed2Url));
  }

  /**
   * Tests {@link IFeedDAO#load(URI)}.
   *
   * @throws Exception
   */
  @Test
  public void testLoadFeed() throws Exception {
    URI feed1Url = new URI("http://www.feed1.com");
    IFeed feed1 = new Feed(feed1Url);
    feed1 = DynamicDAO.save(feed1);

    URI feed2Url = new URI("http://www.feed2.com");
    IFeed feed2 = new Feed(feed2Url);
    feed2 = DynamicDAO.save(feed2);

    assertEquals(feed1, Owl.getPersistenceService().getDAOService().getFeedDAO().load(feed1Url));
    assertEquals(feed2, Owl.getPersistenceService().getDAOService().getFeedDAO().load(feed2Url));
  }

  /**
   * Tests {@link IFeedDAO#load(URI)}.
   *
   * @throws Exception
   */
  @Test
  public void testLoadFeedActivation() throws Exception {
    URI feed1Url = new URI("http://www.feed1.com");
    IFeed feed1 = fFactory.createFeed(null, feed1Url);
    fFactory.createNews(null, feed1, new Date());
    feed1 = DynamicDAO.save(feed1);
    long newsId = feed1.getNews().get(0).getId();
    feed1 = null;
    System.gc();
    feed1 = Owl.getPersistenceService().getDAOService().getFeedDAO().load(feed1Url);
    assertNotNull(feed1);
    assertEquals(1, feed1.getNews().size());
    assertEquals(newsId, feed1.getNews().get(0).getId().longValue());
  }

  /**
   * Tests {@link DynamicDAO#saveAll(Collection)}.
   *
   * @throws Exception
   */
  @Test
  public void testSaveNews() throws Exception {
    IFeed feed1 = new Feed(new URI("http://www.feed1.com"));
    INews news11 = fFactory.createNews(null, feed1, new Date());
    news11.setLink(new URI("http://www.link11.com"));
    INews news12 = fFactory.createNews(null, feed1, new Date());
    news12.setLink(new URI("http://www.link12.com"));
    feed1 = DynamicDAO.save(feed1);

    IFeed feed2 = new Feed(new URI("http://www.feed2.com"));
    INews news21 = fFactory.createNews(null, feed2, new Date());
    news21.setLink(new URI("http://www.link21.com"));
    INews news22 = fFactory.createNews(null, feed2, new Date());
    news22.setLink(new URI("http://www.link22.com"));
    feed2 = DynamicDAO.save(feed2);

    final List<INews> newsList = new ArrayList<INews>();

    for (INews news : feed1.getNews())
      newsList.add(news);

    for (INews news : feed2.getNews())
      newsList.add(news);

    for (INews news : newsList) {
      news.setComments("updated comments");
    }

    final boolean newsUpdatedCalled[] = new boolean[1];
    NewsListener newsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        assertEquals(newsUpdatedCalled[0], false);
        newsUpdatedCalled[0] = true;
        assertEquals(newsList.size(), events.size());
        for (NewsEvent event : events) {
          assertEquals(true, event.isRoot());
          boolean newsFound = false;
          for (INews news : newsList) {
            if (event.getEntity().equals(news)) {
              newsFound = true;
              break;
            }
          }
          assertEquals(true, newsFound);
        }
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    try {
      DynamicDAO.saveAll(newsList);
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
    assertEquals(true, newsUpdatedCalled[0]);
  }

  /**
   * Test {@link IFolderDAO#reparent(List)}
   *
   * @throws Exception
   */
  @Test
  public void testReparentFolderAndMark() throws Exception {
    FolderListener folderListener = null;
    BookMarkListener bookMarkListener = null;
    SearchMarkListener searchMarkListener = null;
    try {
      /* Add */
      final IFolder oldMarkParent = fFactory.createFolder(null, null, "Old parent");
      final IBookMark bookMark = fFactory.createBookMark(null, oldMarkParent, new FeedLinkReference(new URI("http://www.link.com")), "bookmark");
      final ISearchMark searchMark = fFactory.createSearchMark(null, oldMarkParent, "searchmark");
      DynamicDAO.save(oldMarkParent);

      final IFolder newMarkParent = fFactory.createFolder(null, null, "New parent");
      fFactory.createFolder(null, newMarkParent, "New parent child");
      DynamicDAO.save(newMarkParent);

      /* Add */
      final IFolder oldFolderParent = fFactory.createFolder(null, null, "Old parent");
      final IFolder folder = fFactory.createFolder(null, oldFolderParent, "Folder");
      DynamicDAO.save(oldFolderParent);

      final IFolder newFolderParent = fFactory.createFolder(null, null, "New parent");
      fFactory.createFolder(null, newFolderParent, "New parent child");
      DynamicDAO.save(newFolderParent);

      final boolean[] folderUpdateEventOccurred = new boolean[1];
      folderListener = new FolderListener() {
        public void entitiesAdded(Set<FolderEvent> events) {
          fail("Unexpected event");
        }

        public void entitiesDeleted(Set<FolderEvent> events) {
          fail("Unexpected event");
        }

        public void entitiesUpdated(Set<FolderEvent> events) {
          folderUpdateEventOccurred[0] = true;
          assertEquals(7, events.size());
          boolean foundFolder = false;
          for (FolderEvent event : events) {
            if (event.getEntity().equals(folder)) {
              foundFolder = true;
              assertTrue("Expected this Event to be Root Event", event.isRoot());
              assertEquals(oldFolderParent, event.getOldParent());
              assertEquals(newFolderParent, event.getEntity().getParent());
            } else
              assertFalse("Expected this Event to be NO Root Event", event.isRoot());
          }
          assertTrue("No event was issued for folder", foundFolder);
        }
      };
      final boolean[] bookMarkUpdateEventOccurred = new boolean[1];
      bookMarkListener = new BookMarkListener() {
        public void entitiesAdded(Set<BookMarkEvent> events) {
          fail("Unexpected event");
        }

        public void entitiesDeleted(Set<BookMarkEvent> events) {
          fail("Unexpected event");
        }

        public void entitiesUpdated(Set<BookMarkEvent> events) {
          bookMarkUpdateEventOccurred[0] = true;
          assertEquals(1, events.size());
          BookMarkEvent event = events.iterator().next();
          assertEquals(bookMark, event.getEntity());
          assertTrue("Expected this Event to be Root Event", event.isRoot());
          assertEquals(oldMarkParent, event.getOldParent());
          assertEquals(newMarkParent, event.getEntity().getParent());
        }
      };

      final boolean[] searchMarkUpdateEventOccurred = new boolean[1];
      searchMarkListener = new SearchMarkListener() {
        public void entitiesAdded(Set<SearchMarkEvent> events) {
          fail("Unexpected event");
        }

        public void entitiesDeleted(Set<SearchMarkEvent> events) {
          fail("Unexpected event");
        }

        public void entitiesUpdated(Set<SearchMarkEvent> events) {
          searchMarkUpdateEventOccurred[0] = true;
          assertEquals(1, events.size());
          SearchMarkEvent event = events.iterator().next();
          assertEquals(searchMark, event.getEntity());
          assertTrue("Expected this Event to be Root Event", event.isRoot());
          assertEquals(oldMarkParent, event.getOldParent());
          assertEquals(newMarkParent, event.getEntity().getParent());
        }

        public void newsChanged(Set<SearchMarkEvent> events) {
          fail("Unexpected event");
        }
      };

      DynamicDAO.addEntityListener(IFolder.class, folderListener);
      DynamicDAO.addEntityListener(IBookMark.class, bookMarkListener);
      DynamicDAO.addEntityListener(ISearchMark.class, searchMarkListener);

      List<ReparentInfo<IFolderChild, IFolder>> reparentInfos = new ArrayList<ReparentInfo<IFolderChild, IFolder>>();
      reparentInfos.add(new ReparentInfo<IFolderChild, IFolder>(folder, newFolderParent, null, null));
      reparentInfos.add(new ReparentInfo<IFolderChild, IFolder>(bookMark, newMarkParent, null, null));
      reparentInfos.add(new ReparentInfo<IFolderChild, IFolder>(searchMark, newMarkParent, null, null));

      Owl.getPersistenceService().getDAOService().getFolderDAO().reparent(reparentInfos);

      /* Asserts Follow */

      /* Folder reparenting */
      assertFalse(oldFolderParent.getFolders().contains(folder));
      assertEquals(0, oldFolderParent.getFolders().size());
      assertTrue(newFolderParent.getFolders().contains(folder));
      assertEquals(newFolderParent, folder.getParent());
      assertEquals(2, newFolderParent.getFolders().size());

      /* Marks reparenting */
      assertFalse(oldMarkParent.getFolders().contains(bookMark));
      assertFalse(oldMarkParent.getFolders().contains(searchMark));
      assertEquals(0, oldMarkParent.getMarks().size());
      assertTrue(newMarkParent.getMarks().contains(bookMark));
      assertTrue(newMarkParent.getMarks().contains(searchMark));
      assertEquals(newMarkParent, bookMark.getParent());
      assertEquals(newMarkParent, searchMark.getParent());
      assertEquals(2, newMarkParent.getMarks().size());

      /* Events fired */
      assertTrue("Missing folderUpdated Event", folderUpdateEventOccurred[0]);
      assertTrue("Missing bookMarkUpdated Event", bookMarkUpdateEventOccurred[0]);
      assertTrue("Missing searchMarkUpdated Event", searchMarkUpdateEventOccurred[0]);

      DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      DynamicDAO.removeEntityListener(IBookMark.class, bookMarkListener);
      DynamicDAO.removeEntityListener(ISearchMark.class, searchMarkListener);
    } finally {
      /* Cleanup */
      if (folderListener != null)
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      if (bookMarkListener != null)
        DynamicDAO.removeEntityListener(IBookMark.class, bookMarkListener);
      if (searchMarkListener != null)
        DynamicDAO.removeEntityListener(ISearchMark.class, searchMarkListener);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadBookMarks() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.myfeed.com"));
    DynamicDAO.save(feed);
    FeedLinkReference feedLinkRef = new FeedLinkReference(feed.getLink());

    Collection<IBookMark> emptyBookmarks = Owl.getPersistenceService().getDAOService().getBookMarkDAO().loadAll(feedLinkRef);
    assertEquals(0, emptyBookmarks.size());

    IFolder root1 = fFactory.createFolder(null, null, "Root 1");
    FolderReference root1Ref = new FolderReference(DynamicDAO.save(root1).getId());

    IFolder childOfRoot1 = fFactory.createFolder(null, root1Ref.resolve(), "Child of Root 1");
    FolderReference childOfRoot1Ref = new FolderReference(DynamicDAO.save(childOfRoot1).getId());

    IBookMark bookmark1 = fFactory.createBookMark(null, root1Ref.resolve(), new FeedLinkReference(feed.getLink()), "Bookmark 1");
    IBookMark bookmark2 = fFactory.createBookMark(null, root1Ref.resolve(), new FeedLinkReference(feed.getLink()), "Bookmark 2");
    IBookMark bookmark3 = fFactory.createBookMark(null, childOfRoot1Ref.resolve(), new FeedLinkReference(feed.getLink()), "Bookmark 3");

    BookMarkReference bookmarkRef1 = new BookMarkReference(DynamicDAO.save(bookmark1).getId());
    BookMarkReference bookmarkRef2 = new BookMarkReference(DynamicDAO.save(bookmark2).getId());
    BookMarkReference bookmarkRef3 = new BookMarkReference(DynamicDAO.save(bookmark3).getId());

    Collection<IBookMark> filledBookmarks = Owl.getPersistenceService().getDAOService().getBookMarkDAO().loadAll(feedLinkRef);
    assertEquals(3, filledBookmarks.size());
    for (IBookMark mark : filledBookmarks) {
      if (bookmarkRef1.resolve().equals(mark))
        assertEquals(bookmark1.getName(), mark.getName());
      else if (bookmarkRef2.resolve().equals(mark))
        assertEquals(bookmark2.getName(), mark.getName());
      else if (bookmarkRef3.resolve().equals(mark))
        assertEquals(bookmark3.getName(), mark.getName());
      else
        fail();
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadBookMarksActivation() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.myfeed.com"));
    FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());
    long feedId = feedRef.getId();
    IFolder root1 = fFactory.createFolder(null, null, "Root 1");
    final String folderName = root1.getName();
    fFactory.createBookMark(null, root1, new FeedLinkReference(feed.getLink()), "Bookmark 1");
    feedRef = null;
    feed = null;
    DynamicDAO.save(root1);
    root1 = null;
    System.gc();

    feed = DynamicDAO.load(IFeed.class, feedId);
    FeedLinkReference feedLinkRef = new FeedLinkReference(feed.getLink());
    Collection<IBookMark> marks = Owl.getPersistenceService().getDAOService().getBookMarkDAO().loadAll(feedLinkRef);
    assertEquals(1, marks.size());
    assertEquals(folderName, marks.iterator().next().getParent().getName());
    marks = null;
    System.gc();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadAllBookMarks() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.myfeed.com"));
    DynamicDAO.save(feed);

    IBookMarkDAO markDAO = DynamicDAO.getDAO(IBookMarkDAO.class);
    Collection<IBookMark> emptyBookmarks = markDAO.loadAll();
    assertEquals(0, emptyBookmarks.size());

    IFolder root1 = fFactory.createFolder(null, null, "Root 1");
    root1 = DynamicDAO.save(root1);

    IFolder childOfRoot1 = fFactory.createFolder(null, root1, "Child of Root 1");
    childOfRoot1 = DynamicDAO.save(childOfRoot1);

    IBookMark bookmark1 = fFactory.createBookMark(null, root1, new FeedLinkReference(feed.getLink()), "Bookmark 1");
    IBookMark bookmark2 = fFactory.createBookMark(null, root1, new FeedLinkReference(feed.getLink()), "Bookmark 2");
    IBookMark bookmark3 = fFactory.createBookMark(null, childOfRoot1, new FeedLinkReference(feed.getLink()), "Bookmark 3");

    BookMarkReference bookmarkRef1 = new BookMarkReference(DynamicDAO.save(bookmark1).getId());
    BookMarkReference bookmarkRef2 = new BookMarkReference(DynamicDAO.save(bookmark2).getId());
    BookMarkReference bookmarkRef3 = new BookMarkReference(DynamicDAO.save(bookmark3).getId());

    Collection<IBookMark> filledBookmarks = DynamicDAO.loadAll(IBookMark.class);
    assertEquals(3, filledBookmarks.size());

    filledBookmarks = markDAO.loadAll();
    assertEquals(3, filledBookmarks.size());

    for (IBookMark mark : filledBookmarks) {
      if (bookmarkRef1.resolve().equals(mark))
        assertEquals(bookmark1.getName(), mark.getName());
      else if (bookmarkRef2.resolve().equals(mark))
        assertEquals(bookmark2.getName(), mark.getName());
      else if (bookmarkRef3.resolve().equals(mark))
        assertEquals(bookmark3.getName(), mark.getName());
      else
        fail();
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadAllBookMarksActivation() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.myfeed.com"));
    DynamicDAO.save(feed);

    IFolder root1 = fFactory.createFolder(null, null, "Root 1");
    final String folderName = root1.getName();
    fFactory.createBookMark(null, root1, new FeedLinkReference(feed.getLink()), "Bookmark 1");
    feed = null;
    DynamicDAO.save(root1);
    root1 = null;
    System.gc();

    IBookMarkDAO markDAO = DynamicDAO.getDAO(IBookMarkDAO.class);
    Collection<IBookMark> marks = markDAO.loadAll();
    assertEquals(1, marks.size());
    assertEquals(folderName, marks.iterator().next().getParent().getName());
    marks = null;
    System.gc();

    marks = markDAO.loadAll();
    assertEquals(1, marks.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadLabels() throws Exception {
    ILabel label1 = fFactory.createLabel(null, "Important");
    label1.setColor("159,63,63");
    DynamicDAO.save(label1);

    ILabel label2 = fFactory.createLabel(null, "Important");
    label2.setColor("255,153,0");
    DynamicDAO.save(label2);

    ILabel label3 = fFactory.createLabel(null, "Personal");
    label3.setColor("0,153,0");
    DynamicDAO.save(label3);

    Collection<ILabel> labels = DynamicDAO.loadAll(ILabel.class);

    assertEquals(3, labels.size());
    for (ILabel label : labels) {
      if (label.equals(label1)) {
        assertEquals("Important", label.getName());
        assertEquals("159,63,63", label.getColor());
      }

      else if (label.equals(label2)) {
        assertEquals("Important", label.getName());
        assertEquals("255,153,0", label.getColor());
      }

      else if (label.equals(label3)) {
        assertEquals("Personal", label.getName());
        assertEquals("0,153,0", label.getColor());
      }
    }
  }

  /**
   *
   */
  public void testLoadLabelsActivation() {
    String colour = "159,63,63";
    ILabel label1 = fFactory.createLabel(null, "Important");
    label1.setColor(colour);
    DynamicDAO.save(label1);
    label1 = null;
    System.gc();

    Collection<ILabel> labels = DynamicDAO.loadAll(ILabel.class);

    assertEquals(1, labels.size());
    assertEquals(colour, labels.iterator().next().getColor());
  }

  /**
   * Test the Method loadRootFolders()
   *
   * @throws Exception
   */
  @Test
  public void testLoadRootFolders() throws Exception {
    IFolder root1 = fFactory.createFolder(null, null, "Root 1");
    IFolder root2 = fFactory.createFolder(null, null, "Root 2");
    IFolder root3 = fFactory.createFolder(null, null, "Root 3");

    fFactory.createFolder(null, root1, "Child of Root 1");
    fFactory.createFolder(null, root2, "Child of Root 2");

    FolderReference root1Ref = new FolderReference(DynamicDAO.save(root1).getId());
    FolderReference root2Ref = new FolderReference(DynamicDAO.save(root2).getId());
    FolderReference root3Ref = new FolderReference(DynamicDAO.save(root3).getId());

    Collection<IFolder> rootFolders = Owl.getPersistenceService().getDAOService().getFolderDAO().loadRoots();
    assertEquals(3, rootFolders.size());
    for (IFolder folder : rootFolders) {
      if (root1Ref.resolve().equals(folder))
        assertEquals("Root 1", folder.getName());
      else if (root2Ref.resolve().equals(folder))
        assertEquals("Root 2", folder.getName());
      else if (root3Ref.resolve().equals(folder))
        assertEquals("Root 3", folder.getName());
      else
        fail();
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadRootFoldersActivation() throws Exception {
    IFolder root1 = fFactory.createFolder(null, null, "Root 1");
    fFactory.createFolder(null, root1, "Child of Root 1");
    String childFolderName = root1.getFolders().get(0).getName();
    DynamicDAO.save(root1);
    root1 = null;
    System.gc();

    Collection<IFolder> rootFolders = Owl.getPersistenceService().getDAOService().getFolderDAO().loadRoots();
    assertEquals(1, rootFolders.size());
    IFolder folder = rootFolders.iterator().next();
    assertEquals(1, folder.getFolders().size());
    assertEquals(childFolderName, folder.getFolders().get(0).getName());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSetNewsState() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      fFactory.createNews(null, feed, new Date());
      fFactory.createNews(null, feed, new Date());
      fFactory.createNews(null, feed, new Date());

      Feed savedFeed = (Feed) DynamicDAO.save(feed);
      assertTrue(savedFeed.isIdentical(DynamicDAO.load(IFeed.class, savedFeed.getId())));

      INews news1 = savedFeed.getNews().get(0);
      INews news2 = savedFeed.getNews().get(1);
      INews news3 = savedFeed.getNews().get(2);

      List<INews> news = new ArrayList<INews>();
      news.add(news1);
      news.add(news2);

      assertEquals(news1.getState(), INews.State.NEW);
      assertEquals(news2.getState(), INews.State.NEW);
      assertEquals(news3.getState(), INews.State.NEW);

      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          fail("Unexpected Event");
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Unexpected Event");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          assertEquals(2, events.size());
          for (NewsEvent event : events)
            assertEquals(true, event.isRoot());
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(news, INews.State.UNREAD, true, false);

      assertEquals(news1.getState(), INews.State.UNREAD);
      assertEquals(news2.getState(), INews.State.UNREAD);
      assertEquals(news3.getState(), INews.State.NEW);

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(news, INews.State.READ, true, false);

      assertEquals(news1.getState(), INews.State.READ);
      assertEquals(news2.getState(), INews.State.READ);
      assertEquals(news3.getState(), INews.State.NEW);

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(news, INews.State.DELETED, true, false);

      assertEquals(news1.getState(), INews.State.DELETED);
      assertEquals(news2.getState(), INews.State.DELETED);
      assertEquals(news3.getState(), INews.State.NEW);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests {@link INewsDAO#loadAll(FeedLinkReference, Set)}.
   * @throws Exception
   */

  @Test
  public void testLoadAllNewsByFeedAndState() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    FeedLinkReference feedRef = news.getFeedReference();
    news.setState(INews.State.HIDDEN);
    fFactory.createNews(null, feed, new Date()).setState(INews.State.UNREAD);
    fFactory.createNews(null, feed, new Date()).setState(INews.State.UPDATED);
    DynamicDAO.save(feed);

    IFeed anotherFeed = fFactory.createFeed(null, new URI("http://www.anotherfeed.com"));
    INews anotherNews = fFactory.createNews(null, anotherFeed, new Date());
    FeedLinkReference anotherFeedRef = anotherNews.getFeedReference();
    fFactory.createNews(null, anotherFeed, new Date());
    fFactory.createNews(null, anotherFeed, new Date()).setState(INews.State.DELETED);
    DynamicDAO.save(anotherFeed);

    INewsDAO newsDao = DynamicDAO.getDAO(INewsDAO.class);

    /* All states */
    Collection<INews> newsCollection = newsDao.loadAll(feedRef, EnumSet.allOf(INews.State.class));
    assertEquals(feed.getNews().size(), newsCollection.size());
    for (INews newsItem : feed.getNews())
      assertEquals(true, newsCollection.contains(newsItem));

    newsCollection = newsDao.loadAll(anotherFeedRef, EnumSet.allOf(INews.State.class));
    assertEquals(anotherFeed.getNews().size(), newsCollection.size());
    for (INews newsItem : anotherFeed.getNews())
      assertEquals(true, newsCollection.contains(newsItem));

    /* Two matching states */
    newsCollection = newsDao.loadAll(feedRef, EnumSet.of(INews.State.UNREAD, INews.State.UPDATED));
    assertEquals(2, newsCollection.size());
    assertEquals(true, newsCollection.contains(feed.getNews().get(1)));
    assertEquals(true, newsCollection.contains(feed.getNews().get(2)));

    /* One matching state */
    newsCollection = newsDao.loadAll(anotherFeedRef, EnumSet.of(INews.State.DELETED));
    assertEquals(1, newsCollection.size());
    assertEquals(anotherFeed.getNews().get(2), newsCollection.iterator().next());

    /* No matching state */
    newsCollection = newsDao.loadAll(feedRef, EnumSet.of(INews.State.DELETED));
    assertEquals(0, newsCollection.size());

    /* One state with two matches and two states with no matches */
    newsCollection = newsDao.loadAll(anotherFeedRef, EnumSet.of(INews.State.NEW, INews.State.HIDDEN, INews.State.UPDATED));
    assertEquals(2, newsCollection.size());
    assertEquals(true, newsCollection.contains(anotherFeed.getNews().get(0)));
    assertEquals(true, newsCollection.contains(anotherFeed.getNews().get(1)));

    /* Empty states */
    newsCollection = newsDao.loadAll(feedRef, EnumSet.noneOf(INews.State.class));
    assertEquals(0, newsCollection.size());
  }

  /**
   * See bug #558 : Consider not using GUID if isPermaLink is false.
   *
   * <p>
   * Tests that we consider a Guid#isPermaLink == false in the same way we
   * consider a null Guid when calling INewsDao#setState with
   * affectEquivalentNews == true. Note that this should happen no matter
   * what happens in the comparison of Guid#getValue for both News.
   * </p>
   *
   * @throws Exception
   */
  @Test
  public void testSetNewsStateWithGuidIsPermaLinkFalse() throws Exception {
    NewsListener newsListener = null;
    try {
      IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com"));
      IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
      IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));
      IFeed feed4 = fFactory.createFeed(null, new URI("http://www.feed4.com"));

      INews news1 = fFactory.createNews(null, feed1, new Date());
      String link = "www.link.com";
      fFactory.createGuid(news1, link, false);
      news1.setLink(new URI(link));

      INews news2 = fFactory.createNews(null, feed2, new Date());
      fFactory.createGuid(news2, link, false);
      news2.setLink(new URI(link));

      INews news3 = fFactory.createNews(null, feed3, new Date());
      fFactory.createGuid(news3, "http://www.anotherlink.com", false);
      news3.setLink(new URI(link));

      INews news4 = fFactory.createNews(null, feed4, new Date());
      fFactory.createGuid(news4, link, false);
      news4.setLink(new URI("www.anotherlink2.com"));

      /* Create one more news for each feed */
      fFactory.createNews(null, feed1, new Date());
      fFactory.createNews(null, feed2, new Date());
      fFactory.createNews(null, feed3, new Date());
      fFactory.createNews(null, feed4, new Date());

      DynamicDAO.save(feed1);
      DynamicDAO.save(feed2);
      DynamicDAO.save(feed3);
      DynamicDAO.save(feed4);

      final List<INews> expectedUpdatedNews = Arrays.asList(news1, news2, news3);
      final boolean[] newsUpdatedCalled = new boolean[1];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          newsUpdatedCalled[0] = true;
          assertEquals(3, events.size());
          for (NewsEvent event : events) {
            assertTrue("Entity should not be updated: " + event.getEntity(), expectedUpdatedNews.contains(event.getEntity()));
          }
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(Collections.singleton(news2), INews.State.NEW, true, true);
      assertEquals(true, newsUpdatedCalled[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSetNewsStateOnPlanet() throws Exception {
    NewsListener newsListener = null;
    try {
      NewsCounter newsCounter = DynamicDAO.getDAO(INewsCounterDAO.class).load();

      IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com"));
      IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));

      INews news1 = fFactory.createNews(null, feed1, new Date());
      news1.setLink(new URI("www.link.com"));

      INews news2 = fFactory.createNews(null, feed2, new Date());
      news2.setLink(new URI("www.link.com"));

      fFactory.createNews(null, feed1, new Date());
      fFactory.createNews(null, feed2, new Date());

      feed1 = DynamicDAO.save(feed1);
      feed2 = DynamicDAO.save(feed2);

      assertEquals(2, newsCounter.getUnreadCount(news1.getFeedLinkAsText()));
      assertEquals(2, newsCounter.getNewCount(news1.getFeedLinkAsText()));
      assertEquals(2, newsCounter.getUnreadCount(news2.getFeedLinkAsText()));
      assertEquals(2, newsCounter.getNewCount(news2.getFeedLinkAsText()));

      final long feed1ID = feed1.getId();
      final long feed2ID = feed2.getId();
      final long news1ID = feed1.getNews().get(0).getId();
      final long news2ID = feed2.getNews().get(0).getId();

      newsListener = new NewsListener() {
        public void entitiesAdded(Set<NewsEvent> events) {
          fail("Unexpected Event!");
        }

        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("Unexpected Event!");
        }

        public void entitiesUpdated(Set<NewsEvent> events) {
          assertEquals(2, events.size());
          for (NewsEvent event : events) {
            INews news = event.getEntity();
            IFeed parent = news.getFeedReference().resolve();

            if (news.getId() == news1ID)
              assertEquals(feed1ID, parent.getId().longValue());
            else if (news.getId() == news2ID)
              assertEquals(feed2ID, parent.getId().longValue());
            else
              fail("Unexpected Reference in Event!");
          }
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(Arrays.asList(new INews[] { new NewsReference(news1ID).resolve() }), INews.State.READ, true, false);

      assertEquals(1, newsCounter.getUnreadCount(news1.getFeedLinkAsText()));
      assertEquals(1, newsCounter.getNewCount(news1.getFeedLinkAsText()));
      assertEquals(1, newsCounter.getUnreadCount(news2.getFeedLinkAsText()));
      assertEquals(1, newsCounter.getNewCount(news2.getFeedLinkAsText()));
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }
}
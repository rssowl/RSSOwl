/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.editors.feed.NewsFilter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * Tests for the {@link FolderNewsMark}.
 *
 * @author bpasero
 */
public class FolderNewsMarkTest {
  private IModelFactory fFactory = Owl.getModelFactory();

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSimpleFolderNewsMark() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");
    childFolder.setProperty("foo", "bar");

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setState(INews.State.NEW);
    news = fFactory.createNews(null, feed, new Date());
    news.setState(INews.State.READ);
    DynamicDAO.save(feed);

    IBookMark bookMark = fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");
    folder = DynamicDAO.save(folder);

    FolderNewsMark mark = new FolderNewsMark(childFolder);
    assertEquals(childFolder.getId(), mark.getId());
    assertEquals(childFolder, mark.getFolder());
    assertEquals("bar", mark.getProperty("foo"));
    assertTrue(Long.valueOf(mark.toReference().getId()).equals(childFolder.getId()));
    assertTrue(mark.contains(bookMark));

    waitForIndexer();
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);

    assertEquals(2, mark.getNews().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testComplexFolderNewsMark() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(feed);

    IBookMark bookMark1 = fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");

    IFeed otherFeed = fFactory.createFeed(null, new URI("otherfeed"));
    INews othernews1 = fFactory.createNews(null, otherFeed, new Date());
    othernews1.setState(INews.State.NEW);
    INews othernews2 = fFactory.createNews(null, otherFeed, new Date());
    othernews2.setState(INews.State.UNREAD);
    INews othernews3 = fFactory.createNews(null, otherFeed, new Date());
    othernews3.setState(INews.State.READ);
    DynamicDAO.save(otherFeed);

    IBookMark bookMark2 = fFactory.createBookMark(null, folder, new FeedLinkReference(otherFeed.getLink()), "Other Mark");

    INewsBin bin = fFactory.createNewsBin(null, childFolder, "bin");
    DynamicDAO.save(bin);
    INews copiedNews1 = fFactory.createNews(news1, bin);
    INews copiedNews2 = fFactory.createNews(news2, bin);
    INews copiedNews3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(copiedNews1);
    DynamicDAO.save(copiedNews2);
    DynamicDAO.save(copiedNews3);

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
    ISearchMark search = fFactory.createSearchMark(null, childFolder, "search");
    search.addSearchCondition(condition);

    folder = DynamicDAO.save(folder);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    FolderNewsMark mark = new FolderNewsMark(childFolder);
    assertTrue(mark.contains(bookMark1));
    assertFalse(mark.contains(bookMark2));
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);

    {
      List<INews> news = mark.getNews();
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
      assertTrue(news.contains(othernews1));
    }

    {
      List<INews> news = mark.getNews(INews.State.getVisible());
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
      assertTrue(news.contains(othernews1));
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW));
      assertTrue(news.contains(news1));
      assertTrue(news.contains(othernews1));
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(othernews1));
    }

    {
      assertEquals(7, mark.getNewsCount(INews.State.getVisible()));
      assertEquals(7, mark.getNewsCount(EnumSet.of(INews.State.NEW)));
      assertEquals(7, mark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)));
    }

    {
      List<NewsReference> news = mark.getNewsRefs();
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(INews.State.getVisible());
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW));
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      assertTrue(mark.containsNews(news1));
      assertTrue(mark.containsNews(news2));
      assertTrue(mark.containsNews(news3));
      assertTrue(mark.containsNews(copiedNews1));
      assertTrue(mark.containsNews(copiedNews2));
      assertTrue(mark.containsNews(copiedNews3));
      assertTrue(mark.containsNews(othernews1));
    }

    {
      assertTrue(mark.isRelatedTo(news1));
      assertTrue(mark.isRelatedTo(news2));
      assertTrue(mark.isRelatedTo(news3));
      assertTrue(mark.isRelatedTo(copiedNews1));
      assertTrue(mark.isRelatedTo(copiedNews2));
      assertTrue(mark.isRelatedTo(copiedNews3));
    }

    {
      assertTrue(mark.isRelatedTo(search));
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testComplexFolderNewsMarkAdd() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");

    DynamicDAO.save(folder);

    FolderNewsMark mark = new FolderNewsMark(childFolder);
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(feed);

    fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");

    IFeed otherFeed = fFactory.createFeed(null, new URI("otherfeed"));
    INews othernews1 = fFactory.createNews(null, otherFeed, new Date());
    othernews1.setState(INews.State.NEW);
    INews othernews2 = fFactory.createNews(null, otherFeed, new Date());
    othernews2.setState(INews.State.UNREAD);
    INews othernews3 = fFactory.createNews(null, otherFeed, new Date());
    othernews3.setState(INews.State.READ);
    DynamicDAO.save(otherFeed);

    fFactory.createBookMark(null, folder, new FeedLinkReference(otherFeed.getLink()), "Other Mark");

    INewsBin bin = fFactory.createNewsBin(null, childFolder, "bin");
    DynamicDAO.save(bin);
    INews copiedNews1 = fFactory.createNews(news1, bin);
    INews copiedNews2 = fFactory.createNews(news2, bin);
    INews copiedNews3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(copiedNews1);
    DynamicDAO.save(copiedNews2);
    DynamicDAO.save(copiedNews3);

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
    ISearchMark search = fFactory.createSearchMark(null, childFolder, "search");
    search.addSearchCondition(condition);

    folder = DynamicDAO.save(folder);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    mark.add(Collections.singleton(news1));
    mark.add(Collections.singleton(news2));
    mark.add(Collections.singleton(news3));
    mark.add(Collections.singleton(copiedNews1));
    mark.add(Collections.singleton(copiedNews2));
    mark.add(Collections.singleton(copiedNews3));
    mark.add(Collections.singleton(othernews1));

    {
      List<INews> news = mark.getNews();
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
      assertTrue(news.contains(othernews1));
    }

    {
      List<INews> news = mark.getNews(INews.State.getVisible());
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
      assertTrue(news.contains(othernews1));
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW));
      assertTrue(news.contains(news1));
      assertTrue(news.contains(othernews1));
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(othernews1));
    }

    {
      assertEquals(7, mark.getNewsCount(INews.State.getVisible()));
      assertEquals(7, mark.getNewsCount(EnumSet.of(INews.State.NEW)));
      assertEquals(7, mark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)));
    }

    {
      List<NewsReference> news = mark.getNewsRefs();
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(INews.State.getVisible());
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW));
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(othernews1.toReference()));
    }

    {
      assertTrue(mark.containsNews(news1));
      assertTrue(mark.containsNews(news2));
      assertTrue(mark.containsNews(news3));
      assertTrue(mark.containsNews(copiedNews1));
      assertTrue(mark.containsNews(copiedNews2));
      assertTrue(mark.containsNews(copiedNews3));
      assertTrue(mark.containsNews(othernews1));
    }

    {
      assertTrue(mark.isRelatedTo(news1));
      assertTrue(mark.isRelatedTo(news2));
      assertTrue(mark.isRelatedTo(news3));
      assertTrue(mark.isRelatedTo(copiedNews1));
      assertTrue(mark.isRelatedTo(copiedNews2));
      assertTrue(mark.isRelatedTo(copiedNews3));
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testComplexFolderNewsMarkRemove() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(feed);

    fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");

    IFeed otherFeed = fFactory.createFeed(null, new URI("otherfeed"));
    INews otherNews1 = fFactory.createNews(null, otherFeed, new Date());
    otherNews1.setState(INews.State.NEW);
    INews othernews2 = fFactory.createNews(null, otherFeed, new Date());
    othernews2.setState(INews.State.UNREAD);
    INews othernews3 = fFactory.createNews(null, otherFeed, new Date());
    othernews3.setState(INews.State.READ);
    DynamicDAO.save(otherFeed);

    fFactory.createBookMark(null, folder, new FeedLinkReference(otherFeed.getLink()), "Other Mark");

    INewsBin bin = fFactory.createNewsBin(null, childFolder, "bin");
    DynamicDAO.save(bin);
    INews copiedNews1 = fFactory.createNews(news1, bin);
    INews copiedNews2 = fFactory.createNews(news2, bin);
    INews copiedNews3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(copiedNews1);
    DynamicDAO.save(copiedNews2);
    DynamicDAO.save(copiedNews3);

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
    ISearchMark search = fFactory.createSearchMark(null, childFolder, "search");
    search.addSearchCondition(condition);

    folder = DynamicDAO.save(folder);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    FolderNewsMark mark = new FolderNewsMark(childFolder);
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);

    List<NewsEvent> events = new ArrayList<NewsEvent>();
    News oldNews = new News((News) news1, -1);
    oldNews.setId(news1.getId());
    NewsEvent event1 = new NewsEvent(oldNews, news1, true);
    news1.setState(INews.State.HIDDEN);
    News oldNews2 = new News((News) copiedNews1, -1);
    oldNews2.setId(copiedNews1.getId());
    NewsEvent event2 = new NewsEvent(oldNews2, copiedNews1, true);
    copiedNews1.setState(INews.State.HIDDEN);
    News oldNews3 = new News((News) otherNews1, -1);
    oldNews3.setId(otherNews1.getId());
    NewsEvent event3 = new NewsEvent(oldNews3, otherNews1, true);
    otherNews1.setState(INews.State.HIDDEN);
    events.add(event1);
    events.add(event2);
    events.add(event3);

    mark.remove(Arrays.asList(news1, copiedNews1, otherNews1));

    {
      List<INews> news = mark.getNews();
      assertEquals(4, news.size());
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
    }

    {
      List<INews> news = mark.getNews(INews.State.getVisible());
      assertEquals(4, news.size());
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW));
      assertEquals(4, news.size());
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertEquals(4, news.size());
      assertTrue(news.contains(news2));
      assertTrue(news.contains(copiedNews2));
    }

    {
      assertEquals(4, mark.getNewsCount(INews.State.getVisible()));
      assertEquals(4, mark.getNewsCount(EnumSet.of(INews.State.NEW)));
      assertEquals(4, mark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)));
    }

    {
      List<NewsReference> news = mark.getNewsRefs();
      assertEquals(4, news.size());
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(INews.State.getVisible());
      assertEquals(4, news.size());
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW));
      assertEquals(4, news.size());
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertEquals(4, news.size());
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
    }

    {
      assertFalse(mark.containsNews(news1));
      assertTrue(mark.containsNews(news2));
      assertTrue(mark.containsNews(news3));
      assertFalse(mark.containsNews(copiedNews1));
      assertTrue(mark.containsNews(copiedNews2));
      assertTrue(mark.containsNews(copiedNews3));
      assertFalse(mark.containsNews(otherNews1));
    }

    {
      assertTrue(mark.isRelatedTo(news1));
      assertTrue(mark.isRelatedTo(news2));
      assertTrue(mark.isRelatedTo(news3));
      assertTrue(mark.isRelatedTo(copiedNews1));
      assertTrue(mark.isRelatedTo(copiedNews2));
      assertTrue(mark.isRelatedTo(copiedNews3));
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testComplexFolderNewsMarkUpdate() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(feed);

    fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");

    IFeed otherFeed = fFactory.createFeed(null, new URI("otherfeed"));
    INews otherNews1 = fFactory.createNews(null, otherFeed, new Date());
    otherNews1.setState(INews.State.NEW);
    INews othernews2 = fFactory.createNews(null, otherFeed, new Date());
    othernews2.setState(INews.State.UNREAD);
    INews othernews3 = fFactory.createNews(null, otherFeed, new Date());
    othernews3.setState(INews.State.READ);
    DynamicDAO.save(otherFeed);

    fFactory.createBookMark(null, folder, new FeedLinkReference(otherFeed.getLink()), "Other Mark");

    INewsBin bin = fFactory.createNewsBin(null, childFolder, "bin");
    DynamicDAO.save(bin);
    INews copiedNews1 = fFactory.createNews(news1, bin);
    copiedNews1.setState(INews.State.READ);
    INews copiedNews2 = fFactory.createNews(news2, bin);
    INews copiedNews3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(copiedNews1);
    DynamicDAO.save(copiedNews2);
    DynamicDAO.save(copiedNews3);

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
    ISearchMark search = fFactory.createSearchMark(null, childFolder, "search");
    search.addSearchCondition(condition);

    folder = DynamicDAO.save(folder);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    FolderNewsMark mark = new FolderNewsMark(childFolder);
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);

    List<NewsEvent> events = new ArrayList<NewsEvent>();

    News oldNews = new News((News) news1, -1);
    oldNews.setId(news1.getId());
    NewsEvent event1 = new NewsEvent(oldNews, news1, true);
    news1.setState(INews.State.READ);

    News oldNews2 = new News((News) copiedNews1, -1);
    oldNews2.setId(copiedNews1.getId());
    NewsEvent event2 = new NewsEvent(oldNews2, copiedNews1, true);
    copiedNews1.setState(INews.State.NEW);

    News oldNews3 = new News((News) otherNews1, -1);
    oldNews3.setId(otherNews1.getId());
    NewsEvent event3 = new NewsEvent(oldNews3, otherNews1, true);
    otherNews1.setState(INews.State.UNREAD);

    events.add(event1);
    events.add(event2);
    events.add(event3);

    {
      List<INews> news = mark.getNews();
      assertEquals(7, news.size());
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
      assertTrue(news.contains(otherNews1));
    }

    {
      List<INews> news = mark.getNews(INews.State.getVisible());
      assertEquals(7, news.size());
      assertTrue(news.contains(news1));
      assertTrue(news.contains(news2));
      assertTrue(news.contains(news3));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(copiedNews3));
      assertTrue(news.contains(otherNews1));
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW));
      assertEquals(7, news.size());
      assertTrue(news.contains(copiedNews1));
    }

    {
      List<INews> news = mark.getNews(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertEquals(7, news.size());
      assertTrue(news.contains(news2));
      assertTrue(news.contains(copiedNews1));
      assertTrue(news.contains(copiedNews2));
      assertTrue(news.contains(otherNews1));
    }

    {
      assertEquals(7, mark.getNewsCount(INews.State.getVisible()));
      assertEquals(7, mark.getNewsCount(EnumSet.of(INews.State.NEW)));
      assertEquals(7, mark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)));
    }

    {
      List<NewsReference> news = mark.getNewsRefs();
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
      assertTrue(news.contains(otherNews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(INews.State.getVisible());
      assertTrue(news.contains(news1.toReference()));
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(news3.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(copiedNews3.toReference()));
      assertTrue(news.contains(otherNews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW));
      assertEquals(7, news.size());
      assertTrue(news.contains(copiedNews1.toReference()));
    }

    {
      List<NewsReference> news = mark.getNewsRefs(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
      assertEquals(7, news.size());
      assertTrue(news.contains(news2.toReference()));
      assertTrue(news.contains(copiedNews1.toReference()));
      assertTrue(news.contains(copiedNews2.toReference()));
      assertTrue(news.contains(otherNews1.toReference()));
    }

    {
      assertTrue(mark.containsNews(news1));
      assertTrue(mark.containsNews(news2));
      assertTrue(mark.containsNews(news3));
      assertTrue(mark.containsNews(copiedNews1));
      assertTrue(mark.containsNews(copiedNews2));
      assertTrue(mark.containsNews(copiedNews3));
      assertTrue(mark.containsNews(otherNews1));
    }

    {
      assertTrue(mark.isRelatedTo(news1));
      assertTrue(mark.isRelatedTo(news2));
      assertTrue(mark.isRelatedTo(news3));
      assertTrue(mark.isRelatedTo(copiedNews1));
      assertTrue(mark.isRelatedTo(copiedNews2));
      assertTrue(mark.isRelatedTo(copiedNews3));
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFolderNewsMarkResolve_SimpleSM() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    news1.setFlagged(true);
    ILabel label = fFactory.createLabel(null, "Foo");
    DynamicDAO.save(label);
    news1.addLabel(label);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(feed);

    fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");

    IFeed otherFeed = fFactory.createFeed(null, new URI("otherfeed"));
    INews otherNews1 = fFactory.createNews(null, otherFeed, new Date());
    otherNews1.setState(INews.State.NEW);
    INews othernews2 = fFactory.createNews(null, otherFeed, new Date());
    othernews2.setState(INews.State.UNREAD);
    INews othernews3 = fFactory.createNews(null, otherFeed, new Date());
    othernews3.setState(INews.State.READ);
    DynamicDAO.save(otherFeed);

    fFactory.createBookMark(null, folder, new FeedLinkReference(otherFeed.getLink()), "Other Mark");

    INewsBin bin = fFactory.createNewsBin(null, childFolder, "bin");
    DynamicDAO.save(bin);
    INews copiedNews1 = fFactory.createNews(news1, bin);
    copiedNews1.setState(INews.State.READ);
    INews copiedNews2 = fFactory.createNews(news2, bin);
    INews copiedNews3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(copiedNews1);
    DynamicDAO.save(copiedNews2);
    DynamicDAO.save(copiedNews3);

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
    ISearchMark search = fFactory.createSearchMark(null, childFolder, "search");
    search.setMatchAllConditions(true);
    search.addSearchCondition(condition);

    folder = DynamicDAO.save(folder);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    FolderNewsMark mark = new FolderNewsMark(childFolder);

    /* All */
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);
    assertEquals(7, mark.getNewsCount(INews.State.getVisible()));

    /* New */
    mark.resolve(NewsFilter.Type.SHOW_NEW, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));

    /* Unread */
    mark.resolve(NewsFilter.Type.SHOW_UNREAD, null);
    assertEquals(4, mark.getNewsCount(INews.State.getVisible()));

    /* Recent */
    mark.resolve(NewsFilter.Type.SHOW_RECENT, null);
    assertEquals(7, mark.getNewsCount(INews.State.getVisible()));

    /* Last 5 Days */
    mark.resolve(NewsFilter.Type.SHOW_LAST_5_DAYS, null);
    assertEquals(7, mark.getNewsCount(INews.State.getVisible()));

    /* Sticky */
    mark.resolve(NewsFilter.Type.SHOW_STICKY, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));

    /* Labeled */
    mark.resolve(NewsFilter.Type.SHOW_LABELED, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFolderNewsMarkResolve_Complex_AND_SM() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    news1.setFlagged(true);
    ILabel label = fFactory.createLabel(null, "Foo");
    DynamicDAO.save(label);
    news1.addLabel(label);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(feed);

    fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");

    IFeed otherFeed = fFactory.createFeed(null, new URI("otherfeed"));
    INews otherNews1 = fFactory.createNews(null, otherFeed, new Date());
    otherNews1.setState(INews.State.NEW);
    INews othernews2 = fFactory.createNews(null, otherFeed, new Date());
    othernews2.setState(INews.State.UNREAD);
    INews othernews3 = fFactory.createNews(null, otherFeed, new Date());
    othernews3.setState(INews.State.READ);
    DynamicDAO.save(otherFeed);

    fFactory.createBookMark(null, folder, new FeedLinkReference(otherFeed.getLink()), "Other Mark");

    INewsBin bin = fFactory.createNewsBin(null, childFolder, "bin");
    DynamicDAO.save(bin);
    INews copiedNews1 = fFactory.createNews(news1, bin);
    copiedNews1.setState(INews.State.READ);
    INews copiedNews2 = fFactory.createNews(news2, bin);
    INews copiedNews3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(copiedNews1);
    DynamicDAO.save(copiedNews2);
    DynamicDAO.save(copiedNews3);

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    ISearchCondition condition1 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

    ISearchField ageField = fFactory.createSearchField(INews.AGE_IN_DAYS, INews.class.getName());
    ISearchCondition condition2 = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, 2);

    ISearchField attachmentField = fFactory.createSearchField(INews.HAS_ATTACHMENTS, INews.class.getName());
    ISearchCondition condition3 = fFactory.createSearchCondition(attachmentField, SearchSpecifier.IS, false);

    ISearchMark search = fFactory.createSearchMark(null, childFolder, "search");
    search.setMatchAllConditions(true);
    search.addSearchCondition(condition1);
    search.addSearchCondition(condition2);
    search.addSearchCondition(condition3);

    folder = DynamicDAO.save(folder);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    FolderNewsMark mark = new FolderNewsMark(childFolder);

    /* All */
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);
    assertEquals(7, mark.getNewsCount(INews.State.getVisible()));

    /* New */
    mark.resolve(NewsFilter.Type.SHOW_NEW, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));

    /* Unread */
    mark.resolve(NewsFilter.Type.SHOW_UNREAD, null);
    assertEquals(4, mark.getNewsCount(INews.State.getVisible()));

    /* Recent */
    mark.resolve(NewsFilter.Type.SHOW_RECENT, null);
    assertEquals(7, mark.getNewsCount(INews.State.getVisible()));

    /* Last 5 Days */
    mark.resolve(NewsFilter.Type.SHOW_LAST_5_DAYS, null);
    assertEquals(7, mark.getNewsCount(INews.State.getVisible()));

    /* Sticky */
    mark.resolve(NewsFilter.Type.SHOW_STICKY, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));

    /* Labeled */
    mark.resolve(NewsFilter.Type.SHOW_LABELED, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFolderNewsMarkResolve_Complex_OR_SM() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    IFolder childFolder = fFactory.createFolder(null, folder, "Child");

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    news1.setFlagged(true);
    ILabel label = fFactory.createLabel(null, "Foo");
    DynamicDAO.save(label);
    news1.addLabel(label);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(feed);

    fFactory.createBookMark(null, childFolder, new FeedLinkReference(feed.getLink()), "Mark");

    IFeed otherFeed = fFactory.createFeed(null, new URI("otherfeed"));
    INews otherNews1 = fFactory.createNews(null, otherFeed, new Date());
    otherNews1.setState(INews.State.NEW);
    INews othernews2 = fFactory.createNews(null, otherFeed, new Date());
    othernews2.setState(INews.State.UNREAD);
    INews othernews3 = fFactory.createNews(null, otherFeed, new Date());
    othernews3.setState(INews.State.READ);
    DynamicDAO.save(otherFeed);

    fFactory.createBookMark(null, folder, new FeedLinkReference(otherFeed.getLink()), "Other Mark");

    INewsBin bin = fFactory.createNewsBin(null, childFolder, "bin");
    DynamicDAO.save(bin);
    INews copiedNews1 = fFactory.createNews(news1, bin);
    copiedNews1.setState(INews.State.READ);
    INews copiedNews2 = fFactory.createNews(news2, bin);
    INews copiedNews3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(copiedNews1);
    DynamicDAO.save(copiedNews2);
    DynamicDAO.save(copiedNews3);

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    ISearchCondition condition1 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

    ISearchField ageField = fFactory.createSearchField(INews.AGE_IN_DAYS, INews.class.getName());
    ISearchCondition condition2 = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, 2);

    ISearchField attachmentField = fFactory.createSearchField(INews.HAS_ATTACHMENTS, INews.class.getName());
    ISearchCondition condition3 = fFactory.createSearchCondition(attachmentField, SearchSpecifier.IS, false);

    ISearchMark search = fFactory.createSearchMark(null, childFolder, "search");
    search.setMatchAllConditions(false);
    search.addSearchCondition(condition1);
    search.addSearchCondition(condition2);
    search.addSearchCondition(condition3);

    folder = DynamicDAO.save(folder);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    FolderNewsMark mark = new FolderNewsMark(childFolder);

    /* All */
    mark.resolve(NewsFilter.Type.SHOW_ALL, null);
    assertEquals(9, mark.getNewsCount(INews.State.getVisible()));

    /* New */
    mark.resolve(NewsFilter.Type.SHOW_NEW, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));

    /* Unread */
    mark.resolve(NewsFilter.Type.SHOW_UNREAD, null);
    assertEquals(5, mark.getNewsCount(INews.State.getVisible()));

    /* Recent */
    mark.resolve(NewsFilter.Type.SHOW_RECENT, null);
    assertEquals(9, mark.getNewsCount(INews.State.getVisible()));

    /* Last 5 Days */
    mark.resolve(NewsFilter.Type.SHOW_LAST_5_DAYS, null);
    assertEquals(9, mark.getNewsCount(INews.State.getVisible()));

    /* Sticky */
    mark.resolve(NewsFilter.Type.SHOW_STICKY, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));

    /* Labeled */
    mark.resolve(NewsFilter.Type.SHOW_LABELED, null);
    assertEquals(2, mark.getNewsCount(INews.State.getVisible()));
  }

  /**
   * @throws InterruptedException
   */
  protected void waitForIndexer() throws InterruptedException {
    Thread.sleep(2000);
  }
}
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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.LazyList;
import org.rssowl.core.internal.persist.NewsBin;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IGuid;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.dao.ICategoryDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.dao.IPersonDAO;
import org.rssowl.core.persist.dao.ISearchMarkDAO;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.ReparentInfo;
import org.rssowl.ui.internal.Controller;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This TestCase is for testing the Model Plugin (4 of 4).
 *
 * @author bpasero
 */
public class ModelTest4 extends LargeBlockSizeTest {
  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
  }

  /**
   * @throws InterruptedException
   */
  private void waitForIndexer() throws InterruptedException {
    Thread.sleep(500);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testBookMarkExists() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFeed feed = fFactory.createFeed(null, new URI("feed1"));
    DynamicDAO.save(feed);

    IBookMark mark1 = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Mark 1");
    IBookMark mark2 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("feed2")), "Mark 2");

    DynamicDAO.save(root);

    IBookMark mark3 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("feed3")), "Mark 3");

    assertTrue(DynamicDAO.getDAO(IBookMarkDAO.class).exists(mark1.getFeedLinkReference()));
    assertTrue(DynamicDAO.getDAO(IBookMarkDAO.class).exists(mark2.getFeedLinkReference()));
    assertFalse(DynamicDAO.getDAO(IBookMarkDAO.class).exists(mark3.getFeedLinkReference()));

    System.gc();

    assertTrue(DynamicDAO.getDAO(IBookMarkDAO.class).exists(mark1.getFeedLinkReference()));
    assertTrue(DynamicDAO.getDAO(IBookMarkDAO.class).exists(mark2.getFeedLinkReference()));
    assertFalse(DynamicDAO.getDAO(IBookMarkDAO.class).exists(mark3.getFeedLinkReference()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFolderSort() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");
    IBookMark mark2 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("mark1")), "B Mark 2");
    IBookMark mark1 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("mark2")), "A Mark 1");
    IBookMark mark3 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("mark3")), "C Mark 3");
    IFolder folder2 = fFactory.createFolder(null, root, "B Folder");
    IFolder folder1 = fFactory.createFolder(null, root, "A Folder");
    DynamicDAO.save(root);

    assertTrue(root.containsChild(mark1));

    root.sort();

    List<IFolderChild> children = root.getChildren();
    assertEquals(folder1, children.get(0));
    assertEquals(folder2, children.get(1));
    assertEquals(mark1, children.get(2));
    assertEquals(mark2, children.get(3));
    assertEquals(mark3, children.get(4));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFolderReorder() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");
    IBookMark mark1 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("mark2")), "A Mark 1");
    IBookMark mark2 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("mark1")), "B Mark 2");
    IBookMark mark3 = fFactory.createBookMark(null, root, new FeedLinkReference(new URI("mark3")), "C Mark 3");
    IFolder folder1 = fFactory.createFolder(null, root, "A Folder");
    IFolder folder2 = fFactory.createFolder(null, root, "B Folder");
    DynamicDAO.save(root);

    root.reorderChildren(Arrays.asList(new IFolderChild[] { mark1, mark2 }), folder1, true);

    List<IFolderChild> children = root.getChildren();
    assertEquals(mark3, children.get(0));
    assertEquals(folder1, children.get(1));
    assertEquals(mark1, children.get(2));
    assertEquals(mark2, children.get(3));
    assertEquals(folder2, children.get(4));

    root.reorderChildren(Arrays.asList(new IFolderChild[] { mark1, folder2 }), folder1, false);

    children = root.getChildren();
    assertEquals(mark3, children.get(0));
    assertEquals(mark1, children.get(1));
    assertEquals(folder2, children.get(2));
    assertEquals(folder1, children.get(3));
    assertEquals(mark2, children.get(4));
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore
  public void testNewsLazyList() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    INews news2 = fFactory.createNews(null, feed, new Date());
    INews news3 = fFactory.createNews(null, feed, new Date());

    DynamicDAO.save(feed);

    List<INews> list = feed.getNews();
    news1 = list.get(0);
    news2 = list.get(1);
    news3 = list.get(2);

    LazyList<INews> news = (LazyList<INews>) DynamicDAO.getDAO(INewsDAO.class).loadAll();
    assertTrue(news.iterator().hasNext());
    assertEquals(3, news.size());
    assertTrue(news.contains(news1));
    assertTrue(news.containsAll(Arrays.asList(new INews[] { news1, news2, news3 })));

    assertEquals(3, news.toArray().length);
    assertEquals(3, news.toArray(new INews[news.size()]).length);

    assertNotNull(news.get(0));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testBinRemoveNews() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    INewsBin bin = fFactory.createNewsBin(null, root, "Bin");

    DynamicDAO.save(bin);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    INews news2 = fFactory.createNews(null, feed, new Date());
    INews news3 = fFactory.createNews(null, feed, new Date());

    DynamicDAO.save(feed);

    news1 = fFactory.createNews(news1, bin);
    DynamicDAO.save(news1);
    news2 = fFactory.createNews(news2, bin);
    DynamicDAO.save(news2);
    news3 = fFactory.createNews(news3, bin);
    DynamicDAO.save(news3);

    DynamicDAO.save(bin);

    bin.removeNews(news1);
    ((NewsBin) bin).removeNewsRefs(Arrays.asList(new NewsReference[] { news2.toReference() }));

    List<INews> news = bin.getNews();
    assertEquals(1, news.size());
    assertEquals(news3, news.get(0));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchMark() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, true);

    ISearchMark search = fFactory.createSearchMark(null, root, "Search");
    search.addSearchCondition(condition);

    DynamicDAO.save(root);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setFlagged(true);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setFlagged(true);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setFlagged(true);

    DynamicDAO.save(feed);

    waitForIndexer();
    Controller.getDefault().getSavedSearchService().updateSavedSearches(true);

    assertTrue(search.containsNews(news1));
    assertTrue(search.containsNews(news2));
    assertTrue(search.containsNews(news3));

    List<INews> news = search.getNews();
    assertEquals(3, news.size());

    news = search.getNews(INews.State.getVisible());
    assertEquals(3, news.size());

    List<NewsReference> newsRefs = search.getNewsRefs();
    assertEquals(3, newsRefs.size());

    newsRefs = search.getNewsRefs(INews.State.getVisible());
    assertEquals(3, newsRefs.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCountAll() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");
    fFactory.createFolder(null, root, "Child");
    DynamicDAO.save(root);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setFlagged(true);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setFlagged(true);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setFlagged(true);

    DynamicDAO.save(feed);

    assertEquals(2, DynamicDAO.countAll(IFolder.class));
    assertEquals(1, DynamicDAO.countAll(IFeed.class));
    assertEquals(3, DynamicDAO.countAll(INews.class));

    DynamicDAO.delete(feed);
    DynamicDAO.delete(root);

    assertEquals(0, DynamicDAO.countAll(IFolder.class));
    assertEquals(0, DynamicDAO.countAll(IFeed.class));
    assertEquals(0, DynamicDAO.countAll(INews.class));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadAllExists() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");
    fFactory.createFolder(null, root, "Child");
    root = DynamicDAO.save(root);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setFlagged(true);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setFlagged(true);
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setFlagged(true);

    feed = DynamicDAO.save(feed);

    assertEquals(1, DynamicDAO.loadAll(IFeed.class).size());
    assertEquals(3, DynamicDAO.loadAll(INews.class).size());
    assertEquals(2, DynamicDAO.loadAll(IFolder.class).size());

    assertTrue(DynamicDAO.exists(IFolder.class, root.getId()));
    assertTrue(DynamicDAO.exists(IFeed.class, feed.getId()));

    DynamicDAO.delete(feed);
    DynamicDAO.delete(root);

    assertTrue(DynamicDAO.loadAll(IFeed.class).isEmpty());
    assertTrue(DynamicDAO.loadAll(INews.class).isEmpty());
    assertTrue(DynamicDAO.loadAll(IFolder.class).isEmpty());

    assertFalse(DynamicDAO.exists(IFolder.class, root.getId()));
    assertFalse(DynamicDAO.exists(IFeed.class, feed.getId()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadAllCategoryNames() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    feed = DynamicDAO.save(feed);

    ICategory cat1 = fFactory.createCategory(null, feed);
    cat1.setName("foo");

    ICategory cat2 = fFactory.createCategory(null, feed);
    cat2.setName("bar");

    DynamicDAO.save(feed);
    DynamicDAO.save(cat1);
    DynamicDAO.save(cat2);

    Set<String> cats = DynamicDAO.getDAO(ICategoryDAO.class).loadAllNames();
    assertEquals(2, cats.size());
    assertTrue(cats.contains(cat1.getName()));
    assertTrue(cats.contains(cat2.getName()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadAllPersonNames() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    feed = DynamicDAO.save(feed);

    IPerson pers1 = fFactory.createPerson(null, feed);
    pers1.setName("foo");

    IPerson pers2 = fFactory.createPerson(null, feed);
    pers2.setEmail(new URI("bar"));

    DynamicDAO.save(feed);
    DynamicDAO.save(pers1);
    DynamicDAO.save(pers2);

    Set<String> persons = DynamicDAO.getDAO(IPersonDAO.class).loadAllNames();
    assertEquals(2, persons.size());
    assertTrue(persons.contains(pers1.getName()));
    assertTrue(persons.contains(pers2.getEmail().toString()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLoadSearchFromCondition() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, true);

    ISearchMark search = fFactory.createSearchMark(null, root, "Search");
    search.addSearchCondition(condition);

    DynamicDAO.save(root);

    ISearchMark searchmark = DynamicDAO.getDAO(ISearchMarkDAO.class).load(condition);
    assertNotNull(searchmark);
    assertEquals("Search", searchmark.getName());
    assertEquals(root, searchmark.getParent());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testReparent() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFolder child1 = fFactory.createFolder(null, root, "Child 1");
    IFolder child2 = fFactory.createFolder(null, root, "Child 2");
    IFolder child3 = fFactory.createFolder(null, child2, "Child 3");

    root = DynamicDAO.save(root);

    DynamicDAO.getDAO(IFolderDAO.class).reparent(Collections.singletonList(ReparentInfo.create((IFolderChild) child1, child2, child3, true)));

    assertEquals(1, root.getChildren().size());
    IFolder child = (IFolder) root.getChildren().iterator().next();
    assertEquals(2, child.getChildren().size());
    assertEquals("Child 3", child.getFolders().get(0).getName());
    assertEquals("Child 1", child.getFolders().get(1).getName());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFeedLinkReferenceReferences() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    feed = DynamicDAO.save(feed);

    assertTrue(new FeedLinkReference(feed.getLink()).references(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchFilterReference() throws Exception {
    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    filter = DynamicDAO.save(filter);

    assertEquals(filter, filter.toReference().resolve());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchReference() throws Exception {
    ISearch search = fFactory.createSearch(null);
    search = DynamicDAO.save(search);

    assertEquals(search, search.toReference().resolve());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsIsEquivlanet_IgnoreTrailingSlash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("feed"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setLink(new URI("http://www.rssowl.org/news"));

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setLink(new URI("http://www.rssowl.org/news"));

    assertTrue(news1.isEquivalent(news2));
    assertTrue(news2.isEquivalent(news1));

    news1.setLink(new URI("http://www.rssowl.org/news/"));

    assertTrue(news1.isEquivalent(news2));
    assertTrue(news2.isEquivalent(news1));

    news2.setLink(new URI("http://www.rssowl.org/news/"));

    assertTrue(news1.isEquivalent(news2));
    assertTrue(news2.isEquivalent(news1));

    news1.setLink(new URI("http://www.rssowl.org/new"));

    assertFalse(news1.isEquivalent(news2));
    assertFalse(news2.isEquivalent(news1));

    IGuid guid1 = fFactory.createGuid(news1, "http://www.guid.org/", true);
    news1.setGuid(guid1);

    IGuid guid2 = fFactory.createGuid(news2, "http://www.guid.org/", true);
    news2.setGuid(guid2);

    assertTrue(news1.isEquivalent(news2));
    assertTrue(news2.isEquivalent(news1));

    guid1 = fFactory.createGuid(news1, "http://www.guid.org", true);
    news1.setGuid(guid1);

    assertTrue(news1.isEquivalent(news2));
    assertTrue(news2.isEquivalent(news1));

    guid1 = fFactory.createGuid(news1, "http://www.guid.or", true);
    news1.setGuid(guid1);

    assertFalse(news1.isEquivalent(news2));
    assertFalse(news2.isEquivalent(news1));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntityReadWriteFromAdded() throws Exception {
    final AtomicBoolean listenerHit= new AtomicBoolean(false);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    fFactory.createNews(null, feed, new Date());

    NewsListener listener = new NewsListener() {
      @Override
      public void entitiesAdded(Set<NewsEvent> events) {
        listenerHit.set(true);

        for (NewsEvent event : events) {
          event.getEntity().getState();
          event.getEntity().setState(INews.State.READ);
        }
      }

      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {}

      @Override
      public void entitiesDeleted(Set<NewsEvent> events) {}
    };
    DynamicDAO.addEntityListener(INews.class, listener);

    try {
      DynamicDAO.save(feed);
      assertTrue(listenerHit.get());
    } finally {
      DynamicDAO.removeEntityListener(INews.class, listener);
    }
  }

  /**
   * @throws Exception
   * @see IllegalStateException: Cannot acquire the write lock from the same
   * thread as the read lock (Bug 1279)
   */
  @Test
  @Ignore
  public void testEntityReadWriteFromUpdated() throws Exception {
    final AtomicBoolean listenerHit = new AtomicBoolean(false);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    final INews news = fFactory.createNews(null, feed, new Date());

    DynamicDAO.save(feed);

    NewsListener listener = new NewsListener() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        listenerHit.set(true);

        for (NewsEvent event : events) {
          event.getEntity().getState();
          event.getEntity().setState(INews.State.READ);
        }
      }

      @Override
      public void entitiesDeleted(Set<NewsEvent> events) {}

      @Override
      public void entitiesAdded(Set<NewsEvent> events) {}
    };
    DynamicDAO.addEntityListener(INews.class, listener);

    news.setFlagged(true);

    try {
      DynamicDAO.save(news);
      assertTrue(listenerHit.get());
    } finally {
      DynamicDAO.removeEntityListener(INews.class, listener);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntityReadWriteFromDeleted() throws Exception {
    final AtomicBoolean listenerHit= new AtomicBoolean(false);

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    final INews news = fFactory.createNews(null, feed, new Date());

    DynamicDAO.save(feed);

    NewsListener listener = new NewsListener() {
      @Override
      public void entitiesDeleted(Set<NewsEvent> events) {
        listenerHit.set(true);

        for (NewsEvent event : events) {
          event.getEntity().getState();
          event.getEntity().setState(INews.State.READ);
        }
      }

      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {}

      @Override
      public void entitiesAdded(Set<NewsEvent> events) {}
    };
    DynamicDAO.addEntityListener(INews.class, listener);

    try {
      DynamicDAO.delete(news);
      assertTrue(listenerHit.get());
    } finally {
      DynamicDAO.removeEntityListener(INews.class, listener);
    }
  }
}
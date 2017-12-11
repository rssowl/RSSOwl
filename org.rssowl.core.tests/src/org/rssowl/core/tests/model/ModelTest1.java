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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.SearchValueType;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.ISearchValueType;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.tests.TestUtils.NullProgressLongOperationMonitor;
import org.rssowl.ui.internal.util.ModelUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * This TestCase is for testing the Model Plugin (1 of 4).
 *
 * @author bpasero
 */
public class ModelTest1 extends LargeBlockSizeTest {
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
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    System.setProperty("rssowl.reindex", "false"); //Clear any set reindex marker
    DBManager.getDefault().getReIndexFile().delete();
    DBManager.getDefault().getDefragmentFile().delete();
    DBManager.getDefault().getCleanUpIndexFile().delete();
  }

  /**
   * Tests that no UPDATE event is issued for a type that has been deleted (and
   * as such there is also a REMOVE event). See bug #189 for more information.
   *
   * @throws Exception
   */
  @Test
  public void testNoUpdateEventWithRemoveEvent() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    fFactory.createFolder(null, folder, "Child folder #1");
    fFactory.createFolder(null, folder, "Child folder #2");
    fFactory.createFolder(null, folder, "Child folder #3");
    final IFolder savedFolder = DynamicDAO.save(folder);
    final IFolder savedChildFolder1 = savedFolder.getFolders().get(0);
    final IFolder savedChildFolder2 = savedFolder.getFolders().get(1);
    final IFolder savedChildFolder3 = savedFolder.getFolders().get(2);
    List<IFolder> foldersToRemove = new ArrayList<IFolder>();
    foldersToRemove.add(savedChildFolder1);
    foldersToRemove.add(savedChildFolder2);

    final boolean[] folderDeletedCalled = new boolean[1];
    final boolean[] folderUpdatedCalled = new boolean[1];
    FolderListener listener = new FolderAdapter() {
      @Override
      public void entitiesAdded(Set<FolderEvent> events) {
        fail("Unexpected folder added event");
      }

      @Override
      public void entitiesDeleted(Set<FolderEvent> events) {
        assertEquals(2, events.size());
        for (FolderEvent event : events) {
          IFolder folder = event.getEntity();
          if (!folder.equals(savedChildFolder1) && (!folder.equals(savedChildFolder2)))
            fail("No delete event expected for folder: " + folder.getId());

          folderDeletedCalled[0] = true;
        }
      }

      @Override
      public void entitiesUpdated(Set<FolderEvent> events) {
        assertEquals(2, events.size());
        for (FolderEvent event : events) {
          Long id = event.getEntity().getId();
          if (!id.equals(savedChildFolder3.getId()) && (!id.equals(savedFolder.getId())))
            fail("No update event expected for folder: " + id);

        }
        folderUpdatedCalled[0] = true;
      }
    };
    DynamicDAO.addEntityListener(IFolder.class, listener);
    try {
      DynamicDAO.deleteAll(foldersToRemove);
      assertEquals(true, folderDeletedCalled[0]);
      assertEquals(true, folderUpdatedCalled[0]);
    } finally {
      DynamicDAO.removeEntityListener(IFolder.class, listener);
    }
  }

  /**
   * Tests that updating a folder's property and saving it again has the desired
   * effect.
   */
  @Test
  public void testUpdateFolderProperties() {
    IFolder folder = fFactory.createFolder(null, null, "folder");
    String key = "key";
    String value = "value";
    folder.setProperty(key, value);
    DynamicDAO.save(folder);

    String newValue = "newValue";
    folder.setProperty(key, newValue);
    DynamicDAO.save(folder);

    folder = null;
    System.gc();
    folder = Owl.getPersistenceService().getDAOService().getFolderDAO().loadRoots().iterator().next();
    assertEquals(newValue, folder.getProperty(key));
  }

  /**
   * Tests that merging a news with categories doesn't throw any exception.
   *
   * @throws Exception
   */
  @Test
  public void testNewsWithCategoriesMerge() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setId(1L);
    news.setLink(new URI("http://news.com"));
    fFactory.createCategory(null, news);
    INews anotherNews = fFactory.createNews(null, feed, new Date());
    anotherNews.setLink(new URI("http://anothernews.com"));
    fFactory.createCategory(null, anotherNews);
    ICategory category = fFactory.createCategory(null, anotherNews);
    category.setName("name");
    news.merge(anotherNews);
  }

  /**
   * Tests that merging a feed with categories doesn't throw any exception.
   *
   * @throws Exception
   */
  @Test
  public void testFeedWithCategoriesMerge() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
    fFactory.createCategory(null, feed);
    IFeed anotherFeed = fFactory.createFeed(null, new URI("http://www.feed.com"));
    fFactory.createCategory(null, anotherFeed);
    ICategory category = fFactory.createCategory(null, anotherFeed);
    category.setName("name");
    feed.merge(anotherFeed);
  }

  /**
   * Tests that {@link IFeedDAO#save(IFeed)} sets the current and old state
   * correctly in the news when firing a newsUpdated event.
   *
   * @throws Exception
   */
  @Test
  public void testSaveFeedSetsCurrentAndOldStateInNews() throws Exception {
    IFeed feed = new Feed(new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news = fFactory.createNews(null, feed, new Date());
    news.setTitle("News Title #1");
    news.setLink(new URI("http://www.link.com"));
    news.setState(INews.State.UNREAD);

    feed = DynamicDAO.save(feed);

    final INews savedNews = feed.getNews().get(0);
    savedNews.setTitle("News Title Updated #1");

    NewsListener newsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        NewsEvent event = events.iterator().next();
        assertEquals(true, event.getEntity().equals(savedNews));
        assertEquals(State.UNREAD, event.getOldNews().getState());
        assertEquals(State.UNREAD, event.getEntity().getState());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    try {
      feed = DynamicDAO.save(feed);
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
    newsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        NewsEvent event = events.iterator().next();
        assertEquals(savedNews.getId(), event.getEntity().getId());
        assertEquals(State.UNREAD, event.getOldNews().getState());
        assertEquals(State.UPDATED, event.getEntity().getState());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    feed.getNews().get(0).setState(State.UPDATED);
    try {
      feed = DynamicDAO.save(feed);
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests that {@link INewsDAO#save(INews)} sets the current and old state
   * correctly when firing a newsUpdated event.
   *
   * @throws Exception
   */
  @Test
  public void testSaveNewsSetsCurrentAndOldState() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
    feed = DynamicDAO.save(feed);

    INews news = fFactory.createNews(null, feed, new Date());
    news.setTitle("News Title #1");
    news.setLink(new URI("http://www.link.com"));
    news.setState(INews.State.UNREAD);

    feed = DynamicDAO.save(feed);

    INews savedNews = feed.getNews().get(0);
    final Long savedNewsId = savedNews.getId();
    savedNews.setTitle("News Title Updated #1");

    NewsListener newsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        NewsEvent event = events.iterator().next();
        assertEquals(savedNewsId, event.getEntity().getId());
        assertEquals(State.UNREAD, event.getOldNews().getState());
        assertEquals(State.UNREAD, event.getEntity().getState());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    try {
      savedNews = DynamicDAO.save(savedNews);
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
    newsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        NewsEvent event = events.iterator().next();
        assertEquals(savedNewsId, event.getEntity().getId());
        assertEquals(State.UNREAD, event.getOldNews().getState());
        assertEquals(State.UPDATED, event.getEntity().getState());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    savedNews.setState(State.UPDATED);
    try {
      DynamicDAO.save(savedNews);
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests equals and hashCode for FeedLinkReference.
   *
   * @throws Exception
   */
  @Test
  public void testFeedLinkReferenceEqualsAndHashCode() throws Exception {
    String url1 = "http://url1.com";
    String url3 = "http://url3.com";

    FeedLinkReference feedRef1 = new FeedLinkReference(new URI(url1));
    FeedLinkReference feedRef2 = new FeedLinkReference(new URI(url1));

    assertEquals(feedRef1, feedRef2);
    assertEquals(feedRef1.hashCode(), feedRef2.hashCode());

    FeedLinkReference feedRef3 = new FeedLinkReference(new URI(url3));
    assertFalse(feedRef1.equals(feedRef3));
    assertFalse(feedRef1.hashCode() == feedRef3.hashCode());
  }

  /**
   * Tests {@link IFeed#getVisibleNews()} and {@link IFeed#getNewsByStates(Set)}
   *
   * @throws Exception
   */
  @Test
  public void testGetNewsByStatesAndGetVisibleNews() throws Exception {
    IFeed feed = createFeed("http://feed1.com");
    feed = DynamicDAO.save(feed);

    int newNewsCount = 4;
    List<INews> newNews = new ArrayList<INews>();
    for (int i = 0; i < newNewsCount; ++i) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("new News: " + i);
      news.setState(State.NEW);
      newNews.add(news);
    }

    int readNewsCount = 2;
    List<INews> readNews = new ArrayList<INews>();
    for (int i = 0; i < readNewsCount; ++i) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("read News: " + i);
      news.setState(State.READ);
      readNews.add(news);
    }

    int unreadNewsCount = 3;
    List<INews> unreadNews = new ArrayList<INews>();
    for (int i = 0; i < unreadNewsCount; ++i) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("unread News: " + i);
      news.setState(State.UNREAD);
      unreadNews.add(news);
    }

    int updatedNewsCount = 6;
    List<INews> updatedNews = new ArrayList<INews>();
    for (int i = 0; i < updatedNewsCount; ++i) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("updated News: " + i);
      news.setState(State.UPDATED);
      updatedNews.add(news);
    }

    int hiddenNewsCount = 8;
    List<INews> hiddenNews = new ArrayList<INews>();
    for (int i = 0; i < hiddenNewsCount; ++i) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("hidden News: " + i);
      news.setState(State.HIDDEN);
      hiddenNews.add(news);
    }

    int deletedNewsCount = 7;
    List<INews> deletedNews = new ArrayList<INews>();
    for (int i = 0; i < deletedNewsCount; ++i) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("deleted News: " + i);
      news.setState(State.DELETED);
      deletedNews.add(news);
    }

    assertEquals(newNewsCount, feed.getNewsByStates(EnumSet.of(State.NEW)).size());
    int counter = 0;
    for (INews news : feed.getNewsByStates(EnumSet.of(State.NEW))) {
      INews newsItem = newNews.get(counter++);
      assertEquals(newsItem.getTitle(), news.getTitle());
    }

    assertEquals(readNewsCount, feed.getNewsByStates(EnumSet.of(State.READ)).size());
    counter = 0;
    for (INews news : feed.getNewsByStates(EnumSet.of(State.READ))) {
      INews newsItem = readNews.get(counter++);
      assertEquals(newsItem.getTitle(), news.getTitle());
    }

    assertEquals(unreadNewsCount, feed.getNewsByStates(EnumSet.of(State.UNREAD)).size());
    counter = 0;
    for (INews news : feed.getNewsByStates(EnumSet.of(State.UNREAD))) {
      INews newsItem = unreadNews.get(counter++);
      assertEquals(newsItem.getTitle(), news.getTitle());
    }

    assertEquals(updatedNewsCount, feed.getNewsByStates(EnumSet.of(State.UPDATED)).size());
    counter = 0;
    for (INews news : feed.getNewsByStates(EnumSet.of(State.UPDATED))) {
      INews newsItem = updatedNews.get(counter++);
      assertEquals(newsItem.getTitle(), news.getTitle());
    }

    assertEquals(hiddenNewsCount, feed.getNewsByStates(EnumSet.of(State.HIDDEN)).size());
    counter = 0;
    for (INews news : feed.getNewsByStates(EnumSet.of(State.HIDDEN))) {
      INews newsItem = hiddenNews.get(counter++);
      assertEquals(newsItem.getTitle(), news.getTitle());
    }

    assertEquals(deletedNewsCount, feed.getNewsByStates(EnumSet.of(State.DELETED)).size());
    counter = 0;
    for (INews news : feed.getNewsByStates(EnumSet.of(State.DELETED))) {
      INews newsItem = deletedNews.get(counter++);
      assertEquals(newsItem.getTitle(), news.getTitle());
    }

    int visibleNewsCount = newNewsCount + readNewsCount + unreadNewsCount + updatedNewsCount;
    assertEquals(visibleNewsCount, feed.getVisibleNews().size());

    for (INews news : feed.getVisibleNews()) {
      boolean matchFound = false;
      for (INews newsItem : newNews) {
        if (news.getTitle().equals(newsItem.getTitle())) {
          matchFound = true;
          break;
        }
      }
      if (matchFound)
        continue;

      for (INews newsItem : readNews) {
        if (news.getTitle().equals(newsItem.getTitle())) {
          matchFound = true;
          break;
        }
      }
      if (matchFound)
        continue;

      for (INews newsItem : unreadNews) {
        if (news.getTitle().equals(newsItem.getTitle())) {
          matchFound = true;
          break;
        }
      }
      if (matchFound)
        continue;

      for (INews newsItem : updatedNews) {
        if (news.getTitle().equals(newsItem.getTitle())) {
          matchFound = true;
          break;
        }
      }
      if (matchFound)
        continue;
      fail("No match was found. A news that had the wrong state was returned");
    }

    for (INews news : feed.getNewsByStates(EnumSet.of(State.NEW, State.HIDDEN, State.DELETED))) {
      boolean matchFound = false;
      for (INews newsItem : newNews) {
        if (news.getTitle().equals(newsItem.getTitle())) {
          matchFound = true;
          break;
        }
      }
      if (matchFound)
        continue;

      for (INews newsItem : hiddenNews) {
        if (news.getTitle().equals(newsItem.getTitle())) {
          matchFound = true;
          break;
        }
      }
      if (matchFound)
        continue;

      for (INews newsItem : deletedNews) {
        if (news.getTitle().equals(newsItem.getTitle())) {
          matchFound = true;
          break;
        }
      }
      if (matchFound)
        continue;
      fail("No match was found. A news that had the wrong state was returned");
    }

  }

  /**
   * Tests that removing a INews doesn't also remove its parent feed.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveNewsWithoutFeed() throws Exception {
    IFeed feed = createFeed("http://www.rssowl.org");
    fFactory.createNews(null, feed, new Date());
    IFeed savedFeed = DynamicDAO.save(feed);
    DynamicDAO.delete(feed.getNews().get(0));
    savedFeed = DynamicDAO.load(IFeed.class, savedFeed.getId());
    assertNotNull(savedFeed);
  }

  /**
   * Tests that removing a ISearchCondition doesn't also remove its parent
   * ISearchMark.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveSearchConditionWithoutSearchMark() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    ISearchMark searchMark = fFactory.createSearchMark(null, folder, "Mark");
    ISearchField searchField = fFactory.createSearchField(0, INews.class.getName());
    fFactory.createSearchCondition(null, searchMark, searchField, SearchSpecifier.BEGINS_WITH, "Some value");
    IFolder savedFolder = DynamicDAO.save(folder);
    ISearchMark savedMark = (ISearchMark) savedFolder.getMarks().get(0);
    DynamicDAO.delete(savedMark.getSearchConditions().get(0));
    assertNotNull(DynamicDAO.load(ISearchMark.class, savedMark.getId()));
  }

  /**
   * Tests that removing a IPerson doesn't also remove its parent INews.
   *
   * @throws Exception
   */
  @Test
  public void testRemovePersonWithoutNews() throws Exception {
    IFeed feed = createFeed("http://www.rssowl.org");
    feed = DynamicDAO.save(feed);
    INews news = fFactory.createNews(null, feed, new Date());
    fFactory.createPerson(null, news);
    INews savedNews = DynamicDAO.save(news);
    DynamicDAO.delete((savedNews.getAuthor()));
    savedNews = DynamicDAO.load(INews.class, savedNews.getId());
    assertNotNull(savedNews);
  }

  /**
   * Tests that removing a IPerson doesn't also remove its parent feed.
   *
   * @throws Exception
   */
  @Test
  public void testRemovePersonWithoutFeed() throws Exception {
    IFeed feed = createFeed("http://www.rssowl.org");
    fFactory.createPerson(null, feed);
    IFeed savedFeed = DynamicDAO.save(feed);
    DynamicDAO.delete(savedFeed.getAuthor());
    savedFeed = DynamicDAO.load(IFeed.class, savedFeed.getId());
    assertNotNull(savedFeed);
  }

  /**
   * Tests that removing a ICategory doesn't also remove its parent news.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveCategoryWithoutNews() throws Exception {
    IFeed feed = createFeed("http://www.rssowl.org");
    feed = DynamicDAO.save(feed);
    INews news = fFactory.createNews(null, feed, new Date());
    fFactory.createCategory(null, news);
    INews savedNews = DynamicDAO.save(news);
    DynamicDAO.delete(news.getCategories().get(0));
    savedNews = DynamicDAO.load(INews.class, savedNews.getId());
    assertNotNull(savedNews);
  }

  /**
   * Tests that removing a ICategory doesn't also remove its parent feed.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveCategoryWithoutFeed() throws Exception {
    IFeed feed = createFeed("http://www.rssowl.org");
    fFactory.createCategory(null, feed);
    IFeed savedFeed = DynamicDAO.save(feed);
    DynamicDAO.delete(feed.getCategories().get(0));
    savedFeed = DynamicDAO.load(IFeed.class, savedFeed.getId());
    assertNotNull(savedFeed);
  }

  /**
   * Tests that removing a child IFolder doesn't also remove its parent IFolder.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveFolderWithoutParentFolder() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    fFactory.createFolder(null, folder, "Child folder");
    IFolder savedFolder = DynamicDAO.save(folder);
    IFolder savedChildFolder = savedFolder.getFolders().get(0);
    DynamicDAO.delete(savedChildFolder);
    assertNotNull(DynamicDAO.load(IFolder.class, savedFolder.getId()));
  }

  /**
   * Tests that removing a ISearchMark doesn't also remove its parent folder.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveSearchMarkWithoutFolder() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    fFactory.createSearchMark(null, folder, "Mark");
    IFolder savedFolder = DynamicDAO.save(folder);
    ISearchMark savedMark = (ISearchMark) savedFolder.getMarks().get(0);
    DynamicDAO.delete(savedMark);
    assertNotNull(DynamicDAO.load(IFolder.class, savedFolder.getId()));
  }

  /**
   * Tests that removing a ISearchMark causes an update event in the parent
   * folder.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveSearchUpdatesFolder() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    fFactory.createSearchMark(null, folder, "Mark");
    final IFolder savedFolder = DynamicDAO.save(folder);

    final boolean[] folderUpdatedCalled = new boolean[1];
    FolderListener folderListener = new FolderListener() {
      @Override
      public void entitiesAdded(Set<FolderEvent> events) {
        fail("folderAdded should not be called");
      }

      @Override
      public void entitiesDeleted(Set<FolderEvent> events) {
        fail("folderDeleted should not be called");
      }

      @Override
      public void entitiesUpdated(Set<FolderEvent> events) {
        assertEquals(1, events.size());
        assertEquals(true, events.iterator().next().getEntity().equals(savedFolder));
        folderUpdatedCalled[0] = true;
      }
    };
    DynamicDAO.addEntityListener(IFolder.class, folderListener);
    try {
      ISearchMark savedMark = (ISearchMark) savedFolder.getMarks().get(0);
      DynamicDAO.delete(savedMark);
      assertTrue("folderUpdated was not called", folderUpdatedCalled[0]);
    } finally {
      DynamicDAO.removeEntityListener(IFolder.class, folderListener);
    }
  }

  /**
   * Tests that removing a IBookMark doesn't also remove its parent folder.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveBookMarkWithoutFolder() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    IFeed feed = createFeed("http://www.someurl.com");
    feed = DynamicDAO.save(feed);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark");
    IFolder savedFolder = DynamicDAO.save(folder);
    IBookMark savedMark = (IBookMark) savedFolder.getMarks().get(0);
    DynamicDAO.delete(savedMark);
    assertNotNull(DynamicDAO.load(IFolder.class, savedFolder.getId()));
  }

  /**
   * Tests that removing a IAttachment doesn't also remove its parent news.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveAttachmentWithoutNews() throws Exception {
    IFeed feed = createFeed("http://www.rssowl.org");
    feed = DynamicDAO.save(feed);
    INews news = fFactory.createNews(null, feed, new Date());
    fFactory.createAttachment(null, news);
    INews savedNews = DynamicDAO.save(feed).getNews().get(0);
    DynamicDAO.delete(news.getAttachments().get(0));
    savedNews = DynamicDAO.load(INews.class, savedNews.getId());
    assertNotNull(savedNews);
  }

  /**
   * Test removing a IBookmark when it's the only parent of a IFeed. It should
   * cascade in that case. And test removing it when there are two IBookMarks
   * that reference the same IFeed. It should not cascade in that case.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveBookMarkAndFeed() throws Exception {
    {
      IFolder folder = fFactory.createFolder(null, null, "Folder");
      IFeed feed = createFeed("http://www.someurl.com");
      feed = DynamicDAO.save(feed);
      fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark");
      IFolder savedFolder = DynamicDAO.save(folder);
      IBookMark savedMark = (IBookMark) savedFolder.getMarks().get(0);
      IFeed savedFeed = savedMark.getFeedLinkReference().resolve();
      DynamicDAO.delete(savedMark);
      assertNull("Feed must also be deleted since no more bookmarks reference it", DynamicDAO.load(IFeed.class, savedFeed.getId()));
    }
    {
      IFolder folder = fFactory.createFolder(null, null, "AnotherFolder");
      IFeed feed = createFeed("http://www.anotherurl.com");
      feed = DynamicDAO.save(feed);
      fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark1");
      fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark2");
      IFolder savedFolder = DynamicDAO.save(folder);
      IBookMark savedMark1 = (IBookMark) savedFolder.getMarks().get(0);
      IBookMark savedMark2 = (IBookMark) savedFolder.getMarks().get(1);
      if (savedMark1.getName().equals("Mark2")) {
        IBookMark tempMark = savedMark1;
        savedMark1 = savedMark2;
        savedMark2 = tempMark;
      }
      IFeed savedFeed = savedMark1.getFeedLinkReference().resolve();
      DynamicDAO.delete(savedMark1);
      assertNotNull("Feed must not be deleted since one bookmark references it", DynamicDAO.load(IFeed.class, savedFeed.getId()));
      DynamicDAO.delete(savedMark2);
      assertNull("Feed must also be deleted since no more bookmarks reference it", DynamicDAO.load(IFeed.class, savedFeed.getId()));
    }
  }

  /**
   * Test the equals method in model types.
   *
   * @throws Exception
   */
  @Test
  public void testEquals() throws Exception {

    /* IExtendableType */
    IPersistable type1 = fFactory.createLabel(null, "name");
    IPersistable type2 = fFactory.createLabel(null, "name");

    IPersistable type3 = fFactory.createLabel(Long.valueOf(1), "name");
    IPersistable type4 = fFactory.createLabel(Long.valueOf(1), "name");

    IPersistable type5 = fFactory.createLabel(Long.valueOf(1), "name");
    IPersistable type6 = fFactory.createLabel(Long.valueOf(2), "name");

    assertFalse(type1.equals(type2));
    assertTrue(type3.equals(type4));
    assertFalse(type5.equals(type6));

    /* ISearchField */
    ISearchField fieldLabelName1 = fFactory.createSearchField(ILabel.NAME, ILabel.class.getName());
    ISearchField fieldLabelName2 = fFactory.createSearchField(ILabel.NAME, ILabel.class.getName());
    ISearchField fieldLabelAllFields = fFactory.createSearchField(IEntity.ALL_FIELDS, ILabel.class.getName());
    ISearchField fieldNewsTitle = fFactory.createSearchField(INews.TITLE, INews.class.getName());

    assertTrue(fieldLabelName1.equals(fieldLabelName2));
    assertFalse(fieldLabelName1.equals(fieldLabelAllFields));
    assertFalse(fieldLabelName1.equals(fieldNewsTitle));

    /* ISearchValueType */
    SearchValueType valueTypeString1 = new SearchValueType(ISearchValueType.STRING);
    SearchValueType valueTypeString2 = new SearchValueType(ISearchValueType.STRING);
    SearchValueType valueTypeDate = new SearchValueType(ISearchValueType.DATE);

    SearchValueType valueTypeEnum1 = new SearchValueType(new ArrayList<String>(Arrays.asList(new String[] { "Foo", "Bar" })));
    SearchValueType valueTypeEnum2 = new SearchValueType(new ArrayList<String>(Arrays.asList(new String[] { "Foo", "Bar" })));
    SearchValueType valueTypeEnum3 = new SearchValueType(new ArrayList<String>(Arrays.asList(new String[] { "Foo" })));

    assertTrue(valueTypeString1.equals(valueTypeString2));
    assertFalse(valueTypeString1.equals(valueTypeDate));

    assertTrue(valueTypeEnum1.equals(valueTypeEnum2));
    assertFalse(valueTypeEnum1.equals(valueTypeEnum3));
  }

  /**
   * Test the hashCode method in model types.
   *
   * @throws Exception
   */
  @Test
  public void testHashCode() throws Exception {

    /* ExtendableType */
    IPersistable type1 = fFactory.createLabel(null, "name");
    IPersistable type2 = fFactory.createLabel(null, "name");

    IPersistable type3 = fFactory.createLabel(Long.valueOf(1), "name");
    IPersistable type4 = fFactory.createLabel(Long.valueOf(1), "name");

    IPersistable type5 = fFactory.createLabel(Long.valueOf(1), "name");
    IPersistable type6 = fFactory.createLabel(Long.valueOf(2), "name");

    assertFalse(type1.hashCode() == type2.hashCode());
    assertTrue(type3.hashCode() == type4.hashCode());
    assertFalse(type5.hashCode() == type6.hashCode());

    /* ISearchField */
    ISearchField fieldLabelName1 = fFactory.createSearchField(ILabel.NAME, ILabel.class.getName());
    ISearchField fieldLabelName2 = fFactory.createSearchField(ILabel.NAME, ILabel.class.getName());
    ISearchField fieldLabelAllFields = fFactory.createSearchField(IEntity.ALL_FIELDS, ILabel.class.getName());
    ISearchField fieldNewsTitle = fFactory.createSearchField(INews.TITLE, INews.class.getName());

    assertTrue(fieldLabelName1.hashCode() == fieldLabelName2.hashCode());
    assertFalse(fieldLabelName1.hashCode() == fieldLabelAllFields.hashCode());
    assertFalse(fieldLabelName1.hashCode() == fieldNewsTitle.hashCode());

    /* ISearchValueType */
    SearchValueType valueTypeString1 = new SearchValueType(ISearchValueType.STRING);
    SearchValueType valueTypeString2 = new SearchValueType(ISearchValueType.STRING);
    SearchValueType valueTypeDate = new SearchValueType(ISearchValueType.DATE);

    SearchValueType valueTypeEnum1 = new SearchValueType(new ArrayList<String>(Arrays.asList("Foo", "Bar")));
    SearchValueType valueTypeEnum2 = new SearchValueType(new ArrayList<String>(Arrays.asList("Foo", "Bar")));
    SearchValueType valueTypeEnum3 = new SearchValueType(new ArrayList<String>(Arrays.asList("Foo")));

    assertTrue(valueTypeString1.hashCode() == valueTypeString2.hashCode());
    assertFalse(valueTypeString1.hashCode() == valueTypeDate.hashCode());

    assertTrue(valueTypeEnum1.hashCode() == valueTypeEnum2.hashCode());
    assertFalse(valueTypeEnum1.hashCode() == valueTypeEnum3.hashCode());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveSMWithLocationToBookMark() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    DynamicDAO.save(feed);

    IFolderChild bookmark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");
    DynamicDAO.save(root);

    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(bookmark)));
    ISearchMark sm = fFactory.createSearchMark(null, root, "Search");
    sm.addSearchCondition(condition);

    DynamicDAO.save(root);

    Long bmId = bookmark.getId();

    root = null;
    feed = null;
    locationField = null;
    condition = null;
    sm = null;
    bookmark = null;

    Runtime.getRuntime().gc();

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    Collection<ISearchMark> sms = DynamicDAO.loadAll(ISearchMark.class);
    assertEquals(1, sms.size());

    sm = sms.iterator().next();
    assertEquals(1, sm.getSearchConditions().size());

    condition = sm.getSearchConditions().get(0);
    assertNotNull(condition.getValue());
    assertEquals(true, condition.getValue() instanceof Long[][]);

    Long[][] value = (Long[][]) condition.getValue();
    assertEquals(1, value[1].length);
    assertEquals(bmId, value[1][0]);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveSMWithLocationToBin() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFolderChild bin = fFactory.createNewsBin(null, root, "Bin");

    DynamicDAO.save(root);

    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(bin)));
    ISearchMark sm = fFactory.createSearchMark(null, root, "Search");
    sm.addSearchCondition(condition);

    DynamicDAO.save(root);

    Long binId = bin.getId();

    root = null;
    locationField = null;
    condition = null;
    sm = null;
    bin = null;

    Runtime.getRuntime().gc();

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    Collection<ISearchMark> sms = DynamicDAO.loadAll(ISearchMark.class);
    assertEquals(1, sms.size());

    sm = sms.iterator().next();
    assertEquals(1, sm.getSearchConditions().size());

    condition = sm.getSearchConditions().get(0);
    assertNotNull(condition.getValue());
    assertEquals(true, condition.getValue() instanceof Long[][]);

    Long[][] value = (Long[][]) condition.getValue();
    assertEquals(1, value[2].length);
    assertEquals(binId, value[2][0]);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveSMWithLocationToBookMarkAndBin() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    DynamicDAO.save(feed);

    IFolderChild bookmark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");
    IFolderChild bin = fFactory.createNewsBin(null, root, "Bin");

    DynamicDAO.save(root);

    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    ISearchCondition condition1 = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(bookmark)));
    ISearchCondition condition2 = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(bin)));

    ISearchMark sm = fFactory.createSearchMark(null, root, "Search");
    sm.addSearchCondition(condition1);
    sm.addSearchCondition(condition2);

    DynamicDAO.save(root);

    Long bmId = bookmark.getId();
    Long binId = bin.getId();

    root = null;
    locationField = null;
    condition1 = null;
    condition2 = null;
    sm = null;
    bookmark = null;
    bin = null;

    Runtime.getRuntime().gc();

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    Collection<ISearchMark> sms = DynamicDAO.loadAll(ISearchMark.class);
    assertEquals(1, sms.size());

    sm = sms.iterator().next();
    assertEquals(2, sm.getSearchConditions().size());

    List<ISearchCondition> conditions = sm.getSearchConditions();
    boolean foundBm = false;
    boolean foundBin = false;

    for (ISearchCondition condition : conditions) {
      assertNotNull(condition.getValue());
      assertEquals(true, condition.getValue() instanceof Long[][]);

      Long[][] value = (Long[][]) condition.getValue();
      if (value[1].length == 1 && bmId.equals(value[1][0]))
        foundBm = true;
      else if (value[2].length == 1 && binId.equals(value[2][0]))
        foundBin = true;
    }

    assertTrue("Did not find Bin in Location Conditions", foundBin);
    assertTrue("Did not find Bookmark in Location Conditions", foundBm);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveSMWithLocationToTwoBookMarks() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    DynamicDAO.save(feed1);

    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.foo2.com"));
    DynamicDAO.save(feed2);

    IFolderChild bookmark1 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "Bookmark1");
    IFolderChild bookmark2 = fFactory.createBookMark(null, root, new FeedLinkReference(feed2.getLink()), "Bookmark2");

    DynamicDAO.save(root);

    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());

    List<IFolderChild> childs = new ArrayList<IFolderChild>(2);
    childs.add(bookmark1);
    childs.add(bookmark2);

    ISearchCondition condition = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(childs));

    ISearchMark sm = fFactory.createSearchMark(null, root, "Search");
    sm.addSearchCondition(condition);

    DynamicDAO.save(root);

    Long bmId1 = bookmark1.getId();
    Long bmId2 = bookmark2.getId();

    root = null;
    locationField = null;
    condition = null;
    sm = null;
    bookmark1 = null;
    bookmark2 = null;

    Runtime.getRuntime().gc();

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    Collection<ISearchMark> sms = DynamicDAO.loadAll(ISearchMark.class);
    assertEquals(1, sms.size());

    sm = sms.iterator().next();
    assertEquals(1, sm.getSearchConditions().size());

    boolean foundBm1 = false;
    boolean foundBm2 = false;

    condition = sm.getSearchConditions().get(0);
    assertNotNull(condition.getValue());
    assertEquals(true, condition.getValue() instanceof Long[][]);

    Long[][] value = (Long[][]) condition.getValue();
    assertEquals(2, value[1].length);

    for (int i = 0; i < 2; i++) {
      if (bmId1.equals(value[1][i]))
        foundBm1 = true;
      else if (bmId2.equals(value[1][i]))
        foundBm2 = true;
    }

    assertTrue("Did not find Bookmark 1 in Location Conditions", foundBm1);
    assertTrue("Did not find Bookmark 2 in Location Conditions", foundBm2);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveSMWithLocationToFolderAndBin() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFolderChild subRoot = fFactory.createFolder(null, root, "Sub Root");

    IFolderChild bin = fFactory.createNewsBin(null, root, "Bin");

    DynamicDAO.save(root);

    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    ISearchCondition condition1 = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(subRoot)));
    ISearchCondition condition2 = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(bin)));

    ISearchMark sm = fFactory.createSearchMark(null, root, "Search");
    sm.addSearchCondition(condition1);
    sm.addSearchCondition(condition2);

    DynamicDAO.save(root);

    Long folderId = subRoot.getId();
    Long binId = bin.getId();

    root = null;
    locationField = null;
    condition1 = null;
    condition2 = null;
    sm = null;
    subRoot = null;
    bin = null;

    Runtime.getRuntime().gc();

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    Collection<ISearchMark> sms = DynamicDAO.loadAll(ISearchMark.class);
    assertEquals(1, sms.size());

    sm = sms.iterator().next();
    assertEquals(2, sm.getSearchConditions().size());

    List<ISearchCondition> conditions = sm.getSearchConditions();
    boolean foundFolder = false;
    boolean foundBin = false;

    for (ISearchCondition condition : conditions) {
      assertNotNull(condition.getValue());
      assertEquals(true, condition.getValue() instanceof Long[][]);

      Long[][] value = (Long[][]) condition.getValue();
      if (value[0].length == 1 && folderId.equals(value[0][0]))
        foundFolder = true;
      else if (value[2].length == 1 && binId.equals(value[2][0]))
        foundBin = true;
    }

    assertTrue("Did not find Bin in Location Conditions", foundBin);
    assertTrue("Did not find Folder in Location Conditions", foundFolder);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEmergencyStartup() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");

    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    DynamicDAO.save(feed);

    IFolderChild bookmark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");
    DynamicDAO.save(root);

    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(bookmark)));
    ISearchMark sm = fFactory.createSearchMark(null, root, "Search");
    sm.addSearchCondition(condition);

    DynamicDAO.save(root);

    Long bmId = bookmark.getId();

    root = null;
    feed = null;
    locationField = null;
    condition = null;
    sm = null;
    bookmark = null;

    Runtime.getRuntime().gc();

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), true, false);

    {
      Collection<ISearchMark> sms = DynamicDAO.loadAll(ISearchMark.class);
      assertEquals(1, sms.size());

      sm = sms.iterator().next();
      assertEquals(1, sm.getSearchConditions().size());

      condition = sm.getSearchConditions().get(0);
      assertNotNull(condition.getValue());
      assertEquals(true, condition.getValue() instanceof Long[][]);

      Long[][] value = (Long[][]) condition.getValue();
      assertEquals(1, value[1].length);
      assertEquals(bmId, value[1][0]);
    }

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    Collection<ISearchMark> sms = DynamicDAO.loadAll(ISearchMark.class);
    assertEquals(1, sms.size());

    sm = sms.iterator().next();
    assertEquals(1, sm.getSearchConditions().size());

    condition = sm.getSearchConditions().get(0);
    assertNotNull(condition.getValue());
    assertEquals(true, condition.getValue() instanceof Long[][]);

    Long[][] value = (Long[][]) condition.getValue();
    assertEquals(1, value[1].length);
    assertEquals(bmId, value[1][0]);
  }

  private IFeed createFeed(String url) throws URISyntaxException {
    return fFactory.createFeed(null, new URI(url));
  }
}
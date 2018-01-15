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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.Owl.StartLevel;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Category;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.Guid;
import org.rssowl.core.internal.persist.Image;
import org.rssowl.core.internal.persist.Label;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.NewsBin;
import org.rssowl.core.internal.persist.NewsContainer;
import org.rssowl.core.internal.persist.Person;
import org.rssowl.core.internal.persist.SearchCondition;
import org.rssowl.core.internal.persist.SearchMark;
import org.rssowl.core.internal.persist.Source;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.ISource;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IConditionalGetDAO;
import org.rssowl.core.persist.dao.IFeedDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.event.BookMarkAdapter;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.event.CategoryAdapter;
import org.rssowl.core.persist.event.CategoryEvent;
import org.rssowl.core.persist.event.CategoryListener;
import org.rssowl.core.persist.event.FeedAdapter;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.FeedListener;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.LabelAdapter;
import org.rssowl.core.persist.event.LabelEvent;
import org.rssowl.core.persist.event.LabelListener;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.PersonAdapter;
import org.rssowl.core.persist.event.PersonEvent;
import org.rssowl.core.persist.event.PersonListener;
import org.rssowl.core.persist.event.SearchMarkAdapter;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.persist.service.UniqueConstraintException;
import org.rssowl.core.tests.TestUtils;
import org.rssowl.core.util.Pair;

import com.db4o.ObjectContainer;
import com.db4o.query.Query;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ismael Juma (ismael@juma.me.uk)
 */
@SuppressWarnings("nls")
public class DBManagerTest extends LargeBlockSizeTest {

  private static final String NOT_IDENTICAL_MESSAGE = "Item in the database is not identical to initial item.";
  private IModelFactory fTypesFactory;
  private ObjectContainer fDb;
  private INewsDAO fNewsDAO;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fTypesFactory = Owl.getModelFactory();
    fDb = DBManager.getDefault().getObjectContainer();
    fNewsDAO = DynamicDAO.getDAO(INewsDAO.class);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsDaoLoadAllFromFeed() throws Exception {
    IFolder root = fTypesFactory.createFolder(null, null, "Root");
    INewsBin bin = fTypesFactory.createNewsBin(null, root, "Bin");
    DynamicDAO.save(root);

    IFeed parent = fTypesFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fTypesFactory.createNews(null, parent, new Date());
    DynamicDAO.save(parent);

    INews newsCopy = fTypesFactory.createNews(news, bin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(bin);

    Collection<INews> newsFromFeed = DynamicDAO.getDAO(INewsDAO.class).loadAll(new FeedLinkReference(parent.getLink()), INews.State.getVisible());
    assertEquals(1, newsFromFeed.size());
    assertEquals(news, newsFromFeed.iterator().next());
  }

  /**
   *
   */
  @Test
  public void testAddDuplicateNewsDeadlocks() {
    List<INews> newsList = new ArrayList<INews>();
    IFeed feed = createFeed();
    INews news = createNews(feed);
    newsList.add(news);
    newsList.add(news);
    DynamicDAO.saveAll(newsList);
  }

  /**
   * Tests that {@link INewsDAO#setState(Set, State, boolean)} works correctly.
   *
   * @throws Exception s
   */
  @Test
  public void testNewsDAODeleteByStates() throws Exception {
    String feed0Link = "http://www.feed0.com";
    IFeed feed = fTypesFactory.createFeed(null, new URI(feed0Link));
    INews news = fTypesFactory.createNews(null, feed, new Date());
    news.setState(INews.State.HIDDEN);
    String news0Guid = "news0";
    fTypesFactory.createGuid(news, news0Guid, true);
    news = fTypesFactory.createNews(null, feed, new Date());
    news.setState(INews.State.NEW);
    String news1Guid = "news1";
    fTypesFactory.createGuid(news, news1Guid, true);
    DynamicDAO.save(feed);

    String feed1Link = "http://www.feed1.com";
    feed = fTypesFactory.createFeed(null, new URI(feed1Link));
    news = fTypesFactory.createNews(null, feed, new Date());
    news.setState(INews.State.HIDDEN);
    String news2Guid = "news2";
    fTypesFactory.createGuid(news, news2Guid, true);
    news = fTypesFactory.createNews(null, feed, new Date());
    news.setState(INews.State.READ);
    String news3Guid = "news3";
    fTypesFactory.createGuid(news, news3Guid, true);
    DynamicDAO.save(feed);
    news = null;
    feed = null;
    System.gc();

    final Set<NewsEvent> newsEvents = new HashSet<NewsEvent>();
    NewsListener newsListener = new NewsListener() {
      @Override
      public void entitiesAdded(Set<NewsEvent> events) {
        fail("Only update events expected");
      }

      @Override
      public void entitiesDeleted(Set<NewsEvent> events) {
        fail("Only update events expected");
      }

      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        newsEvents.addAll(events);
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsListener);
    try {
      fNewsDAO.setState(EnumSet.of(INews.State.HIDDEN), INews.State.DELETED, false);
      assertEquals(2, newsEvents.size());
      for (NewsEvent newsEvent : newsEvents) {
        String guid = newsEvent.getEntity().getGuid().getValue();
        assertEquals(INews.State.DELETED, newsEvent.getEntity().getState());
        assertEquals(true, guid.equals(news0Guid) || guid.equals(news2Guid));
      }

      IFeedDAO feedDao = DynamicDAO.getDAO(IFeedDAO.class);
      feed = feedDao.load(new URI(feed0Link));
      assertEquals(2, feed.getNews().size());

      feed = feedDao.load(new URI(feed1Link));
      assertEquals(2, feed.getNews().size());
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests that saving a NewsBin with a INews that was never saved in DELETED
   * state causes the INews to be removed from the INewsBin.
   *
   * @throws Exception
   */
  @Test
  @Ignore("Not sure we need to support this case")
  public void testSaveNewsBinWithNeverSavedNewsInDeletedState() throws Exception {
    IFeed feed = fTypesFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fTypesFactory.createNews(null, feed, new Date());
    news.setState(INews.State.DELETED);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    assertEquals(0, newsBin.getNews().size());
    assertEquals(null, fNewsDAO.load(newsCopy.getId()));
  }

  /**
   * Test case where original news is copied with a state != NEW, the copied
   * news state is changed to NEW before saving and then it's saved. The news
   * bin should have the news in the right bucket after save is finished.
   *
   * @throws Exception
   */
  @Test
  public void testCopyReadNewsButSaveWithNewState() throws Exception {
    IFeed feed = fTypesFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fTypesFactory.createNews(null, feed, new Date());
    news.setState(INews.State.READ);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    newsCopy.setState(INews.State.NEW);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    assertEquals(1, newsBin.getNews(EnumSet.of(INews.State.NEW)).size());
  }

  /**
   *
   */
  @Test
  public void testSetStateWhereTwoEquivalentNewsAreChangedToInvisible() {
    IFeed feed0 = createFeed();
    INews news0 = fTypesFactory.createNews(null, feed0, new Date());
    URI link = createURI("http://rssowl.org");
    news0.setLink(link);
    IFeed feed1 = fTypesFactory.createFeed(null, createURI("http://www.afeedlink.com"));
    INews news1 = fTypesFactory.createNews(null, feed1, new Date());
    news1.setLink(link);
    DynamicDAO.save(feed0);
    DynamicDAO.save(feed1);

    DynamicDAO.getDAO(INewsDAO.class).setState(Arrays.asList(news0, news1), INews.State.HIDDEN, true, false);
    assertEquals(INews.State.HIDDEN, news0.getState());
    assertEquals(INews.State.HIDDEN, news1.getState());
  }

  /**
   * Tests that deleting a INews.isCopy() will cause either: <li>The original
   * feed not to be deleted if it is referenced by a BM or another
   * INews.isCopy()</li> <li>The original feed to be deleted otherwise.</li>
   */
  @Test
  public void testDeleteNewsIsCopy() {
    IFeed feed = createFeed();
    INews news = fTypesFactory.createNews(null, feed, new Date());
    fTypesFactory.createGuid(news, "http://www.link.com", true);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    IBookMark bookMark = fTypesFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    /* Feed referenced by bookMark and newsCopy */
    fNewsDAO.setState(Collections.singleton(newsCopy), INews.State.DELETED, false, false);
    assertEquals(feed, DynamicDAO.load(IFeed.class, feed.getId()));

    newsCopy = fTypesFactory.createNews(news, newsBin);
    fNewsDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    DynamicDAO.delete(bookMark);
    newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin2");
    DynamicDAO.save(newsBin);
    INews newsCopy2 = fTypesFactory.createNews(newsCopy, newsBin);
    DynamicDAO.save(newsCopy2);
    DynamicDAO.save(newsBin);

    /* Feed referenced by newsCopy and newsCopy2 */
    fNewsDAO.setState(Collections.singleton(newsCopy), INews.State.DELETED, false, false);
    assertEquals(feed, DynamicDAO.load(IFeed.class, feed.getId()));

    /* Feed referenced by newsCopy2 which is being deleted */
    fNewsDAO.setState(Collections.singleton(newsCopy2), INews.State.DELETED, false, false);
    assertEquals(null, DynamicDAO.load(IFeed.class, feed.getId()));
  }

  /**
   * Tests that changing the state of a INews#isCopy() does not update the
   * NewsCounter for its feed. Also tests that the parent INewsBin is updated.
   */
  @Test
  public void testNewsCopyStateChangeDoesNotUpdateNewsCounter() {
    IFeed feed = createFeed();
    INews news = fTypesFactory.createNews(null, feed, new Date());
    fTypesFactory.createGuid(news, "http://www.link.com", true);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    IBookMark bookMark = fTypesFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    assertEquals(1, bookMark.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(1, newsBin.getNewsCount(EnumSet.of(INews.State.NEW)));

    fNewsDAO.setState(Collections.singleton(newsCopy), INews.State.READ, false, false);

    assertEquals(1, bookMark.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(0, newsBin.getNewsCount(EnumSet.of(INews.State.NEW)));
  }

  /**
   * Tests that news copy is actually deleted and removed from INewsBin when
   * it's saved after changing state to DELETED. Also checks that INewsBin is
   * updated if the state of the INews contained in it is changed to HIDDEN.
   */
  @Test
  public void testNewsIsCopyActuallyDeletedOnDeleteStateChange() {
    IFeed feed = createFeed();
    INews news = fTypesFactory.createNews(null, feed, new Date());
    fTypesFactory.createGuid(news, "http://www.link.com", true);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    fTypesFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    fNewsDAO.setState(Collections.singleton(newsCopy), INews.State.HIDDEN, false, false);
    assertEquals(1, newsBin.getNewsRefs().size());
    assertEquals(1, newsBin.getNewsRefs(EnumSet.of(INews.State.HIDDEN)).size());
    assertEquals(0, newsBin.getNewsRefs(EnumSet.of(INews.State.NEW)).size());
    assertEquals(newsCopy, newsBin.getNews().get(0));

    fNewsDAO.setState(Collections.singleton(newsCopy), INews.State.DELETED, false, false);
    assertEquals(0, newsBin.getNewsRefs().size());
    assertNull(fNewsDAO.load(newsCopy.getId()));
  }

  /**
   * Tests that copying a sticky news from the original feed to a INewsBin
   * followed by deletion of the original IBookMark works correctly. In other
   * words, we want to verify that the original feed is not deleted with the
   * IBookMark.
   */
  @Test
  public void testCopyStickyNewsAndDeleteOriginalBookMark() {
    IFeed feed = createFeed();
    INews news = fTypesFactory.createNews(null, feed, new Date());
    news.setFlagged(true);
    fTypesFactory.createGuid(news, "http://www.link.com", true);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    IBookMark mark = fTypesFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    DynamicDAO.delete(mark);
    /*
     * Mark containing the feed was deleted, but feed is not deleted because
     * it's still referenced by a copied news. The feed is hence empty.
     */
    assertEquals(0, feed.getNews().size());
    assertEquals(1, newsBin.getNewsRefs().size());
    assertEquals(newsCopy, newsBin.getNews().get(0));
  }

  /**
   * Tests that deleting a IBookMark where the feed has news copies does not
   * delete the feed.
   */
  @Test
  public void testDeleteBookMarkWithNewsCopies() {
    IFeed feed = createFeed();
    INews news = fTypesFactory.createNews(null, feed, new Date());
    fTypesFactory.createGuid(news, "http://www.link.com", true);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    IBookMark bookMark = fTypesFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Mark");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "bin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    DynamicDAO.delete(bookMark);
    feed = DynamicDAO.load(IFeed.class, feed.getId());
    assertNotNull(feed);
    assertEquals(0, feed.getNews().size());
  }

  /**
   * Tests that querying on News#fParentId to determine if it is contained in a
   * INewsBin works.
   */
  @Test
  public void testQueryNewsOnIsCopy() {
    IFeed feed = createFeed();
    INews news = fTypesFactory.createNews(null, feed, new Date());
    fTypesFactory.createGuid(news, "http://www.link.com", true);
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    fTypesFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    INewsBin newsBin = fTypesFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, newsBin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(newsBin);

    Query query = fDb.query();
    query.constrain(News.class);
    query.descend("fParentId").constrain(0).not();
    List<?> newsCopies = query.execute();
    assertEquals(1, newsCopies.size());
    assertEquals(newsCopy, newsCopies.get(0));

    query = fDb.query();
    query.constrain(News.class);
    query.descend("fParentId").constrain(0);
    List<?> newsList = query.execute();
    assertEquals(1, newsList.size());
    assertEquals(news, newsList.get(0));

    query = fDb.query();
    query.constrain(News.class);
    query.descend("fGuidValue").constrain(news.getGuid().getValue());
    query.descend("fParentId").constrain(0).not();
    newsCopies = query.execute();
    assertEquals(1, newsCopies.size());
    assertEquals(newsCopy, newsCopies.get(0));

    query = fDb.query();
    query.constrain(News.class);
    query.descend("fGuidValue").constrain(news.getGuid().getValue());
    query.descend("fParentId").constrain(0);
    newsList = query.execute();
    assertEquals(1, newsList.size());
    assertEquals(news, newsList.get(0));
  }

  /**
   * Tests that deleting a search condition removes it and its search field from
   * the database.
   */
  @Test
  public void testDeleteSearchCondition() {
    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    ISearchMark searchMark = fTypesFactory.createSearchMark(null, folder, "Mark");
    DynamicDAO.save(folder);
    ISearchField searchField = fTypesFactory.createSearchField(0, "SomeEntity");
    ISearchCondition searchCondition = fTypesFactory.createSearchCondition(null, searchMark, searchField, SearchSpecifier.BEGINS_WITH, "value");
    DynamicDAO.save(searchMark);
    assertNotNull(DynamicDAO.load(ISearchCondition.class, searchCondition.getId()));
    long searchFieldId = fDb.ext().getID(searchCondition.getField());
    assertNotNull(fDb.ext().getByID(searchFieldId));
    DynamicDAO.delete(searchCondition);
    assertNull(DynamicDAO.load(ISearchCondition.class, searchCondition.getId()));
    assertNull(fDb.ext().getByID(searchFieldId));
    assertEquals(0, searchMark.getSearchConditions().size());
  }

  /**
   * Tests that deleting a search mark removes it from the database and cascades
   * it appropriately to its children.
   */
  @Test
  public void testDeleteSearchMark() {
    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    ISearchMark searchMark = fTypesFactory.createSearchMark(null, folder, "Mark");
    ISearchField searchField = fTypesFactory.createSearchField(0, "SomeEntity");
    ISearchCondition searchCondition = fTypesFactory.createSearchCondition(null, searchMark, searchField, SearchSpecifier.BEGINS_WITH, "value");
    DynamicDAO.save(folder);
    assertNotNull(DynamicDAO.load(ISearchMark.class, searchMark.getId()));
    assertNotNull(DynamicDAO.load(ISearchCondition.class, searchCondition.getId()));
    long searchFieldId = fDb.ext().getID(searchCondition.getField());
    assertNotNull(fDb.ext().getByID(searchFieldId));
    DynamicDAO.delete(searchMark);
    assertNull(DynamicDAO.load(ISearchMark.class, searchMark.getId()));
    assertNull(DynamicDAO.load(ISearchCondition.class, searchCondition.getId()));
    assertNull(fDb.ext().getByID(searchFieldId));
  }

  /**
   * Tests that deleting a news bin deletes the internal news container too.
   */
  @Test
  public void testDeleteNewsBinDeletesNewsContainer() {
    IFeed feed = createFeed();
    INews news = fTypesFactory.createNews(null, feed, new Date());
    DynamicDAO.save(feed);

    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    fTypesFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    NewsBin bin = (NewsBin) fTypesFactory.createNewsBin(null, folder, "News Bin");
    DynamicDAO.save(folder);

    INews newsCopy = fTypesFactory.createNews(news, bin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(bin);

    /*
     * Ensure that arrays are treated specially by db4o, don't need to delete
     * them manually
     */
    assertFalse(fDb.ext().isStored(bin.internalGetNewsContainer().internalGetNewsIds()));

    NewsContainer newsContainer = bin.internalGetNewsContainer();
    DynamicDAO.delete(bin);
    assertFalse(fDb.ext().isStored(newsContainer));
  }

  /**
   * Tests that deleting a news bin is not sending out folder-deleted events.
   */
  @Test
  public void testDeleteBinIsNotSendindFolderDeleteEvents() {
    IFolder folder = fTypesFactory.createFolder(null, null, "Folder");
    INewsBin bin = fTypesFactory.createNewsBin(null, folder, "News Bin");
    DynamicDAO.save(folder);

    assertNotNull(DynamicDAO.load(INewsBin.class, bin.getId()));

    final boolean[] bool = new boolean[] { false };

    FolderAdapter folderListener = new FolderAdapter() {
      @Override
      public void entitiesDeleted(Set<FolderEvent> events) {
        bool[0] = true;
      }
    };

    DynamicDAO.addEntityListener(IFolder.class, folderListener);

    DynamicDAO.delete(bin);
    assertNull(DynamicDAO.load(INewsBin.class, bin.getId()));

    DynamicDAO.removeEntityListener(IFolder.class, folderListener);

    assertFalse("Unexpected Folder Event", bool[0]);
  }

  private ICategory createFeedCategory() throws PersistenceException {
    IFeed feed = DynamicDAO.save(createFeed());
    ICategory category = fTypesFactory.createCategory(null, feed);
    category.setName("categoryName");
    category.setDomain("some/domain");
    category.setProperty("one_property", "value");
    return category;
  }

  private ICategory createNewsCategory() throws PersistenceException {
    IFeed feed = DynamicDAO.save(createFeed());
    INews news = DynamicDAO.save(createNews(feed));
    ICategory category = fTypesFactory.createCategory(null, news);
    category.setName("categoryName");
    category.setDomain("some/domain");
    category.setProperty("one_property", "value");
    return category;
  }

  private IBookMark createBookMark() throws PersistenceException {
    IFolder folder = DynamicDAO.save(createFolder());
    return createBookMark(folder);
  }

  private ISearchMark createSearchMark() throws PersistenceException {
    IFolder folder = DynamicDAO.save(createFolder());
    return createSearchMark(folder);
  }

  private IBookMark createBookMark(IFolder folder) throws PersistenceException {
    IFeed feed = createFeed();
    IFeed savedFeed = Owl.getPersistenceService().getDAOService().getFeedDAO().load(feed.getLink());
    if (savedFeed == null)
      savedFeed = DynamicDAO.save(feed);

    IBookMark bookMark = fTypesFactory.createBookMark(null, folder, new FeedLinkReference(savedFeed.getLink()), "Default bookmark");
    bookMark.setLastVisitDate(createDate());
    bookMark.setPopularity(50);
    bookMark.setErrorLoading(false);
    return bookMark;
  }

  private IFolder createFolder() {
    return createFolder(null);
  }

  private IFolder createFolder(IFolder parent) {
    IFolder folder = fTypesFactory.createFolder(null, parent, "SomeFolder");
    folder.setBlogrollLink(createURI("http://someuri.com"));
    folder.setProperty("somekey", "somevalue");
    return folder;
  }

  /**
   * Checks: - Addition of person - That ADD event is issued - Updating of
   * person - That UPDATE event is issued - Getting of person
   */
  @Test
  public void testAddUpdateAndGetPerson() {
    IFeed feed = createFeed();
    final Person initialJohn = (Person) createPersonJohn(feed);
    final Person[] updatedJohn = new Person[1];
    final boolean[] personAddedCalled = new boolean[] { false };
    final boolean[] personUpdatedCalled = new boolean[] { false };
    PersonListener personListener = null;
    try {
      personListener = new PersonAdapter() {
        @Override
        public void entitiesAdded(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            personAddedCalled[0] = true;
            Person dbPerson = (Person) event.getEntity();
            initialJohn.setId(dbPerson.getId());
            assertTrue(initialJohn.isIdentical(dbPerson));
          }
        }

        @Override
        public void entitiesUpdated(Set<PersonEvent> events) {
          PersonEvent event = events.iterator().next();
          personUpdatedCalled[0] = true;
          Person dbPerson = (Person) event.getEntity();
          assertTrue(updatedJohn[0].isIdentical(dbPerson));
        }
      };
      DynamicDAO.addEntityListener(IPerson.class, personListener);
      long savedJohnId = DynamicDAO.save(initialJohn).getId().longValue();
      System.gc();
      IPerson savedJohn = DynamicDAO.load(IPerson.class, savedJohnId);
      initialJohn.setId(savedJohn.getId());
      assertTrue(initialJohn.isIdentical(savedJohn));
      URI oldJohnEmail = savedJohn.getEmail();
      IPerson dan = createPersonDan(feed);
      dan.setEmail(oldJohnEmail);
      DynamicDAO.removeEntityListener(IPerson.class, personListener);
      DynamicDAO.save(dan);
      savedJohn.setEmail(createURI("anewemailaddress@gmail.com"));
      updatedJohn[0] = (Person) savedJohn;
      DynamicDAO.addEntityListener(IPerson.class, personListener);
      DynamicDAO.save(savedJohn);
      assertTrue(personAddedCalled[0]);
      assertTrue(personUpdatedCalled[0]);
      DynamicDAO.delete(updatedJohn[0]);

    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (personListener != null) {
        DynamicDAO.removeEntityListener(IPerson.class, personListener);
      }
    }

  }

  /**
   * Tests that {@link News#merge(INews)} copies the link property correctly.
   *
   * @throws Exception
   */
  @Test
  public void testNewsMergeLink() throws Exception {
    IFeed feed = DynamicDAO.save(createFeed());
    INews news = fTypesFactory.createNews(null, feed, new Date());
    news.setId(1L);
    news.setLink(createURI("http://www.uri.com"));

    INews anotherNews = fTypesFactory.createNews(null, feed, new Date());
    URI uri = createURI("http://www.anotheruri.com");
    anotherNews.setLink(uri);

    news.merge(anotherNews);

    assertEquals(uri, news.getLink());
  }

  /**
   * Tests INews#setLink.
   *
   * @throws Exception
   */
  @Test
  public void testNewsSetLink() throws Exception {
    IFeed feed = DynamicDAO.save(createFeed());
    INews news = fTypesFactory.createNews(null, feed, new Date());
    URI uri = createURI("http://uri.com");
    news.setLink(uri);
    assertEquals(uri, news.getLink());
    news.setLink(null);
    assertNull(news.getLink());
    news.setLink(uri);
    assertEquals(uri, news.getLink());
    URI anotherUri = createURI("http://anotheruri.com");
    news.setLink(anotherUri);

    assertEquals(anotherUri, news.getLink());
  }

  /**
   * Tests deleting a INews and checking that the parent feed doesn't have it
   * anymore.
   *
   * @throws PersistenceException
   */
  @Test
  public void testDeleteNews() throws PersistenceException {
    IFeed feed = createFeed();
    feed = DynamicDAO.save(feed);
    INews news = createNews(feed);
    news = DynamicDAO.save(news);
    Long newsId = news.getId();
    DynamicDAO.delete(new NewsReference(newsId).resolve());
    feed = DynamicDAO.load(IFeed.class, feed.getId());
    assertEquals(0, feed.getNews().size());
    assertNull(DynamicDAO.load(INews.class, newsId));
  }

  /**
   * Tests loading the root folders.
   */
  @Test
  public void testLoadRootFolders() {
    try {
      // Save root folders
      IFolder folder1 = createFolder();
      folder1 = DynamicDAO.save(folder1);

      IFolder folder2 = fTypesFactory.createFolder(null, null, "AnotherFolder");
      folder2 = DynamicDAO.save(folder2);

      // Save non-root folder
      IFolder nonRootFolder = createFolderWithParent();
      DynamicDAO.save(nonRootFolder);

      Collection<IFolder> rootFolderRefs = Owl.getPersistenceService().getDAOService().getFolderDAO().loadRoots();
      assertEquals(3, rootFolderRefs.size());

      for (IFolder rootFolder : rootFolderRefs) {
        assertTrue(rootFolder.equals(folder1) || rootFolder.equals(folder2) || rootFolder.equals(nonRootFolder.getParent()));
      }

    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Simply adds and deletes a IBookMark and fails if an exceptions is thrown.
   */
  @Test
  public void testAddAndDeleteBookMark() {
    try {
      IBookMark bookMark = createBookMark();
      bookMark = DynamicDAO.save(bookMark);
      DynamicDAO.delete(bookMark);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Simply adds and deletes a ICategory with a feed as parent and fails if an
   * exception is thrown.
   */
  @Test
  public void testAddAndDeleteFeedCategory() {
    try {
      ICategory category = createFeedCategory();
      ICategory savedCategory = DynamicDAO.save(category);
      DynamicDAO.delete(savedCategory);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Simply adds and deletes a ICategory with a news as parent and fails if an
   * exception is thrown.
   */
  @Test
  public void testAddAndDeleteNewsCategory() {
    try {
      ICategory category = createNewsCategory();
      ICategory savedCategory = DynamicDAO.save(category);
      DynamicDAO.delete(savedCategory);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Adds, retrieves and checks if the added object matches the retrieved one.
   */
  @Test
  public void testAddAndGetFeedCategory() {
    Category category = (Category) createFeedCategory();
    DynamicDAO.save(category);
    Collection<ICategory> categories = DynamicDAO.loadAll(ICategory.class);
    assertEquals(1, categories.size());
    category.setId(categories.iterator().next().getId());
    assertTrue(category.isIdentical(categories.iterator().next()));
    DynamicDAO.deleteAll(categories);
  }

  /**
   * Adds, retrieves and checks if the added object matches the retrieved one.
   */
  public void testAddAndGetNewsCategory() {
    Category category = (Category) createNewsCategory();
    DynamicDAO.save(category);
    Collection<ICategory> categories = DynamicDAO.loadAll(ICategory.class);
    assertEquals(1, categories.size());
    category.setId(categories.iterator().next().getId());
    assertTrue(category.isIdentical(categories.iterator().next()));
    DynamicDAO.deleteAll(categories);
  }

  private ILabel createLabel() {
    ILabel label = fTypesFactory.createLabel(null, "someLabel");
    label.setColor("200,100,009");
    return label;
  }

  /**
   * Checks: - Addition of label - That ADD event is issued - Updating of label
   * - That UPDATE event is issued - Getting of label
   */
  @Test
  public void testAddUpdateAndGetLabel() {
    final Label initialLabel = (Label) createLabel();
    final Label[] updatedLabel = new Label[1];
    final boolean[] labelAddedCalled = new boolean[] { false };
    final boolean[] labelUpdatedCalled = new boolean[] { false };
    LabelListener labelListener = null;
    try {
      labelListener = new LabelAdapter() {
        @Override
        public void entitiesAdded(Set<LabelEvent> events) {
          for (LabelEvent event : events) {
            labelAddedCalled[0] = true;
            Label dbLabel = (Label) event.getEntity();
            initialLabel.setId(dbLabel.getId());
            assertTrue(initialLabel.isIdentical(dbLabel));
          }
        }

        @Override
        public void entitiesUpdated(Set<LabelEvent> events) {
          LabelEvent event = events.iterator().next();
          labelUpdatedCalled[0] = true;
          Label dbLabel = (Label) event.getEntity();
          assertTrue(updatedLabel[0].isIdentical(dbLabel));
        }
      };
      DynamicDAO.addEntityListener(ILabel.class, labelListener);
      long savedLabelId = DynamicDAO.save(initialLabel).getId().longValue();
      System.gc();
      ILabel dbLabel = DynamicDAO.load(ILabel.class, savedLabelId);
      initialLabel.setId(dbLabel.getId());
      assertTrue(initialLabel.isIdentical(dbLabel));
      dbLabel.setColor("255,255,137");
      updatedLabel[0] = (Label) dbLabel;
      DynamicDAO.save(dbLabel);
      assertTrue(labelAddedCalled[0]);
      assertTrue(labelUpdatedCalled[0]);
      DynamicDAO.delete(updatedLabel[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (labelListener != null) {
        DynamicDAO.removeEntityListener(ILabel.class, labelListener);
      }
    }
  }

  private void doTestAddUpdateGetAndDeleteSearchFilter(boolean gc) {
    /* Add */
    {
      ISearch search = fTypesFactory.createSearch(null);
      search.setMatchAllConditions(true);

      ISearchCondition condition1 = fTypesFactory.createSearchCondition(fTypesFactory.createSearchField(INews.TITLE, INews.class.getName()), SearchSpecifier.IS, "Foo");
      search.addSearchCondition(condition1);
      ISearchCondition condition2 = fTypesFactory.createSearchCondition(fTypesFactory.createSearchField(INews.ATTACHMENTS_CONTENT, INews.class.getName()), SearchSpecifier.CONTAINS, "Bar");
      search.addSearchCondition(condition2);

      ISearchFilter filter = fTypesFactory.createSearchFilter(null, search, "Filter");
      filter.setEnabled(true);
      filter.setOrder(5);

      IFilterAction action = fTypesFactory.createFilterAction("org.rssowl.ActionId1");
      action.setData(100);
      filter.addAction(action);

      action = fTypesFactory.createFilterAction("org.rssowl.ActionId2");
      action.setData(new Long[] { 1l, 2l, 3l });
      filter.addAction(action);

      filter = DynamicDAO.save(filter);
      if (gc)
        System.gc();

      Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
      assertEquals(1, filters.size());
      ISearchFilter savedFilter = filters.iterator().next();
      assertEquals(filter.getName(), savedFilter.getName());
      assertEquals(filter.getOrder(), savedFilter.getOrder());
      assertEquals(filter.isEnabled(), savedFilter.isEnabled());

      List<IFilterAction> actions = savedFilter.getActions();
      assertEquals(2, actions.size());
      assertEquals("org.rssowl.ActionId1", actions.get(0).getActionId());
      assertEquals(100, actions.get(0).getData());
      assertEquals("org.rssowl.ActionId2", actions.get(1).getActionId());
      assertEquals(true, Arrays.equals((Object[]) actions.get(1).getData(), new Long[] { 1l, 2l, 3l }));

      ISearch savedSearch = savedFilter.getSearch();
      assertEquals(search.matchAllConditions(), savedSearch.matchAllConditions());
      List<ISearchCondition> savedConditions = savedSearch.getSearchConditions();
      assertEquals(2, savedConditions.size());

      assertEquals(true, ((SearchCondition) savedConditions.get(0)).isIdentical(condition1));
      assertEquals(true, ((SearchCondition) savedConditions.get(1)).isIdentical(condition2));
    }

    /* Update */
    {
      Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
      ISearchFilter savedFilter = filters.iterator().next();

      savedFilter.setName("Disabled Filter");
      savedFilter.setEnabled(false);
      savedFilter.setOrder(1);

      List<IFilterAction> actions = savedFilter.getActions();
      savedFilter.removeAction(actions.get(0));
      actions.get(1).setData(new Long[] { 3l, 2l, 1l });
      IFilterAction action = fTypesFactory.createFilterAction("org.rssowl.ActionId3");
      action.setData(200);
      savedFilter.addAction(action);

      ISearch savedSearch = savedFilter.getSearch();
      savedSearch.setMatchAllConditions(false);
      List<ISearchCondition> savedConditions = savedSearch.getSearchConditions();

      ISearchCondition condition1 = savedConditions.get(0);
      condition1.setSpecifier(SearchSpecifier.CONTAINS_NOT);
      savedSearch.removeSearchCondition(savedConditions.get(1));

      ISearchCondition condition2 = fTypesFactory.createSearchCondition(fTypesFactory.createSearchField(INews.LOCATION, INews.class.getName()), SearchSpecifier.IS, new Long[][] { { 2l } });
      savedSearch.addSearchCondition(condition2);

      DynamicDAO.save(savedFilter);
      if (gc)
        System.gc();

      filters = DynamicDAO.loadAll(ISearchFilter.class);
      assertEquals(1, filters.size());
      ISearchFilter loadedFilter = filters.iterator().next();
      assertEquals(savedFilter.getName(), loadedFilter.getName());
      assertEquals(savedFilter.getOrder(), loadedFilter.getOrder());
      assertEquals(savedFilter.isEnabled(), loadedFilter.isEnabled());

      List<IFilterAction> loadedActions = loadedFilter.getActions();
      assertEquals(2, loadedActions.size());
      assertEquals("org.rssowl.ActionId2", loadedActions.get(0).getActionId());
      assertEquals(true, Arrays.equals((Object[]) actions.get(1).getData(), new Long[] { 3l, 2l, 1l }));
      assertEquals("org.rssowl.ActionId3", loadedActions.get(1).getActionId());
      assertEquals(200, loadedActions.get(1).getData());

      ISearch loadedSearch = loadedFilter.getSearch();
      assertEquals(savedSearch.matchAllConditions(), loadedSearch.matchAllConditions());
      List<ISearchCondition> loadedConditions = loadedSearch.getSearchConditions();
      assertEquals(2, loadedConditions.size());

      assertEquals(true, ((SearchCondition) loadedConditions.get(0)).isIdentical(condition1));
      assertEquals(true, ((SearchCondition) loadedConditions.get(1)).isIdentical(condition2));
    }

    /* Delete */
    {
      Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
      assertEquals(1, filters.size());

      DynamicDAO.delete(filters.iterator().next());
      if (gc)
        System.gc();

      filters = DynamicDAO.loadAll(ISearchFilter.class);
      assertTrue(filters.isEmpty());
    }
  }

  /**
   * Test add, update, get and delete of ISearchFilter
   */
  @Test
  public void testAddUpdateGetAndDeleteSearchFilter() {
    doTestAddUpdateGetAndDeleteSearchFilter(true);
    doTestAddUpdateGetAndDeleteSearchFilter(false);
  }

  /**
   * Checks: - Addition of category - That ADD event is issued - Updating of
   * category - That UPDATE event is issued - Getting of category
   */
  @Test
  public void testAddUpdateAndGetCategory() {
    final Category[] initialCategory = new Category[1];
    try {
      initialCategory[0] = (Category) createNewsCategory();
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
    final Category[] updatedCategory = new Category[1];
    final boolean[] categoryAddedCalled = new boolean[] { false };
    final boolean[] categoryUpdatedCalled = new boolean[] { false };
    CategoryListener categoryListener = null;
    try {
      categoryListener = new CategoryAdapter() {
        @Override
        public void entitiesAdded(Set<CategoryEvent> events) {
          CategoryEvent event = events.iterator().next();
          try {
            categoryAddedCalled[0] = true;
            Category dbCategory = (Category) event.getEntity();
            initialCategory[0].setId(dbCategory.getId());
            assertTrue(initialCategory[0].isIdentical(dbCategory));
            dbCategory.setDomain("newDomain/newDomain");
            updatedCategory[0] = dbCategory;
            DynamicDAO.save(dbCategory);
          } catch (PersistenceException e) {
            fail(e.getMessage());
          }
        }

        @Override
        public void entitiesUpdated(Set<CategoryEvent> events) {
          CategoryEvent event = events.iterator().next();
          categoryUpdatedCalled[0] = true;
          Category dbCategory = (Category) event.getEntity();
          assertTrue(updatedCategory[0].isIdentical(dbCategory));
        }
      };
      DynamicDAO.addEntityListener(ICategory.class, categoryListener);
      DynamicDAO.save(initialCategory[0]);
      assertTrue(categoryAddedCalled[0]);
      assertTrue(categoryUpdatedCalled[0]);
      DynamicDAO.delete(updatedCategory[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (categoryListener != null) {
        DynamicDAO.removeEntityListener(ICategory.class, categoryListener);
      }
    }
  }

  /**
   * Tests adding, updating and getting a folder with no parent.
   */
  @Test
  public void testAddUpdateAndGetFolder() {
    final Folder initialFolder = (Folder) createFolder();
    final Folder[] updatedFolder = new Folder[1];
    final boolean[] folderAddedCalled = new boolean[] { false };
    final boolean[] folderUpdatedCalled = new boolean[] { false };
    FolderListener folderListener = null;
    try {
      folderListener = new FolderAdapter() {
        @Override
        public void entitiesAdded(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            try {
              folderAddedCalled[0] = true;
              Folder dbFolder = (Folder) event.getEntity();
              initialFolder.setId(dbFolder.getId());
              assertTrue(NOT_IDENTICAL_MESSAGE, initialFolder.isIdentical(dbFolder));
              dbFolder.setBlogrollLink(createURI("http://www.newuri.com"));
              dbFolder.setName("New name");
              updatedFolder[0] = dbFolder;
              DynamicDAO.save(dbFolder);
            } catch (PersistenceException e) {
              fail(e.getMessage());
            }
          }
        }

        @Override
        public void entitiesUpdated(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            folderUpdatedCalled[0] = true;
            Folder dbFolder = (Folder) event.getEntity();
            assertTrue(updatedFolder[0].isIdentical(dbFolder));
          }
        }
      };
      DynamicDAO.addEntityListener(IFolder.class, folderListener);
      DynamicDAO.save(initialFolder);
      assertTrue(folderAddedCalled[0]);
      assertTrue(folderUpdatedCalled[0]);
      DynamicDAO.delete(updatedFolder[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (folderListener != null) {
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      }
    }
  }

  private IFolder createFolderWithParent() throws PersistenceException {
    IFolder parentFolder = createFolder();
    parentFolder = DynamicDAO.save(parentFolder);
    IFolder folder = fTypesFactory.createFolder(null, parentFolder, "MainFolder");
    folder.setBlogrollLink(createURI("http://www.rssowl.com"));
    folder.setProperty("skey", "svalue");
    return folder;

  }

  /**
   * Tests adding, updating and getting a folder that has a parent.
   */
  @Test
  public void testAddUpdateAndGetFolderWithParent() {
    final Folder[] initialFolder = new Folder[1];
    final String[] folderName = new String[1];
    try {
      initialFolder[0] = (Folder) createFolderWithParent();
      folderName[0] = initialFolder[0].getName();
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
    final Folder[] updatedFolder = new Folder[1];
    final boolean[] folderAddedCalled = new boolean[] { false };
    final boolean[] folderUpdatedCalled = new boolean[] { false };
    FolderListener folderListener = null;
    try {
      folderListener = new FolderAdapter() {
        @Override
        public void entitiesAdded(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            try {
              folderAddedCalled[0] = true;
              Folder dbFolder = (Folder) event.getEntity();
              initialFolder[0].setId(dbFolder.getId());
              assertTrue(initialFolder[0].isIdentical(dbFolder));
              dbFolder.setBlogrollLink(createURI("http://www.newuri.com"));
              updatedFolder[0] = dbFolder;
              DynamicDAO.save(dbFolder);
            } catch (PersistenceException e) {
              fail(e.getMessage());
            }
          }
        }

        @Override
        public void entitiesUpdated(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            /* Ignore event from parent */
            if (!folderName[0].equals(event.getEntity().getName())) {
              return;
            }
            folderUpdatedCalled[0] = true;
            Folder dbFolder = (Folder) event.getEntity();
            assertTrue(updatedFolder[0].isIdentical(dbFolder));
          }
        }
      };
      DynamicDAO.addEntityListener(IFolder.class, folderListener);
      DynamicDAO.save(initialFolder[0]);
      assertTrue(folderAddedCalled[0]);
      assertTrue(folderUpdatedCalled[0]);
      DynamicDAO.delete(updatedFolder[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (folderListener != null) {
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      }
    }
  }

  /**
   * When updating a child folder, the root event should be on the folder where
   * save is called.
   */
  @Test
  public void testRootFolderWhenFolderChildIsUpdated() {
    final Folder[] initialFolder = new Folder[1];
    final String[] folderName = new String[1];
    try {
      initialFolder[0] = (Folder) createFolderWithParent();
      folderName[0] = initialFolder[0].getName();
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
    FolderListener folderListener = null;
    try {
      folderListener = new FolderAdapter() {
        @Override
        public void entitiesAdded(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            if (folderName[0].equals(event.getEntity().getName())) {
              assertEquals(true, event.isRoot());
            } else {
              assertEquals(false, event.isRoot());
            }
          }
        }
      };
      DynamicDAO.addEntityListener(IFolder.class, folderListener);
      DynamicDAO.save(initialFolder[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (folderListener != null) {
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      }
    }
  }

  private IPerson createPersonJohn(IPersistable type) {
    IPerson person = fTypesFactory.createPerson(null, type);
    person.setName("John");
    person.setEmail(createURI("john@hotmail.com"));
    person.setUri(createURI("http://mysite.hotmail.com"));
    person.setProperty("property", "property_value");
    return person;
  }

  URI createURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      // should not happen;
      return null;
    }
  }

  private IPerson createPersonMary(IPersistable type) {
    IPerson person = fTypesFactory.createPerson(null, type);
    person.setName("Mary");
    person.setEmail(createURI("mary@hotmail.com"));
    person.setUri(createURI("http://mary.hotmail.com"));
    person.setProperty("test", "property");
    return person;
  }

  @SuppressWarnings("unused")
  private IPerson createPersonDan(IPersistable type) {
    IPerson person = fTypesFactory.createPerson(null, type);
    person.setName("Dan");
    person.setEmail(createURI("dan@yahoo.com"));
    return person;
  }

  /**
   * Saves the same feed twice without merging. Should throw an
   * UniqueConstraintException.
   *
   * @throws PersistenceException
   */
  @Test(expected = UniqueConstraintException.class)
  public void testSaveFeedTwice() throws PersistenceException {
    DynamicDAO.save(createFeed());
    DynamicDAO.save(createFeed());
  }

  /**
   * Saves the same feed twice, but merge it before saving it a second time. No
   * exception should be thrown.
   */
  @Test
  public void testSaveFeedTwiceAfterMerging() {
    IFeed savedFeed = DynamicDAO.save(createFeed());
    savedFeed.merge(createFeed());
    DynamicDAO.save(savedFeed);
  }

  /**
   * Tests adding, updating and getting a bookmark.
   */
  @Test
  public void testAddUpdateAndGetBookMark() {
    BookMarkListener bookMarkListener = null;
    try {
      final BookMark initialBookMark = (BookMark) createBookMark();
      final BookMark[] updatedBookMark = new BookMark[1];
      final boolean[] bookMarkAddedCalled = new boolean[] { false };
      final boolean[] bookMarkUpdatedCalled = new boolean[] { false };
      bookMarkListener = new BookMarkAdapter() {
        @Override
        public void entitiesAdded(Set<BookMarkEvent> events) {
          BookMarkEvent event = events.iterator().next();
          try {
            bookMarkAddedCalled[0] = true;
            BookMark dbBookMark = (BookMark) event.getEntity();
            initialBookMark.setId(dbBookMark.getId());
            assertTrue(initialBookMark.isIdentical(dbBookMark));
            dbBookMark.setName("Another name");
            updatedBookMark[0] = dbBookMark;
            DynamicDAO.save(dbBookMark);
          } catch (PersistenceException e) {
            fail(e.getMessage());
          }
        }

        @Override
        public void entitiesUpdated(Set<BookMarkEvent> events) {
          BookMarkEvent event = events.iterator().next();
          bookMarkUpdatedCalled[0] = true;
          BookMark dbBookMark = (BookMark) event.getEntity();
          assertTrue(updatedBookMark[0].isIdentical(dbBookMark));
        }
      };
      DynamicDAO.addEntityListener(IBookMark.class, bookMarkListener);
      DynamicDAO.save(initialBookMark);
      assertTrue(bookMarkAddedCalled[0]);
      assertTrue(bookMarkUpdatedCalled[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }

    finally {
      if (bookMarkListener != null) {
        DynamicDAO.removeEntityListener(IBookMark.class, bookMarkListener);
      }
    }
  }

  /**
   * Tests adding, updating and getting a ISearchMark.
   */
  @Test
  public void testAddUpdateAndGetSearchMark() {
    SearchMarkListener searchMarkListener = null;
    try {
      final SearchMark initialSearchMark = (SearchMark) createSearchMark();
      final SearchMark[] updatedSearchMark = new SearchMark[1];
      final boolean[] searchMarkAddedCalled = new boolean[] { false };
      final boolean[] searchMarkUpdatedCalled = new boolean[] { false };
      searchMarkListener = new SearchMarkAdapter() {
        @Override
        public void entitiesAdded(Set<SearchMarkEvent> events) {
          SearchMarkEvent event = events.iterator().next();
          try {
            searchMarkAddedCalled[0] = true;
            SearchMark dbSearchMark = (SearchMark) event.getEntity();
            initialSearchMark.setId(dbSearchMark.getId());
            assertTrue(initialSearchMark.isIdentical(dbSearchMark));
            dbSearchMark.setName("Another name");
            updatedSearchMark[0] = dbSearchMark;
            DynamicDAO.save(dbSearchMark);
          } catch (PersistenceException e) {
            fail(e.getMessage());
          }
        }

        @Override
        public void entitiesUpdated(Set<SearchMarkEvent> events) {
          SearchMarkEvent event = events.iterator().next();
          searchMarkUpdatedCalled[0] = true;
          SearchMark dbSearchMark = (SearchMark) event.getEntity();
          assertTrue(updatedSearchMark[0].isIdentical(dbSearchMark));
        }
      };
      DynamicDAO.addEntityListener(ISearchMark.class, searchMarkListener);
      DynamicDAO.save(initialSearchMark);
      assertTrue(searchMarkAddedCalled[0]);
      assertTrue(searchMarkUpdatedCalled[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }

    finally {
      if (searchMarkListener != null) {
        DynamicDAO.removeEntityListener(ISearchMark.class, searchMarkListener);
      }
    }
  }

  private Date createDate() {
    return new Date();
  }

  private IImage createImage(IFeed feed) {
    IImage image = fTypesFactory.createImage(feed);
    image.setHomepage(createURI("http://www.rssowl.org/image.png"));
    return image;
  }

  /**
   * Tests that saving a news with an attachment stores the attachment
   * correctly.
   */
  @Test
  public void testAddAttachmentToNewsAfterGC() {
    NewsListener newsListener = null;
    try {
      IFeed feed = createFeed();
      INews news = createNews(feed);
      List<IAttachment> attachments = new ArrayList<IAttachment>(news.getAttachments());
      for (IAttachment attachment : attachments)
        news.removeAttachment(attachment);

      DynamicDAO.save(feed);
      NewsReference newsRef = new NewsReference(feed.getNews().get(0).getId());
      fTypesFactory.createAttachment(null, news);
      DynamicDAO.save(feed);
      feed = null;
      news = null;
      System.gc();
      assertEquals(1, newsRef.resolve().getAttachments().size());
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          assertEquals(1, events.size());
          int attachmentsSize = events.iterator().next().getOldNews().getAttachments().size();
          assertEquals(1, attachmentsSize);
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      DynamicDAO.save(newsRef.resolve());
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Tests adding, updating and getting a news.
   */
  @Test
  public void testAddUpdateAndGetNews() {
    final IFeed feed;
    try {
      feed = DynamicDAO.save(createFeed());
    } catch (PersistenceException e) {
      fail(e.getMessage());
      return;
    }
    final News initialNews = (News) createNews(feed);
    final Feed[] initialFeed = new Feed[1];
    final Person[] initialAuthor = new Person[1];
    final Source[] initialSource = new Source[1];
    final Guid[] initialGuid = new Guid[1];
    final News[] updatedNews = new News[1];
    final boolean[] NewsAddedCalled = new boolean[] { false };
    final boolean[] NewsUpdatedCalled = new boolean[] { false };
    NewsListener newsListener = null;
    try {
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          NewsEvent event = events.iterator().next();
          NewsAddedCalled[0] = true;
          initialFeed[0] = (Feed) initialNews.getFeedReference().resolve();
          initialAuthor[0] = (Person) initialNews.getAuthor();
          initialSource[0] = (Source) initialNews.getSource();
          initialGuid[0] = (Guid) initialNews.getGuid();
          final News dbNews = (News) event.getEntity();
          initialAuthor[0].setId(dbNews.getAuthor().getId());
          initialAuthor[0].isIdentical(dbNews.getAuthor());
          //FIXME Find a way to verify this without changing the id from the
          //event handler
//          initialNews.setId(dbNews.getId());
//          assertTrue(initialNews.isIdentical(dbNews));
        }

        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          NewsEvent event = events.iterator().next();
          NewsUpdatedCalled[0] = true;
          News dbNews = (News) event.getEntity();
          assertTrue(updatedNews[0].isIdentical(dbNews));
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      INews news = DynamicDAO.save(initialNews);
      news.setDescription("The description has been changed in the news");
      news.setState(State.UNREAD);
      updatedNews[0] = (News) news;
      DynamicDAO.save(news);
      assertTrue(NewsAddedCalled[0]);
      assertTrue(NewsUpdatedCalled[0]);
      DynamicDAO.delete(updatedNews[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (newsListener != null) {
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      }
    }
  }

  /**
   * Tests that {@link IFeedDAO#save(IFeed)} cascades the complex types of INews
   * appropriately.
   */
  @Test
  public void testSaveFeedNewsCascadeWithGC() {
    IFeed feed = createFeed();
    INews news = createNews(feed);
    DynamicDAO.save(feed);
    String authorName = news.getAuthor().getName();
    URI attachmentLink = news.getAttachments().get(0).getLink();
    String categoryName = news.getCategories().get(0).getName();
    URI sourceLink = news.getSource().getLink();
    DynamicDAO.save(news);
    NewsReference newsRef = new NewsReference(news.getId());
    feed = null;
    news = null;
    System.gc();
    news = newsRef.resolve();
    assertNewsEquals(categoryName, attachmentLink, authorName, sourceLink, news);
    authorName = authorName + " changed";
    attachmentLink = createURI(attachmentLink.toString() + "/new");
    categoryName = categoryName + " changed";
    sourceLink = createURI(sourceLink.toString() + "/new");
    updateNews(authorName, attachmentLink, categoryName, sourceLink, news);
    DynamicDAO.save(news.getFeedReference().resolve());
    news = null;
    System.gc();
    news = newsRef.resolve();
    assertNewsEquals(categoryName, attachmentLink, authorName, sourceLink, news);
  }

  /**
   * Tests that {@link INewsDAO#save(INews)} cascades the complex types
   * appropriately.
   */
  @Test
  public void testSaveNewsCascadeWithGC() {
    IFeed feed = createFeed();
    DynamicDAO.save(feed);
    INews news = createNews(feed);
    String authorName = news.getAuthor().getName();
    URI attachmentLink = news.getAttachments().get(0).getLink();
    String categoryName = news.getCategories().get(0).getName();
    URI sourceLink = news.getSource().getLink();
    DynamicDAO.save(news);
    NewsReference newsRef = new NewsReference(news.getId());
    feed = null;
    news = null;
    System.gc();
    news = newsRef.resolve();
    assertNewsEquals(categoryName, attachmentLink, authorName, sourceLink, news);
    authorName = authorName + " changed";
    attachmentLink = createURI(attachmentLink.toString() + "/new");
    categoryName = categoryName + " changed";
    sourceLink = createURI(sourceLink.toString() + "/new");
    updateNews(authorName, attachmentLink, categoryName, sourceLink, news);
    DynamicDAO.save(news);
    news = null;
    System.gc();
    news = newsRef.resolve();
    assertNewsEquals(categoryName, attachmentLink, authorName, sourceLink, news);
  }

  /**
   * Tests that {@link DynamicDAO#saveAll(Collection)} cascades the complex
   * types appropriately.
   */
  @Test
  public void testSaveNewsListCascadeWithGC() {
    IFeed feed = createFeed();
    DynamicDAO.save(feed);
    INews news = createNews(feed);
    String authorName = news.getAuthor().getName();
    URI attachmentLink = news.getAttachments().get(0).getLink();
    String categoryName = news.getCategories().get(0).getName();
    URI sourceLink = news.getSource().getLink();
    DynamicDAO.saveAll(Collections.singletonList(news));
    NewsReference newsRef = new NewsReference(news.getId());
    feed = null;
    news = null;
    System.gc();
    news = newsRef.resolve();
    assertNewsEquals(categoryName, attachmentLink, authorName, sourceLink, news);
    authorName = authorName + " changed";
    attachmentLink = createURI(attachmentLink.toString() + "/new");
    categoryName = categoryName + " changed";
    sourceLink = createURI(sourceLink.toString() + "/new");
    updateNews(authorName, attachmentLink, categoryName, sourceLink, news);
    DynamicDAO.saveAll(Collections.singletonList(news));
    news = null;
    System.gc();
    news = newsRef.resolve();
    assertNewsEquals(categoryName, attachmentLink, authorName, sourceLink, news);
  }

  private void updateNews(String authorName, URI attachmentLink, String categoryName, URI sourceLink, INews news) {
    news.getAuthor().setName(authorName);
    news.getAttachments().get(0).setLink(attachmentLink);
    news.getCategories().get(0).setName(categoryName);
    news.getSource().setLink(sourceLink);
  }

  private void assertNewsEquals(String categoryName, URI attachmentLink, String authorName, URI sourceLink, INews news) {
    assertNotNull(news);
    assertEquals(1, news.getCategories().size());
    assertEquals(categoryName, news.getCategories().get(0).getName());
    assertEquals(1, news.getAttachments().size());
    assertEquals(attachmentLink, news.getAttachments().get(0).getLink());
    assertEquals(authorName, news.getAuthor().getName());
    assertEquals(sourceLink, news.getSource().getLink());
  }

  private INews createNews(IFeed feed) {
    INews news = fTypesFactory.createNews(null, feed, createDate());
    IAttachment attachment = fTypesFactory.createAttachment(null, news);
    attachment.setLink(createURI("http://attachmenturi.com"));
    ICategory category = fTypesFactory.createCategory(null, news);
    category.setName("Category name #1");
    news.setAuthor(createPersonMary(news));
    news.setBase(createURI("http://www.someuri.com"));
    news.setComments("One comment");
    news.setState(State.HIDDEN);
    news.setDescription("News description");
    fTypesFactory.createGuid(news, "someGUIDvalue", null);
    news.setLink(createURI("http://www.somelocation.com/feed.rss"));
    news.setModifiedDate(createDate());
    news.setProperty("property", "value");
    news.setPublishDate(createDate());
    ISource source = fTypesFactory.createSource(news);
    source.setLink(createURI("http://www.someuri.com"));
    news.setSource(source);
    news.setTitle("This is the news title");
    news.setRating(70);
    return news;
  }

  private Feed createFeed() {
    return createFeed("http://www.rssowl.org/feed.rss");
  }

  private Feed createFeed(String link) {
    Feed feed = (Feed) fTypesFactory.createFeed(null, createURI(link));
    feed.setTitle("feed title");
    feed.setDescription("feed description");
    feed.setHomepage(createURI("http://www.rssowl.org"));
    feed.setAuthor(createPersonJohn(feed));
    feed.setLanguage("English");
    feed.setCopyright("This feed is copyrighted");
    feed.setDocs(createURI("http://www.rssowl.org/documentation.html"));
    feed.setGenerator("Manual");
    feed.setImage(createImage(feed));
    feed.setPublishDate(createDate());
    feed.setLastBuildDate(createDate());
    feed.setLastModifiedDate(createDate());
    feed.setWebmaster("Webmaster");
    feed.setTTL(60);
    feed.setFormat("RSS");
    feed.setProperty("feedProperty", "randomValue");
    feed.setBase(createURI("http://www.baseuri.com/"));
    return feed;
  }

  /**
   * Saves feed and verifies that the feed still contains the news after being
   * GC'd and reloaded. See bug #261.
   *
   * @throws Exception
   */
  @Test
  public void testFeedRetainsLinkToNewsAfterSave() throws Exception {
    IFeed feed = createFeed();

    /* Need to save this without the news first */
    DynamicDAO.save(feed);

    createNews(feed);
    DynamicDAO.save(feed);

    long feedId = feed.getId();
    feed = null;
    System.gc();
    IFeed savedFeed = DynamicDAO.load(IFeed.class, feedId);
    assertEquals(1, savedFeed.getNews().size());
    DynamicDAO.delete(new FeedReference(feedId).resolve());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testDeleteFeed() throws Exception {
    IFeed feed = createFeed();
    createNews(feed);
    fTypesFactory.createNews(null, feed, new Date());
    DynamicDAO.save(feed);

    FeedReference feedRef = new FeedReference(feed.getId());
    System.gc();
    DynamicDAO.delete(feedRef.resolve());
    assertNull(DynamicDAO.load(IFeed.class, feedRef.getId()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testDeleteFeedDeletesConditionalGet() throws Exception {
    IFeed feed = createFeed();

    DynamicDAO.save(feed);

    IConditionalGet conditionalGet = fTypesFactory.createConditionalGet("2005/11/04", feed.getLink(), null);

    DynamicDAO.save(conditionalGet);

    DynamicDAO.delete(feed);
    assertNull(DynamicDAO.load(IFeed.class, feed.getId()));
    assertNull(DynamicDAO.getDAO(IConditionalGetDAO.class).load(feed.getLink()));
  }

  /**
   *
   */
  @Test
  public void testAddUpdateAndGetConditionalGet() {
    IFeed feed = createFeed();
    DynamicDAO.save(feed);

    final String ifModifiedSince = "2005/11/04";
    final String ifNoneMatch = "2005/05/12";
    IConditionalGet conditionalGet = fTypesFactory.createConditionalGet(ifModifiedSince, feed.getLink(), ifNoneMatch);
    DynamicDAO.save(conditionalGet);
    conditionalGet = null;
    System.gc();
    conditionalGet = DynamicDAO.getDAO(IConditionalGetDAO.class).load(feed.getLink());
    assertEquals(ifModifiedSince, conditionalGet.getIfModifiedSince());
    assertEquals(ifNoneMatch, conditionalGet.getIfNoneMatch());
    assertEquals(feed.getLink().toString(), conditionalGet.getLink().toString());
  }

  /**
   * Simply adds and deletes a feed and fails if an exception is thrown
   */
  @Test
  public void testAddAndDeleteFeed() {
    try {
      IFeed feed = createFeed();
      feed = DynamicDAO.save(feed);
      DynamicDAO.delete(new FeedReference(feed.getId()).resolve());
      assertNull(DynamicDAO.load(IFeed.class, feed.getId()));
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Simply adds and deletes a feed and fails if an exception is thrown
   */
  @Test
  public void testAddAndDeleteFeed2() {
    try {
      IFeed feed = createFeed();
      feed = DynamicDAO.save(feed);
      DynamicDAO.delete(feed);
      assertNull(DynamicDAO.load(IFeed.class, feed.getId()));
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Tests adding, updating and getting a feed.
   */
  // TODO Need to test news and categories
  @Test
  public void testAddUpdateAndGetFeed() {
    final Feed initialFeed = createFeed();
    final Person[] initialAuthor = new Person[1];
    final Image[] initialImage = new Image[1];
    final Feed[] updatedFeed = new Feed[1];
    final boolean[] feedAddedCalled = new boolean[] { false };
    final boolean[] feedUpdatedCalled = new boolean[] { false };
    FeedListener feedListener = null;
    try {
      feedListener = new FeedAdapter() {
        @Override
        public void entitiesAdded(Set<FeedEvent> events) {
          FeedEvent event = events.iterator().next();
          try {
            feedAddedCalled[0] = true;
            initialAuthor[0] = (Person) initialFeed.getAuthor();
            initialImage[0] = (Image) initialFeed.getImage();
            Feed dbFeed = (Feed) event.getEntity();
            initialAuthor[0].setId(dbFeed.getAuthor().getId());
            initialAuthor[0].isIdentical(dbFeed.getAuthor());
            initialImage[0].equals(dbFeed.getImage());
            initialFeed.setId(dbFeed.getId());
            assertTrue(NOT_IDENTICAL_MESSAGE, initialFeed.isIdentical(dbFeed));
            dbFeed.setCopyright("GPL");
            dbFeed.setFormat("someDifferentformat");
            dbFeed.getImage().setHeight(150);
            dbFeed.getImage().setDescription("Some new description");
            dbFeed.getImage().setTitle("yet another title");
            updatedFeed[0] = dbFeed;
            DynamicDAO.save(dbFeed);
          } catch (PersistenceException e) {
            fail(e.getMessage());
          }
        }

        @Override
        public void entitiesUpdated(Set<FeedEvent> events) {
          FeedEvent event = events.iterator().next();
          feedUpdatedCalled[0] = true;
          Feed dbFeed = (Feed) event.getEntity();
          assertTrue(updatedFeed[0].isIdentical(dbFeed));
        }
      };
      DynamicDAO.addEntityListener(IFeed.class, feedListener);
      DynamicDAO.save(initialFeed);
      assertTrue(feedAddedCalled[0]);
      assertTrue(feedUpdatedCalled[0]);
      assertTrue(DynamicDAO.exists(IFeed.class, initialFeed.getId()));
      DynamicDAO.delete(updatedFeed[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (feedListener != null) {
        DynamicDAO.removeEntityListener(IFeed.class, feedListener);
      }
    }
  }

  /**
   * Tests that the image attribute override solves the name clashes between
   * certain column names in Feed and Image.
   */
  @Test
  public void testImageAttributeOverride() {
    final Feed initialFeed = createFeed();
    final URI feedLink = initialFeed.getLink();
    IImage image = fTypesFactory.createImage(initialFeed);
    image.setHomepage(createURI("http://www.someuri.com"));
    initialFeed.setImage(image);
    initialFeed.getImage().setDescription("Some description");
    initialFeed.getImage().setTitle("Title");
    initialFeed.getImage().setHomepage(createURI("http://www.imageuri.com"));
    final Feed[] updatedFeed = new Feed[1];
    FeedListener feedListener = null;
    try {
      feedListener = new FeedAdapter() {
        @Override
        public void entitiesAdded(Set<FeedEvent> events) {
          try {
            FeedEvent event = events.iterator().next();
            Feed dbFeed = (Feed) event.getEntity();
            dbFeed.setDescription("feed description2");
            dbFeed.setTitle("feed title2");
            dbFeed.getImage().setDescription("Some new description");
            dbFeed.getImage().setTitle("yet another title");
            dbFeed.getImage().setHomepage(createURI("http://www.newimageuri.com"));
            updatedFeed[0] = dbFeed;
            DynamicDAO.save(dbFeed);
          } catch (PersistenceException e) {
            TestUtils.fail(e);
          }
        }

        @Override
        public void entitiesUpdated(Set<FeedEvent> events) {
          try {
            FeedEvent event = events.iterator().next();
            // TODO Add method to load entities without using the cache and
            // then use it here. Atm, this test won't actually show the bug
            // we want it to show because it's getting the feed from the cache
            // bypassing the db inconsistency
            Feed dbFeed = (Feed) event.getEntity();
            assertEquals(feedLink.toString(), dbFeed.getLink().toString());
          } catch (RuntimeException re) {
            fail(re.getMessage());
          }
        }

      };
      DynamicDAO.addEntityListener(IFeed.class, feedListener);
      DynamicDAO.save(initialFeed);
      DynamicDAO.delete(updatedFeed[0]);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (feedListener != null) {
        DynamicDAO.removeEntityListener(IFeed.class, feedListener);
      }
    }
  }

  /**
   * Tests that no event is sent when NewsManager#setState() is called and the
   * new state is the same as the old state.
   */
  @Test
  public void testNewsManagerSetSameStateWithQuery() {
    final IFeed feed;
    try {
      feed = DynamicDAO.save(createFeed());
    } catch (PersistenceException e) {
      fail(e.getMessage());
      return;
    }
    final News initialNews = (News) createNews(feed);
    initialNews.setState(State.NEW);
    INews news = null;
    try {
      news = DynamicDAO.save(initialNews);
    } catch (PersistenceException e) {
      fail(e.getMessage());
      return;
    }
    NewsListener newsListener = null;
    try {
      newsListener = new NewsListener() {
        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          fail("No events should have been fired, but NewsListener#entitiesAdded() was called");
        }

        @Override
        public void entitiesDeleted(Set<NewsEvent> events) {
          fail("No events should have been fired, but NewsListener#newsDeleted() was called.");
        }

        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          fail("No events should have been fired, but NewsListener#newsUpdated() was called.");
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      List<INews> newsList = new ArrayList<INews>();
      newsList.add(news);
      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.NEW, true, false);
      DynamicDAO.removeEntityListener(INews.class, newsListener);
      DynamicDAO.delete(news);
      DynamicDAO.delete(feed);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    } finally {
      if (newsListener != null) {
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      }
    }
  }

  /**
   * Tests {@link INewsDAO#setState(Collection, State, boolean, boolean)}
   */
  @Test
  public void testNewsManagerSetState() {
    final IFeed feed;
    feed = DynamicDAO.save(createFeed());
    final News initialNews = (News) createNews(feed);
    initialNews.setState(State.NEW);
    INews newsItem = null;
    NewsReference newsRef = null;
    newsItem = DynamicDAO.save(initialNews);
    newsRef = new NewsReference(newsItem.getId());
    List<INews> newsList = new ArrayList<INews>();
    newsList.add(newsItem);
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.UPDATED, true, false);
    INews news = newsRef.resolve();
    assertEquals(State.UPDATED, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.DELETED, true, false);
    news = newsRef.resolve();
    assertEquals(State.DELETED, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.HIDDEN, false, false);
    news = newsRef.resolve();
    assertEquals(State.HIDDEN, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.READ, false, false);
    news = newsRef.resolve();
    assertEquals(State.READ, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.UNREAD, true, false);
    news = newsRef.resolve();
    assertEquals(State.UNREAD, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.NEW, true, false);
    news = newsRef.resolve();
    assertEquals(State.NEW, news.getState());
    // Make sure it doesn't change when we set it to the same
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.NEW, true, false);
    news = newsRef.resolve();
    assertEquals(State.NEW, news.getState());

    DynamicDAO.delete(newsRef.resolve());
    DynamicDAO.delete(feed);
  }

  public void testNewsDAOSetStateFromDeletedWithAffectEquivalentNews() {
    final IFeed feed;
    feed = DynamicDAO.save(createFeed());
    final News initialNews = (News) createNews(feed);
    initialNews.setState(State.NEW);
    INews newsItem = null;
    NewsReference newsRef = null;
    newsItem = DynamicDAO.save(initialNews);
    newsRef = new NewsReference(newsItem.getId());
    List<INews> newsList = new ArrayList<INews>();
    newsList.add(newsItem);
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.DELETED, true, false);
    INews news = newsRef.resolve();
    assertEquals(State.DELETED, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.HIDDEN, true, false);
  }

  public void testNewsDAOSetStateFromHiddenWithAffectEquivalentNews() {
    final IFeed feed;
    feed = DynamicDAO.save(createFeed());
    final News initialNews = (News) createNews(feed);
    initialNews.setState(State.NEW);
    INews newsItem = null;
    NewsReference newsRef = null;
    newsItem = DynamicDAO.save(initialNews);
    newsRef = new NewsReference(newsItem.getId());
    List<INews> newsList = new ArrayList<INews>();
    newsList.add(newsItem);
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.HIDDEN, true, false);
    INews news = newsRef.resolve();
    assertEquals(State.HIDDEN, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.READ, true, false);
  }

  /**
   * Tests that the folders inside the folder are returned from the db in the
   * same order as they were saved.
   *
   * @throws PersistenceException
   */
  @Test
  public void testFoldersOrder() throws PersistenceException {
    IFolder root = createFolder();
    root = DynamicDAO.save(root);
    for (int i = 0; i < 10; ++i) {
      IFolder child = createFolder(root);
      child.setName(String.valueOf(i));
    }
    root = DynamicDAO.save(root);
    int counter = 0;
    for (IFolder child : root.getFolders()) {
      String name = String.valueOf(counter++);
      assertEquals(name, child.getName());
    }
  }

  /**
   * Tests that the marks inside the folder are returned from the db in the same
   * order as they were saved.
   *
   * @throws PersistenceException
   */
  @Test
  public void testMarksOrder() throws PersistenceException {
    IFolder root = createFolder();
    root = DynamicDAO.save(root);
    int count = 10;
    for (int i = 0; i < count; ++i) {
      IBookMark child = createBookMark(root);
      child.setName(String.valueOf(i));
    }
    int count2 = count * 2;
    for (int i = count; i < count2; ++i) {
      ISearchMark child = createSearchMark(root);
      child.setName(String.valueOf(i));
    }
    root = DynamicDAO.save(root);
    int counter = 0;
    for (IMark child : root.getMarks()) {
      String name = String.valueOf(counter++);
      assertEquals(name, child.getName());
    }
  }

  private ISearchMark createSearchMark(IFolder folder) {
    ISearchMark mark = fTypesFactory.createSearchMark(null, folder, "SomeName");
    mark.setCreationDate(new Date());
    mark.setLastVisitDate(createDate());
    mark.setPopularity(50);
    ISearchField field1 = fTypesFactory.createSearchField(INews.STATE, INews.class.getName());
    fTypesFactory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, State.NEW);

    ISearchField field2 = fTypesFactory.createSearchField(INews.STATE, INews.class.getName());
    fTypesFactory.createSearchCondition(null, mark, field2, SearchSpecifier.IS, State.UPDATED);

    ISearchField field3 = fTypesFactory.createSearchField(INews.STATE, INews.class.getName());
    fTypesFactory.createSearchCondition(null, mark, field3, SearchSpecifier.IS, State.UNREAD);

    return mark;
  }

  /**
   * Tests that {@link INewsDAO#setState(Collection, State, boolean, boolean)}
   * changes the state in news that have the same link but are in different
   * feeds.
   */
  @Test
  public void testNewsManagerSetStateWithMultipleFeedsAndGuidNull() {
    final IFeed feed1;
    IFeed feed2;
    NewsAdapter newsAdapter;
    try {
      feed1 = DynamicDAO.save(createFeed());
      IFeed tempFeed = createFeed("http://adifferentlink.com");
      tempFeed.setTitle("A different title");
      feed2 = DynamicDAO.save(tempFeed);
    } catch (PersistenceException e) {
      fail(e.getMessage());
      return;
    }

    final NewsReference[] newsRef = new NewsReference[1];
    newsAdapter = new NewsAdapter() {
      @Override
      public void entitiesAdded(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        newsRef[0] = new NewsReference(events.iterator().next().getEntity().getId());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsAdapter);

    final News initialNews1 = (News) createNews(feed1);
    initialNews1.setGuid(null);
    initialNews1.setState(State.NEW);
    final News initialNews2 = (News) createNews(feed2);
    initialNews2.setGuid(null);
    initialNews2.setState(State.NEW);

    INews newsItem1 = null;
    NewsReference newsRef1 = null;

    INews newsItem2 = null;
    NewsReference newsRef2 = null;

    NewsReference newsRef3 = null;
    try {
      DynamicDAO.save(feed1);
      newsRef1 = newsRef[0];
      newsItem1 = newsRef1.resolve();

      feed2 = DynamicDAO.save(feed2);
      newsRef2 = newsRef[0];
      newsItem2 = newsRef2.resolve();

      final News initialNews3 = (News) createNews(feed2);
      initialNews3.setTitle("Some other title");
      initialNews3.setGuid(null);
      initialNews3.setLink(null);
      initialNews3.setState(State.NEW);
      DynamicDAO.save(feed2);
      newsRef3 = newsRef[0];
    } catch (PersistenceException e) {
      fail(e.getMessage());
      return;
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsAdapter);
    }
    try {
      List<INews> newsList1 = new ArrayList<INews>();
      newsList1.add(newsItem1);

      List<INews> newsList2 = new ArrayList<INews>();
      newsList2.add(newsItem2);

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.UPDATED, true, false);
      INews news1 = newsRef1.resolve();
      INews news2 = newsRef2.resolve();
      INews news3 = newsRef3.resolve();
      assertEquals(State.UPDATED, news1.getState());
      assertEquals(State.UPDATED, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList2, State.READ, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.READ, news1.getState());
      assertEquals(State.READ, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.UNREAD, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.UNREAD, news1.getState());
      assertEquals(State.UNREAD, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList2, State.READ, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.READ, news1.getState());
      assertEquals(State.READ, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.UNREAD, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.UNREAD, news1.getState());
      assertEquals(State.UNREAD, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList2, State.NEW, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.NEW, news1.getState());
      assertEquals(State.NEW, news2.getState());
      assertEquals(State.NEW, news3.getState());

      // Make sure it doesn't change when we set it to the same
      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.NEW, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.NEW, news1.getState());
      assertEquals(State.NEW, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.HIDDEN, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.HIDDEN, news1.getState());
      assertEquals(State.HIDDEN, news2.getState());
      assertEquals(State.NEW, news3.getState());

      DynamicDAO.delete(newsRef1.resolve());
      DynamicDAO.delete(newsRef2.resolve());
      DynamicDAO.delete(feed1);
      DynamicDAO.delete(feed2);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Tests that {@link INewsDAO#setState(Collection, State, boolean, boolean)}
   * changes the state in news that have the same guid but are in different
   * feeds.
   */
  @Test
  public void testNewsManagerSetStateWithMultipleFeeds() {
    final IFeed feed1;
    IFeed feed2;
    NewsListener newsAdapter = null;
    try {
      feed1 = DynamicDAO.save(createFeed());
      IFeed tempFeed = createFeed("http://adifferentlink.com");
      tempFeed.setTitle("A different title");
      feed2 = DynamicDAO.save(tempFeed);
    } catch (PersistenceException e) {
      fail(e.getMessage());
      return;
    }
    final News initialNews1 = (News) createNews(feed1);
    initialNews1.setState(State.NEW);
    final News initialNews2 = (News) createNews(feed2);
    initialNews2.setState(State.NEW);

    final NewsReference[] newsRef = new NewsReference[1];

    newsAdapter = new NewsAdapter() {
      @Override
      public void entitiesAdded(Set<NewsEvent> events) {
        assertEquals(1, events.size());
        newsRef[0] = new NewsReference(events.iterator().next().getEntity().getId());
      }
    };
    DynamicDAO.addEntityListener(INews.class, newsAdapter);
    INews newsItem1 = null;
    NewsReference newsRef1 = null;

    INews newsItem2 = null;
    NewsReference newsRef2 = null;

    NewsReference newsRef3 = null;
    try {
      DynamicDAO.save(feed1);
      newsRef1 = newsRef[0];
      newsItem1 = newsRef1.resolve();

      feed2 = DynamicDAO.save(feed2);
      newsRef2 = newsRef[0];
      newsItem2 = newsRef2.resolve();

      final News initialNews3 = (News) createNews(feed2);
      initialNews3.setTitle("Some other title");
      initialNews3.setGuid(null);
      initialNews3.setLink(null);
      initialNews3.setState(State.NEW);
      DynamicDAO.save(feed2);
      newsRef3 = newsRef[0];
    } catch (PersistenceException e) {
      fail(e.getMessage());
      return;
    } finally {
      DynamicDAO.removeEntityListener(INews.class, newsAdapter);
    }
    try {
      List<INews> newsList1 = new ArrayList<INews>();
      newsList1.add(newsItem1);

      List<INews> newsList2 = new ArrayList<INews>();
      newsList2.add(newsItem2);

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.UPDATED, true, false);
      INews news1 = newsRef1.resolve();
      INews news2 = newsRef2.resolve();
      INews news3 = newsRef3.resolve();
      assertEquals(State.UPDATED, news1.getState());
      assertEquals(State.UPDATED, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList2, State.READ, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.READ, news1.getState());
      assertEquals(State.READ, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.UNREAD, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.UNREAD, news1.getState());
      assertEquals(State.UNREAD, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList2, State.READ, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.READ, news1.getState());
      assertEquals(State.READ, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.UNREAD, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.UNREAD, news1.getState());
      assertEquals(State.UNREAD, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList2, State.NEW, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.NEW, news1.getState());
      assertEquals(State.NEW, news2.getState());
      assertEquals(State.NEW, news3.getState());

      // Make sure it doesn't change when we set it to the same
      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.NEW, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.NEW, news1.getState());
      assertEquals(State.NEW, news2.getState());
      assertEquals(State.NEW, news3.getState());

      Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList1, State.DELETED, true, false);
      news1 = newsRef1.resolve();
      news2 = newsRef2.resolve();
      news3 = newsRef3.resolve();
      assertEquals(State.DELETED, news1.getState());
      assertEquals(State.DELETED, news2.getState());
      assertEquals(State.NEW, news3.getState());

      DynamicDAO.delete(newsRef1.resolve());
      DynamicDAO.delete(newsRef2.resolve());
      DynamicDAO.delete(feed1);
      DynamicDAO.delete(feed2);
    } catch (PersistenceException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Tests {@link INewsDAO#setState(Collection, State, boolean, boolean)}.
   */
  @Test
  public void testNewsManagerSetStateWithGuidNull() {
    final IFeed feed = DynamicDAO.save(createFeed());
    final News initialNews = (News) createNews(feed);
    initialNews.setState(State.NEW);
    initialNews.setGuid(null);
    INews newsItem = null;
    NewsReference newsRef = null;
    newsItem = DynamicDAO.save(initialNews);
    newsRef = new NewsReference(newsItem.getId());
    List<INews> newsList = new ArrayList<INews>();
    newsList.add(newsItem);
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.UPDATED, true, false);
    INews news = newsRef.resolve();
    assertEquals(State.UPDATED, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.READ, true, false);
    news = newsRef.resolve();
    assertEquals(State.READ, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.UNREAD, true, false);
    news = newsRef.resolve();
    assertEquals(State.UNREAD, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.READ, true, false);
    news = newsRef.resolve();
    assertEquals(State.READ, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.UNREAD, true, false);
    news = newsRef.resolve();
    assertEquals(State.UNREAD, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.NEW, true, false);
    news = newsRef.resolve();
    assertEquals(State.NEW, news.getState());
    // Make sure it doesn't change when we set it to the same
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.NEW, true, false);
    news = newsRef.resolve();
    assertEquals(State.NEW, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.DELETED, true, false);
    news = newsRef.resolve();
    assertEquals(State.DELETED, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.READ, false, false);
    news = newsRef.resolve();
    assertEquals(State.READ, news.getState());
    Owl.getPersistenceService().getDAOService().getNewsDAO().setState(newsList, State.HIDDEN, false, false);
    news = newsRef.resolve();
    assertEquals(State.HIDDEN, news.getState());

    DynamicDAO.delete(newsRef.resolve());
    DynamicDAO.delete(feed);
  }

  /**
   * Tests {@link NewsCounterService} for added news that are DELETED
   */
  @Test
  public void testNewsCounterOnDeletedAddedNews() {
    final IFeed feed = DynamicDAO.save(createFeed());
    INews news = createNews(feed);
    news.setFlagged(true);
    news.setState(INews.State.DELETED);

    DynamicDAO.save(news);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Root");
    IBookMark bookmark = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");

    DynamicDAO.save(bookmark);

    assertEquals(0, bookmark.getStickyNewsCount());
    assertEquals(0, bookmark.getNewsCount(INews.State.getVisible()));
  }

  /**
   * Tests {@link NewsCounterService} for added news that are DELETED and Sticky
   */
  @Test
  public void testNewsCounterOnStickyDeletedNews() {
    final IFeed feed = DynamicDAO.save(createFeed());
    INews news = createNews(feed);
    news.setState(INews.State.NEW);
    news.setFlagged(true);

    DynamicDAO.save(news);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Root");
    IBookMark bookmark = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");

    DynamicDAO.save(bookmark);

    assertEquals(1, bookmark.getStickyNewsCount());
    assertEquals(1, bookmark.getNewsCount(INews.State.getVisible()));

    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singleton(news), INews.State.HIDDEN, false, false);

    assertEquals(0, bookmark.getStickyNewsCount());
    assertEquals(0, bookmark.getNewsCount(INews.State.getVisible()));

    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singleton(news), INews.State.DELETED, false, false);

    assertEquals(0, bookmark.getStickyNewsCount());
    assertEquals(0, bookmark.getNewsCount(INews.State.getVisible()));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetProfile() {
    Pair<File, Long> profile = InternalOwl.getDefault().getProfile();
    assertTrue(profile.getFirst().exists());
    if (profile.getSecond() != null)
      assertTrue(profile.getSecond() > 0);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testBackups() {
    List<File> backups = InternalOwl.getDefault().getProfileBackups();
    if (!backups.isEmpty()) {
      for (File backup : backups) {
        assertTrue(backup.exists());
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testStartLevel() {
    StartLevel startLevel = Owl.getStartLevel();
    assertEquals(StartLevel.STARTED, startLevel);
  }
}
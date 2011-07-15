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

package org.rssowl.core.tests.persist.service;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Attachment;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Category;
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.Label;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.Person;
import org.rssowl.core.internal.persist.Preference;
import org.rssowl.core.internal.persist.SearchCondition;
import org.rssowl.core.internal.persist.SearchMark;
import org.rssowl.core.internal.persist.service.Counter;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.EntityIdsByEventType;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.NewsCounterItem;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.query.Query;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Tests defragmentation of db.
 */
public class DefragmentTest {
  private URI fPluginLocation;
  private IModelFactory fFactory = Owl.getModelFactory();

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fPluginLocation = FileLocator.toFileURL(Platform.getBundle("org.rssowl.core.tests").getEntry("/")).toURI();

    /* Label */
    ILabel label1 = fFactory.createLabel(null, "Label 0");
    ILabel label2 = fFactory.createLabel(null, "Label 1");
    label2.setColor("255,0,0");
    DynamicDAO.save(label1);
    DynamicDAO.save(label2);

    /* Feeds and News */
    List<IFeed> feeds = saveFeeds();
    IFeed feed = feeds.get(1);
    INews news = feed.getNews().get(2);
    news.addLabel(label1);
    news.addLabel(label2);
    DynamicDAO.save(news);

    INews anotherNews = feeds.get(feeds.size() - 1).getNews().get(0);
    anotherNews.addLabel(label1);
    DynamicDAO.save(anotherNews);

    /* Folder, Bookmark and Searchmark */
    IFolder folder = fFactory.createFolder(null, null, "Folder");
    folder.setProperty("key", "value");
    folder.setProperty("value", "key");

    IBookMark bm = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    bm.setProperty("key", "value");
    bm.setProperty("value", "key");

    IFeed anotherFeed = fFactory.createFeed(null, URI.create("http://www.rssowl.org"));

    INews news1 = fFactory.createNews(null, anotherFeed, new Date());
    news1.setState(INews.State.NEW);

    INews news2 = fFactory.createNews(null, anotherFeed, new Date());
    news2.setState(INews.State.UNREAD);

    INews news4 = fFactory.createNews(null, anotherFeed, new Date());
    news4.setState(INews.State.READ);
    news4.setFlagged(true);

    INews news5 = fFactory.createNews(null, anotherFeed, new Date());
    news5.setState(INews.State.READ);
    news5.setFlagged(true);

    INews news6 = fFactory.createNews(null, anotherFeed, new Date());
    news6.setState(INews.State.READ);
    news6.setFlagged(true);

    INews news7 = fFactory.createNews(null, anotherFeed, new Date());
    news7.setState(INews.State.HIDDEN);

    DynamicDAO.save(anotherFeed);

    bm = fFactory.createBookMark(null, folder, new FeedLinkReference(URI.create("http://www.rssowl.org")), "Other BookMark");
    NewsCounter counter = DynamicDAO.getDAO(INewsCounterDAO.class).load();
    NewsCounterItem item = new NewsCounterItem(1, 2, 3);
    counter.put(bm.getFeedLinkReference().getLinkAsText(), item);
    DynamicDAO.save(counter);
    ((BookMark) bm).setNewsCounter(counter);

    ISearchMark sm = fFactory.createSearchMark(null, folder, "SM");
    sm.setProperty("key", "value");
    sm.setProperty("value", "key");

    ISearchCondition sc1 = fFactory.createSearchCondition(fFactory.createSearchField(INews.TITLE, INews.class.getName()), SearchSpecifier.IS, "foobar");
    ISearchCondition sc2 = fFactory.createSearchCondition(fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName()), SearchSpecifier.CONTAINS, "test");
    ISearchCondition sc3 = fFactory.createSearchCondition(fFactory.createSearchField(INews.PUBLISH_DATE, INews.class.getName()), SearchSpecifier.IS, new Date());

    sm.addSearchCondition(sc1);
    sm.addSearchCondition(sc2);
    sm.addSearchCondition(sc3);

    /* News Bin with News */
    INewsBin bin = fFactory.createNewsBin(null, folder, "NewsBin");
    DynamicDAO.save(folder);
    INews newsCopy = fFactory.createNews(news, bin);
    DynamicDAO.save(newsCopy);
    DynamicDAO.save(bin);

    /* Preference */
    Preference pref = new Preference("longs");
    pref.putLongs(2, 3, 4);
    DynamicDAO.save(pref);

    pref = new Preference("strings");
    pref.putStrings("foo", "bar");
    DynamicDAO.save(pref);

    pref = new Preference("booleans");
    pref.putBooleans(false, true);
    DynamicDAO.save(pref);

    pref = new Preference("integers");
    pref.putIntegers(5, 6, 7);
    DynamicDAO.save(pref);

    pref = new Preference("long");
    pref.putLongs(5);
    DynamicDAO.save(pref);

    pref = new Preference("string");
    pref.putStrings("foobar");
    DynamicDAO.save(pref);

    pref = new Preference("boolean");
    pref.putBooleans(true);
    DynamicDAO.save(pref);

    pref = new Preference("integer");
    pref.putIntegers(8);
    DynamicDAO.save(pref);

    /* ISearchFilter with ISearch */
    ISearch search = fFactory.createSearch(null);
    sc1 = fFactory.createSearchCondition(fFactory.createSearchField(INews.TITLE, INews.class.getName()), SearchSpecifier.IS, "foobar");
    sc2 = fFactory.createSearchCondition(fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName()), SearchSpecifier.CONTAINS, "test");
    sc3 = fFactory.createSearchCondition(fFactory.createSearchField(INews.PUBLISH_DATE, INews.class.getName()), SearchSpecifier.IS, new Date());
    search.addSearchCondition(sc1);
    search.addSearchCondition(sc2);
    search.addSearchCondition(sc3);

    ISearchFilter filter = fFactory.createSearchFilter(null, search, "filter");
    filter.addAction(fFactory.createFilterAction("action1"));
    filter.addAction(fFactory.createFilterAction("action2"));

    DynamicDAO.save(filter);

    IConditionalGet conditionalGet = fFactory.createConditionalGet("2008-10-12-1943", URI.create("http://www.rssowl.org"), "foobar");
    DynamicDAO.save(conditionalGet);
    conditionalGet = fFactory.createConditionalGet("2008-10-12-1943", URI.create("http://www.rssowl.de"), "foobar");
    DynamicDAO.save(conditionalGet);
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
   * Tests defragment.
   */
  @Test
  @SuppressWarnings({ "unchecked", "null" })
  public void testDefragment() {
    internalDefragment(false);
  }

  /**
   * Tests defragment to a larger blocksize.
   */
  @Test
  @SuppressWarnings({ "unchecked", "null" })
  public void testDefragmentWithLargeBlockSize() {
    internalDefragment(true);
  }

  @SuppressWarnings({ "unchecked", "null" })
  private void internalDefragment(boolean useLargeBlocksize) {
    DBManager.getDefault().shutdown();

    String dbPath = DBManager.getDBFilePath();

    File originDbFile = new File(dbPath + ".origin");
    if (originDbFile.exists())
      originDbFile.delete();

    DBHelper.copyFileIO(new File(dbPath), originDbFile, new NullProgressMonitor());

    File defragmentedDbFile = new File(dbPath + ".dest");
    if (defragmentedDbFile.exists())
      defragmentedDbFile.delete();

    DBManager.copyDatabase(originDbFile, defragmentedDbFile, useLargeBlocksize, new NullProgressMonitor());

    System.gc();
    ObjectContainer db = Db4o.openFile(DBManager.createConfiguration(false), originDbFile.getAbsolutePath());
    ObjectContainer defragmentedDb = Db4o.openFile(DBManager.createConfiguration(false), defragmentedDbFile.getAbsolutePath());

    try {

      /* Assert Number of Entities */
      List<IEntity> entities = db.query(IEntity.class);
      assertEquals(entities.size(), defragmentedDb.query(IEntity.class).size());
      for (IEntity entity : entities) {
        Query query = defragmentedDb.query();
        query.constrain(entity.getClass());
        query.descend("fId").constrain(Long.valueOf(entity.getId())); //$NON-NLS-1$

        List<?> result = query.execute();
        assertEquals(1, result.size());
        assertEquals(entity, result.get(0));

        if (entity instanceof Attachment)
          assertTrue(((Attachment) entity).isIdentical((Attachment) result.get(0)));
        else if (entity instanceof BookMark)
          assertTrue(((BookMark) entity).isIdentical((BookMark) result.get(0)));
        else if (entity instanceof Category)
          assertTrue(((Category) entity).isIdentical((Category) result.get(0)));
        else if (entity instanceof Feed)
          assertTrue(((Feed) entity).isIdentical((Feed) result.get(0)));
        else if (entity instanceof Folder)
          assertTrue(((Folder) entity).isIdentical((Folder) result.get(0)));
        else if (entity instanceof Label)
          assertTrue(((Label) entity).isIdentical((Label) result.get(0)));
        else if (entity instanceof News)
          assertTrue(((News) entity).isIdentical((News) result.get(0)));
        else if (entity instanceof Person)
          assertTrue(((Person) entity).isIdentical((Person) result.get(0)));
        else if (entity instanceof SearchCondition)
          assertTrue(((SearchCondition) entity).isIdentical((SearchCondition) result.get(0)));
        else if (entity instanceof SearchMark)
          assertTrue(((SearchMark) entity).isIdentical((SearchMark) result.get(0)));
      }

      /* Assert News */
      List<INews> newsList = db.query(INews.class);
      assertEquals(newsList.size(), defragmentedDb.query(INews.class).size());
      for (INews news : newsList) {
        Query query = defragmentedDb.query();
        query.constrain(news.getClass());
        query.descend("fId").constrain(Long.valueOf(news.getId())); //$NON-NLS-1$

        List<INews> result = query.execute();
        assertEquals(1, result.size());
        assertEquals(news.getTitle(), result.get(0).getTitle());
      }

      /* Assert Description */
      List<Description> descriptions = db.query(Description.class);
      assertEquals(descriptions.size(), defragmentedDb.query(Description.class).size());
      for (Description description : descriptions) {
        Query query = defragmentedDb.query();
        query.constrain(description.getClass());
        query.descend("fNewsId").constrain(Long.valueOf(description.getNews().getId())); //$NON-NLS-1$

        List<Description> result = query.execute();
        assertEquals(1, result.size());
        assertEquals(description.getValue(), result.get(0).getValue());
      }

      /* Assert News Bins */
      List<INewsBin> newsBins = db.query(INewsBin.class);
      assertEquals(newsBins.size(), defragmentedDb.query(INewsBin.class).size());
      for (INewsBin newsBin : newsBins) {
        Query query = defragmentedDb.query();
        query.constrain(newsBin.getClass());
        query.descend("fId").constrain(Long.valueOf(newsBin.getId())); //$NON-NLS-1$

        List<INewsBin> result = query.execute();
        assertEquals(1, result.size());
        assertEquals(newsBin.getNews(), result.get(0).getNews());
      }

      /* Assert Folders, Bookmarks and Searchmarks */
      List<IFolder> folders = db.query(IFolder.class);
      assertEquals(folders.size(), defragmentedDb.query(IFolder.class).size());
      for (IFolder folder : folders) {
        Query query = defragmentedDb.query();
        query.constrain(folder.getClass());
        query.descend("fId").constrain(Long.valueOf(folder.getId())); //$NON-NLS-1$

        List<IFolder> result = query.execute();
        assertEquals(1, result.size());

        IFolder otherFolder = result.get(0);
        assertTrue(folder.getName().equals(otherFolder.getName()));
        assertTrue(folder.getProperties().equals(otherFolder.getProperties()));

        IBookMark bm = null;
        ISearchMark sm = null;
        List<IFolderChild> children = folder.getChildren();
        for (IFolderChild child : children) {
          if (child instanceof IBookMark)
            bm = (IBookMark) child;
          else if (child instanceof ISearchMark)
            sm = (ISearchMark) child;
        }

        IBookMark otherBM = null;
        ISearchMark otherSM = null;
        List<IFolderChild> otherChildren = otherFolder.getChildren();
        for (IFolderChild otherChild : otherChildren) {
          if (otherChild instanceof IBookMark)
            otherBM = (IBookMark) otherChild;
          else if (otherChild instanceof ISearchMark)
            otherSM = (ISearchMark) otherChild;
        }

        assertNotNull(bm);
        assertNotNull(sm);
        assertNotNull(otherBM);
        assertNotNull(otherSM);

        assertTrue(bm.getName().equals(otherBM.getName()));
        assertTrue(bm.getProperties().equals(otherBM.getProperties()));

        assertTrue(sm.getSearchConditions().size() == otherSM.getSearchConditions().size());
      }

      /* Assert Preference */
      List<IPreference> preferences = db.query(IPreference.class);
      assertEquals(preferences.size(), defragmentedDb.query(IPreference.class).size());
      for (IPreference preference : preferences) {
        Query query = defragmentedDb.query();
        query.constrain(preference.getClass());
        query.descend("fId").constrain(Long.valueOf(preference.getId())); //$NON-NLS-1$

        List<IPreference> result = query.execute();
        assertEquals(1, result.size());

        IPreference otherPreference = result.get(0);

        assertEquals(preference.getKey(), otherPreference.getKey());
        if ("string".equals(preference.getKey()))
          assertEquals(preference.getString(), otherPreference.getString());

        if ("strings".equals(preference.getKey()))
          assertTrue(Arrays.equals(preference.getStrings(), otherPreference.getStrings()));

        if ("boolean".equals(preference.getKey()))
          assertEquals(preference.getBoolean(), otherPreference.getBoolean());

        if ("booleans".equals(preference.getKey()))
          assertTrue(Arrays.equals(preference.getBooleans(), otherPreference.getBooleans()));

        if ("integer".equals(preference.getKey()))
          assertEquals(preference.getInteger(), otherPreference.getInteger());

        if ("integers".equals(preference.getKey()))
          assertTrue(Arrays.equals(preference.getIntegers(), otherPreference.getIntegers()));

        if ("long".equals(preference.getKey()))
          assertEquals(preference.getLong(), otherPreference.getLong());

        if ("longs".equals(preference.getKey()))
          assertTrue(Arrays.equals(preference.getLongs(), otherPreference.getLongs()));
      }

      /* Assert Label */
      List<ILabel> labels = db.query(ILabel.class);
      assertEquals(labels.size(), defragmentedDb.query(ILabel.class).size());
      for (ILabel label : labels) {
        Query query = defragmentedDb.query();
        query.constrain(label.getClass());
        query.descend("fId").constrain(Long.valueOf(label.getId())); //$NON-NLS-1$

        List<INewsBin> result = query.execute();
        assertEquals(1, result.size());
        assertTrue(((Label) label).isIdentical((ILabel) result.get(0)));
      }

      /* Assert Counter */
      assertEquals(db.query(Counter.class).get(0).getValue(), defragmentedDb.query(Counter.class).get(0).getValue());

      /* Assert Search Filter */
      List<ISearchFilter> filters = db.query(ISearchFilter.class);
      assertEquals(filters.size(), defragmentedDb.query(ISearchFilter.class).size());
      for (ISearchFilter filter : filters) {
        Query query = defragmentedDb.query();
        query.constrain(filter.getClass());
        query.descend("fId").constrain(Long.valueOf(filter.getId())); //$NON-NLS-1$

        List<INewsBin> result = query.execute();
        assertEquals(1, result.size());

        ISearchFilter otherFilter = (ISearchFilter) result.get(0);
        assertTrue(filter.getName().equals(otherFilter.getName()));
        assertEquals(filter.getActions().size(), otherFilter.getActions().size());

        ISearch search = filter.getSearch();
        ISearch otherSearch = otherFilter.getSearch();

        assertEquals(search.getSearchConditions().size(), otherSearch.getSearchConditions().size());
      }

      /* Assert Conditional Get */
      List<IConditionalGet> condGets = db.query(IConditionalGet.class);
      assertEquals(condGets.size(), defragmentedDb.query(IConditionalGet.class).size());
      for (IConditionalGet condGet : condGets) {
        Query query = defragmentedDb.query();
        query.constrain(condGet.getClass());
        query.descend("fLink").constrain(condGet.getLink().toString()); //$NON-NLS-1$

        List<IConditionalGet> result = query.execute();
        assertEquals(1, result.size());

        IConditionalGet otherCondGet = result.get(0);
        assertEquals(condGet.getIfModifiedSince(), otherCondGet.getIfModifiedSince());
        assertEquals(condGet.getIfNoneMatch(), otherCondGet.getIfNoneMatch());
      }

      /* Assert EntityIdsByEventType */
      EntityIdsByEventType eventType = db.query(EntityIdsByEventType.class).get(0);
      EntityIdsByEventType otherEventType = defragmentedDb.query(EntityIdsByEventType.class).get(0);
      assertNotNull(eventType);
      assertNotNull(otherEventType);
      assertEquals(eventType, otherEventType);

      /* Assert NewsCounter / NewsCounterItem */
      NewsCounter newsCounter = db.query(NewsCounter.class).get(0);
      db.activate(newsCounter, Integer.MAX_VALUE);
      NewsCounter otherNewsCounter = defragmentedDb.query(NewsCounter.class).get(0);
      defragmentedDb.activate(otherNewsCounter, Integer.MAX_VALUE);
      assertNotNull(newsCounter);
      assertNotNull(otherNewsCounter);

      NewsCounterItem item = otherNewsCounter.get("http://www.rssowl.org");
      assertEquals(1, item.getNewCounter());
      assertEquals(2, item.getUnreadCounter());
      assertEquals(3, item.getStickyCounter());
    } finally {
      db.close();
      defragmentedDb.close();
    }
  }

  private List<IFeed> saveFeeds() throws Exception {
    List<IFeed> feeds = new ArrayList<IFeed>();
    for (int i = 1; i < 200; i++) {
      URI feedLink = fPluginLocation.resolve("data/performance/" + i + ".xml").toURL().toURI();
      IFeed feed = new Feed(feedLink);

      InputStream inS = new BufferedInputStream(new FileInputStream(new File(feed.getLink())));
      Owl.getInterpreter().interpret(inS, feed, null);

      feeds.add(feed);
    }
    DynamicDAO.saveAll(feeds);
    return feeds;
  }
}
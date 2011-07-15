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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.search.ModelSearchQueries;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.tests.TestUtils;
import org.rssowl.core.util.SearchHit;
import org.rssowl.ui.internal.util.ModelUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * Test searching types from the persistence layer.
 *
 * @author bpasero
 */
public class ModelSearchTest4 extends AbstractModelSearchTest {
  private static final boolean TEST_FAILING_WILDCARD = false;

  /**
   * @throws Exception
   */
  @Test
  public void testPhraseSearch_CONTAINS() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      /* Title */
      INews news1 = createNews(feed, "Johnny lives hungry Depp", "http://www.news.com/news1.html", State.NEW);

      /* Description */
      INews news2 = createNews(feed, "News2", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer name like Michael Jackson.");

      /* Author */
      INews news3 = createNews(feed, "News3", "http://www.news.com/news3.html", State.NEW);
      IPerson author = fFactory.createPerson(null, news3);
      author.setName("Arnold Schwarzenegger");

      /* Category */
      INews news4 = createNews(feed, "lives", "http://www.news.com/news4.html", State.NEW);
      ICategory category = fFactory.createCategory(null, news4);
      category.setName("Roberts");

      /* Attachment Content */
      INews news5 = createNews(feed, "hungry", "http://www.news.com/news5.html", State.NEW);
      IAttachment attachment = fFactory.createAttachment(null, news5);
      attachment.setLink(new URI("http://www.attachment.com/att1news2.file"));
      attachment.setType("Hasselhoff");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"lives hungry\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"longer name like\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"lives hungry\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"longer name like\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"longer name like\"");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news2);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"longer name like\"");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertTrue(result.isEmpty());
      }

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"lives hungry\" lives hungry");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"lives hungry\" lives hungry");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news4, news5);
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testPhraseSearch_CONTAINS_ALL() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      /* Title */
      INews news1 = createNews(feed, "Johnny lives hungry Depp", "http://www.news.com/news1.html", State.NEW);

      /* Description */
      INews news2 = createNews(feed, "News2", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer name like Michael Jackson.");

      /* Author */
      INews news3 = createNews(feed, "News3", "http://www.news.com/news3.html", State.NEW);
      IPerson author = fFactory.createPerson(null, news3);
      author.setName("Arnold Schwarzenegger");

      /* Category */
      INews news4 = createNews(feed, "lives", "http://www.news.com/news4.html", State.NEW);
      ICategory category = fFactory.createCategory(null, news4);
      category.setName("Roberts");

      /* Attachment Content */
      INews news5 = createNews(feed, "hungry", "http://www.news.com/news5.html", State.NEW);
      IAttachment attachment = fFactory.createAttachment(null, news5);
      attachment.setLink(new URI("http://www.attachment.com/att1news2.file"));
      attachment.setType("Hasselhoff");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"lives hungry\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"longer name like\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"lives hungry\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"longer name like\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"longer name like\"");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news2);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"longer name like\"");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertTrue(result.isEmpty());
      }

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"lives hungry\" lives hungry");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"lives hungry\" lives hung");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertTrue(result.isEmpty());
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testPhraseSearch_CONTAINS_NOT() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      /* Title */
      INews news1 = createNews(feed, "Johnny lives hungry Depp", "http://www.news.com/news1.html", State.NEW);

      /* Description */
      INews news2 = createNews(feed, "News2", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer name like Michael Jackson.");

      /* Author */
      INews news3 = createNews(feed, "News3", "http://www.news.com/news3.html", State.NEW);
      IPerson author = fFactory.createPerson(null, news3);
      author.setName("Arnold Schwarzenegger");

      /* Category */
      INews news4 = createNews(feed, "lives", "http://www.news.com/news4.html", State.NEW);
      ICategory category = fFactory.createCategory(null, news4);
      category.setName("Roberts");

      /* Attachment Content */
      INews news5 = createNews(feed, "hungry", "http://www.news.com/news5.html", State.NEW);
      IAttachment attachment = fFactory.createAttachment(null, news5);
      attachment.setLink(new URI("http://www.attachment.com/att1news2.file"));
      attachment.setType("Hasselhoff");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"lives hungry\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"longer name like\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"lives hungry\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"longer name like\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"longer name like\"");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"longer name like\"");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"Johnny lives hungry Depp\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news3, news4, news5);
      }

      {
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"lives hungry\" lives hungry");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);
      }

      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "\"lives hungry\" lives hungry");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLocationSearch_BINs() throws Exception {
    IFolderChild root = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(root);

    IFolderChild child = fFactory.createFolder(null, (IFolder) root, "Child");
    DynamicDAO.save(child);

    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Title", "http://www.news.com/news1.html", State.NEW);
    DynamicDAO.save(feed);

    IFolderChild mark = fFactory.createBookMark(null, (IFolder) child, new FeedLinkReference(feed.getLink()), "Mark");
    DynamicDAO.save(mark);

    IFolderChild bin = fFactory.createNewsBin(null, (IFolder) root, "Bin");
    DynamicDAO.save(bin);
    News copiedNews = new News((News) news1, bin.getId().longValue());
    DynamicDAO.save(copiedNews);
    DynamicDAO.save(bin);

    /* Wait for Indexer */
    waitForIndexer();

    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(mark)));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertEquals(1, result.size());
      assertSame(result, news1);
    }

    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(bin)));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertEquals(1, result.size());
      assertSame(result, copiedNews);
    }

    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(child)));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertEquals(1, result.size());
      assertSame(result, news1);
    }

    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(root)));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertEquals(2, result.size());
      assertSame(result, news1, copiedNews);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testHiddenDeletedNewsNotIndexed_1() throws Exception {

    /* First add some Types */
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com/feed1.xml"));

    createNews(feed1, "First News of Feed One", "http://www.news.com/news1.html", State.HIDDEN);
    createNews(feed1, "Second News of Feed One", "http://www.news.com/news2.html", State.DELETED);

    DynamicDAO.save(feed1);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field1 = fFactory.createSearchField(INews.FEED, fNewsEntityName);
    ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, "http://www.feed.com/feed1.xml");

    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
    assertEquals(0, result.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testHiddenDeletedNewsNotIndexed_2() throws Exception {

    /* First add some Types */
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com/feed1.xml"));

    INews news1 = createNews(feed1, "First News of Feed One", "http://www.news.com/news1.html", State.NEW);
    INews news2 = createNews(feed1, "Second News of Feed One", "http://www.news.com/news2.html", State.READ);

    DynamicDAO.save(feed1);

    /* Wait for Indexer */
    waitForIndexer();

    news1.setState(INews.State.HIDDEN);
    news2.setState(INews.State.DELETED);

    DynamicDAO.save(feed1);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field1 = fFactory.createSearchField(INews.FEED, fNewsEntityName);
    ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, "http://www.feed.com/feed1.xml");

    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
    assertEquals(0, result.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRestoredNewsSearchable() throws Exception {

    /* First add some Types */
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com/feed1.xml"));

    INews news1 = createNews(feed1, "First News of Feed One", "http://www.news.com/news1.html", State.HIDDEN);
    INews news2 = createNews(feed1, "Second News of Feed One", "http://www.news.com/news2.html", State.HIDDEN);

    DynamicDAO.save(feed1);

    /* Wait for Indexer */
    waitForIndexer();

    news1.setState(INews.State.NEW);
    news2.setState(INews.State.READ);

    DynamicDAO.save(feed1);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field1 = fFactory.createSearchField(INews.FEED, fNewsEntityName);
    ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, "http://www.feed.com/feed1.xml");

    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
    assertSame(result, news1, news2);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testScopeSearch() throws Exception {

    /* First add some Types */
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFolder subFolder1 = fFactory.createFolder(null, rootFolder, "Sub Folder 1");
    DynamicDAO.save(subFolder1);

    IFolder subFolder2 = fFactory.createFolder(null, rootFolder, "Sub Folder 2");
    DynamicDAO.save(subFolder2);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed.com/feed1.xml"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed.com/feed2.xml"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed.com/feed3.xml"));

    INews news1 = createNews(feed1, "First News of Feed One", "http://www.news.com/news1.html", State.NEW);
    INews news2 = createNews(feed2, "First News of Feed Two", "http://www.news.com/news2.html", State.UNREAD);
    INews news3 = createNews(feed3, "First News of Feed Three", "http://www.news.com/news3.html", State.READ);
    news3.setFlagged(true);

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, subFolder1, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, subFolder2, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField fieldLoc = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
    ISearchField fieldState = fFactory.createSearchField(INews.STATE, fNewsEntityName);
    ISearchField fieldIsFlagged = fFactory.createSearchField(INews.IS_FLAGGED, fNewsEntityName);
    ISearchField fieldAllFields = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

    /* Search Test: Simple */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder })));
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news1, news2, news3);
    }

    /* Search Test: Simple */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subFolder1 })));
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news2);
    }

    /* Search Test: Simple */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subFolder2 })));
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news3);
    }

    /* Search Test: Simple */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subFolder1, subFolder2 })));
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news2, news3);
    }

    /* Search Test: Simple */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news1, news2, news3);
    }

    /* Search Test: Simple */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1 })));
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news1);
    }

    /* Search Test: Simple */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, subFolder2 })));
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news1, news3);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), false);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), true);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder, subFolder2 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), false);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), false);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), false);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, new Long[][] { { null }, { null }, { null } });

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, new Long[][] { { null }, { null }, { null } });
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, INews.State.UNREAD));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), false);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), false);
      assertSame(result, news1, news3);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "One");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), false);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "One");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), false);
      assertSame(result, news1, news3);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "One");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS_ALL, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), false);
      assertSame(result, news1, news3);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS_ALL, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS_ALL, "One");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS_NOT, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), false);
      assertSame(result, news1, news2);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS_NOT, "Three");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1, bm2, bm3 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS_NOT, "One");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3), true);
      assertTrue(result.isEmpty());
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bm1 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");
      ISearchCondition cond4 = fFactory.createSearchCondition(fieldIsFlagged, SearchSpecifier.IS, true);

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3, cond4), false);
      assertSame(result, news1);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subFolder1, subFolder2, bm1 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");
      ISearchCondition cond4 = fFactory.createSearchCondition(fieldIsFlagged, SearchSpecifier.IS, true);

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3, cond4), false);
      assertSame(result, news1, news3);
    }

    /* Search Test: Complex */
    {
      ISearchCondition cond1 = fFactory.createSearchCondition(fieldLoc, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subFolder1, subFolder2, bm1 })));
      ISearchCondition cond2 = fFactory.createSearchCondition(fieldState, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
      ISearchCondition cond3 = fFactory.createSearchCondition(fieldAllFields, SearchSpecifier.CONTAINS, "Three");
      ISearchCondition cond4 = fFactory.createSearchCondition(fieldIsFlagged, SearchSpecifier.IS, true);

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2, cond3, cond4), true);
      assertSame(result, news3);
    }
  }

  /**
   * See http://dev.rssowl.org/show_bug.cgi?id=1122
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchNewsWithPhraseInCategory() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news = createNews(feed, "Friend", "http://www.news.com/news3.html", State.READ);
      ICategory category = fFactory.createCategory(null, news);
      category.setName("Global");
      news.addCategory(category);

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* Condition 1 */
      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"Giant Global Graph\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1), false);
        assertEquals(0, result.size());
      }

      /* Condition 2 */
      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Giant Global Graph\"");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1), false);
        assertEquals(0, result.size());
      }

      /* Condition 1 */
      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Giant Global Graph");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1), false);
        assertEquals(0, result.size());
      }

      /* Condition 1 */
      {
        ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Giant Global Graph");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1), false);
        assertEquals(1, result.size());
        assertSame(result, news);
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithInvalidLocation() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

    INews news = createNews(feed, "Friend", "http://www.news.com/news3.html", State.READ);
    ICategory category = fFactory.createCategory(null, news);
    category.setName("Global");
    news.addCategory(category);

    DynamicDAO.save(feed);

    IFolder root = fFactory.createFolder(null, null, "Root");
    fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");
    DynamicDAO.save(root);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);

    ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.IS, new Long[][] { { 10l }, { 20l }, { 30l } });

    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1), false);
    assertEquals(0, result.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsReindexedWhenLabelChanges() throws Exception {
    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Foo"));

    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "News with Label", "http://www.news.com/news3.html", State.READ);
    news.addLabel(label);
    DynamicDAO.save(feed);

    waitForIndexer();

    ISearchField field = fFactory.createSearchField(INews.LABEL, fNewsEntityName);

    ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.IS, "foo");

    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1), false);
    assertEquals(1, result.size());
    assertEquals("News with Label", result.get(0).getResult().resolve().getTitle());

    label.setName("Bar");
    DynamicDAO.save(label);

    waitForIndexer();

    condition1 = fFactory.createSearchCondition(field, SearchSpecifier.IS, "bar");

    result = fModelSearch.searchNews(list(condition1), false);
    assertEquals(1, result.size());
    assertEquals("News with Label", result.get(0).getResult().resolve().getTitle());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMaxClauseCount() throws Exception {
    int maxClauseCount = BooleanQuery.getMaxClauseCount();

    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    createNews(feed, "Foo", "http://www.news.com/news3.html", State.READ);
    DynamicDAO.save(feed);

    waitForIndexer();

    ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();
    for (int i = 0; i < 1030; i++) {
      ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo" + i);
      conditions.add(condition1);
    }

    conditions.add(fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo"));

    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(conditions, false);
    assertEquals(1, result.size());
    assertEquals("Foo", result.get(0).getResult().resolve().getTitle());

    BooleanQuery.setMaxClauseCount(maxClauseCount);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMaxClauseCountForQuery() throws Exception {
    int maxClauseCount = BooleanQuery.getMaxClauseCount();
    BooleanQuery.setMaxClauseCount(3);

    IFolderChild root = fFactory.createFolder(null, null, "Root");
    IFeed feed1 = DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed.com/feed1.xml")));
    IFeed feed2 = DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed.com/feed2.xml")));
    IFeed feed3 = DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed.com/feed3.xml")));
    IFeed feed4 = DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed.com/feed4.xml")));

    DynamicDAO.save(fFactory.createBookMark(null, (IFolder) root, new FeedLinkReference(feed1.getLink()), "BM1"));
    DynamicDAO.save(fFactory.createBookMark(null, (IFolder) root, new FeedLinkReference(feed2.getLink()), "BM1"));
    DynamicDAO.save(fFactory.createBookMark(null, (IFolder) root, new FeedLinkReference(feed3.getLink()), "BM1"));
    DynamicDAO.save(fFactory.createBookMark(null, (IFolder) root, new FeedLinkReference(feed4.getLink()), "BM1"));

    ISearchField field = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();
    conditions.add(fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList(root))));

    Query query = ModelSearchQueries.createQuery(conditions, null, false);
    assertNotNull(query);

    BooleanQuery.setMaxClauseCount(maxClauseCount);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testReindexAll() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    createNews(feed, "Foo", "http://www.news.com/news.html", State.NEW);
    DynamicDAO.save(feed);

    waitForIndexer();

    ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo");

    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertEquals("Foo", result.get(0).getResult().resolve().getTitle());

    fModelSearch.reindexAll(new NullProgressMonitor());
    fModelSearch.optimize();

    waitForIndexer();

    fModelSearch.shutdown(false);
    fModelSearch.startup();

    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertEquals("Foo", result.get(0).getResult().resolve().getTitle());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsByAge() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

    INews news_1_Minute = fFactory.createNews(null, feed, new Date());
    news_1_Minute.setPublishDate(new Date(System.currentTimeMillis() - 1 * MINUTE));

    INews news_2_Minutes = fFactory.createNews(null, feed, new Date());
    news_2_Minutes.setPublishDate(new Date(System.currentTimeMillis() - 2 * MINUTE));

    INews news_1_Hour = fFactory.createNews(null, feed, new Date());
    news_1_Hour.setPublishDate(new Date(System.currentTimeMillis() - 60 * MINUTE));

    INews news_2_Hours = fFactory.createNews(null, feed, new Date());
    news_2_Hours.setPublishDate(new Date(System.currentTimeMillis() - 120 * MINUTE));

    INews news_1_Day = fFactory.createNews(null, feed, new Date());
    news_1_Day.setPublishDate(new Date(System.currentTimeMillis() - 1 * DAY - 1 * MINUTE));

    INews news_2_Days = fFactory.createNews(null, feed, new Date());
    news_2_Days.setPublishDate(new Date(System.currentTimeMillis() - 2 * DAY));

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField ageField = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsEntityName);

    /* 1 Minute */
    ISearchCondition condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS, -1);
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_1_Minute));

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, -1);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(0, result.size());

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_GREATER_THAN, -1);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(5, result.size());

    /* 2 Minutes */
    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS, -2);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_2_Minutes));

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, -2);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_1_Minute));

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_GREATER_THAN, -2);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(4, result.size());

    /* 1 Hour */
    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS, -60);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_1_Hour));

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, -60);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(2, result.size());

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_GREATER_THAN, -60);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(3, result.size());

    /* 2 Hours */
    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS, -120);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_2_Hours));

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, -120);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(3, result.size());

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_GREATER_THAN, -120);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(2, result.size());

    /* 1 Day */
    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS, 1);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_1_Day));

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, 1);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(4, result.size());

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_GREATER_THAN, 1);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_2_Days));

    /* 2 Days */
    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS, 2);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getResult().references(news_2_Days));

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, 2);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(5, result.size());

    condition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_GREATER_THAN, 2);
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(0, result.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithOddDoubleQuotes() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    createNews(feed, "Hello World", "http://www.news.com/news1.html", State.READ);

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Hello");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Hello World");
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"Hello World\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Hello\" World");
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Hello\" \"World");
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "\"Hello World\"\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertEquals(1, result.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Dash() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "IBM-Microsoft", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Foo", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "Bar", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    ICategory category = fFactory.createCategory(null, news);
    category.setName("Apple-Google");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField catField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM-Microsoft");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"IBM-Microsoft\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.CONTAINS_ALL, "Apple-Google");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.BEGINS_WITH, "Apple-");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.ENDS_WITH, "-Google");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.CONTAINS_ALL, "\"Apple-Google\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    if (TEST_FAILING_WILDCARD) {
      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM-Micr*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM-Micr?soft");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "I?M-Microsoft");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Apostroph() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "IBM'Microsoft", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Foo", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "Bar", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    ICategory category = fFactory.createCategory(null, news);
    category.setName("Apple'Google");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField catField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM'Microsoft");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"IBM'Microsoft\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM'Micr*");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM'Micr?soft");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "I?M'Microsoft");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.CONTAINS_ALL, "Apple'Google");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.BEGINS_WITH, "Apple'");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.ENDS_WITH, "Google");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.CONTAINS_ALL, "\"Apple'Google\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_German() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "IBM\u00f6\u00e4\u00fc\u00dfMicrosoft", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Foo", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "Bar", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    ICategory category = fFactory.createCategory(null, news);
    category.setName("Apple\u00f6\u00e4\u00fc\u00dfGoogle");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    int index = 0;

    ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField catField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM\u00f6\u00e4\u00fc\u00dfMicrosoft");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"IBM\u00f6\u00e4\u00fc\u00dfMicrosoft\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM\u00f6\u00e4\u00fc\u00dfMicr*");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM\u00f6\u00e4\u00fc\u00dfMicr?soft");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "I?M\u00f6\u00e4\u00fc\u00dfMicrosoft");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM\u00f6*\u00dfMicrosoft");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.IS, "Apple\u00f6\u00e4\u00fc\u00dfGoogle");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.IS, "Apple\u00f6?\u00fc\u00dfGoogle");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.BEGINS_WITH, "Apple\u00f6");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.BEGINS_WITH, "App*");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.BEGINS_WITH, "App?");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.ENDS_WITH, "\u00dfGoogle");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.ENDS_WITH, "?Google");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);

    condition = fFactory.createSearchCondition(catField, SearchSpecifier.ENDS_WITH, "*\u00dfGoogle");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(String.valueOf(index++), result, news);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Author() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    IPerson author = fFactory.createPerson(null, news);
    author.setName("Jacek Jędruch");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Jacek Jędruch");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Jac* Ję?ruch");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "\"Jacek Jędruch\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Jędruch");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Jęd?uch");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "\"Jędruch\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    author = fFactory.createPerson(null, news);
    author.setName("Tōkaidōchū Hizakurige");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Tōkaidōchū Hizakurige");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Tōkaidōchū");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "\"Tōkaidōchū Hizakurige\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "\"Tōkaidōchū\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    author = fFactory.createPerson(null, news);
    author.setName("Matija Zmajević");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Matija Zmajević");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Zmajević");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    author = fFactory.createPerson(null, news);
    author.setName("Peter Mötiß");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Peter Mötiß");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "Mötiß");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Author_Dash() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    IPerson author = fFactory.createPerson(null, news);
    author.setName("Benjamin Wilhelm-Tello");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(authorField);
    fields.add(allField);

    for (ISearchField field : fields) {
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin Wilhelm-Tello");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Benjamin Wilhelm-Tello\"");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Benjamin Wilhelm-Tello\"");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Wilhelm-Tello");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Benjamin Wilhelm-Tello\"");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin Wilhelm-Tello");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Benjamin Wilhelm-Tello\"");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Wilhelm-Tello");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin Wilhelm-Tel*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin Wilhelm-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin Wi*-Tello");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Ben* Wilhelm-Tello");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Author_AngleBrackets() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    IPerson author = fFactory.createPerson(null, news);
    author.setName("<Benjamin Wilhelm-Tello>");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "<Benjamin Wilhelm-Tello>");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "<Benjamin Wi?helm-Tello>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "<Benjamin Wi?helm-*>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    author = fFactory.createPerson(null, news);
    author.setName("<Benjamin>");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "<Benjamin>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_ALL, "<Ben*");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Category_AngleBrackets() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    ICategory category = fFactory.createCategory(null, news);
    category.setName("<Benjamin Wilhelm-Tello>");

    category = fFactory.createCategory(null, news);
    category.setName("<karakas>");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(categoryField, SearchSpecifier.IS, "<Benjamin Wilhelm-Tello>");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(categoryField, SearchSpecifier.IS, "<Benjamin Wi?helm-Tello>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(categoryField, SearchSpecifier.IS, "<Benjamin Wi?helm-*>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(categoryField, SearchSpecifier.IS, "<karakas>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(categoryField, SearchSpecifier.IS, "<kar?kas>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(categoryField, SearchSpecifier.IS, "<kara*");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Category_Dash() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    ICategory category = fFactory.createCategory(null, news);
    category.setName("Benjamin Wilhelm-Tello");

    category = fFactory.createCategory(null, news);
    category.setName("IBM-Research");

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);

    /*
     * TODO Known issue with searches in RSSOwl: Searching in all fields for a
     * category that gets tokenized into more than one token will fail.
     * Workaround: Search directly for the category.
     */
    //ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == categoryField ? SearchSpecifier.IS : SearchSpecifier.CONTAINS_ALL);

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Benjamin Wilhelm-Tello");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "Benjamin Wilhelm-Tel*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "Benjamin Wilhelm-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "Benjamin Wi*-Tello");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "Ben* Wilhelm-Tello");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "IBM-Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "IBM-Resea*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "IBM-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "IBM*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);

      condition = fFactory.createSearchCondition(field, specifier, "I?M-Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Mix_1() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "This [leverages] Pasero Adlistr. 27 the \"approach\" (IBM) Corp. 030-800600200 and WIL_Tel all@yes.org if 127.0.0.1 &lt;karakas&gt; sees Malara?", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "[leverages]");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "leverages");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"[leverages]\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "(IBM)");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"(IBM)\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Corp.");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Corp");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Corp.\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "WIL_Tel");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "WIL");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "W*L Tel");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"WIL_Tel\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "all@yes.org");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"all@yes.org\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "030-800600200");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "030-800*0");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"030-800600200\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Adlistr. 27");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Adlistr");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    if (TEST_FAILING_WILDCARD) {
      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "A*istr. 27");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Adlistr. 27\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "approach");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "appr?ach");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"approach\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "127.0.0.1");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "1?7.0.0.1");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"127.0.0.1\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Malara");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "M*ara");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Malara\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    if (TEST_FAILING_WILDCARD) {
      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "[lev*ages]");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }

    if (TEST_FAILING_WILDCARD) {
      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "(I?M)");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }

    if (TEST_FAILING_WILDCARD) {
      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Co?p.");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a?l@yes.o?g");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "<karakas>");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"<karakas>\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Mix_2() throws Exception {

    /* First add some Types */
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "This {leverages} /Pasero/ Adlistr. 27 the App=Roach #IBM $Corp 030+800600200 Lora: WIL_Tel all@yes.org if 127.0.0.1 %karakas% sees Malara!", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);

    /* Wait for Indexer */
    waitForIndexer();

    ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "{leverages}");
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "leverages");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"{leverages}\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "pasero");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "/Pasero/");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "App=Roach");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"App=Roach\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Roach");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "#IBM");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "IBM");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"IBM\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"#IBM\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "$Corp");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Corp");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Corp\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"$Corp\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "%karakas%");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"%karakas%\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "karakas");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"karakas\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "030+800600200");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "800600200");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"030+800600200\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Malara");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "M*ara");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Malara\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Lora");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Lora:");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "\"Lora\"");
    result = fModelSearch.searchNews(list(condition), false);
    assertSame(result, news);

    if (TEST_FAILING_WILDCARD) {
      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "030+800*0");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_Mix_3() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news5.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);

    Thread.sleep(2000);

    for (int i = 0; i < 255; i++) {
      char c = (char) i;
      if ((c > 32 && c < 48) || // !, ", #, $, %, &, ', (, ), *, +, ,, -, ., /
          (c > 57 && c < 65) || // :, ;, <, =, >, ?, @
          (c > 90 && c < 97) || // [, \, ], ^, _, `
          (c > 122 && c < 127) || // {, |, }, ~
          (String.valueOf(c).equals("§")) //Not part of ASCII //$NON-NLS-1$
      ) {
        String s = Character.toString(c);
        if (s.equals("<") || s.equals(">") || s.equals("?") || s.equals("*") || s.equals("\""))
          continue; //Unsupported

        feed.getNews().get(0).setDescription("The " + s + "startstart and endend" + s + " of middle" + s + "middle is " + s + "betbetween" + s + " and Para" + s + "Glyde.");
        feed.getNews().get(1).setDescription("This is Para" + s + "Market.");
        feed.getNews().get(2).setDescription("And yes the Para" + s + "Baring!");
        DynamicDAO.save(feed);
        waitForIndexer();

        ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        ISearchField descriptionField = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);

        List<ISearchField> fields = new ArrayList<ISearchField>();
        fields.add(allField);
        fields.add(descriptionField);

        for (ISearchField field : fields) {

          /* Without Wildcards */
          ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, s + "startstart");
          List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
          assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

          condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "endend" + s);
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

          condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "middle" + s + "middle");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

          condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, s + "betbetween" + s);
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

          /* With Wildcards */
          if (TEST_FAILING_WILDCARD) {
            condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, s + "sta?tstart");
            result = fModelSearch.searchNews(list(condition), false);
            assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

            condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "end*" + s);
            result = fModelSearch.searchNews(list(condition), false);
            assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

            condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mid?le" + s + "mid*");
            result = fModelSearch.searchNews(list(condition), false);
            assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

            condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, s + "*etb?tween" + s);
            result = fModelSearch.searchNews(list(condition), false);
            assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1);

            condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Par*");
            result = fModelSearch.searchNews(list(condition), false);
            assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1, news2, news3);

            if (s.equals("."))
              System.out.println("jo");//$NON-NLS-1$
            condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Para" + s + "*");
            result = fModelSearch.searchNews(list(condition), false);
            assertSame("Character used: " + s + ", Field: " + field.getName(), result, news1, news2, news3);
          }
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_AttachmentContent() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news5.html", State.READ); //Used to validate count of results == 1

    IAttachment attachment1 = fFactory.createAttachment(null, news1);
    IAttachment attachment2 = fFactory.createAttachment(null, news2);
    IAttachment attachment3 = fFactory.createAttachment(null, news3);

    attachment1.setType("hello.mp3");
    attachment2.setType("hello.doc");
    attachment3.setType("foobar.mp3");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField attachmentField = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(attachmentField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field.getId() == IEntity.ALL_FIELDS) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "hello.mp3");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertEquals(1, result.size());

      condition = fFactory.createSearchCondition(field, specifier, "hello.doc");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news2);

      condition = fFactory.createSearchCondition(field, specifier, "foobar.mp3");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news3);

      condition = fFactory.createSearchCondition(field, specifier, "hello.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "*.mp3");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news3);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithSpecialCharacters_IP() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "127.0.0.1", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "127.0.0.2", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "255.0.0.2", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news5.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField attachmentField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(attachmentField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = SearchSpecifier.CONTAINS_ALL;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "127.0.0.1");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1);

      condition = fFactory.createSearchCondition(field, specifier, "127.0.0.2");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news2);

      condition = fFactory.createSearchCondition(field, specifier, "255.0.0.2");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news3);

      condition = fFactory.createSearchCondition(field, specifier, "127.0.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "*.0.0.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "127.?.?.?");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news2);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsPhraseSearch_SpecialHandlingCategories() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    createNews(feed, "Hello", "http://www.news.com/news2.html", State.READ); //Used to validate count of results == 1
    createNews(feed, "World", "http://www.news.com/news3.html", State.READ); //Used to validate count of results == 1

    ICategory category = fFactory.createCategory(null, news);
    category.setName("Karakas");

    category = fFactory.createCategory(null, news);
    category.setName("Paris Hilton");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField categoriesField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(categoriesField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field.getId() == IEntity.ALL_FIELDS) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Karakas");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news);

      if (field == categoriesField) {
        condition = fFactory.createSearchCondition(field, specifier, "Paris Hilton");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news);
      }

      if (field == allField) {
        condition = fFactory.createSearchCondition(field, specifier, "\"Karakas\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news);

        condition = fFactory.createSearchCondition(field, specifier, "\"Paris Hilton\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Dash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "GNC-2012-12-13 #634 On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This GNC-2010-15-13 #634 On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "GNC-2011-16-13 #634 On the Mend", "http://www.news.com/news3.html", State.READ);
    INews news4 = createNews(feed, "The OAL-Research #634 On the Mend", "http://www.news.com/news4.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news5.html", State.READ); //Used to validate count of results

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "GNC-2012-12-13");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "GNC-2010-15-13");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "\"GNC-2010-15-13\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "GNC-2011-16-13");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        condition = fFactory.createSearchCondition(field, specifier, "GNC-*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

        condition = fFactory.createSearchCondition(field, specifier, "GNC*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

        condition = fFactory.createSearchCondition(field, specifier, "GNC-??12-12-13");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "GNC-??12-*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "GNC-????-*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

        condition = fFactory.createSearchCondition(field, specifier, "G?C-*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

        condition = fFactory.createSearchCondition(field, specifier, "*-2010-*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "OAL-Research");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

        condition = fFactory.createSearchCondition(field, specifier, "\"OAL-Research\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "OAL-Res?arch");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

          condition = fFactory.createSearchCondition(field, specifier, "O?L-Research");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

          condition = fFactory.createSearchCondition(field, specifier, "OAL-*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);
        }

        condition = fFactory.createSearchCondition(field, specifier, "OAL*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "*-Research");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);
        }

        condition = fFactory.createSearchCondition(field, specifier, "*Research");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Dash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    INews news4 = createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("GNC-2012-12-13");
    fFactory.createCategory(null, news2).setName("GNC-2010-15-13");
    fFactory.createCategory(null, news3).setName("GNC-2011-16-13");
    fFactory.createCategory(null, news4).setName("OAL-Research");

    fFactory.createPerson(null, news1).setName("GNC-2012-12-13");
    fFactory.createPerson(null, news2).setName("GNC-2010-15-13");
    fFactory.createPerson(null, news3).setName("GNC-2011-16-13");
    fFactory.createPerson(null, news4).setName("OAL-Research");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "GNC-2012-12-13");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"GNC-2012-12-13\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      condition = fFactory.createSearchCondition(field, specifier, "GNC-2010-15-13");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

      condition = fFactory.createSearchCondition(field, specifier, "GNC-2011-16-13");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

      condition = fFactory.createSearchCondition(field, specifier, "GNC-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "GNC*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "GNC-??12-12-13");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      condition = fFactory.createSearchCondition(field, specifier, "GNC-??12-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      condition = fFactory.createSearchCondition(field, specifier, "GNC-????-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "G?C-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "*-2010-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

      condition = fFactory.createSearchCondition(field, specifier, "OAL-Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "OAL-Res?arch");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "O?L-Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "OAL-*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "OAL*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "*-Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "*Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Dash_Telephone() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "030-800800-20 On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "The 040-800800-20 This On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "On 040-800700-30 the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "030-800800-20");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "040-800800-20");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "040-800700-30");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        condition = fFactory.createSearchCondition(field, specifier, "0?0-800800-*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

        condition = fFactory.createSearchCondition(field, specifier, "040*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2, news3);

        condition = fFactory.createSearchCondition(field, specifier, "040-*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2, news3);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Dollar() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "500$ On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "The 1000$ This On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "On $700 the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "500$");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "1000$");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "$700");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "$7??");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

          condition = fFactory.createSearchCondition(field, specifier, "$7*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

          condition = fFactory.createSearchCondition(field, specifier, "$?00");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Dot() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "127.0.0.1 On the 2.50 Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This 255.0.0.1 On the 3.50 Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "127.5.5.4 On the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "127.0.0.1");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "2.50");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "255.0.0.1");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "127.5.5.4");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        condition = fFactory.createSearchCondition(field, specifier, "127.*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);

        condition = fFactory.createSearchCondition(field, specifier, "127.?.?.1");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "127*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);

        condition = fFactory.createSearchCondition(field, specifier, "127.*.*.*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);

        condition = fFactory.createSearchCondition(field, specifier, "2.5?");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "2*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

        condition = fFactory.createSearchCondition(field, specifier, "?.50");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Dot() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    INews news4 = createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("GNC.2012.12.13");
    fFactory.createCategory(null, news2).setName("GNC.2010.15.13");
    fFactory.createCategory(null, news3).setName("GNC.2011.16.13");
    fFactory.createCategory(null, news4).setName("OAL.Research");

    fFactory.createPerson(null, news1).setName("GNC.2012.12.13");
    fFactory.createPerson(null, news2).setName("GNC.2010.15.13");
    fFactory.createPerson(null, news3).setName("GNC.2011.16.13");
    fFactory.createPerson(null, news4).setName("OAL.Research");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "GNC.2012.12.13");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      condition = fFactory.createSearchCondition(field, specifier, "GNC.2010.15.13");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"GNC.2010.15.13\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);
      }

      condition = fFactory.createSearchCondition(field, specifier, "GNC.2011.16.13");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

      condition = fFactory.createSearchCondition(field, specifier, "GNC.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "GNC*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "GNC.??12.12.13");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      condition = fFactory.createSearchCondition(field, specifier, "GNC.??12.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      condition = fFactory.createSearchCondition(field, specifier, "GNC.????.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "G?C.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

      condition = fFactory.createSearchCondition(field, specifier, "*.2010.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

      condition = fFactory.createSearchCondition(field, specifier, "OAL.Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "OAL.Res?arch");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "O?L.Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "OAL.*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "OAL*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "*.Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);

      condition = fFactory.createSearchCondition(field, specifier, "*Research");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news4);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Paragraph() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "§520 On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This On §525b the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "§6520 On the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "§520");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"§520\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "§*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);

          condition = fFactory.createSearchCondition(field, specifier, "§*20");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);
        }

        condition = fFactory.createSearchCondition(field, specifier, "§525b");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "§525?");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Hash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "#germany On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This #germanies On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "#665 On the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "#germany");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"#germany\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "#germ*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
        }

        condition = fFactory.createSearchCondition(field, specifier, "#665");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        condition = fFactory.createSearchCondition(field, specifier, "\"#665\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "#66?");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

          condition = fFactory.createSearchCondition(field, specifier, "#*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Hash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("#germany");
    fFactory.createCategory(null, news2).setName("#germanies");
    fFactory.createCategory(null, news3).setName("#665");

    fFactory.createPerson(null, news1).setName("#germany");
    fFactory.createPerson(null, news2).setName("#germanies");
    fFactory.createPerson(null, news3).setName("#665");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "#germany");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"#germany\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      condition = fFactory.createSearchCondition(field, specifier, "#germ*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "#665");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"#665\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);
      }

      condition = fFactory.createSearchCondition(field, specifier, "#66?");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

      condition = fFactory.createSearchCondition(field, specifier, "#*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Colon() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "See Karakas: On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This foo:bar construct On the Mend", "http://www.news.com/news2.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Karakas");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"Karakas\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "foo:bar");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "\"foo:bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "Kara*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "foo*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);

        condition = fFactory.createSearchCondition(field, specifier, "*bar");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_AngleBrackets() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("<Foo>");
    fFactory.createCategory(null, news2).setName("<FooBar>");

    fFactory.createPerson(null, news1).setName("<Foo>");
    fFactory.createPerson(null, news2).setName("<FooBar>");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "<Foo>");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"<Foo>\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      condition = fFactory.createSearchCondition(field, specifier, "<Foo*>");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "<Foo*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Pipe() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo|Bar On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Foo|Help On the Mend", "http://www.news.com/news2.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo|Bar");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"Foo|Bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "Foo|*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
        }

        condition = fFactory.createSearchCondition(field, specifier, "Foo*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "*|Bar");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Underline() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo_Bar On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This Foo_Help On the Mend", "http://www.news.com/news2.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo_Bar");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"Foo_Bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "Foo_*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
        }

        condition = fFactory.createSearchCondition(field, specifier, "Foo*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "*_Help");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Underline() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("Foo_Bar");
    fFactory.createCategory(null, news2).setName("Foo_Help");
    fFactory.createCategory(null, news3).setName("Bar_Help");

    fFactory.createPerson(null, news1).setName("Foo_Bar");
    fFactory.createPerson(null, news2).setName("Foo_Help");
    fFactory.createPerson(null, news3).setName("Bar_Help");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo_Bar");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"Foo_Bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      condition = fFactory.createSearchCondition(field, specifier, "Foo_*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "Foo*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "*_Help");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2, news3);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Plus() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo+Bar On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This On Foo+Help the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "On Bar+Help the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo+Bar");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"Foo+Bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "Foo+*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
        }

        condition = fFactory.createSearchCondition(field, specifier, "Foo*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

        condition = fFactory.createSearchCondition(field, specifier, "*Help");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2, news3);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Plus() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("Foo+Bar");
    fFactory.createCategory(null, news2).setName("Foo+Help");
    fFactory.createCategory(null, news3).setName("Bar+Help");

    fFactory.createPerson(null, news1).setName("Foo+Bar");
    fFactory.createPerson(null, news2).setName("Foo+Help");
    fFactory.createPerson(null, news3).setName("Bar+Help");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo+Bar");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"Foo+Bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      condition = fFactory.createSearchCondition(field, specifier, "Foo+*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "Foo*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "*+Help");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2, news3);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Slash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo/Bar On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news3 = createNews(feed, "On Foo/Fighter/Yes the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo/Fighter/Yes");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        condition = fFactory.createSearchCondition(field, specifier, "\"Foo/Fighter/Yes\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "Foo/*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);
        }

        condition = fFactory.createSearchCondition(field, specifier, "Foo*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);

        condition = fFactory.createSearchCondition(field, specifier, "*ighter");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Sash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("Foo/Bar");
    fFactory.createCategory(null, news2).setName("Foo/Help");
    fFactory.createCategory(null, news3).setName("Bar/Help");

    fFactory.createPerson(null, news1).setName("Foo/Bar");
    fFactory.createPerson(null, news2).setName("Foo/Help");
    fFactory.createPerson(null, news3).setName("Bar/Help");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo/Bar");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"Foo/Bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      if (TEST_FAILING_WILDCARD) {
        condition = fFactory.createSearchCondition(field, specifier, "Foo/*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
      }

      condition = fFactory.createSearchCondition(field, specifier, "Foo*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      if (TEST_FAILING_WILDCARD) {
        condition = fFactory.createSearchCondition(field, specifier, "*/Help");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2, news3);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Backslash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo\\Bar On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news3 = createNews(feed, "On Foo\\Fighter\\Yes the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo\\Fighter\\Yes");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        condition = fFactory.createSearchCondition(field, specifier, "\"Foo\\Fighter\\Yes\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "Foo\\*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);
        }

        condition = fFactory.createSearchCondition(field, specifier, "Foo*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);

        condition = fFactory.createSearchCondition(field, specifier, "*ighter");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Backslash() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("Foo\\Bar");
    fFactory.createCategory(null, news2).setName("Foo\\Help");
    fFactory.createCategory(null, news3).setName("Bar\\Help");

    fFactory.createPerson(null, news1).setName("Foo\\Bar");
    fFactory.createPerson(null, news2).setName("Foo\\Help");
    fFactory.createPerson(null, news3).setName("Bar\\Help");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "Foo\\Bar");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"Foo\\Bar\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      if (TEST_FAILING_WILDCARD) {
        condition = fFactory.createSearchCondition(field, specifier, "Foo\\*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
      }

      condition = fFactory.createSearchCondition(field, specifier, "Foo*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      if (TEST_FAILING_WILDCARD) {
        condition = fFactory.createSearchCondition(field, specifier, "*\\Help");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news2, news3);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_At() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "bpasero@bpasero.de On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This foo@bpasero.de On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "On you@me the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "bpasero@bpasero.de");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"bpasero@bpasero.de\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "bpasero@*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "bpasero*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "*bpasero*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

        condition = fFactory.createSearchCondition(field, specifier, "you@me");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news3);

        condition = fFactory.createSearchCondition(field, specifier, "*@bpasero.de");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

        condition = fFactory.createSearchCondition(field, specifier, "*.de");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_At() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("bpasero@bpasero.de");
    fFactory.createCategory(null, news2).setName("foo@bpasero.de");
    fFactory.createCategory(null, news3).setName("bpasero@foo.de");

    fFactory.createPerson(null, news1).setName("bpasero@bpasero.de");
    fFactory.createPerson(null, news2).setName("foo@bpasero.de");
    fFactory.createPerson(null, news3).setName("bpasero@foo.de");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "bpasero@bpasero.de");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"bpasero@bpasero.de\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      condition = fFactory.createSearchCondition(field, specifier, "bpasero@*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);

      condition = fFactory.createSearchCondition(field, specifier, "bpasero*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news3);

      condition = fFactory.createSearchCondition(field, specifier, "*@bpasero.de");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "*.de");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Percentage() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "20% On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This 200% On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "8000% On the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "20%");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"20%\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "2*%");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "*%");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Parenthesis() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "(Foobar) On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "(Footest) This On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "On (Startest) the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "(Foobar)");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"(Foobar)\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "(Foo*)");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "(F*)");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "(F*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "(*)");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Brackets() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "[Foobar] On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "[Footest] This On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "On [Startest] the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "[Foobar]");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"[Foobar]\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "[Foo*]");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "[F*]");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "[F*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "[*]");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CategoriesAuthors_Brackets() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "Bar", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "Hello World", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Stuff", "http://www.news.com/news5.html", State.READ);

    fFactory.createCategory(null, news1).setName("[Foobar]");
    fFactory.createCategory(null, news2).setName("[Footest]");
    fFactory.createCategory(null, news3).setName("[Startest]");

    fFactory.createPerson(null, news1).setName("[Foobar]");
    fFactory.createPerson(null, news2).setName("[Footest]");
    fFactory.createPerson(null, news3).setName("[Startest]");

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField categoryField = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(categoryField);
    fields.add(authorField);

    for (ISearchField field : fields) {
      SearchSpecifier specifier = (field == authorField) ? SearchSpecifier.CONTAINS_ALL : SearchSpecifier.IS;

      ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "[Foobar]");
      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

      if (specifier != SearchSpecifier.IS) {
        condition = fFactory.createSearchCondition(field, specifier, "\"[Foobar]\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);
      }

      condition = fFactory.createSearchCondition(field, specifier, "[Foo*]");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "[F*]");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "[F*");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

      condition = fFactory.createSearchCondition(field, specifier, "[*]");
      result = fModelSearch.searchNews(list(condition), false);
      assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_CurvedBrackets() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "{Foobar} On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "{Footest} This On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "On {Startest} the Mend", "http://www.news.com/news3.html", State.READ);
    createNews(feed, "Anything Else", "http://www.news.com/news4.html", State.READ); //Used to validate count of results == 1

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);

    List<ISearchField> fields = new ArrayList<ISearchField>();
    fields.add(allField);
    fields.add(titleField);

    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    specifiers.add(SearchSpecifier.CONTAINS_ALL);
    specifiers.add(SearchSpecifier.CONTAINS);

    for (ISearchField field : fields) {
      for (SearchSpecifier specifier : specifiers) {

        ISearchCondition condition = fFactory.createSearchCondition(field, specifier, "{Foobar}");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        condition = fFactory.createSearchCondition(field, specifier, "\"{Foobar}\"");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1);

        if (TEST_FAILING_WILDCARD) {
          condition = fFactory.createSearchCondition(field, specifier, "{Foo*}");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "{F*}");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "{F*");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2);

          condition = fFactory.createSearchCondition(field, specifier, "{*}");
          result = fModelSearch.searchNews(list(condition), false);
          assertSame("Field: " + field.getName() + ", Specifier: " + specifier, result, news1, news2, news3);
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsRealWorld_Mix() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "GNC-2012-12-13 #634 On the Mend", "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, "This GNC-2010-15-13 #634 On the Mend", "http://www.news.com/news2.html", State.READ);
    INews news3 = createNews(feed, "GNC-2011-16-13 #634 On the Mend", "http://www.news.com/news3.html", State.UNREAD);
    INews news4 = createNews(feed, "The OAL-Research #634 On the Mend", "http://www.news.com/news4.html", State.READ);
    INews news5 = createNews(feed, "Anything Else", "http://www.news.com/news5.html", State.UPDATED); //Used to validate count of results

    DynamicDAO.save(feed);
    waitForIndexer();

    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
    ISearchField stateField = fFactory.createSearchField(INews.STATE, fNewsEntityName);

    /* Cond 1 AND Cond 2 */
    ISearchCondition condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    ISearchCondition condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
    assertSame(result, news1, news2);

    /* Cond 1 OR Cond 2 */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), false);
    assertSame(result, news1, news2, news3, news4);

    /* Cond 1 NOT Cond 2 */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS_NOT, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), true);
    assertSame(result, news3);

    /* NOT Cond 1 AND Cond 2 */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_NOT, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), true);
    assertSame(result, news4);

    /* NOT Cond 1 OR Cond 2 */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_NOT, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), false);
    assertSame(result, news1, news2, news4, news5);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithScopeCondition() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");
    IFolder child = fFactory.createFolder(null, root, "Child");

    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed.com/feed2.xml"));
    INews news1 = createNews(feed, "GNC-2012-12-13 #634 On the Mend", "http://www.news.com/news1.html", State.READ);
    news1.setFlagged(true);
    INews news2 = createNews(feed, "This GNC-2010-15-13 #634 On the Mend", "http://www.news.com/news2.html", State.READ);
    news2.setReceiveDate(new Date(0));
    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Foo"));
    news2.addLabel(label);
    INews news3 = createNews(feed, "GNC-2011-16-13 #634 On the Mend", "http://www.news.com/news3.html", State.UNREAD);
    news3.setFlagged(true);
    INews news4 = createNews(feed, "The OAL-Research #634 On the Mend", "http://www.news.com/news4.html", State.READ);
    label = DynamicDAO.save(fFactory.createLabel(null, "Bar"));
    news4.addLabel(label);
    createNews(feed, "Anything Else", "http://www.news.com/news5.html", State.UPDATED); //Used to validate count of results

    IBookMark mark1 = fFactory.createBookMark(null, child, new FeedLinkReference(feed.getLink()), "Bookmark");
    IBookMark mark2 = fFactory.createBookMark(null, child, new FeedLinkReference(feed2.getLink()), "Bookmark 2");

    DynamicDAO.save(feed);
    DynamicDAO.save(feed2);
    DynamicDAO.save(root);
    waitForIndexer();

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
    ISearchField titleField = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
    ISearchField stateField = fFactory.createSearchField(INews.STATE, fNewsEntityName);

    ISearchField stickyField = fFactory.createSearchField(INews.IS_FLAGGED, fNewsEntityName);
    ISearchCondition stickyCondition = fFactory.createSearchCondition(stickyField, SearchSpecifier.IS, true);

    ISearchField labelField = fFactory.createSearchField(INews.LABEL, fNewsEntityName);
    ISearchCondition labelCondition = fFactory.createSearchCondition(labelField, SearchSpecifier.IS, "*");

    ISearchField ageField = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsEntityName);
    ISearchCondition ageCondition = fFactory.createSearchCondition(ageField, SearchSpecifier.IS_LESS_THAN, 5);

    /* Scope Condition: Is Sticky (AND) */
    ISearchCondition condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    ISearchCondition condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
    assertSame(result, news1, news2);
    result = fModelSearch.searchNews(list(condition1, condition2), stickyCondition, true);
    assertSame(result, news1);

    /* Scope Condition: Is Sticky (AND, All Fields) */
    condition1 = fFactory.createSearchCondition(allField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), true);
    assertSame(result, news1, news2);
    result = fModelSearch.searchNews(list(condition1, condition2), stickyCondition, true);
    assertSame(result, news1);

    /* Scope Condition: Is Labeled (AND) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), true);
    assertSame(result, news1, news2);
    result = fModelSearch.searchNews(list(condition1, condition2), labelCondition, true);
    assertSame(result, news2);

    /* Scope Condition: Recent Age (AND) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), true);
    assertSame(result, news1, news2);
    result = fModelSearch.searchNews(list(condition1, condition2), ageCondition, true);
    assertSame(result, news1);

    /* Scope Condition: Is Sticky (OR) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), stickyCondition, false);
    assertSame(result, news1, news3);

    /* Scope Condition: Is Sticky (OR, All Fields) */
    condition1 = fFactory.createSearchCondition(allField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), stickyCondition, false);
    assertSame(result, news1, news3);

    /* Scope Condition: Is Labeled (OR) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), labelCondition, false);
    assertSame(result, news2, news4);

    /* Scope Condition: Recent Age (OR) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(condition1, condition2), ageCondition, false);
    assertSame(result, news1, news3, news4);

    /* Scope Condition: Is Sticky (AND, with Location) */
    ISearchCondition conditionMatch = fFactory.createSearchCondition(locationField, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Collections.singleton((IFolderChild) mark1)));
    ISearchCondition conditionNoMatch = fFactory.createSearchCondition(locationField, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Collections.singleton((IFolderChild) mark2)));
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), stickyCondition, true);
    assertSame(result, news1);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());

    /* Scope Condition: Is Sticky (AND, with Location, all fields) */
    condition1 = fFactory.createSearchCondition(allField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), stickyCondition, true);
    assertSame(result, news1);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());

    /* Scope Condition: Is Labeled (AND, with Location) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), labelCondition, true);
    assertSame(result, news2);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());

    /* Scope Condition: Recent Age (AND, with Location) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), ageCondition, true);
    assertSame(result, news1);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());

    /* Scope Condition: Is Sticky (OR, with Location) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), stickyCondition, false);
    assertSame(result, news1, news3);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());

    /* Scope Condition: Is Sticky (OR, with Location, all fields) */
    condition1 = fFactory.createSearchCondition(allField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), stickyCondition, false);
    assertSame(result, news1, news3);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());

    /* Scope Condition: Is Labeled (OR, with Location) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), labelCondition, false);
    assertSame(result, news2, news4);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());

    /* Scope Condition: Recent Age (OR, with Location) */
    condition1 = fFactory.createSearchCondition(titleField, SearchSpecifier.CONTAINS_ALL, "GNC-*");
    condition2 = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.READ));
    result = fModelSearch.searchNews(list(conditionMatch, condition1, condition2), ageCondition, false);
    assertSame(result, news1, news3, news4);
    result = fModelSearch.searchNews(list(conditionNoMatch, condition1, condition2), stickyCondition, true);
    assertTrue(result.isEmpty());
  }
}
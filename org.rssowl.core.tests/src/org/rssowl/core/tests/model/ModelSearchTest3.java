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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
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
import java.util.EnumSet;
import java.util.List;

/**
 * Test searching types from the persistence layer.
 *
 * @author bpasero
 */
public class ModelSearchTest3 extends AbstractModelSearchTest {

  /**
   * @throws Exception
   */
  @Test
  public void testSearchNewsWithBINLocation() throws Exception {

    /* First add some Types */
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFolder subFolder = fFactory.createFolder(null, rootFolder, "Sub Folder");
    DynamicDAO.save(subFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.testSearchNewsWithLocationFeed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.testSearchNewsWithLocationFeed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.testSearchNewsWithLocationFeed3.com"));

    INews news1 = createNews(feed1, "First News of Feed One", "http://www.news.com/news1.html", State.UNREAD);
    INews news2 = createNews(feed1, "Second News of Feed One", "http://www.news.com/news2.html", State.NEW);

    INews news3 = createNews(feed2, "First News of Feed Two", "http://www.news.com/news3.html", State.READ);
    INews news4 = createNews(feed2, "Second News of Feed Two", "http://www.news.com/news4.html", State.NEW);

    INews news5 = createNews(feed3, "First News of Feed Three", "http://www.news.com/news5.html", State.UPDATED);
    INews news6 = createNews(feed3, "Second News of Feed Three", "http://www.news.com/news6.html", State.NEW);

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    INewsBin rootBin = fFactory.createNewsBin(null, rootFolder, "Root Bin");
    INewsBin subRootBin = fFactory.createNewsBin(null, subFolder, "Sub Root Bin");

    DynamicDAO.save(rootFolder);
    List<INews> copiedNews = new ArrayList<INews>();
    INews news1Copy = fFactory.createNews(news1, rootBin);
    copiedNews.add(news1Copy);
    INews news2Copy = fFactory.createNews(news2, rootBin);
    copiedNews.add(news2Copy);

    INews news3CopyRoot = fFactory.createNews(news3, rootBin);
    copiedNews.add(news3CopyRoot);
    INews news3CopySubRoot = fFactory.createNews(news3, subRootBin);
    copiedNews.add(news3CopySubRoot);

    INews news4Copy = fFactory.createNews(news4, subRootBin);
    copiedNews.add(news4Copy);
    INews news5Copy = fFactory.createNews(news5, subRootBin);
    copiedNews.add(news5Copy);
    INews news6Copy = fFactory.createNews(news6, subRootBin);
    copiedNews.add(news6Copy);

    DynamicDAO.saveAll(copiedNews);
    DynamicDAO.save(rootBin);
    DynamicDAO.save(subRootBin);

    /* Wait for Indexer */
    waitForIndexer();

    /* Location IS Root Folder */
    {
      ISearchField field1 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootFolder })));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), true);
      assertSame(result, news1Copy, news2Copy, news3CopyRoot, news3CopySubRoot, news4Copy, news5Copy, news6Copy);
    }

    /* Location IS Sub Folder */
    {
      ISearchField field1 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subFolder })));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), true);
      assertSame(result, news3CopySubRoot, news4Copy, news5Copy, news6Copy);
    }

    /* Location IS Root Bin */
    {
      ISearchField field1 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootBin })));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), true);
      assertSame(result, news1Copy, news2Copy, news3CopyRoot);
    }

    /* Location IS Root Bin or Sub Root Bin */
    {
      ISearchField field1 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootBin })));

      ISearchField field2 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond2 = fFactory.createSearchCondition(field2, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subRootBin })));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), false);
      assertSame(result, news1Copy, news2Copy, news3CopyRoot, news3CopySubRoot, news4Copy, news5Copy, news6Copy);
    }

    /* Location IS (Root Bin, Sub Root Bin) */
    {
      ISearchField field1 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { rootBin, subRootBin })));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), false);
      assertSame(result, news1Copy, news2Copy, news3CopyRoot, news3CopySubRoot, news4Copy, news5Copy, news6Copy);
    }

    /* Location IS Sub Folder AND State is new */
    {
      ISearchField field1 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { subFolder })));

      ISearchField field2 = fFactory.createSearchField(INews.STATE, fNewsEntityName);
      ISearchCondition cond2 = fFactory.createSearchCondition(field2, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1, cond2), true);
      assertSame(result, news4Copy, news6Copy);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchEntireNewsWith_CONTAINS_Specifier() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "This is Radio no (DVD)", "http://www.news.com/news1.html", State.READ);

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer Radio no (DVD) description with <html><h2>included!</h2></html>");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* All Fields */
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "(DVD)");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news2);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "(DVD)");
      ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "no");

      result = fModelSearch.searchNews(list(condition1, condition2), true);
      assertSame(result, news1, news2);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "(DVD)");
      condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "no");
      ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "RadIO");

      result = fModelSearch.searchNews(list(condition1, condition2, condition3), true);
      assertSame(result, news1, news2);
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchEntireNewsWithWildcards() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      /* Title */
      INews news1 = createNews(feed, "Benjamin Pasero", "http://www.news.com/news1.html", State.NEW);

      /* Description */
      INews news2 = createNews(feed, "Michael Jordan", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer name like Benjamin Pasero.");

      /* Author */
      INews news3 = createNews(feed, "Jordan Kinsey", "http://www.news.com/news3.html", State.NEW);
      IPerson author = fFactory.createPerson(null, news3);
      author.setName("Benjamin Pasero");

      /* Category */
      INews news4 = createNews(feed, "McDonalds", "http://www.news.com/news4.html", State.NEW);
      ICategory category = fFactory.createCategory(null, news4);
      category.setName("Benjamin Pasero");

      /* Attachment Content */
      INews news5 = createNews(feed, "McFlurry", "http://www.news.com/news5.html", State.NEW);
      IAttachment attachment = fFactory.createAttachment(null, news5);
      attachment.setLink(new URI("http://www.attachment.com/att1news2.file"));
      attachment.setType("Benjamin Pasero");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* All Fields */
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Benjamin Pasero");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news2, news3, news4, news5);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Benjamin P*");

      result = fModelSearch.searchNews(list(condition1), false);
      assertSame(result, news1, news2, news3, news4, news5);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Benjamin Pa?e*o");

      result = fModelSearch.searchNews(list(condition1), false);
      assertSame(result, news1, news2, news3, news4, news5);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Ben* Paser?");

      result = fModelSearch.searchNews(list(condition1), false);
      assertSame(result, news1, news2, news3, news4, news5);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "?enjamin ?asero");

      result = fModelSearch.searchNews(list(condition1), false);
      assertSame(result, news1, news2, news3, news4, news5);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "B* P*");

      result = fModelSearch.searchNews(list(condition1), false);
      assertSame(result, news1, news2, news3, news4, news5);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*e?j?mi? *a?e?*");

      result = fModelSearch.searchNews(list(condition1), false);
      assertSame(result, news1, news2, news3, news4, news5);

    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchEntireNewsWith_CONTAINS_ALL_Specifier() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "This is Radio no (DVD)", "http://www.news.com/news1.html", State.READ);

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer Radio no (DVD) description with <html><h2>included!</h2></html>");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* All Fields */
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "(DVD)");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1, news2);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "(DVD) description included");

      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news2);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "(DVD)");
      ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "no");

      result = fModelSearch.searchNews(list(condition1, condition2), true);
      assertSame(result, news1, news2);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "(DVD) no");

      result = fModelSearch.searchNews(list(condition1), true);
      assertSame(result, news1, news2);

      field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "(DVD)");
      condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "no");
      ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "RadIO");

      result = fModelSearch.searchNews(list(condition1, condition2, condition3), true);
      assertSame(result, news1, news2);
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testEmptyFolderLocationCondition() throws Exception {

    /* First add some Types */
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFolder subFolder = fFactory.createFolder(null, rootFolder, "Sub Folder");
    DynamicDAO.save(subFolder);

    IFolder emptyFolder = fFactory.createFolder(null, rootFolder, "Empty Folder");
    DynamicDAO.save(emptyFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.testSearchNewsWithLocationFeed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.testSearchNewsWithLocationFeed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.testSearchNewsWithLocationFeed3.com"));

    createNews(feed1, "First News of Feed One", "http://www.news.com/news1.html", State.UNREAD);
    createNews(feed1, "Second News of Feed One", "http://www.news.com/news2.html", State.NEW);

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark rootMark1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "rootMark1");
    DynamicDAO.save(rootMark1);

    IBookMark subRootMark1 = fFactory.createBookMark(null, subFolder, new FeedLinkReference(feed2.getLink()), "subRootMark1");
    DynamicDAO.save(subRootMark1);

    IBookMark subRootMark2 = fFactory.createBookMark(null, subFolder, new FeedLinkReference(feed3.getLink()), "subRootMark2");
    DynamicDAO.save(subRootMark2);

    /* Wait for Indexer */
    waitForIndexer();

    /* Location IS Empty Folder */
    {
      ISearchField field1 = fFactory.createSearchField(INews.LOCATION, fNewsEntityName);
      ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { emptyFolder })));

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(cond1), true);
      assertEquals(0, result.size());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSearchEntireNewsAllCases() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      /* Title */
      INews news1 = createNews(feed, "Johnny Depp", "http://www.news.com/news1.html", State.NEW);

      /* Description */
      INews news2 = createNews(feed, "News2", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer name like Michael Jackson.");

      /* Author */
      INews news3 = createNews(feed, "News3", "http://www.news.com/news3.html", State.NEW);
      IPerson author = fFactory.createPerson(null, news3);
      author.setName("Arnold Schwarzenegger");

      /* Category */
      INews news4 = createNews(feed, "News4", "http://www.news.com/news4.html", State.NEW);
      ICategory category = fFactory.createCategory(null, news4);
      category.setName("Roberts");

      /* Attachment Content */
      INews news5 = createNews(feed, "News5", "http://www.news.com/news5.html", State.NEW);
      IAttachment attachment = fFactory.createAttachment(null, news5);
      attachment.setLink(new URI("http://www.attachment.com/att1news2.file"));
      attachment.setType("Hasselhoff");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* All Fields */
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);

      /* Contains Any - Single Condition */
      {
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Johnny Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Jo?nny D*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny arnold michael schwarzenegger roberts hasselhoff");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny ar?old mic?ael schw?rzenegger rob?rts hasselh?ff");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh* arn*d mi*el sch* *rts *elh?ff");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains Any - Multi Condition (require all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts schwarzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts schw?rzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh* sch*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains Any - Multi Condition (require all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts schwarzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts schw?rzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh* sch*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4), true);
        assertTrue(result.isEmpty());
      }

      /* Contains All - Single Condition */
      {
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Johnny Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jo?nny D*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "johnny arnold michael schwarzenegger roberts hasselhoff");
        result = fModelSearch.searchNews(list(condition), false);
        assertTrue(result.isEmpty());

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "jo?nny ar?old mic?ael schw?rzenegger rob?rts hasselh?ff");
        result = fModelSearch.searchNews(list(condition), false);
        assertTrue(result.isEmpty());

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "joh* arn*d mi*el sch* *rts *elh?ff");
        result = fModelSearch.searchNews(list(condition), false);
        assertTrue(result.isEmpty());

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael Jackson");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "M?ch?el *ckson");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a longer name like Michael Jackson");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a lo?ger name like Mic*el Jack*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Arnold Schwarzenegger");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old Schwarz*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news4);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "r?ber*ts");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news4);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Hasselhoff");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "h?ssel*ff");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);
      }

      /* Contains All - Multi Condition (required all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*elh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "M?ch?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*ckson");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        ISearchCondition condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        ISearchCondition condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "n?me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich*");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "J?ckson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Th?s is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "l*ke");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jac?son");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger n*me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich* *son");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7), false);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarzenegger");

        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarz*");

        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news4");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");

        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news4);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "h?ssel*ff");

        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news5);
      }

      /* Contains All - Multi Condition (required all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*elh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "M?ch?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*ckson");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        ISearchCondition condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        ISearchCondition condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "n?me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich*");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "J?ckson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "l*ke");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jac?son");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger n*me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich* *son");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarzenegger");

        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarz*");

        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news4");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");

        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news4);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "h?ssel*ff");

        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news5);
      }

      /* Contains Not - Single Condition */
      {
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Johnny Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny D*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Michael Jackson");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Mich?el Jack*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arnold Schwarzenegger");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arn* Sch?arzen?gger");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news4, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Roberts");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "r*b?rts");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news5);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Hasselhoff");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "has?elh*");
        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "johnny arnold michael schwarzenegger roberts hasselhoff");
        result = fModelSearch.searchNews(list(condition), false);
        assertTrue(result.isEmpty());

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jo?nny ar?old mic?ael schw?rzenegger rob?rts hasselh?ff");
        result = fModelSearch.searchNews(list(condition), false);
        assertTrue(result.isEmpty());

        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "joh* arn*d mi*el sch* *rts *elh?ff");
        result = fModelSearch.searchNews(list(condition), false);
        assertTrue(result.isEmpty());
      }

      /* Contains Not - Multi Condition (required all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Mich?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jack*");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Schwarzenegger");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news2, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arn*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Sch?arzen?gger");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news2, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Roberts");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "R?be*s");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Ha?se*");
        result = fModelSearch.searchNews(list(condition1, condition2), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arnold");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains Not - Multi Condition (required all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3), true);
        assertSame(result, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Mich?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jack*");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Schwarzenegger");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1, news2, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arn*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Sch?arzen?gger");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1, news2, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Roberts");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1, news2, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "R?be*s");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Ha?se*");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1, news2, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arnold");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6), true);
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
  public void testSearchEntireNewsAllCasesWithStateCondition() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      /* Title */
      INews news1 = createNews(feed, "Johnny Depp", "http://www.news.com/news1.html", State.NEW);

      /* Description */
      INews news2 = createNews(feed, "News2", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer name like Michael Jackson.");

      /* Author */
      INews news3 = createNews(feed, "News3", "http://www.news.com/news3.html", State.NEW);
      IPerson author = fFactory.createPerson(null, news3);
      author.setName("Arnold Schwarzenegger");

      /* Category */
      INews news4 = createNews(feed, "News4", "http://www.news.com/news4.html", State.NEW);
      ICategory category = fFactory.createCategory(null, news4);
      category.setName("Roberts");

      /* Attachment Content */
      INews news5 = createNews(feed, "News5", "http://www.news.com/news5.html", State.NEW);
      IAttachment attachment = fFactory.createAttachment(null, news5);
      attachment.setLink(new URI("http://www.attachment.com/att1news2.file"));
      attachment.setType("Hasselhoff");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* All Fields */
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchField stateField = fFactory.createSearchField(INews.STATE, fNewsEntityName);
      ISearchCondition stateCondition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      /* Contains Any - Multi Condition (require all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains Any - Multi Condition (require all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts schwarzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts schw?rzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh* sch*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, stateCondition), true);
        assertTrue(result.isEmpty());
      }

      /* Contains All - Multi Condition (required all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains All - Multi Condition (required all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2), true);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*elh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "M?ch?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*ckson");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        ISearchCondition condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        ISearchCondition condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "n?me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich*");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "J?ckson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "l*ke");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jac?son");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger n*me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich* *son");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7, stateCondition), true);
        assertSame(result, news2);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarzenegger");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarz*");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news4");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news4);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "h?ssel*ff");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news5);
      }

      /* Contains Not - Multi Condition (required all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arnold");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains Not - Multi Condition (required all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, stateCondition), true);
        assertSame(result, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Mich?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Schwarzenegger");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1, news2, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arn*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Sch?arzen?gger");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1, news2, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Roberts");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1, news2, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "R?be*s");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Ha?se*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertSame(result, news1, news2, news3);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arnold");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
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
  public void testSearchEntireNewsAllCasesWithNegationStateCondition() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      /* Title */
      INews news1 = createNews(feed, "Johnny Depp", "http://www.news.com/news1.html", State.NEW);

      /* Description */
      INews news2 = createNews(feed, "News2", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer name like Michael Jackson.");

      /* Author */
      INews news3 = createNews(feed, "News3", "http://www.news.com/news3.html", State.NEW);
      IPerson author = fFactory.createPerson(null, news3);
      author.setName("Arnold Schwarzenegger");

      /* Category */
      INews news4 = createNews(feed, "News4", "http://www.news.com/news4.html", State.NEW);
      ICategory category = fFactory.createCategory(null, news4);
      category.setName("Roberts");

      /* Attachment Content */
      INews news5 = createNews(feed, "News5", "http://www.news.com/news5.html", State.NEW);
      IAttachment attachment = fFactory.createAttachment(null, news5);
      attachment.setLink(new URI("http://www.attachment.com/att1news2.file"));
      attachment.setType("Hasselhoff");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* All Fields */
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
      ISearchField stateField = fFactory.createSearchField(INews.STATE, fNewsEntityName);
      ISearchCondition stateCondition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS_NOT, EnumSet.of(INews.State.NEW));

      /* Contains Any - Multi Condition (require all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains Any - Multi Condition (require all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "johnny arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "michael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "roberts schwarzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "jo?nny ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mic?ael");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "rob?rts schw?rzenegger");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "joh* sch*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*rts mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, stateCondition), true);
        assertTrue(result.isEmpty());
      }

      /* Contains All - Multi Condition (required all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news1);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains All - Multi Condition (required all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arnold");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "hasselh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*elh?ff");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "M?ch?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*ckson");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        ISearchCondition condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        ISearchCondition condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jackson");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael Jackson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "longer");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "n?me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich*");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "J?ckson");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This is a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "name");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "l*ke");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Michael");
        condition8 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Jac?son");

        result = fModelSearch.searchNews(list(condition1, condition4, condition5, condition6, condition7, condition8, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "This");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "is");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "a");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "lo?ger n*me");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "like");
        condition7 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Mich* *son");

        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition6, condition7, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarzenegger");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ar?old");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Schwarz*");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news4");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "roberts");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Hasselhoff");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "news5");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "h?ssel*ff");

        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());
      }

      /* Contains Not - Multi Condition (required all: false) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), false);
        assertSame(result, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arnold");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Contains Not - Multi Condition (required all: true) */
      {
        ISearchCondition condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Johnny");
        ISearchCondition condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Depp");
        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "D*");
        ISearchCondition condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Michael");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jackson");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Mich?el");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Jack*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arnold");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Schwarzenegger");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Arn*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Sch?arzen?gger");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Roberts");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "R?be*s");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Ha?se*");
        result = fModelSearch.searchNews(list(condition1, condition2, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "johnny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arnold");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "michael");
        ISearchCondition condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schwarzenegger");
        ISearchCondition condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "roberts");
        ISearchCondition condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselhoff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "jo?nny");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "ar?old");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mic?ael");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "schw?rzenegger");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "rob?rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "hasselh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());

        condition1 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "joh*");
        condition2 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "arn*d");
        condition3 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "mi*el");
        condition4 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "sch*");
        condition5 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*rts");
        condition6 = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*elh?ff");
        result = fModelSearch.searchNews(list(condition1, condition2, condition3, condition4, condition5, condition6, stateCondition), true);
        assertTrue(result.isEmpty());
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }
}
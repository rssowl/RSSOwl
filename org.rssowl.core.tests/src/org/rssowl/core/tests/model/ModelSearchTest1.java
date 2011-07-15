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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISource;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.tests.TestUtils;
import org.rssowl.core.util.SearchHit;

import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * Test searching types from the persistence layer.
 *
 * @author bpasero
 */
public class ModelSearchTest1 extends AbstractModelSearchTest {

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchNewsWith_IS_Specifier() throws Exception {
    try {
      Calendar cal = Calendar.getInstance();

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
      ICategory news1cat1 = fFactory.createCategory(null, news1);
      news1cat1.setName("apple");
      ILabel label1 = fFactory.createLabel(null, "work");
      news1.addLabel(label1);
      IAttachment att1news1 = fFactory.createAttachment(null, news1);
      att1news1.setLink(new URI("http://www.attachment.com/att1news1.file"));
      att1news1.setType("bin/mp3");

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      news2.setRating(10);
      ICategory news2cat1 = fFactory.createCategory(null, news2);
      news2cat1.setName("apple");
      ICategory news2cat2 = fFactory.createCategory(null, news2);
      news2cat2.setName("windows");
      ILabel label2 = fFactory.createLabel(null, "todo");
      news2.addLabel(label2);
      IAttachment att1news2 = fFactory.createAttachment(null, news2);
      att1news2.setLink(new URI("http://www.attachment.com/att1news2.file"));
      att1news2.setType("bin/doc");
      IAttachment att2news2 = fFactory.createAttachment(null, news2);
      att2news2.setLink(new URI("http://www.attachment.com/att2news2.file"));
      att2news2.setType("bin/wav");
      cal.setTimeInMillis(System.currentTimeMillis() - DAY);
      news2.setPublishDate(cal.getTime());

      INews news3 = createNews(feed, "Foo Bar", "http://www.news.com/news3.html", State.NEW);
      IPerson author3 = fFactory.createPerson(null, news3);
      author3.setName("Benjamin Pasero");
      ICategory news3cat1 = fFactory.createCategory(null, news3);
      news3cat1.setName("apple");
      ICategory news3cat2 = fFactory.createCategory(null, news3);
      news3cat2.setName("windows");
      ICategory news3cat3 = fFactory.createCategory(null, news3);
      news3cat3.setName("slashdot");
      cal.setTimeInMillis(System.currentTimeMillis() - 5 * DAY);
      news3.setModifiedDate(cal.getTime());
      cal.setTimeInMillis(System.currentTimeMillis() - 10 * DAY);
      news3.setPublishDate(cal.getTime());

      INews news4 = createNews(feed, null, "http://www.news.com/news4.html", State.UPDATED);
      Date news4Date = new Date(1000000);
      news4.setPublishDate(news4Date);
      IPerson author4 = fFactory.createPerson(null, news4);
      author4.setName("Pasero");
      ISource source4 = fFactory.createSource(news4);
      source4.setLink(new URI("http://www.source.com"));

      INews news5 = createNews(feed, null, "http://www.news.com/news5.html", State.NEW);
      news5.setFlagged(true);
      IPerson author5 = fFactory.createPerson(null, news5);
      author5.setEmail(new URI("test@rssowl.org"));
      ISource source5 = fFactory.createSource(news5);
      source5.setName("Source for News 5");
      ICategory news5cat1 = fFactory.createCategory(null, news5);
      news5cat1.setName("Apache Lucene");
      ICategory news5cat2 = fFactory.createCategory(null, news5);
      news5cat2.setName("Java");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* Condition 1a: Enum (match) */
      {
        ISearchField field = fFactory.createSearchField(INews.STATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(State.READ));

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);
      }

      /* Condition 1b: Enum (no match) */
      {
        ISearchField field = fFactory.createSearchField(INews.STATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(State.DELETED));

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }

      /* Condition 2a: Integer (match) */
      {

        /* Rating */
        ISearchField field = fFactory.createSearchField(INews.RATING, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, 10);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);

        /* Age in Days */
        field = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, 0);

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news5);

        field = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, 5);

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, 1);

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2);
      }

      /* Condition 2b: Integer (no match) */
      {

        /* Rating */
        ISearchField field = fFactory.createSearchField(INews.RATING, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, 15);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Age in Days */
        field = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, 100);

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, 8);

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }

      /* Condition 3a: String (match) */
      {

        /* Categories */
        ISearchField field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "apple");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "windows");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "slash*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "a*le");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "apache lucene");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        /* Labels */
        field = fFactory.createSearchField(INews.LABEL, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, label1.getName());

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        /* Source Name */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "Source for News 5");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);
      }

      /* Condition 3b: String (no match) */
      {

        /* Author */
        ISearchField field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "Pasero Benjamin");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Categories */
        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "apple slashdot");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "sleshdod");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "lucene apache");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Source Name */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "Source for");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }

      /* Condition 4a: Date (match) */
      {
        ISearchField field = fFactory.createSearchField(INews.PUBLISH_DATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, news4Date);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news4);
      }

      /* Condition 4b: Date (no match) */
      {
        Date wrongDate = new Date();
        ISearchField field = fFactory.createSearchField(INews.PUBLISH_DATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, wrongDate);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }

      /* Condition 5a: Boolean (one match) */
      {
        ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, true);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        /* Attachments */
        field = fFactory.createSearchField(INews.HAS_ATTACHMENTS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, true);

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2);
      }

      /* Condition 5b: Boolean (other matches) */
      {
        ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, false);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        /* Attachments */
        field = fFactory.createSearchField(INews.HAS_ATTACHMENTS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, false);

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4, news5);
      }

      /* Condition 6a: Link (match) */
      {

        /* News Link */
        ISearchField field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.news.com/news1.html");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.news.com/news?.html");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "*www.news.com/news1.html");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        /* Source Link */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.source.com");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news4);

        /* Feed Link */
        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.feed.com/feed.xml");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.feed.com/*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Condition 6b: Link (no match) */
      {

        /* News Link */
        ISearchField field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.news.com/news6.html");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.news.com/news?");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "*www.news.com/");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Source Link */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.othersource.com");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Feed Link */
        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.feed.com/feed2.xml");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, "http://www.otherfeed.com/*");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchNewsWith_IS_NOT_Specifier() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
      ICategory news1cat1 = fFactory.createCategory(null, news1);
      news1cat1.setName("apple");
      ILabel label1 = fFactory.createLabel(null, "work");
      news1.addLabel(label1);
      IAttachment att1news1 = fFactory.createAttachment(null, news1);
      att1news1.setLink(new URI("http://www.attachment.com/att1news1.file"));
      att1news1.setType("bin/mp3");

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      news2.setRating(10);
      ICategory news2cat1 = fFactory.createCategory(null, news2);
      news2cat1.setName("apple");
      ICategory news2cat2 = fFactory.createCategory(null, news2);
      news2cat2.setName("windows");
      ILabel label2 = fFactory.createLabel(null, "todo");
      news2.addLabel(label2);
      IAttachment att1news2 = fFactory.createAttachment(null, news2);
      att1news2.setLink(new URI("http://www.attachment.com/att1news2.file"));
      att1news2.setType("bin/doc");
      IAttachment att2news2 = fFactory.createAttachment(null, news2);
      att2news2.setLink(new URI("http://www.attachment.com/att2news2.file"));
      att2news2.setType("bin/wav");

      INews news3 = createNews(feed, "Foo Bar", "http://www.news.com/news3.html", State.NEW);
      IPerson author3 = fFactory.createPerson(null, news3);
      author3.setName("Benjamin Pasero");
      ICategory news3cat1 = fFactory.createCategory(null, news3);
      news3cat1.setName("apple");
      ICategory news3cat2 = fFactory.createCategory(null, news3);
      news3cat2.setName("windows");
      ICategory news3cat3 = fFactory.createCategory(null, news3);
      news3cat3.setName("slashdot");

      INews news4 = createNews(feed, null, "http://www.news.com/news4.html", State.UPDATED);
      Date news4Date = new Date(1000000);
      news4.setPublishDate(news4Date);
      IPerson author4 = fFactory.createPerson(null, news4);
      author4.setName("Pasero");
      ISource source4 = fFactory.createSource(news4);
      source4.setLink(new URI("http://www.source.com"));

      INews news5 = createNews(feed, null, "http://www.news.com/news5.html", State.NEW);
      news5.setFlagged(true);
      IPerson author5 = fFactory.createPerson(null, news5);
      author5.setEmail(new URI("test@rssowl.org"));
      ISource source5 = fFactory.createSource(news5);
      source5.setName("Source for News 5");
      ICategory news5cat1 = fFactory.createCategory(null, news5);
      news5cat1.setName("Apache Lucene");
      ICategory news5cat2 = fFactory.createCategory(null, news5);
      news5cat2.setName("Java");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* Condition 1a: Enum */
      {
        ISearchField field = fFactory.createSearchField(INews.STATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, EnumSet.of(State.READ));

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);
      }

      /* Condition 1b: Enum */
      {
        ISearchField field = fFactory.createSearchField(INews.STATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, EnumSet.of(State.DELETED));

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Condition 2a: Integer */
      {

        /* Rating */
        ISearchField field = fFactory.createSearchField(INews.RATING, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, 10);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4, news5);
      }

      /* Condition 2b: Integer */
      {

        /* Rating */
        ISearchField field = fFactory.createSearchField(INews.RATING, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, 15);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Condition 3a: String (match) */
      {

        /* Categories */
        ISearchField field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "apple");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news4, news5);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "windows");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news4, news5);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "slash*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news4, news5);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "a*le");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news4, news5);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "apache lucene");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        /* Labels */
        field = fFactory.createSearchField(INews.LABEL, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, label1.getName());

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);

        /* Source Name */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "Source for News 5");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);
      }

      /* Condition 3b: String (no match) */
      {

        /* Author */
        ISearchField field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "Pasero Benjamin");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        /* Categories */
        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "apple slashdot");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "sleshdod");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.CATEGORIES, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "lucene apache");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        /* Source Name */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "Source for");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Condition 4a: Date (match) */
      {
        ISearchField field = fFactory.createSearchField(INews.PUBLISH_DATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, news4Date);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news5);
      }

      /* Condition 4b: Date */
      {
        Date wrongDate = new Date();
        ISearchField field = fFactory.createSearchField(INews.PUBLISH_DATE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, wrongDate);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }

      /* Condition 5a: Boolean (one match) */
      {
        ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, true);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        /* Attachments */
        field = fFactory.createSearchField(INews.HAS_ATTACHMENTS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, true);

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4, news5);
      }

      /* Condition 5b: Boolean (other matches) */
      {
        ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, false);

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        /* Attachments */
        field = fFactory.createSearchField(INews.HAS_ATTACHMENTS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, false);

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2);
      }

      /* Condition 6a: Link (match) */
      {

        /* News Link */
        ISearchField field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.news.com/news1.html");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.news.com/news?.html");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "*www.news.com/news1.html");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);

        /* Source Link */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.source.com");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news5);

        /* Feed Link */
        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.feed.com/feed.xml");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.feed.com/*");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }

      /* Condition 6b: Link */
      {

        /* News Link */
        ISearchField field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.news.com/news6.html");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.news.com/news?");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.LINK, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "*www.news.com/");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        /* Source Link */
        field = fFactory.createSearchField(INews.SOURCE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.othersource.com");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        /* Feed Link */
        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.feed.com/feed2.xml");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.FEED, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.otherfeed.com/*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchNewsWith_CONTAINS_Specifier() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
      ICategory news1cat1 = fFactory.createCategory(null, news1);
      news1cat1.setName("apple");
      ILabel label1 = fFactory.createLabel(null, "work");
      news1.addLabel(label1);
      IAttachment att1news1 = fFactory.createAttachment(null, news1);
      att1news1.setLink(new URI("http://www.attachment.com/att1news1.file"));
      att1news1.setType("bin/mp3");

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer description with <html><h2>included!</h2></html>");
      ICategory news2cat1 = fFactory.createCategory(null, news2);
      news2cat1.setName("apple");
      ICategory news2cat2 = fFactory.createCategory(null, news2);
      news2cat2.setName("pasero");
      ILabel label2 = fFactory.createLabel(null, "todo");
      news2.addLabel(label2);
      IAttachment att1news2 = fFactory.createAttachment(null, news2);
      att1news2.setLink(new URI("http://www.attachment.com/att1news2.file"));
      att1news2.setType("bin/doc");
      IAttachment att2news2 = fFactory.createAttachment(null, news2);
      att2news2.setLink(new URI("http://www.attachment.com/att2news2.file"));
      att2news2.setType("bin/wav");

      INews news3 = createNews(feed, "Foo Bar", "http://www.news.com/news3.html", State.NEW);
      news3.setDescription("This is a longer description with \n newlines and <html><h2>included!</h2></html>");
      IPerson author3 = fFactory.createPerson(null, news3);
      author3.setName("Benjamin Pasero");
      ICategory news3cat1 = fFactory.createCategory(null, news3);
      news3cat1.setName("apple");
      ICategory news3cat2 = fFactory.createCategory(null, news3);
      news3cat2.setName("windows");
      ICategory news3cat3 = fFactory.createCategory(null, news3);
      news3cat3.setName("slashdot");

      INews news4 = createNews(feed, "BAR FOO", "http://www.news.com/news4.html", State.UPDATED);
      Date news4Date = new Date(1000000);
      news4.setPublishDate(news4Date);
      IPerson author4 = fFactory.createPerson(null, news4);
      author4.setName("Pasero");
      ISource source4 = fFactory.createSource(news4);
      source4.setLink(new URI("http://www.source.com"));

      INews news5 = createNews(feed, null, "http://www.news.com/news5.html", State.NEW);
      news5.setFlagged(true);
      IPerson author5 = fFactory.createPerson(null, news5);
      author5.setEmail(new URI("test@rssowl.org"));
      ISource source5 = fFactory.createSource(news5);
      source5.setName("Source for News 5");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* Condition 1a: String (match) */
      {

        /* Title */
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo bar");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "bar foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "b* f*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "fo? b*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        /* Description */
        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "included");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "lon?er description");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        /* Attachments */
        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "bin/mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "www.attachment.com*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2);

        /* Author */
        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Benjamin Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Ben?amin Pase*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Ben*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "test@rssowl.org");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "test@rssowl?*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        /* All Fields */
        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "description new?ines");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);
      }

      /* Condition 1b: String (no match) */
      {

        /* Title */
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "barfoo");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "f? b?");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Description */
        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "html");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "loner desription");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Attachment */
        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "bin/ogg");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*ogg");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "www.attachments.com*");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* All Fields */
        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foobar");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*barfoo");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchNewsWith_CONTAINS_ALL_Specifier_Behaves_Like_CONTAINS_For_Single_Terms() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
      ICategory news1cat1 = fFactory.createCategory(null, news1);
      news1cat1.setName("apple");
      ILabel label1 = fFactory.createLabel(null, "work");
      news1.addLabel(label1);
      IAttachment att1news1 = fFactory.createAttachment(null, news1);
      att1news1.setLink(new URI("http://www.attachment.com/att1news1.file"));
      att1news1.setType("bin/mp3");

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer description with <html><h2>included!</h2></html>");
      ICategory news2cat1 = fFactory.createCategory(null, news2);
      news2cat1.setName("apple");
      ICategory news2cat2 = fFactory.createCategory(null, news2);
      news2cat2.setName("pasero");
      ILabel label2 = fFactory.createLabel(null, "todo");
      news2.addLabel(label2);
      IAttachment att1news2 = fFactory.createAttachment(null, news2);
      att1news2.setLink(new URI("http://www.attachment.com/att1news2.file"));
      att1news2.setType("bin/doc");
      IAttachment att2news2 = fFactory.createAttachment(null, news2);
      att2news2.setLink(new URI("http://www.attachment.com/att2news2.file"));
      att2news2.setType("bin/wav");

      INews news3 = createNews(feed, "Foo Bar", "http://www.news.com/news3.html", State.NEW);
      news3.setDescription("This is a longer description with \n newlines and <html><h2>included!</h2></html>");
      IPerson author3 = fFactory.createPerson(null, news3);
      author3.setName("Benjamin Pasero");
      ICategory news3cat1 = fFactory.createCategory(null, news3);
      news3cat1.setName("apple");
      ICategory news3cat2 = fFactory.createCategory(null, news3);
      news3cat2.setName("windows");
      ICategory news3cat3 = fFactory.createCategory(null, news3);
      news3cat3.setName("slashdot");

      INews news4 = createNews(feed, "BAR FOO", "http://www.news.com/news4.html", State.UPDATED);
      Date news4Date = new Date(1000000);
      news4.setPublishDate(news4Date);
      IPerson author4 = fFactory.createPerson(null, news4);
      author4.setName("Pasero");
      ISource source4 = fFactory.createSource(news4);
      source4.setLink(new URI("http://www.source.com"));

      INews news5 = createNews(feed, null, "http://www.news.com/news5.html", State.NEW);
      news5.setFlagged(true);
      IPerson author5 = fFactory.createPerson(null, news5);
      author5.setEmail(new URI("test@rssowl.org"));
      ISource source5 = fFactory.createSource(news5);
      source5.setName("Source for News 5");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* Condition 1a: String (match) */
      {

        /* Title */
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foo");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "b?r");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "b?r");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "b*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "b*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4);

        /* Description */
        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "included");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "included");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "inc?uded");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "inc?uded");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "inc*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "inc*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        /* Attachments */
        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "bin/mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "bin/mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "*mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        /* Author */
        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Pa?ero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Pa?ero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Pa*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Pa*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        /* All Fields */
        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "f?o");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "f?o");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "fo*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "fo*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchNewsWith_CONTAINS_ALL_Specifier() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
      ICategory news1cat1 = fFactory.createCategory(null, news1);
      news1cat1.setName("apple");
      ILabel label1 = fFactory.createLabel(null, "work");
      news1.addLabel(label1);
      IAttachment att1news1 = fFactory.createAttachment(null, news1);
      att1news1.setLink(new URI("http://www.attachment.com/att1news1.file"));
      att1news1.setType("bin/mp3");

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      news2.setDescription("This is a longer description with <html><h2>included!</h2></html>");
      ICategory news2cat1 = fFactory.createCategory(null, news2);
      news2cat1.setName("apple");
      ICategory news2cat2 = fFactory.createCategory(null, news2);
      news2cat2.setName("pasero");
      ILabel label2 = fFactory.createLabel(null, "todo");
      news2.addLabel(label2);
      IAttachment att1news2 = fFactory.createAttachment(null, news2);
      att1news2.setLink(new URI("http://www.attachment.com/att1news2.file"));
      att1news2.setType("bin/doc");
      IAttachment att2news2 = fFactory.createAttachment(null, news2);
      att2news2.setLink(new URI("http://www.attachment.com/att2news2.file"));
      att2news2.setType("bin/wav");

      INews news3 = createNews(feed, "Foo Bar", "http://www.news.com/news3.html", State.NEW);
      news3.setDescription("This is a longer description with \n newlines and <html><h2>included!</h2></html>");
      IPerson author3 = fFactory.createPerson(null, news3);
      author3.setName("Benjamin Pasero");
      ICategory news3cat1 = fFactory.createCategory(null, news3);
      news3cat1.setName("apple");
      ICategory news3cat2 = fFactory.createCategory(null, news3);
      news3cat2.setName("windows");
      ICategory news3cat3 = fFactory.createCategory(null, news3);
      news3cat3.setName("slashdot");

      INews news4 = createNews(feed, "BAR FOO", "http://www.news.com/news4.html", State.UPDATED);
      Date news4Date = new Date(1000000);
      news4.setPublishDate(news4Date);
      IPerson author4 = fFactory.createPerson(null, news4);
      author4.setName("Pasero");
      ISource source4 = fFactory.createSource(news4);
      source4.setLink(new URI("http://www.source.com"));

      INews news5 = createNews(feed, null, "http://www.news.com/news5.html", State.NEW);
      news5.setFlagged(true);
      IPerson author5 = fFactory.createPerson(null, news5);
      author5.setEmail(new URI("test@rssowl.org"));
      ISource source5 = fFactory.createSource(news5);
      source5.setName("Source for News 5");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* Condition 1a: String (match) */
      {

        /* Title */
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foo bar");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "bar foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "b* f*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "fo? b*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        /* Description */
        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "newlines included");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "new?ines description");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        /* Attachments */
        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "bin/mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "www.attachment.com*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2);

        /* Author */
        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Benjamin Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Ben?amin Pase*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "Ben*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "test@rssowl.org");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "test@rssowl?*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        /* All Fields */
        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "description new?ines");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foo appl? bin/mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "ba? Apple descript*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "BAR FOO PASERO");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4);
      }

      /* Condition 1b: String (no match) */
      {

        /* Title */
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "barfoo");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "f? b?");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Description */
        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "html");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "loner desription");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* Attachment */
        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "bin/ogg");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*ogg");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "www.attachments.com*");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        /* All Fields */
        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foobar");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "*barfoo");

        result = fModelSearch.searchNews(list(condition), false);
        assertEquals(0, result.size());
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchNewsWith_CONTAINS_NOT_Specifier() throws Exception {
    try {

      /* First add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "Foo", "http://www.news.com/news1.html", State.READ);
      ICategory news1cat1 = fFactory.createCategory(null, news1);
      news1cat1.setName("apple");
      ILabel label1 = fFactory.createLabel(null, "work");
      news1.addLabel(label1);
      IAttachment att1news1 = fFactory.createAttachment(null, news1);
      att1news1.setLink(new URI("http://www.attachment.com/att1news1.file"));
      att1news1.setType("bin/mp3");

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      ICategory news2cat1 = fFactory.createCategory(null, news2);
      news2cat1.setName("apple");
      ICategory news2cat2 = fFactory.createCategory(null, news2);
      news2cat2.setName("pasero");
      ILabel label2 = fFactory.createLabel(null, "todo");
      news2.addLabel(label2);
      IAttachment att1news2 = fFactory.createAttachment(null, news2);
      att1news2.setLink(new URI("http://www.attachment.com/att1news2.file"));
      att1news2.setType("bin/doc");
      IAttachment att2news2 = fFactory.createAttachment(null, news2);
      att2news2.setLink(new URI("http://www.attachment.com/att2news2.file"));
      att2news2.setType("bin/wav");

      INews news3 = createNews(feed, "Foo Bar", "http://www.news.com/news3.html", State.NEW);
      news3.setDescription("This is a longer description with \n newlines and <html><h2>included!</h2></html>");
      IPerson author3 = fFactory.createPerson(null, news3);
      author3.setName("Benjamin Pasero");
      ICategory news3cat1 = fFactory.createCategory(null, news3);
      news3cat1.setName("apple");
      ICategory news3cat2 = fFactory.createCategory(null, news3);
      news3cat2.setName("windows");
      ICategory news3cat3 = fFactory.createCategory(null, news3);
      news3cat3.setName("slashdot");

      INews news4 = createNews(feed, "BAR FOO", "http://www.news.com/news4.html", State.UPDATED);
      Date news4Date = new Date(1000000);
      news4.setPublishDate(news4Date);
      IPerson author4 = fFactory.createPerson(null, news4);
      author4.setName("Pasero");
      ISource source4 = fFactory.createSource(news4);
      source4.setLink(new URI("http://www.source.com"));

      INews news5 = createNews(feed, null, "http://www.news.com/news5.html", State.NEW);
      news5.setFlagged(true);
      IPerson author5 = fFactory.createPerson(null, news5);
      author5.setEmail(new URI("test@rssowl.org"));
      ISource source5 = fFactory.createSource(news5);
      source5.setName("Source for News 5");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      /* Condition 1a: String (match) */
      {

        /* Title */
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "foo bar");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "bar foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "b* f*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "fo? b*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news5);

        /* Description */
        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "included");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news4, news5);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "lon?er description");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news4, news5);

        /* Attachments */
        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "bin/mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*mp3");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "www.attachment.com*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news3, news4, news5);

        /* Author */
        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Benjamin Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news5);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news5);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Ben?amin Pase*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news5);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "Ben*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news4, news5);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "test@rssowl.org");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        field = fFactory.createSearchField(INews.AUTHOR, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "test@rssowl?*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4);

        /* All Fields */
        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "foo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news2, news5);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*pasero");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news5);
      }

      /* Condition 1b: String (no match) */
      {

        /* Title */
        ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "barfoo");

        List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "f? b?");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        /* Description */
        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "html");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.DESCRIPTION, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "loner desription");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        /* Attachment */
        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "bin/ogg");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*ogg");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "www.attachments.com*");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        /* All Fields */
        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "foobar");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);

        field = fFactory.createSearchField(IEntity.ALL_FIELDS, fNewsEntityName);
        condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "*barfoo");

        result = fModelSearch.searchNews(list(condition), false);
        assertSame(result, news1, news2, news3, news4, news5);
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSearchCleanup() throws Exception {
    try {

      /* Run Clean Up */
      fModelSearch.cleanUp(new NullProgressMonitor());

      /* Add some Types */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));

      INews news1 = createNews(feed, "Brabbels", "http://www.news.com/news1.html", State.READ);
      ICategory news1cat1 = fFactory.createCategory(null, news1);
      news1cat1.setName("apple");
      ILabel label1 = fFactory.createLabel(null, "work");
      news1.addLabel(label1);
      IAttachment att1news1 = fFactory.createAttachment(null, news1);
      att1news1.setLink(new URI("http://www.attachment.com/att1news1.file"));
      att1news1.setType("bin/mp3");

      INews news2 = createNews(feed, " Bar", "http://www.news.com/news2.html", State.NEW);
      ICategory news2cat1 = fFactory.createCategory(null, news2);
      news2cat1.setName("apple");
      ICategory news2cat2 = fFactory.createCategory(null, news2);
      news2cat2.setName("pasero");
      ILabel label2 = fFactory.createLabel(null, "todo");
      news2.addLabel(label2);
      IAttachment att1news2 = fFactory.createAttachment(null, news2);
      att1news2.setLink(new URI("http://www.attachment.com/att1news2.file"));
      att1news2.setType("bin/doc");
      IAttachment att2news2 = fFactory.createAttachment(null, news2);
      att2news2.setLink(new URI("http://www.attachment.com/att2news2.file"));
      att2news2.setType("bin/wav");

      INews news3 = createNews(feed, "Foo Bar", "http://www.news.com/news3.html", State.NEW);
      news3.setDescription("This is a longer description with \n newlines and <html><h2>included!</h2></html>");
      IPerson author3 = fFactory.createPerson(null, news3);
      author3.setName("Benjamin Pasero");
      ICategory news3cat1 = fFactory.createCategory(null, news3);
      news3cat1.setName("apple");
      ICategory news3cat2 = fFactory.createCategory(null, news3);
      news3cat2.setName("windows");
      ICategory news3cat3 = fFactory.createCategory(null, news3);
      news3cat3.setName("slashdot");

      INews news4 = createNews(feed, "BAR FOO", "http://www.news.com/news4.html", State.UPDATED);
      Date news4Date = new Date(1000000);
      news4.setPublishDate(news4Date);
      IPerson author4 = fFactory.createPerson(null, news4);
      author4.setName("Pasero");
      ISource source4 = fFactory.createSource(news4);
      source4.setLink(new URI("http://www.source.com"));

      INews news5 = createNews(feed, null, "http://www.news.com/news5.html", State.NEW);
      news5.setFlagged(true);
      IPerson author5 = fFactory.createPerson(null, news5);
      author5.setEmail(new URI("test@rssowl.org"));
      ISource source5 = fFactory.createSource(news5);
      source5.setName("Source for News 5");

      DynamicDAO.save(feed);

      /* Wait for Indexer */
      waitForIndexer();

      ISearchField field = fFactory.createSearchField(INews.TITLE, fNewsEntityName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Brabbels");

      List<SearchHit<NewsReference>> result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1);

      /* Run Clean Up */
      fModelSearch.cleanUp(new NullProgressMonitor());

      result = fModelSearch.searchNews(list(condition), false);
      assertSame(result, news1);

      DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singleton(news1), INews.State.HIDDEN, false, false);

      /* Wait for Indexer */
      waitForIndexer();

      result = fModelSearch.searchNews(list(condition), false);
      assertTrue(result.isEmpty());

      /* Run Clean Up */
      fModelSearch.cleanUp(new NullProgressMonitor());

      result = fModelSearch.searchNews(list(condition), false);
      assertTrue(result.isEmpty());
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }
}
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

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.util.SearchHit;
import org.rssowl.core.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Test searching types from the persistence layer.
 *
 * @author bpasero
 */
public abstract class AbstractModelSearchTest {

  /** One Day in Millis */
  protected static final Long DAY = 1000 * 3600 * 24L;

  /** One Minute in Millis */
  protected static final Long MINUTE = 1000 * 60L;

  /** Model Factory */
  protected IModelFactory fFactory;

  /** Model Search */
  protected IModelSearch fModelSearch;

  /** News Entity Name */
  protected String fNewsEntityName;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    fModelSearch = Owl.getPersistenceService().getModelSearch();
    fFactory = Owl.getModelFactory();
    fNewsEntityName = INews.class.getName();

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * @param feed
   * @param title
   * @param link
   * @param state
   * @return INews
   * @throws URISyntaxException
   */
  protected INews createNews(IFeed feed, String title, String link, INews.State state) throws URISyntaxException {
    return createNews(feed, title, null, link, state);
  }

  /**
   * @param feed
   * @param title
   * @param description
   * @param link
   * @param state
   * @return INews
   * @throws URISyntaxException
   */
  protected INews createNews(IFeed feed, String title, String description, String link, INews.State state) throws URISyntaxException {
    INews news = fFactory.createNews(null, feed, new Date(System.currentTimeMillis()));
    news.setState(state);
    news.setLink(new URI(link));
    news.setTitle(title);
    if (description != null)
      news.setDescription(description);

    return news;
  }

  /**
   * @throws InterruptedException
   */
  protected void waitForIndexer() throws InterruptedException {
    Thread.sleep(500);
  }

  /**
   * @param condition
   * @return List<ISearchCondition>
   */
  protected List<ISearchCondition> list(ISearchCondition... condition) {
    return new ArrayList<ISearchCondition>(Arrays.asList(condition));
  }

  /**
   * @param result
   * @param news
   */
  protected void assertSame(List<SearchHit<NewsReference>> result, INews... news) {
    assertSame(null, result, news);
  }

  /**
   * @param msg
   * @param result
   * @param news
   */
  protected void assertSame(String msg, List<SearchHit<NewsReference>> result, INews... news) {
    if (result.size() != news.length) {
      if (StringUtils.isSet(msg))
        fail("Results don't have the same number of Elements (" + news.length + " expected, " + result.size() + " actual)! Message: " + msg);
      else
        fail("Results don't have the same number of Elements (" + news.length + " expected, " + result.size() + " actual)!");
    }

    for (INews newsitem : news) {
      boolean found = false;
      for (SearchHit<NewsReference> hit : result) {
        if (hit.getResult().getId() == newsitem.getId()) {
          found = true;
          break;
        }
      }

      if (msg != null)
        assertEquals(msg, true, found);
      else
        assertEquals(true, found);
    }
  }
}
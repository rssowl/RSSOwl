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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.MergeResult;
import org.rssowl.core.internal.persist.dao.EntitiesToBeIndexedDAOImpl;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.internal.persist.service.EntityIdsByEventType;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsBinDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.dao.ISearchMarkDAO;
import org.rssowl.core.persist.event.AttachmentAdapter;
import org.rssowl.core.persist.event.AttachmentEvent;
import org.rssowl.core.persist.event.AttachmentListener;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.event.CategoryEvent;
import org.rssowl.core.persist.event.CategoryListener;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.FeedListener;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.LabelEvent;
import org.rssowl.core.persist.event.LabelListener;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.PersonEvent;
import org.rssowl.core.persist.event.PersonListener;
import org.rssowl.core.persist.event.SearchConditionEvent;
import org.rssowl.core.persist.event.SearchConditionListener;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.persist.reference.AttachmentReference;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.CategoryReference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.reference.LabelReference;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.reference.PersonReference;
import org.rssowl.core.persist.reference.SearchConditionReference;
import org.rssowl.core.persist.reference.SearchMarkReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.tests.TestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * This TestCase is for testing the Model Plugin (3 of 4).
 *
 * @author bpasero
 */
@SuppressWarnings("nls")
public class ModelTest3 extends LargeBlockSizeTest {
  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
  }

  private IFeed createFeed(String url) throws URISyntaxException {
    return fFactory.createFeed(null, new URI(url));
  }

  /**
   * Tests {@link INews#merge(INews)}. Particularly: - If the state of both news
   * is different, it's changed to the new state with one exception: if the
   * second News is in the NEW state, the state of the first news won't be
   * changed.
   *
   * @throws Exception
   */
  @Test
  public void testNewsStateMerge() throws Exception {
    /* Initial Add News */
    String url = "http://www.feed-case1.com";
    IFeed feed = createFeed(url);
    final String newsTitle = "News Title Case_1";
    fFactory.createNews(null, feed, new Date()).setTitle(newsTitle);
    FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

    /*
     * Recreate the feed because the existing one got changed when it was saved
     */
    feed = createFeed(url);

    /* a) Different publish date and news to be merged is in NEW state */
    INews news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());

    IFeed mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

    /* b) Different publish date and news to be merged is in DELETED state */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.DELETED);

    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.DELETED, feedRef.resolve().getNews().get(0).getState());

    /* c) Different publish date and news to be merged is in HIDDEN state */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.HIDDEN);

    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.HIDDEN, feedRef.resolve().getNews().get(0).getState());

    /* d) Different publish date and news to be merged is in READ state */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.READ);

    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.READ, feedRef.resolve().getNews().get(0).getState());

    /* e) Different publish date and news to be merged is in UNREAD state */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.UNREAD);

    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.UNREAD, feedRef.resolve().getNews().get(0).getState());

    /* f) Different publish date and news to be merged is in UPDATED state */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.UPDATED);

    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

    /*
     * g) Different publish date and news to be merged is in NEW state, but
     * existing News is in UPDATED state
     */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.NEW);

    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());
  }

  /**
   * Tests {@link INews#merge(INews)}. Particularly: - The state does not change
   * to UPDATED if both news have the same state and it is NEW, DELETED or
   * HIDDEN.
   *
   * @throws Exception
   */
  @Test
  public void testNewsStateMerge2() throws Exception {
    /* Initial Add News */
    String url = "http://www.feed-case1.com";
    IFeed feed = createFeed(url);
    final String newsTitle = "News Title Case_1";
    fFactory.createNews(null, feed, new Date()).setTitle(newsTitle);
    FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

    /*
     * Recreate the feed because the existing one got changed when it was saved
     */
    feed = createFeed(url);

    /* a) Different publish date and both news are in NEW state */
    INews news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());

    IFeed mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

    /* b) Different publish date and both news are in DELETED state */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.DELETED);

    IFeed dbFeed = feedRef.resolve();
    dbFeed.getNews().get(0).setState(State.DELETED);
    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.DELETED, feedRef.resolve().getNews().get(0).getState());

    /* c) Different publish date and both news are in HIDDEN state */
    feed = createFeed(url);
    news = fFactory.createNews(null, feed, new Date());
    news.setTitle(newsTitle);
    news.setPublishDate(new Date());
    news.setState(State.HIDDEN);

    dbFeed = feedRef.resolve();
    dbFeed.getNews().get(0).setState(State.HIDDEN);
    mergedFeed = feedRef.resolve();
    mergedFeed.merge(feed);
    feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
    assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
    assertEquals("Existing News State changed unexpectedly!", INews.State.HIDDEN, feedRef.resolve().getNews().get(0).getState());
  }

  /**
   * Test all cases of a News being added to the DB, which is already present.
   * Check all possible combinatios that include description.
   *
   * @throws Exception
   */
  @Test
  public void testNewsAddedUpdatedWithDescription() throws Exception {
    String description = "Initial description";
    /* News with Title and Description */
    {
      /* Initial Add News */
      long time = System.currentTimeMillis();
      String url = "http://www.feed-case4.com";
      IFeed feed = createFeed(url);
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_4");
      news.setPublishDate(new Date(time));
      news.setDescription(description);
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      /* Mark News Read */
      news = feedRef.resolve().getNews().get(0);
      news.setState(INews.State.READ);
      DynamicDAO.save(news);

      /* b) Add the same News with updated Description */
      feed = createFeed(url);
      news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_4");
      news.setPublishDate(new Date(time));
      news.setDescription(description + "updated");
      IFeed mergedFeed = feedRef.resolve();
      mergedFeed.merge(feed);
      feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
      assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
      assertEquals("Existing News State is not READ!", INews.State.READ, feedRef.resolve().getNews().get(0).getState());
    }

    /* News with Title, URL and Description */
    {
      /* Initial Add News */
      long time = System.currentTimeMillis();
      String url = "http://www.feed-case5.com";
      IFeed feed = createFeed(url);
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_5");
      news.setLink(new URI("http://www.news-case5.com/index.html"));
      news.setPublishDate(new Date(time));
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      /* Mark News Read */
      news = feedRef.resolve().getNews().get(0);
      news.setState(INews.State.READ);
      DynamicDAO.save(news);

      /* c) Add the same News with updated description */
      feed = createFeed(url);
      news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_5 Updated");
      news.setLink(new URI("http://www.news-case5.com/index.html"));
      news.setPublishDate(new Date(time));
      news.setDescription(description + "updated#2");
      IFeed mergedFeed = feedRef.resolve();
      mergedFeed.merge(feed);
      feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
      assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
      assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());
    }

    /* News with Title, Guid and Description */
    {
      /* Initial Add News */
      long time = System.currentTimeMillis();
      String url = "http://www.feed-case6.com";
      IFeed feed = createFeed(url);
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_6");
      fFactory.createGuid(news, "News_Case_6_Guid", null);
      news.setPublishDate(new Date(time));
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      /* Mark News Read */
      news = feedRef.resolve().getNews().get(0);
      news.setState(INews.State.READ);
      DynamicDAO.save(news);

      /* d) Add the same News with updated description */
      feed = createFeed(url);
      news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_6");
      fFactory.createGuid(news, "News_Case_6_Guid", null);
      news.setDescription(description + "updated#3");
      IFeed mergedFeed = feedRef.resolve();
      mergedFeed.merge(feed);
      feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
      assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
      assertEquals("Existing News State is not READ!", INews.State.READ, feedRef.resolve().getNews().get(0).getState());
    }

    /* News with Title, URL, Guid and Description */
    {
      /* Initial Add News */
      long time = System.currentTimeMillis();
      String url = "http://www.feed-case8.com";
      IFeed feed = createFeed(url);
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_8");
      news.setLink(new URI("http://www.news-case8.com/index.html"));
      fFactory.createGuid(news, "News_Case_8_Guid", null);
      news.setPublishDate(new Date(time));
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      /* Mark News Read */
      news = feedRef.resolve().getNews().get(0);
      news.setState(INews.State.READ);
      DynamicDAO.save(news);

      /* d) Add the same News with updated Publish Date */
      feed = createFeed(url);
      news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News Title Case_8");
      news.setLink(new URI("http://www.news-case8.com/index.html"));
      fFactory.createGuid(news, "News_Case_8_Guid", null);
      news.setPublishDate(new Date(System.currentTimeMillis() + 1000));
      news.setDescription(description + "updated#4");
      IFeed mergedFeed = feedRef.resolve();
      mergedFeed.merge(feed);
      feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
      assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
      assertEquals("Existing News State is not READ!", INews.State.READ, feedRef.resolve().getNews().get(0).getState());
    }
  }

  /**
   * Test all cases of a News being added to the DB, which is already present.
   * Check all possible combinatios of a News containing Title, URL, Guid,
   * PublishDate.
   *
   * @throws Exception
   */
  @Test
  public void testNewsAddedUpdated() throws Exception {
    try {

      /* Case 1: News with Title */
      {
        /* Initial Add News */
        String url = "http://www.feed-case1.com";
        IFeed feed = createFeed(url);
        fFactory.createNews(null, feed, new Date()).setTitle("News Title Case_1");
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /*
         * Recreate the feed because the existing one got changed when it was
         * saved
         */
        feed = createFeed(url);

        /* a) Add the same News */
        fFactory.createNews(null, feed, new Date()).setTitle("News Title Case_1");
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());
      }

      /* Case 2: News with Title and URL */
      {
        /* Initial Add News */
        String url = "http://www.feed-case2.com";
        IFeed feed = createFeed(url);
        INews news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_2");
        news.setLink(new URI("http://www.news-case2.com/index.html"));
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /* a) Add the same News */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_2");
        news.setLink(new URI("http://www.news-case2.com/index.html"));
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* b) Add the same News with updated Title */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_2 Updated");
        news.setLink(new URI("http://www.news-case2.com/index.html"));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());
      }

      /* Case 3: News with Title and Guid */
      {
        /* Initial Add News */
        String url = "http://www.feed-case3.com";
        IFeed feed = createFeed(url);
        INews news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_3");
        fFactory.createGuid(news, "News_Case_3_Guid", null);
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /* a) Add the same News */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_3");
        fFactory.createGuid(news, "News_Case_3_Guid", null);
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* b) Add the same News with updated Title */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_3 Updated");
        fFactory.createGuid(news, "News_Case_3_Guid", null);
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());
      }

      /* Case 4: News with Title and Publish Date */
      {
        /* Initial Add News */
        long time = System.currentTimeMillis();
        String url = "http://www.feed-case4.com";
        IFeed feed = createFeed(url);
        INews news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_4");
        news.setPublishDate(new Date(time));
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /* a) Add the same News */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_4");
        news.setPublishDate(new Date(time));
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* b) Add the same News with updated Publish Date */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_4");
        news.setPublishDate(new Date(System.currentTimeMillis() + 1000));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not READ!", INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      }

      /* Case 5: News with Title, URL and Publish Date */
      {
        /* Initial Add News */
        long time = System.currentTimeMillis();
        String url = "http://www.feed-case5.com";
        IFeed feed = createFeed(url);
        INews news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_5");
        news.setLink(new URI("http://www.news-case5.com/index.html"));
        news.setPublishDate(new Date(time));
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /* a) Add the same News */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_5");
        news.setLink(new URI("http://www.news-case5.com/index.html"));
        news.setPublishDate(new Date(time));
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* b) Add the same News with updated Title */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_5 Updated");
        news.setLink(new URI("http://www.news-case5.com/index.html"));
        news.setPublishDate(new Date(time));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* c) Add the same News with updated Publish Date */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_5 Updated");
        news.setLink(new URI("http://www.news-case5.com/index.html"));
        news.setPublishDate(new Date(System.currentTimeMillis() + 1000));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not READ!", INews.State.READ, feedRef.resolve().getNews().get(0).getState());
      }

      /* Case 6: News with Title, Guid and Publish Date */
      {
        /* Initial Add News */
        long time = System.currentTimeMillis();
        String url = "http://www.feed-case6.com";
        IFeed feed = createFeed(url);
        INews news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_6");
        fFactory.createGuid(news, "News_Case_6_Guid", null);
        news.setPublishDate(new Date(time));
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /* a) Add the same News */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_6");
        fFactory.createGuid(news, "News_Case_6_Guid", null);
        news.setPublishDate(new Date(time));
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* b) Add the same News with updated Title */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_6 Updated");
        fFactory.createGuid(news, "News_Case_6_Guid", null);
        news.setPublishDate(new Date(time));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

        /* c) Add the same News with updated Guid */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_6");
        fFactory.createGuid(news, "News_Case_6_Guid_Updated", null);
        news.setPublishDate(new Date(time));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Expected two News in this Feed!", 2, feedRef.resolve().getNews().size());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* d) Add the same News with updated Publish Date */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_6");
        fFactory.createGuid(news, "News_Case_6_Guid", null);
        news.setPublishDate(new Date(time + 1000));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 2, feedRef.resolve().getNews().size());

        List<INews> newsRefs = feedRef.resolve().getNews();
        for (INews newsRef : newsRefs) {
          if ("News_Case_6_Guid".equals(newsRef.getGuid().getValue()))
            assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, newsRef.getState());
        }
      }

      /* Case 7: News with Title, URL and Guid */
      {
        /* Initial Add News */
        String url = "http://www.feed-case7.com";
        IFeed feed = createFeed(url);
        INews news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_7");
        fFactory.createGuid(news, "News_Case_7_Guid", null);
        news.setLink(new URI("http://www.news-case7.com/index.html"));
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /* a) Add the same News */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_7");
        fFactory.createGuid(news, "News_Case_7_Guid", null);
        news.setLink(new URI("http://www.news-case7.com/index.html"));
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* b) Add the same News with updated Title */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_7 Updated");
        fFactory.createGuid(news, "News_Case_7_Guid", null);
        news.setLink(new URI("http://www.news-case7.com/index.html"));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* c) Add the same News with updated URL */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_7 Updated");
        fFactory.createGuid(news, "News_Case_7_Guid", null);
        news.setLink(new URI("http://www.news-case7.com/index-updated.html"));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not READ!", INews.State.READ, feedRef.resolve().getNews().get(0).getState());

        /* d) Add the same News with updated Guid */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_7");
        fFactory.createGuid(news, "News_Case_7_Guid_Updated", null);
        news.setLink(new URI("http://www.news-case7.com/index.html"));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Expected two News in this Feed!", 2, feedRef.resolve().getNews().size());
      }

      /* Case 8: News with Title, URL, Guid and Publish Date */
      {
        /* Initial Add News */
        long time = System.currentTimeMillis();
        String url = "http://www.feed-case8.com";
        IFeed feed = createFeed(url);
        INews news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_8");
        news.setLink(new URI("http://www.news-case8.com/index.html"));
        fFactory.createGuid(news, "News_Case_8_Guid", null);
        news.setPublishDate(new Date(time));
        FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

        /* a) Add the same News */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_8");
        news.setLink(new URI("http://www.news-case8.com/index.html"));
        fFactory.createGuid(news, "News_Case_8_Guid", null);
        news.setPublishDate(new Date(time));
        IFeed mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State changed unexpectedly!", INews.State.NEW, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* b) Add the same News with updated Title */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_8 Updated");
        news.setLink(new URI("http://www.news-case8.com/index.html"));
        fFactory.createGuid(news, "News_Case_8_Guid", null);
        news.setPublishDate(new Date(time));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 1, feedRef.resolve().getNews().size());
        assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, feedRef.resolve().getNews().get(0).getState());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* c) Add the same News with updated Guid */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_8");
        news.setLink(new URI("http://www.news-case8.com/index.html"));
        fFactory.createGuid(news, "News_Case_8_Guid_Updated", null);
        news.setPublishDate(new Date(time));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Expected two News in this Feed!", 2, feedRef.resolve().getNews().size());

        /* Mark News Read */
        news = feedRef.resolve().getNews().get(0);
        news.setState(INews.State.READ);
        DynamicDAO.save(news);

        /* d) Add the same News with updated Publish Date */
        feed = createFeed(url);
        news = fFactory.createNews(null, feed, new Date());
        news.setTitle("News Title Case_8");
        news.setLink(new URI("http://www.news-case8.com/index.html"));
        fFactory.createGuid(news, "News_Case_8_Guid", null);
        news.setPublishDate(new Date(System.currentTimeMillis() + 1000));
        mergedFeed = feedRef.resolve();
        mergedFeed.merge(feed);
        feedRef = new FeedReference(DynamicDAO.save(mergedFeed).getId());
        assertEquals("Same News was added twice!", 2, feedRef.resolve().getNews().size());

        List<INews> newsRefs = feedRef.resolve().getNews();
        for (INews newsRef : newsRefs) {
          if ("News_Case_8_Guid".equals(newsRef.getGuid().getValue()))
            assertEquals("Existing News State is not UPDATED!", INews.State.UPDATED, newsRef.getState());
        }
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * Test setting a News' state to deleted and then check wether the DB is
   * correctly deleting it completly from the DB, if no longer contained in the
   * Feed.
   *
   * @throws Exception
   */
  @Test
  public void testReallyDeleteNews() throws Exception {
    NewsListener newsListener = null;
    try {

      /* Add initial News */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      INews news1 = fFactory.createNews(null, feed, new Date());
      news1.setTitle("News1 Title");
      INews news2 = fFactory.createNews(null, feed, new Date());
      news2.setTitle("News2 Title");
      INews news3 = fFactory.createNews(null, feed, new Date());
      news3.setTitle("News3 Title");

      final URI news1Link = new URI("http://www.news1.com/index.html");
      final URI news2Link = new URI("http://www.news2.com/index.html");
      final URI news3Link = new URI("http://www.news3.com/index.html");
      news1.setLink(news1Link);
      news2.setLink(news2Link);
      news3.setLink(news3Link);
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      assertEquals(3, DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().size());

      /* Mark 2 News as Deleted and save News */
      news1 = DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().get(0);
      news1.setState(INews.State.DELETED);
      news2 = DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().get(1);
      news2.setState(INews.State.DELETED);
      news3 = DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().get(2);
      news3.setState(INews.State.READ);

      final boolean newsUpdatedEvents[] = new boolean[2];

      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            INews news = event.getEntity();
            if (news.getLink().equals(news1Link))
              newsUpdatedEvents[0] = true;
            else if (news.getLink().equals(news2Link))
              newsUpdatedEvents[1] = true;
          }
        }
      };

      DynamicDAO.addEntityListener(INews.class, newsListener);

      NewsReference newsReference1 = new NewsReference(DynamicDAO.save(news1).getId());
      NewsReference newsReference2 = new NewsReference(DynamicDAO.save(news2).getId());
      NewsReference newsReference3 = new NewsReference(DynamicDAO.save(news3).getId());

      assertEquals(INews.State.DELETED, DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().get(0).getState());
      assertEquals(INews.State.DELETED, DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().get(1).getState());
      assertEquals(INews.State.READ, DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().get(2).getState());

      /* Check Deleted News now being Deleted from DB */
      IFeed emptyFeed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      IFeed savedFeed = feedRef.resolve();
      MergeResult mergeResult = savedFeed.mergeAndCleanUp(emptyFeed);
      TestUtils.saveFeed(mergeResult);

      feed = null;
      news1 = null;
      news2 = null;
      news3 = null;
      System.gc();

      /* Asserts follow */
      assertEquals(1, DynamicDAO.load(IFeed.class, feedRef.getId()).getNews().size());
      assertNull(DynamicDAO.load(INews.class, newsReference1.getId()));
      assertNull(DynamicDAO.load(INews.class, newsReference2.getId()));
      assertNotNull(DynamicDAO.load(INews.class, newsReference3.getId()));

      for (int i = 0; i < newsUpdatedEvents.length; i++)
        if (!newsUpdatedEvents[i])
          fail("Missing newsUpdated event in NewsListener!");
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on Folder persistence
   * operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatFolderEvents() throws Exception {
    FolderListener folderListener = null;
    try {
      /* Add */
      final FolderReference rootFolder = new FolderReference(DynamicDAO.save(fFactory.createFolder(null, null, "Root")).getId());

      IFolder folder = fFactory.createFolder(null, rootFolder.resolve(), "Folder");
      final boolean folderEvents[] = new boolean[3];
      final FolderReference folderReference[] = new FolderReference[1];
      folderListener = new FolderListener() {
        boolean updateEventOccurred = false;

        @Override
        public void entitiesAdded(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            folderEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (folderReference[0].references(event.getEntity()))
              folderEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            if (updateEventOccurred)
              return;

            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (folderReference[0].references(event.getEntity()))
              folderEvents[2] = true;

            updateEventOccurred = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IFolder.class, folderListener);
      folderReference[0] = new FolderReference(DynamicDAO.save(folder).getId());

      /* Update */
      folder = folderReference[0].resolve();
      folder.setName("Folder Updated");
      DynamicDAO.save(folder);

      /* Delete */
      DynamicDAO.delete(folderReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing folderAdded Event", folderEvents[0]);
      assertTrue("Missing folderUpdated Event", folderEvents[2]);
      assertTrue("Missing folderDeleted Event", folderEvents[1]);
    } finally {
      /* Cleanup */
      if (folderListener != null)
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on SearchMark persistence
   * operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatSearchMarkEvents() throws Exception {
    SearchMarkListener searchMarkListener = null;
    try {
      /* Add */
      final FolderReference folderRef = new FolderReference(DynamicDAO.save(fFactory.createFolder(null, null, "Folder")).getId());
      ISearchMark searchMark = fFactory.createSearchMark(null, folderRef.resolve(), "SearchMark");
      final boolean searchMarkEvents[] = new boolean[3];
      final SearchMarkReference searchMarkReference[] = new SearchMarkReference[1];
      searchMarkListener = new SearchMarkListener() {
        @Override
        public void entitiesAdded(Set<SearchMarkEvent> events) {
          for (SearchMarkEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertEquals(folderRef.getId(), event.getEntity().getParent().getId().longValue());
            searchMarkEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<SearchMarkEvent> events) {
          for (SearchMarkEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertEquals(folderRef.getId(), event.getEntity().getParent().getId().longValue());
            if (searchMarkReference[0].references(event.getEntity()))
              searchMarkEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<SearchMarkEvent> events) {
          for (SearchMarkEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertEquals(folderRef.getId(), event.getEntity().getParent().getId().longValue());
            if (searchMarkReference[0].references(event.getEntity()))
              searchMarkEvents[2] = true;
          }
        }

        @Override
        public void newsChanged(Set<SearchMarkEvent> events) {
          fail("Unexpected event");
        }
      };
      DynamicDAO.addEntityListener(ISearchMark.class, searchMarkListener);
      searchMarkReference[0] = new SearchMarkReference(DynamicDAO.save(searchMark).getId());

      /* Update */
      searchMark = searchMarkReference[0].resolve();
      searchMark.setName("SearchMark Updated");
      DynamicDAO.save(searchMark);

      /* Delete */
      DynamicDAO.delete(searchMarkReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing searchMarkAdded Event", searchMarkEvents[0]);
      assertTrue("Missing searchMarkUpdated Event", searchMarkEvents[2]);
      assertTrue("Missing searchMarkDeleted Event", searchMarkEvents[1]);
    } finally {
      /* Cleanup */
      if (searchMarkListener != null)
        DynamicDAO.removeEntityListener(ISearchMark.class, searchMarkListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on SearchCondition persistence
   * operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatSearchConditionEvents() throws Exception {
    SearchConditionListener searchConditionListener = null;
    try {
      /* Add */
      FolderReference folderRef = new FolderReference(DynamicDAO.save(fFactory.createFolder(null, null, "Folder")).getId());
      ISearchMark searchMark = DynamicDAO.save(fFactory.createSearchMark(null, folderRef.resolve(), "SearchMark"));
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
      ISearchCondition searchCondition = fFactory.createSearchCondition(null, searchMark, field, SearchSpecifier.CONTAINS, "Foo");
      final boolean searchConditionEvents[] = new boolean[3];
      final SearchConditionReference searchConditionReference[] = new SearchConditionReference[1];
      searchConditionListener = new SearchConditionListener() {
        @Override
        public void entitiesAdded(Set<SearchConditionEvent> events) {
          for (SearchConditionEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            searchConditionEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<SearchConditionEvent> events) {
          for (SearchConditionEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (searchConditionReference[0].references(event.getEntity()))
              searchConditionEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<SearchConditionEvent> events) {
          for (SearchConditionEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (searchConditionReference[0].references(event.getEntity()))
              searchConditionEvents[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ISearchCondition.class, searchConditionListener);
      searchConditionReference[0] = new SearchConditionReference(DynamicDAO.save(searchCondition).getId());
      DynamicDAO.removeEntityListener(ISearchCondition.class, searchConditionListener);
      DynamicDAO.save(searchMark);
      searchMark = null;
      System.gc();

      /* Update */
      DynamicDAO.addEntityListener(ISearchCondition.class, searchConditionListener);
      searchCondition = searchConditionReference[0].resolve();
      searchCondition.setValue("Bar");
      searchCondition.setSpecifier(SearchSpecifier.CONTAINS_NOT);
      DynamicDAO.save(searchCondition);

      /* Delete */
      DynamicDAO.delete(searchConditionReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing searchConditionAdded Event", searchConditionEvents[0]);
      assertTrue("Missing searchConditionUpdated Event", searchConditionEvents[2]);
      assertTrue("Missing searchConditionDeleted Event", searchConditionEvents[1]);

    } finally {
      if (searchConditionListener != null)
        DynamicDAO.removeEntityListener(ISearchCondition.class, searchConditionListener);
    }

  }

  /**
   * Test added, updated and deleted Events sent on BookMark persistence
   * operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatBookMarkEvents() throws Exception {
    BookMarkListener bookMarkListener = null;
    try {
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      /* Add */
      DynamicDAO.save(feed);
      final FolderReference folderRef = new FolderReference(DynamicDAO.save(fFactory.createFolder(null, null, "Folder")).getId());
      IBookMark bookMark = fFactory.createBookMark(null, folderRef.resolve(), new FeedLinkReference(feed.getLink()), "BookMark");
      final boolean bookMarkEvents[] = new boolean[3];
      final BookMarkReference bookMarkReference[] = new BookMarkReference[1];
      bookMarkListener = new BookMarkListener() {
        @Override
        public void entitiesAdded(Set<BookMarkEvent> events) {
          for (BookMarkEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertEquals(folderRef.getId(), event.getEntity().getParent().getId().longValue());
            bookMarkEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<BookMarkEvent> events) {
          for (BookMarkEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertEquals(folderRef.getId(), event.getEntity().getParent().getId().longValue());
            if (bookMarkReference[0].references(event.getEntity()))
              bookMarkEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<BookMarkEvent> events) {
          for (BookMarkEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertEquals(folderRef.getId(), event.getEntity().getParent().getId().longValue());
            if (bookMarkReference[0].references(event.getEntity()))
              bookMarkEvents[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IBookMark.class, bookMarkListener);
      bookMarkReference[0] = new BookMarkReference(DynamicDAO.save(bookMark).getId());

      /* Update */
      bookMark = bookMarkReference[0].resolve();
      bookMark.setName("BookMark Updated");
      DynamicDAO.save(bookMark);

      /* Delete */
      DynamicDAO.delete(bookMarkReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing bookMarkAdded Event", bookMarkEvents[0]);
      assertTrue("Missing bookMarkUpdated Event", bookMarkEvents[2]);
      assertTrue("Missing bookMarkDeleted Event", bookMarkEvents[1]);

    } finally {
      /* Cleanup */
      if (bookMarkListener != null)
        DynamicDAO.removeEntityListener(IBookMark.class, bookMarkListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on Feed persistence operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatFeedEvents() throws Exception {
    FeedListener feedListener = null;
    try {
      /* Add */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      final boolean feedEvents[] = new boolean[3];
      final FeedReference feedReference[] = new FeedReference[1];
      feedListener = new FeedListener() {
        @Override
        public void entitiesAdded(Set<FeedEvent> events) {
          for (FeedEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            feedEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<FeedEvent> events) {
          for (FeedEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (feedReference[0].references(event.getEntity()))
              feedEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<FeedEvent> events) {
          for (FeedEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (feedReference[0].references(event.getEntity()))
              feedEvents[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IFeed.class, feedListener);
      feedReference[0] = new FeedReference(DynamicDAO.save(feed).getId());

      /* Update */
      feed = feedReference[0].resolve();
      feed.setTitle("Feed Updated");
      DynamicDAO.save(feed);

      /* Delete */
      DynamicDAO.delete(feedReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing feedAdded Event", feedEvents[0]);
      assertTrue("Missing feedUpdated Event", feedEvents[2]);
      assertTrue("Missing feedDeleted Event", feedEvents[1]);

    } finally {
      /* Cleanup */
      if (feedListener != null)
        DynamicDAO.removeEntityListener(IFeed.class, feedListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on News persistence operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatNewsEvents() throws Exception {
    NewsListener newsListener = null;
    try {
      /* Add */
      final IFeed feed = DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed.com")));
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News");
      final boolean newsEvents[] = new boolean[3];
      final NewsReference newsReference[] = new NewsReference[1];
      newsListener = new NewsListener() {
        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertLinkEquals(feed.getLink(), event.getEntity().getFeedReference().getLink());
            newsEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertLinkEquals(feed.getLink(), event.getEntity().getFeedReference().getLink());
            if (newsReference[0].references(event.getEntity()))
              newsEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            assertLinkEquals(feed.getLink(), event.getEntity().getFeedReference().getLink());
            if (newsReference[0].references(event.getEntity()))
              newsEvents[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);
      newsReference[0] = new NewsReference(DynamicDAO.save(news).getId());

      /* Update */
      news = newsReference[0].resolve();
      news.setTitle("News Updated");
      DynamicDAO.save(news);

      /* Delete */
      DynamicDAO.delete(newsReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing newsAdded Event", newsEvents[0]);
      assertTrue("Missing newsUpdated Event", newsEvents[2]);
      assertTrue("Missing newsDeleted Event", newsEvents[1]);

    } finally {
      /* Cleanup */
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
    }
  }

  private void assertLinkEquals(URI expected, URI actual) {
    assertEquals(expected.toString(), actual.toString());
  }

  /**
   * Test added, updated and deleted Events sent on Attachment persistence
   * operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatAttachmentEvents() throws Exception {
    AttachmentListener attachmentListener = null;
    try {
      /* Add */
      FeedReference feedRef = new FeedReference(DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed1.com"))).getId());
      NewsReference newsRef = new NewsReference(DynamicDAO.save(fFactory.createNews(null, feedRef.resolve(), new Date())).getId());
      IAttachment attachment = fFactory.createAttachment(null, newsRef.resolve());
      attachment.setLink(new URI("http://www.attachment.com"));
      final boolean attachmentEvents[] = new boolean[3];
      final AttachmentReference attachmentReference[] = new AttachmentReference[1];
      attachmentListener = new AttachmentAdapter() {
        @Override
        public void entitiesAdded(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            attachmentEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (attachmentReference[0].references(event.getEntity()))
              attachmentEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (attachmentReference[0].references(event.getEntity()))
              attachmentEvents[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);
      attachmentReference[0] = new AttachmentReference(DynamicDAO.save(attachment).getId());

      /* Update */
      attachment = attachmentReference[0].resolve();
      attachment.setType("MP3");
      DynamicDAO.save(attachment);

      /* Delete */
      DynamicDAO.delete(attachmentReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing attachmentAdded Event", attachmentEvents[0]);
      assertTrue("Missing attachmentUpdated Event", attachmentEvents[2]);
      assertTrue("Missing attachmentDeleted Event", attachmentEvents[1]);

    } finally {
      /* Cleanup */
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on Category persistence
   * operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatCategoryEvents() throws Exception {
    CategoryListener categoryListener = null;
    try {
      /* Add */
      FeedReference feedRef = new FeedReference(DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed2.com"))).getId());
      NewsReference newsRef = new NewsReference(DynamicDAO.save(fFactory.createNews(null, feedRef.resolve(), new Date())).getId());
      ICategory category1 = fFactory.createCategory(null, feedRef.resolve());
      category1.setName("Category");
      ICategory category2 = fFactory.createCategory(null, newsRef.resolve());
      category2.setName("Category");
      final boolean categoryEvents[] = new boolean[6];
      final CategoryReference categoryReference[] = new CategoryReference[2];
      categoryListener = new CategoryListener() {
        @Override
        public void entitiesAdded(Set<CategoryEvent> events) {
          for (CategoryEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (categoryEvents[0])
              categoryEvents[1] = true;
            categoryEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<CategoryEvent> events) {
          for (CategoryEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (categoryReference[0].references(event.getEntity()))
              categoryEvents[2] = true;
            else if (categoryReference[1].references(event.getEntity()))
              categoryEvents[3] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<CategoryEvent> events) {
          for (CategoryEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (categoryReference[0].references(event.getEntity()))
              categoryEvents[4] = true;
            else if (categoryReference[1].references(event.getEntity()))
              categoryEvents[5] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ICategory.class, categoryListener);
      categoryReference[0] = new CategoryReference(DynamicDAO.save(category1).getId());
      categoryReference[1] = new CategoryReference(DynamicDAO.save(category2).getId());

      /* Update */
      category1 = categoryReference[0].resolve();
      category1.setName("Category Updated");
      category2 = categoryReference[1].resolve();
      category2.setName("Category Updated");
      DynamicDAO.save(category1);
      DynamicDAO.save(category2);

      /* Delete */
      DynamicDAO.delete(categoryReference[0].resolve());
      DynamicDAO.delete(categoryReference[1].resolve());

      /* Asserts Follow */
      assertTrue("Missing categoryAdded Event", categoryEvents[0]);
      assertTrue("Missing categoryAdded Event", categoryEvents[1]);
      assertTrue("Missing categoryUpdated Event", categoryEvents[4]);
      assertTrue("Missing categoryUpdated Event", categoryEvents[5]);
      assertTrue("Missing categoryDeleted Event", categoryEvents[2]);
      assertTrue("Missing categoryDeleted Event", categoryEvents[3]);

    } finally {
      /* Cleanup */
      if (categoryListener != null)
        DynamicDAO.removeEntityListener(ICategory.class, categoryListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on Person persistence
   * operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatPersonEvents() throws Exception {
    PersonListener personListener = null;
    try {
      /* Add */
      FeedReference feedRef = new FeedReference(DynamicDAO.save(fFactory.createFeed(null, new URI("http://www.feed4.com"))).getId());
      NewsReference newsRef = new NewsReference(DynamicDAO.save(fFactory.createNews(null, feedRef.resolve(), new Date())).getId());
      IPerson person1 = fFactory.createPerson(null, feedRef.resolve());
      person1.setName("Person1");
      IPerson person2 = fFactory.createPerson(null, newsRef.resolve());
      person2.setName("Person2");
      final boolean personEvents[] = new boolean[6];
      final PersonReference personReference[] = new PersonReference[2];
      personListener = new PersonListener() {
        @Override
        public void entitiesAdded(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (personEvents[0])
              personEvents[1] = true;
            personEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (personReference[0].references(event.getEntity()))
              personEvents[2] = true;
            else if (personReference[1].references(event.getEntity()))
              personEvents[3] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (personReference[0].references(event.getEntity()))
              personEvents[4] = true;
            else if (personReference[1].references(event.getEntity()))
              personEvents[5] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IPerson.class, personListener);
      personReference[0] = new PersonReference(DynamicDAO.save(person1).getId());
      personReference[1] = new PersonReference(DynamicDAO.save(person2).getId());

      /* Update */
      person1 = personReference[0].resolve();
      person1.setName("Person Updated");
      person2 = personReference[1].resolve();
      person2.setName("Person Updated");
      DynamicDAO.save(person1);
      DynamicDAO.save(person2);

      /* Delete */
      DynamicDAO.delete(personReference[0].resolve());
      DynamicDAO.delete(personReference[1].resolve());

      /* Asserts Follow */
      assertTrue("Missing personAdded Event", personEvents[0]);
      assertTrue("Missing personAdded Event", personEvents[1]);
      assertTrue("Missing personUpdated Event", personEvents[4]);
      assertTrue("Missing personUpdated Event", personEvents[5]);
      assertTrue("Missing personDeleted Event", personEvents[2]);
      assertTrue("Missing personDeleted Event", personEvents[3]);

    } finally {
      /* Cleanup */
      if (personListener != null)
        DynamicDAO.removeEntityListener(IPerson.class, personListener);
    }
  }

  /**
   * Test added, updated and deleted Events sent on Label persistence operations
   *
   * @throws Exception
   */
  @Test
  public void testFlatLabelEvents() throws Exception {
    LabelListener labelListener = null;
    try {
      /* Add */
      ILabel label = fFactory.createLabel(null, "Label Name");
      final boolean labelEvents[] = new boolean[3];
      final LabelReference labelReference[] = new LabelReference[1];
      labelListener = new LabelListener() {
        @Override
        public void entitiesAdded(Set<LabelEvent> events) {
          for (LabelEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            labelEvents[0] = true;
          }
        }

        @Override
        public void entitiesDeleted(Set<LabelEvent> events) {
          for (LabelEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (labelReference[0].references(event.getEntity()))
              labelEvents[1] = true;
          }
        }

        @Override
        public void entitiesUpdated(Set<LabelEvent> events) {
          for (LabelEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (labelReference[0].references(event.getEntity()))
              labelEvents[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ILabel.class, labelListener);
      labelReference[0] = new LabelReference(DynamicDAO.save(label).getId());

      /* Update */
      label = labelReference[0].resolve();
      label.setColor("255,255,128");
      DynamicDAO.save(label);

      /* Delete */
      DynamicDAO.delete(labelReference[0].resolve());

      /* Asserts Follow */
      assertTrue("Missing labelAdded Event", labelEvents[0]);
      assertTrue("Missing labelUpdated Event", labelEvents[2]);
      assertTrue("Missing labelDeleted Event", labelEvents[1]);

    } finally {
      /* Cleanup */
      if (labelListener != null)
        DynamicDAO.removeEntityListener(ILabel.class, labelListener);
    }
  }

  /**
   * Test adding Properties to Types.
   *
   * @throws Exception
   */
  @Test
  public void testTypeProperties() throws Exception {
    try {

      /* Add Properties to a Folder */
      IFolder folder = fFactory.createFolder(null, null, "Folder");
      folder.setProperty("String", "Foo");
      folder.setProperty("Integer", 1);
      folder.setProperty("Boolean", true);
      folder.setProperty("Double", 2.2D);
      folder.setProperty("Float", 3.3F);
      FolderReference folderRef = new FolderReference(DynamicDAO.save(folder).getId());
      folder = folderRef.resolve();
      assertEquals("Foo", folder.getProperty("String"));
      assertEquals(1, folder.getProperty("Integer"));
      assertEquals(true, folder.getProperty("Boolean"));
      assertEquals(2.2D, folder.getProperty("Double"));
      assertEquals(3.3F, folder.getProperty("Float"));

      /* Add Properties to a Feed */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.myfeed.com"));
      feed.setProperty("String", "Foo");
      feed.setProperty("Integer", 1);
      feed.setProperty("Boolean", true);
      feed.setProperty("Double", 2.2D);
      feed.setProperty("Float", 3.3F);
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());
      feed = feedRef.resolve();
      assertEquals("Foo", feed.getProperty("String"));
      assertEquals(1, feed.getProperty("Integer"));
      assertEquals(true, feed.getProperty("Boolean"));
      assertEquals(2.2D, feed.getProperty("Double"));
      assertEquals(3.3F, feed.getProperty("Float"));

      /* Add Properties to a BookMark */
      IBookMark bookMark = fFactory.createBookMark(null, folderRef.resolve(), new FeedLinkReference(feed.getLink()), "BookMark");
      bookMark.setProperty("String", "Foo");
      bookMark.setProperty("Integer", 1);
      bookMark.setProperty("Boolean", true);
      bookMark.setProperty("Double", 2.2D);
      bookMark.setProperty("Float", 3.3F);
      BookMarkReference bookMarkRef = new BookMarkReference(DynamicDAO.save(bookMark).getId());
      bookMark = bookMarkRef.resolve();
      assertEquals("Foo", bookMark.getProperty("String"));
      assertEquals(1, bookMark.getProperty("Integer"));
      assertEquals(true, bookMark.getProperty("Boolean"));
      assertEquals(2.2D, bookMark.getProperty("Double"));
      assertEquals(3.3F, bookMark.getProperty("Float"));

      /* Add Properties to a News */
      INews news = fFactory.createNews(null, feedRef.resolve(), new Date());
      news.setProperty("String", "Foo");
      news.setProperty("Integer", 1);
      news.setProperty("Boolean", true);
      news.setProperty("Double", 2.2D);
      news.setProperty("Float", 3.3F);
      NewsReference newsRef = new NewsReference(DynamicDAO.save(news).getId());
      news = newsRef.resolve();
      assertEquals("Foo", news.getProperty("String"));
      assertEquals(1, news.getProperty("Integer"));
      assertEquals(true, news.getProperty("Boolean"));
      assertEquals(2.2D, news.getProperty("Double"));
      assertEquals(3.3F, news.getProperty("Float"));

      /* Add Properties to an Attachment */
      IAttachment attachment = fFactory.createAttachment(null, newsRef.resolve());
      attachment.setLink(new URI("http://www.attachment.com"));
      attachment.setProperty("String", "Foo");
      attachment.setProperty("Integer", 1);
      attachment.setProperty("Boolean", true);
      attachment.setProperty("Double", 2.2D);
      attachment.setProperty("Float", 3.3F);
      AttachmentReference attachmentRef = new AttachmentReference(DynamicDAO.save(attachment).getId());
      attachment = attachmentRef.resolve();
      assertEquals("Foo", attachment.getProperty("String"));
      assertEquals(1, attachment.getProperty("Integer"));
      assertEquals(true, attachment.getProperty("Boolean"));
      assertEquals(2.2D, attachment.getProperty("Double"));
      assertEquals(3.3F, attachment.getProperty("Float"));

    } catch (PersistenceException e) {
      TestUtils.fail(e);
    }
  }

  /**
   * Test Adding, Deleting a Feed with no News.
   *
   * @throws Exception
   */
  @Test
  public void testAddDeleteFeedWithNoNews() throws Exception {
    NewsListener feedListener = null;
    try {
      IFeed feed = Owl.getModelFactory().createFeed(null, new URI("http://www.feed.com"));
      final boolean addedEvent[] = new boolean[1];
      final boolean deletedEvent[] = new boolean[1];

      feedListener = new NewsAdapter() {
        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          addedEvent[0] = true;
        }

        @Override
        public void entitiesDeleted(Set<NewsEvent> events) {
          deletedEvent[0] = true;
        }
      };
      DynamicDAO.addEntityListener(INews.class, feedListener);

      feed = DynamicDAO.save(feed);
      DynamicDAO.delete(feed);

      if (addedEvent[0])
        fail("Unexpected newsAdded Event for Feed with 0 News");
      if (deletedEvent[0])
        fail("Unexpected newsDeleted Event for Feed with 0 News");

    } catch (PersistenceException e) {
      TestUtils.fail(e);
    } finally {
      if (feedListener != null)
        DynamicDAO.removeEntityListener(INews.class, feedListener);
    }
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testSetNewsState() throws Exception {
    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("http://www.feed.com"));

    Owl.getModelFactory().createNews(null, feed, new Date());
    Owl.getModelFactory().createNews(null, feed, new Date());
    Owl.getModelFactory().createNews(null, feed, new Date());

    FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

    NewsReference news1 = new NewsReference(feedRef.resolve().getNews().get(0).getId());
    NewsReference news2 = new NewsReference(feedRef.resolve().getNews().get(1).getId());
    NewsReference news3 = new NewsReference(feedRef.resolve().getNews().get(2).getId());

    List<NewsReference> news = new ArrayList<NewsReference>();
    news.add(news1);
    news.add(news2);

    assertEquals(news1.resolve().getState(), INews.State.NEW);
    assertEquals(news2.resolve().getState(), INews.State.NEW);
    assertEquals(news3.resolve().getState(), INews.State.NEW);

    for (NewsReference reference : news) {
      INews newsitem = reference.resolve();
      newsitem.setState(INews.State.UNREAD);
      DynamicDAO.save(newsitem);
    }

    assertEquals(news1.resolve().getState(), INews.State.UNREAD);
    assertEquals(news2.resolve().getState(), INews.State.UNREAD);
    assertEquals(news3.resolve().getState(), INews.State.NEW);

    for (NewsReference reference : news) {
      INews newsitem = reference.resolve();
      newsitem.setState(INews.State.READ);
      DynamicDAO.save(newsitem);
    }

    assertEquals(news1.resolve().getState(), INews.State.READ);
    assertEquals(news2.resolve().getState(), INews.State.READ);
    assertEquals(news3.resolve().getState(), INews.State.NEW);

    for (NewsReference reference : news) {
      INews newsitem = reference.resolve();
      newsitem.setState(INews.State.DELETED);
      DynamicDAO.save(newsitem);
    }

    assertEquals(news1.resolve().getState(), INews.State.DELETED);
    assertEquals(news2.resolve().getState(), INews.State.DELETED);
    assertEquals(news3.resolve().getState(), INews.State.NEW);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testLoadNewsStates() throws Exception {
    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("http://www.feed.com"));
    FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

    for (int i = 0; i < 5; i++) {
      INews news = Owl.getModelFactory().createNews(null, feed, new Date());
      DynamicDAO.save(news);
      news.setState(INews.State.NEW);
      DynamicDAO.save(news);
    }

    for (int i = 0; i < 4; i++) {
      INews news = Owl.getModelFactory().createNews(null, feed, new Date());
      DynamicDAO.save(news);
      news.setState(INews.State.UPDATED);
      DynamicDAO.save(news);
    }

    for (int i = 0; i < 3; i++) {
      INews news = Owl.getModelFactory().createNews(null, feed, new Date());
      DynamicDAO.save(news);
      news.setState(INews.State.UNREAD);
      DynamicDAO.save(news);
    }

    for (int i = 0; i < 2; i++) {
      INews news = Owl.getModelFactory().createNews(null, feed, new Date());
      DynamicDAO.save(news);
      news.setState(INews.State.READ);
      DynamicDAO.save(news);
    }

    for (int i = 0; i < 1; i++) {
      INews news = Owl.getModelFactory().createNews(null, feed, new Date());
      DynamicDAO.save(news);
      news.setState(INews.State.HIDDEN);
      DynamicDAO.save(news);
    }

    int newCount = 0, updatedCount = 0, unreadCount = 0, readCount = 0, hiddenCount = 0;

    List<State> states = new ArrayList<State>();

    feed = feedRef.resolve();
    List<INews> news = feed.getNews();

    for (INews newsitem : news) {
      states.add(newsitem.getState());
    }

    for (State state : states) {
      if (state == INews.State.NEW)
        newCount++;
      else if (state == INews.State.UPDATED)
        updatedCount++;
      else if (state == INews.State.UNREAD)
        unreadCount++;
      else if (state == INews.State.READ)
        readCount++;
      else if (state == INews.State.HIDDEN)
        hiddenCount++;
    }

    assertEquals(newCount, 5);
    assertEquals(updatedCount, 4);
    assertEquals(unreadCount, 3);
    assertEquals(readCount, 2);
    assertEquals(hiddenCount, 1);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testDeleteTypeFromDeleteParent() throws Exception {

    /* Folder, BookMark, Feed, News (Folder Deleted) */
    {
      IFolder root = fFactory.createFolder(null, null, "Root");
      root = DynamicDAO.save(root);

      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      fFactory.createNews(null, feed, new Date());
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      IBookMark mark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "BookMark");
      root = DynamicDAO.save(root);
      mark = (IBookMark) root.getMarks().get(0);

      assertEquals(1, new FeedReference(feed.getId()).resolve().getNews().size());

      NewsReference newsRef = new NewsReference(feedRef.resolve().getNews().get(0).getId());

      DynamicDAO.delete(root);

      assertNull("Expected this Entity to be NULL", new FolderReference(root.getId()).resolve());
      assertNull("Expected this Entity to be NULL", new BookMarkReference(mark.getId()).resolve());
      assertNull("Expected this Entity to be NULL", feedRef.resolve());
      assertNull("Expected this Entity to be NULL", newsRef.resolve());
    }

    /* Root Folder, Folder, BookMark, Feed, News (Folder Deleted) */
    {
      IFolder root = fFactory.createFolder(null, null, "Root");
      root = DynamicDAO.save(root);

      IFolder folder = fFactory.createFolder(null, root, "Folder");
      folder = DynamicDAO.save(folder);

      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed2.com"));
      fFactory.createNews(null, feed, new Date());
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      IBookMark mark = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
      folder = DynamicDAO.save(folder);
      mark = (IBookMark) folder.getMarks().get(0);

      assertEquals(1, new FeedReference(feed.getId()).resolve().getNews().size());

      NewsReference newsRef = new NewsReference(feedRef.resolve().getNews().get(0).getId());

      DynamicDAO.delete(folder);

      assertNull("Expected this Entity to be NULL", new FolderReference(folder.getId()).resolve());
      assertNull("Expected this Entity to be NULL", new BookMarkReference(mark.getId()).resolve());
      assertNull("Expected this Entity to be NULL", feedRef.resolve());
      assertNull("Expected this Entity to be NULL", newsRef.resolve());
    }

    /* Root Folder, Folder, BookMark, Feed, News (Folder Deleted #2) */
    {
      IFolder root = fFactory.createFolder(null, null, "Root");
      root = DynamicDAO.save(root);

      IFolder folder = fFactory.createFolder(null, root, "Folder");
      folder = DynamicDAO.save(folder);

      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed3.com"));
      fFactory.createNews(null, feed, new Date());
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      IBookMark mark = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
      folder = DynamicDAO.save(folder);
      mark = (IBookMark) folder.getMarks().get(0);

      assertEquals(1, new FeedReference(feed.getId()).resolve().getNews().size());

      NewsReference newsRef = new NewsReference(feedRef.resolve().getNews().get(0).getId());

      /* Delete by calling delete */
      DynamicDAO.delete(folder);

      final long rootFolderId = root.getId();
      FolderListener folderListener = new FolderAdapter() {
        @Override
        public void entitiesUpdated(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            if (event.getEntity().getId() == rootFolderId)
              assertTrue(event.isRoot());
            else
              assertFalse(event.isRoot());
          }
        }
      };
      DynamicDAO.addEntityListener(IFolder.class, folderListener);
      try {
        DynamicDAO.save(root);
      } finally {
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      }

      assertNull("Expected this Entity to be NULL", new FolderReference(folder.getId()).resolve());
      assertNull("Expected this Entity to be NULL", new BookMarkReference(mark.getId()).resolve());
      assertNull("Expected this Entity to be NULL", feedRef.resolve());
      assertNull("Expected this Entity to be NULL", newsRef.resolve());
    }

    /* Folder, BookMark, Feed, News (BookMark Deleted) */
    {
      IFolder root = fFactory.createFolder(null, null, "Root");
      root = DynamicDAO.save(root);

      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed4.com"));
      fFactory.createNews(null, feed, new Date());
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      IBookMark mark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "BookMark");
      root = DynamicDAO.save(root);
      mark = (IBookMark) root.getMarks().get(0);

      assertEquals(1, new FeedReference(feed.getId()).resolve().getNews().size());

      NewsReference newsRef = new NewsReference(feedRef.resolve().getNews().get(0).getId());

      DynamicDAO.delete(mark);

      assertNull("Expected this Entity to be NULL", new BookMarkReference(mark.getId()).resolve());
      assertNull("Expected this Entity to be NULL", feedRef.resolve());
      assertNull("Expected this Entity to be NULL", newsRef.resolve());
    }

    /* Folder, BookMark, Feed, News (BookMark Deleted #2) */
    {
      IFolder root = fFactory.createFolder(null, null, "Root");
      root = DynamicDAO.save(root);

      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed5.com"));
      fFactory.createNews(null, feed, new Date());
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      IBookMark mark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "BookMark");
      root = DynamicDAO.save(root);
      mark = (IBookMark) root.getMarks().get(0);

      assertEquals(1, new FeedReference(feed.getId()).resolve().getNews().size());

      NewsReference newsRef = new NewsReference(feedRef.resolve().getNews().get(0).getId());

      /* Delete by calling delete */
      DynamicDAO.delete(mark);

      assertNull("Expected this Entity to be NULL", new BookMarkReference(mark.getId()).resolve());
      assertNull("Expected this Entity to be NULL", feedRef.resolve());
      assertNull("Expected this Entity to be NULL", newsRef.resolve());
    }

    /* Feed, News (Feed Deleted) */
    {
      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed6.com"));
      fFactory.createNews(null, feed, new Date());
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      assertEquals(1, new FeedReference(feed.getId()).resolve().getNews().size());

      NewsReference newsRef = new NewsReference(feedRef.resolve().getNews().get(0).getId());

      DynamicDAO.delete(feedRef.resolve());

      assertNull("Expected this Entity to be NULL", feedRef.resolve());
      assertNull("Expected this Entity to be NULL", newsRef.resolve());
    }
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void testNoUpdateEventForDeletedChildsOfSavedParent() throws Exception {
    FolderAdapter folderListener = null;

    try {
      IFolder root = fFactory.createFolder(null, null, "Root");
      root = DynamicDAO.save(root);

      IFolder folder1 = fFactory.createFolder(null, root, "Folder #1");
      root = DynamicDAO.save(root);
      folder1 = root.getFolders().get(0);

      IFolder folder2 = fFactory.createFolder(null, root, "Folder #2");
      root = DynamicDAO.save(root);
      folder2 = root.getFolders().get(1);

      IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      fFactory.createNews(null, feed, new Date());
      DynamicDAO.save(feed);

      fFactory.createBookMark(null, folder1, new FeedLinkReference(feed.getLink()), "BookMark");
      folder1 = DynamicDAO.save(folder1);

      assertEquals(1, new FeedReference(feed.getId()).resolve().getNews().size());

      folderListener = new FolderAdapter() {
        @Override
        public void entitiesUpdated(Set<FolderEvent> events) {
          for (FolderEvent folderEvent : events) {
            IFolder folder = folderEvent.getEntity();
            if (folder.getName().startsWith("Folder"))
              fail("Unexpected Event");
          }
        }
      };

      DynamicDAO.addEntityListener(IFolder.class, folderListener);

      root.removeChild(folder1);
      root.removeChild(folder2);
      DynamicDAO.save(root);
    } finally {
      if (folderListener != null)
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
    }
  }

  /**
   * Tests {@link ISearchMarkDAO#load(ISearchCondition)}.
   */
  @Test
  public void testLoadFromSearchCondition() {
    /* Add */
    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Folder"));

    ISearchMark searchMark0 = DynamicDAO.save(fFactory.createSearchMark(null, folder, "SearchMark 0"));
    ISearchField field0 = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    ISearchCondition searchCondition0 = fFactory.createSearchCondition(null, searchMark0, field0, SearchSpecifier.CONTAINS, "Foo");
    DynamicDAO.save(searchCondition0);
    SearchConditionReference searchCondRef0 = new SearchConditionReference(searchCondition0.getId());

    ISearchField field1 = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    ISearchCondition searchCondition1 = fFactory.createSearchCondition(null, searchMark0, field1, SearchSpecifier.BEGINS_WITH, "Bar");
    DynamicDAO.save(searchCondition1);

    DynamicDAO.save(searchMark0);
    Long searchMark0Id = searchMark0.getId();

    ISearchMark searchMark1 = DynamicDAO.save(fFactory.createSearchMark(null, folder, "SearchMark 1"));
    ISearchField field2 = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    ISearchCondition searchCondition2 = fFactory.createSearchCondition(null, searchMark1, field2, SearchSpecifier.CONTAINS, "Foo");
    DynamicDAO.save(searchCondition2);

    DynamicDAO.save(searchMark1);

    searchMark0 = null;
    searchCondition0 = null;
    System.gc();

    /* Verify */
    searchCondition0 = searchCondRef0.resolve();
    ISearchMarkDAO dao = DynamicDAO.getDAO(ISearchMarkDAO.class);
    searchMark0 = dao.load(searchCondition0);
    assertEquals(searchMark0Id, searchMark0.getId());

    assertEquals(searchMark1, dao.load(searchCondition2));
  }

  /**
   * Tests {@link ISearchMarkDAO#visited(ISearchMark)}.
   *
   * @throws Exception
   */
  @Test
  public void testVisited() throws Exception {
    SearchConditionListener listener = new SearchConditionListener() {
      @Override
      public void entitiesAdded(Set<SearchConditionEvent> events) {
        fail("Unexpected event");
      }

      @Override
      public void entitiesDeleted(Set<SearchConditionEvent> events) {
        fail("Unexpected event");
      }

      @Override
      public void entitiesUpdated(Set<SearchConditionEvent> events) {
        fail("Unexpected event");
      }
    };
    try {
      /* Add */
      IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Folder"));

      ISearchMark searchMark = DynamicDAO.save(fFactory.createSearchMark(null, folder, "SearchMark 0"));
      ISearchField field0 = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
      fFactory.createSearchCondition(null, searchMark, field0, SearchSpecifier.CONTAINS, "Foo");

      ISearchField field1 = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
      fFactory.createSearchCondition(null, searchMark, field1, SearchSpecifier.BEGINS_WITH, "Bar");

      DynamicDAO.save(searchMark);

      SearchMarkReference searchMarkRef = new SearchMarkReference(searchMark.getId());
      int popularity = searchMark.getPopularity();
      Date lastVisitDate = searchMark.getLastVisitDate();

      if (lastVisitDate == null) {
        lastVisitDate = new Date();
      }
      Thread.sleep(100);

      ISearchMarkDAO dao = DynamicDAO.getDAO(ISearchMarkDAO.class);
      DynamicDAO.addEntityListener(ISearchCondition.class, listener);
      dao.visited(searchMark);
      searchMark = null;
      System.gc();

      searchMark = searchMarkRef.resolve();
      assertEquals(popularity + 1, searchMark.getPopularity());
      assertTrue(searchMark.getLastVisitDate().compareTo(lastVisitDate) > 0);
      assertTrue(searchMark.getLastVisitDate().compareTo(new Date()) < 0);
    } finally {
      DynamicDAO.removeEntityListener(ISearchCondition.class, listener);
    }
  }

  /**
   * Tests {@link INewsBinDAO#visited(INewsBin)}.
   *
   * @throws Exception
   */
  @Test
  public void testVisitedNewsBin() throws Exception {

    /* Add */
    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Folder"));

    INewsBin bin = DynamicDAO.save(fFactory.createNewsBin(null, folder, "News Bin"));

    IFeed feed = DynamicDAO.save(fFactory.createFeed(null, URI.create("http://www.rssowl.org")));
    INews news1 = fFactory.createNews(null, feed, new Date());
    INews news2 = fFactory.createNews(null, feed, new Date());
    INews news3 = fFactory.createNews(null, feed, new Date());
    DynamicDAO.save(feed);

    DynamicDAO.save(fFactory.createNews(news1, bin));
    DynamicDAO.save(fFactory.createNews(news2, bin));
    DynamicDAO.save(fFactory.createNews(news3, bin));

    DynamicDAO.save(bin);

    NewsBinReference binRef = new NewsBinReference(bin.getId());
    int popularity = bin.getPopularity();
    Date lastVisitDate = bin.getLastVisitDate();

    if (lastVisitDate == null) {
      lastVisitDate = new Date();
    }
    Thread.sleep(100);

    INewsBinDAO dao = DynamicDAO.getDAO(INewsBinDAO.class);
    dao.visited(bin);
    bin = null;
    System.gc();

    bin = binRef.resolve();
    assertEquals(popularity + 1, bin.getPopularity());
    assertTrue(bin.getLastVisitDate().compareTo(lastVisitDate) > 0);
    assertTrue(bin.getLastVisitDate().compareTo(new Date()) < 0);
    assertEquals(3, bin.getNews().size());

    bin = null;
    System.gc();

    bin = binRef.resolve();
    dao.visited(bin);
    dao.visited(bin);
    dao.visited(bin);

    bin = null;
    System.gc();

    bin = binRef.resolve();
    assertEquals(3, bin.getNews().size());
  }

  /**
   * Tests that deleting a label also removes it from all News containing it.
   *
   * @throws Exception
   */
  @Test
  public void testDeleteLabelContainedInNews() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    ILabel label = fFactory.createLabel(null, "Label");
    DynamicDAO.save(label);
    news.addLabel(label);
    DynamicDAO.save(feed);
    assertEquals(1, news.getLabels().size());
    DynamicDAO.delete(label);
    assertEquals(0, news.getLabels().size());

    label = fFactory.createLabel(null, "Another label");
    DynamicDAO.save(label);
    news.addLabel(label);
    NewsReference newsRef = new NewsReference(news.getId());
    news = null;
    feed = null;
    System.gc();
    DynamicDAO.delete(label);
    assertEquals(0, newsRef.resolve().getLabels().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testStateChangeFromNewToUnreadToHiddenInBin() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");
    INewsBin newsBin = fFactory.createNewsBin(null, root, "Bin");

    DynamicDAO.save(root);

    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());

    DynamicDAO.save(feed);

    INews copiedNews = fFactory.createNews(news, newsBin);
    copiedNews.setState(INews.State.UNREAD);

    DynamicDAO.save(copiedNews);
    DynamicDAO.save(newsBin);

    DynamicDAO.getDAO(INewsDAO.class).setState(Collections.singleton(copiedNews), INews.State.HIDDEN, false, false);

    assertEquals(0, newsBin.getNews(INews.State.getVisible()).size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddLabelToNewsDoesNotDeleteDescription() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setDescription("Hello World");

    DynamicDAO.save(feed);

    assertNotNull(news.getDescription());

    ILabel label = fFactory.createLabel(null, "Label");
    DynamicDAO.save(label);

    news.addLabel(label);
    DynamicDAO.save(news);

    assertNotNull(news.getDescription());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSetStateDoesNotAffectHiddenDeletedNews() throws Exception {
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.foo1.com"));
    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setLink(new URI("http://www.news.com"));

    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.foo2.com"));
    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);

    INewsDAO newsDao = DynamicDAO.getDAO(INewsDAO.class);

    newsDao.setState(Collections.singleton(news1), INews.State.READ, true, false);

    assertEquals(INews.State.READ, news1.getState());
    assertEquals(INews.State.READ, news2.getState());

    newsDao.setState(Collections.singleton(news1), INews.State.UNREAD, true, false);

    assertEquals(INews.State.UNREAD, news1.getState());
    assertEquals(INews.State.UNREAD, news2.getState());

    newsDao.setState(Collections.singleton(news1), INews.State.HIDDEN, false, false);

    assertEquals(INews.State.HIDDEN, news1.getState());
    assertEquals(INews.State.UNREAD, news2.getState());

    newsDao.setState(Collections.singleton(news2), INews.State.READ, true, false);

    assertEquals(INews.State.HIDDEN, news1.getState());
    assertEquals(INews.State.READ, news2.getState());

    newsDao.setState(Collections.singleton(news1), INews.State.DELETED, false, false);

    assertEquals(INews.State.DELETED, news1.getState());
    assertEquals(INews.State.READ, news2.getState());

    newsDao.setState(Collections.singleton(news2), INews.State.UNREAD, true, false);

    assertEquals(INews.State.DELETED, news1.getState());
    assertEquals(INews.State.UNREAD, news2.getState());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntitiesToBeIndexedOnRestart_NEW() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    EntitiesToBeIndexedDAOImpl dao = DBHelper.getEntitiesToBeIndexedDAO();
    EntityIdsByEventType outstandingNewsIds = dao.load();
    assertEquals(1, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());

    /* Wait for Indexer */
    waitForIndexer();

    /* Force a Flush */
    runBogusSearch();

    outstandingNewsIds = dao.load();
    assertEquals(0, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntitiesToBeIndexedOnRestart_NEW_to_HIDDEN() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    news.setState(INews.State.HIDDEN);
    DynamicDAO.save(news);

    EntitiesToBeIndexedDAOImpl dao = DBHelper.getEntitiesToBeIndexedDAO();
    EntityIdsByEventType outstandingNewsIds = dao.load();
    assertEquals(1, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(1, outstandingNewsIds.getRemovedEntityRefs().size());

    /* Wait for Indexer */
    waitForIndexer();

    /* Force a Flush */
    runBogusSearch();

    outstandingNewsIds = dao.load();
    assertEquals(0, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntitiesToBeIndexedOnRestart_NEW_to_DELETED() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    news.setState(INews.State.DELETED);
    DynamicDAO.save(news);

    EntitiesToBeIndexedDAOImpl dao = DBHelper.getEntitiesToBeIndexedDAO();
    EntityIdsByEventType outstandingNewsIds = dao.load();
    assertEquals(1, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(1, outstandingNewsIds.getRemovedEntityRefs().size());

    /* Wait for Indexer */
    waitForIndexer();

    /* Force a Flush */
    runBogusSearch();

    outstandingNewsIds = dao.load();
    assertEquals(0, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntitiesToBeIndexedOnRestart_NEW_to_READ() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    news.setState(INews.State.READ);
    DynamicDAO.save(news);

    EntitiesToBeIndexedDAOImpl dao = DBHelper.getEntitiesToBeIndexedDAO();
    EntityIdsByEventType outstandingNewsIds = dao.load();
    assertEquals(1, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(1, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());

    /* Wait for Indexer */
    waitForIndexer();

    /* Force a Flush */
    runBogusSearch();

    outstandingNewsIds = dao.load();
    assertEquals(0, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntitiesToBeIndexedOnRestart_HIDDEN_to_DELETED() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    news.setState(INews.State.HIDDEN);
    DynamicDAO.save(news);

    news.setState(INews.State.DELETED);
    DynamicDAO.save(news);

    EntitiesToBeIndexedDAOImpl dao = DBHelper.getEntitiesToBeIndexedDAO();
    EntityIdsByEventType outstandingNewsIds = dao.load();
    assertEquals(1, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(1, outstandingNewsIds.getRemovedEntityRefs().size());

    /* Wait for Indexer */
    waitForIndexer();

    /* Force a Flush */
    runBogusSearch();

    outstandingNewsIds = dao.load();
    assertEquals(0, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEntitiesToBeIndexedOnRestart_HIDDEN_to_NEW_1() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI("http://www.news.com"));

    DynamicDAO.save(feed);

    news.setState(INews.State.HIDDEN);
    DynamicDAO.save(news);

    news.setState(INews.State.NEW);
    DynamicDAO.save(news);

    EntitiesToBeIndexedDAOImpl dao = DBHelper.getEntitiesToBeIndexedDAO();
    EntityIdsByEventType outstandingNewsIds = dao.load();
    assertEquals(2, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(1, outstandingNewsIds.getRemovedEntityRefs().size());

    /* Wait for Indexer */
    waitForIndexer();

    /* Force a Flush */
    runBogusSearch();

    outstandingNewsIds = dao.load();
    assertEquals(0, outstandingNewsIds.getPersistedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getUpdatedEntityRefs().size());
    assertEquals(0, outstandingNewsIds.getRemovedEntityRefs().size());
  }

  /**
   * @throws InterruptedException
   */
  private void waitForIndexer() throws InterruptedException {
    Thread.sleep(500);
  }

  /* Search to trigger a flush in the Index */
  private void runBogusSearch() {
    ISearchField field1 = fFactory.createSearchField(INews.FEED, INews.class.getName());
    ISearchCondition cond1 = fFactory.createSearchCondition(field1, SearchSpecifier.IS, "http://www.feed.com/feed1.xml");
    Owl.getPersistenceService().getModelSearch().searchNews(Collections.singleton(cond1), false);
  }

  /**
   * See Bug 1041: NPE from News.mergeAttachments
   *
   * @throws Exception
   */
  @Test
  public void testMergeNewsAttachmentsWithNullLink() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.foo.com"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setLink(new URI("http://www.news.com"));
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setLink(new URI("http://www.news.com"));
    fFactory.createAttachment(null, news1);
    DynamicDAO.save(feed);
    fFactory.createAttachment(null, news2);

    news2.merge(news1);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testDeleteSearchFilter() throws Exception {
    ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, INews.class.getName());
    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, true);
    ISearch search = fFactory.createSearch(null);
    search.addSearchCondition(condition);

    ISearchFilter filter = fFactory.createSearchFilter(null, search, "Filter");
    DynamicDAO.save(filter);

    assertNotNull(filter.toReference().resolve());
    assertNotNull(search.toReference().resolve());
    assertNotNull(condition.toReference().resolve());

    DynamicDAO.delete(filter);

    assertNull(filter.toReference().resolve());
    assertNull(search.toReference().resolve());
    assertNull(condition.toReference().resolve());
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore
  public void testSaveBinCausesNoUnwantedEvents() throws Exception {
    SearchConditionListener listener = null;
    try {
      IFolder root = fFactory.createFolder(null, null, "Root");

      IFolder childFolder = fFactory.createFolder(null, root, "Child");

      IFeed feed = fFactory.createFeed(null, new URI("feed"));
      DynamicDAO.save(feed);

      IBookMark bookmark = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");

      INewsBin bin = fFactory.createNewsBin(null, root, "Bin");

      ISearchMark searchMark = fFactory.createSearchMark(null, root, "Search");
      ISearchField field = fFactory.createSearchField(INews.TITLE, INews.class.getName());
      searchMark.addSearchCondition(fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "Foo"));

      DynamicDAO.save(root);

      listener = new SearchConditionListener() {

        @Override
        public void entitiesUpdated(Set<SearchConditionEvent> events) {
          fail("Unexpected Event");
        }

        @Override
        public void entitiesDeleted(Set<SearchConditionEvent> events) {
          fail("Unexpected Event");
        }

        @Override
        public void entitiesAdded(Set<SearchConditionEvent> events) {
          fail("Unexpected Event");
        }
      };
      DynamicDAO.addEntityListener(ISearchCondition.class, listener);

      childFolder.setName("Other");
      DynamicDAO.save(childFolder);

      bookmark.setName("Other");
      DynamicDAO.save(bookmark);

      bin.setName("Other");
      DynamicDAO.save(bin);
    } finally {
      DynamicDAO.removeEntityListener(ISearchCondition.class, listener);
    }
  }
}
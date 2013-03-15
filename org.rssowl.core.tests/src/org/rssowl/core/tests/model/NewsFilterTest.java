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
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.IApplicationService;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.SearchFilterAdapter;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.util.ModelUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests functionality of news filters.
 *
 * @author bpasero
 */
public class NewsFilterTest extends LargeBlockSizeTest {
  private IModelFactory fFactory;
  private IApplicationService fAppService;

  /* IDs of contributed News Actions */
  private static final String MOVE_NEWS_ID = "org.rssowl.core.MoveNewsAction";
  private static final String COPY_NEWS_ID = "org.rssowl.core.CopyNewsAction";
  private static final String MARK_READ_ID = "org.rssowl.core.MarkReadNewsAction";
  private static final String MARK_UNREAD_ID = "org.rssowl.core.MarkUnreadNewsAction";
  private static final String MARK_STICKY_ID = "org.rssowl.core.MarkStickyNewsAction";
  private static final String LABEL_NEWS_ID = "org.rssowl.core.LabelNewsAction";
  private static final String STOP_FILTER_ID = "org.rssowl.core.StopFilterAction";
  private static final String DELETE_NEWS_ID = "org.rssowl.core.DeleteNewsAction";

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl) Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
    fAppService = Owl.getApplicationService();
  }

  /**
   * @throws Exception
   */
  @Test
  public void test_MarkRead_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    final ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MARK_READ_ID);
    filter.addAction(action);

    DynamicDAO.save(filter);

    final AtomicBoolean listenerCalled = new AtomicBoolean();
    SearchFilterAdapter listener = new SearchFilterAdapter() {
      @Override
      public void filterApplied(ISearchFilter f, Collection<INews> news) {
        if (filter.equals(f) && news.size() == 3)
          listenerCalled.set(true);
      }
    };

    DynamicDAO.addEntityListener(ISearchFilter.class, listener);

    try {
      fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

      assertTrue(listenerCalled.get());

      List<INews> news = bm.getFeedLinkReference().resolve().getNews();
      assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.READ)));
      assertEquals(3, news.size());
      for (INews newsitem : news) {
        assertEquals(INews.State.READ, newsitem.getState());
      }
    } finally {
      DynamicDAO.removeEntityListener(ISearchFilter.class, listener);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void test_MarkUnread_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MARK_UNREAD_ID);
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.UNREAD)));
    assertEquals(3, news.size());
    for (INews newsitem : news) {
      assertEquals(INews.State.UNREAD, newsitem.getState());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void test_MarkSticky_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MARK_STICKY_ID);
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(3, news.size());
    for (INews newsitem : news) {
      assertEquals(true, newsitem.isFlagged());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void test_AddLabel_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    ILabel label = fFactory.createLabel(null, "New Label");
    DynamicDAO.save(label);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(LABEL_NEWS_ID);
    action.setData(label.getId());
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : news) {
      assertEquals(1, newsitem.getLabels().size());
      assertEquals(label, newsitem.getLabels().iterator().next());
      assertEquals("New Label", newsitem.getLabels().iterator().next().getName());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void test_Delete_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(DELETE_NEWS_ID);
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.DELETED)));
    for (INews newsitem : news) {
      assertEquals(INews.State.DELETED, newsitem.getState());
    }
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("cast")
  @Test
  public void test_CopyNews_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    INewsBin bin = fFactory.createNewsBin(null, bm.getParent(), "Bin");
    DynamicDAO.save(bin);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(COPY_NEWS_ID);
    action.setData(new Long[] { bin.getId() });
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : news) {
      assertEquals(INews.State.NEW, newsitem.getState());
      assertEquals(0, newsitem.getParentId());
    }

    List<INews> binNews = bin.getNews();
    assertEquals(3, bin.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : binNews) {
      assertEquals(INews.State.NEW, newsitem.getState());
      assertEquals(bin.getId(), (Long) newsitem.getParentId());
    }

  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("cast")
  @Test
  public void test_MoveNews_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    INewsBin bin = fFactory.createNewsBin(null, bm.getParent(), "Bin");
    DynamicDAO.save(bin);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MOVE_NEWS_ID);
    action.setData(new Long[] { bin.getId() });
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.DELETED)));
    for (INews newsitem : news) {
      assertEquals(INews.State.DELETED, newsitem.getState());
    }

    List<INews> binNews = bin.getNews();
    assertEquals(3, bin.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : binNews) {
      assertEquals(INews.State.NEW, newsitem.getState());
      assertEquals(bin.getId(), (Long) newsitem.getParentId());
    }
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("cast")
  @Test
  public void test_MoveNews_MatchAll_RunAllActions() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    INewsBin bin = fFactory.createNewsBin(null, bm.getParent(), "Bin");
    DynamicDAO.save(bin);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MOVE_NEWS_ID);
    action.setData(new Long[] { bin.getId() });
    filter.addAction(action);

    action = fFactory.createFilterAction(MARK_STICKY_ID);
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.DELETED)));
    for (INews newsitem : news) {
      assertEquals(INews.State.DELETED, newsitem.getState());
    }

    List<INews> binNews = bin.getNews();
    assertEquals(3, bin.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : binNews) {
      assertEquals(INews.State.NEW, newsitem.getState());
      assertEquals(bin.getId(), (Long) newsitem.getParentId());
      assertEquals(true, newsitem.isFlagged());
    }

  }

  /**
   * @throws Exception
   */
  @Test
  public void testStopFilter() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);
    news1.setFlagged(true);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    {
      ISearch search = createStickySearch(true);

      ISearchFilter filter = fFactory.createSearchFilter(null, search, "New News");
      filter.setEnabled(true);

      IFilterAction action = fFactory.createFilterAction(STOP_FILTER_ID);
      filter.addAction(action);
      filter.setOrder(0);

      action = fFactory.createFilterAction(MARK_STICKY_ID);
      filter.addAction(action);

      DynamicDAO.save(filter);
    }

    {
      ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
      filter.setEnabled(true);
      filter.setMatchAllNews(true);
      filter.setOrder(1);

      IFilterAction action = fFactory.createFilterAction(MARK_READ_ID);
      filter.addAction(action);

      DynamicDAO.save(filter);
    }

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(1, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(2, bm.getNewsCount(EnumSet.of(INews.State.READ)));
    for (INews newsitem : news) {
      if (newsitem.equals(news1)) {
        assertEquals(INews.State.NEW, news1.getState());
        assertTrue(news1.isFlagged());
      } else if (newsitem.equals(news2))
        assertEquals(INews.State.READ, news2.getState());
      else if (newsitem.equals(news3))
        assertEquals(INews.State.READ, news3.getState());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testDisabledFilter() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);
    news1.setFlagged(true);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    {
      ISearch search = createStickySearch(true);

      ISearchFilter filter = fFactory.createSearchFilter(null, search, "New News");
      filter.setEnabled(false);

      IFilterAction action = fFactory.createFilterAction(STOP_FILTER_ID);
      filter.addAction(action);
      filter.setOrder(0);

      action = fFactory.createFilterAction(MARK_STICKY_ID);
      filter.addAction(action);

      DynamicDAO.save(filter);
    }

    {
      ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
      filter.setEnabled(true);
      filter.setMatchAllNews(true);
      filter.setOrder(1);

      IFilterAction action = fFactory.createFilterAction(MARK_READ_ID);
      filter.addAction(action);

      DynamicDAO.save(filter);
    }

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.READ)));
    for (INews newsitem : news) {
      if (newsitem.equals(news1))
        assertEquals(INews.State.READ, news1.getState());
      else if (newsitem.equals(news2))
        assertEquals(INews.State.READ, news2.getState());
      else if (newsitem.equals(news3))
        assertEquals(INews.State.READ, news3.getState());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFilterOrder_AllNews() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);
    news1.setFlagged(true);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    {
      ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
      filter.setEnabled(true);
      filter.setMatchAllNews(true);
      filter.setOrder(0);

      IFilterAction action = fFactory.createFilterAction(MARK_READ_ID);
      filter.addAction(action);

      DynamicDAO.save(filter);
    }

    {
      ISearch search = createStickySearch(true);

      ISearchFilter filter = fFactory.createSearchFilter(null, search, "New News");
      filter.setEnabled(true);

      IFilterAction action = fFactory.createFilterAction(STOP_FILTER_ID);
      filter.addAction(action);
      filter.setOrder(1);

      action = fFactory.createFilterAction(MARK_STICKY_ID);
      filter.addAction(action);

      DynamicDAO.save(filter);
    }

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.READ)));
    for (INews newsitem : news) {
      if (newsitem.equals(news1))
        assertEquals(INews.State.READ, news1.getState());
      else if (newsitem.equals(news2))
        assertEquals(INews.State.READ, news2.getState());
      else if (newsitem.equals(news3))
        assertEquals(INews.State.READ, news3.getState());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testComplexFilter() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "Title", "Link1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "Title", "Link2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "Other");
    news3.setState(INews.State.NEW);

    INews news4 = createNews(feed, "Nothing");
    news4.setState(INews.State.NEW);

    ILabel label = fFactory.createLabel(null, "New Label");
    DynamicDAO.save(label);

    INewsBin bin1 = fFactory.createNewsBin(null, bm.getParent(), "Bin 1");
    INewsBin bin2 = fFactory.createNewsBin(null, bm.getParent(), "Bin 2");

    DynamicDAO.save(bin1);
    DynamicDAO.save(bin2);

    /* Filter "Title is Title": Mark Read, Sticky, Label, Copy */
    {
      ISearch search = createTitleSearch("Title");

      ISearchFilter filter = fFactory.createSearchFilter(null, search, "Title is Title");
      filter.setEnabled(true);
      filter.setOrder(0);

      filter.addAction(fFactory.createFilterAction(MARK_READ_ID));
      filter.addAction(fFactory.createFilterAction(MARK_STICKY_ID));

      IFilterAction labelAction = fFactory.createFilterAction(LABEL_NEWS_ID);
      labelAction.setData(label.getId());
      filter.addAction(labelAction);

      IFilterAction copyAction = fFactory.createFilterAction(COPY_NEWS_ID);
      copyAction.setData(new Long[] { bin1.getId(), bin2.getId() });
      filter.addAction(copyAction);

      DynamicDAO.save(filter);
    }

    /* Filter "Title is Other": Move */
    {
      ISearch search = createTitleSearch("Other");

      ISearchFilter filter = fFactory.createSearchFilter(null, search, "Title is Other");
      filter.setEnabled(true);
      filter.setOrder(1);

      IFilterAction moveAction = fFactory.createFilterAction(MOVE_NEWS_ID);
      moveAction.setData(new Long[] { bin1.getId(), bin2.getId() });
      filter.addAction(moveAction);

      DynamicDAO.save(filter);
    }

    /* Filter "Match All": Label News */
    {

      ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
      filter.setEnabled(true);
      filter.setMatchAllNews(true);
      filter.setOrder(2);

      IFilterAction labelAction = fFactory.createFilterAction(LABEL_NEWS_ID);
      labelAction.setData(label.getId());
      filter.addAction(labelAction);

      DynamicDAO.save(filter);
    }

    final AtomicInteger listenerCalled = new AtomicInteger();
    SearchFilterAdapter listener = new SearchFilterAdapter() {
      @Override
      public void filterApplied(ISearchFilter f, Collection<INews> news) {
        listenerCalled.incrementAndGet();
      }
    };

    DynamicDAO.addEntityListener(ISearchFilter.class, listener);

    try {
      fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());
    } finally {
      DynamicDAO.removeEntityListener(ISearchFilter.class, listener);
    }

    assertEquals(3, listenerCalled.get());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(4, news.size());
    assertEquals(2, bm.getNewsCount(EnumSet.of(INews.State.READ)));
    assertEquals(1, bm.getNewsCount(EnumSet.of(INews.State.DELETED)));
    assertEquals(1, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : news) {
      if (newsitem.equals(news1)) {
        assertEquals(INews.State.READ, news1.getState());
        assertTrue(newsitem.isFlagged());
        assertTrue(!news1.getLabels().isEmpty());
        assertEquals(label, news1.getLabels().iterator().next());
      } else if (newsitem.equals(news2)) {
        assertEquals(INews.State.READ, news2.getState());
        assertTrue(newsitem.isFlagged());
        assertTrue(!news2.getLabels().isEmpty());
        assertEquals(label, news2.getLabels().iterator().next());
      } else if (newsitem.equals(news3)) {
        assertEquals(INews.State.DELETED, news3.getState());
      } else if (newsitem.equals(news4)) {
        assertTrue(!news4.getLabels().isEmpty());
        assertEquals(label, news4.getLabels().iterator().next());
      }
    }

    assertEquals(3, bin1.getNews().size());
    assertEquals(1, bin1.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(2, bin1.getNewsCount(EnumSet.of(INews.State.READ)));

    List<INews> binNews = bin1.getNews();
    for (INews newsitem : binNews) {
      if (newsitem.equals(news1)) {
        assertEquals(INews.State.READ, news1.getState());
        assertTrue(newsitem.isFlagged());
        assertTrue(!news1.getLabels().isEmpty());
        assertEquals(label, news1.getLabels().iterator().next());
      } else if (newsitem.equals(news2)) {
        assertEquals(INews.State.READ, news2.getState());
        assertTrue(newsitem.isFlagged());
        assertTrue(!news2.getLabels().isEmpty());
        assertEquals(label, news2.getLabels().iterator().next());
      } else if (newsitem.equals(news3)) {
        assertEquals(INews.State.NEW, news3.getState());
        assertTrue(newsitem.getLabels().isEmpty());
        assertTrue(!newsitem.isFlagged());
      }
    }

    assertEquals(3, bin2.getNews().size());
    assertEquals(1, bin2.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(2, bin2.getNewsCount(EnumSet.of(INews.State.READ)));

    binNews = bin2.getNews();
    for (INews newsitem : binNews) {
      if (newsitem.equals(news1)) {
        assertEquals(INews.State.READ, news1.getState());
        assertTrue(newsitem.isFlagged());
      } else if (newsitem.equals(news2)) {
        assertEquals(INews.State.READ, news2.getState());
        assertTrue(newsitem.isFlagged());
      } else if (newsitem.equals(news3)) {
        assertEquals(INews.State.NEW, news3.getState());
        assertTrue(newsitem.getLabels().isEmpty());
        assertTrue(!newsitem.isFlagged());
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFilterAndRetention_Count() throws Exception {
    IBookMark bm = createBookMark("local1");
    IPreferenceScope preferences = Owl.getPreferenceService().getEntityScope(bm);
    preferences.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    preferences.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 2);

    DynamicDAO.save(bm);

    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    ILabel label = fFactory.createLabel(null, "New Label");
    DynamicDAO.save(label);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(LABEL_NEWS_ID);
    action.setData(label.getId());
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : news) {
      assertEquals(1, newsitem.getLabels().size());
      assertEquals(label, newsitem.getLabels().iterator().next());
      assertEquals("New Label", newsitem.getLabels().iterator().next().getName());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFilterAndRetention_Age() throws Exception {
    IBookMark bm = createBookMark("local1");
    IPreferenceScope preferences = Owl.getPreferenceService().getEntityScope(bm);
    preferences.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    preferences.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 2);
    preferences.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, false);
    preferences.putBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE, false);

    DynamicDAO.save(bm);

    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(cal.getTimeInMillis() - DateUtils.WEEK);
    Date oldDate = cal.getTime();

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.UNREAD);
    news1.setReceiveDate(oldDate);
    news1.setPublishDate(oldDate);
    news1.setModifiedDate(oldDate);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.UNREAD);
    news2.setReceiveDate(oldDate);
    news2.setPublishDate(oldDate);
    news2.setModifiedDate(oldDate);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.UNREAD);
    news3.setReceiveDate(oldDate);
    news3.setPublishDate(oldDate);
    news3.setModifiedDate(oldDate);

    ILabel label = fFactory.createLabel(null, "New Label");
    DynamicDAO.save(label);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(LABEL_NEWS_ID);
    action.setData(label.getId());
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(0, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.DELETED)));
    for (INews newsitem : news) {
      assertEquals(0, newsitem.getLabels().size());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSimpleFilterOnNewsWithDescription() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);
    news1.setDescription("Description 1");

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);
    news2.setDescription("Description 1");

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);
    news3.setDescription("Description 1");

    ILabel label = fFactory.createLabel(null, "New Label");
    DynamicDAO.save(label);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(LABEL_NEWS_ID);
    action.setData(label.getId());
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : news) {
      assertEquals(1, newsitem.getLabels().size());
      assertEquals(label, newsitem.getLabels().iterator().next());
      assertEquals("New Label", newsitem.getLabels().iterator().next().getName());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testComplexFilterOnNewsWithDescription() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);
    news1.setDescription("Description");

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);
    news2.setDescription("Foo");

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);
    news3.setDescription("Bar");

    ILabel label = fFactory.createLabel(null, "New Label");
    DynamicDAO.save(label);

    ISearchFilter filter = fFactory.createSearchFilter(null, createDescriptionSearch("description"), "Some News");
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(LABEL_NEWS_ID);
    action.setData(label.getId());
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    boolean labelFound = false;
    for (INews newsitem : news) {
      if (news1.equals(newsitem)) {
        assertEquals("New Label", newsitem.getLabels().iterator().next().getName());
        labelFound = true;
      }
    }

    assertTrue(labelFound);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFilterOnDuplicateNews() throws Exception {
    IBookMark bm1 = createBookMark("local1");
    IFeed feed1 = bm1.getFeedLinkReference().resolve();

    INews news1_feed1 = createNews(feed1, "News1");
    news1_feed1.setState(INews.State.READ);
    news1_feed1.setLink(new URI("news_link"));

    DynamicDAO.save(feed1);

    IBookMark bm2 = createBookMark("local2");
    IFeed feed2 = fFactory.createFeed(null, bm2.getFeedLinkReference().getLink());

    INews news1_feed2 = createNews(feed2, "News2");
    news1_feed2.setLink(new URI("news_link"));
    news1_feed2.setState(INews.State.NEW);

    ILabel label = fFactory.createLabel(null, "New Label");
    DynamicDAO.save(label);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(LABEL_NEWS_ID);
    action.setData(label.getId());
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm2, feed2, null, false, true, new NullProgressMonitor());

    List<INews> news = bm2.getFeedLinkReference().resolve().getNews();
    assertEquals(1, news.size());
    assertEquals(1, bm2.getNewsCount(EnumSet.of(INews.State.READ)));
    for (INews newsitem : news) {
      assertEquals(1, newsitem.getLabels().size());
      assertEquals(INews.State.READ, newsitem.getState()); //Because the news is duplicate in another feed and read
      assertEquals(label, newsitem.getLabels().iterator().next());
      assertEquals("New Label", newsitem.getLabels().iterator().next().getName());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testScopeFilter_Matching() throws Exception {
    IBookMark bm1 = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm1.getFeedLinkReference().getLink());

    IBookMark bm2 = fFactory.createBookMark(null, bm1.getParent(), new FeedLinkReference(new URI("local2")), "local2");
    DynamicDAO.save(bm2);

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    ISearch search = createScopeSearch(bm1);

    ISearchFilter filter = fFactory.createSearchFilter(null, search, "All News in BM1");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MARK_READ_ID);
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm1, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm1.getFeedLinkReference().resolve().getNews();
    assertEquals(3, bm1.getNewsCount(EnumSet.of(INews.State.READ)));
    assertEquals(3, news.size());
    for (INews newsitem : news) {
      assertEquals(INews.State.READ, newsitem.getState());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testScopeFilter_NotMatching() throws Exception {
    IBookMark bm1 = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm1.getFeedLinkReference().getLink());

    IBookMark bm2 = fFactory.createBookMark(null, bm1.getParent(), new FeedLinkReference(new URI("local2")), "local2");
    DynamicDAO.save(bm2);

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    ISearch search = createScopeSearch(bm2);

    ISearchFilter filter = fFactory.createSearchFilter(null, search, "All News in BM2");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MARK_READ_ID);
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm1, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm1.getFeedLinkReference().resolve().getNews();
    assertEquals(0, bm1.getNewsCount(EnumSet.of(INews.State.READ)));
    assertEquals(3, news.size());
    for (INews newsitem : news) {
      assertEquals(INews.State.NEW, newsitem.getState());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void test_MoveNewsToInvalidLocation_MatchAll() throws Exception {
    IBookMark bm = createBookMark("local1");
    IFeed feed = fFactory.createFeed(null, bm.getFeedLinkReference().getLink());

    INews news1 = createNews(feed, "News1");
    news1.setState(INews.State.NEW);

    INews news2 = createNews(feed, "News2");
    news2.setState(INews.State.NEW);

    INews news3 = createNews(feed, "News3");
    news3.setState(INews.State.NEW);

    INewsBin bin = fFactory.createNewsBin(null, bm.getParent(), "Bin");
    DynamicDAO.save(bin);

    ISearchFilter filter = fFactory.createSearchFilter(null, null, "All News");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);

    IFilterAction action = fFactory.createFilterAction(MOVE_NEWS_ID);
    action.setData(new Long[] { bin.getId() + 1 });
    filter.addAction(action);

    DynamicDAO.save(filter);

    fAppService.handleFeedReload(bm, feed, null, false, true, new NullProgressMonitor());

    List<INews> news = bm.getFeedLinkReference().resolve().getNews();
    assertEquals(3, news.size());
    assertEquals(3, bm.getNewsCount(EnumSet.of(INews.State.NEW)));
    for (INews newsitem : news) {
      assertEquals(INews.State.NEW, newsitem.getState());
    }

    List<INews> binNews = bin.getNews();
    assertEquals(0, binNews.size());
  }

  private ISearch createStickySearch(boolean sticky) {
    ISearch search = fFactory.createSearch(null);
    ISearchField field = fFactory.createSearchField(INews.IS_FLAGGED, INews.class.getName());

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, sticky);
    search.addSearchCondition(condition);

    return search;
  }

  private ISearch createScopeSearch(IFolderChild scope) {
    ISearch search = fFactory.createSearch(null);
    ISearchField field = fFactory.createSearchField(INews.LOCATION, INews.class.getName());

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { scope })));
    search.addSearchCondition(condition);

    return search;
  }

  private ISearch createTitleSearch(String title) {
    ISearch search = fFactory.createSearch(null);
    ISearchField field = fFactory.createSearchField(INews.TITLE, INews.class.getName());

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, title);
    search.addSearchCondition(condition);

    return search;
  }

  private ISearch createDescriptionSearch(String description) {
    ISearch search = fFactory.createSearch(null);
    ISearchField field = fFactory.createSearchField(INews.DESCRIPTION, INews.class.getName());

    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, description);
    search.addSearchCondition(condition);

    return search;
  }

  private INews createNews(IFeed feed, String title) {
    INews news = fFactory.createNews(null, feed, new Date());
    news.setTitle(title);
    return news;
  }

  private INews createNews(IFeed feed, String title, String link) throws URISyntaxException {
    INews news = fFactory.createNews(null, feed, new Date());
    news.setLink(new URI(link));
    news.setTitle(title);
    return news;
  }

  private IBookMark createBookMark(String link) throws URISyntaxException {
    IFolder folder = fFactory.createFolder(null, null, "Root");

    IFeed feed = fFactory.createFeed(null, new URI(link));
    DynamicDAO.save(feed);

    IBookMark bm = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    DynamicDAO.save(folder);

    return bm;
  }
}
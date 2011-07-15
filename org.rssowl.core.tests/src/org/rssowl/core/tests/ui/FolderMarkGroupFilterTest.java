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

package org.rssowl.core.tests.ui;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.Viewer;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.NewsCounterItem;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.views.explorer.BookMarkFilter;
import org.rssowl.ui.internal.views.explorer.BookMarkGrouping;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Test the Folder/Mark-Grouping and Folder/Mark-Filter of the BookMark
 * Explorer.
 *
 * @author bpasero
 */
public class FolderMarkGroupFilterTest {

  /* Some Date Constants */
  private static final long DAY = 24 * 60 * 60 * 1000;
  private static final long WEEK = 7 * DAY;

  private IModelFactory fFactory;
  private BookMarkGrouping fGrouping;
  private BookMarkFilter fFiltering;
  private Date fLastWeek;
  private Date fYesterday;
  private Date fEarlierThisWeek;
  private Date fToday;
  private Viewer fNullViewer = new NullViewer();
  private boolean fTodayIsFirstDayOfWeek;
  private boolean fYesterdayIsFirstDayOfWeek;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
    fGrouping = new BookMarkGrouping();
    fFiltering = new BookMarkFilter();

    fToday = new Date(DateUtils.getToday().getTimeInMillis() + 1000);
    fYesterday = new Date(fToday.getTime() - DAY);
    Calendar today = DateUtils.getToday();
    fTodayIsFirstDayOfWeek = today.get(Calendar.DAY_OF_WEEK) == today.getFirstDayOfWeek();
    fYesterdayIsFirstDayOfWeek = today.get(Calendar.DAY_OF_WEEK) == today.getFirstDayOfWeek() + 1;
    today.set(Calendar.DAY_OF_WEEK, today.getFirstDayOfWeek());
    fEarlierThisWeek = new Date(today.getTimeInMillis() + 1000);
    fLastWeek = new Date(fEarlierThisWeek.getTime() - WEEK);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFolderMarkGrouping() throws Exception {
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.link1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.link2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.link3.com"));
    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IFolder root = fFactory.createFolder(null, null, "Root");

    /* Today */
    IBookMark bookmark1 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "BookMark 1");
    bookmark1.setLastVisitDate(fToday);
    ISearchMark searchmark1 = fFactory.createSearchMark(null, root, "SearchMark 1");
    searchmark1.setLastVisitDate(fToday);

    /* Yesterday */
    IBookMark bookmark2 = fFactory.createBookMark(null, root, new FeedLinkReference(feed2.getLink()), "BookMark 2");
    bookmark2.setLastVisitDate(fYesterday);
    ISearchMark searchmark2 = fFactory.createSearchMark(null, root, "SearchMark 2");
    searchmark2.setLastVisitDate(fYesterday);

    /* Earlier this Week */
    IBookMark bookmark3 = fFactory.createBookMark(null, root, new FeedLinkReference(feed3.getLink()), "BookMark 3");
    bookmark3.setLastVisitDate(fEarlierThisWeek);
    ISearchMark searchmark3 = fFactory.createSearchMark(null, root, "SearchMark 3");
    searchmark3.setLastVisitDate(fEarlierThisWeek);

    /* Last Week */
    IBookMark bookmark4 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "BookMark 4");
    bookmark4.setLastVisitDate(fLastWeek);
    ISearchMark searchmark4 = fFactory.createSearchMark(null, root, "SearchMark 4");
    searchmark4.setLastVisitDate(fLastWeek);

    /* More than one Week ago */
    IBookMark bookmark5 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "BookMark 5");
    bookmark5.setLastVisitDate(new Date(0));

    /* Visited Never */
    ISearchMark searchmark5 = fFactory.createSearchMark(null, root, "SearchMark 5");

    /* Future */
    IBookMark bookmark6 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "BookMark 6");
    bookmark6.setLastVisitDate(new Date(fToday.getTime() + 10000000));

    List<IEntity> input = new ArrayList<IEntity>();
    input.add(bookmark1);
    input.add(bookmark2);
    input.add(bookmark3);
    input.add(bookmark4);
    input.add(bookmark5);
    input.add(bookmark6);
    input.add(searchmark1);
    input.add(searchmark2);
    input.add(searchmark3);
    input.add(searchmark4);
    input.add(searchmark5);

    /* Group by Last Visit */
    {
      fGrouping.setType(BookMarkGrouping.Type.GROUP_BY_LAST_VISIT);
      EntityGroup[] group = fGrouping.group(input);

      assertEquals(fTodayIsFirstDayOfWeek || fYesterdayIsFirstDayOfWeek ? 5 : 6, group.length);
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == BookMarkGrouping.Group.TODAY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(fTodayIsFirstDayOfWeek ? 5 : 3, entities.size());

          if (!fTodayIsFirstDayOfWeek)
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark1, searchmark1, bookmark6 })));
          else
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark1, searchmark1, bookmark6, bookmark3, searchmark3 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.YESTERDAY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(fYesterdayIsFirstDayOfWeek ? 4 : 2, entities.size());

          if (!fYesterdayIsFirstDayOfWeek)
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark2, searchmark2 })));
          else
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark2, searchmark2, bookmark3, searchmark3 })));
        }

        else if (!fTodayIsFirstDayOfWeek && !fYesterdayIsFirstDayOfWeek && entityGroup.getId() == BookMarkGrouping.Group.EARLIER_THIS_WEEK.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark3, searchmark3 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.LAST_WEEK.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark4, searchmark4 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.OLDER.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark5 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.NEVER.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { searchmark5 })));
        }
      }
    }

    /* Group by Popularity */
    {
      fGrouping.setType(BookMarkGrouping.Type.GROUP_BY_POPULARITY);

      input = new ArrayList<IEntity>();
      input.add(bookmark1);
      input.add(bookmark2);
      input.add(bookmark3);
      input.add(bookmark4);
      input.add(searchmark1);
      input.add(searchmark2);

      bookmark1.setPopularity(0);
      bookmark2.setPopularity(25);
      bookmark3.setPopularity(50);
      bookmark4.setPopularity(75);

      searchmark1.setPopularity(10);
      searchmark2.setPopularity(80);

      EntityGroup[] group = fGrouping.group(input);

      assertEquals(4, group.length);
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == BookMarkGrouping.Group.VERY_POPULAR.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark4, searchmark2 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.POPULAR.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark3 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.FAIRLY_POPULAR.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark2 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.UNPOPULAR.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark1, searchmark1 })));
        }
      }
    }

    input = new ArrayList<IEntity>();
    input.add(bookmark1);
    input.add(bookmark2);
    input.add(bookmark3);
    input.add(bookmark4);
    input.add(bookmark5);
    input.add(bookmark6);
    input.add(searchmark1);
    input.add(searchmark2);
    input.add(searchmark3);
    input.add(searchmark4);
    input.add(searchmark5);

    INewsBin bin = fFactory.createNewsBin(null, root, "Bin");
    input.add(bin);

    NewsCounter count = new NewsCounter();
    NewsCounterItem item = new NewsCounterItem(1, 0, 0);
    count.put(feed1.getLink().toString(), item);
    ((BookMark) bookmark1).setNewsCounter(count);

    count = new NewsCounter();
    item = new NewsCounterItem(0, 1, 0);
    count.put(feed2.getLink().toString(), item);
    ((BookMark) bookmark2).setNewsCounter(count);

    count = new NewsCounter();
    item = new NewsCounterItem(0, 0, 1);
    count.put(feed3.getLink().toString(), item);
    ((BookMark) bookmark3).setNewsCounter(count);

    INews news = fFactory.createNews(null, fFactory.createFeed(null, new URI("feed4")), new Date());
    news.setState(INews.State.NEW);
    news.setId(System.currentTimeMillis());
    bin.addNews(news);

    /* Group by State */
    {
      fGrouping.setType(BookMarkGrouping.Type.GROUP_BY_STATE);
      EntityGroup[] group = fGrouping.group(input);

      assertEquals(4, group.length);
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == BookMarkGrouping.Group.STICKY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark3 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.NEW.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark1, bin })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.UNREAD.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark2 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.OTHER.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(8, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark4, bookmark5, bookmark6, searchmark1, searchmark2, searchmark3, searchmark4, searchmark5 })));
        }
      }
    }

    /* Group by Type */
    {
      fGrouping.setType(BookMarkGrouping.Type.GROUP_BY_TYPE);
      EntityGroup[] group = fGrouping.group(input);

      assertEquals(3, group.length);
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == BookMarkGrouping.Group.BOOKMARK.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(6, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bookmark1, bookmark2, bookmark3, bookmark4, bookmark5, bookmark6 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.SEARCH.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(5, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { searchmark1, searchmark2, searchmark3, searchmark4, searchmark5 })));
        }

        else if (entityGroup.getId() == BookMarkGrouping.Group.BIN.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { bin })));
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFolderMarkFiltering() throws Exception {
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.foo.com"));
    feed1.setDescription("This is the foo.");
    fFactory.createNews(null, feed1, new Date());
    fFactory.createNews(null, feed1, new Date()).setState(INews.State.UNREAD);
    DynamicDAO.save(feed1);

    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.bar.com"));
    feed2.setDescription("This is the bar.");
    DynamicDAO.save(feed2);

    IFolder root = fFactory.createFolder(null, null, "Root");

    IBookMark bm1 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "FookMark 1");
    bm1.setErrorLoading(true);
    bm1.setPopularity(1);

    ISearchMark sm1 = fFactory.createSearchMark(null, root, "SearchMark 1");
    sm1.setPopularity(1);

    IBookMark bm2 = fFactory.createBookMark(null, root, new FeedLinkReference(feed2.getLink()), "BookMark 2");
    bm2.setPopularity(1);

    ISearchMark sm2 = fFactory.createSearchMark(null, root, "SearchMark 2");
    sm2.setPopularity(1);

    IBookMark bm3 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "BookMark 3");
    bm3.setPopularity(1);

    ISearchMark sm3 = fFactory.createSearchMark(null, root, "SearchMark 3");
    sm3.setPopularity(1);

    IBookMark bm4 = fFactory.createBookMark(null, root, new FeedLinkReference(feed2.getLink()), "BookMark 4");
    bm4.setPopularity(1);

    ISearchMark sm4 = fFactory.createSearchMark(null, root, "SearchMark 4");
    sm4.setPopularity(1);

    IBookMark bm5 = fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "BookMark 5");
    bm5.setPopularity(1);

    ISearchMark sm5 = fFactory.createSearchMark(null, root, "SearchMark 5");

    IBookMark bm6 = fFactory.createBookMark(null, root, new FeedLinkReference(feed2.getLink()), "BookMark 6");

    /* Fill into Array */
    Object elements[] = new Object[] { bm1, bm2, bm3, bm4, bm5, bm6, sm1, sm2, sm3, sm4, sm5 };

    /* Filter: Show All */
    {
      fFiltering.setType(BookMarkFilter.Type.SHOW_ALL);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(elements.length, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { bm1, bm2, bm3, bm4, bm5, bm6, sm1, sm2, sm3, sm4, sm5 })));
    }

    /* Filter: Show Erroneous */
    {
      fFiltering.setType(BookMarkFilter.Type.SHOW_ERRONEOUS);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(1, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { bm1 })));
    }

    /* Filter: Show Never Visited */
    {
      fFiltering.setType(BookMarkFilter.Type.SHOW_NEVER_VISITED);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(2, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { sm5, bm6 })));
    }

    /* Filter: Show New */
    {
      fFiltering.setType(BookMarkFilter.Type.SHOW_NEW);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(3, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { bm1, bm3, bm5 })));
    }

    /* Filter: Show Unread */
    {
      fFiltering.setType(BookMarkFilter.Type.SHOW_UNREAD);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(3, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { bm1, bm3, bm5 })));
    }

    /* Filter: Text Pattern (Name) */
    {
      fFiltering.setType(BookMarkFilter.Type.SHOW_ALL);
      fFiltering.setPattern("*foo*");
      fFiltering.setSearchTarget(BookMarkFilter.SearchTarget.NAME);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(1, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { bm1 })));
    }

    /* Filter: Text Pattern (Link) */
    {
      fFiltering.setSearchTarget(BookMarkFilter.SearchTarget.LINK);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(3, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { bm1, bm3, bm5 })));
    }
  }

  private int countEntities(EntityGroup group[]) {
    int count = 0;

    for (EntityGroup entityGroup : group) {
      count += entityGroup.getEntities().size();
    }

    return count;
  }
}
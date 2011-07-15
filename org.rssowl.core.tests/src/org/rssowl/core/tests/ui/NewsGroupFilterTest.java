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
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.editors.feed.NewsFilter;
import org.rssowl.ui.internal.editors.feed.NewsGrouping;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Test the News-Grouping and News-Filter of the FeedView.
 *
 * @author bpasero
 */
public class NewsGroupFilterTest {

  /* Some Date Constants */
  private static final long DAY = 24 * 60 * 60 * 1000;
  private static final long WEEK = 7 * DAY;

  private IModelFactory fFactory;
  private NewsGrouping fGrouping;
  private NewsFilter fFiltering;
  private Date fToday;
  private Date fYesterday;
  private Date fEarlierThisWeek;
  private Date fLastWeek;
  private Viewer fNullViewer = new NullViewer();
  private boolean fTodayIsFirstDayOfWeek;
  private boolean fYesterdayIsFirstDayOfWeek;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    fFactory = Owl.getModelFactory();
    fGrouping = new NewsGrouping();
    fFiltering = new NewsFilter();
    fToday = new Date(DateUtils.getToday().getTimeInMillis());
    fYesterday = new Date(fToday.getTime() - DAY);
    Calendar today = DateUtils.getToday();
    fTodayIsFirstDayOfWeek = today.get(Calendar.DAY_OF_WEEK) == today.getFirstDayOfWeek();
    fYesterdayIsFirstDayOfWeek = today.get(Calendar.DAY_OF_WEEK) == today.getFirstDayOfWeek() + 1;
    today.set(Calendar.DAY_OF_WEEK, today.getFirstDayOfWeek());
    fEarlierThisWeek = new Date(today.getTimeInMillis() + 1000);
    fLastWeek = new Date(fEarlierThisWeek.getTime() - WEEK);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  private void waitForIndexer() throws InterruptedException {
    Thread.sleep(500);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsGrouping() throws Exception {
    IFolder folder= fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(folder);

    IFeed feed = fFactory.createFeed(null, new URI("http://www.link.com"));
    feed.setTitle("Feed Name");
    DynamicDAO.save(feed);

    IBookMark bookmark= fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Feed Name");
    DynamicDAO.save(bookmark);

    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News 1");
    news1.setPublishDate(fToday);

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News 2");
    news2.setPublishDate(fYesterday);

    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News 3");
    news3.setPublishDate(fEarlierThisWeek);

    INews news4 = fFactory.createNews(null, feed, new Date());
    news4.setTitle("News 4");
    news4.setPublishDate(fLastWeek);

    INews news5 = fFactory.createNews(null, feed, new Date());
    news5.setTitle("News 5");
    news5.setPublishDate(new Date(0));

    INews news6 = fFactory.createNews(null, feed, new Date());
    news6.setTitle("News 6");
    news6.setPublishDate(new Date(0));

    INews news7 = fFactory.createNews(null, feed, new Date());
    news7.setTitle("News 7");
    news7.setPublishDate(new Date(0));

    List<INews> input = new ArrayList<INews>();
    input.add(news1);
    input.add(news2);
    input.add(news3);
    input.add(news4);
    input.add(news5);
    input.add(news6);
    input.add(news7);

    /* Group by Date */
    {
      fGrouping.setType(NewsGrouping.Type.GROUP_BY_DATE);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(fTodayIsFirstDayOfWeek || fYesterdayIsFirstDayOfWeek ? 4 : 5, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == NewsGrouping.Group.TODAY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(fTodayIsFirstDayOfWeek ? 2 : 1, entities.size());

          if (!fTodayIsFirstDayOfWeek)
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1 })));
          else
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1, news3 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.YESTERDAY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(fYesterdayIsFirstDayOfWeek ? 2 : 1, entities.size());

          if (!fYesterdayIsFirstDayOfWeek)
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news2 })));
          else
            assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news2, news3 })));
        }

        else if (!fTodayIsFirstDayOfWeek && !fYesterdayIsFirstDayOfWeek && entityGroup.getId() == NewsGrouping.Group.EARLIER_THIS_WEEK.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news3 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.LAST_WEEK.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news4 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.OLDER.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(3, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news5, news6, news7 })));
        }
      }
    }

    /* Group by State */
    {
      news1.setState(INews.State.UNREAD);
      news2.setState(INews.State.READ);
      news3.setState(INews.State.HIDDEN);
      news4.setState(INews.State.DELETED);
      news5.setState(INews.State.UPDATED);

      fGrouping.setType(NewsGrouping.Type.GROUP_BY_STATE);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(4, group.size());
      assertEquals(5, countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == NewsGrouping.Group.NEW.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news6, news7 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.UNREAD.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.UPDATED.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news5 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.READ.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news2 })));
        }
      }

      news3.setState(INews.State.NEW);
      news4.setState(INews.State.NEW);
    }

    /* Group by Author */
    {
      IPerson author1 = fFactory.createPerson(null, news1);
      author1.setName("Author 1");

      IPerson author2 = fFactory.createPerson(null, news2);
      author2.setName("Author 2");

      fGrouping.setType(NewsGrouping.Type.GROUP_BY_AUTHOR);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(3, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == NewsGrouping.Group.UNKNOWN_AUTHOR.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(5, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news3, news4, news5, news6, news7 })));
        }

        else if (entityGroup.getName().equals("Author 1")) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1 })));
        }

        else if (entityGroup.getName().equals("Author 2")) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news2 })));
        }
      }
    }

    /* Group by Category */
    {
      fFactory.createCategory(null, news1).setName("Category 1");
      fFactory.createCategory(null, news2).setName("Category 2");

      fGrouping.setType(NewsGrouping.Type.GROUP_BY_CATEGORY);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(3, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == NewsGrouping.Group.UNKNOWN_CATEGORY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(5, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news3, news4, news5, news6, news7 })));
        }

        else if (entityGroup.getName().equals("Category 1")) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1 })));
        }

        else if (entityGroup.getName().equals("Category 2")) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news2 })));
        }
      }
    }

    /* Group by Label */
    {
      ILabel label1 = fFactory.createLabel(null, "Label 1");
      ILabel label2 = fFactory.createLabel(null, "Label 2");
      news6.addLabel(label1);
      news7.addLabel(label2);

      fGrouping.setType(NewsGrouping.Type.GROUP_BY_LABEL);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(3, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == NewsGrouping.Group.NONE.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(5, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1, news2, news3, news4, news5 })));
        }

        else if (entityGroup.getName().equals("Label 1")) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news6 })));
        }

        else if (entityGroup.getName().equals("Label 2")) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news7 })));
        }
      }
    }

    /* Group by Rating */
    {
      news1.setRating(0);
      news2.setRating(20);
      news3.setRating(40);
      news4.setRating(60);
      news5.setRating(80);

      fGrouping.setType(NewsGrouping.Type.GROUP_BY_RATING);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(5, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == NewsGrouping.Group.FANTASTIC.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news5 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.GOOD.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news4 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.MODERATE.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news3 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.BAD.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(1, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news2 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.VERY_BAD.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(3, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1, news6, news7 })));
        }
      }
    }

    /* Group by Feed */
    {
      fGrouping.setType(NewsGrouping.Type.GROUP_BY_FEED);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(1, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getName().equals("Feed Name")) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(7, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1, news2, news3, news4, news5, news6, news7 })));
        }
      }
    }

    /* Group by Flagged */
    {
      news1.setFlagged(true);
      news2.setFlagged(true);

      fGrouping.setType(NewsGrouping.Type.GROUP_BY_STICKY);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(2, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getId() == NewsGrouping.Group.STICKY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1, news2 })));
        }

        else if (entityGroup.getId() == NewsGrouping.Group.NOT_STICKY.ordinal()) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(5, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news3, news4, news5, news6, news7 })));
        }
      }
    }

    /* Group by Title */
    {
      news2.setTitle(news1.getTitle());

      fGrouping.setType(NewsGrouping.Type.GROUP_BY_TOPIC);
      Collection<EntityGroup> group = fGrouping.group(input);

      assertEquals(6, group.size());
      assertEquals(input.size(), countEntities(group));

      for (EntityGroup entityGroup : group) {
        if (entityGroup.getName().equals(news1.getTitle())) {
          List<IEntity> entities = entityGroup.getEntities();
          assertEquals(2, entities.size());
          assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { news1, news2 })));
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsFiltering() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.link.com"));
    feed.setTitle("Feed Name");

    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News 1");
    news1.setPublishDate(fToday);

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News 2");
    news2.setPublishDate(fYesterday);

    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News 3");
    news3.setPublishDate(new Date(0));

    INews news4 = fFactory.createNews(null, feed, new Date());
    news4.setTitle("News 4");
    news4.setPublishDate(new Date(0));

    INews news5 = fFactory.createNews(null, feed, new Date());
    news5.setTitle("News 5");
    news5.setPublishDate(new Date(0));

    DynamicDAO.save(news1);
    DynamicDAO.save(news2);
    DynamicDAO.save(news3);
    DynamicDAO.save(news4);
    DynamicDAO.save(news5);

    waitForIndexer();

    /* Fill into Array */
    Object elements[] = new Object[] { news1, news2, news3, news4, news5 };

    /* Filter: Show All */
    {
      fFiltering.setType(NewsFilter.Type.SHOW_ALL);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(elements.length, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2, news3, news4, news5 })));
    }

    /* Filter: Show New */
    {
      news2.setState(INews.State.UNREAD);
      news3.setState(INews.State.READ);
      news4.setState(INews.State.HIDDEN);
      news5.setState(INews.State.DELETED);

      fFiltering.setType(NewsFilter.Type.SHOW_NEW);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(1, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1 })));

      news4.setState(INews.State.UNREAD);
      news5.setState(INews.State.NEW);
    }

    /* Filter: Show Unread */
    {
      fFiltering.setType(NewsFilter.Type.SHOW_UNREAD);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(4, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news2, news4 })));
    }

    /* Filter: Show Flagged */
    {
      news1.setFlagged(true);
      news2.setFlagged(true);

      fFiltering.setType(NewsFilter.Type.SHOW_STICKY);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(2, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2 })));
    }

    /* Filter: Show Labeled */
    {
      news1.addLabel(fFactory.createLabel(null, "Foo"));
      news2.addLabel(fFactory.createLabel(null, "Bar"));

      fFiltering.setType(NewsFilter.Type.SHOW_LABELED);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(2, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2 })));
    }

    /* Filter: Show Recent */
    {
      fFiltering.setType(NewsFilter.Type.SHOW_RECENT);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(2, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2 })));
    }

    /* Filter: Text Pattern (Headline) */
    {
      news1.setTitle("Foo Bar");
      news2.setTitle("Bar foo");

      DynamicDAO.save(news1);
      DynamicDAO.save(news2);
      waitForIndexer();

      fFiltering.setType(NewsFilter.Type.SHOW_ALL);
      fFiltering.setPattern("*foo*");
      fFiltering.setSearchTarget(NewsFilter.SearchTarget.HEADLINE);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(2, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2 })));

      news1.setTitle("News 1");
      news2.setTitle("News 2");
    }

    /* Filter: Text Pattern (Entire News) */
    {
      news1.setDescription("Foo bar");
      news2.setTitle("Foo bar");
      news3.setComments("Foo bar");
      fFactory.createAttachment(null, news4).setLink(new URI("http://www.foo.com"));
      fFactory.createPerson(null, news5).setName("Foo bar");

      DynamicDAO.save(news1);
      DynamicDAO.save(news2);
      DynamicDAO.save(news3);
      DynamicDAO.save(news4);
      DynamicDAO.save(news5);
      waitForIndexer();

      fFiltering.setPattern("foo");
      fFiltering.setSearchTarget(NewsFilter.SearchTarget.ALL);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(3, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2, news5 })));
    }

    /* Filter: Text Pattern (Author) */
    {
      fFiltering.setSearchTarget(NewsFilter.SearchTarget.AUTHOR);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(1, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news5 })));
    }

    /* Filter: Text Pattern (Category) */
    {
      fFactory.createCategory(null, news1).setName("Foo bar");
      fFactory.createCategory(null, news2).setName("Bar Foo");

      DynamicDAO.save(news1);
      DynamicDAO.save(news2);
      waitForIndexer();

      fFiltering.setPattern("*foo*");
      fFiltering.setSearchTarget(NewsFilter.SearchTarget.CATEGORY);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(2, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2 })));
    }

    /* Filter: Text Pattern (Source) */
    {
      fFactory.createSource(news4).setLink(new URI("http://www.foo.com"));
      fFactory.createSource(news5).setLink(new URI("http://www.foo.com"));

      DynamicDAO.save(news4);
      DynamicDAO.save(news5);
      waitForIndexer();

      fFiltering.setPattern("*foo*");
      fFiltering.setSearchTarget(NewsFilter.SearchTarget.SOURCE);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(2, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news4, news5 })));
    }

    /* Filter: Text Pattern (Attachments) */
    {
      IAttachment a1 = fFactory.createAttachment(null, news1);
      a1.setLink(new URI("http://www.link1.com"));
      a1.setType("Foo bar");

      fFactory.createAttachment(null, news2).setLink(new URI("http://www.foo.com"));

      DynamicDAO.save(news1);
      DynamicDAO.save(news2);
      waitForIndexer();

      fFiltering.setPattern("*foo*");
      fFiltering.setSearchTarget(NewsFilter.SearchTarget.ATTACHMENTS);
      List<?> result = Arrays.asList(fFiltering.filter(fNullViewer, (Object) null, elements));
      assertEquals(3, result.size());
      assertEquals(true, result.containsAll(Arrays.asList(new IEntity[] { news1, news2, news4 })));
    }
  }

  private int countEntities(Collection<EntityGroup> group) {
    int count = 0;

    for (EntityGroup entityGroup : group) {
      count += entityGroup.getEntities().size();
    }

    return count;
  }
}
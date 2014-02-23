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

import static junit.framework.Assert.assertEquals;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.dialogs.cleanup.CleanUpSummaryModel;
import org.rssowl.ui.internal.dialogs.cleanup.operations.AbstractCleanUpOperation;
import org.rssowl.ui.internal.dialogs.cleanup.operations.ICleanUpOperation;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.DeleteBookMarkTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.CleanUpTaskGroup;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.AbstractCleanUpTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.DeleteNewsTask;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Tests the CleanUp operations.
 *
 * @author bpasero
 */
public class CleanUpTests {

  /* One Day in millis */
  private static final long DAY = 24 * 60 * 60 * 1000;

  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    fFactory = Owl.getModelFactory();

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * Test: Delete BookMarks that have Last Visit > X Days ago
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpBookmarksByLastVisitDate() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Last Visit Date = 3 days */
    ICleanUpOperation ops = new AbstractCleanUpOperation(true, 3, false, 0, false, false, false, false, 0, false, 0, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> tasks = model.getTasks();

      /* Assert Empty (except default ops) */
      assertEquals(1, tasks.size());
    }

    bm3.setCreationDate(new Date(System.currentTimeMillis() - 5 * DAY));

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);

      assertEquals(bm3, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
    }

    bm3.setCreationDate(new Date(System.currentTimeMillis() - 2 * DAY));
    bm1.setLastVisitDate(new Date(System.currentTimeMillis() - 2 * DAY));

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> tasks = model.getTasks();

      /* Assert Empty (except default ops) */
      assertEquals(1, tasks.size());
    }

    bm1.setLastVisitDate(new Date(System.currentTimeMillis() - 3 * DAY));

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> tasks = model.getTasks();

      /* Assert Empty (except default ops) */
      assertEquals(1, tasks.size());
    }

    bm1.setLastVisitDate(new Date(System.currentTimeMillis() - 4 * DAY));

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);

      assertEquals(bm1, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
    }

    bm2.setLastVisitDate(new Date(System.currentTimeMillis() - 40 * DAY));

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(2, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);
      assertEquals(true, tasks.get(1) instanceof DeleteBookMarkTask);

      assertEquals(bm1, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
      assertEquals(bm2, ((DeleteBookMarkTask) tasks.get(1)).getBookmarkMark());
    }
  }

  /**
   * Test: Delete BookMarks that have Last Update > X Days ago
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpBookmarksByLastUpdateDate() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 5 * DAY));

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));

    INews news3 = fFactory.createNews(null, feed3, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() - 2 * DAY));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    bm1.setMostRecentNewsDate(DateUtils.getRecentDate(news1));

    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    bm2.setMostRecentNewsDate(DateUtils.getRecentDate(news2));

    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");
    bm3.setMostRecentNewsDate(DateUtils.getRecentDate(news3));

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Last Update Date = 3 days */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, true, 3, false, false, false, false, 0, false, 0, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(2, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);
      assertEquals(true, tasks.get(1) instanceof DeleteBookMarkTask);

      assertEquals(bm1, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
      assertEquals(bm2, ((DeleteBookMarkTask) tasks.get(1)).getBookmarkMark());
    }
  }

  /**
   * Test: Delete BookMarks that have a connection error
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpBookmarksByConnectionError() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");
    bm3.setErrorLoading(true);

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Last Update Date = 3 days */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, true, false, false, false, 0, false, 0, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);

      assertEquals(bm3, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
    }
  }

  /**
   * Test: Delete duplicate BookMarks
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpBookmarksByDuplicates() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    Calendar cal = Calendar.getInstance();

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    cal.set(2008, 10, 15, 12, 0);
    bm1.setCreationDate(cal.getTime());

    IBookMark bmMostRecentDuplicate = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1 Most Recent Duplicate");
    cal.set(2008, 10, 18, 12, 0);
    bmMostRecentDuplicate.setCreationDate(cal.getTime());

    IBookMark bmOldestDuplicate = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1 Oldest Duplicate");
    cal.set(2007, 9, 18, 12, 0);
    bmOldestDuplicate.setCreationDate(cal.getTime());

    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bmMostRecentDuplicate);
    DynamicDAO.save(bmOldestDuplicate);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bmMostRecentDuplicate);
    marks.add(bmOldestDuplicate);
    marks.add(bm2);
    marks.add(bm3);

    /* Delete Duplicates */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, true, false, false, 0, false, 0, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(2, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);

      assertEquals(bm1, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
      assertEquals(bmMostRecentDuplicate, ((DeleteBookMarkTask) tasks.get(1)).getBookmarkMark());
    }
  }

  /**
   * Test: Delete BookMarks that have Last Update > X Days ago and Last Visit >
   * X Days ago
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpBookmarksByLastUpdateAndLastVisit() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));

    INews news3 = fFactory.createNews(null, feed3, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() - 2 * DAY));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    bm1.setMostRecentNewsDate(news1.getPublishDate());

    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    bm2.setMostRecentNewsDate(news2.getPublishDate());

    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");
    bm3.setMostRecentNewsDate(news3.getPublishDate());

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Last Update Date = 3 days, Last Visit = 3 days */
    ICleanUpOperation ops = new AbstractCleanUpOperation(true, 3, true, 3, false, false, false, false, 0, false, 0, false, false, false);

    bm3.setLastVisitDate(new Date(System.currentTimeMillis() - 4 * DAY));

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(3, groups.size());

      List<AbstractCleanUpTask> tasks1 = groups.get(1).getTasks();
      assertEquals(1, tasks1.size());
      assertEquals(true, tasks1.get(0) instanceof DeleteBookMarkTask);

      List<AbstractCleanUpTask> tasks2 = groups.get(2).getTasks();
      assertEquals(1, tasks2.size());
      assertEquals(true, tasks2.get(0) instanceof DeleteBookMarkTask);

      assertEquals(bm1, ((DeleteBookMarkTask) tasks2.get(0)).getBookmarkMark());
      assertEquals(bm3, ((DeleteBookMarkTask) tasks1.get(0)).getBookmarkMark());
    }

    bm1.setLastVisitDate(new Date(System.currentTimeMillis() - 4 * DAY));
    bm2.setLastVisitDate(new Date(System.currentTimeMillis() - 4 * DAY));

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks1 = groups.get(1).getTasks();
      assertEquals(3, tasks1.size());

      assertEquals(bm1, ((DeleteBookMarkTask) tasks1.get(0)).getBookmarkMark());
      assertEquals(bm2, ((DeleteBookMarkTask) tasks1.get(1)).getBookmarkMark());
      assertEquals(bm3, ((DeleteBookMarkTask) tasks1.get(2)).getBookmarkMark());
    }
  }

  /**
   * Test: Delete News that exceed a certain limit per feed
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpNewsByCount() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    fFactory.createNews(null, feed1, new Date());

    fFactory.createNews(null, feed2, new Date());
    fFactory.createNews(null, feed2, new Date()).setFlagged(true);

    fFactory.createNews(null, feed3, new Date());
    INews news = fFactory.createNews(null, feed3, new Date(0));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Max News Count: 1 */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, true, 1, false, 0, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteNewsTask);

      assertEquals(news, ((DeleteNewsTask) tasks.get(0)).getNews().iterator().next().resolve());
    }

    /* Max News Count: 2 */
    ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, true, 2, false, 0, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Empty */
      assertEquals(1, groups.size());
    }
  }

  /**
   * Test: Delete News that have Age > X Days
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpNewsByAge() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));

    INews news3 = fFactory.createNews(null, feed3, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() - 2 * DAY));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Max News Age = 3 days */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, false, 0, true, 3, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteNewsTask);

      assertEquals(news1, ((DeleteNewsTask) tasks.get(0)).getNews().iterator().next().resolve());
    }

    /* Max News Age = 1 days */
    ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, false, 0, true, 1, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(3, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteNewsTask);
      assertEquals(true, tasks.get(1) instanceof DeleteNewsTask);
      assertEquals(true, tasks.get(2) instanceof DeleteNewsTask);
    }
  }

  /**
   * Test: Delete News that have Age > X Days but Keep Unread
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpNewsByAgeButKeepUnread() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));

    INews news3 = fFactory.createNews(null, feed3, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() - 2 * DAY));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Max News Age = 3 days and keep unread */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, false, 0, true, 3, false, true, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Empty */
      assertEquals(1, groups.size());
    }

    news1.setState(INews.State.READ);
    DynamicDAO.save(news1);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteNewsTask);

      assertEquals(news1, ((DeleteNewsTask) tasks.get(0)).getNews().iterator().next().resolve());
    }
  }

  /**
   * Test: Delete News that have Age > X Days but Keep Labeled
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpNewsByAgeButKeepLabeled() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));
    news1.addLabel(label);

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));
    news2.addLabel(label);

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Max News Age = 3 days and keep unread */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, false, 0, true, 3, false, false, true);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Empty */
      assertEquals(1, groups.size());
    }

    news1.removeLabel(label);
    news2.removeLabel(label);
    DynamicDAO.save(news1);
    DynamicDAO.save(news2);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteNewsTask);

      assertEquals(news1, ((DeleteNewsTask) tasks.get(0)).getNews().iterator().next().resolve());
    }
  }

  /**
   * Test: Delete News that have Age > X Days but Keep Labeled and Unread
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpNewsByAgeButKeepLabeledAndUnread() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));
    news1.addLabel(label);

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Max News Age = 3 days and keep unread */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, false, 0, true, 3, false, false, true);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Empty */
      assertEquals(1, groups.size());
    }

    news1.removeLabel(label);
    news2.setState(INews.State.READ);
    DynamicDAO.save(news1);
    DynamicDAO.save(news2);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(1, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteNewsTask);

      assertEquals(news1, ((DeleteNewsTask) tasks.get(0)).getNews().iterator().next().resolve());
    }
  }

  /**
   * Test: Delete BookMarks that have Last Visit > X Days ago AND Delete News
   * that have Age > X Days
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpBookMarksByLastVisitAndNewsByAge() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));

    INews news3 = fFactory.createNews(null, feed3, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() - 2 * DAY));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    bm1.setLastVisitDate(new Date(System.currentTimeMillis() - 5 * DAY));

    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    bm2.setLastVisitDate(new Date(System.currentTimeMillis() - 4 * DAY));

    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");
    bm3.setLastVisitDate(new Date(System.currentTimeMillis() - 2 * DAY));

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Max Last Visit Age = 3 days && Max News Age = 3 days */
    ICleanUpOperation ops = new AbstractCleanUpOperation(true, 3, false, 0, false, false, false, false, 0, true, 3, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(2, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);
      assertEquals(true, tasks.get(1) instanceof DeleteBookMarkTask);

      assertEquals(bm1, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
      assertEquals(bm2, ((DeleteBookMarkTask) tasks.get(1)).getBookmarkMark());
    }
  }

  /**
   * Test: Delete News that have Age > X Days and exceed a certain Limit.
   *
   * @throws Exception
   */
  @Test
  public void testCleanUpNewsByAgeAndCount() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("http://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("http://www.feed3.com"));

    INews news1 = fFactory.createNews(null, feed1, new Date());
    news1.setPublishDate(new Date(System.currentTimeMillis() - 4 * DAY));

    INews news2 = fFactory.createNews(null, feed2, new Date());
    news2.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));

    INews news3 = fFactory.createNews(null, feed3, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() - 2 * DAY));

    INews news4 = fFactory.createNews(null, feed3, new Date());
    news4.setPublishDate(new Date(System.currentTimeMillis() - 3 * DAY));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Max News Age = 3 days and Max Count = 1 */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, false, true, 1, true, 3, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(3, groups.size());

      List<AbstractCleanUpTask> tasks1 = groups.get(1).getTasks();
      assertEquals(1, tasks1.size());
      assertEquals(true, tasks1.get(0) instanceof DeleteNewsTask);

      List<AbstractCleanUpTask> tasks2 = groups.get(2).getTasks();
      assertEquals(1, tasks1.size());
      assertEquals(true, tasks2.get(0) instanceof DeleteNewsTask);

      assertEquals(news4, ((DeleteNewsTask) tasks1.get(0)).getNews().iterator().next().resolve());
      assertEquals(news1, ((DeleteNewsTask) tasks2.get(0)).getNews().iterator().next().resolve());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore
  public void testCleanUpBookmarksBySynchronization() throws Exception {
    IFolder rootFolder = fFactory.createFolder(null, null, "Root");
    DynamicDAO.save(rootFolder);

    IFeed feed1 = fFactory.createFeed(null, new URI("reader://www.feed1.com"));
    IFeed feed2 = fFactory.createFeed(null, new URI("reader://www.feed2.com"));
    IFeed feed3 = fFactory.createFeed(null, new URI("reader://www.rssowl.org/node/feed"));

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);
    DynamicDAO.save(feed3);

    IBookMark bm1 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed1.getLink()), "BM1");
    IBookMark bm2 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed2.getLink()), "BM2");
    IBookMark bm3 = fFactory.createBookMark(null, rootFolder, new FeedLinkReference(feed3.getLink()), "BM3");

    DynamicDAO.save(bm1);
    DynamicDAO.save(bm2);
    DynamicDAO.save(bm3);

    List<IBookMark> marks = new ArrayList<IBookMark>();
    marks.add(bm1);
    marks.add(bm2);
    marks.add(bm3);

    /* Last Update Date = 3 days */
    ICleanUpOperation ops = new AbstractCleanUpOperation(false, 0, false, 0, false, false, true, false, 0, false, 0, false, false, false);

    {
      CleanUpSummaryModel model = new CleanUpSummaryModel(ops, marks);
      model.generate(new NullProgressMonitor());
      List<CleanUpTaskGroup> groups = model.getTasks();

      /* Assert Filled */
      assertEquals(2, groups.size());

      List<AbstractCleanUpTask> tasks = groups.get(1).getTasks();
      assertEquals(2, tasks.size());
      assertEquals(true, tasks.get(0) instanceof DeleteBookMarkTask);
      assertEquals(true, tasks.get(1) instanceof DeleteBookMarkTask);

      assertEquals(bm1, ((DeleteBookMarkTask) tasks.get(0)).getBookmarkMark());
      assertEquals(bm2, ((DeleteBookMarkTask) tasks.get(1)).getBookmarkMark());
    }
  }
}
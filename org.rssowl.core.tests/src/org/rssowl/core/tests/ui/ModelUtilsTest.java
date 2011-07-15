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
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Category;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.Label;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.Person;
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
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.EntityGroupItem;
import org.rssowl.ui.internal.util.ModelUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests for the ModelUtils and CoreUtils class of the UI Plugin.
 *
 * @author bpasero
 */
public class ModelUtilsTest {
  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetEntitiesFromSelection() throws Exception {
    ILabel label1 = fFactory.createLabel(null, "Label 1");
    ILabel label2 = fFactory.createLabel(null, "Label 2");
    ILabel label3 = fFactory.createLabel(null, "Label 3");

    EntityGroup group = new EntityGroup(1, "Group");
    new EntityGroupItem(group, label2);
    new EntityGroupItem(group, label3);

    Object selectedItems[] = new Object[] { label1, group };

    IStructuredSelection sel = new StructuredSelection(selectedItems);
    List<IEntity> entities = ModelUtils.getEntities(sel);

    assertEquals(3, entities.size());

    int l1 = 0, l2 = 0, l3 = 0;

    for (IEntity entity : entities) {
      assertTrue(entity instanceof ILabel);
      if ("Label 1".equals(((ILabel) entity).getName()))
        l1++;
      else if ("Label 2".equals(((ILabel) entity).getName()))
        l2++;
      else if ("Label 3".equals(((ILabel) entity).getName()))
        l3++;
    }

    assertEquals(1, l1);
    assertEquals(1, l2);
    assertEquals(1, l3);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetEntitiesFromSelectionWithClass() throws Exception {
    ILabel label1 = fFactory.createLabel(null, "Label 1");
    ILabel label2 = fFactory.createLabel(null, "Label 2");
    ILabel label3 = fFactory.createLabel(null, "Label 3");
    IFeed feed1 = fFactory.createFeed(null, new URI("http://www.news.com"));

    EntityGroup group = new EntityGroup(1, "Group");
    new EntityGroupItem(group, label2);
    new EntityGroupItem(group, label3);

    Object selectedItems[] = new Object[] { label1, feed1, group };

    IStructuredSelection sel = new StructuredSelection(selectedItems);
    List<ILabel> labels = ModelUtils.getEntities(sel, ILabel.class);

    assertEquals(3, labels.size());

    int l1 = 0, l2 = 0, l3 = 0;

    for (IEntity entity : labels) {
      assertTrue(entity instanceof ILabel);
      if ("Label 1".equals(((ILabel) entity).getName()))
        l1++;
      else if ("Label 2".equals(((ILabel) entity).getName()))
        l2++;
      else if ("Label 3".equals(((ILabel) entity).getName()))
        l3++;
    }
    assertEquals(1, l1);
    assertEquals(1, l2);
    assertEquals(1, l3);

    List<IFeed> feeds = ModelUtils.getEntities(sel, IFeed.class);
    assertEquals(1, feeds.size());
    assertEquals(feed1, feeds.get(0));

    List<INews> newsList = ModelUtils.getEntities(sel, INews.class);
    assertEquals(0, newsList.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRelax() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.link.com"));
    feed = DynamicDAO.save(feed);

    IFolder root1 = fFactory.createFolder(null, null, "Root 1");
    IFolder subRoot = fFactory.createFolder(null, root1, "Sub Root 1");

    IBookMark mark1 = fFactory.createBookMark(null, root1, new FeedLinkReference(feed.getLink()), "Mark 1");
    IBookMark mark2 = fFactory.createBookMark(null, root1, new FeedLinkReference(feed.getLink()), "Mark 2");
    IBookMark mark3 = fFactory.createBookMark(null, subRoot, new FeedLinkReference(feed.getLink()), "Mark 3");
    IBookMark mark4 = fFactory.createBookMark(null, subRoot, new FeedLinkReference(feed.getLink()), "Mark 4");

    IFolder root2 = fFactory.createFolder(null, null, "Root 2");
    IBookMark mark5 = fFactory.createBookMark(null, root2, new FeedLinkReference(feed.getLink()), "Mark 5");
    IBookMark mark6 = fFactory.createBookMark(null, root2, new FeedLinkReference(feed.getLink()), "Mark 6");

    /* Relax Root 1 */
    List<IEntity> entities = new ArrayList<IEntity>();
    entities.add(root1);
    entities.add(root2);
    entities.add(mark5);
    entities.add(mark6);
    entities.add(subRoot);
    entities.add(mark1);
    entities.add(mark2);
    entities.add(mark3);
    entities.add(mark4);

    CoreUtils.normalize(root1, entities);
    assertEquals(4, entities.size());
    assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { root1, root2, mark5, mark6 })));

    /* Relax Sub Root 1 */
    entities = new ArrayList<IEntity>();
    entities.add(root1);
    entities.add(root2);
    entities.add(mark5);
    entities.add(mark6);
    entities.add(subRoot);
    entities.add(mark1);
    entities.add(mark2);
    entities.add(mark3);
    entities.add(mark4);

    CoreUtils.normalize(subRoot, entities);
    assertEquals(7, entities.size());
    assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { root1, root2, subRoot, mark1, mark2, mark5, mark6 })));

    /* Relax Root 2 */
    entities = new ArrayList<IEntity>();
    entities.add(root1);
    entities.add(root2);
    entities.add(mark5);
    entities.add(mark6);
    entities.add(subRoot);
    entities.add(mark1);
    entities.add(mark2);
    entities.add(mark3);
    entities.add(mark4);

    CoreUtils.normalize(root2, entities);
    assertEquals(7, entities.size());
    assertEquals(true, entities.containsAll(Arrays.asList(new IEntity[] { root1, root2, subRoot, mark1, mark2, mark3, mark4 })));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsNewsStateChange() throws Exception {
    IFeed feed = fFactory.createFeed(Long.valueOf(0L), new URI("http://www.rssowl.org"));

    Set<ModelEvent> events = new HashSet<ModelEvent>();

    INews newNews = fFactory.createNews(0L, feed, new Date());
    newNews.setState(INews.State.NEW);

    INews unreadNews = fFactory.createNews(1L, feed, new Date());
    unreadNews.setState(INews.State.UNREAD);

    INews readNews = fFactory.createNews(2L, feed, new Date());
    readNews.setState(INews.State.READ);

    INews hiddenNews = fFactory.createNews(3L, feed, new Date());
    hiddenNews.setState(INews.State.HIDDEN);

    INews deletedNews = fFactory.createNews(4L, feed, new Date());
    deletedNews.setState(INews.State.DELETED);

    INews readNews2 = fFactory.createNews(5L, feed, new Date());
    readNews2.setState(INews.State.READ);

    INews unreadNews2 = fFactory.createNews(6L, feed, new Date());
    unreadNews2.setState(INews.State.UNREAD);

    INews hiddenNews2 = fFactory.createNews(7L, feed, new Date());
    hiddenNews2.setState(INews.State.HIDDEN);

    NewsEvent event1 = new NewsEvent(newNews, newNews, true);
    NewsEvent event2 = new NewsEvent(newNews, unreadNews, true);
    NewsEvent event3 = new NewsEvent(newNews, readNews, true);
    NewsEvent event4 = new NewsEvent(unreadNews, readNews2, true);
    NewsEvent event5 = new NewsEvent(unreadNews, unreadNews2, true);
    NewsEvent event6 = new NewsEvent(hiddenNews, hiddenNews, true);
    NewsEvent event7 = new NewsEvent(newNews, hiddenNews2, true);
    NewsEvent event8 = new NewsEvent(newNews, deletedNews, true);

    events.add(event1);
    assertEquals(false, CoreUtils.isNewStateChange(events));
    events.add(event4);
    assertEquals(false, CoreUtils.isNewStateChange(events));
    events.add(event5);
    assertEquals(false, CoreUtils.isNewStateChange(events));
    events.add(event6);
    assertEquals(false, CoreUtils.isNewStateChange(events));
    events.add(event2);
    assertEquals(true, CoreUtils.isNewStateChange(events));
    events.add(event3);
    assertEquals(true, CoreUtils.isNewStateChange(events));

    events.clear();
    events.add(event7);
    assertEquals(true, CoreUtils.isNewStateChange(events));

    events.clear();
    events.add(event8);
    assertEquals(true, CoreUtils.isNewStateChange(events));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testHasBecomeUnread() throws Exception {
    IFeed feed = fFactory.createFeed(Long.valueOf(0L), new URI("http://www.rssowl.org"));

    Set<ModelEvent> events = new HashSet<ModelEvent>();

    INews newNews = fFactory.createNews(0L, feed, new Date());
    newNews.setState(INews.State.NEW);

    INews unreadNews = fFactory.createNews(1L, feed, new Date());
    unreadNews.setState(INews.State.UNREAD);

    INews readNews = fFactory.createNews(2L, feed, new Date());
    readNews.setState(INews.State.READ);

    INews hiddenNews = fFactory.createNews(3L, feed, new Date());
    hiddenNews.setState(INews.State.HIDDEN);

    INews deletedNews = fFactory.createNews(4L, feed, new Date());
    deletedNews.setState(INews.State.DELETED);

    INews readNews2 = fFactory.createNews(5L, feed, new Date());
    readNews2.setState(INews.State.READ);

    INews unreadNews2 = fFactory.createNews(6L, feed, new Date());
    unreadNews2.setState(INews.State.UNREAD);

    INews hiddenNews2 = fFactory.createNews(7L, feed, new Date());
    hiddenNews2.setState(INews.State.HIDDEN);

    NewsEvent event1 = new NewsEvent(newNews, newNews, true);
    NewsEvent event2 = new NewsEvent(newNews, unreadNews, true);
    NewsEvent event3 = new NewsEvent(newNews, readNews, true);
    NewsEvent event4 = new NewsEvent(unreadNews, readNews2, true);
    NewsEvent event5 = new NewsEvent(unreadNews, unreadNews2, true);
    NewsEvent event6 = new NewsEvent(hiddenNews, hiddenNews, true);
    NewsEvent event7 = new NewsEvent(newNews, hiddenNews2, true);
    NewsEvent event8 = new NewsEvent(newNews, deletedNews, true);
    NewsEvent event9 = new NewsEvent(readNews, newNews, true);
    NewsEvent event10 = new NewsEvent(hiddenNews, unreadNews, true);
    NewsEvent event11 = new NewsEvent(readNews, unreadNews, true);

    events.add(event1);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));
    events.add(event4);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));
    events.add(event5);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));
    events.add(event6);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));
    events.add(event2);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));
    events.add(event3);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));

    events.clear();
    events.add(event7);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));

    events.clear();
    events.add(event8);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));

    events.clear();
    events.add(event9);
    assertEquals(true, CoreUtils.changedFromReadToUnread(events));

    events.clear();
    events.add(event10);
    assertEquals(false, CoreUtils.changedFromReadToUnread(events));

    events.clear();
    events.add(event11);
    assertEquals(true, CoreUtils.changedFromReadToUnread(events));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsReadStateChange() throws Exception {
    IFeed feed = fFactory.createFeed(Long.valueOf(0L), new URI("http://www.rssowl.org"));

    Set<ModelEvent> events = new HashSet<ModelEvent>();

    INews newNews = fFactory.createNews(0L, feed, new Date());
    newNews.setState(INews.State.NEW);

    INews unreadNews = fFactory.createNews(1L, feed, new Date());
    unreadNews.setState(INews.State.UNREAD);

    INews readNews = fFactory.createNews(2L, feed, new Date());
    readNews.setState(INews.State.READ);

    INews hiddenNews = fFactory.createNews(3L, feed, new Date());
    hiddenNews.setState(INews.State.HIDDEN);

    INews deletedNews = fFactory.createNews(4L, feed, new Date());
    deletedNews.setState(INews.State.DELETED);

    INews updatedNews = fFactory.createNews(5L, feed, new Date());
    updatedNews.setState(INews.State.UPDATED);

    INews unreadNews2 = fFactory.createNews(6L, feed, new Date());
    unreadNews2.setState(INews.State.UNREAD);

    INews hiddenNews2 = fFactory.createNews(7L, feed, new Date());
    hiddenNews2.setState(INews.State.HIDDEN);

    NewsEvent event1 = new NewsEvent(newNews, newNews, true);
    NewsEvent event2 = new NewsEvent(deletedNews, unreadNews, true);
    NewsEvent event3 = new NewsEvent(hiddenNews, updatedNews, true);
    NewsEvent event4 = new NewsEvent(unreadNews, readNews, true);
    NewsEvent event5 = new NewsEvent(unreadNews, unreadNews2, true);
    NewsEvent event6 = new NewsEvent(hiddenNews, hiddenNews, true);
    NewsEvent event7 = new NewsEvent(newNews, hiddenNews2, true);
    NewsEvent event8 = new NewsEvent(newNews, deletedNews, true);

    events.add(event1);
    assertEquals(false, CoreUtils.isReadStateChange(events));
    events.add(event4);
    assertEquals(true, CoreUtils.isReadStateChange(events));
    events.add(event5);
    assertEquals(true, CoreUtils.isReadStateChange(events));

    events.clear();
    events.add(event7);
    assertEquals(true, CoreUtils.isReadStateChange(events));

    events.clear();
    events.add(event8);
    assertEquals(true, CoreUtils.isReadStateChange(events));

    events.clear();
    events.add(event2);
    assertEquals(true, CoreUtils.isReadStateChange(events));

    events.clear();
    events.add(event3);
    assertEquals(true, CoreUtils.isReadStateChange(events));

    events.clear();
    events.add(event6);
    assertEquals(false, CoreUtils.isReadStateChange(events));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsNewOrReadStateChange() throws Exception {
    {
      IFeed feed = fFactory.createFeed(Long.valueOf(0L), new URI("http://www.rssowl.org"));

      Set<ModelEvent> events = new HashSet<ModelEvent>();

      INews newNews = fFactory.createNews(0L, feed, new Date());
      newNews.setState(INews.State.NEW);

      INews unreadNews = fFactory.createNews(1L, feed, new Date());
      unreadNews.setState(INews.State.UNREAD);

      INews readNews = fFactory.createNews(2L, feed, new Date());
      readNews.setState(INews.State.READ);

      INews hiddenNews = fFactory.createNews(3L, feed, new Date());
      hiddenNews.setState(INews.State.HIDDEN);

      INews deletedNews = fFactory.createNews(4L, feed, new Date());
      deletedNews.setState(INews.State.DELETED);

      INews readNews2 = fFactory.createNews(5L, feed, new Date());
      readNews2.setState(INews.State.READ);

      INews unreadNews2 = fFactory.createNews(6L, feed, new Date());
      unreadNews2.setState(INews.State.UNREAD);

      INews hiddenNews2 = fFactory.createNews(7L, feed, new Date());
      hiddenNews2.setState(INews.State.HIDDEN);

      NewsEvent event1 = new NewsEvent(newNews, newNews, true);
      NewsEvent event2 = new NewsEvent(newNews, unreadNews, true);
      NewsEvent event3 = new NewsEvent(newNews, readNews, true);
      NewsEvent event4 = new NewsEvent(unreadNews, readNews2, true);
      NewsEvent event5 = new NewsEvent(unreadNews, unreadNews2, true);
      NewsEvent event6 = new NewsEvent(hiddenNews, hiddenNews, true);
      NewsEvent event7 = new NewsEvent(newNews, hiddenNews2, true);
      NewsEvent event8 = new NewsEvent(newNews, deletedNews, true);

      events.add(event1);
      assertEquals(false, CoreUtils.isNewOrReadStateChange(events));
      events.add(event4);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));
      events.add(event5);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));
      events.add(event6);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));
      events.add(event2);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));
      events.add(event3);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));

      events.clear();
      events.add(event7);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));

      events.clear();
      events.add(event8);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));
    }

    {
      IFeed feed = fFactory.createFeed(Long.valueOf(0L), new URI("http://www.rssowl.org"));

      Set<ModelEvent> events = new HashSet<ModelEvent>();

      INews newNews = fFactory.createNews(0L, feed, new Date());
      newNews.setState(INews.State.NEW);

      INews unreadNews = fFactory.createNews(1L, feed, new Date());
      unreadNews.setState(INews.State.UNREAD);

      INews readNews = fFactory.createNews(2L, feed, new Date());
      readNews.setState(INews.State.READ);

      INews hiddenNews = fFactory.createNews(3L, feed, new Date());
      hiddenNews.setState(INews.State.HIDDEN);

      INews deletedNews = fFactory.createNews(4L, feed, new Date());
      deletedNews.setState(INews.State.DELETED);

      INews updatedNews = fFactory.createNews(5L, feed, new Date());
      updatedNews.setState(INews.State.UPDATED);

      INews unreadNews2 = fFactory.createNews(6L, feed, new Date());
      unreadNews2.setState(INews.State.UNREAD);

      INews hiddenNews2 = fFactory.createNews(7L, feed, new Date());
      hiddenNews2.setState(INews.State.HIDDEN);

      NewsEvent event1 = new NewsEvent(newNews, newNews, true);
      NewsEvent event2 = new NewsEvent(deletedNews, unreadNews, true);
      NewsEvent event3 = new NewsEvent(hiddenNews, updatedNews, true);
      NewsEvent event4 = new NewsEvent(unreadNews, readNews, true);
      NewsEvent event5 = new NewsEvent(unreadNews, unreadNews2, true);
      NewsEvent event6 = new NewsEvent(hiddenNews, hiddenNews, true);
      NewsEvent event7 = new NewsEvent(newNews, hiddenNews2, true);
      NewsEvent event8 = new NewsEvent(newNews, deletedNews, true);

      events.add(event1);
      assertEquals(false, CoreUtils.isNewOrReadStateChange(events));
      events.add(event4);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));
      events.add(event5);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));

      events.clear();
      events.add(event7);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));

      events.clear();
      events.add(event8);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));

      events.clear();
      events.add(event2);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));

      events.clear();
      events.add(event3);
      assertEquals(true, CoreUtils.isNewOrReadStateChange(events));

      events.clear();
      events.add(event6);
      assertEquals(false, CoreUtils.isNewOrReadStateChange(events));
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsDateChange() throws Exception {
    Date now = new Date();

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    news1.setPublishDate(now);

    INews news2 = new News(null, feed, new Date());
    news2.setPublishDate(now);

    INews news3 = new News(null, feed, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() + 1000));

    feed.addNews(news1);
    feed.addNews(news2);
    feed.addNews(news3);

    NewsEvent event1 = new NewsEvent(news1, news2, true);
    assertEquals(false, CoreUtils.isDateChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1, news3, true);
    assertEquals(true, CoreUtils.isDateChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsPublishedDateChange() throws Exception {
    Date now = new Date();

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    news1.setPublishDate(now);

    INews news2 = new News(null, feed, new Date());
    news2.setPublishDate(now);

    INews news3 = new News(null, feed, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() + 1000));

    feed.addNews(news1);
    feed.addNews(news2);
    feed.addNews(news3);

    NewsEvent event1 = new NewsEvent(news1, news2, true);
    assertEquals(false, CoreUtils.isPublishedDateChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1, news3, true);
    assertEquals(true, CoreUtils.isPublishedDateChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsModifiedDateChange() throws Exception {
    Date now = new Date();

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    news1.setModifiedDate(now);

    INews news2 = new News(null, feed, new Date());
    news2.setModifiedDate(now);

    INews news3 = new News(null, feed, new Date());
    news3.setModifiedDate(new Date(System.currentTimeMillis() + 1000));

    feed.addNews(news1);
    feed.addNews(news2);
    feed.addNews(news3);

    NewsEvent event1 = new NewsEvent(news1, news2, true);
    assertEquals(false, CoreUtils.isModifiedDateChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1, news3, true);
    assertEquals(true, CoreUtils.isModifiedDateChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsLocationChange() throws Exception {
    IFolder root = fFactory.createFolder(null, null, "Root");
    INewsBin bin1 = fFactory.createNewsBin(null, root, "A");
    INewsBin bin2 = fFactory.createNewsBin(null, root, "B");

    DynamicDAO.save(root);

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    feed.addNews(news1);

    INews news1Bin = fFactory.createNews(news1, bin1);
    INews news2Bin = fFactory.createNews(news1, bin2);

    NewsEvent event1 = new NewsEvent(news1, news1Bin, true);
    assertEquals(false, CoreUtils.isLocationChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1Bin, news2Bin, true);
    assertEquals(true, CoreUtils.isLocationChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsAuthorChange() throws Exception {
    IPerson author1 = new Person((Long) null);
    author1.setName("Foo");

    IPerson author2 = new Person((Long) null);
    author2.setName("Bar");

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    news1.setAuthor(author1);

    INews news2 = new News(null, feed, new Date());
    news2.setAuthor(author1);

    INews news3 = new News(null, feed, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() + 1000));
    news3.setAuthor(author2);

    INews news4 = new News(null, feed, new Date());

    feed.addNews(news1);
    feed.addNews(news2);
    feed.addNews(news3);
    feed.addNews(news4);

    NewsEvent event1 = new NewsEvent(news1, news2, true);
    assertEquals(false, CoreUtils.isAuthorChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1, news3, true);
    assertEquals(true, CoreUtils.isAuthorChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1, news4, true);
    assertEquals(true, CoreUtils.isAuthorChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news4, news1, true);
    assertEquals(true, CoreUtils.isAuthorChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsCategoryChange() throws Exception {
    ICategory cat1 = new Category();
    cat1.setName("Cat1");

    ICategory cat2 = new Category();
    cat2.setName("Cat2");

    ICategory cat3 = new Category();
    cat3.setName("Cat3");

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    news1.addCategory(cat1);

    INews news2 = new News(null, feed, new Date());
    news2.addCategory(cat1);

    INews news3 = new News(null, feed, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() + 1000));
    news3.addCategory(cat2);

    INews news4 = new News(null, feed, new Date());
    news4.addCategory(cat1);
    news4.addCategory(cat2);

    INews news5 = new News(null, feed, new Date());

    INews news6 = new News(null, feed, new Date());
    news6.addCategory(cat1);
    news6.addCategory(cat2);
    news6.addCategory(cat3);

    INews news7 = new News(null, feed, new Date());
    news7.addCategory(cat1);
    news7.addCategory(cat3);

    feed.addNews(news1);
    feed.addNews(news2);
    feed.addNews(news3);
    feed.addNews(news4);
    feed.addNews(news5);
    feed.addNews(news6);
    feed.addNews(news7);

    NewsEvent event1 = new NewsEvent(news1, news2, true);
    assertEquals(false, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1, news3, true);
    assertEquals(true, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news1, news4, true);
    assertEquals(true, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news4, news1, true);
    assertEquals(true, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news5, news6, true);
    assertEquals(true, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news6, news5, true);
    assertEquals(true, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news7, news4, true);
    assertEquals(true, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));

    event1 = new NewsEvent(news4, news7, true);
    assertEquals(true, CoreUtils.isCategoryChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event1 }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsLabelChangeSingle() throws Exception {
    ILabel label1 = new Label(null, "Label1");
    ILabel label2 = new Label(null, "Label2");

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    news1.addLabel(label1);

    INews news2 = new News(null, feed, new Date());
    news2.addLabel(label1);

    INews news3 = new News(null, feed, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() + 1000));
    news3.addLabel(label2);

    INews news4 = new News(null, feed, new Date());

    feed.addNews(news1);
    feed.addNews(news2);
    feed.addNews(news3);
    feed.addNews(news4);

    NewsEvent event = new NewsEvent(news1, news2, true);
    assertEquals(false, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));

    event = new NewsEvent(news1, news3, true);
    assertEquals(true, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));

    event = new NewsEvent(news1, news4, true);
    assertEquals(true, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));

    event = new NewsEvent(news4, news1, true);
    assertEquals(true, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsLabelChangeMulti() throws Exception {
    ILabel label1 = new Label(null, "Label1");
    ILabel label2 = new Label(null, "Label2");
    ILabel label3 = new Label(null, "Label3");

    IFeed feed = new Feed(new URI("http://www.link.com"));
    INews news1 = new News(null, feed, new Date());
    news1.addLabel(label1);
    news1.addLabel(label3);

    INews news2 = new News(null, feed, new Date());
    news2.addLabel(label1);

    INews news3 = new News(null, feed, new Date());
    news3.setPublishDate(new Date(System.currentTimeMillis() + 1000));
    news3.addLabel(label1);
    news3.addLabel(label2);

    INews news4 = new News(null, feed, new Date());

    feed.addNews(news1);
    feed.addNews(news2);
    feed.addNews(news3);
    feed.addNews(news4);

    NewsEvent event = new NewsEvent(news1, news2, true);
    assertEquals(true, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));

    event = new NewsEvent(news1, news3, true);
    assertEquals(true, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));

    event = new NewsEvent(news1, news4, true);
    assertEquals(true, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));

    event = new NewsEvent(news4, news1, true);
    assertEquals(true, CoreUtils.isLabelChange(new HashSet<NewsEvent>(Arrays.asList(new NewsEvent[] { event }))));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testHasChildRelation() throws Exception {
    IFolder root = new Folder(null, null, "Root");
    FeedLinkReference feed = new FeedLinkReference(new URI("http://www.link.com"));

    /* Sub Root 1 */
    IFolder subRootFolder1 = new Folder(null, root, "SubRootFolder1");
    root.addFolder(subRootFolder1, null, false);

    IFolder subRoot1Folder1 = new Folder(null, subRootFolder1, "SubRoot1Folder1");
    subRootFolder1.addFolder(subRoot1Folder1, null, false);

    IFolder subRoot11Folder1 = new Folder(null, subRoot1Folder1, "SubRoot11Folder1");
    subRoot1Folder1.addFolder(subRoot11Folder1, null, false);

    IBookMark subRoot11Mark1 = new BookMark(null, subRoot1Folder1, feed, "SubRoot11Mark1");
    subRoot1Folder1.addMark(subRoot11Mark1, null, false);

    IBookMark subRoot1Mark1 = new BookMark(null, subRootFolder1, feed, "SubRoot1Mark1");
    subRootFolder1.addMark(subRoot1Mark1, null, false);

    IBookMark subRoot1Mark2 = new BookMark(null, subRootFolder1, feed, "SubRoot1Mark2");
    subRootFolder1.addMark(subRoot1Mark2, null, false);

    /* Begin Testing */
    assertEquals(true, CoreUtils.hasChildRelation(root, subRootFolder1));
    assertEquals(true, CoreUtils.hasChildRelation(root, subRoot1Folder1));
    assertEquals(true, CoreUtils.hasChildRelation(root, subRoot11Folder1));
    assertEquals(true, CoreUtils.hasChildRelation(root, subRoot11Mark1));
    assertEquals(true, CoreUtils.hasChildRelation(root, subRoot1Mark1));
    assertEquals(true, CoreUtils.hasChildRelation(root, subRoot1Mark2));

    assertEquals(true, CoreUtils.hasChildRelation(subRootFolder1, subRoot11Folder1));
    assertEquals(true, CoreUtils.hasChildRelation(subRootFolder1, subRoot11Mark1));
    assertEquals(true, CoreUtils.hasChildRelation(subRootFolder1, subRoot1Mark1));
    assertEquals(true, CoreUtils.hasChildRelation(subRootFolder1, subRoot1Mark2));

    assertEquals(false, CoreUtils.hasChildRelation(subRootFolder1, root));
    assertEquals(false, CoreUtils.hasChildRelation(subRoot11Folder1, root));
    assertEquals(false, CoreUtils.hasChildRelation(subRoot11Folder1, root));
    assertEquals(false, CoreUtils.hasChildRelation(subRoot11Folder1, subRoot1Mark2));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNormalizeTitle() throws Exception {
    String title = "Hello World";
    assertEquals(title, CoreUtils.normalizeTitle(title));

    title = "";
    assertEquals("", CoreUtils.normalizeTitle(title));

    title = null;
    assertEquals(null, CoreUtils.normalizeTitle(title));

    title = "Re: ";
    assertEquals("", CoreUtils.normalizeTitle(title));

    title = "Re[33]:";
    assertEquals("", CoreUtils.normalizeTitle(title));

    title = "Re Hello World";
    assertEquals("Re Hello World", CoreUtils.normalizeTitle(title));

    title = "Re:Hello World";
    assertEquals("Hello World", CoreUtils.normalizeTitle(title));

    title = "Re: Hello World";
    assertEquals("Hello World", CoreUtils.normalizeTitle(title));

    title = "Re: Re: Hello World";
    assertEquals("Hello World", CoreUtils.normalizeTitle(title));

    title = "Re:Re: Hello World";
    assertEquals("Hello World", CoreUtils.normalizeTitle(title));

    title = "Re:Re: Hello World Re:";
    assertEquals("Hello World Re:", CoreUtils.normalizeTitle(title));

    title = "Re: Hello World (re from)";
    assertEquals("Hello World (re from)", CoreUtils.normalizeTitle(title));

    title = "Re(33): Hello World";
    assertEquals("Hello World", CoreUtils.normalizeTitle(title));

    title = "Re[33]: Hello World";
    assertEquals("Hello World", CoreUtils.normalizeTitle(title));

    title = "Re(33: Hello World";
    assertEquals("Re(33: Hello World", CoreUtils.normalizeTitle(title));

    title = "Re[33: Hello World";
    assertEquals("Re[33: Hello World", CoreUtils.normalizeTitle(title));

    title = "Re(33): Hello World[]";
    assertEquals("Hello World[]", CoreUtils.normalizeTitle(title));

    title = "Re[33]: Hello World()";
    assertEquals("Hello World()", CoreUtils.normalizeTitle(title));

    title = "Re(33): Hello World(3)";
    assertEquals("Hello World(3)", CoreUtils.normalizeTitle(title));

    title = "Re[33]: Hello World[3]";
    assertEquals("Hello World[3]", CoreUtils.normalizeTitle(title));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetLabelsForAll() throws Exception {
    ILabel label1 = DynamicDAO.save(fFactory.createLabel(null, "Foo"));
    ILabel label2 = DynamicDAO.save(fFactory.createLabel(null, "Bar"));

    IFeed feed = fFactory.createFeed(null, new URI("feed"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    INews news2 = fFactory.createNews(null, feed, new Date());
    INews news3 = fFactory.createNews(null, feed, new Date());

    news1.addLabel(label1);
    news1.addLabel(label2);
    news2.addLabel(label1);
    news3.addLabel(label2);

    Set<ILabel> labels = ModelUtils.getLabelsForAll(new StructuredSelection(news1));
    assertEquals(2, labels.size());

    labels = ModelUtils.getLabelsForAll(new StructuredSelection(new Object[] { news1, news2 }));
    assertEquals(1, labels.size());

    labels = ModelUtils.getLabelsForAll(new StructuredSelection(new Object[] { news1, news2, news3 }));
    assertEquals(0, labels.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetAttachmentLinks() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.rssowl.org"));
    INews news1 = fFactory.createNews(null, feed, new Date());
    INews news2 = fFactory.createNews(null, feed, new Date());

    IAttachment att = fFactory.createAttachment(null, news2);

    att = fFactory.createAttachment(null, news2);
    att.setLink(new URI("foobar"));

    INews news3 = fFactory.createNews(null, feed, new Date());

    att = fFactory.createAttachment(null, news3);
    att.setLink(new URI("http://www.rssowl.org/download1.mp3"));

    att = fFactory.createAttachment(null, news3);
    att.setLink(new URI("/download2.mp3"));

    att = fFactory.createAttachment(null, news3);
    att.setLink(new URI("download3.mp3"));

    List<INews> news = new ArrayList<INews>();
    news.add(news1);
    news.add(news2);
    news.add(news3);

    DynamicDAO.save(feed);

    List<Pair<IAttachment, URI>> links = ModelUtils.getAttachmentLinks(new StructuredSelection(news));
    assertEquals(4, links.size());

    List<URI> uris = new ArrayList<URI>();
    for (Pair<IAttachment, URI> pair : links) {
      uris.add(pair.getSecond());
    }

    assertTrue(uris.contains(new URI("http://www.rssowl.org/foobar")));
    assertTrue(uris.contains(new URI("http://www.rssowl.org/download1.mp3")));
    assertTrue(uris.contains(new URI("http://www.rssowl.org/download2.mp3")));
    assertTrue(uris.contains(new URI("http://www.rssowl.org/download3.mp3")));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCountNews() throws Exception {
    IFeed feed = fFactory.createFeed(Long.valueOf(0L), new URI("http://www.rssowl.org"));

    INews newNews = fFactory.createNews(null, feed, new Date());
    newNews.setState(INews.State.NEW);

    INews unreadNews = fFactory.createNews(null, feed, new Date());
    unreadNews.setState(INews.State.UNREAD);

    INews readNews = fFactory.createNews(null, feed, new Date());
    readNews.setState(INews.State.READ);

    DynamicDAO.save(feed);

    IFolder folder = fFactory.createFolder(null, null, "Folder");
    IBookMark bm = fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");
    DynamicDAO.save(folder);

    /* Wait for Indexer */
    waitForIndexer();

    assertEquals(3, ModelUtils.countNews(bm));
  }

  private void waitForIndexer() throws InterruptedException {
    Thread.sleep(500);
  }
}
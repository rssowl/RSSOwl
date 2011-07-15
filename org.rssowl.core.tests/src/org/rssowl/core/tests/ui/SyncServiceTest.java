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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.newsaction.DeleteNewsAction;
import org.rssowl.core.internal.newsaction.LabelNewsAction;
import org.rssowl.core.internal.newsaction.MarkReadNewsAction;
import org.rssowl.core.internal.newsaction.MarkStickyNewsAction;
import org.rssowl.core.internal.newsaction.MarkUnreadNewsAction;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.util.SyncItem;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.internal.services.SyncItemsManager;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

/**
 * Tests for the {@link SyncService}, {@link SyncItemsManager} and
 * {@link SyncItem}.
 *
 * @author bpasero
 */
public class SyncServiceTest {
  private IModelFactory fFactory = Owl.getModelFactory();

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl) Owl.getPersistenceService()).recreateSchemaForTests();
    SyncItemsManager manager = new SyncItemsManager();
    manager.startup();
    manager.clearUncommittedItems();
    manager.shutdown();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testEventToSyncItem() throws Exception {
    ILabel label1 = fFactory.createLabel(null, "Foo");
    ILabel label2 = fFactory.createLabel(null, "Bar");

    IFeed feed = fFactory.createFeed(null, URI.create("rssowl.org"));

    INews oldNews = fFactory.createNews(null, feed, new Date());
    oldNews.setInReplyTo(feed.getLink().toString());
    oldNews.setGuid(fFactory.createGuid(oldNews, "tag:google.com/foo", true));

    INews currentNews = fFactory.createNews(null, feed, new Date());
    currentNews.setInReplyTo(feed.getLink().toString());
    currentNews.setGuid(fFactory.createGuid(currentNews, "tag:google.com/foo", true));

    SyncItem item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertNull(item);

    currentNews.setState(INews.State.READ);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertTrue(item.isMarkedRead());
    assertFalse(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertFalse(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    oldNews.setState(INews.State.READ);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertNull(item);

    currentNews.setState(INews.State.DELETED);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertNull(item);

    currentNews.setState(INews.State.HIDDEN);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertNull(item);

    currentNews.setState(INews.State.UNREAD);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertFalse(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    currentNews.setFlagged(true);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertTrue(item.isStarred());
    assertFalse(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    oldNews.setFlagged(true);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertFalse(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    currentNews.setFlagged(false);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertTrue(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    currentNews.addLabel(label1);
    currentNews.addLabel(label2);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertTrue(item.isUnStarred());
    assertEquals(2, item.getAddedLabels().size());
    assertTrue(item.getRemovedLabels().isEmpty());

    oldNews.addLabel(label1);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertTrue(item.isUnStarred());
    assertEquals(1, item.getAddedLabels().size());
    assertTrue(item.getRemovedLabels().isEmpty());

    currentNews.removeLabel(label1);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertTrue(item.isUnStarred());
    assertEquals(1, item.getAddedLabels().size());
    assertEquals("Bar", item.getAddedLabels().get(0));
    assertEquals(1, item.getRemovedLabels().size());
    assertEquals("Foo", item.getRemovedLabels().get(0));

    oldNews.addLabel(label2);
    currentNews.removeLabel(label2);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertTrue(item.isUnStarred());
    assertEquals(0, item.getAddedLabels().size());
    assertEquals(2, item.getRemovedLabels().size());

    currentNews.setState(INews.State.HIDDEN);
    oldNews.setState(INews.State.NEW);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertTrue(item.isMarkedRead());
    assertFalse(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertTrue(item.isUnStarred());
    assertEquals(0, item.getAddedLabels().size());
    assertEquals(2, item.getRemovedLabels().size());

    currentNews.setFlagged(true);
    oldNews.setFlagged(true);
    item = SyncItem.toSyncItem(new NewsEvent(oldNews, currentNews, true));
    assertTrue(item.isMarkedRead());
    assertFalse(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertTrue(item.isUnStarred());
    assertEquals(0, item.getAddedLabels().size());
    assertEquals(2, item.getRemovedLabels().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFilterToSyncItem() throws Exception {
    ILabel label1 = fFactory.createLabel(null, "Foo");
    ILabel label2 = fFactory.createLabel(null, "Bar");

    DynamicDAO.save(label1);
    DynamicDAO.save(label2);

    IFeed feed = fFactory.createFeed(null, URI.create("rssowl.org"));
    ISearchFilter filter = fFactory.createSearchFilter(null, fFactory.createSearch(null), "Filter");

    INews currentNews = fFactory.createNews(null, feed, new Date());
    currentNews.setInReplyTo(feed.getLink().toString());
    currentNews.setGuid(fFactory.createGuid(currentNews, "tag:google.com/foo", true));

    SyncItem item = SyncItem.toSyncItem(filter, currentNews);
    assertNull(item);

    filter.addAction(fFactory.createFilterAction(MarkReadNewsAction.ID));
    item = SyncItem.toSyncItem(filter, currentNews);

    assertTrue(item.isMarkedRead());
    assertFalse(item.isMarkedUnread());
    assertFalse(item.isStarred());
    assertFalse(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    filter.addAction(fFactory.createFilterAction(MarkStickyNewsAction.ID));
    item = SyncItem.toSyncItem(filter, currentNews);

    assertTrue(item.isMarkedRead());
    assertFalse(item.isMarkedUnread());
    assertTrue(item.isStarred());
    assertFalse(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    filter.addAction(fFactory.createFilterAction(MarkUnreadNewsAction.ID));
    item = SyncItem.toSyncItem(filter, currentNews);

    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertTrue(item.isStarred());
    assertFalse(item.isUnStarred());
    assertTrue(item.getAddedLabels().isEmpty());
    assertTrue(item.getRemovedLabels().isEmpty());

    IFilterAction action = fFactory.createFilterAction(LabelNewsAction.ID);
    action.setData(label1.getId());
    filter.addAction(action);
    item = SyncItem.toSyncItem(filter, currentNews);

    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertTrue(item.isStarred());
    assertFalse(item.isUnStarred());
    assertEquals(1, item.getAddedLabels().size());
    assertEquals("Foo", item.getAddedLabels().get(0));
    assertTrue(item.getRemovedLabels().isEmpty());

    action = fFactory.createFilterAction(LabelNewsAction.ID);
    action.setData(label2.getId());
    filter.addAction(action);
    item = SyncItem.toSyncItem(filter, currentNews);

    assertFalse(item.isMarkedRead());
    assertTrue(item.isMarkedUnread());
    assertTrue(item.isStarred());
    assertFalse(item.isUnStarred());
    assertEquals(2, item.getAddedLabels().size());
    assertEquals("Foo", item.getAddedLabels().get(0));
    assertEquals("Bar", item.getAddedLabels().get(1));
    assertTrue(item.getRemovedLabels().isEmpty());

    action = fFactory.createFilterAction(DeleteNewsAction.ID);
    filter.addAction(action);
    item = SyncItem.toSyncItem(filter, currentNews);
    assertTrue(item.isMarkedRead());
    assertFalse(item.isMarkedUnread());
    assertTrue(item.isStarred());
    assertFalse(item.isUnStarred());
    assertEquals(2, item.getAddedLabels().size());
    assertEquals("Foo", item.getAddedLabels().get(0));
    assertEquals("Bar", item.getAddedLabels().get(1));
    assertTrue(item.getRemovedLabels().isEmpty());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMergeSyncItem() throws Exception {
    IFeed feed = fFactory.createFeed(null, URI.create("rssowl.org"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setInReplyTo(feed.getLink().toString());
    news1.setGuid(fFactory.createGuid(news1, "tag:google.com/foo", true));

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setInReplyTo(feed.getLink().toString());
    news2.setGuid(fFactory.createGuid(news2, "tag:google.com/foo", true));

    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setInReplyTo(feed.getLink().toString());
    news3.setGuid(fFactory.createGuid(news3, "tag:google.com/foo", true));

    SyncItem item1 = SyncItem.toSyncItem(news1);
    SyncItem item2 = SyncItem.toSyncItem(news2);
    SyncItem item3 = SyncItem.toSyncItem(news3);

    item1.merge(item2);

    assertFalse(item1.isMarkedRead());
    assertFalse(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertFalse(item1.isUnStarred());
    assertTrue(item1.getAddedLabels().isEmpty());
    assertTrue(item1.getRemovedLabels().isEmpty());

    item2.setMarkedRead();
    item1.merge(item2);
    assertTrue(item1.isMarkedRead());
    assertFalse(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertFalse(item1.isUnStarred());
    assertTrue(item1.getAddedLabels().isEmpty());
    assertTrue(item1.getRemovedLabels().isEmpty());

    item2.setMarkedUnread();
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertFalse(item1.isUnStarred());
    assertTrue(item1.getAddedLabels().isEmpty());
    assertTrue(item1.getRemovedLabels().isEmpty());

    item2.setStarred();
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertTrue(item1.isStarred());
    assertFalse(item1.isUnStarred());
    assertTrue(item1.getAddedLabels().isEmpty());
    assertTrue(item1.getRemovedLabels().isEmpty());

    item2.setUnStarred();
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertTrue(item1.getAddedLabels().isEmpty());
    assertTrue(item1.getRemovedLabels().isEmpty());

    item2.addLabel("Foo");
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertEquals(1, item1.getAddedLabels().size());
    assertEquals("Foo", item1.getAddedLabels().get(0));
    assertTrue(item1.getRemovedLabels().isEmpty());

    item2.addLabel("Bar");
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertEquals(2, item1.getAddedLabels().size());
    assertEquals("Foo", item1.getAddedLabels().get(0));
    assertEquals("Bar", item1.getAddedLabels().get(1));
    assertTrue(item1.getRemovedLabels().isEmpty());

    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertEquals(2, item1.getAddedLabels().size());
    assertEquals("Foo", item1.getAddedLabels().get(0));
    assertEquals("Bar", item1.getAddedLabels().get(1));
    assertTrue(item1.getRemovedLabels().isEmpty());

    item2.removeLabel("hello");
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertEquals(2, item1.getAddedLabels().size());
    assertEquals("Foo", item1.getAddedLabels().get(0));
    assertEquals("Bar", item1.getAddedLabels().get(1));
    assertEquals(1, item1.getRemovedLabels().size());
    assertEquals("hello", item1.getRemovedLabels().get(0));

    item2.removeLabel("world");
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertEquals(2, item1.getAddedLabels().size());
    assertEquals("Foo", item1.getAddedLabels().get(0));
    assertEquals("Bar", item1.getAddedLabels().get(1));
    assertEquals(2, item1.getRemovedLabels().size());
    assertEquals("hello", item1.getRemovedLabels().get(0));
    assertEquals("world", item1.getRemovedLabels().get(1));

    item2.addLabel("world");
    item1.merge(item2);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertEquals(3, item1.getAddedLabels().size());
    assertEquals("Foo", item1.getAddedLabels().get(0));
    assertEquals("Bar", item1.getAddedLabels().get(1));
    assertEquals("world", item1.getAddedLabels().get(2));
    assertEquals(1, item1.getRemovedLabels().size());
    assertEquals("hello", item1.getRemovedLabels().get(0));

    item1.merge(item3);
    assertFalse(item1.isMarkedRead());
    assertTrue(item1.isMarkedUnread());
    assertFalse(item1.isStarred());
    assertTrue(item1.isUnStarred());
    assertEquals(3, item1.getAddedLabels().size());
    assertEquals("Foo", item1.getAddedLabels().get(0));
    assertEquals("Bar", item1.getAddedLabels().get(1));
    assertEquals("world", item1.getAddedLabels().get(2));
    assertEquals(1, item1.getRemovedLabels().size());
    assertEquals("hello", item1.getRemovedLabels().get(0));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testApplySyncItem() throws Exception {
    IFeed feed = fFactory.createFeed(null, URI.create("rssowl.org"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setInReplyTo(feed.getLink().toString());
    news1.setGuid(fFactory.createGuid(news1, "tag:google.com/foo", true));

    SyncItem sync = SyncItem.toSyncItem(news1);
    sync.applyTo(news1);

    assertTrue(news1.getState() == INews.State.NEW);
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_READ));
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD));
    assertFalse(news1.isFlagged());
    assertTrue(news1.getLabels().isEmpty());

    sync.setMarkedRead();
    sync.applyTo(news1);

    assertTrue(news1.getState() == INews.State.NEW);
    assertNotNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_READ));
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD));
    assertFalse(news1.isFlagged());
    assertTrue(news1.getLabels().isEmpty());

    sync.setMarkedUnread();
    sync.applyTo(news1);

    assertTrue(news1.getState() == INews.State.NEW);
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_READ));
    assertNotNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD));
    assertFalse(news1.isFlagged());
    assertTrue(news1.getLabels().isEmpty());

    sync.setStarred();
    sync.applyTo(news1);

    assertTrue(news1.getState() == INews.State.NEW);
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_READ));
    assertNotNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD));
    assertTrue(news1.isFlagged());
    assertTrue(news1.getLabels().isEmpty());

    sync.setUnStarred();
    sync.applyTo(news1);

    assertTrue(news1.getState() == INews.State.NEW);
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_READ));
    assertNotNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD));
    assertFalse(news1.isFlagged());
    assertTrue(news1.getLabels().isEmpty());

    sync.addLabel("Foo");
    sync.addLabel("Hello World");
    sync.removeLabel("Bar");
    sync.applyTo(news1);

    assertTrue(news1.getState() == INews.State.NEW);
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_READ));
    assertNotNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD));
    assertFalse(news1.isFlagged());
    assertTrue(news1.getLabels().isEmpty());

    Object labelsObj = news1.getProperty(SyncUtils.GOOGLE_LABELS);
    assertNotNull(labelsObj);
    assertTrue(labelsObj instanceof String[]);

    String[] labels = (String[]) labelsObj;
    assertEquals(2, labels.length);
    assertTrue(labels[0].equals("Foo") || labels[1].equals("Foo"));
    assertTrue(labels[0].equals("Hello World") || labels[1].equals("Hello World"));

    sync.removeLabel("Foo");
    sync.removeLabel("Hello World");
    sync.addLabel("Bar");
    sync.applyTo(news1);

    assertTrue(news1.getState() == INews.State.NEW);
    assertNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_READ));
    assertNotNull(news1.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD));
    assertFalse(news1.isFlagged());
    assertTrue(news1.getLabels().isEmpty());

    labelsObj = news1.getProperty(SyncUtils.GOOGLE_LABELS);
    assertNotNull(labelsObj);
    assertTrue(labelsObj instanceof String[]);

    labels = (String[]) labelsObj;
    assertEquals(1, labels.length);
    assertTrue(labels[0].equals("Bar"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsSyncItemEquivalent() throws Exception {
    IFeed feed = fFactory.createFeed(null, URI.create("rssowl.org"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setInReplyTo(feed.getLink().toString());
    news1.setGuid(fFactory.createGuid(news1, "tag:google.com/foo", true));

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setInReplyTo(feed.getLink().toString());
    news2.setGuid(fFactory.createGuid(news2, "tag:google.com/foo", true));

    SyncItem item1 = SyncItem.toSyncItem(news1);
    SyncItem item2 = SyncItem.toSyncItem(news2);

    assertTrue(item1.isEquivalent(item2));

    item2.setMarkedRead();
    assertFalse(item1.isEquivalent(item2));
    item1.setMarkedRead();
    assertTrue(item1.isEquivalent(item2));

    item2.setMarkedUnread();
    assertFalse(item1.isEquivalent(item2));
    item1.setMarkedUnread();
    assertTrue(item1.isEquivalent(item2));

    item2.setStarred();
    assertFalse(item1.isEquivalent(item2));
    item1.setStarred();
    assertTrue(item1.isEquivalent(item2));

    item2.setUnStarred();
    assertFalse(item1.isEquivalent(item2));
    item1.setUnStarred();
    assertTrue(item1.isEquivalent(item2));

    item2.addLabel("Foo");
    assertFalse(item1.isEquivalent(item2));
    item1.addLabel("Foo");
    assertTrue(item1.isEquivalent(item2));

    item2.addLabel("Bar");
    assertFalse(item1.isEquivalent(item2));
    item1.addLabel("Bar");
    assertTrue(item1.isEquivalent(item2));

    item2.removeLabel("Hello");
    assertFalse(item1.isEquivalent(item2));
    item1.removeLabel("Hello");
    assertTrue(item1.isEquivalent(item2));

    item2.removeLabel("World");
    assertFalse(item1.isEquivalent(item2));
    item1.removeLabel("World");
    assertTrue(item1.isEquivalent(item2));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSyncItemsManager() throws Exception {
    SyncItemsManager manager = new SyncItemsManager();
    manager.startup();

    assertTrue(manager.getUncommittedItems().isEmpty());

    IFeed feed = fFactory.createFeed(null, URI.create("rssowl.org"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setInReplyTo(feed.getLink().toString());
    news1.setGuid(fFactory.createGuid(news1, "tag:google.com/foo", true));

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setInReplyTo(feed.getLink().toString());
    news2.setGuid(fFactory.createGuid(news2, "tag:google.com/bar", true));

    SyncItem item1 = SyncItem.toSyncItem(news1);
    item1.setMarkedRead();

    SyncItem item2 = SyncItem.toSyncItem(news2);
    item2.addLabel("Foo");
    item2.addLabel("Bar");
    item2.removeLabel("Hello World");

    manager.addUncommitted(Arrays.asList(item1, item2));

    assertTrue(manager.hasUncommittedItems());

    manager.shutdown();
    assertFalse(manager.hasUncommittedItems());
    manager.startup();
    assertTrue(manager.hasUncommittedItems());

    Collection<SyncItem> uncommittedItems = manager.getUncommittedItems().values();
    assertEquals(2, uncommittedItems.size());

    Iterator<SyncItem> iterator = uncommittedItems.iterator();
    SyncItem loadedItem1 = iterator.next();
    SyncItem loadedItem2 = iterator.next();

    assertTrue(item1.isEquivalent(loadedItem1));
    assertTrue(item2.isEquivalent(loadedItem2));

    SyncItem item3 = SyncItem.toSyncItem(news1);
    item3.setMarkedUnread();
    item3.setStarred();

    manager.addUncommitted(Collections.singleton(item3));

    uncommittedItems = manager.getUncommittedItems().values();
    assertEquals(2, uncommittedItems.size());

    iterator = uncommittedItems.iterator();
    loadedItem1 = iterator.next();

    assertFalse(loadedItem1.isMarkedRead());
    assertTrue(loadedItem1.isMarkedUnread());
    assertTrue(loadedItem1.isStarred());
  }
}
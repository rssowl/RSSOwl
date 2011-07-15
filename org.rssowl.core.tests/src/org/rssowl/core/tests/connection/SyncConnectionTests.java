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

package org.rssowl.core.tests.connection;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.HttpConnectionInputStream;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.IConnectionService;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IGuid;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.SyncItem;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.Triple;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.services.SyncItemsManager;
import org.rssowl.ui.internal.services.SyncService;
import org.rssowl.ui.internal.services.SyncService.SyncStatus;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This TestCase covers use-cases for the Connection Plugin.
 *
 * @author bpasero
 */
public class SyncConnectionTests {

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
  @SuppressWarnings("nls")
  public void testGetLabel() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("reader://www.rssowl.org/node/feed");
    String label = conManager.getLabel(feedUrl, new NullProgressMonitor());
    assertEquals("RSSOwl News", label);
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testGetFavicon() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("reader://www.rssowl.org/node/feed");
    byte[] feedIcon = conManager.getFeedIcon(feedUrl, new NullProgressMonitor());
    assertNotNull(feedIcon);
    assertTrue(feedIcon.length != 0);
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testLoadFeedFromWebsite() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("reader://www.heise.de");

    assertEquals("http://www.heise.de/newsticker/heise-atom.xml", conManager.getFeed(feedUrl, new NullProgressMonitor()).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testReaderRecommendedNews() throws Exception {
    URI feedUrl = new URI(SyncUtils.GOOGLE_READER_RECOMMENDED_ITEMS_FEED);

    Triple<IFeed, IConditionalGet, URI> result = Owl.getConnectionService().reload(feedUrl, null, null);
    assertNotNull(result);

    IFeed feed = result.getFirst();

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testReaderHttpsFeed() throws Exception {
    URI feedUrl = new URI("https://sourceforge.net/export/rss2_projnews.php?group_id=141424&rss_fulltext=1");

    Triple<IFeed, IConditionalGet, URI> result = Owl.getConnectionService().reload(feedUrl, null, null);
    assertNotNull(result);

    IFeed feed = result.getFirst();

    assertNotNull(feed.getTitle());
    assertFalse(feed.getNews().isEmpty());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testReaderSharedNews() throws Exception {
    URI feedUrl = new URI(SyncUtils.GOOGLE_READER_SHARED_ITEMS_FEED);

    Triple<IFeed, IConditionalGet, URI> result = Owl.getConnectionService().reload(feedUrl, null, null);
    assertNotNull(result);

    IFeed feed = result.getFirst();

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertEquals(1, feed.getNews().size());
    assertEquals("Hello World", feed.getNews().get(0).getDescription());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testReaderNotes() throws Exception {
    URI feedUrl = new URI(SyncUtils.GOOGLE_READER_NOTES_FEED);

    Triple<IFeed, IConditionalGet, URI> result = Owl.getConnectionService().reload(feedUrl, null, null);
    assertNotNull(result);

    IFeed feed = result.getFirst();

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertEquals(1, feed.getNews().size());
    assertEquals("Hello World", feed.getNews().get(0).getDescription());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGzipConnectionInputStream() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI url = new URI("http://www.google.com/reader/api/0/stream/contents/feed/http%3A%2F%2Frss.golem.de%2Frss.php%3Ffeed%3DRSS1.0?r=n&n=20&ck=" + System.currentTimeMillis() + "&client=scroll");
    IProtocolHandler handler = conManager.getHandler(url);

    Map<String, String> headers = new HashMap<String, String>();
    String token = SyncUtils.getGoogleAuthToken("rssowl@mailinator.com", "rssowl.org", true, new NullProgressMonitor());
    headers.put("Authorization", SyncUtils.getGoogleAuthorizationHeader(token));
    headers.put("Accept-Charset", "utf-8");
    headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");

    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.HEADERS, headers);

    InputStream stream = handler.openStream(url, null, properties);

    assertEquals("gzip", ((HttpConnectionInputStream) stream).getContentEncoding());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSpiegelFeedMarkReadAndUnread() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    URI feedUrl = new URI("reader://www.spiegel.de/schlagzeilen/index.rss");
    IFeed feed = Owl.getModelFactory().createFeed(null, feedUrl);
    DynamicDAO.save(feed);

    IBookMark bm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(feedUrl), "BM");
    DynamicDAO.save(root);

    /* Initial Load of Feed */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    List<INews> newNews = new ArrayList<INews>();
    for (INews news : feed.getNews()) {
      if (news.getState() == INews.State.NEW)
        newNews.add(news);
    }

    if (newNews.isEmpty())
      return; //Only works if some new news are present

    /* Test Outgoing Sync (mark read) */
    SyncService service = new SyncService();
    assertTrue(service.getStatus() == null);

    List<SyncItem> syncItems = new ArrayList<SyncItem>();
    for (INews news : newNews) {
      SyncItem item = SyncItem.toSyncItem(news);
      item.setMarkedRead();
      syncItems.add(item);
    }

    service.testSync(syncItems);

    /* Assert Status */
    assertNotNull(service.getStatus());
    SyncStatus status = service.getStatus();
    assertTrue(status.isOK());
    assertEquals(newNews.size(), status.getItemCount());
    assertEquals(newNews.size(), status.getTotalItemCount());

    /* Simulate incoming sync (merge read state) */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    /* Assert Read State */
    for (INews news : feed.getNews()) {
      if (news.getState() == INews.State.NEW)
        fail("Unexpected state");
    }

    /* Test Mark Unread */
    syncItems = new ArrayList<SyncItem>();
    for (INews news : newNews) {
      SyncItem item = SyncItem.toSyncItem(news);
      item.setMarkedUnread();
      syncItems.add(item);
    }

    service.testSync(syncItems);

    /* Assert Status */
    assertNotNull(service.getStatus());
    status = service.getStatus();
    assertTrue(status.isOK());
    assertEquals(newNews.size(), status.getItemCount());
    assertEquals(newNews.size() * 2, status.getTotalItemCount());

    /* Simulate incoming sync (merge read state) */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    /* Assert Read State */
    List<INews> unreadNews = new ArrayList<INews>();
    for (INews news : feed.getNews()) {
      if (news.getState() == INews.State.UNREAD)
        unreadNews.add(news);
    }

    assertTrue(unreadNews.size() == newNews.size());

    /* Test Outgoing Sync (mark read) */
    syncItems = new ArrayList<SyncItem>();
    for (INews news : unreadNews) {
      SyncItem item = SyncItem.toSyncItem(news);
      item.setMarkedRead();
      syncItems.add(item);
    }

    service.testSync(syncItems);

    /* Assert Status */
    assertNotNull(service.getStatus());
    status = service.getStatus();
    assertTrue(status.isOK());
    assertEquals(unreadNews.size(), status.getItemCount());
    assertEquals(unreadNews.size() * 3, status.getTotalItemCount());

    /* Simulate incoming sync (merge read state) */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    /* Assert Read State */
    for (INews news : feed.getNews()) {
      if (news.getState() == INews.State.NEW || news.getState() == INews.State.UNREAD)
        fail("Unexpected state");
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSpiegelFeedStarLabels() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    URI feedUrl = new URI("reader://www.spiegel.de/schlagzeilen/index.rss");
    IFeed feed = Owl.getModelFactory().createFeed(null, feedUrl);
    DynamicDAO.save(feed);

    IBookMark bm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(feedUrl), "BM");
    DynamicDAO.save(root);

    /* Initial Load of Feed */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    /* Test Outgoing Sync */
    SyncService service = new SyncService();
    assertTrue(service.getStatus() == null);

    List<INews> newsToSync = new ArrayList<INews>();
    List<SyncItem> syncItems = new ArrayList<SyncItem>();
    Set<String> guids = new HashSet<String>();
    for (int i = 0; i < 20; i++) {
      INews newsitem = feed.getNews().get(i);
      SyncItem item = SyncItem.toSyncItem(newsitem);
      newsToSync.add(newsitem);
      item.setStarred();
      item.addLabel("Testing");
      item.addLabel("Hello World");
      syncItems.add(item);
      guids.add(newsitem.getGuid().getValue());
    }

    service.testSync(syncItems);

    /* Assert Status */
    assertNotNull(service.getStatus());
    SyncStatus status = service.getStatus();
    assertTrue(status.isOK());
    assertEquals(syncItems.size(), status.getItemCount());
    assertEquals(syncItems.size(), status.getTotalItemCount());

    /* Simulate incoming sync (merge starred state and labels) */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    /* Assert Starred State and Labels */
    for (INews news : feed.getNews()) {
      if (!guids.contains(news.getGuid().getValue()))
        continue;

      assertTrue(news.isFlagged());
      Set<ILabel> labels = news.getLabels();
      assertTrue(labels.size() >= 2);
      int count = 0;
      for (ILabel label : labels) {
        if ("Testing".equals(label.getName()) && label.getId() > 0)
          count++;
        else if ("Hello World".equals(label.getName()) && label.getId() > 0)
          count++;
      }

      assertEquals(2, count);
    }

    /* Test removing starred state and labels */
    syncItems = new ArrayList<SyncItem>();
    for (INews news : newsToSync) {
      SyncItem item = SyncItem.toSyncItem(news);
      item.setUnStarred();
      item.removeLabel("Testing");
      item.removeLabel("Hello World");
      syncItems.add(item);
    }

    service.testSync(syncItems);

    /* Assert Status */
    assertNotNull(service.getStatus());
    status = service.getStatus();
    assertTrue(status.isOK());
    assertEquals(syncItems.size(), status.getItemCount());
    assertEquals(syncItems.size() * 2, status.getTotalItemCount());

    /* Simulate incoming sync (merge starred state and labels) */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    /* Assert Starred State and Labels */
    for (INews news : feed.getNews()) {
      if (!guids.contains(news.getGuid().getValue()))
        continue;

      assertFalse(news.isFlagged());
      Set<ILabel> labels = news.getLabels();
      for (ILabel label : labels) {
        if ("Testing".equals(label.getName()))
          fail("Unexpected Label found");
        else if ("Hello World".equals(label.getName()))
          fail("Unexpected Label found");
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testSpiegelFeedIncomingExistingLabel() throws Exception {
    ILabel label = Owl.getModelFactory().createLabel(null, "Super Label");
    DynamicDAO.save(label);

    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    URI feedUrl = new URI("reader://www.spiegel.de/schlagzeilen/index.rss");
    IFeed feed = Owl.getModelFactory().createFeed(null, feedUrl);
    DynamicDAO.save(feed);

    IBookMark bm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(feedUrl), "BM");
    DynamicDAO.save(root);

    /* Initial Load of Feed */
    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    /* Test Outgoing Sync */
    SyncService service = new SyncService();
    assertTrue(service.getStatus() == null);

    IGuid marker = feed.getNews().get(0).getGuid();
    SyncItem item = SyncItem.toSyncItem(feed.getNews().get(0));
    item.addLabel(label.getName());

    service.testSync(Collections.singleton(item));

    assertNotNull(service.getStatus());
    SyncStatus status = service.getStatus();
    assertEquals(1, status.getItemCount());
    assertEquals(1, status.getTotalItemCount());

    Controller.getDefault().reload(bm, null, null);

    assertNotNull(feed.getTitle());
    assertNotNull(feed.getPublishDate());
    assertFalse(feed.getNews().isEmpty());

    boolean labelFound = false;
    Outer: for (INews news : feed.getNews()) {
      if (news.getGuid().getValue().equals(marker.getValue())) {
        Set<ILabel> labels = news.getLabels();
        for (ILabel newsLabel : labels) {
          if (newsLabel.getName().equals(label.getName())) {
            assertTrue(newsLabel.equals(label));
            labelFound = true;
            break Outer;
          }
        }
      }
    }

    assertTrue(labelFound);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMultiFeedSync() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    IFeed sternFeed = Owl.getModelFactory().createFeed(null, new URI("reader://www.stern.de/feed/standard/all/"));
    DynamicDAO.save(sternFeed);

    IBookMark sternBm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(sternFeed.getLink()), "Stern");

    IFeed heiseFeed = Owl.getModelFactory().createFeed(null, new URI("reader://www.heise.de/newsticker/heise-atom.xml"));
    DynamicDAO.save(heiseFeed);

    IBookMark heiseBm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(heiseFeed.getLink()), "Heise");

    IFeed golemFeed = Owl.getModelFactory().createFeed(null, new URI("reader://rss.golem.de/rss.php?feed=RSS1.0"));
    DynamicDAO.save(golemFeed);

    IBookMark golemBm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(golemFeed.getLink()), "Golem");

    DynamicDAO.save(root);

    Controller.getDefault().reload(sternBm, null, null);
    Controller.getDefault().reload(heiseBm, null, null);
    Controller.getDefault().reload(golemBm, null, null);

    SyncService service = new SyncService();
    assertTrue(service.getStatus() == null);

    List<SyncItem> items = new ArrayList<SyncItem>();

    INews sternNews1 = sternFeed.getNews().get(0);
    INews sternNews2 = sternFeed.getNews().get(1);
    INews heiseNews1 = heiseFeed.getNews().get(0);
    INews heiseNews2 = heiseFeed.getNews().get(1);
    INews golemNews1 = golemFeed.getNews().get(0);
    INews golemNews2 = golemFeed.getNews().get(1);

    SyncItem item = SyncItem.toSyncItem(sternNews1);
    item.setStarred();
    items.add(item);

    item = SyncItem.toSyncItem(heiseNews1);
    item.setStarred();
    items.add(item);

    item = SyncItem.toSyncItem(golemNews1);
    item.setStarred();
    items.add(item);

    item = SyncItem.toSyncItem(sternNews2);
    item.addLabel("Foo Bar");
    items.add(item);

    item = SyncItem.toSyncItem(heiseNews2);
    item.addLabel("Foo Bar");
    items.add(item);

    item = SyncItem.toSyncItem(golemNews2);
    item.addLabel("Foo Bar");
    item.addLabel("Hello World");
    items.add(item);

    service.testSync(items);

    assertNotNull(service.getStatus());
    SyncStatus status = service.getStatus();
    assertEquals(6, status.getItemCount());
    assertEquals(6, status.getTotalItemCount());

    Controller.getDefault().reload(sternBm, null, null);
    Controller.getDefault().reload(heiseBm, null, null);
    Controller.getDefault().reload(golemBm, null, null);

    int counter = 0;
    for (INews news : sternFeed.getNews()) {
      if (news.getGuid().getValue().equals(sternNews1.getGuid().getValue())) {
        if (news.isFlagged())
          counter++;
      } else if (news.getGuid().getValue().equals(sternNews2.getGuid().getValue())) {
        if (news.isFlagged())
          counter++;

        Set<ILabel> labels = news.getLabels();
        for (ILabel label : labels) {
          if (label.getName().equals("Foo Bar"))
            counter++;
        }
      }
    }

    for (INews news : heiseFeed.getNews()) {
      if (news.getGuid().getValue().equals(heiseNews1.getGuid().getValue())) {
        if (news.isFlagged())
          counter++;
      } else if (news.getGuid().getValue().equals(heiseNews2.getGuid().getValue())) {
        if (news.isFlagged())
          counter++;

        Set<ILabel> labels = news.getLabels();
        for (ILabel label : labels) {
          if (label.getName().equals("Foo Bar"))
            counter++;
        }
      }
    }

    for (INews news : golemFeed.getNews()) {
      if (news.getGuid().getValue().equals(golemNews1.getGuid().getValue())) {
        if (news.isFlagged())
          counter++;
      } else if (news.getGuid().getValue().equals(golemNews2.getGuid().getValue())) {
        if (news.isFlagged())
          counter++;

        Set<ILabel> labels = news.getLabels();
        for (ILabel label : labels) {
          if (label.getName().equals("Foo Bar"))
            counter++;
          if (label.getName().equals("Hello World"))
            counter++;
        }
      }
    }

    assertEquals(7, counter);

    /* Now remove again and check */
    item = SyncItem.toSyncItem(sternNews1);
    item.setUnStarred();
    items.add(item);

    item = SyncItem.toSyncItem(heiseNews1);
    item.setUnStarred();
    items.add(item);

    item = SyncItem.toSyncItem(golemNews1);
    item.setUnStarred();
    items.add(item);

    item = SyncItem.toSyncItem(sternNews2);
    item.removeLabel("Foo Bar");
    items.add(item);

    item = SyncItem.toSyncItem(heiseNews2);
    item.removeLabel("Foo Bar");
    items.add(item);

    item = SyncItem.toSyncItem(golemNews2);
    item.removeLabel("Foo Bar");
    item.removeLabel("Hello World");
    items.add(item);

    service.testSync(items);

    assertNotNull(service.getStatus());
    status = service.getStatus();
    assertEquals(6, status.getItemCount());
    assertEquals(12, status.getTotalItemCount());

    Controller.getDefault().reload(sternBm, null, null);
    Controller.getDefault().reload(heiseBm, null, null);
    Controller.getDefault().reload(golemBm, null, null);

    for (INews news : sternFeed.getNews()) {
      if (news.getGuid().getValue().equals(sternNews1.getGuid().getValue())) {
        if (news.isFlagged())
          fail("Unexpected state");
      } else if (news.getGuid().getValue().equals(sternNews2.getGuid().getValue())) {
        if (news.isFlagged()) {
          Set<ILabel> labels = news.getLabels();
          for (ILabel label : labels) {
            if (label.getName().equals("Foo Bar"))
              fail("Unexpected state");
          }
        }
      }
    }

    for (INews news : heiseFeed.getNews()) {
      if (news.getGuid().getValue().equals(heiseNews1.getGuid().getValue())) {
        if (news.isFlagged())
          fail("Unexpected state");
      } else if (news.getGuid().getValue().equals(heiseNews2.getGuid().getValue())) {
        if (news.isFlagged()) {
          Set<ILabel> labels = news.getLabels();
          for (ILabel label : labels) {
            if (label.getName().equals("Foo Bar"))
              fail("Unexpected state");
          }
        }
      }
    }

    for (INews news : golemFeed.getNews()) {
      if (news.getGuid().getValue().equals(golemNews1.getGuid().getValue())) {
        if (news.isFlagged())
          fail("Unexpected state");
      } else if (news.getGuid().getValue().equals(golemNews2.getGuid().getValue())) {
        if (news.isFlagged()) {
          Set<ILabel> labels = news.getLabels();
          for (ILabel label : labels) {
            if (label.getName().equals("Foo Bar"))
              fail("Unexpected state");
            if (label.getName().equals("Hello World"))
              fail("Unexpected state");
          }
        }
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFeedSyncNoUpdateEvents() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    IFeed bildFeed = Owl.getModelFactory().createFeed(null, new URI("reader://www.bild.de/rss-feeds/rss-16725492,feed=home.bild.html"));
    DynamicDAO.save(bildFeed);

    IBookMark bildBm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(bildFeed.getLink()), "Bild");

    DynamicDAO.save(root);

    Controller.getDefault().reload(bildBm, null, null);

    SyncService service = new SyncService();
    assertTrue(service.getStatus() == null);

    INews bildNews = bildFeed.getNews().get(0);
    SyncItem item = SyncItem.toSyncItem(bildNews);
    item.setStarred();
    item.addLabel("Foo");
    item.addLabel("Bar");
    item.addLabel("Hello World");
    item.addLabel("World Hello");

    service.testSync(Collections.singleton(item));

    assertNotNull(service.getStatus());
    SyncStatus status = service.getStatus();
    assertEquals(1, status.getItemCount());
    assertEquals(1, status.getTotalItemCount());

    Controller.getDefault().reload(bildBm, null, null);

    final AtomicBoolean listenerCalled = new AtomicBoolean();
    NewsListener listener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        listenerCalled.set(true);
      }
    };
    DynamicDAO.addEntityListener(INews.class, listener);

    try {
      Controller.getDefault().reload(bildBm, null, null);
      assertFalse(listenerCalled.get());
    } finally {
      DynamicDAO.removeEntityListener(INews.class, listener);
    }

    /* Clean Up */
    item = SyncItem.toSyncItem(bildNews);
    item.setUnStarred();
    item.removeLabel("Foo");
    item.removeLabel("Bar");
    item.removeLabel("Hello World");
    item.removeLabel("World Hello");

    service.testSync(Collections.singleton(item));

    assertNotNull(service.getStatus());
    status = service.getStatus();
    assertEquals(1, status.getItemCount());
    assertEquals(2, status.getTotalItemCount());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFeedSyncLabelMerge() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    IFeed testFeed = Owl.getModelFactory().createFeed(null, new URI("reader://www.test.de/rss/themen/alle/alles/"));
    DynamicDAO.save(testFeed);

    IBookMark testBm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(testFeed.getLink()), "Test");

    DynamicDAO.save(root);

    Controller.getDefault().reload(testBm, null, null);

    SyncService service = new SyncService();
    assertTrue(service.getStatus() == null);

    INews testNews = testFeed.getNews().get(0);

    assertEquals(1, testNews.getLabels().size());
    assertEquals("TestFeed", testNews.getLabels().iterator().next().getName());

    SyncItem item = SyncItem.toSyncItem(testNews);
    item.addLabel("Foo");
    item.addLabel("Bar");
    item.addLabel("Hello World");
    item.addLabel("World Hello");

    service.testSync(Collections.singleton(item));

    assertNotNull(service.getStatus());
    SyncStatus status = service.getStatus();
    assertEquals(1, status.getItemCount());
    assertEquals(1, status.getTotalItemCount());

    Controller.getDefault().reload(testBm, null, null);

    assertEquals(5, testNews.getLabels().size());

    testNews.removeLabel(testNews.getLabels().iterator().next());
    DynamicDAO.save(testFeed);

    assertEquals(4, testNews.getLabels().size());

    Controller.getDefault().reload(testBm, null, null);

    assertEquals(5, testNews.getLabels().size());

    item = SyncItem.toSyncItem(testNews);
    item.removeLabel("Foo");
    item.removeLabel("Bar");
    item.addLabel("Bababu");

    service.testSync(Collections.singleton(item));

    Controller.getDefault().reload(testBm, null, null);
    assertEquals(4, testNews.getLabels().size());

    item = SyncItem.toSyncItem(testNews);
    item.removeLabel("Hello World");
    item.removeLabel("World Hello");
    item.removeLabel("Bababu");

    service.testSync(Collections.singleton(item));

    Controller.getDefault().reload(testBm, null, null);
    assertEquals(1, testNews.getLabels().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSyncHugeFeed() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    IFeed slashdotFeed = Owl.getModelFactory().createFeed(null, new URI("reader://rss.slashdot.org/Slashdot/slashdot"));
    DynamicDAO.save(slashdotFeed);

    IBookMark slashdotBm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(slashdotFeed.getLink()), "Slashdot");
    IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(slashdotBm);
    prefs.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 800);

    DynamicDAO.save(root);

    Controller.getDefault().reload(slashdotBm, null, null);

    SyncService service = new SyncService();
    assertTrue(service.getStatus() == null);

    int itemCount = slashdotFeed.getNews().size();
    assertTrue(itemCount > 790); //Sometimes seems to hit 799

    List<SyncItem> items = new ArrayList<SyncItem>();
    for (INews news : slashdotFeed.getNews()) {
      SyncItem item = SyncItem.toSyncItem(news);
      item.setStarred();
      item.setMarkedRead();
      items.add(item);

      item = SyncItem.toSyncItem(news);
      item.addLabel("Foo");
      items.add(item);

      item = SyncItem.toSyncItem(news);
      item.addLabel("Hello World");
      items.add(item);
    }

    service.testSync(items);
    assertEquals(itemCount, service.getStatus().getItemCount());
    assertEquals(itemCount, service.getStatus().getTotalItemCount());

    items = new ArrayList<SyncItem>();
    for (INews news : slashdotFeed.getNews()) {
      SyncItem item = SyncItem.toSyncItem(news);
      item.setUnStarred();
      items.add(item);

      item = SyncItem.toSyncItem(news);
      item.removeLabel("Foo");
      items.add(item);

      item = SyncItem.toSyncItem(news);
      item.removeLabel("Hello World");
      items.add(item);
    }

    service.testSync(items);
    assertEquals(itemCount, service.getStatus().getItemCount());
    assertEquals(itemCount * 2, service.getStatus().getTotalItemCount());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSyncRecentNewsFromFeed() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    IFeed slashdotFeed = Owl.getModelFactory().createFeed(null, new URI("reader://rss.slashdot.org/Slashdot/slashdot"));
    DynamicDAO.save(slashdotFeed);

    IBookMark slashdotBm = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(slashdotFeed.getLink()), "Slashdot");
    IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(slashdotBm);
    prefs.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 2);

    long ageLimit = DateUtils.getToday().getTimeInMillis() - (2 * DateUtils.DAY);
    ageLimit -= 1000 * 60 * 60; //Tolerate some invariance

    DynamicDAO.save(root);

    Controller.getDefault().reload(slashdotBm, null, null);

    for (INews news : slashdotFeed.getNews()) {
      Date date = news.getPublishDate();
      assertTrue(ageLimit <= date.getTime());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIncomingMergeConflict() throws Exception {
    URI slashdot = new URI("reader://rss.slashdot.org/Slashdot/slashdot");
    IConnectionService conManager = Owl.getConnectionService();
    IProtocolHandler handler = conManager.getHandler(slashdot);

    Map<Object, Object> properties = new HashMap<Object, Object>();
    Map<String, SyncItem> syncitems = new HashMap<String, SyncItem>();
    properties.put(IConnectionPropertyConstants.UNCOMMITTED_ITEMS, syncitems);

    Triple<IFeed, IConditionalGet, URI> result = handler.reload(slashdot, new NullProgressMonitor(), null);

    List<INews> news = result.getFirst().getNews();
    for (int i = 0; i < 10; i++) {
      INews item = news.get(i);
      SyncItem sync = SyncItem.toSyncItem(item);
      syncitems.put(sync.getId(), sync);
      sync.setMarkedRead();
      sync.setStarred();
      sync.addLabel("Foo");
    }

    result = handler.reload(slashdot, new NullProgressMonitor(), properties);
    news = result.getFirst().getNews();
    for (INews item : news) {
      if (syncitems.containsKey(item.getGuid().getValue())) {
        assertTrue(item.getProperty(SyncUtils.GOOGLE_MARKED_READ) != null);
        assertTrue(item.isFlagged());

        Object labelsObj = item.getProperty(SyncUtils.GOOGLE_LABELS);
        assertNotNull(labelsObj);

        String[] labels = (String[]) labelsObj;
        boolean labelFound = false;
        for (String label : labels) {
          if ("Foo".equals(label)) {
            labelFound = true;
            break;
          }
        }

        assertTrue(labelFound);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSyncWithDeletedLabels() throws Exception {
    IFolder root = Owl.getModelFactory().createFolder(null, null, "Root");

    IFeed bbcFeed = Owl.getModelFactory().createFeed(null, new URI("reader://feeds.bbci.co.uk/news/rss.xml"));
    DynamicDAO.save(bbcFeed);

    IBookMark bbcBM = Owl.getModelFactory().createBookMark(null, root, new FeedLinkReference(bbcFeed.getLink()), "Slashdot");

    DynamicDAO.save(root);

    Controller.getDefault().reload(bbcBM, null, null);

    Collection<ILabel> labels = DynamicDAO.loadAll(ILabel.class);
    boolean bbcLabelFound = false;
    ILabel bbcLabel = null;
    for (ILabel label : labels) {
      if ("BBC".equals(label.getName())) {
        bbcLabelFound = true;
        bbcLabel = label;
        break;
      }
    }

    assertTrue(bbcLabelFound);

    DynamicDAO.delete(bbcLabel);

    for (INews news : bbcFeed.getNews()) {
      assertTrue(news.getLabels().isEmpty());
    }

    Controller.getDefault().reload(bbcBM, null, null);

    labels = DynamicDAO.loadAll(ILabel.class);
    bbcLabelFound = false;
    for (ILabel label : labels) {
      if ("BBC".equals(label.getName())) {
        bbcLabelFound = true;
        break;
      }
    }

    assertFalse(bbcLabelFound);

    for (INews news : bbcFeed.getNews()) {
      assertTrue(news.getLabels().isEmpty());
    }

    bbcLabel = Owl.getModelFactory().createLabel(null, "BBC");
    DynamicDAO.save(bbcLabel);

    Controller.getDefault().reload(bbcBM, null, null);
    bbcLabelFound = false;
    for (INews news : bbcFeed.getNews()) {
      if (news.getLabels().contains(bbcLabel)) {
        bbcLabelFound = true;
        break;
      }
    }

    assertTrue(bbcLabelFound);

    DynamicDAO.delete(bbcLabel);

    for (INews news : bbcFeed.getNews()) {
      assertTrue(news.getLabels().isEmpty());
    }

    Controller.getDefault().reload(bbcBM, null, null);

    labels = DynamicDAO.loadAll(ILabel.class);
    bbcLabelFound = false;
    for (ILabel label : labels) {
      if ("BBC".equals(label.getName())) {
        bbcLabelFound = true;
        break;
      }
    }

    assertFalse(bbcLabelFound);

    for (INews news : bbcFeed.getNews()) {
      assertTrue(news.getLabels().isEmpty());
    }

    bbcLabel = Owl.getModelFactory().createLabel(null, "BBC Other");
    DynamicDAO.save(bbcLabel);

    bbcLabel.setName("BBC");
    DynamicDAO.save(bbcLabel);

    Controller.getDefault().reload(bbcBM, null, null);
    bbcLabelFound = false;
    for (INews news : bbcFeed.getNews()) {
      if (news.getLabels().contains(bbcLabel)) {
        bbcLabelFound = true;
        break;
      }
    }

    assertTrue(bbcLabelFound);
  }
}
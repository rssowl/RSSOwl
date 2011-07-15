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

package org.rssowl.core.tests.persist;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.EntityIdsByEventType;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.interpreter.InterpreterException;
import org.rssowl.core.interpreter.ParserException;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISource;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.tests.TestUtils.NullProgressLongOperationMonitor;
import org.rssowl.core.tests.model.DBManagerTest;
import org.rssowl.core.tests.model.LargeBlockSizeTest;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.LongOperationMonitor;
import org.rssowl.ui.internal.util.ImportUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * Tests that trigger lifecycle methods run as last tests to not interfer other
 * tests.
 */
public class StartupShutdownTest extends LargeBlockSizeTest {
  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl) Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    System.setProperty("rssowl.reindex", "false"); //Clear any set reindex marker
    DBManager.getDefault().getReIndexFile().delete();
    DBManager.getDefault().getDefragmentFile().delete();
    DBManager.getDefault().getCleanUpIndexFile().delete();
  }

  /**
   * Tests that uncommitted news are saved and loaded correctly in the presence
   * of an emergency or normal shutdown.
   *
   * @throws Exception
   */
  @Test
  public void testSaveLoadEntitiesToBeIndexed() throws Exception {
    EntityIdsByEventType entitiesToBeIndexed = DBHelper.getEntitiesToBeIndexedDAO().load();
    assertEquals(0, entitiesToBeIndexed.size());

    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com/feed.xml"));
    INews news1 = createNews(feed, "Foo", null, "http://www.news.com/news1.html", State.READ);
    INews news2 = createNews(feed, " Bar", null, "http://www.news.com/news2.html", State.NEW);
    DynamicDAO.save(feed);

    entitiesToBeIndexed = DBHelper.getEntitiesToBeIndexedDAO().load();
    assertEquals(2, entitiesToBeIndexed.size());
    Owl.getPersistenceService().shutdown(true);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    entitiesToBeIndexed = DBHelper.getEntitiesToBeIndexedDAO().load();
    assertEquals(2, entitiesToBeIndexed.size());
    for (long id : entitiesToBeIndexed.getPersistedEntityIds().toArray()) {
      assertTrue(id == news1.getId().longValue() || id == news2.getId().longValue());
    }

    Owl.getPersistenceService().shutdown(false);
    Owl.getPersistenceService().startup(new NullProgressLongOperationMonitor(), false, false);

    waitForIndexer();

    entitiesToBeIndexed = DBHelper.getEntitiesToBeIndexedDAO().load();
    assertEquals(0, entitiesToBeIndexed.size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCleanUpOnNextStartup() {
    Owl.getPersistenceService().getModelSearch().cleanUpOnNextStartup();
    assertTrue(DBManager.getDefault().getCleanUpIndexFile().exists());
    DBManager.getDefault().getCleanUpIndexFile().delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testReIndexOnNextStartup() {
    Owl.getPersistenceService().getModelSearch().reIndexOnNextStartup();
    assertTrue(DBManager.getDefault().getReIndexFile().exists());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRecreateEmptyProfile() {
    IFeed feed = DynamicDAO.save(createFeed());
    INews news = createNews(feed);
    news.setState(INews.State.NEW);
    news.setFlagged(true);

    DynamicDAO.save(news);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Root");
    IBookMark bookmark = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");

    DynamicDAO.save(bookmark);

    assertNotNull(feed.toReference().resolve());
    assertNotNull(news.toReference().resolve());
    assertNotNull(folder.toReference().resolve());
    assertNotNull(bookmark.toReference().resolve());

    InternalOwl.getDefault().recreateProfile(false); //Creates a new, empty rssowl.db.restore
    assertTrue(new File(DBManager.getDBRestoreFilePath()).exists());
    InternalOwl.getDefault().startup(new LongOperationMonitor(new NullProgressMonitor()) {}, false, false); //Normal startup will pickup rssowl.db.restore
    assertFalse(new File(DBManager.getDBRestoreFilePath()).exists());

    assertNull(feed.toReference().resolve());
    assertNull(news.toReference().resolve());
    assertNull(folder.toReference().resolve());
    assertNull(bookmark.toReference().resolve());

    feed = DynamicDAO.save(createFeed());
    news = createNews(feed);
    news.setState(INews.State.NEW);
    news.setFlagged(true);

    DynamicDAO.save(news);

    folder = Owl.getModelFactory().createFolder(null, null, "Root");
    bookmark = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");

    DynamicDAO.save(bookmark);

    assertNotNull(feed.toReference().resolve());
    assertNotNull(news.toReference().resolve());
    assertNotNull(folder.toReference().resolve());
    assertNotNull(bookmark.toReference().resolve());
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore
  public void testRecreateOPMLProfile() throws IOException, InterpreterException, ParserException {
    IFeed feed = DynamicDAO.save(createFeed());
    INews news = createNews(feed);
    news.setState(INews.State.NEW);
    news.setFlagged(true);

    DynamicDAO.save(news);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Root");
    IBookMark bookmark = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");

    DynamicDAO.save(bookmark);

    assertNotNull(feed.toReference().resolve());
    assertNotNull(news.toReference().resolve());
    assertNotNull(folder.toReference().resolve());
    assertNotNull(bookmark.toReference().resolve());

    InternalOwl.getDefault().recreateProfile(true);
    assertTrue(new File(DBManager.getDBRestoreFilePath()).exists());

    assertNull(feed.toReference().resolve());
    assertNull(news.toReference().resolve());
    assertNull(folder.toReference().resolve());
    assertNull(bookmark.toReference().resolve());

    File tmpFile = File.createTempFile("rssowlopml", "tmp");
    if (!tmpFile.exists())
      tmpFile.createNewFile();
    tmpFile.deleteOnExit();

    CoreUtils.copy(DBManagerTest.class.getResourceAsStream("/data/default_feeds.xml"), new FileOutputStream(tmpFile));

    List<? extends IEntity> types = InternalOwl.getDefault().getInterpreter().importFrom(new FileInputStream(tmpFile));
    ImportUtils.doImport(null, types, false);

    assertTrue(DynamicDAO.loadAll(INews.class).isEmpty());
    assertTrue(DynamicDAO.loadAll(IBookMark.class).size() > 100);
    assertTrue(DynamicDAO.loadAll(IFolder.class).size() > 20);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRestoreProfile() throws IOException {
    File marker = DBManager.getLargeBlockSizeMarkerFile();
    boolean markerExists = marker.exists();

    IFeed feed = DynamicDAO.save(createFeed());
    INews news = createNews(feed);
    news.setState(INews.State.NEW);
    news.setFlagged(true);

    DynamicDAO.save(news);

    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Root");
    IBookMark bookmark = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark");

    DynamicDAO.save(bookmark);

    assertNotNull(feed.toReference().resolve());
    assertNotNull(news.toReference().resolve());
    assertNotNull(folder.toReference().resolve());
    assertNotNull(bookmark.toReference().resolve());

    File tmpFile = File.createTempFile("rssowldb", "tmp");
    if (!tmpFile.exists())
      tmpFile.createNewFile();
    tmpFile.deleteOnExit();

    CoreUtils.copy(DBManagerTest.class.getResourceAsStream("/data/rssowl.db"), new FileOutputStream(tmpFile));
    InternalOwl.getDefault().restoreProfile(tmpFile);
    assertTrue(new File(DBManager.getDBRestoreFilePath()).exists());
    if (markerExists)
      assertFalse(marker.exists());
    InternalOwl.getDefault().startup(new LongOperationMonitor(new NullProgressMonitor()) {}, true, false);
    assertFalse(new File(DBManager.getDBRestoreFilePath()).exists());

    assertTrue(DynamicDAO.loadAll(INews.class).isEmpty());
    assertTrue(DynamicDAO.loadAll(IBookMark.class).size() > 100);
    assertTrue(DynamicDAO.loadAll(IFolder.class).size() > 20);

    if (markerExists && !marker.exists())
      marker.createNewFile();
  }

  private INews createNews(IFeed feed, String title, String description, String link, INews.State state) throws URISyntaxException {
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

  private INews createNews(IFeed feed) {
    INews news = fFactory.createNews(null, feed, createDate());
    IAttachment attachment = fFactory.createAttachment(null, news);
    attachment.setLink(createURI("http://attachmenturi.com"));
    ICategory category = fFactory.createCategory(null, news);
    category.setName("Category name #1");
    news.setAuthor(createPersonMary(news));
    news.setBase(createURI("http://www.someuri.com"));
    news.setComments("One comment");
    news.setState(State.HIDDEN);
    news.setDescription("News description");
    fFactory.createGuid(news, "someGUIDvalue", null);
    news.setLink(createURI("http://www.somelocation.com/feed.rss"));
    news.setModifiedDate(createDate());
    news.setProperty("property", "value");
    news.setPublishDate(createDate());
    ISource source = fFactory.createSource(news);
    source.setLink(createURI("http://www.someuri.com"));
    news.setSource(source);
    news.setTitle("This is the news title");
    news.setRating(70);
    return news;
  }

  private Feed createFeed() {
    return createFeed("http://www.rssowl.org/feed.rss");
  }

  private Feed createFeed(String link) {
    Feed feed = (Feed) fFactory.createFeed(null, createURI(link));
    feed.setTitle("feed title");
    feed.setDescription("feed description");
    feed.setHomepage(createURI("http://www.rssowl.org"));
    feed.setAuthor(createPersonJohn(feed));
    feed.setLanguage("English");
    feed.setCopyright("This feed is copyrighted");
    feed.setDocs(createURI("http://www.rssowl.org/documentation.html"));
    feed.setGenerator("Manual");
    feed.setImage(createImage(feed));
    feed.setPublishDate(createDate());
    feed.setLastBuildDate(createDate());
    feed.setLastModifiedDate(createDate());
    feed.setWebmaster("Webmaster");
    feed.setTTL(60);
    feed.setFormat("RSS");
    feed.setProperty("feedProperty", "randomValue");
    feed.setBase(createURI("http://www.baseuri.com/"));
    return feed;
  }

  URI createURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      // should not happen;
      return null;
    }
  }

  private IPerson createPersonJohn(IPersistable type) {
    IPerson person = fFactory.createPerson(null, type);
    person.setName("John");
    person.setEmail(createURI("john@hotmail.com"));
    person.setUri(createURI("http://mysite.hotmail.com"));
    person.setProperty("property", "property_value");
    return person;
  }

  private IPerson createPersonMary(IPersistable type) {
    IPerson person = fFactory.createPerson(null, type);
    person.setName("Mary");
    person.setEmail(createURI("mary@hotmail.com"));
    person.setUri(createURI("http://mary.hotmail.com"));
    person.setProperty("test", "property");
    return person;
  }

  @SuppressWarnings("unused")
  private IPerson createPersonDan(IPersistable type) {
    IPerson person = fFactory.createPerson(null, type);
    person.setName("Dan");
    person.setEmail(createURI("dan@yahoo.com"));
    return person;
  }

  private Date createDate() {
    return new Date();
  }

  private IImage createImage(IFeed feed) {
    IImage image = fFactory.createImage(feed);
    image.setHomepage(createURI("http://www.rssowl.org/image.png"));
    return image;
  }
}
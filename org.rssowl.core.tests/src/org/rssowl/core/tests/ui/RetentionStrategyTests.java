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

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.RetentionStrategy;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Tests about Retention of News.
 *
 * @author bpasero
 */
public class RetentionStrategyTests {

  /* One Day in millis (-1 second crush zone) */
  private static final long DAY = 24 * 60 * 59 * 1000;

  /* One Hour in millis */
  private static final long HOUR = 60 * 60 * 1000;

  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
    Owl.getPreferenceService().getGlobalScope().putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, false);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAge() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    RetentionStrategy.process(folder);
    assertEquals(200, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(100, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    RetentionStrategy.process(folder);
    assertEquals(200, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(100, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAge_NoDeleteUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    RetentionStrategy.process(folder);
    assertEquals(200, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(100, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAge_NoDeleteUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    RetentionStrategy.process(folder);
    assertEquals(200, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(100, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(140, countNews(folder));
    assertEquals(20, countNews(bookMark1));
    assertEquals(40, countNews(bookMark2));
    assertEquals(80, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByCountKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(230, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(80, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByCount_NoDeleteUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(230, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(80, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByCount_NoDeleteUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(230, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(80, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(60, countNews(folder));
    assertEquals(20, countNews(bookMark1));
    assertEquals(40, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndCountKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndCount_NoDeleteUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndCount_NoDeleteUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByCountAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(60, countNews(folder));
    assertEquals(20, countNews(bookMark1));
    assertEquals(40, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByCountAndStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndCountAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(60, countNews(folder));
    assertEquals(20, countNews(bookMark1));
    assertEquals(40, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFolderByAgeAndCountAndStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed1 = createFeedWithNews(new URI("http://www.url1.com"), 100, 0, today - DAY, today + 5 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed1.getLink()), "BookMark1");

    IFeed feed2 = createFeedWithNews(new URI("http://www.url2.com"), 100, 50, today - 7 * DAY, today + 8 * HOUR, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed2.getLink()), "BookMark2");

    IFeed feed3 = createFeedWithNews(new URI("http://www.url3.com"), 100, 100, today - 31 * DAY, today - 25 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed3.getLink()), "BookMark3");

    DynamicDAO.save(folder);

    IBookMark bookMark1 = null, bookMark2 = null, bookMark3 = null;
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark.getName().equals("BookMark1"))
        bookMark1 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark2"))
        bookMark2 = (IBookMark) mark;
      else if (mark.getName().equals("BookMark3"))
        bookMark3 = (IBookMark) mark;
    }

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookMark1);
    IPreferenceScope prefs2 = Owl.getPreferenceService().getEntityScope(bookMark2);
    IPreferenceScope prefs3 = Owl.getPreferenceService().getEntityScope(bookMark3);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs2.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);
    prefs3.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 20);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 20);
    prefs2.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 40);
    prefs3.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 80);
    RetentionStrategy.process(folder);
    assertEquals(150, countNews(folder));
    assertEquals(100, countNews(bookMark1));
    assertEquals(50, countNews(bookMark2));
    assertEquals(0, countNews(bookMark3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAge() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    RetentionStrategy.process(bookmark);
    assertEquals(0, countNews(folder));
    assertEquals(0, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAge_NoDeleteUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAge_NoDeleteUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(50, countNews(folder));
    assertEquals(50, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByCountKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByCount_NoDeleteUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByCount_NoDeleteUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(0, countNews(folder));
    assertEquals(0, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCountKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCount_NoDeleteUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCount_NoDeleteUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    RetentionStrategy.process(bookmark);
    assertEquals(0, countNews(folder));
    assertEquals(0, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByCountAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(50, countNews(folder));
    assertEquals(50, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByCountAndStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCountAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(0, countNews(folder));
    assertEquals(0, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCountAndStateKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(80, countNews(folder));
    assertEquals(80, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCountAndStateWithStickyNews() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 10);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(10, countNews(folder));
    assertEquals(10, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessBookMarkByAgeAndCountAndStateWithStickyNewsKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 10, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(100, countNews(bookmark));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    RetentionStrategy.process(bookmark);
    assertEquals(90, countNews(folder));
    assertEquals(90, countNews(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByAge() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.UNREAD);
    news3.setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(101, updatedNews.size());
    assertEquals(2, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News #3");
    news3.setState(INews.State.UNREAD);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(53, updatedNews.size());
    assertEquals(50, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByCountThatExceedLimit() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News #3");
    news3.setState(INews.State.UNREAD);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(53, updatedNews.size());
    assertEquals(50, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News #3");
    news3.setState(INews.State.UNREAD);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(21, updatedNews.size());
    assertEquals(82, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByAgeAndCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News #3");
    news3.setState(INews.State.UNREAD);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(101, updatedNews.size());
    assertEquals(2, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByAgeAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News #3");
    news3.setState(INews.State.UNREAD);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(102, updatedNews.size());
    assertEquals(1, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByCountAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News #3");
    news3.setState(INews.State.UNREAD);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(53, updatedNews.size());
    assertEquals(50, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithUnpersistedNewsByAgeAndCountAndState() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 20, today - 7 * DAY, today - 6 * DAY, 0);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");

    DynamicDAO.save(folder);

    /* Add unpersisted News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.READ);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setTitle("News #3");
    news3.setState(INews.State.UNREAD);

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, 5);
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(102, updatedNews.size());
    assertEquals(1, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithStickyNewsByCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 100);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.UNREAD);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.UNREAD);
    news3.setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(3, updatedNews.size());
    assertEquals(100, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithStickyNewsByCountKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 100, true);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setPublishDate(new Date(today - 7 * DAY));
    fFactory.createNews(null, feed, new Date()).setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(0, updatedNews.size());
    assertEquals(103, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithLabeledNewsByCount() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 0, false, label);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.UNREAD);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.UNREAD);
    news3.setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(3, updatedNews.size());
    assertEquals(100, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithLabeledNewsByCountKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 0, true, label);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setPublishDate(new Date(today - 7 * DAY));
    fFactory.createNews(null, feed, new Date()).setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(0, updatedNews.size());
    assertEquals(103, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithStickyNewsByCountKeepUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 100, false, null);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    news1.setState(INews.State.UNREAD);
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setState(INews.State.UNREAD);
    news2.setPublishDate(new Date(today - 7 * DAY));
    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.UNREAD);
    news3.setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(0, updatedNews.size());
    assertEquals(103, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithStickyNewsByCountKeepUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 100, true, null);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setPublishDate(new Date(today - 7 * DAY));
    fFactory.createNews(null, feed, new Date()).setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(0, updatedNews.size());
    assertEquals(103, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithLabeledNewsByCountKeepUnread() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 0, true, label);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setPublishDate(new Date(today - 7 * DAY));
    fFactory.createNews(null, feed, new Date()).setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(0, updatedNews.size());
    assertEquals(103, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithLabeledNewsByCountDoNotKeepLabeled() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 0, false, label);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setPublishDate(new Date(today - 7 * DAY));
    fFactory.createNews(null, feed, new Date()).setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE, false);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(53, updatedNews.size());
    assertEquals(50, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithLabeledNewsByCountKeepLabeled() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 0, false, label);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setPublishDate(new Date(today - 7 * DAY));
    fFactory.createNews(null, feed, new Date()).setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(0, updatedNews.size());
    assertEquals(103, countNews(feed));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testProcessFeedWithLabeledNewsByCountKeepUnreadKeepNew() throws Exception {
    long today = DateUtils.getToday().getTimeInMillis();

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));

    IFeed feed = createFeedWithNews(new URI("http://www.url.com"), 100, 100, today - 7 * DAY, today - 6 * DAY, 0, true, label);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark1");

    DynamicDAO.save(folder);

    /* Add unpersisted *new* News */
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setTitle("News #1");
    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setTitle("News #2");
    news2.setPublishDate(new Date(today - 7 * DAY));
    fFactory.createNews(null, feed, new Date()).setTitle("News #3");

    IBookMark bookmark = (IBookMark) folder.getMarks().get(0);
    assertEquals(103, countNews(feed));

    /* Preferences */
    IPreferenceScope prefs1 = Owl.getPreferenceService().getEntityScope(bookmark);

    /* Setup Retention */
    prefs1.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, true);
    prefs1.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, true);

    /* Run and Validate Retention */
    prefs1.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 100);
    List<INews> updatedNews = RetentionStrategy.process(bookmark, feed);
    assertEquals(0, updatedNews.size());
    assertEquals(103, countNews(feed));
  }

  private IFeed createFeedWithNews(URI link, int total, int read, long from, long to, int sticky) throws PersistenceException {
    return createFeedWithNews(link, total, read, from, to, sticky, false);
  }

  private IFeed createFeedWithNews(URI link, int total, int read, long from, long to, int sticky, boolean keepNew) throws PersistenceException {
    return createFeedWithNews(link, total, read, from, to, sticky, keepNew, null);
  }

  private IFeed createFeedWithNews(URI link, int total, int read, long from, long to, int sticky, boolean keepNew, ILabel label) throws PersistenceException {
    long dateDif = to - from;
    IFeed feed = fFactory.createFeed(null, link);

    for (int i = 0; i < total; i++) {
      INews news = fFactory.createNews(null, feed, new Date());
      news.setTitle("News 1");

      if (read > i)
        news.setState(INews.State.READ);
      else if (!keepNew)
        news.setState(INews.State.UNREAD);

      if (sticky > i)
        news.setFlagged(true);

      if (label != null)
        news.addLabel(label);

      news.setPublishDate(new Date(from + (dateDif * i / total)));
    }

    return DynamicDAO.save(feed);
  }

  private int countNews(IEntity entity) throws PersistenceException {
    int count = 0;

    if (entity instanceof IFolder) {
      IFolder folder = new FolderReference(entity.getId()).resolve();
      List<IMark> marks = folder.getMarks();
      for (IMark mark : marks) {
        if (mark instanceof IBookMark) {
          IFeed feed = ((IBookMark) mark).getFeedLinkReference().resolve();
          count += feed.getVisibleNews().size();
        }
      }
    }

    else if (entity instanceof IBookMark) {
      IBookMark bookmark = new BookMarkReference(entity.getId()).resolve();
      count += bookmark.getFeedLinkReference().resolve().getVisibleNews().size();
    }

    else if (entity instanceof IFeed) {
      count += ((IFeed) entity).getVisibleNews().size();
    }

    return count;
  }
}
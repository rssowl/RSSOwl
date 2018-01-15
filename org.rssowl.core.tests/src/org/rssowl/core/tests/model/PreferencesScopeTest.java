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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.GlobalScope;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.BookMarkAdapter;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferencesInitializer;
import org.rssowl.core.persist.reference.FeedLinkReference;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bpasero
 */
@SuppressWarnings("nls")
public class PreferencesScopeTest extends LargeBlockSizeTest implements IPreferencesInitializer {
  private static final AtomicBoolean IS_SECOND_RUN = new AtomicBoolean(false);
  private static final String TEST_BOOLEAN = "testBoolean";
  private static final String TEST_BOOLEAN_INITIAL_FALSE = "testBooleanInitialFalse";
  private static final String TEST_INTEGER = "testInteger";
  private static final String TEST_INTEGERS = "testIntegers";
  private static final String TEST_LONG = "testLong";
  private static final String TEST_LONGS = "testLongs";
  private static final String TEST_STRING = "testString";
  private static final String TEST_STRINGS = "testStrings";
  private static final String TEST_BOOLEAN_ECLIPSE = "/testBoolean";
  private static final String TEST_INTEGER_ECLIPSE = "/testInteger";
  private static final String TEST_LONG_ECLIPSE = "/testLong";
  private static final String TEST_STRING_ECLIPSE = "/testString";
  private IModelFactory fFactory;

  @BeforeClass
  public static void reInitializeIfNecessary() {
    if (!IS_SECOND_RUN.get())
      IS_SECOND_RUN.set(true);
    else
      doInitialize(Owl.getPreferenceService().getDefaultScope());
  }

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    ((GlobalScope) Owl.getPreferenceService().getGlobalScope()).clearCache();
    fFactory = Owl.getModelFactory();
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testPreferencesInitializer() throws Exception {
    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();
    assertEquals(true, defaultScope.getBoolean(TEST_BOOLEAN));
    assertEquals(1, defaultScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 1, 2, 3 }, defaultScope.getIntegers(TEST_INTEGERS)));
    assertEquals("foo", defaultScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "foo", "bar" }, defaultScope.getStrings(TEST_STRINGS)));
    assertEquals(1L, defaultScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 1L, 2L, 3L }, defaultScope.getLongs(TEST_LONGS)));
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testGlobalScope() throws Exception {
    IPreferenceScope globalScope = Owl.getPreferenceService().getGlobalScope();

    /* Test Defaults Taken */
    assertEquals(true, globalScope.getBoolean(TEST_BOOLEAN));
    assertEquals(1, globalScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 1, 2, 3 }, globalScope.getIntegers(TEST_INTEGERS)));
    assertEquals("foo", globalScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "foo", "bar" }, globalScope.getStrings(TEST_STRINGS)));
    assertEquals(1L, globalScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 1L, 2L, 3L }, globalScope.getLongs(TEST_LONGS)));

    /* Change Settings and test again */
    globalScope.putBoolean(TEST_BOOLEAN, false);
    globalScope.putInteger(TEST_INTEGER, 2);
    globalScope.putIntegers(TEST_INTEGERS, new int[] { 4, 5, 6, 7, 8 });
    globalScope.putString(TEST_STRING, "hello");
    globalScope.putStrings(TEST_STRINGS, new String[] { "hello", "world", "!" });
    globalScope.putLong(TEST_LONG, 2L);
    globalScope.putLongs(TEST_LONGS, new long[] { 4L, 5L, 6L, 7L, 8L });

    /* Test new Settings */
    assertEquals(false, globalScope.getBoolean(TEST_BOOLEAN));
    assertEquals(2, globalScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 4, 5, 6, 7, 8 }, globalScope.getIntegers(TEST_INTEGERS)));
    assertEquals("hello", globalScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "hello", "world", "!" }, globalScope.getStrings(TEST_STRINGS)));
    assertEquals(2L, globalScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 4L, 5L, 6L, 7L, 8L }, globalScope.getLongs(TEST_LONGS)));

    /* Delete Settings */
    globalScope.delete(TEST_BOOLEAN);
    globalScope.delete(TEST_INTEGER);
    globalScope.delete(TEST_INTEGERS);
    globalScope.delete(TEST_STRING);
    globalScope.delete(TEST_STRINGS);
    globalScope.delete(TEST_LONG);
    globalScope.delete(TEST_LONGS);

    /* Test Defaults Again */
    assertEquals(true, globalScope.getBoolean(TEST_BOOLEAN));
    assertEquals(1, globalScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 1, 2, 3 }, globalScope.getIntegers(TEST_INTEGERS)));
    assertEquals("foo", globalScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "foo", "bar" }, globalScope.getStrings(TEST_STRINGS)));
    assertEquals(1L, globalScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 1L, 2L, 3L }, globalScope.getLongs(TEST_LONGS)));
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testEclipseScope() throws Exception {
    IPreferenceScope prefs = Owl.getPreferenceService().getEclipseScope();

    /* Test Defaults Taken */
    assertEquals(true, prefs.getBoolean(TEST_BOOLEAN_ECLIPSE));
    assertEquals(1, prefs.getInteger(TEST_INTEGER_ECLIPSE));
    assertEquals("foo", prefs.getString(TEST_STRING_ECLIPSE));
    assertEquals(1L, prefs.getLong(TEST_LONG_ECLIPSE));

    /* Change Settings and test again */
    prefs.putBoolean(TEST_BOOLEAN_ECLIPSE, false);
    prefs.putInteger(TEST_INTEGER_ECLIPSE, 2);
    prefs.putString(TEST_STRING_ECLIPSE, "hello");
    prefs.putLong(TEST_LONG_ECLIPSE, 2L);

    /* Test new Settings */
    assertEquals(false, prefs.getBoolean(TEST_BOOLEAN_ECLIPSE));
    assertEquals(2, prefs.getInteger(TEST_INTEGER_ECLIPSE));
    assertEquals("hello", prefs.getString(TEST_STRING_ECLIPSE));
    assertEquals(2L, prefs.getLong(TEST_LONG_ECLIPSE));

    prefs.flush();

    /* Test Again */
    assertEquals(false, prefs.getBoolean(TEST_BOOLEAN_ECLIPSE));
    assertEquals(2, prefs.getInteger(TEST_INTEGER_ECLIPSE));
    assertEquals("hello", prefs.getString(TEST_STRING_ECLIPSE));
    assertEquals(2L, prefs.getLong(TEST_LONG_ECLIPSE));

    /* Revert back to old settings */
    prefs.putBoolean(TEST_BOOLEAN_ECLIPSE, true);
    prefs.putInteger(TEST_INTEGER_ECLIPSE, 1);
    prefs.putString(TEST_STRING_ECLIPSE, "foo");
    prefs.putLong(TEST_LONG_ECLIPSE, 1L);

    prefs.flush();
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testEntityScope() throws Exception {
    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));
    IFeed feed = fFactory.createFeed(null, new URI("http://www.link.com"));
    feed = DynamicDAO.save(feed);
    fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
    folder = DynamicDAO.save(folder);

    IPreferenceScope entityScope = Owl.getPreferenceService().getEntityScope(folder);

    /* Test Defaults Taken */
    assertEquals(true, entityScope.getBoolean(TEST_BOOLEAN));
    assertEquals(1, entityScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 1, 2, 3 }, entityScope.getIntegers(TEST_INTEGERS)));
    assertEquals("foo", entityScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "foo", "bar" }, entityScope.getStrings(TEST_STRINGS)));
    assertEquals(1L, entityScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 1L, 2L, 3L }, entityScope.getLongs(TEST_LONGS)));

    /* Change Settings and test again */
    entityScope.putBoolean(TEST_BOOLEAN, false);
    entityScope.putInteger(TEST_INTEGER, 2);
    entityScope.putIntegers(TEST_INTEGERS, new int[] { 4, 5, 6, 7, 8 });
    entityScope.putString(TEST_STRING, "hello");
    entityScope.putStrings(TEST_STRINGS, new String[] { "hello", "world", "!" });
    entityScope.putLong(TEST_LONG, 2L);
    entityScope.putLongs(TEST_LONGS, new long[] { 4L, 5L, 6L, 7L, 8L });
    entityScope.flush();

    /* Test new Settings */
    assertEquals(false, entityScope.getBoolean(TEST_BOOLEAN));
    assertEquals(2, entityScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 4, 5, 6, 7, 8 }, entityScope.getIntegers(TEST_INTEGERS)));
    assertEquals("hello", entityScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "hello", "world", "!" }, entityScope.getStrings(TEST_STRINGS)));
    assertEquals(2L, entityScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 4L, 5L, 6L, 7L, 8L }, entityScope.getLongs(TEST_LONGS)));

    /* Delete Settings */
    entityScope.delete(TEST_BOOLEAN);
    entityScope.delete(TEST_INTEGER);
    entityScope.delete(TEST_INTEGERS);
    entityScope.delete(TEST_STRING);
    entityScope.delete(TEST_STRINGS);
    entityScope.delete(TEST_LONG);
    entityScope.delete(TEST_LONGS);
    entityScope.flush();

    /* Test Defaults Again */
    assertEquals(true, entityScope.getBoolean(TEST_BOOLEAN));
    assertEquals(1, entityScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 1, 2, 3 }, entityScope.getIntegers(TEST_INTEGERS)));
    assertEquals("foo", entityScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "foo", "bar" }, entityScope.getStrings(TEST_STRINGS)));
    assertEquals(1L, entityScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 1L, 2L, 3L }, entityScope.getLongs(TEST_LONGS)));

    /* Test Global Settings Taken */
    IPreferenceScope globalScope = Owl.getPreferenceService().getGlobalScope();
    globalScope.putBoolean(TEST_BOOLEAN, false);
    globalScope.putInteger(TEST_INTEGER, 2);
    globalScope.putIntegers(TEST_INTEGERS, new int[] { 4, 5, 6, 7, 8 });
    globalScope.putString(TEST_STRING, "hello");
    globalScope.putStrings(TEST_STRINGS, new String[] { "hello", "world", "!" });
    globalScope.putLong(TEST_LONG, 2L);
    globalScope.putLongs(TEST_LONGS, new long[] { 4L, 5L, 6L, 7L, 8L });

    assertEquals(false, entityScope.getBoolean(TEST_BOOLEAN));
    assertEquals(2, entityScope.getInteger(TEST_INTEGER));
    assertEquals(true, Arrays.equals(new int[] { 4, 5, 6, 7, 8 }, entityScope.getIntegers(TEST_INTEGERS)));
    assertEquals("hello", entityScope.getString(TEST_STRING));
    assertEquals(true, Arrays.equals(new String[] { "hello", "world", "!" }, entityScope.getStrings(TEST_STRINGS)));
    assertEquals(2L, entityScope.getLong(TEST_LONG));
    assertEquals(true, Arrays.equals(new long[] { 4L, 5L, 6L, 7L, 8L }, entityScope.getLongs(TEST_LONGS)));
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testEntityScopeChangeWithGC() throws Exception {
    IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));
    IFeed feed = fFactory.createFeed(null, new URI("http://www.link.com"));
    feed = DynamicDAO.save(feed);
    folder = DynamicDAO.save(folder);

    IPreferenceScope entityScope = Owl.getPreferenceService().getEntityScope(folder);

    assertEquals(false, entityScope.getBoolean(TEST_BOOLEAN_INITIAL_FALSE));

    entityScope.putBoolean(TEST_BOOLEAN_INITIAL_FALSE, true);
    entityScope.flush();
    assertEquals(true, entityScope.getBoolean(TEST_BOOLEAN_INITIAL_FALSE));

    folder = null;
    feed = null;
    entityScope = null;
    System.gc();

    folder = Owl.getPersistenceService().getDAOService().getFolderDAO().loadRoots().iterator().next();
    entityScope = Owl.getPreferenceService().getEntityScope(folder);

    assertEquals(true, entityScope.getBoolean(TEST_BOOLEAN_INITIAL_FALSE));

    entityScope.putBoolean(TEST_BOOLEAN_INITIAL_FALSE, false);
    entityScope.flush();
    assertEquals(false, entityScope.getBoolean(TEST_BOOLEAN_INITIAL_FALSE));

    folder = null;
    entityScope = null;
    System.gc();

    folder = Owl.getPersistenceService().getDAOService().getFolderDAO().loadRoots().iterator().next();
    entityScope = Owl.getPreferenceService().getEntityScope(folder);

    assertEquals(false, entityScope.getBoolean(TEST_BOOLEAN_INITIAL_FALSE));
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testEntityScopeUpdateEvents() throws Exception {
    BookMarkListener bookmarkListener = null;
    try {
      IFolder folder = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));
      IFeed feed = fFactory.createFeed(null, new URI("http://www.link.com"));
      feed = DynamicDAO.save(feed);
      fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "BookMark");
      folder = DynamicDAO.save(folder);

      final int eventsCounter[] = new int[] { 0 };
      bookmarkListener = new BookMarkAdapter() {
        @Override
        public void entitiesUpdated(Set<BookMarkEvent> events) {
          eventsCounter[0]++;
        }
      };
      DynamicDAO.addEntityListener(IBookMark.class, bookmarkListener);

      IMark mark = folder.getMarks().get(0);

      IPreferenceScope bookmarkScope = Owl.getPreferenceService().getEntityScope(mark);
      bookmarkScope.putString("key10", "value1");
      bookmarkScope.flush();

      assertEquals(1, eventsCounter[0]);

      bookmarkScope.putString("key20", "value2");
      bookmarkScope.flush();

      assertEquals(2, eventsCounter[0]);
    } finally {
      if (bookmarkListener != null)
        DynamicDAO.removeEntityListener(IBookMark.class, bookmarkListener);
    }
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesInitializer#initialize(org
   * .rssowl.core.model.preferences.IPreferencesScope)
   */
  @Override
  public void initialize(IPreferenceScope defaultScope) {
    doInitialize(defaultScope);
  }

  private static void doInitialize(IPreferenceScope scope) {
    scope.putBoolean(TEST_BOOLEAN, true);
    scope.putInteger(TEST_INTEGER, 1);
    scope.putIntegers(TEST_INTEGERS, new int[] { 1, 2, 3 });
    scope.putString(TEST_STRING, "foo");
    scope.putStrings(TEST_STRINGS, new String[] { "foo", "bar" });
    scope.putLong(TEST_LONG, 1L);
    scope.putLongs(TEST_LONGS, new long[] { 1L, 2L, 3L });

    scope.putBoolean(TEST_BOOLEAN_ECLIPSE, true);
    scope.putInteger(TEST_INTEGER_ECLIPSE, 1);
    scope.putString(TEST_STRING_ECLIPSE, "foo");
    scope.putLong(TEST_LONG_ECLIPSE, 1L);
  }
}
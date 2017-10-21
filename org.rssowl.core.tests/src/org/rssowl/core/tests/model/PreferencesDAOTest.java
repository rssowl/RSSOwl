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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IPreferenceDAO;
import org.rssowl.core.persist.event.PreferenceEvent;
import org.rssowl.core.persist.event.PreferenceListener;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Ismael Juma (ismael@juma.me.uk)
 * @author bpasero
 */
@SuppressWarnings("nls")
public class PreferencesDAOTest extends LargeBlockSizeTest {
  private IPreferenceDAO fDao;
  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fDao = DynamicDAO.getDAO(IPreferenceDAO.class);
    fFactory = Owl.getModelFactory();
  }

  /**
   * Tests that IPreference#getType returns the right value after the object
   * has been saved and loaded by the db.
   * @throws Exception
   */
  @Test
  public void testGetTypeAfterSave() throws Exception   {
    String booleanKey = "boolean";
    String longKey = "long";
    String stringKey = "string";

    IPreference booleanPref = fDao.loadOrCreate(booleanKey);
    booleanPref.putBooleans(true);
    fDao.save(booleanPref);

    IPreference longPref = fDao.loadOrCreate(longKey);
    longPref.putLongs(5L);
    fDao.save(longPref);

    IPreference stringPref = fDao.loadOrCreate(stringKey);
    stringPref.putStrings("some string");
    fDao.save(stringPref);

    booleanPref = null;
    stringPref = null;
    longPref = null;
    System.gc();

    assertEquals(IPreference.Type.BOOLEAN, fDao.load(booleanKey).getType());
    assertEquals(IPreference.Type.LONG, fDao.load(longKey).getType());
    assertEquals(IPreference.Type.STRING, fDao.load(stringKey).getType());
  }

  /**
   * Test adding and getting boolean Preferences.
   *
   * @throws Exception
   */
  @Test
  public final void testPutGetBoolean() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";

    IPreference pref = fDao.loadOrCreate(key1);
    pref.putBooleans(true);
    fDao.save(pref);

    pref = fDao.loadOrCreate(key2);
    pref.putBooleans(true);
    fDao.save(pref);

    pref = fDao.loadOrCreate(key3);
    pref.putBooleans(false);
    fDao.save(pref);

    assertEquals(Boolean.TRUE, fDao.load(key1).getBoolean());
    assertEquals(Boolean.TRUE, fDao.load(key2).getBoolean());
    assertEquals(Boolean.FALSE, fDao.load(key3).getBoolean());

    pref = fDao.loadOrCreate(key2);
    pref.putBooleans(false);
    fDao.save(pref);

    assertEquals(Boolean.TRUE, fDao.load(key1).getBoolean());
    assertEquals(Boolean.FALSE, fDao.load(key2).getBoolean());
    assertEquals(Boolean.FALSE, fDao.load(key3).getBoolean());
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testActivation() throws Exception   {
    String key = "key";
    IPreference pref = fFactory.createPreference(key);
    pref.putBooleans(true);
    fDao.save(pref);
    pref = null;
    System.gc();
    assertEquals(Boolean.TRUE, fDao.load(key).getBoolean());
    String anotherKey = "anotherKey";
    String[] longs = new String[] { "2", "3", "5"};
    pref = fFactory.createPreference(anotherKey);
    pref.putStrings(longs);
    fDao.save(pref);
    longs = null;
    pref = null;
    System.gc();
    assertEquals(3, fDao.load(anotherKey).getStrings().length);
  }

  /**
   * Test adding and getting Strings Preferences.
   *
   * @throws Exception
   */
  @Test
  public final void testPutGetStrings() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";

    IPreference pref1 = fFactory.createPreference(key1);
    String[] value1 = new String[] { "value1.1", "value1.2", "value1.3" };
    pref1.putStrings(value1);

    IPreference pref2 = fFactory.createPreference(key2);
    String[] value2 = new String[] { "value2.1", "value2.2", "value2.3" };
    pref2.putStrings(value2);

    IPreference pref3 = fFactory.createPreference(key3);
    String[] value3 = new String[] { "value3.1", "value3.2", "value3.3" };
    pref3.putStrings(value3);

    fDao.save(pref1);
    fDao.save(pref2);
    fDao.save(pref3);

    assertArrayEquals(value1, fDao.load(key1).getStrings());
    assertArrayEquals(value2, fDao.load(key2).getStrings());
    assertArrayEquals(value3, fDao.load(key3).getStrings());

    value2 = new String[] { "newvalue1.1", "newvalue1.2", "newvalue1.3" };
    pref2.putStrings(value2);
    fDao.save(pref2);

    assertArrayEquals(value1, fDao.load(key1).getStrings());
    assertArrayEquals(value2, fDao.load(key2).getStrings());
    assertArrayEquals(value3, fDao.load(key3).getStrings());
  }

  /**
   * Test adding and getting Longs Preferences.
   *
   * @throws Exception
   */
  @Test
  public final void testPutGetLongs() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";

    long[] value1 = new long[] { 11, 12, 13 };
    long[] value2 = new long[] { 21, 22, 23 };
    long[] value3 = new long[] { 31, 32, 33 };

    IPreference pref = fDao.loadOrCreate(key1);
    pref.putLongs(value1);
    fDao.save(pref);

    pref = fDao.loadOrCreate(key2);
    pref.putLongs(value2);
    fDao.save(pref);

    pref = fDao.loadOrCreate(key3);
    pref.putLongs(value3);
    fDao.save(pref);

    assertEquals(true, Arrays.equals(value1, fDao.load(key1).getLongs()));
    assertEquals(true, Arrays.equals(value2, fDao.load(key2).getLongs()));
    assertEquals(true, Arrays.equals(value3, fDao.load(key3).getLongs()));

    value2 = new long[] { 110, 120, 130 };
    pref = fDao.loadOrCreate(key2);
    pref.putLongs(value2);
    fDao.save(pref);

    assertEquals(true, Arrays.equals(value1, fDao.load(key1).getLongs()));
    assertEquals(true, Arrays.equals(value2, fDao.load(key2).getLongs()));
    assertEquals(true, Arrays.equals(value3, fDao.load(key3).getLongs()));
  }

  /**
   * Test adding and getting Ints Preferences.
   *
   * @throws Exception
   */
  @Test
  public final void testPutGetInts() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";

    int[] value1 = new int[] { 11, 12, 13 };
    int[] value2 = new int[] { 21, 22, 23 };
    int[] value3 = new int[] { 31, 32, 33 };

    IPreference pref = fFactory.createPreference(key1);
    pref.putIntegers(value1);
    fDao.save(pref);

    pref = fFactory.createPreference(key2);
    pref.putIntegers(value2);
    fDao.save(pref);

    pref = fFactory.createPreference(key3);
    pref.putIntegers(value3);
    fDao.save(pref);

    assertEquals(true, Arrays.equals(value1, fDao.load(key1).getIntegers()));
    assertEquals(true, Arrays.equals(value2, fDao.load(key2).getIntegers()));
    assertEquals(true, Arrays.equals(value3, fDao.load(key3).getIntegers()));

    value2 = new int[] { 110, 120, 130 };
    pref = fDao.loadOrCreate(key2);
    pref.putIntegers(value2);
    fDao.save(pref);

    assertEquals(true, Arrays.equals(value1, fDao.load(key1).getIntegers()));
    assertEquals(true, Arrays.equals(value2, fDao.load(key2).getIntegers()));
    assertEquals(true, Arrays.equals(value3, fDao.load(key3).getIntegers()));
  }

  /**
   * Test adding and getting Long Preferences.
   *
   * @throws Exception
   */
  @Test
  public final void testPutGetLong() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";

    long value1 = 10;
    long value2 = 15;
    long value3 = 20;

    IPreference pref = fFactory.createPreference(key1);
    pref.putLongs(value1);
    fDao.save(pref);

    pref = fFactory.createPreference(key2);
    pref.putLongs(value2);
    fDao.save(pref);

    pref = fFactory.createPreference(key3);
    pref.putLongs(value3);
    fDao.save(pref);

    assertEquals(Long.valueOf(value1), fDao.load(key1).getLong());
    assertEquals(Long.valueOf(value2), fDao.load(key2).getLong());
    assertEquals(Long.valueOf(value3), fDao.load(key3).getLong());

    value3 = 5;
    pref.putLongs(value3);
    fDao.save(pref);

    assertEquals(Long.valueOf(value1), fDao.load(key1).getLong());
    assertEquals(Long.valueOf(value2), fDao.load(key2).getLong());
    assertEquals(Long.valueOf(value3), fDao.load(key3).getLong());
  }

  /**
   * Test adding and getting String Preference.
   *
   * @throws Exception
   */
  @Test
  public final void testPutGetString() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";

    String value1 = "value1";
    String value2 = "value2";
    String value3 = "value3";

    IPreference pref = fFactory.createPreference(key1);
    pref.putStrings(value1);
    fDao.save(pref);

    pref = fFactory.createPreference(key2);
    pref.putStrings(value2);
    fDao.save(pref);

    pref = fFactory.createPreference(key3);
    pref.putStrings(value3);
    fDao.save(pref);

    assertEquals(value1, fDao.load(key1).getString());
    assertEquals(value2, fDao.load(key2).getString());
    assertEquals(value3, fDao.load(key3).getString());

    value1 = "newValue1";
    pref = fDao.load(key1);
    pref.putStrings(value1);
    fDao.save(pref);

    assertEquals(value1, fDao.load(key1).getString());
    assertEquals(value2, fDao.load(key2).getString());
    assertEquals(value3, fDao.load(key3).getString());
  }

  /**
   * Test adding and getting Integer Preferences.
   *
   * @throws Exception
   */
  @Test
  public final void testPutGetInteger() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";

    int value1 = 10;
    int value2 = 15;
    int value3 = 20;

    IPreference pref = fFactory.createPreference(key1);
    pref.putIntegers(value1);
    fDao.save(pref);

    pref = fFactory.createPreference(key2);
    pref.putIntegers(value2);
    fDao.save(pref);

    pref = fFactory.createPreference(key3);
    pref.putIntegers(value3);
    fDao.save(pref);

    assertEquals(Integer.valueOf(value1), fDao.load(key1).getInteger());
    assertEquals(Integer.valueOf(value2), fDao.load(key2).getInteger());
    assertEquals(Integer.valueOf(value3), fDao.load(key3).getInteger());

    value3 = 5;
    pref.putIntegers(value3);
    fDao.save(pref);

    assertEquals(Integer.valueOf(value1), fDao.load(key1).getInteger());
    assertEquals(Integer.valueOf(value2), fDao.load(key2).getInteger());
    assertEquals(Integer.valueOf(value3), fDao.load(key3).getInteger());
  }

  /**
   * Test Deleting Preferences
   *
   * @throws Exception
   */
  @Test
  public final void testDelete() throws Exception {
    String key1 = "key1";
    String key2 = "key2";
    String key3 = "key3";
    String key4 = "key4";
    boolean value1 = true;
    String value2 = "value2";
    int value3 = 34;
    String[] value4 = new String[] { "value4.1", "value4.2", "value4.3" };

    IPreference pref = fFactory.createPreference(key1);
    pref.putBooleans(value1);
    fDao.save(pref);

    pref = fFactory.createPreference(key2);
    pref.putStrings(value2);
    fDao.save(pref);

    pref = fFactory.createPreference(key3);
    pref.putIntegers(value3);
    fDao.save(pref);

    pref = fFactory.createPreference(key4);
    pref.putStrings(value4);
    fDao.save(pref);

    assertEquals(Boolean.valueOf(value1), fDao.load(key1).getBoolean());
    assertEquals(value2, fDao.load(key2).getString());
    assertEquals(Integer.valueOf(value3), fDao.load(key3).getInteger());
    assertArrayEquals(value4, fDao.load(key4).getStrings());

    boolean deleted = fDao.delete(key3);
    assertTrue(deleted);
    assertEquals(Boolean.valueOf(value1), fDao.load(key1).getBoolean());
    assertEquals(value2, fDao.load(key2).getString());
    assertNull("key3 should be null, but it is: " + key3, fDao.load(key3));
    assertArrayEquals(value4, fDao.load(key4).getStrings());

    deleted = fDao.delete(key1);
    assertTrue(deleted);
    assertNull(fDao.load(key1));
    assertEquals(value2, fDao.load(key2).getString());
    assertNull(fDao.load(key3));
    assertArrayEquals(value4, fDao.load(key4).getStrings());

    /* Call delete on key that has already been deleted */
    deleted = fDao.delete(key1);
    assertFalse(deleted);
    assertNull(fDao.load(key1));
    assertEquals(value2, fDao.load(key2).getString());
    assertNull(fDao.load(key3));
    assertArrayEquals(value4, fDao.load(key4).getStrings());

    deleted = fDao.delete(key4);
    assertTrue(deleted);
    assertNull(fDao.load(key1));
    assertEquals(value2, fDao.load(key2).getString());
    assertNull(fDao.load(key3));
    assertNull(fDao.load(key4));

    deleted = fDao.delete(key2);
    assertTrue(deleted);
    assertNull(fDao.load(key1));
    assertNull(fDao.load(key2));
    assertNull(fDao.load(key3));
    assertNull(fDao.load(key4));
  }

  /**
   * Test the Events for getting Add, Update and Delete Events.
   *
   * @throws Exception
   */
  @Test
  public void testPreferenceEvents() throws Exception {
    PreferenceListener prefListener = null;
    try {
      final String key1 = "key1";
      boolean value1 = true;

      final String key2 = "key2";
      int value2 = 1;

      final String key3 = "key3";
      String value3 = "value";

      final String key4 = "key4";
      String value4[] = new String[] { "1", "2", "3", "4" };

      /* Event Handling */
      final boolean additionEvents[] = new boolean[4];
      final boolean updatedEvents[] = new boolean[4];
      final boolean deletionEvents[] = new boolean[4];

      prefListener = new PreferenceListener() {
        @Override
        public void entitiesAdded(Set<PreferenceEvent> events) {
          assertEquals(1, events.size());
          PreferenceEvent event = events.iterator().next();
          String key = event.getEntity().getKey();
          if (key1.equals(key))
            additionEvents[0] = event.getEntity().getBoolean().booleanValue();
          else if (key2.equals(key))
            additionEvents[1] = event.getEntity().getInteger().intValue() == 1;
          else if (key3.equals(key))
            additionEvents[2] = event.getEntity().getString().equals("value");
          else if (key4.equals(key))
            additionEvents[3] = Arrays.equals(event.getEntity().getStrings(), new String[] { "1", "2", "3", "4" });
        }

        @Override
        public void entitiesUpdated(Set<PreferenceEvent> events) {
          assertEquals(1, events.size());
          PreferenceEvent event = events.iterator().next();
          String key = event.getEntity().getKey();
          if (key1.equals(key))
            updatedEvents[0] = !event.getEntity().getBoolean().booleanValue();
          else if (key2.equals(key))
            updatedEvents[1] = event.getEntity().getInteger().intValue() == 0;
          else if (key3.equals(key))
            updatedEvents[2] = event.getEntity().getString().equals("updated_value");
          else if (key4.equals(key))
            updatedEvents[3] = Arrays.equals(event.getEntity().getStrings(), new String[] { "4", "3", "2", "1" });
        }

        @Override
        public void entitiesDeleted(Set<PreferenceEvent> events) {
          assertEquals(1, events.size());
          PreferenceEvent event = events.iterator().next();
          String key = event.getEntity().getKey();
          if (key1.equals(key))
            deletionEvents[0] = true;
          else if (key2.equals(key))
            deletionEvents[1] = true;
          else if (key3.equals(key))
            deletionEvents[2] = true;
          else if (key4.equals(key))
            deletionEvents[3] = true;
        }
      };
      fDao.addEntityListener(prefListener);

      /* Add some Preferences */
      IPreference pref = fFactory.createPreference(key1);
      pref.putBooleans(value1);
      fDao.save(pref);

      pref = fFactory.createPreference(key2);
      pref.putIntegers(value2);
      fDao.save(pref);

      pref = fFactory.createPreference(key3);
      pref.putStrings(value3);
      fDao.save(pref);

      pref = fFactory.createPreference(key4);
      pref.putStrings(value4);
      fDao.save(pref);

      /* Update some Preferences */
      pref = fDao.loadOrCreate(key1);
      pref.putBooleans(false);
      fDao.save(pref);

      pref = fDao.loadOrCreate(key2);
      pref.putIntegers(0);
      fDao.save(pref);

      pref = fDao.loadOrCreate(key3);
      pref.putStrings("updated_value");
      fDao.save(pref);

      pref = fDao.loadOrCreate(key4);
      pref.putStrings("4", "3", "2", "1");
      fDao.save(pref);

      /* Delete some Preferences */
      fDao.delete(key1);
      fDao.delete(key2);
      fDao.delete(key3);
      fDao.delete(key4);

      /* Asserts */
      for (boolean element : additionEvents)
        assertTrue("Missing Preference Added Event", element);

      for (boolean element : updatedEvents)
        assertTrue("Missing Preference Updated Event", element);

      for (boolean element : deletionEvents)
        assertTrue("Missing Preference Deleted Event", element);
    } finally {
      if (prefListener != null)
        fDao.removeEntityListener(prefListener);
    }
  }

  /**
   * Save a single-entry String-Array.
   *
   * @throws Exception
   */
  @Test
  public void testSaveSingleEntryStringArray() throws Exception {
    IPreference pref = fFactory.createPreference("Foo");
    pref.putStrings("Bar");
    fDao.save(pref);
    fDao.load(pref.getKey()).getStrings();
  }

  /**
   * Save Strings that contain equal values.
   *
   * @throws Exception
   */
  @Test
  public void testSaveStringsDuplicate() throws Exception {
    IPreference pref = fFactory.createPreference("Foo");
    pref.putStrings("1", "2", "3", "1", "2", "3");
    fDao.save(pref);
    pref = fDao.loadOrCreate("Foo");
    pref.putStrings("1", "2", "3", "1", "2", "3");
    fDao.save(pref);
  }

  /**
   * Save an array of strings with duplicate elements.
   *
   * @throws Exception
   */
  @Test
  public void testSaveArrayWithDuplicateStrings() throws Exception {
    IPreference pref = fFactory.createPreference("Foo");
    pref.putStrings("1", "2", "3", "1", "2", "3");
    fDao.save(pref);
  }

  /**
   * Saves an array and then updates it.
   * @throws Exception
   */
  @Test
  public void testUpdateArray() throws Exception {
    String key = "Foo";
    IPreference pref = fFactory.createPreference(key);
    pref.putStrings("1", "2", "3", "1", "2", "3");
    fDao.save(pref);
    String[] updatedStrings = new String[] { "1", "3", "2" };
    pref.putStrings(updatedStrings);
    fDao.save(pref);
    String[] savedStrings = fDao.load(key).getStrings();
    assertArrayEquals(updatedStrings, savedStrings);
  }
}
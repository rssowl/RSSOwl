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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.rssowl.core.internal.persist.LongArrayList;

/**
 * Unit tests for LongArrayList.
 */
public class LongArrayListTest {

  /**
   * Tests copy constructor.
   */
  @Test
  public void testCopyConstructor() {
    LongArrayList list = new LongArrayList(10);
    list.add(0);
    list.add(1);
    list.add(2);
    assertEquals(3, list.size());
    assertEquals(0, list.get(0));
    assertEquals(1, list.get(1));
    assertEquals(2, list.get(2));

    LongArrayList copy = new LongArrayList(list);
    assertEquals(list.size(), copy.size());
    assertEquals(list, copy);
  }

  /**
   * Tests that passing a negative to get throws appropriate exception.
   */
  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetNegative() {
    LongArrayList list = new LongArrayList(10);
    list.add(0);
    list.add(1);
    list.add(2);
    list.get(-1);
  }

  /**
   * Tests that trying to retrieve an index that is not in the list throws the
   * appropriate exception.
   */
  @Test(expected = IndexOutOfBoundsException.class)
  public void testOutOfBounds() {
    LongArrayList list = new LongArrayList(10);
    list.add(0);
    list.add(1);
    list.add(2);
    list.get(3);
  }

  /**
   * Test some more API of LongArrayList.
   */
  @Test
  public void testApi() {
    LongArrayList list = new LongArrayList(10);
    list.add(0);
    list.add(1);
    list.add(2);

    assertTrue(list.elementsEqual(new long[] { 0, 1, 2 }));
    assertFalse(list.elementsEqual(new long[] { 1, 6, 2 }));

    assertEquals(2, list.lastIndexOf(2));

    list.removeByIndex(2);
    assertEquals(2, list.size());
    assertFalse(list.isEmpty());
  }
}
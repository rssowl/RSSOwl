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

package org.rssowl.core.util;

/**
 * Utility methods for Arrays.
 *
 * @author bpasero
 */
public final class ArrayUtils {

  /* This utility class constructor is hidden */
  private ArrayUtils() {
  // Protect default constructor
  }

  /**
   * @param a the array to search in
   * @param key the key to search for
   * @param endIndex the limit for the search
   * @return the index of the key to search for
   */
  public static int binarySearch(long[] a, long key, int endIndex) {
    int low = 0;
    int high = endIndex - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      long midVal = a[mid];

      if (midVal < key)
        low = mid + 1;
      else if (midVal > key)
        high = mid - 1;
      else
        return mid;
    }
    return -(low + 1);
  }

  /**
   * @param array the array to reverse
   * @param size the size of the array
   */
  public static void reverse(int[] array, int size) {
    for (int left = 0, right = size - 1; left < right; left++, right--) {
      int temp = array[left];
      array[left] = array[right];
      array[right] = temp;
    }
  }

  /**
   * @param array the array to ensure a capacity for
   * @param minCapacity the minimum capacity for the array
   * @return a new array with minCapacity length
   */
  public static int[] ensureCapacity(int[] array, int minCapacity) {
    int oldCapacity = array.length;
    if (minCapacity > oldCapacity) {
      int[] oldData = array;
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity)
        newCapacity = minCapacity;
      // minCapacity is usually close to size, so this is a win:
      int[] copy = new int[newCapacity];
      System.arraycopy(array, 0, copy, 0, Math.min(oldData.length, newCapacity));
      return copy;
    }
    return array;
  }

  /**
   * @param array the array to ensure a capacity for
   * @param minCapacity the minimum capacity for the array
   * @return a new array with minCapacity length
   */
  public static long[] ensureCapacity(long[] array, int minCapacity) {
    int oldCapacity = array.length;
    if (minCapacity > oldCapacity) {
      long[] oldData = array;
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity)
        newCapacity = minCapacity;
      // minCapacity is usually close to size, so this is a win:
      long[] copy = new long[newCapacity];
      System.arraycopy(array, 0, copy, 0, Math.min(oldData.length, newCapacity));
      return copy;
    }
    return array;
  }
}
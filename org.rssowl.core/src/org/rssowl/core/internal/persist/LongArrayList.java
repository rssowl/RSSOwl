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

package org.rssowl.core.internal.persist;

import org.rssowl.core.util.ArrayUtils;

import java.util.Arrays;

/**
 * A List providing access to an array of {@link Long}.
 */
public class LongArrayList {
  protected long[] fElements;
  protected int fSize;

  /**
   * Provided for deserialization.
   */
  protected LongArrayList() {
    super();
  }

  public LongArrayList(int initialCapacity) {
    this.fElements = new long[initialCapacity];
  }

  public LongArrayList(LongArrayList list) {
    this.fElements = new long[list.size()];
    System.arraycopy(list.fElements, 0, fElements, 0, list.fSize);
    fSize = list.fSize;
  }

  /**
   * @return the number of elements in this list.
   */
  public final int size() {
    return fSize;
  }

  /**
   * @param index the index of the element to return.
   * @return the element at the given index.
   */
  public final long get(int index) {
    rangeCheck(index);
    return fElements[index];
  }

  /**
   * @return the underlying array with all elements of this list.
   */
  public final long[] getElements() {
    return fElements;
  }

  /**
   * @param element the long value to find the index for
   * @return the index of the long value or -1 if none.
   */
  public int indexOf(long element) {
    for (int i = 0; i < fSize; ++i) {
      if (fElements[i] == element)
        return i;
    }

    return -1;
  }

  /**
   * @param element the long value to find the last index for.
   * @return the last index of the long value or -1 if none.
   */
  public int lastIndexOf(long element) {
    for (int i = fSize - 1; i >= 0; --i) {
      if (fElements[i] == element)
        return i;
    }

    return -1;
  }

  /**
   * @param element the long value to remove
   * @return <code>true</code> in case the element was actually contained in
   * this list and <code>false</code> otherwise.
   */
  public final boolean removeByElement(long element) {
    int index = indexOf(element);
    if (index >= 0) {
      remove(index);
      return true;
    }

    return false;
  }

  private void remove(int index) {
    int movedCount = fSize - 1 - index;

    if (movedCount > 0)
      System.arraycopy(fElements, index + 1, fElements, index, movedCount);

    fElements[--fSize] = 0L;
  }

  /**
   * @param index the index of the element to remove
   * @return the value of the element from the provided index.
   */
  public final long removeByIndex(int index) {
    long oldValue = get(index);
    remove(index);
    return oldValue;
  }

  /**
   * @param elements the array of values to set to this list.
   */
  public void setAll(long[] elements) {
    fSize = elements.length;
    if (fElements.length > elements.length)
      Arrays.fill(fElements, fSize, fElements.length, 0);
    else if (fElements.length < elements.length)
      fElements = new long[elements.length];

    System.arraycopy(elements, 0, fElements, 0, elements.length);
  }

  /**
   * @param other an array of long values
   * @return <code>true</code> if the long values are identical with this list
   * and <code>false</code> otherwise.
   */
  public final boolean elementsEqual(long[] other) {
    if (fSize != other.length)
      return false;

    for (int i = 0, c = fSize; i < c; i++)
      if (fElements[i] != other[i])
        return false;

    return true;
  }

  private void rangeCheck(int index) {
    if (index >= fSize)
      throw new IndexOutOfBoundsException("size: " + fSize + ", index: " + index); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @param element the long value to search for
   * @return <code>true</code> in case this list contains the given value and
   * <code>false</code> otherwise.
   */
  public final boolean contains(long element) {
    return indexOf(element) >= 0;
  }

  /**
   * @param element the element to add into this list.
   */
  public void add(long element) {
    fElements = ArrayUtils.ensureCapacity(fElements, fSize + 1);
    fElements[fSize++] = element;
  }

  /**
   * Removes all unassigned values from this list.
   */
  public final void compact() {
    long[] compacted = new long[fSize];
    System.arraycopy(fElements, 0, compacted, 0, fSize);
    fElements = compacted;
  }

  /**
   * Removes all entries from this list.
   */
  public final void clear() {
    fSize = 0;
    fElements = new long[0];
  }

  /**
   * @param list the {@link LongArrayList} with values to remove from this list.
   */
  public void removeAll(LongArrayList list) { //TODO This can be slow, try to optimize
    for (long element : list.fElements)
      removeByElement(element);
  }

  /**
   * @return the list of long values as array.
   */
  public long[] toArray() {
    long[] copy = new long[fSize];
    System.arraycopy(fElements, 0, copy, 0, fSize);
    return copy;
  }

  /**
   * @return <code>true</code> if this list is empty and <code>false</code>
   * otherwise.
   */
  public final boolean isEmpty() {
    return fSize == 0;
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    return Arrays.toString(fElements);
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public final int hashCode() {
    int hashCode = 1;
    for (int i = 0; i < fSize; ++i) {
      long element = fElements[i];
      hashCode = 31 * hashCode + ((int) (element ^ (element >>> 32)));
    }
    return hashCode;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public final boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof LongArrayList))
      return false;

    LongArrayList otherList = (LongArrayList) o;

    if (fSize != otherList.fSize)
      return false;

    for (int i = 0, c = fSize; i < c; i++)
      if (fElements[i] != otherList.fElements[i])
        return false;

    return true;
  }
}
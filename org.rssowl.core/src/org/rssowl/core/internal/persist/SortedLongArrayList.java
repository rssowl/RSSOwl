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
 * Subclass of {@link LongArrayList} that sorts its elements.
 */
public final class SortedLongArrayList extends LongArrayList {

  /**
   * Provided for deserialization.
   */
  protected SortedLongArrayList() {
    super();
  }

  public SortedLongArrayList(int initialCapacity) {
    super(initialCapacity);
  }

  /*
   * @see org.rssowl.core.internal.persist.LongArrayList#indexOf(long)
   */
  @Override
  public int indexOf(long element) {
    return ArrayUtils.binarySearch(fElements, element, fSize);
  }

  /*
   * @see org.rssowl.core.internal.persist.LongArrayList#add(long)
   */
  @Override
  public void add(long element) {
    fElements = ArrayUtils.ensureCapacity(fElements, fSize + 1);
    int insertionPoint = indexOf(element);
    if (insertionPoint < 0)
      insertionPoint = (-insertionPoint) - 1;

    System.arraycopy(fElements, insertionPoint, fElements, insertionPoint + 1, fSize - insertionPoint);
    fElements[insertionPoint] = element;
    ++fSize;
  }

  /*
   * @see org.rssowl.core.internal.persist.LongArrayList#setAll(long[])
   */
  @Override
  public void setAll(long[] elements) {
    super.setAll(elements);
    Arrays.sort(fElements, 0, fSize);
  }
}
/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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
package org.rssowl.ui.internal.util;

import java.util.Arrays;

public class ListViewerUtils {

  public static boolean canMoveUp(int[] indices, @SuppressWarnings("unused")
  int total) {
    int count = indices.length;
    return !checkContinious(indices) || indices[count - 1] > count - 1;
  }

  public static boolean canMoveDown(int[] indices, int total) {
    int count = indices.length;
    return !checkContinious(indices) || indices[0] < total - count;
  }

  private static boolean checkContinious(int[] indices) {
    int startIndex = indices[0];
    for (int i = 0; i < indices.length; i++) {
      if (indices[i] != startIndex + i) {
        return false;
      }
    }
    return true;
  }

  /**
   * Move multiple items on one index
   *
   * @param inidicesToMove
   * @param totalItems
   * @param up
   * @return it any items was moved
   */
  public static int[] moveMultipleItems(int[] indicesToMove, int totalItems, boolean up) {
    boolean moved = false;
    int moveItemsCount = indicesToMove.length;
    int[] currentIndex = new int[totalItems];
    for (int i = 0; i < totalItems; i++) {
      currentIndex[i] = i;
    }

    Arrays.sort(indicesToMove);

    if (up) { // moving up
      int previousMoveItemIndex = -1;
      for (int i = 0; i < moveItemsCount; i++) {
        int index = indicesToMove[i];
        if ((index == 0 && i == 0) || index - 1 == previousMoveItemIndex) {
          // check if had preceding item to move and this item in preceding in current index
          // can't move up top element
          previousMoveItemIndex = index;
          continue;
        }
        // move single item
        currentIndex[index] = currentIndex[index - 1]; // move other file down
        previousMoveItemIndex = index - 1;
        currentIndex[index - 1] = index; // move item up
        moved = true;
      }
    } else { // moving down
      int previousMoveItemIndex = totalItems;
      for (int i = moveItemsCount - 1; i >= 0; i--) {
        int index = indicesToMove[i];
        if ((index == totalItems - 1 && i == moveItemsCount - 1) || index + 1 == previousMoveItemIndex) {
          // check if had preceding item to move and this item in preceding in current index
          // can't move up top element
          previousMoveItemIndex = index;
          continue;
        }
        // move single item
        currentIndex[index] = currentIndex[index + 1]; // move other file up
        previousMoveItemIndex = index + 1;
        currentIndex[index + 1] = index; // move item up
        moved = true;
      }

    }

    return moved ? currentIndex : null;
  }

}

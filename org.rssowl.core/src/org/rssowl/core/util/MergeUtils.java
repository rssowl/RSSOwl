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

import org.rssowl.core.internal.persist.ComplexMergeResult;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.MergeCapable;
import org.rssowl.core.persist.Reparentable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some useful methods for dealing with MergeCapable objects.,
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public class MergeUtils {

  /* Set of Properties to ignore during merge */
  private static final Set<String> EXCLUDE_PROPERTIES = new HashSet<String>(Arrays.asList(SyncUtils.GOOGLE_MARKED_READ, SyncUtils.GOOGLE_MARKED_UNREAD, SyncUtils.GOOGLE_LABELS));

  /**
   * Helper method that simply does:
   * <code>o1 == null ? o2 == null : o1.equals(o2)</code> This is useful because
   * it handles nulls and it forces {@code o2} to be assignable to {@code o1}.
   *
   * @param <T> The type of the first object being compared.
   * @param <U> The type of the second object being compared.
   * @param o1 First object being compared.
   * @param o2 Second object being compared.
   * @return <code>true</code> if both objects are equal or if both objects are
   * <code>null</code>.
   */
  public static <T, U extends T> boolean equals(T o1, U o2) {
    return o1 == null ? o2 == null : o1.equals(o2);
  }

  /**
   * Merges origin into destination. This is mostly useful because it deals with
   * the case where one of the items is null.
   *
   * @param <T>
   * @param destination
   * @param origin
   * @return an object where the origin was merged into the destination.
   */
  public static final <T extends MergeCapable<T>> ComplexMergeResult<T> merge(T destination, T origin) {
    if (destination == null) {
      ComplexMergeResult<T> mergeResult;
      if (origin == null)
        mergeResult = ComplexMergeResult.create(null);
      else
        mergeResult = ComplexMergeResult.create(origin, true);

      return mergeResult;
    }
    if (origin == null)
      return ComplexMergeResult.create(null, true);

    ComplexMergeResult<T> mergeResult = ComplexMergeResult.create(destination);
    mergeResult.addAll(destination.merge(origin));
    return mergeResult;
  }

  /**
   * Convenience method that calls
   * {@link #merge(List, List, Comparator, IPersistable)} with a Comparator that
   * uses equals().
   *
   * @param <T>
   * @param existingList
   * @param newList
   * @param newParent
   * @return ListMergeResult indicating the results of the merge operation.
   */
  public static final <T extends MergeCapable<T>> ComplexMergeResult<List<T>> merge(List<T> existingList, List<T> newList, IPersistable newParent) {

    return merge(existingList, newList, new Comparator<T>() {
      public int compare(T o1, T o2) {
        if ((o1 == null ? o2 == null : o1.equals(o2))) {
          return 0;
        }
        return -1;
      }
    }, newParent);
  }

  /**
   * Merges the contents of <code>newList</code> into <code>existingList</code>
   * and returns a ListMergeResult indications the operations performed.
   *
   * @param <T>
   * @param existingList
   * @param newList
   * @param comparator
   * @param newParent
   * @return ListMergeResult indicating the results of the merge operation.
   */
  public static final <T extends MergeCapable<T>> ComplexMergeResult<List<T>> merge(List<T> existingList, List<T> newList, Comparator<T> comparator, IPersistable newParent) {
    if ((existingList == null) && (newList == null || newList.isEmpty()))
      return ComplexMergeResult.create(null);

    if (newList == null && existingList != null) {
      existingList.clear();
      return ComplexMergeResult.create(existingList, true);
    }

    /* Defensive copy */
    List<T> newListCopy = new ArrayList<T>(newList);
    if (existingList == null) {
      ComplexMergeResult<List<T>> mergeResult = ComplexMergeResult.create(newListCopy, true);
      for (T item : newListCopy) {
        reparent(item, newParent);
        mergeResult.addUpdatedObject(item);
      }
      return mergeResult;
    }

    ComplexMergeResult<List<T>> mergeResult = ComplexMergeResult.create(existingList);
    Iterator<T> existingIt = existingList.iterator();
    while (existingIt.hasNext()) {
      T existingItem = existingIt.next();
      boolean matchFound = false;
      for (Iterator<T> newItemsIt = newListCopy.iterator(); newItemsIt.hasNext();) {
        T newItem = newItemsIt.next();

        /*
         * If the existing List already has an item that matches this new item,
         * then remove it from the List of new items, set the matchFound flag
         * and break from the loop.
         */
        if (comparator.compare(newItem, existingItem) == 0) {
          newItemsIt.remove();
          mergeResult.addAll(existingItem.merge(newItem));
          matchFound = true;
          break;
        }
      }

      /*
       * If this existing item does not match any item in the newItems List then
       * remove it from the existing items List.
       */
      if (!matchFound) {
        mergeResult.setStructuralChange(true);
        mergeResult.addRemovedObject(existingItem);
        existingIt.remove();
      }
    }

    /*
     * Add all the items left in newList to the existing container and change
     * parent to new parent, if necessary
     */
    for (T item : newListCopy) {
      reparent(item, newParent);
      existingList.add(item);
      mergeResult.addUpdatedObject(item);
      mergeResult.setStructuralChange(true);
    }

    return mergeResult;
  }

  @SuppressWarnings("unchecked")
  private static void reparent(Object object, IPersistable newParent) {
    if (newParent == null)
      return;

    if (object instanceof Reparentable) {
      ((Reparentable<IPersistable>) object).setParent(newParent);
    } else {
      throw new IllegalArgumentException("if newParent is non-null, the elements " + //$NON-NLS-1$
          "of the list must implement the IReparentable interface."); //$NON-NLS-1$
    }
  }

  /**
   * Merges the properties of the newType into the existingType.
   *
   * @param existingType
   * @param newType
   * @return ListMergeResult indicating the operations performed as part of the
   * merge.
   */
  public static final ComplexMergeResult<?> mergeProperties(IEntity existingType, IEntity newType) {
    ComplexMergeResult<Object> mergeResult = ComplexMergeResult.create(null);
    Map<String, Serializable> existingProperties = existingType.getProperties();
    Map<String, Serializable> newProperties = newType.getProperties();

    /* Add / Update Properties from New Type */
    for (Map.Entry<String, Serializable> entry : newProperties.entrySet()) {
      String key = entry.getKey();
      if (EXCLUDE_PROPERTIES.contains(key))
        continue;

      Serializable value = entry.getValue();
      Serializable existingValue = existingProperties.get(key);
      if (!value.equals(existingValue)) {
        if (existingValue != null)
          mergeResult.addRemovedObject(existingValue);

        existingType.setProperty(key, value);
        mergeResult.setStructuralChange(true);
      }
    }

    /* Remove Properties from Old Type */
    List<Map.Entry<String, ?>> entries = new ArrayList<Map.Entry<String, ?>>(existingProperties.entrySet());
    for (Map.Entry<String, ?> entry : entries) {
      String key = entry.getKey();
      if (EXCLUDE_PROPERTIES.contains(key))
        continue;

      Object value = newProperties.get(key);
      if (value == null) {
        existingType.removeProperty(key);
        mergeResult.addRemovedObject(entry.getValue());
        mergeResult.setStructuralChange(true);
      }
    }
    return mergeResult;
  }
}
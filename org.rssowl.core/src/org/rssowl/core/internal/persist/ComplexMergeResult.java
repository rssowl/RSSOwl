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

import org.rssowl.core.persist.MergeCapable;

/**
 * Contains the details of a complex merge operation. Examples of a complex
 * merge operation are one involving two List of items or two Maps.
 *
 * This object is not thread-safe.
 *
 * @param <T> The type of the merged object.
 * @see MergeCapable
 * @see MergeResult
 */
public final class ComplexMergeResult<T> extends MergeResult    {
  private boolean fStructuralChange;
  private final T fMergedObject;

  /**
   * Creates an instance of this object and returns it.
   *
   * @param <T> The type of the merged object.
   * @param mergedObject The merged object.
   * @return an instance of this class.
   */
  public static <T> ComplexMergeResult<T> create(T mergedObject) {
    return new ComplexMergeResult<T>(mergedObject);
  }

  /**
   * Creates an instance of this object and returns it.
   *
   * @param <T> The type of the merged object.
   * @param mergedObject The merged object.
   * @param structuralChange Whether a structuralChange took place as a result
   * of the merge.
   * @return an instance of this class.
   */
  public static <T> ComplexMergeResult<T> create(T mergedObject, boolean structuralChange) {
    ComplexMergeResult<T> mergeResult = create(mergedObject);
    mergeResult.setStructuralChange(structuralChange);
    return mergeResult;
  }

  private ComplexMergeResult(T result) {
    fMergedObject = result;
  }

  /**
   * @return <code>true</code> if there was a structural change as part of the
   * merge. An example of a structural change is an item being removed from,
   * added to or moved in a list.
   */
  public final boolean isStructuralChange() {
    return fStructuralChange;
  }

  /**
   * Sets the structuralChange property.
   *
   * @param structuralChange Value of the structuralChange property.
   */
  public final void setStructuralChange(boolean structuralChange) {
    fStructuralChange = structuralChange;
  }

  /**
   * @return the merged object.
   */
  public final T getMergedObject() {
    return fMergedObject;
  }
}
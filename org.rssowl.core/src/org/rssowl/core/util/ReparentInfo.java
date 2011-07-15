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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.dao.IFolderDAO;

/**
 * An object that holds information regarding a reparenting operation.
 *
 * @param <T> The type of the object that will have its parent changed.
 * @param
 * <P>
 * The type of the parent.
 * @see IFolderDAO#reparent(java.util.List)
 */
public class ReparentInfo<T, P> {
  private final T object;
  private final P newParent;
  private final T newPosition;
  private final Boolean after;

  /**
   * Creates an instance of this object and return it. This is preferable to the
   * constructor because it performs type inferencing, removing some
   * duplication.
   *
   * @param <T> The type of the object that will have its parent changed.
   * @param
   * <P>
   * The type of the parent.
   * @param object Non-null object to be reparented.
   * @param newParent Non-null new parent of <code>object</code>.
   * @param newPosition Neighbour of <code>object</code> in
   * <code>newParent</code>. May be <code>null</code>.
   * @param after Whether <code>object</code> will be before or after <code>
   * newPosition</code>
   * in <code>newParent</code>. Has to be <code>null</code> if
   * <code>newPosition</code> is <code>null</code> and non-null otherwise.
   * @return ReparentInfo instance.
   */
  public final static <T, P> ReparentInfo<T, P> create(T object, P newParent, T newPosition, Boolean after) {
    return new ReparentInfo<T, P>(object, newParent, newPosition, after);
  }

  /**
   * Creates an instance of this object.
   *
   * @param object Non-null object to be reparented.
   * @param newParent Non-null new parent of <code>object</code>.
   * @param newPosition Neighbour of <code>object</code> in
   * <code>newParent</code>. May be <code>null</code>.
   * @param after Whether <code>object</code> will be before or after <code>
   * newPosition</code>
   * in <code>newParent</code>. Has to be <code>null</code> if
   * <code>newPosition</code> is <code>null</code> and non-null otherwise.
   */
  public ReparentInfo(T object, P newParent, T newPosition, Boolean after) {
    Assert.isNotNull(object, "object cannot be null"); //$NON-NLS-1$
    Assert.isNotNull(newParent, "newParent cannot be null"); //$NON-NLS-1$
    if (newPosition == null)
      Assert.isLegal(after == null, "If position is null, after must also be null."); //$NON-NLS-1$
    else
      Assert.isNotNull(after, "if position is non-null, after must also be non-null"); //$NON-NLS-1$

    this.object = object;
    this.newParent = newParent;
    this.newPosition = newPosition;
    this.after = after;
  }

  /**
   * @return the object that will have its parent changed.
   */
  public final T getObject() {
    return object;
  }

  /**
   * @return the new parent of <code>object</code>.
   */
  public final P getNewParent() {
    return newParent;
  }

  /**
   * @return the neighbour of <code>object</code> in <code>newParent</code>.
   * Together with <code>after</code> determines the actual position of
   * <code>object</code> in <code>newParent</code>. May be
   * <code>null</code>.
   */
  public final T getNewPosition() {
    return newPosition;
  }

  /**
   * @return whether <code>object</code> will be positioned before or after
   * <code>newPosition</code> in <code>newParent</code>. May be
   * <code>null</code>.
   */
  public final Boolean isAfter() {
    return after;
  }
}
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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.MergeCapable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains the details of a merge operation. This can be useful
 * to only perform persistence related operations on the objects that
 * have changed as part of the merge.
 *
 * This object is not thread-safe.
 *
 * @see MergeCapable
 */
public class MergeResult {
  private Set<Object> fRemovedObjects;
  private Set<Object> fUpdatedObjects;

  /**
   * Creates an instance of this object.
   */
  public MergeResult() {
    super();
  }

  /**
   * Returns a set of objects that were removed as part of a merge operation.
   *
   * @return Set of objects.
   */
  public final Set<Object> getRemovedObjects() {
    if (fRemovedObjects == null)
      return Collections.emptySet();

    return Collections.unmodifiableSet(fRemovedObjects);
  }

  /**
   * Returns a set of objects that were added or updated as part of a merge
   * operation.
   *
   * @return Set of objects.
   */
  public final Set<Object> getUpdatedObjects() {
    if (fUpdatedObjects == null)
      return Collections.emptySet();

    return Collections.unmodifiableSet(fUpdatedObjects);
  }

  /**
   * Adds all of the contents of <code>mergeResult</code> into this object.
   *
   * @param mergeResult MergeResult to copy all items from.
   */
  public final void addAll(MergeResult mergeResult) {
    Assert.isNotNull(mergeResult, "mergeResult"); //$NON-NLS-1$
    fRemovedObjects = addAll(fRemovedObjects, mergeResult.getRemovedObjects());
    fUpdatedObjects = addAll(fUpdatedObjects, mergeResult.getUpdatedObjects());
  }

  private Set<Object> addAll(Set<Object> entities, Set<Object> entitiesToAdd) {
    if (entities == null)
      entities = new HashSet<Object>(entitiesToAdd);
    else
      entities.addAll(entitiesToAdd);

    return entities;
  }

  /**
   * Adds an object that was added or updated as part of a merge operation.
   *
   * @param object Object to add.
   */
  public final void addUpdatedObject(Object object) {
    if (getRemovedObjects().contains(object))
      return;

    if (fUpdatedObjects == null)
      fUpdatedObjects = new HashSet<Object>(3);

    fUpdatedObjects.add(object);
  }

  private void checkArgument(Object entity) {
    if (entity instanceof IEntity) {
      Assert.isNotNull(((IEntity) entity).getId(), "entity.getId()"); //$NON-NLS-1$
    }
  }

  /**
   * Adds an object that was removed as part of a merge operation.
   *
   * @param object Object to be added.
   */
  public final void addRemovedObject(Object object) {
    checkArgument(object);
    if (fRemovedObjects == null)
      fRemovedObjects = new HashSet<Object>(3);

    if (getUpdatedObjects().contains(object))
      fUpdatedObjects.remove(object);

    fRemovedObjects.add(object);
  }

  /**
   * @return <code>true</code> if both <code>removedObjects</code> and
   * <code>updatedObjects</code> are empty.
   */
  public final boolean isEmpty() {
    return getRemovedObjects().isEmpty() && getUpdatedObjects().isEmpty();
  }
}
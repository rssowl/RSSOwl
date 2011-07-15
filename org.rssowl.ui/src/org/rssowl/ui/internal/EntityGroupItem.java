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

package org.rssowl.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IEntity;

/**
 * Instances of <code>EntityGroupItem</code> act as a wrapper arround a
 * <code>IEntity</code>. The can be added into
 * <code>EntityGroup</code>s.
 *
 * @author bpasero
 */
public class EntityGroupItem {
  private EntityGroup fGroup;
  private IEntity fEntity;

  /**
   * Creates a new EntityGroupItem with the given EntityGroup as parent and a
   * IEntity to wrap.
   *
   * @param group The EntityGroup this Item is contained.
   * @param entity The instance of <code>IEntity</code> to wrap.
   */
  public EntityGroupItem(EntityGroup group, IEntity entity) {
    Assert.isNotNull(group);
    fGroup = group;
    Assert.isNotNull(entity);
    fEntity = entity;

    /* Link to Parent */
    fGroup.add(this);
  }

  /**
   * @return Returns the EntityGroup this Item is contained in.
   */
  public EntityGroup getGroup() {
    return fGroup;
  }

  /**
   * @return Returns the ModelReference this Item is wrapping.
   */
  public IEntity getEntity() {
    return fEntity;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if ((obj == null) || (obj.getClass() != getClass()))
      return false;

    EntityGroupItem item = (EntityGroupItem) obj;
    return fEntity.equals(item.fEntity);
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return fEntity.hashCode();
  }
}
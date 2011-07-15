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

package org.rssowl.core.persist.event;

import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.event.runnable.EventRunnable;

/**
 * The abstract super-type of all ModelEvents in the Application. This type
 * guarantees that each Event-Object issued for a Model-Type contains the
 * <code>IEntity</code> of the affected type.
 *
 * @author bpasero
 */
public abstract class ModelEvent {
  private final IEntity fEntity;
  private final boolean fIsRoot;

  /**
   * Stores an instance of <code>IEntity</code> for the affected Type in this
   * Event.
   *
   * @param entity An instance of <code>IEntity</code> for the affected Type.
   */
  protected ModelEvent(IEntity entity) {
    this(entity, true);
  }

  /**
   * Stores an instance of <code>IEntity</code> for the affected Type in this
   * Event.
   *
   * @param entity An instance of <code>IEntity</code> for the affected Type.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   */
  protected ModelEvent(IEntity entity, boolean isRoot) {
    fEntity = entity;
    fIsRoot = isRoot;
  }

  /**
   * @return An instance of <code>IEntity</code> for the affected Type.
   */
  public IEntity getEntity() {
    return fEntity;
  }

  /**
   * <p>
   * Some Events, like the import of a Folder filled with BookMarks, result in
   * Events being sent out for each type affected. Some Listeners however,
   * especially tree-based ones, might only be interested in the Event for the
   * Type that contains the others. They can ask the Event if it is the
   * Root-Event and decide wether an update is required.
   * </p>
   * Note: Any Event created is a Root-Event by default.
   *
   * @return Returns <code>TRUE</code> if this Event is the Root-Event,
   * <code>FALSE</code> otherwise.
   */
  public boolean isRoot() {
    return fIsRoot;
  }

  /**
   * Creates a subclass of <code>EventRunnable</code> that can be used to hold
   * events of the same type as this one. This is useful if the event should be
   * fired in the future.
   * <p>
   * As an example, if the method was called on a <code>NewsEvent</code>, a
   * <code>NewsEventRunnable</code> would be created and returned. The same
   * NewsEvent could then be added to the NewsEventRunnable and when
   * appropriate, the event could be fired by calling the
   * {@link EventRunnable#run()} method.
   *
   * @return EventRunnable subclass holding no events.
   */
  public abstract EventRunnable<? extends ModelEvent> createEventRunnable();

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + (fIsRoot ? 1231 : 1237);
    result = PRIME * result + ((fEntity == null) ? 0 : fEntity.hashCode());
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ModelEvent other = (ModelEvent) obj;
    if (fIsRoot != other.fIsRoot)
      return false;
    if (fEntity == null) {
      if (other.fEntity != null)
        return false;
    } else if (!fEntity.equals(other.fEntity))
      return false;
    return true;
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String name = super.toString();
    int index = name.lastIndexOf('.');
    if (index != -1)
      name = name.substring(index + 1, name.length());

    return name + " (Reference = " + fEntity + ", Root Event = " + fIsRoot + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
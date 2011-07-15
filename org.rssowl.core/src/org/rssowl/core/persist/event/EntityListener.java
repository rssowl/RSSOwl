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
import org.rssowl.core.persist.dao.DynamicDAO;

import java.util.Set;

/**
 * Instances of <code>EntityListener</code> can be used to be notified when a
 * certain <code>IEntity</code> is added, updated or deleted. Use the
 * <code>DynamicDAO</code> facade to register listeners.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 * @param <E> A subclass of <code>ModelEvent</code>
 * @param <T> A subclass of <code>IEntity</code>
 * @see DynamicDAO
 */
public interface EntityListener<E extends ModelEvent, T extends IEntity> {

  /**
   * Called when <code>IEntity</code>s have been added.
   *
   * <p>
   * Note that receivers of these events should _not_ update the entities
   * contained in {@code events} in the same thread. This may cause an exception
   * to be thrown or a deadlock to occur. If possible, updating the entities
   * from within handlers should be avoided altogether, but otherwise a separate
   * thread should be used for it.
   * </p>
   *
   * @param events A collection of <code>ModelEvent</code>s describing the
   * added entities.
   */
  public void entitiesAdded(Set<E> events);

  /**
   * Called when <code>IEntity</code>s have been deleted.
   *
   * <p>
   * Note that receivers of these events should _not_ update the entities
   * contained in {@code events} in the same thread. This may cause an exception
   * to be thrown or a deadlock to occur. If possible, updating the entities
   * from within handlers should be avoided altogether, but otherwise a separate
   * thread should be used for it.
   * </p>
   *
   * @param events A collection of <code>ModelEvent</code>s describing the
   * deleted entities.
   */
  public void entitiesDeleted(Set<E> events);

  /**
   * Called when <code>IEntity</code>s have been updated.
   *
   * <p>
   * Note that receivers of these events should _not_ update the entities
   * contained in {@code events} in the same thread. This may cause an exception
   * to be thrown or a deadlock to occur. If possible, updating the entities
   * from within handlers should be avoided altogether, but otherwise a separate
   * thread should be used for it.
   * </p>
   *
   * @param events A collection of <code>ModelEvent</code>s describing the
   * updated entities.
   */
  public void entitiesUpdated(Set<E> events);
}
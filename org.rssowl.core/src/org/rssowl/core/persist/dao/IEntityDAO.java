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

package org.rssowl.core.persist.dao;

import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.event.EntityListener;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.event.runnable.EventType;
import org.rssowl.core.persist.service.PersistenceException;

import java.util.Set;

/**
 * The <code>IEntityDAO</code> is the base interface for all entity data
 * access objects. It provides methods to register and remove entity-listeners
 * as well as a method to notify them.
 *
 * @param <T> A subclass of <code>IEntity</code>
 * @param <L> A subclass of <code>EntityListener</code>
 * @param <E> A subclass of <code>ModelEvent</code>
 */
public interface IEntityDAO<T extends IEntity, L extends EntityListener<E, T>, E extends ModelEvent> extends IPersistableDAO<T> {

  /**
   * Returns <code>true</code> if there's an entity with <code>id</code>
   * in the persistence system. Returns <code>false</code> otherwise.
   *
   * @param id The id to be checked.
   * @return <code>true</code> if there's an entity with <code>id</code>
   * in the persistence system. Returns <code>false</code> otherwise.
   * @throws PersistenceException In case of an error while accessing the
   * persistence system.
   */
  boolean exists(long id) throws PersistenceException;

  /**
   * Loads the persistable with <code>id</code> from the persistence system
   * and returns it. If no persistable with the provided id exists,
   * <code>null</code> is returned.
   *
   * @param id The id of the persistable to load from the persistence system.
   * @return the persistable with <code>id</code> or <code>null</code> in
   * case none exists.
   * @throws PersistenceException In case of an error while loading the
   * persistable.
   */
  T load(long id) throws PersistenceException;

  /**
   * Adds a listener to the collection of listeners who will be notified
   * whenever entities of type <code>T extends IEntity</code> get added,
   * updated or removed.
   *
   * @param listener The listener that will be added to the collection of
   * listeners for the given Entity.
   */
  public void addEntityListener(L listener);

  /**
   * Removes a listener from the collection of listeners who will be notified
   * whenever entities of type <code>T extends IEntity</code> get added,
   * updated or removed.
   *
   * @param listener The listener that will be removed from the collection of
   * listeners for the given Entity.
   */
  public void removeEntityListener(L listener);

  /**
   * Notifies all listeners that listen on the given type of event that one of
   * the <code>EventType</code> has occured.
   *
   * @param events A Set of Events that describe the given event type.
   * @param eventType One of the supported event types (add, update or remove).
   */
  public void fireEvents(Set<E> events, EventType eventType);
}
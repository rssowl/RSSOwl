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

package org.rssowl.core.persist.event.runnable;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.persist.dao.IEntityDAO;
import org.rssowl.core.persist.event.ModelEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for runnables that fire events. These runnables are useful when
 * one wants to specify a Set of events to be fired in the future. Another
 * possible use is to fire the events in an event loop.
 *
 * @param <T> A subclass of ModelEvent that establishes the event type that this
 * class represents.
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public abstract class EventRunnable<T extends ModelEvent> implements Runnable {
  private Set<T> fPersistEvents;
  private Set<T> fRemoveEvents;
  private Set<T> fUpdateEvents;
  private final Class<T> fEventClass;
  private final IEntityDAO<?, ?, T> fEntityDAO;

  /**
   * Creates an instance of this class.
   *
   * @param eventClass The subclass of ModelEvent represented by this runnable.
   * @param entityDAO The entityDAO to be used for firing the events in this
   * runnable.
   */
  protected EventRunnable(Class<T> eventClass, IEntityDAO<?, ?, T> entityDAO) {
    Assert.isNotNull(eventClass, "eventClass"); //$NON-NLS-1$
    Assert.isNotNull(entityDAO, "entityDAO"); //$NON-NLS-1$
    fEventClass = eventClass;
    fEntityDAO = entityDAO;
  }

  /**
   * @return the current {@link DAOService} instance.
   */
  protected static final DAOService getDAOService() {
    return Owl.getPersistenceService().getDAOService();
  }

  /**
   * @return {@code true} is there are any events stored in this runnable.
   */
  public boolean isEmpty() {
    return isEmpty(fPersistEvents) && isEmpty(fRemoveEvents) && isEmpty(fUpdateEvents);
  }

  private boolean isEmpty(Set<?> set) {
    return set == null || set.isEmpty();
  }

  /**
   * Fires the event type defined by the eventType property appropriate to T.
   */
  public final void run() {
    if (shouldFirePersistEvents())
      fireEvents(fPersistEvents, EventType.PERSIST);

    if (shouldFireRemoveEvents())
      fireEvents(fRemoveEvents, EventType.REMOVE);

    if (shouldFireUpdateEvents())
      fireEvents(fUpdateEvents, EventType.UPDATE);
  }

  private void fireEvents(Set<T> persistEvents, EventType eventType) {
    fEntityDAO.fireEvents(Collections.unmodifiableSet(persistEvents), eventType);
  }

  /**
   * Checks if {@code event} is an instance of {@code T} and if so calls
   * {@code addPersistEvent}.
   *
   * @param event Persist event to be added.
   * @throws IllegalArgumentException if {@code event} is not an instance of
   * {@code T}.
   */
  @SuppressWarnings("unchecked")
  public final void addCheckedPersistEvent(ModelEvent event) {
    checkEventType(getEventClass(), event);
    addPersistEvent((T) event);
  }

  private Class<? extends ModelEvent> getEventClass() {
    return fEventClass;
  }

  private void checkEventType(Class<?> expectedClass, ModelEvent eventReceived) {
    if (!expectedClass.isInstance(eventReceived))
      throw new IllegalArgumentException("event must be of type: " + //$NON-NLS-1$
          expectedClass + ", but it is of type: " + eventReceived.getClass()); //$NON-NLS-1$
  }

  /**
   * Checks if {@code event} is an instance of {@code T} and if so calls
   * {@code addRemoveEvent}.
   *
   * @param event Remove event to be added.
   * @throws IllegalArgumentException if {@code event} is not an instance of
   * {@code T}.
   */
  @SuppressWarnings("unchecked")
  public final void addCheckedRemoveEvent(ModelEvent event) {
    checkEventType(getEventClass(), event);
    addRemoveEvent((T) event);
  }

  /**
   * Checks if {@code event} is an instance of {@code T} and if so calls
   * {@code addUpdateEvent}.
   *
   * @param event Update event to be added.
   * @throws IllegalArgumentException if {@code event} is not an instance of
   * {@code T}.
   */
  @SuppressWarnings("unchecked")
  public final void addCheckedUpdateEvent(ModelEvent event) {
    checkEventType(getEventClass(), event);
    addUpdateEvent((T) event);
  }

  /**
   * Adds {@code event} to the list of persist events.
   *
   * @param event Persist event to be added.
   */
  public final void addPersistEvent(T event) {
    if (fPersistEvents == null)
      fPersistEvents = new HashSet<T>(3);

    if (removeEventsContains(event))
      return;

    fPersistEvents.add(event);
  }

  private boolean removeEventsContains(ModelEvent event) {
    return fRemoveEvents != null && fRemoveEvents.contains(event);
  }

  private boolean persistEventsContains(ModelEvent event) {
    return fPersistEvents != null && fPersistEvents.contains(event);
  }

  /**
   * Adds {@code event} to the list of remove events.
   *
   * @param event Remove event to be added.
   */
  public final void addRemoveEvent(T event) {
    if (fRemoveEvents == null)
      fRemoveEvents = new HashSet<T>(3);

    if (fUpdateEvents != null)
      fUpdateEvents.remove(event);
    if (fPersistEvents != null)
      fPersistEvents.remove(event);

    fRemoveEvents.add(event);
  }

  /**
   * Adds {@code event} to the list of update events.
   *
   * @param event Update event to be added.
   */
  public final void addUpdateEvent(T event) {
    if (fUpdateEvents == null)
      fUpdateEvents = new HashSet<T>(3);

    if (removeEventsContains(event) || persistEventsContains(event))
      return;

    fUpdateEvents.add(event);
  }

  /**
   * Returns a list of all events stored in this runnable.
   *
   * @return a list of all events stored in this runnable.
   */
  public final List<T> getAllEvents() {
    List<T> allEvents = new ArrayList<T>(getPersistEvents().size() + getRemoveEvents().size() + getUpdateEvents().size());
    allEvents.addAll(getPersistEvents());
    allEvents.addAll(getRemoveEvents());
    allEvents.addAll(getUpdateEvents());
    return allEvents;
  }

  private boolean shouldFirePersistEvents() {
    return (fPersistEvents != null) && (fPersistEvents.size() > 0);
  }

  private boolean shouldFireUpdateEvents() {
    return (fUpdateEvents != null) && (fUpdateEvents.size() > 0);
  }

  private boolean shouldFireRemoveEvents() {
    return (fRemoveEvents != null) && (fRemoveEvents.size() > 0);
  }

  /**
   * Returns an unmodifiable list containing the persist events stored in this
   * runnable.
   *
   * @return an unmodifiable list of persist events.
   */
  public final Set<T> getPersistEvents() {
    if (fPersistEvents == null)
      return Collections.emptySet();

    return Collections.unmodifiableSet(fPersistEvents);
  }

  /**
   * Returns an unmodifiable list containing the remove events stored in this
   * runnable.
   *
   * @return an unmodifiable list of remove events.
   */
  public final Set<T> getRemoveEvents() {
    if (fRemoveEvents == null)
      return Collections.emptySet();

    return Collections.unmodifiableSet(fRemoveEvents);
  }

  /**
   * Returns an unmodifiable list containing the update events stored in this
   * runnable.
   *
   * @return an unmodifiable list of update events.
   */
  public final Set<T> getUpdateEvents() {
    if (fUpdateEvents == null)
      return Collections.emptySet();

    return Collections.unmodifiableSet(fUpdateEvents);
  }
}
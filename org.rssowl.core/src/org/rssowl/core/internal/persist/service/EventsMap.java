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

package org.rssowl.core.internal.persist.service;

import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.event.runnable.EventRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link Map} of {@link ModelEvent} pointing to {@link EventRunnable}.
 */
public class EventsMap {
  private static final EventsMap INSTANCE = new EventsMap();

  private static class InternalMap extends HashMap<Class<? extends ModelEvent>, EventRunnable<? extends ModelEvent>> {
    InternalMap() {
      super();
    }
  }

  private final ThreadLocal<InternalMap> fEvents = new ThreadLocal<InternalMap>();
  private final ThreadLocal<Map<IEntity, ModelEvent>> fEventTemplatesMap = new ThreadLocal<Map<IEntity, ModelEvent>>();

  private EventsMap() {
    // Enforce singleton pattern
  }

  public final static EventsMap getInstance() {
    return INSTANCE;
  }

  public final void putPersistEvent(ModelEvent event) {
    EventRunnable<? extends ModelEvent> eventRunnable = getEventRunnable(event);
    eventRunnable.addCheckedPersistEvent(event);
  }

  public final void putUpdateEvent(ModelEvent event) {
    EventRunnable<? extends ModelEvent> eventRunnable = getEventRunnable(event);
    eventRunnable.addCheckedUpdateEvent(event);
  }

  public final void putRemoveEvent(ModelEvent event) {
    EventRunnable<? extends ModelEvent> eventRunnable = getEventRunnable(event);
    eventRunnable.addCheckedRemoveEvent(event);
  }

  public final boolean containsPersistEvent(Class<? extends ModelEvent> eventClass, IEntity entity) {
    EventRunnable<? extends ModelEvent> eventRunnable = getEventRunnable(eventClass);
    return eventRunnable.getPersistEvents().contains(entity);
  }

  public final boolean containsUpdateEvent(Class<? extends ModelEvent> eventClass, IEntity entity) {
    EventRunnable<? extends ModelEvent> eventRunnable = getEventRunnable(eventClass);
    return eventRunnable.getUpdateEvents().contains(entity);
  }

  public final boolean containsRemoveEvent(Class<? extends ModelEvent> eventClass, IEntity entity) {
    EventRunnable<? extends ModelEvent> eventRunnable = getEventRunnable(eventClass);
    return eventRunnable.getRemoveEvents().contains(entity);
  }

  private EventRunnable<? extends ModelEvent> getEventRunnable(Class<? extends ModelEvent> eventClass) {
    InternalMap map = fEvents.get();
    if (map == null) {
      map = new InternalMap();
      fEvents.set(map);
    }
    EventRunnable<? extends ModelEvent> eventRunnable = map.get(eventClass);
    return eventRunnable;
  }

  private EventRunnable<? extends ModelEvent> getEventRunnable(ModelEvent event) {
    Class<? extends ModelEvent> eventClass = event.getClass();
    EventRunnable<? extends ModelEvent> eventRunnable = getEventRunnable(eventClass);
    if (eventRunnable == null) {
      eventRunnable = event.createEventRunnable();
      fEvents.get().put(eventClass, eventRunnable);
    }
    return eventRunnable;
  }

  public EventRunnable<? extends ModelEvent> removeEventRunnable(Class<? extends ModelEvent> klass) {
    InternalMap map = fEvents.get();
    if (map == null)
      return null;

    EventRunnable<? extends ModelEvent> runnable = map.remove(klass);
    return runnable;
  }

  public List<EventRunnable<?>> getEventRunnables() {
    InternalMap map = fEvents.get();
    if (map == null)
      return new ArrayList<EventRunnable<?>>(0);

    List<EventRunnable<?>> eventRunnables = new ArrayList<EventRunnable<?>>(map.size());
    for (Map.Entry<Class<? extends ModelEvent>, EventRunnable<? extends ModelEvent>> entry : map.entrySet()) {
      eventRunnables.add(entry.getValue());
    }
    return eventRunnables;
  }

  public List<EventRunnable<?>> removeEventRunnables() {
    InternalMap map = fEvents.get();
    if (map == null)
      return new ArrayList<EventRunnable<?>>(0);

    List<EventRunnable<?>> eventRunnables = getEventRunnables();
    map.clear();
    return eventRunnables;
  }

  public void putEventTemplate(ModelEvent event) {
    Map<IEntity, ModelEvent> map = fEventTemplatesMap.get();
    if (map == null) {
      map = new IdentityHashMap<IEntity, ModelEvent>();
      fEventTemplatesMap.set(map);
    }
    map.put(event.getEntity(), event);
  }

  public final Map<IEntity, ModelEvent> getEventTemplatesMap() {
    Map<IEntity, ModelEvent> map = fEventTemplatesMap.get();
    if (map == null)
      return Collections.emptyMap();

    return Collections.unmodifiableMap(fEventTemplatesMap.get());
  }

  public Map<IEntity, ModelEvent> removeEventTemplatesMap() {
    Map<IEntity, ModelEvent> map = fEventTemplatesMap.get();
    fEventTemplatesMap.remove();
    return map;
  }
}
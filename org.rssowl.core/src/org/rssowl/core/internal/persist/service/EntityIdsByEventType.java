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

import org.rssowl.core.internal.persist.LongArrayList;
import org.rssowl.core.internal.persist.Persistable;
import org.rssowl.core.internal.persist.SortedLongArrayList;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.reference.NewsReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Maps entity identifiers to event types.
 */
public final class EntityIdsByEventType extends Persistable implements IPersistable {
  private static final int DEFAULT_CAPACITY = 5;

  private LongArrayList fPersistedEntities;
  private LongArrayList fUpdatedEntities;
  private LongArrayList fRemovedEntities;

  /**
   * Provided for deserialization.
   */
  public EntityIdsByEventType() {
    super();
  }

  public EntityIdsByEventType(EntityIdsByEventType o) {
    fPersistedEntities = new LongArrayList(o.fPersistedEntities);
    fUpdatedEntities = new LongArrayList(o.fUpdatedEntities);
    fRemovedEntities = new LongArrayList(o.fRemovedEntities);
  }

  public EntityIdsByEventType(boolean sorted) {
    if (sorted) {
      fPersistedEntities = new SortedLongArrayList(DEFAULT_CAPACITY);
      fUpdatedEntities = new SortedLongArrayList(DEFAULT_CAPACITY);
      fRemovedEntities = new SortedLongArrayList(DEFAULT_CAPACITY);
    } else {
      fPersistedEntities = new LongArrayList(DEFAULT_CAPACITY);
      fUpdatedEntities = new LongArrayList(DEFAULT_CAPACITY);
      fRemovedEntities = new LongArrayList(DEFAULT_CAPACITY);
    }
  }

  public synchronized <T extends ModelEvent> void addAllEntities(Collection<T> persistedEntity, Collection<T> updatedEntity, Collection<T> removedEntity) {
    addAllEntities(fPersistedEntities, persistedEntity);
    addAllEntities(fUpdatedEntities, updatedEntity);
    addAllEntities(fRemovedEntities, removedEntity);
  }

  private static void addAllEntities(LongArrayList entityIds, Collection<? extends ModelEvent> events) {
    for (ModelEvent event : events)
      entityIds.add(event.getEntity().getId());
  }

  public synchronized void removeAll(Collection<ModelEvent> persistedEntity, Collection<ModelEvent> updatedEntity, Collection<ModelEvent> removedEntity) {
    removeAllEntities(fPersistedEntities, persistedEntity);
    removeAllEntities(fUpdatedEntities, updatedEntity);
    removeAllEntities(fRemovedEntities, removedEntity);
  }

  private static void removeAllEntities(LongArrayList entityIds, Collection<ModelEvent> events) {
    for (ModelEvent event : events)
      entityIds.removeByElement(event.getEntity().getId());
  }

  public synchronized List<NewsReference> getPersistedEntityRefs() {
    return getEntityIds(fPersistedEntities);
  }

  public synchronized List<NewsReference> getUpdatedEntityRefs() {
    return getEntityIds(fUpdatedEntities);
  }

  public synchronized List<NewsReference> getRemovedEntityRefs() {
    return getEntityIds(fRemovedEntities);
  }

  private static List<NewsReference> getEntityIds(LongArrayList list) {
    List<NewsReference> newsRef = new ArrayList<NewsReference>(list.size());
    for (int i = 0, c = list.size(); i < c; ++i) {
      newsRef.add(new NewsReference(list.get(i)));
    }
    return newsRef;
  }

  public synchronized void removeAll(LongArrayList addedEntityIds, LongArrayList updatedEntityIds, LongArrayList removedEntityIds) {
    fPersistedEntities.removeAll(addedEntityIds);
    fUpdatedEntities.removeAll(updatedEntityIds);
    fRemovedEntities.removeAll(removedEntityIds);
  }

  public synchronized void addPersistedEntity(IEntity entity) {
    fPersistedEntities.add(entity.getId());
  }

  public synchronized void addUpdatedEntity(IEntity entity) {
    fUpdatedEntities.add(entity.getId());
  }

  public synchronized void addRemovedEntity(IEntity entity) {
    fRemovedEntities.add(entity.getId());
  }

  public synchronized void addRemovedEntityId(long id) {
    fRemovedEntities.add(id);
  }

  public synchronized LongArrayList getRemovedEntityIds() {
    return fRemovedEntities;
  }

  public synchronized LongArrayList getPersistedEntityIds() {
    return fPersistedEntities;
  }

  public synchronized LongArrayList getUpdatedEntityIds() {
    return fUpdatedEntities;
  }

  public synchronized void clear() {
    fPersistedEntities.clear();
    fUpdatedEntities.clear();
    fRemovedEntities.clear();
  }

  public synchronized void compact() {
    fPersistedEntities.compact();
    fUpdatedEntities.compact();
    fRemovedEntities.compact();
  }

  public synchronized int size() {
    int size = fPersistedEntities.size() + fUpdatedEntities.size() + fRemovedEntities.size();
    return size;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + fPersistedEntities.hashCode();
    result = prime * result + fRemovedEntities.hashCode();
    result = prime * result + fUpdatedEntities.hashCode();
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
    EntityIdsByEventType other = (EntityIdsByEventType) obj;
    return fPersistedEntities.equals(other.fPersistedEntities) && fRemovedEntities.equals(other.fRemovedEntities) && fUpdatedEntities.equals(other.fUpdatedEntities);
  }
}
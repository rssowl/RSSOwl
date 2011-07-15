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

package org.rssowl.core.internal.persist.dao;

import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.DatabaseEvent;
import org.rssowl.core.internal.persist.service.DatabaseListener;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.dao.IEntityDAO;
import org.rssowl.core.persist.event.EntityListener;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.event.runnable.EventType;
import org.rssowl.core.persist.service.PersistenceException;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache that stores all entities of type T in memory. In the future, we could
 * improve it to store only entities that have been requested. We could also add
 * eviction strategies at some point.
 *
 * @param <D>
 * @param <T>
 * @param <L>
 * @param <E>
 */
public abstract class CachingDAO<D extends AbstractEntityDAO<T, L, E>, T extends IEntity, L extends EntityListener<E, T>, E extends ModelEvent> implements IEntityDAO<T, L, E> {
  protected static final boolean USE_LEGACY_CACHE_ACTIVATION = true;

  private final D fDAO;
  private final ConcurrentMap<Long, T> fCache;

  public CachingDAO(D dao) {
    fDAO = dao;
    fDAO.addEntityListener(createEntityListener());
    fCache = new ConcurrentHashMap<Long, T>(16, 0.75f, 1);

    /* Update the Cache based on Database Events */
    DBManager.getDefault().addEntityStoreListener(new DatabaseListener() {
      public void databaseClosed(DatabaseEvent event) {
        onDatabaseClosed(event);
      }

      public void databaseOpened(DatabaseEvent event) {
        onDatabaseOpened(event);
      }
    });
  }

  protected void putAll(Set<E> events) {
    for (E event : events) {
      fCache.put(event.getEntity().getId(), fDAO.getEntityClass().cast(event.getEntity()));
    }
  }

  protected void removeAll(Set<E> events) {
    for (E event : events) {
      fCache.remove(event.getEntity().getId());
    }
  }

  protected void onDatabaseClosed(@SuppressWarnings("unused") DatabaseEvent event) {
    fCache.clear();
  }

  protected void onDatabaseOpened(@SuppressWarnings("unused") DatabaseEvent event) {

    /* Ensure we start with a fresh cache */
    fCache.clear();

    /* Add all from DAO */
    for (T entity : fDAO.loadAll()) {
      fCache.put(entity.getId(), entity);
    }
  }

  /**
   * @return the listener to properly handle updates to the cache.
   */
  protected abstract L createEntityListener();

  /**
   * @return the DAO this cache is wrapping around.
   */
  protected final D getDAO() {
    return fDAO;
  }

  /**
   * @return the cache implementation (Map).
   */
  protected final ConcurrentMap<Long, T> getCache() {
    return fCache;
  }

  /*
   * @see org.rssowl.core.persist.dao.IEntityDAO#exists(long)
   */
  public final boolean exists(long id) throws PersistenceException {
    return fCache.containsKey(id);
  }

  /*
   * @see org.rssowl.core.persist.dao.IEntityDAO#load(long)
   */
  public final T load(long id) throws PersistenceException {
    return fCache.get(id);
  }

  /*
   * @see org.rssowl.core.persist.dao.IPersistableDAO#loadAll()
   */
  public final Collection<T> loadAll() throws PersistenceException {
    return Collections.unmodifiableCollection(fCache.values());
  }

  /*
   * @see org.rssowl.core.persist.dao.IEntityDAO#fireEvents(java.util.Set,
   * org.rssowl.core.persist.event.runnable.EventType)
   */
  public final void fireEvents(Set<E> events, EventType eventType) {
    fDAO.fireEvents(events, eventType);
  }

  /*
   * @see org.rssowl.core.persist.dao.IPersistableDAO#countAll()
   */
  public final long countAll() throws PersistenceException {
    return fCache.size();
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IEntityDAO#addEntityListener(org.rssowl.core
   * .persist.event.EntityListener)
   */
  public final void addEntityListener(L listener) {
    fDAO.addEntityListener(listener);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IEntityDAO#removeEntityListener(org.rssowl.
   * core.persist.event.EntityListener)
   */
  public final void removeEntityListener(L listener) {
    fDAO.removeEntityListener(listener);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IPersistableDAO#delete(org.rssowl.core.persist
   * .IPersistable)
   */
  public final void delete(T persistable) throws PersistenceException {
    fDAO.delete(persistable);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IPersistableDAO#deleteAll(java.util.Collection)
   */
  public final void deleteAll(Collection<T> persistables) throws PersistenceException {
    fDAO.deleteAll(persistables);
  }

  /*
   * @see org.rssowl.core.persist.dao.IPersistableDAO#getEntityClass()
   */
  public final Class<? extends T> getEntityClass() {
    return fDAO.getEntityClass();
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IPersistableDAO#save(org.rssowl.core.persist
   * .IPersistable)
   */
  public final T save(T persistable) throws PersistenceException {
    return fDAO.save(persistable);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IPersistableDAO#saveAll(java.util.Collection)
   */
  public final void saveAll(Collection<T> persistables) throws PersistenceException {
    fDAO.saveAll(persistables);
  }
}
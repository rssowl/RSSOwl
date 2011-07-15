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

import org.rssowl.core.Owl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.event.EntityListener;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.service.PersistenceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convinient access to commonly used methods from the persistence layer.
 */
public final class DynamicDAO {

  /* Singleton Instance */
  private static DAOService DAO_SERVICE;

  private synchronized static final DAOService getDAOService() {
    if (DAO_SERVICE == null)
      DAO_SERVICE = Owl.getPersistenceService().getDAOService();

    return DAO_SERVICE;
  }

  /**
   * Returns <code>true</code> if there's an entity with <code>id</code> and
   * <code>entityClass</code> in the persistence system. Returns
   * <code>false</code> otherwise.
   *
   * @param entityClass The Class of the entity.
   * @param id The id to be checked.
   * @return <code>true</code> if there's an entity with <code>id</code> and
   * <code>entityClass</code> in the persistence system. Returns
   * <code>false</code> otherwise.
   * @throws PersistenceException In case of an error while accessing the
   * persistence system.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>entityClass</code>.
   */
  public static boolean exists(Class<? extends IEntity> entityClass, long id) throws PersistenceException {
    IEntityDAO<?, ?, ?> dao = getDAOFromEntity(entityClass);
    checkEntityDAO(entityClass, dao);
    return dao.exists(id);
  }

  /**
   * Loads the persistable with <code>id</code> and <code>entityClass</code>
   * from the persistence system and returns it. If no persistable that match
   * these conditions exists, <code>null</code> is returned.
   *
   * @param <T> The type of the entity to be loaded.
   * @param entityClass The Class of the entity to be loaded.
   * @param id The id of the persistable to load from the persistence system.
   * @return the persistable with <code>id</code> or <code>null</code> in case
   * none exists.
   * @throws PersistenceException In case of an error while loading the
   * persistable.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>entityClass</code>.
   */
  public static <T extends IEntity> T load(Class<T> entityClass, long id) throws PersistenceException {
    IEntityDAO<T, ?, ?> dao = getDAOFromEntity(entityClass);
    checkEntityDAO(entityClass, dao);
    return dao.load(id);
  }

  /**
   * Loads a <code>Collection</code> of all <code>IPersistable</code>s of type
   * <code>persistableClass</code>.
   *
   * @param <T> The type of IPersistable to be loaded.
   * @param persistableClass The Class of IPersistable to be loaded.
   * @return Returns a <code>Collection</code> of all <code>IPersistable</code>s
   * this DAO of type <code>persistableClass</code>.
   * @throws PersistenceException In case of an error while trying to perform
   * the operation.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>persistableClass</code>.
   */
  public static <T extends IPersistable> Collection<T> loadAll(Class<T> persistableClass) throws PersistenceException {
    IPersistableDAO<T> dao = getDAOFromPersistable(persistableClass);
    checkPersistableDAO(persistableClass, dao);
    return dao.loadAll();
  }

  /**
   * Saves <code>persistable</code> to the persistence system. This method
   * handles new and existing persistables. In other words, it will add or
   * update the persistable as appropriate.
   *
   * @param <T> The type of the persistable to be saved.
   * @param persistable The persistable to save.
   * @return The persistable saved.
   * @throws PersistenceException In case of an error while trying to perform
   * the operation.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>persistable</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IPersistable> T save(T persistable) throws PersistenceException {
    Class<? extends IPersistable> persistableClass = persistable.getClass();
    IPersistableDAO<T> dao = (IPersistableDAO<T>) getDAOFromPersistable(persistableClass);
    checkPersistableDAO(persistableClass, dao);
    return dao.save(persistable);
  }

  /**
   * Saves the given Collection of <code>persistable</code>s to the persistence
   * system in a single operation. This method handles new and existing
   * persistables. In other words, it will add or update the persistable as
   * appropriate.
   *
   * @param <T> The supertype of persistables to be saved.
   * @param persistables The persistables to save.
   * @throws PersistenceException In case of an error while trying to perform
   * the operation.
   * @throws IllegalArgumentException if there is no DAO for any of the
   * persistables in the provided collection.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IPersistable> void saveAll(Collection<T> persistables) throws PersistenceException {
    if (persistables.size() == 0)
      return;

    Map<Class<? extends IPersistable>, List<T>> persistablesMap = getPersistablesMap(persistables);
    for (Map.Entry<Class<? extends IPersistable>, List<T>> entry : persistablesMap.entrySet()) {
      Class<? extends IPersistable> key = entry.getKey();
      IPersistableDAO<T> dao = (IPersistableDAO<T>) getDAOFromPersistable(key);
      checkPersistableDAO(key, dao);
      dao.saveAll(entry.getValue());
    }
  }

  private static <T extends IPersistable> Map<Class<? extends IPersistable>, List<T>> getPersistablesMap(Collection<T> persistables) {
    Map<Class<? extends IPersistable>, List<T>> persistablesMap = new LinkedHashMap<Class<? extends IPersistable>, List<T>>(3);
    for (T persistable : persistables) {
      Class<? extends IPersistable> persistableClass = persistable.getClass();
      List<T> persistableList = persistablesMap.get(persistableClass);
      if (persistableList == null) {
        persistableList = new ArrayList<T>(persistables.size());
        persistablesMap.put(persistableClass, persistableList);
      }
      persistableList.add(persistable);
    }
    return persistablesMap;
  }

  /**
   * Deletes <code>persistable</code> from the persistence system.
   *
   * @param <T> The type of the persistable to delete.
   * @param persistable The persistable to delete.
   * @throws PersistenceException In case of an error while trying to perform
   * the operation.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>entityClass</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IPersistable> void delete(T persistable) throws PersistenceException {
    Class<? extends IPersistable> persistableClass = persistable.getClass();
    IPersistableDAO<T> dao = (IPersistableDAO<T>) getDAOFromPersistable(persistableClass);
    checkPersistableDAO(persistableClass, dao);
    dao.delete(persistable);
  }

  /**
   * Deletes the given Collection of <code>persistable</code>s from the
   * persistence system in a single operation.
   *
   * @param <T> The supertype of the persistables to delete.
   * @param persistables The persistables to delete.
   * @throws PersistenceException In case of an error while trying to perform
   * the operation.
   * @throws IllegalArgumentException if there is no DAO for any of the
   * persistables in the provided collection.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IPersistable> void deleteAll(Collection<T> persistables) throws PersistenceException {
    if (persistables.size() == 0)
      return;

    Map<Class<? extends IPersistable>, List<T>> persistablesMap = getPersistablesMap(persistables);
    for (Map.Entry<Class<? extends IPersistable>, List<T>> entry : persistablesMap.entrySet()) {
      Class<? extends IPersistable> key = entry.getKey();
      IPersistableDAO<T> dao = (IPersistableDAO<T>) getDAOFromPersistable(key);
      checkPersistableDAO(key, dao);
      dao.deleteAll(entry.getValue());
    }
  }

  /**
   * Counts the number of <code>IPersistable</code>s of type
   * <code>persistableClass</code> stored in the persistence layer.
   *
   * @param <T>
   * @param persistableClass
   * @return The number of <code>IPersistable</code>s of type
   * <code>persistableClass</code> stored in the persistence layer.
   * @throws PersistenceException In case of an error while trying to perform
   * the operation.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>persistableClass</code>.
   */
  public static <T extends IPersistable> long countAll(Class<T> persistableClass) throws PersistenceException {
    IPersistableDAO<T> dao = getDAOFromPersistable(persistableClass);
    checkPersistableDAO(persistableClass, dao);
    return dao.countAll();
  }

  private static void checkPersistableDAO(Class<? extends IPersistable> persistableClass, IPersistableDAO<?> dao) {
    if (dao == null)
      throw new IllegalArgumentException("There is no DAO for persistable of type: " + persistableClass); //$NON-NLS-1$
  }

  private static void checkEntityDAO(Class<? extends IPersistable> entityClass, IEntityDAO<?, ?, ?> dao) {
    if (dao == null)
      throw new IllegalArgumentException("There is no DAO for entity of type: " + entityClass); //$NON-NLS-1$
  }

  /**
   * Adds a listener to the collection of listeners who will be notified
   * whenever entities of type <code>T extends IEntity</code> get added, updated
   * or removed.
   *
   * @param <T> The type of the entity.
   * @param <L> The type of the listener.
   * @param <E> The type of the event.
   * @param entityClass The class of the entity.
   * @param listener The listener that will be added to the collection of
   * listeners for the given Entity.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>entityClass</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IEntity, L extends EntityListener<E, T>, E extends ModelEvent> void addEntityListener(Class<T> entityClass, L listener) {
    IEntityDAO<T, L, E> dao = (IEntityDAO<T, L, E>) getDAOFromPersistable(entityClass);
    checkEntityDAO(entityClass, dao);
    dao.addEntityListener(listener);
  }

  /**
   * Removes a listener from the collection of listeners who will be notified
   * whenever entities of type <code>T extends IEntity</code> get added, updated
   * or removed.
   *
   * @param <T> The type of the entity.
   * @param <L> The type of the listener.
   * @param <E> The type of the event.
   * @param entityClass The class of the entity.
   * @param listener The listener that will be removed from the collection of
   * listeners for the given Entity.
   * @throws IllegalArgumentException if there is no DAO for
   * <code>entityClass</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IEntity, L extends EntityListener<E, T>, E extends ModelEvent> void removeEntityListener(Class<T> entityClass, L listener) {
    IEntityDAO<T, L, E> dao = (IEntityDAO<T, L, E>) getDAOFromPersistable(entityClass);
    checkEntityDAO(entityClass, dao);
    dao.removeEntityListener(listener);
  }

  /**
   * Returns the current implementation of the DAO for the specified interface
   * or <code>null</code> if no such DAO exists.
   *
   * @param <D> The subinterface of IPersistableDAO required.
   * @param <T> The type of persistable that the required DAO is responsible
   * for.
   * @param daoInterface The class object of type <code>D</code>.
   * @return The current implementation of the DAO for the specified interface
   * or <code>null</code> if no such DAO exists.
   */
  public static <D extends IPersistableDAO<T>, T extends IPersistable> D getDAO(Class<D> daoInterface) {
    return getDAOService().getDAO(daoInterface);
  }

  /**
   * Returns the DAO responsible for <code>persistableClass</code> or
   * <code>null</code> if no such DAO exists.
   *
   * @param <T> The type of the persistable that the required DAO is responsible
   * for.
   * @param persistableClass A Class instance representing <code>T</code>.
   * @return The DAO responsible for <code>persistableClass</code> or
   * <code>null</code> if no such DAO exists.
   */
  public static <T extends IPersistable> IPersistableDAO<T> getDAOFromPersistable(Class<? extends T> persistableClass) {
    return getDAOService().getDAOFromPersistable(persistableClass);
  }

  /**
   * Returns the DAO responsible for <code>entityClass</code> or
   * <code>null</code> if no such DAO exists.
   *
   * @param <T> The type of the entity that the required DAO is responsible for.
   * @param entityClass A Class instance representing <code>T</code>.
   * @return The DAO responsible for <code>entityClass</code> or
   * <code>null</code> if no such DAO exists.
   */
  public static <T extends IEntity> IEntityDAO<T, ?, ?> getDAOFromEntity(Class<? extends T> entityClass) {
    return getDAOService().getDAOFromPersistable(entityClass);
  }
}
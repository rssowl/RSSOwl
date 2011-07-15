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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.DatabaseEvent;
import org.rssowl.core.internal.persist.service.DatabaseListener;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.dao.IPersistableDAO;
import org.rssowl.core.persist.service.PersistenceException;

import com.db4o.ObjectContainer;
import com.db4o.ext.Db4oException;
import com.db4o.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * The base abstract class for all DAO to access entities from the database and
 * store/update them.
 *
 * @param <T> any {@link IEntity}
 */
public abstract class AbstractPersistableDAO<T extends IPersistable> implements IPersistableDAO<T> {
  protected final Class<? extends T> fEntityClass;
  protected final boolean fSaveFully;

  protected volatile ReadWriteLock fLock;
  protected volatile Lock fWriteLock;
  protected volatile Lock fReadLock;
  protected volatile ObjectContainer fDb;

  public AbstractPersistableDAO(Class<? extends T> entityClass, boolean saveFully) {
    Assert.isNotNull(entityClass, "entityClass"); //$NON-NLS-1$
    fEntityClass = entityClass;
    fSaveFully = saveFully;
    DBManager.getDefault().addEntityStoreListener(new DatabaseListener() {
      public void databaseOpened(DatabaseEvent event) {
        onDatabaseOpened(event);
      }

      public void databaseClosed(DatabaseEvent event) {
        onDatabaseClosed(event);
      }
    });
  }

  /**
   * Called when the database is opened.
   *
   * @param event
   */
  protected void onDatabaseOpened(DatabaseEvent event) {
    fDb = event.getObjectContainer();
    fLock = event.getLock();
    fWriteLock = fLock.writeLock();
    fReadLock = fLock.readLock();
  }

  /**
   * Called when the database is closed.
   *
   * @param event
   */
  protected void onDatabaseClosed(@SuppressWarnings("unused")
  DatabaseEvent event) {
    fDb = null;
  }

  /*
   * @see org.rssowl.core.persist.dao.IPersistableDAO#getEntityClass()
   */
  public final Class<? extends T> getEntityClass() {
    return fEntityClass;
  }

  protected final T getSingleResult(Query query) {
    List<T> result = getList(query);
    int resultSize = result.size();
    if (resultSize == 0)
      return null;

    if (resultSize == 1)
      return result.get(0);

    throw new IllegalStateException("Expected a single result, but got: " + resultSize); //$NON-NLS-1$
  }

  @SuppressWarnings("unchecked")
  protected final List<T> getList(Query query) {
    return query.execute();
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.dao.PersistableDAO#loadAll()
   */
  public Collection<T> loadAll() {
    try {
      List<? extends T> entities = fDb.query(fEntityClass);
      activateAll(entities);

      return new ArrayList<T>(entities);
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }
  }

  protected final <C extends Collection<O>, O> C activateAll(C collection) {
    for (O o : collection)
      fDb.ext().activate(o, Integer.MAX_VALUE);

    return collection;
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.dao.PersistableDAO#save(T)
   */
  public T save(T object) {
    saveAll(Collections.singletonList(object));
    return object;
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.dao.PersistableDAO#saveAll(C)
   */
  public void saveAll(Collection<T> objects) {
    if (objects.isEmpty())
      return;

    try {
      preSaveAll(objects);
      fWriteLock.lock();
      try {
        for (T object : objects) {
          preSave(object);
        }

        for (T object : objects) {
          doSave(object);
        }

        preCommit();
        fDb.commit();
      } catch (Db4oException e) {
        throw DBHelper.rollbackAndPE(fDb, e);
      } finally {
        fWriteLock.unlock();
      }

      DBHelper.cleanUpAndFireEvents();
    } finally {
      postSaveAll(objects);
    }
  }

  protected void preSaveAll(@SuppressWarnings("unused")
  Collection<T> objects) {
    // Do nothing by default
  }

  protected void postSaveAll(@SuppressWarnings("unused")
  Collection<T> objects) {
    // Do nothing by default
  }

  protected void preSave(@SuppressWarnings("unused")
  T persistable) {
    // Do nothing by default
  }

  protected void doSave(T entity) {
    if (fSaveFully)
      fDb.ext().set(entity, Integer.MAX_VALUE);
    else
      fDb.set(entity);
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.dao.PersistableDAO#delete(T)
   */
  public void delete(T object) {
    deleteAll(Collections.singletonList(object));
  }

  protected void doDelete(T entity) {
    fDb.delete(entity);
  }

  /*
   * @see
   * org.rssowl.core.model.internal.db4o.dao.PersistableDAO#deleteAll(java.util
   * .Collection)
   */
  public void deleteAll(Collection<T> objects) {
    if (objects.isEmpty())
      return;

    fWriteLock.lock();
    try {
      for (T object : objects) {
        preDelete(object);
      }

      for (T object : objects) {
        doDelete(object);
      }

      preCommit();
      fDb.commit();
    } catch (Db4oException e) {
      DBHelper.rollbackAndPE(fDb, e);
    } finally {
      fWriteLock.unlock();
    }

    DBHelper.cleanUpAndFireEvents();
  }

  protected void preCommit() {
    DBHelper.preCommit(fDb);
  }

  protected void preDelete(@SuppressWarnings("unused")
  T persistable) {
    // Do nothing by default
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.dao.PersistableDAO#countAll()
   */
  public long countAll() {
    try {
      List<? extends T> entities = fDb.query(fEntityClass);
      return entities.size();
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }
  }
}
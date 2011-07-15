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
import org.rssowl.core.internal.persist.service.DatabaseEvent;
import org.rssowl.core.internal.persist.service.EntityIdsByEventType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A data-access-object for <code>EntityIdsByEventType</code>s.
 */
public class EntitiesToBeIndexedDAOImpl extends AbstractPersistableDAO<EntityIdsByEventType> {

  private volatile EntityIdsByEventType fEntityIds;

  public EntitiesToBeIndexedDAOImpl() {
    super(EntityIdsByEventType.class, true);
  }

  public final EntityIdsByEventType load() {
    return fEntityIds;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#onDatabaseOpened
   * (org.rssowl.core.internal.persist.service.DatabaseEvent)
   */
  @Override
  protected void onDatabaseOpened(DatabaseEvent event) {
    super.onDatabaseOpened(event);
    EntityIdsByEventType entityIds = doLoad();
    if (entityIds == null) {
      entityIds = new EntityIdsByEventType(false);
      save(entityIds);
    }
    fEntityIds = entityIds;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#preCommit()
   */
  @Override
  protected void preCommit() {
    //Do nothing
  }

  private EntityIdsByEventType doLoad() {
    Collection<EntityIdsByEventType> entityIdsCollection = super.loadAll();
    Assert.isLegal(entityIdsCollection.size() <= 1, "There shouldn't be more than 1 EntityIdsByEventType, size: " + entityIdsCollection.size()); //$NON-NLS-1$

    for (EntityIdsByEventType entityIds : entityIdsCollection) {
      return entityIds; //Return the first one since we assert that we don't have more than one
    }

    return null;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#onDatabaseClosed
   * (org.rssowl.core.internal.persist.service.DatabaseEvent)
   */
  @Override
  protected void onDatabaseClosed(DatabaseEvent event) {
    super.onDatabaseClosed(event);
    fEntityIds = null;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#delete(org.
   * rssowl.core.persist.IPersistable)
   */
  @Override
  public final void delete(EntityIdsByEventType newsCounter) {
    if (!newsCounter.equals(load()))
      throw new IllegalArgumentException("Only a single entity should be used. " + "Trying to delete a non-existent one."); //$NON-NLS-1$ //$NON-NLS-2$

    super.delete(newsCounter);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#loadAll()
   */
  @Override
  public Collection<EntityIdsByEventType> loadAll() {
    List<EntityIdsByEventType> newsCounters = new ArrayList<EntityIdsByEventType>(1);
    newsCounters.add(load());

    return newsCounters;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#saveAll(java
   * .util.Collection)
   */
  @Override
  public final void saveAll(Collection<EntityIdsByEventType> entities) {
    if (entities.size() > 1)
      throw new IllegalArgumentException("Only a single entity can be stored"); //$NON-NLS-1$

    super.saveAll(entities);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#doSave(org.
   * rssowl.core.persist.IPersistable)
   */
  @Override
  protected final void doSave(EntityIdsByEventType entity) {
    if (!fDb.ext().isStored(entity) && (load() != null))
      throw new IllegalArgumentException("Only a single entity can be stored"); //$NON-NLS-1$

    super.doSave(entity);
  }
}
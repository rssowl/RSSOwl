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

import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.dao.IFeedDAO;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.FeedListener;
import org.rssowl.core.persist.service.PersistenceException;

import com.db4o.ext.Db4oException;

import java.net.URI;
import java.util.Collection;

/**
 * A data-access-object for <code>IFeed</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class FeedDAOImpl extends AbstractEntityDAO<IFeed, FeedListener, FeedEvent> implements IFeedDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public FeedDAOImpl() {
    super(Feed.class, false);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#doSave(org.
   * rssowl.core.persist.IPersistable)
   */
  @Override
  protected final void doSave(IFeed entity) {
    DBHelper.saveFeed(fDb, entity);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final FeedEvent createDeleteEventTemplate(IFeed entity) {
    return createSaveEventTemplate(entity);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final FeedEvent createSaveEventTemplate(IFeed entity) {
    return new FeedEvent(entity, true);
  }

  /*
   * @see org.rssowl.core.persist.dao.IFeedDAO#load(java.net.URI)
   */
  public final Feed load(URI link) {
    return DBHelper.loadFeed(fDb, link, Integer.MAX_VALUE);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#loadAll()
   */
  @Override
  public final Collection<IFeed> loadAll() {
    try {
      return DBHelper.loadAllFeeds(fDb);
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }
  }

  /*
   * @see org.rssowl.core.persist.dao.IFeedDAO#exists(java.net.URI)
   */
  public boolean exists(URI link) {
    return DBHelper.existsFeed(fDb, link);
  }
}
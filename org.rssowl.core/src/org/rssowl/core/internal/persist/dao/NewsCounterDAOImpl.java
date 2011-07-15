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
import org.rssowl.core.internal.persist.service.DatabaseEvent;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.NewsCounterItem;
import org.rssowl.core.persist.dao.INewsCounterDAO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * A data-access-object for <code>NewsCounter</code>.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class NewsCounterDAOImpl extends AbstractPersistableDAO<NewsCounter> implements INewsCounterDAO {
  private volatile NewsCounter fNewsCounter;

  /** Default constructor using the specific IPersistable for this DAO */
  public NewsCounterDAOImpl() {
    super(NewsCounter.class, true);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#onDatabaseOpened
   * (org.rssowl.core.internal.persist.service.DatabaseEvent)
   */
  @Override
  protected void onDatabaseOpened(DatabaseEvent event) {
    super.onDatabaseOpened(event);
    NewsCounter newsCounter = doLoad();
    if (newsCounter == null) {
      newsCounter = doCountAll();
      save(newsCounter);
    }
    fNewsCounter = newsCounter;
  }

  private NewsCounter doLoad() {
    Collection<NewsCounter> counters = super.loadAll();
    Assert.isLegal(counters.size() <= 1, "There shouldn't be more than 1 NewsCounter, size: " + counters.size()); //$NON-NLS-1$

    for (NewsCounter newsCounter : counters) {
      return newsCounter; //Return the first one since we assert that we don't have more than one
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
    fNewsCounter = null;
  }

  /*
   * @see org.rssowl.core.persist.dao.INewsCounterDAO#delete()
   */
  public final void delete() {
    super.delete(load());
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#preCommit()
   */
  @Override
  protected void preCommit() {
    //Do nothing
  }

  private NewsCounter doCountAll() {
    NewsCounter newsCounter = new NewsCounter();
    Collection<IFeed> feeds = DBHelper.loadAllFeeds(fDb);

    for (IFeed feed : feeds)
      newsCounter.put(feed.getLink().toString(), doCount(feed));

    return newsCounter;
  }

  private NewsCounterItem doCount(IFeed feed) {
    NewsCounterItem counterItem = new NewsCounterItem();

    List<INews> newsList = feed.getVisibleNews();
    for (INews news : newsList) {
      if (EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED).contains(news.getState()))
        counterItem.incrementUnreadCounter();

      if (INews.State.NEW.equals(news.getState()))
        counterItem.incrementNewCounter();

      if (news.isFlagged())
        counterItem.incrementStickyCounter();
    }

    return counterItem;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#delete(org.
   * rssowl.core.persist.IPersistable)
   */
  @Override
  public final void delete(NewsCounter newsCounter) {
    if (!newsCounter.equals(load()))
      throw new IllegalArgumentException("Only a single newsCounter should be used. " + "Trying to delete a non-existent one."); //$NON-NLS-1$ //$NON-NLS-2$

    super.delete(newsCounter);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#loadAll()
   */
  @Override
  public Collection<NewsCounter> loadAll() {
    List<NewsCounter> newsCounters = new ArrayList<NewsCounter>(1);
    newsCounters.add(load());
    return newsCounters;
  }

  /*
   * @see org.rssowl.core.persist.dao.INewsCounterDAO#load()
   */
  public final NewsCounter load() {
    return fNewsCounter;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#saveAll(java
   * .util.Collection)
   */
  @Override
  public final void saveAll(Collection<NewsCounter> entities) {
    if (entities.size() > 1) {
      throw new IllegalArgumentException("Only a single newsCounter can be stored"); //$NON-NLS-1$
    }
    super.saveAll(entities);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#doSave(org.
   * rssowl.core.persist.IPersistable)
   */
  @Override
  protected final void doSave(NewsCounter entity) {
    if (!fDb.ext().isStored(entity) && (load() != null))
      throw new IllegalArgumentException("Only a single newsCounter can be stored"); //$NON-NLS-1$

    super.doSave(entity);
  }

  /*
   * @see org.rssowl.core.persist.dao.INewsCounterDAO#save()
   */
  public void save() {
    Assert.isNotNull(fNewsCounter, "fNewsCounter"); //$NON-NLS-1$
    fDb.ext().set(fNewsCounter, Integer.MAX_VALUE);
  }
}
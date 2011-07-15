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
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.LazyList;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.search.ModelSearchImpl;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.CoreUtils;

import com.db4o.ext.Db4oException;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * A data-access-object for <code>INews</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class NewsDAOImpl extends AbstractEntityDAO<INews, NewsListener, NewsEvent> implements INewsDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public NewsDAOImpl() {
    super(News.class, false);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#doSave(org.
   * rssowl.core.persist.IPersistable)
   */
  @Override
  protected final void doSave(INews entity) {
    DBHelper.saveAndCascadeNews(fDb, entity, true);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#preSaveAll(
   * java.util.Collection)
   */
  @Override
  protected void preSaveAll(Collection<INews> objects) {
    for (INews news : objects) {
      DBHelper.putEventTemplate(createSaveEventTemplate(news));
      ((News) news).acquireReadLockSpecial();
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#loadAll()
   */
  @Override
  public Collection<INews> loadAll() {
    return new LazyList<INews>(fDb.query(News.class), fDb);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#postSaveAll
   * (java.util.Collection)
   */
  @Override
  protected void postSaveAll(Collection<INews> objects) {
    for (INews news : objects) {
      News n = (News) news;
      n.releaseReadLockSpecial();
      n.clearTransientDescription();
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final NewsEvent createDeleteEventTemplate(INews entity) {
    return new NewsEvent(null, entity, true);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final NewsEvent createSaveEventTemplate(INews entity) {
    INews oldNews = DBHelper.peekPersistedNews(fDb, entity);
    return new NewsEvent(oldNews, entity, true);
  }

  /*
   * @see org.rssowl.core.persist.dao.INewsDAO#setState(java.util.Collection,
   * org.rssowl.core.persist.INews.State, boolean, boolean)
   */
  public void setState(Collection<INews> news, State state, boolean affectEquivalentNews, boolean force) throws PersistenceException {
    if (news.isEmpty())
      return;

    fWriteLock.lock();
    Set<INews> changedNews = null;
    try {

      /* Find equivalent news to update */
      if (affectEquivalentNews) {

        /*
         * Give extra 25% size to take into account news that have same guid or
         * link.
         */
        int capacity = news.size() + (news.size() / 4);
        changedNews = new HashSet<INews>(capacity);
        for (INews newsItem : news) {
          if (newsItem.getId() == null)
            throw new IllegalArgumentException("newsItem was never saved to the database"); //$NON-NLS-1$

          if (changedNews.contains(newsItem))
            continue; //Already handled this news because it's equivalent to one of the ones processed earlier

          if (!newsItem.isVisible() && affectEquivalentNews)
            continue; //It is possible that another thread marked the news as deleted meanwhile, in this case, continue.

          List<INews> equivalentNews;

          /* Not supported for news inside bins */
          if (newsItem.getParentId() != 0)
            equivalentNews = Collections.singletonList(newsItem);

          /* Search equivalent news by GUID */
          else if (newsItem.getGuid() != null && newsItem.getGuid().isPermaLink()) {
            equivalentNews = getNewsFromGuid(newsItem, true);
            if (equivalentNews.isEmpty()) {
              throw createIllegalException("No news were found with guid: " + newsItem.getGuid().getValue(), newsItem); //$NON-NLS-1$
            }
          }

          /* Search equivalent news by Link */
          else if (newsItem.getLinkAsText() != null) {
            equivalentNews = getNewsFromLink(newsItem, true);
            if (equivalentNews.isEmpty()) {
              throw createIllegalException("No news were found with link: " + newsItem.getLinkAsText(), newsItem); //$NON-NLS-1$
            }
          }

          /* No equivalent news at all */
          else
            equivalentNews = Collections.singletonList(newsItem);

          changedNews.addAll(setState(equivalentNews, state, force));
        }
      }

      /* Just set the state directly */
      else {
        changedNews = setState(news, state, force);
      }

      /* Commit */
      try {
        preSaveAll(changedNews);
        save(changedNews);
        preCommit();
        fDb.commit();
      } finally {
        postSaveAll(changedNews);
      }
    } catch (Db4oException e) {
      throw DBHelper.rollbackAndPE(fDb, e);
    } finally {
      fWriteLock.unlock();
    }

    DBHelper.cleanUpAndFireEvents();
  }

  private void save(Set<INews> newsList) {
    for (INews news : newsList) {
      fDb.ext().set(news, 1);
    }
  }

  private RuntimeException createIllegalException(String message, INews newsItem) {
    News dbNews = (News) DBHelper.peekPersistedNews(fDb, newsItem);
    if (dbNews == null)
      return new IllegalArgumentException("The news has been deleted from the persistence layer: " + newsItem); //$NON-NLS-1$

    return new IllegalStateException(message + ". This news in the db looks like: " + dbNews.toLongString()); //$NON-NLS-1$
  }

  private List<INews> getNewsFromLink(INews newsItem, boolean newsSaved) {
    return searchNews(newsItem, false, newsSaved);
  }

  private List<INews> getNewsFromGuid(INews newsItem, boolean newsSaved) {
    return searchNews(newsItem, true, newsSaved);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#saveAll(java
   * .util.Collection)
   */
  @Override
  public void saveAll(Collection<INews> news) {
    IdentityHashMap<INews, Object> map = new IdentityHashMap<INews, Object>();
    for (INews newsItem : news)
      map.put(newsItem, newsItem);
    super.saveAll(map.keySet());
  }

  private List<INews> searchNews(INews newsItem, boolean guid, boolean newsSaved) {
    List<INews> news = doSearchNews(newsItem, guid);
    if (!newsSaved)
      return news;

    if (news.contains(newsItem))
      return news;

    //TODO Get EntityIdsByEventType and check if the news is in there
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      /*
       * If we're interrupted we act just as if the sleep time had finished
       * since it's a short operation.
       */
    }

    news = doSearchNews(newsItem, guid);
    if (!news.contains(newsItem))
      news.add(newsItem);

    return news;
  }

  private List<INews> doSearchNews(INews newsItem, boolean guid) {
    ModelSearchImpl modelSearch = (ModelSearchImpl) Owl.getPersistenceService().getModelSearch();
    List<NewsReference> hits;
    if (guid)
      hits = modelSearch.searchNewsByGuid(newsItem.getGuid(), false);
    else
      hits = modelSearch.searchNewsByLink(newsItem.getLink(), false);

    List<INews> news = new ArrayList<INews>(hits.size());
    for (NewsReference hit : hits) {
      if (newsItem.getId() != null && (hit.getId() == newsItem.getId().longValue()))
        news.add(newsItem);
      else {
        INews resolvedNewsItem = hit.resolve();
        if (resolvedNewsItem != null && resolvedNewsItem.isVisible())
          news.add(resolvedNewsItem);
        else
          CoreUtils.reportIndexIssue();
      }
    }

    return news;
  }

  private Set<INews> setState(Collection<INews> news, State state, boolean force) {
    Set<INews> changedNews = new HashSet<INews>(news.size());
    for (INews newsItem : news) {
      if (newsItem.getState() != state || force) {
        newsItem.setState(state);
        changedNews.add(newsItem);
      }
    }

    return changedNews;
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.INewsDAO#loadAll(org.rssowl.core.persist.reference
   * .FeedLinkReference, java.util.Set)
   */
  public Collection<INews> loadAll(FeedLinkReference feedRef, Set<State> states) {
    Assert.isNotNull(feedRef, "feedRef"); //$NON-NLS-1$
    Assert.isNotNull(states, "states"); //$NON-NLS-1$
    if (states.isEmpty())
      return new ArrayList<INews>(0);

    try {
      Query query = fDb.query();
      query.constrain(News.class);
      query.descend("fFeedLink").constrain(feedRef.getLink().toString()); //$NON-NLS-1$
      query.descend("fParentId").constrain(0); //$NON-NLS-1$
      if (!states.containsAll(EnumSet.allOf(INews.State.class))) {
        Constraint constraint = null;
        for (INews.State state : states) {
          if (constraint == null)
            constraint = query.descend("fStateOrdinal").constrain(state.ordinal()); //$NON-NLS-1$
          else
            constraint = query.descend("fStateOrdinal").constrain(state.ordinal()).or(constraint); //$NON-NLS-1$
        }
      }

      Collection<INews> news = getList(query);
      activateAll(news);

      return new ArrayList<INews>(news);
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }
  }

  /*
   * @see org.rssowl.core.persist.dao.INewsDAO#setState(java.util.Set,
   * org.rssowl.core.persist.INews.State, boolean)
   */
  public void setState(Set<State> originalStates, State state, boolean affectEquivalentNews) throws PersistenceException {
    Assert.isNotNull(originalStates, "states"); //$NON-NLS-1$
    Assert.isNotNull(state, "state"); //$NON-NLS-1$
    if (originalStates.isEmpty())
      return;

    try {
      Query query = fDb.query();
      query.constrain(News.class);
      if (!originalStates.containsAll(EnumSet.allOf(INews.State.class))) {
        Constraint constraint = null;
        for (INews.State originalState : originalStates) {
          if (constraint == null)
            constraint = query.descend("fStateOrdinal").constrain(originalState.ordinal()); //$NON-NLS-1$
          else
            constraint = query.descend("fStateOrdinal").constrain(originalState.ordinal()).or(constraint); //$NON-NLS-1$
        }
      }

      Collection<INews> news = getList(query);
      activateAll(news);
      setState(news, state, affectEquivalentNews, false);
    } catch (Db4oException e) {
      throw DBHelper.rollbackAndPE(fDb, e);
    }
  }
}
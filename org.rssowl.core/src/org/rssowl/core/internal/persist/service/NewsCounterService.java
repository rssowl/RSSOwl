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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.NewsCounterItem;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.NewsEvent;

import com.db4o.ObjectContainer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to update the news counter that is used for quick access of counts
 * per news state.
 */
public final class NewsCounterService {
  private final ObjectContainer fDb;
  private final NewsCounter fNewsCounter;

  public NewsCounterService(INewsCounterDAO newsCounterDAO, ObjectContainer db) {
    this.fDb = db;
    fNewsCounter = newsCounterDAO.load();
  }

  /**
   * Update the news counter from the list of added news.
   *
   * @param newsEvents the added news
   */
  public void onNewsAdded(Collection<NewsEvent> newsEvents) {
    if (newsEvents.isEmpty())
      return;

    boolean newsCounterUpdated = false;
    Map<String, NewsCounterItem> updatedCounterItems = new HashMap<String, NewsCounterItem>();
    synchronized (fNewsCounter) {
      for (NewsEvent newsEvent : newsEvents) {
        INews news = newsEvent.getEntity();

        if (news.getParentId() != 0) //News is stored inside a Bin
          continue;

        if (news.getState() == INews.State.DELETED) //Could be DELETED from filters
          continue;

        NewsCounterItem newsCounterItem = fNewsCounter.get(news.getFeedLinkAsText());

        /* Create Counter if not yet done */
        if (newsCounterItem == null) {
          newsCounterItem = new NewsCounterItem();
          fNewsCounter.put(news.getFeedLinkAsText(), newsCounterItem);
          newsCounterUpdated = true;
        }

        /* Update Counter */
        if (news.getState() == INews.State.NEW)
          newsCounterItem.incrementNewCounter();
        if (isUnread(news.getState()))
          newsCounterItem.incrementUnreadCounter();
        if (news.isFlagged())
          newsCounterItem.incrementStickyCounter();

        if (!newsCounterUpdated)
          updatedCounterItems.put(news.getFeedLinkAsText(), newsCounterItem);
      }

      if (newsCounterUpdated)
        fDb.ext().set(fNewsCounter, Integer.MAX_VALUE);
      else {
        for (NewsCounterItem item : updatedCounterItems.values())
          fDb.set(item);
      }
    }
  }

  /**
   * Update the news counter from the list of updated news.
   *
   * @param newsEvents the updated news
   */
  public void onNewsUpdated(Collection<NewsEvent> newsEvents) {
    Map<String, NewsCounterItem> updatedCounterItems = new HashMap<String, NewsCounterItem>();
    synchronized (fNewsCounter) {
      for (NewsEvent event : newsEvents) {
        INews currentNews = event.getEntity();

        if (currentNews.getParentId() != 0) //News is stored inside a Bin
          continue;

        INews oldNews = event.getOldNews();
        Assert.isNotNull(oldNews, "oldNews cannot be null on newsUpdated"); //$NON-NLS-1$

        boolean oldStateUnread = isUnread(oldNews.getState());
        boolean currentStateUnread = isUnread(currentNews.getState());

        boolean oldStateNew = INews.State.NEW.equals(oldNews.getState());
        boolean currentStateNew = INews.State.NEW.equals(currentNews.getState());

        boolean oldStateSticky = oldNews.isFlagged() && oldNews.isVisible();
        boolean newStateSticky = currentNews.isFlagged() && currentNews.isVisible();

        /* No Change - continue */
        if (oldStateUnread == currentStateUnread && oldStateNew == currentStateNew && oldStateSticky == newStateSticky)
          continue;

        NewsCounterItem counterItem = fNewsCounter.get(currentNews.getFeedLinkAsText());

        /* News became read */
        if (oldStateUnread && !currentStateUnread)
          counterItem.decrementUnreadCounter();

        /* News became unread */
        else if (!oldStateUnread && currentStateUnread)
          counterItem.incrementUnreadCounter();

        /* News no longer New */
        if (oldStateNew && !currentStateNew)
          counterItem.decrementNewCounter();

        /* News became New */
        else if (!oldStateNew && currentStateNew)
          counterItem.incrementNewCounter();

        /* News became unsticky */
        if (oldStateSticky && !newStateSticky)
          counterItem.decrementStickyCounter();

        /* News became sticky */
        else if (!oldStateSticky && newStateSticky)
          counterItem.incrementStickyCounter();

        updatedCounterItems.put(currentNews.getFeedLinkAsText(), counterItem);
      }

      for (NewsCounterItem counterItem : updatedCounterItems.values())
        fDb.set(counterItem);
    }
  }

  /**
   * Update the news counter from the list of deleted news.
   *
   * @param newsEvents the deleted news
   */
  public void onNewsRemoved(Collection<NewsEvent> newsEvents) {
    Map<String, NewsCounterItem> updatedCounterItems = new HashMap<String, NewsCounterItem>();

    synchronized (fNewsCounter) {
      for (NewsEvent newsEvent : newsEvents) {
        INews news = newsEvent.getEntity();

        if (news.getParentId() != 0) //News is stored inside a Bin
          continue;

        NewsCounterItem counterItem = fNewsCounter.get(news.getFeedLinkAsText());

        /* Update Counter */
        if (news.getState() == INews.State.NEW)
          counterItem.decrementNewCounter();
        if (isUnread(news.getState()))
          counterItem.decrementUnreadCounter();
        if (news.isFlagged() && (!EnumSet.of(INews.State.DELETED, INews.State.HIDDEN).contains(news.getState())))
          counterItem.decrementStickyCounter();

        updatedCounterItems.put(news.getFeedLinkAsText(), counterItem);
      }

      for (NewsCounterItem counterItem : updatedCounterItems.values())
        fDb.set(counterItem);
    }
  }

  /**
   * Update the news counter from the list of deleted feeds.
   *
   * @param feedEvents the deleted feeds
   */
  public void onFeedRemoved(Collection<FeedEvent> feedEvents) {
    if (feedEvents.isEmpty())
      return;

    synchronized (fNewsCounter) {
      for (FeedEvent feedEvent : feedEvents) {
        IFeed feed = feedEvent.getEntity();
        fNewsCounter.remove(feed.getLink().toString());
      }

      fDb.ext().set(fNewsCounter, Integer.MAX_VALUE);
    }
  }

  private static boolean isUnread(INews.State state) {
    return state == INews.State.NEW || state == INews.State.UPDATED || state == INews.State.UNREAD;
  }
}
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

package org.rssowl.core.persist;

import org.rssowl.core.internal.persist.Persistable;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>NewsCounter</code> stores and provides access to
 * <code>NewsCounterItem</code>. The key being used is a <code>String</code>.
 *
 * @see NewsCounterItem
 */
public final class NewsCounter extends Persistable {
  private Map<String, NewsCounterItem> fCountersMap;

  /** Leave default constructor for reflection. */
  public NewsCounter() {}

  /**
   * Stores the given NewsCounterItem using the given link as key.
   *
   * @param link The link being used to identify the
   * <code>NewsCounterItem</code>.
   * @param item The <code>NewsCounterItem</code> to store.
   */
  public synchronized void put(String link, NewsCounterItem item) {
    if (fCountersMap == null)
      fCountersMap = new HashMap<String, NewsCounterItem>();

    fCountersMap.put(link, item);
  }

  /**
   * Retrieves the NewsCounterItem stored for the given link or NULL if none.
   *
   * @param link The link being used to identify the
   * <code>NewsCounterItem</code>.
   * @return Returns the <code>NewsCounterItem</code> for the given link or NULL
   * if none.
   */
  public synchronized NewsCounterItem get(String link) {
    if (fCountersMap == null)
      return null;

    return fCountersMap.get(link);
  }

  /**
   * Removes the NewsCounterItem identified by the given link and returns it.
   *
   * @param link The link being used to identify the
   * <code>NewsCounterItem</code>.
   * @return Returns the <code>NewsCounterItem</code> that was removed.
   */
  public synchronized NewsCounterItem remove(String link) {
    if (fCountersMap == null)
      return null;

    return fCountersMap.remove(link);
  }

  /**
   * Returns the number of new News for the feed with {@code feedLink}.
   *
   * @param feedLink The link of the Feed.
   * @return the number of unread News for the Feed having the given link.
   */
  public synchronized int getNewCount(String feedLink) {
    NewsCounterItem counter = get(feedLink);

    /* Feed has no news */
    if (counter == null)
      return 0;

    return counter.getNewCounter();
  }

  /**
   * Returns the number of new unread for the feed with {@code feedLink}.
   *
   * @param feedLink The link of the Feed.
   * @return the number of unread News for the Feed having the given link.
   */
  public synchronized int getUnreadCount(String feedLink) {
    NewsCounterItem counter = get(feedLink);

    /* Feed has no news */
    if (counter == null)
      return 0;

    return counter.getUnreadCounter();
  }

  /**
   * Returns the number of sticky News for the feed with {@code feedLink}.
   *
   * @param feedLink The link of the Feed.
   * @return the number of unread News for the Feed having the given link.
   */
  public synchronized int getStickyCount(String feedLink) {
    NewsCounterItem counter = get(feedLink);

    /* Feed has no news */
    if (counter == null)
      return 0;

    return counter.getStickyCounter();
  }
}
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

import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.service.PersistenceException;

import java.util.Collection;
import java.util.Set;

/**
 * A data-access-object for <code>INews</code>.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface INewsDAO extends IEntityDAO<INews, NewsListener, NewsEvent> {

  /**
   * Sets the state of all the news items contained in <code>news</code> to
   * <code>state</code>. In addition, if <code>affectEquivalentNews</code>
   * is <code>true</code>, the state of equivalent news in other feeds will
   * also be changed to <code>state</code>. Note that news items whose state
   * is equal to <code>state</code> will not be changed or updated in the
   * persistence layer.
   *
   * @param news A Collection of <code>INews</code> whose state should be
   * changed.
   * @param state The state to set the news items to.
   * @param affectEquivalentNews If set to <code>TRUE</code> the state of
   * equivalent news in other feeds will also be changed to <code>state</code>
   * @param force If set to <code>TRUE</code>, the method will update even
   * those News that match the given state.
   * @throws PersistenceException
   */
  void setState(Collection<INews> news, INews.State state, boolean affectEquivalentNews, boolean force) throws PersistenceException;

  /**
   * Loads all the news that have a feedReference equal to <code>feedRef</code>
   * and state matching any of <code>states</code>.
   *
   * @param feedRef A non-null FeedLinkReference.
   * @param states A non-null Set (typically EnumSet) containing the all the
   * acceptable states for the returns INews items.
   * @return A Collection of INews with <code>feedRef</code> and any of
   * <code>states</code>.
   */
  Collection<INews> loadAll(FeedLinkReference feedRef, Set<INews.State> states);

  /**
   * Finds all the news from the system whose state is equal to one of the
   * elements in {@code states}, changes their state to {@code state} and saves
   * them.
   *
   * @param originalStates The state of the news whose state should be changed.
   * @param state The state to set the news items to.
   * @param affectEquivalentNews If set to <code>TRUE</code> the state of
   * equivalent news in other feeds will also be changed to <code>state</code>
   * @throws PersistenceException
   */
  void setState(Set<INews.State> originalStates, INews.State state, boolean affectEquivalentNews) throws PersistenceException;
}
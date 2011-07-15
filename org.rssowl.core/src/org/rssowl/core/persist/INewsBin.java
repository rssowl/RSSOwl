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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.persist.reference.NewsReference;

import java.util.Collection;
import java.util.Set;

/**
 * News bins are containers for news that are copied or moved from bookmarks.
 *
 * @author bpasero
 */
public interface INewsBin extends INewsMark {

  /**
   * A helper class to associate a news reference with an old and a new state.
   */
  public static final class StatesUpdateInfo {
    private final INews.State fOldState;
    private final INews.State fNewState;
    private final NewsReference fNewsReference;

    /**
     * @param oldState old state of the news
     * @param newState new state of the news
     * @param newsReference the reference to the news
     */
    public StatesUpdateInfo(State oldState, State newState, NewsReference newsReference) {
      Assert.isNotNull(newState, "newState"); //$NON-NLS-1$
      this.fOldState = oldState;
      this.fNewState = newState;
      this.fNewsReference = newsReference;
    }

    /**
     * @return old state of the news
     */
    public INews.State getOldState() {
      return fOldState;
    }

    /**
     * @return new state of the news
     */
    public INews.State getNewState() {
      return fNewState;
    }

    /**
     * @return the reference to the news
     */
    public NewsReference getNewsReference() {
      return fNewsReference;
    }
  }

  /**
   * @param news the news to add into this bin.
   */
  void addNews(INews news);

  /**
   * @param statesUpdateInfos
   * @return <code>true</code> if any state was changed and <code>false</code>
   * otherwise.
   */
  boolean updateNewsStates(Collection<StatesUpdateInfo> statesUpdateInfos);

  /**
   * @param news the news to remove from this bin.
   */
  void removeNews(INews news);

  /**
   * @param states the state of news to remove.
   * @return a collection of news references that have been removed.
   */
  Collection<NewsReference> removeNews(Set<INews.State> states);

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  NewsBinReference toReference();
}
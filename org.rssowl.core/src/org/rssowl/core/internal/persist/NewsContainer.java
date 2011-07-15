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

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin.StatesUpdateInfo;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@link NewsContainer} provides an array of {@link LongArrayList} that can
 * be used to store news ids sorted by state.
 */
public final class NewsContainer {
  private LongArrayList[] fNewsIds;

  protected NewsContainer() {
    super();
  }

  /**
   * @param statesToSortedMap a {@link Map} that indicates per INews.State if
   * the list of news ids should be sorted or not.
   */
  public NewsContainer(Map<INews.State, Boolean> statesToSortedMap) {
    int length = INews.State.values().length;
    fNewsIds = new LongArrayList[length];
    for (int i = 0; i < fNewsIds.length; i++) {
      Boolean sort = statesToSortedMap.get(INews.State.getState(i));
      if (sort != null && sort.equals(Boolean.TRUE))
        fNewsIds[i] = new SortedLongArrayList(0);
      else
        fNewsIds[i] = new LongArrayList(0);
    }
  }

  /**
   * Should only be used for testing.
   *
   * @return the internal array used by this class.
   */
  public LongArrayList[] internalGetNewsIds() {
    return fNewsIds;
  }

  /**
   * @param newsMap the map of news references by state to add to this container
   * @return a {@link Pair} where the first boolean indicates if the container
   * has changed at all and the second boolean indicates if new news have been
   * added.
   */
  public Pair<Boolean, Boolean> setNews(Map<INews.State, List<NewsReference>> newsMap) {
    Assert.isNotNull(newsMap, "newsMap"); //$NON-NLS-1$

    boolean changed = false;
    boolean isNewNewsAdded = false;

    Set<INews.State> statesToReset = EnumSet.allOf(INews.State.class);
    statesToReset.removeAll(newsMap.keySet());

    /* Reset all ArrayLists whose state is not present in newsMap */
    for (INews.State state : statesToReset) {
      int index = state.ordinal();
      LongArrayList currentArrayList = fNewsIds[index];
      if (currentArrayList.size() > 0) {
        currentArrayList.clear();
        changed = true;
      }
    }

    /* For each Result */
    for (Map.Entry<INews.State, List<NewsReference>> mapEntry : newsMap.entrySet()) {
      List<NewsReference> news = mapEntry.getValue();
      INews.State state = mapEntry.getKey();

      Assert.isNotNull(news, "news"); //$NON-NLS-1$
      Assert.isNotNull(state, "state"); //$NON-NLS-1$

      long[] newArray = new long[news.size()];

      /* Fill Bucket */
      for (int i = 0; i < news.size(); i++) {
        newArray[i] = news.get(i).getId();
      }

      int index = state.ordinal();
      LongArrayList currentArrayList = fNewsIds[index];

      /* New News */
      if (state.equals(INews.State.NEW)) {

        /* Check for added *new* News */
        if (newArray.length > currentArrayList.size())
          isNewNewsAdded = true;
        else {
          for (long value : newArray) {
            if (currentArrayList.indexOf(value) < 0) {
              isNewNewsAdded = true;
              break;
            }
          }
        }

        if (isNewNewsAdded || (newArray.length != currentArrayList.size())) {
          changed = true;
          currentArrayList.setAll(newArray);
        }
      }

      /* Any other News State */
      else {
        if (!currentArrayList.elementsEqual(newArray)) {
          currentArrayList.setAll(newArray);
          changed = true;
        }
      }
    }

    return Pair.create(changed, isNewNewsAdded);
  }

  /**
   * @param news the news to add to this container.
   */
  public void addNews(INews news) {
    checkNewsIdNotNull(news);
    fNewsIds[getIndex(news)].add(news.getId());
  }

  private int getIndex(INews news) {
    return news.getState().ordinal();
  }

  /**
   * @param news the news to be removed from this container.
   * @return <code>true</code> if this element was actually contained in the
   * container and <code>false</code> otherwise.
   */
  public boolean removeNews(INews news) {
    checkNewsIdNotNull(news);
    return fNewsIds[getIndex(news)].removeByElement(news.getId());
  }

  /**
   * @param states the news states to count
   * @return the number of news for the given states.
   */
  public int getNewsCount(Set<INews.State> states) {
    Assert.isNotNull(states, "states"); //$NON-NLS-1$

    int count = 0;

    for (INews.State state : states) {
      count += fNewsIds[state.ordinal()].size();
    }

    return count;
  }

  /**
   * @param news the news to search for
   * @return <code>true</code> if this container contains the given news and
   * <code>false</code> otherwise.
   */
  public boolean containsNews(INews news) {
    checkNewsIdNotNull(news);
    for (int i = 0, c = fNewsIds.length; i < c; ++i) {
      if (fNewsIds[i].contains(news.getId()))
        return true;
    }
    return false;
  }

  private void checkNewsIdNotNull(INews news) {
    Assert.isNotNull(news.getId(), "news.getId()"); //$NON-NLS-1$
  }

  /**
   * @return a {@link List} of {@link NewsReference} of all news this container
   * contains.
   */
  public List<NewsReference> getNews() {
    return getNews(EnumSet.allOf(INews.State.class));
  }

  /**
   * @param states the states to get news for
   * @return a {@link List} of {@link NewsReference} of all news this container
   * contains with the given news states.
   */
  public List<NewsReference> getNews(Set<INews.State> states) {
    List<NewsReference> newsRefs = new ArrayList<NewsReference>(getNewsCount(states));

    for (INews.State state : states) {
      int index = state.ordinal();
      LongArrayList newsIds = fNewsIds[index];
      for (int i = 0, c = newsIds.size(); i < c; ++i) {
        long newsId = newsIds.get(i);
        Assert.isLegal(newsId != 0);
        newsRefs.add(new NewsReference(newsId));
      }
    }

    return newsRefs;
  }

  /**
   * @param states the news states to remove from this container
   * @return the {@link List} of {@link NewsReference} that got removed from
   * this container.
   */
  public List<NewsReference> removeNews(Set<INews.State> states) {
    List<NewsReference> newsRefs = getNews(states);
    for (INews.State state : states) {
      int index = state.ordinal();
      fNewsIds[index].clear();
    }
    return newsRefs;
  }

  /**
   * @param newsRefs the list of news references to remove from this container
   */
  public void removeNewsRefs(List<NewsReference> newsRefs) {
    for (LongArrayList list : fNewsIds) {
      for (NewsReference newsRef : newsRefs)
        list.removeByElement(newsRef.getId());
    }
  }

  /**
   * @param statesUpdateInfos a collection of references which have their states
   * updated
   * @return <code>true</code> if this news container has changed after the
   * method is completed and <code>false</code> otherwise.
   */
  public boolean updateNewsStates(Collection<StatesUpdateInfo> statesUpdateInfos) {
    boolean changed = false;
    for (StatesUpdateInfo info : statesUpdateInfos) {
      long newsId = info.getNewsReference().getId();

      /* Old State Present */
      if (info.getOldState() == null) {
        boolean itemRemoved = fNewsIds[INews.State.NEW.ordinal()].removeByElement(newsId);
        if (!itemRemoved) {
          EnumSet<State> remainingStates = EnumSet.allOf(INews.State.class);
          remainingStates.remove(INews.State.NEW);
          remainingStates.remove(info.getNewState());
          for (INews.State state : remainingStates) {
            if (fNewsIds[state.ordinal()].removeByElement(newsId))
              itemRemoved = true;
          }
        }

        if (itemRemoved) {
          changed = true;
          fNewsIds[info.getNewState().ordinal()].add(newsId);
        }
      }

      /* No Old State Present  */
      else if (fNewsIds[info.getOldState().ordinal()].removeByElement(newsId)) {
        changed = true;
        fNewsIds[info.getNewState().ordinal()].add(newsId);
      }
    }

    return changed;
  }
}
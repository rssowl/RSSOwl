/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.editors.feed;

import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.Triple;
import org.rssowl.ui.internal.EntityGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The news view model is a representation of the visible news in the
 * {@link NewsBrowserViewer}. This includes groups (if enabled), sorting and
 * other UI related state.
 * <p>
 * The model is safe to be used from multiple threads.
 * </p>
 *
 * @author bpasero
 */
public class NewsBrowserViewModel {
  private final List<Item> fItemList = new ArrayList<NewsBrowserViewModel.Item>();
  private final Map<Long, Item> fNewsMap = new HashMap<Long, NewsBrowserViewModel.Item>();
  private final Map<Long, Group> fGroupMap = new HashMap<Long, NewsBrowserViewModel.Group>();
  private final Map<Long, List<Long>> fEntityGroupToNewsMap = new HashMap<Long, List<Long>>();
  private final Set<Long> fExpandedNews = new HashSet<Long>();
  private final Set<Long> fCollapsedGroups = new HashSet<Long>();
  private final Set<Long> fHiddenNews = new HashSet<Long>();
  private final Set<Long> fHiddenGroups = new HashSet<Long>();
  private final Object fLock = new Object();
  private final NewsBrowserViewer fViewer;

  public NewsBrowserViewModel(NewsBrowserViewer viewer) {
    fViewer = viewer;
  }

  /* Base Class of all Items in the Model */
  private static class Item {
    private final long fId;

    public Item(long id) {
      fId = id;
    }

    public long getId() {
      return fId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (fId ^ (fId >>> 32));

      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;

      if (obj == null)
        return false;

      if (getClass() != obj.getClass())
        return false;

      Item other = (Item) obj;
      if (fId != other.fId)
        return false;

      return true;
    }
  }

  /* Special Item that contains other Items in the View */
  private static class Group extends Item {
    public Group(long id) {
      super(id);
    }
  }

  /**
   * Updates this view model with the contents of the provided elements.
   *
   * @param elements the elements to create the view model from.
   * @param pageSize the number of elements per page or <code>0</code> in case
   * paging is disabled.
   */
  public void setInput(Object[] elements, int pageSize) {
    synchronized (fLock) {

      /* Clear Caches */
      fItemList.clear();
      fNewsMap.clear();
      fGroupMap.clear();
      fEntityGroupToNewsMap.clear();
      fExpandedNews.clear();
      fCollapsedGroups.clear();
      fHiddenNews.clear();
      fHiddenGroups.clear();

      /* Build the Model based on the Elements */
      if (elements != null && elements.length > 0) {

        /* Build Model */
        int newsCounter = 0;
        List<Long> currentGroupEntryList = null;
        for (Object element : elements) {
          Item entry = null;

          /* Entity Group */
          if (element instanceof EntityGroup) {
            EntityGroup group = (EntityGroup) element;
            entry = new Group(group.getId());
            fGroupMap.put(entry.getId(), (Group) entry);

            currentGroupEntryList = new ArrayList<Long>();
            fEntityGroupToNewsMap.put(group.getId(), currentGroupEntryList);

            /*
             * We use ">=" here to check if the group is visible or not to avoid the case
             * of a group being made visible that contains no visible news at all.
             */
            if (pageSize != 0 && newsCounter >= pageSize)
              setGroupVisible(group.getId(), false);
          }

          /* News Item */
          else if (element instanceof INews) {
            newsCounter++;
            INews news = (INews) element;
            entry = new Item(news.getId());
            fNewsMap.put(entry.getId(), entry);

            if (currentGroupEntryList != null)
              currentGroupEntryList.add(news.getId());

            if (pageSize != 0 && newsCounter > pageSize)
              setNewsVisible(news, false);
          }

          /* Add Entry into Collection */
          if (entry != null)
            fItemList.add(entry);
        }
      }
    }
  }

  /**
   * @return the {@link Map} of groups if grouping is enabled.
   */
  public Map<Long, List<Long>> getGroups() {
    synchronized (fLock) {
      return new HashMap<Long, List<Long>>(fEntityGroupToNewsMap);
    }
  }

  /**
   * @param groupId the group identifier to look for
   * @return <code>true</code> if a group with the given identifier exists and
   * <code>false</code> otherwise.
   */
  public boolean hasGroup(long groupId) {
    synchronized (fLock) {
      return fEntityGroupToNewsMap.containsKey(groupId);
    }
  }

  /**
   * @param newsId the news identifier to look for
   * @return <code>true</code> if a news with the given identifier exists and
   * <code>false</code> otherwise.
   */
  public boolean hasNews(long newsId) {
    synchronized (fLock) {
      return fNewsMap.containsKey(newsId);
    }
  }

  /**
   * @param groupId the group identifier to use
   * @return the number of elements inside the group with the given identifier
   * or 0 if none.
   */
  public int getGroupSize(long groupId) {
    synchronized (fLock) {
      List<Long> entries = fEntityGroupToNewsMap.get(groupId);
      return entries != null ? entries.size() : 0;
    }
  }

  /**
   * @param groupId the group identifier to use
   * @return the list of news ids being held by the given group.
   */
  @SuppressWarnings("unchecked")
  public List<Long> getNewsIds(long groupId) {
    synchronized (fLock) {
      List<Long> newsIds = fEntityGroupToNewsMap.get(groupId);
      return newsIds != null ? new ArrayList<Long>(newsIds) : Collections.EMPTY_LIST;
    }
  }

  /**
   * @param news the news item to check for expanded state
   * @return <code>true</code> if the news is expanded and <code>false</code>
   * otherwise.
   */
  public boolean isNewsExpanded(INews news) {
    synchronized (fLock) {
      return fExpandedNews.contains(news.getId());
    }
  }

  /**
   * @param news the news item to check for being visible or not
   * @return <code>true</code> if the news is visible and <code>false</code>
   * otherwise.
   */
  public boolean isNewsVisible(INews news) {
    return news.getId() != null && isNewsVisible(news.getId());
  }

  /**
   * @param newsId the news item to check for being visible or not
   * @return <code>true</code> if the news is visible and <code>false</code>
   * otherwise.
   */
  public boolean isNewsVisible(long newsId) {
    synchronized (fLock) {
      return !fHiddenNews.contains(newsId);
    }
  }

  /**
   * @param groupId
   * @return <code>true</code> if the group is expanded and <code>false</code>
   * otherwise.
   */
  public boolean isGroupExpanded(long groupId) {
    synchronized (fLock) {
      return !fCollapsedGroups.contains(groupId);
    }
  }

  /**
   * @param groupId
   * @return <code>true</code> if the group is visible and <code>false</code>
   * otherwise.
   */
  public boolean isGroupVisible(long groupId) {
    synchronized (fLock) {
      return !fHiddenGroups.contains(groupId);
    }
  }

  /**
   * @return the identifier of the currently expanded news or -1 if none.
   */
  public long getExpandedNews() {
    synchronized (fLock) {
      if (!fExpandedNews.isEmpty())
        return fExpandedNews.iterator().next();

      return -1L;
    }
  }

  /**
   * @param news the news to expand or collapse
   * @param expanded <code>true</code> if expanded and <code>false</code> if
   * collapsed
   */
  public void setNewsExpanded(INews news, boolean expanded) {
    synchronized (fLock) {
      if (expanded)
        fExpandedNews.add(news.getId());
      else
        fExpandedNews.remove(news.getId());
    }
  }

  /**
   * @param news the news to hide or show
   * @param visible <code>true</code> if visible and <code>false</code> if
   * hidden
   */
  public void setNewsVisible(INews news, boolean visible) {
    if (news.getId() != null)
      setNewsVisible(news.getId(), visible);
  }

  /**
   * @param newsId the news to hide or show
   * @param visible <code>true</code> if visible and <code>false</code> if
   * hidden
   */
  public void setNewsVisible(long newsId, boolean visible) {
    synchronized (fLock) {
      if (visible)
        fHiddenNews.remove(newsId);
      else
        fHiddenNews.add(newsId);
    }
  }

  /**
   * @param newsId the news to get the index of
   * @return the index of the provided news or <code>-1</code> if none.
   */
  public int indexOfNewsItem(long newsId) {
    synchronized (fLock) {
      for (int i = 0; i < fItemList.size(); i++) {
        Item item = fItemList.get(i);
        if (!(item instanceof Group) && item.getId() == newsId)
          return i;
      }
    }

    return -1;
  }

  /**
   * @param groupId the group to hide or show
   * @param visible <code>true</code> if visible and <code>false</code> if
   * hidden
   */
  public void setGroupVisible(long groupId, boolean visible) {
    synchronized (fLock) {
      if (visible)
        fHiddenGroups.remove(groupId);
      else
        fHiddenGroups.add(groupId);
    }
  }

  /**
   * @param groupId the group to expand or collapse
   * @param expanded <code>true</code> if expanded and <code>false</code> if
   * collapsed
   */
  public void setGroupExpanded(long groupId, boolean expanded) {
    synchronized (fLock) {
      if (expanded)
        fCollapsedGroups.remove(groupId);
      else
        fCollapsedGroups.add(groupId);
    }
  }

  /**
   * @param newsId the identifier of the news to find the group for
   * @return the identifier of the group for the given news or -1 if none
   */
  public long findGroup(long newsId) {
    synchronized (fLock) {
      Set<java.util.Map.Entry<Long, List<Long>>> entries = fEntityGroupToNewsMap.entrySet();
      for (java.util.Map.Entry<Long, List<Long>> entry : entries) {
        List<Long> newsInGroup = entry.getValue();
        if (newsInGroup.contains(newsId))
          return entry.getKey();
      }
    }

    return -1L;
  }

  /**
   * @return <code>true</code> if the first item showing in the browser is
   * unread and <code>false</code>otherwise. Will always return false if
   * grouping is enabled as the first item then will be a group.
   */
  public boolean isFirstItemUnread() {
    synchronized (fLock) {
      if (!fItemList.isEmpty()) {
        Item item = fItemList.get(0);
        return isUnread(item);
      }
    }

    return false;
  }

  /**
   * @return <code>true</code> if the browser viewer is displaying items and
   * <code>false</code> otherwise.
   */
  public boolean hasItems() {
    synchronized (fLock) {
      return !fItemList.isEmpty();
    }
  }

  /**
   * @return <code>true</code> if the browser viewer is containing hidden news
   * and <code>false</code> otherwise.
   */
  public boolean hasHiddenNews() {
    synchronized (fLock) {
      return !fHiddenNews.isEmpty();
    }
  }

  /**
   * @return the number of news in the model (both visible and hidden).
   */
  public int getNewsCount() {
    synchronized (fLock) {
      return fItemList.size() - fEntityGroupToNewsMap.keySet().size();
    }
  }

  /**
   * @return the number of visible news displayed.
   */
  public int getVisibleNewsCount() {
    synchronized (fLock) {
      return fItemList.size() - fEntityGroupToNewsMap.keySet().size() - fHiddenNews.size();
    }
  }

  /**
   * @return a list of ids of news that are unread and visible.
   */
  public List<Long> getVisibleUnreadNews() {
    List<Long> visibleUnreadNewsIds = new ArrayList<Long>();
    synchronized (fLock) {
      for (Item item : fItemList) {
        if (item instanceof Group)
          continue;

        if (!isNewsVisible(item.getId()))
          break;

        if (isNewsInCollapsedGroup(item.getId()))
          continue;

        if (isUnread(item))
          visibleUnreadNewsIds.add(item.getId());
      }
    }

    return visibleUnreadNewsIds;
  }

  private boolean isNewsInCollapsedGroup(long newsId) {
    synchronized (fLock) {
      for (Long groupId : fCollapsedGroups) {
        if (fEntityGroupToNewsMap.get(groupId).contains(newsId))
          return true;
      }

      return false;
    }
  }

  /**
   * Returns the first news that is hidden and optionally unread or
   * <code>-1</code> if none.
   *
   * @param onlyUnread if set to <code>true</code>, only unread news will be
   * considered.
   * @return the identifier of the first hidden news or <code>-1</code> if none.
   */
  public long getFirstHiddenNews(boolean onlyUnread) {
    synchronized (fLock) {
      if (fHiddenNews.isEmpty())
        return -1;

      for (int i = 0; i < fItemList.size(); i++) {
        Item item = fItemList.get(i);

        if (item instanceof Group || isNewsVisible(item.getId()))
          continue;

        if (!onlyUnread || isUnread(item))
          return item.getId();
      }
    }

    return -1;
  }

  /**
   * Returns the last news that is visible or <code>-1</code> if none.
   *
   * @return the identifier of the last hidden news or <code>-1</code> if none.
   */
  public long getLastVisibleNews() {
    Item lastVisibleNews = null;
    synchronized (fLock) {
      for (int i = 0; i < fItemList.size(); i++) {
        Item item = fItemList.get(i);

        if (item instanceof Group)
          continue;

        if (isNewsVisible(item.getId()))
          lastVisibleNews = item;
        else
          break;
      }
    }

    return lastVisibleNews != null ? lastVisibleNews.getId() : -1;
  }

  /**
   * @return the last news item of this model or -1 if none.
   */
  public long getLastNews() {
    synchronized (fLock) {
      for (int i = fItemList.size() - 1; i >= 0; i--) {
        Item item = fItemList.get(i);
        if (item instanceof Group)
          continue;

        return item.getId();
      }
    }

    return -1;
  }

  /**
   * @param news the news to remove from the view model.
   * @return the identifier of a group that needs an update now that the news
   * has been removed or -1 if none.
   */
  public long removeNews(INews news) {
    synchronized (fLock) {

      /* Remove from generic Item Collections */
      fHiddenNews.remove(news.getId());
      Item item = fNewsMap.get(news.getId());
      if (item != null) {
        fItemList.remove(item);
        fNewsMap.remove(item.getId());
      }

      /* Remove from Collection of expanded Elements */
      fExpandedNews.remove(news.getId());

      /* Remove from Group Mapping */
      Set<java.util.Map.Entry<Long, List<Long>>> entries = fEntityGroupToNewsMap.entrySet();
      for (java.util.Map.Entry<Long, List<Long>> entry : entries) {
        Long groupId = entry.getKey();
        List<Long> newsInGroup = entry.getValue();
        if (newsInGroup.contains(news.getId())) {
          newsInGroup.remove(news.getId());

          /* In case the group is now empty, remove it as well */
          if (newsInGroup.isEmpty()) {
            fEntityGroupToNewsMap.remove(groupId);
            fCollapsedGroups.remove(groupId);
            fHiddenGroups.remove(groupId);

            Group group = fGroupMap.get(groupId);
            if (group != null) {
              fItemList.remove(group);
              fGroupMap.remove(group.getId());
            }
          }

          return groupId; //News can only be part of one group
        }
      }

      return -1L;
    }
  }

  /**
   * @param unread if the next news should be unread or not
   * @param offset the offset to start navigating from
   * @return the identifier of the next news or -1 if none
   */
  public long nextNews(boolean unread, long offset) {
    synchronized (fLock) {

      /* Get the next news using provided one as starting location or from beginning if no location provided */
      Item item = new Item(offset);
      int nextIndex = (offset != -1 && fItemList.contains(item)) ? fItemList.indexOf(item) + 1 : 0;

      /* More Elements available */
      for (int i = nextIndex; i < fItemList.size(); i++) {
        Item nextItem = fItemList.get(i);
        if (nextItem instanceof Group)
          continue; //We only want to navigate to News Items

        /* Return Item if it matches the criteria */
        if (!unread || isUnread(nextItem))
          return nextItem.getId();
      }
    }

    return -1L;
  }

  /**
   * @param unread if the previous news should be unread or not
   * @param offset the offset to start navigating from
   * @return the identifier of the previous news or -1 if none
   */
  public long previousNews(boolean unread, long offset) {
    synchronized (fLock) {

      /* Get the next news using provided one as starting location or from end if no location provided */
      Item item = new Item(offset);
      int previousIndex = (offset != -1 && fItemList.contains(item)) ? fItemList.indexOf(item) - 1 : fItemList.size() - 1;

      /* More Elements available */
      for (int i = previousIndex; i >= 0 && i < fItemList.size(); i--) {
        Item previousItem = fItemList.get(i);
        if (previousItem instanceof Group)
          continue; //We only want to navigate to News Items

        /* Return Item if it matches the criteria */
        if (!unread || isUnread(previousItem))
          return previousItem.getId();
      }
    }

    return -1L;
  }

  private boolean isUnread(Item item) {
    if (item instanceof Group)
      return false;

    INews news;
    if (fViewer != null)
      news = fViewer.resolve(item.getId());
    else
      news = DynamicDAO.load(INews.class, item.getId());

    if (news == null)
      return false;

    switch (news.getState()) {
      case NEW:
      case UNREAD:
      case UPDATED:
        return true;
    }

    return false;
  }

  /**
   * Retrieves the identifiers of the elements for the next page.
   *
   * @param pageSize the number of elements per page.
   * @return a {@link Triple} of lists. The first contains the identifiers of
   * groups revealed and the second the list of news identifiers.
   */
  public Pair<List<Long> /* Groups */, List<Long> /* News Items */> getNextPage(int pageSize) {
    List<Long> groups = new ArrayList<Long>(1);
    List<Long> news = new ArrayList<Long>(pageSize);

    /* Get the next page if paging is enabled */
    if (pageSize != 0) {
      synchronized (fLock) {
        int indexOfFirstHiddenItem = indexOfFirstHiddenItem(-1);
        if (indexOfFirstHiddenItem != -1) {
          int newsCounter = 0;
          for (int i = indexOfFirstHiddenItem; i < fItemList.size(); i++) {
            Item item = fItemList.get(i);

            /* Group */
            if (item instanceof Group) {
              groups.add(item.getId());
            }

            /* News */
            else {
              newsCounter++;
              news.add(item.getId());
            }

            if (newsCounter == pageSize)
              break; //Reached the next page, so stop looping
          }
        }
      }
    }

    return Pair.create(groups, news);
  }

  /**
   * Will return lists of groups (if any) and news that are hidden up to the
   * provided news and including the entire page the news is in. This allows to
   * reveal the actual page the provided news is in if hidden.
   *
   * @param newsId the identifier of the news item that is being revealed.
   * @param pageSize the number of elements per page.
   * @return a {@link Pair} of lists. The first contains the identifiers of
   * groups revealed and the second the list of news identifiers.
   */
  public Pair<List<Long> /* Groups */, List<Long> /* News Items */> revealPage(long newsId, int pageSize) {
    List<Long> groups = new ArrayList<Long>(1);
    List<Long> news = new ArrayList<Long>();

    /* Find all pages until target news is found */
    if (pageSize != 0) {
      synchronized (fLock) {
        int indexOfFirstHiddenItem = indexOfFirstHiddenItem(newsId);
        if (indexOfFirstHiddenItem != -1) {
          int newsCounter = 0;
          for (int i = indexOfFirstHiddenItem; i < fItemList.size(); i++) {
            Item item = fItemList.get(i);

            /* Group */
            if (item instanceof Group) {
              groups.add(item.getId());
            }

            /* News */
            else {
              newsCounter++;
              news.add(item.getId());
            }

            if (news.contains(newsId) && newsCounter % pageSize == 0)
              break; //Reached the page that contains the target news, so stop
          }
        }
      }
    }

    return Pair.create(groups, news);
  }

  private int indexOfFirstHiddenItem(long toId) {
    synchronized (fLock) {
      if (fHiddenNews.isEmpty() && fHiddenGroups.isEmpty())
        return -1;

      for (int i = 0; i < fItemList.size(); i++) {
        Item item = fItemList.get(i);

        if (!isVisible(item))
          return i; //Hidden Item found

        if (toId != -1 && toId == item.getId())
          break; //Limit reached
      }
    }

    return -1;
  }

  private boolean isVisible(Item item) {
    if (item instanceof Group)
      return isGroupVisible(item.getId());

    return isNewsVisible(item.getId());
  }
}
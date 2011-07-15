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

package org.rssowl.ui.internal.editors.feed;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.LongArrayList;
import org.rssowl.core.internal.persist.SearchMark;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.SearchMarkAdapter;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.ModelReference;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.reference.SearchMarkReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.SearchHit;
import org.rssowl.core.util.Triple;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.EntityGroupItem;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.FolderNewsMark.FolderNewsMarkReference;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.NewsFilter.Type;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.ModelUtils;
import org.rssowl.ui.internal.util.UIBackgroundJob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bpasero
 */
@SuppressWarnings("restriction")
public class NewsContentProvider implements ITreeContentProvider {

  /* The maximum number of items returned from a FolderNewsMark */
  static final int MAX_FOLDER_ELEMENTS = 500;

  /* The maximum number of items that will get resolved from a FolderNewsMark */
  private static final int MAX_RESOLVED_FOLDER_ELEMENTS = 5000;

  /* The maximum number of items in a NewsMark before scoping the results as specified by the filter */
  private static final int NEWSMARK_SCOPE_SEARCH_LIMIT = 200;

  /* The maximum number of items in a Bookmark before scoping the results as specified by the filter */
  private static final int BOOKMARK_SCOPE_SEARCH_LIMIT = 500;

  /* System Property to override the limits above */
  private static final String NO_FOLDER_LIMIT_PROPERTY = "noFolderLimit"; //$NON-NLS-1$

  private final NewsBrowserViewer fBrowserViewer;
  private final NewsTableViewer fTableViewer;
  private final NewsGrouping fGrouping;
  private final NewsFilter fFilter;
  private NewsListener fNewsListener;
  private SearchMarkAdapter fSearchMarkListener;
  private INewsMark fInput;
  private final FeedView fFeedView;
  private final AtomicBoolean fDisposed = new AtomicBoolean(false);
  private final INewsDAO fNewsDao;
  private final IModelFactory fFactory;
  private final IModelSearch fSearch;
  private final boolean fNoFolderLimit;

  /* Cache displayed News */
  private final Map<Long, INews> fCachedNews;

  /* Enumeration of possible news event types */
  private static enum NewsEventType {
    PERSISTED, UPDATED, REMOVED, RESTORED
  }

  /**
   * @param tableViewer
   * @param browserViewer
   * @param feedView
   */
  public NewsContentProvider(NewsTableViewer tableViewer, NewsBrowserViewer browserViewer, FeedView feedView) {
    fTableViewer = tableViewer;
    fBrowserViewer = browserViewer;
    fFeedView = feedView;
    fGrouping = feedView.getGrouper();
    fFilter = feedView.getFilter();
    fCachedNews = new HashMap<Long, INews>();
    fNewsDao = DynamicDAO.getDAO(INewsDAO.class);
    fFactory = Owl.getModelFactory();
    fSearch = Owl.getPersistenceService().getModelSearch();
    fNoFolderLimit = System.getProperty(NO_FOLDER_LIMIT_PROPERTY) != null;
  }

  /*
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    List<Object> elements = new ArrayList<Object>();

    /* Wrap into Object Array */
    if (!(inputElement instanceof Object[]))
      inputElement = new Object[] { inputElement };

    /* Foreach Object */
    Object[] objects = (Object[]) inputElement;
    for (Object object : objects) {

      /* This is a News */
      if (object instanceof INews && ((INews) object).isVisible()) {
        elements.add(object);
      }

      /* This is a NewsReference */
      else if (object instanceof NewsReference) {
        NewsReference newsRef = (NewsReference) object;
        INews news = obtainFromCache(newsRef);
        if (news != null)
          elements.add(news);
      }

      /* This is a FeedReference */
      else if (object instanceof FeedLinkReference) {
        synchronized (NewsContentProvider.this) {
          Collection<INews> news = fCachedNews.values();
          if (news != null) {
            if (fGrouping.getType() == NewsGrouping.Type.NO_GROUPING)
              elements.addAll(news);
            else
              elements.addAll(fGrouping.group(news));
          }
        }
      }

      /* This is a class that implements IMark */
      else if (object instanceof ModelReference) {
        Class<? extends IEntity> entityClass = ((ModelReference) object).getEntityClass();
        if (IMark.class.isAssignableFrom(entityClass) || IFolder.class.isAssignableFrom(entityClass)) { //Suppoer FolderNewsMark too
          synchronized (NewsContentProvider.this) {
            Collection<INews> news = fCachedNews.values();
            if (news != null) {
              if (fGrouping.getType() == NewsGrouping.Type.NO_GROUPING)
                elements.addAll(news);
              else
                elements.addAll(fGrouping.group(news));
            }
          }
        }
      }

      /* This is a EntityGroup */
      else if (object instanceof EntityGroup) {
        EntityGroup group = (EntityGroup) object;

        List<EntityGroupItem> items = group.getItems();
        for (EntityGroupItem item : items) {
          if (((INews) item.getEntity()).isVisible())
            elements.add(item.getEntity());
        }
      }
    }

    return elements.toArray();
  }

  /*
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object parentElement) {
    List<Object> children = new ArrayList<Object>();

    /* Handle EntityGroup */
    if (parentElement instanceof EntityGroup) {
      List<EntityGroupItem> items = ((EntityGroup) parentElement).getItems();
      for (EntityGroupItem item : items)
        children.add(item.getEntity());
    }

    return children.toArray();
  }

  /*
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object element) {

    /* Handle Grouping specially */
    if (fGrouping.isActive() && element instanceof INews) {
      Collection<EntityGroup> groups = fGrouping.group(Collections.singletonList((INews) element));
      if (groups.size() == 1)
        return groups.iterator().next();
    }

    return null;
  }

  /*
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object element) {
    return element instanceof EntityGroup;
  }

  /*
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public synchronized void dispose() {
    fDisposed.set(true);
    unregisterListeners();
    fCachedNews.clear();
  }

  /*
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
   * java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    /* Ignore - Input changes are handled via refreshCache(Object input) */
  }

  boolean isGroupingEnabled() {
    return fGrouping.getType() != NewsGrouping.Type.NO_GROUPING;
  }

  boolean isGroupingByFeed() {
    return fGrouping.getType() == NewsGrouping.Type.GROUP_BY_FEED;
  }

  boolean isGroupingByStickyness() {
    return fGrouping.getType() == NewsGrouping.Type.GROUP_BY_STICKY;
  }

  boolean isGroupingByLabel() {
    return fGrouping.getType() == NewsGrouping.Type.GROUP_BY_LABEL;
  }

  boolean isGroupingByState() {
    return fGrouping.getType() == NewsGrouping.Type.GROUP_BY_STATE;
  }

  synchronized void refreshCache(IProgressMonitor monitor, INewsMark input) throws PersistenceException {
    refreshCache(monitor, input, null);
  }

  @SuppressWarnings("unchecked")
  synchronized void refreshCache(IProgressMonitor monitor, INewsMark input, NewsComparator comparer) throws PersistenceException {

    /* If input is identical, keep the cache during this method to speed up lookup of already resolved items */
    Map<Long, INews> cacheCopy = null;
    if (fInput != null && fInput.equals(input))
      cacheCopy = new HashMap(fCachedNews);

    /* Update Input */
    fInput = input;

    /* Register Listeners if not yet done */
    if (fNewsListener == null)
      registerListeners();

    /* Clear old Data */
    fCachedNews.clear();

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled(monitor))
      return;

    /* Obtain the News */
    List<INews> resolvedNews = new ArrayList<INews>();

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled(monitor))
      return;

    /* Determine Set of News States based on the filter */
    Type filter = fFilter.getType();
    Set<State> states;
    if (filter == Type.SHOW_NEW)
      states = EnumSet.of(INews.State.NEW);
    else if (filter == Type.SHOW_UNREAD)
      states = EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED);
    else
      states = INews.State.getVisible();

    /* Handle Folder, Newsbin and Saved Search or bookmark under certain circumstances */
    boolean needToFilter = true;
    if (input.isGetNewsRefsEfficient() || (input instanceof IBookMark && shouldResolveBookMarkWithSearch((IBookMark) input, filter))) {
      Triple<Boolean, Boolean, List<NewsReference>> result = getNewsRefsFromInput(input, fFilter, states, monitor);
      needToFilter = !result.getFirst();
      List<NewsReference> newsReferences = result.getThird();
      for (NewsReference newsRef : newsReferences) {

        /* Check if ContentProvider was already disposed or RSSOwl shutting down */
        if (canceled(monitor))
          return;

        INews resolvedNewsItem = null;

        /* Ask the local cache first */
        if (cacheCopy != null)
          resolvedNewsItem = cacheCopy.get(newsRef.getId());

        /* Otherwise resolve from DB */
        if (resolvedNewsItem == null)
          resolvedNewsItem = fNewsDao.load(newsRef.getId());

        /* Add if visible */
        if (resolvedNewsItem != null && resolvedNewsItem.isVisible())
          resolvedNews.add(resolvedNewsItem);

        /* News is null from a search, potential index issue - report it */
        else if (result.getSecond()) //TRUE if search was involved
          CoreUtils.reportIndexIssue();

        /* Never resolve more than MAX_RESOLVED_FOLDER_ELEMENTS for a folder */
        if (input instanceof FolderNewsMark && !fNoFolderLimit && resolvedNews.size() > MAX_RESOLVED_FOLDER_ELEMENTS)
          break;
      }

      /* Special treat folders and limit them by size */
      if (input instanceof FolderNewsMark)
        resolvedNews = limitFolderNewsMark(resolvedNews, comparer != null ? comparer : fFeedView.getComparator());
    }

    /* Resolve directly by state (check for news counts as optimization) */
    else if (shouldResolve(input, filter)) {
      resolvedNews.addAll(input.getNews(states));
    }

    /* Filter Elements as needed */
    if (needToFilter && isFilteredByOtherThanState())
      filterElements(resolvedNews);

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled(monitor))
      return;

    /* Add into Cache */
    for (INews news : resolvedNews) {
      fCachedNews.put(news.getId(), news);
    }
  }

  private boolean shouldResolveBookMarkWithSearch(IBookMark input, NewsFilter.Type filter) {

    /* Return if input is not a bookmark or not filtering at all */
    if (filter == Type.SHOW_ALL)
      return false;

    /* Return if filter can already quickly be handled from bookmark itself */
    if (filter == Type.SHOW_NEW || filter == Type.SHOW_UNREAD)
      return false;

    /* Check for number of new, unread and updated news */
    if (input.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)) > BOOKMARK_SCOPE_SEARCH_LIMIT)
      return true;

    /* Return if bookmark retention is setup, assuming that the number of elements is limited already */
    if (!hasRetentionLimit(input))
      return true;

    return false;
  }

  private boolean hasRetentionLimit(IBookMark bookmark) {
    IPreferenceScope preferences = Owl.getPreferenceService().getEntityScope(bookmark);

    /* High Retention: Read News Deleted */
    if (preferences.getBoolean(DefaultPreferences.DEL_READ_NEWS_STATE))
      return true;

    /* Medium Retention: Aged News Deleted */
    if (preferences.getBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE))
      return true;

    /* Low-High Retention: News Deleted by Count (Depends on actual count) */
    if (preferences.getBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE) && preferences.getInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE) <= BOOKMARK_SCOPE_SEARCH_LIMIT)
      return true;

    return false;
  }

  private boolean shouldResolve(INewsMark input, NewsFilter.Type filter) {

    /* Check for NEW News in Input */
    if (filter == Type.SHOW_NEW && input.getNewsCount(EnumSet.of(INews.State.NEW)) == 0)
      return false;

    /* Check for UNREAD News in Input */
    else if (filter == Type.SHOW_UNREAD && input.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)) == 0)
      return false;

    /* Check for Sticky News in Bookmark */
    else if (filter == Type.SHOW_STICKY && input instanceof IBookMark && ((IBookMark) input).getStickyNewsCount() == 0)
      return false;

    /* Check for Recent Date or 5 Days Age */
    else if ((filter == Type.SHOW_RECENT || filter == Type.SHOW_LAST_5_DAYS) && input instanceof IBookMark) {
      Date mostRecentNewsDate = ((IBookMark) input).getMostRecentNewsDate();
      if (mostRecentNewsDate != null) { //Date can be null e.g. when having more than 1 Bookmark for the same Feed (known issue)
        if (filter == Type.SHOW_RECENT && (mostRecentNewsDate.getTime() < (DateUtils.getToday().getTimeInMillis() - DateUtils.DAY)))
          return false;
        else if (filter == Type.SHOW_LAST_5_DAYS && (mostRecentNewsDate.getTime() < (DateUtils.getToday().getTimeInMillis() - 5 * DateUtils.DAY)))
          return false;
      }
    }

    return true;
  }

  private Triple<Boolean /* Filtered */, Boolean /* Searched */, List<NewsReference>> getNewsRefsFromInput(INewsMark input, NewsFilter newsFilter, Set<State> states, IProgressMonitor monitor) {
    Type filter = newsFilter.getType();

    /*
     * Optimization: If input is a saved search or bin with many results and the news filter is set to any condition that
     * is not scoped by news state, we get the results from a search to potentially get less results and so need less memory.
     */
    if (input instanceof ISearchMark || input instanceof INewsBin) {
      if (isFilteredByOtherThanState() && input.getNewsCount(states) > NEWSMARK_SCOPE_SEARCH_LIMIT) {
        ISearchCondition filterCondition = ModelUtils.getConditionForFilter(filter);
        List<SearchHit<NewsReference>> result = null;

        /* Inject into Saved Search */
        if (input instanceof ISearchMark) {
          ISearchMark searchMark = (ISearchMark) input;
          result = fSearch.searchNews(searchMark.getSearchConditions(), filterCondition, searchMark.matchAllConditions());
        }

        /* Location search for News Bin */
        else {
          INewsBin newsBin = (INewsBin) input;
          ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
          ISearchCondition locationCondition = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singleton((IFolderChild) newsBin)));
          result = fSearch.searchNews(Arrays.asList(locationCondition, filterCondition), true);
        }

        /* Fill Newsreferences from Search Results */
        List<NewsReference> newsRefs = new ArrayList<NewsReference>(result.size());
        for (SearchHit<NewsReference> item : result) {
          newsRefs.add(item.getResult());
        }

        return Triple.create(true, true, newsRefs);
      }
    }

    /* Resolve items from bookmark through searching inside */
    else if (input instanceof IBookMark) {
      IBookMark bookmark = (IBookMark) input;

      /* Return early if bookmark should not be resolved at all */
      if (!shouldResolve(bookmark, filter))
        return Triple.create(true, false, Collections.<NewsReference> emptyList());

      ISearchCondition filterCondition = ModelUtils.getConditionForFilter(filter);
      ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
      ISearchCondition locationCondition = fFactory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singleton((IFolderChild) bookmark)));
      List<SearchHit<NewsReference>> result = fSearch.searchNews(Arrays.asList(locationCondition, filterCondition), true);

      /* Fill Newsreferences from Search Results */
      List<NewsReference> newsRefs = new ArrayList<NewsReference>(result.size());
      for (SearchHit<NewsReference> item : result) {
        newsRefs.add(item.getResult());
      }

      return Triple.create(true, true, newsRefs);
    }

    /* Resolve Folder News Mark and pass in current filter */
    else if (input instanceof FolderNewsMark) {
      ((FolderNewsMark) input).resolve(filter, monitor);
      List<NewsReference> references = input.getNewsRefs(states);

      /* Optimization: If the folder has lots of elements and a text filter is set, limit result by this pattern */
      if (!fNoFolderLimit && input.getNewsCount(states) > MAX_FOLDER_ELEMENTS && newsFilter.isPatternSet()) {
        Iterator<NewsReference> iterator = references.iterator();
        while (iterator.hasNext()) {
          if (!newsFilter.isTextPatternMatch(iterator.next().getId()))
            iterator.remove();
        }
      }

      /* Optimization: If the folder contains more than MAX_RESOLVED_FOLDER_ELEMENTS, put the most recent at the beginning */
      if (!fNoFolderLimit && references.size() > MAX_RESOLVED_FOLDER_ELEMENTS)
        sortDescendingById(references);

      return Triple.create(true, true, references);
    }

    /* Return news refs by state */
    return Triple.create(false, false, input.getNewsRefs(states));
  }

  private void sortDescendingById(List<NewsReference> references) {
    Collections.sort(references, new Comparator<NewsReference>() {
      public int compare(NewsReference o1, NewsReference o2) {
        return o1.getId() > o2.getId() ? -1 : 1;
      }
    });
  }

  private synchronized Triple<Boolean /* Was Empty */, Collection<NewsEvent>, Collection<INews>> addToCache(Collection<NewsEvent> events, Collection<INews> addedNews) {
    boolean wasEmpty = fCachedNews.isEmpty();
    Collection<NewsEvent> visibleEvents = new ArrayList<NewsEvent>();
    Collection<INews> visibleNews = new ArrayList<INews>();

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled())
      return Triple.create(wasEmpty, visibleEvents, visibleNews);

    /* Use the filter (if set) to determine elements to cache */
    if (isFilteredByState() || isFilteredByOtherThanState()) {

      /* Quickly Map from News to Event */
      Map<INews, NewsEvent> mapNewsToEvent = new HashMap<INews, NewsEvent>(events.size());
      for (NewsEvent event : events) {
        mapNewsToEvent.put(event.getEntity(), event);
      }

      /* Filter the Added News */
      visibleNews.addAll(addedNews);
      filterElements(visibleNews);

      /* Also indicate related events for visible news */
      for (INews news : visibleNews)
        visibleEvents.add(mapNewsToEvent.get(news));
    }

    /* Not relevant for bookmarks, just add all */
    else {
      visibleEvents = events;
      visibleNews = addedNews;
    }

    /* Add to Cache */
    for (INews news : visibleNews) {
      fCachedNews.put(news.getId(), news);
    }

    /*
     * Since the folder news mark is bound to the lifecycle of the feedview,
     * make sure that the contents are updated properly from here.
     */
    if (fInput instanceof FolderNewsMark)
      ((FolderNewsMark) fInput).add(visibleNews);

    return Triple.create(wasEmpty, visibleEvents, visibleNews);
  }

  private synchronized Pair<List<NewsEvent>, List<INews>> updateCache(Set<NewsEvent> events, List<INews> updatedNews) {
    List<NewsEvent> visibleEvents = new ArrayList<NewsEvent>(events.size());
    List<INews> visibleNews = new ArrayList<INews>(updatedNews.size());

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled())
      return Pair.create(visibleEvents, visibleNews);

    for (NewsEvent event : events) {
      if (event.getEntity().getId() != null && fCachedNews.containsKey(event.getEntity().getId())) {
        visibleEvents.add(event);
        visibleNews.add(event.getEntity());
      }
    }

    return Pair.create(visibleEvents, visibleNews);
  }

  private synchronized Pair<List<NewsEvent>, List<INews>> removeFromCache(Set<NewsEvent> events, List<INews> deletedNews) {
    List<NewsEvent> visibleEvents = new ArrayList<NewsEvent>(events.size());
    List<INews> visibleNews = new ArrayList<INews>(deletedNews.size());

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled())
      return Pair.create(visibleEvents, visibleNews);

    /* Remove from Cache and keep track of contained items */
    for (NewsEvent event : events) {
      if (event.getEntity().getId() != null && fCachedNews.remove(event.getEntity().getId()) != null) {
        visibleEvents.add(event);
        visibleNews.add(event.getEntity());
      }
    }

    /*
     * Since the folder news mark is bound to the lifecycle of the feedview,
     * make sure that the contents are updated properly from here.
     */
    if (fInput instanceof FolderNewsMark)
      ((FolderNewsMark) fInput).remove(deletedNews);

    return Pair.create(visibleEvents, visibleNews);
  }

  private synchronized Pair<List<INews>, Boolean> newsChangedFromSearch(IProgressMonitor monitor, List<SearchMarkEvent> eventsRelatedToInput, boolean onlyHandleAddedNews) {

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled(monitor))
      return Pair.create(Collections.<INews> emptyList(), false);

    boolean needToFilter = true;
    boolean wasEmpty = fCachedNews.isEmpty();
    List<INews> addedNews = new ArrayList<INews>();

    /* Update Saved Search from Events */
    if (fInput instanceof ISearchMark) {

      /* Update cache alltogether based on search results */
      if (!onlyHandleAddedNews) {
        refreshCache(monitor, fInput);
        addedNews.addAll(fCachedNews.values());
        needToFilter = false;
      }

      /* Only show the added news */
      else {
        Set<Long> newsIds = extractNewsIds(eventsRelatedToInput);
        for (Long newsId : newsIds) {

          /* Skip already cached news */
          if (hasCachedNews(newsId))
            continue;

          /* Resolve News */
          INews news = fNewsDao.load(newsId);
          if (news != null && news.isVisible())
            addedNews.add(news);

          /* Check if ContentProvider was already disposed or RSSOwl shutting down */
          if (canceled(monitor))
            return Pair.create(Collections.<INews> emptyList(), false);
        }
      }
    }

    /* Update Folder News Mark from Events (we only add news, never remove) */
    else if (fInput instanceof FolderNewsMark) {
      FolderNewsMark folderNewsMark = (FolderNewsMark) fInput;
      Set<Long> newsIds = extractNewsIds(eventsRelatedToInput);
      for (Long newsId : newsIds) {

        /* Skip already cached news */
        if (hasCachedNews(newsId) || folderNewsMark.containsNews(newsId))
          continue;

        /* Resolve News */
        INews news = fNewsDao.load(newsId);
        if (news != null && news.isVisible())
          addedNews.add(news);

        /* Check if ContentProvider was already disposed or RSSOwl shutting down */
        if (canceled(monitor))
          return Pair.create(Collections.<INews> emptyList(), false);
      }
    }

    /* Check if ContentProvider was already disposed or RSSOwl shutting down */
    if (canceled(monitor))
      return Pair.create(Collections.<INews> emptyList(), false);

    /* Optimization: Only consider those news that pass the filter when news are added (or in general for Folder News Mark) */
    if (needToFilter && isFilteredByOtherThanState())
      filterElements(addedNews);

    /* Add to Cache */
    for (INews news : addedNews) {
      fCachedNews.put(news.getId(), news);
    }

    /* Add to Folder if necessary */
    if (fInput instanceof FolderNewsMark)
      ((FolderNewsMark) fInput).add(addedNews);

    return Pair.create(addedNews, wasEmpty);
  }

  private boolean isFilteredByState() {
    return fFilter.getType() == Type.SHOW_NEW || fFilter.getType() == Type.SHOW_UNREAD;
  }

  private boolean isFilteredByOtherThanState() {
    return fFilter.getType() == Type.SHOW_STICKY || fFilter.getType() == Type.SHOW_LABELED || fFilter.getType() == Type.SHOW_RECENT || fFilter.getType() == Type.SHOW_LAST_5_DAYS;
  }

  private void filterElements(Collection<INews> elements) {
    Iterator<INews> iterator = elements.iterator();
    while (iterator.hasNext()) {
      if (!fFilter.select(iterator.next(), true))
        iterator.remove();
    }
  }

  private Set<Long> extractNewsIds(List<SearchMarkEvent> events) {
    Set<Long> set = new HashSet<Long>();
    for (SearchMarkEvent event : events) {
      LongArrayList[] newsIds = ((SearchMark) event.getEntity()).internalGetNewsContainer().internalGetNewsIds();
      for (int i = 0; i < newsIds.length; i++) {

        /* Ignore hidden/deleted and states that are filtered */
        if (i == INews.State.HIDDEN.ordinal() || i == INews.State.DELETED.ordinal())
          continue;
        else if (fFilter.getType() == Type.SHOW_NEW && i != INews.State.NEW.ordinal())
          continue;
        else if (fFilter.getType() == Type.SHOW_UNREAD && i == INews.State.READ.ordinal())
          continue;

        long[] elements = newsIds[i].getElements();
        for (long element : elements) {
          if (element > 0)
            set.add(element);
        }
      }
    }

    return set;
  }

  private List<INews> limitFolderNewsMark(List<INews> resolvedNews, NewsComparator comparer) {

    /* Return if no capping is required at all */
    if (fNoFolderLimit || resolvedNews.size() <= MAX_FOLDER_ELEMENTS)
      return resolvedNews;

    /* First add those news that are Labeled or Sticky if this group mode is active */
    List<INews> priorityItems = Collections.emptyList();
    if (isGroupingByLabel() || isGroupingByStickyness()) {
      priorityItems = new ArrayList<INews>();
      for (INews news : resolvedNews) {
        if (isGroupingByLabel() && !news.getLabels().isEmpty() || isGroupingByStickyness() && news.isFlagged())
          priorityItems.add(news);
      }
    }

    /* Check if Labeled/Sticky News already at limit size and return then */
    if (priorityItems.size() >= MAX_FOLDER_ELEMENTS)
      return priorityItems;

    /* Need to sort now to pick the top N remaining elements */
    Object[] elements = resolvedNews.toArray();
    comparer.sort(null, elements);

    /* Pick top N remaining Elements */
    int limit = MAX_FOLDER_ELEMENTS - priorityItems.size();
    List<INews> limitedResult = new ArrayList<INews>(Math.min(elements.length, MAX_FOLDER_ELEMENTS));
    for (int i = 0, c = 0; i < elements.length && c < limit; i++) {
      INews news = (INews) elements[i];
      if (!priorityItems.contains(news)) {
        limitedResult.add(news);
        c++;
      }
    }

    /* Fill in priority items if any */
    limitedResult.addAll(priorityItems);

    return limitedResult;
  }

  synchronized INewsMark getInput() {
    return fInput;
  }

  synchronized Collection<INews> getCachedNewsCopy() {
    return new ArrayList<INews>(fCachedNews.values());
  }

  synchronized boolean hasCachedNews() {
    return !fCachedNews.isEmpty();
  }

  synchronized boolean hasCachedNews(INews news) {
    return news.getId() != null && hasCachedNews(news.getId());
  }

  private synchronized boolean hasCachedNews(long newsId) {
    return fCachedNews.containsKey(newsId);
  }

  private synchronized INews obtainFromCache(NewsReference ref) {
    return obtainFromCache(ref.getId());
  }

  synchronized INews obtainFromCache(long newsId) {
    return fCachedNews.get(newsId);
  }

  private void registerListeners() {

    /* Saved Search Listener */
    fSearchMarkListener = new SearchMarkAdapter() {
      @Override
      public void newsChanged(Set<SearchMarkEvent> events) {
        final List<SearchMarkEvent> eventsRelatedToInput = new ArrayList<SearchMarkEvent>(1);

        /* Check if ContentProvider was already disposed or RSSOwl shutting down */
        if (canceled())
          return;

        /* Find those events that are related to the current input */
        for (SearchMarkEvent event : events) {
          ISearchMark searchMark = event.getEntity();
          if (fInput.equals(searchMark)) {
            eventsRelatedToInput.add(event);
            break; //Can only be one search mark per feed view
          } else if (fInput instanceof FolderNewsMark && ((FolderNewsMark) fInput).isRelatedTo(searchMark)) {
            eventsRelatedToInput.add(event);
          }
        }

        /* Check if ContentProvider was already disposed or RSSOwl shutting down */
        if (canceled())
          return;

        /* Properly update given searches are related to input */
        if (!eventsRelatedToInput.isEmpty()) {
          JobRunner.runInUIThread(fFeedView.getEditorControl(), new Runnable() {
            public void run() {
              final boolean onlyHandleAddedNews = fFeedView.isVisible();

              JobRunner.runUIUpdater(new UIBackgroundJob(fFeedView.getEditorControl()) {
                private List<INews> fAddedNews;
                private boolean fWasEmpty;

                @Override
                protected void runInBackground(IProgressMonitor monitor) {
                  if (canceled(monitor))
                    return;

                  Pair<List<INews>, Boolean> result = newsChangedFromSearch(monitor, eventsRelatedToInput, onlyHandleAddedNews);
                  fAddedNews = result.getFirst();
                  fWasEmpty = result.getSecond();
                }

                @Override
                protected void runInUI(IProgressMonitor monitor) {
                  if (canceled(monitor))
                    return;

                  /* Check if we need to Refresh at all */
                  if (onlyHandleAddedNews && (fAddedNews == null || fAddedNews.size() == 0))
                    return;

                  /* Refresh only Table Viewer if not using Newspaper Mode in Browser */
                  if (!browserShowsCollection())
                    fFeedView.refreshTableViewer(true, true); //TODO Seems some JFace caching problem here (redraw=true)

                  /* Browser shows Newspaper Mode: Only refresh under certain circumstances */
                  else {
                    if (canDoBrowserRefresh(fWasEmpty))
                      fFeedView.refreshBrowserViewer();
                    else
                      fFeedView.getNewsBrowserControl().setInfoBarVisible(true);
                  }
                }
              });
            }
          });

          /* Done */
          return;
        }
      }
    };

    DynamicDAO.addEntityListener(ISearchMark.class, fSearchMarkListener);

    /* News Listener */
    fNewsListener = new NewsAdapter() {

      /* News got Added */
      @Override
      public void entitiesAdded(final Set<NewsEvent> events) {
        JobRunner.runInUIThread(fFeedView.getEditorControl(), new Runnable() {
          public void run() {
            Set<NewsEvent> addedNews = null;

            /* Check if ContentProvider was already disposed or RSSOwl shutting down */
            if (canceled())
              return;

            /* Filter News which are from a different Feed than displayed */
            for (NewsEvent event : events) {
              if (event.getEntity().isVisible() && isInputRelatedTo(event, NewsEventType.PERSISTED)) {
                if (addedNews == null)
                  addedNews = new HashSet<NewsEvent>();

                addedNews.add(event);
              }

              /* Return on Shutdown or disposal */
              if (canceled())
                return;
            }

            /* Event not interesting for us or we are disposed */
            if (addedNews == null || addedNews.size() == 0)
              return;

            /* Handle */
            boolean refresh = handleAddedNews(addedNews);
            if (refresh) {
              if (!browserShowsCollection())
                fFeedView.refreshTableViewer(true, false);
              else
                fFeedView.refresh(true, false);
            }
          }
        });
      }

      /* News got Updated */
      @Override
      public void entitiesUpdated(final Set<NewsEvent> events) {
        JobRunner.runInUIThread(fFeedView.getEditorControl(), new Runnable() {
          public void run() {
            Set<NewsEvent> restoredNews = null;
            Set<NewsEvent> updatedNews = null;
            Set<NewsEvent> deletedNews = null;

            /* Check if ContentProvider was already disposed or RSSOwl shutting down */
            if (canceled())
              return;

            /* Filter News which are from a different Feed than displayed */
            for (NewsEvent event : events) {
              boolean isRestored = gotRestored(event, fFilter.getType());
              INews news = event.getEntity();

              /* Return on Shutdown or disposal */
              if (canceled())
                return;

              /* Check if input relates to news events */
              if (isInputRelatedTo(event, isRestored ? NewsEventType.RESTORED : NewsEventType.UPDATED)) {

                /* News got Deleted */
                if (!news.isVisible()) {
                  if (deletedNews == null)
                    deletedNews = new HashSet<NewsEvent>();

                  deletedNews.add(event);
                }

                /* News got Restored */
                else if (isRestored) {
                  if (restoredNews == null)
                    restoredNews = new HashSet<NewsEvent>();

                  restoredNews.add(event);
                }

                /* News got Updated */
                else {
                  if (updatedNews == null)
                    updatedNews = new HashSet<NewsEvent>();

                  updatedNews.add(event);
                }
              }
            }

            /* Return on Shutdown or disposal */
            if (canceled())
              return;

            boolean refresh = false;
            boolean updateSelectionFromDelete = false;

            /* Handle Restored News */
            if (restoredNews != null && !restoredNews.isEmpty())
              refresh = handleAddedNews(restoredNews);

            /* Handle Updated News */
            if (updatedNews != null && !updatedNews.isEmpty())
              refresh = handleUpdatedNews(updatedNews);

            /* Handle Deleted News */
            if (deletedNews != null && !deletedNews.isEmpty()) {
              refresh = handleDeletedNews(deletedNews);
              updateSelectionFromDelete = refresh;
            }

            /* Check if ContentProvider was already disposed or RSSOwl shutting down */
            if (canceled())
              return;

            /* Refresh and update selection due to deletion */
            if (updateSelectionFromDelete) {
              fTableViewer.updateSelectionAfterDelete(new Runnable() {
                public void run() {
                  refreshViewers(events, NewsEventType.REMOVED);
                }
              });
            }

            /* Normal refresh w/o deletion */
            else if (refresh)
              refreshViewers(events, NewsEventType.UPDATED);
          }
        });
      }

      /* News got Deleted */
      @Override
      public void entitiesDeleted(final Set<NewsEvent> events) {
        JobRunner.runInUIThread(fFeedView.getEditorControl(), new Runnable() {
          public void run() {
            Set<NewsEvent> deletedNews = null;

            /* Check if ContentProvider was already disposed or RSSOwl shutting down */
            if (canceled())
              return;

            /* Filter News which are from a different Feed than displayed */
            for (NewsEvent event : events) {
              INews news = event.getEntity();
              if ((news.isVisible() || news.getParentId() != 0) && isInputRelatedTo(event, NewsEventType.REMOVED)) {
                if (deletedNews == null)
                  deletedNews = new HashSet<NewsEvent>();

                deletedNews.add(event);
              }

              /* Return on Shutdown or disposal */
              if (canceled())
                return;
            }

            /* Event not interesting for us or we are disposed */
            if (deletedNews == null || deletedNews.size() == 0)
              return;

            /* Handle Deleted News */
            boolean refresh = handleDeletedNews(deletedNews);

            /* Check if ContentProvider was already disposed or RSSOwl shutting down */
            if (canceled())
              return;

            /* Handle Refresh */
            if (refresh) {
              if (!browserShowsCollection())
                fFeedView.refreshTableViewer(true, false);
              else
                fFeedView.refresh(true, false);
            }
          }
        });
      }
    };

    DynamicDAO.addEntityListener(INews.class, fNewsListener);
  }

  private boolean gotRestored(NewsEvent event, NewsFilter.Type filter) {
    INews news = event.getEntity();
    INews old = event.getOldNews();

    /* Quickly check common conditions under which the news can not be a restored one */
    if (news == null || old == null || !news.isVisible() || hasCachedNews(news))
      return false;

    INews.State newState = news.getState();
    INews.State oldState = old.getState();

    /* Restored: Deletion was undone */
    if (oldState == INews.State.HIDDEN || oldState == INews.State.DELETED)
      return true;

    /* Check if new state matches filter now */
    switch (filter) {
      case SHOW_NEW:
        return newState == INews.State.NEW && oldState != INews.State.NEW;

      case SHOW_UNREAD:
        return newState != INews.State.READ && oldState == INews.State.READ;

      case SHOW_STICKY:
        return CoreUtils.isStickyStateChange(Collections.singleton(event), true);

      case SHOW_LABELED:
        return CoreUtils.isLabelChange(Collections.singleton(event), true);
    }

    return false;
  }

  private void refreshViewers(final Set<NewsEvent> events, NewsEventType type) {

    /* Return on Shutdown or disposal */
    if (canceled())
      return;

    /*
     * Optimization: The Browser is likely only showing a single news and thus
     * there is no need to refresh the entire content but rather use the update
     * instead.
     */
    if (!browserShowsCollection()) {
      List<INews> items = new ArrayList<INews>(events.size());
      for (NewsEvent event : events) {
        items.add(event.getEntity());
      }

      /* Update Browser Viewer */
      if (fFeedView.isBrowserViewerVisible() && contains(fBrowserViewer.getInput(), items)) {

        /* Update */
        if (type == NewsEventType.UPDATED) {
          Set<NewsEvent> newsToUpdate = events;

          /*
           * Optimization: If more than a single news is to update, check
           * if the Browser only shows a single news to avoid a full refresh.
           */
          if (events.size() > 1) {
            NewsEvent event = findShowingEventFromBrowser(events);
            if (event != null)
              newsToUpdate = Collections.singleton(event);
          }

          fBrowserViewer.update(newsToUpdate);
        }

        /* Remove */
        else if (type == NewsEventType.REMOVED)
          fBrowserViewer.remove(items.toArray());
      }

      /* Check if ContentProvider was already disposed or RSSOwl shutting down */
      if (canceled())
        return;

      /* Refresh Table Viewer */
      fFeedView.refreshTableViewer(true, true);
    }

    /* Browser is showing Collection, thereby perform a refresh */
    else
      fFeedView.refresh(true, true);
  }

  private boolean handleAddedNews(Set<NewsEvent> events) {

    /*
     * Input can be NULL if this listener was called before NewsTableControl.setPartInput()
     * has been called (can happen if the viewer has thousands of items to load)
     */
    if (fFeedView.isTableViewerVisible() && fTableViewer.getInput() == null)
      return false;

    /* Receive added News */
    List<INews> addedNews = new ArrayList<INews>(events.size());
    for (NewsEvent event : events) {
      addedNews.add(event.getEntity());
    }

    /* Add to Cache */
    Triple<Boolean, Collection<NewsEvent>, Collection<INews>> result = addToCache(events, addedNews);
    boolean wasEmpty = result.getFirst();
    Collection<NewsEvent> visibleEvents = result.getSecond();
    Collection<INews> visibleNews = result.getThird();

    /* Return early if a refresh is required anyways */
    if (fGrouping.needsRefresh(visibleEvents, false)) {

      /* Avoid a refresh when user is reading a filled newspaper view at the moment */
      if (!browserShowsCollection() || canDoBrowserRefresh(wasEmpty, visibleEvents))
        return true;
    }

    /* Return on Shutdown or disposal */
    if (canceled())
      return false;

    /* Add to Viewers */
    addToViewers(visibleNews, visibleEvents, wasEmpty);

    return false;
  }

  /* Add a List of News to Table and Browser Viewers */
  private void addToViewers(Collection<INews> addedNews, Collection<NewsEvent> events, boolean wasEmpty) {

    /* Return on Shutdown or disposal */
    if (canceled())
      return;

    /* Return early if nothing to do */
    if (addedNews.isEmpty())
      return;

    /* Add to Table-Viewer if Visible (keep top item and selection stable) */
    if (fFeedView.isTableViewerVisible()) {
      Tree tree = fTableViewer.getTree();
      TreeItem topItem = tree.getTopItem();
      int indexOfTopItem = 0;
      if (topItem != null)
        indexOfTopItem = tree.indexOf(topItem);

      tree.setRedraw(false);
      try {
        fTableViewer.add(fTableViewer.getInput(), addedNews.toArray());
        if (topItem != null && indexOfTopItem != 0)
          tree.setTopItem(topItem);
      } finally {
        tree.setRedraw(true);
      }
    }

    /* Add to Browser-Viewer if showing entire Feed */
    else if (browserShowsCollection()) {

      /* Feedview is active and user reads news, thereby only show info about added news */
      if (!canDoBrowserRefresh(wasEmpty, events))
        fFeedView.getNewsBrowserControl().setInfoBarVisible(true);

      /* Otherwise refresh the browser viewer to show added news */
      else
        fBrowserViewer.add(fBrowserViewer.getInput(), addedNews.toArray());
    }
  }

  /* Some conditions under which a browser refresh is tolerated */
  @SuppressWarnings("unchecked")
  private boolean canDoBrowserRefresh(boolean wasEmpty) {
    return canDoBrowserRefresh(wasEmpty, Collections.EMPTY_SET);
  }

  /* Some conditions under which a browser refresh is tolerated */
  private boolean canDoBrowserRefresh(boolean wasEmpty, Collection<NewsEvent> events) {
    return (wasEmpty || !fFeedView.isVisible() || OwlUI.isMinimized() || CoreUtils.gotRestored(events));
  }

  /* Browser shows collection if maximized */
  private boolean browserShowsCollection() {
    Object input = fBrowserViewer.getInput();
    return (input instanceof BookMarkReference || input instanceof NewsBinReference || input instanceof SearchMarkReference || input instanceof FolderNewsMarkReference);
  }

  private boolean handleUpdatedNews(Set<NewsEvent> events) {

    /* Receive updated News */
    List<INews> updatedNews = new ArrayList<INews>(events.size());
    for (NewsEvent event : events) {
      updatedNews.add(event.getEntity());
    }

    /* Update Cache */
    Pair<List<NewsEvent>, List<INews>> result = updateCache(events, updatedNews);
    final List<NewsEvent> visibleEvents = result.getFirst();
    List<INews> visibleNews = result.getSecond();

    /* Return if news was not part of cache at all (e.g. limited Folder News Mark) */
    if (visibleNews.isEmpty())
      return false;

    /* Return on Shutdown or disposal */
    if (canceled())
      return false;

    /* Return early if refresh is required anyways for Grouper */
    if (fGrouping.needsRefresh(visibleEvents, true))
      return true;

    /* Return early if refresh is required anyways for Sorter */
    if (fFeedView.isTableViewerVisible()) { //Only makes sense if Browser not maximized
      ViewerComparator sorter = fTableViewer.getComparator();
      if (sorter instanceof NewsComparator && ((NewsComparator) sorter).needsRefresh(visibleEvents))
        return true;
    }

    /* Update in Table-Viewer */
    if (fFeedView.isTableViewerVisible())
      fTableViewer.update(visibleNews.toArray(), null);

    /* Update in Browser-Viewer */
    if (fFeedView.isBrowserViewerVisible() && contains(fBrowserViewer.getInput(), visibleNews)) {
      Collection<NewsEvent> newsToUpdate = visibleEvents;

      /*
       * Optimization: If more than a single news is to update, check
       * if the Browser only shows a single news to avoid a full refresh.
       */
      if (visibleEvents.size() > 1) {
        NewsEvent event = findShowingEventFromBrowser(visibleEvents);
        if (event != null)
          newsToUpdate = Collections.singleton(event);
      }

      fBrowserViewer.update(newsToUpdate);
    }

    return false;
  }

  private boolean handleDeletedNews(Set<NewsEvent> events) {

    /* Receive deleted News */
    List<INews> deletedNews = new ArrayList<INews>(events.size());
    for (NewsEvent event : events) {
      deletedNews.add(event.getEntity());
    }

    /* Remove from Cache */
    Pair<List<NewsEvent>, List<INews>> result = removeFromCache(events, deletedNews);
    List<NewsEvent> visibleEvents = result.getFirst();
    List<INews> visibleNews = result.getSecond();

    /* Return if news was not part of cache at all (e.g. limited Folder News Mark) */
    if (visibleNews.isEmpty())
      return false;

    /* Return on Shutdown or disposal */
    if (canceled())
      return false;

    /* Only refresh if grouping requires this from table viewer */
    if (isGroupingEnabled() && fFeedView.isTableViewerVisible() && fGrouping.needsRefresh(visibleEvents, false))
      return true;

    /* Otherwise: Remove from Table-Viewer */
    if (fFeedView.isTableViewerVisible())
      fTableViewer.remove(visibleNews.toArray());

    /* And: Remove from Browser-Viewer */
    if (fFeedView.isBrowserViewerVisible() && contains(fBrowserViewer.getInput(), visibleNews))
      fBrowserViewer.remove(visibleNews.toArray());

    return false;
  }

  private void unregisterListeners() {
    DynamicDAO.removeEntityListener(INews.class, fNewsListener);
    DynamicDAO.removeEntityListener(ISearchMark.class, fSearchMarkListener);
  }

  private boolean isInputRelatedTo(NewsEvent event, NewsEventType type) {
    INews news = event.getEntity();

    /* Check if BookMark references the News' Feed and is not a copy */
    if (fInput instanceof IBookMark) {

      /* Return early if news is from bin */
      if (news.getParentId() != 0)
        return false;

      /* Perform fast HashMap lookup first */
      if (hasCachedNews(news))
        return true;

      /* Otherwise compare by feed link */
      IBookMark bookmark = (IBookMark) fInput;
      if (bookmark.getFeedLinkReference().equals(news.getFeedReference()))
        return true;
    }

    /* Check if Saved Search contains the given News */
    else if (type != NewsEventType.PERSISTED && fInput instanceof ISearchMark) {
      return hasCachedNews(news) || fInput.containsNews(news);
    }

    /* Update / Remove: Check if News points to this Bin */
    else if (fInput instanceof INewsBin) {
      return news.getParentId() == fInput.getId();
    }

    /* In Memory Folder News Mark (aggregated news) */
    else if (fInput instanceof FolderNewsMark) {

      /* Perform fast HashMap lookup first */
      if (hasCachedNews(news))
        return true;

      /* Ask FolderNewsMark directly */
      return ((FolderNewsMark) fInput).isRelatedTo(news);
    }

    return false;
  }

  private boolean contains(Object input, List<INews> list) {

    /* Can only belong to this Feed since filtered before already */
    if (input instanceof BookMarkReference || input instanceof NewsBinReference || input instanceof SearchMarkReference || input instanceof FolderNewsMarkReference)
      return true;

    /* News */
    else if (input instanceof INews)
      return list.contains(input);

    /* Entity Group */
    else if (input instanceof EntityGroup) {
      List<EntityGroupItem> items = ((EntityGroup) input).getItems();
      for (EntityGroupItem item : items) {
        if (list.contains(item.getEntity()))
          return true;
      }
    }

    /* Other Input */
    else if (input instanceof Object[]) {
      Object inputNews[] = (Object[]) input;
      for (Object inputNewsItem : inputNews) {
        if (list.contains(inputNewsItem))
          return true;
      }
    }

    return false;
  }

  private NewsEvent findShowingEventFromBrowser(Collection<NewsEvent> events) {
    Object input = fBrowserViewer.getInput();
    if (input instanceof INews) {
      INews news = (INews) input;
      for (NewsEvent event : events) {
        if (news.equals(event.getEntity()))
          return event;
      }
    }

    return null;
  }

  private boolean canceled() {
    return canceled(null);
  }

  private boolean canceled(IProgressMonitor monitor) {
    return fDisposed.get() || Controller.getDefault().isShuttingDown() || (monitor != null && monitor.isCanceled());
  }
}
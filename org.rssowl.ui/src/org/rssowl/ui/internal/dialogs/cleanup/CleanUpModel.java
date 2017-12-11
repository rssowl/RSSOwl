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

package org.rssowl.ui.internal.dialogs.cleanup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.IAbortable;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.SearchHit;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
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
import java.util.TreeSet;

/**
 * Creates the collection of <code>CleanUpTask</code> that the user may choose
 * to perform as clean up.
 *
 * @author bpasero
 */
public class CleanUpModel {

  /* One Day in millis */
  private static final long DAY = 24 * 60 * 60 * 1000;

  private final List<CleanUpGroup> fTasks;
  private final CleanUpOperations fOps;
  private final Collection<IBookMark> fBookmarks;
  private final IModelFactory fFactory;
  private final IModelSearch fModelSearch;
  private final ISearchField fLocationField;
  private final String fNewsName;
  private final INewsDAO fNewsDao;
  private final IPreferenceScope fPreferences;

  /**
   * @param operations
   * @param bookmarks
   */
  public CleanUpModel(CleanUpOperations operations, Collection<IBookMark> bookmarks) {
    fOps = operations;
    fBookmarks = bookmarks;
    fTasks = new ArrayList<CleanUpGroup>();
    fFactory = Owl.getModelFactory();
    fModelSearch = Owl.getPersistenceService().getModelSearch();
    fNewsDao = DynamicDAO.getDAO(INewsDAO.class);
    fPreferences = Owl.getPreferenceService().getGlobalScope();

    String newsName = INews.class.getName();
    fLocationField = fFactory.createSearchField(INews.LOCATION, newsName);
    fNewsName = INews.class.getName();
  }

  /**
   * @return Returns the Task Groups
   */
  public List<CleanUpGroup> getTasks() {
    return fTasks;
  }

  private ISearchCondition getLocationCondition(IBookMark mark) {
    Long[][] value = new Long[3][1];
    value[1][0] = mark.getId();
    return fFactory.createSearchCondition(fLocationField, SearchSpecifier.IS, value);
  }

  /**
   * Calculate Tasks
   *
   * @param monitor
   */
  public void generate(IProgressMonitor monitor) {
    Set<IBookMark> bookmarksToDelete = new HashSet<IBookMark>();
    Map<IBookMark, Set<NewsReference>> newsToDelete = new HashMap<IBookMark, Set<NewsReference>>();

    /* 0.) Create Recommended Tasks */
    CleanUpGroup recommendedTasks = new CleanUpGroup(Messages.CleanUpModel_RECOMMENDED_OPS);
    if (fPreferences.getBoolean(DefaultPreferences.CLEAN_UP_INDEX))
      recommendedTasks.addTask(new CleanUpIndexTask(recommendedTasks));
    recommendedTasks.addTask(new DefragDatabaseTask(recommendedTasks));
    recommendedTasks.addTask(new OptimizeSearchTask(recommendedTasks));
    fTasks.add(recommendedTasks);

    /* Look for orphaned saved searches */
    {
      List<ISearchMark> orphanedSearches = new ArrayList<ISearchMark>();
      Collection<ISearchMark> searches = DynamicDAO.loadAll(ISearchMark.class);
      for (ISearchMark search : searches) {
        if (CoreUtils.isOrphaned(search))
          orphanedSearches.add(search);
      }

      if (!orphanedSearches.isEmpty())
        recommendedTasks.addTask(new DeleteOrphanedSearchMarkTask(recommendedTasks, orphanedSearches));
    }

    /* Look for orphaned news filters */
    {
      List<ISearchFilter> orphanedFilters = new ArrayList<ISearchFilter>();
      Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
      for (ISearchFilter filter : filters) {
        if (filter.isEnabled() && CoreUtils.isOrphaned(filter))
          orphanedFilters.add(filter);
      }

      if (!orphanedFilters.isEmpty())
        recommendedTasks.addTask(new DisableOrphanedNewsFiltersTask(recommendedTasks, orphanedFilters));
    }

    /* Return if user cancelled the preview */
    if (monitor.isCanceled())
      return;

    /* 1.) Delete BookMarks that have Last Visit > X Days ago */
    if (fOps.deleteFeedByLastVisit()) {
      CleanUpGroup group = new CleanUpGroup(NLS.bind(Messages.CleanUpModel_DELETE_BY_VISIT, fOps.getLastVisitDays()));

      int days = fOps.getLastVisitDays();
      long maxLastVisitDate = DateUtils.getToday().getTimeInMillis() - (days * DAY);

      for (IBookMark mark : fBookmarks) {
        Date date = mark.getLastVisitDate();

        /* Use Creation Date if mark was never visited */
        if (date == null)
          date = mark.getCreationDate();

        if (date == null || date.getTime() < maxLastVisitDate) {
          bookmarksToDelete.add(mark);
          group.addTask(new BookMarkTask(group, mark));
        }
      }

      if (!group.isEmpty())
        fTasks.add(group);
    }

    /* Return if user cancelled the preview */
    if (monitor.isCanceled())
      return;

    /* 2.) Delete BookMarks that have not updated in X Days */
    if (fOps.deleteFeedByLastUpdate()) {
      CleanUpGroup group = new CleanUpGroup(NLS.bind(Messages.CleanUpModel_DELETE_BY_UPDATE, fOps.getLastUpdateDays()));

      int days = fOps.getLastUpdateDays();
      long maxLastUpdateDate = DateUtils.getToday().getTimeInMillis() - (days * DAY);

      /* For each selected Bookmark */
      for (IBookMark mark : fBookmarks) {

        /* Ignore if Bookmark gets already deleted */
        if (bookmarksToDelete.contains(mark))
          continue;

        Date mostRecentNewsDate = mark.getMostRecentNewsDate();
        Date creationDate = mark.getCreationDate();
        boolean deleteBookMark = false;

        /* Ask for most recent news date if present */
        if (mostRecentNewsDate != null && mostRecentNewsDate.getTime() < maxLastUpdateDate)
          deleteBookMark = true;

        /* Alternatively check for creation date */
        else if (mostRecentNewsDate == null && creationDate.getTime() < maxLastUpdateDate)
          deleteBookMark = true;

        if (deleteBookMark) {
          bookmarksToDelete.add(mark);
          group.addTask(new BookMarkTask(group, mark));
        }
      }

      if (!group.isEmpty())
        fTasks.add(group);
    }

    /* Return if user cancelled the preview */
    if (monitor.isCanceled())
      return;

    /* 3.) Delete BookMarks that have Connection Error */
    if (fOps.deleteFeedsByConError()) {
      CleanUpGroup group = new CleanUpGroup(Messages.CleanUpModel_DELETE_CON_ERROR);

      for (IBookMark mark : fBookmarks) {
        if (!bookmarksToDelete.contains(mark) && mark.isErrorLoading()) {
          bookmarksToDelete.add(mark);
          group.addTask(new BookMarkTask(group, mark));
        }
      }

      if (!group.isEmpty())
        fTasks.add(group);
    }

    /* Return if user cancelled the preview */
    if (monitor.isCanceled())
      return;

    /* 4.) Delete Duplicate BookMarks */
    if (fOps.deleteFeedsByDuplicates()) {
      CleanUpGroup group = new CleanUpGroup(Messages.CleanUpModel_DELETE_DUPLICATES);

      for (IBookMark currentBookMark : fBookmarks) {
        if (!bookmarksToDelete.contains(currentBookMark)) {

          /* Group of Bookmarks referencing the same Feed sorted by Creation Date */
          Set<IBookMark> sortedBookmarkGroup = new TreeSet<IBookMark>(new Comparator<IBookMark>() {
            @Override
            public int compare(IBookMark o1, IBookMark o2) {
              if (o1.equals(o2))
                return 0;

              return o1.getCreationDate() == null ? -1 : o1.getCreationDate().compareTo(o2.getCreationDate());
            }
          });

          /* Add Current Bookmark and Duplicates */
          for (IBookMark bookMark : fBookmarks) {
            if (!bookmarksToDelete.contains(bookMark) && bookMark.getFeedLinkReference().equals(currentBookMark.getFeedLinkReference())) {
              sortedBookmarkGroup.add(bookMark);
            }
          }

          /* Delete most recent duplicates if any */
          if (sortedBookmarkGroup.size() > 1) {
            Iterator<IBookMark> iterator = sortedBookmarkGroup.iterator();
            iterator.next(); // Ignore first, oldest one

            while (iterator.hasNext()) {
              IBookMark bookmark = iterator.next();
              bookmarksToDelete.add(bookmark);
              group.addTask(new BookMarkTask(group, bookmark));
            }
          }
        }
      }

      if (!group.isEmpty())
        fTasks.add(group);
    }

    /* 5.) Delete BookMarks no longer subscribed to in Google Reader */
    if (fOps.deleteFeedsBySynchronization()) {
      CleanUpGroup group = new CleanUpGroup(Messages.CleanUpModel_DELETE_UNSUBSCRIBED_FEEDS);
      Set<String> googleReaderFeeds = loadGoogleReaderFeeds(monitor);

      if (googleReaderFeeds != null) {
        for (IBookMark mark : fBookmarks) {
          if (bookmarksToDelete.contains(mark) || !SyncUtils.isSynchronized(mark))
            continue;

          String feedLink = URIUtils.toHTTP(mark.getFeedLinkReference().getLinkAsText());
          if (!googleReaderFeeds.contains(feedLink)) {
            bookmarksToDelete.add(mark);
            group.addTask(new BookMarkTask(group, mark));
          }
        }
      }

      if (!group.isEmpty())
        fTasks.add(group);
    }

    /* Return if user cancelled the preview */
    if (monitor.isCanceled())
      return;

    /* Reusable State Condition */
    EnumSet<State> states = fOps.keepUnreadNews() ? EnumSet.of(INews.State.READ) : EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED, INews.State.READ);
    ISearchField stateField = fFactory.createSearchField(INews.STATE, fNewsName);
    ISearchCondition stateCondition = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, states);

    /* Reusable Sticky Condition */
    ISearchField stickyField = fFactory.createSearchField(INews.IS_FLAGGED, fNewsName);
    ISearchCondition stickyCondition = fFactory.createSearchCondition(stickyField, SearchSpecifier.IS_NOT, true);

    /* Reusable Label Condition */
    Collection<ILabel> labels = DynamicDAO.getDAO(ILabelDAO.class).loadAll();
    ISearchField labelField = fFactory.createSearchField(INews.LABEL, fNewsName);
    List<ISearchCondition> labelConditions = new ArrayList<ISearchCondition>(labels.size());
    for (ILabel label : labels) {
      labelConditions.add(fFactory.createSearchCondition(labelField, SearchSpecifier.IS_NOT, label.getName()));
    }

    /* 4.) Delete News that exceed a certain limit in a Feed */
    if (fOps.deleteNewsByCount()) {
      CleanUpGroup group = new CleanUpGroup(NLS.bind(Messages.CleanUpModel_DELETE_BY_COUNT, fOps.getMaxNewsCountPerFeed()));

      /* For each selected Bookmark */
      for (IBookMark mark : fBookmarks) {

        /* Return if user cancelled the preview */
        if (monitor.isCanceled())
          return;

        /* Ignore if Bookmark gets already deleted */
        if (bookmarksToDelete.contains(mark))
          continue;

        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(3);
        conditions.add(getLocationCondition(mark));
        conditions.add(stickyCondition);
        conditions.add(stateCondition);
        if (fOps.keepLabeledNews())
          conditions.addAll(labelConditions);

        /* Check if result count exceeds limit */
        List<SearchHit<NewsReference>> results = filterInvalidResults(fModelSearch.searchNews(conditions, true), monitor);
        if (results.size() > fOps.getMaxNewsCountPerFeed()) {
          int toDeleteValue = results.size() - fOps.getMaxNewsCountPerFeed();

          /* Resolve News */
          List<INews> resolvedNews = new ArrayList<INews>(results.size());
          for (SearchHit<NewsReference> result : results) {
            if (monitor.isCanceled())
              return;

            INews resolvedNewsItem = result.getResult().resolve();
            if (resolvedNewsItem != null && resolvedNewsItem.isVisible())
              resolvedNews.add(resolvedNewsItem);
            else
              CoreUtils.reportIndexIssue();
          }

          /* Return if user cancelled the preview */
          if (monitor.isCanceled())
            return;

          /* Sort by Date */
          Collections.sort(resolvedNews, new Comparator<INews>() {
            @Override
            public int compare(INews news1, INews news2) {
              return DateUtils.getRecentDate(news1).compareTo(DateUtils.getRecentDate(news2));
            }
          });

          /* Return if user cancelled the preview */
          if (monitor.isCanceled())
            return;

          Set<NewsReference> newsOfMarkToDelete = new HashSet<NewsReference>();
          for (int i = 0; i < resolvedNews.size() && i < toDeleteValue; i++)
            newsOfMarkToDelete.add(new NewsReference(resolvedNews.get(i).getId()));

          if (!newsOfMarkToDelete.isEmpty() && !monitor.isCanceled()) {
            newsToDelete.put(mark, newsOfMarkToDelete);
            group.addTask(new NewsTask(group, mark, newsOfMarkToDelete));
          }
        }
      }

      if (!group.isEmpty() && !monitor.isCanceled())
        fTasks.add(group);
    }

    /* Return if user cancelled the preview */
    if (monitor.isCanceled())
      return;

    /* 5.) Delete News with an age > X Days */
    if (fOps.deleteNewsByAge()) {
      CleanUpGroup group = new CleanUpGroup(NLS.bind(Messages.CleanUpModel_DELETE_BY_AGE, fOps.getMaxNewsAge()));

      ISearchField ageInDaysField = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsName);
      ISearchCondition ageCond = fFactory.createSearchCondition(ageInDaysField, SearchSpecifier.IS_GREATER_THAN, fOps.getMaxNewsAge());

      /* For each selected Bookmark */
      for (IBookMark mark : fBookmarks) {

        /* Return if user cancelled the preview */
        if (monitor.isCanceled())
          return;

        /* Ignore if Bookmark gets already deleted */
        if (bookmarksToDelete.contains(mark))
          continue;

        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(4);
        conditions.add(getLocationCondition(mark));
        conditions.add(ageCond);
        conditions.add(stateCondition);
        conditions.add(stickyCondition);
        if (fOps.keepLabeledNews())
          conditions.addAll(labelConditions);

        List<SearchHit<NewsReference>> results = filterInvalidResults(fModelSearch.searchNews(conditions, true), monitor);
        Set<NewsReference> newsOfMarkToDelete = new HashSet<NewsReference>();
        for (SearchHit<NewsReference> result : results)
          newsOfMarkToDelete.add(result.getResult());

        if (!newsOfMarkToDelete.isEmpty()) {
          Collection<NewsReference> existingNewsOfMarkToDelete = newsToDelete.get(mark);

          /* Return if user cancelled the preview */
          if (monitor.isCanceled())
            return;

          /* First time the Mark is treated */
          if (existingNewsOfMarkToDelete == null) {
            newsToDelete.put(mark, newsOfMarkToDelete);
            group.addTask(new NewsTask(group, mark, newsOfMarkToDelete));
          }

          /* Existing Mark */
          else {
            newsOfMarkToDelete.removeAll(existingNewsOfMarkToDelete);

            if (!newsOfMarkToDelete.isEmpty()) {
              existingNewsOfMarkToDelete.addAll(newsOfMarkToDelete);
              group.addTask(new NewsTask(group, mark, newsOfMarkToDelete));
            }
          }
        }
      }

      if (!group.isEmpty() && !monitor.isCanceled())
        fTasks.add(group);
    }

    /* Return if user cancelled the preview */
    if (monitor.isCanceled())
      return;

    /* 6.) Delete Read News */
    if (fOps.deleteReadNews()) {
      CleanUpGroup group = new CleanUpGroup(Messages.CleanUpModel_READ_NEWS);

      EnumSet<State> readState = EnumSet.of(INews.State.READ);
      ISearchCondition stateCond = fFactory.createSearchCondition(stateField, SearchSpecifier.IS, readState);

      /* For each selected Bookmark */
      for (IBookMark mark : fBookmarks) {

        /* Return if user cancelled the preview */
        if (monitor.isCanceled())
          return;

        /* Ignore if Bookmark gets already deleted */
        if (bookmarksToDelete.contains(mark))
          continue;

        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(3);
        conditions.add(getLocationCondition(mark));
        conditions.add(stateCond);
        conditions.add(stickyCondition);
        if (fOps.keepLabeledNews())
          conditions.addAll(labelConditions);

        List<SearchHit<NewsReference>> results = filterInvalidResults(fModelSearch.searchNews(conditions, true), monitor);
        Set<NewsReference> newsOfMarkToDelete = new HashSet<NewsReference>();
        for (SearchHit<NewsReference> result : results)
          newsOfMarkToDelete.add(result.getResult());

        if (!newsOfMarkToDelete.isEmpty() && !monitor.isCanceled()) {
          Collection<NewsReference> existingNewsOfMarkToDelete = newsToDelete.get(mark);

          /* First time the Mark is treated */
          if (existingNewsOfMarkToDelete == null) {
            newsToDelete.put(mark, newsOfMarkToDelete);
            group.addTask(new NewsTask(group, mark, newsOfMarkToDelete));
          }

          /* Existing Mark */
          else {
            newsOfMarkToDelete.removeAll(existingNewsOfMarkToDelete);

            if (!newsOfMarkToDelete.isEmpty()) {
              existingNewsOfMarkToDelete.addAll(newsOfMarkToDelete);
              group.addTask(new NewsTask(group, mark, newsOfMarkToDelete));
            }
          }
        }
      }

      if (!group.isEmpty() && !monitor.isCanceled())
        fTasks.add(group);
    }
  }

  private Set<String> loadGoogleReaderFeeds(IProgressMonitor monitor) {
    InputStream inS = null;
    boolean isCanceled = false;
    try {

      /* Obtain Google Credentials */
      ICredentials credentials = Owl.getConnectionService().getAuthCredentials(URI.create(SyncUtils.GOOGLE_LOGIN_URL), null);
      if (credentials == null)
        return null;

      /* Load Google Auth Token */
      String authToken = SyncUtils.getGoogleAuthToken(credentials.getUsername(), credentials.getPassword(), false, monitor);
      if (authToken == null)
        authToken = SyncUtils.getGoogleAuthToken(credentials.getUsername(), credentials.getPassword(), true, monitor);

      /* Return on Cancellation */
      if (monitor.isCanceled() || !StringUtils.isSet(authToken))
        return null;

      /* Import from Google */
      URI opmlImportUri = URI.create(SyncUtils.GOOGLE_READER_OPML_URI);
      IProtocolHandler handler = Owl.getConnectionService().getHandler(opmlImportUri);

      Map<Object, Object> properties = new HashMap<Object, Object>();
      Map<String, String> headers = new HashMap<String, String>();
      headers.put("Authorization", SyncUtils.getGoogleAuthorizationHeader(authToken)); //$NON-NLS-1$
      properties.put(IConnectionPropertyConstants.HEADERS, headers);

      inS = handler.openStream(opmlImportUri, monitor, properties);

      /* Return on Cancellation */
      if (monitor.isCanceled()) {
        isCanceled = true;
        return null;
      }

      /* Find Bookmarks */
      List<IEntity> types = Owl.getInterpreter().importFrom(inS);
      Set<IBookMark> bookmarks = new HashSet<IBookMark>();
      for (IEntity type : types) {
        if (type instanceof IBookMark)
          bookmarks.add((IBookMark) type);
        else if (type instanceof IFolder)
          CoreUtils.fillBookMarks(bookmarks, Collections.singleton((IFolder) type));
      }

      Set<String> feeds = new HashSet<String>();
      for (IBookMark bookmark : bookmarks) {
        feeds.add(bookmark.getFeedLinkReference().getLinkAsText());
      }

      feeds.add(URIUtils.toHTTP(SyncUtils.GOOGLE_READER_NOTES_FEED));
      feeds.add(URIUtils.toHTTP(SyncUtils.GOOGLE_READER_SHARED_ITEMS_FEED));
      feeds.add(URIUtils.toHTTP(SyncUtils.GOOGLE_READER_RECOMMENDED_ITEMS_FEED));

      return feeds;
    } catch (CoreException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    } finally {
      if (inS != null) {
        try {
          if ((isCanceled && inS instanceof IAbortable))
            ((IAbortable) inS).abort();
          else
            inS.close();
        } catch (IOException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
      }
    }

    return null;
  }

  /* Have to test if Entity really exists (bug 337) */
  private List<SearchHit<NewsReference>> filterInvalidResults(List<SearchHit<NewsReference>> results, IProgressMonitor monitor) {
    List<SearchHit<NewsReference>> validResults = new ArrayList<SearchHit<NewsReference>>(results.size());

    for (SearchHit<NewsReference> searchHit : results) {
      if (monitor.isCanceled())
        break;

      if (fNewsDao.exists(searchHit.getResult().getId()))
        validResults.add(searchHit);
      else
        CoreUtils.reportIndexIssue();
    }

    return validResults;
  }
}
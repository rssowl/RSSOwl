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

package org.rssowl.ui.internal.dialogs.cleanup.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.SearchHit;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.NewsDeleteTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DeleteNewsAbstractOperation implements ICleanUpOperation {

  private static boolean keepLabeledState;
  private static boolean keepUnreadState;

  public static void saveKeepNewsPreferences(IPreferenceScope preferences) {
    preferences.putBoolean(DefaultPreferences.CLEAN_UP_NEVER_DEL_LABELED_NEWS_STATE, keepLabeledState);
    preferences.putBoolean(DefaultPreferences.CLEAN_UP_NEVER_DEL_UNREAD_NEWS_STATE, keepUnreadState);
  }

  public static boolean keepLabeledNews() {
    return keepLabeledState;
  }

  public static void setKeepLabeledNews(boolean state) {
    keepLabeledState = state;
  }

  public static boolean keepUnreadNews() {
    return keepUnreadState;
  }

  public static void setKeepUnreadNews(boolean state) {
    keepUnreadState = state;
  }

  private final static List<DeleteNewsAbstractOperation> scheduledOps = new ArrayList<DeleteNewsAbstractOperation>();
  private final static Hashtable<DeleteNewsAbstractOperation, SummaryModelTaskGroup> opGroups = new Hashtable<DeleteNewsAbstractOperation, SummaryModelTaskGroup>();

  private final static Map<IBookMark, Set<NewsReference>> newsToDelete = new HashMap<IBookMark, Set<NewsReference>>();

  private static Set<NewsReference> getDeletedNewsForFeed(IBookMark bookmark) {
    return newsToDelete.get(bookmark);
  }

  private static void addDeletedNewsForFeed(IBookMark bookmark, Set<NewsReference> news) {
    if (newsToDelete.containsKey(bookmark)) {
      newsToDelete.get(bookmark).addAll(news);
    } else {
      newsToDelete.put(bookmark, news);
    }
  }

  public static void reset() {
    scheduledOps.clear();
    opGroups.clear();

    // reset keep news booleans to default ???
    keepUnreadState = true;
    keepLabeledState = true;

    // TODO move DeleteNewsTask statics here
    newsToDelete.clear();
  }

  private static SummaryModelTaskGroup getGroup(DeleteNewsAbstractOperation op) {
    if (opGroups.containsKey(op)) {
      return opGroups.get(op);
    }
    opGroups.put(op, op.createGroup());
    return opGroups.get(op);
  }

  protected final String fNewsName;
  protected final IModelFactory fFactory;
  protected final IModelSearch fModelSearch;
  protected final INewsDAO fNewsDao;

  //  protected abstract Collection<Pair<IBookMark, Set<NewsReference>>> filter(Collection<IBookMark> bookmarks, IProgressMonitor monitor);

  protected abstract Set<NewsReference> searchNews(IBookMark bookmark, IProgressMonitor monitor);

  protected abstract SummaryModelTaskGroup createGroup();

  protected DeleteNewsAbstractOperation() {
    fFactory = Owl.getModelFactory();
    fModelSearch = Owl.getPersistenceService().getModelSearch();
    fNewsName = INews.class.getName();
    fNewsDao = DynamicDAO.getDAO(INewsDAO.class);
  }

  /**
   * Schedule operations for later batch execution
   *
   * @param op
   */
  public static void schedule(DeleteNewsAbstractOperation op) {
    scheduledOps.add(op);
  }

  public static Collection<SummaryModelTaskGroup> process(Collection<IBookMark> bookmarks, IProgressMonitor monitor) {
    /* For each selected Bookmark */
    for (IBookMark bookmark : bookmarks) {

      /* Return if user cancelled the preview */
      if (monitor.isCanceled())
        return null;

      /* Ignore if Bookmark gets already deleted */
      if (DeleteFeedsAbstractOperation.isScheduledToDelete(bookmark))
        continue;

      // execute operation one by one
      for (DeleteNewsAbstractOperation op : scheduledOps) {

        // search for valid operation news
        Set<NewsReference> news = op.searchNews(bookmark, monitor);
        if (news == null || news.isEmpty())
          continue; // go to next operation

        /* Return if user cancelled the preview */
        if (monitor.isCanceled())
          continue;

        SummaryModelTaskGroup group = getGroup(op);

        // check if some news are to be deleted already
        Set<NewsReference> existingNews = getDeletedNewsForFeed(bookmark);

        /* First time the Mark is treated */
        if (existingNews == null) {
          addDeletedNewsForFeed(bookmark, news);
          group.addTask(new NewsDeleteTask(group, bookmark, news));
        }
        /* Existing Mark */
        else {
          news.removeAll(existingNews);
          if (!news.isEmpty()) {
            addDeletedNewsForFeed(bookmark, news);
            group.addTask(new NewsDeleteTask(group, bookmark, news));
          }
        }
      }
    }
    return opGroups.values();
  }

  protected List<SearchHit<NewsReference>> searchNews(IBookMark mark, IProgressMonitor monitor, List<ISearchCondition> additionalConditions) {
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();
    conditions.add(getLocationCondition(mark));
    if (additionalConditions != null && !additionalConditions.isEmpty())
      conditions.addAll(additionalConditions);
    conditions.add(getStickyCondition());
    conditions.add(getStateCondition());
    if (keepLabeledNews())
      conditions.addAll(getLabeledConditions());
    /* Check if result count exceeds limit */
    return filterInvalidResults(fModelSearch.searchNews(conditions, true), monitor);
  }

  protected ISearchCondition getLocationCondition(IBookMark mark) {
    Long[][] value = new Long[3][1];
    value[1][0] = mark.getId();
    ISearchField fLocationField = fFactory.createSearchField(INews.LOCATION, fNewsName);
    return fFactory.createSearchCondition(fLocationField, SearchSpecifier.IS, value);
  }

  /**
   * Reusable Sticky Condition
   *
   * @return condition to filter out sticky news
   */
  protected ISearchCondition getStickyCondition() {
    ISearchField stickyField = fFactory.createSearchField(INews.IS_FLAGGED, fNewsName);
    return fFactory.createSearchCondition(stickyField, SearchSpecifier.IS_NOT, true);
  }

  /**
   * Reusable State Condition
   *
   * @return condition to filter out unread news
   */
  protected ISearchCondition getStateCondition() {
    EnumSet<State> states = keepUnreadNews() ? EnumSet.of(INews.State.READ) : EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED, INews.State.READ);
    ISearchField stateField = fFactory.createSearchField(INews.STATE, fNewsName);
    return fFactory.createSearchCondition(stateField, SearchSpecifier.IS, states);
  }

  /**
   * Reusable Label Condition
   *
   * @return condition to filter out labeled news
   */
  protected List<ISearchCondition> getLabeledConditions() {
    Collection<ILabel> labels = DynamicDAO.getDAO(ILabelDAO.class).loadAll();
    ISearchField labelField = fFactory.createSearchField(INews.LABEL, fNewsName);
    List<ISearchCondition> labelConditions = new ArrayList<ISearchCondition>(labels.size());
    for (ILabel label : labels) {
      labelConditions.add(fFactory.createSearchCondition(labelField, SearchSpecifier.IS_NOT, label.getName()));
    }
    return labelConditions;
  }

  /**
   * Condition to search news with age more that given
   *
   * @param maxAge maximum age of news in days
   * @return condition to filter out old news
   */
  protected ISearchCondition getNewsAgeCondition(int maxAge) {
    ISearchField ageInDaysField = fFactory.createSearchField(INews.AGE_IN_DAYS, fNewsName);
    return fFactory.createSearchCondition(ageInDaysField, SearchSpecifier.IS_GREATER_THAN, maxAge);
  }

  /**
   * Return search condition for news with READ state
   *
   * @return condition to filter out read news
   */
  protected ISearchCondition getNewsStateIsReadCondition() {
    EnumSet<State> readState = EnumSet.of(INews.State.READ);
    ISearchField stateField = fFactory.createSearchField(INews.STATE, fNewsName);
    return fFactory.createSearchCondition(stateField, SearchSpecifier.IS, readState);
  }

  /* Have to test if Entity really exists (bug 337) */
  protected List<SearchHit<NewsReference>> filterInvalidResults(List<SearchHit<NewsReference>> results, IProgressMonitor monitor) {
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

  /**
   * getting actual news from search results
   *
   * @param results
   * @return Set of NewsReference to search results
   */
  protected Set<NewsReference> getSearchResults(List<SearchHit<NewsReference>> results) {
    Set<NewsReference> news = new HashSet<NewsReference>();
    for (SearchHit<NewsReference> sr : results)
      news.add(sr.getResult());
    return news;
  }
}

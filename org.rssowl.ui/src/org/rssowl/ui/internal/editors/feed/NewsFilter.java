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

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.SearchHit;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public class NewsFilter extends ViewerFilter {

  /** Possible Filter Values */
  public enum Type {

    /** Show all News */
    SHOW_ALL(Messages.NewsFilter_SHOW_ALL, Messages.NewsFilter_ALL_NEWS),

    /** Show New News */
    SHOW_NEW(Messages.NewsFilter_SHOW_NEW, Messages.NewsFilter_NEW_NEWS),

    /** Show Unread News */
    SHOW_UNREAD(Messages.NewsFilter_SHOW_UNREAD, Messages.NewsFilter_UNREAD_NEWS),

    /** Show Recent News */
    SHOW_RECENT(Messages.NewsFilter_SHOW_RECENT, Messages.NewsFilter_RECENT_NEWS),

    /** Show Sticky News */
    SHOW_STICKY(Messages.NewsFilter_SHOW_STICKY, Messages.NewsFilter_STICKY_NEWS),

    /** Show Recent News */
    SHOW_LAST_5_DAYS(Messages.NewsFilter_SHOW_LAST_DAYS, Messages.NewsFilter_LAST_DAYS),

    /** Show Labeled News */
    SHOW_LABELED(Messages.NewsFilter_SHOW_LABELED_NEWS, Messages.NewsFilter_LABELED_NEWS);

    String fName;
    String fDisplayName;

    Type(String actionName, String displayName) {
      fName = actionName;
      fDisplayName = displayName;
    }

    /**
     * Returns a human-readable Name of this enum-value.
     *
     * @return A human-readable Name of this enum-value.
     */
    public String getName() {
      return fName;
    }

    /**
     * @return the display name when this grouping is active.
     */
    public String getDisplayName() {
      return fDisplayName;
    }
  }

  /** Possible Search Targets */
  public enum SearchTarget {

    /** Search Headlines */
    HEADLINE(Messages.NewsFilter_HEADLINE),

    /** Search Entire News */
    ALL(Messages.NewsFilter_ENTIRE_NEWS),

    /** Search Author */
    AUTHOR(Messages.NewsFilter_AUTHOR),

    /** Search Category */
    CATEGORY(Messages.NewsFilter_CATEGORY),

    /** Search Source */
    SOURCE(Messages.NewsFilter_SOURCE),

    /** Search Attachments */
    ATTACHMENTS(Messages.NewsFilter_ATTACHMENTS),

    /** Search Labels */
    LABELS(Messages.NewsFilter_LABELS);

    String fName;

    SearchTarget(String name) {
      fName = name;
    }

    /**
     * Returns a human-readable Name of this enum-value.
     *
     * @return A human-readable Name of this enum-value.
     */
    public String getName() {
      return fName;
    }
  }

  /* Current Filter Value */
  private Type fType = Type.SHOW_ALL;

  /* Current Search Target */
  private SearchTarget fSearchTarget = SearchTarget.HEADLINE;

  /* Misc. */
  private INewsMark fNewsMark;
  private Set<Long> fCachedPatternMatchingNews;
  private IModelFactory fModelFactory = Owl.getModelFactory();
  private String fPatternString;

  /**
   * @param newsMark the {@link INewsMark} that is used as source for all news.
   */
  public void setNewsMark(INewsMark newsMark) {
    fNewsMark = newsMark;
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
   * java.lang.Object, java.lang.Object)
   */
  @Override
  public final boolean select(Viewer viewer, Object parentElement, Object element) {

    /* Filter not Active */
    if (fCachedPatternMatchingNews == null && fType == Type.SHOW_ALL)
      return true;

    return isElementVisible(viewer, element);
  }

  /**
   * Answers whether the given element in the given viewer matches the filter
   * pattern. This is a default implementation that will show a leaf element in
   * the tree based on whether the provided filter text matches the text of the
   * given element's text, or that of it's children (if the element has any).
   * Subclasses may override this method.
   *
   * @param viewer the tree viewer in which the element resides
   * @param element the element in the tree to check for a match
   * @return true if the element matches the filter pattern
   */
  boolean isElementVisible(Viewer viewer, Object element) {
    return isParentMatch(viewer, element) || isLeafMatch(viewer, element, false);
  }

  /**
   * Answers whether the given element is a valid selection in the filtered
   * tree. For example, if a tree has items that are categorized, the category
   * itself may not be a valid selection since it is used merely to organize the
   * elements.
   *
   * @param element
   * @return true if this element is eligible for automatic selection
   */
  boolean isElementSelectable(Object element) {
    return element != null;
  }

  /**
   * Check if the parent (category) is a match to the filter text. The default
   * behavior returns true if the element has at least one child element that is
   * a match with the filter text. Subclasses may override this method.
   *
   * @param viewer the viewer that contains the element
   * @param element the tree element to check
   * @return true if the given element has children that matches the filter text
   */
  private boolean isParentMatch(Viewer viewer, Object element) {
    if (viewer instanceof AbstractTreeViewer) {
      ITreeContentProvider provider = (ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider();
      Object[] children = provider.getChildren(element);

      if ((children != null) && (children.length > 0))
        return filter(viewer, element, children).length > 0;
    }

    return false;
  }

  /**
   * @param news the {@link INews} to check if matching the filter or not.
   * @param ignorePattern if <code>true</code> ignore the text search pattern
   * and <code>false</code> otherwise.
   * @return <code>true</code> if the given {@link INews} should be selected by
   * the filter and <code>false</code> otherwise.
   */
  boolean select(INews news, boolean ignorePattern) {
    return isLeafMatch(null, news, ignorePattern);
  }

  /**
   * @param news the identifier of the {@link INews} to check if matching the
   * text based filter or not.
   * @return <code>true</code> if the given {@link INews} should be selected by
   * the text based filter and <code>false</code> otherwise.
   */
  boolean isTextPatternMatch(Long newsId) {
    return fCachedPatternMatchingNews == null || fCachedPatternMatchingNews.contains(newsId);
  }

  /**
   * Check if the current (leaf) element is a match with the filter text. The
   * default behavior checks that the label of the element is a match.
   * Subclasses should override this method.
   *
   * @param viewer the viewer that contains the element
   * @param element the tree element to check
   * @param ignorePattern if <code>true</code> ignore the text search pattern
   * and <code>false</code> otherwise.
   * @return true if the given element's label matches the filter text
   */
  private boolean isLeafMatch(Viewer viewer, Object element, boolean ignorePattern) {

    /* Filter not Active */
    if ((ignorePattern || fCachedPatternMatchingNews == null) && fType == Type.SHOW_ALL)
      return true;

    /* Element is a News */
    if (element instanceof INews) {
      INews news = (INews) element;
      INews.State state = news.getState();
      boolean isMatch = false;

      switch (fType) {

      /* Show: All */
        case SHOW_ALL:
          isMatch = true;
          break;

        /* Show New News */
        case SHOW_NEW:
          isMatch = (state == INews.State.NEW);
          break;

        /* Show Unread News */
        case SHOW_UNREAD:
          isMatch = (state == INews.State.UNREAD || state == INews.State.NEW || state == INews.State.UPDATED);
          break;

        /* Show Sticky News */
        case SHOW_STICKY:
          isMatch = news.isFlagged();
          break;

        /* Show Labeled News */
        case SHOW_LABELED:
          isMatch = !news.getLabels().isEmpty();
          break;

        /* Show Recent News (max 48h old) */
        case SHOW_RECENT:
          Date date = DateUtils.getRecentDate(news);
          isMatch = (date.getTime() >= (DateUtils.getToday().getTimeInMillis() - DateUtils.DAY));
          break;

        /* Show Last 5 Days */
        case SHOW_LAST_5_DAYS:
          date = DateUtils.getRecentDate(news);
          isMatch = (date.getTime() >= (DateUtils.getToday().getTimeInMillis() - 5 * DateUtils.DAY));
          break;
      }

      /* Finally check the Pattern */
      if (isMatch && !ignorePattern && fCachedPatternMatchingNews != null)
        isMatch = isTextPatternMatch(news.getId());

      return isMatch;
    }

    return false;
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerFilter#isFilterProperty(java.lang.Object, java.lang.String)
   */
  @Override
  public boolean isFilterProperty(Object element, String property) {
    return false; // This is handled in needsRefresh() already
  }

  /**
   * Set the Type of this Filter. The Type is describing which elements are
   * filtered.
   *
   * @param type The Type of this Filter as described in the <code>Type</code>
   * enumeration.
   */
  public void setType(Type type) {
    if (fType != type)
      fType = type;
  }

  /**
   * Get the Type of this Filter. The Type is describing which elements are
   * filtered.
   *
   * @return Returns the Type of this Filter as described in the
   * <code>Type</code> enumeration.
   */
  Type getType() {
    return fType;
  }

  /**
   * Get the Target of the Search. The Target is describing which elements to
   * search when a Text-Search is performed.
   *
   * @return Returns the SearchTarget of the Search as described in the
   * <code>SearchTarget</code> enumeration.
   */
  SearchTarget getSearchTarget() {
    return fSearchTarget;
  }

  /**
   * @return Returns the current set pattern string or <code>null</code> if
   * none.
   */
  String getPatternString() {
    return fPatternString;
  }

  /**
   * Set the Target of the Search. The Target is describing which elements to
   * search when a Text-Search is performed.
   *
   * @param searchTarget The SearchTarget of the Search as described in the
   * <code>SearchTarget</code> enumeration.
   */
  public void setSearchTarget(SearchTarget searchTarget) {
    SearchTarget oldTarget = fSearchTarget;
    fSearchTarget = searchTarget;

    /* Cause re-search if required */
    if (oldTarget != fSearchTarget)
      setPattern(fPatternString);
  }

  /**
   * The pattern string for which this filter should select elements in the
   * viewer.
   *
   * @param patternString
   */
  public void setPattern(String patternString) {
    fPatternString = patternString;

    /* Pattern Reset */
    if (!StringUtils.isSet(patternString))
      fCachedPatternMatchingNews = null;

    /* Pattern Set */
    else {
      try {
        fCachedPatternMatchingNews = cacheMatchingNews(patternString.trim());
      }

      /* This happens expectedly if max-clauses count reaches a certain limit */
      catch (PersistenceException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }
  }

  private Set<Long> cacheMatchingNews(String pattern) {
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(2);
    ISearchCondition locationCondition = null;
    ISearchCondition textCondition = null;

    /* Explicitly return on empty String */
    if (!StringUtils.isSet(pattern))
      return Collections.emptySet();

    /* Convert to Wildcard Query */
    if (StringUtils.supportsTrailingWildcards(pattern))
      pattern = pattern + "*"; //$NON-NLS-1$

    /* Match on Location (not supported for search marks and folder news marks) */
    if (fNewsMark != null && !(fNewsMark instanceof ISearchMark) && !(fNewsMark instanceof FolderNewsMark)) {
      ISearchField field = fModelFactory.createSearchField(INews.LOCATION, INews.class.getName());
      locationCondition = fModelFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList((IFolderChild) fNewsMark)));
      conditions.add(locationCondition);
    }

    /* Match on Pattern */
    ISearchField field = null;
    SearchSpecifier specifier = SearchSpecifier.CONTAINS_ALL;
    switch (fSearchTarget) {
      case ALL:
        field = fModelFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
        break;

      case ATTACHMENTS:
        field = fModelFactory.createSearchField(INews.ATTACHMENTS_CONTENT, INews.class.getName());
        break;

      case AUTHOR:
        field = fModelFactory.createSearchField(INews.AUTHOR, INews.class.getName());
        break;

      case CATEGORY:
        field = fModelFactory.createSearchField(INews.CATEGORIES, INews.class.getName());
        specifier = SearchSpecifier.IS;
        break;

      case HEADLINE:
        field = fModelFactory.createSearchField(INews.TITLE, INews.class.getName());
        break;

      case SOURCE:
        field = fModelFactory.createSearchField(INews.SOURCE, INews.class.getName());
        specifier = SearchSpecifier.IS;
        break;

      case LABELS:
        field = fModelFactory.createSearchField(INews.LABEL, INews.class.getName());
        specifier = SearchSpecifier.IS;
        break;
    }

    textCondition = fModelFactory.createSearchCondition(field, specifier, pattern);
    conditions.add(textCondition);

    /* Perform Search */
    List<SearchHit<NewsReference>> result;
    if (fNewsMark != null && fNewsMark instanceof ISearchMark) { //Inject conditions into search conditions
      ISearchMark searchMark = (ISearchMark) fNewsMark;
      result = Owl.getPersistenceService().getModelSearch().searchNews(searchMark.getSearchConditions(), textCondition, searchMark.matchAllConditions());
    } else { //Use the provided conditions as is
      result = Owl.getPersistenceService().getModelSearch().searchNews(conditions, true);
    }

    Set<Long> resultSet = new HashSet<Long>(result.size());
    for (SearchHit<NewsReference> hit : result) {
      resultSet.add(hit.getResult().getId());
    }

    return resultSet;
  }

  /**
   * @return <code>TRUE</code> in case a Pattern is set and <code>FALSE</code>
   * otherwise.
   */
  boolean isPatternSet() {
    return fCachedPatternMatchingNews != null;
  }
}
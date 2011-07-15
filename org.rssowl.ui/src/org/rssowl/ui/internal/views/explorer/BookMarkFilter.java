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

package org.rssowl.ui.internal.views.explorer;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringMatcher;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Filter matching the leaf-nodes of a Tree-Structure. Parent Nodes are made
 * visible as soon as leaf-nodes match the filter. This causes the entire tree
 * structure to be realized.
 * <p>
 * Note: Inline StringMatcher from 3.2 final as it is internal API.
 * </p>
 *
 * @author bpasero
 */
public class BookMarkFilter extends ViewerFilter {

  /** Possible Filter Values */
  public enum Type {

    /** Show all Feeds */
    SHOW_ALL,

    /** Show Feeds with new News */
    SHOW_NEW,

    /** Show Feeds with unread News */
    SHOW_UNREAD,

    /** Show Feeds that had an error while loading */
    SHOW_ERRONEOUS,

    /** Show never visited Feeds */
    SHOW_NEVER_VISITED,

    /** Show sticky Feeds */
    SHOW_STICKY
  }

  /** Possible Search Targets */
  public enum SearchTarget {

    /** Search Name */
    NAME,

    /** Search Link */
    LINK,
  }

  /* The string pattern matcher used for this pattern filter */
  private StringMatcher fMatcher;

  /* Current Filter Value */
  private Type fType = Type.SHOW_ALL;

  /* Current Search Target */
  private SearchTarget fSearchTarget = SearchTarget.NAME;

  /*
   * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
   * java.lang.Object, java.lang.Object)
   */
  @Override
  public final boolean select(Viewer viewer, Object parentElement, Object element) {

    /* Filter not Active */
    if (fMatcher == null && fType == Type.SHOW_ALL)
      return true;

    return isElementVisible(viewer, element);
  }

  @Override
  public Object[] filter(Viewer viewer, Object parent, Object[] elements) {

    /* Filter not Active */
    if (fMatcher == null && fType == BookMarkFilter.Type.SHOW_ALL)
      return elements;

    return super.filter(viewer, parent, elements);
  }

  @Override
  public boolean isFilterProperty(Object element, String property) {
    return false; // This is handled in needsRefresh() already
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
   * Set the Target of the Search. The Target is describing which elements to
   * search when a Text-Search is performed.
   *
   * @param searchTarget The SearchTarget of the Search as described in the
   * <code>SearchTarget</code> enumeration.
   */
  public void setSearchTarget(SearchTarget searchTarget) {
    fSearchTarget = searchTarget;
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
   * The pattern string for which this filter should select elements in the
   * viewer.
   *
   * @param patternString
   */
  public void setPattern(String patternString) {
    if (patternString == null || patternString.equals("")) //$NON-NLS-1$
      fMatcher = null;
    else
      fMatcher = new StringMatcher(patternString + "*", true, false); //$NON-NLS-1$
  }

  /**
   * Answers whether the given String matches the pattern.
   *
   * @param string the String to test
   * @return whether the string matches the pattern
   */
  private boolean match(String string) {
    if (fMatcher == null)
      return true;

    return fMatcher.match(string);
  }
  boolean needsRefresh(Class<? extends IEntity> entityClass, Set<? extends ModelEvent> events) {
    return needsRefresh(entityClass, events, false);
  }

  boolean needsRefresh(Class<? extends IEntity> entityClass, Set<? extends ModelEvent> events, boolean searchResultsChanged) {

    /* In case the Filter is not active at all */
    if (fMatcher == null && fType == Type.SHOW_ALL)
      return false;

    /* News Event */
    if (entityClass.equals(INews.class)) {
      if (fType == Type.SHOW_NEW)
        return CoreUtils.isNewStateChange(events);
      else if (fType == Type.SHOW_UNREAD)
        return CoreUtils.isReadStateChange(events);
      else if (fType == Type.SHOW_STICKY)
        return CoreUtils.isStickyStateChange(events);
    }

    /* Bookmark Event */
    else if (IBookMark.class.isAssignableFrom(entityClass)) {
      if (fMatcher != null)
        return true;

      if (fType == Type.SHOW_NEVER_VISITED || fType == Type.SHOW_ERRONEOUS)
        return true;
    }

    /* Searchmark / News Bin Event */
    else if (ISearchMark.class.isAssignableFrom(entityClass) || INewsBin.class.isAssignableFrom(entityClass)) {
      if (fMatcher != null && !searchResultsChanged)
        return true;

      if (fType == Type.SHOW_NEW || fType == Type.SHOW_UNREAD)
        return true;
    }

    return false;
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
    return isParentMatch(viewer, element) || isLeafMatch(viewer, element);
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
    if (!(viewer instanceof AbstractTreeViewer))
      return false;

    Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider()).getChildren(element);

    if ((children != null) && (children.length > 0))
      return filter(viewer, element, children).length > 0;

    return false;
  }

  /**
   * Check if the current (leaf) element is a match with the filter text. The
   * default behavior checks that the label of the element is a match.
   * Subclasses should override this method.
   *
   * @param viewer the viewer that contains the element
   * @param element the tree element to check
   * @return true if the given element's label matches the filter text
   */
  protected boolean isLeafMatch(Viewer viewer, Object element) {

    /* Element is a News Mark */
    if (element instanceof INewsMark) {
      INewsMark newsmark = (INewsMark) element;
      boolean isMatch = false;

      /* Look at Type */
      switch (fType) {

        case SHOW_ALL:
          isMatch = true;
          break;

        /* Show: Feeds with New News */
        case SHOW_NEW:
          isMatch = newsmark.getNewsCount(EnumSet.of(INews.State.NEW)) > 0;
          break;

        /* Show: Unread Marks */
        case SHOW_UNREAD:
          isMatch = newsmark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)) > 0;
          break;

        /* Show: Sticky Marks */
        case SHOW_STICKY:
          if (newsmark instanceof IBookMark) {
            IBookMark bookmark = (IBookMark) newsmark;
            isMatch = bookmark.getStickyNewsCount() > 0;
          }
          break;

        /* Show: Feeds that had an Error while loading */
        case SHOW_ERRONEOUS:
          if (newsmark instanceof IBookMark)
            isMatch = ((IBookMark) newsmark).isErrorLoading();
          break;

        /* Show: Never visited Marks */
        case SHOW_NEVER_VISITED:
          isMatch = newsmark.getPopularity() <= 0;
          break;
      }

      /* Finally check the Pattern */
      if (isMatch && fMatcher != null) {
        if (!wordMatches(newsmark) && !wordMatches(newsmark.getParent()))
          return false;
      }

      return isMatch;
    }

    return false;
  }

  /**
   * Take the given filter text and break it down into words using a
   * BreakIterator.
   *
   * @param text
   * @return an array of words
   */
  private String[] getWords(String text) {
    List<String> words = new ArrayList<String>();

    /*
     * Break the text up into words, separating based on whitespace and common
     * punctuation. Previously used String.split(..., "\\W"), where "\W" is a
     * regular expression (see the Javadoc for class Pattern). Need to avoid
     * both String.split and regular expressions, in order to compile against
     * JCL Foundation (bug 80053). Also need to do this in an NL-sensitive way.
     * The use of BreakIterator was suggested in bug 90579.
     */
    BreakIterator iter = BreakIterator.getWordInstance();
    iter.setText(text);
    int i = iter.first();
    while (i != java.text.BreakIterator.DONE && i < text.length()) {
      int j = iter.following(i);
      if (j == java.text.BreakIterator.DONE)
        j = text.length();

      /* match the word */
      if (Character.isLetterOrDigit(text.charAt(i))) {
        String word = text.substring(i, j);
        words.add(word);
      }
      i = j;
    }
    return words.toArray(new String[words.size()]);
  }

  private boolean wordMatches(IFolderChild node) {

    /* Return early if node is a Bookmark-Set */
    if (node.getParent() == null)
      return false;

    /* Search Name */
    if (fSearchTarget == SearchTarget.NAME)
      return wordMatches(node.getName());

    /* Search Link */
    if (fSearchTarget == SearchTarget.LINK && node instanceof IBookMark)
      return wordMatches(((IBookMark) node).getFeedLinkReference().getLinkAsText());

    return false;
  }

  /**
   * Return whether or not if any of the words in text satisfy the match
   * critera.
   *
   * @param text the text to match
   * @return boolean <code>true</code> if one of the words in text satisifes the
   * match criteria.
   */
  protected boolean wordMatches(String text) {
    if (text == null)
      return false;

    /* If the whole text matches we are all set */
    if (match(text))
      return true;

    /* Otherwise check if any of the words of the text matches */
    String[] words = getWords(text);
    for (String word : words) {
      if (match(word))
        return true;
    }

    return false;
  }
}
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
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.reference.SearchMarkReference;
import org.rssowl.core.util.Pair;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link ISearchMark}.
 *
 * @author bpasero
 * @see ISearchMark
 */
public class SearchMark extends Mark implements ISearchMark {
  private List<ISearchCondition> fSearchConditions;
  private boolean fMatchAllConditions;
  private transient final NewsContainer fNewsContainer;

  /**
   * Creates a new Element of the type SearchMark. A SearchMark is only visually
   * represented in case it was added to a Folder. Make sure to add it to a
   * Folder using <code>Folder#addMark(Mark)</code>
   *
   * @param id The unique ID of this SearchMark.
   * @param folder The Folder this SearchMark belongs to.
   * @param name The Name of this SearchMark.
   */
  public SearchMark(Long id, IFolder folder, String name) {
    super(id, folder, name);
    fSearchConditions = new ArrayList<ISearchCondition>(5);
    fNewsContainer = createNewsContainer();
  }

  private NewsContainer createNewsContainer() {
    Map<INews.State, Boolean> statesToSortedMap = new EnumMap<INews.State, Boolean>(INews.State.class);
    statesToSortedMap.put(INews.State.NEW, Boolean.TRUE);
    return new NewsContainer(statesToSortedMap);
  }

  /**
   * Default constructor for deserialization
   */
  protected SearchMark() {
    fNewsContainer = createNewsContainer();
  }

  /**
   * @return the {@link NewsContainer} being used in this search.
   */
  public synchronized NewsContainer internalGetNewsContainer() {
    return fNewsContainer;
  }

  /*
   * @see org.rssowl.core.persist.ISearchMark#setResult(java.util.List)
   */
  public synchronized Pair<Boolean, Boolean> setNewsRefs(Map<INews.State, List<NewsReference>> results) {
    return fNewsContainer.setNews(results);
  }

  /*
   * @see org.rssowl.core.model.types.ISearchMark#addSearchCondition(org.rssowl.core.model.reference.SearchConditionReference)
   */
  public synchronized void addSearchCondition(ISearchCondition searchCondition) {
    Assert.isNotNull(searchCondition, "Exception adding NULL as Search Condition into SearchMark"); //$NON-NLS-1$
    fSearchConditions.add(searchCondition);
  }

  /*
   * @see org.rssowl.core.model.types.ISearchMark#removeSearchCondition(org.rssowl.core.model.search.ISearchCondition)
   */
  public synchronized boolean removeSearchCondition(ISearchCondition searchCondition) {
    return fSearchConditions.remove(searchCondition);
  }

  /*
   * @see org.rssowl.core.persist.ISearch#containsSearchCondition(org.rssowl.core.persist.ISearchCondition)
   */
  public synchronized boolean containsSearchCondition(ISearchCondition searchCondition) {
    return fSearchConditions.contains(searchCondition);
  }

  /*
   * @see org.rssowl.core.model.types.ISearchMark#getSearchConditions()
   */
  public synchronized List<ISearchCondition> getSearchConditions() {
    return new ArrayList<ISearchCondition>(fSearchConditions);
  }

  /*
   * @see org.rssowl.core.model.types.ISearchMark#requiresAllConditions()
   */
  public synchronized boolean matchAllConditions() {
    return fMatchAllConditions;
  }

  /*
   * @see org.rssowl.core.model.types.ISearchMark#setRequireAllConditions(boolean)
   */
  public synchronized void setMatchAllConditions(boolean requiresAllConditions) {
    fMatchAllConditions = requiresAllConditions;
  }

  /* getIdAsPrimitive is synchronized so this method doesn't need to be */
  public SearchMarkReference toReference() {
    return new SearchMarkReference(getIdAsPrimitive());
  }

  /* getNews(states) takes care of synchronization, so not done here */
  public List<INews> getNews() {
    return getNews(EnumSet.allOf(INews.State.class));
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNews(java.util.Set)
   */
  public List<INews> getNews(Set<State> states) {
    List<NewsReference> newsRefs;
    synchronized (this) {
      newsRefs = fNewsContainer.getNews(states);
    }
    return getNews(newsRefs);
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNewsCount(java.util.Set)
   */
  public synchronized int getNewsCount(Set<State> states) {
    Assert.isNotNull(states, "states"); //$NON-NLS-1$
    return fNewsContainer.getNewsCount(states);
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNewsRefs()
   */
  public synchronized List<NewsReference> getNewsRefs() {
    return fNewsContainer.getNews();
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNewsRefs(java.util.Set)
   */
  public synchronized List<NewsReference> getNewsRefs(Set<State> states) {
    return fNewsContainer.getNews(states);
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#isGetNewsRefsEfficient()
   */
  public boolean isGetNewsRefsEfficient() {
    return true;
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#containsNews(org.rssowl.core.persist.INews)
   */
  public boolean containsNews(INews news) {
    return fNewsContainer.containsNews(news);
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param searchMark to be compared.
   * @return whether this object and <code>searchMark</code> are identical. It
   * compares all the fields.
   */
  public synchronized boolean isIdentical(ISearchMark searchMark) {
    if (this == searchMark)
      return true;

    if (searchMark instanceof SearchMark == false)
      return false;

    SearchMark s = (SearchMark) searchMark;

    return (getId() == null ? s.getId() == null : getId().equals(s.getId())) &&
           (getParent() == null ? s.getParent() == null : getParent().equals(s.getParent())) &&
           (fSearchConditions == null ? s.fSearchConditions == null : fSearchConditions.equals(s.fSearchConditions)) &&
           (getLastVisitDate() == null ? s.getLastVisitDate() == null : getLastVisitDate().equals(s.getLastVisitDate())) &&
           getPopularity() == s.getPopularity() &&
           fMatchAllConditions == s.matchAllConditions() &&
           (getProperties() == null ? s.getProperties() == null : getProperties().equals(s.getProperties()));
  }

  /**
   * Returns a String describing the state of this Entity.
   *
   * @return A String describing the state of this Entity.
   */
  @Override
  public synchronized String toLongString() {
    return super.toString() + "Search Conditions = " + fSearchConditions.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }
}
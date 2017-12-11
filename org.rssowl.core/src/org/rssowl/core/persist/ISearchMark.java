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

import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.reference.SearchMarkReference;
import org.rssowl.core.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * A SearchMark acts like a virtual folder. The user defines some criteria, e.g.
 * "hardware" as part of the news title, and all News items that match this
 * criteria will be associated to this SearchMark.
 *
 * @author bpasero
 */
public interface ISearchMark extends INewsMark, ISearch {

  /** One of the fields in this type described as constant */
  public static final int MATCHING_NEWS = 4;

  /** One of the fields in this type described as constant */
  public static final int SEARCH_CONDITIONS = 5;

  /** One of the fields in this type described as constant */
  public static final int MATCH_ALL_CONDITIONS = 6;

  /**
   * Sets the result of this search mark. The results are represented by a
   * non-null Map (typically an EnumMap) of <code>INews.State</code> to a List
   * of <code>NewsReference</code>s that represent the news that match the
   * search.
   * <p>
   * Note: Any INews.State that is not included in the map will default to an
   * an empty List as the result value.
   * </p>
   *
   * @param results The results are represented by a non-null Map (typically an
   * EnumMap) of <code>INews.State</code> to a List of
   * <code>NewsReference</code>s that represent the news that match the
   * search.
   * @return Returns a {@link Pair} where the first {@link Boolean} indicates
   * whether the new result differs from the existing one and the second
   * {@link Boolean} indicates if there is any *new* news that where added with
   * the new result.
   */
  Pair<Boolean, Boolean> setNewsRefs(Map<INews.State, List<NewsReference>> results);

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  @Override
  SearchMarkReference toReference();
}
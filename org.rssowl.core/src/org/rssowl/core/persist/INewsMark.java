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

import java.util.List;
import java.util.Set;

/**
 * The abstract super-type of entities that provide news like book marks or
 * saved searches.
 *
 * @author bpasero
 */
public interface INewsMark extends IMark {

  /**
   * Returns a List of all news contained in this INewsMark. To reduce the
   * memory impact of this method, the news are returned as
   * <code>NewsReference</code>.
   *
   * @return Returns a List of all news contained in this INewsMark. To reduce
   * the memory impact of this method, the news are returned as
   * <code>NewsReference</code>.
   */
  List<NewsReference> getNewsRefs();

  /**
   * Returns a List of all news contained in this INewsMark. To reduce the
   * memory impact of this method, the news are returned as
   * <code>NewsReference</code>.
   *
   * @param states A Set (typically an EnumSet) of <code>INews.State</code> that
   * the resulting news must have.
   * @return Returns a List of all news contained in this INewsMark. To reduce
   * the memory impact of this method, the news are returned as
   * <code>NewsReference</code>.
   */
  List<NewsReference> getNewsRefs(Set<INews.State> states);

  /**
   * Returns the number of news that contained in this INewsMark in the provided
   * <code>INews.State</code>s.
   *
   * @param states A Set (typically an EnumSet) of <code>INews.State</code> of
   * the INews that should be included in the count.
   * @return the number of news that contained in this INewsMark in the provided
   * <code>INews.State</code>s.
   */
  int getNewsCount(Set<INews.State> states);

  /**
   * @param news the news to look for.
   * @return <code>true</code> if this entity contains the news and
   * <code>false</code> otherwise.
   */
  boolean containsNews(INews news);

  /**
   * @return all contained news of this entity.
   */
  List<INews> getNews();

  /**
   * @param states the states to look for.
   * @return all contained news of this entity with the given states.
   */
  List<INews> getNews(Set<INews.State> states);

  /**
   * @return <code>true</code> if asking for <code>NewsReference</code> is
   * efficient or <code>false</code> otherwise.
   */
  boolean isGetNewsRefsEfficient();
}
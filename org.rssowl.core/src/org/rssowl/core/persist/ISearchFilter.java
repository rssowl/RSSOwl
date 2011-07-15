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

import org.rssowl.core.INewsAction;

import java.util.List;

/**
 * Instances of of {@link ISearchFilter} perform a set of operations on entities
 * matching a certain search.
 *
 * @see INewsAction
 * @see IFilterAction
 * @author bpasero
 */
public interface ISearchFilter extends IEntity {

  /**
   * @return the human readable name of the filter.
   */
  String getName();

  /**
   * @param name the human readable name of the filter.
   */
  void setName(String name);

  /**
   * @return <code>true</code> if this filter is enabled and <code>false</code>
   * otherwise.
   */
  boolean isEnabled();

  /**
   * @param enabled <code>true</code> if this filter is enabled and
   * <code>false</code> otherwise.
   */
  void setEnabled(boolean enabled);

  /**
   * @return the order value in which the filter should be applied to matching
   * entities.
   */
  int getOrder();

  /**
   * @param order the order value in which the filter should be applied to
   * matching entities.
   */
  void setOrder(int order);

  /**
   * @return the {@link ISearch} containing the search conditions of the filter
   * or <code>null</code> if <code>matchAllNews()</code> returns <code>true</code>.
   */
  ISearch getSearch();

  /**
   * @param search the {@link ISearch} containing the search conditions of the
   * filter.
   */
  void setSearch(ISearch search);

  /**
   * @return the list of {@link IFilterAction} to perform on matching entities.
   */
  List<IFilterAction> getActions();

  /**
   * @param action an {@link IFilterAction} to perform on matching entities.
   */
  void addAction(IFilterAction action);

  /**
   * @param action an {@link IFilterAction} to remove from the list of actions.
   */
  void removeAction(IFilterAction action);

  /**
   * @param matchAllNews <code>true</code> if this filter should apply on all news and
   * <code>false</code> otherwise.
   */
  void setMatchAllNews(boolean matchAllNews);

  /**
   * @return <code>true</code> if this filter should apply on all news and
   * <code>false</code> otherwise.
   */
  boolean matchAllNews();
}
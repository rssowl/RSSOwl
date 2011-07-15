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

package org.rssowl.core.persist.dao;

import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;

import java.util.Set;

/**
 * A data-access-object for <code>ISearchMark</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface ISearchMarkDAO extends IEntityDAO<ISearchMark, SearchMarkListener, SearchMarkEvent> {

  /**
   * Notify <code>SearchMarkListener</code> that the news of a Set of
   * SearchMarks have changed. This can either be the number of news that have
   * changed, or the news state inside the results having changed.
   *
   * @param events The Set of SearchMarkEvents identifying the searchmarks of
   * this event.
   */
  void fireNewsChanged(Set<SearchMarkEvent> events);

  /**
   * Loads the {@code ISearchMark} that contains {@code searchCondition}.
   * and returns it. If {@code searchCondition} is not contained in any
   * ISearchMark, {@code null} is returned.
   *
   * @param searchCondition non-null ISearchCondition for which we want to find
   * the parent ISearchMark.
   * @return ISearchMark containing {@code searchCondition} or {@code null}.
   */
  ISearchMark load(ISearchCondition searchCondition);

  /**
   * Records a visit to the mark and saves it to the database. This method is
   * guaranteed not to change the search conditions contained in {@code mark}
   * and such no {@code SearchConditionEvent}s will be issued as a result.
   *
   * @param mark ISearchMark that has been visited.
   */
  void visited(ISearchMark mark);
}
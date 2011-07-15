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

import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.event.SearchFilterEvent;
import org.rssowl.core.persist.event.SearchFilterListener;

import java.util.Collection;

/**
 * A data-access-object for <code>ISearchFilter</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface ISearchFilterDAO extends IEntityDAO<ISearchFilter, SearchFilterListener, SearchFilterEvent> {

  /**
   * Notify <code>SearchFilterListener</code> that the provided search filter
   * was applied to the given list of news.
   *
   * @param filter the instance of {@link ISearchFilter} that was applied.
   * @param news the {@link Collection} of {@link INews} the
   * {@link ISearchFilter} was running on.
   */
  void fireFilterApplied(ISearchFilter filter, Collection<INews> news);
}
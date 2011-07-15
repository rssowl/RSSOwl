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

package org.rssowl.core.persist.event;

import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchFilter;

import java.util.Collection;

/**
 * A Listener being notified whenever the type <code>ISearchFilter</code> was
 * added, updated or deleted in the persistance layer.
 *
 * @author bpasero
 */
public interface SearchFilterListener extends EntityListener<SearchFilterEvent, ISearchFilter> {

  /**
   * Notifies that a {@link ISearchFilter} was applied on a {@link Collection}
   * of {@link INews}.
   *
   * @param filter the instance of {@link ISearchFilter} that was applied.
   * @param news the {@link Collection} of {@link INews} the
   * {@link ISearchFilter} was running on.
   */
  void filterApplied(ISearchFilter filter, Collection<INews> news);
}
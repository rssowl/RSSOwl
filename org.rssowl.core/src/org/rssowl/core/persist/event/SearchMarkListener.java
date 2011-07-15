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

import org.rssowl.core.persist.ISearchMark;

import java.util.Set;

/**
 * A Listener being notified whenever the type <code>ISearchMark</code> was
 * added, updated or deleted in the persistance layer. In addition, a method is
 * provided to notify about news inside the search being changed.
 *
 * @author bpasero
 */
public interface SearchMarkListener extends EntityListener<SearchMarkEvent, ISearchMark> {

  /**
   * Called when the news of a <code>ISearchMark</code> have changed. This can
   * either be the number of news that have changed or the state of any news has
   * changed.
   *
   * @param events A Set of SearchMarkEvents identifying the searchmarks that
   * provide new results.
   */
  void newsChanged(Set<SearchMarkEvent> events);
}
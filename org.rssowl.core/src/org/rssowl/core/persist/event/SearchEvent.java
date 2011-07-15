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

import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.event.runnable.SearchEventRunnable;

/**
 * <p>
 * An Event-Object being used to notify Listeners, whenever the type
 * <code>ISearch</code> was added, updated or deleted in the persistance layer.
 * </p>
 *
 * @author bpasero
 */
public final class SearchEvent extends ModelEvent {

  /**
   * Stores an instance of <code>ISearch</code> for the affected Type in this
   * Event.
   *
   * @param search An instance of <code>ISearch</code> for the affected Type.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   */
  public SearchEvent(ISearch search, boolean isRoot) {
    super(search, isRoot);
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#createEventRunnable()
   */
  @Override
  public SearchEventRunnable createEventRunnable() {
    return new SearchEventRunnable();
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#getEntity()
   */
  @Override
  public ISearch getEntity() {
    return (ISearch) super.getEntity();
  }
}
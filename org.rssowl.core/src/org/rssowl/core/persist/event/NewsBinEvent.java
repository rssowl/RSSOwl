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

import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.event.runnable.NewsBinEventRunnable;

/**
 * <p>
 * An Event-Object being used to notify Listeners, whenever the type
 * <code>INewsBin</code> was added, updated or deleted in the persistance layer.
 * </p>
 *
 * @author bpasero
 */
public final class NewsBinEvent extends MarkEvent {

  /**
   * Stores an instance of <code>INewsBin</code> for the affected Type in this
   * Event.
   *
   * @param newsBin An instance of <code>INewsBin</code> for the affected Type.
   * @param oldParent The previous saved version of the affected type or
   * <code>null</code> if not known.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   */
  public NewsBinEvent(INewsBin newsBin, IFolder oldParent, boolean isRoot) {
    super(newsBin, oldParent, isRoot);
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#createEventRunnable()
   */
  @Override
  public NewsBinEventRunnable createEventRunnable() {
    return new NewsBinEventRunnable();
  }

  /*
   * @see org.rssowl.core.model.events.ModelEvent#getReference()
   */
  @Override
  public INewsBin getEntity() {
    return (INewsBin) super.getEntity();
  }
}
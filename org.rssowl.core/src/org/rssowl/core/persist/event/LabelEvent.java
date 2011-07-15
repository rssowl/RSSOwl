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

import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.event.runnable.LabelEventRunnable;

/**
 * <p>
 * An Event-Object being used to notify Listeners, whenever the type
 * <code>ILabel</code> was added, updated or deleted in the persistance layer.
 * </p>
 *
 * @author bpasero
 */
public final class LabelEvent extends ModelEvent {
  private final ILabel fOldLabel;

  /**
   * Stores an instance of <code>ILabel</code> for the affected Type in this
   * Event.
   *
   * @param oldLabel The previous saved version of the affected type or {@code
   * null} if not known.
   * @param currentLabel An instance of <code>ILabel</code> for the affected
   * Type.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   */
  public LabelEvent(ILabel oldLabel, ILabel currentLabel, boolean isRoot) {
    super(currentLabel, isRoot);
    fOldLabel = oldLabel;
  }

  /**
   * @return The previous saved version of the affected type.
   */
  public ILabel getOldLabel() {
    return fOldLabel;
  }

  /*
   * @see org.rssowl.core.model.events.ModelEvent#getReference()
   */
  @Override
  public ILabel getEntity() {
    return (ILabel) super.getEntity();
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#createEventRunnable()
   */
  @Override
  public LabelEventRunnable createEventRunnable() {
    return new LabelEventRunnable();
  }
}
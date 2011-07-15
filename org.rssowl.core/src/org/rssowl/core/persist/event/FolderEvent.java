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
import org.rssowl.core.persist.event.runnable.FolderEventRunnable;

/**
 * <p>
 * An Event-Object being used to notify Listeners, whenever the type
 * <code>IFolder</code> was added, updated or deleted in the persistance layer.
 * </p>
 *
 * @author bpasero
 */
public final class FolderEvent extends ModelEvent {

  /* In case of Reparenting, remember the old parent */
  private final IFolder fOldParent;

  /**
   * Stores an instance of <code>IFolder</code> and the Parent Reference for the
   * affected Type in this Event.
   *
   * @param folder An instance of <code>IFolder</code> for the affected Type.
   * @param oldParent If this Event informs about a Reparenting the old parent
   * is used to do updates in the UI, <code>NULL</code> otherwise.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   */
  public FolderEvent(IFolder folder, IFolder oldParent, boolean isRoot) {
    super(folder, isRoot);
    fOldParent = oldParent;
  }

  /*
   * @see org.rssowl.core.model.events.ModelEvent#getReference()
   */
  @Override
  public IFolder getEntity() {
    return (IFolder) super.getEntity();
  }

  /**
   * Get the previous Parent of this Type in case this Event informs about a
   * Reparenting.
   * <p>
   * Note that this Method <em>will</em> return <code>NULL</code> in any case
   * where the Event is not informing about reparenting!
   * </p>
   *
   * @return Returns the previous Parent of this Type in case this Event informs
   * about Reparenting. Otherwise this Method will return <code>NULL</code>.
   */
  public IFolder getOldParent() {
    return fOldParent;
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#createEventRunnable()
   */
  @Override
  public FolderEventRunnable createEventRunnable() {
    return new FolderEventRunnable();
  }
}
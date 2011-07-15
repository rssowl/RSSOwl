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
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.event.runnable.SearchMarkEventRunnable;

/**
 * <p>
 * An Event-Object being used to notify Listeners, whenever the type
 * <code>ISearchMark</code> was added, updated or deleted in the persistance
 * layer.
 * </p>
 *
 * @author bpasero
 */
public final class SearchMarkEvent extends MarkEvent {

  /* Only for newsChanged(): TRUE if *new* news where added */
  private final boolean fAddedNewNews;

  /**
   * Stores an instance of <code>ISearchMark</code> and the Parent Reference for
   * the affected Type in this Event.
   *
   * @param mark An instance of <code>ISearchMark</code> for the affected Type.
   * @param oldParent If this Event informs about a Reparenting the old parent
   * is used to do updates in the UI, <code>NULL</code> otherwise.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   */
  public SearchMarkEvent(ISearchMark mark, IFolder oldParent, boolean isRoot) {
    this(mark, oldParent, isRoot, false);
  }

  /**
   * Stores an instance of <code>ISearchMark</code> and the Parent Reference for
   * the affected Type in this Event.
   *
   * @param mark An instance of <code>ISearchMark</code> for the affected Type.
   * @param oldParent If this Event informs about a Reparenting the old parent
   * is used to do updates in the UI, <code>NULL</code> otherwise.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   * @param addedNewNews TRUE if *new* news where added.
   */
  public SearchMarkEvent(ISearchMark mark, IFolder oldParent, boolean isRoot, boolean addedNewNews) {
    super(mark, oldParent, isRoot);
    fAddedNewNews = addedNewNews;
  }

  /*
   * @see org.rssowl.core.model.events.ModelEvent#getReference()
   */
  @Override
  public ISearchMark getEntity() {
    return (ISearchMark) super.getEntity();
  }

  /**
   * @return TRUE if *new* news where added from a newsChanged() event.
   */
  public boolean isAddedNewNews() {
    return fAddedNewNews;
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#createEventRunnable()
   */
  @Override
  public SearchMarkEventRunnable createEventRunnable() {
    return new SearchMarkEventRunnable();
  }
}
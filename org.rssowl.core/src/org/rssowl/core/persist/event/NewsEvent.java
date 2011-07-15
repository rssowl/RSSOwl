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
import org.rssowl.core.persist.event.runnable.NewsEventRunnable;

/**
 * <p>
 * An Event-Object being used to notify Listeners, whenever the type
 * <code>INews</code> was added, updated or deleted in the persistance layer.
 * </p>
 *
 * @author bpasero
 */
public final class NewsEvent extends ModelEvent {
  private final INews fOldNews;
  private final boolean fIsMerged;

  /**
   * Stores an instance of <code>INews</code> for the affected Type in this
   * Event.
   *
   * @param news An instance of <code>INews</code> for the affected Type.
   */
  public NewsEvent(INews news) {
    super(news);
    fOldNews = null;
    fIsMerged = false;
  }

  /**
   * Creates an instance of this event type.
   *
   * @param oldNews The previous saved version of the affected type or
   * <code>null</code> if not known.
   * @param currentNews The affected type.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   */
  public NewsEvent(INews oldNews, INews currentNews, boolean isRoot) {
    super(currentNews, isRoot);
    fOldNews = oldNews;
    fIsMerged = false;
  }

  /**
   * Creates an instance of this event type.
   *
   * @param oldNews The previous saved version of the affected type or
   * <code>null</code> if not known.
   * @param currentNews The affected type.
   * @param isRoot <code>TRUE</code> if this Event is a Root-Event,
   * <code>FALSE</code> otherwise.
   * @param isMerged if <code>true</code>, indicates that this event is
   * triggered from a news that got merged with another one (e.g. on feed
   * reload) and <code>false</code> otherwise.
   */
  public NewsEvent(INews oldNews, INews currentNews, boolean isRoot, boolean isMerged) {
    super(currentNews, isRoot);
    fOldNews = oldNews;
    fIsMerged = isMerged;
  }

  /*
   * @see org.rssowl.core.model.events.ModelEvent#getReference()
   */
  @Override
  public final INews getEntity() {
    return (INews) super.getEntity();
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#createEventRunnable()
   */
  @Override
  public NewsEventRunnable createEventRunnable() {
    return new NewsEventRunnable();
  }

  /**
   * @return The previous saved version of the affected type.
   */
  public final INews getOldNews() {
    return fOldNews;
  }

  /**
   * @return <code>true</code> indicates that this event is triggered from a
   * news that got merged with another one (e.g. on feed reload) and
   * <code>false</code> otherwise.
   */
  public boolean isMerged() {
    return fIsMerged;
  }
}
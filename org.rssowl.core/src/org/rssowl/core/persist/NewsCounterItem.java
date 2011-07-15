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

package org.rssowl.core.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.internal.persist.Persistable;

/**
 * Instances of <code>NewsCounterItem</code> store number values and are kept by
 * a <code>NewsCounter</code>. They provide leightweight access to certain
 * information of a <code>IFeed</code> entity.
 *
 * @see NewsCounter
 */
public final class NewsCounterItem extends Persistable {
  private int fNewCounter;
  private int fUnreadCounter;
  private int fStickyCounter;

  /** Default constructor for reflection */
  public NewsCounterItem() {}

  /**
   * @param newCounter number of new news
   * @param unreadCounter number of unread news
   * @param stickyCounter number of sticky news
   */
  public NewsCounterItem(int newCounter, int unreadCounter, int stickyCounter) {
    Assert.isLegal(newCounter >= 0, "newCounter should be >= 0"); //$NON-NLS-1$
    Assert.isLegal(unreadCounter >= 0, "unreadCounter should be >= 0"); //$NON-NLS-1$
    Assert.isLegal(stickyCounter >= 0, "stickyCounter should be >= 0"); //$NON-NLS-1$
    fNewCounter = newCounter;
    fUnreadCounter = unreadCounter;
    fStickyCounter = stickyCounter;
  }

  /**
   * @return Returns the value of *new* News contained in the feed.
   */
  public final int getNewCounter() {
    return fNewCounter;
  }

  /**
   * Increment the value of *new* News contained in the feed.
   */
  public final void incrementNewCounter() {
    ++fNewCounter;
  }

  /**
   * Decrement the value of *new* News contained in the feed.
   */
  public final void decrementNewCounter() {
    Assert.isTrue(fNewCounter > 0, "newCounter must not be negative"); //$NON-NLS-1$
    --fNewCounter;
  }

  /**
   * @return Returns the value of *unread* News contained in the feed.
   */
  public final int getUnreadCounter() {
    return fUnreadCounter;
  }

  /**
   * Increment the value of *unread* News contained in the feed.
   */
  public final void incrementUnreadCounter() {
    ++fUnreadCounter;
  }

  /**
   * Decrement the value of *unread* News contained in the feed.
   */
  public final void decrementUnreadCounter() {
    Assert.isTrue(fUnreadCounter > 0, "unreadCounter must not be negative"); //$NON-NLS-1$
    --fUnreadCounter;
  }

  /**
   * @return Returns the value of *sticky* News contained in the feed.
   */
  public final int getStickyCounter() {
    return fStickyCounter;
  }

  /**
   * Increment the value of *sticky* News contained in the feed.
   */
  public final void incrementStickyCounter() {
    ++fStickyCounter;
  }

  /**
   * Decrement the value of *sticky* News contained in the feed.
   */
  public final void decrementStickyCounter() {
    Assert.isTrue(fStickyCounter > 0, "stickyCounter must not be negative"); //$NON-NLS-1$
    --fStickyCounter;
  }
}
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

import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.reference.FeedLinkReference;

import java.util.Collection;

/**
 * A data-access-object for <code>IBookMark</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface IBookMarkDAO extends IEntityDAO<IBookMark, BookMarkListener, BookMarkEvent> {

  /**
   * Loads all <code>IBookMark</code>s from the database that reference the
   * given feed.
   *
   * @param feedRef A reference to the feed of interest.
   * @return Returns a Collection of all <code>IBookMark</code>s that reference
   * the given feed.
   */
  Collection<IBookMark> loadAll(FeedLinkReference feedRef);

  /**
   * Checks whether a {@link IBookMark} with the given {@link FeedLinkReference}
   * exists.
   *
   * @param feedRef A reference to the feed of interest.
   * @return <code>true</code> if a {@link IBookMark} with the given
   * {@link FeedLinkReference} exists and <code>false</code> otherwise.
   */
  boolean exists(FeedLinkReference feedRef);

  /**
   * Records a visit to the mark and saves it to the database.
   *
   * @param mark IBookMark that has been visited.
   */
  void visited(IBookMark mark);
}
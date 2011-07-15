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

import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FeedLinkReference;

import java.util.Date;

/**
 * A usual bookmark as seen in Firefox or other Browsers. The Bookmark is used
 * to define a position for a <code>Feed</code> inside the hierarchy of
 * Folders. The user may define some properties, e.g. how often to reload the
 * related Feed.
 *
 * @author bpasero
 */
public interface IBookMark extends INewsMark {

  /** One of the fields in this type described as constant */
  public static final int IS_ERROR_LOADING = 4;

  /**
   * @return TRUE in case the last time this BookMark's Feed was reloading
   * returned an Error, FALSE otherwise.
   */
  boolean isErrorLoading();

  /**
   * @param isErrorLoading TRUE in case the last time this BookMark's Feed was
   * reloading returned an Error, FALSE otherwise.
   */
  void setErrorLoading(boolean isErrorLoading);

  /**
   * @return a reference to the link of the feed that this mark is related to.
   */
  FeedLinkReference getFeedLinkReference();

  /**
   * Sets the reference to the link of the feed that this mark is related to.
   *
   * @param feedLinkRef
   */
  void setFeedLinkReference(FeedLinkReference feedLinkRef);

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  BookMarkReference toReference();

  /**
   * @return the number of news in this IBookMark that are sticky.
   */
  int getStickyNewsCount();

  /**
   * @return the most recent Date when new INews where added to this IBookMark
   * or {@code null} if not yet set.
   */
  Date getMostRecentNewsDate();

  /**
   * Set the most recent Date when new INews where added to this IBookMark.
   *
   * @param date Non-null Date object.
   */
  void setMostRecentNewsDate(Date date);
}
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

import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.FeedListener;

import java.net.URI;

/**
 * A data-access-object for <code>IFeed</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface IFeedDAO extends IEntityDAO<IFeed, FeedListener, FeedEvent> {

  /**
   * Loads the <code>IFeed</code> that points to the given <code>URI</code>.
   *
   * @param link The Link to load the <code>IFeed</code> for.
   * @return Returns the <code>IFeed</code> that points to the given
   * <code>URI</code>.
   */
  IFeed load(URI link);

  /**
   * @param link the link of the feed to check if it exists.
   * @return <code>true</code> if the database has a {@link IFeed} stored with
   * the given link and <code>false</code> otherwise.
   */
  boolean exists(URI link);
}
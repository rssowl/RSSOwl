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

import java.util.Set;

/**
 * Provides an empty implementation of <code>FeedListener</code>. Useful if
 * the client only needs to implement a subset of the interface.
 *
 * @author bpasero
 */
public class FeedAdapter implements FeedListener {

  /*
   * @see org.rssowl.core.model.events.FeedListener#entitiesAdded(java.util.List)
   */
  @Override
  public void entitiesAdded(Set<FeedEvent> events) { }

  /*
   * @see org.rssowl.core.model.events.FeedListener#entitiesDeleted(java.util.List)
   */
  @Override
  public void entitiesDeleted(Set<FeedEvent> events) { }

  /*
   * @see org.rssowl.core.model.events.FeedListener#entitiesUpdated(java.util.List)
   */
  @Override
  public void entitiesUpdated(Set<FeedEvent> events) { }
}
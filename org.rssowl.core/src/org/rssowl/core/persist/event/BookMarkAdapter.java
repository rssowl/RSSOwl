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
 * Provides an empty implementation of <code>SearchMarkListener</code>.
 * Useful if the client only needs to implement a subset of the interface.
 *
 * @author bpasero
 */
public class BookMarkAdapter implements BookMarkListener {

  /*
   * @see org.rssowl.core.model.events.BookMarkListener#bookMarkAdded(java.util.List)
   */
  @Override
  public void entitiesAdded(Set<BookMarkEvent> events) { }

  /*
   * @see org.rssowl.core.model.events.BookMarkListener#bookMarkDeleted(java.util.List)
   */
  @Override
  public void entitiesDeleted(Set<BookMarkEvent> events) { }

  /*
   * @see org.rssowl.core.model.events.BookMarkListener#bookMarkUpdated(java.util.List)
   */
  @Override
  public void entitiesUpdated(Set<BookMarkEvent> events) { }
}
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
public class SearchConditionAdapter implements SearchConditionListener {

  /*
   * @see org.rssowl.core.model.events.SearchConditionListener#searchConditionAdded(java.util.List)
   */
  public void entitiesAdded(Set<SearchConditionEvent> events) { }

  /*
   * @see org.rssowl.core.model.events.SearchConditionListener#searchConditionDeleted(java.util.List)
   */
  public void entitiesDeleted(Set<SearchConditionEvent> events) { }

  /*
   * @see org.rssowl.core.model.events.SearchConditionListener#searchConditionUpdated(java.util.List)
   */
  public void entitiesUpdated(Set<SearchConditionEvent> events) { }
}
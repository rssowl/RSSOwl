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

import org.rssowl.core.INewsAction;

/**
 * Instances of {@link IFilterAction} are stored in a {@link ISearchFilter} and
 * provide the ID and arbitrary data of an operation being performed for
 * entities matching the conditions as specified in the {@link ISearchFilter}.
 *
 * @see INewsAction
 * @author bpasero
 */
public interface IFilterAction extends IPersistable {

  /**
   * @return the ID of an action to be performed.
   */
  String getActionId();

  /**
   * @param data arbitrary data for the action being performed.
   */
  void setData(Object data);

  /**
   * @return the arbitrary data for the action being performed.
   */
  Object getData();
}
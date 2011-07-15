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

import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.event.NewsBinEvent;
import org.rssowl.core.persist.event.NewsBinListener;

/**
 * A data-access-object for <code>INewsBin</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface INewsBinDAO extends IEntityDAO<INewsBin, NewsBinListener, NewsBinEvent> {

  /**
   * Records a visit to the mark and saves it to the database. This method is
   * fast because it avoids to save all the news references inside the bin.
   *
   * @param mark INewsBin that has been visited.
   */
  void visited(INewsBin mark);
}
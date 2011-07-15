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

package org.rssowl.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;

/**
 * The {@link IApplicationService} interface is providing methods that access
 * the Persistance Layer as required by the Application. It is very important
 * that implementors optimize the performance of all these methods.
 * <p>
 * Contributed via <code>org.rssowl.core.ApplicationService</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 */
public interface IApplicationService {

  /**
   * Handles all the persistence-related operations for a feed that has been
   * provided by the interpreter. This includes: <li>Merging the contents of the
   * interpreted feed with the currently saved one.</li> <li>Running the
   * retention policy.</li> <li>Updating the ConditionalGet object associated
   * with the feed.</li>
   *
   * @param bookMark The BookMark that contains the feed that has been reloaded.
   * @param interpretedFeed The IFeed object that has been supplied by the
   * interpreter based on its online contents.
   * @param conditionalGet IConditionalObject associated with the IFeed or
   * <code>null</code> if there isn't one.
   * @param deleteConditionalGet if <code>true</code> an existing
   * IConditionalGet object associated with the IFeed will be deleted as part of
   * this operation.
   * @param monitor a monitor to show progress and react on cancellation.
   */
  void handleFeedReload(IBookMark bookMark, IFeed interpretedFeed, IConditionalGet conditionalGet, boolean deleteConditionalGet, IProgressMonitor monitor);
}
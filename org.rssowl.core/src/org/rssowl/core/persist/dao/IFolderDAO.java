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

import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.ReparentInfo;

import java.util.Collection;
import java.util.List;

/**
 * A data-access-object for <code>IFolder</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface IFolderDAO extends IEntityDAO<IFolder, FolderListener, FolderEvent> {

  /**
   * Loads all Folders from the persistance layer that do not have any parent
   * Folder (in other words root folders).
   *
   * @return A Collection containing all Folders that do not have any parent
   * Folder.
   * @throws PersistenceException In case of an error while accessing the
   * persistance layer implementation.
   */
  Collection<IFolder> loadRoots() throws PersistenceException;

  /**
   * <p>
   * Performs the reparenting of the folder children as described by
   * <code>reparentInfos</code>. Depending on the type of children,
   * FolderEvents, BookMarkEvents or SearchMarkEvents will be issed. They will
   * contain a non-null <code>oldParent</code> property to indicate that
   * reparenting took place.
   * </p>
   *
   * @param reparentInfos A non-null list of ReparentInfo objects
   * describing the reparenting details for a list of IFolderChild objects.
   * @throws PersistenceException In case of an error while loading the Types.
   */
  void reparent(List<ReparentInfo<IFolderChild, IFolder>> reparentInfos) throws PersistenceException;
}
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

import org.rssowl.core.persist.dao.IFolderDAO;

/**
 * The base interface for IFolder children. It's basically a named IEntity.
 *
 * @see IFolder#getChildren()
 * @see IFolder
 * @see IEntity
 */
public interface IFolderChild extends IEntity, Reparentable<IFolder> {

  /**
   * Returns the name of this child.
   *
   * @return the name of this child.
   */
  String getName();
  
  /**
   * Returns the parent folder of this child or <code>null</code> if the child
   * has no parent. Some implementations of this interface may be stricter and
   * never return <code>null</code>. If that's the case, they should specify
   * that in their documentation.
   * 
   * @return the parent of this child or <code>null</code>.
   */
  IFolder getParent();
  
  /**
   * Sets the parent folder to <code>folder</code>. Some implementations of
   * this interface may be stricter and not accept <code>null</code>. If
   * that's the case, they should specify that in their documentation.
   * <p>
   * Note that this method should not be used under normal circumstances.
   * Instead call {@link IFolderDAO#reparent(java.util.List)} to ensure that the
   * event is correctly populated with the old parent.
   * </p>
   * 
   * @param folder new folder parent.
   * @see IFolderDAO#reparent(java.util.List)
   */
  void setParent(IFolder folder);
}
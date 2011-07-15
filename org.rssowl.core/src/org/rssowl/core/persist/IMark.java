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

import java.util.Date;

/**
 * The abstract super-type of <code>BookMark</code> and
 * <code>SearchMark</code>. Used to associate Bookmarks and Searchmarks with
 * a Folder. These Elements are considered to be leaves of the Tree.
 *
 * @author bpasero
 */
public interface IMark extends IFolderChild {

  /** One of the fields in this type described as constant */
  public static final int NAME = 0;

  /** One of the fields in this type described as constant */
  public static final int CREATION_DATE = 1;

  /** One of the fields in this type described as constant */
  public static final int LAST_VISIT_DATE = 2;

  /** One of the fields in this type described as constant */
  public static final int POPULARITY = 3;

  /**
   * @return How often this Feed has been visited by the User.
   */
  int getPopularity();

  /**
   * @param popularity How often this Feed has been visited by the User.
   */
  void setPopularity(int popularity);

  /**
   * Get the Date this Mark was last displayed to the User.
   *
   * @return the Date this Mark was last displayed to the User.
   */
  Date getLastVisitDate();

  /**
   * Set the Date this Mark was last displayed to the User.
   *
   * @param lastVisitDate The Date this Mark was last displayed to the User.
   */
  void setLastVisitDate(Date lastVisitDate);

  /**
   * Get the Date this Mark was created.
   *
   * @return the creation date of this mark.
   */
  Date getCreationDate();

  /**
   * Set the Date this Mark was created.
   *
   * @param creationDate The creation date of this mark.
   */
  void setCreationDate(Date creationDate);

  /**
   * Get the Name for this Mark.
   *
   * @return the name of the mark.
   */
  String getName();

  /**
   * Set the Name of this Mark.
   *
   * @param name The Name of this Mark.
   */
  void setName(String name);

  /**
   * Returns the parent of this IMark. This method should never return
   * <code>null</code>.
   *
   * @return the parent of this child.
   */
  IFolder getParent();

  /**
   * Sets the parent folder to <code>folder</code>. <code>folder</code> should
   * not be <code>null</code>.
   *
   * <p>
   * Note that this method should not be used under normal circumstances.
   * Instead call {@link IFolderDAO#reparent(java.util.List)} to ensure that the
   * event is correctly populated with the old parent.
   * </p>
   *
   * @param folder new folder parent. This should never be null.
   * @see IFolderDAO#reparent(java.util.List)
   */
  void setParent(IFolder folder);
}
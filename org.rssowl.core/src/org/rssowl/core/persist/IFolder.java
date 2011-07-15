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

import org.rssowl.core.persist.reference.FolderReference;

import java.net.URI;
import java.util.List;

/**
 * Folders store a number of Marks in an hierachical order. The hierachical
 * order is achieved by allowing to store Folders inside Folders.
 * <p>
 * In case a Blogroll URL is set for the Folder, it is to be interpreted as
 * root-folder of a "Synchronized Blogroll". This special kind of Folder allows
 * to synchronize its contents from a remote OPML file that contains a number of
 * Feeds.
 * </p>
 *
 * @author bpasero
 */
public interface IFolder extends IFolderChild {

  /** One of the fields in this type described as constant */
  public static final int NAME = 0;

  /** One of the fields in this type described as constant */
  public static final int BLOGROLL_LINK = 1;

  /** One of the fields in this type described as constant */
  public static final int MARKS = 2;

  /** One of the fields in this type described as constant */
  public static final int FOLDERS = 3;

  /**
   * @return {@code true} if the folder has no children. Otherwise, it returns
   * {@code false}.
   */
  public boolean isEmpty();

  /**
   * Returns {@code true} if this IFolder contains {@code child} and
   * {@false} otherwise.
   *
   * @param child element whose presence should be tested.
   *
   * @return {@code true} if this IFolder contains {@code child} and
   * {@false} otherwise.
   */
  public boolean containsChild(IFolderChild child);

  /**
   * Get a list of the children contained in this folder. Typically, these
   * children will be of type IMark or IFolder.
   *
   * @return a list of children contained in this folder.
   * <p>
   * Note: The returned List should not be modified. The default implementation
   * returns an unmodifiable List using
   * <code>Collections.unmodifiableList()</code>. Trying to modify the List
   * will result in <code>UnsupportedOperationException</code>.
   * </p>
   * @see #getMarks()
   * @see #getFolders()
   */
  List<IFolderChild> getChildren();

  /**
   * Of there is an instance of <code>IFolderChild</code> that is equal to
   * <code>child</code> in the list of children, removes it and returns
   * <code>true</code>. Otherwise, returns <code>false</code>.
   *
   * @param child An instance of <code>IFolderChild</code> to be removed.
   * @return <code>true</code> if a child is removed from children,
   * <code>false</code> otherwise.
   */
  boolean removeChild(IFolderChild child);

  /**
   * Adds an instance of <code>IMark</code> as Child to this Folder.
   *
   * @param mark An instance of <code>IMark</code> to be added as Child to
   * this Folder.
   * @param position The new Position identified by a <code>IFolderChild</code>
   * contained in this folder or <code>NULL</code> to add the mark as last
   * element.
   * @param after If <code>true</code>, move the folders to a one index after
   * the given position. May be <code>NULL</code> if the position is not
   * provided.
   */
  void addMark(IMark mark, IFolderChild position, Boolean after);

  /**
   * Moves a List of <code>IFolderChild</code> contained in this Folder to a
   * new position.
   *
   * @param children The List of <code>IFolderChild</code> being moved to a
   * new position.
   * @param position The new Position identified by a <code>IFolderChild</code>
   * contained in this folder.
   * @param after If <code>true</code>, move the folders to a one index after
   * the given position. May be <code>NULL</code> if the position is not
   * provided.
   */
  void reorderChildren(List<? extends IFolderChild> children, IFolderChild position, Boolean after);

  /**
   * Sorts all {@link IFolderChild} contained in this folder by their names.
   * Different kinds of {@link IFolderChild} get grouped together and not mixed.
   */
  void sort();

  /**
   * Get a list of marks contained in this folder. Typically, these marks may be
   * of type ISearchMark and/or IBookMark.
   *
   * @return a list of marks contained in this folder. Typically, these marks
   * may be of type ISearchMark and/or IBookMark.
   */
  List<IMark> getMarks();

  /**
   * Adds an instance of <code>IFolder</code> as Child to this Folder.
   *
   * @param folder An instance of <code>IFolder</code> to be added to this
   * Folder.
   * @param position The new Position identified by a <code>IFolderChild</code>
   * contained in this folder or <code>NULL</code> to add the mark as last
   * element.
   * @param after If <code>true</code>, move the folders to a one index after
   * the given position. May be <code>NULL</code> if the position is not
   * provided.
   */
  void addFolder(IFolder folder, IFolderChild position, Boolean after);

  /**
   * Get a list of the sub-folders contained in this folder.
   *
   * @return a list of sub-folders of this folder.
   */
  List<IFolder> getFolders();

  /**
   * Get the Name of this Folder.
   *
   * @return the name of the folder.
   */
  String getName();

  /**
   * Set the Name of this Folder.
   *
   * @param name the name of the folder to set.
   */
  void setName(String name);

  /**
   * Get the Link to the Blogroll this Folder is pointing to.
   *
   * @return Returns the Link to the Blogroll this Folder is pointing to.
   */
  URI getBlogrollLink();

  /**
   * Set the Link to the Blogroll this Folder is pointing to.
   *
   * @param blogrollLink the Link to the Blogroll this Folder is pointing to.
   */
  void setBlogrollLink(URI blogrollLink);

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  FolderReference toReference();
}
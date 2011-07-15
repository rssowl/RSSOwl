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

import java.net.URI;

/**
 * The super-type of all Source Elements in Feeds.
 * 
 * @author bpasero
 */
public interface ISource extends IPersistable, MergeCapable<ISource> {

  /** One of the fields in this type described as constant */
  public static final int NAME = 0;

  /** One of the fields in this type described as constant */
  public static final int LINK = 1;

  /**
   * The Name of the Feed that this News came from.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   * 
   * @param name The Name of the Feed that this News came from to set.
   */
  void setName(String name);

  /**
   * The Link to the Homepage that this News came from.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   * 
   * @param link The Link to the Homepage that this News came from to set.
   */
  void setLink(URI link);

  /**
   * The Name of the Feed that this News came from.
   * 
   * @return Returns the Name of the Feed that this News came from.
   */
  String getName();

  /**
   * Returns the Link to the Homepage that this News came from.
   * 
   * @return Returns the Link to the Homepage that this News came from.
   */
  URI getLink();
}
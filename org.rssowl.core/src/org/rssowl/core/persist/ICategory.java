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

import org.rssowl.core.persist.reference.CategoryReference;

/**
 * The super-type of all Category Elements in Feeds.
 *
 * @author bpasero
 */
public interface ICategory extends IEntity, MergeCapable<ICategory> {

  /** One of the fields in this type described as constant */
  public static final int NAME = 0;

  /** One of the fields in this type described as constant */
  public static final int DOMAIN = 1;

  /**
   * The Name of the Category.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param name The Name of the Category to set.
   */
  void setName(String name);

  /**
   * A String that identifies a categorization taxonomy. The value of the
   * element is a forward-slash-separated string that identifies a hierarchic
   * location in the indicated taxonomy.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param domain The categorization taxonomy to set.
   */
  void setDomain(String domain);

  /**
   * A String that identifies a categorization taxonomy. The value of the
   * element is a forward-slash-separated string that identifies a hierarchic
   * location in the indicated taxonomy.
   *
   * @return Returns the domain of this category.
   */
  String getDomain();

  /**
   * The Name of this Category.
   *
   * @return Returns the name of this category.
   */
  String getName();

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  CategoryReference toReference();
}
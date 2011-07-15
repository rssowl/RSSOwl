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

/**
 * The super-type of all GUID Elements in Feeds.
 *
 * @author bpasero
 */
public interface IGuid extends IPersistable {

  /** One of the fields in this type described as constant */
  public static final int VALUE = 0;

  /** One of the fields in this type described as constant */
  public static final int PERMALINK = 1;

  /**
   * If the guid element has an attribute named "isPermaLink" with a value of
   * true, the reader may assume that it is a permalink to the item, that is, a
   * url that can be opened in a Web browser, that points to the full item
   * described by the Item Element.
   *
   * @return Returns wether this GUID is a permalink.
   */
  boolean isPermaLink();

  /**
   * GUID stands for globally unique identifier. It's a string that uniquely
   * identifies the item. When present, an aggregator may choose to use this
   * string to determine if an item is new.
   *
   * @return Returns the value of this GUID.
   */
  String getValue();
}
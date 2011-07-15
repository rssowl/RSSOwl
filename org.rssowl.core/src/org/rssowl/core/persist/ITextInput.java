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

import org.rssowl.core.persist.reference.FeedReference;

import java.net.URI;

/**
 * The super-type of all TextInput Elements in Feeds.
 * 
 * @author bpasero
 */
public interface ITextInput extends IPersistable {

  /** One of the fields in this type described as constant */
  public static final int TITLE = 0;

  /** One of the fields in this type described as constant */
  public static final int DESCRIPTION = 1;

  /** One of the fields in this type described as constant */
  public static final int NAME = 2;

  /** One of the fields in this type described as constant */
  public static final int LINK = 3;

  /**
   * The label of the Submit button in the text input area.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   * 
   * @param title The label of the Submit button in the text input area to set.
   */
  void setTitle(String title);

  /**
   * Explains the text input area.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   * 
   * @param description Explains the text input area.
   */
  void setDescription(String description);

  /**
   * The name of the text object in the text input area.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   * 
   * @param name The name of the text object in the text input area to set.
   */
  void setName(String name);

  /**
   * The URL of the CGI script that processes text input requests.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   * 
   * @param link The URL of the CGI script that processes text input requests to
   * set.
   */
  void setLink(URI link);

  /**
   * Explains the text input area.
   * 
   * @return Returns an explanation of the text input area.
   */
  String getDescription();

  /**
   * The URL of the CGI script that processes text input requests.
   * 
   * @return Returns the URL of the CGI script that processes text input
   * requests.
   */
  URI getLink();

  /**
   * The name of the text object in the text input area.
   * 
   * @return Returns the name of the text object in the text input area.
   */
  String getName();

  /**
   * The label of the Submit button in the text input area.
   * 
   * @return Returns the label of the Submit button in the text input area.
   */
  String getTitle();

  /**
   * The Feed that this TextInput belongs to.
   * 
   * @return the feed that this textinput belongs to.
   */
  FeedReference getFeed();
}
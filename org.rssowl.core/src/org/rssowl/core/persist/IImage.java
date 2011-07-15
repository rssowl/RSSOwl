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
 * The super-type of all Image Elements in Feeds.
 *
 * @author bpasero
 */
public interface IImage extends IPersistable, MergeCapable<IImage> {

  /** One of the fields in this type described as constant */
  public static final int LINK = 0;

  /** One of the fields in this type described as constant */
  public static final int TITLE = 1;

  /** One of the fields in this type described as constant */
  public static final int HOMEPAGE = 2;

  /** One of the fields in this type described as constant */
  public static final int WIDTH = 3;

  /** One of the fields in this type described as constant */
  public static final int HEIGHT = 4;

  /** One of the fields in this type described as constant */
  public static final int DESCRIPTION = 5;

  /**
   * The Link of this Image that represents the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param link The Link of this Image that represents the Feed to set.
   */
  void setLink(URI link);

  /**
   * The Title of the Image.
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
   * @param title The Title of the Image to set.
   */
  void setTitle(String title);

  /**
   * The Link to the Homepage of the Feed this Image is coming from.
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
   * @param homepage The Link to the Homepage of the Feed this Image is coming
   * from to set.
   */
  void setHomepage(URI homepage);

  /**
   * The Width of the Image in pixels.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param width The Width of the Image in pixels to set.
   */
  void setWidth(int width);

  /**
   * The Height of the Image in pixels.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param height The Height of the Image in pixels to set.
   */
  void setHeight(int height);

  /**
   * Description of the Image.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param description The Description of the Image to set.
   */
  void setDescription(String description);

  /**
   * Description of the Image.
   *
   * @return Returns the description of the Image.
   */
  String getDescription();

  /**
   * The Height of the Image in pixels.
   *
   * @return Returns the height of the Image in pixels.
   */
  int getHeight();

  /**
   * The Link to the Homepage of the Feed this Image is coming from.
   *
   * @return Returns the Link to the Homepage of the Feed this Image is coming
   * from.
   */
  URI getHomepage();

  /**
   * The Title of the Image.
   *
   * @return Returns the Title of the Image.
   */
  String getTitle();

  /**
   * The Link of this Image that represents the Feed.
   *
   * @return Returns the Link of this Image that represents the Feed.
   */
  URI getLink();

  /**
   * The Width of the Image in pixels.
   *
   * @return Returns the width of the Image in pixels.
   */
  int getWidth();
}
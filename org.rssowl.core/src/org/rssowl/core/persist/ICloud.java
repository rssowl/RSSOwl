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

/**
 * The super-type of all Cloud Elements in Feeds.
 * 
 * @author bpasero
 */
public interface ICloud extends IPersistable {

  /** One of the fields in this type described as constant */
  public static final int DOMAIN = 0;

  /** One of the fields in this type described as constant */
  public static final int PORT = 1;

  /** One of the fields in this type described as constant */
  public static final int PATH = 2;

  /** One of the fields in this type described as constant */
  public static final int PROCEDURE = 3;

  /** One of the fields in this type described as constant */
  public static final int PROTOCOL = 4;

  /**
   * The Domain of the WebService.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   * 
   * @param domain The Domain of the WebService to set.
   */
  void setDomain(String domain);

  /**
   * The Port of the WebService.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   * 
   * @param port The Port of the WebService to set.
   */
  void setPort(int port);

  /**
   * The Path of the WebService.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   * 
   * @param path The Path of the WebService to set.
   */
  void setPath(String path);

  /**
   * The Procdeure Call.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   * 
   * @param registerProcedure The Procdeure Call to set.
   */
  void setRegisterProcedure(String registerProcedure);

  /**
   * The Protocol of the WebService.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   * 
   * @param protocol The Protocol of the WebService to set.
   */
  void setProtocol(String protocol);

  /**
   * The Domain of the WebService.
   * 
   * @return Returns the Domain of the WebService.
   */
  String getDomain();

  /**
   * The Path of the WebService.
   * 
   * @return Returns the Path of the WebService.
   */
  String getPath();

  /**
   * The Port of the WebService.
   * 
   * @return Returns the Port of the WebService.
   */
  int getPort();

  /**
   * The Protocol of the WebService.
   * 
   * @return Returns the Protocol of the WebService.
   */
  String getProtocol();

  /**
   * The Procdeure Call.
   * 
   * @return Returns the Procdeure Call.
   */
  String getRegisterProcedure();

  /**
   * The Feed this Cloud belongs to.
   * 
   * @return The Feed this Cloud belongs to.
   */
  FeedReference getFeed();
}
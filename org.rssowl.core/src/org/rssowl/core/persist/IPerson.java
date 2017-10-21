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

import org.rssowl.core.persist.reference.PersonReference;

import java.net.URI;

/**
 * The super-type of all Person Elements in Feeds.
 *
 * @author bpasero
 */
public interface IPerson extends IEntity, MergeCapable<IPerson> {

  /** One of the fields in this type described as constant */
  public static final int NAME = 0;

  /** One of the fields in this type described as constant */
  public static final int URI = 1;

  /** One of the fields in this type described as constant */
  public static final int EMAIL = 2;

  /**
   * Human-readable name for the person
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>OPML 1.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param name The Human-readable name for the person to set.
   */
  void setName(String name);

  /**
   * An Internationalized Resource Identifier associated with the person.
   * <p>
   * Used by:
   * <ul>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param uri The Internationalized Resource Identifier associated with the
   * person to set.
   */
  void setUri(URI uri);

  /**
   * An e-mail address associated with the person.
   * <p>
   * Used by:
   * <ul>
   * <li>Atom</li>
   * <li>OPML 1.0</li>
   * </ul>
   * </p>
   *
   * @param email an e-mail address associated with the person to set.
   */
  void setEmail(URI email);

  /**
   * Get the Human-readable name for the person
   *
   * @return The Human-readable name for the person
   */
  String getName();

  /**
   * Get the Internationalized Resource Identifier associated with the person
   *
   * @return The Internationalized Resource Identifier associated with the
   * person
   */
  URI getUri();

  /**
   * Get the e-mail address associated with the person
   *
   * @return an e-mail address associated with the person
   */
  URI getEmail();

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  @Override
  PersonReference toReference();
}
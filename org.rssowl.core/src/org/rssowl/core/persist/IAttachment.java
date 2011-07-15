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

import org.rssowl.core.persist.reference.AttachmentReference;

import java.net.URI;

/**
 * The super-type of all Attachment Elements in Feeds.
 *
 * @author bpasero
 */
public interface IAttachment extends IEntity, MergeCapable<IAttachment>, Reparentable<INews> {

  /** One of the fields in this type described as constant */
  public static final int LINK = 0;

  /** One of the fields in this type described as constant */
  public static final int TYPE = 1;

  /** One of the fields in this type described as constant */
  public static final int LENGTH = 2;

  /**
   * A URI pointing to where the Attachment is located.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param link The URI pointing to where the enclosure is located to set.
   */
  void setLink(URI link);

  /**
   * The standard MIME Type of the Attachment.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param type The standard MIME Type of the Attachment to set.
   */
  void setType(String type);

  /**
   * The size of the Attachment in Bytes.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param length The size of the Attachment in Bytes to set.
   */
  void setLength(int length);

  /**
   * The size of the Attachment in Bytes.
   *
   * @return Returns te size of the Attachment in Bytes.
   */
  int getLength();

  /**
   * The standard MIME Type of the Attachment.
   *
   * @return Returns the standard MIME Type of the Attachment.
   */
  String getType();

  /**
   * A URI pointing to where the Attachment is located.
   *
   * @return Returns a URI pointing to where the Attachment is located.
   */
  URI getLink();

  /**
   * The News this Attachment belongs to.
   *
   * @return Returns the News this Attachment belongs to.
   */
  INews getNews();

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  AttachmentReference toReference();
}
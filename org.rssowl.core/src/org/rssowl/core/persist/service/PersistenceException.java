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

package org.rssowl.core.persist.service;

/**
 * Runtime Exception thrown from Data Access Objects operations.
 * 
 * @author bpasero
 */
public class PersistenceException extends RuntimeException {

  /**
   * Constructs a <code>PersistenceException</code> with no detail message.
   */
  public PersistenceException() {
    super();
  }

  /**
   * Constructs a <code>PersistenceException</code> with the specified detail
   * message.
   * 
   * @param detailMessage the detail message.
   */
  public PersistenceException(String detailMessage) {
    super(detailMessage);
  }

  /**
   * Constructs a <code>PersistenceException</code> with the specified detail
   * message and cause.
   * 
   * @param detailMessage the detail message.
   * @param cause the cause.
   */
  public PersistenceException(String detailMessage, Throwable cause) {
    super(detailMessage, cause);
  }

  /**
   * Constructs a <code>PersistenceException</code> with the specified cause.
   * 
   * @param cause the cause.
   */
  public PersistenceException(Throwable cause) {
    super(cause);
  }
}
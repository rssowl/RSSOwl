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
 * Exception thrown if the system is unable to write to the disk due to lack of
 * space. Note that heuristics may be used to infer this so it may be inaccurate
 * at times. It always indicates a writing error, however.
 */
public class DiskFullException extends PersistenceException {

  /**
   * Creates an instance of this exception with the provided message and cause.
   *
   * @param message The message of the exception.
   * @param cause The exception thrown by the actual persistence layer or
   * {@code null} if there is no such message.
   */
  public DiskFullException(String message, Throwable cause) {
    super(message, cause);
  }
}
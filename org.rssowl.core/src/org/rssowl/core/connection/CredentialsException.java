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

package org.rssowl.core.connection;

import org.eclipse.core.runtime.IStatus;

/**
 * Checked Exception thrown in case of an error while retrieving Credentials for
 * Authentication or Proxy.
 * 
 * @author bpasero
 */
public class CredentialsException extends ConnectionException {

  /**
   * Creates a new exception with the given status object. The message of the
   * given status is used as the exception message.
   * 
   * @param status the status object to be associated with this exception
   */
  public CredentialsException(IStatus status) {
    super(status);
  }
}
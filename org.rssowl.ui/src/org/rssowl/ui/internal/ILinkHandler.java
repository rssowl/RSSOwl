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

package org.rssowl.ui.internal;

import java.net.URI;

/**
 * Instances of <code>ILinkHandler</code> register on a <code>CBrowser</code>
 * to handle Links that use the Handler-Protocol.
 *
 * @author bpasero
 */
public interface ILinkHandler {

  /** The Application Protocol for use with ILinkHandler */
  public static final String HANDLER_PROTOCOL = "rssowl://"; //$NON-NLS-1$

  /**
   * A callback from the <code>CBrowser</code> that knows about this handler.
   * Is called whenever a link was selected, that makes use of the Handler
   * protocol and matched this handler's ID.
   *
   * @param id The ID that was used to register this handler in the instance of
   * <code>CBrowser</code>.
   * @param link The Link that was selected in the <code>CBrowser</code>.
   */
  void handle(String id, URI link);
}
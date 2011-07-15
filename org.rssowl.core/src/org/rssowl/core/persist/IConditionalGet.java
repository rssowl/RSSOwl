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
 * This Interface provides the API to implement the Conditional GET mechanism as
 * used by HTTP.
 * 
 * @author bpasero
 */
public interface IConditionalGet extends IPersistable {

  /**
   * @return the link that this object refers to.
   */
  URI getLink();

  /**
   * Get the If-Modified Header to be sent as If-Modified-Since Request Header.
   * 
   * @return the If-Modified Header to be sent as If-Modified-Since Request
   * Header.
   */
  String getIfModifiedSince();

  /**
   * Get the ETag Header to be sent as If-None-Match Request Header.
   * 
   * @return the ETag Header to be sent as If-None-Match Request Header.
   */
  String getIfNoneMatch();

  /**
   * Sets the If-Modified Response Header and the ETag Response Header. Note
   * that at least one of the parameters must be non-null.
   * 
   * @param ifModifiedSince The If-Modified Response Header to set.
   * @param ifNoneMatch The ETag Response Header to set.
   * @throws IllegalArgumentException if both parameters are <code>null</code>.
   */
  void setHeaders(String ifModifiedSince, String ifNoneMatch);
}
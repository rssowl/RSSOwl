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

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IConditionalGet;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Implementation of IConditionalGet.
 */
public class ConditionalGet extends Persistable implements IConditionalGet {
  private String fLink;
  private String fIfModifiedSince;
  private String fIfNoneMatch;

  /**
   * Creates an instance of this object.
   *
   * @param ifModifiedSince the If-Modified Header to be sent as
   * If-Modified-Since Request Header.
   * @param link the link that this object refers to.
   * @param ifNoneMatch the ETag Header to be sent as If-None-Match Request
   * Header.
   * @throws IllegalArgumentException if <code>ifModifiedSince</code> and
   * <code>ifNoneMatch</code> are both null, or if <code>link</code> is
   * null.
   */
  public ConditionalGet(String ifModifiedSince, URI link, String ifNoneMatch) {
    Assert.isNotNull(link, "feedLink cannot be null"); //$NON-NLS-1$
    internalSetHeaders(ifModifiedSince, ifNoneMatch);
    fLink = link.toString();
  }

  private void internalSetHeaders(String ifModifiedSince, String ifNoneMatch) {
    Assert.isLegal(ifModifiedSince != null || ifNoneMatch != null, "ifModifiedSince and ifNoneMatch are null. Either of them has to be non-null"); //$NON-NLS-1$
    fIfModifiedSince = ifModifiedSince;
    fIfNoneMatch = ifNoneMatch;
  }

  /**
   * Provided for deserialization.
   */
  protected ConditionalGet() {
    super();
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.IConditionalGet#getFeedLink()
   */
  @Override
  public synchronized URI getLink() {
    try {
      return new URI(fLink);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Malformed URI was stored: " + fLink); //$NON-NLS-1$
    }
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.IConditionalGet#getIfModifiedSince()
   */
  @Override
  public synchronized String getIfModifiedSince() {
    return fIfModifiedSince;
  }

  /*
   * @see org.rssowl.core.model.internal.db4o.IConditionalGet#getIfNoneMatch()
   */
  @Override
  public synchronized String getIfNoneMatch() {
    return fIfNoneMatch;
  }

  /*
   * @see org.rssowl.core.persist.IConditionalGet#setHeaders(java.lang.String, java.lang.String)
   */
  @Override
  public synchronized void setHeaders(String ifModifiedSince, String ifNoneMatch) {
    internalSetHeaders(ifModifiedSince, ifNoneMatch);
  }
}
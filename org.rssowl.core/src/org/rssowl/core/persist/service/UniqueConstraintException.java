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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IPersistable;

/**
 * This exception should be thrown in situations where saving a IPersistable
 * would break a unique field invariant.
 */
public final class UniqueConstraintException extends PersistenceException {
  private final String fPropertyName;
  private final IPersistable fPersistable;

  /**
   * Creates an instance of this class.
   *
   * @param propertyName The name of the property whose invariant was broken.
   * @param persistable The persistable whose invariant was broken.
   * @see #getPropertyName()
   * @see #getPersistable()
   */
  public UniqueConstraintException(String propertyName, IPersistable persistable) {
    Assert.isNotNull(persistable, "persistable"); //$NON-NLS-1$
    Assert.isNotNull(propertyName, "propertyName"); //$NON-NLS-1$
    fPersistable = persistable;
    fPropertyName = propertyName;
  }

  /**
   * Returns the name of the property whose invariant was broken. For example,
   * in the case of {@link IFeed#getLink()}, the property name would be "link".
   *
   * @return The name of the property whose invariant was broken.
   */
  public final String getPropertyName() {
    return fPropertyName;
  }

  /**
   * Returns the persistable whose invariant has been broken. For example, in
   * the case of {@link IFeed#getLink()}, it would be an instance of IFeed.
   *
   * @return The persistable whose invariant has been broken.
   */
  public final IPersistable getPersistable() {
    return fPersistable;
  }
}
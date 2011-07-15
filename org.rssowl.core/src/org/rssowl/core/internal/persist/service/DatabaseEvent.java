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

package org.rssowl.core.internal.persist.service;

import com.db4o.ObjectContainer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Event object used in many listeners to provide access to the DB and global
 * Lock.
 */
public final class DatabaseEvent {
  private final ObjectContainer fObjectContainer;
  private final ReadWriteLock fLock;

  public DatabaseEvent(ObjectContainer objectContainer, ReadWriteLock lock) {
    fObjectContainer = objectContainer;
    fLock = lock;
  }

  /**
   * @return the {@link ObjectContainer} that provides access to the underlying
   * DB.
   */
  public final ObjectContainer getObjectContainer() {
    return fObjectContainer;
  }

  /**
   * @return the global {@link Lock} that is used by the underlying DB.
   */
  public final ReadWriteLock getLock() {
    return fLock;
  }
}
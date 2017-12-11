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

import org.rssowl.core.persist.service.IDGenerator;

import com.db4o.ObjectContainer;

import java.util.List;

/**
 * db4o implementation of IDGenerator.
 */
public class DB4OIDGenerator implements IDGenerator, DatabaseListener {
  private final static int BATCH_SIZE = 100;

  private long fCurrent = -1;
  private long fMax;
  private ObjectContainer fDb;
  private Counter fCounter;

  /**
   * Creates an instance of this class.
   */
  public DB4OIDGenerator() {
    DBManager.getDefault().addEntityStoreListener(this);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.service.DatabaseListener#databaseClosed
   * (org.rssowl.core.internal.persist.service.DatabaseEvent)
   */
  @Override
  public void databaseClosed(DatabaseEvent event) {
    setObjectContainer(null);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.service.DatabaseListener#databaseOpened
   * (org.rssowl.core.internal.persist.service.DatabaseEvent)
   */
  @Override
  public void databaseOpened(DatabaseEvent event) {
    setObjectContainer(event.getObjectContainer());
  }

  private synchronized void setObjectContainer(ObjectContainer db) {
    fDb = db;
    if (fDb == null) {
      fCurrent = -1;
      fCounter = null;
      fMax = 0;
    } else {
      fCounter = loadOrCreateCounter();
      fCurrent = fCounter.getValue();
      fMax = increaseMax(true);
    }
  }

  /*
   * @see org.rssowl.core.persist.service.IDGenerator#getNext()
   */
  @Override
  public long getNext() {
    return getNext(true);
  }

  /**
   * Implements the contract of {@link #getNext()} with additional control over
   * whether this method is allowed to commit a db4o transaction. This should be
   * set to <code>false</code> if this method is called from within a db4o
   * transaction. However, in the case the transaction is rolled back, the ids
   * provided during that transaction are invalid.
   *
   * @param commit
   * @return a long value that has not been returned from this method before.
   */
  public synchronized long getNext(boolean commit) {
    checkCurrent();
    ++fCurrent;
    if (fCurrent > fMax) {
      fMax = increaseMax(commit);
    }
    return fCurrent;
  }

  private void checkCurrent() {
    if (fCurrent == -1) {
      throw new IllegalStateException("current has not been initialised yet."); //$NON-NLS-1$
    }
  }

  private long increaseMax(boolean commit) {
    fCounter.increment(BATCH_SIZE);
    fDb.set(fCounter);
    if (commit)
      fDb.commit();

    return fCounter.getValue();
  }

  @Override
  public synchronized void shutdown() {
    if (fCounter != null) { //Could be NULL if DB never opened
      fMax = fCurrent;
      fCounter.setValue(fCurrent + 1);
      fDb.set(fCounter);
      fDb.commit();
    }
  }

  private Counter loadCounter() {
    List<Counter> counters = fDb.ext().query(Counter.class);
    if (!counters.isEmpty())
      return counters.iterator().next();

    return null;
  }

  private Counter loadOrCreateCounter() {
    Counter counter = loadCounter();
    if (counter == null)
      counter = new Counter(0L);

    return counter;
  }
}
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

/**
 * A simple pojo to serve as counter, e.g. for ids.
 */
public final class Counter {
  private long fValue;

  /**
   * Provided for deserialization.
   */
  protected Counter() {
    super();
  }

  /**
   * @param value the initial value of the counter
   */
  public Counter(long value) {
    fValue = value;
  }

  /**
   * @param amount the value to increment the counter by.
   * @return the new value of the counter.
   */
  public long increment(int amount) {
    fValue += amount;
    return fValue;
  }

  /**
   * @return the current value of the counter.
   */
  public final long getValue() {
    return fValue;
  }

  /**
   * @param value the new value of the counter.
   */
  public void setValue(long value) {
    this.fValue = value;
  }
}
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

package org.rssowl.core.util;

/**
 * A Listener being called from the <code>JobQueue</code> on certain Events.
 * 
 * @author bpasero
 */
public interface JobQueueListener {

  /** 
   * Called when all scheduled Jobs in the queue have been finished. Note that 
   * there is no guarantee that the queue will be empty by the time the
   * listener is called. It just means that the Queue had been empty at some
   * point before the listener was called.
   * <p>
   * One of the scenarios where this listener is useful is when the 
   * user of the queue is not scheduling jobs anymore, and it needs to wait
   * for the currently scheduled ones to finish before it can proceed to
   * execute something else. 
   */
  void workDone();
}
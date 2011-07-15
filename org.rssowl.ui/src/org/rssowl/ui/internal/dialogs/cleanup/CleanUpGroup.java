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

package org.rssowl.ui.internal.dialogs.cleanup;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>CleanUpGroup</code> contains a list of <code>CleanUpTasks</code>
 * to perform.
 *
 * @author bpasero
 */
public class CleanUpGroup {
  List<CleanUpTask> fTasks;
  private final String fLabel;

  CleanUpGroup(String label) {
    fLabel = label;
    fTasks = new ArrayList<CleanUpTask>();
  }

  void addTask(CleanUpTask task) {
    fTasks.add(task);
  }

  /**
   * @return Returns the list of clean up tasks for this group.
   */
  public List<CleanUpTask> getTasks() {
    return fTasks;
  }

  boolean isEmpty() {
    return fTasks.isEmpty();
  }

  String getLabel() {
    return fLabel;
  }
}
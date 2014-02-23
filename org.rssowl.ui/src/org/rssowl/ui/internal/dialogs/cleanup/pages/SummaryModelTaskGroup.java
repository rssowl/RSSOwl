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

package org.rssowl.ui.internal.dialogs.cleanup.pages;

import org.rssowl.ui.internal.dialogs.cleanup.tasks.AbstractCleanUpTask;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>CleanUpTaskGroup</code> contains a list of
 * <code>AbstractCleanUpTask</code> objects to perform.
 *
 * @author bpasero
 */
public class SummaryModelTaskGroup {
  List<AbstractCleanUpTask> fTasks;
  private final String fLabel;

  public SummaryModelTaskGroup(String label) {
    fLabel = label;
    fTasks = new ArrayList<AbstractCleanUpTask>();
  }

  public void addTask(AbstractCleanUpTask task) {
    if (task == null)
      return;

    fTasks.add(task);
  }

  /**
   * @return Returns the list of clean up tasks for this group.
   */
  public List<AbstractCleanUpTask> getTasks() {
    return fTasks;
  }

  public boolean isEmpty() {
    return fTasks.isEmpty();
  }

  public String getLabel() {
    return fLabel;
  }
}
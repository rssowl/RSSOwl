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

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.ui.internal.dialogs.cleanup.operations.DefaultOperations;
import org.rssowl.ui.internal.dialogs.cleanup.operations.DeleteFeedsAbstractOperation;
import org.rssowl.ui.internal.dialogs.cleanup.operations.DeleteNewsAbstractOperation;
import org.rssowl.ui.internal.dialogs.cleanup.operations.ICleanUpOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates the collection of <code>CleanUpTask</code> that the user may choose
 * to perform as clean up.
 *
 * @author bpasero
 */
public final class SummaryModel {
  private final List<SummaryModelTaskGroup> fSummaryTaskGroups;
  private final ArrayList<ICleanUpOperation> fOperations;
  private final Collection<IBookMark> fBookmarks;

  /**
   * @param operations
   * @param bookmarks
   */
  public SummaryModel(ArrayList<ICleanUpOperation> operations, Collection<IBookMark> bookmarks) {
    fOperations = operations;
    fBookmarks = bookmarks;
    fSummaryTaskGroups = new ArrayList<SummaryModelTaskGroup>();
  }

  /**
   * @return Returns the Task Groups
   */
  public List<SummaryModelTaskGroup> getSummaryTaskGroups() {
    return fSummaryTaskGroups;
  }

  /**
   * Calculate Tasks
   *
   * @param monitor
   */
  public void generate(IProgressMonitor monitor) {
    Collection<SummaryModelTaskGroup> groups;

    // reset some static fields
    DeleteNewsAbstractOperation.reset();
    DeleteFeedsAbstractOperation.reset();

    // add default tasks
    groups = DefaultOperations.process(monitor);
    for (SummaryModelTaskGroup gr : groups)
      addTaskGroup(gr);

    for (ICleanUpOperation op : fOperations) {

      /* Return if user cancelled the preview */
      if (monitor.isCanceled())
        return;

      if (!op.isEnabled())
        continue;

      if (op instanceof DeleteFeedsAbstractOperation) {
        SummaryModelTaskGroup group = DeleteFeedsAbstractOperation.process((DeleteFeedsAbstractOperation) op, fBookmarks, monitor);
        addTaskGroup(group);
        continue;
      }

      if (op instanceof DeleteNewsAbstractOperation) {
        DeleteNewsAbstractOperation.schedule((DeleteNewsAbstractOperation) op);
      }
    }

    // executing scheduled news delete operations
    groups = DeleteNewsAbstractOperation.process(fBookmarks, monitor);
    for (SummaryModelTaskGroup gr : groups)
      addTaskGroup(gr);
  }

  private void addTaskGroup(SummaryModelTaskGroup group) {
    // add non-empty group
    if (group != null && !group.isEmpty())
      fSummaryTaskGroups.add(group);
  }
}
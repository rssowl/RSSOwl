/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.dialogs.cleanup.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.DatabaseDefragmentTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.NewsFilterDisableOrphanedTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.SearchCleanUpTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.SearchMarkDeleteOrphanedTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.SearchOptimizeTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.SearchReindexTask;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultOperations implements ICleanUpOperation {

  public boolean isEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Add some default operations to execute while cleaning up
   *
   * @param monitor canceling and progress handling
   * @return list of <code>SummaryModelTaskGroup</code>
   */
  public static Collection<SummaryModelTaskGroup> process(IProgressMonitor monitor) {
    SummaryModelTaskGroup group;
    Collection<SummaryModelTaskGroup> groups = new ArrayList<SummaryModelTaskGroup>();

    // database tasks
    group = new SummaryModelTaskGroup(Messages.CleanUpModel_RECOMMENDED_DEFRAGMENT);
    group.addTask(new DatabaseDefragmentTask(group));
    groups.add(group);

    // search index tasks
    group = new SummaryModelTaskGroup(Messages.CleanUpModel_RECOMMENDED_SEARCH_INDEX);
    group.addTask(new SearchCleanUpTask(group));
    group.addTask(new SearchOptimizeTask(group));
    group.addTask(new SearchReindexTask(group));
    groups.add(group);

    // Look for orphaned saved searches
    group = new SummaryModelTaskGroup(Messages.CleanUpModel_DELETE_BROKEN_SEARCHES);
    group.addTask(SearchMarkDeleteOrphanedTask.create(group));
    // Look for orphaned news filters
    group.addTask(NewsFilterDisableOrphanedTask.create(group));
    groups.add(group);

    return groups;
  }

  public void savePreferences(IPreferenceScope preferences) {}

}

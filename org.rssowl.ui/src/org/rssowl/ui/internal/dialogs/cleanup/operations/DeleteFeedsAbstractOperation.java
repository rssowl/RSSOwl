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
import org.rssowl.core.persist.IBookMark;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.BookMarkDeleteTask;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class DeleteFeedsAbstractOperation implements ICleanUpOperation {

  private final static Set<IBookMark> bookmarksToDelete = new HashSet<IBookMark>();

  public static void reset(){
    bookmarksToDelete.clear();
  }
  public static boolean isScheduledToDelete(IBookMark mark) {
    return bookmarksToDelete.contains(mark);
  }

  protected abstract Collection<IBookMark> filter(Collection<IBookMark> bookmarks, IProgressMonitor monitor);

  protected abstract SummaryModelTaskGroup createGroup();

  public static SummaryModelTaskGroup process(DeleteFeedsAbstractOperation op, Collection<IBookMark> bookmarks, IProgressMonitor monitor) {
    Collection<IBookMark> validBookmarks = op.filter(bookmarks, monitor);
    if (validBookmarks.isEmpty())
      return null;
    SummaryModelTaskGroup group = op.createGroup();

    for (IBookMark mark : bookmarks) {
      /* Ignore if Bookmark gets already deleted */
      if (isScheduledToDelete(mark))
        continue;

      group.addTask(new BookMarkDeleteTask(group, mark));
      bookmarksToDelete.add(mark);
    }
    return group;
  }

}

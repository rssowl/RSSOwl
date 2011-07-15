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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.ui.internal.OwlUI;

import java.util.List;

/**
 * An instance of <code>CleanUpTask</code> to delete saved searches that are not
 * functional anymore because the search points to a location that was deleted
 * or does not exist.
 *
 * @author bpasero
 */
class DeleteOrphanedSearchMarkTask extends CleanUpTask {
  private final List<ISearchMark> fSearchMarks;

  DeleteOrphanedSearchMarkTask(CleanUpGroup group, List<ISearchMark> searchMarks) {
    super(group);
    fSearchMarks = searchMarks;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getImage()
   */
  @Override
  ImageDescriptor getImage() {
    return OwlUI.getImageDescriptor("icons/elcl16/remove.gif"); //$NON-NLS-1$
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getLabel()
   */
  @Override
  String getLabel() {
    if (fSearchMarks.size() == 1)
      return Messages.DeleteOrphanedSearchMarkTask_DELETED_ORPHANED_SEARCH_MARK;

    return NLS.bind(Messages.DeleteOrphanedSearchMarkTask_DELETE_ORPHANED_SEARCH_MARKS, fSearchMarks.size());
  }

  List<ISearchMark> getSearchMarks() {
    return fSearchMarks;
  }
}
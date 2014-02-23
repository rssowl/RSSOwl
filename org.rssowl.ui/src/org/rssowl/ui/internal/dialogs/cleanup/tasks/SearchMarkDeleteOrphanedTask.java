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

package org.rssowl.ui.internal.dialogs.cleanup.tasks;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.cleanup.PostProcessingWork;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An instance of <code>CleanUpTask</code> to delete saved searches that are not
 * functional anymore because the search points to a location that was deleted
 * or does not exist.
 *
 * @author bpasero
 */
public class SearchMarkDeleteOrphanedTask extends AbstractCleanUpTask {
  private final List<ISearchMark> fSearchMarks;

  public SearchMarkDeleteOrphanedTask(SummaryModelTaskGroup group, List<ISearchMark> searchMarks) {
    super(group);
    fSearchMarks = searchMarks;
  }

  public static AbstractCleanUpTask create(SummaryModelTaskGroup group) {
    List<ISearchMark> orphanedSearches = new ArrayList<ISearchMark>();
    Collection<ISearchMark> searches = DynamicDAO.loadAll(ISearchMark.class);
    for (ISearchMark search : searches) {
      if (CoreUtils.isOrphaned(search))
        orphanedSearches.add(search);
    }
    return orphanedSearches.isEmpty() ? null : new SearchMarkDeleteOrphanedTask(group, orphanedSearches);
  }

  @Override
  public void perform(PostProcessingWork flags) {
    DynamicDAO.deleteAll(fSearchMarks);
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getImage()
   */
  @Override
  public ImageDescriptor getImage() {
    return OwlUI.getImageDescriptor("icons/elcl16/remove.gif"); //$NON-NLS-1$
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getLabel()
   */
  @Override
  public String getLabel() {
    if (fSearchMarks.size() == 1)
      return Messages.TASK_LABEL_DELETE_ORPHANED_SEARCH_MARK;

    return NLS.bind(Messages.TASK_LABEL_DELETE_ORPHANED_SEARCH_MARK, fSearchMarks.size());
  }

  public String getSearchesNames() {
    StringBuilder smNames = new StringBuilder();
    for (ISearchMark search : fSearchMarks) {
      smNames.append(search.getName()).append(", "); //$NON-NLS-1$
    }

    if (smNames.length() != 0)
      smNames.delete(smNames.length() - 2, smNames.length());

    return smNames.toString();
  }

  public int getSearchesCount() {
    return fSearchMarks.size();
  }

  @Override
  public int getWorkUnits() {
    return fSearchMarks.size();
  }

  @Override
  public int getActualWorkUnits() {
    // TODO Auto-generated method stub
    return fSearchMarks.size();
  }
}
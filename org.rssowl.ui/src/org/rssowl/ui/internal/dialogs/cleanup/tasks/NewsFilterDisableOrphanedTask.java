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
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.cleanup.PostProcessingWork;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An instance of <code>CleanUpTask</code> to disable news filters that are not
 * functional anymore because either the search or the performed action points
 * to a location that was deleted or does not exist.
 *
 * @author bpasero
 */
public class NewsFilterDisableOrphanedTask extends AbstractCleanUpTask {
  private final List<ISearchFilter> fFilters;

  public NewsFilterDisableOrphanedTask(SummaryModelTaskGroup group, List<ISearchFilter> filters) {
    super(group);
    fFilters = filters;
  }

  public static AbstractCleanUpTask create(SummaryModelTaskGroup group) {
    List<ISearchFilter> orphanedFilters = new ArrayList<ISearchFilter>();
    Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
    for (ISearchFilter filter : filters) {
      if (filter.isEnabled() && CoreUtils.isOrphaned(filter))
        orphanedFilters.add(filter);
    }

    return orphanedFilters.isEmpty() ? null : new NewsFilterDisableOrphanedTask(group, orphanedFilters);
  }

  @Override
  public void perform(PostProcessingWork work) {
    List<ISearchFilter> filters = fFilters;
    for (ISearchFilter filter : filters) {
      filter.setEnabled(false);
    }
    DynamicDAO.saveAll(filters);
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getImage()
   */
  @Override
  public ImageDescriptor getImage() {
    return OwlUI.getImageDescriptor("icons/etool16/filter.gif"); //$NON-NLS-1$
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getLabel()
   */
  @Override
  public String getLabel() {
    if (fFilters.size() == 1)
      return Messages.TASK_LABEL_DISABLE_ORPHANED_NEWS_FILTER;

    return NLS.bind(Messages.TASK_LABEL_DISABLE_ORPHANED_NEWS_FILTERS, fFilters.size());
  }

  @Override
  public int getWorkUnits() {
    return fFilters.size();
  }

  @Override
  public int getActualWorkUnits() {
    return fFilters.size();
  }
}
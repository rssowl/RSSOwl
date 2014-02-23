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

package org.rssowl.ui.internal.dialogs.cleanup.tasks;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rssowl.core.Owl;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.cleanup.PostProcessingWork;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

public final class SearchReindexTask extends AbstractCleanUpTask {

  public SearchReindexTask(SummaryModelTaskGroup group) {
    super(group);
  }

  @Override
  public void perform(PostProcessingWork work) {
    Owl.getPersistenceService().getModelSearch().reIndexOnNextStartup();
    work.setAskForRestart(true);
  }

  @Override
  public String getLabel() {
    return Messages.TASK_LABEL_SEARCH_REINDEX;
  }

  @Override
  public ImageDescriptor getImage() {
    return OwlUI.getImageDescriptor("icons/obj16/reindex.gif"); //$NON-NLS-1$
  }
}

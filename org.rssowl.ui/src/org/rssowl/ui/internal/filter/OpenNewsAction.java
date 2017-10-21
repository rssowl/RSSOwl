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

package org.rssowl.ui.internal.filter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.rssowl.core.INewsAction;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.ITask;
import org.rssowl.core.util.JobQueue;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.TaskAdapter;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.OpenInBrowserAction;
import org.rssowl.ui.internal.util.JobRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link INewsAction} to open all matching news in the
 * browser.
 *
 * @author bpasero
 */
public class OpenNewsAction implements INewsAction {

  /* Delay between opening Browsers */
  private static final int OPEN_BROWSER_DELAY = 1000;

  private final JobQueue fOpenInBrowserQueue;

  /* Implementation of ITask to open a news link in the browser */
  private static class OpenInBrowserTask extends TaskAdapter {
    private final String fLink;

    OpenInBrowserTask(String link) {
      fLink = link;
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
      JobRunner.runInUIThread(null, new Runnable() {
        @Override
        public void run() {
          if (!monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
            OpenInBrowserAction openAction = new OpenInBrowserAction();
            openAction.setForceOpenInBackground(true);
            openAction.selectionChanged(null, new StructuredSelection(fLink));
            openAction.run();
          }
        }
      });

      /* Delay opening in external browser to avoid Program spam */
      if (OwlUI.useExternalBrowser() && !monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
        try {
          Thread.sleep(OPEN_BROWSER_DELAY);
        } catch (InterruptedException e) {
        }
      }

      return Status.OK_STATUS;
    }
  }

  public OpenNewsAction() {
    fOpenInBrowserQueue = new JobQueue(Messages.OpenNewsAction_OPEN_BROWSER_QUEUE, 1, Integer.MAX_VALUE, false, 0);
  }

  /*
   * @see org.rssowl.core.INewsAction#run(java.util.List, java.util.Map, java.lang.Object)
   */
  @Override
  public List<IEntity> run(List<INews> news, Map<INews, INews> replacements, Object data) {

    /* Handle the case of RSSOwl shutting down */
    if (Controller.getDefault().isShuttingDown()) {
      fOpenInBrowserQueue.cancel(false, true);
      return Collections.emptyList();
    }

    /* Ensure to Pickup Replaces */
    news = CoreUtils.replace(news, replacements);

    /* Fill into Queue */
    List<ITask> tasks = new ArrayList<ITask>(news.size());
    for (INews item : news) {
      String link = CoreUtils.getLink(item);
      if (StringUtils.isSet(link))
        tasks.add(new OpenInBrowserTask(link));
    }

    /* Schedule */
    fOpenInBrowserQueue.schedule(tasks);

    /* Handle the case of RSSOwl shutting down */
    if (Controller.getDefault().isShuttingDown()) {
      fOpenInBrowserQueue.cancel(false, true);
      return Collections.emptyList();
    }

    /* Nothing to Save */
    return Collections.emptyList();
  }

  /*
   * @see org.rssowl.core.INewsAction#conflictsWith(org.rssowl.core.INewsAction)
   */
  @Override
  public boolean conflictsWith(INewsAction otherAction) {
    return false;
  }

  /*
   * @see org.rssowl.core.INewsAction#getLabel(java.lang.Object)
   */
  @Override
  public String getLabel(Object data) {
    return null;
  }
}
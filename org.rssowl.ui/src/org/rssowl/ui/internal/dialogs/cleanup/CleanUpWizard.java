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

// eclipse
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
// rssowl core
import org.rssowl.core.Owl;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.StringUtils;
// rssowl UI
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.ConfirmDialog;
import org.rssowl.ui.internal.dialogs.cleanup.pages.EntitySelectionPage;
import org.rssowl.ui.internal.dialogs.cleanup.pages.OperationsPage;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryPage;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.AbstractCleanUpTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.BookMarkDeleteTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.NewsDeleteTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.SearchMarkDeleteOrphanedTask;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bpasero
 */
public class CleanUpWizard extends Wizard {
  private OperationsPage fCleanUpOptionsPage;
  private EntitySelectionPage fFeedSelectionPage;
  private SummaryPage fCleanUpSummaryPage;

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    setWindowTitle(Messages.CleanUpWizard_CLEAN_UP);
    setHelpAvailable(false);

    /* Choose Feeds for Clean-Up */
    fFeedSelectionPage = new EntitySelectionPage(Messages.CleanUpWizard_CHOOSE_BOOKMARKS);
    addPage(fFeedSelectionPage);

    /* Clean Up Options */
    fCleanUpOptionsPage = new OperationsPage(Messages.CleanUpWizard_CLEANUP_OPS);
    addPage(fCleanUpOptionsPage);

    /* Clean Up Summary */
    fCleanUpSummaryPage = new SummaryPage(Messages.CleanUpWizard_SUMMARY);
    addPage(fCleanUpSummaryPage);
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    final AtomicBoolean askForRestart = new AtomicBoolean(false);

    /* Receive Tasks */
    final List<AbstractCleanUpTask> tasks = fCleanUpSummaryPage.getTasks();

    // Show final confirmation prompt
    if (!showConfirmDialog(tasks))
      return false;

    /* Runnable that performs the tasks */
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) {

        // this is Hash for return boolean flags
        PostProcessingWork work = new PostProcessingWork();

        // start progress monitor
        int units = 0;
        units += 1; // from optimize search index
        for (AbstractCleanUpTask task : tasks)
          units += task.getWorkUnits();

        monitor.beginTask(Messages.CleanUpWizard_WAIT_CLEANUP, units);

        /* Perform Tasks */
        for (AbstractCleanUpTask task : tasks) {
          monitor.subTask(task.getLabel());
          task.perform(work);
          monitor.worked(task.getActualWorkUnits());
        }

        // if we should restart
        askForRestart.set(work.needRestart());

        /* Delete BookMarks */
        {
          monitor.subTask("Deleting Bookmarks"); //$NON-NLS-1$
          Controller.getDefault().getSavedSearchService().forceQuickUpdate();
          int taskUnits = work.bookmarksToDelete().size();
          DynamicDAO.deleteAll(work.bookmarksToDelete());
          monitor.worked(taskUnits);
        }

        /* Optimize Search */

        if (work.isOptimizeSearch()) {
          monitor.subTask("Optimizing Search Index"); //$NON-NLS-1$
          Owl.getPersistenceService().getModelSearch().optimize();
          monitor.worked(1);
        }

        monitor.done();
      }
    };

    /* Perform Runnable in separate Thread and show progress */
    try {
      getContainer().run(true, false, runnable); // runs in another thread?
    } catch (InvocationTargetException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    } catch (InterruptedException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }

    /* Save Operations Preferences in case clean up was performed */
    fCleanUpOptionsPage.savePreferences(Owl.getPreferenceService().getGlobalScope());

    /* Ask to restart if necessay */
    if (askForRestart.get()) {
      boolean restart = MessageDialog.openQuestion(getShell(), Messages.CleanUpWizard_RESTART_RSSOWL, Messages.CleanUpWizard_RESTART_TO_CLEANUP);
      if (restart) {
        Controller.getDefault().restart();
      }
    }

    return true;
  }

  /* Show final confirmation prompt */
  private boolean showConfirmDialog(List<AbstractCleanUpTask> tasks) {
    // SearchMarks info
    String searchmarksNames = null;
    int searchmarksCounter = 0;
    int bookmarksCounter = 0;
    int newsCounter = 0;
    for (AbstractCleanUpTask task : tasks) {
      if (task instanceof BookMarkDeleteTask) {
        bookmarksCounter++;
      } else if (task instanceof NewsDeleteTask) {
        newsCounter += ((NewsDeleteTask) task).getNews().size();
      } else if (task instanceof SearchMarkDeleteOrphanedTask) {
        searchmarksNames = ((SearchMarkDeleteOrphanedTask) task).getSearchesNames();
        searchmarksCounter += ((SearchMarkDeleteOrphanedTask) task).getSearchesCount();
      }
    }

    if (bookmarksCounter != 0 || newsCounter != 0 || searchmarksCounter != 0) {
      StringBuilder msg = new StringBuilder(Messages.CleanUpWizard_PLEASE_CONFIRM_DELETE).append("\n\n"); //$NON-NLS-1$

      if (bookmarksCounter == 1) {
        msg.append(Messages.CleanUpWizard_ONE_FEED).append("\n"); //$NON-NLS-1$
      } else if (bookmarksCounter > 1) {
        msg.append(NLS.bind(Messages.CleanUpWizard_N_FEEDS, bookmarksCounter)).append("\n"); //$NON-NLS-1$
      }

      if (newsCounter != 0) {
        msg.append(NLS.bind(Messages.CleanUpWizard_N_NEWS, newsCounter)).append("\n"); //$NON-NLS-1$
      }

      if (searchmarksCounter == 1) {
        msg.append(NLS.bind(Messages.CleanUpWizard_ONE_SEARCH, StringUtils.replaceAll(searchmarksNames, "&", "&&"))).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if (searchmarksCounter > 1) {
        msg.append(NLS.bind(Messages.CleanUpWizard_N_SEARCHES, searchmarksCounter, StringUtils.replaceAll(searchmarksNames, "&", "&&"))).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }

      ConfirmDialog dialog = new ConfirmDialog(getShell(), Messages.CleanUpWizard_CONFIRM_DELETE, Messages.CleanUpWizard_NO_UNDO, msg.toString(), null);
      if (dialog.open() != Window.OK)
        return false;
    }

    /* Restore Editors if Bookmarks are to be deleted */
    if (bookmarksCounter > 0) {
      OwlUI.getFeedViews();
    }

    return true;
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#needsProgressMonitor()
   */
  @Override
  public boolean needsProgressMonitor() {
    return true;
  }
}
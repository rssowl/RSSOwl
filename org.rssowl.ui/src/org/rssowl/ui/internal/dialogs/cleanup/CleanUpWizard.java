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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.ConfirmDialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bpasero
 */
public class CleanUpWizard extends Wizard {
  private CleanUpOptionsPage fCleanUpOptionsPage;
  private FeedSelectionPage fFeedSelectionPage;
  private CleanUpSummaryPage fCleanUpSummaryPage;

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    setWindowTitle(Messages.CleanUpWizard_CLEAN_UP);
    setHelpAvailable(false);

    /* Choose Feeds for Clean-Up */
    fFeedSelectionPage = new FeedSelectionPage(Messages.CleanUpWizard_CHOOSE_BOOKMARKS);
    addPage(fFeedSelectionPage);

    /* Clean Up Options */
    fCleanUpOptionsPage = new CleanUpOptionsPage(Messages.CleanUpWizard_CLEANUP_OPS);
    addPage(fCleanUpOptionsPage);

    /* Clean Up Summary */
    fCleanUpSummaryPage = new CleanUpSummaryPage(Messages.CleanUpWizard_SUMMARY);
    addPage(fCleanUpSummaryPage);
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    final INewsDAO newsDao = DynamicDAO.getDAO(INewsDAO.class);
    final CleanUpOperations operations = fCleanUpOptionsPage.getOperations();
    final AtomicBoolean askForRestart = new AtomicBoolean(false);

    /* Receive Tasks */
    final List<CleanUpTask> tasks = fCleanUpSummaryPage.getTasks();

    /* Show final confirmation prompt */
    StringBuilder smNames= new StringBuilder();
    int bmCounter = 0;
    int smCounter = 0;
    int newsCounter = 0;
    for (CleanUpTask task : tasks) {
      if (task instanceof BookMarkTask)
        bmCounter++;
      else if (task instanceof NewsTask)
        newsCounter += ((NewsTask) task).getNews().size();
      else if (task instanceof DeleteOrphanedSearchMarkTask) {
        List<ISearchMark> searches = ((DeleteOrphanedSearchMarkTask) task).getSearchMarks();
        smCounter += searches.size();
        for (ISearchMark search : searches) {
          smNames.append(search.getName()).append(", "); //$NON-NLS-1$
        }

        if (smNames.length() != 0)
          smNames.delete(smNames.length() - 2, smNames.length());
      }
    }

    if (bmCounter != 0 || newsCounter != 0 || smCounter != 0) {
      StringBuilder msg = new StringBuilder(Messages.CleanUpWizard_PLEASE_CONFIRM_DELETE).append("\n\n"); //$NON-NLS-1$

      if (bmCounter == 1)
        msg.append(Messages.CleanUpWizard_ONE_FEED).append("\n"); //$NON-NLS-1$
      else if (bmCounter > 1)
        msg.append(NLS.bind(Messages.CleanUpWizard_N_FEEDS, bmCounter)).append("\n"); //$NON-NLS-1$

      if (newsCounter != 0)
        msg.append(NLS.bind(Messages.CleanUpWizard_N_NEWS, newsCounter)).append("\n"); //$NON-NLS-1$

      if (smCounter == 1)
        msg.append(NLS.bind(Messages.CleanUpWizard_ONE_SEARCH, StringUtils.replaceAll(smNames.toString(), "&", "&&"))).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      else if (smCounter > 1)
        msg.append(NLS.bind(Messages.CleanUpWizard_N_SEARCHES, smCounter, StringUtils.replaceAll(smNames.toString(), "&", "&&"))).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      ConfirmDialog dialog = new ConfirmDialog(getShell(), Messages.CleanUpWizard_CONFIRM_DELETE, Messages.CleanUpWizard_NO_UNDO, msg.toString(), null);
      if (dialog.open() != Window.OK)
        return false;
    }

    /* Restore Editors if Bookmarks are to be deleted */
    if (bmCounter > 0)
      OwlUI.getFeedViews();

    /* Runnable that performs the tasks */
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) {
        IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
        boolean optimizeSearch = false;
        monitor.beginTask(Messages.CleanUpWizard_WAIT_CLEANUP, IProgressMonitor.UNKNOWN);

        /* Perform Tasks */
        List<IBookMark> bookmarks = new ArrayList<IBookMark>();
        for (CleanUpTask task : tasks) {

          /* Delete Bookmark Task */
          if (task instanceof BookMarkTask)
            bookmarks.add(((BookMarkTask) task).getMark());

          /* Delete News Task */
          else if (task instanceof NewsTask) {
            Collection<NewsReference> news = ((NewsTask) task).getNews();
            List<INews> resolvedNews = new ArrayList<INews>(news.size());
            for (NewsReference newsRef : news) {
              INews resolvedNewsItem = newsRef.resolve();
              if (resolvedNewsItem != null && resolvedNewsItem.isVisible())
                resolvedNews.add(resolvedNewsItem);
              else
                CoreUtils.reportIndexIssue();
            }

            newsDao.setState(resolvedNews, INews.State.DELETED, false, false);
          }

          /* Delete Orphaned Searches */
          else if (task instanceof DeleteOrphanedSearchMarkTask) {
            List<ISearchMark> searches = ((DeleteOrphanedSearchMarkTask) task).getSearchMarks();
            DynamicDAO.deleteAll(searches);
          }

          /* Disable Orphaned News Filters */
          else if (task instanceof DisableOrphanedNewsFiltersTask) {
            List<ISearchFilter> filters = ((DisableOrphanedNewsFiltersTask) task).getFilters();
            for (ISearchFilter filter : filters) {
              filter.setEnabled(false);
            }
            DynamicDAO.saveAll(filters);
          }

          /* Optimize Lucene Index */
          else if (task instanceof OptimizeSearchTask)
            optimizeSearch = true;

          /* Clean-Up Lucene Index */
          else if (task instanceof CleanUpIndexTask) {
            preferences.putBoolean(DefaultPreferences.CLEAN_UP_INDEX, false);
            Owl.getPersistenceService().getModelSearch().cleanUpOnNextStartup();
            askForRestart.set(true);
          }

          /* Defrag Database */
          else if (task instanceof DefragDatabaseTask) {
            Owl.getPersistenceService().defragmentOnNextStartup();
            askForRestart.set(true);
          }
        }

        /* Delete BookMarks */
        Controller.getDefault().getSavedSearchService().forceQuickUpdate();
        DynamicDAO.deleteAll(bookmarks);

        /* Optimize Search */
        if (optimizeSearch)
          Owl.getPersistenceService().getModelSearch().optimize();

        /* Save Operations */
        preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_LAST_VISIT_STATE, operations.deleteFeedByLastVisit());
        preferences.putInteger(DefaultPreferences.CLEAN_UP_BM_BY_LAST_VISIT_VALUE, operations.getLastVisitDays());

        preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_LAST_UPDATE_STATE, operations.deleteFeedByLastUpdate());
        preferences.putInteger(DefaultPreferences.CLEAN_UP_BM_BY_LAST_UPDATE_VALUE, operations.getLastUpdateDays());
        if (SyncUtils.hasSyncCredentials())
          preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_SYNCHRONIZATION, operations.deleteFeedsBySynchronization());

        preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_CON_ERROR, operations.deleteFeedsByConError());
        preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_DUPLICATES, operations.deleteFeedsByDuplicates());

        preferences.putBoolean(DefaultPreferences.CLEAN_UP_NEWS_BY_AGE_STATE, operations.deleteNewsByAge());
        preferences.putInteger(DefaultPreferences.CLEAN_UP_NEWS_BY_AGE_VALUE, operations.getMaxNewsAge());

        preferences.putBoolean(DefaultPreferences.CLEAN_UP_NEWS_BY_COUNT_STATE, operations.deleteNewsByCount());
        preferences.putInteger(DefaultPreferences.CLEAN_UP_NEWS_BY_COUNT_VALUE, operations.getMaxNewsCountPerFeed());

        preferences.putBoolean(DefaultPreferences.CLEAN_UP_READ_NEWS_STATE, operations.deleteReadNews());
        preferences.putBoolean(DefaultPreferences.CLEAN_UP_NEVER_DEL_UNREAD_NEWS_STATE, operations.keepUnreadNews());
        preferences.putBoolean(DefaultPreferences.CLEAN_UP_NEVER_DEL_LABELED_NEWS_STATE, operations.keepLabeledNews());

        monitor.done();
      }
    };

    /* Perform Runnable in separate Thread and show progress */
    try {
      getContainer().run(true, false, runnable);
    } catch (InvocationTargetException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    } catch (InterruptedException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }

    /* Ask to restart if Task was used */
    if (askForRestart.get()) {
      boolean restart = MessageDialog.openQuestion(getShell(), Messages.CleanUpWizard_RESTART_RSSOWL, Messages.CleanUpWizard_RESTART_TO_CLEANUP);
      if (restart)
        Controller.getDefault().restart();
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
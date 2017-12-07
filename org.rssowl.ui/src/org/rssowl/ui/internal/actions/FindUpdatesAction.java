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

package org.rssowl.ui.internal.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.operations.IInstallFeatureOperation;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.dialogs.UpdateDialog;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.UIBackgroundJob;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An action to check for updates to RSSOwl. Will not include updates to
 * installed extensions.
 *
 * @author bpasero
 */
public class FindUpdatesAction extends Action implements IWorkbenchWindowActionDelegate {
  private static final String UPDATE_SITE = "http://boreal.rssowl.org/update/program/"; //$NON-NLS-1$

  /* System Property to Control Site for Updates to look for */
  private static final String EXTENSION_SITE_PROPERTY = "updateSite"; //$NON-NLS-1$

  private Shell fShell;
  private final boolean fUserInitiated;

  /** Keep default constructor for reflection. */
  public FindUpdatesAction() {
    this(true);
  }

  /**
   * @param userInitiated if <code>true</code> will open a dialog informing about
   * updates, even when none exist.
   */
  public FindUpdatesAction(boolean userInitiated) {
    fUserInitiated = userInitiated;
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {

    /* Respect System Property */
    if (Controller.getDefault().isUpdateDisabled())
      return;

    try {
      UpdateSearchScope scope = new UpdateSearchScope();

      /* Check for User Defined Sites from System Property */
      String extensionSites = System.getProperty(EXTENSION_SITE_PROPERTY);
      if (StringUtils.isSet(extensionSites)) {
        try {
          URL url = new URL(extensionSites);
          scope.addSearchSite(url.toString(), url, null);
        } catch (MalformedURLException e) {
          // skip bad URLs
        }
      }

      /* Add RSSOwl.org if user did not define any other sites */
      if (scope.getSearchSites().length == 0) {
        URL url = new URL(UPDATE_SITE);
        scope.addSearchSite("RSSOwl.org", url, null); //$NON-NLS-1$
        scope.setFeatureProvidedSitesEnabled(false);
      }

      /* Run in Update Job */
      final UpdateJob job = new UpdateJob(Messages.FindUpdatesAction_UPDATE_SEARCH, true, false);
      job.getSearchRequest().setScope(scope);
      job.addJobChangeListener(new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
          JobRunner.runInUIThread(fShell, new Runnable() {
            @Override
            public void run() {
              if (Controller.getDefault().isShuttingDown() || (fShell != null && fShell.isDisposed()))
                return;

              if (job.getStatus().isOK())
                handleUpdates(job.getUpdates());
              else
                handleError(job.getStatus());
            }
          });
        }
      });

      if (fUserInitiated) {
        job.setUser(true);
        job.setPriority(Job.INTERACTIVE);
      } else {
        job.setUser(false);
        job.setSystem(true);
      }

      /* Schedule */
      job.schedule();
    } catch (MalformedURLException e) {
      Activator.safeLogError(e.getMessage(), e);
    }
  }

  /* Handle Updates */
  @SuppressWarnings("restriction")
  private void handleUpdates(final IInstallFeatureOperation[] updates) {

    /* Inform that no Updates are Available (if user initiated) */
    if (updates.length == 0) {
      if (fUserInitiated)
        MessageDialog.openInformation(fShell, Messages.FindUpdatesAction_CHECK_UPDATES, Messages.FindUpdatesAction_NO_UPDATES_AVAILABLE);
      return;
    }

    /* Ask for Confirmation to Update */
    UpdateDialog dialog = new UpdateDialog(fShell, updates);
    if (dialog.open() != IDialogConstants.OK_ID)
      return;

    /* Show Activity */
    ShowActivityAction action = new ShowActivityAction();
    action.init(fShell);
    action.run(null);

    /* Perform Update */
    JobRunner.runUIUpdater(new UIBackgroundJob(fShell, Messages.FindUpdatesAction_DOWNLOADING_UPDATES) {
      boolean errorUpdating;

      /* Download & Install Updates */
      @Override
      protected void runInBackground(IProgressMonitor monitor) {
        for (IInstallFeatureOperation update : updates) {
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
            break;

          try {
            IConfiguredSite configSite = org.eclipse.update.internal.operations.UpdateUtils.getDefaultTargetSite(SiteManager.getLocalSite().getCurrentConfiguration(), update);
            if (configSite != null) {
              update.setTargetSite(configSite);
              Activator.safeLogInfo("Start: Application Update"); //$NON-NLS-1$
              update.execute(monitor, null);
              Activator.safeLogInfo("Finished: Application Update"); //$NON-NLS-1$
            }
          } catch (CoreException e) {
            errorUpdating = true;
            Activator.safeLogError(e.getMessage(), e);
          } catch (InvocationTargetException e) {
            errorUpdating = true;
            Activator.safeLogError(e.getMessage(), e);
          }
        }
      }

      /* Ask to Restart */
      @Override
      protected void runInUI(IProgressMonitor monitor) {
        if (!Controller.getDefault().isShuttingDown()) {
          if (!errorUpdating) {
            boolean restart = MessageDialog.openQuestion(fShell, Messages.FindUpdatesAction_RESTART_RSSOWL, Messages.FindUpdatesAction_RESTART_AFTER_UPDATE);
            if (restart)
              Controller.getDefault().restart();
          } else
            MessageDialog.openWarning(fShell, Messages.FindUpdatesAction_CHECK_UPDATES, Messages.FindUpdatesAction_WARNING_UPDATE_FAILED);
        }
      }
    }, true);
  }

  /* Handle Error */
  private void handleError(IStatus status) {
    if (fUserInitiated) {
      String msg = Messages.FindUpdatesAction_WARNING_SEARCH_FAILED;
      if (StringUtils.isSet(status.getMessage()))
        msg += "\n\n" + NLS.bind(Messages.FindUpdatesAction_REASON, status.getMessage()); //$NON-NLS-1$

      MessageDialog.openWarning(fShell, Messages.FindUpdatesAction_CHECK_UPDATES, msg);
    }
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  @Override
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  public void init(IWorkbenchWindow window) {
    fShell = window.getShell();
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run(IAction action) {
    run();
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {}
}
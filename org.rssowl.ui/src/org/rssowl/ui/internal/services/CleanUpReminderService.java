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

package org.rssowl.ui.internal.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.CleanUpAction;
import org.rssowl.ui.internal.dialogs.CleanUpReminderDialog;
import org.rssowl.ui.internal.util.JobRunner;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple service that controls showing a reminder for clean-up if required.
 *
 * @author bpasero@rssowl.org
 */
public class CleanUpReminderService {

  /* Delay in Millies to try opening the reminder when the Shell is Minimized to Tray */
  private static final long SHELL_MINIMIZED_RESCHEDULE_DELAY = 1000 * 60 * 5;

  private final Job fReminderJob;
  private final IPreferenceScope fPreferences = Owl.getPreferenceService().getGlobalScope();

  /**
   * Instantiates a new Clean-Up Reminder Service.
   */
  public CleanUpReminderService() {
    fReminderJob = createJob();
    fReminderJob.setSystem(true);
    fReminderJob.setUser(false);

    initIfNecessary();
    reschedule();
  }

  /* Check if this is the first start */
  private void initIfNecessary() {
    if (fPreferences.getBoolean(DefaultPreferences.CLEAN_UP_REMINDER_STATE)) {
      long millies = fPreferences.getLong(DefaultPreferences.CLEAN_UP_REMINDER_DATE_MILLIES);
      if (millies == 0)
        storeNextReminderDate();
    }
  }

  private void reschedule() {
    if (fPreferences.getBoolean(DefaultPreferences.CLEAN_UP_REMINDER_STATE)) {
      long nextReminderDate = fPreferences.getLong(DefaultPreferences.CLEAN_UP_REMINDER_DATE_MILLIES);
      long diff = nextReminderDate - System.currentTimeMillis();

      fReminderJob.schedule(diff > 0 ? diff : 0);
    }
  }

  private Job createJob() {
    return new Job("") { //$NON-NLS-1$
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        /* Check if Reminder should show */
        if (!monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {

          /* Check if reminder is enabled */
          if (!fPreferences.getBoolean(DefaultPreferences.CLEAN_UP_REMINDER_STATE))
            return Status.OK_STATUS;

          /* Show Reminder */
          final Shell shell = OwlUI.getPrimaryShell();
          if (shell != null && !monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
            final AtomicBoolean needShortReschedule = new AtomicBoolean(false);

            JobRunner.runSyncedInUIThread(shell, new Runnable() {
              public void run() {
                if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
                  return;

                CleanUpReminderDialog visibleInstance = CleanUpReminderDialog.getVisibleInstance();

                /* Shell is Minimized to Tray, reschedule shortly later */
                if (visibleInstance == null && !shell.isVisible())
                  needShortReschedule.set(true);

                /* Open Cleanup Reminder Dialog */
                else if (visibleInstance == null && new CleanUpReminderDialog(shell).open() == IDialogConstants.OK_ID) {
                  OwlUI.restoreWindow(shell);
                  new CleanUpAction().openWizard(shell);
                }
              }
            });

            /* Shell is Minimized to Tray, try again after a short delay */
            if (needShortReschedule.get())
              fReminderJob.schedule(SHELL_MINIMIZED_RESCHEDULE_DELAY);

            /* Store Next Date and reschedule */
            else if (fPreferences.getBoolean(DefaultPreferences.CLEAN_UP_REMINDER_STATE)) {
              storeNextReminderDate();
              reschedule();
            }
          }
        }

        return Status.OK_STATUS;
      }
    };
  }

  private void storeNextReminderDate() {
    int days = fPreferences.getInteger(DefaultPreferences.CLEAN_UP_REMINDER_DAYS_VALUE);
    fPreferences.putLong(DefaultPreferences.CLEAN_UP_REMINDER_DATE_MILLIES, System.currentTimeMillis() + (days * DateUtils.DAY));
  }

  /**
   * Stops this Service.
   */
  public void stopService() {
    fReminderJob.cancel();
  }
}
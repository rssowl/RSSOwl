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

package org.rssowl.ui.internal.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.rssowl.core.util.ITask;

/**
 * The JobTracker guarantees that at anytime only the most recent
 * <code>ITask</code> is executed.
 *
 * @author bpasero
 */
public class JobTracker {
  private final int fDelay;
  private final boolean fShowProgress;
  private final boolean fRunInUIThread;
  private Job fJob;
  private ITask fTask;
  private volatile boolean fRunning;

  /**
   * Creates a new JobTracker with the given Delay. The JobTracker guarantees
   * that at anytime only the most recent <code>ITask</code> is executed.
   *
   * @param delay The delay in milliseconds after which new Tasks should be
   * scheduled.
   * @param showProgress if <code>TRUE</code>, progress is shown in the Progress
   * View.
   * @param priority The Priority of the <code>ITask</code>s that are to be run
   * by this tracker.
   */
  public JobTracker(int delay, boolean showProgress, ITask.Priority priority) {
    this(delay, showProgress, false, priority);
  }

  /**
   * Creates a new JobTracker with the given Delay. The JobTracker guarantees
   * that at anytime only the most recent <code>ITask</code> is executed.
   *
   * @param delay The delay in milliseconds after which new Tasks should be
   * scheduled.
   * @param showProgress if <code>TRUE</code>, progress is shown in the Progress
   * View.
   * @param runInUIThread if <code>TRUE</code>, this Job will run in the
   * UI-Thread by creating a <code>UIJob</code>.
   * @param priority The Priority of the <code>ITask</code>s that are to be run
   * by this tracker.
   */
  public JobTracker(int delay, boolean showProgress, boolean runInUIThread, ITask.Priority priority) {
    fDelay = delay;
    fShowProgress = showProgress;
    fRunInUIThread = runInUIThread;
    createJob(priority);
  }

  /**
   * Get the Delay after which Jobs should be scheduled for any new Tasks.
   *
   * @return Returns the Delay after which Jobs should be scheduled for any new
   * given Tasks.
   */
  public int getDelay() {
    return fDelay;
  }

  /**
   * Runs the given task after a certain delay that can be configured for this
   * Tracker. Any previous Task that the Tracker scheduled is canceld first, if
   * it was not yet running.
   *
   * @param task An instanceof <code>ITask</code> to be scheduled after the
   * Delay that has been set for this Tracker.
   */
  public void run(final ITask task) {

    /* Cancel any other JobTracker of this Family first */
    cancel();

    /* Set current Task */
    fTask = task;

    /* Schedule with Delay */
    fRunning = true;
    fJob.schedule(getDelay());
  }

  private void createJob(ITask.Priority priority) {

    /* Create a UI-Job */
    if (fRunInUIThread) {
      fJob = new UIJob("") { //$NON-NLS-1$
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
          try {
            if (!monitor.isCanceled() && fTask != null)
              return fTask.run(monitor);

            return Status.CANCEL_STATUS;
          } finally {
            fRunning = false;
          }
        }

        @Override
        public boolean belongsTo(Object family) {
          return family == JobTracker.this;
        }
      };
    }

    /* Create a Normal Job */
    else {
      fJob = new Job("") { //$NON-NLS-1$
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          try {
            if (!monitor.isCanceled() && fTask != null)
              return fTask.run(monitor);

            return Status.CANCEL_STATUS;
          } finally {
            fRunning = false;
          }
        }

        @Override
        public boolean belongsTo(Object family) {
          return family == JobTracker.this;
        }
      };
    }

    /* Make it a System Job if required */
    if (!fShowProgress)
      fJob.setSystem(true);

    /* Apply Priority */
    if (priority == ITask.Priority.INTERACTIVE)
      fJob.setPriority(Job.INTERACTIVE);
    else if (priority == ITask.Priority.SHORT)
      fJob.setPriority(Job.SHORT);
  }

  /** Cancels any Task that did not yet run. */
  public void cancel() {

    /* First check if actually running */
    if (!fRunning)
      return;

    /* Cancel */
    Job.getJobManager().cancel(this);
    fRunning = false;
  }

  /**
   * @return <code>true</code> if the JobTracker has a {@link ITask} pending to
   * run and <code>false</code> otherwise.
   */
  public boolean isRunning() {
    return fRunning;
  }
}
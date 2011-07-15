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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.progress.IProgressConstants;
import org.rssowl.core.util.ITask;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.services.DownloadService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link DownloadJobQueue} is used by the {@link DownloadService} to queue
 * requests for files to download.
 *
 * @author bpasero
 */
public class DownloadJobQueue {
  private final AtomicInteger fScheduledJobs = new AtomicInteger(0); // Count number of running Jobs
  private final BlockingQueue<DownloadTask> fOpenTasksQueue;
  private final int fMaxConcurrentJobs;
  private String fName;

  /** The Task used for Downloads */
  public static abstract class DownloadTask implements ITask {

    /*
     * @see org.rssowl.core.util.ITask#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus run(IProgressMonitor monitor) {
      return run(null, monitor);
    }

    /**
     * @param job the {@link Job} that is running this task.
     * @param monitor The provided monitor can be used to report progress and
     * respond to cancellation. If the progress monitor has been canceled, the
     * task should finish its execution at the earliest convenience and return a
     * result status of severity IStatus.CANCEL.
     * @return Returns the result of the operation as an instance of
     * <code>IStatus</code>.
     * @see org.rssowl.core.util.ITask#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public abstract IStatus run(Job job, IProgressMonitor monitor);
  }

  /**
   * Creates an instance of <code>JobQueue</code> that allows to add
   * <code>Runnables</code> into a Queue to process them in Jobs up to a certain
   * amount of allowed parallel Jobs.
   *
   * @param name A human-readable name that is displayed in the Progress-View
   * while the Queue is processed.
   * @param maxConcurrentJobs The maximum number of concurrent running Tasks.
   * @param maxQueueSize The maximum number of tasks that this queue will accept
   * before blocking.
   */
  public DownloadJobQueue(String name, int maxConcurrentJobs, int maxQueueSize) {
    Assert.isNotNull(name);
    fName = name;
    fMaxConcurrentJobs = maxConcurrentJobs;
    fOpenTasksQueue = new LinkedBlockingQueue<DownloadTask>(maxQueueSize);
  }

  /**
   * Adds the given Task into the Queue waiting if necessary for space to become
   * available. The Task is processed in a <code>Job</code> once the number of
   * parallel processed Tasks is below <code>MAX_SCHEDULED_JOBS</code>.
   *
   * @param task The Task to add into this Queue.
   * @return {@code true} if all the tasks were scheduled or {@code false} if
   * some tasks were not scheduled because the current thread was interrupted.
   */
  public boolean schedule(DownloadTask task) {
    return schedule(Collections.singletonList(task));
  }

  /**
   * Adds the given List of Tasks into the Queue waiting is necessary for space
   * to become available. Each Runnable is processed in a <code>Job</code> once
   * the number of parallel processed Tasks is below
   * <code>MAX_SCHEDULED_JOBS</code>.
   *
   * @param tasks The Tasks to add into this Queue.
   * @return {@code true} if all the tasks were scheduled or {@code false} if
   * some tasks were not scheduled because the current thread was interrupted.
   */
  public boolean schedule(List<DownloadTask> tasks) {
    final int tasksSize = tasks.size();

    /* Ignore empty lists */
    if (tasksSize == 0)
      return true;

    /* Add into List of open tasks */
    for (DownloadTask task : tasks) {
      try {
        fOpenTasksQueue.put(task);
      } catch (InterruptedException e) {
        return false;
      }
    }

    /* Optimisation: We are able to release the calling thread without locking. */
    if (fScheduledJobs.get() >= fMaxConcurrentJobs)
      return true;

    /* Start a new Job for each free Slot */
    for (int i = 0; i < tasksSize && !fOpenTasksQueue.isEmpty(); ++i) {

      /* Never exceed max number of allowed concurrent Jobs */
      if (fScheduledJobs.incrementAndGet() > fMaxConcurrentJobs) {
        fScheduledJobs.decrementAndGet();
        break;
      }

      /* Schedule Job */
      scheduleTaskJob();
    }

    return true;
  }

  private void scheduleTaskJob() {

    /* Create the Job */
    Job job = createTaskJob();

    /* Listen to Job's Lifecycle */
    job.addJobChangeListener(new JobChangeAdapter() {

      /* Update Fields when a Job is Done */
      @Override
      public void done(IJobChangeEvent event) {

        /* Schedule a new Job if there is work left to do */
        if (!fOpenTasksQueue.isEmpty())
          scheduleTaskJob();
        else
          fScheduledJobs.decrementAndGet();
      }
    });

    /* Do not interrupt on any Error */
    job.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);

    /* Schedule it immediately */
    job.schedule();
  }

  /**
   * Determines whether the given Task is already queued in this Queue. That is,
   * the Task is scheduled and did not yet run to completion.
   *
   * @param task The Task to check for being queued in this Queue.
   * @return <code>TRUE</code> in case the given Task is already queued in this
   * Queue, meaning that it has been scheduled but did not yet complete
   * execution, <code>FALSE</code> otherwise.
   */
  public boolean isQueued(DownloadTask task) {
    return fOpenTasksQueue.contains(task);
  }

  /* Create a Job for a Task to handle */
  private Job createTaskJob() {
    Job job = new Job(fName) {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        final Job job = this;
        final IStatus[] status = new IStatus[1];

        /* Poll the next Task */
        final DownloadTask task = fOpenTasksQueue.poll();

        /* Queue is empty - so all work is done */
        if (task == null)
          return Status.OK_STATUS;

        /* Perform the Operation if not yet Cancelled */
        if (!monitor.isCanceled()) {
          SafeRunner.run(new LoggingSafeRunnable() {
            public void run() throws Exception {
              status[0] = task.run(job, monitor);

              /* Log anything that is an Error */
              if (status[0].getSeverity() == IStatus.ERROR) {
                if (Activator.getDefault() != null)
                  Activator.getDefault().getLog().log(status[0]);
              }
            }
          });
        }

        /* Inform about cancelation if present */
        return monitor.isCanceled() ? Status.CANCEL_STATUS : status[0];
      }

      @Override
      public boolean belongsTo(Object family) {
        return family == DownloadJobQueue.this;
      }
    };

    return job;
  }

  /**
   * Cancels all Jobs that belong to this Queue. Optionally the caller may
   * decide to join the running Jobs that are not yet done. Note that this will
   * <em>block</em> the calling Thread until all running Tasks have finished so
   * this should only be considered for <em>short-running</em> Tasks.
   *
   * @param joinRunning If <code>TRUE</code>, join the running Jobs that are not
   * yet done.
   */
  public void cancel(boolean joinRunning) {
    synchronized (this) {

      /* Clear open tasks */
      fOpenTasksQueue.clear();

      /* Cancel scheduled Jobs */
      Job.getJobManager().cancel(this);
    }
    /* Join running Jobs if any */
    if (joinRunning) {
      while (Job.getJobManager().find(this).length != 0) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          break;
        }
      }
    }
  }

  /**
   * @return <code>true</code> if there are active download jobs running and
   * <code>false</code> otherwise.
   */
  public boolean isWorking() {
    Job[] activeDownloads = Job.getJobManager().find(this);
    return activeDownloads != null && activeDownloads.length > 0;
  }
}
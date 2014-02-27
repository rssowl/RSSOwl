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

package org.rssowl.core.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.internal.Activator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class allows to add <code>Runnables</code> into a Queue to process them
 * in Jobs up to a certain amount of allowed parallel Jobs.
 *
 * @author bpasero
 */
public class JobQueue {

  /* Helper for the Progress Monitor */
  private static final double TOTAL_TASK_WORK_LOAD = 9900;
  private static final double TOTAL_PROGRESS_WORK_LOAD = 10000;

  /* Delay in ms for the Progress Job to update the Monitor */
  private static final int PROGRESS_UPDATE_DELAY = 300;

  /** This was copied from IProgressConstants to avoid UI dependancy */
  public static final QualifiedName NO_IMMEDIATE_ERROR_PROMPT_PROPERTY = new QualifiedName("org.eclipse.ui.workbench.progress", "delayErrorPrompt"); //$NON-NLS-1$ //$NON-NLS-2$

  private final Job fProgressJob;
  private final int fMaxConcurrentJobs;
  private final int fProgressDelay;
  private final String fName;
  private String fTaskPrefix;
  private final boolean fShowProgress;
  private boolean fIsUnknownProgress;
  private final ListenerList fListeners = new ListenerList();

  /* These fields are accessed from N Jobs concurrently */
  private volatile boolean fProgressJobScheduled;
  private volatile String fCurrentTask = ""; //$NON-NLS-1$
  private final AtomicInteger fTotalWork = new AtomicInteger(0); // Number of Tasks in Total
  private final AtomicInteger fWorkDone = new AtomicInteger(0); // Number of finished Tasks
  private final AtomicInteger fProgressShown = new AtomicInteger(0); // Number of Progress Shown
  private final AtomicInteger fProgressBuf = new AtomicInteger(0); // Buffer for the Progress Monitor
  private final AtomicInteger fScheduledJobs = new AtomicInteger(0); // Count number of running Jobs
  private final AtomicBoolean fIsSealed = new AtomicBoolean(false);
  private final BlockingQueue<ITask> fOpenTasksQueue;

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
   * @param showProgress If TRUE, show Progress of Jobs running from Queue.
   * @param progressDelay The time in milliseconds to wait before showing any
   * progress. This is useful in case the Tasks finish very quickly. Setting it
   * to 0 will show Progress instantly with no delay.
   */
  public JobQueue(String name, int maxConcurrentJobs, int maxQueueSize, boolean showProgress, int progressDelay) {
    this(name, name, maxConcurrentJobs, maxQueueSize, showProgress, progressDelay);
  }

  /**
   * Creates an instance of <code>JobQueue</code> that allows to add
   * <code>Runnables</code> into a Queue to process them in Jobs up to a certain
   * amount of allowed parallel Jobs.
   *
   * @param globalName A human-readable name that is displayed in the
   * Progress-View while the Queue is processed.
   * @param taskPrefix A human-readable prefix that is shown before the name of
   * a task that is currently processed.
   * @param maxConcurrentJobs The maximum number of concurrent running Tasks.
   * @param maxQueueSize The maximum number of tasks that this queue will accept
   * before blocking.
   * @param showProgress If TRUE, show Progress of Jobs running from Queue.
   * @param progressDelay The time in milliseconds to wait before showing any
   * progress. This is useful in case the Tasks finish very quickly. Setting it
   * to 0 will show Progress instantly with no delay.
   */
  public JobQueue(String globalName, String taskPrefix, int maxConcurrentJobs, int maxQueueSize, boolean showProgress, int progressDelay) {
    Assert.isNotNull(globalName);
    Assert.isNotNull(taskPrefix);
    Assert.isLegal(progressDelay >= 0, "JobQueue Progress delay is negative"); //$NON-NLS-1$
    fName = globalName;
    fTaskPrefix = taskPrefix;
    fMaxConcurrentJobs = maxConcurrentJobs;
    fShowProgress = showProgress;
    fProgressDelay = progressDelay;
    fOpenTasksQueue = new LinkedBlockingQueue<ITask>(maxQueueSize);

    /* Eagerly create the Progress-Job if we need one */
    if (showProgress)
      fProgressJob = createProgressJob();
    else
      fProgressJob = null;
  }

  /**
   * @param isUnknownProgress Sets the progress reporting of the progress Job
   * used for this {@link JobQueue} to {@link IProgressMonitor#UNKNOWN} if
   * <code>true</code>.
   */
  public void setUnknownProgress(boolean isUnknownProgress) {
    fIsUnknownProgress = isUnknownProgress;
  }

  /**
   * Cancels all Jobs that belong to this Queue. Optionally the caller may
   * decide to join the running Jobs that are not yet done. Note that this will
   * <em>block</em> the calling Thread until all running Tasks have finished so
   * this should only be considered for <em>short-running</em> Tasks.
   *
   * @param joinRunning If <code>TRUE</code>, join the running Jobs that are not
   * yet done.
   * @param seal if <code>true</code> this queue is sealed and no tasks will
   * ever be scheduled anymore.
   */
  public void cancel(boolean joinRunning, boolean seal) {
    synchronized (this) {

      /* Seal */
      if (seal)
        seal();

      /* Clear open tasks */
      fOpenTasksQueue.clear();

      /* Cancel scheduled Jobs */
      Job.getJobManager().cancel(this);

      /* Cancel Progress Job */
      if (fProgressJob != null)
        fProgressJob.cancel();
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
   * Seals this queue so that no task can be added anymore.
   */
  public void seal() {
    fIsSealed.set(true);
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
  public boolean isQueued(ITask task) {
    return fOpenTasksQueue.contains(task);
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
  public boolean schedule(ITask task) {
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
   * some tasks were not scheduled because the current thread was interrupted or
   * this queue is sealed.
   */
  public boolean schedule(List<ITask> tasks) {
    final int tasksSize = tasks.size();

    /* Ignore empty lists */
    if (tasksSize == 0)
      return true;

    /* Return if Queue is Sealed */
    if (fIsSealed.get())
      return false;

    /* Add into List of open tasks */
    for (ITask task : tasks) {
      try {
        fOpenTasksQueue.put(task);

        /* Adjust Total Work Counter */
        fTotalWork.incrementAndGet();
      } catch (InterruptedException e) {
        return false;
      }
    }

    /* Schedule Job if not yet done */
    if (!fProgressJobScheduled && fShowProgress) {
      fProgressJobScheduled = true;
      fProgressJob.schedule(fProgressDelay);
    }

    /* Optimisation: We are able to release the calling thread without locking. */
    if (fScheduledJobs.get() >= fMaxConcurrentJobs)
      return true;

    /* Start a new Job for each free Slot */
    while (!fOpenTasksQueue.isEmpty()) {


      // TODO: move next code to Job Pool code
      /* Never exceed max number of allowed concurrent Jobs */
      if (fScheduledJobs.incrementAndGet() > fMaxConcurrentJobs) {
        fScheduledJobs.decrementAndGet();
        break;
      }

      /* Create the Job */
      Job job = createJob();

      /* Listen to Job's Lifecycle */
      job.addJobChangeListener(new JobChangeAdapter() {

        /* Update Fields when a Job is Done */
        @Override
        public void done(IJobChangeEvent event) {

          /* Re-Schedule this Job if there is work left to do */
          if (!fOpenTasksQueue.isEmpty())
            event.getJob().schedule();
          else
            fScheduledJobs.decrementAndGet();
        }
      });

      /* Do not interrupt on any Error */
      job.setProperty(NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);

      /*
       * Workaround: Since we are using our own Job for displaying Progress, we
       * don't want these Jobs show up in the Progress View. There is currently
       * no bug-free solution of aggregating the Progress of N Jobs into a
       * single Monitor.
       */
      job.setSystem(true);

      /* Schedule it immediately */
      job.schedule();
    }
    return true;
  }

  /* Create a Job for a Task to handle */
  private Job createJob() {
    Job job = new Job("") { //$NON-NLS-1$
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        /* Poll the next Task */
        final ITask task = fOpenTasksQueue.poll();

        /* Queue is empty - so all work is done */
        if (task == null)
          return Status.OK_STATUS;

        /* Perform the Operation if not yet Cancelled */
        if (!monitor.isCanceled()) {
          SafeRunner.run(new LoggingSafeRunnable() {
            public void run() throws Exception {
              fCurrentTask = task.getName();
              IStatus status = task.run(monitor);

              /* Log anything that is an Error or Warning */
              if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.WARNING) {
                if (Activator.getDefault() != null)
                  Activator.getDefault().getLog().log(status);
              }
            }
          });

          /* Update Work Fields if not cancelled meanwhile */
          if (!monitor.isCanceled()) {
            fWorkDone.incrementAndGet();

            /* Calculate the Progress */
            int workDifference = fTotalWork.get() - fWorkDone.get();
            if (workDifference > 0 && fProgressJobScheduled) {
              int progress = (int) ((TOTAL_TASK_WORK_LOAD - fProgressShown.get()) / workDifference);
              fProgressShown.addAndGet(progress);
              fProgressBuf.addAndGet(progress);
            }
          }
        }

        /* Inform about cancelation if present */
        return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
      }

      @Override
      public boolean belongsTo(Object family) {
        return family == JobQueue.this;
      }
    };

    return job;
  }

  /**
   * Returns <code>TRUE</code> in case the JobQueue has finished all open Tasks.
   *
   * @return <code>TRUE</code> in case the JobQueue has finished all open Tasks,
   * <code>FALSE</code> otherwise.
   */
  public synchronized boolean isEmpty() {
    return internalIsEmpty();
  }

  private boolean internalIsEmpty() {
    return fTotalWork.get() - fWorkDone.get() == 0;
  }

  /* Creates the Job for displaying Progress while Tasks are processed */
  private Job createProgressJob() {
    return new Job(fName) {
      private int fLastWorkDone = -1;
      private String fLastTask;

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        boolean interrupted = false;

        /* Indicate Beginning if there is still work to do */
        if (!internalIsEmpty())
          monitor.beginTask(fName, fIsUnknownProgress ? IProgressMonitor.UNKNOWN : (int) TOTAL_PROGRESS_WORK_LOAD);

        /* Update Progress while not Cancelled and not Done */
        while (!monitor.isCanceled() && !internalIsEmpty()) {

          /* Update the Task Label if there was an Update */
          if (fCurrentTask != null && ((fLastWorkDone != fWorkDone.get()) || !StringUtils.isSet(fLastTask))) {
            fLastWorkDone = fWorkDone.get();
            fLastTask = fCurrentTask;
            monitor.setTaskName(formatTask());
          }

          /* Increment Monitor Progress */
          if (fProgressBuf.get() > 0) {
            monitor.worked(fProgressBuf.get());
            fProgressBuf.set(0);
          }

          try {
            Thread.sleep(PROGRESS_UPDATE_DELAY);
          } catch (InterruptedException e) {
            interrupted = true;
            break;
          }
        }

        /* Always call done() even if canceled */
        monitor.done();

        /* Task Processing has been canceled */
        if (monitor.isCanceled())
          Job.getJobManager().cancel(JobQueue.this);

        /* Be ready for the next Tasks */
        synchronized (JobQueue.this) {
          reset();
        }

        fLastWorkDone = -1;
        fLastTask = null;

        notifyWorkDone();

        /* Inform about cancellation if present */
        if (monitor.isCanceled() || interrupted)
          return Status.CANCEL_STATUS;

        return Status.OK_STATUS;
      }

      @Override
      public boolean belongsTo(Object family) {
        return family == JobQueue.this;
      }
    };
  }

  private void notifyWorkDone() {
    Object listeners[] = fListeners.getListeners();
    for (Object element : listeners) {
      final JobQueueListener listener = (JobQueueListener) element;
      SafeRunner.run(new LoggingSafeRunnable() {
        public void run() throws Exception {
          listener.workDone();
        }
      });
    }
  }

  /**
   * @param listener The Listener to add to the List of Listeners.
   */
  public void addJobQueueListener(JobQueueListener listener) {
    fListeners.add(listener);
  }

  /**
   * @param listener The Listener to remove from the List of Listeners.
   */
  public void removeJobQueueListener(JobQueueListener listener) {
    fListeners.remove(listener);
  }

  private String formatTask() {
    int workDone = fWorkDone.get();
    Object[] bindings = Arrays.asList(fTaskPrefix, String.valueOf((workDone != 0 ? workDone : 1)), String.valueOf(fTotalWork.get()), fCurrentTask.replaceAll("&", "&&")).toArray(); //$NON-NLS-1$//$NON-NLS-2$

    String str = NLS.bind(Messages.JobQueue_TASK_NAME, bindings);
    return str;
  }

  /* Reset fields and cancel all Jobs of this Family */
  private void reset() {
    fScheduledJobs.set(0);
    fProgressJobScheduled = false;
    fWorkDone.set(0);
    fTotalWork.set(0);
    fProgressBuf.set(0);
    fProgressShown.set(0);
    fCurrentTask = ""; //$NON-NLS-1$
    fOpenTasksQueue.clear();
  }
}
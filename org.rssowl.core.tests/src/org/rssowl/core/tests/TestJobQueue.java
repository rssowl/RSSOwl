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

package org.rssowl.core.tests;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.util.ITask;
import org.rssowl.core.util.LoggingSafeRunnable;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author bpasero
 */
public class TestJobQueue {
  private final int fMaxConcurrentJobs;

  private final AtomicInteger fScheduledJobs = new AtomicInteger(0);
  private final AtomicInteger fTotalWork = new AtomicInteger(0);
  private final AtomicLong fDuration = new AtomicLong(0);
  private final AtomicInteger fDone = new AtomicInteger(0);

  private final ConcurrentLinkedQueue<ITask> fOpenTasksQueue;

  /**
   * Creates an instance of <code>JobQueue</code> that allows to add
   * <code>Runnables</code> into a Queue to process them in Jobs up to a
   * certain amount of allowed parallel Jobs.
   * 
   * @param maxConcurrentJobs The maximum number of concurrent running Tasks.
   */
  public TestJobQueue(int maxConcurrentJobs) {
    fMaxConcurrentJobs = maxConcurrentJobs;
    fOpenTasksQueue = new ConcurrentLinkedQueue<ITask>();
  }

  /**
   * Adds the given List of Tasks into the Queue. Each Runnable is processed in
   * a <code>Job</code> once the number of parallel processed Tasks is below
   * <code>MAX_SCHEDULED_JOBS</code>.
   * 
   * @param tasks The Tasks to add into this Queue.
   */
  public void schedule(List<ITask> tasks) {
    final int tasksSize = tasks.size();
    final long start = System.currentTimeMillis();

    /* Ignore empty lists */
    if (tasksSize == 0)
      return;

    /* Add into List of open tasks */
    fOpenTasksQueue.addAll(tasks);

    /* Adjust Total Work Counter */
    fTotalWork.addAndGet(tasksSize);

    /* Optimisation: We are able to release the calling thread without locking. */
    if (fScheduledJobs.get() >= fMaxConcurrentJobs)
      return;

    /* Start a new Job for each free Slot */
    for (int i = 0; i < tasksSize && !fOpenTasksQueue.isEmpty(); ++i) {

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
          fDone.incrementAndGet();

          if (fDone.get() == fTotalWork.get())
            fDuration.set(System.currentTimeMillis() - start);

          /* Re-Schedule this Job if there is work left to do */
          if (!fOpenTasksQueue.isEmpty())
            event.getJob().schedule();
          else
            fScheduledJobs.decrementAndGet();
        }
      });

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
              IStatus status = task.run(monitor);

              /* Log anything that is an Error or Warning */
              if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.WARNING)
                Activator.getDefault().getLog().log(status);
            }
          });
        }

        /* Inform about cancelation if present */
        return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
      }

      @Override
      public boolean belongsTo(Object family) {
        return family == TestJobQueue.this;
      }
    };

    return job;
  }

  /**
   * @return The value of done Jobs.
   */
  public int getDone() {
    return fDone.get();
  }

  /**
   * @return The Duration of this Queue.
   */
  public long getDuration() {
    return fDuration.get();
  }
}
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The <code>BatchedBuffer</code> can be used to batch certain types of Objects
 * for later processing. The type of Object is identified by the generic
 * <code>T</code>.
 *
 * @author bpasero
 * @param <T> The type that will be buffered
 */
public class BatchedBuffer<T> {
  private final int fBatchInterval;
  private final List<T> fBuffer;
  private final Job fBufferProcessor;
  private final Receiver<T> fReceiver;
  private final AtomicBoolean fSealed = new AtomicBoolean(false);
  private final AtomicBoolean fRunning = new AtomicBoolean(false);

  /**
   * @param <T> The type that will be received from the
   * <code>BatchedBuffer</code>
   */
  public interface Receiver<T> {

    /**
     * Asks the implementor to process the given Collection of Objects. This
     * method will be called after <code>batchInterval</code> millies.
     *
     * @param objects A Collection of Objects that have been added during
     * <code>batchInterval</code>.
     * @param job the {@link Job} that is calling the receive method.
     * @param monitor a progress monitor to report progress and react on
     * cancellation.
     * @return the {@link IStatus} of the operation.
     */
    IStatus receive(Collection<T> objects, Job job, IProgressMonitor monitor);
  }

  /**
   * @param receiver An instance of <code>Receiver</code> to process the
   * elements of this buffer after <code>batchInterval</code> millies.
   * @param batchInterval The interval in millies before types get processed.
   */
  public BatchedBuffer(Receiver<T> receiver, int batchInterval) {
    this(receiver, batchInterval, null);
  }

  /**
   * @param receiver An instance of <code>Receiver</code> to process the
   * elements of this buffer after <code>batchInterval</code> millies.
   * @param batchInterval The interval in millies before types get processed.
   * @param taskName if provided, the batched buffer will show progress with the
   * given name.
   */
  public BatchedBuffer(Receiver<T> receiver, int batchInterval, String taskName) {
    fReceiver = receiver;
    fBatchInterval = batchInterval;
    fBuffer = new ArrayList<T>();
    fBufferProcessor = createBufferProcessor(taskName);
  }

  /**
   * Adds all Objects of the given Collection to this Buffer. The Buffer will be
   * cleared after <code>batchInterval</code> millies by calling the
   * <code>receive(Set)</code> method.
   *
   * @param objects The Collection of Objects to add into this Buffer.
   */
  public void addAll(Collection<T> objects) {
    if (objects.isEmpty() || fSealed.get())
      return;

    synchronized (fBuffer) {
      
      /* Check again for being sealed */
      if (fSealed.get())
        return;

      /* New Batch */
      if (fBuffer.isEmpty()) {
        fBuffer.addAll(objects);
        fBufferProcessor.schedule(fBatchInterval);
      }

      /* Existing Batch */
      else {
        for (T object : objects) {
          if (!fBuffer.contains(object))
            fBuffer.add(object);
        }
      }
    }
  }

  /**
   * Cancels the BatchedBuffer from running or processing tasks.
   *
   * @param joinRunning If <code>TRUE</code>, join the running Jobs that are not
   * yet done.
   */
  public void cancel(boolean joinRunning) {

    /* Seal the Buffer */
    fSealed.set(true);

    /* Wait until Buffer has completed work */
    if (joinRunning) {

      /* Do not wait until buffer is scheduled, schedule right now instead */
      if (isScheduled() && !isRunning()) {
        fBufferProcessor.cancel();
        fBufferProcessor.schedule();
      }

      /* Wait until Buffer is done processing */
      while (isScheduled()) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          break;
        }
      }
    }

    /* Cancel Batched Buffer and don't wait for finish */
    else
      fBufferProcessor.cancel();
  }

  /**
   * @return <code>true</code> if this buffer is running or scheduled and
   * <code>false</code> otherwise.
   */
  public boolean isScheduled() {
    return isRunning() || Job.getJobManager().find(this).length != 0;
  }

  /**
   * @return <code>true</code> if this buffer is running and <code>false</code>
   * otherwise.
   */
  public boolean isRunning() {
    return fRunning.get();
  }

  /* Creates a Job to process the contents of the Buffer */
  private Job createBufferProcessor(String taskName) {
    Job job = new Job(taskName != null ? taskName : "") { //$NON-NLS-1$
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        fRunning.set(true);
        try {
          if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

          IStatus status;
          synchronized (fBuffer) {
            status = fReceiver.receive(new ArrayList<T>(fBuffer), this, monitor);
            fBuffer.clear();
          }

          return status;
        } finally {
          fRunning.set(false);
        }
      }

      @Override
      public boolean belongsTo(Object family) {
        return family == BatchedBuffer.this;
      }
    };

    if (!StringUtils.isSet(taskName)) {
      job.setSystem(true);
      job.setUser(false);
    }

    return job;
  }
}
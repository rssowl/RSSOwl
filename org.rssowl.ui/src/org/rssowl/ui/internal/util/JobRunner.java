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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bpasero
 */
public class JobRunner {

  /* Copied from BusyIndicator */
  private static int nextBusyId = 1;
  private static final String BUSYID_NAME = "SWT BusyIndicator"; //$NON-NLS-1$

  /* Default Delay */
  private static final int DELAY = 800;

  /**
   * @param runnable
   * @param widget
   */
  public static void runInUIThread(final Widget widget, final Runnable runnable) {
    runInUIThread(0, widget, runnable);
  }

  /**
   * @param runnable
   * @param widget
   */
  public static void runDelayedInUIThread(final Widget widget, final Runnable runnable) {
    runInUIThread(DELAY, widget, runnable);
  }

  /**
   * @param delay
   * @param runnable
   * @param widget
   */
  public static void runInUIThread(int delay, final Widget widget, final Runnable runnable) {
    runInUIThread(delay, false, widget, runnable);
  }

  /**
   * @param delay
   * @param forceAsync
   * @param runnable
   * @param widget
   */
  public static void runInUIThread(int delay, boolean forceAsync, final Widget widget, final Runnable runnable) {
    Assert.isNotNull(runnable);

    /* Run directly if already in UI Thread */
    if (!forceAsync && delay == 0 && (widget == null || !widget.isDisposed()) && Display.getCurrent() != null)
      runnable.run();

    /* Otherwise use UI Job */
    else {
      UIJob uiJob = new UIJob("") { //$NON-NLS-1$
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
          if (widget == null || !widget.isDisposed())
            runnable.run();
          return Status.OK_STATUS;
        }
      };

      uiJob.setSystem(true);
      uiJob.setUser(false);
      uiJob.schedule(delay);
    }
  }

  /**
   * @param runnable
   * @param widget
   */
  public static void runSyncedInUIThread(final Widget widget, final Runnable runnable) {
    Assert.isNotNull(runnable);
    Assert.isNotNull(widget);

    if (!widget.isDisposed()) {
      widget.getDisplay().syncExec(new Runnable() {
        public void run() {
          if (!widget.isDisposed())
            runnable.run();
        }
      });
    }
  }

  /**
   * @param runnable
   */
  public static void runSyncedInUIThread(final Runnable runnable) {
    Assert.isNotNull(runnable);

    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        runnable.run();
      }
    });
  }

  /**
   * @param runnable
   */
  public static void runInBackgroundThread(final Runnable runnable) {
    runInBackgroundThread(0, runnable);
  }

  /**
   * @param runnable
   */
  public static void runDelayedInBackgroundThread(final Runnable runnable) {
    runInBackgroundThread(DELAY, runnable);
  }

  /**
   * @param delay
   * @param runnable
   */
  public static void runInBackgroundThread(int delay, final Runnable runnable) {
    Assert.isNotNull(runnable);
    Job job = new Job("") { //$NON-NLS-1$
      @Override
      public IStatus run(IProgressMonitor monitor) {
        runnable.run();
        return Status.OK_STATUS;
      }
    };

    job.setSystem(true);
    job.setUser(false);
    job.schedule(delay);
  }

  /**
   * @param job
   */
  public static void runUIUpdater(UIBackgroundJob job) {
    runUIUpdater(job, false);
  }

  /**
   * @param job
   * @param showProgress if <code>true</code> will show progress from the
   * background operation to the user.
   */
  public static void runUIUpdater(UIBackgroundJob job, boolean showProgress) {
    Assert.isNotNull(job);
    if (!showProgress) {
      job.setSystem(true);
      job.setUser(false);
    }
    job.schedule();
  }

  /**
   * @param runnable
   */
  public static void runInBackgroundWithBusyIndicator(final Runnable runnable) {
    final Display display = Display.getCurrent();
    final Integer busyId = Integer.valueOf(nextBusyId++);

    /* Guard against Illegal-Thread-Access */
    if (display == null)
      throw new IllegalStateException("Method was not called from the UI-Thread!"); //$NON-NLS-1$

    /* Set the Cursor */
    Cursor cursor = display.getSystemCursor(SWT.CURSOR_APPSTARTING);
    Shell[] shells = display.getShells();
    for (Shell shell : shells) {
      Integer id = (Integer) shell.getData(BUSYID_NAME);
      if (id == null) {
        shell.setCursor(cursor);
        shell.setData(BUSYID_NAME, busyId);
      }
    }

    /* Run the Runnable and update cursor afterwards */
    runUIUpdater(new UIBackgroundJob(null) {

      @Override
      protected void runInBackground(IProgressMonitor monitor) {
        runnable.run();
      }

      @Override
      protected void runInUI(IProgressMonitor monitor) {
        if (!display.isDisposed()) {
          Shell[] shells = display.getShells();
          for (Shell shell : shells) {
            Integer id = (Integer) shell.getData(BUSYID_NAME);
            if (busyId.equals(id)) {
              shell.setCursor(null);
              shell.setData(BUSYID_NAME, null);
            }
          }
        }
      }
    });
  }

  /**
   * @param delay
   * @param flag
   */
  public static void runDelayedFlagInversion(int delay, final AtomicBoolean flag) {
    flag.set(!flag.get());
    runInBackgroundThread(delay, new Runnable() {
      public void run() {
        flag.set(!flag.get());
      }
    });
  }
}
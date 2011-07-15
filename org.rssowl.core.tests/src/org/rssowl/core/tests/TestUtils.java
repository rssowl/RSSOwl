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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.rssowl.core.IApplicationService;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.ApplicationServiceImpl;
import org.rssowl.core.internal.persist.MergeResult;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.ITask;
import org.rssowl.core.util.LongOperationMonitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Some helper Methods used through the various Tests.
 *
 * @author bpasero
 */
public class TestUtils {
  private static long start;
  private static int count = 0;

  /**
   * Calls ApplicationLayerImpl#saveFeed. TODO Must come up with general
   * approach for testing private methods.
   *
   * @param mergeResult
   * @throws IllegalArgumentException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void saveFeed(MergeResult mergeResult) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Method m = ApplicationServiceImpl.class.getDeclaredMethod("saveFeed", MergeResult.class);
    m.setAccessible(true);
    IApplicationService appService = Owl.getApplicationService();
    m.invoke(appService, mergeResult);
  }

  /**
   * Bench
   *
   * @return Returns the Time.
   */
  public static long bench() {
    if (count == 0) {
      count++;
      start = System.currentTimeMillis();
    } else {
      count--;
      return System.currentTimeMillis() - start;
    }

    return 0;
  }

  /**
   * Helper for fail on PersistenceLayerExceptions
   *
   * @param e The exception that occured.
   */
  public static void fail(PersistenceException e) {
    Assert.fail(e.getMessage());
    Activator.getDefault().logError(e.getMessage(), e);
  }

  /**
   * Helper for concurrent Performance Tests.
   *
   * @param tasks The Tasks to Run.
   * @param jobs The max. Number of Jobs allowed to run Concurrently.
   * @return Returns the Time that passed for processing the Tasks.
   */
  public static long executeAndWait(List<ITask> tasks, int jobs) {
    TestJobQueue queue = new TestJobQueue(jobs);
    int taskCount = tasks.size();
    queue.schedule(new ArrayList<ITask>(tasks));

    while (queue.getDone() < taskCount) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted while executing."); //$NON-NLS-1$
      }
    }

    return queue.getDuration();
  }

  public static class NullProgressLongOperationMonitor extends LongOperationMonitor {
    public NullProgressLongOperationMonitor() {
      super(new NullProgressMonitor());
    }

    @Override
    public void beginLongOperation(boolean isCancelable) {}
  }
}
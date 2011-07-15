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

/**
 * The interface <code>ITask</code> offers a run()-Method that returns the
 * result of the operation as an instance of <code>IStatus</code>. If the
 * Task is running from the <code>JobQueue</code> the given Priority will be
 * used as priority for the Job that runs the Task.
 * 
 * @author bpasero
 */
public interface ITask {

  /** Allowed Priority Values */
  public static enum Priority {

    /**
     * Task priority constant for interactive tasks. Interactive tasks generally
     * have priority over all other tasks. Interactive tasks should be either
     * fast running or very low on CPU usage to avoid blocking other interactive
     * tasks from running.
     */
    INTERACTIVE,

    /**
     * Task priority constant for short background Tasks. Short background Tasks
     * are Tasks that typically complete within a second, but may take longer in
     * some cases. Short Tasks are given priority over all other Tasks except
     * interactive Tasks.
     */
    SHORT,

    /** Job priority constant for long-running background jobs (default). */
    DEFAULT
  }

  /**
   * The general contract of the method <code>run</code> is that it may take
   * any action whatsoever.
   * 
   * @param monitor The provided monitor can be used to report progress and
   * respond to cancellation. If the progress monitor has been canceled, the
   * task should finish its execution at the earliest convenience and return a
   * result status of severity IStatus.CANCEL.
   * @return Returns the result of the operation as an instance of
   * <code>IStatus</code>.
   */
  IStatus run(IProgressMonitor monitor);

  /**
   * Get a human-readable Name of this Task in order to display it to the User
   * from the User Interface while its running.
   * 
   * @return Returns a humanr-readable Name of this Task, or <code>NULL</code>
   * if none.
   */
  String getName();

  /**
   * The Priority is used for the Job that is executing this Task.
   * 
   * @return Returns the Priority of this Task as defined by the
   * <code>ITask.Priority</code> Enumeration.
   */
  ITask.Priority getPriority();
}
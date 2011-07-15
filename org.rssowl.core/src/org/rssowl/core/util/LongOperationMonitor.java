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

/**
 * The <code>LongOperationMonitor</code> is wrapping around and existing
 * <code>IProgressMonitor</code> and adds a new method to indicate that a long
 * operation is about to start. Implementors can then decide to show a progress
 * dialog for instance.
 *
 * @author bpasero
 */
public abstract class LongOperationMonitor implements IProgressMonitor {
  private final IProgressMonitor fMonitor;
  private boolean fIsLongOperationRunning;

  /**
   * @param monitor The progress monitor to wrap around.
   */
  public LongOperationMonitor(IProgressMonitor monitor) {
    fMonitor = monitor;
  }

  /**
   * Indicates that a long operation is about to start. Implementors can then
   * decide to show a progress dialog for instance.
   *
   * @param isCancelable set to <code>true</code> in case the operation can be
   * cancelled and <code>false</code> otherwise.
   */
  public void beginLongOperation(boolean isCancelable) {
    fIsLongOperationRunning = true;
  }

  /**
   * @return <code>true</code> if
   * {@link LongOperationMonitor#beginLongOperation(boolean)} has been called
   * and <code>false</code> otherwise.
   */
  public boolean isLongOperationRunning() {
    return fIsLongOperationRunning;
  }

  /*
   * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
   * int)
   */
  public void beginTask(String name, int totalWork) {
    fMonitor.beginTask(name, totalWork);
  }

  /*
   * @see org.eclipse.core.runtime.IProgressMonitor#done()
   */
  public void done() {
    fMonitor.done();
  }

  /*
   * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
   */
  public void internalWorked(double work) {
    fMonitor.internalWorked(work);
  }

  /*
   * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
   */
  public boolean isCanceled() {
    return fMonitor.isCanceled();
  }

  /*
   * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
   */
  public void setCanceled(boolean value) {
    fMonitor.setCanceled(value);
  }

  /*
   * @see
   * org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
   */
  public void setTaskName(String name) {
    fMonitor.setTaskName(name);
  }

  /*
   * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
   */
  public void subTask(String name) {
    fMonitor.subTask(name);
  }

  /*
   * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
   */
  public void worked(int work) {
    fMonitor.worked(work);
  }
}
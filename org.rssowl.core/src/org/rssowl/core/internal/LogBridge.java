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

package org.rssowl.core.internal;

import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.IStatus;

/**
 * Implementation of <code>Log</code> that writes error and fatal messages to
 * the Eclipse Log Mechanism.
 *
 * @author bpasero
 */
public class LogBridge implements Log {

  /** Keep for reflection */
  public LogBridge() {}

  /**
   * Keep for reflection
   *
   * @param str the class using this log
   */
  public LogBridge(String str) {}

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#error(java.lang.Object,
   * java.lang.Throwable)
   */
  public void error(Object message, Throwable t) {
    logError(message, t);
  }

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#error(java.lang.Object)
   */
  public void error(Object message) {
    logError(message, null);
  }

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#fatal(java.lang.Object,
   * java.lang.Throwable)
   */
  public void fatal(Object message, Throwable t) {
    logError(message, t);
  }

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#fatal(java.lang.Object)
   */
  public void fatal(Object message) {
    logError(message, null);
  }

  private void logError(Object message, Throwable t) {
    if (message instanceof String || t instanceof Exception) {
      String msg = null;
      if (message instanceof String)
        msg = message.toString();

      Exception e = null;
      if (t instanceof Exception)
        e = (Exception) t;

      if (msg == null && e != null && e.getMessage() != null)
        msg = e.getMessage();

      Activator activator = Activator.getDefault();
      if (activator != null) {
        IStatus status = activator.createErrorStatus(msg, e);
        activator.getLog().log(status);
      }
    }
  }

  /*
   * @see org.apache.commons.logging.Log#debug(java.lang.Object)
   */
  public void debug(Object message) {}

  /*
   * @see org.apache.commons.logging.Log#debug(java.lang.Object,
   * java.lang.Throwable)
   */
  public void debug(Object message, Throwable t) {}

  /*
   * @see org.apache.commons.logging.Log#info(java.lang.Object)
   */
  public void info(Object message) {}

  /*
   * @see org.apache.commons.logging.Log#info(java.lang.Object,
   * java.lang.Throwable)
   */
  public void info(Object message, Throwable t) {}

  /*
   * @see org.apache.commons.logging.Log#warn(java.lang.Object)
   */
  public void warn(Object message) {}

  /*
   * @see org.apache.commons.logging.Log#warn(java.lang.Object,
   * java.lang.Throwable)
   */
  public void warn(Object message, Throwable t) {}

  /*
   * @see org.apache.commons.logging.Log#trace(java.lang.Object)
   */
  public void trace(Object message) {}

  /*
   * @see org.apache.commons.logging.Log#trace(java.lang.Object,
   * java.lang.Throwable)
   */
  public void trace(Object message, Throwable t) {}

  /*
   * @see org.apache.commons.logging.Log#isDebugEnabled()
   */
  public boolean isDebugEnabled() {
    return false;
  }

  /*
   * @see org.apache.commons.logging.Log#isErrorEnabled()
   */
  public boolean isErrorEnabled() {
    return true;
  }

  /*
   * @see org.apache.commons.logging.Log#isFatalEnabled()
   */
  public boolean isFatalEnabled() {
    return true;
  }

  /*
   * @see org.apache.commons.logging.Log#isInfoEnabled()
   */
  public boolean isInfoEnabled() {
    return false;
  }

  /*
   * @see org.apache.commons.logging.Log#isTraceEnabled()
   */
  public boolean isTraceEnabled() {
    return false;
  }

  /*
   * @see org.apache.commons.logging.Log#isWarnEnabled()
   */
  public boolean isWarnEnabled() {
    return false;
  }
}
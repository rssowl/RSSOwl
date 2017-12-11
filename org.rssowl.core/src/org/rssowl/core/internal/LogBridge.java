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
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;

/**
 * Implementation of <code>Log</code> that writes error and fatal messages to
 * the Eclipse Log Mechanism.
 *
 * @author bpasero
 */
public class LogBridge implements Log {

  /* System Properties to Control Logging Levels */
  private static final String DEBUG_PROPERTY = "rssowl.http.debug"; //$NON-NLS-1$
  private static final String TRACE_PROPERTY = "rssowl.http.trace"; //$NON-NLS-1$
  private static final String INFO_PROPERTY = "rssowl.http.info"; //$NON-NLS-1$
  private static final String WARN_PROPERTY = "rssowl.http.warn"; //$NON-NLS-1$

  private final boolean fDebug;
  private final boolean fTrace;
  private final boolean fInfo;
  private final boolean fWarn;
  private final String fNl;

  private enum Level {
    INFO, WARNING, ERROR
  }

  /** Keep for reflection */
  public LogBridge() {
    this(null);
  }

  /**
   * Keep for reflection
   *
   * @param str the class using this log
   */
  public LogBridge(String str) {
    fDebug = System.getProperty(DEBUG_PROPERTY) != null;
    fTrace = System.getProperty(TRACE_PROPERTY) != null;
    fInfo = System.getProperty(INFO_PROPERTY) != null;
    fWarn = System.getProperty(WARN_PROPERTY) != null;

    String nl = System.getProperty("line.separator"); //$NON-NLS-1$
    if (!StringUtils.isSet(nl))
      nl = "\n"; //$NON-NLS-1$

    fNl = nl;
  }

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#error(java.lang.Object,
   * java.lang.Throwable)
   */
  @Override
  public void error(Object message, Throwable t) {
    logError(message, t);
  }

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#error(java.lang.Object)
   */
  @Override
  public void error(Object message) {
    logError(message, null);
  }

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#fatal(java.lang.Object,
   * java.lang.Throwable)
   */
  @Override
  public void fatal(Object message, Throwable t) {
    logError(message, t);
  }

  /*
   * @see org.apache.commons.logging.impl.NoOpLog#fatal(java.lang.Object)
   */
  @Override
  public void fatal(Object message) {
    logError(message, null);
  }

  private void logInfo(Object message, Throwable t) {
    logStatus(message, t, Level.INFO);
  }

  private void logWarning(Object message, Throwable t) {
    logStatus(message, t, Level.WARNING);
  }

  private void logError(Object message, Throwable t) {
    logStatus(message, t, Level.ERROR);
  }

  private void logStatus(Object message, Throwable t, Level level) {
    if (message instanceof String || t instanceof Exception) {
      String msg = null;
      if (message instanceof String)
        msg = message.toString();

      Exception e = null;
      if (t instanceof Exception)
        e = (Exception) t;

      if (msg == null && e != null && e.getMessage() != null)
        msg = e.getMessage();

      /* Write Info and Warning to Log Directly */
      if (level == Level.INFO || level == Level.WARNING) {
        if (StringUtils.isSet(msg)) {
          CoreUtils.appendLogMessage(msg);
          CoreUtils.appendLogMessage(fNl);
        }
      }

      /* Log Error Status for Errors */
      else {
        Activator activator = Activator.getDefault();
        if (activator != null) {
          IStatus status = activator.createErrorStatus(msg, e);
          activator.getLog().log(status);
        }
      }
    }
  }

  /*
   * @see org.apache.commons.logging.Log#debug(java.lang.Object)
   */
  @Override
  public void debug(Object message) {
    if (isDebugEnabled())
      logInfo(message, null);
  }

  /*
   * @see org.apache.commons.logging.Log#debug(java.lang.Object,
   * java.lang.Throwable)
   */
  @Override
  public void debug(Object message, Throwable t) {
    if (isDebugEnabled())
      logInfo(message, t);
  }

  /*
   * @see org.apache.commons.logging.Log#info(java.lang.Object)
   */
  @Override
  public void info(Object message) {
    if (isInfoEnabled())
      logInfo(message, null);
  }

  /*
   * @see org.apache.commons.logging.Log#info(java.lang.Object,
   * java.lang.Throwable)
   */
  @Override
  public void info(Object message, Throwable t) {
    if (isInfoEnabled())
      logInfo(message, t);
  }

  /*
   * @see org.apache.commons.logging.Log#warn(java.lang.Object)
   */
  @Override
  public void warn(Object message) {
    if (isWarnEnabled())
      logWarning(message, null);
  }

  /*
   * @see org.apache.commons.logging.Log#warn(java.lang.Object,
   * java.lang.Throwable)
   */
  @Override
  public void warn(Object message, Throwable t) {
    if (isWarnEnabled())
      logWarning(message, t);
  }

  /*
   * @see org.apache.commons.logging.Log#trace(java.lang.Object)
   */
  @Override
  public void trace(Object message) {
    if (isTraceEnabled())
      logInfo(message, null);
  }

  /*
   * @see org.apache.commons.logging.Log#trace(java.lang.Object,
   * java.lang.Throwable)
   */
  @Override
  public void trace(Object message, Throwable t) {
    if (isTraceEnabled())
      logInfo(message, t);
  }

  /*
   * @see org.apache.commons.logging.Log#isDebugEnabled()
   */
  @Override
  public boolean isDebugEnabled() {
    return fDebug;
  }

  /*
   * @see org.apache.commons.logging.Log#isErrorEnabled()
   */
  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  /*
   * @see org.apache.commons.logging.Log#isFatalEnabled()
   */
  @Override
  public boolean isFatalEnabled() {
    return true;
  }

  /*
   * @see org.apache.commons.logging.Log#isInfoEnabled()
   */
  @Override
  public boolean isInfoEnabled() {
    return fInfo;
  }

  /*
   * @see org.apache.commons.logging.Log#isTraceEnabled()
   */
  @Override
  public boolean isTraceEnabled() {
    return fTrace;
  }

  /*
   * @see org.apache.commons.logging.Log#isWarnEnabled()
   */
  @Override
  public boolean isWarnEnabled() {
    return fWarn;
  }
}
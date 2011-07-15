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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.ui.internal.Controller;

/**
 * @author bpasero
 */
public class Activator extends Plugin {
  private static Activator fPlugin;

  /**
   * The constructor.
   */
  public Activator() {
    fPlugin = this;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    InternalOwl.TESTING = true;
    Controller.getDefault();
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    fPlugin = null;
  }

  /**
   * Returns the shared instance.
   *
   * @return the shared instance
   */
  public static Activator getDefault() {
    return fPlugin;
  }

  /**
   * Log an Error Message.
   *
   * @param msg The message to log as Error.
   * @param e The occuring Exception to log.
   */
  public void logError(String msg, Throwable e) {
    if (msg == null)
      msg = ""; //$NON-NLS-1$
    getLog().log(new Status(IStatus.ERROR, getBundle().getSymbolicName(), IStatus.ERROR, msg, e));
  }
}
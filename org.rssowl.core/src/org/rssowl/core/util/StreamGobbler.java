/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2003-2009 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **                                                                          **
 **   This program and the accompanying materials are made available under   **
 **   the terms of the Eclipse Public License 1.0 which accompanies this     **
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
 **     RSSOwl - initial API and implementation (bpasero@rssowl.org)         **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.core.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.rssowl.core.internal.Activator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The StreamGobbler class is used to handle input and error streams of the
 * created process. <br />
 * <br />
 * It was first printed by JavaWorld (www.javaworld.com) in "When Runtime.exec()
 * won't", December 2000
 * (http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html)
 *
 * @author Taken from Java Pitfalls (www.javaworld.com)
 */
public class StreamGobbler extends Job {
  private final InputStream fIs;

  /**
   * Instantiate a new StreamGobbler
   *
   * @param is The inputstream of the process
   */
  public StreamGobbler(InputStream is) {
    super(""); //$NON-NLS-1$
    setSystem(true);
    fIs = is;
  }

  /*
   * @see
   * org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor
   * )
   */
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    BufferedReader br = null;
    try {
      StringBuilder msg = new StringBuilder(""); //$NON-NLS-1$
      String line;
      br = new BufferedReader(new InputStreamReader(fIs));

      /* Read output */
      while ((line = br.readLine()) != null)
        msg.append(line);

      /* If there is output, log it */
      if (msg.toString().trim().length() > 0)
        Activator.getDefault().logInfo(msg.toString());
    }

    /* Log any error */
    catch (IOException e) {
      Activator.safeLogError(e.getMessage(), e);
    }

    /* Close Stream */
    finally {
      try {
        if (br != null)
          br.close();
      } catch (IOException e) {
        Activator.safeLogError(e.getMessage(), e);
      }
    }

    return Status.OK_STATUS;
  }
}
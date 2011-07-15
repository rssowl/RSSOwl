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

package org.rssowl.ui.internal.dialogs.cleanup;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The abstract super type of all tasks the clean-up wizard may perform.
 *
 * @author bpasero
 */
public abstract class CleanUpTask {
  private final CleanUpGroup fGroup;

  /**
   * Creates a new CleanUpTask
   *
   * @param group the parent of this Task.
   */
  protected CleanUpTask(CleanUpGroup group) {
    fGroup = group;
  }

  CleanUpGroup getGroup() {
    return fGroup;
  }

  /* Returns the Label for the Task */
  abstract String getLabel();

  /* Returns the Image for the Task */
  abstract ImageDescriptor getImage();
}
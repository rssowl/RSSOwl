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

package org.rssowl.ui.internal.dialogs.cleanup.tasks;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.rssowl.ui.internal.dialogs.cleanup.PostProcessingWork;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

/**
 * The abstract super type of all tasks the clean-up wizard may perform.
 *
 * @author bpasero
 */
public abstract class AbstractCleanUpTask {

  private final SummaryModelTaskGroup fGroup;

  protected AbstractCleanUpTask(SummaryModelTaskGroup group) {
    Assert.isNotNull(group);
    fGroup = group;
  }

  public SummaryModelTaskGroup getGroup() {
    Assert.isNotNull(fGroup);
    return fGroup;
  }

  /**
   * Perform Summary Task on database Updates preferences if needed Change some
   * flag values to do some work after all tasks
   *
   * @param work stores actions to do after processing all tasks if necessary
   */
  public abstract void perform(PostProcessingWork work);

  /* Returns the Label for the Task */
  public abstract String getLabel();

  /* Returns the Image for the Task */
  public abstract ImageDescriptor getImage();

  /**
   * Return number of work units execution of this task will take
   *
   * @return number of work units
   */
  public int getWorkUnits() {
    return 1;
  }

  /**
   * Return actual number of work units execution of this task taken
   *
   * @return number of work units
   */
  public int getActualWorkUnits() {
    return 1;
  }
}

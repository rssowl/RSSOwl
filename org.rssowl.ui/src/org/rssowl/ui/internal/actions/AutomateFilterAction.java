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

package org.rssowl.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.CreateFilterAction.PresetAction;

/**
 * @author bpasero
 */
public class AutomateFilterAction extends Action {
  private final PresetAction fAction;
  private final ISelection fSelection;

  /**
   * @param action
   * @param selection
   */
  public AutomateFilterAction(PresetAction action, ISelection selection) {
    fAction = action;
    fSelection = selection;
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    switch (fAction) {
      case DOWNLOAD:
        return Messages.AutomateFilterAction_AUTOMATE_DOWNLOAD;

      case LABEL:
        return Messages.AutomateFilterAction_AUTOMATE_LABEL;

      case COPY:
        return Messages.AutomateFilterAction_AUTOMATE_COPY;

      case MOVE:
        return Messages.AutomateFilterAction_AUTOMATE_MOVE;
    }

    return super.getText();
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    CreateFilterAction action = new CreateFilterAction();
    action.setPresetAction(fAction);
    action.selectionChanged(null, fSelection);
    action.run(null);
  }

  /*
   * @see org.eclipse.jface.action.Action#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor() {
    return OwlUI.FILTER;
  }
}
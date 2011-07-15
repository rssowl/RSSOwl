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

package org.rssowl.ui.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.ToggleReadStateAction;

/**
 * This {@link IHandler} is required to support key-bindings for programmatic
 * added actions like the {@link ToggleReadStateAction}.
 *
 * @author bpasero
 */
public class ToggleReadStateHandler extends AbstractHandler {

  /*
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent arg0) {
    IStructuredSelection selection = OwlUI.getActiveFeedViewSelection();

    if (selection != null && !selection.isEmpty())
      new ToggleReadStateAction(selection).run();

    return null; //As per JavaDoc
  }
}
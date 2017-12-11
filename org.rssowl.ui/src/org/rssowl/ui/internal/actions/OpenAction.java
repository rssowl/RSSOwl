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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.rssowl.ui.internal.OwlUI;

/**
 * @author bpasero
 */
public class OpenAction extends Action {
  private ISelectionProvider fSelectionProvider;
  private IWorkbenchPage fPage;

  /**
   * @param page
   * @param selectionProvider
   */
  public OpenAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
    fPage = page;
    fSelectionProvider = selectionProvider;

    setText(Messages.OpenAction_OPEN);
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    final IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
    BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() {
      @Override
      public void run() {

        /* Open in Feedview */
        OwlUI.openInFeedView(fPage, selection);
      }
    });
  }
}
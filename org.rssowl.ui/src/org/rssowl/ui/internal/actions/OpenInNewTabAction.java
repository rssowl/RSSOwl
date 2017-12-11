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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.rssowl.core.persist.IFolder;
import org.rssowl.ui.internal.OwlUI;

import java.util.EnumSet;

/**
 * @author bpasero
 */
public class OpenInNewTabAction extends Action {
  private IStructuredSelection fSelection;
  private IWorkbenchPage fPage;
  private IFolder fFolder;

  /**
   * @param page
   * @param selectionProvider
   */
  public OpenInNewTabAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
    this(page, (IStructuredSelection) selectionProvider.getSelection());
  }

  /**
   * @param page
   * @param selection
   */
  public OpenInNewTabAction(IWorkbenchPage page, IStructuredSelection selection) {
    fPage = page;
    fSelection = selection;

    if (selection.size() == 1)
      setText(Messages.OpenInNewTabAction_OPEN_IN_NEW_TAB);
    else
      setText(Messages.OpenInNewTabAction_OPEN_IN_NEW_TABS);
  }

  /**
   * @param page
   * @param folder
   */
  public OpenInNewTabAction(IWorkbenchPage page, IFolder folder) {
    fPage = page;
    fFolder = folder;
    setText(Messages.OpenInNewTabAction_OPEN_ALL_IN_TABS);
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {

    /* Find Elements to Open */
    final IStructuredSelection selection;
    if (fSelection != null)
      selection = fSelection;
    else
      selection = new StructuredSelection(fFolder.getMarks());

    /* Open in Feedview */
    BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() {
      @Override
      public void run() {
        OwlUI.openInFeedView(fPage, selection, EnumSet.of(OwlUI.FeedViewOpenMode.IGNORE_REUSE));
      }
    });
  }
}
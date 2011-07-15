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
import org.eclipse.ui.IWorkbenchWindow;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.SearchNewsDialog;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.List;

/**
 * @author bpasero
 */
public class SearchInTypeAction extends Action {
  private ISelectionProvider fSelectionProvider;
  private IWorkbenchWindow fWindow;

  /**
   * @param window
   * @param selectionProvider
   */
  public SearchInTypeAction(IWorkbenchWindow window, ISelectionProvider selectionProvider) {
    fWindow = window;
    fSelectionProvider = selectionProvider;

    setText(Messages.SearchInTypeAction_SEARCH_NEWS);
    setImageDescriptor(OwlUI.getImageDescriptor("icons/obj16/searchmark.gif")); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
    if (selection.isEmpty())
      selection = new StructuredSelection(OwlUI.getSelectedBookMarkSet());

    List<IFolderChild> entities = ModelUtils.getFoldersBookMarksBins(selection);

    /* Normalize */
    CoreUtils.normalize(entities);

    SearchNewsDialog dialog = new SearchNewsDialog(fWindow.getShell(), entities);
    dialog.open();
  }
}
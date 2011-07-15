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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.dao.DynamicDAO;

import java.util.List;

/**
 * Sorts a selected {@link IFolder} ascending by the alphabet.
 *
 * @author bpasero
 */
public class SortByNameAction implements IActionDelegate {
  private IStructuredSelection fSelection;

  /** Leave for Reflection */
  public SortByNameAction() {}

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    List<?> list = fSelection.toList();
    for (Object obj : list) {
      if (obj instanceof IFolder) {
        IFolder folder = (IFolder) obj;
        folder.sort();
        DynamicDAO.save(folder);
      }
    }
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection)
      fSelection = (IStructuredSelection) selection;
  }
}
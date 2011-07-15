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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.JobRunner;

import java.util.Collection;

/**
 * Action to reload all BookMarks.
 *
 * @author bpasero
 */
public class ReloadAllAction extends Action implements IWorkbenchWindowActionDelegate {

  /** Action ID */
  public static final String ID = "org.rssowl.ui.actions.ReloadAll"; //$NON-NLS-1$

  private Shell fShell;

  /**
   * Action to reload all BookMarks.
   */
  public ReloadAllAction() {
    this(true);
  }

  /**
   * Action to reload all BookMarks.
   *
   * @param registerIds if <code>true</code> registers with the command and
   * shows a keybinding.
   */
  public ReloadAllAction(boolean registerIds) {
    super(Messages.ReloadAllAction_UPDATE_ALL, OwlUI.getImageDescriptor("icons/elcl16/reload_all.gif")); //$NON-NLS-1$
    if (registerIds) {
      setId(ID);
      setActionDefinitionId(ID);
    }
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    fShell = window.getShell();
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    run();
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    JobRunner.runInBackgroundThread(new Runnable() {
      public void run() {
        Collection<IFolder> rootFolders = CoreUtils.loadRootFolders();
        new ReloadTypesAction(new StructuredSelection(rootFolders.toArray()), fShell).run();
      }
    });
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {}
}
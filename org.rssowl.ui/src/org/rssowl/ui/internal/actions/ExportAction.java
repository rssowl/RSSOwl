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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.exporter.ExportWizard;

/**
 * Opens a Wizard to export {@link IFolderChild} with the option to also
 * consider Filters, Labels and Preferences.
 *
 * @author bpasero
 */
public class ExportAction implements IWorkbenchWindowActionDelegate {

  /** Action ID */
  public static final String ID = "org.rssowl.ui.actions.ExportFeeds"; //$NON-NLS-1$

  /* Section for Dialogs Settings */
  private static final String SETTINGS_SECTION = "org.rssowl.ui.internal.dialogs.exporter.ExportWizard"; //$NON-NLS-1$

  private IWorkbenchWindow fWindow;

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    fWindow = window;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    openWizard(fWindow.getShell());
  }

  /**
   * @param shell the {@link Shell} acting as parent of the wizard.
   */
  public void openWizard(Shell shell) {
    ExportWizard exportWizard = new ExportWizard();
    OwlUI.openWizard(shell, exportWizard, true, false, SETTINGS_SECTION);
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {}
}
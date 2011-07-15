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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author bpasero
 */
public class ManageConfigurationAction extends Action implements IWorkbenchWindowActionDelegate {
  private static final String CONFIGURATION_VIEW_SHOW_NESTED_FEATURES = "ConfigurationView.showNestedFeatures"; //$NON-NLS-1$
  private static final String CONFIGURATION_VIEW_SHOW_SITES = "ConfigurationView.showSites"; //$NON-NLS-1$
  private static final String CONFIGURATION_VIEW_SHOW_DISABLED = "ConfigurationView.showUnconf"; //$NON-NLS-1$

  private Shell fShell;

  /** Keep default constructor for reflection. */
  public ManageConfigurationAction() {}

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @SuppressWarnings("restriction")
  @Override
  public void run() {

    /* Properly Set Preferences to Control Configuration UI */
    Preferences pluginPreferences = org.eclipse.update.internal.ui.UpdateUI.getDefault().getPluginPreferences();
    pluginPreferences.setDefault(CONFIGURATION_VIEW_SHOW_SITES, true);
    pluginPreferences.setValue(CONFIGURATION_VIEW_SHOW_SITES, false);
    pluginPreferences.setDefault(CONFIGURATION_VIEW_SHOW_NESTED_FEATURES, true);
    pluginPreferences.setValue(CONFIGURATION_VIEW_SHOW_NESTED_FEATURES, false);
    pluginPreferences.setDefault(CONFIGURATION_VIEW_SHOW_DISABLED, false);
    pluginPreferences.setValue(CONFIGURATION_VIEW_SHOW_DISABLED, true);
    org.eclipse.update.internal.ui.UpdateUI.getDefault().savePluginPreferences();

    /* Open Config Dialog */
    ApplicationWindow appWindow = new org.eclipse.update.internal.ui.ConfigurationManagerWindow(fShell) {
      @Override
      public MenuManager getMenuBarManager() {
        return new MenuManager(); //Disables the Menu Bar
      }
    };
    appWindow.create();
    appWindow.getShell().setText(Messages.ManageConfigurationAction_MANAGE_ADDONS);
    appWindow.getShell().setImages(fShell.getImages());
    appWindow.open();
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
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {}
}
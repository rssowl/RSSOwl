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
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.OwlUI;

/**
 * @author bpasero
 */
public class NewTypeDropdownAction implements IWorkbenchWindowPulldownDelegate, IMenuCreator {
  private Shell fShell;
  private Menu fMenu;
  private IFolder fParent;
  private IMark fPosition;
  private LocalResourceManager fResources = new LocalResourceManager(JFaceResources.getResources());
  private IBindingService fBindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);

  /*
   * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
   */
  @Override
  public Menu getMenu(Control parent) {
    if (fMenu != null)
      OwlUI.safeDispose(fMenu);

    fMenu = new Menu(parent);

    MenuItem newBookMark = new MenuItem(fMenu, SWT.PUSH);
    newBookMark.setText(getLabelWithBinding("org.rssowl.ui.actions.NewBookMark", Messages.NewTypeDropdownAction_BOOKMARK)); //$NON-NLS-1$
    newBookMark.setImage(OwlUI.getImage(fResources, OwlUI.BOOKMARK));
    newBookMark.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          addBookmark();
        } catch (Exception e1) {
          Activator.getDefault().logError(e1.getMessage(), e1);
        }
      }
    });

    MenuItem newNewsBin = new MenuItem(fMenu, SWT.PUSH);
    newNewsBin.setText(getLabelWithBinding("org.rssowl.ui.actions.NewNewsBin", Messages.NewTypeDropdownAction_NEWSBIN)); //$NON-NLS-1$
    newNewsBin.setImage(OwlUI.getImage(fResources, OwlUI.NEWSBIN));
    newNewsBin.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          addNewsBin();
        } catch (Exception e1) {
          Activator.getDefault().logError(e1.getMessage(), e1);
        }
      }
    });

    MenuItem newSearchMark = new MenuItem(fMenu, SWT.PUSH);
    newSearchMark.setText(getLabelWithBinding("org.rssowl.ui.actions.NewSearchMark", Messages.NewTypeDropdownAction_SAVED_SEARCH)); //$NON-NLS-1$
    newSearchMark.setImage(OwlUI.getImage(fResources, OwlUI.SEARCHMARK));
    newSearchMark.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          addSearchMark();
        } catch (Exception e1) {
          Activator.getDefault().logError(e1.getMessage(), e1);
        }
      }
    });

    new MenuItem(fMenu, SWT.SEPARATOR);

    MenuItem newFolder = new MenuItem(fMenu, SWT.PUSH);
    newFolder.setText(getLabelWithBinding("org.rssowl.ui.actions.NewFolder", Messages.NewTypeDropdownAction_FOLDER)); //$NON-NLS-1$
    newFolder.setImage(OwlUI.getImage(fResources, OwlUI.FOLDER));
    newFolder.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          addFolder();
        } catch (Exception e1) {
          Activator.getDefault().logError(e1.getMessage(), e1);
        }
      }
    });

    return fMenu;
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  @Override
  public void dispose() {
    fResources.dispose();
    if (fMenu != null)
      OwlUI.safeDispose(fMenu);
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  public void init(IWorkbenchWindow window) {
    fShell = window.getShell();
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run(IAction action) {
    try {
      addBookmark();
    } catch (Exception e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {

    /* Delete the old Selection */
    fParent = null;
    fPosition = null;

    /* Check Selection */
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structSel = (IStructuredSelection) selection;
      if (!structSel.isEmpty()) {
        Object firstElement = structSel.getFirstElement();
        if (firstElement instanceof IFolder)
          fParent = (IFolder) firstElement;
        else if (firstElement instanceof IMark) {
          fParent = ((IMark) firstElement).getParent();
          fPosition = ((IMark) firstElement);
        }
      }
    }
  }

  private void addBookmark() {
    new NewBookMarkAction(fShell, fParent, fPosition).run(null);
  }

  private void addNewsBin() {
    new NewNewsBinAction(fShell, fParent, fPosition).run(null);
  }

  private void addFolder() {
    new NewFolderAction(fShell, fParent, fPosition).run(null);
  }

  private void addSearchMark() {
    new NewSearchMarkAction(fShell, fParent, fPosition).run(null);
  }

  /*
   * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
   */
  @Override
  public Menu getMenu(Menu parent) {
    return null;
  }

  private String getLabelWithBinding(String id, String label) {
    TriggerSequence binding = fBindingService.getBestActiveBindingFor(id);
    if (binding != null)
      return NLS.bind(Messages.NewTypeDropdownAction_LABEL_BINDING, label, binding.format());

    return label;
  }
}
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.properties.EntityPropertyDialog;
import org.rssowl.ui.internal.dialogs.properties.EntityPropertyPageWrapper;
import org.rssowl.ui.internal.util.ModelUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

import java.util.List;
import java.util.Set;

/**
 * Action to bring up the Dialog to edit properties of selected Entities.
 *
 * @author bpasero
 */
public class EntityPropertyDialogAction extends Action implements IObjectActionDelegate {
  private IShellProvider fShellProvider;
  private ISelectionProvider fSelectionProvider;
  private IStructuredSelection fSelection;

  /** Keep for reflection */
  public EntityPropertyDialogAction() {}

  /**
   * @param shellProvider
   * @param selectionProvider
   */
  public EntityPropertyDialogAction(IShellProvider shellProvider, ISelectionProvider selectionProvider) {
    fShellProvider = shellProvider;
    fSelectionProvider = selectionProvider;
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {

    /* Retrieve Selection */
    IStructuredSelection selection;
    if (fSelection != null)
      selection = fSelection;
    else
      selection = (IStructuredSelection) fSelectionProvider.getSelection();

    /* Selection Present */
    if (!selection.isEmpty()) {

      /* Retrieve selected Entities */
      List<IEntity> selectedEntities = ModelUtils.getEntities(selection);

      /* Collect responsible property-pages */
      Set<EntityPropertyPageWrapper> pages = Controller.getDefault().getEntityPropertyPagesFor(selectedEntities);

      /* Pages are present */
      if (!pages.isEmpty()) {

        /* Create & Open the Property-Dialog */
        EntityPropertyDialog dialog = new EntityPropertyDialog(fShellProvider.getShell(), selectedEntities);
        dialog.setTitle(getTitle(selectedEntities));

        /* Add contributed pages */
        for (EntityPropertyPageWrapper page : pages) {
          page.createPage();
          dialog.addPage(page);
        }

        /* Re-Sort if sorting by name is enabled */
        if (dialog.open() == IDialogConstants.OK_ID) {
          if (dialog.entitiesUpdated() && selectedEntities.size() == 1) { // Name can only be changed on single entity
            IEntity entity = selectedEntities.get(0);
            if (entity instanceof IFolderChild && ((IFolderChild) entity).getParent() != null) {
              IFolder parent = ((IFolderChild) entity).getParent();
              BookMarkExplorer explorer = OwlUI.getOpenedBookMarkExplorer();
              if (explorer != null && explorer.isSortByNameEnabled())
                ((StructuredViewer) explorer.getViewSite().getSelectionProvider()).refresh(parent);
            }
          }
        }
      }
    }
  }

  /*
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   * org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    fShellProvider = targetPart.getSite();
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
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection)
      fSelection = (IStructuredSelection) selection;
  }

  private String getTitle(List<IEntity> entities) {

    /* Single Entity selected */
    if (entities.size() == 1) {
      IEntity entity = entities.get(0);

      if (entity instanceof IFolder)
        return NLS.bind(Messages.EntityPropertyDialogAction_PROPERTIES_FOR_N, ((IFolder) entity).getName());

      if (entity instanceof IMark)
        return NLS.bind(Messages.EntityPropertyDialogAction_PROPERTIES_FOR_N, ((IMark) entity).getName());
    }

    /* Multi Entities selected */
    else if (entities.size() > 1) {
      int folderCount = 0;
      int bookMarkCount = 0;
      int searchMarkCount = 0;
      int newsBinCount = 0;

      for (IEntity entity : entities) {
        if (entity instanceof IFolder)
          folderCount++;
        else if (entity instanceof IBookMark)
          bookMarkCount++;
        else if (entity instanceof INewsBin)
          newsBinCount++;
        else if (entity instanceof ISearchMark)
          searchMarkCount++;
      }

      StringBuilder buf = new StringBuilder();

      if (folderCount > 0)
        buf.append(folderCount == 1 ? NLS.bind(Messages.EntityPropertyDialogAction_N_FOLDER, folderCount) : NLS.bind(Messages.EntityPropertyDialogAction_N_FOLDERS, folderCount)).append(", "); //$NON-NLS-1$

      if (bookMarkCount > 0)
        buf.append(bookMarkCount == 1 ? NLS.bind(Messages.EntityPropertyDialogAction_N_BOOKMARK, bookMarkCount) : NLS.bind(Messages.EntityPropertyDialogAction_N_BOOKMARKS, bookMarkCount)).append(", "); //$NON-NLS-1$

      if (searchMarkCount > 0)
        buf.append(searchMarkCount == 1 ? NLS.bind(Messages.EntityPropertyDialogAction_N_SEARCH, searchMarkCount) : NLS.bind(Messages.EntityPropertyDialogAction_N_SEARCHES, searchMarkCount)).append(", "); //$NON-NLS-1$

      if (newsBinCount > 0)
        buf.append(newsBinCount == 1 ? NLS.bind(Messages.EntityPropertyDialogAction_N_BIN, newsBinCount) : NLS.bind(Messages.EntityPropertyDialogAction_N_BINS, newsBinCount)).append(", "); //$NON-NLS-1$

      if (buf.length() > 0)
        buf.delete(buf.length() - 2, buf.length());

      return buf.toString();
    }

    return null;
  }
}
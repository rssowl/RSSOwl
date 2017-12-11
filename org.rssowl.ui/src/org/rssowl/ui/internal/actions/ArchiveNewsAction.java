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
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.ArchiveNewsDialog;
import org.rssowl.ui.internal.editors.feed.NewsGrouping;
import org.rssowl.ui.internal.util.ModelUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Moves selected news to the archive news bin.
 *
 * @author bpasero
 */
public class ArchiveNewsAction extends Action implements IWorkbenchWindowActionDelegate {
  private static final String ID = "org.rssowl.ui.ArchiveCommand"; //$NON-NLS-1$

  private IStructuredSelection fSelection;

  /**
   * Leave for Reflection.
   */
  public ArchiveNewsAction() {
    this(StructuredSelection.EMPTY);
  }

  /**
   * @param selection
   */
  public ArchiveNewsAction(IStructuredSelection selection) {
    fSelection = selection;
    setId(ID);
    setActionDefinitionId(ID);
  }

  /*
   * @see org.eclipse.jface.action.Action#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor() {
    return OwlUI.ARCHIVE;
  }

  /*
   * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
   */
  @Override
  public ImageDescriptor getDisabledImageDescriptor() {
    return OwlUI.ARCHIVE_DISABLED;
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    IBindingService bs = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
    TriggerSequence binding = bs.getBestActiveBindingFor(ID);

    return binding != null ? NLS.bind(Messages.ArchiveNewsAction_ARCHIVE_NEWS_BINDING, binding.format()) : Messages.ArchiveNewsAction_ARCHIVE_NEWS;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run(IAction action) {
    run();
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {

    /* Return early if no News selected */
    List<INews> newsList = ModelUtils.getEntities(fSelection, INews.class);
    if (newsList.isEmpty())
      return;

    /* Find the Archive Bin */
    INewsBin archive = CoreUtils.findArchive();

    /* Archive not found, inform the user and let him pick a bin for archiving */
    if (archive == null)
      handleFirstArchive();

    /* Archive found, move the selected News over */
    else
      archiveNews(archive);
  }

  private void archiveNews(INewsBin archive) {
    MoveCopyNewsToBinAction action = MoveCopyNewsToBinAction.createArchiveAction(fSelection, archive);
    action.run();
  }

  private void handleFirstArchive() {

    /* Inform the user about archive feature */
    ArchiveNewsDialog dialog = new ArchiveNewsDialog(OwlUI.getActiveShell());
    int res = dialog.open();

    /* Check for Cancellation */
    if (res == IDialogConstants.CANCEL_ID)
      return;

    /* Find current selected set from Feeds View */
    IFolder selectedSet = OwlUI.getSelectedBookMarkSet();

    /* Create new Archive bin in selected set */
    if (selectedSet != null) {
      INewsBin archive = Owl.getModelFactory().createNewsBin(null, selectedSet, Messages.ArchiveNewsAction_ARCHIVE);

      /* Copy all Properties from Parent into the Archive */
      Map<String, Serializable> properties = selectedSet.getProperties();
      for (Map.Entry<String, Serializable> property : properties.entrySet())
        archive.setProperty(property.getKey(), property.getValue());

      /* Set the archive to group by date and flag the archive bin */
      IPreferenceScope archivePreferences = Owl.getPreferenceService().getEntityScope(archive);
      archivePreferences.putInteger(DefaultPreferences.BM_NEWS_GROUPING, NewsGrouping.Type.GROUP_BY_DATE.ordinal());
      archivePreferences.putBoolean(DefaultPreferences.ARCHIVE_BIN_MARKER, true);

      /* Save Archive */
      DynamicDAO.save(selectedSet);

      /* Actually archive selected news now into new Archive */
      archiveNews(archive);

      /* Select the Archive in the Feeds View */
      StructuredSelection selection = new StructuredSelection(archive);
      BookMarkExplorer explorer = OwlUI.getOpenedBookMarkExplorer();
      if (explorer != null)
        explorer.getViewSite().getSelectionProvider().setSelection(selection);

      /* Open the Archive */
      OwlUI.openInFeedView(OwlUI.getPage(), selection);
    }
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection)
      fSelection = (IStructuredSelection) selection;
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  @Override
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  public void init(IWorkbenchWindow window) {}
}
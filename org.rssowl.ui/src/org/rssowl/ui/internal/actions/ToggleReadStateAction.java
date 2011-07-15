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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.undo.NewsStateOperation;
import org.rssowl.ui.internal.undo.UndoStack;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.EnumSet;
import java.util.List;

/**
 * @author bpasero
 */
public class ToggleReadStateAction extends Action implements IWorkbenchWindowActionDelegate {

  /** Action ID */
  public static final String ID = "org.rssowl.ui.ToggleReadState"; //$NON-NLS-1$

  /* Unread States (New, Unread, Updated) */
  private static final EnumSet<INews.State> UNREAD_STATES = EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED);

  private IStructuredSelection fSelection;
  private boolean fMarkRead;

  /** Leave for reflection */
  public ToggleReadStateAction() {}

  /**
   * @param selection
   */
  public ToggleReadStateAction(IStructuredSelection selection) {
    fSelection = selection;
    fMarkRead = shouldMarkRead(fSelection);
  }

  /*
   * @see org.eclipse.jface.action.Action#getStyle()
   */
  @Override
  public int getStyle() {
    return IAction.AS_CHECK_BOX;
  }

  private boolean shouldMarkRead(IStructuredSelection selection) {
    List<INews> entities = ModelUtils.getEntities(selection, INews.class);
    for (INews entity : entities) {
      if (UNREAD_STATES.contains(entity.getState())) {
        return true;
      }
    }

    return false;
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    IBindingService bs = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
    TriggerSequence binding = bs.getBestActiveBindingFor(ID);

    return binding != null ? NLS.bind(Messages.ToggleReadStateAction_NEWS_READ_BINDING, binding.format()) : Messages.ToggleReadStateAction_NEWS_READ;
  }

  /*
   * @see org.eclipse.jface.action.Action#isChecked()
   */
  @Override
  public boolean isChecked() {
    return !fMarkRead;
  }

  /*
   * @see org.eclipse.jface.action.Action#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor() {
    return OwlUI.getImageDescriptor("icons/elcl16/mark_read.gif"); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    internalRun(fSelection, fMarkRead);
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    FeedView activeFeedView = OwlUI.getActiveFeedView();
    if (activeFeedView != null) {
      IStructuredSelection selection = (IStructuredSelection) activeFeedView.getSite().getSelectionProvider().getSelection();
      if (selection != null && !selection.isEmpty())
        internalRun(selection, shouldMarkRead(selection));
    }
  }

  private void internalRun(IStructuredSelection selection, boolean markRead) {

    /* Nothing to do */
    if (selection.isEmpty())
      return;

    /* Mark Read */
    if (markRead)
      new MarkTypesReadAction(selection).run();

    /* Mark Unread */
    else {

      /* Mark Saved Search Service as in need for a quick Update */
      Controller.getDefault().getSavedSearchService().forceQuickUpdate();

      /* Only consider INews */
      List<INews> newsList = ModelUtils.getEntities(selection, INews.class);

      /* Support Undo */
      UndoStack.getInstance().addOperation(new NewsStateOperation(newsList, INews.State.UNREAD, false));

      /* Perform Operation */
      DynamicDAO.getDAO(INewsDAO.class).setState(newsList, INews.State.UNREAD, false, false);
    }
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {}

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {}
}
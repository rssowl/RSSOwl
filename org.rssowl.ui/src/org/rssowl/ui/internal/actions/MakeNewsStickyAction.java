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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.undo.StickyOperation;
import org.rssowl.ui.internal.undo.UndoStack;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.List;

/**
 * Sets the Sticky-State of selected Items.
 *
 * @author bpasero
 */
public class MakeNewsStickyAction extends Action implements IWorkbenchWindowActionDelegate {
  private static final String ID = "org.rssowl.ui.actions.MarkSticky"; //$NON-NLS-1$

  private IStructuredSelection fSelection;
  private boolean fMarkSticky;

  /**
   * Leave for Reflection.
   */
  public MakeNewsStickyAction() {
    this(StructuredSelection.EMPTY);
  }

  /**
   * @param selection
   */
  public MakeNewsStickyAction(IStructuredSelection selection) {
    fSelection = selection;
    init();
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    IBindingService bs = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
    TriggerSequence binding = bs.getBestActiveBindingFor(ID);

    return binding != null ? NLS.bind(Messages.MakeNewsStickyAction_NEWS_STICKY_BINDING, binding.format()) : Messages.MakeNewsStickyAction_NEWS_STICKY;
  }

  /*
   * @see org.eclipse.jface.action.Action#getStyle()
   */
  @Override
  public int getStyle() {
    return IAction.AS_CHECK_BOX;
  }

  /*
   * @see org.eclipse.jface.action.Action#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor() {
    return fMarkSticky ? OwlUI.NEWS_PIN : OwlUI.NEWS_PINNED;
  }

  /*
   * @see org.eclipse.jface.action.Action#isChecked()
   */
  @Override
  public boolean isChecked() {
    return !fMarkSticky;
  }

  private void init() {
    List<INews> entities = ModelUtils.getEntities(fSelection, INews.class);
    for (INews entity : entities) {

      /* News which is not sticky */
      if (!entity.isFlagged()) {
        fMarkSticky = true;
        break;
      }
    }
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
    List<INews> newsList = ModelUtils.getEntities(fSelection, INews.class);
    if (newsList.isEmpty())
      return;

    /* Support Undo */
    UndoStack.getInstance().addOperation(new StickyOperation(newsList, fMarkSticky));

    /* Set Sticky State */
    for (INews newsItem : newsList) {
      newsItem.setFlagged(fMarkSticky);
    }

    /* Mark Saved Search Service as in need for a quick Update */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Save List of INews */
    DynamicDAO.saveAll(newsList);

    /* Update in case this action is rerun on the same selection */
    fMarkSticky = !fMarkSticky;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection)
      fSelection = (IStructuredSelection) selection;
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {}
}
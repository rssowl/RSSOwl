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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.EntityGroupItem;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.editors.feed.FeedViewInput;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Global Action for Reloading a Selection of Folders and contained BookMarks.
 *
 * @author bpasero
 */
public class ReloadTypesAction extends Action implements IObjectActionDelegate {

  /** ID of this Action */
  public static final String ID = "org.rssowl.ui.actions.Reload"; //$NON-NLS-1$

  private IStructuredSelection fSelection;
  private Shell fShell;

  /**
   * Keep default constructor for reflection.
   * <p>
   * Note: This Constructor should <em>not</em> directly be called. Use
   * <code>ReloadTypesAction(IStructuredSelection selection)</code> instead.
   * </p>
   */
  public ReloadTypesAction() {
    this(StructuredSelection.EMPTY, null);
  }

  /**
   * Creates a new Action for Reloading Types from the given Selection.
   *
   * @param selection The Selection to Reload.
   * @param shell The Shell this operation is running from.
   */
  public ReloadTypesAction(IStructuredSelection selection, Shell shell) {
    Assert.isNotNull(selection);
    fSelection = selection;
    fShell = shell;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run(IAction action) {
    run();
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
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    Set<IBookMark> selectedBookMarks = new TreeSet<IBookMark>(new Comparator<IBookMark>() {
      @Override
      public int compare(IBookMark o1, IBookMark o2) {
        if (o1.equals(o2))
          return 0;

        return 1;
      }
    });

    /* Set Selection to Input if Empty */
    if (fSelection.isEmpty()) {
      FeedView activeFeedView = OwlUI.getActiveFeedView();
      if (activeFeedView != null) {
        FeedViewInput input = (FeedViewInput) activeFeedView.getEditorInput();
        fSelection = new StructuredSelection(input.getMark());
      }
    }

    /* Run Update */
    List<?> list = fSelection.toList();
    Set<FeedLinkReference> handledFeedsFromNews = new HashSet<FeedLinkReference>();
    for (Object selection : list) {
      if (selection instanceof IFolder) {
        IFolder folder = (IFolder) selection;
        getBookMarks(folder, selectedBookMarks);
      }

      else if (selection instanceof IBookMark) {
        IBookMark bookMark = (IBookMark) selection;
        selectedBookMarks.add(bookMark);
      }

      else if (selection instanceof INews) {
        INews news = (INews) selection;
        if (!handledFeedsFromNews.contains(news.getFeedReference())) {
          handledFeedsFromNews.add(news.getFeedReference());
          Collection<IBookMark> relatedBookmarks = Owl.getPersistenceService().getDAOService().getBookMarkDAO().loadAll(news.getFeedReference());
          if (relatedBookmarks.size() > 0)
            selectedBookMarks.add(relatedBookmarks.iterator().next());
        }
      }

      else if (selection instanceof EntityGroup) {
        EntityGroup group = (EntityGroup) selection;
        List<EntityGroupItem> items = group.getItems();
        for (EntityGroupItem item : items) {
          IEntity entity = item.getEntity();
          if (entity instanceof IBookMark) {
            IBookMark bookMark = (IBookMark) entity;
            selectedBookMarks.add(bookMark);
          } else if (entity instanceof INews) {
            INews news = (INews) entity;
            if (!handledFeedsFromNews.contains(news.getFeedReference())) {
              handledFeedsFromNews.add(news.getFeedReference());
              Collection<IBookMark> relatedBookmarks = Owl.getPersistenceService().getDAOService().getBookMarkDAO().loadAll(news.getFeedReference());
              if (relatedBookmarks.size() > 0)
                selectedBookMarks.add(relatedBookmarks.iterator().next());
            }
          }
        }
      }
    }

    /* Force quick update of the first News coming in */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Pass to controller for a queued reloading using ITasks */
    Controller.getDefault().reloadQueued(selectedBookMarks, null, fShell);
  }

  private void getBookMarks(IFolder folder, Set<IBookMark> bookmarks) {

    /* Go through BookMarks */
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark)
        bookmarks.add((IBookMark) mark);
    }

    /* Go through Subfolders */
    List<IFolder> folders = folder.getFolders();
    for (IFolder childFolder : folders)
      getBookMarks(childFolder, bookmarks);
  }

  /*
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   * org.eclipse.ui.IWorkbenchPart)
   */
  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    fShell = targetPart.getSite().getShell();
  }
}
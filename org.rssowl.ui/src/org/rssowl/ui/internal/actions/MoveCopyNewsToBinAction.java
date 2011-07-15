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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.undo.CopyOperation;
import org.rssowl.ui.internal.undo.MoveOperation;
import org.rssowl.ui.internal.undo.UndoStack;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Action to move or copy a selection of News to a Newsbin.
 *
 * @author bpasero
 */
public class MoveCopyNewsToBinAction extends Action {
  private final IStructuredSelection fSelection;
  private final boolean fIsMove;
  private final boolean fIsArchive;
  private INewsBin fBin;

  /**
   * @param selection
   * @param archive
   * @return an action to archive a selection of news
   */
  public static MoveCopyNewsToBinAction createArchiveAction(IStructuredSelection selection, INewsBin archive) {
    return new MoveCopyNewsToBinAction(selection, archive, true, true);
  }

  /**
   * @param selection
   * @param bin
   * @param isMove
   */
  public MoveCopyNewsToBinAction(IStructuredSelection selection, INewsBin bin, boolean isMove) {
    this(selection, bin, isMove, false);
  }

  private MoveCopyNewsToBinAction(IStructuredSelection selection, INewsBin bin, boolean isMove, boolean isArchive) {
    fSelection = selection;
    fBin = bin;
    fIsMove = isMove;
    fIsArchive = isArchive;
  }

  /*
   * @see org.eclipse.jface.action.Action#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor() {
    if (fBin != null) {
      boolean isArchive = fBin.getProperty(DefaultPreferences.ARCHIVE_BIN_MARKER) != null;
      if (isArchive)
        return OwlUI.ARCHIVE;

      return fBin.getNewsCount(INews.State.getVisible()) > 0 ? OwlUI.NEWSBIN : OwlUI.NEWSBIN_EMPTY;
    }

    return OwlUI.NEWSBIN;
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    return fBin != null ? fBin.getName() : Messages.MoveCopyNewsToBinAction_NEW_NEWSBIN;
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {

    /* Open Dialog to create a new Bin first */
    if (fBin == null) {
      NewNewsBinAction action = new NewNewsBinAction();
      action.run(null);
      fBin = action.getNewsbin();
    }

    /* Move / Copy */
    if (fBin != null)
      moveCopyToBin();
  }

  private void moveCopyToBin() {
    List<?> objects = fSelection.toList();
    Collection<INews> news = ModelUtils.normalize(objects);
    boolean requiresSave = false;

    /* Only consider those not already present in the Bin */
    List<INews> newsToMoveCopy = new ArrayList<INews>(news.size());
    for (INews newsitem : news) {
      if (!fBin.containsNews(newsitem))
        newsToMoveCopy.add(newsitem);
    }

    /* Return if nothing to do */
    if (newsToMoveCopy.isEmpty())
      return;

    /* For each News: Copy */
    List<INews> copiedNews = new ArrayList<INews>(newsToMoveCopy.size());
    for (INews newsitem : newsToMoveCopy) {
      INews newsCopy = Owl.getModelFactory().createNews(newsitem, fBin);
      copiedNews.add(newsCopy);

      /* Ensure the state is *unread* since it has been seen */
      if (newsCopy.getState() == INews.State.NEW)
        newsCopy.setState(INews.State.UNREAD);

      requiresSave = true;
    }

    /* Mark Saved Search Service as in need for a quick Update */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Save */
    if (requiresSave) {
      DynamicDAO.saveAll(copiedNews);
      DynamicDAO.save(fBin);
    }

    /* Support Undo/Redo */
    if (fIsMove)
      UndoStack.getInstance().addOperation(new MoveOperation(newsToMoveCopy, copiedNews, fIsArchive));
    else
      UndoStack.getInstance().addOperation(new CopyOperation(copiedNews));

    /* Delete News from Source if required */
    if (fIsMove) {

      /* Mark Saved Search Service as in need for a quick Update */
      Controller.getDefault().getSavedSearchService().forceQuickUpdate();

      /* Delete News in single Transaction */
      DynamicDAO.getDAO(INewsDAO.class).setState(newsToMoveCopy, INews.State.HIDDEN, false, false);
    }
  }
}
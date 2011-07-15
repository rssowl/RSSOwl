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

package org.rssowl.ui.internal;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;

/**
 * Add the <code>StatusLineUpdater</code> to your ViewPart to have the statusbar
 * describing the selected elements.
 *
 * @author bpasero
 */
public class StatusLineUpdater implements ISelectionChangedListener {
  private IStatusLineManager fStatusLineManager;

  /**
   * @param statusLineManager
   */
  public StatusLineUpdater(IStatusLineManager statusLineManager) {
    fStatusLineManager = statusLineManager;
  }

  /*
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    String text = formatElements(selection.toArray());

    /* Show Message */
    fStatusLineManager.setMessage(text);
  }

  private String formatElements(Object elements[]) {

    /* No Element selected */
    if (elements.length == 0)
      return ""; //$NON-NLS-1$

    /* Only 1 Element selected */
    if (elements.length == 1) {
      Object element = elements[0];
      if (element instanceof IFolder)
        return ((IFolder) element).getName();
      else if (element instanceof IMark)
        return ((IMark) element).getName();
      else if (element instanceof EntityGroup)
        return ((EntityGroup) element).getName();
      else if (element instanceof INews) // Ignore This
        return ""; //$NON-NLS-1$

      return Messages.StatusLineUpdater_ITEM_SELECTED;
    }

    /* More than 1 Element selected */
    int newsCount = 0;
    int folderCount = 0;
    int bookMarkCount = 0;
    int newsBinCount = 0;
    int searchMarkCount = 0;
    int viewerGroupCount = 0;

    for (Object element : elements) {
      if (element instanceof IFolder)
        folderCount++;
      else if (element instanceof IBookMark)
        bookMarkCount++;
      else if (element instanceof INewsBin)
        newsBinCount++;
      else if (element instanceof ISearchMark)
        searchMarkCount++;
      else if (element instanceof EntityGroup)
        viewerGroupCount++;
      else if (element instanceof INews)
        newsCount++;
    }

    StringBuilder itemsBuf = new StringBuilder();
    if (folderCount > 0)
      itemsBuf.append(folderCount == 1 ? NLS.bind(Messages.StatusLineUpdater_N_FOLDER, folderCount) : NLS.bind(Messages.StatusLineUpdater_N_FOLDERS, folderCount)).append(", "); //$NON-NLS-1$

    if (bookMarkCount > 0)
      itemsBuf.append(bookMarkCount == 1 ? NLS.bind(Messages.StatusLineUpdater_N_BOOKMARK, bookMarkCount) : NLS.bind(Messages.StatusLineUpdater_N_BOOKMARKS, bookMarkCount)).append(", "); //$NON-NLS-1$

    if (newsBinCount > 0)
      itemsBuf.append(newsBinCount == 1 ? NLS.bind(Messages.StatusLineUpdater_N_BIN, newsBinCount) : NLS.bind(Messages.StatusLineUpdater_N_BINS, newsBinCount)).append(", "); //$NON-NLS-1$

    if (searchMarkCount > 0)
      itemsBuf.append(searchMarkCount == 1 ? NLS.bind(Messages.StatusLineUpdater_N_SEARCH, searchMarkCount) : NLS.bind(Messages.StatusLineUpdater_N_SEARCHES, searchMarkCount)).append(", "); //$NON-NLS-1$

    if (viewerGroupCount > 0)
      itemsBuf.append(viewerGroupCount == 1 ? NLS.bind(Messages.StatusLineUpdater_N_GROUP, viewerGroupCount) : NLS.bind(Messages.StatusLineUpdater_N_GROUPS, viewerGroupCount)).append(", "); //$NON-NLS-1$

    if (newsCount > 0)
      itemsBuf.append(NLS.bind(Messages.StatusLineUpdater_N_NEWS, newsCount)).append(", "); //$NON-NLS-1$

    if (itemsBuf.length() > 0)
      itemsBuf.delete(itemsBuf.length() - 2, itemsBuf.length());

    StringBuilder buf = new StringBuilder();
    buf.append(NLS.bind(Messages.StatusLineUpdater_N_ITEMS_SELECTED, elements.length, itemsBuf.toString()));

    return buf.toString();
  }
}
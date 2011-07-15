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

package org.rssowl.ui.internal.undo;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A subclass of {@link CopyOperation} allowing to undo/redo moving News into a
 * Bin.
 *
 * @author bpasero
 */
public class MoveOperation extends CopyOperation {
  private final Map<State, List<NewsReference>> fOriginalNews;
  private final boolean fIsArchive;

  /**
   * @param originalnews
   * @param copiednews
   * @param isArchive
   */
  public MoveOperation(List<INews> originalnews, List<INews> copiednews, boolean isArchive) {
    super(copiednews);
    fIsArchive = isArchive;

    Assert.isTrue(originalnews.size() == copiednews.size());

    /* Fill original News */
    fOriginalNews = CoreUtils.toStateMap(originalnews);
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#getName()
   */
  @Override
  public String getName() {
    return fIsArchive ? NLS.bind(Messages.MoveOperation_ARCHIVE_N, fNewsCount) : NLS.bind(Messages.MoveOperation_MOVE_N, fNewsCount);
  }

  /*
   * @see org.rssowl.ui.internal.undo.CopyOperation#undo()
   */
  @Override
  public void undo() {

    /* Undo the Copy-Part */
    super.undo();

    /* Restore original News */
    Set<Entry<State, List<NewsReference>>> entries = fOriginalNews.entrySet();
    for (Entry<State, List<NewsReference>> entry : entries) {
      INews.State oldState = entry.getKey();
      List<NewsReference> newsRefs = entry.getValue();

      List<INews> resolvedNews = new ArrayList<INews>(newsRefs.size());
      for (NewsReference newsRef : newsRefs) {
        INews news = newsRef.resolve();
        if (news != null)
          resolvedNews.add(news);
      }

      /* Force quick update of saved searches */
      Controller.getDefault().getSavedSearchService().forceQuickUpdate();

      /* Set old state back to all news */
      fNewsDao.setState(resolvedNews, oldState, false, false);
    }
  }

  /*
   * @see org.rssowl.ui.internal.undo.CopyOperation#redo()
   */
  @Override
  public void redo() {

    /* Redo the Copy-Part */
    super.redo();

    /* Force quick update of saved searches */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Delete News in single Transaction */
    DynamicDAO.getDAO(INewsDAO.class).setState(CoreUtils.resolveAll(fOriginalNews), INews.State.HIDDEN, false, false);
  }
}
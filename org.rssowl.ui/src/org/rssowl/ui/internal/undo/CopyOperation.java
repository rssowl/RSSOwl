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
import java.util.Set;
import java.util.Map.Entry;

/**
 * An instance of {@link IUndoOperation} allowing to undo/redo copying of News
 * into a Bin.
 *
 * @author bpasero
 */
public class CopyOperation implements IUndoOperation {

  /* Limit when this operation is becoming a long running one */
  private static final int LONG_RUNNING_LIMIT = 50;

  private final Map<State, List<NewsReference>> fCopiedNews;
  final int fNewsCount;
  final INewsDAO fNewsDao = DynamicDAO.getDAO(INewsDAO.class);

  /**
   * @param copiednews
   */
  public CopyOperation(List<INews> copiednews) {
    fCopiedNews = CoreUtils.toStateMap(copiednews);
    fNewsCount = copiednews.size();
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#getName()
   */
  public String getName() {
    return NLS.bind(Messages.CopyOperation_COPY_N, fNewsCount);
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#undo()
   */
  public void undo() {

    /* Force quick update of saved searches */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Set Copied News to Hidden */
    fNewsDao.setState(CoreUtils.resolveAll(fCopiedNews), INews.State.HIDDEN, false, false);
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#redo()
   */
  public void redo() {
    Set<Entry<State, List<NewsReference>>> entries = fCopiedNews.entrySet();
    for (Entry<State, List<NewsReference>> entry : entries) {
      INews.State oldState = entry.getKey();
      List<NewsReference> newsRefs = entry.getValue();

      List<INews> resolvedNews = new ArrayList<INews>(newsRefs.size());
      for (NewsReference newsRef : newsRefs) {
        INews newsitem = newsRef.resolve();
        if (newsitem != null) {
          resolvedNews.add(newsitem);
        }
      }

      /* Force quick update of saved searches */
      Controller.getDefault().getSavedSearchService().forceQuickUpdate();

      /* Set old state back to all news */
      fNewsDao.setState(resolvedNews, oldState, false, false);
    }
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#isLongRunning()
   */
  public boolean isLongRunning() {
    return fNewsCount > LONG_RUNNING_LIMIT;
  }
}
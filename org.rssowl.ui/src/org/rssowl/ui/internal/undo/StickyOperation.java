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
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.ui.internal.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * An instance of {@link IUndoOperation} allowing to undo/redo marking news as
 * sticky.
 *
 * @author bpasero
 */
public class StickyOperation implements IUndoOperation {

  /* Limit when this operation is becoming a long running one */
  private static final int LONG_RUNNING_LIMIT = 50;

  private final Map<Boolean, List<NewsReference>> fOldStickyStates;
  private final boolean fMakeSticky;
  private final int fNewsCount;

  /**
   * @param news
   * @param makeSticky
   */
  public StickyOperation(Collection<INews> news, boolean makeSticky) {
    fOldStickyStates = toStickyMap(news);
    fMakeSticky = makeSticky;
    fNewsCount = news.size();
  }

  private Map<Boolean, List<NewsReference>> toStickyMap(Collection<INews> news) {
    Map<Boolean, List<NewsReference>> map = new HashMap<Boolean, List<NewsReference>>();
    for (INews newsitem : news) {
      List<NewsReference> newsrefs = map.get(newsitem.isFlagged());
      if (newsrefs == null) {
        newsrefs = new ArrayList<NewsReference>();
        map.put(newsitem.isFlagged(), newsrefs);
      }

      newsrefs.add(newsitem.toReference());
    }

    return map;
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#getName()
   */
  public String getName() {
    return fMakeSticky ? NLS.bind(Messages.StickyOperation_MARK_N_STICKY, fNewsCount) : NLS.bind(Messages.StickyOperation_MARK_N_UNSTICKY, fNewsCount);
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#undo()
   */
  public void undo() {
    Set<Entry<Boolean, List<NewsReference>>> entries = fOldStickyStates.entrySet();
    for (Entry<Boolean, List<NewsReference>> entry : entries) {
      boolean oldSticky = entry.getKey();
      List<NewsReference> newsRefs = entry.getValue();

      List<INews> resolvedNews = new ArrayList<INews>(newsRefs.size());
      for (NewsReference newsRef : newsRefs) {
        INews news = newsRef.resolve();
        if (news != null) {
          resolvedNews.add(news);
          news.setFlagged(oldSticky);
        }
      }

      /* Force quick update of saved searches */
      Controller.getDefault().getSavedSearchService().forceQuickUpdate();

      /* Set old sticky-state back to all news */
      DynamicDAO.saveAll(resolvedNews);
    }
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#redo()
   */
  public void redo() {

    /* Resolve News */
    List<INews> resolvedNews = new ArrayList<INews>(fNewsCount);
    Collection<List<NewsReference>> newsRefLists = fOldStickyStates.values();
    for (List<NewsReference> newsRefList : newsRefLists) {
      for (NewsReference newsRef : newsRefList) {
        INews news = newsRef.resolve();
        if (news != null) {
          resolvedNews.add(news);
          news.setFlagged(fMakeSticky);
        }
      }
    }

    /* Force quick update of saved searches */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Set state back to all news */
    DynamicDAO.saveAll(resolvedNews);
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#isLongRunning()
   */
  public boolean isLongRunning() {
    return fNewsCount > LONG_RUNNING_LIMIT;
  }
}
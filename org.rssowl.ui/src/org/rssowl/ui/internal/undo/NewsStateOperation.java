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
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An instance of {@link IUndoOperation} that allows to undo/redo changing the
 * state of News. For example this supports undo/redo of deleting news or
 * marking news as read.
 *
 * @author bpasero
 */
public class NewsStateOperation implements IUndoOperation {
  private static final EnumSet<INews.State> SUPPORTED_STATES = EnumSet.of(INews.State.HIDDEN, INews.State.READ, INews.State.UNREAD);

  /* Limit when this operation is becoming a long running one */
  private static final int LONG_RUNNING_LIMIT = 50;

  private final Map<State, List<NewsReference>> fOldStates;
  private final State fNewState;
  private final int fNewsCount;
  private final boolean fOnlyNewNewsAffected;
  private final boolean fAffectEquivalentNews;
  private final INewsDAO fNewsDao = DynamicDAO.getDAO(INewsDAO.class);

  /**
   * @param news
   * @param newState
   * @param affectEquivalentNews
   */
  public NewsStateOperation(Collection<INews> news, INews.State newState, boolean affectEquivalentNews) {
    Assert.isTrue(SUPPORTED_STATES.contains(newState), Messages.NewsStateOperation_UNSUPPORTED);

    fOldStates = CoreUtils.toStateMap(news);
    fNewState = newState;
    fAffectEquivalentNews = affectEquivalentNews;
    fNewsCount = news.size();
    fOnlyNewNewsAffected = fOldStates.containsKey(INews.State.NEW) && fOldStates.get(INews.State.NEW).size() == fNewsCount;
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#getName()
   */
  @Override
  public String getName() {
    switch (fNewState) {
      case HIDDEN:
        return NLS.bind(Messages.NewsStateOperation_DELETE_N_NEWS, fNewsCount);

      case READ:
        return NLS.bind(Messages.NewsStateOperation_MARK_N_READ, fNewsCount);

      case UNREAD: {
        if (fOnlyNewNewsAffected)
          return NLS.bind(Messages.NewsStateOperation_MARK_N_NEW_UNREAD, fNewsCount);

        return NLS.bind(Messages.NewsStateOperation_MARK_N_UNREAD, fNewsCount);
      }

      default:
        return Messages.NewsStateOperation_UNSUPPORTED;
    }
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#undo()
   */
  @Override
  public void undo() {
    Set<Entry<State, List<NewsReference>>> entries = fOldStates.entrySet();
    for (Entry<State, List<NewsReference>> entry : entries) {
      INews.State oldState = entry.getKey();
      List<NewsReference> newsRefs = entry.getValue();

      List<INews> resolvedNews = new ArrayList<INews>(newsRefs.size());
      for (NewsReference newsRef : newsRefs) {
        INews news = newsRef.resolve();

        /* Only support Undo for News if state of news is still current */
        if (news != null && news.getState() == fNewState)
          resolvedNews.add(news);
      }

      /* Force quick update of saved searches */
      Controller.getDefault().getSavedSearchService().forceQuickUpdate();

      /* Set old state back to all news */
      fNewsDao.setState(resolvedNews, oldState, fAffectEquivalentNews, false);
    }
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#redo()
   */
  @Override
  public void redo() {
    Set<Entry<State, List<NewsReference>>> entries = fOldStates.entrySet();
    for (Entry<State, List<NewsReference>> entry : entries) {
      INews.State oldState = entry.getKey();
      List<NewsReference> newsRefs = entry.getValue();

      List<INews> resolvedNews = new ArrayList<INews>(newsRefs.size());
      for (NewsReference newsRef : newsRefs) {
        INews news = newsRef.resolve();

        /* Only support Undo for News if state of news is still matching the old one */
        if (news != null && news.getState() == oldState)
          resolvedNews.add(news);
      }

      /* Force quick update of saved searches */
      Controller.getDefault().getSavedSearchService().forceQuickUpdate();

      /* Set state back to all news */
      fNewsDao.setState(resolvedNews, fNewState, fAffectEquivalentNews, false);
    }
  }

  /*
   * @see org.rssowl.ui.internal.undo.IUndoOperation#isLongRunning()
   */
  @Override
  public boolean isLongRunning() {
    return fNewsCount > LONG_RUNNING_LIMIT;
  }
}
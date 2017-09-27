/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.dialogs.cleanup.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.SearchHit;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Delete News that exceed a certain limit in a Feed
 */
public class DeleteNewsByCountOperation extends DeleteNewsAbstractOperation {
  private boolean isEnabled;
  private int fMaxNewsCountPerFeed;

  public DeleteNewsByCountOperation(boolean isEnabled, Object data) {
    this.isEnabled = isEnabled;
    fMaxNewsCountPerFeed = (Integer) data;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void savePreferences(IPreferenceScope preferences) {
    preferences.putBoolean(DefaultPreferences.CLEAN_UP_NEWS_BY_COUNT_STATE, isEnabled);
    preferences.putInteger(DefaultPreferences.CLEAN_UP_NEWS_BY_COUNT_VALUE, fMaxNewsCountPerFeed);
  }

  public int getMaxNewsCountPerFeed() {
    return fMaxNewsCountPerFeed;
  }

  @Override
  protected SummaryModelTaskGroup createGroup() {
    return new SummaryModelTaskGroup(NLS.bind(Messages.CleanUpModel_DELETE_BY_COUNT, fMaxNewsCountPerFeed));
  }

  @Override
  protected Set<NewsReference> searchNews(IBookMark bookmark, IProgressMonitor monitor) {

    // search
    List<SearchHit<NewsReference>> searchResults = searchNews(bookmark, monitor, null);
    if (searchResults.size() < fMaxNewsCountPerFeed)
      return null;

    /* Resolve News */
    List<INews> resolvedNews = new ArrayList<INews>(searchResults.size());
    for (SearchHit<NewsReference> sr : searchResults) {
      INews resolvedNewsItem = sr.getResult().resolve();
      if (resolvedNewsItem != null && resolvedNewsItem.isVisible())
        resolvedNews.add(resolvedNewsItem);
      else
        CoreUtils.reportIndexIssue();
    }

    /* Sort by Date */
    Collections.sort(resolvedNews, new Comparator<INews>() {
      public int compare(INews news1, INews news2) {
        return DateUtils.getRecentDate(news1).compareTo(DateUtils.getRecentDate(news2));
      }
    });

    // delete excessive news
    int toDeleteValue = resolvedNews.size() - fMaxNewsCountPerFeed;
    Set<NewsReference> news = new HashSet<NewsReference>();
    for (int i = 0; i < resolvedNews.size() && i < toDeleteValue; i++)
      news.add(new NewsReference(resolvedNews.get(i).getId()));

    return news;
  }
}

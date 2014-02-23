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

package org.rssowl.ui.internal.dialogs.cleanup.tasks;

// eclipse
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
// rssowl core
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
// UI
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.cleanup.PostProcessingWork;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An instance of <code>CleanUpTask</code> to delete a collection of
 * {@link INews}.
 *
 * @author bpasero
 */
public class NewsDeleteTask extends AbstractCleanUpTask {
  private final Collection<NewsReference> fNews;
  private ImageDescriptor fImage;
  private String fLabel;

  public NewsDeleteTask(SummaryModelTaskGroup group, IBookMark bookmark, Set<NewsReference> news) {
    super(group);
    Assert.isNotNull(news);
    Assert.isTrue(!news.isEmpty());
    fNews = news;
    init(bookmark);
  }

  private void init(IBookMark container) {

    /* Label */
    fLabel = NLS.bind(Messages.TASK_LABEL_DELETE_N_NEWS_FROM_M, fNews.size(), container.getName());

    /* Image */
    fImage = OwlUI.getFavicon(container);
    if (fImage == null)
      fImage = OwlUI.BOOKMARK;
  }

  @Override
  public void perform(PostProcessingWork work) {
    final INewsDAO newsDao = DynamicDAO.getDAO(INewsDAO.class);

    Collection<NewsReference> news = getNews();
    List<INews> resolvedNews = new ArrayList<INews>(news.size());
    for (NewsReference newsRef : news) {
      INews resolvedNewsItem = newsRef.resolve();
      if (resolvedNewsItem != null && resolvedNewsItem.isVisible())
        resolvedNews.add(resolvedNewsItem);
      else
        CoreUtils.reportIndexIssue();
    }
    newsDao.setState(resolvedNews, INews.State.DELETED, false, false);
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getImage()
   */
  @Override
  public ImageDescriptor getImage() {
    return fImage;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getLabel()
   */
  @Override
  public String getLabel() {
    return fLabel;
  }

  /**
   * @return Returns the News that are to be deleted.
   */
  public Collection<NewsReference> getNews() {
    return fNews;
  }

  @Override
  public int getWorkUnits() {
    return fNews.size(); // may be some coefficient
  }

  @Override
  public int getActualWorkUnits() {
    return fNews.size();
  }
}
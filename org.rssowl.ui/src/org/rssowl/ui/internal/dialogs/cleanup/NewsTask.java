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

package org.rssowl.ui.internal.dialogs.cleanup;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.ui.internal.OwlUI;

import java.util.Collection;

/**
 * An instance of <code>CleanUpTask</code> to delete a collection of
 * {@link INews}.
 *
 * @author bpasero
 */
public class NewsTask extends CleanUpTask {
  private final Collection<NewsReference> fNews;
  private ImageDescriptor fImage;
  private String fLabel;

  NewsTask(CleanUpGroup group, IBookMark container, Collection<NewsReference> news) {
    super(group);

    Assert.isNotNull(container);
    Assert.isNotNull(news);
    Assert.isTrue(!news.isEmpty());

    fNews = news;
    init(container);
  }

  private void init(IBookMark container) {

    /* Label */
    fLabel = NLS.bind(Messages.NewsTask_DELETE_N_NEWS_FROM_M, fNews.size(), container.getName());

    /* Image */
    fImage = OwlUI.getFavicon(container);
    if (fImage == null)
      fImage = OwlUI.BOOKMARK;
  }

  /**
   * @return Returns the News that are to be deleted.
   */
  public Collection<NewsReference> getNews() {
    return fNews;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getImage()
   */
  @Override
  ImageDescriptor getImage() {
    return fImage;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getLabel()
   */
  @Override
  String getLabel() {
    return fLabel;
  }
}
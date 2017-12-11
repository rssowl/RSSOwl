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

package org.rssowl.ui.internal.notifier;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.RGB;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.OpenInBrowserAction;
import org.rssowl.ui.internal.actions.OpenNewsAction;

import java.util.Date;
import java.util.Set;

/**
 * Instance of {@link NotificationItem} to display new {@link INews} in the
 * Notifier.
 *
 * @author bpasero
 */
public class NewsNotificationItem extends NotificationItem {

  /* Max. length of the Description Excerpt */
  private static final int MAX_DESCRIPTION_LENGTH = 500;

  private final String fNewsLink;
  private final FeedLinkReference fFeedReference;
  private final NewsReference fNewsReference;
  private final Date fRecentNewsDate;
  private boolean fIsNewsSticky;
  private boolean fIsNewsRead;
  private String fCachedDescriptionExcerpt;
  private String fCachedOrigin;
  private ImageDescriptor fImage;
  private RGB fColor;

  /**
   * @param news The news that is to be displayed in the Notifier.
   */
  public NewsNotificationItem(INews news) {
    this(news, null);
  }

  /**
   * @param news The news that is to be displayed in the Notifier.
   * @param color The color for the news in the Notifier or <code>null</code> if
   * none.
   */
  public NewsNotificationItem(INews news, RGB color) {
    super(makeText(news), OwlUI.BOOKMARK); //We resolve the real favicon later lazily

    fNewsLink = CoreUtils.getLink(news);
    fFeedReference = news.getFeedReference();
    fNewsReference = new NewsReference(news.getId());
    fRecentNewsDate = DateUtils.getRecentDate(news);
    fIsNewsSticky = news.isFlagged();
    fIsNewsRead = (INews.State.READ == news.getState());

    if (color != null)
      fColor = color;
    else {
      Set<ILabel> labels = CoreUtils.getSortedLabels(news);
      if (!labels.isEmpty())
        fColor = OwlUI.getRGB(labels.iterator().next());
    }
  }

  private String extractDescriptionExcerpt(INews news) {
    if (news == null)
      return null;

    String description = news.getDescription();
    if (!StringUtils.isSet(description))
      return null;

    String content = StringUtils.stripTags(description, true);
    content = StringUtils.normalizeString(content);
    content = StringUtils.smartTrim(content, MAX_DESCRIPTION_LENGTH);

    if (content.contains("&")) //$NON-NLS-1$
      content = StringUtils.replaceAll(content, "&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$

    return content.length() > 0 ? content : null;
  }

  private static ImageDescriptor makeImage(FeedLinkReference feedReference) {
    IBookMark bookMark = CoreUtils.getBookMark(feedReference);
    if (bookMark != null) {
      ImageDescriptor favicon = OwlUI.getFavicon(bookMark);
      if (favicon != null)
        return favicon;
    }

    return OwlUI.BOOKMARK;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#getImage()
   */
  @Override
  public ImageDescriptor getImage() {
    if (fImage == null)
      fImage = makeImage(fFeedReference);

    return fImage;
  }

  private static String makeText(INews news) {
    String headline = CoreUtils.getHeadline(news, true);
    if (headline.contains("&")) //$NON-NLS-1$
      headline = StringUtils.replaceAll(headline, "&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$

    return headline;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#getDescription()
   */
  @Override
  public String getDescription() {
    if (fCachedDescriptionExcerpt == null)
      fCachedDescriptionExcerpt = extractDescriptionExcerpt(fNewsReference.resolve());

    return fCachedDescriptionExcerpt;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#setColor(org.eclipse.swt.graphics.RGB)
   */
  @Override
  public void setColor(RGB color) {
    fColor = color;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#getColor()
   */
  @Override
  public RGB getColor() {
    return fColor;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#getOrigin()
   */
  @Override
  public String getOrigin() {
    if (fCachedOrigin == null) {
      IBookMark bookMark = CoreUtils.getBookMark(fFeedReference);
      if (bookMark != null)
        fCachedOrigin = bookMark.getName();
    }

    return fCachedOrigin;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#open()
   */
  @Override
  public void open(MouseEvent e) {

    /* Open Link in Browser if Modifier Key is pressed */
    if ((e.stateMask & SWT.MOD1) != 0) {
      new OpenInBrowserAction(new StructuredSelection(fNewsLink)).run();
    }

    /* Open Link in Feed View */
    else {
      INews news = fNewsReference.resolve();
      if (news != null) {
        OpenNewsAction action = new OpenNewsAction(new StructuredSelection(news));
        action.setRestoreWindow(true);
        action.setPreferActiveFeedView();
        action.run();
      }
    }
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#supportsSticky()
   */
  @Override
  public boolean supportsSticky() {
    return true;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#isSticky()
   */
  @Override
  public boolean isSticky() {
    return fIsNewsSticky;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#makeSticky(boolean)
   */
  @Override
  public void setSticky(boolean sticky) {
    fIsNewsSticky = sticky;

    INews news = fNewsReference.resolve();
    if (news != null && news.isVisible()) {
      news.setFlagged(sticky);
      DynamicDAO.save(news);
    }
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#supportsMarkRead()
   */
  @Override
  public boolean supportsMarkRead() {
    return true;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#setRead(boolean)
   */
  @Override
  public void setRead(boolean read) {
    fIsNewsRead = read;

    INews news = fNewsReference.resolve();
    if (news != null && news.isVisible()) {
      news.setState(read ? INews.State.READ : INews.State.NEW);
      DynamicDAO.save(news);
    }
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#isRead()
   */
  @Override
  public boolean isRead() {
    return fIsNewsRead;
  }

  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(NotificationItem o) {

    /* Compare with other News Item */
    if (o instanceof NewsNotificationItem) {

      /* Return 0 if the Items Equal */
      if (equals(o))
        return 0;

      /* Compare by Date */
      Date date1 = fRecentNewsDate;
      Date date2 = ((NewsNotificationItem) o).fRecentNewsDate;

      int res = date2.compareTo(date1);
      return (res != 0) ? res : -1;
    }

    /* Otherwise sort to Bottom */
    return 1;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fNewsReference == null) ? 0 : fNewsReference.hashCode());
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (getClass() != obj.getClass())
      return false;

    NewsNotificationItem other = (NewsNotificationItem) obj;
    if (fNewsReference == null) {
      if (other.fNewsReference != null)
        return false;
    } else if (!fNewsReference.equals(other.fNewsReference))
      return false;

    return true;
  }
}
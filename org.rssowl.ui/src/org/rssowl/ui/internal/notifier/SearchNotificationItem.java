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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.util.EditorUtils;

import java.util.EnumSet;

/**
 * Instance of {@link NotificationItem} to display new search results from a
 * {@link ISearchMark} in the Notifier.
 *
 * @author bpasero
 */
public class SearchNotificationItem extends NotificationItem {
  private final ISearchMark fSearchmark;
  private final int fTotalResultCount;

  /**
   * @param searchmark the saved search containing new search results.
   * @param unreadResultCount the number of unread results for the saved search.
   */
  public SearchNotificationItem(ISearchMark searchmark, int unreadResultCount) {
    super(makeText(searchmark, unreadResultCount), OwlUI.SEARCHMARK);
    fSearchmark = searchmark;
    fTotalResultCount = fSearchmark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.READ, INews.State.UPDATED));
  }

  private static String makeText(ISearchMark searchmark, int unreadResultCount) {
    return NLS.bind(Messages.SearchNotificationItem_NAME_UNREAD_COUNT, searchmark.getName(), unreadResultCount);
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#open()
   */
  @Override
  public void open(MouseEvent e) {

    /* Open Feed View with the Saved Search as input */
    IWorkbenchPage page = OwlUI.getPage();
    if (page != null) {

      /* Restore Window */
      OwlUI.restoreWindow(page);

      /* First try if the Search is already visible */
      IEditorReference editorRef = EditorUtils.findEditor(page.getEditorReferences(), fSearchmark);
      if (editorRef != null) {
        IEditorPart editor = editorRef.getEditor(false);
        if (editor instanceof FeedView)
          page.activate(editor);
      }

      /* Otherwise Open */
      else
        OwlUI.openInFeedView(page, new StructuredSelection(fSearchmark));
    }
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#makeSticky(boolean)
   */
  @Override
  public void setSticky(boolean sticky) {}

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#isSticky()
   */
  @Override
  public boolean isSticky() {
    return false;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#supportsSticky()
   */
  @Override
  public boolean supportsSticky() {
    return false;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#setRead(boolean)
   */
  @Override
  public void setRead(boolean read) {}

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#setColor(org.eclipse.swt.graphics.RGB)
   */
  @Override
  public void setColor(RGB color) {}

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#isRead()
   */
  @Override
  public boolean isRead() {
    return false;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#supportsMarkRead()
   */
  @Override
  public boolean supportsMarkRead() {
    return false;
  }

  /*
   * @see org.rssowl.ui.internal.notifier.NotificationItem#getDescription()
   */
  @Override
  public String getDescription() {
    return NLS.bind(Messages.SearchNotificationItem_NEW_RESULTS, fTotalResultCount);
  }

  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(NotificationItem o) {

    /* Compare with other Search Item */
    if (o instanceof SearchNotificationItem) {

      /* Return 0 if the Items are Equal */
      if (equals(o))
        return 0;

      /* Compare by Name */
      int res = getText().compareTo(o.getText());
      return (res != 0) ? res : -1;
    }

    /* Otherwise sort to top */
    return -1;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fSearchmark == null) ? 0 : fSearchmark.hashCode());
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

    SearchNotificationItem other = (SearchNotificationItem) obj;
    if (fSearchmark == null) {
      if (other.fSearchmark != null)
        return false;
    } else if (!fSearchmark.equals(other.fSearchmark))
      return false;

    return true;
  }
}
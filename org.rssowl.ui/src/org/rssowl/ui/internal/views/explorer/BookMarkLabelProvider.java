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

package org.rssowl.ui.internal.views.explorer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.OwlUI;

import java.util.EnumSet;
import java.util.List;

/**
 * @author bpasero
 */
public class BookMarkLabelProvider extends CellLabelProvider {

  /* Resource Manager */
  private LocalResourceManager fResources;

  /* Commonly used Resources */
  private Image fFolderIcon;
  private Image fFolderNewIcon;
  private Image fBookMarkErrorIcon;
  private Image fBookMarkIcon;
  private Image fSearchMarkIcon;
  private Image fSearchMarkNewIcon;
  private Image fSearchMarkEmptyIcon;
  private Image fGroupIcon;
  private Image fBookmarkSetIcon;
  private Image fArchiveIcon;
  private Image fArchiveNewIcon;
  private Image fNewsBinIcon;
  private Image fNewsBinNewIcon;
  private Image fNewsBinEmptyIcon;
  private Color fStickyBgColor;
  private Color fGradientFgColor;
  private Color fGradientBgColor;
  private Color fGradientEndColor;
  private Color fGroupFgColor;
  private Font fBoldFont;
  private Font fDefaultFont;

  /* Settings */
  private boolean fIndicateState;
  private final boolean fUseDialogFont;
  private boolean fUseFavicons = true;

  /* Helper to compute a Folders Decoration */
  private static class FolderDecorationHelper {
    int fNewCount;
    int fUnreadCount;
    boolean fHasSticky;
  }

  /** Default Constructor */
  public BookMarkLabelProvider() {
    this(true);
  }

  /**
   * @param indicateState
   */
  public BookMarkLabelProvider(boolean indicateState) {
    this(indicateState, false);
  }

  /**
   * @param indicateState
   * @param useDialogFont
   */
  public BookMarkLabelProvider(boolean indicateState, boolean useDialogFont) {
    fIndicateState = indicateState;
    fUseDialogFont = useDialogFont;
    fResources = new LocalResourceManager(JFaceResources.getResources());
    createResources();
  }

  void updateResources() {

    /* Sticky Color */
    fStickyBgColor = OwlUI.getThemeColor(OwlUI.STICKY_BG_COLOR_ID, fResources, new RGB(255, 255, 180));
  }

  void setUseFavicons(boolean useFavicons) {
    fUseFavicons = useFavicons;
  }

  private void createResources() {

    /* Images */
    fBookmarkSetIcon = OwlUI.getImage(fResources, OwlUI.BOOKMARK_SET);
    fGroupIcon = OwlUI.getImage(fResources, OwlUI.GROUP);
    fFolderIcon = OwlUI.getImage(fResources, OwlUI.FOLDER);
    fFolderNewIcon = OwlUI.getImage(fResources, OwlUI.FOLDER_NEW);
    fBookMarkErrorIcon = OwlUI.getImage(fResources, OwlUI.BOOKMARK_ERROR);
    fBookMarkIcon = OwlUI.getImage(fResources, OwlUI.BOOKMARK);
    fSearchMarkIcon = OwlUI.getImage(fResources, OwlUI.SEARCHMARK);
    fSearchMarkNewIcon = OwlUI.getImage(fResources, OwlUI.SEARCHMARK_NEW);
    fSearchMarkEmptyIcon = OwlUI.getImage(fResources, OwlUI.SEARCHMARK_EMPTY);
    fArchiveIcon = OwlUI.getImage(fResources, OwlUI.ARCHIVE);
    fArchiveNewIcon = OwlUI.getImage(fResources, OwlUI.ARCHIVE_NEW);
    fNewsBinIcon = OwlUI.getImage(fResources, OwlUI.NEWSBIN);
    fNewsBinNewIcon = OwlUI.getImage(fResources, OwlUI.NEWSBIN_NEW);
    fNewsBinEmptyIcon = OwlUI.getImage(fResources, OwlUI.NEWSBIN_EMPTY);

    /* Fonts */
    fBoldFont = OwlUI.getThemeFont(fUseDialogFont ? OwlUI.DIALOG_FONT_ID : OwlUI.BKMRK_EXPLORER_FONT_ID, SWT.BOLD);
    fDefaultFont = OwlUI.getThemeFont(fUseDialogFont ? OwlUI.DIALOG_FONT_ID : OwlUI.BKMRK_EXPLORER_FONT_ID, SWT.NORMAL);

    /* Colors */
    fStickyBgColor = OwlUI.getThemeColor(OwlUI.STICKY_BG_COLOR_ID, fResources, new RGB(255, 255, 180));
    fGroupFgColor = OwlUI.getColor(fResources, OwlUI.GROUP_FG_COLOR);
    fGradientFgColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_FG_COLOR);
    fGradientBgColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_BG_COLOR);
    fGradientEndColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_END_COLOR);
  }

  /*
   * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  public void update(ViewerCell cell) {
    Object element = cell.getElement();
    int unreadNewsCount = 0;

    /* Create Label for a Folder */
    if (element instanceof IFolder) {
      IFolder folder = (IFolder) element;
      int newNewsCount = 0;
      boolean hasSticky = false;

      if (fIndicateState) {
        FolderDecorationHelper helper = new FolderDecorationHelper();
        computeFolderDecoration(folder, helper);

        hasSticky = helper.fHasSticky;
        unreadNewsCount = helper.fUnreadCount;
        newNewsCount = helper.fNewCount;
      }

      /* Image */
      if (folder.getParent() == null)
        cell.setImage(fBookmarkSetIcon);
      else if (newNewsCount == 0)
        cell.setImage(fFolderIcon);
      else
        cell.setImage(fFolderNewIcon);

      /* Font */
      if (unreadNewsCount > 0)
        cell.setFont(fBoldFont);
      else
        cell.setFont(fDefaultFont);

      /* Text */
      if (unreadNewsCount > 0)
        cell.setText(NLS.bind(Messages.BookMarkLabelProvider_NAME_UNREAD, folder.getName(), unreadNewsCount));
      else
        cell.setText(folder.getName());

      /* Reset Foreground */
      cell.setForeground(null);

      /* Background Color */
      if (hasSticky && !OwlUI.isHighContrast())
        cell.setBackground(fStickyBgColor);
      else
        cell.setBackground(null);
    }

    /* Create generic Label for instances of INewsMark */
    else if (element instanceof INewsMark) {
      boolean hasNew = false;
      int stickyNewsCount = 0;
      INewsMark newsmark = (INewsMark) element;

      if (fIndicateState) {
        unreadNewsCount = newsmark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
        hasNew = newsmark.getNewsCount(EnumSet.of(INews.State.NEW)) != 0;
      }

      /* Font */
      if (unreadNewsCount > 0)
        cell.setFont(fBoldFont);
      else
        cell.setFont(fDefaultFont);

      /* Text */
      if (unreadNewsCount > 0)
        cell.setText(NLS.bind(Messages.BookMarkLabelProvider_NAME_UNREAD, newsmark.getName(), unreadNewsCount));
      else
        cell.setText(newsmark.getName());

      /* Background for IBookMark (TODO Support All News Marks) */
      if (newsmark instanceof IBookMark && fIndicateState)
        stickyNewsCount = ((IBookMark) newsmark).getStickyNewsCount();

      /* Background Color */
      if (stickyNewsCount > 0 && !OwlUI.isHighContrast())
        cell.setBackground(fStickyBgColor);
      else
        cell.setBackground(null);

      /* Reset Foreground */
      cell.setForeground(null);

      /* Icon */
      if (newsmark instanceof IBookMark)
        cell.setImage(getIconForBookMark((IBookMark) newsmark, hasNew));
      else if (newsmark instanceof ISearchMark)
        cell.setImage(getIconForSearchMark((ISearchMark) newsmark, hasNew, unreadNewsCount));
      else if (newsmark instanceof INewsBin)
        cell.setImage(getIconForNewsBin((INewsBin) newsmark, hasNew, unreadNewsCount));
    }

    /* Create Label for EntityGroup */
    else if (element instanceof EntityGroup) {
      EntityGroup group = (EntityGroup) element;

      /* Text */
      cell.setText(group.getName());

      /* Image */
      cell.setImage(fGroupIcon);

      /* Foreground */
      if (!OwlUI.isHighContrast())
        cell.setForeground(fGroupFgColor);

      /* Reset Background */
      cell.setBackground(null);

      /* Font */
      cell.setFont(fBoldFont);
    }
  }

  private Image getIconForBookMark(IBookMark bookmark, boolean hasNew) {

    /* Load the FavIcon (if enabled) */
    ImageDescriptor favicon = fUseFavicons ? OwlUI.getFavicon(bookmark) : null;

    /* Indicate Error */
    if (bookmark.isErrorLoading()) {

      /* Overlay with Error Icon if required */
      if (favicon != null) {
        Image faviconImg = OwlUI.getImage(fResources, favicon);
        DecorationOverlayIcon overlay = new DecorationOverlayIcon(faviconImg, OwlUI.getImageDescriptor("icons/ovr16/error.gif"), IDecoration.BOTTOM_RIGHT); //$NON-NLS-1$
        return OwlUI.getImage(fResources, overlay);
      }

      /* Default Error Icon */
      return fBookMarkErrorIcon;
    }

    /* Use normal Icon */
    Image icon = favicon != null ? OwlUI.getImage(fResources, favicon) : fBookMarkIcon;

    /* Overlay if News are *new* */
    if (hasNew) {
      DecorationOverlayIcon overlay = new DecorationOverlayIcon(icon, OwlUI.getImageDescriptor("icons/ovr16/new.gif"), IDecoration.BOTTOM_RIGHT); //$NON-NLS-1$
      return OwlUI.getImage(fResources, overlay);
    }

    /* Don't overlay */
    return icon;
  }

  private Image getIconForSearchMark(ISearchMark searchmark, boolean hasNew, int unreadNewsCount) {
    boolean hasMatchingNews = unreadNewsCount > 0 || searchmark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED, INews.State.READ)) != 0;

    if (hasNew)
      return fSearchMarkNewIcon;
    else if (hasMatchingNews)
      return fSearchMarkIcon;
    else
      return fSearchMarkEmptyIcon;
  }

  private Image getIconForNewsBin(INewsBin newsbin, boolean hasNew, int unreadNewsCount) {
    boolean hasMatchingNews = unreadNewsCount > 0 || newsbin.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED, INews.State.READ)) != 0;
    boolean isArchive = newsbin.getProperty(DefaultPreferences.ARCHIVE_BIN_MARKER) != null;

    if (hasNew)
      return isArchive ? fArchiveNewIcon : fNewsBinNewIcon;
    else if (isArchive)
      return fArchiveIcon;
    else if (hasMatchingNews)
      return fNewsBinIcon;
    else
      return fNewsBinEmptyIcon;
  }

  /*
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  @Override
  public void dispose() {
    fResources.dispose();
  }

  /*
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  private void computeFolderDecoration(IFolder folder, FolderDecorationHelper helper) {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {

      /* News Mark */
      if (child instanceof INewsMark) {
        INewsMark newsMark = (INewsMark) child;
        helper.fUnreadCount += getUnreadNewsCount(newsMark);
        helper.fNewCount += newsMark.getNewsCount(EnumSet.of(INews.State.NEW));

        if (!helper.fHasSticky && newsMark instanceof IBookMark && ((IBookMark) newsMark).getStickyNewsCount() > 0)
          helper.fHasSticky = true;
      }

      /* Folder */
      else if (child instanceof IFolder)
        computeFolderDecoration((IFolder) child, helper);
    }
  }

  private int getUnreadNewsCount(INewsMark newsMark) {
    return newsMark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));
  }

  void erase(Event event, Object element) {
    if (element instanceof EntityGroup)
      OwlUI.codDrawGradient(event, fGradientFgColor, fGradientBgColor, fGradientEndColor);
  }
}
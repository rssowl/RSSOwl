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

package org.rssowl.ui.internal.editors.feed;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.OwlUI;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ismael Juma (ismael@juma.me.uk)
 * @author bpasero
 */
public class NewsTableLabelProvider extends OwnerDrawLabelProvider {

  /** News Column Model to use */
  protected NewsColumnViewModel fColumnModel;

  /* Some Colors of a Label */
  private static final String LABEL_COLOR_BLACK = "0,0,0"; //$NON-NLS-1$
  private static final String LABEL_COLOR_WHITE = "255,255,255"; //$NON-NLS-1$

  /** Resource Manager to use */
  protected LocalResourceManager fResources;

  /* Date Formatter for News */
  private final DateFormat fDateFormat = OwlUI.getShortDateFormat();

  /* Time Formatter for News */
  private DateFormat fTimeFormat = OwlUI.getShortTimeFormat();

  /* Pre-Cache some Colors being used */
  private Color fStickyBgColor;
  private Color fGradientFgColor;
  private Color fGradientBgColor;
  private Color fGradientEndColor;
  private Color fGroupFgColor;
  private Color fGroupBgColor;
  private Color fNewsBgGradientStartColor;
  private Color fNewsBgGradientEndColor;
  private RGB fListBackground;
  private RGB fListSelectionBackground;

  /* Pre-Cache some Images being used */
  private Image fNewsUnreadIcon;
  private Image fNewsNewIcon;
  private Image fNewsUpdatedIcon;
  private Image fNewsReadIcon;
  private Image fNewsStickyIcon;
  private Image fNewsNonStickyIcon;
  private Image fGroupIcon;

  /* Pre-Cache some Fonts being used */
  private Font fBoldFont;

  /* A cache for the Feed and Location Column */
  private Map<Long, String> fMapBinIdToLocation = new HashMap<Long, String>();
  private Map<String, String> fMapFeedLinkToLocation = new HashMap<String, String>();
  private Map<String, ImageDescriptor> fMapFeedLinkToFeedIcon = new HashMap<String, ImageDescriptor>();

  /* Today in Millies */
  private final long fTodayInMillies;

  /**
   * Creates a new instance of this LabelProvider
   *
   * @param model the column model.
   */
  public NewsTableLabelProvider(NewsColumnViewModel model) {
    fColumnModel = model;
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fTodayInMillies = DateUtils.getToday().getTimeInMillis();

    createResources();
  }

  /*
   * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
   */
  @Override
  protected void initialize(ColumnViewer viewer, ViewerColumn column) {
    super.initialize(viewer, column, false); //Disable Custom Ownerdrawn
  }

  /**
   * @param model
   */
  public void init(NewsColumnViewModel model) {
    fColumnModel = model;
  }

  void updateResources() {

    /* Sticky Color */
    fStickyBgColor = OwlUI.getThemeColor(OwlUI.STICKY_BG_COLOR_ID, fResources, new RGB(255, 255, 180));

    /* News Background Color */
    createNewsListBackgroundResources();
  }

  private void createResources() {

    /* Colors */
    fStickyBgColor = OwlUI.getThemeColor(OwlUI.STICKY_BG_COLOR_ID, fResources, new RGB(255, 255, 180));
    fGradientFgColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_FG_COLOR);
    fGradientBgColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_BG_COLOR);
    fGradientEndColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_END_COLOR);
    fGroupFgColor = OwlUI.getColor(fResources, OwlUI.GROUP_FG_COLOR);
    fGroupBgColor = OwlUI.getColor(fResources, OwlUI.GROUP_BG_COLOR);

    fListBackground = fResources.getDevice().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
    fListSelectionBackground = fResources.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();

    createNewsListBackgroundResources();

    /* Icons */
    fNewsUnreadIcon = OwlUI.getImage(fResources, OwlUI.NEWS_STATE_UNREAD);
    fNewsNewIcon = OwlUI.getImage(fResources, OwlUI.NEWS_STATE_NEW);
    fNewsUpdatedIcon = OwlUI.getImage(fResources, OwlUI.NEWS_STATE_UPDATED);
    fNewsReadIcon = OwlUI.getImage(fResources, OwlUI.NEWS_STATE_READ);
    fNewsStickyIcon = OwlUI.getImage(fResources, OwlUI.NEWS_PINNED);
    fNewsNonStickyIcon = OwlUI.getImage(fResources, OwlUI.NEWS_PIN);
    fGroupIcon = OwlUI.getImage(fResources, OwlUI.GROUP);

    /* Fonts */
    fBoldFont = OwlUI.getThemeFont(OwlUI.HEADLINES_FONT_ID, SWT.BOLD);
  }

  private void createNewsListBackgroundResources() {
    fNewsBgGradientStartColor = null;
    fNewsBgGradientEndColor = null;

    RGB listBackgroundRGB = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
    RGB newsBgColorRGB = OwlUI.getThemeRGB(OwlUI.NEWS_LIST_BG_COLOR_ID, listBackgroundRGB);
    if (newsBgColorRGB != null && !listBackgroundRGB.equals(newsBgColorRGB) && !isInvalidColor(newsBgColorRGB)) {
      fNewsBgGradientEndColor = OwlUI.getColor(fResources, newsBgColorRGB);

      RGB newsBgColorRGBLighter = new RGB(0, 0, 0);
      newsBgColorRGBLighter.red = Math.min(newsBgColorRGB.red + 5, 255);
      newsBgColorRGBLighter.green = Math.min(newsBgColorRGB.green + 5, 255);
      newsBgColorRGBLighter.blue = Math.min(newsBgColorRGB.blue + 5, 255);
      fNewsBgGradientStartColor = OwlUI.getColor(fResources, newsBgColorRGBLighter);
    }
  }

  /*
   * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  public void update(ViewerCell cell) {
    NewsColumn column = fColumnModel.getColumn(cell.getColumnIndex());

    /* Text */
    cell.setText(getColumnText(cell.getElement(), column, cell.getColumnIndex()));

    /* Image */
    cell.setImage(getColumnImage(cell.getElement(), column, cell.getColumnIndex()));

    /* Font */
    cell.setFont(getFont(cell.getElement(), cell.getColumnIndex()));

    /* Foreground */
    Color foreground = getForeground(cell.getElement(), cell.getColumnIndex());

    /* This is required to invalidate + redraw the entire TableItem! */
    if (!OwlUI.isHighContrast()) {
      Item item = (Item) cell.getItem();
      if (item instanceof TreeItem)
        ((TreeItem) cell.getItem()).setForeground(foreground);
      else if (item instanceof TableItem)
        ((TableItem) cell.getItem()).setForeground(foreground);
    }

    /* Background */
    if (!OwlUI.isHighContrast())
      cell.setBackground(getBackground(cell.getElement(), cell.getColumnIndex()));
  }

  /*
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
   */
  @Override
  public String getToolTipText(Object element) {

    /* News */
    if (element instanceof INews) {
      INews news = (INews) element;
      String feedRef = news.getFeedLinkAsText();
      IBookMark bookMark = CoreUtils.getBookMark(feedRef);

      String name = null;
      if (bookMark != null)
        name = bookMark.getName();
      else
        name = feedRef;

      if (news.getParentId() != 0) {
        INewsBin bin = DynamicDAO.load(INewsBin.class, news.getParentId());
        if (bin != null) {
          name = NLS.bind(Messages.NewsTableLabelProvider_BIN_NAME, bin.getName(), name);
        }
      }

      return StringUtils.replaceAll(name, "&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /* Entity Group */
    else if (element instanceof EntityGroup) {
      return StringUtils.replaceAll(((EntityGroup) element).getName(), "&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return super.getToolTipText(element);
  }

  /*
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipShift(java.lang.Object)
   */
  @Override
  public Point getToolTipShift(Object object) {
    if (Application.IS_WINDOWS)
      return new Point(0, 21);

    return super.getToolTipShift(object);
  }

  /**
   * @param element
   * @param column
   * @param colIndex
   * @return String
   */
  protected String getColumnText(Object element, NewsColumn column, int colIndex) {
    String text = null;

    /* Handle News */
    if (element instanceof INews) {
      INews news = (INews) element;

      switch (column) {
        case TITLE:
          text = CoreUtils.getHeadline(news, true);
          break;

        case DATE:
          Date date = DateUtils.getRecentDate(news);
          text = DateUtils.isAfterIncludingToday(date, fTodayInMillies) ? fTimeFormat.format(date) : fDateFormat.format(date);
          break;

        case PUBLISHED:
          Date published = news.getPublishDate();
          if (published != null)
            text = DateUtils.isAfterIncludingToday(published, fTodayInMillies) ? fTimeFormat.format(published) : fDateFormat.format(published);
          break;

        case MODIFIED:
          Date modified = news.getModifiedDate();
          if (modified != null)
            text = DateUtils.isAfterIncludingToday(modified, fTodayInMillies) ? fTimeFormat.format(modified) : fDateFormat.format(modified);
          break;

        case RECEIVED:
          Date received = news.getReceiveDate();
          if (received != null)
            text = DateUtils.isAfterIncludingToday(received, fTodayInMillies) ? fTimeFormat.format(received) : fDateFormat.format(received);
          break;

        case AUTHOR:
          IPerson author = news.getAuthor();
          if (author != null) {
            if (author.getName() != null)
              text = author.getName();
            else if (author.getEmail() != null)
              text = author.getEmail().toString();
          }
          break;

        case CATEGORY:
          List<ICategory> categories = news.getCategories();
          if (!categories.isEmpty()) {
            StringBuilder str = new StringBuilder();
            for (ICategory category : categories) {
              if (StringUtils.isSet(category.getName()))
                str.append(category.getName().trim()).append(", "); //$NON-NLS-1$
              else if (StringUtils.isSet(category.getDomain()))
                str.append(category.getDomain().trim()).append(", "); //$NON-NLS-1$
            }

            if (str.length() > 0)
              str = str.delete(str.length() - 2, str.length());
            text = str.toString();
          }
          break;

        case LABELS:
          Set<ILabel> labels = CoreUtils.getSortedLabels(news);
          if (!labels.isEmpty()) {
            StringBuilder str = new StringBuilder();
            for (ILabel label : labels) {
              str.append(label.getName()).append(", "); //$NON-NLS-1$
            }

            if (str.length() > 0)
              str = str.delete(str.length() - 2, str.length());
            text = str.toString();
          }
          break;

        case STATUS:
          State state = news.getState();
          if (state == State.NEW)
            text = Messages.NewsTableLabelProvider_NEW;
          else if (state == State.UNREAD)
            text = Messages.NewsTableLabelProvider_UNREAD;
          else if (state == State.UPDATED)
            text = Messages.NewsTableLabelProvider_UPDATED;
          else if (state == State.READ)
            text = Messages.NewsTableLabelProvider_READ;

          break;

        case LOCATION:

          /* Location: Bin */
          if (news.getParentId() > 0) {
            String location = fMapBinIdToLocation.get(news.getParentId());
            if (location == null) {
              NewsBinReference ref = new NewsBinReference(news.getParentId());
              INewsBin bin = ref.resolve();
              location = bin.getName();
              fMapBinIdToLocation.put(news.getParentId(), location);
            }

            text = location;
          }

          /* Location: Bookmark */
          else {
            String location = fMapFeedLinkToLocation.get(news.getFeedLinkAsText());
            if (location == null) {
              IBookMark bookmark = CoreUtils.getBookMark(news.getFeedLinkAsText());
              if (bookmark != null) {
                location = bookmark.getName();
                fMapFeedLinkToLocation.put(news.getFeedLinkAsText(), location);
              }
            }

            text = location;
          }
          break;

        case LINK:
          text = CoreUtils.getLink(news);
          if (StringUtils.isSet(text)) {
            text = StringUtils.replaceAll(text, URIUtils.HTTP, ""); //$NON-NLS-1$
            text = StringUtils.replaceAll(text, "www.", ""); //$NON-NLS-1$ //$NON-NLS-2$
          }
      }
    }

    /* Handle EntityGroup */
    else if (element instanceof EntityGroup && column == NewsColumn.TITLE)
      text = ((EntityGroup) element).getName();

    /* Make sure to normalize the Text for the Table */
    return text != null ? StringUtils.normalizeString(text) : null;
  }

  /**
   * @param element
   * @param newsColumn
   * @param colIndex
   * @return Image
   */
  protected Image getColumnImage(Object element, NewsColumn newsColumn, int colIndex) {

    /* News */
    if (element instanceof INews) {
      INews news = (INews) element;

      /* News Icon */
      if (newsColumn == NewsColumn.TITLE) {
        if (news.getState() == INews.State.UNREAD)
          return fNewsUnreadIcon;
        else if (news.getState() == INews.State.NEW)
          return fNewsNewIcon;
        else if (news.getState() == INews.State.UPDATED)
          return fNewsUpdatedIcon;
        else if (news.getState() == INews.State.READ)
          return fNewsReadIcon;
      }

      /* Feed Column */
      else if (newsColumn == NewsColumn.FEED) {
        String feedRef = news.getFeedLinkAsText();
        ImageDescriptor feedIcon = fMapFeedLinkToFeedIcon.get(feedRef);
        if (feedIcon == null) {
          IBookMark bookMark = CoreUtils.getBookMark(feedRef);
          if (bookMark != null)
            feedIcon = OwlUI.getFavicon(bookMark);

          if (feedIcon == null)
            feedIcon = OwlUI.BOOKMARK;

          fMapFeedLinkToFeedIcon.put(feedRef, feedIcon);
        }

        return OwlUI.getImage(fResources, feedIcon);
      }

      /* Sticky State */
      else if (newsColumn == NewsColumn.STICKY) {
        if (news.isFlagged())
          return fNewsStickyIcon;

        return fNewsNonStickyIcon;
      }

      /* Attachment */
      else if (newsColumn == NewsColumn.ATTACHMENTS) {
        List<IAttachment> attachments = news.getAttachments();
        if (!attachments.isEmpty())
          return OwlUI.getImage(fResources, OwlUI.ATTACHMENT);
      }
    }

    /* EntityGroup Image */
    else if (element instanceof EntityGroup && newsColumn == NewsColumn.TITLE) {
      EntityGroup group = (EntityGroup) element;
      if (group.getImage() != null)
        return OwlUI.getImage(fResources, group.getImage());

      return fGroupIcon;
    }

    return null;
  }

  /**
   * @param element
   * @param columnIndex
   * @return Font
   */
  protected Font getFont(Object element, int columnIndex) {

    /* Use a Bold Font for Unread News */
    if (element instanceof INews) {
      INews news = (INews) element;
      INews.State state = news.getState();
      if (state == null)
        return null;

      /* Bold for New, Updated and Unread News */
      if (state == INews.State.NEW || state == INews.State.UPDATED || state == INews.State.UNREAD)
        return fBoldFont;
    }

    /* Use Bold Font for EntityGroup */
    if (element instanceof EntityGroup)
      return fBoldFont;

    return null;
  }

  /**
   * @param element
   * @param columnIndex
   * @return Color
   */
  protected Color getBackground(Object element, int columnIndex) {

    /* Handle INews */
    if (element instanceof INews && ((INews) element).isFlagged())
      return fStickyBgColor;

    /* Handle EntityGroup */
    else if (element instanceof EntityGroup)
      return fGroupBgColor;

    return null;
  }

  /**
   * @param element
   * @param columnIndex
   * @return Color
   */
  protected Color getForeground(Object element, int columnIndex) {

    /* Handle EntityGroup */
    if (element instanceof EntityGroup) {
      EntityGroup group = (EntityGroup) element;
      if (group.getColorHint() != null) {
        if (!fListBackground.equals(group.getColorHint()) && !fListSelectionBackground.equals(group.getColorHint()))
          return OwlUI.getColor(fResources, group.getColorHint());
      }

      return fGroupFgColor;
    }

    /* Handle INews */
    else if (element instanceof INews) {
      Set<ILabel> labels = CoreUtils.getSortedLabels((INews) element);
      if (!labels.isEmpty()) {
        RGB labelRGB = OwlUI.getRGB(labels.iterator().next());
        if (!fListBackground.equals(labelRGB) && !fListSelectionBackground.equals(labelRGB))
          return OwlUI.getColor(fResources, labelRGB);
      }
    }

    return null;
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

  /*
   * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#erase(org.eclipse.swt.widgets.Event,
   * java.lang.Object)
   */
  @Override
  public void erase(Event event, Object element) {

    /* Erase News */
    if (element instanceof INews)
      eraseNews(event, (INews) element);

    /* Erase Group */
    else if (element instanceof EntityGroup)
      eraseGroup(event, (EntityGroup) element);

  }

  private void eraseGroup(Event event, EntityGroup group) {
    Scrollable scrollable = (Scrollable) event.widget;
    GC gc = event.gc;

    /* Draw Color if Selected */
    if (group.getColorHint() != null && (event.detail & SWT.SELECTED) != 0) {

      /* Some conditions under which we don't override the selection color */
      if (!scrollable.isFocusControl() || isInvalidColor(group.getColorHint()))
        return;

      Rectangle clArea = scrollable.getClientArea();
      Rectangle itemRect = event.getBounds();

      /* Paint the selection beyond the end of last column */
      OwlUI.codExpandRegion(event, scrollable, gc, clArea);

      /* Draw Rectangle */
      Color oldBackground = gc.getBackground();
      gc.setBackground(OwlUI.getColor(fResources, group.getColorHint()));
      gc.fillRectangle(0, itemRect.y, clArea.width, itemRect.height);
      gc.setBackground(oldBackground);
      gc.setForeground(scrollable.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

      /* Mark as Selected being handled */
      event.detail &= ~SWT.SELECTED;
    }

    /* Draw Gradient */
    else
      OwlUI.codDrawGradient(event, fGradientFgColor, fGradientBgColor, fGradientEndColor);
  }

  private void eraseNews(Event event, INews news) {
    Scrollable scrollable = (Scrollable) event.widget;
    GC gc = event.gc;

    /* Handle selected News (Linux: Note Bug 444) */
    if ((event.detail & SWT.SELECTED) != 0 && (Application.IS_WINDOWS || !news.isFlagged())) {

      /* Do not override selection color if not focus control */
      if (!scrollable.isFocusControl())
        return;

      /* Load Labels */
      Set<ILabel> labels = CoreUtils.getSortedLabels(news);
      if (labels.isEmpty())
        return;

      ILabel label = labels.iterator().next();
      if (isInvalidColor(label))
        return;

      Rectangle clArea = scrollable.getClientArea();
      Rectangle itemRect = event.getBounds();

      /* Paint the selection beyond the end of last column */
      OwlUI.codExpandRegion(event, scrollable, gc, clArea);

      /* Draw Rectangle */
      Color oldBackground = gc.getBackground();
      gc.setBackground(OwlUI.getColor(fResources, label));
      gc.fillRectangle(0, itemRect.y, clArea.width, itemRect.height);
      gc.setBackground(oldBackground);
      gc.setForeground(scrollable.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

      /* Mark as Selected being handled */
      event.detail &= ~SWT.SELECTED;
    }

    /* Handle Non-Selected flagged News */
    else if (news.isFlagged()) {
      Rectangle clArea = scrollable.getClientArea();
      Rectangle itemRect = event.getBounds();

      /* Paint the selection beyond the end of last column */
      OwlUI.codExpandRegion(event, scrollable, gc, clArea);

      /* Draw Rectangle */
      Color oldBackground = gc.getBackground();
      gc.setBackground(fStickyBgColor);
      gc.fillRectangle(0, itemRect.y, clArea.width, itemRect.height);
      gc.setBackground(oldBackground);

      /* Mark as Background being handled */
      event.detail &= ~SWT.BACKGROUND;
    }

    /* Handle News List Background Color if set */
    else if (fNewsBgGradientStartColor != null && fNewsBgGradientEndColor != null) {
      int index = 0;

      /* Tree */
      if (event.item instanceof TreeItem) {
        TreeItem item = (TreeItem) event.item;
        TreeItem parentItem = item.getParentItem();
        if (parentItem != null)
          index = parentItem.indexOf(item);
        else
          index = item.getParent().indexOf(item);
      }

      /* Table */
      else if (event.item instanceof TableItem) {
        TableItem item = (TableItem) event.item;
        index = item.getParent().indexOf(item);
      }

      if (index % 2 != 0)
        OwlUI.codDrawGradient(event, fNewsBgGradientStartColor, fNewsBgGradientEndColor, fNewsBgGradientEndColor);
    }
  }

  private boolean isInvalidColor(ILabel label) {
    return label.getColor().equals(LABEL_COLOR_BLACK) || label.getColor().equals(LABEL_COLOR_WHITE);
  }

  private boolean isInvalidColor(RGB color) {
    if (color.blue == 0 && color.red == 0 && color.green == 0)
      return true;

    if (color.blue == 255 && color.red == 255 && color.green == 255)
      return true;

    return false;
  }

  /*
   * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event,
   * java.lang.Object)
   */
  @Override
  protected void measure(Event event, Object element) {
    /* Ignore */
  }

  /*
   * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#paint(org.eclipse.swt.widgets.Event,
   * java.lang.Object)
   */
  @Override
  protected void paint(Event event, Object element) {
    /* Ignore */
  }
}
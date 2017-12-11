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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.notifier.NotificationService.Mode;
import org.rssowl.ui.internal.util.CCLabel;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Todo:
 * <ul>
 * <li>SearchNotificationItems are not aggregated if the max. number of items is
 * already showing</li>
 * </ul>
 *
 * @author bpasero
 */
public class NotificationPopup extends PopupDialog {

  /* Height of a CLabel with 16px Icon inside */
  private static final int CLABEL_HEIGHT = 22;

  /* Max. Number of Items being displayed in the Popup */
  private static final int MAX_ITEMS = 30;

  /* Default Width of the Popup */
  private static final int DEFAULT_WIDTH = 420;

  /* Milliseconds before incrementing the alpha for fade in / fade out */
  private static final int FADE_DELAY = 40;

  /* Alpha increment for fade in */
  private static final int FADE_IN_INCREMENT = 20;

  /* A field that is set to true in case fading is not supported on the OS */
  private static boolean fgFadeSupported = true;

  /* System Property to disable Transparency for the Notifier */
  private static final String DISABLE_TRANSPARENCY = "disableNotifierTransparency"; //$NON-NLS-1$
  private static final boolean FADE_ENABLED = System.getProperty(DISABLE_TRANSPARENCY) == null;

  private Shell fShell;
  private final List<NotificationItem> fDisplayedItems = new ArrayList<NotificationItem>();
  private final ResourceManager fResources;
  private Image fCloseImageNormal;
  private Image fCloseImageHot;
  private Image fCloseImagePressed;
  private CLabel fTitleCircleLabel;
  private CLabel fFooterCircleLabel;
  private Composite fInnerContentCircle;
  private Composite fOuterContentCircle;
  private final Font fNormalTextFont;
  private final Font fBoldTextFont;
  private UIJob fAutoCloser;
  private MouseTrackListener fMouseTrackListner;
  private final IPreferenceScope fGlobalScope;
  private final int fVisibleItemsCount;
  private int fItemLimit;
  private NotifierColors fNotifierColors;
  private Region fLastUsedRegion;
  private Image fMarkReadIcon;
  private Image fMarkReadDisabledIcon;
  private Image fItemStickyIcon;
  private Image fItemNonStickyIcon;
  private Image fItemNonStickyDisabledIcon;
  private Color fStickyBgColor;
  private Image fPrevImageNormal;
  private Image fPrevImagePressed;
  private Image fPrevImageDisabled;
  private Image fNextImageNormal;
  private Image fNextImagePressed;
  private Image fNextImageDisabled;
  private Image fTitleBgImage;
  private Image fFooterBgImage;
  private CLabel fNextButton;
  private CLabel fPrevButton;
  private int fDisplayOffset;
  private boolean fMouseOverNotifier;
  private FadeJob fFadeJob;
  private Collection<NotificationItem> fInitialItems;
  private final Mode fMode;

  private class FadeJob extends UIJob {
    FadeJob() {
      super(""); //$NON-NLS-1$
      setSystem(true);
    }

    private boolean proceed(IProgressMonitor monitor) {
      Shell shell = getShell();
      if (monitor.isCanceled() || shell == null || shell.isDisposed() || shell.getDisplay().isDisposed() || Controller.getDefault().isShuttingDown())
        return false;

      return true;
    }

    @Override
    public IStatus runInUIThread(final IProgressMonitor monitor) {
      if (proceed(monitor)) {
        final int alpha = getShell().getAlpha();

        if (shouldFadeIn() && alpha < 255) {
          if (proceed(monitor)) {
            getShell().getDisplay().syncExec(new Runnable() {
              @Override
              public void run() {
                if (proceed(monitor)) {
                  int newAlpha = Math.min(alpha + FADE_IN_INCREMENT, 255);
                  getShell().setAlpha(newAlpha);

                  if (newAlpha != getShell().getAlpha())
                    fgFadeSupported = false;
                }
              }
            });
            schedule(FADE_DELAY);
          }
        }
      }

      return Status.OK_STATUS;
    }
  };

  NotificationPopup(int visibleItemCount, Mode mode) {
    super(new Shell(PlatformUI.getWorkbench().getDisplay()), SWT.NO_TRIM | SWT.ON_TOP, mode != Mode.INCOMING_AUTOMATIC, false, false, false, false, null, null);
    fMode = mode;
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fBoldTextFont = OwlUI.getThemeFont(OwlUI.NOTIFICATION_POPUP_FONT_ID, SWT.BOLD);
    fNormalTextFont = OwlUI.getThemeFont(OwlUI.NOTIFICATION_POPUP_FONT_ID, SWT.NORMAL);
    fGlobalScope = Owl.getPreferenceService().getGlobalScope();

    fItemLimit = fGlobalScope.getInteger(DefaultPreferences.LIMIT_NOTIFICATION_SIZE);
    if (fItemLimit <= 0)
      fItemLimit = MAX_ITEMS;

    fVisibleItemsCount = (visibleItemCount > fItemLimit) ? fItemLimit : visibleItemCount;
    createAutoCloser();
    createMouseTrackListener();

    initResources();
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    return SWT.NO_TRIM | SWT.ON_TOP;
  }

  /*
   * @see org.eclipse.jface.window.Window#create()
   */
  @Override
  public void create() {
    super.create();

    /* Show initial set of items */
    makeVisible(fInitialItems);

    /* Make shell invisible initially if fading in */
    if (shouldFadeIn() && fGlobalScope.getBoolean(DefaultPreferences.FADE_NOTIFIER))
      getShell().setAlpha(0);
  }

  /**
   * @param items the initial items to show inside the window.
   * @return the status code from opening the window.
   */
  public int open(Collection<NotificationItem> items) {
    Assert.isLegal(items != null && !items.isEmpty());
    fInitialItems = items;
    int res = super.open();

    /* Make shell fade in */
    if (shouldFadeIn() && fGlobalScope.getBoolean(DefaultPreferences.FADE_NOTIFIER))
      fadeIn();

    return res;
  }

  private void fadeIn() {
    if (fFadeJob != null)
      fFadeJob.cancel();

    fFadeJob = new FadeJob();
    fFadeJob.schedule();
  }

  private void addRegion(Shell shell) {
    Region region = new Region();
    Point s = shell.getSize();

    /* Add entire Shell */
    region.add(0, 0, s.x, s.y);

    /* Subtract Top-Left Corner */
    region.subtract(0, 0, 5, 1);
    region.subtract(0, 1, 3, 1);
    region.subtract(0, 2, 2, 1);
    region.subtract(0, 3, 1, 1);
    region.subtract(0, 4, 1, 1);

    /* Subtract Top-Right Corner */
    region.subtract(s.x - 5, 0, 5, 1);
    region.subtract(s.x - 3, 1, 3, 1);
    region.subtract(s.x - 2, 2, 2, 1);
    region.subtract(s.x - 1, 3, 1, 1);
    region.subtract(s.x - 1, 4, 1, 1);

    /* Subtract Bottom-Left Corner */
    region.subtract(0, s.y, 5, 1);
    region.subtract(0, s.y - 1, 3, 1);
    region.subtract(0, s.y - 2, 2, 1);
    region.subtract(0, s.y - 3, 1, 1);
    region.subtract(0, s.y - 4, 1, 1);

    /* Subtract Bottom-Right Corner */
    region.subtract(s.x - 5, 0, 5, 1);
    region.subtract(s.x - 3, 1, 3, 1);
    region.subtract(s.x - 2, 2, 2, 1);
    region.subtract(s.x - 1, 3, 1, 1);
    region.subtract(s.x - 1, 4, 1, 1);

    /* Dispose old first */
    if (shell.getRegion() != null)
      shell.getRegion().dispose();

    /* Apply Region */
    shell.setRegion(region);

    /* Remember to dispose later */
    fLastUsedRegion = region;
  }

  private void createAutoCloser() {
    fAutoCloser = new UIJob(PlatformUI.getWorkbench().getDisplay(), "") { //$NON-NLS-1$
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        if (!fMouseOverNotifier && getShell() != null && !getShell().isDisposed() && !monitor.isCanceled())
          doClose();

        return Status.OK_STATUS;
      }
    };

    fAutoCloser.setSystem(true);
  }

  /* Listener to control Auto-Close Job */
  private void createMouseTrackListener() {
    fMouseTrackListner = new MouseTrackAdapter() {
      @Override
      public void mouseEnter(MouseEvent e) {
        fMouseOverNotifier = true;

        if (!fGlobalScope.getBoolean(DefaultPreferences.STICKY_NOTIFICATION_POPUP))
          fAutoCloser.cancel();

        if (shouldFadeIn() && fGlobalScope.getBoolean(DefaultPreferences.FADE_NOTIFIER) && getShell().getAlpha() < 255) {
          if (fFadeJob != null)
            fFadeJob.cancel();
          getShell().setAlpha(255);
        }
      }

      @Override
      public void mouseExit(MouseEvent e) {
        fMouseOverNotifier = false;

        if (!fGlobalScope.getBoolean(DefaultPreferences.STICKY_NOTIFICATION_POPUP))
          fAutoCloser.schedule(fGlobalScope.getInteger(DefaultPreferences.AUTOCLOSE_NOTIFICATION_VALUE) * 1000);
      }
    };
  }

  void makeVisible(Collection<NotificationItem> items) {

    /* Cancel Auto Closer and reschedule */
    if (!fGlobalScope.getBoolean(DefaultPreferences.STICKY_NOTIFICATION_POPUP)) {
      fAutoCloser.cancel();
      fAutoCloser.schedule(fGlobalScope.getInteger(DefaultPreferences.AUTOCLOSE_NOTIFICATION_VALUE) * 1000);
    }

    /* Add / Update Notification Items to display */
    for (NotificationItem item : items) {
      int indexOfItem = fDisplayedItems.indexOf(item);

      /* Replace existing Item */
      if (indexOfItem >= 0) {
        NotificationItem existingItem = fDisplayedItems.get(indexOfItem);
        if (existingItem.getColor() != null && item.getColor() == null)
          item.setColor(existingItem.getColor());

        fDisplayedItems.set(indexOfItem, item);
      }

      /* Add new Item to the end */
      else
        fDisplayedItems.add(item);
    }

    /* Update Title Label */
    updateTitleLabel();

    /* Update Contents */
    updateContents(fDisplayOffset);
  }

  private void updateTitleLabel() {
    int totalPages = (fDisplayedItems.size() / fItemLimit) + ((fDisplayedItems.size() % fItemLimit != 0) ? 1 : 0);
    int currentPage = (fDisplayOffset / fItemLimit) + 1;

    /* Count News Notification Items and Search Notification Items */
    int newsCount = 0;
    int searchCount = 0;
    for (NotificationItem item : fDisplayedItems) {
      if (item instanceof NewsNotificationItem)
        newsCount++;
      else
        searchCount++;
    }

    String titlePart;
    if (fMode == Mode.RECENT)
      titlePart = Messages.NotificationPopup_RECENT_NEWS;
    else if (fMode == Mode.INCOMING_MANUAL)
      titlePart = Messages.NotificationPopup_INCOMING_NEWS;
    else if (newsCount == 0 && searchCount > 0)
      titlePart = (searchCount == 1) ? NLS.bind(Messages.NotificationPopup_N_SEARCH_MATCH, searchCount) : NLS.bind(Messages.NotificationPopup_N_SEARCH_MATCHES, searchCount);
    else
      titlePart = NLS.bind(Messages.NotificationPopup_N_INCOMING, newsCount);

    String footerPart = ""; //$NON-NLS-1$

    /* More than one page */
    if (totalPages > 1)
      footerPart = NLS.bind(Messages.NotificationPopup_PAGE_N_OF_M, currentPage, totalPages);

    /* Apply Text */
    fTitleCircleLabel.setText(titlePart);
    fFooterCircleLabel.setText(footerPart);
  }

  private void updateContents(int offset) {

    /* Dispose old Items first */
    Control[] children = fInnerContentCircle.getChildren();
    for (Control child : children) {
      child.dispose();
    }

    /* Show Items */
    for (int i = offset; i < offset + fItemLimit && i < fDisplayedItems.size(); i++)
      renderItem(fDisplayedItems.get(i));

    /* Layout */
    fOuterContentCircle.layout(true, true);

    /* Update Shell Bounds */
    Point oldSize = fShell.getSize();
    int newHeight = fShell.computeSize(DEFAULT_WIDTH, SWT.DEFAULT).y;
    newHeight = Math.max(oldSize.y, newHeight);

    Point newSize = new Point(oldSize.x, newHeight);
    Point newLocation = getInitialLocation(newSize);
    fShell.setBounds(newLocation.x, newLocation.y, newSize.x, newSize.y);

    /* Add Region to Shell */
    addRegion(fShell);

    /* Update Nav Buttons */
    updateNavButtons();
  }

  private void updateItemColor(NotificationItem item, CCLabel itemLabel) {
    if (!OwlUI.isHighContrast()) {
      if (item.getColor() == null)
        itemLabel.setForeground(itemLabel.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      else
        itemLabel.setForeground(OwlUI.getColor(fResources, item.getColor()));
    }
  }

  private void renderItem(final NotificationItem item) {

    /* Image Label with Tooltip */
    final CLabel imageLabel = new CLabel(fInnerContentCircle, SWT.NONE);
    imageLabel.setBackground(fInnerContentCircle.getBackground());
    imageLabel.setCursor(imageLabel.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    imageLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    imageLabel.addMouseTrackListener(fMouseTrackListner);
    imageLabel.setImage(OwlUI.getImage(fResources, item.getImage()));
    imageLabel.setToolTipText(item.getOrigin());

    /* Use a CCLabel per Item */
    final CCLabel itemLabel = new CCLabel(fInnerContentCircle, SWT.NONE);
    itemLabel.setBackground(fInnerContentCircle.getBackground());
    itemLabel.setCursor(itemLabel.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    itemLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    itemLabel.setText(item.getText());
    itemLabel.setFont(item.isRead() ? fNormalTextFont : fBoldTextFont);
    updateItemColor(item, itemLabel);
    itemLabel.addMouseTrackListener(fMouseTrackListner);

    /* Paint text blue on mouse-enter */
    MouseTrackAdapter mouseTrackListener = new MouseTrackAdapter() {

      @Override
      public void mouseEnter(MouseEvent e) {
        if (!OwlUI.isHighContrast())
          itemLabel.setForeground(itemLabel.getDisplay().getSystemColor(SWT.COLOR_BLUE));
      }

      @Override
      public void mouseExit(MouseEvent e) {
        updateItemColor(item, itemLabel);
      }
    };

    itemLabel.addMouseTrackListener(mouseTrackListener);
    imageLabel.addMouseTrackListener(mouseTrackListener);

    /* Clicked on item to open it */
    MouseAdapter mouseListener = new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {

        /* Close Popup if required after opening */
        if (fGlobalScope.getBoolean(DefaultPreferences.CLOSE_NOTIFIER_ON_OPEN)) {
          item.open(e);
          doClose();
        }

        /* Indicate the item is marked as read now and open */
        else {
          item.setRead(true);
          itemLabel.setFont(fNormalTextFont);
          item.open(e);
        }
      }
    };

    itemLabel.addMouseListener(mouseListener);
    imageLabel.addMouseListener(mouseListener);

    /* Offer Label to mark item read */
    CLabel markReadLabel = new CLabel(fInnerContentCircle, SWT.NONE);
    markReadLabel.setImage(item.supportsMarkRead() ? fMarkReadIcon : fMarkReadDisabledIcon);
    markReadLabel.setBackground(fInnerContentCircle.getBackground());
    markReadLabel.addMouseTrackListener(fMouseTrackListner);
    markReadLabel.setEnabled(item.supportsMarkRead());
    markReadLabel.setCursor(fShell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    markReadLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        boolean newStateRead = !item.isRead();

        /* Update Font */
        itemLabel.setFont(newStateRead ? fNormalTextFont : fBoldTextFont);

        /* Apply state */
        item.setRead(newStateRead);
      }
    });

    /* Offer Label to mark item sticky */
    final CLabel markStickyLabel = new CLabel(fInnerContentCircle, SWT.NONE);
    markStickyLabel.setImage(item.supportsSticky() ? fItemNonStickyIcon : fItemNonStickyDisabledIcon);
    markStickyLabel.setBackground(fInnerContentCircle.getBackground());
    markStickyLabel.setEnabled(item.supportsSticky());
    markStickyLabel.addMouseTrackListener(fMouseTrackListner);
    markStickyLabel.setCursor(fShell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    markStickyLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        boolean newStateSticky = !item.isSticky();

        /* Update Background Color */
        if (!OwlUI.isHighContrast())
          itemLabel.setRoundRectangleBackground(newStateSticky ? fStickyBgColor : null);

        /* Update Image */
        markStickyLabel.setImage(newStateSticky ? fItemStickyIcon : fItemNonStickyIcon);

        /* Apply state */
        item.setSticky(newStateSticky);
      }
    });

    /* Show Sticky if required */
    if (item.supportsSticky() && item.isSticky()) {
      if (!OwlUI.isHighContrast())
        itemLabel.setRoundRectangleBackground(fStickyBgColor);
      markStickyLabel.setImage(fItemStickyIcon);
    }

    /* Show excerpt of content if set */
    if (fGlobalScope.getBoolean(DefaultPreferences.SHOW_EXCERPT_IN_NOTIFIER)) {
      String description = item.getDescription();
      if (!StringUtils.isSet(description))
        description = Messages.NotificationPopup_NO_CONTENT;

      final Composite descriptionContainer = new Composite(fInnerContentCircle, SWT.NONE);
      descriptionContainer.setLayout(LayoutUtils.createGridLayout(1));
      ((GridLayout) descriptionContainer.getLayout()).marginBottom = 5;
      descriptionContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
      descriptionContainer.addMouseTrackListener(fMouseTrackListner);
      descriptionContainer.setBackground(fInnerContentCircle.getBackground());
      descriptionContainer.addPaintListener(new PaintListener() {
        @Override
        public void paintControl(PaintEvent e) {
          GC gc = e.gc;
          Rectangle clArea = descriptionContainer.getClientArea();

          gc.setForeground(fNotifierColors.getBorder());
          gc.setBackground(fNotifierColors.getGradientEnd());
          gc.fillGradientRectangle(4, 1, clArea.width, 1, false);
        }
      });

      Label descriptionText = new Label(descriptionContainer, SWT.WRAP);
      descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      descriptionText.setFont(fNormalTextFont);
      descriptionText.setBackground(fInnerContentCircle.getBackground());
      descriptionText.setText(description);
      descriptionText.addMouseTrackListener(fMouseTrackListner);
    }
  }

  private void initResources() {

    /* Colors */
    fNotifierColors = new NotifierColors(getParentShell().getDisplay(), fResources);
    fStickyBgColor = OwlUI.getThemeColor(OwlUI.STICKY_BG_COLOR_ID, fResources, new RGB(255, 255, 180));

    /* Icons */
    fCloseImageNormal = OwlUI.getImage(fResources, "icons/etool16/close_normal.png"); //$NON-NLS-1$
    fCloseImageHot = OwlUI.getImage(fResources, "icons/etool16/close_hot.png"); //$NON-NLS-1$
    fCloseImagePressed = OwlUI.getImage(fResources, "icons/etool16/close_pressed.png"); //$NON-NLS-1$
    fMarkReadIcon = OwlUI.getImage(fResources, "icons/elcl16/mark_read_light16.gif"); //$NON-NLS-1$
    fMarkReadDisabledIcon = OwlUI.getImage(fResources, "icons/dlcl16/mark_read.gif"); //$NON-NLS-1$
    fItemStickyIcon = OwlUI.getImage(fResources, "icons/obj16/news_pinned_light16.gif"); //$NON-NLS-1$
    fItemNonStickyIcon = OwlUI.getImage(fResources, "icons/obj16/news_pin_light16.gif"); //$NON-NLS-1$
    fItemNonStickyDisabledIcon = OwlUI.getImage(fResources, "icons/obj16/news_pin_disabled.gif"); //$NON-NLS-1$
    fPrevImageNormal = OwlUI.getImage(fResources, "icons/etool16/prev_normal.png"); //$NON-NLS-1$
    fPrevImagePressed = OwlUI.getImage(fResources, "icons/etool16/prev_pressed.png"); //$NON-NLS-1$
    fPrevImageDisabled = OwlUI.getImage(fResources, "icons/etool16/prev_disabled.png"); //$NON-NLS-1$
    fNextImageNormal = OwlUI.getImage(fResources, "icons/etool16/next_normal.png"); //$NON-NLS-1$
    fNextImagePressed = OwlUI.getImage(fResources, "icons/etool16/next_pressed.png"); //$NON-NLS-1$
    fNextImageDisabled = OwlUI.getImage(fResources, "icons/etool16/next_disabled.png"); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.jface.dialogs.PopupDialog#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    fShell = parent.getShell();
    parent.setBackground(fNotifierColors.getBorder());

    return createDialogArea(parent);
  }

  /*
   * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    ((GridLayout) parent.getLayout()).marginWidth = 1;
    ((GridLayout) parent.getLayout()).marginHeight = 1;

    /* Outer Compositing holding the controlls */
    final Composite outerCircle = new Composite(parent, SWT.NO_FOCUS);
    outerCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    outerCircle.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0));

    /* Title area containing label and close button */
    final Composite titleCircle = new Composite(outerCircle, SWT.NO_FOCUS);
    titleCircle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    titleCircle.setBackgroundMode(SWT.INHERIT_FORCE);
    titleCircle.setLayout(LayoutUtils.createGridLayout(2, 3, 0, 5, 3, false));
    titleCircle.addMouseTrackListener(fMouseTrackListner);
    titleCircle.addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        Rectangle clArea = titleCircle.getClientArea();
        Image oldBgImage = fTitleBgImage;
        fTitleBgImage = new Image(titleCircle.getDisplay(), clArea.width, clArea.height);
        GC gc = new GC(fTitleBgImage);

        /* Gradient */
        drawGradient(gc, clArea);

        /* Fix Region Shape */
        fixRegion(gc, clArea);

        gc.dispose();

        titleCircle.setBackgroundImage(fTitleBgImage);

        if (oldBgImage != null)
          oldBgImage.dispose();
      }

      private void drawGradient(GC gc, Rectangle clArea) {
        gc.setForeground(fNotifierColors.getGradientBegin());
        gc.setBackground(fNotifierColors.getGradientEnd());
        gc.fillGradientRectangle(clArea.x, clArea.y, clArea.width, clArea.height, true);
      }

      private void fixRegion(GC gc, Rectangle clArea) {
        gc.setForeground(fNotifierColors.getBorder());

        /* Fill Top Left */
        gc.drawPoint(2, 0);
        gc.drawPoint(3, 0);
        gc.drawPoint(1, 1);
        gc.drawPoint(0, 2);
        gc.drawPoint(0, 3);

        /* Fill Top Right */
        gc.drawPoint(clArea.width - 4, 0);
        gc.drawPoint(clArea.width - 3, 0);
        gc.drawPoint(clArea.width - 2, 1);
        gc.drawPoint(clArea.width - 1, 2);
        gc.drawPoint(clArea.width - 1, 3);
      }
    });

    /* Title Label displaying RSSOwl */
    fTitleCircleLabel = new CLabel(titleCircle, SWT.NO_FOCUS);
    fTitleCircleLabel.setImage(OwlUI.getImage(fResources, "icons/product/24x24.png")); //$NON-NLS-1$
    fTitleCircleLabel.setText("RSSOwl"); //$NON-NLS-1$
    fTitleCircleLabel.setFont(fBoldTextFont);
    fTitleCircleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    fTitleCircleLabel.addMouseTrackListener(fMouseTrackListner);
    fTitleCircleLabel.setCursor(fShell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    fTitleCircleLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        doRestore();
      }
    });

    /* CLabel to display a cross to close the popup */
    final CLabel closeButton = new CLabel(titleCircle, SWT.NO_FOCUS);
    closeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    closeButton.setImage(fCloseImageNormal);
    closeButton.setCursor(fShell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    closeButton.addMouseTrackListener(fMouseTrackListner);
    closeButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        doClose();
      }

      @Override
      public void mouseDown(MouseEvent e) {
        closeButton.setImage(fCloseImagePressed);
      }
    });

    closeButton.addMouseTrackListener(new MouseTrackAdapter() {
      @Override
      public void mouseEnter(MouseEvent e) {
        closeButton.setImage(fCloseImageHot);
      }

      @Override
      public void mouseExit(MouseEvent e) {
        closeButton.setImage(fCloseImageNormal);
      }
    });

    /* Outer composite to hold content controlls */
    fOuterContentCircle = new Composite(outerCircle, SWT.NONE);
    fOuterContentCircle.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    fOuterContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fOuterContentCircle.setBackground(outerCircle.getBackground());

    /* Middle composite to show a 1px black line around the content controlls */
    Composite middleContentCircle = new Composite(fOuterContentCircle, SWT.NO_FOCUS);
    middleContentCircle.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    ((GridLayout) middleContentCircle.getLayout()).marginTop = 1;
    ((GridLayout) middleContentCircle.getLayout()).marginBottom = 1;
    middleContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    middleContentCircle.setBackground(fNotifierColors.getBorder());

    /* Inner composite containing the content controlls */
    fInnerContentCircle = new Composite(middleContentCircle, SWT.NO_FOCUS);
    fInnerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fInnerContentCircle.setLayout(LayoutUtils.createGridLayout(4, 0, 5, 0, 0, false));
    ((GridLayout) fInnerContentCircle.getLayout()).marginLeft = 5;
    ((GridLayout) fInnerContentCircle.getLayout()).marginRight = 2;
    fInnerContentCircle.addMouseTrackListener(fMouseTrackListner);
    fInnerContentCircle.setBackground(fShell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    /* Footer Area containing navigational controls */
    final Composite footerCircle = new Composite(outerCircle, SWT.NO_FOCUS);
    footerCircle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    footerCircle.setBackgroundMode(SWT.INHERIT_FORCE);
    footerCircle.setLayout(LayoutUtils.createGridLayout(3, 3, 0, 5, 3, false));
    footerCircle.addMouseTrackListener(fMouseTrackListner);
    footerCircle.addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        Rectangle clArea = footerCircle.getClientArea();
        Image oldBgImage = fFooterBgImage;
        fFooterBgImage = new Image(footerCircle.getDisplay(), clArea.width, clArea.height);
        GC gc = new GC(fFooterBgImage);

        /* Gradient */
        drawGradient(gc, clArea);

        /* Fix Region Shape */
        fixRegion(gc, clArea);

        gc.dispose();

        footerCircle.setBackgroundImage(fFooterBgImage);

        if (oldBgImage != null)
          oldBgImage.dispose();
      }

      private void drawGradient(GC gc, Rectangle clArea) {
        gc.setBackground(fNotifierColors.getGradientBegin());
        gc.setForeground(fNotifierColors.getGradientEnd());
        gc.fillGradientRectangle(clArea.x, clArea.y, clArea.width, clArea.height, true);
      }

      private void fixRegion(GC gc, Rectangle clArea) {
        gc.setForeground(fNotifierColors.getBorder());

        /* Fill Bottom Left */
        gc.drawPoint(2, clArea.height - 0);
        gc.drawPoint(3, clArea.height - 0);
        gc.drawPoint(1, clArea.height - 1);
        gc.drawPoint(0, clArea.height - 2);
        gc.drawPoint(0, clArea.height - 3);

        /* Fill Bottom Right */
        gc.drawPoint(clArea.width - 4, clArea.height - 0);
        gc.drawPoint(clArea.width - 3, clArea.height - 0);
        gc.drawPoint(clArea.width - 2, clArea.height - 1);
        gc.drawPoint(clArea.width - 1, clArea.height - 2);
        gc.drawPoint(clArea.width - 1, clArea.height - 3);
      }
    });

    /* Title Label displaying RSSOwl */
    fFooterCircleLabel = new CLabel(footerCircle, SWT.NO_FOCUS);
    fFooterCircleLabel.setFont(fBoldTextFont);
    fFooterCircleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

    /* Nav to previous News */
    fPrevButton = new CLabel(footerCircle, SWT.NO_FOCUS);
    fPrevButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));
    fPrevButton.setCursor(fShell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    fPrevButton.addMouseTrackListener(fMouseTrackListner);
    fPrevButton.setImage(fPrevImageDisabled);
    fPrevButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        onNavPrevious();
      }

      @Override
      public void mouseDown(MouseEvent e) {
        if (canNavPrevious())
          fPrevButton.setImage(fPrevImagePressed);
      }
    });

    /* Nav to next News */
    fNextButton = new CLabel(footerCircle, fMode == Mode.INCOMING_AUTOMATIC ? SWT.NO_FOCUS : SWT.NONE);
    fNextButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
    fNextButton.addMouseTrackListener(fMouseTrackListner);
    fNextButton.setCursor(fShell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    fNextButton.setImage(fNextImageDisabled);
    fNextButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        onNavNext();
      }

      @Override
      public void mouseDown(MouseEvent e) {
        if (canNavNext())
          fNextButton.setImage(fNextImagePressed);
      }
    });

    /* Simple Keybinding Support */
    KeyAdapter keyListener = new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        handleUserAction();

        if (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.PAGE_UP)
          onNavPrevious();
        else if (e.keyCode == SWT.ARROW_RIGHT || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.PAGE_DOWN)
          onNavNext();
        else if (e.keyCode == SWT.HOME)
          onNavFirstPage();
        else if (e.keyCode == SWT.END)
          onNavLastPage();
        else if (e.keyCode == ' ')
          onNavNextAndMarkRead();
        else if (e.keyCode == SWT.CR)
          doRestore();
        else if (e.keyCode == SWT.ESC)
          doClose();
      }
    };

    if (fMode == Mode.INCOMING_AUTOMATIC)
      fShell.addKeyListener(keyListener);
    else
      fNextButton.addKeyListener(keyListener);

    /* Allow to navigate with Mouse Wheel Too */
    fShell.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseScrolled(MouseEvent e) {
        handleUserAction();

        if (e.count < 0)
          onNavNext();
        else if (e.count > 0)
          onNavPrevious();
      }
    });

    /* Close Shell once deactivated and not incoming automatic */
    fShell.addListener(SWT.Deactivate, new Listener() {
      @Override
      public void handleEvent(Event event) {
        if (fMode != Mode.INCOMING_AUTOMATIC && fGlobalScope.getBoolean(DefaultPreferences.CLOSE_NOTIFIER_ON_OPEN))
          doClose();
      }
    });

    return outerCircle;
  }

  private void handleUserAction() {
    if (fMouseOverNotifier || fGlobalScope.getBoolean(DefaultPreferences.STICKY_NOTIFICATION_POPUP))
      return;

    fAutoCloser.cancel();
    fAutoCloser.schedule(fGlobalScope.getInteger(DefaultPreferences.AUTOCLOSE_NOTIFICATION_VALUE) * 1000);
  }

  /*
   * @see org.eclipse.jface.dialogs.PopupDialog#getFocusControl()
   */
  @Override
  protected Control getFocusControl() {
    return fNextButton;
  }

  private void onNavFirstPage() {
    int newOffset = 0;
    fDisplayOffset = newOffset;
    updateTitleLabel();
    updateContents(newOffset);
  }

  private void onNavLastPage() {
    int newOffset;
    if (fDisplayedItems.size() % fItemLimit == 0)
      newOffset = fDisplayedItems.size() - fItemLimit;
    else
      newOffset = fDisplayedItems.size() - (fDisplayedItems.size() % fItemLimit);

    if (newOffset >= 0) {
      fDisplayOffset = newOffset;
      updateTitleLabel();
      updateContents(newOffset);
    }
  }

  private boolean onNavPrevious() {
    int newOffset = fDisplayOffset - fItemLimit;
    if (newOffset >= 0) {
      fDisplayOffset = newOffset;
      updateTitleLabel();
      updateContents(newOffset);

      return true;
    }

    return false;
  }

  private boolean onNavNext() {
    int newOffset = fDisplayOffset + fItemLimit;
    if (newOffset < fDisplayedItems.size()) {
      fDisplayOffset = newOffset;
      updateTitleLabel();
      updateContents(newOffset);

      return true;
    }

    return false;
  }

  private void onNavNextAndMarkRead() {
    for (int i = fDisplayOffset; i < fDisplayOffset + fItemLimit && i < fDisplayedItems.size(); i++) {
      NotificationItem item = fDisplayedItems.get(i);
      item.setRead(true);
    }

    boolean navigated = onNavNext();
    if (!navigated)
      doClose();
  }

  private void updateNavButtons() {
    boolean isPrevEnabled = canNavPrevious();
    boolean isNextEnabled = canNavNext();

    fPrevButton.setImage(isPrevEnabled ? fPrevImageNormal : fPrevImageDisabled);
    fNextButton.setImage(isNextEnabled ? fNextImageNormal : fNextImageDisabled);
  }

  private boolean canNavNext() {
    return fDisplayOffset + fItemLimit < fDisplayedItems.size();
  }

  private boolean canNavPrevious() {
    return fDisplayOffset - fItemLimit >= 0;
  }

  /*
   * @see org.eclipse.jface.dialogs.PopupDialog#getInitialLocation(org.eclipse.swt.graphics.Point)
   */
  @Override
  protected Point getInitialLocation(Point initialSize) {
    Rectangle clArea = getPrimaryClientArea();

    return new Point(clArea.width + clArea.x - initialSize.x, clArea.height + clArea.y - initialSize.y);
  }

  /*
   * @see org.eclipse.jface.dialogs.PopupDialog#getInitialSize()
   */
  @Override
  protected Point getInitialSize() {
    int initialHeight = fShell.computeSize(DEFAULT_WIDTH, SWT.DEFAULT).y;

    return new Point(DEFAULT_WIDTH, initialHeight + fVisibleItemsCount * CLABEL_HEIGHT);
  }

  /**
   * Get the Client Area of the primary Monitor.
   *
   * @return Returns the Client Area of the primary Monitor.
   */
  private Rectangle getPrimaryClientArea() {
    Monitor primaryMonitor = fShell.getDisplay().getPrimaryMonitor();
    return (primaryMonitor != null) ? primaryMonitor.getClientArea() : fShell.getDisplay().getClientArea();
  }

  /*
   * @see org.eclipse.jface.dialogs.PopupDialog#close()
   */
  @Override
  public boolean close() {

    /*
     * This method is overriden because the parent PopupDialog class automatically
     * closes the popup when the Shell is deactivated.
     * We want to leave the popup open in this case though for stability reasons.
     */
    return false;
  }

  /**
   * The actual implementation of close().
   *
   * @return <code>true</code> if the window is (or was already) closed, and
   * <code>false</code> if it is still open
   */
  protected boolean doClose() {
    fResources.dispose();
    if (fLastUsedRegion != null)
      fLastUsedRegion.dispose();
    if (fTitleBgImage != null)
      fTitleBgImage.dispose();
    if (fFooterBgImage != null)
      fFooterBgImage.dispose();
    if (fInitialItems != null)
      fInitialItems.clear();

    return super.close();
  }

  /* Restore RSSOwl and Close the Notifier */
  private void doRestore() {
    IWorkbenchPage page = OwlUI.getPage();
    if (page != null) {

      /* Restore Window */
      OwlUI.restoreWindow(page);

      /* Close Notifier */
      doClose();
    }
  }

  private boolean shouldFadeIn() {
    return fMode == Mode.INCOMING_AUTOMATIC && FADE_ENABLED && fgFadeSupported;
  }
}
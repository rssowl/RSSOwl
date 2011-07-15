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

package org.rssowl.ui.internal.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.interpreter.InterpreterException;
import org.rssowl.core.interpreter.ParserException;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.OpenInBrowserAction;
import org.rssowl.ui.internal.editors.feed.NewsBrowserLabelProvider;
import org.rssowl.ui.internal.util.CBrowser;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.UIBackgroundJob;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The {@link PreviewFeedDialog} can be used to preview a {@link IBookMark}s
 * contents without adding it to the list of subscriptions.
 *
 * @author bpasero
 */
public class PreviewFeedDialog extends Dialog {
  private static final int DIALOG_WIDTH_DLUS = 600;
  private static final int DIALOG_HEIGHT_DLUS = 400;
  private static final String DIALOG_SETTINGS_KEY = "org.rssowl.ui.internal.dialogs.PreviewFeedDialog"; //$NON-NLS-1$

  private static final int MAX_NEWS_SHOWN = 50;

  private IBookMark fBookmark;
  private FeedLinkReference fFeedReference;
  private IFeed fLoadedFeed;
  private CBrowser fBrowser;
  private boolean fFirstTimeOpen;
  private NewsBrowserLabelProvider fLabelProvider;
  private String fNewsFontFamily;
  private String fNormalFontCSS;
  private Link fStatusLabel;

  /**
   * @param parentShell
   * @param bookmark
   */
  public PreviewFeedDialog(Shell parentShell, IBookMark bookmark) {
    this(parentShell, bookmark, null, null);
  }

  /**
   * @param parentShell
   * @param bookmark
   * @param feedReference
   */
  public PreviewFeedDialog(Shell parentShell, IBookMark bookmark, FeedLinkReference feedReference) {
    this(parentShell, bookmark, null, feedReference);
  }

  /**
   * @param parentShell
   * @param bookmark
   * @param feed
   */
  public PreviewFeedDialog(Shell parentShell, IBookMark bookmark, IFeed feed) {
    this(parentShell, bookmark, feed, null);
  }

  PreviewFeedDialog(Shell parentShell, IBookMark bookmark, IFeed feed, FeedLinkReference feedReference) {
    super(parentShell);
    fBookmark = bookmark;
    fLoadedFeed = feed;
    fFeedReference = feedReference;
    fFirstTimeOpen = (Activator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_KEY) == null);
    createFonts();
  }

  /* Init the Theme Font (from UI Thread) */
  private void createFonts() {
    int fontHeight = 10;
    Font newsFont = OwlUI.getThemeFont(OwlUI.NEWS_TEXT_FONT_ID, SWT.NORMAL);
    FontData[] fontData = newsFont.getFontData();
    if (fontData.length > 0) {
      fNewsFontFamily = fontData[0].getName();
      fontHeight = fontData[0].getHeight();
    }

    int normal = fontHeight;
    String fontUnit = "pt"; //$NON-NLS-1$
    fNormalFontCSS = "font-size: " + normal + fontUnit + ";"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Composite to hold all components */
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Browser to preview News */
    fBrowser = new CBrowser(composite, SWT.NONE);
    fBrowser.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fBrowser.setScriptDisabled(true);
    fBrowser.getControl().addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.character == SWT.ESC)
          close();
      }
    });

    /* Label Provider to produce HTML per News */
    fLabelProvider = new NewsBrowserLabelProvider(fBrowser);

    /* Load and Display the Feed */
    loadFeed();

    /* Separator */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    applyDialogFont(composite);

    return composite;
  }

  private void loadFeed() {

    /* Show Info that Feed is loading */
    if (fLoadedFeed == null || fLoadedFeed.getVisibleNews().isEmpty()) {
      if (StringUtils.isSet(fBookmark.getName()))
        showMessage(NLS.bind(Messages.PreviewFeedDialog_LOAD_FEED_N, fBookmark.getName()), false, true);
      else
        showMessage(Messages.PreviewFeedDialog_LOAD_FEED, false, true);
    }

    /* Load Feed in Background */
    JobRunner.runUIUpdater(new UIBackgroundJob(fBrowser.getControl()) {
      private IFeed feed;
      private Exception error;

      @Override
      protected void runInBackground(IProgressMonitor monitor) {

        /* First Check if a Feed was already provided */
        if (fLoadedFeed != null && !fLoadedFeed.getVisibleNews().isEmpty()) {
          feed = fLoadedFeed;
          return;
        }

        /* Otherwise Load Feed */
        try {

          /* Resolve Feed if existing */
          if (fFeedReference != null)
            feed = fFeedReference.resolve();

          /* Create Temporary Feed */
          if (feed == null || feed.getVisibleNews().isEmpty()) {
            feed = Owl.getModelFactory().createFeed(null, fBookmark.getFeedLinkReference().getLink());

            /* Return if dialog closed */
            if (monitor.isCanceled() || getShell().isDisposed() || fBrowser.getControl().isDisposed())
              return;

            /* Retrieve Stream */
            IProtocolHandler handler = Owl.getConnectionService().getHandler(feed.getLink());
            InputStream inS = handler.openStream(feed.getLink(), monitor, null);

            /* Return if dialog closed */
            if (monitor.isCanceled() || getShell().isDisposed() || fBrowser.getControl().isDisposed())
              return;

            /* Interpret Feed */
            Owl.getInterpreter().interpret(inS, feed, null);
          }
        } catch (ConnectionException e) {
          error = e;
          Activator.safeLogError(e.getMessage(), e);
        } catch (ParserException e) {
          error = e;
          Activator.safeLogError(e.getMessage(), e);
        } catch (InterpreterException e) {
          error = e;
          Activator.safeLogError(e.getMessage(), e);
        }
      }

      @Override
      protected void runInUI(IProgressMonitor monitor) {
        if (feed != null && error == null)
          showFeed(feed);
        else if (error != null) {
          String errorMessage = CoreUtils.toMessage(error);
          if (StringUtils.isSet(errorMessage))
            showMessage(NLS.bind(Messages.PreviewFeedDialog_UNABLE_LOAD_FEED, errorMessage), true, false);
        }
      }
    });
  }

  private void showMessage(String msg, boolean isError, boolean showProgress) {
    if (fBrowser.getControl().isDisposed())
      return;

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"); //$NON-NLS-1$
    html.append("<html>\n"); //$NON-NLS-1$
    html.append("<body style=\"overflow: auto; font-family: ").append(fNewsFontFamily).append(",Verdanna,sans-serif; ").append(fNormalFontCSS).append("\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if (showProgress)
      html.append("<img src=\"" + OwlUI.getImageUri("/icons/obj16/progress.gif", "progress.gif") + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    if (isError)
      html.append("<span style=\"color: darkred;\">"); //$NON-NLS-1$
    else if (showProgress)
      html.append("<span style=\"padding-left:3px; vertical-align:top;\">"); //$NON-NLS-1$

    html.append(msg);

    if (isError || showProgress)
      html.append("</span>"); //$NON-NLS-1$

    html.append("</body>\n"); //$NON-NLS-1$
    html.append("</html>\n"); //$NON-NLS-1$

    fBrowser.getControl().setText(html.toString());
  }

  private void showFeed(final IFeed feed) {
    if (feed != null && !fBrowser.getControl().isDisposed()) {
      List<INews> news = feed.getNewsByStates(INews.State.getVisible());
      Collections.sort(news, new Comparator<INews>() {
        public int compare(INews news1, INews news2) {
          Date date1 = DateUtils.getRecentDate(news1);
          Date date2 = DateUtils.getRecentDate(news2);

          return date2.compareTo(date1);
        }
      });

      int newsCount = news.size();
      if (news.size() > MAX_NEWS_SHOWN)
        news = news.subList(0, MAX_NEWS_SHOWN);

      /* Render Elements */
      String html = fLabelProvider.render(news.toArray(), (feed.getBase() != null) ? URIUtils.toHTTP(feed.getBase()) : URIUtils.toHTTP(feed.getLink()), true);

      /* Apply to Browser */
      fBrowser.getControl().setText(html);

      /* Also Update Status */
      if (StringUtils.isSet(fBookmark.getName())) {
        StringBuilder str = new StringBuilder();
        if (feed.getHomepage() != null) {
          str.append(NLS.bind(Messages.PreviewFeedDialog_FOUND_N_NEWS_HOMEPAGE, newsCount, fBookmark.getName()));
          fStatusLabel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              new OpenInBrowserAction(new StructuredSelection(feed.getHomepage())).run();
            }
          });
        } else
          str.append(NLS.bind(Messages.PreviewFeedDialog_FOUND_N_NEWS, newsCount, fBookmark.getName()));

        fStatusLabel.setText(str.toString());
      }
    }
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);

    if (StringUtils.isSet(fBookmark.getName()))
      shell.setText(NLS.bind(Messages.PreviewFeedDialog_PREVIEW_OF, fBookmark.getName()));
    else
      shell.setText(Messages.PreviewFeedDialog_PREVIEW);
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    int style = SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.CLOSE | getDefaultOrientation();

    return style;
  }

  /*
   * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {
    GridLayout layout = new GridLayout(1, false);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

    Composite buttonBar = new Composite(parent, SWT.NONE);
    buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    buttonBar.setLayout(layout);

    /* Status Label */
    fStatusLabel = new Link(buttonBar, SWT.NONE);
    applyDialogFont(fStatusLabel);
    fStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    if (StringUtils.isSet(fBookmark.getName()))
      fStatusLabel.setText(fBookmark.getName());

    /* Close */
    Button closeButton = createButton(buttonBar, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
    closeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        close();
      }
    });

    return buttonBar;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
   */
  @Override
  protected IDialogSettings getDialogBoundsSettings() {
    IDialogSettings settings = Activator.getDefault().getDialogSettings();
    IDialogSettings section = settings.getSection(DIALOG_SETTINGS_KEY);
    if (section != null)
      return section;

    return settings.addNewSection(DIALOG_SETTINGS_KEY);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
   */
  @Override
  protected int getDialogBoundsStrategy() {
    return Dialog.DIALOG_PERSISTLOCATION | Dialog.DIALOG_PERSISTSIZE;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  @Override
  protected void initializeBounds() {
    super.initializeBounds();

    Shell shell = getShell();

    /* Minimum Size */
    int minWidth = convertHorizontalDLUsToPixels(DIALOG_WIDTH_DLUS);
    int minHeight = convertHorizontalDLUsToPixels(DIALOG_HEIGHT_DLUS);

    /* Required Size */
    Point requiredSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    /* Set Size */
    shell.setSize(Math.max(minWidth, requiredSize.x), Math.max(minHeight, requiredSize.y));

    /* Set Location */
    if (fFirstTimeOpen)
      LayoutUtils.positionShell(shell);
  }
}
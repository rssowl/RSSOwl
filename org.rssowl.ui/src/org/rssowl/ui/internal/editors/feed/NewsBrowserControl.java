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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.ApplicationServer;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.ILinkHandler;
import org.rssowl.ui.internal.LinkTransformer;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.OwlUI.Layout;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * Part of the FeedView to display News in a BrowserViewer.
 *
 * @author bpasero
 */
public class NewsBrowserControl implements IFeedViewPart {
  private IFeedViewSite fFeedViewSite;
  private NewsBrowserViewer fViewer;
  private ISelectionListener fSelectionListener;
  private Object fInitialInput;
  private boolean fInputSet;
  private IPreferenceScope fInputPreferences;
  private IPropertyChangeListener fPropertyChangeListener;
  private boolean fStripImagesFromNews;
  private boolean fStripMediaFromNews;
  private boolean fHeadlinesOnly;
  private NewsComparator fNewsSorter;
  private FeedViewInput fEditorInput;
  private Composite fInfoBar;
  private Label fInfoBarSeparator;

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#init(org.rssowl.ui.internal.editors.feed.IFeedViewSite)
   */
  public void init(IFeedViewSite feedViewSite) {
    fFeedViewSite = feedViewSite;
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#onInputChanged(org.rssowl.ui.internal.editors.feed.FeedViewInput)
   */
  public void onInputChanged(FeedViewInput input) {
    fEditorInput = input;
    fInputPreferences = Owl.getPreferenceService().getEntityScope(input.getMark());
    fStripImagesFromNews = !fInputPreferences.getBoolean(DefaultPreferences.ENABLE_IMAGES);
    fStripMediaFromNews = !fInputPreferences.getBoolean(DefaultPreferences.ENABLE_MEDIA);
    fHeadlinesOnly = (OwlUI.getLayout(fInputPreferences) == Layout.HEADLINES);
    if (fViewer != null && fViewer.getLabelProvider() != null) {
      ((NewsBrowserLabelProvider) fViewer.getLabelProvider()).setStripMediaFromNews(fStripImagesFromNews, fStripMediaFromNews);
      ((NewsBrowserLabelProvider) fViewer.getLabelProvider()).setHeadlinesOnly(fHeadlinesOnly);
    }
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#onLayoutChanged(org.rssowl.ui.internal.OwlUI.Layout)
   */
  public void onLayoutChanged(Layout newLayout) {
    fHeadlinesOnly = (newLayout == Layout.HEADLINES);
    if (fViewer != null) {

      /* Indicate Headlines Mode */
      if (fViewer.getLabelProvider() != null)
        ((NewsBrowserLabelProvider) fViewer.getLabelProvider()).setHeadlinesOnly(fHeadlinesOnly);

      /* Pass on to Viewer */
      fViewer.onLayoutChanged(newLayout);
    }
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#createPart(org.eclipse.swt.widgets.Composite)
   */
  public void createPart(Composite parent) {
    Composite container = new Composite(parent, SWT.None);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    container.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));
    container.setBackground(parent.getBackground());

    /* Info Bar to indicate new incoming news when in news paper view */
    createInfoBar(container);
    setInfoBarVisible(false, false);

    /* Browser Viewer for News */
    fViewer = new NewsBrowserViewer(container, SWT.NONE, fFeedViewSite) {
      @Override
      protected void onRefresh() {
        setInfoBarVisible(false);
      }
    };
    fViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
  }

  private void createInfoBar(final Composite parent) {
    fInfoBar = new Composite(parent, SWT.None);
    fInfoBar.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    fInfoBar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fInfoBar.setLayout(LayoutUtils.createGridLayout(3, 3, 3));
    fInfoBar.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    fInfoBar.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        onInfoBarClicked();
      }
    });

    Label imgLabel = new Label(fInfoBar, SWT.None);
    imgLabel.setImage(OwlUI.getImage(imgLabel, "icons/obj16/info.gif")); //$NON-NLS-1$
    imgLabel.setBackground(fInfoBar.getBackground());
    imgLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    Link textLink = new Link(fInfoBar, SWT.NONE);
    textLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    textLink.setBackground(fInfoBar.getBackground());
    textLink.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
    textLink.setText(Messages.NewsBrowserControl_ADDITIONAL_NEWS_INFO);
    textLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onInfoBarClicked();
      }
    });

    textLink.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        onInfoBarClicked();
      }
    });

    ToolBar bar = new ToolBar(fInfoBar, SWT.FLAT);
    bar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
    bar.setBackground(fInfoBar.getBackground());

    ToolItem closeItem = new ToolItem(bar, SWT.PUSH);
    closeItem.setToolTipText(Messages.NewsBrowserControl_CLOSE);
    closeItem.setImage(OwlUI.getImage(bar, "icons/etool16/close_normal.png")); //$NON-NLS-1$
    closeItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setInfoBarVisible(false);
      }
    });

    /* Separator */
    fInfoBarSeparator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    fInfoBarSeparator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
  }

  private void onInfoBarClicked() {
    boolean moveToTop = OwlUI.getPageSize(fInputPreferences).getPageSize() == 0; //Only move to top when paging is disabled
    fViewer.refresh(true, moveToTop); //Refresh will take care of closing the info bar
  }

  /**
   * @param visible <code>true</code> to show the info bar and
   * <code>false</code> otherwise.
   */
  public void setInfoBarVisible(boolean visible) {
    setInfoBarVisible(visible, true);
  }

  private void setInfoBarVisible(boolean visible, boolean layout) {

    /* Return early if disposed */
    if (fInfoBar == null || fInfoBar.isDisposed())
      return;

    /* Return early if visibility already identical to request */
    if (((GridData) fInfoBar.getLayoutData()).exclude != visible)
      return;

    /* Update Layout and Visibility */
    ((GridData) fInfoBarSeparator.getLayoutData()).exclude = !visible;
    ((GridData) fInfoBar.getLayoutData()).exclude = !visible;
    fInfoBarSeparator.setVisible(visible);
    fInfoBar.setVisible(visible);
    if (layout)
      fInfoBar.getParent().layout(true, true);
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#getViewer()
   */
  public NewsBrowserViewer getViewer() {
    return fViewer;
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#initViewer(org.eclipse.jface.viewers.IStructuredContentProvider,
   * org.eclipse.jface.viewers.ViewerFilter)
   */
  public void initViewer(IStructuredContentProvider contentProvider, ViewerFilter filter) {

    /* Apply ContentProvider */
    fViewer.setContentProvider(contentProvider);

    /* Create LabelProvider */
    NewsBrowserLabelProvider labelProvider = new NewsBrowserLabelProvider(fViewer);
    labelProvider.setStripMediaFromNews(fStripImagesFromNews, fStripMediaFromNews);
    labelProvider.setHeadlinesOnly(fHeadlinesOnly);
    fViewer.setLabelProvider(labelProvider);

    /* Create Sorter */
    fNewsSorter = new NewsComparator();
    fViewer.setComparator(fNewsSorter);
    updateSorting(fEditorInput.getMark(), false);

    /* Add ViewerFilter */
    fViewer.addFilter(filter);

    /* Register Listeners */
    registerListener();
  }

  void updateSorting(Object input, boolean refreshIfChanged) {
    if (fViewer.getControl().isDisposed())
      return;

    IPreferenceScope preferences;
    if (input instanceof IEntity)
      preferences = Owl.getPreferenceService().getEntityScope((IEntity) input);
    else
      preferences = Owl.getPreferenceService().getGlobalScope();

    NewsColumn sortColumn = NewsColumn.values()[preferences.getInteger(DefaultPreferences.BM_NEWS_SORT_COLUMN)];
    boolean ascending = preferences.getBoolean(DefaultPreferences.BM_NEWS_SORT_ASCENDING);

    NewsColumn oldSortColumn = fNewsSorter.getSortBy();
    boolean oldAscending = fNewsSorter.isAscending();

    fNewsSorter.setSortBy(sortColumn);
    fNewsSorter.setAscending(ascending);

    if (refreshIfChanged && ((oldSortColumn != sortColumn) || (oldAscending != ascending)))
      fViewer.refresh();
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#setInput(java.lang.Object)
   */
  public void setPartInput(Object input) {

    /* Update Columns for Input */
    if (input instanceof EntityGroup || input instanceof INewsMark)
      updateSorting(input, false);

    /* Set input to Viewer */
    Pair<?, Boolean> computedInput = getInput(input);
    fViewer.setInput(computedInput.getFirst(), computedInput.getSecond());

    /* Remember as initial Input */
    fInitialInput = fViewer.getInput();
    fInputSet = true;
  }

  private Pair<? /* Input from News */, Boolean /* Block External Navigation */> getInput(Object obj) {

    /* Return Reference */
    if (obj instanceof INewsMark)
      return Pair.create(((INewsMark) obj).toReference(), false);

    /* News: Handle special dependant on settings */
    else if (obj instanceof INews)
      return getInput((INews) obj);

    /* NewsReference: Resolve and special handle */
    else if (obj instanceof NewsReference) {
      INews resolvedNews = null;

      if (fViewer.getContentProvider() instanceof NewsContentProvider)
        resolvedNews = ((NewsContentProvider) fViewer.getContentProvider()).obtainFromCache(((NewsReference) obj).getId());

      if (resolvedNews == null)
        resolvedNews = ((NewsReference) obj).resolve();

      return getInput(resolvedNews);
    }

    return Pair.create(obj, false);
  }

  private Pair<? /* Input from News */, Boolean /* Block External Navigation */> getInput(INews news) {

    /* Check if user configured to open link of news or not */
    if (!fInputPreferences.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_NEWS))
      return Pair.create(news, false);

    /* Check if user configured to only link if news is empty or not */
    if (fInputPreferences.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_EMPTY_NEWS) && !CoreUtils.isEmpty(news))
      return Pair.create(news, false);

    /* Check if a news link is provided at all */
    String newsLink = CoreUtils.getLink(news);
    if (!StringUtils.isSet(newsLink))
      return Pair.create(news, false);

    /* Check if user configured to use a link transformer */
    if (fInputPreferences.getBoolean(DefaultPreferences.BM_USE_TRANSFORMER)) {
      String transformerId = fInputPreferences.getString(DefaultPreferences.BM_TRANSFORMER_ID);
      LinkTransformer transformer = Controller.getDefault().getLinkTransformer(transformerId);
      if (transformer != null)
        return Pair.create(transformer.toTransformedUrl(newsLink), true);
    }

    /* User wants to open the link of the news */
    return Pair.create(newsLink, false);
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#dispose()
   */
  public void dispose() {
    unregisterListeners();
    fEditorInput = null;
  }

  private void registerListener() {

    /* Listen on selection-changes */
    fSelectionListener = new ISelectionListener() {
      public void selectionChanged(IWorkbenchPart part, ISelection sel) {

        /* Only Track selections from the HeadlineControl */
        if (!part.equals(fFeedViewSite.getEditorSite().getPart()))
          return;

        /* Return early if browser is not even visible */
        if (!fFeedViewSite.isBrowserViewerVisible())
          return;

        IStructuredSelection selection = (IStructuredSelection) sel;

        /* Restore Initial Input (if set) if selection is empty */
        if (selection.isEmpty() && fInputSet) {
          fViewer.setInput(fInitialInput);
        }

        /* Set Elements as Input if 1 Item is selected */
        else if (selection.size() == 1)
          setPartInput(selection.getFirstElement());
      }
    };
    fFeedViewSite.getEditorSite().getPage().addSelectionListener(fSelectionListener);

    /* Send Browser-Status to Workbench-Status */
    ((Browser) fViewer.getControl()).addStatusTextListener(new StatusTextListener() {
      public void changed(StatusTextEvent event) {

        /* Don't show Status for the Handler Protocol */
        if (event.text != null && !event.text.contains(ILinkHandler.HANDLER_PROTOCOL) && !event.text.contains(ApplicationServer.DEFAULT_LOCALHOST)) {

          /* Do not post to status line if browser is hidden (e.g. hidden tab) */
          if (!fViewer.getControl().isDisposed() && fViewer.getControl().isVisible()) {
            String statusText = event.text;
            statusText = URIUtils.fastDecode(statusText);
            statusText = statusText.replaceAll("&", "&&"); //$NON-NLS-1$//$NON-NLS-2$
            if (URIUtils.isManaged(statusText))
              statusText = URIUtils.toUnManaged(statusText);

            fFeedViewSite.getEditorSite().getActionBars().getStatusLineManager().setMessage(statusText);
          }
        }
      }
    });

    /* Control Browser's visibility based on the location */
    ((Browser) fViewer.getControl()).addLocationListener(new LocationAdapter() {
      @Override
      public void changing(LocationEvent event) {
        if (event.doit) {
          String loc = event.location;
          boolean visible = fViewer.getControl().getVisible();

          /* Make Browser visible now */
          if (!visible && StringUtils.isSet(loc) && !URIUtils.ABOUT_BLANK.equals(loc))
            fViewer.getControl().setVisible(true);
        }
      }
    });

    /* Refresh Browser when Font Changes */
    fPropertyChangeListener = new IPropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        if (fViewer.getControl().isDisposed())
          return;

        String property = event.getProperty();
        if (OwlUI.NEWS_TEXT_FONT_ID.equals(property) || OwlUI.STICKY_BG_COLOR_ID.equals(property) || OwlUI.LINK_FG_COLOR_ID.equals(property) || OwlUI.NEWS_LIST_BG_COLOR_ID.equals(property))
          fViewer.getBrowser().refresh();
      }
    };
    PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(fPropertyChangeListener);
  }

  private void unregisterListeners() {
    fFeedViewSite.getEditorSite().getPage().removeSelectionListener(fSelectionListener);
    PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(fPropertyChangeListener);
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewPart#setFocus()
   */
  public void setFocus() {
    fViewer.getControl().setFocus();
  }
}
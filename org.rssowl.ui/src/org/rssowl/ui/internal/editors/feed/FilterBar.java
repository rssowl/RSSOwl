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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.actions.ActionFactory;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.ITask;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.TaskAdapter;
import org.rssowl.ui.internal.ContextMenuCreator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.OwlUI.Layout;
import org.rssowl.ui.internal.dialogs.SearchMarkDialog;
import org.rssowl.ui.internal.editors.feed.NewsFilter.SearchTarget;
import org.rssowl.ui.internal.editors.feed.NewsFilter.Type;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.JobTracker;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * The FilterBar is the central control to filter News that are showing in the
 * FeedView. It supports filtering, grouping and a quick-search.
 *
 * @author bpasero
 */
public class FilterBar {

  /* Action to Filter News */
  private static final String FILTER_ACTION = "org.rssowl.ui.internal.editors.feed.FilterAction"; //$NON-NLS-1$

  /* Action to Group News */
  private static final String GROUP_ACTION = "org.rssowl.ui.internal.editors.feed.GroupAction"; //$NON-NLS-1$

  /* Action to Layout News */
  private static final String LAYOUT_ACTION = "org.rssowl.ui.internal.editors.feed.LayoutAction"; //$NON-NLS-1$

  /* Action to Quicksearch */
  private static final String QUICKSEARCH_ACTION = "org.rssowl.ui.internal.editors.feed.QuickSearchAction"; //$NON-NLS-1$

  private Composite fParent;
  private Composite fContainer;
  private ToolBarManager fFilterGroupingLayoutToolBarManager;
  private ToolBarManager fClearQuicksearchToolBar;
  private ToolBarManager fHighlightToolBarManager;
  private IAction fHighlightSearchAction;
  private FeedView fFeedView;
  private JobTracker fQuickSearchTracker;
  private Text fSearchInput;
  private IPreferenceScope fGlobalPreferences;
  private boolean fBlockRefresh;
  private NewsFilter.Type fLastFilterType;
  private NewsGrouping.Type fLastGroupType;
  private boolean fSearchSelectAllOnce = true;

  /**
   * @param feedView
   * @param parent
   */
  public FilterBar(FeedView feedView, Composite parent) {
    fFeedView = feedView;
    fParent = parent;
    fQuickSearchTracker = new JobTracker(500, false, true, ITask.Priority.SHORT);
    fGlobalPreferences = Owl.getPreferenceService().getGlobalScope();

    createControl();
  }

  boolean isVisible() {
    IPreferenceScope globalScope = Owl.getPreferenceService().getGlobalScope();
    boolean hideFilterBar = globalScope.getBoolean(DefaultPreferences.FV_FEED_TOOLBAR_HIDDEN);

    return !hideFilterBar;
  }

  private boolean isListLayout() {
    return (getLayout() == Layout.LIST);
  }

  private Layout getLayout() {
    FeedViewInput input = ((FeedViewInput) fFeedView.getEditorInput());
    if (input != null)
      return OwlUI.getLayout(Owl.getPreferenceService().getEntityScope(input.getMark()));

    return OwlUI.getLayout(fGlobalPreferences);
  }

  private boolean isSearchMark() {
    FeedViewInput input = ((FeedViewInput) fFeedView.getEditorInput());
    if (input != null && input.getMark() instanceof ISearchMark)
      return true;

    return false;
  }

  /**
   * Clear the Quick-Search
   *
   * @param refresh
   */
  public void clearQuickSearch(boolean refresh) {
    setSearchControlsVisible(false);

    if (fSearchInput.getText().length() != 0) {
      fBlockRefresh = !refresh;
      try {
        fSearchInput.setText(""); //$NON-NLS-1$
      } finally {
        fBlockRefresh = false;
      }
    }
  }

  /** Give Focus to the Quicksearch Input */
  public void focusQuickSearch() {
    fSearchInput.setFocus();
  }

  private void createControl() {
    fContainer = new Composite(fParent, SWT.NONE);
    fContainer.setLayout(LayoutUtils.createGridLayout(5, 3, 0, 0, 0, false));
    fContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    updateVisibility();

    /* Left Toolbar with Filter, Grouping and Layout */
    fFilterGroupingLayoutToolBarManager = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    createFilterBar();
    createGrouperBar();
    createLayoutBar();
    fFilterGroupingLayoutToolBarManager.createControl(fContainer);
    fFilterGroupingLayoutToolBarManager.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

    /* Quick Search */
    createQuickSearch(fContainer);
  }

  void updateVisibility() {
    boolean isVisible = isVisible();

    ((GridData) fContainer.getLayoutData()).exclude = !isVisible;
    fContainer.setVisible(isVisible);
  }

  private boolean setHighlight(boolean enabled) {

    /* Highlighting is unsupported when headlines layout is used */
    if (isListLayout())
      return false;

    /* Return if already in same state */
    boolean isHighlightEnabled = fGlobalPreferences.getBoolean(DefaultPreferences.FV_HIGHLIGHT_SEARCH_RESULTS);
    if (enabled == isHighlightEnabled)
      return false;

    fGlobalPreferences.putBoolean(DefaultPreferences.FV_HIGHLIGHT_SEARCH_RESULTS, !isHighlightEnabled);
    fHighlightSearchAction.setChecked(!isHighlightEnabled);

    return true;
  }

  /* Quick Search */
  private void createQuickSearch(Composite parent) {
    Composite searchContainer = new Composite(parent, SWT.NONE);
    searchContainer.setLayout(LayoutUtils.createGridLayout(OwlUI.needsCancelControl() ? 4 : 3, 0, 0, 0, 0, false));
    ((GridLayout) searchContainer.getLayout()).marginTop = 1;
    searchContainer.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
    ((GridData) searchContainer.getLayoutData()).widthHint = 280;

    final ToolBarManager manager = new ToolBarManager(SWT.FLAT);
    final NewsFilter filter = fFeedView.getFilter();

    IAction quickSearch = new Action(Messages.FilterBar_QUICK_SEARCH, IAction.AS_DROP_DOWN_MENU) {
      @Override
      public void run() {
        OwlUI.positionDropDownMenu(this, manager);
      }

      @Override
      public String getId() {
        return QUICKSEARCH_ACTION;
      }
    };
    quickSearch.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/find.gif")); //$NON-NLS-1$

    quickSearch.setMenuCreator(new ContextMenuCreator() {

      @Override
      public Menu createMenu(Control parent) {
        Menu menu = new Menu(parent);

        /* Search on: Subject */
        final MenuItem searchHeadline = new MenuItem(menu, SWT.RADIO);
        searchHeadline.setText(NewsFilter.SearchTarget.HEADLINE.getName());
        searchHeadline.setSelection(NewsFilter.SearchTarget.HEADLINE == filter.getSearchTarget());
        searchHeadline.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchHeadline.getSelection() && filter.getSearchTarget() != NewsFilter.SearchTarget.HEADLINE)
              doSearch(NewsFilter.SearchTarget.HEADLINE);
          }
        });

        /* Search on: Entire News */
        final MenuItem searchEntireNews = new MenuItem(menu, SWT.RADIO);
        searchEntireNews.setText(NewsFilter.SearchTarget.ALL.getName());
        searchEntireNews.setSelection(NewsFilter.SearchTarget.ALL == filter.getSearchTarget());
        searchEntireNews.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchEntireNews.getSelection() && filter.getSearchTarget() != NewsFilter.SearchTarget.ALL)
              doSearch(NewsFilter.SearchTarget.ALL);
          }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        /* Search on: Author */
        final MenuItem searchAuthor = new MenuItem(menu, SWT.RADIO);
        searchAuthor.setText(NewsFilter.SearchTarget.AUTHOR.getName());
        searchAuthor.setSelection(NewsFilter.SearchTarget.AUTHOR == filter.getSearchTarget());
        searchAuthor.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchAuthor.getSelection() && filter.getSearchTarget() != NewsFilter.SearchTarget.AUTHOR)
              doSearch(NewsFilter.SearchTarget.AUTHOR);
          }
        });

        /* Search on: Category */
        final MenuItem searchCategory = new MenuItem(menu, SWT.RADIO);
        searchCategory.setText(NewsFilter.SearchTarget.CATEGORY.getName());
        searchCategory.setSelection(NewsFilter.SearchTarget.CATEGORY == filter.getSearchTarget());
        searchCategory.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchCategory.getSelection() && filter.getSearchTarget() != NewsFilter.SearchTarget.CATEGORY)
              doSearch(NewsFilter.SearchTarget.CATEGORY);
          }
        });

        /* Search on: Source */
        final MenuItem searchSource = new MenuItem(menu, SWT.RADIO);
        searchSource.setText(NewsFilter.SearchTarget.SOURCE.getName());
        searchSource.setSelection(NewsFilter.SearchTarget.SOURCE == filter.getSearchTarget());
        searchSource.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchSource.getSelection() && filter.getSearchTarget() != NewsFilter.SearchTarget.SOURCE)
              doSearch(NewsFilter.SearchTarget.SOURCE);
          }
        });

        /* Search on: Attachments */
        final MenuItem searchAttachments = new MenuItem(menu, SWT.RADIO);
        searchAttachments.setText(NewsFilter.SearchTarget.ATTACHMENTS.getName());
        searchAttachments.setSelection(NewsFilter.SearchTarget.ATTACHMENTS == filter.getSearchTarget());
        searchAttachments.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchAttachments.getSelection() && filter.getSearchTarget() != NewsFilter.SearchTarget.ATTACHMENTS)
              doSearch(NewsFilter.SearchTarget.ATTACHMENTS);
          }
        });

        /* Search on: Labels */
        final MenuItem searchLabels = new MenuItem(menu, SWT.RADIO);
        searchLabels.setText(NewsFilter.SearchTarget.LABELS.getName());
        searchLabels.setSelection(NewsFilter.SearchTarget.LABELS == filter.getSearchTarget());
        searchLabels.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchLabels.getSelection() && filter.getSearchTarget() != NewsFilter.SearchTarget.LABELS)
              doSearch(NewsFilter.SearchTarget.LABELS);
          }
        });

        /* Offer to Save as Search */
        INewsMark inputMark = ((FeedViewInput) fFeedView.getEditorInput()).getMark();
        if (inputMark instanceof IBookMark || inputMark instanceof INewsBin || inputMark instanceof FolderNewsMark) {

          /* Separator */
          new MenuItem(menu, SWT.SEPARATOR);

          /* Convert Filter to Saved Search */
          final MenuItem createSavedSearch = new MenuItem(menu, SWT.RADIO);
          createSavedSearch.setText(Messages.FilterBar_SAVE_SEARCH);
          createSavedSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              onCreateSavedSearch(true);
            }
          });
        }

        return menu;
      }
    });

    manager.add(quickSearch);
    manager.createControl(searchContainer);

    /* Input for the Search */
    fSearchInput = new Text(searchContainer, SWT.BORDER | SWT.SINGLE | SWT.SEARCH | SWT.CANCEL);
    fSearchInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    fSearchInput.setMessage(fFeedView.getFilter().getSearchTarget().getName());

    /* Register this Input Field to Context Service */
    Controller.getDefault().getContextService().registerInputField(fSearchInput);

    /* Reset any Filter if set on ESC */
    fSearchInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.ESC) {
          clearQuickSearch(true);
          fFeedView.handleQuicksearchTraversalEvent(SWT.TRAVERSE_RETURN, true);
        }
      }
    });

    /* Handle the CR Key Pressed */
    fSearchInput.addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_PAGE_NEXT || e.detail == SWT.TRAVERSE_PAGE_PREVIOUS) {
          e.doit = false;
          fFeedView.handleQuicksearchTraversalEvent(e.detail, false);
        }
      }
    });

    /* Run search when text is entered */
    fSearchInput.addModifyListener(new ModifyListener() {
      private boolean highlightChanged = false;

      @Override
      public void modifyText(ModifyEvent e) {

        /* Clear Search immediately */
        if (fSearchInput.getText().length() == 0 && fFeedView.getFilter().isPatternSet()) {
          fFeedView.getFilter().setPattern(fSearchInput.getText());
          if (!fBlockRefresh) {
            BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
              @Override
              public void run() {
                if (highlightChanged) {
                  setHighlight(false);
                  highlightChanged = false;
                }
                if (needsCacheRevalidationFromSearch())
                  fFeedView.revalidateCaches();
                fFeedView.refresh(true, false);
              }
            });
          }

          setSearchControlsVisible(false);
        }

        /* Run Search in JobTracker */
        else if (fSearchInput.getText().length() > 0) {
          fQuickSearchTracker.run(new TaskAdapter() {
            @Override
            public IStatus run(IProgressMonitor monitor) {
              BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
                @Override
                public void run() {
                  if (setHighlight(true))
                    highlightChanged = true;
                  fFeedView.getFilter().setPattern(fSearchInput.getText());
                  if (needsCacheRevalidationFromSearch())
                    fFeedView.revalidateCaches();
                  fFeedView.refresh(true, false);
                  updateBrowserSelection();
                }
              });
              setSearchControlsVisible(true);
              return Status.OK_STATUS;
            }
          });
        }
      }
    });

    fSearchInput.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        fFeedView.getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.CUT.getId()).setEnabled(true);
        fFeedView.getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId()).setEnabled(true);
        fFeedView.getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.PASTE.getId()).setEnabled(true);
      }

      @Override
      public void focusLost(FocusEvent e) {
        fFeedView.getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.CUT.getId()).setEnabled(false);
        fFeedView.getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId()).setEnabled(false);
        fFeedView.getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.PASTE.getId()).setEnabled(false);
        fSearchSelectAllOnce = true;
      }
    });

    /* Select All on Mouse Up */
    fSearchInput.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        if (fSearchSelectAllOnce && fSearchInput.getSelectionCount() == 0)
          fSearchInput.selectAll();

        fSearchSelectAllOnce = false;
      }
    });

    /* Clear Button */
    if (OwlUI.needsCancelControl()) {
      ToolBar toolBar = new ToolBar(searchContainer, SWT.FLAT | SWT.HORIZONTAL);
      fClearQuicksearchToolBar = new ToolBarManager(toolBar);
      toolBar.setBackground(parent.getBackground());
      toolBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

      /* Initially Hide */
      ((GridData) toolBar.getLayoutData()).exclude = true;
      toolBar.setVisible(false);

      IAction clearTextAction = new Action("", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
        @Override
        public void run() {
          clearQuickSearch(true);
          fFeedView.handleQuicksearchTraversalEvent(SWT.TRAVERSE_RETURN, true);
        }
      };

      clearTextAction.setToolTipText(Messages.FilterBar_CLEAR);
      clearTextAction.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/clear.gif")); //$NON-NLS-1$

      fClearQuicksearchToolBar.add(clearTextAction);
      fClearQuicksearchToolBar.update(false);
    }

    /* Highlight Button */
    ToolBar toolBar = new ToolBar(searchContainer, SWT.FLAT | SWT.HORIZONTAL);
    fHighlightToolBarManager = new ToolBarManager(toolBar);
    toolBar.setBackground(parent.getBackground());
    toolBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
    ((GridData) toolBar.getLayoutData()).horizontalIndent = 2;

    /* Set Initial State based on Input and Layout */
    boolean show = isSearchMark() && !isListLayout();
    ((GridData) toolBar.getLayoutData()).exclude = !show;
    toolBar.setVisible(show);

    fHighlightSearchAction = new Action(Messages.FilterBar_HIGHLIGHT, IAction.AS_CHECK_BOX) {
      @Override
      public void run() {
        fGlobalPreferences.putBoolean(DefaultPreferences.FV_HIGHLIGHT_SEARCH_RESULTS, isChecked());
        if (fFeedView.isBrowserViewerVisible())
          fFeedView.getNewsBrowserControl().getViewer().refresh();
      }
    };

    fHighlightSearchAction.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/highlight.gif")); //$NON-NLS-1$
    fHighlightSearchAction.setToolTipText(Messages.FilterBar_HIGHLIGHT);
    fHighlightSearchAction.setChecked(fGlobalPreferences.getBoolean(DefaultPreferences.FV_HIGHLIGHT_SEARCH_RESULTS));

    fHighlightToolBarManager.add(fHighlightSearchAction);
    fHighlightToolBarManager.update(false);
  }

  private void updateBrowserSelection() {
    if (fFeedView.isTableViewerVisible() && fFeedView.isBrowserViewerVisible() && fFeedView.isBrowserShowingNews()) {
      NewsTableControl newsTable = fFeedView.getNewsTableControl();
      if (newsTable.getViewer().getSelection().isEmpty())
        fFeedView.getNewsBrowserControl().setPartInput(null);
    }
  }

  void setSearchControlsVisible(boolean visible) {
    if (!isVisible())
      return;

    boolean layout = false;

    /* Clear */
    if (fClearQuicksearchToolBar != null && !fClearQuicksearchToolBar.getControl().isDisposed() && ((GridData) fClearQuicksearchToolBar.getControl().getLayoutData()).exclude == visible) {
      ((GridData) fClearQuicksearchToolBar.getControl().getLayoutData()).exclude = !visible;
      fClearQuicksearchToolBar.getControl().setVisible(visible);
      layout = true;
    }

    /* Highlight */
    if (isListLayout())
      visible = false; //Never show highlight bar for headlines layout
    else if (isSearchMark())
      visible = true; //Always show highlight bar for saved searches

    if (fHighlightToolBarManager != null && !fHighlightToolBarManager.getControl().isDisposed() && ((GridData) fHighlightToolBarManager.getControl().getLayoutData()).exclude == visible) {
      ((GridData) fHighlightToolBarManager.getControl().getLayoutData()).exclude = !visible;
      fHighlightToolBarManager.getControl().setVisible(visible);
      layout = true;
    }

    /* Layout as necessary */
    if (layout)
      fSearchInput.getParent().layout();
  }

  /* News Filter */
  private void createFilterBar() {
    final NewsFilter filter = fFeedView.getFilter();

    IAction newsFilterAction = new Action(Messages.FilterBar_FILTER_NEWS, IAction.AS_DROP_DOWN_MENU) {
      @Override
      public void run() {

        /* Toggle Show All */
        if (filter.getType() != NewsFilter.Type.SHOW_ALL)
          onFilter(NewsFilter.Type.SHOW_ALL);

        /* Toggle back to previous filter */
        else if (fLastFilterType != null)
          onFilter(fLastFilterType);

        /* Show Menu */
        else
          OwlUI.positionDropDownMenu(this, fFilterGroupingLayoutToolBarManager);
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        if (filter.getType() == NewsFilter.Type.SHOW_ALL)
          return OwlUI.FILTER;

        return OwlUI.getImageDescriptor("icons/etool16/filter_active.gif"); //$NON-NLS-1$
      }

      @Override
      public String getText() {
        return filter.getType().getDisplayName();
      }
    };
    newsFilterAction.setId(FILTER_ACTION);

    ActionContributionItem item = new ActionContributionItem(newsFilterAction);
    item.setMode(ActionContributionItem.MODE_FORCE_TEXT);

    fFilterGroupingLayoutToolBarManager.add(item);

    newsFilterAction.setMenuCreator(new ContextMenuCreator() {

      @Override
      public Menu createMenu(Control parent) {
        Menu menu = new Menu(parent);

        /* Filter: None */
        final MenuItem showAll = new MenuItem(menu, SWT.RADIO);
        showAll.setText(NewsFilter.Type.SHOW_ALL.getName());
        showAll.setSelection(NewsFilter.Type.SHOW_ALL == filter.getType());
        showAll.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showAll.getSelection() && filter.getType() != NewsFilter.Type.SHOW_ALL)
              onFilter(NewsFilter.Type.SHOW_ALL);
          }
        });
        menu.setDefaultItem(showAll);

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Filter: New */
        final MenuItem showNew = new MenuItem(menu, SWT.RADIO);
        showNew.setText(NewsFilter.Type.SHOW_NEW.getName());
        showNew.setSelection(NewsFilter.Type.SHOW_NEW == filter.getType());
        showNew.addSelectionListener(new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showNew.getSelection() && filter.getType() != NewsFilter.Type.SHOW_NEW)
              onFilter(NewsFilter.Type.SHOW_NEW);
          }
        });

        /* Filter: Unread */
        final MenuItem showUnread = new MenuItem(menu, SWT.RADIO);
        showUnread.setText(NewsFilter.Type.SHOW_UNREAD.getName());
        showUnread.setSelection(NewsFilter.Type.SHOW_UNREAD == filter.getType());
        showUnread.addSelectionListener(new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showUnread.getSelection() && filter.getType() != NewsFilter.Type.SHOW_UNREAD)
              onFilter(NewsFilter.Type.SHOW_UNREAD);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Filter: Sticky */
        final MenuItem showSticky = new MenuItem(menu, SWT.RADIO);
        showSticky.setText(NewsFilter.Type.SHOW_STICKY.getName());
        showSticky.setSelection(NewsFilter.Type.SHOW_STICKY == filter.getType());
        showSticky.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showSticky.getSelection() && filter.getType() != NewsFilter.Type.SHOW_STICKY)
              onFilter(NewsFilter.Type.SHOW_STICKY);
          }
        });

        /* Filter: Labeled */
        final MenuItem showLabeled = new MenuItem(menu, SWT.RADIO);
        showLabeled.setText(NewsFilter.Type.SHOW_LABELED.getName());
        showLabeled.setSelection(NewsFilter.Type.SHOW_LABELED == filter.getType());
        showLabeled.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showLabeled.getSelection() && filter.getType() != NewsFilter.Type.SHOW_LABELED)
              onFilter(NewsFilter.Type.SHOW_LABELED);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Filter: Recent News */
        final MenuItem showRecent = new MenuItem(menu, SWT.RADIO);
        showRecent.setText(NewsFilter.Type.SHOW_RECENT.getName());
        showRecent.setSelection(NewsFilter.Type.SHOW_RECENT == filter.getType());
        showRecent.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showRecent.getSelection() && filter.getType() != NewsFilter.Type.SHOW_RECENT)
              onFilter(NewsFilter.Type.SHOW_RECENT);
          }
        });

        /* Filter: Last 5 Days */
        final MenuItem showLastFiveDays = new MenuItem(menu, SWT.RADIO);
        showLastFiveDays.setText(NewsFilter.Type.SHOW_LAST_5_DAYS.getName());
        showLastFiveDays.setSelection(NewsFilter.Type.SHOW_LAST_5_DAYS == filter.getType());
        showLastFiveDays.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showLastFiveDays.getSelection() && filter.getType() != NewsFilter.Type.SHOW_LAST_5_DAYS)
              onFilter(NewsFilter.Type.SHOW_LAST_5_DAYS);
          }
        });

        /* Offer to Save as Search */
        INewsMark inputMark = ((FeedViewInput) fFeedView.getEditorInput()).getMark();
        if (inputMark instanceof IBookMark || inputMark instanceof INewsBin || inputMark instanceof FolderNewsMark) {

          /* Separator */
          new MenuItem(menu, SWT.SEPARATOR);

          /* Convert Filter to Saved Search */
          final MenuItem createSavedSearch = new MenuItem(menu, SWT.RADIO);
          createSavedSearch.setText(Messages.FilterBar_SAVE_SEARCH);
          createSavedSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              onCreateSavedSearch(false);
            }
          });
        }

        return menu;
      }
    });
  }

  private void onCreateSavedSearch(boolean withQuickSearch) {
    IModelFactory factory = Owl.getModelFactory();
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(2);

    /* Create Condition from Location */
    List<IFolderChild> searchScope = new ArrayList<IFolderChild>(1);
    searchScope.add(((FeedViewInput) fFeedView.getEditorInput()).getMark());
    ISearchField field = factory.createSearchField(INews.LOCATION, INews.class.getName());
    conditions.add(factory.createSearchCondition(field, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(searchScope)));

    /* Create Condition from Filter */
    Type filterType = fFeedView.getFilter().getType();
    switch (filterType) {
      case SHOW_ALL:
        if (!withQuickSearch) {
          field = factory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "")); //$NON-NLS-1$
        }
        break;

      case SHOW_NEW:
        field = factory.createSearchField(INews.STATE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(INews.State.NEW)));
        break;

      case SHOW_RECENT:
        field = factory.createSearchField(INews.AGE_IN_DAYS, INews.class.getName());
        conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS_LESS_THAN, 2));
        break;

      case SHOW_LAST_5_DAYS:
        field = factory.createSearchField(INews.AGE_IN_DAYS, INews.class.getName());
        conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS_LESS_THAN, 6));
        break;

      case SHOW_STICKY:
        field = factory.createSearchField(INews.IS_FLAGGED, INews.class.getName());
        conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS, true));
        break;

      case SHOW_LABELED:
        field = factory.createSearchField(INews.LABEL, INews.class.getName());
        conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS, "*")); //$NON-NLS-1$
        break;

      case SHOW_UNREAD:
        field = factory.createSearchField(INews.STATE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)));
        break;
    }

    /* Also add Quick Search if required */
    if (withQuickSearch) {
      SearchTarget target = fFeedView.getFilter().getSearchTarget();
      String text = fSearchInput.getText();

      /* Convert to Wildcard Query */
      if (StringUtils.supportsTrailingWildcards(text))
        text = text + "*"; //$NON-NLS-1$

      switch (target) {
        case ALL:
          field = factory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, text));
          break;

        case HEADLINE:
          field = factory.createSearchField(INews.TITLE, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, text));
          break;

        case ATTACHMENTS:
          field = factory.createSearchField(INews.ATTACHMENTS_CONTENT, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, text));
          break;

        case AUTHOR:
          field = factory.createSearchField(INews.AUTHOR, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, text));
          break;

        case CATEGORY:
          field = factory.createSearchField(INews.CATEGORIES, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS, text));
          break;

        case LABELS:
          field = factory.createSearchField(INews.LABEL, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS, text));
          break;

        case SOURCE:
          field = factory.createSearchField(INews.SOURCE, INews.class.getName());
          conditions.add(factory.createSearchCondition(field, SearchSpecifier.IS, text));
          break;
      }
    }

    /* Create and Show SM Dialog */
    SearchMarkDialog dialog = new SearchMarkDialog(fParent.getShell(), OwlUI.getBookMarkExplorerSelection(), null, conditions, true);
    dialog.open();
  }

  private void onFilter(NewsFilter.Type type) {
    doFilter(type, true, true);
    EditorUtils.updateFilterAndGrouping(fFeedView);
  }

  void doFilter(final NewsFilter.Type type, boolean refresh, boolean saveSettings) {
    doFilter(type, refresh, saveSettings, null);
  }

  void doFilter(final NewsFilter.Type type, boolean refresh, boolean saveSettings, final Runnable browserRefreshRunnable) {
    Type oldType = fFeedView.getFilter().getType();
    boolean noChange = (oldType == type);

    /* Remember last filter type */
    if (type != Type.SHOW_ALL)
      fLastFilterType = type;
    else if (fFeedView.getFilter().getType() != Type.SHOW_ALL)
      fLastFilterType = oldType;

    /* No need to refresh or save settings if nothing changed */
    if (noChange)
      return;

    /* Apply Type */
    fFeedView.getFilter().setType(type);
    fFilterGroupingLayoutToolBarManager.find(FILTER_ACTION).update();

    /* Refresh if set */
    if (refresh) {
      final Runnable uiRunnable = new Runnable() {
        @Override
        public void run() {
          if (browserRefreshRunnable != null) //If runnable is passed in, it will take care of refreshing
            fFeedView.getNewsBrowserControl().getViewer().setBlockRefresh(true);
          try {

            /* Only Refresh Table as Browser shows single News */
            NewsTableControl newsTable = fFeedView.getNewsTableControl();
            boolean isNewsTableVisible = fFeedView.isTableViewerVisible();
            if (newsTable != null && isNewsTableVisible)
              fFeedView.refreshTableViewer(true, false);

            /* Refresh All */
            else
              fFeedView.refresh(true, false);

            /* Update Selection */
            updateBrowserSelection();

            /* Execute passed in code if provided */
            if (browserRefreshRunnable != null)
              browserRefreshRunnable.run();
          } finally {
            if (browserRefreshRunnable != null)
              fFeedView.getNewsBrowserControl().getViewer().setBlockRefresh(false);
          }
        }
      };

      /* Filter has changed - ask Feedview to revalidate caches in Background Thread */
      if (oldType != type) {
        JobRunner.runInBackgroundWithBusyIndicator(new Runnable() {
          @Override
          public void run() {

            /* Potential Long-op running in Background */
            fFeedView.revalidateCaches();

            /* Execute UI Code in UI Thread again */
            JobRunner.runInUIThread(fParent, uiRunnable);
          }
        });
      }

      /* No Filter Change, directly run UI Code */
      else
        uiRunnable.run();
    }

    /* Update Settings */
    if (saveSettings)
      saveIntegerValue(DefaultPreferences.BM_NEWS_FILTERING, type.ordinal());
  }

  private boolean needsCacheRevalidationFromSearch() {
    INewsMark mark = ((FeedViewInput) fFeedView.getEditorInput()).getMark();
    return (mark instanceof FolderNewsMark && mark.getNewsCount(INews.State.getVisible()) > NewsContentProvider.MAX_FOLDER_ELEMENTS);
  }

  private void doSearch(final NewsFilter.SearchTarget target) {
    fFeedView.getFilter().setSearchTarget(target);
    fSearchInput.setMessage(fFeedView.getFilter().getSearchTarget().getName());
    fSearchInput.setFocus();

    if (fSearchInput.getText().length() > 0) {
      if (needsCacheRevalidationFromSearch())
        fFeedView.revalidateCaches();
      fFeedView.refresh(true, false);
      updateBrowserSelection();
    }

    /* Update Settings */
    JobRunner.runInBackgroundThread(new Runnable() {
      @Override
      public void run() {
        fGlobalPreferences.putInteger(DefaultPreferences.FV_SEARCH_TARGET, target.ordinal());
      }
    });
  }

  /* News Group */
  private void createGrouperBar() {
    final NewsGrouping grouping = fFeedView.getGrouper();

    final IAction newsGroup = new Action(Messages.FilterBar_GROUP_NEWS, IAction.AS_DROP_DOWN_MENU) {
      @Override
      public void run() {

        /* Toggle Ungrouped */
        if (fFeedView.getGrouper().getType() != NewsGrouping.Type.NO_GROUPING)
          onGrouping(NewsGrouping.Type.NO_GROUPING);

        /* Toggle back to previous grouping */
        else if (fLastGroupType != null)
          onGrouping(fLastGroupType);

        /* Show Menu */
        else
          OwlUI.positionDropDownMenu(this, fFilterGroupingLayoutToolBarManager);
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        if (grouping.getType() == NewsGrouping.Type.NO_GROUPING)
          return OwlUI.getImageDescriptor("icons/etool16/group.gif"); //$NON-NLS-1$

        return OwlUI.getImageDescriptor("icons/etool16/group_active.gif"); //$NON-NLS-1$
      }

      @Override
      public String getText() {
        return grouping.getType().getDisplayName();
      }
    };

    newsGroup.setId(GROUP_ACTION);

    newsGroup.setMenuCreator(new ContextMenuCreator() {

      @Override
      public Menu createMenu(Control parent) {
        Menu menu = new Menu(parent);

        /* Group: None */
        final MenuItem noGrouping = new MenuItem(menu, SWT.RADIO);
        noGrouping.setText(NewsGrouping.Type.NO_GROUPING.getName());
        noGrouping.setSelection(grouping.getType() == NewsGrouping.Type.NO_GROUPING);
        noGrouping.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (noGrouping.getSelection() && grouping.getType() != NewsGrouping.Type.NO_GROUPING)
              onGrouping(NewsGrouping.Type.NO_GROUPING);
          }
        });
        menu.setDefaultItem(noGrouping);

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Group: By Date */
        final MenuItem groupByDate = new MenuItem(menu, SWT.RADIO);
        groupByDate.setText(NewsGrouping.Type.GROUP_BY_DATE.getName());
        groupByDate.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_DATE);
        groupByDate.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByDate.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_DATE)
              onGrouping(NewsGrouping.Type.GROUP_BY_DATE);
          }
        });

        /* Group: By Author */
        final MenuItem groupByAuthor = new MenuItem(menu, SWT.RADIO);
        groupByAuthor.setText(NewsGrouping.Type.GROUP_BY_AUTHOR.getName());
        groupByAuthor.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_AUTHOR);
        groupByAuthor.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByAuthor.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_AUTHOR)
              onGrouping(NewsGrouping.Type.GROUP_BY_AUTHOR);
          }
        });

        /* Group: By Category */
        final MenuItem groupByCategory = new MenuItem(menu, SWT.RADIO);
        groupByCategory.setText(NewsGrouping.Type.GROUP_BY_CATEGORY.getName());
        groupByCategory.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_CATEGORY);
        groupByCategory.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByCategory.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_CATEGORY)
              onGrouping(NewsGrouping.Type.GROUP_BY_CATEGORY);
          }
        });

        /* Group: By Topic */
        final MenuItem groupByTopic = new MenuItem(menu, SWT.RADIO);
        groupByTopic.setText(NewsGrouping.Type.GROUP_BY_TOPIC.getName());
        groupByTopic.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_TOPIC);
        groupByTopic.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByTopic.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_TOPIC)
              onGrouping(NewsGrouping.Type.GROUP_BY_TOPIC);
          }
        });

        /* Group: By Feed */
        final MenuItem groupByFeed = new MenuItem(menu, SWT.RADIO);
        groupByFeed.setText(NewsGrouping.Type.GROUP_BY_FEED.getName());
        groupByFeed.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_FEED);
        groupByFeed.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByFeed.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_FEED)
              onGrouping(NewsGrouping.Type.GROUP_BY_FEED);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Group: By State */
        final MenuItem groupByState = new MenuItem(menu, SWT.RADIO);
        groupByState.setText(NewsGrouping.Type.GROUP_BY_STATE.getName());
        groupByState.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_STATE);
        groupByState.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByState.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_STATE)
              onGrouping(NewsGrouping.Type.GROUP_BY_STATE);
          }
        });

        /* Group: By Stickyness */
        final MenuItem groupByStickyness = new MenuItem(menu, SWT.RADIO);
        groupByStickyness.setText(NewsGrouping.Type.GROUP_BY_STICKY.getName());
        groupByStickyness.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_STICKY);
        groupByStickyness.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByStickyness.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_STICKY)
              onGrouping(NewsGrouping.Type.GROUP_BY_STICKY);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Group: By Label */
        final MenuItem groupByLabel = new MenuItem(menu, SWT.RADIO);
        groupByLabel.setText(NewsGrouping.Type.GROUP_BY_LABEL.getName());
        groupByLabel.setSelection(grouping.getType() == NewsGrouping.Type.GROUP_BY_LABEL);
        groupByLabel.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByLabel.getSelection() && grouping.getType() != NewsGrouping.Type.GROUP_BY_LABEL)
              onGrouping(NewsGrouping.Type.GROUP_BY_LABEL);
          }
        });

        return menu;
      }
    });

    ActionContributionItem item = new ActionContributionItem(newsGroup);
    item.setMode(ActionContributionItem.MODE_FORCE_TEXT);

    fFilterGroupingLayoutToolBarManager.add(item);
  }

  /* Layout */
  private void createLayoutBar() {
    final IAction newsLayout = new Action("", IAction.AS_DROP_DOWN_MENU) { //$NON-NLS-1$
      @Override
      public void run() {
        OwlUI.positionDropDownMenu(this, fFilterGroupingLayoutToolBarManager);
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        Layout currentLayout = getLayout();
        switch (currentLayout) {
          case CLASSIC:
            return OwlUI.getImageDescriptor("icons/obj16/classic_layout.gif"); //$NON-NLS-1$
          case VERTICAL:
            return OwlUI.getImageDescriptor("icons/obj16/vertical_layout.gif"); //$NON-NLS-1$
          case LIST:
            return OwlUI.getImageDescriptor("icons/obj16/list_layout.gif"); //$NON-NLS-1$
          case NEWSPAPER:
            return OwlUI.getImageDescriptor("icons/obj16/newspaper_layout.gif"); //$NON-NLS-1$
          case HEADLINES:
            return OwlUI.getImageDescriptor("icons/obj16/headlines_layout.gif"); //$NON-NLS-1$
        }

        return OwlUI.getImageDescriptor("icons/obj16/classic_layout.gif"); //$NON-NLS-1$
      }

      @Override
      public String getText() {
        Layout currentLayout = getLayout();
        return currentLayout.getName();
      }
    };

    newsLayout.setId(LAYOUT_ACTION);
    newsLayout.setMenuCreator(new ContextMenuCreator() {

      @Override
      public Menu createMenu(Control parent) {
        Layout currentLayout = getLayout();
        Menu menu = new Menu(parent);

        Layout[] layouts = new Layout[] { Layout.CLASSIC, Layout.VERTICAL, Layout.LIST, Layout.NEWSPAPER, Layout.HEADLINES };
        for (final Layout layout : layouts) {
          final MenuItem layoutMenuItem = new MenuItem(menu, SWT.RADIO);
          layoutMenuItem.setText(layout.getName());
          layoutMenuItem.setSelection(layout == currentLayout);
          layoutMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              if (layoutMenuItem.getSelection())
                onLayout(layout);
            }
          });
        }

        return menu;
      }
    });

    ActionContributionItem item = new ActionContributionItem(newsLayout);
    item.setMode(ActionContributionItem.MODE_FORCE_TEXT);

    fFilterGroupingLayoutToolBarManager.add(item);
  }

  private void onGrouping(NewsGrouping.Type type) {
    doGrouping(type, true, true);
    EditorUtils.updateFilterAndGrouping(fFeedView);
  }

  private void onLayout(Layout layout) {
    doLayout(layout, true);
    EditorUtils.updateLayout();
  }

  void doLayout(final Layout layout, boolean saveSettings) {

    /* Update Settings */
    if (saveSettings)
      saveIntegerValue(DefaultPreferences.FV_LAYOUT, layout.ordinal());

    /* Update Toolbar */
    fFilterGroupingLayoutToolBarManager.find(LAYOUT_ACTION).update();
  }

  void doGrouping(final NewsGrouping.Type type, boolean refresh, boolean saveSettings) {
    boolean noChange = fFeedView.getGrouper().getType() == type;

    if (type != NewsGrouping.Type.NO_GROUPING)
      fLastGroupType = type;
    else if (fFeedView.getGrouper().getType() != NewsGrouping.Type.NO_GROUPING)
      fLastGroupType = fFeedView.getGrouper().getType();

    fFeedView.getGrouper().setType(type);
    fFilterGroupingLayoutToolBarManager.find(GROUP_ACTION).update();

    /* No need to refresh or save settings if nothing changed */
    if (noChange)
      return;

    /* Refresh if set */
    if (refresh) {
      NewsTableControl newsTable = fFeedView.getNewsTableControl();
      boolean isNewsTableVisible = fFeedView.isTableViewerVisible();
      try {

        /* Only Refresh Table as Browser shows single News */
        if (newsTable != null && isNewsTableVisible) {
          newsTable.setBlockNewsStateTracker(true);
          fFeedView.refreshTableViewer(true, false);
        }

        /* Refresh All */
        else
          fFeedView.refresh(true, false);
      } finally {
        if (newsTable != null && isNewsTableVisible)
          newsTable.setBlockNewsStateTracker(false);
      }
    }

    /* Update Settings */
    if (saveSettings)
      saveIntegerValue(DefaultPreferences.BM_NEWS_GROUPING, type.ordinal());
  }

  /*
   * This Method stores an Integer value to either the entity scope or global scope,
   * depending on if the current feed view input has the given setting stored in the
   * entity or not.
   */
  private void saveIntegerValue(String key, int value) {
    FeedViewInput input = ((FeedViewInput) fFeedView.getEditorInput());

    /* Save only into Entity if the Entity was configured with the given Settings before */
    IPreferenceScope entityPrefs = Owl.getPreferenceService().getEntityScope(input.getMark());
    if (entityPrefs.hasKey(key)) {
      entityPrefs.putInteger(key, value);
      if (input.getMark() instanceof FolderNewsMark)
        DynamicDAO.save(((FolderNewsMark) input.getMark()).getFolder());
      else
        DynamicDAO.save(input.getMark());
    }

    /* Save Globally */
    else
      fGlobalPreferences.putInteger(key, value);
  }
}
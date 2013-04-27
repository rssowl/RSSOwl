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

package org.rssowl.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.keys.IBindingService;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.OwlUI.Layout;
import org.rssowl.ui.internal.OwlUI.PageSize;
import org.rssowl.ui.internal.actions.ArchiveNewsAction;
import org.rssowl.ui.internal.actions.AssignLabelsAction;
import org.rssowl.ui.internal.actions.AutomateFilterAction;
import org.rssowl.ui.internal.actions.CopyLinkAction;
import org.rssowl.ui.internal.actions.CreateFilterAction;
import org.rssowl.ui.internal.actions.CreateFilterAction.PresetAction;
import org.rssowl.ui.internal.actions.FindAction;
import org.rssowl.ui.internal.actions.LabelAction;
import org.rssowl.ui.internal.actions.MakeNewsStickyAction;
import org.rssowl.ui.internal.actions.MarkAllNewsReadAction;
import org.rssowl.ui.internal.actions.MoveCopyNewsToBinAction;
import org.rssowl.ui.internal.actions.OpenInBrowserAction;
import org.rssowl.ui.internal.actions.OpenInExternalBrowserAction;
import org.rssowl.ui.internal.actions.OpenInNewTabAction;
import org.rssowl.ui.internal.actions.RedoAction;
import org.rssowl.ui.internal.actions.ReloadAllAction;
import org.rssowl.ui.internal.actions.ReloadTypesAction;
import org.rssowl.ui.internal.actions.SearchNewsAction;
import org.rssowl.ui.internal.actions.SendLinkAction;
import org.rssowl.ui.internal.actions.ToggleReadStateAction;
import org.rssowl.ui.internal.actions.UndoAction;
import org.rssowl.ui.internal.dialogs.CustomizeToolbarDialog;
import org.rssowl.ui.internal.dialogs.LabelDialog;
import org.rssowl.ui.internal.dialogs.LabelDialog.DialogMode;
import org.rssowl.ui.internal.dialogs.preferences.ManageLabelsPreferencePage;
import org.rssowl.ui.internal.dialogs.preferences.NotifierPreferencesPage;
import org.rssowl.ui.internal.dialogs.preferences.OverviewPreferencesPage;
import org.rssowl.ui.internal.dialogs.preferences.SharingPreferencesPage;
import org.rssowl.ui.internal.dialogs.welcome.TutorialPage.Chapter;
import org.rssowl.ui.internal.dialogs.welcome.TutorialWizard;
import org.rssowl.ui.internal.editors.browser.WebBrowserContext;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.editors.feed.FeedViewInput;
import org.rssowl.ui.internal.editors.feed.NewsColumn;
import org.rssowl.ui.internal.editors.feed.NewsColumnViewModel;
import org.rssowl.ui.internal.editors.feed.NewsFilter;
import org.rssowl.ui.internal.editors.feed.NewsGrouping;
import org.rssowl.ui.internal.filter.DownloadAttachmentsNewsAction;
import org.rssowl.ui.internal.handler.RemoveLabelsHandler;
import org.rssowl.ui.internal.handler.TutorialHandler;
import org.rssowl.ui.internal.services.DownloadService.DownloadRequest;
import org.rssowl.ui.internal.util.BrowserUtils;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.util.ModelUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;
import org.rssowl.ui.internal.views.explorer.BookMarkFilter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

  /** Name of the "Manage Extensions" SubMenu */
  public static final String M_MANAGE_EXTENSIONS = "manageExtensions"; //$NON-NLS-1$

  /** Name of the View Top Menu */
  public static final String M_VIEW = "view"; //$NON-NLS-1$

  /** Start of the View Top Menu */
  public static final String M_VIEW_START = "viewStart"; //$NON-NLS-1$

  /** End of the View Top Menu */
  public static final String M_VIEW_END = "viewEnd"; //$NON-NLS-1$

  /* Local Resource Manager (lives across entire application life) */
  private static ResourceManager fgResources = new LocalResourceManager(JFaceResources.getResources());

  private CoolBarAdvisor fCoolBarAdvisor;
  private IContributionItem fOpenWindowsItem;
  private IContributionItem fReopenEditors;
  private FindAction fFindAction;

  /**
   * @param configurer
   */
  public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
    super(configurer);
  }

  /*
   * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  protected void makeActions(IWorkbenchWindow window) {

    /* Menu: File */
    register(ActionFactory.SAVE_AS.create(window));
    register(ActionFactory.CLOSE.create(window));
    register(ActionFactory.CLOSE_ALL.create(window));
    register(ActionFactory.PRINT.create(window));
    register(ActionFactory.QUIT.create(window));

    fReopenEditors = ContributionItemFactory.REOPEN_EDITORS.create(window);

    /* Menu: Edit */
    register(ActionFactory.CUT.create(window));
    register(ActionFactory.COPY.create(window));
    register(ActionFactory.PASTE.create(window));
    register(ActionFactory.DELETE.create(window));
    register(ActionFactory.SELECT_ALL.create(window));
    register(ActionFactory.PROPERTIES.create(window));

    fFindAction = new FindAction();
    register(fFindAction);

    /* Menu: Tools */
    register(ActionFactory.PREFERENCES.create(window));

    /* Menu: Window */
    register(ActionFactory.OPEN_NEW_WINDOW.create(window));
    getAction(ActionFactory.OPEN_NEW_WINDOW.getId()).setText(Messages.ApplicationActionBarAdvisor_NEW_WINDOW);
    fOpenWindowsItem = ContributionItemFactory.OPEN_WINDOWS.create(window);

    //    register(ActionFactory.TOGGLE_COOLBAR.create(window));
    //    register(ActionFactory.RESET_PERSPECTIVE.create(window));
    //    register(ActionFactory.EDIT_ACTION_SETS.create(window));
    //    register(ActionFactory.ACTIVATE_EDITOR.create(window));
    //    register(ActionFactory.MAXIMIZE.create(window));
    //    register(ActionFactory.MINIMIZE.create(window));
    //    register(ActionFactory.NEXT_EDITOR.create(window));
    //    register(ActionFactory.PREVIOUS_EDITOR.create(window));
    //    register(ActionFactory.PREVIOUS_PART.create(window));
    //    register(ActionFactory.NEXT_PART.create(window));
    //    register(ActionFactory.SHOW_EDITOR.create(window));

    /* Menu: Help */
    // register(ActionFactory.INTRO.create(window));
    register(ActionFactory.ABOUT.create(window));
    getAction(ActionFactory.ABOUT.getId()).setText(Messages.ApplicationActionBarAdvisor_ABOUT_RSSOWL);

    /* CoolBar: Contextual Menu */
    register(ActionFactory.LOCK_TOOL_BAR.create(window));
  }

  /*
   * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
   */
  @Override
  protected void fillMenuBar(IMenuManager menuBar) {

    /* File Menu */
    createFileMenu(menuBar);

    /* Edit Menu */
    createEditMenu(menuBar);

    /* View Menu */
    createViewMenu(menuBar);

    /* Go Menu */
    createGoMenu(menuBar);

    /* Bookmarks Menu */
    createBookMarksMenu(menuBar);

    /* News Menu */
    createNewsMenu(menuBar);

    /* Allow Top-Level Menu Contributions here */
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

    /* Menu: Tools */
    createToolsMenu(menuBar);

    /* Window Menu */
    createWindowMenu(menuBar);

    /* Menu: Help */
    createHelpMenu(menuBar);
  }

  /* Menu: File */
  private void createFileMenu(IMenuManager menuBar) {
    MenuManager fileMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_FILE, IWorkbenchActionConstants.M_FILE);
    menuBar.add(fileMenu);

    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
    fileMenu.add(new Separator());

    fileMenu.add(getAction(ActionFactory.CLOSE.getId()));
    fileMenu.add(getAction(ActionFactory.CLOSE_ALL.getId()));
    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
    fileMenu.add(new Separator());
    fileMenu.add(getAction(ActionFactory.SAVE_AS.getId()));
    if (!Application.IS_MAC) { //Printing is not supported on Mac
      fileMenu.add(new Separator());
      fileMenu.add(getAction(ActionFactory.PRINT.getId()));
    }

    fileMenu.add(new Separator());
    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

    fileMenu.add(new Separator());
    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
    fileMenu.add(new Separator());

    if (Application.IS_LINUX) {
      fileMenu.add(getAction(ActionFactory.PROPERTIES.getId()));
      fileMenu.add(new Separator());
    }

    fileMenu.add(getAction(ActionFactory.QUIT.getId()));
  }

  /* Menu: Edit */
  private void createEditMenu(IMenuManager menuBar) {
    MenuManager editMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_EDIT, IWorkbenchActionConstants.M_EDIT);
    editMenu.add(getAction(ActionFactory.COPY.getId())); //Dummy action
    menuBar.add(editMenu);

    editMenu.setRemoveAllWhenShown(true);
    editMenu.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager editMenu) {
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
        editMenu.add(new Separator());

        editMenu.add(new UndoAction());
        editMenu.add(new RedoAction());
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        editMenu.add(new Separator());

        editMenu.add(getAction(ActionFactory.CUT.getId()));
        editMenu.add(getAction(ActionFactory.COPY.getId()));
        editMenu.add(new CopyLinkAction());
        editMenu.add(getAction(ActionFactory.PASTE.getId()));
        editMenu.add(new Separator());
        editMenu.add(getAction(ActionFactory.DELETE.getId()));
        editMenu.add(getAction(ActionFactory.SELECT_ALL.getId()));

        editMenu.add(new Separator());

        editMenu.add(new SearchNewsAction(OwlUI.getWindow()));
        editMenu.add(fFindAction);

        editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
        editMenu.add(new Separator());

        if (Application.IS_LINUX) {
          IAction preferences = getAction(ActionFactory.PREFERENCES.getId());
          preferences.setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/preferences.gif")); //$NON-NLS-1$
          editMenu.add(preferences);
        } else
          editMenu.add(getAction(ActionFactory.PROPERTIES.getId()));
      }
    });
  }

  /* Menu: View */
  private void createViewMenu(IMenuManager menuBar) {
    final IPreferenceScope globalPreferences = Owl.getPreferenceService().getGlobalScope();
    final IPreferenceScope eclipsePrefs = Owl.getPreferenceService().getEclipseScope();

    MenuManager viewMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_VIEW, M_VIEW);
    viewMenu.setRemoveAllWhenShown(true);
    menuBar.add(viewMenu);

    /* Add dummy action to show the top level menu */
    viewMenu.add(new Action("") { //$NON-NLS-1$
          @Override
          public void run() {}
        });

    /* Build Menu dynamically */
    viewMenu.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        final FeedView activeFeedView = OwlUI.getActiveFeedView();
        final IPreferenceScope entityPreferences = OwlUI.getActiveFeedViewPreferences();
        final Layout layout = OwlUI.getLayout(entityPreferences != null ? entityPreferences : globalPreferences);
        final PageSize pageSize = OwlUI.getPageSize(entityPreferences != null ? entityPreferences : globalPreferences);

        manager.add(new GroupMarker(M_VIEW_START));

        /* Layout */
        MenuManager layoutMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_LAYOUT);
        layoutMenu.add(new Action(Messages.ApplicationActionBarAdvisor_CLASSIC_LAYOUT, IAction.AS_RADIO_BUTTON) {
          @Override
          public void run() {
            if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
              updateLayoutPreferences(globalPreferences, entityPreferences, Layout.CLASSIC);
          }

          @Override
          public boolean isChecked() {
            return layout == Layout.CLASSIC;
          }
        });

        layoutMenu.add(new Action(Messages.ApplicationActionBarAdvisor_VERTICAL_LAYOUT, IAction.AS_RADIO_BUTTON) {
          @Override
          public void run() {
            if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
              updateLayoutPreferences(globalPreferences, entityPreferences, Layout.VERTICAL);
          }

          @Override
          public boolean isChecked() {
            return layout == Layout.VERTICAL;
          }
        });

        layoutMenu.add(new Action(Messages.ApplicationActionBarAdvisor_LIST_LAYOUT, IAction.AS_RADIO_BUTTON) {
          @Override
          public void run() {
            if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
              updateLayoutPreferences(globalPreferences, entityPreferences, Layout.LIST);
          }

          @Override
          public boolean isChecked() {
            return layout == Layout.LIST;
          }
        });

        layoutMenu.add(new Action(Messages.ApplicationActionBarAdvisor_NEWSPAPER_LAYOUT, IAction.AS_RADIO_BUTTON) {
          @Override
          public void run() {
            if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
              updateLayoutPreferences(globalPreferences, entityPreferences, Layout.NEWSPAPER);
          }

          @Override
          public boolean isChecked() {
            return layout == Layout.NEWSPAPER;
          }
        });

        layoutMenu.add(new Action(Messages.ApplicationActionBarAdvisor_HEADLINES_LAYOUT, IAction.AS_RADIO_BUTTON) {
          @Override
          public void run() {
            if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
              updateLayoutPreferences(globalPreferences, entityPreferences, Layout.HEADLINES);
          }

          @Override
          public boolean isChecked() {
            return layout == Layout.HEADLINES;
          }
        });

        /* Add the Page Size if using Newspaper or Headlines layout */
        if (layout == Layout.NEWSPAPER || layout == Layout.HEADLINES) {
          layoutMenu.add(new Separator());

          MenuManager pageMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_PAGE_SIZE);

          pageMenu.add(new Action(Messages.ApplicationActionBarAdvisor_T_ARTICLES, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
                updatePageSizePreferences(globalPreferences, entityPreferences, PageSize.TEN);
            }

            @Override
            public boolean isChecked() {
              return pageSize == PageSize.TEN;
            }
          });

          pageMenu.add(new Action(Messages.ApplicationActionBarAdvisor_TF_ARTICLES, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
                updatePageSizePreferences(globalPreferences, entityPreferences, PageSize.TWENTY_FIVE);
            }

            @Override
            public boolean isChecked() {
              return pageSize == PageSize.TWENTY_FIVE;
            }
          });

          pageMenu.add(new Action(Messages.ApplicationActionBarAdvisor_F_ARTICLES, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
                updatePageSizePreferences(globalPreferences, entityPreferences, PageSize.FIFTY);
            }

            @Override
            public boolean isChecked() {
              return pageSize == PageSize.FIFTY;
            }
          });

          pageMenu.add(new Action(Messages.ApplicationActionBarAdvisor_H_ARTICLES, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
                updatePageSizePreferences(globalPreferences, entityPreferences, PageSize.HUNDRED);
            }

            @Override
            public boolean isChecked() {
              return pageSize == PageSize.HUNDRED;
            }
          });

          pageMenu.add(new Separator());

          pageMenu.add(new Action(Messages.ApplicationActionBarAdvisor_ALL_ARTICLES, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
                updatePageSizePreferences(globalPreferences, entityPreferences, PageSize.NO_PAGING);
            }

            @Override
            public boolean isChecked() {
              return pageSize == PageSize.NO_PAGING;
            }
          });

          layoutMenu.add(pageMenu);
        }

        manager.add(layoutMenu);

        /* Columns */
        final boolean isColumnsEnabled = (layout != Layout.NEWSPAPER && layout != Layout.HEADLINES);
        MenuManager columnsMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_COLUMNS);
        final NewsColumnViewModel model = NewsColumnViewModel.loadFrom(entityPreferences != null ? entityPreferences : globalPreferences);
        NewsColumn[] columns = NewsColumn.values();
        for (final NewsColumn column : columns) {
          if (column.isSelectable()) {
            columnsMenu.add(new Action(column.getName(), IAction.AS_CHECK_BOX) {
              @Override
              public void run() {
                if (model.contains(column))
                  model.removeColumn(column);
                else
                  model.addColumn(column);

                updateColumnsPreferences(globalPreferences, entityPreferences, model, DefaultPreferences.BM_NEWS_COLUMNS);
              }

              @Override
              public boolean isChecked() {
                return model.contains(column);
              }

              @Override
              public boolean isEnabled() {
                return isColumnsEnabled;
              };
            });
          }
        }
        columnsMenu.add(new Separator());
        columnsMenu.add(new Action(Messages.ApplicationActionBarAdvisor_RESTORE_DEFAULT_COLUMNS) {
          @Override
          public void run() {
            NewsColumnViewModel defaultModel = NewsColumnViewModel.createDefault(false);
            updateColumnsPreferences(globalPreferences, entityPreferences, defaultModel, DefaultPreferences.BM_NEWS_COLUMNS, DefaultPreferences.BM_NEWS_SORT_COLUMN, DefaultPreferences.BM_NEWS_SORT_ASCENDING);
          }

          @Override
          public boolean isEnabled() {
            return isColumnsEnabled;
          };
        });

        manager.add(columnsMenu);

        /* Sorting */
        MenuManager sortingMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_SORT_BY);
        for (final NewsColumn column : columns) {
          if (column.isSelectable()) {
            sortingMenu.add(new Action(column.getName(), IAction.AS_RADIO_BUTTON) {
              @Override
              public void run() {
                if (super.isChecked()) { //Need to use parent scope to get real selection state from UI and not Model
                  model.setSortColumn(column);
                  updateColumnsPreferences(globalPreferences, entityPreferences, model, DefaultPreferences.BM_NEWS_SORT_COLUMN);
                }
              }

              @Override
              public boolean isChecked() {
                return model.getSortColumn().equals(column);
              }
            });
          }
        }

        sortingMenu.add(new Separator());

        sortingMenu.add(new Action(Messages.ApplicationActionBarAdvisor_ASCENDING, IAction.AS_RADIO_BUTTON) {
          @Override
          public void run() {
            if (super.isChecked()) { //Need to use parent scope to get real selection state from UI and not Model
              model.setAscending(true);
              updateColumnsPreferences(globalPreferences, entityPreferences, model, DefaultPreferences.BM_NEWS_SORT_ASCENDING);
            }
          }

          @Override
          public boolean isChecked() {
            return model.isAscending();
          }
        });

        sortingMenu.add(new Action(Messages.ApplicationActionBarAdvisor_DESCENDING, IAction.AS_RADIO_BUTTON) {
          @Override
          public void run() {
            if (super.isChecked()) { //Need to use parent scope to get real selection state from UI and not Model
              model.setAscending(false);
              updateColumnsPreferences(globalPreferences, entityPreferences, model, DefaultPreferences.BM_NEWS_SORT_ASCENDING);
            }
          }

          @Override
          public boolean isChecked() {
            return !model.isAscending();
          }
        });

        manager.add(sortingMenu);

        /* News Filter */
        manager.add(new Separator());
        MenuManager filterMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_FILTER_NEWS);

        int selectedFilterIndex;
        if (entityPreferences != null)
          selectedFilterIndex = ModelUtils.loadIntegerValueWithFallback(entityPreferences, DefaultPreferences.BM_NEWS_FILTERING, globalPreferences, DefaultPreferences.FV_FILTER_TYPE);
        else
          selectedFilterIndex = ModelUtils.loadIntegerValueWithFallback(globalPreferences, DefaultPreferences.BM_NEWS_FILTERING, globalPreferences, DefaultPreferences.FV_FILTER_TYPE);

        final NewsFilter.Type selectedFilter = NewsFilter.Type.values()[selectedFilterIndex];
        NewsFilter.Type[] filters = new NewsFilter.Type[] { NewsFilter.Type.SHOW_ALL, NewsFilter.Type.SHOW_NEW, NewsFilter.Type.SHOW_UNREAD, NewsFilter.Type.SHOW_STICKY, NewsFilter.Type.SHOW_LABELED, NewsFilter.Type.SHOW_RECENT, NewsFilter.Type.SHOW_LAST_5_DAYS };
        for (final NewsFilter.Type filter : filters) {
          filterMenu.add(new Action(filter.getName(), IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
                updateFilterAndGroupingPreferences(globalPreferences, entityPreferences, filter, null);
            };

            @Override
            public boolean isChecked() {
              return filter == selectedFilter;
            };
          });

          /* Add Separators to improve readability of filter options */
          if (filter == NewsFilter.Type.SHOW_ALL || filter == NewsFilter.Type.SHOW_UNREAD || filter == NewsFilter.Type.SHOW_LABELED)
            filterMenu.add(new Separator());
        }

        manager.add(filterMenu);

        /* News Grouping */
        MenuManager groupMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_GROUP_NEWS);

        int selectedGroupIndex;
        if (entityPreferences != null)
          selectedGroupIndex = ModelUtils.loadIntegerValueWithFallback(entityPreferences, DefaultPreferences.BM_NEWS_GROUPING, globalPreferences, DefaultPreferences.FV_GROUP_TYPE);
        else
          selectedGroupIndex = ModelUtils.loadIntegerValueWithFallback(globalPreferences, DefaultPreferences.BM_NEWS_GROUPING, globalPreferences, DefaultPreferences.FV_GROUP_TYPE);

        final NewsGrouping.Type selectedGroup = NewsGrouping.Type.values()[selectedGroupIndex];
        NewsGrouping.Type[] groups = new NewsGrouping.Type[] { NewsGrouping.Type.NO_GROUPING, NewsGrouping.Type.GROUP_BY_DATE, NewsGrouping.Type.GROUP_BY_AUTHOR, NewsGrouping.Type.GROUP_BY_CATEGORY, NewsGrouping.Type.GROUP_BY_TOPIC, NewsGrouping.Type.GROUP_BY_FEED, NewsGrouping.Type.GROUP_BY_STATE, NewsGrouping.Type.GROUP_BY_STICKY, NewsGrouping.Type.GROUP_BY_LABEL };
        for (final NewsGrouping.Type group : groups) {
          groupMenu.add(new Action(group.getName(), IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (super.isChecked()) //Need to use parent scope to get real selection state from UI and not Model
                updateFilterAndGroupingPreferences(globalPreferences, entityPreferences, null, group);
            };

            @Override
            public boolean isChecked() {
              return group == selectedGroup;
            };
          });

          /* Add Separators to improve readability of grouping options */
          if (group == NewsGrouping.Type.NO_GROUPING || group == NewsGrouping.Type.GROUP_BY_FEED || group == NewsGrouping.Type.GROUP_BY_STICKY)
            groupMenu.add(new Separator());
        }

        manager.add(groupMenu);

        /* Zoom (In, Out, Reset) */
        final boolean isZoomingEnabled = (activeFeedView != null && activeFeedView.isBrowserShowingNews());
        manager.add(new Separator());
        MenuManager zoomMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_ZOOM);

        /* Zoom In */
        zoomMenu.add(new Action(Messages.ApplicationActionBarAdvisor_ZOOM_IN) {
          @Override
          public void run() {
            OwlUI.zoomNewsText(true, false);
          }

          @Override
          public String getId() {
            return "org.rssowl.ui.ZoomInCommand"; //$NON-NLS-1$
          }

          @Override
          public String getActionDefinitionId() {
            return "org.rssowl.ui.ZoomInCommand"; //$NON-NLS-1$
          }

          @Override
          public boolean isEnabled() {
            return isZoomingEnabled;
          }
        });

        /* Zoom Out */
        zoomMenu.add(new Action(Messages.ApplicationActionBarAdvisor_ZOOM_OUT) {
          @Override
          public void run() {
            OwlUI.zoomNewsText(false, false);
          }

          @Override
          public String getId() {
            return "org.rssowl.ui.ZoomOutCommand"; //$NON-NLS-1$
          }

          @Override
          public String getActionDefinitionId() {
            return "org.rssowl.ui.ZoomOutCommand"; //$NON-NLS-1$
          }

          @Override
          public boolean isEnabled() {
            return isZoomingEnabled;
          }
        });

        /* Reset */
        zoomMenu.add(new Separator());
        zoomMenu.add(new Action(Messages.ApplicationActionBarAdvisor_RESET) {
          @Override
          public void run() {
            OwlUI.zoomNewsText(false, true);
          }

          @Override
          public String getId() {
            return "org.rssowl.ui.ZoomResetCommand"; //$NON-NLS-1$
          }

          @Override
          public String getActionDefinitionId() {
            return "org.rssowl.ui.ZoomResetCommand"; //$NON-NLS-1$
          }

          @Override
          public boolean isEnabled() {
            return isZoomingEnabled;
          }
        });

        manager.add(zoomMenu);

        /* Toolbars */
        manager.add(new Separator());
        final MenuManager toolbarsMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_TOOLBARS);
        toolbarsMenu.setRemoveAllWhenShown(true);
        toolbarsMenu.addMenuListener(new IMenuListener() {
          public void menuAboutToShow(IMenuManager manager) {
            boolean useExternalBrowser = OwlUI.useExternalBrowser();

            /* Main Toolbar */
            toolbarsMenu.add(new Action(Messages.ApplicationActionBarAdvisor_TOOLBAR, IAction.AS_CHECK_BOX) {
              @Override
              public void run() {
                ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;

                boolean isMainToolBarVisible = globalPreferences.getBoolean(DefaultPreferences.SHOW_TOOLBAR);
                configurer.setToolBarVisible(!isMainToolBarVisible, true);
                globalPreferences.putBoolean(DefaultPreferences.SHOW_TOOLBAR, !isMainToolBarVisible);
              }

              @Override
              public boolean isChecked() {
                return globalPreferences.getBoolean(DefaultPreferences.SHOW_TOOLBAR);
              }
            });

            /* Feed Toolbar */
            toolbarsMenu.add(new Action(Messages.ApplicationActionBarAdvisor_FEED_TOOLBAR, IAction.AS_CHECK_BOX) {
              @Override
              public void run() {
                boolean isFeedToolBarHidden = globalPreferences.getBoolean(DefaultPreferences.FV_FEED_TOOLBAR_HIDDEN);
                globalPreferences.putBoolean(DefaultPreferences.FV_FEED_TOOLBAR_HIDDEN, !isFeedToolBarHidden);

                /* Update opened Feedviews */
                EditorUtils.updateToolbarVisibility();
              }

              @Override
              public boolean isChecked() {
                return !globalPreferences.getBoolean(DefaultPreferences.FV_FEED_TOOLBAR_HIDDEN);
              }
            });

            /* Browser Toolbar */
            if (!useExternalBrowser) {
              toolbarsMenu.add(new Action(Messages.ApplicationActionBarAdvisor_BROWSER_TOOLBAR, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                  boolean isBrowserToolBarHidden = globalPreferences.getBoolean(DefaultPreferences.FV_BROWSER_TOOLBAR_HIDDEN);
                  globalPreferences.putBoolean(DefaultPreferences.FV_BROWSER_TOOLBAR_HIDDEN, !isBrowserToolBarHidden);

                  /* Update opened Feedviews */
                  EditorUtils.updateToolbarVisibility();
                }

                @Override
                public boolean isChecked() {
                  return !globalPreferences.getBoolean(DefaultPreferences.FV_BROWSER_TOOLBAR_HIDDEN);
                }
              });
            }
          }
        });
        manager.add(toolbarsMenu);

        /* Toggle State of Status Bar Visibility */
        manager.add(new Action(Messages.ApplicationActionBarAdvisor_STATUS, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;

            boolean isStatusVisible = globalPreferences.getBoolean(DefaultPreferences.SHOW_STATUS);
            configurer.setStatusVisible(!isStatusVisible, true);
            globalPreferences.putBoolean(DefaultPreferences.SHOW_STATUS, !isStatusVisible);
          }

          @Override
          public boolean isChecked() {
            return globalPreferences.getBoolean(DefaultPreferences.SHOW_STATUS);
          }
        });

        /* Toggle State of Bookmarks Visibility */
        manager.add(new Action(Messages.ApplicationActionBarAdvisor_BOOKMARKS, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            OwlUI.toggleBookmarks();
          }

          @Override
          public String getActionDefinitionId() {
            return "org.rssowl.ui.ToggleBookmarksCommand"; //$NON-NLS-1$
          }

          @Override
          public String getId() {
            return "org.rssowl.ui.ToggleBookmarksCommand"; //$NON-NLS-1$
          }

          @Override
          public boolean isChecked() {
            IWorkbenchPage page = OwlUI.getPage();
            if (page != null)
              return page.findView(BookMarkExplorer.VIEW_ID) != null;

            return false;
          }
        });

        /* Customize Toolbar */
        manager.add(new Separator());
        manager.add(new Action(Messages.ApplicationActionBarAdvisor_CUSTOMIZE_TOOLBAR) {
          @Override
          public void run() {

            /* Unhide Toolbar if hidden */
            ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;

            boolean isToolBarVisible = globalPreferences.getBoolean(DefaultPreferences.SHOW_TOOLBAR);
            if (!isToolBarVisible) {
              configurer.setToolBarVisible(true, true);
              globalPreferences.putBoolean(DefaultPreferences.SHOW_TOOLBAR, true);
            }

            /* Open Dialog to Customize Toolbar */
            CustomizeToolbarDialog dialog = new CustomizeToolbarDialog(getActionBarConfigurer().getWindowConfigurer().getWindow().getShell());
            if (dialog.open() == IDialogConstants.OK_ID)
              fCoolBarAdvisor.advise(true);
          }
        });

        /* Tabbed Browsing */
        manager.add(new Separator());
        manager.add(new Action(Messages.ApplicationActionBarAdvisor_TABBED_BROWSING, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            boolean tabbedBrowsingEnabled = isChecked();

            /* Disable Tabbed Browsing */
            if (tabbedBrowsingEnabled) {

              /* Close other Tabs if necessary */
              boolean doit = true;
              IWorkbenchPage page = OwlUI.getPage();
              if (page != null) {
                IEditorReference[] editorReferences = page.getEditorReferences();
                if (editorReferences.length > 1) {
                  MessageBox confirmDialog = new MessageBox(page.getWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                  confirmDialog.setText(Messages.ApplicationActionBarAdvisor_DISABLE_TABBED_BROWSING);
                  confirmDialog.setMessage(NLS.bind(Messages.ApplicationActionBarAdvisor_TABS_MESSAGE, editorReferences.length));
                  if (confirmDialog.open() == SWT.YES)
                    OwlUI.closeOtherEditors();
                  else
                    doit = false;
                }
              }

              /* Update Preferences */
              if (doit) {
                eclipsePrefs.putBoolean(DefaultPreferences.ECLIPSE_MULTIPLE_TABS, false);
                eclipsePrefs.putBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS, true);
                eclipsePrefs.putInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD, 1);
              }
            }

            /* Enable Tabbed Browsing */
            else {
              eclipsePrefs.putBoolean(DefaultPreferences.ECLIPSE_MULTIPLE_TABS, true);
              eclipsePrefs.putBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS, false);
              eclipsePrefs.putInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD, 5);
            }
          }

          @Override
          public boolean isChecked() {
            return OwlUI.isTabbedBrowsingEnabled();
          }
        });

        /* Fullscreen Mode */
        manager.add(new Action(Messages.ApplicationActionBarAdvisor_FULL_SCREEN, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            OwlUI.toggleFullScreen();
          }

          @Override
          public String getActionDefinitionId() {
            return "org.rssowl.ui.FullScreenCommand"; //$NON-NLS-1$
          }

          @Override
          public String getId() {
            return "org.rssowl.ui.FullScreenCommand"; //$NON-NLS-1$
          }

          @Override
          public boolean isChecked() {
            Shell shell = OwlUI.getActiveShell();
            if (shell != null)
              return shell.getFullScreen();

            return super.isChecked();
          }
        });

        /* Minimize */
        manager.add(new Action(Messages.ApplicationActionBarAdvisor_MINIMIZE) {
          @Override
          public void run() {
            Shell shell = OwlUI.getActiveShell();
            if (shell != null)
              shell.setMinimized(true);
          }

          @Override
          public String getActionDefinitionId() {
            return "org.rssowl.ui.MinimizeCommand"; //$NON-NLS-1$
          }

          @Override
          public String getId() {
            return "org.rssowl.ui.MinimizeCommand"; //$NON-NLS-1$
          }
        });

        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new GroupMarker(M_VIEW_START));
      }
    });
  }

  private void updatePageSizePreferences(IPreferenceScope globalPreferences, IPreferenceScope entityPreferences, PageSize pageSize) {

    /* Update Global */
    globalPreferences.putInteger(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE, pageSize.getPageSize());

    /* Update Entity if it was configured explicitly */
    if (entityPreferences != null) {
      if (entityPreferences.hasKey(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE)) {
        entityPreferences.delete(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE);
        entityPreferences.flush();
      }
    }
  }

  private void updateLayoutPreferences(IPreferenceScope globalPreferences, IPreferenceScope entityPreferences, Layout layout) {

    /* Update Global */
    globalPreferences.putInteger(DefaultPreferences.FV_LAYOUT, layout.ordinal());

    /* Update Entity if it was configured explicitly */
    if (entityPreferences != null) {
      if (entityPreferences.hasKey(DefaultPreferences.FV_LAYOUT)) {
        entityPreferences.delete(DefaultPreferences.FV_LAYOUT);
        entityPreferences.flush();
      }
    }

    /* Update Feed Views */
    EditorUtils.updateLayout();
  }

  private void updateFilterAndGroupingPreferences(IPreferenceScope globalPreferences, IPreferenceScope entityPreferences, NewsFilter.Type filter, NewsGrouping.Type grouping) {

    /* Update Global */
    if (filter != null)
      globalPreferences.putInteger(DefaultPreferences.BM_NEWS_FILTERING, filter.ordinal());
    if (grouping != null)
      globalPreferences.putInteger(DefaultPreferences.BM_NEWS_GROUPING, grouping.ordinal());

    /* Update Entity if it was configured explicitly */
    if (entityPreferences != null) {
      boolean flush = false;

      if (filter != null && entityPreferences.hasKey(DefaultPreferences.BM_NEWS_FILTERING)) {
        entityPreferences.delete(DefaultPreferences.BM_NEWS_FILTERING);
        flush = true;
      }

      if (grouping != null && entityPreferences.hasKey(DefaultPreferences.BM_NEWS_GROUPING)) {
        entityPreferences.delete(DefaultPreferences.BM_NEWS_GROUPING);
        flush = true;
      }

      if (flush)
        entityPreferences.flush();
    }

    /* Update Feed Views */
    EditorUtils.updateFilterAndGrouping();
  }

  private void updateColumnsPreferences(IPreferenceScope globalPreferences, IPreferenceScope entityPreferences, NewsColumnViewModel model, String... prefKeys) {

    /* Update Global */
    boolean saveColumns = contains(DefaultPreferences.BM_NEWS_COLUMNS, prefKeys);
    boolean saveSortColumn = contains(DefaultPreferences.BM_NEWS_SORT_COLUMN, prefKeys);
    boolean saveSortDirection = contains(DefaultPreferences.BM_NEWS_SORT_ASCENDING, prefKeys);

    model.saveTo(globalPreferences, false, saveColumns, saveSortColumn, saveSortDirection);

    /* Update Entity if it was configured explicitly */
    if (entityPreferences != null && prefKeys != null) {
      boolean flush = false;

      for (String prefKey : prefKeys) {
        if (entityPreferences.hasKey(prefKey)) {
          entityPreferences.delete(prefKey);
          flush = true;
        }
      }

      if (flush)
        entityPreferences.flush();
    }

    /* Update Feed Views */
    EditorUtils.updateColumns();
  }

  private boolean contains(String key, String... elements) {
    if (elements == null)
      return false;

    for (String element : elements) {
      if (key.equals(element))
        return true;
    }

    return false;
  }

  /* Menu: Go */
  private void createGoMenu(IMenuManager menuBar) {
    MenuManager goMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_GO, IWorkbenchActionConstants.M_NAVIGATE);
    menuBar.add(goMenu);

    goMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    goMenu.add(fReopenEditors);
  }

  /* Menu: Bookmarks */
  private void createBookMarksMenu(IMenuManager menuBar) {
    MenuManager bmMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_BOOKMARKS, "bookmarks"); //$NON-NLS-1$
    bmMenu.setRemoveAllWhenShown(true);
    bmMenu.add(new Action("") {}); //Dummy Action //$NON-NLS-1$
    bmMenu.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        fillBookMarksMenu(manager, getActionBarConfigurer().getWindowConfigurer().getWindow());
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });

    menuBar.add(bmMenu);
  }

  /* Menu News */
  private void createNewsMenu(IMenuManager menuBar) {
    final MenuManager newsMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_NEWS, "news"); //$NON-NLS-1$
    menuBar.add(newsMenu);
    newsMenu.setRemoveAllWhenShown(true);

    newsMenu.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        final IStructuredSelection selection;

        FeedView activeFeedView = OwlUI.getActiveFeedView();
        FeedViewInput activeInput = null;
        if (activeFeedView != null) {
          selection = (IStructuredSelection) activeFeedView.getSite().getSelectionProvider().getSelection();
          activeInput = (FeedViewInput) activeFeedView.getEditorInput();
        } else
          selection = StructuredSelection.EMPTY;

        boolean isEntityGroupSelected = ModelUtils.isEntityGroupSelected(selection);

        /* Open */
        if (!isEntityGroupSelected) {
          manager.add(new Separator("open")); //$NON-NLS-1$

          /* Open News in Browser */
          manager.add(new OpenInBrowserAction(selection, WebBrowserContext.createFrom(selection, activeFeedView)) {
            @Override
            public boolean isEnabled() {
              return !selection.isEmpty();
            }
          });

          /* Open Externally - Show only when internal browser is used */
          if (!selection.isEmpty() && !OwlUI.useExternalBrowser())
            manager.add(new OpenInExternalBrowserAction(selection));
        }

        /* Attachments */
        {
          fillAttachmentsMenu(manager, selection, getActionBarConfigurer().getWindowConfigurer().getWindow(), false);
        }

        /* Mark / Label */
        {
          manager.add(new Separator("mark")); //$NON-NLS-1$

          /* Mark */
          {
            MenuManager markMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_MARK, "mark"); //$NON-NLS-1$
            manager.add(markMenu);

            /* Mark as Read */
            IAction action = new ToggleReadStateAction(selection);
            action.setEnabled(!selection.isEmpty());
            markMenu.add(action);

            /* Mark All Read */
            action = new MarkAllNewsReadAction();
            action.setEnabled(activeFeedView != null);
            markMenu.add(action);

            /* Sticky */
            markMenu.add(new Separator());
            action = new MakeNewsStickyAction(selection);
            action.setEnabled(!selection.isEmpty());
            markMenu.add(action);
          }

          /* Label */
          fillLabelMenu(manager, selection, getActionBarConfigurer().getWindowConfigurer().getWindow(), false);
        }

        /* Move To / Copy To */
        if (!selection.isEmpty()) {
          manager.add(new Separator("movecopy")); //$NON-NLS-1$

          /* Load all news bins and sort by name */
          List<INewsBin> newsbins = new ArrayList<INewsBin>(DynamicDAO.loadAll(INewsBin.class));

          Comparator<INewsBin> comparator = new Comparator<INewsBin>() {
            public int compare(INewsBin o1, INewsBin o2) {
              return o1.getName().compareTo(o2.getName());
            };
          };

          Collections.sort(newsbins, comparator);

          /* Move To */
          MenuManager moveMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_MOVE_TO, "moveto"); //$NON-NLS-1$
          manager.add(moveMenu);

          for (INewsBin bin : newsbins) {
            if (activeInput != null && activeInput.getMark().equals(bin))
              continue;

            moveMenu.add(new MoveCopyNewsToBinAction(selection, bin, true));
          }

          moveMenu.add(new MoveCopyNewsToBinAction(selection, null, true));
          moveMenu.add(new Separator());
          moveMenu.add(new AutomateFilterAction(PresetAction.MOVE, selection));

          /* Copy To */
          MenuManager copyMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_COPY_TO, "copyto"); //$NON-NLS-1$
          manager.add(copyMenu);

          for (INewsBin bin : newsbins) {
            if (activeInput != null && activeInput.getMark().equals(bin))
              continue;

            copyMenu.add(new MoveCopyNewsToBinAction(selection, bin, false));
          }

          copyMenu.add(new MoveCopyNewsToBinAction(selection, null, false));
          copyMenu.add(new Separator());
          copyMenu.add(new AutomateFilterAction(PresetAction.COPY, selection));

          /* Archive */
          manager.add(new ArchiveNewsAction(selection));
        }

        /* Share */
        fillShareMenu(manager, selection, getActionBarConfigurer().getWindowConfigurer().getWindow(), false);

        /* Filter */
        if (!selection.isEmpty()) {
          manager.add(new Separator("filter")); //$NON-NLS-1$

          /* Create Filter */
          manager.add(new Action(Messages.ApplicationActionBarAdvisor_CREATE_FILTER) {
            @Override
            public void run() {
              CreateFilterAction action = new CreateFilterAction();
              action.selectionChanged(null, selection);
              action.run(null);
            }

            @Override
            public ImageDescriptor getImageDescriptor() {
              return OwlUI.FILTER;
            }

            @Override
            public String getActionDefinitionId() {
              return CreateFilterAction.ID;
            };

            @Override
            public String getId() {
              return CreateFilterAction.ID;
            };
          });
        }

        /* Update */
        {
          manager.add(new Separator("reload")); //$NON-NLS-1$

          /* Update */
          manager.add(new Action(Messages.ApplicationActionBarAdvisor_UPDATE) {
            @Override
            public void run() {
              IActionDelegate action = new ReloadTypesAction();
              action.selectionChanged(null, selection);
              action.run(null);
            }

            @Override
            public ImageDescriptor getImageDescriptor() {
              return OwlUI.getImageDescriptor("icons/elcl16/reload.gif"); //$NON-NLS-1$
            }

            @Override
            public ImageDescriptor getDisabledImageDescriptor() {
              return OwlUI.getImageDescriptor("icons/dlcl16/reload.gif"); //$NON-NLS-1$
            }

            @Override
            public boolean isEnabled() {
              return !selection.isEmpty() || OwlUI.getActiveFeedView() != null;
            }

            @Override
            public String getActionDefinitionId() {
              return ReloadTypesAction.ID;
            }

            @Override
            public String getId() {
              return ReloadTypesAction.ID;
            }
          });

          /* Update All */
          manager.add(new ReloadAllAction());
        }

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });
  }

  /* Menu: Tools */
  private void createToolsMenu(IMenuManager menuBar) {
    MenuManager toolsMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_TOOLS, OwlUI.M_TOOLS);
    menuBar.add(toolsMenu);

    /* Contributions */
    toolsMenu.add(new GroupMarker("begin")); //$NON-NLS-1$
    toolsMenu.add(new Separator());
    toolsMenu.add(new GroupMarker("middle")); //$NON-NLS-1$
    toolsMenu.add(new Separator("addons")); //$NON-NLS-1$
    toolsMenu.add(new Separator());
    toolsMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    toolsMenu.add(new Separator());
    toolsMenu.add(new GroupMarker("end")); //$NON-NLS-1$

    /* Preferences (Windows, Mac) */
    if (!Application.IS_LINUX) {
      toolsMenu.add(new Separator());
      IAction preferences = getAction(ActionFactory.PREFERENCES.getId());
      preferences.setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/preferences.gif")); //$NON-NLS-1$
      toolsMenu.add(preferences);
      if (Application.IS_MAC) {
        IContributionItem item = toolsMenu.find(ActionFactory.PREFERENCES.getId());
        if (item != null)
          item.setVisible(false);
      }
    }
  }

  /* Menu: Window */
  private void createWindowMenu(IMenuManager menuBar) {
    MenuManager windowMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_WINDOW, IWorkbenchActionConstants.M_WINDOW);
    menuBar.add(windowMenu);

    IAction openNewWindowAction = getAction(ActionFactory.OPEN_NEW_WINDOW.getId());
    openNewWindowAction.setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/newwindow.gif")); //$NON-NLS-1$
    windowMenu.add(openNewWindowAction);

    windowMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    windowMenu.add(fOpenWindowsItem);

    //    MenuManager showViewMenu = new MenuManager("&Show View");
    //    windowMenu.add(showViewMenu);
    //    showViewMenu.add(fShowViewMenu);
    //    windowMenu.add(new Separator());

    //    windowMenu.add(getAction(ActionFactory.EDIT_ACTION_SETS.getId()));
    //    windowMenu.add(getAction(ActionFactory.RESET_PERSPECTIVE.getId()));
    //    windowMenu.add(new Separator());
    //
    //    MenuManager navigationMenu = new MenuManager("&Navigation");
    //    windowMenu.add(navigationMenu);
    //
    //    navigationMenu.add(getAction(ActionFactory.SHOW_PART_PANE_MENU.getId()));
    //    navigationMenu.add(getAction(ActionFactory.SHOW_VIEW_MENU.getId()));
    //    navigationMenu.add(new Separator());
    //    navigationMenu.add(getAction(ActionFactory.MAXIMIZE.getId()));
    //    navigationMenu.add(getAction(ActionFactory.MINIMIZE.getId()));
    //    navigationMenu.add(new Separator());
    //    navigationMenu.add(getAction(ActionFactory.ACTIVATE_EDITOR.getId()));
    //    navigationMenu.add(getAction(ActionFactory.SHOW_EDITOR.getId()));
    //    navigationMenu.add(getAction(ActionFactory.NEXT_EDITOR.getId()));
    //    navigationMenu.add(getAction(ActionFactory.PREVIOUS_EDITOR.getId()));
    //    navigationMenu.add(getAction(ActionFactory.SHOW_OPEN_EDITORS.getId()));
    //    navigationMenu.add(getAction(ActionFactory.SHOW_WORKBOOK_EDITORS.getId()));
    //    navigationMenu.add(new Separator());
    //    navigationMenu.add(getAction(ActionFactory.NEXT_PART.getId()));
    //    navigationMenu.add(getAction(ActionFactory.PREVIOUS_PART.getId()));
  }

  /* Menu: Help */
  private void createHelpMenu(IMenuManager menuBar) {
    MenuManager helpMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_HELP, IWorkbenchActionConstants.M_HELP);
    menuBar.add(helpMenu);

    helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));

    /* Tutorial Wizard */
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_TUTORIAL) {
      @Override
      public void run() {
        TutorialWizard wizard = new TutorialWizard();
        OwlUI.openWizard(getActionBarConfigurer().getWindowConfigurer().getWindow().getShell(), wizard, false, false, null);
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        return OwlUI.getImageDescriptor("icons/elcl16/help.gif"); //$NON-NLS-1$
      }

      @Override
      public String getId() {
        return TutorialHandler.ID;
      }

      @Override
      public String getActionDefinitionId() {
        return TutorialHandler.ID;
      }
    });

    /* Google Reader Synchronization */
    if (SyncUtils.ENABLED) {
      helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_GOOGLE_READER_SYNC) {
        @Override
        public void run() {
          TutorialWizard wizard = new TutorialWizard(Chapter.SYNCHRONIZATION);
          OwlUI.openWizard(getActionBarConfigurer().getWindowConfigurer().getWindow().getShell(), wizard, false, false, null);
        }
      });
    }

    /* Link to Help */
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_FAQ) {
      @Override
      public void run() {
        BrowserUtils.openLinkExternal("http://www.rssowl.org/help"); //$NON-NLS-1$
      }
    });

    /* Link to Forum */
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_VISIT_FORUM) {
      @Override
      public void run() {
        BrowserUtils.openLinkExternal("http://sourceforge.net/projects/rssowl/forums/forum/296910"); //$NON-NLS-1$
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        return OwlUI.getImageDescriptor("icons/obj16/forum.gif"); //$NON-NLS-1$
      }
    });

    /* Show Key Bindings */
    helpMenu.add(new Separator());
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_SHOW_KEY_BINDINGS) {
      @Override
      public void run() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IBindingService bindingService = (IBindingService) workbench.getService(IBindingService.class);
        bindingService.openKeyAssistDialog();
      }
    });

    helpMenu.add(new Separator());

    /* Report Bugs */
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_REPORT_PROBLEMS) {
      @Override
      public void run() {
        BrowserUtils.openLinkExternal("http://dev.rssowl.org"); //$NON-NLS-1$
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        return OwlUI.getImageDescriptor("icons/elcl16/bug.gif"); //$NON-NLS-1$
      }
    });

    /* Export Log to File */
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_EXPORT_LOGFILE) {
      @Override
      public void run() {
        FileDialog dialog = new FileDialog(getActionBarConfigurer().getWindowConfigurer().getWindow().getShell(), SWT.SAVE);
        dialog.setText(Messages.ApplicationActionBarAdvisor_EXPORT_LOGFILE_DIALOG);
        dialog.setFilterExtensions(new String[] { "*.log" }); //$NON-NLS-1$
        dialog.setFileName("rssowl.log"); //$NON-NLS-1$
        dialog.setOverwrite(true);

        String file = dialog.open();
        if (StringUtils.isSet(file)) {
          try {

            /* Check for Log Message from Core to have a complete log */
            String logMessages = CoreUtils.getAndFlushLogMessages();
            if (logMessages != null && logMessages.length() > 0)
              Activator.safeLogError(logMessages, null);

            /* Help to find out where the log is coming from */
            Activator.safeLogInfo("Error Log Exported"); //$NON-NLS-1$

            /* Export Log File */
            File logFile = Platform.getLogFileLocation().toFile();
            InputStream inS;
            if (logFile.exists())
              inS = new FileInputStream(logFile);
            else
              inS = new ByteArrayInputStream(new byte[0]);
            FileOutputStream outS = new FileOutputStream(new File(file));
            CoreUtils.copy(inS, outS);

            /* Append a Report of Feeds that are not loading if any */
            String nl = System.getProperty("line.separator"); //$NON-NLS-1$
            if (!StringUtils.isSet(nl))
              nl = "\n"; //$NON-NLS-1$

            StringBuilder errorReport = new StringBuilder();
            Collection<IBookMark> bookmarks = DynamicDAO.getDAO(IBookMarkDAO.class).loadAll();
            for (IBookMark bookmark : bookmarks) {
              if (bookmark.isErrorLoading()) {
                Object errorObj = bookmark.getProperty(Controller.LOAD_ERROR_KEY);
                if (errorObj != null && errorObj instanceof String) {
                  errorReport.append(Controller.getDefault().createLogEntry(bookmark, null, (String) errorObj));
                  errorReport.append(nl).append(nl);
                }
              }
            }

            if (errorReport.length() > 0) {
              FileWriter writer = new FileWriter(new File(file), true);
              try {
                writer.append(nl).append(nl).append(nl);
                writer.write("--- Summary of Feeds that are not Loading -----------------------------------------------"); //$NON-NLS-1$
                writer.append(nl).append(nl);
                writer.write(errorReport.toString());
                writer.close();
              } finally {
                writer.close();
              }
            }
          } catch (FileNotFoundException e) {
            Activator.getDefault().logError(e.getMessage(), e);
          } catch (IOException e) {
            Activator.getDefault().logError(e.getMessage(), e);
          }
        }
      }
    });

    helpMenu.add(new Separator());

    /* Homepage */
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_HOMEPAGE) {
      @Override
      public void run() {
        BrowserUtils.openLinkExternal("http://www.rssowl.org"); //$NON-NLS-1$
      }
    });

    /* License */
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_LICENSE) {
      @Override
      public void run() {
        BrowserUtils.openLinkExternal("http://www.rssowl.org/legal/epl-v10.html"); //$NON-NLS-1$
      }
    });

    /* Donate */
    helpMenu.add(new Separator());
    helpMenu.add(new Action(Messages.ApplicationActionBarAdvisor_DONATE) {
      @Override
      public void run() {
        BrowserUtils.openLinkExternal("http://sourceforge.net/donate/index.php?group_id=86683"); //$NON-NLS-1$
      }
    });

    helpMenu.add(new Separator());

    helpMenu.add(new Separator());
    helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
    helpMenu.add(new Separator());

    helpMenu.add(getAction(ActionFactory.ABOUT.getId()));
    if (Application.IS_MAC) {
      IContributionItem item = helpMenu.find(ActionFactory.ABOUT.getId());
      if (item != null)
        item.setVisible(false);
    }
  }

  /*
   * @see org.eclipse.ui.application.ActionBarAdvisor#fillStatusLine(org.eclipse.jface.action.IStatusLineManager)
   */
  @Override
  protected void fillStatusLine(IStatusLineManager statusLine) {
    super.fillStatusLine(statusLine);
  }

  /*
   * @see org.eclipse.ui.application.ActionBarAdvisor#fillActionBars(int)
   */
  @Override
  public void fillActionBars(int flags) {
    super.fillActionBars(flags);
  }

  /**
   * @param trayItem
   * @param shell
   * @param advisor
   */
  protected void fillTrayItem(IMenuManager trayItem, final Shell shell, final ApplicationWorkbenchWindowAdvisor advisor) {
    trayItem.add(new ReloadAllAction(false));
    trayItem.add(new Separator());

    trayItem.add(new Action(Messages.ApplicationActionBarAdvisor_CONFIGURE_NOTIFICATIONS) {
      @Override
      public void run() {
        advisor.restoreFromTray(shell);
        PreferencesUtil.createPreferenceDialogOn(shell, NotifierPreferencesPage.ID, null, null).open();
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        return OwlUI.getImageDescriptor("icons/elcl16/notification.gif"); //$NON-NLS-1$
      }
    });

    trayItem.add(new Action(Messages.ApplicationActionBarAdvisor_PREFERENCES) {
      @Override
      public void run() {
        advisor.restoreFromTray(shell);
        PreferencesUtil.createPreferenceDialogOn(shell, OverviewPreferencesPage.ID, null, null).open();
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        return OwlUI.getImageDescriptor("icons/elcl16/preferences.gif"); //$NON-NLS-1$
      }
    });

    trayItem.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

    trayItem.add(new Action(Messages.ApplicationActionBarAdvisor_EXIT) {
      @Override
      public void run() {
        PlatformUI.getWorkbench().close();
      }
    });
  }

  /*
   * @see org.eclipse.ui.application.ActionBarAdvisor#fillCoolBar(org.eclipse.jface.action.ICoolBarManager)
   */
  @Override
  protected void fillCoolBar(ICoolBarManager coolBar) {

    /* CoolBar Context Menu */
    MenuManager coolBarContextMenuManager = new MenuManager(null, "org.rssowl.ui.CoolBarContextMenu"); //$NON-NLS-1$
    coolBar.setContextMenuManager(coolBarContextMenuManager);

    /* Customize Coolbar */
    coolBarContextMenuManager.add(new Action(Messages.ApplicationActionBarAdvisor_CUSTOMIZE_TOOLBAR) {
      @Override
      public void run() {
        CustomizeToolbarDialog dialog = new CustomizeToolbarDialog(getActionBarConfigurer().getWindowConfigurer().getWindow().getShell());
        if (dialog.open() == IDialogConstants.OK_ID)
          fCoolBarAdvisor.advise(true);
      }
    });

    /* Lock Coolbar  */
    coolBarContextMenuManager.add(new Separator());
    IAction lockToolbarAction = getAction(ActionFactory.LOCK_TOOL_BAR.getId());
    lockToolbarAction.setText(Messages.ApplicationActionBarAdvisor_LOCK_TOOLBAR);
    coolBarContextMenuManager.add(lockToolbarAction);

    /* Toggle State of Toolbar Visibility */
    coolBarContextMenuManager.add(new Action(Messages.ApplicationActionBarAdvisor_HIDE_TOOLBAR) {
      @Override
      public void run() {
        ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;
        configurer.setToolBarVisible(false, true);
        Owl.getPreferenceService().getGlobalScope().putBoolean(DefaultPreferences.SHOW_TOOLBAR, false);
      }
    });

    /* Support for more Contributions */
    coolBarContextMenuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

    /* Coolbar Advisor */
    fCoolBarAdvisor = new CoolBarAdvisor(coolBar, getActionBarConfigurer().getWindowConfigurer().getWindow());
    fCoolBarAdvisor.advise();
  }

  /**
   * @param manager the {@link IMenuManager} to fill this menu into.
   * @param selection the current {@link IStructuredSelection} of {@link INews}.
   * @param shellProvider a {@link IShellProvider} for dialogs.
   * @param directMenu if <code>true</code> directly fill all items to the menu,
   * otherwise create a sub menu.
   */
  public static void fillAttachmentsMenu(IMenuManager manager, final IStructuredSelection selection, final IShellProvider shellProvider, boolean directMenu) {
    final List<Pair<IAttachment, URI>> attachments = ModelUtils.getAttachmentLinks(selection);
    if (!attachments.isEmpty()) {
      manager.add(new Separator("attachments")); //$NON-NLS-1$

      /* Either as direct Menu or Submenu */
      IMenuManager attachmentMenu;
      if (directMenu)
        attachmentMenu = manager;
      else {
        attachmentMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_ATTACHMENTS, "attachments"); //$NON-NLS-1$
        manager.add(attachmentMenu);
      }

      final IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();

      /* Offer to Download All */
      if (attachments.size() > 1) {
        int sumBytes = 0;
        for (Pair<IAttachment, URI> attachment : attachments) {
          if (attachment.getFirst().getLength() > 0) {
            sumBytes += attachment.getFirst().getLength();
          } else {
            sumBytes = 0;
            break;
          }
        }
        String sumSize = OwlUI.getSize(sumBytes);

        Action downloadAllAction = new Action(sumSize != null ? (NLS.bind(Messages.ApplicationActionBarAdvisor_DOWNLOAD_ALL_WITH_SIZE, sumSize)) : (Messages.ApplicationActionBarAdvisor_DOWNLOAD_ALL)) {
          @Override
          public void run() {
            DirectoryDialog dialog = new DirectoryDialog(shellProvider.getShell(), SWT.None);
            dialog.setText(Messages.ApplicationActionBarAdvisor_SELECT_FOLDER_FOR_DOWNLOADS);

            String downloadFolder = preferences.getString(DefaultPreferences.DOWNLOAD_FOLDER);
            if (StringUtils.isSet(downloadFolder) && new File(downloadFolder).exists())
              dialog.setFilterPath(downloadFolder);

            String folder = dialog.open();
            if (StringUtils.isSet(folder)) {
              for (Pair<IAttachment, URI> attachment : attachments) {
                Controller.getDefault().getDownloadService().download(DownloadRequest.createAttachmentDownloadRequest(attachment.getFirst(), attachment.getSecond(), new File(folder), true, null));
              }

              /* Remember Download Folder in Settings */
              preferences.putString(DefaultPreferences.DOWNLOAD_FOLDER, folder);
            }
          }
        };
        downloadAllAction.setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/save_all.gif")); //$NON-NLS-1$
        attachmentMenu.add(downloadAllAction);
        attachmentMenu.add(new Separator());
      }

      /* Collect openable Attachments that have already been downloaded */
      List<Action> openActions = new ArrayList<Action>(1);
      Set<String> downloadLocations = getDownloadLocations();

      /* Offer Download Action for each */
      for (final Pair<IAttachment, URI> attachmentPair : attachments) {
        IAttachment attachment = attachmentPair.getFirst();
        final String fileName = URIUtils.getFile(attachmentPair.getSecond(), OwlUI.getExtensionForMime(attachment.getType()));
        String size = OwlUI.getSize(attachment.getLength());

        Action action = new Action(size != null ? (NLS.bind(Messages.ApplicationActionBarAdvisor_FILE_SIZE, fileName, size)) : (fileName)) {
          @Override
          public void run() {
            FileDialog dialog = new FileDialog(shellProvider.getShell(), SWT.SAVE);
            dialog.setText(Messages.ApplicationActionBarAdvisor_SELECT_FILE_FOR_DOWNLOAD);
            dialog.setFileName(Application.IS_WINDOWS ? CoreUtils.getSafeFileNameForWindows(fileName) : fileName);
            dialog.setOverwrite(true);

            String downloadFolder = preferences.getString(DefaultPreferences.DOWNLOAD_FOLDER);
            if (StringUtils.isSet(downloadFolder) && new File(downloadFolder).exists())
              dialog.setFilterPath(downloadFolder);

            String selectedFileName = dialog.open();
            if (StringUtils.isSet(selectedFileName)) {
              File file = new File(selectedFileName);
              Controller.getDefault().getDownloadService().download(DownloadRequest.createAttachmentDownloadRequest(attachmentPair.getFirst(), attachmentPair.getSecond(), file.getParentFile(), true, file.getName()));

              /* Remember Download Folder in Settings */
              preferences.putString(DefaultPreferences.DOWNLOAD_FOLDER, file.getParentFile().toString());
            }
          }
        };

        action.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/save_as.gif")); //$NON-NLS-1$
        attachmentMenu.add(action);

        /* Check if Attachment already exists and offer Open Action then */
        String usedFileName = Application.IS_WINDOWS ? CoreUtils.getSafeFileNameForWindows(fileName) : fileName;
        if (shouldOfferOpenAction(usedFileName)) {
          for (String downloadLocation : downloadLocations) {
            final File downloadedFile = new File(downloadLocation, usedFileName);
            if (downloadedFile.exists()) {
              Action openAction = new Action(NLS.bind(Messages.ApplicationActionBarAdvisor_OPEN_FILE, fileName)) {
                @Override
                public void run() {
                  Program.launch(downloadedFile.toString());
                }
              };

              openAction.setImageDescriptor(OwlUI.getAttachmentImage(fileName, attachmentPair.getFirst().getType()));
              openActions.add(openAction);

              break;
            }
          }
        }
      }

      boolean separate = true;

      /* Offer Open Action for each downloaded */
      if (!openActions.isEmpty()) {
        attachmentMenu.add(new Separator());
        for (Action openAction : openActions) {
          attachmentMenu.add(openAction);
        }
      }

      /* Offer Copy Action for Attachment Link */
      else if (attachments.size() == 1) {
        separate = false;
        attachmentMenu.add(new Separator());
        CopyLinkAction action = new CopyLinkAction();
        action.setIgnoreActiveSelection(true);
        action.selectionChanged(action, new StructuredSelection(attachments.iterator().next().getFirst()));
        attachmentMenu.add(action);
      }

      /* Offer to Automize Downloading */
      if (separate)
        attachmentMenu.add(new Separator());
      attachmentMenu.add(new AutomateFilterAction(PresetAction.DOWNLOAD, selection));
    }
  }

  private static boolean shouldOfferOpenAction(String filename) {
    if (Application.IS_WINDOWS)
      return !filename.endsWith(".exe") && !filename.endsWith(".bat") && !filename.endsWith(".com"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    return true;
  }

  private static Set<String> getDownloadLocations() {
    IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
    Set<String> locations = new HashSet<String>(1);

    /* Check Preference */
    String folderPath = preferences.getString(DefaultPreferences.DOWNLOAD_FOLDER);
    if (folderPath != null) {
      File folder = new File(folderPath);
      if (folder.exists())
        locations.add(folderPath);
    }

    /* Check Filters */
    Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
    for (ISearchFilter filter : filters) {
      List<IFilterAction> actions = filter.getActions();
      for (IFilterAction action : actions) {
        if (DownloadAttachmentsNewsAction.ID.equals(action.getActionId()) && action.getData() instanceof String) {
          folderPath = (String) action.getData();
          if (folderPath != null) {
            File folder = new File(folderPath);
            if (folder.exists())
              locations.add(folderPath);
          }
        }
      }
    }

    return locations;
  }

  /**
   * @param manager the {@link IMenuManager} to fill this menu into.
   * @param selection the current {@link IStructuredSelection} of {@link INews}.
   * @param shellProvider a {@link IShellProvider} for dialogs.
   * @param directMenu if <code>true</code> directly fill all items to the menu,
   * otherwise create a sub menu.
   */
  public static void fillShareMenu(IMenuManager manager, final IStructuredSelection selection, final IShellProvider shellProvider, boolean directMenu) {
    manager.add(new Separator("share")); //$NON-NLS-1$

    /* Either as direct Menu or Submenu */
    IMenuManager shareMenu;
    if (directMenu)
      shareMenu = manager;
    else {
      shareMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_SHARE_NEWS, OwlUI.SHARE, "sharenews"); //$NON-NLS-1$
      manager.add(shareMenu);
    }

    /* List all selected Share Providers  */
    final boolean isEnabled = !selection.isEmpty() && !ModelUtils.isEntityGroupSelected(selection);
    List<ShareProvider> providers = Controller.getDefault().getShareProviders();
    for (final ShareProvider provider : providers) {
      if (provider.isEnabled()) {
        shareMenu.add(new Action(provider.getName()) {
          @Override
          public void run() {
            Controller.getDefault().share(selection, provider);
          };

          @Override
          public ImageDescriptor getImageDescriptor() {
            if (StringUtils.isSet(provider.getIconPath()))
              return OwlUI.getImageDescriptor(provider.getPluginId(), provider.getIconPath());

            return super.getImageDescriptor();
          };

          @Override
          public String getText() {
            IBindingService bs = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
            TriggerSequence binding = bs.getBestActiveBindingFor(provider.getId());

            return binding != null ? NLS.bind(Messages.ApplicationActionBarAdvisor_SHARE_BINDING, provider.getName(), binding.format()) : provider.getName();
          }

          @Override
          public boolean isEnabled() {
            return isEnabled;
          }

          @Override
          public String getActionDefinitionId() {
            return SendLinkAction.ID.equals(provider.getId()) ? SendLinkAction.ID : super.getActionDefinitionId();
          }

          @Override
          public String getId() {
            return SendLinkAction.ID.equals(provider.getId()) ? SendLinkAction.ID : super.getId();
          }
        });
      }
    }

    /* Allow to Configure Providers */
    shareMenu.add(new Separator());
    shareMenu.add(new Action(Messages.ApplicationActionBarAdvisor_CONFIGURE) {
      @Override
      public void run() {
        PreferencesUtil.createPreferenceDialogOn(shellProvider.getShell(), SharingPreferencesPage.ID, null, null).open();
      };
    });
  }

  /**
   * @param manager the {@link IMenuManager} to fill this menu into.
   * @param selection the current {@link IStructuredSelection} of {@link INews}.
   * @param shellProvider a {@link IShellProvider} for dialogs.
   * @param directMenu if <code>true</code> directly fill all items to the menu,
   * otherwise create a sub menu.
   */
  public static void fillLabelMenu(IMenuManager manager, final IStructuredSelection selection, final IShellProvider shellProvider, boolean directMenu) {

    /* Either as direct Menu or Submenu */
    IMenuManager labelMenu;
    if (directMenu)
      labelMenu = manager;
    else {
      labelMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_LABEL);
      manager.add(labelMenu);
    }

    /* Assign  Labels */
    labelMenu.add(new AssignLabelsAction(shellProvider.getShell(), selection));

    /* Organize Labels */
    labelMenu.add(new Action(Messages.ApplicationActionBarAdvisor_ORGANIZE_LABELS) {
      @Override
      public void run() {
        PreferencesUtil.createPreferenceDialogOn(shellProvider.getShell(), ManageLabelsPreferencePage.ID, null, null).open();
      }
    });

    /* Load Labels */
    final Collection<ILabel> labels = CoreUtils.loadSortedLabels();

    /* Retrieve Labels that all selected News contain */
    labelMenu.add(new Separator());
    Set<ILabel> selectedLabels = ModelUtils.getLabelsForAll(selection);
    for (final ILabel label : labels) {
      LabelAction labelAction = new LabelAction(label, selection);
      labelAction.setChecked(selectedLabels.contains(label));
      labelMenu.add(labelAction);
    }

    /* New Label */
    labelMenu.add(new Action(Messages.ApplicationActionBarAdvisor_NEW_LABEL) {
      @Override
      public void run() {
        LabelDialog dialog = new LabelDialog(shellProvider.getShell(), DialogMode.ADD, null);
        if (dialog.open() == IDialogConstants.OK_ID) {
          String name = dialog.getName();
          RGB color = dialog.getColor();

          ILabel newLabel = Owl.getModelFactory().createLabel(null, name);
          newLabel.setColor(OwlUI.toString(color));
          newLabel.setOrder(labels.size());
          DynamicDAO.save(newLabel);

          LabelAction labelAction = new LabelAction(newLabel, selection);
          labelAction.run();
        }
      }

      @Override
      public boolean isEnabled() {
        return !selection.isEmpty();
      }
    });

    /* Remove All Labels */
    labelMenu.add(new Separator());
    LabelAction removeAllLabels = new LabelAction(null, selection);
    removeAllLabels.setId(RemoveLabelsHandler.ID);
    removeAllLabels.setActionDefinitionId(RemoveLabelsHandler.ID);
    removeAllLabels.setEnabled(!selection.isEmpty() && !labels.isEmpty());
    labelMenu.add(removeAllLabels);
  }

  /**
   * @param menu the {@link IMenuManager} to fill.
   * @param window the {@link IWorkbenchWindow} where the menu is living.
   */
  public static void fillBookMarksMenu(IMenuManager menu, IWorkbenchWindow window) {
    Set<IFolder> roots = CoreUtils.loadRootFolders();

    /* Filter Options */
    final IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
    BookMarkFilter.Type[] allFilters = BookMarkFilter.Type.values();
    BookMarkFilter.Type selectedFilter = allFilters[preferences.getInteger(DefaultPreferences.BM_MENU_FILTER)];
    List<BookMarkFilter.Type> displayedFilters = Arrays.asList(new BookMarkFilter.Type[] { BookMarkFilter.Type.SHOW_ALL, BookMarkFilter.Type.SHOW_NEW, BookMarkFilter.Type.SHOW_UNREAD, BookMarkFilter.Type.SHOW_STICKY });

    MenuManager optionsMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_FILTER_ELEMENTS, (selectedFilter == BookMarkFilter.Type.SHOW_ALL) ? OwlUI.FILTER : OwlUI.getImageDescriptor("icons/etool16/filter_active.gif"), null); //$NON-NLS-1$
    for (final BookMarkFilter.Type filter : displayedFilters) {
      String name = Messages.ApplicationActionBarAdvisor_SHOW_ALL;
      switch (filter) {
        case SHOW_NEW:
          name = Messages.ApplicationActionBarAdvisor_SHOW_NEW;
          break;
        case SHOW_UNREAD:
          name = Messages.ApplicationActionBarAdvisor_SHOW_UNREAD;
          break;
        case SHOW_STICKY:
          name = Messages.ApplicationActionBarAdvisor_SHOW_STICKY;
          break;
      }

      Action action = new Action(name, IAction.AS_RADIO_BUTTON) {
        @Override
        public void run() {
          if (isChecked())
            preferences.putInteger(DefaultPreferences.BM_MENU_FILTER, filter.ordinal());
        }
      };
      action.setChecked(filter == selectedFilter);
      optionsMenu.add(action);
      if (filter == BookMarkFilter.Type.SHOW_ALL)
        optionsMenu.add(new Separator());
    }
    menu.add(optionsMenu);
    menu.add(new Separator());

    /* Single Bookmark Set */
    if (roots.size() == 1) {
      fillBookMarksMenu(window, preferences, menu, roots.iterator().next().getChildren(), selectedFilter);
    }

    /* More than one Bookmark Set */
    else {
      for (IFolder root : roots) {
        if (shouldShow(root, selectedFilter)) {
          MenuManager rootItem = new MenuManager(root.getName(), OwlUI.BOOKMARK_SET, null);
          menu.add(rootItem);

          fillBookMarksMenu(window, preferences, rootItem, root.getChildren(), selectedFilter);
        }
      }
    }

    /* Indicate that no Items are Showing */
    if (menu.getItems().length == 2 && selectedFilter != BookMarkFilter.Type.SHOW_ALL) {
      boolean hasBookMarks = false;
      for (IFolder root : roots) {
        if (!root.getChildren().isEmpty()) {
          hasBookMarks = true;
          break;
        }
      }

      if (hasBookMarks) {
        menu.add(new Action(Messages.ApplicationActionBarAdvisor_SOME_ELEMENTS_FILTERED) {
          @Override
          public boolean isEnabled() {
            return false;
          }
        });
      }
    }
  }

  private static boolean shouldShow(IFolderChild child, BookMarkFilter.Type filter) {
    switch (filter) {
      case SHOW_ALL:
        return true;

      case SHOW_NEW:
        return hasNewsWithState(child, EnumSet.of(INews.State.NEW));

      case SHOW_UNREAD:
        return hasNewsWithState(child, EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));

      case SHOW_STICKY:
        return hasStickyNews(child);

      default:
        return true;
    }
  }

  private static void fillBookMarksMenu(final IWorkbenchWindow window, final IPreferenceScope preferences, IMenuManager parent, List<IFolderChild> childs, final BookMarkFilter.Type filter) {
    for (final IFolderChild child : childs) {

      /* Check if a Filter applies */
      if (!shouldShow(child, filter))
        continue;

      /* News Mark or Empty Folder */
      if (child instanceof INewsMark || (child instanceof IFolder && ((IFolder) child).getChildren().isEmpty())) {
        String name = child.getName();
        if (child instanceof INewsMark) {
          int unreadNewsCount = (((INewsMark) child).getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)));
          if (unreadNewsCount > 0)
            name = NLS.bind(Messages.ApplicationActionBarAdvisor_MARK_UNREAD_COUNT, name, unreadNewsCount);
        }

        Action action = new Action(name) {
          @Override
          public void run() {
            if (child instanceof INewsMark)
              OwlUI.openInFeedView(window.getActivePage(), new StructuredSelection(child));
          }
        };
        action.setImageDescriptor(getImageDescriptor(child));
        parent.add(action);
      }

      /* Folder with Children */
      else if (child instanceof IFolder) {
        final IFolder folder = (IFolder) child;

        final MenuManager folderMenu = new MenuManager(folder.getName(), getImageDescriptor(folder), null);
        parent.add(folderMenu);
        folderMenu.add(new Action("") {}); //Dummy Action //$NON-NLS-1$
        folderMenu.setRemoveAllWhenShown(true);
        folderMenu.addMenuListener(new IMenuListener() {
          public void menuAboutToShow(IMenuManager manager) {

            /* Open Folder */
            folderMenu.add(new Action(Messages.ApplicationActionBarAdvisor_OPEN_FOLDER) {
              @Override
              public void run() {
                OwlUI.openInFeedView(window.getActivePage(), new StructuredSelection(child));
              }
            });

            /* Offer Actions to force open a new Tab */
            if (preferences.getBoolean(DefaultPreferences.ALWAYS_REUSE_FEEDVIEW) && OwlUI.isTabbedBrowsingEnabled()) {

              /* Open Folder in New Tab */
              folderMenu.add(new OpenInNewTabAction(OwlUI.getPage(window), new StructuredSelection(child)));

              /* Open All in New Tabs */
              folderMenu.add(new OpenInNewTabAction(OwlUI.getPage(window), folder));
            }

            folderMenu.add(new Separator());

            /* Show other entries */
            fillBookMarksMenu(window, preferences, folderMenu, folder.getChildren(), filter);
          }
        });
      }
    }
  }

  private static ImageDescriptor getImageDescriptor(IFolderChild child) {
    boolean hasNewNews = hasNewsWithState(child, EnumSet.of(INews.State.NEW));

    /* Bookmark */
    if (child instanceof IBookMark) {
      ImageDescriptor favicon = OwlUI.getFavicon((IBookMark) child);
      if (!hasNewNews)
        return (favicon != null) ? favicon : OwlUI.BOOKMARK;

      /* Overlay if News are *new* */
      Image base = (favicon != null) ? OwlUI.getImage(fgResources, favicon) : OwlUI.getImage(fgResources, OwlUI.BOOKMARK);
      DecorationOverlayIcon overlay = new DecorationOverlayIcon(base, OwlUI.getImageDescriptor("icons/ovr16/new.gif"), IDecoration.BOTTOM_RIGHT); //$NON-NLS-1$
      return overlay;
    }

    /* Saved Search */
    else if (child instanceof ISearchMark) {
      if (hasNewNews)
        return OwlUI.SEARCHMARK_NEW;
      else if (((INewsMark) child).getNewsCount(INews.State.getVisible()) != 0)
        return OwlUI.SEARCHMARK;

      return OwlUI.SEARCHMARK_EMPTY;
    }

    /* News Bin */
    else if (child instanceof INewsBin) {
      boolean isArchive = child.getProperty(DefaultPreferences.ARCHIVE_BIN_MARKER) != null;

      if (hasNewNews)
        return isArchive ? OwlUI.ARCHIVE_NEW : OwlUI.NEWSBIN_NEW;
      else if (isArchive)
        return OwlUI.ARCHIVE;
      else if (((INewsMark) child).getNewsCount(INews.State.getVisible()) != 0)
        return OwlUI.NEWSBIN;

      return OwlUI.NEWSBIN_EMPTY;
    }

    /* Folder */
    else if (child instanceof IFolder)
      return hasNewNews ? OwlUI.FOLDER_NEW : OwlUI.FOLDER;

    return null;
  }

  private static boolean hasNewsWithState(IFolderChild child, EnumSet<INews.State> states) {
    if (child instanceof IFolder)
      return hasNewsWithStates((IFolder) child, states);

    return ((INewsMark) child).getNewsCount(states) != 0;
  }

  private static boolean hasNewsWithStates(IFolder folder, EnumSet<INews.State> states) {
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof INewsMark && ((INewsMark) mark).getNewsCount(states) != 0)
        return true;
    }

    List<IFolder> folders = folder.getFolders();
    for (IFolder child : folders) {
      if (hasNewsWithStates(child, states))
        return true;
    }

    return false;
  }

  private static boolean hasStickyNews(IFolderChild child) {
    if (child instanceof IFolder)
      return hasStickyNews((IFolder) child);

    if (child instanceof IBookMark)
      return ((IBookMark) child).getStickyNewsCount() != 0;

    return false;
  }

  private static boolean hasStickyNews(IFolder folder) {
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark && ((IBookMark) mark).getStickyNewsCount() != 0)
        return true;
    }

    List<IFolder> folders = folder.getFolders();
    for (IFolder child : folders) {
      if (hasStickyNews(child))
        return true;
    }

    return false;
  }
}
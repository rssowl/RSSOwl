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

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IPreferenceDAO;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.PreferenceEvent;
import org.rssowl.core.persist.event.PreferenceListener;
import org.rssowl.core.persist.event.runnable.EventType;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.SearchHit;
import org.rssowl.ui.internal.dialogs.ActivityDialog;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.notifier.NotificationService;
import org.rssowl.ui.internal.notifier.NotificationService.Mode;
import org.rssowl.ui.internal.util.JobRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author bpasero
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

  /** Key for Data-Slot in Controls that support this Hook */
  public static final String FOCUSLESS_SCROLL_HOOK = "org.rssowl.ui.internal.FocuslessScrollHook"; //$NON-NLS-1$

  /* WebSite class being used for the Browser on Windows only */
  private static final String SWT_BROWSER_WIN = "org.eclipse.swt.browser.WebSite"; //$NON-NLS-1$

  /* Maximum Number of News for Teasing */
  private static final int TEASE_LIMIT = 200;

  private TrayItem fTrayItem;
  private boolean fTrayTeasing;
  private boolean fTrayEnabled;
  private boolean fMinimizedToTray;
  private ApplicationActionBarAdvisor fActionBarAdvisor;
  private LocalResourceManager fResources;
  private IPreferenceScope fPreferences;
  private boolean fBlockIconifyEvent;
  private boolean fMinimizeFromClose;
  private Menu fTrayMenu;
  private List<Long> fTeasingNewsCache;
  private ISearch fTodaysNewsSearch;

  /* Listeners */
  private NewsAdapter fNewsListener;
  private ShellListener fTrayShellListener;
  private PreferenceListener fPrefListener;

  /**
   * @param configurer
   */
  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    super(configurer);
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fTeasingNewsCache = new ArrayList<Long>();
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
   */
  @Override
  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
    fActionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
    return fActionBarAdvisor;
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
   */
  @Override
  public void preWindowOpen() {
    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

    /* Set Window State and define visibility of UI elements */
    configurer.setShowCoolBar(true);
    configurer.setShowPerspectiveBar(false);
    configurer.setShowStatusLine(true);
    configurer.setShowMenuBar(true);
    configurer.setShowFastViewBars(false);
    configurer.setShowProgressIndicator(true);
    configurer.setTitle("RSSOwl"); //$NON-NLS-1$

    /* Set Window Size to match monitor size (only on single monitor) */
    Point size = OwlUI.getFirstMonitorSize();
    if (size != null)
      configurer.setInitialSize(size);

    /* Apply DND Support for Editor Area */
    configurer.addEditorAreaTransfer(LocalSelectionTransfer.getTransfer());
    configurer.configureEditorAreaDropListener(new EditorDNDImpl());

    /* Retrieve Preferences */
    fPreferences = Owl.getPreferenceService().getGlobalScope();
  }

  void setToolBarVisible(boolean visible, boolean layout) {
    getWindowConfigurer().setShowCoolBar(visible);

    if (layout)
      getWindowConfigurer().getWindow().getShell().layout();
  }

  /**
   * @param visible
   * @param layout
   */
  public void setStatusVisible(boolean visible, boolean layout) {
    getWindowConfigurer().setShowStatusLine(visible);
    getWindowConfigurer().setShowProgressIndicator(visible);

    /* Hack: To avoid cheese, update ToolBar Too */
    boolean showsToolBar = getWindowConfigurer().getShowCoolBar();
    getWindowConfigurer().setShowCoolBar(!showsToolBar);
    getWindowConfigurer().setShowCoolBar(showsToolBar);

    if (layout)
      getWindowConfigurer().getWindow().getShell().layout();
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowCreate()
   */
  @Override
  public void postWindowCreate() {

    /* If set to move to tray on startup, block shell opening once */
    IWorkbenchWindow window = getWindowConfigurer().getWindow();
    if (window != null && fPreferences.getBoolean(DefaultPreferences.TRAY_ON_START))
      blockShellOpen(window);

    /* Toolbar & Status Visibility */
    IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
    if (!preferences.getBoolean(DefaultPreferences.SHOW_TOOLBAR))
      setToolBarVisible(false, false);
    if (!preferences.getBoolean(DefaultPreferences.SHOW_STATUS))
      setStatusVisible(false, false);
  }

  /* Calls safely into a patched version of JFaces Window class to block shell opening once */
  private void blockShellOpen(IWorkbenchWindow window) {
    try {
      Method method = window.getClass().getMethod("blockShellOpenOnce"); //$NON-NLS-1$
      if (method != null)
        method.invoke(window);
    } catch (Exception e) {
      /* Ignore Silently */
    }
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
   */
  @Override
  public void postWindowOpen() {
    final Shell shell = getWindowConfigurer().getWindow().getShell();

    /* System Tray */
    SafeRunner.run(new LoggingSafeRunnable() {
      public void run() throws Exception {
        boolean trayEnabled = false;

        /* Hook TrayItem if supported on OS and 1st Window */
        if (fPreferences.getBoolean(DefaultPreferences.TRAY_ON_MINIMIZE) || fPreferences.getBoolean(DefaultPreferences.TRAY_ON_CLOSE) || fPreferences.getBoolean(DefaultPreferences.TRAY_ON_START))
          trayEnabled = enableTray();

        /* Win only: Allow Scroll over Cursor-Control */
        if (Application.IS_WINDOWS)
          hookFocuslessScrolling(shell.getDisplay());

        /* Register Listeners */
        registerListeners();

        /* Move to Tray if set */
        if (trayEnabled && fPreferences.getBoolean(DefaultPreferences.TRAY_ON_START))
          moveToTray(shell);
      }
    });

    /* Hook into Selection Listeners to open Activiy Dialog if needed (this is a hack :-) ) */
    shell.getDisplay().addFilter(SWT.Selection, new Listener() {
      @SuppressWarnings("restriction")
      public void handleEvent(Event event) {
        if (event.item == null && event.widget instanceof ToolItem) {
          ToolItem item = (ToolItem) event.widget;
          ToolBar toolbar = item.getParent();
          Composite parent = toolbar.getParent();
          if (!(parent instanceof Shell)) {
            parent = parent.getParent();
            if (parent != null && !(parent instanceof Shell) && !parent.isDisposed() && parent.getLayoutData() instanceof org.eclipse.ui.internal.progress.ProgressRegion) {
              event.doit = false;
              event.type = SWT.None;
              asyncOpenActivityDialog(toolbar.getShell());
            }
          }
        }
      }
    });

    /* Notify Controller */
    Controller.getDefault().postWindowOpen();
  }

  private void asyncOpenActivityDialog(final Shell shell) {
    shell.getDisplay().asyncExec(new Runnable() {
      public void run() {
        ActivityDialog instance = ActivityDialog.getVisibleInstance();
        if (instance == null) {
          ActivityDialog dialog = new ActivityDialog(shell);
          dialog.setBlockOnOpen(false);
          dialog.open();
        } else {
          instance.getShell().forceActive();
        }
      }
    });
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowShellClose()
   */
  @Override
  public boolean preWindowShellClose() {
    final boolean[] res = new boolean[] { true };
    SafeRunner.run(new LoggingSafeRunnable() {
      public void run() throws Exception {

        /* Check if Prefs tell to move to tray */
        if (ApplicationWorkbenchWindowAdvisor.this.equals(ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor) && fPreferences.getBoolean(DefaultPreferences.TRAY_ON_CLOSE)) {
          fMinimizeFromClose = true;
          getWindowConfigurer().getWindow().getShell().notifyListeners(SWT.Iconify, new Event());
          res[0] = false;
          fMinimizeFromClose = false;
        }

        /* Notify any open feedview about closing application */
        else {
          onClose();
        }
      }
    });

    return res[0];
  }

  /**
   * @return TRUE if the Window is minimized to the tray.
   */
  public boolean isMinimizedToTray() {
    return fMinimizedToTray;
  }

  /**
   * @return TRUE if the Window is minimized.
   */
  public boolean isMinimized() {
    return getWindowConfigurer().getWindow().getShell().getMinimized();
  }

  private void registerListeners() {

    /* Add Shell sListener */
    getWindowConfigurer().getWindow().getShell().addShellListener(new ShellAdapter() {
      @Override
      public void shellIconified(ShellEvent e) {
        if (!fBlockIconifyEvent)
          onMinimize();
      }
    });

    /* Listen on Preferences Changes */
    fPrefListener = new PreferenceListener() {
      public void entitiesAdded(Set<PreferenceEvent> events) {
        onPreferencesChange(events, EventType.PERSIST);
      }

      public void entitiesDeleted(Set<PreferenceEvent> events) {
        onPreferencesChange(events, EventType.REMOVE);
      }

      public void entitiesUpdated(Set<PreferenceEvent> events) {
        onPreferencesChange(events, EventType.UPDATE);
      }
    };
    DynamicDAO.getDAO(IPreferenceDAO.class).addEntityListener(fPrefListener);
  }

  private void unregisterListeners() {
    DynamicDAO.getDAO(IPreferenceDAO.class).removeEntityListener(fPrefListener);
  }

  private void onPreferencesChange(Set<PreferenceEvent> events, EventType type) {
    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();
    boolean useTray = false;
    boolean affectsTray = false;

    for (PreferenceEvent event : events) {
      String key = event.getEntity().getKey();

      /* Tray Preference Change: Tray on Minimize */
      if (DefaultPreferences.TRAY_ON_MINIMIZE.equals(key)) {
        affectsTray = true;
        if (type == EventType.REMOVE)
          useTray = defaultScope.getBoolean(DefaultPreferences.TRAY_ON_MINIMIZE) || fPreferences.getBoolean(DefaultPreferences.TRAY_ON_CLOSE);
        else
          useTray = event.getEntity().getBoolean() || fPreferences.getBoolean(DefaultPreferences.TRAY_ON_CLOSE);
      }

      /* Tray Preference Change: Tray on Close */
      else if (DefaultPreferences.TRAY_ON_CLOSE.equals(key)) {
        affectsTray = true;
        if (type == EventType.REMOVE)
          useTray = defaultScope.getBoolean(DefaultPreferences.TRAY_ON_CLOSE) || fPreferences.getBoolean(DefaultPreferences.TRAY_ON_MINIMIZE);
        else
          useTray = event.getEntity().getBoolean() || fPreferences.getBoolean(DefaultPreferences.TRAY_ON_MINIMIZE);
      }

      /* Enable Tray */
      if (affectsTray && useTray && !fTrayEnabled) {
        JobRunner.runInUIThread(null, new Runnable() {
          public void run() {
            enableTray();
          }
        });
      }

      /* Disable Tray */
      else if (affectsTray && !useTray && fTrayEnabled) {
        JobRunner.runInUIThread(null, new Runnable() {
          public void run() {
            disableTray();
          }
        });
      }
    }
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#dispose()
   */
  @Override
  public void dispose() {
    unregisterListeners();

    if (fTrayItem != null)
      fTrayItem.dispose();

    if (fNewsListener != null)
      DynamicDAO.removeEntityListener(INews.class, fNewsListener);

    fResources.dispose();
  }

  private void onMinimize() {

    /* Mark displayed News as Read on Minimize if set in Preferences */
    IEditorPart activeEditor = OwlUI.getActiveEditor();
    if (activeEditor != null && activeEditor instanceof FeedView) {
      FeedView feedView = (FeedView) activeEditor;
      feedView.notifyUIEvent(FeedView.UIEvent.MINIMIZE);
    }

    /* Trigger synchronization as the user is leaving the RSSOwl window */
    Controller.getDefault().getSyncService().synchronize();
  }

  private void onClose() {

    /* Mark new News as Unread on Close */
    IEditorPart activeEditor = OwlUI.getActiveEditor();
    if (activeEditor != null && activeEditor instanceof FeedView) {
      FeedView feedView = (FeedView) activeEditor;
      feedView.notifyUIEvent(FeedView.UIEvent.CLOSE);
    }
  }

  /* Enable System-Tray Support */
  private boolean enableTray() {

    /* Avoid that this is being called redundantly */
    if (fTrayEnabled)
      return true;

    /* Only enable for Primary Window */
    IWorkbenchWindow primaryWindow = OwlUI.getPrimaryWindow();
    if (primaryWindow == null || !primaryWindow.equals(getWindowConfigurer().getWindow()))
      return false;

    final Shell shell = primaryWindow.getShell();
    final int doubleClickTime = shell.getDisplay().getDoubleClickTime();
    final Tray tray = shell.getDisplay().getSystemTray();

    /* Tray not support on the OS */
    if (tray == null)
      return false;

    /* Create Item in Tray */
    fTrayItem = new TrayItem(tray, SWT.NONE);
    fTrayItem.setToolTipText("RSSOwl"); //$NON-NLS-1$
    fTrayEnabled = true;

    if (Application.IS_WINDOWS)
      fTrayItem.setVisible(false);

    /* Apply Image */
    fTrayItem.setImage(OwlUI.getImage(fResources, OwlUI.TRAY_OWL));

    /* Minimize to Tray on Shell Iconify if set */
    fTrayShellListener = new ShellAdapter() {

      @Override
      public void shellIconified(ShellEvent e) {
        if (!fBlockIconifyEvent && (fMinimizeFromClose || fPreferences.getBoolean(DefaultPreferences.TRAY_ON_MINIMIZE)))
          moveToTray(shell);
      }
    };
    shell.addShellListener(fTrayShellListener);

    /* Show Menu on Selection */
    fTrayItem.addListener(SWT.MenuDetect, new Listener() {
      public void handleEvent(Event event) {
        showTrayMenu(shell);
      }
    });

    /* Handle Selection (Default and Normal) */
    Listener selectionListener = new Listener() {
      private long lastDoubleClickTime;

      public void handleEvent(Event event) {
        boolean restoreOnDoubleclick = fPreferences.getBoolean(DefaultPreferences.RESTORE_TRAY_DOUBLECLICK);
        boolean isDoubleClick = (event.type == SWT.DefaultSelection);

        /* Remember when Doubleclick was invoked */
        if (isDoubleClick)
          lastDoubleClickTime = System.currentTimeMillis();

        /* Invoke Single Click Action if this is not a double click and we are on Windows */
        if (!isDoubleClick && restoreOnDoubleclick && Application.IS_WINDOWS) {
          NotificationService service = Controller.getDefault().getNotificationService();

          /* Notifier showing - close it instantly */
          if (service.isPopupVisible()) {
            service.closePopup();
          }

          /* Notifier not showing - invoke single click action (only if not recently closed) */
          else if (!service.wasPopupRecentlyClosed()) {
            JobRunner.runInBackgroundThread(doubleClickTime, new Runnable() {
              public void run() {
                if (lastDoubleClickTime < System.currentTimeMillis() - doubleClickTime) {
                  JobRunner.runInUIThread(tray, new Runnable() {
                    public void run() {
                      if (!shell.isDisposed())
                        onSingleClick(shell);
                    }
                  });
                }
              }
            });
          }
        }

        /* Do not restore if settings are different */
        if (restoreOnDoubleclick != isDoubleClick)
          return;

        /* Restore from Tray */
        if (!shell.isVisible())
          restoreFromTray(shell);

        /* Move to Tray */
        else if (!Application.IS_WINDOWS)
          moveToTray(shell);
      }
    };

    fTrayItem.addListener(SWT.DefaultSelection, selectionListener);
    fTrayItem.addListener(SWT.Selection, selectionListener);

    /* Indicate new News in Tray */
    fNewsListener = new NewsAdapter() {

      @Override
      public void entitiesAdded(final Set<NewsEvent> events) {
        if (!CoreUtils.containsState(events, INews.State.NEW))
          return;

        JobRunner.runInUIThread(fTrayItem, new Runnable() {
          public void run() {

            /* Update Icon only when Tray is visible */
            if (!fTrayItem.getVisible() || shell.getVisible())
              return;

            /* Return on Shutdown */
            if (Controller.getDefault().isShuttingDown())
              return;

            /* Remember Added News (Windows Only and Only if restoring with Doubleclick) */
            if (Application.IS_WINDOWS && fPreferences.getBoolean(DefaultPreferences.RESTORE_TRAY_DOUBLECLICK)) {
              synchronized (fTeasingNewsCache) {
                for (NewsEvent event : events) {
                  if (event.getEntity().getState() == INews.State.NEW)
                    fTeasingNewsCache.add(event.getEntity().getId());
                }

                if (!fTeasingNewsCache.isEmpty())
                  fTrayItem.setToolTipText(NLS.bind(Messages.ApplicationWorkbenchWindowAdvisor_N_INCOMING_NEWS, fTeasingNewsCache.size()));
              }
            }

            /* Show Teaser */
            if (!fTrayTeasing) {
              fTrayTeasing = true;
              fTrayItem.setImage(OwlUI.getImage(fResources, OwlUI.TRAY_OWL_TEASING));
            }
          }
        });
      }
    };
    DynamicDAO.addEntityListener(INews.class, fNewsListener);

    return true;
  }

  /* Move to System Tray */
  private void moveToTray(Shell shell) {
    if (Application.IS_WINDOWS)
      fTrayItem.setVisible(true);

    /*
     * Bug in SWT: For some reason, calling setVisible(false) here will result
     * in a second Iconify Event. The fix is to disable processing of this event
     * meanwhile.
     */
    fBlockIconifyEvent = true;
    try {
      shell.setVisible(false);
    } finally {
      fBlockIconifyEvent = false;
    }

    fMinimizedToTray = true;
  }

  /**
   * @param shell
   */
  public void restoreFromTray(Shell shell) {

    /* Mac: Un-Minimize if minimized */
    if (Application.IS_MAC && shell.getMinimized())
      shell.setMinimized(false);

    /* Restore Shell */
    shell.setVisible(true);
    shell.setActive();

    /* Non Mac: Un-Minimize if minimized */
    if (!Application.IS_MAC && shell.getMinimized())
      shell.setMinimized(false);

    if (Application.IS_WINDOWS)
      fTrayItem.setVisible(false);

    fMinimizedToTray = false;

    clearTease(false);
  }

  private void clearTease(boolean clearTray) {
    if (fTrayTeasing)
      fTrayItem.setImage(OwlUI.getImage(fResources, OwlUI.TRAY_OWL));

    fTrayTeasing = false;

    if (Application.IS_WINDOWS) {
      synchronized (fTeasingNewsCache) {
        fTeasingNewsCache.clear();
      }

      fTrayItem.setToolTipText(clearTray ? "" : "RSSOwl"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /* Disable System-Tray Support */
  private void disableTray() {

    /* Avoid that this is being called redundantly */
    if (!fTrayEnabled)
      return;

    /* First make sure to have the Window restored */
    restoreFromTray(getWindowConfigurer().getWindow().getShell());

    fTrayEnabled = false;
    fMinimizedToTray = false;

    if (fTrayItem != null)
      fTrayItem.dispose();

    if (fNewsListener != null)
      DynamicDAO.removeEntityListener(INews.class, fNewsListener);

    if (fTrayShellListener != null)
      getWindowConfigurer().getWindow().getShell().removeShellListener(fTrayShellListener);
  }

  /* Support for focusless scrolling */
  private void hookFocuslessScrolling(final Display display) {
    display.addFilter(SWT.MouseWheel, new Listener() {
      public void handleEvent(Event event) {
        Control control = display.getCursorControl();

        /* Control must be non-focus undisposed */
        if (control == null || control.isDisposed() || control.isFocusControl())
          return;

        /* Pass focus to control and disable event if allowed */
        boolean isBrowser = SWT_BROWSER_WIN.equals(control.getClass().getName());
        if (isBrowser || control.getData(FOCUSLESS_SCROLL_HOOK) != null) {

          /* Break Condition */
          control.setFocus();

          /* Re-Post Event to Cursor Control */
          event.doit = false;
          event.widget = control;
          display.post(event);
        }
      }
    });
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#getWindowConfigurer()
   */
  @Override
  public IWorkbenchWindowConfigurer getWindowConfigurer() {
    return super.getWindowConfigurer();
  }

  private void showTrayMenu(final Shell shell) {
    MenuManager trayMenuManager = new MenuManager();

    /* Restore */
    trayMenuManager.add(new ContributionItem() {
      @Override
      public void fill(Menu menu, int index) {
        MenuItem restoreItem = new MenuItem(menu, SWT.PUSH);
        restoreItem.setText(Messages.ApplicationWorkbenchWindowAdvisor_RESTORE);
        restoreItem.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            restoreFromTray(shell);
          }
        });
        menu.setDefaultItem(restoreItem);
      }
    });

    /* Separator */
    trayMenuManager.add(new Separator());

    /* Other Items */
    fActionBarAdvisor.fillTrayItem(trayMenuManager, shell, ApplicationWorkbenchWindowAdvisor.this);

    if (fTrayMenu != null)
      OwlUI.safeDispose(fTrayMenu);

    fTrayMenu = trayMenuManager.createContextMenu(shell);
    fTrayMenu.setVisible(true);
  }

  private void onSingleClick(Shell shell) {
    NotificationService service = Controller.getDefault().getNotificationService();
    List<INews> newsToShow = new ArrayList<INews>();
    Mode mode = Mode.RECENT;

    /* Return early if Notifier already showing */
    if (service.isPopupVisible())
      return;

    /* Show News Downloaded while Minimized */
    if (!fTeasingNewsCache.isEmpty()) {
      synchronized (fTeasingNewsCache) {
        int counter = 0;
        for (int i = fTeasingNewsCache.size() - 1; i >= 0; i--) {
          NewsReference reference = new NewsReference(fTeasingNewsCache.get(i));
          INews newsitem = reference.resolve();
          if (newsitem != null && newsitem.getState() == INews.State.NEW && service.shouldShow(newsitem)) {
            newsToShow.add(newsitem);

            if (++counter >= TEASE_LIMIT)
              break;
          } else
            CoreUtils.reportIndexIssue();

          /* Return early if Notifier already showing */
          if (service.isPopupVisible())
            return;
        }
      }
      mode = Mode.INCOMING_MANUAL;
    }

    /* Show Recent News if no teasing news present */
    if (newsToShow.isEmpty()) {

      /* Build the Search if not yet done */
      if (fTodaysNewsSearch == null) {
        IModelFactory factory = Owl.getModelFactory();

        String newsClassName = INews.class.getName();
        ISearchField ageInDaysField = factory.createSearchField(INews.AGE_IN_DAYS, newsClassName);
        ISearchField ageInMinutesField = factory.createSearchField(INews.AGE_IN_MINUTES, newsClassName);

        ISearchCondition dayCondition = factory.createSearchCondition(ageInDaysField, SearchSpecifier.IS_LESS_THAN, 1); // From Today after Midnight
        ISearchCondition recentCondition = factory.createSearchCondition(ageInMinutesField, SearchSpecifier.IS_LESS_THAN, -60 * 6); // Up to 6 Hours Ago

        fTodaysNewsSearch = factory.createSearch(null);
        fTodaysNewsSearch.setMatchAllConditions(false);
        fTodaysNewsSearch.addSearchCondition(dayCondition);
        fTodaysNewsSearch.addSearchCondition(recentCondition);
      }

      /* Sort by Id (simulate sorting by date) */
      List<SearchHit<NewsReference>> result = Owl.getPersistenceService().getModelSearch().searchNews(fTodaysNewsSearch);
      Set<NewsReference> recentNews = new TreeSet<NewsReference>(new Comparator<NewsReference>() {
        public int compare(NewsReference ref1, NewsReference ref2) {
          if (ref1.equals(ref2))
            return 0;

          return ref1.getId() > ref2.getId() ? -1 : 1;
        }
      });

      for (SearchHit<NewsReference> hit : result) {
        recentNews.add(hit.getResult());
      }

      /* Resolve and Add News from Result */
      int counter = 0;
      for (NewsReference reference : recentNews) {
        INews newsitem = reference.resolve();
        if (newsitem != null && newsitem.isVisible() && service.shouldShow(newsitem)) {
          newsToShow.add(newsitem);

          if (++counter >= TEASE_LIMIT)
            break;
        } else if (newsitem == null)
          CoreUtils.reportIndexIssue();

        /* Return early if Notifier already showing */
        if (service.isPopupVisible())
          return;
      }

      mode = Mode.RECENT;
    }

    /* Return early if Notifier already showing */
    if (service.isPopupVisible())
      return;

    /* Tease with News */
    if (!newsToShow.isEmpty()) {
      service.show(newsToShow, null, mode);
      clearTease(true);
    }

    /* Otherwise show Context Menu */
    else
      showTrayMenu(shell);
  }
}
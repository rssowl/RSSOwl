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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.ITask;
import org.rssowl.core.util.ITask.Priority;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.ApplicationActionBarAdvisor;
import org.rssowl.ui.internal.ApplicationServer;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.EntityGroupItem;
import org.rssowl.ui.internal.ILinkHandler;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.OwlUI.Layout;
import org.rssowl.ui.internal.OwlUI.PageSize;
import org.rssowl.ui.internal.actions.ArchiveNewsAction;
import org.rssowl.ui.internal.actions.AutomateFilterAction;
import org.rssowl.ui.internal.actions.CreateFilterAction.PresetAction;
import org.rssowl.ui.internal.actions.MakeNewsStickyAction;
import org.rssowl.ui.internal.actions.MarkAllNewsReadAction;
import org.rssowl.ui.internal.actions.MoveCopyNewsToBinAction;
import org.rssowl.ui.internal.actions.NavigationActionFactory;
import org.rssowl.ui.internal.actions.OpenInExternalBrowserAction;
import org.rssowl.ui.internal.actions.OpenNewsAction;
import org.rssowl.ui.internal.actions.ToggleReadStateAction;
import org.rssowl.ui.internal.dialogs.SearchNewsDialog;
import org.rssowl.ui.internal.editors.feed.NewsBrowserLabelProvider.Dynamic;
import org.rssowl.ui.internal.undo.NewsStateOperation;
import org.rssowl.ui.internal.undo.StickyOperation;
import org.rssowl.ui.internal.undo.UndoStack;
import org.rssowl.ui.internal.util.CBrowser;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.JobTracker;
import org.rssowl.ui.internal.util.ModelUtils;
import org.rssowl.ui.internal.util.UIBackgroundJob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author bpasero
 */
public class NewsBrowserViewer extends ContentViewer implements ILinkHandler {

  /* ID for Link Handlers */
  static final String MARK_READ_HANDLER_ID = "org.rssowl.ui.MarkRead"; //$NON-NLS-1$
  static final String TOGGLE_READ_HANDLER_ID = "org.rssowl.ui.ToggleRead"; //$NON-NLS-1$
  static final String TOGGLE_STICKY_HANDLER_ID = "org.rssowl.ui.ToggleSticky"; //$NON-NLS-1$
  static final String ARCHIVE_HANDLER_ID = "org.rssowl.ui.Archive"; //$NON-NLS-1$
  static final String DELETE_HANDLER_ID = "org.rssowl.ui.Delete"; //$NON-NLS-1$
  static final String ATTACHMENT_HANDLER_ID = "org.rssowl.ui.DownloadAttachment"; //$NON-NLS-1$
  static final String ATTACHMENTS_MENU_HANDLER_ID = "org.rssowl.ui.AttachmentsMenu"; //$NON-NLS-1$
  static final String LABELS_MENU_HANDLER_ID = "org.rssowl.ui.LabelsMenu"; //$NON-NLS-1$
  static final String EXPAND_NEWS_HANDLER_ID = "org.rssowl.ui.ExpandNews"; //$NON-NLS-1$
  static final String COLLAPSE_NEWS_HANDLER_ID = "org.rssowl.ui.CollapseNews"; //$NON-NLS-1$
  static final String EXPAND_GROUP_HANDLER_ID = "org.rssowl.ui.ExpandGroup"; //$NON-NLS-1$
  static final String COLLAPSE_GROUP_HANDLER_ID = "org.rssowl.ui.CollapseGroup"; //$NON-NLS-1$
  static final String GROUP_MENU_HANDLER_ID = "org.rssowl.ui.GroupMenu"; //$NON-NLS-1$
  static final String NEWS_MENU_HANDLER_ID = "org.rssowl.ui.NewsMenu"; //$NON-NLS-1$
  static final String SHARE_NEWS_MENU_HANDLER_ID = "org.rssowl.ui.ShareNewsMenu"; //$NON-NLS-1$
  static final String NEXT_NEWS_HANDLER_ID = "org.rssowl.ui.NextNews"; //$NON-NLS-1$
  static final String NEXT_UNREAD_NEWS_HANDLER_ID = "org.rssowl.ui.NextUnreadNews"; //$NON-NLS-1$
  static final String PREVIOUS_NEWS_HANDLER_ID = "org.rssowl.ui.PreviousNews"; //$NON-NLS-1$
  static final String PREVIOUS_UNREAD_NEWS_HANDLER_ID = "org.rssowl.ui.PreviousUnreadNews"; //$NON-NLS-1$
  static final String TRANSFORM_HANDLER_ID = "org.rssowl.ui.TransformNews"; //$NON-NLS-1$
  static final String RELATED_NEWS_MENU_HANDLER_ID = "org.rssowl.ui.RelatedNewsMenu"; //$NON-NLS-1$
  static final String NEXT_PAGE_HANDLER_ID = "org.rssowl.ui.NextPage"; //$NON-NLS-1$
  static final String SCROLL_NEXT_PAGE_HANDLER_ID = "org.rssowl.ui.ScrollNextPage"; //$NON-NLS-1$

  /* Delay in millies before reacting on user interaction */
  private static final int USER_INTERACTION_DELAY = 500;

  /* Unique identifier of the <body> element */
  private static final String BODY_ELEMENT_ID = "owlbody"; //$NON-NLS-1$

  private Object fInput;
  private CBrowser fBrowser;
  private IFeedViewSite fSite;
  private boolean fIsEmbedded;
  private Menu fNewsContextMenu;
  private Menu fAttachmentsContextMenu;
  private Menu fLabelsContextMenu;
  private Menu fShareNewsContextMenu;
  private Menu fFindRelatedContextMenu;
  private IStructuredSelection fCurrentSelection = StructuredSelection.EMPTY;
  private final ApplicationServer fServer;
  private final String fId;
  private boolean fBlockRefresh;
  private boolean fMarkReadOnExpand = true;
  private boolean fMarkReadOnScrolling = true;
  private int fPageSize;
  private final IModelFactory fFactory;
  private final IPreferenceScope fPreferences = Owl.getPreferenceService().getGlobalScope();
  private final INewsDAO fNewsDao = DynamicDAO.getDAO(INewsDAO.class);
  private final JobTracker fUserInteractionTracker = new JobTracker(USER_INTERACTION_DELAY, false, true, Priority.INTERACTIVE);
  private final Set<Long> fMarkedUnreadByUserCache = Collections.synchronizedSet(new HashSet<Long>());

  /* This viewer's sorter. <code>null</code> means there is no sorter. */
  private ViewerComparator fSorter;

  /* This viewer's filters (element type: <code>ViewerFilter</code>). */
  private List<ViewerFilter> fFilters;
  private NewsFilter fNewsFilter;

  /* A model of what is displayed in the browser */
  private final NewsBrowserViewModel fViewModel;

  /* Special Element that denotes a Paging Latch */
  static final class PageLatch {}

  /* Task to perform some news related actions based on user interaction */
  final class UserInteractionTask implements ITask {
    private final NewsBrowserViewModel fViewModel;
    private final CBrowser fCBrowser;

    public UserInteractionTask(NewsBrowserViewModel model, CBrowser browser) {
      fViewModel = model;
      fCBrowser = browser;
    }

    public IStatus run(IProgressMonitor monitor) {

      /* Return early if canceled or disposed */
      if (monitor.isCanceled() || fCBrowser.getControl().isDisposed())
        return Status.OK_STATUS;

      /* Reveal next page as necessary */
      if (fPageSize != 0 && fViewModel.hasHiddenNews()) {
        long lastVisibleNewsId = fViewModel.getLastVisibleNews();
        if (lastVisibleNewsId != -1) {
          StringBuilder js = new StringBuilder();
          if (fCBrowser.isIE()) {
            js.append("var scrollPosY = document.body.scrollTop; "); //$NON-NLS-1$
            js.append("var windowHeight = document.body.clientHeight; "); //$NON-NLS-1$
          } else {
            js.append("var scrollPosY = window.pageYOffset; "); //$NON-NLS-1$
            js.append("var windowHeight = window.innerHeight; "); //$NON-NLS-1$
          }

          js.append("if (scrollPosY > 0) {"); //$NON-NLS-1$
          js.append("  var node = document.getElementById('").append(Dynamic.NEWS.getId(lastVisibleNewsId)).append("'); "); //$NON-NLS-1$//$NON-NLS-2$
          js.append("  if (node) {"); //$NON-NLS-1$
          js.append("    if ((scrollPosY + windowHeight) >= node.offsetTop) {"); //$NON-NLS-1$
          js.append("      window.location.href = '").append(ILinkHandler.HANDLER_PROTOCOL + SCROLL_NEXT_PAGE_HANDLER_ID).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$
          js.append("    }"); //$NON-NLS-1$
          js.append("  }"); //$NON-NLS-1$
          js.append("}"); //$NON-NLS-1$

          if (!monitor.isCanceled() && !fCBrowser.getControl().isDisposed())
            fCBrowser.execute(js.toString(), "UserInteractionTask#0"); //$NON-NLS-1$
        }
      }

      /* Return early if canceled or disposed */
      if (monitor.isCanceled() || fCBrowser.getControl().isDisposed())
        return Status.OK_STATUS;

      /* Mark Seen News as Read if necessary */
      if (fMarkReadOnScrolling && !isGroupingByState()) {//Ignore if grouping by state to avoid refresh
        StringBuilder js = new StringBuilder();
        if (fCBrowser.isIE()) {
          js.append("var scrollPosY = document.body.scrollTop; "); //$NON-NLS-1$
          js.append("var windowHeight = document.body.clientHeight; "); //$NON-NLS-1$
        } else {
          js.append("var scrollPosY = window.pageYOffset; "); //$NON-NLS-1$
          js.append("var windowHeight = window.innerHeight; "); //$NON-NLS-1$
        }

        boolean varDefined = false;
        List<Long> visibleUnreadNews = fViewModel.getVisibleUnreadNews();
        long lastNews = fViewModel.getLastNews();
        js.append("var lastNews = document.getElementById('").append(Dynamic.NEWS.getId(lastNews)).append("'); "); //$NON-NLS-1$//$NON-NLS-2$
        js.append("if (lastNews) {"); //$NON-NLS-1$
        js.append("  var newsIds = ''; "); //$NON-NLS-1$
        js.append("  var lastNewsPosY = lastNews.offsetTop; "); //$NON-NLS-1$
        js.append("  var lastNewsHeight = lastNews.offsetHeight; "); //$NON-NLS-1$
        for (Long id : visibleUnreadNews) {
          if (fMarkedUnreadByUserCache.contains(id))
            continue; //Skip those news explicitly marked as unread by the user

          if (!varDefined) {
            js.append("var "); //$NON-NLS-1$
            varDefined = true;
          }

          /*
           * Conditions under which a news gets marked as read:
           *
           * "divPosY < scrollPosY" : Top Border of News is above top scroll position
           * "lastNewsPosY < scrollPosY + windowHeight" : Last news is visible
           */
          js.append("node = document.getElementById('").append(Dynamic.NEWS.getId(id)).append("'); "); //$NON-NLS-1$//$NON-NLS-2$
          js.append("  if (node) {"); //$NON-NLS-1$
          js.append("    var divPosY = node.offsetTop; "); //$NON-NLS-1$
          js.append("    var divHeight = node.offsetHeight; "); //$NON-NLS-1$
          js.append("    if (divPosY < scrollPosY || (lastNewsPosY > 0 && lastNewsPosY < scrollPosY + windowHeight)) {"); //$NON-NLS-1$
          js.append("      newsIds = newsIds + '").append(id).append(",'; "); //$NON-NLS-1$ //$NON-NLS-2$
          js.append("    }"); //$NON-NLS-1$
          js.append("  }"); //$NON-NLS-1$
        }

        js.append("  if (newsIds.length != 0) { "); //$NON-NLS-1$
        js.append("    window.location.href = '").append(ILinkHandler.HANDLER_PROTOCOL + MARK_READ_HANDLER_ID + "?").append("' + newsIds; "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        js.append("  } "); //$NON-NLS-1$
        js.append("}"); //$NON-NLS-1$

        if (!monitor.isCanceled() && !fCBrowser.getControl().isDisposed())
          fCBrowser.execute(js.toString(), "UserInteractionTask#1"); //$NON-NLS-1$
      }

      return Status.OK_STATUS;
    }

    public String getName() {
      return null;
    }

    public Priority getPriority() {
      return Priority.INTERACTIVE;
    }
  }

  /**
   * @param parent the parent {@link Composite} to host this viewer.
   * @param style the style of the {@link Browser} in this viewer.
   */
  public NewsBrowserViewer(Composite parent, int style) {
    this(parent, style, null);
  }

  /**
   * @param parent the parent {@link Composite} to host this viewer.
   * @param style the style of the {@link Browser} in this viewer.
   * @param site the {@link IFeedViewSite} if this viewer is being used from a
   * {@link FeedView} or <code>null</code> otherwise.
   */
  public NewsBrowserViewer(Composite parent, int style, IFeedViewSite site) {
    fBrowser = new CBrowser(parent, style);
    fViewModel = new NewsBrowserViewModel(this);
    fSite = site;
    fIsEmbedded = (fSite != null);
    hookControl(fBrowser.getControl());
    hookNewsContextMenu();
    hookAttachmentsContextMenu();
    hookLabelContextMenu();
    hookShareNewsContextMenu();
    hookFindRelatedContextMenu();
    fId = String.valueOf(hashCode());
    fServer = ApplicationServer.getDefault();
    fServer.register(fId, this);
    fFactory = Owl.getModelFactory();

    /* Register Link Handler */
    fBrowser.addLinkHandler(MARK_READ_HANDLER_ID, this);
    fBrowser.addLinkHandler(TOGGLE_READ_HANDLER_ID, this);
    fBrowser.addLinkHandler(TOGGLE_STICKY_HANDLER_ID, this);
    fBrowser.addLinkHandler(ARCHIVE_HANDLER_ID, this);
    fBrowser.addLinkHandler(DELETE_HANDLER_ID, this);
    fBrowser.addLinkHandler(ATTACHMENT_HANDLER_ID, this);
    fBrowser.addLinkHandler(ATTACHMENTS_MENU_HANDLER_ID, this);
    fBrowser.addLinkHandler(LABELS_MENU_HANDLER_ID, this);
    fBrowser.addLinkHandler(EXPAND_NEWS_HANDLER_ID, this);
    fBrowser.addLinkHandler(COLLAPSE_NEWS_HANDLER_ID, this);
    fBrowser.addLinkHandler(EXPAND_GROUP_HANDLER_ID, this);
    fBrowser.addLinkHandler(COLLAPSE_GROUP_HANDLER_ID, this);
    fBrowser.addLinkHandler(GROUP_MENU_HANDLER_ID, this);
    fBrowser.addLinkHandler(NEWS_MENU_HANDLER_ID, this);
    fBrowser.addLinkHandler(SHARE_NEWS_MENU_HANDLER_ID, this);
    fBrowser.addLinkHandler(NEXT_NEWS_HANDLER_ID, this);
    fBrowser.addLinkHandler(NEXT_UNREAD_NEWS_HANDLER_ID, this);
    fBrowser.addLinkHandler(PREVIOUS_NEWS_HANDLER_ID, this);
    fBrowser.addLinkHandler(PREVIOUS_UNREAD_NEWS_HANDLER_ID, this);
    fBrowser.addLinkHandler(TRANSFORM_HANDLER_ID, this);
    fBrowser.addLinkHandler(RELATED_NEWS_MENU_HANDLER_ID, this);
    fBrowser.addLinkHandler(NEXT_PAGE_HANDLER_ID, this);
    fBrowser.addLinkHandler(SCROLL_NEXT_PAGE_HANDLER_ID, this);

    /* React on User Interaction (Mouse Scrolling, Mouse Down, Key Pressed) */
    Listener listener = new Listener() {
      public void handleEvent(Event event) {
        onUserInteraction();
      }
    };
    fBrowser.getControl().addListener(SWT.MouseWheel, listener);
    fBrowser.getControl().addListener(SWT.MouseDown, listener);
    fBrowser.getControl().addListener(SWT.KeyDown, listener);
  }

  private void onUserInteraction() {

    /* Return if feature not necessary at all */
    if (!fIsEmbedded || (!fMarkReadOnScrolling && fPageSize == 0))
      return;

    /* Return if disposed or already running */
    if (fBrowser.getControl().isDisposed() || fUserInteractionTracker.isRunning())
      return;

    /* Tell the tracker about user interaction*/
    fUserInteractionTracker.run(new UserInteractionTask(fViewModel, fBrowser));
  }

  private void hookNewsContextMenu() {
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(new IMenuListener() {
      @SuppressWarnings("restriction")
      public void menuAboutToShow(IMenuManager manager) {

        /* Open */
        {
          boolean useSeparator = true;

          /* Open in FeedView */
          if (!fIsEmbedded) {
            manager.add(new Separator("internalopen")); //$NON-NLS-1$
            if (!fCurrentSelection.isEmpty()) {
              manager.appendToGroup("internalopen", new OpenNewsAction(fCurrentSelection, fBrowser.getControl().getShell())); //$NON-NLS-1$
              useSeparator = false;
            }
          }

          manager.add(useSeparator ? new Separator("open") : new GroupMarker("open")); //$NON-NLS-1$ //$NON-NLS-2$

          /* Show only when internal browser is used */
          if (!fCurrentSelection.isEmpty() && !OwlUI.useExternalBrowser())
            manager.add(new OpenInExternalBrowserAction(fCurrentSelection));
        }

        /* Attachments */
        {
          ApplicationActionBarAdvisor.fillAttachmentsMenu(manager, fCurrentSelection, new SameShellProvider(fBrowser.getControl().getShell()), false);
        }

        /* Mark / Label */
        {
          manager.add(new Separator("mark")); //$NON-NLS-1$

          /* Mark */
          MenuManager markMenu = new MenuManager(Messages.NewsBrowserViewer_MARK, "mark"); //$NON-NLS-1$
          manager.add(markMenu);

          /* Mark as Read */
          IAction action = new ToggleReadStateAction(fCurrentSelection);
          action.setEnabled(!fCurrentSelection.isEmpty());
          markMenu.add(action);

          /* Mark All Read */
          action = new MarkAllNewsReadAction();
          markMenu.add(action);

          /* Sticky */
          markMenu.add(new Separator());
          action = new MakeNewsStickyAction(fCurrentSelection);
          action.setEnabled(!fCurrentSelection.isEmpty());
          markMenu.add(action);

          /* Label */
          ApplicationActionBarAdvisor.fillLabelMenu(manager, fCurrentSelection, new SameShellProvider(fBrowser.getControl().getShell()), false);
        }

        /* Move To / Copy To */
        if (!fCurrentSelection.isEmpty()) {
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
          MenuManager moveMenu = new MenuManager(Messages.NewsBrowserViewer_MOVE_TO, "moveto"); //$NON-NLS-1$
          manager.add(moveMenu);

          for (INewsBin bin : newsbins) {
            if (contained(bin, fCurrentSelection))
              continue;

            moveMenu.add(new MoveCopyNewsToBinAction(fCurrentSelection, bin, true));
          }

          moveMenu.add(new MoveCopyNewsToBinAction(fCurrentSelection, null, true));
          moveMenu.add(new Separator());
          moveMenu.add(new AutomateFilterAction(PresetAction.MOVE, fCurrentSelection));

          /* Copy To */
          MenuManager copyMenu = new MenuManager(Messages.NewsBrowserViewer_COPY_TO, "copyto"); //$NON-NLS-1$
          manager.add(copyMenu);

          for (INewsBin bin : newsbins) {
            if (contained(bin, fCurrentSelection))
              continue;

            copyMenu.add(new MoveCopyNewsToBinAction(fCurrentSelection, bin, false));
          }

          copyMenu.add(new MoveCopyNewsToBinAction(fCurrentSelection, null, false));
          copyMenu.add(new Separator());
          copyMenu.add(new AutomateFilterAction(PresetAction.COPY, fCurrentSelection));

          /* Archive */
          manager.add(new ArchiveNewsAction(fCurrentSelection));
        }

        /* Share */
        boolean entityGroupSelected = ModelUtils.isEntityGroupSelected(fCurrentSelection);
        if (!entityGroupSelected)
          ApplicationActionBarAdvisor.fillShareMenu(manager, fCurrentSelection, new SameShellProvider(fBrowser.getControl().getShell()), false);

        manager.add(new Separator("filter")); //$NON-NLS-1$
        manager.add(new Separator("copy")); //$NON-NLS-1$
        manager.add(new GroupMarker("edit")); //$NON-NLS-1$

        /* Collapse All */
        if (entityGroupSelected) {
          manager.add(new Separator());
          ImageDescriptor icon = OwlUI.getImageDescriptor("icons/etool16/collapseall.gif"); //$NON-NLS-1$
          manager.add(new Action(Messages.NewsBrowserViewer_COLLAPSE_GROUPS, icon) {
            @Override
            public void run() {
              Set<Entry<Long, List<Long>>> groups = fViewModel.getGroups().entrySet();
              for (Entry<Long, List<Long>> group : groups) {
                Long groupId = group.getKey();
                if (fViewModel.isGroupVisible(groupId)) {
                  List<Long> newsIds = group.getValue();
                  if (newsIds != null && !newsIds.isEmpty())
                    setGroupExpanded(groupId, newsIds, false);
                }
              }
            };
          });
        }

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        /* Fill Contributions if Context Menu not registered */
        if (fSite == null)
          org.eclipse.ui.internal.ObjectActionContributorManager.getManager().contributeObjectActions(null, manager, NewsBrowserViewer.this);
      }
    });

    /* Create and Register with Workbench */
    fNewsContextMenu = manager.createContextMenu(fBrowser.getControl().getShell());

    /* Register with Part Site if possible */
    if (fSite != null)
      fSite.getEditorSite().registerContextMenu(manager, this);
  }

  private void hookAttachmentsContextMenu() {
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        ApplicationActionBarAdvisor.fillAttachmentsMenu(manager, fCurrentSelection, new SameShellProvider(fBrowser.getControl().getShell()), true);
      }
    });

    /* Create  */
    fAttachmentsContextMenu = manager.createContextMenu(fBrowser.getControl().getShell());
  }

  private void hookLabelContextMenu() {
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        ApplicationActionBarAdvisor.fillLabelMenu(manager, fCurrentSelection, new SameShellProvider(fBrowser.getControl().getShell()), true);
      }
    });

    /* Create  */
    fLabelsContextMenu = manager.createContextMenu(fBrowser.getControl().getShell());
  }

  private void hookShareNewsContextMenu() {
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        ApplicationActionBarAdvisor.fillShareMenu(manager, fCurrentSelection, new SameShellProvider(fBrowser.getControl().getShell()), true);
      }
    });

    /* Create  */
    fShareNewsContextMenu = manager.createContextMenu(fBrowser.getControl().getShell());
  }

  private void hookFindRelatedContextMenu() {
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        if (fCurrentSelection.size() == 1) {
          Object element = fCurrentSelection.getFirstElement();
          if (element instanceof INews) {
            final INews news = (INews) element;
            final String entity = INews.class.getName();

            /* Find Related by Title */
            manager.add(new Action(Messages.NewsBrowserViewer_SIMILAR_CONTENT) {
              @Override
              public void run() {
                List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);
                String headline = CoreUtils.getHeadline(news, false);

                ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, entity);
                ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, headline);
                conditions.add(condition);

                doSearch(conditions, true);
              };
            });

            /* Find Related by Author */
            if (news.getAuthor() != null) {
              IPerson person = news.getAuthor();
              String name = person.getName();
              String email = (person.getEmail() != null) ? person.getEmail().toASCIIString() : null;

              final String author = StringUtils.isSet(name) ? name : email;
              if (StringUtils.isSet(author)) {
                manager.add(new Separator());
                manager.add(new Action(NLS.bind(Messages.NewsBrowserViewer_AUTHORED_BY, escapeForMenu(author))) {
                  @Override
                  public void run() {
                    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);

                    ISearchField field = fFactory.createSearchField(INews.AUTHOR, entity);
                    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, author);
                    conditions.add(condition);

                    doSearch(conditions, false);
                  };
                });
              }
            }

            /* Find Related by Category */
            if (!news.getCategories().isEmpty()) {

              /* Directly show for one category */
              if (news.getCategories().size() == 1) {
                final String name = news.getCategories().get(0).getName();
                if (StringUtils.isSet(name)) {
                  manager.add(new Action(NLS.bind(Messages.NewsBrowserViewer_CATEGORIZED_N, escapeForMenu(name))) {
                    @Override
                    public void run() {
                      List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);

                      ISearchField field = fFactory.createSearchField(INews.CATEGORIES, entity);
                      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, name);
                      conditions.add(condition);

                      doSearch(conditions, false);
                    };
                  });
                }
              }

              /* Use a Sub Menu for many categories */
              else {
                MenuManager categoriesMenu = new MenuManager(Messages.NewsBrowserViewer_BY_CATEGORY);
                for (ICategory category : news.getCategories()) {
                  final String name = category.getName();
                  if (StringUtils.isSet(name)) {
                    categoriesMenu.add(new Action(escapeForMenu(name)) {
                      @Override
                      public void run() {
                        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);

                        ISearchField field = fFactory.createSearchField(INews.CATEGORIES, entity);
                        ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, name);
                        conditions.add(condition);

                        doSearch(conditions, false);
                      };
                    });
                  }
                }
                manager.add(categoriesMenu);
              }
            }

            /* Find Related by Labels */
            if (!news.getLabels().isEmpty()) {
              manager.add(new Separator());
              for (final ILabel label : news.getLabels()) {
                manager.add(new Action(NLS.bind(Messages.NewsBrowserViewer_LABELED_N, escapeForMenu(label.getName()))) {
                  @Override
                  public void run() {
                    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);

                    ISearchField field = fFactory.createSearchField(INews.LABEL, entity);
                    ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, label.getName());
                    conditions.add(condition);

                    doSearch(conditions, false);
                  };
                });
              }
            }
          }
        }
      }
    });

    /* Create  */
    fFindRelatedContextMenu = manager.createContextMenu(fBrowser.getControl().getShell());
  }

  private String escapeForMenu(String str) {
    return StringUtils.replaceAll(str, "&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private void doSearch(final List<ISearchCondition> conditions, final boolean useLowScoreFilter) {
    if (conditions.size() >= 1 && !fBrowser.getControl().isDisposed()) {

      /* See Bug 747 - run asynced */
      delayInUI(new Runnable() {
        public void run() {
          SearchNewsDialog dialog = new SearchNewsDialog(fBrowser.getControl().getShell(), conditions, true, true);
          dialog.setUseLowScoreFilter(useLowScoreFilter);
          dialog.open();
        }
      });
    }
  }

  private boolean contained(INewsBin bin, IStructuredSelection selection) {
    if (selection == null || selection.isEmpty())
      return false;

    Object element = selection.getFirstElement();
    if (element instanceof INews) {
      INews news = (INews) element;
      return news.getParentId() == bin.getId();
    }

    return false;
  }

  void setBlockRefresh(boolean block) {
    fBlockRefresh = block;
  }

  /*
   * @see org.rssowl.ui.internal.ILinkHandler#handle(java.lang.String, java.net.URI)
   */
  public void handle(final String id, URI link) {

    /* Extract Query Part and Decode */
    String query = link.getQuery();
    boolean queryProvided = StringUtils.isSet(query);
    if (queryProvided) {
      query = URIUtils.urlDecode(query).trim();
      queryProvided = StringUtils.isSet(query);
    }

    /* Mark Read */
    if (queryProvided && MARK_READ_HANDLER_ID.equals(id)) {
      final List<INews> news = getNewsList(query);
      Runnable runnable = new Runnable() {
        public void run() {
          INews.State newState = INews.State.READ;
          boolean affectEquivalentNews = OwlUI.markReadDuplicates();
          UndoStack.getInstance().addOperation(new NewsStateOperation(news, newState, affectEquivalentNews));
          fNewsDao.setState(news, newState, affectEquivalentNews, false);
        }
      };

      if (CBrowser.isMozillaRunningOnWindows()) //Bug in XULRunner, otherwise won't work
        delayInUI(runnable);
      else
        runnable.run();
    }

    /*  Toggle Read */
    else if (queryProvided && TOGGLE_READ_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {

        /* Remove Focus from Link */
        blur(Dynamic.TOGGLE_READ_LINK.getId(news));

        /* Update State */
        INews.State newState = (news.getState() == INews.State.READ) ? INews.State.UNREAD : INews.State.READ;
        Set<INews> singleNewsSet = Collections.singleton(news);
        boolean affectEquivalentNews = (newState != INews.State.UNREAD && OwlUI.markReadDuplicates());
        UndoStack.getInstance().addOperation(new NewsStateOperation(singleNewsSet, newState, affectEquivalentNews));
        fNewsDao.setState(singleNewsSet, newState, affectEquivalentNews, false);
        if (newState == INews.State.UNREAD)
          fMarkedUnreadByUserCache.add(news.getId());
      }
    }

    /*  Toggle Sticky */
    else if (queryProvided && TOGGLE_STICKY_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {

        /* Remove Focus from Link */
        blur(Dynamic.TOGGLE_STICKY_LINK.getId(news));
        if (isHeadlinesLayout())
          blur(Dynamic.TINY_TOGGLE_STICKY_LINK.getId(news), true);

        /* Toggle Sticky State */
        Set<INews> singleNewsSet = Collections.singleton(news);
        UndoStack.getInstance().addOperation(new StickyOperation(singleNewsSet, !news.isFlagged()));
        news.setFlagged(!news.isFlagged());
        Controller.getDefault().getSavedSearchService().forceQuickUpdate();
        DynamicDAO.saveAll(singleNewsSet);
      }
    }

    /*  Archive */
    else if (queryProvided && ARCHIVE_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {
        ArchiveNewsAction action = new ArchiveNewsAction(new StructuredSelection(news));
        action.run();
      }
    }

    /*  Delete */
    else if (queryProvided && DELETE_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {
        Set<INews> singleNewsSet = Collections.singleton(news);
        UndoStack.getInstance().addOperation(new NewsStateOperation(singleNewsSet, INews.State.HIDDEN, false));
        fNewsDao.setState(singleNewsSet, INews.State.HIDDEN, false, false);
      }
    }

    /*  Labels Menu */
    else if (queryProvided && LABELS_MENU_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {

        /* Remove Focus from Link */
        blur(Dynamic.LABELS_MENU_LINK.getId(news));

        /* Show Menu */
        setSelection(new StructuredSelection(news));
        Point cursorLocation = fBrowser.getControl().getDisplay().getCursorLocation();
        cursorLocation.y = cursorLocation.y + 16;
        fLabelsContextMenu.setLocation(cursorLocation);
        fLabelsContextMenu.setVisible(true);
      }
    }

    /*  Attachments Menu */
    else if (queryProvided && (ATTACHMENTS_MENU_HANDLER_ID.equals(id) || ATTACHMENT_HANDLER_ID.equals(id))) {
      INews news = getNews(query);
      if (news != null) {

        /* Remove Focus from Link */
        if (ATTACHMENT_HANDLER_ID.equals(id))
          blur(Dynamic.ATTACHMENT_LINK.getId(news));
        else if (ATTACHMENTS_MENU_HANDLER_ID.equals(id))
          blur(Dynamic.ATTACHMENTS_MENU_LINK.getId(news));

        /* Show Menu */
        setSelection(new StructuredSelection(news));
        Point cursorLocation = fBrowser.getControl().getDisplay().getCursorLocation();
        cursorLocation.y = cursorLocation.y + 16;
        fAttachmentsContextMenu.setLocation(cursorLocation);
        fAttachmentsContextMenu.setVisible(true);
      }
    }

    /* Toggle News Item Visibility */
    else if (queryProvided && (EXPAND_NEWS_HANDLER_ID.equals(id) || COLLAPSE_NEWS_HANDLER_ID.equals(id))) {
      INews news = getNews(query);
      if (news != null) {
        setNewsExpanded(news, EXPAND_NEWS_HANDLER_ID.equals(id));
        onUserInteraction();
      }
    }

    /* Toggle Group Items Visibility */
    else if (queryProvided && (EXPAND_GROUP_HANDLER_ID.equals(id) || COLLAPSE_GROUP_HANDLER_ID.equals(id))) {
      long groupId = getId(query);
      List<Long> newsIds = fViewModel.getNewsIds(groupId);
      if (!newsIds.isEmpty())
        setGroupExpanded(groupId, newsIds, EXPAND_GROUP_HANDLER_ID.equals(id));
    }

    /* Group Context Menu */
    else if (queryProvided && GROUP_MENU_HANDLER_ID.equals(id)) {
      EntityGroup group = getEntityGroup(query);
      if (group != null) {

        /* Remove Focus from Link */
        blur(Dynamic.GROUP_MENU_LINK.getId(group));

        /* Show Menu */
        setSelection(new StructuredSelection(group));
        Point cursorLocation = fBrowser.getControl().getDisplay().getCursorLocation();
        cursorLocation.y = cursorLocation.y + 16;
        fNewsContextMenu.setLocation(cursorLocation);
        fNewsContextMenu.setVisible(true);
      }
    }

    /* News Context Menu */
    else if (queryProvided && NEWS_MENU_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {

        /* Remove Focus from Link */
        blur(Dynamic.NEWS_MENU_LINK.getId(news), true);
        blur(Dynamic.FOOTER_NEWS_MENU_LINK.getId(news), true);

        /* Show Menu */
        setSelection(new StructuredSelection(news));
        Point cursorLocation = fBrowser.getControl().getDisplay().getCursorLocation();
        cursorLocation.y = cursorLocation.y + 16;
        fNewsContextMenu.setLocation(cursorLocation);
        fNewsContextMenu.setVisible(true);
      }
    }

    /* Share News Context Menu */
    else if (queryProvided && SHARE_NEWS_MENU_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {

        /* Remove Focus from Link */
        blur(Dynamic.SHARE_MENU_LINK.getId(news));

        /* Show Menu */
        setSelection(new StructuredSelection(news));
        Point cursorLocation = fBrowser.getControl().getDisplay().getCursorLocation();
        cursorLocation.y = cursorLocation.y + 16;
        fShareNewsContextMenu.setLocation(cursorLocation);
        fShareNewsContextMenu.setVisible(true);
      }
    }

    /* Find Related Context Menu */
    else if (queryProvided && RELATED_NEWS_MENU_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null) {

        /* Remove Focus from Link */
        blur(Dynamic.FIND_RELATED_MENU_LINK.getId(news));

        /* Show Menu */
        setSelection(new StructuredSelection(news));
        Point cursorLocation = fBrowser.getControl().getDisplay().getCursorLocation();
        cursorLocation.y = cursorLocation.y + 16;
        fFindRelatedContextMenu.setLocation(cursorLocation);
        fFindRelatedContextMenu.setVisible(true);
      }
    }

    /* Go to Next News / Go to Next Unread News / Go to Previous News / Go to Previous Unread News */
    else if (NEXT_NEWS_HANDLER_ID.equals(id) || NEXT_UNREAD_NEWS_HANDLER_ID.equals(id) || PREVIOUS_NEWS_HANDLER_ID.equals(id) || PREVIOUS_UNREAD_NEWS_HANDLER_ID.equals(id)) {
      Runnable runnable = new Runnable() {
        public void run() {
          handleNavigateAction(id);
        }
      };

      if (CBrowser.isMozillaRunningOnWindows()) //Bug in XULRunner, otherwise won't work
        delayInUI(runnable);
      else
        runnable.run();
    }

    /* Transform News */
    else if (TRANSFORM_HANDLER_ID.equals(id)) {
      INews news = getNews(query);
      if (news != null)
        transformNews(news);
    }

    /* Reveal Next Page */
    else if (NEXT_PAGE_HANDLER_ID.equals(id)) {
      revealNextPage(true);
    }

    /* Scroll Reveal Next Page */
    else if (SCROLL_NEXT_PAGE_HANDLER_ID.equals(id)) {
      Runnable runnable = new Runnable() {
        public void run() {
          revealNextPage(false);
        }
      };

      if (CBrowser.isMozillaRunningOnWindows()) //Bug in XULRunner, otherwise won't work
        delayInUI(runnable);
      else
        runnable.run();
    }
  }

  private void handleNavigateAction(final String id) {

    /* Special Case Navigation in Newspaper mode when some news are hidden from the page */
    if (!isHeadlinesLayout() && fPageSize != 0 && (NEXT_NEWS_HANDLER_ID.equals(id) || NEXT_UNREAD_NEWS_HANDLER_ID.equals(id))) {
      boolean onlyUnread = NEXT_UNREAD_NEWS_HANDLER_ID.equals(id);
      int totalNewsCount = fViewModel.getNewsCount();
      int visibleNewsCount = fViewModel.getVisibleNewsCount();

      /* There are hidden News beyond the Page Latch */
      if (totalNewsCount != 0 && totalNewsCount > visibleNewsCount) {
        long firstHiddenNewsId = fViewModel.getFirstHiddenNews(onlyUnread);
        if (firstHiddenNewsId != -1) {
          showSelection(new StructuredSelection(new NewsReference(firstHiddenNewsId)));
          return;
        }
      }
    }

    /* Forward the navigation action to the outer scope */
    delayInUI(new Runnable() {
      public void run() {
        NavigationActionFactory factory = new NavigationActionFactory();
        try {
          NavigationActionFactory.NavigationActionType type = null;
          if (NEXT_NEWS_HANDLER_ID.equals(id))
            type = NavigationActionFactory.NavigationActionType.NEXT_FEED_NEXT_NEWS;
          else if (NEXT_UNREAD_NEWS_HANDLER_ID.equals(id))
            type = NavigationActionFactory.NavigationActionType.NEXT_UNREAD_FEED_NEXT_UNREAD_NEWS;
          else if (PREVIOUS_NEWS_HANDLER_ID.equals(id))
            type = NavigationActionFactory.NavigationActionType.PREVIOUS_FEED_PREVIOUS_NEWS;
          else if (PREVIOUS_UNREAD_NEWS_HANDLER_ID.equals(id))
            type = NavigationActionFactory.NavigationActionType.PREVIOUS_UNREAD_FEED_PREVIOUS_UNREAD_NEWS;

          if (type != null) {
            factory.setInitializationData(null, null, type.getId());
            IWorkbenchWindowActionDelegate action = (IWorkbenchWindowActionDelegate) factory.create();
            action.run(null);
          }
        } catch (CoreException e) {
          /* Ignore */
        }
      }
    });
  }

  private void setNewsExpanded(INews news, boolean expanded) {
    setNewsExpanded(news, expanded, true);
  }

  private void setNewsExpanded(INews news, boolean expanded, boolean scrollIntoView) {

    /* Return early if visibility already matches state */
    if (expanded == fViewModel.isNewsExpanded(news))
      return;

    /* Link and Image */
    final StringBuilder js = new StringBuilder();
    String newsLink = CoreUtils.getLink(news);

    /* Blur Links */
    js.append(getElementById(Dynamic.TITLE_LINK.getId(news)).append(".blur(); ")); //$NON-NLS-1$

    /* Update Links */
    String link = HANDLER_PROTOCOL + (expanded ? COLLAPSE_NEWS_HANDLER_ID : EXPAND_NEWS_HANDLER_ID) + "?" + news.getId(); //$NON-NLS-1$
    if (expanded && StringUtils.isSet(newsLink))
      js.append(getElementById(Dynamic.TITLE_LINK.getId(news)).append(".href = '" + URIUtils.toManaged(newsLink) + "'; ")); //$NON-NLS-1$ //$NON-NLS-2$
    else
      js.append(getElementById(Dynamic.TITLE_LINK.getId(news)).append(".href='").append(link).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$

    /* Update Toggle Sticky Link Visibility */
    if (expanded)
      js.append(getElementById(Dynamic.TINY_TOGGLE_STICKY_LINK.getId(news))).append(".style.display='none'; "); //$NON-NLS-1$
    else
      js.append(getElementById(Dynamic.TINY_TOGGLE_STICKY_LINK.getId(news))).append(".style.display='inline'; "); //$NON-NLS-1$

    /* Update Subtitle if present */
    if (expanded)
      js.append(getElementById(Dynamic.SUBTITLE_LINK.getId(news))).append(".style.display='none'; "); //$NON-NLS-1$
    else {
      StringBuilder subtitleContent = new StringBuilder();
      IBaseLabelProvider lp = getLabelProvider();
      if (lp instanceof NewsBrowserLabelProvider)
        ((NewsBrowserLabelProvider) lp).fillSubtitle(subtitleContent, news, CoreUtils.getSortedLabels(news), false);
      if (subtitleContent.length() > 0)
        js.append(getElementById(Dynamic.SUBTITLE_LINK.getId(news))).append(".innerHTML='").append(escapeForInnerHtml(subtitleContent.toString())).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$
      js.append(getElementById(Dynamic.SUBTITLE_LINK.getId(news))).append(".style.display='inline'; "); //$NON-NLS-1$
    }

    /* Update News Div Visibility */
    Set<Dynamic> elements = EnumSet.of(Dynamic.SUBLINE, Dynamic.DELETE, Dynamic.CONTENT, Dynamic.FOOTER);
    for (Dynamic element : elements) {
      if (expanded)
        js.append(getElementById(element.getId(news))).append(".style.display='block'; "); //$NON-NLS-1$
      else
        js.append(getElementById(element.getId(news))).append(".style.display='none'; "); //$NON-NLS-1$
    }

    /* Update Headlines Separator if present and parent group (if any) is not collapsed */
    boolean showHeadlinesSeparator = !expanded;
    if (isGroupingEnabled()) {
      long groupId = fViewModel.findGroup(news.getId());
      if (!fViewModel.isGroupExpanded(groupId))
        showHeadlinesSeparator = false;
    }

    js.append("if (").append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(news))).append(") {"); //$NON-NLS-1$ //$NON-NLS-2$
    if (showHeadlinesSeparator)
      js.append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(news))).append(".style.display='block'; "); //$NON-NLS-1$
    else
      js.append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(news))).append(".style.display='none'; "); //$NON-NLS-1$
    js.append("}"); //$NON-NLS-1$

    /* Update Title CSS */
    if (expanded)
      js.append(getElementById(Dynamic.TITLE.getId(news))).append(".className='titleExpanded'; "); //$NON-NLS-1$
    else
      js.append(getElementById(Dynamic.TITLE.getId(news))).append(".className='titleCollapsed'; "); //$NON-NLS-1$

    /* Update News Content as needed */
    if (expanded)
      fillNewsContent(news, js, newsLink);
    else
      js.append(getElementById(Dynamic.CONTENT.getId(news)).append(".innerHTML = ''; ")); //$NON-NLS-1$

    /* Scroll expanded news into view as necessary */
    if (scrollIntoView && expanded)
      scrollIfNecessary(news, js);

    /* Collapse other visible news if present */
    if (expanded) {
      long expandedNewsId = fViewModel.getExpandedNews();
      if (expandedNewsId != -1) {
        INews item = resolve(expandedNewsId);
        if (item != null)
          setNewsExpanded(item, false);
      }
    }

    /* Block external navigation while setting innerHTML */
    fBrowser.blockExternalNavigationWhile(new Runnable() {
      public void run() {
        fBrowser.execute(js.toString(), "setNewsExpanded"); //$NON-NLS-1$
      }
    });

    /* Update State if not already marked as read */
    if (fMarkReadOnExpand && expanded && news.getState() != INews.State.READ && !isGroupingByState()) { //Ignore if grouping by state to avoid refresh
      Set<INews> singleNewsSet = Collections.singleton(news);
      boolean affectEquivalentNews = OwlUI.markReadDuplicates();
      UndoStack.getInstance().addOperation(new NewsStateOperation(singleNewsSet, INews.State.READ, affectEquivalentNews));
      fNewsDao.setState(singleNewsSet, INews.State.READ, affectEquivalentNews, false);
    }

    /* Update Cache of Expanded News */
    fViewModel.setNewsExpanded(news, expanded);
  }

  private void fillNewsContent(INews news, final StringBuilder js, String newsLink) {
    String description = news.getDescription();

    /* Content is provided */
    if (StringUtils.isSet(description) && !description.equals(news.getTitle())) {
      IBaseLabelProvider labelProvider = getLabelProvider();
      if (labelProvider instanceof NewsBrowserLabelProvider) {
        description = ((NewsBrowserLabelProvider) labelProvider).stripMediaTagsIfNecessary(description);
        description = ((NewsBrowserLabelProvider) labelProvider).highlightSearchTermsIfNecessary(description);
      }
    }

    /* Content is not provided */
    else {
      StringBuilder emptyDescription = new StringBuilder();
      emptyDescription.append(Messages.NewsBrowserViewer_NO_CONTENT);
      if (StringUtils.isSet(newsLink)) {
        String link = HANDLER_PROTOCOL + TRANSFORM_HANDLER_ID + "?" + news.getId(); //$NON-NLS-1$
        emptyDescription.append(" <a href=\"").append(link).append("\">").append(Messages.NewsBrowserViewer_DOWNLOAD_CONTENT).append("</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }

      description = emptyDescription.toString();
    }

    js.append(getElementById(Dynamic.CONTENT.getId(news)).append(".innerHTML = '" + escapeForInnerHtml(description) + "'; ")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private void scrollIfNecessary(INews news, final StringBuilder js) {
    if (fBrowser.isIE()) {
      js.append("var scrollPosY = document.body.scrollTop; "); //$NON-NLS-1$
      js.append("var windowHeight = document.body.clientHeight; "); //$NON-NLS-1$
    } else {
      js.append("var scrollPosY = window.pageYOffset; "); //$NON-NLS-1$
      js.append("var windowHeight = window.innerHeight; "); //$NON-NLS-1$
    }

    js.append("var divPosY = ").append(getElementById(Dynamic.NEWS.getId(news))).append(".offsetTop; "); //$NON-NLS-1$ //$NON-NLS-2$
    js.append("var divHeight = ").append(getElementById(Dynamic.NEWS.getId(news))).append(".offsetHeight; "); //$NON-NLS-1$ //$NON-NLS-2$
    js.append("if (scrollPosY > divPosY || divHeight > windowHeight) {"); //$NON-NLS-1$ //Scroll up to reveal the top of the news (also scroll up if the news is larger the client height
    js.append(getElementById(Dynamic.NEWS.getId(news))).append(".scrollIntoView(true); "); //$NON-NLS-1$
    js.append("} else if (scrollPosY + windowHeight < divPosY + divHeight) {"); //$NON-NLS-1$ //Scroll down to reveal the bottom of the news
    js.append(getElementById(Dynamic.NEWS.getId(news))).append(".scrollIntoView(false); "); //$NON-NLS-1$
    js.append("}"); //$NON-NLS-1$
  }

  private void setGroupExpanded(long groupId, List<Long> newsIds, boolean expanded) {
    setGroupExpanded(groupId, newsIds, expanded, true);
  }

  private void setGroupExpanded(long groupId, List<Long> newsIds, boolean expanded, boolean scrollIntoView) {

    /* Image */
    StringBuilder js = new StringBuilder();
    String newToggleImgUri;
    if (fBrowser.isIE())
      newToggleImgUri = expanded ? OwlUI.getImageUri("/icons/elcl16/expanded.gif", "expanded.gif") : OwlUI.getImageUri("/icons/elcl16/collapsed.gif", "collapsed.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    else
      newToggleImgUri = expanded ? ApplicationServer.getDefault().toResourceUrl("/icons/elcl16/expanded.gif") : ApplicationServer.getDefault().toResourceUrl("/icons/elcl16/collapsed.gif"); //$NON-NLS-1$ //$NON-NLS-2$

    /* Blur Links */
    js.append(getElementById(Dynamic.TOGGLE_GROUP_LINK.getId(groupId)).append(".blur(); ")); //$NON-NLS-1$
    js.append(getElementById(Dynamic.GROUP_MENU_LINK.getId(groupId)).append(".blur(); ")); //$NON-NLS-1$

    /* Update Links */
    String toggleVisibilityLink = HANDLER_PROTOCOL + (expanded ? COLLAPSE_GROUP_HANDLER_ID : EXPAND_GROUP_HANDLER_ID) + "?" + groupId; //$NON-NLS-1$
    js.append(getElementById(Dynamic.TOGGLE_GROUP_LINK.getId(groupId)).append(".href='").append(toggleVisibilityLink).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$
    if (expanded)
      js.append(getElementById(Dynamic.GROUP_MENU_LINK.getId(groupId)).append(".href='").append(HANDLER_PROTOCOL + GROUP_MENU_HANDLER_ID + "?" + groupId).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    else
      js.append(getElementById(Dynamic.GROUP_MENU_LINK.getId(groupId)).append(".href='").append(toggleVisibilityLink).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$

    /* Update Triangle Image */
    js.append(getElementById(Dynamic.TOGGLE_GROUP_IMG.getId(groupId)).append(".src = '" + newToggleImgUri + "'; ")); //$NON-NLS-1$ //$NON-NLS-2$

    /* Update Visibility */
    for (Long id : newsIds) {
      if (!fViewModel.isNewsVisible(id))
        continue; //Skip news if it is actually hidden

      /* News Item */
      if (expanded)
        js.append(getElementById(Dynamic.NEWS.getId(id))).append(".style.display='block'; "); //$NON-NLS-1$
      else
        js.append(getElementById(Dynamic.NEWS.getId(id))).append(".style.display='none'; "); //$NON-NLS-1$

      /* Separator if using headlines layout */
      if (isHeadlinesLayout()) {
        js.append("if (").append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(id))).append(") {"); //$NON-NLS-1$ //$NON-NLS-2$
        if (expanded)
          js.append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(id))).append(".style.display='block'; "); //$NON-NLS-1$
        else
          js.append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(id))).append(".style.display='none'; "); //$NON-NLS-1$
        js.append("}"); //$NON-NLS-1$
      }
    }

    /* Scroll expanded group into view as necessary */
    if (scrollIntoView && expanded && !newsIds.isEmpty()) {
      if (fBrowser.isIE()) {
        js.append("var scrollPosY = document.body.scrollTop; "); //$NON-NLS-1$
        js.append("var windowHeight = document.body.clientHeight; "); //$NON-NLS-1$
      } else {
        js.append("var scrollPosY = window.pageYOffset; "); //$NON-NLS-1$
        js.append("var windowHeight = window.innerHeight; "); //$NON-NLS-1$
      }

      /* Find the last visible news of the group */
      long lastVisibleNewsId = -1;
      for (int i = newsIds.size() - 1; i >= 0; i--) {
        if (fViewModel.isNewsVisible(newsIds.get(i))) {
          lastVisibleNewsId = newsIds.get(i);
          break;
        }
      }

      if (lastVisibleNewsId != -1) {
        js.append("var divPosY = ").append(getElementById(Dynamic.NEWS.getId(lastVisibleNewsId))).append(".offsetTop; "); //$NON-NLS-1$ //$NON-NLS-2$
        js.append("var divHeight = ").append(getElementById(Dynamic.NEWS.getId(lastVisibleNewsId))).append(".offsetHeight; "); //$NON-NLS-1$ //$NON-NLS-2$
        js.append("if (scrollPosY + windowHeight < divPosY + divHeight) {"); //$NON-NLS-1$ //Scroll up to reveal as much news as possible from the group
        js.append(getElementById(Dynamic.TOGGLE_GROUP_IMG.getId(groupId))).append(".scrollIntoView(true); "); //$NON-NLS-1$
        js.append("}"); //$NON-NLS-1$
      }
    }

    /* Execute */
    fBrowser.execute(js.toString(), "setGroupExpanded"); //$NON-NLS-1$

    /* Update View Model */
    fViewModel.setGroupExpanded(groupId, expanded);
  }

  private void blur(String elementId) {
    blur(elementId, false);
  }

  private void blur(String elementId, boolean guardNull) {
    StringBuilder js = new StringBuilder();
    if (guardNull) {
      js.append("if (").append(getElementById(elementId)).append(") {"); //$NON-NLS-1$ //$NON-NLS-2$
      js.append(getElementById(elementId).append(".blur();")); //$NON-NLS-1$
      js.append("}"); //$NON-NLS-1$
    } else
      js.append(getElementById(elementId).append(".blur();")); //$NON-NLS-1$
    fBrowser.execute(js.toString(), "blur"); //$NON-NLS-1$
  }

  private void transformNews(final INews news) {
    String link = CoreUtils.getLink(news);
    if (!StringUtils.isSet(link))
      return;

    /* Indicate Progress */
    StringBuilder js = new StringBuilder();
    js.append(getElementById(Dynamic.FULL_CONTENT_LINK_TEXT.getId(news)).append(".innerHTML='").append(escapeForInnerHtml(Messages.NewsBrowserViewer_LOADING)).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$
    js.append(getElementById(Dynamic.FULL_CONTENT_LINK.getId(news)).append(".blur(); ")); //$NON-NLS-1$
    js.append(getElementById(Dynamic.FULL_CONTENT_LINK.getId(news)).append(".style.fontStyle = 'italic'; ")); //$NON-NLS-1$
    fBrowser.execute(js.toString(), "transformNews"); //$NON-NLS-1$

    /* First cancel all running jobs for this news if any */
    NewsReference reference = news.toReference();
    Job.getJobManager().cancel(reference);

    /* Load news content in background and update HTML afterwards */
    final String transformedUrl = Controller.getDefault().getEmbeddedTransformedUrl(link);
    UIBackgroundJob transformationJob = new UIBackgroundJob(fBrowser.getControl(), Messages.NewsBrowserViewer_RETRIEVING_ARTICLE_CONTENT, reference) {
      StringBuilder result = new StringBuilder();

      @Override
      protected void runInBackground(IProgressMonitor monitor) {
        try {
          URI uri = new URI(transformedUrl);
          IProtocolHandler handler = Owl.getConnectionService().getHandler(uri);
          if (handler != null) {
            BufferedReader reader = null;
            try {
              InputStream inS = handler.openStream(uri, monitor, null);
              reader = new BufferedReader(new InputStreamReader(inS, "UTF-8")); //$NON-NLS-1$
              String line;
              while (!monitor.isCanceled() && (line = reader.readLine()) != null) {
                result.append(line);
              }
            } catch (IOException e) {
              Activator.getDefault().logError(e.getMessage(), e);
              monitor.setCanceled(true);
            } finally {
              if (reader != null) {
                try {
                  reader.close();
                } catch (IOException e) {
                  monitor.setCanceled(true);
                }
              }
            }
          }
        } catch (URISyntaxException e) {
          Activator.getDefault().logError(e.getMessage(), e);
          monitor.setCanceled(true);
        } catch (ConnectionException e) {
          Activator.getDefault().logError(e.getMessage(), e);
          monitor.setCanceled(true);
        }
      }

      @Override
      protected void runInUI(IProgressMonitor monitor) {
        if (result.length() > 0 && !monitor.isCanceled() && !fBrowser.getControl().isDisposed())
          showTransformation(news, result.toString());
      }
    };

    JobRunner.runUIUpdater(transformationJob, true);
  }

  private void showTransformation(INews news, String result) {

    /* Transformer script surrounds content with a DIV, display it inline to avoid extra whitespace */
    int index = result.indexOf("<div"); //$NON-NLS-1$
    if (index != -1 && index < 10) { //Ensure the DIV is really at the beginning
      index += "<div".length(); //$NON-NLS-1$
      StringBuilder inlineResult = new StringBuilder();
      inlineResult.append(result.substring(0, index));
      inlineResult.append(" style=\"display: inline;\" "); //$NON-NLS-1$
      inlineResult.append(result.substring(index));
      result = inlineResult.toString();
    }

    /* Support stripping media tags if set and highlight search terms if any */
    IBaseLabelProvider labelProvider = getLabelProvider();
    if (labelProvider instanceof NewsBrowserLabelProvider) {
      result = ((NewsBrowserLabelProvider) labelProvider).stripMediaTagsIfNecessary(result);
      result = ((NewsBrowserLabelProvider) labelProvider).highlightSearchTermsIfNecessary(result);
    }

    final StringBuilder js = new StringBuilder();
    js.append(getElementById(Dynamic.CONTENT.getId(news)).append(".innerHTML='").append(escapeForInnerHtml(result)).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$
    js.append(getElementById(Dynamic.FULL_CONTENT_LINK.getId(news)).append(".style.fontStyle = 'normal'; ")); //$NON-NLS-1$
    js.append(getElementById(Dynamic.FULL_CONTENT_LINK_TEXT.getId(news)).append(".innerHTML='").append(escapeForInnerHtml(Messages.NewsBrowserViewer_FULL_CONTENT)).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$
    scrollIfNecessary(news, js);

    /* Block external navigation while setting innerHTML */
    fBrowser.blockExternalNavigationWhile(new Runnable() {
      public void run() {
        fBrowser.execute(js.toString(), "showTransformation"); //$NON-NLS-1$
      }
    });
  }

  private String escapeForInnerHtml(String str) {
    StringBuilder result = new StringBuilder(str.length());

    BufferedReader reader = new BufferedReader(new StringReader(str));
    String line;
    try {
      while ((line = reader.readLine()) != null) {

        /* Escape single and double quotes */
        line = StringUtils.replaceAll(line, "\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        line = StringUtils.replaceAll(line, "'", "\\'"); //$NON-NLS-1$ //$NON-NLS-2$

        /* XULRunner: Need to escape % with its ASCII HEX counterpart as XUL decodes it */
        if (CBrowser.isMozillaRunningOnWindows())
          line = StringUtils.replaceAll(line, "%", "%25"); //$NON-NLS-1$ //$NON-NLS-2$

        /* Properly escape multilines */
        result.append(line).append("\\n"); //$NON-NLS-1$
      }
    } catch (IOException e) {
    }

    return result.toString().trim();
  }

  @SuppressWarnings("unchecked")
  private void revealNextPage(boolean scrollIntoView) {

    /* Get next page from View Model */
    Pair<List<Long>, List<Long>> nextPage = fViewModel.getNextPage(fPageSize);
    List<Long> revealedGroups = nextPage.getFirst();
    List<Long> revealedNews = nextPage.getSecond();

    revealItems(revealedGroups, revealedNews, scrollIntoView);
  }

  @SuppressWarnings("unchecked")
  private void revealItems(List<Long> revealedGroups, List<Long> revealedNews, boolean scrollIntoView) {

    /* Return early if no more news to reveal */
    if (revealedNews.isEmpty())
      return;

    final StringBuilder js = new StringBuilder();

    /* Blur Links */
    if (scrollIntoView) //Action was invoked from latch
      js.append(getElementById(Dynamic.PAGE_LATCH_LINK.getId()).append(".blur(); ")); //$NON-NLS-1$

    /* Check if the first revealed news belongs to a group that is already showing but collapsed */
    long group = fViewModel.findGroup(revealedNews.get(0));
    if (group != -1 && fViewModel.isGroupVisible(group) && !fViewModel.isGroupExpanded(group))
      setGroupExpanded(group, fViewModel.getNewsIds(group), true, false);

    /* Reveal Groups */
    for (Long groupId : revealedGroups) {
      if (fViewModel.isGroupVisible(groupId))
        continue; //Skip if already visible

      /* Update View Model */
      fViewModel.setGroupVisible(groupId, true);

      /* Show Group */
      js.append(getElementById(Dynamic.GROUP.getId(groupId))).append(".style.display='block'; "); //$NON-NLS-1$
    }

    /* Reveal News */
    Long firstNewsId = null;
    for (Long newsId : revealedNews) {
      if (fViewModel.isNewsVisible(newsId))
        continue; //Skip if already visible

      /* Remember the first news made visible to scroll to */
      if (firstNewsId == null)
        firstNewsId = newsId;

      /* Update View Model */
      fViewModel.setNewsVisible(newsId, true);

      /* Get News from Contentprovider */
      INews news = resolve(newsId);
      if (news == null)
        continue;

      final StringBuilder newsJs = new StringBuilder(); //Use own builder since News JS can be large depending on content

      String html = ((NewsBrowserLabelProvider) getLabelProvider()).getLabel(news, true, true, true, fViewModel.indexOfNewsItem(newsId));
      newsJs.append(getElementById(Dynamic.NEWS.getId(newsId))).append(".style.display='block'; "); //$NON-NLS-1$
      newsJs.append(getElementById(Dynamic.NEWS.getId(newsId))).append(".innerHTML ='").append(escapeForInnerHtml(html)).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$;
      if (isHeadlinesLayout())
        newsJs.append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(newsId))).append(".style.display='block'; "); //$NON-NLS-1$

      /* Block external navigation while setting innerHTML */
      fBrowser.blockExternalNavigationWhile(new Runnable() {
        public void run() {
          fBrowser.execute(newsJs.toString(), "revealItems#0"); //$NON-NLS-1$
        }
      });
    }

    /* Update Scroll Position */
    if (firstNewsId != null && scrollIntoView)
      js.append(getElementById(Dynamic.NEWS.getId(firstNewsId))).append(".scrollIntoView(true); "); //$NON-NLS-1$

    /* Update Latch if necessary */
    updateLatchIfNecessary(js);

    /* Block external navigation while setting innerHTML */
    fBrowser.blockExternalNavigationWhile(new Runnable() {
      public void run() {
        fBrowser.execute(js.toString(), "revealItems#1"); //$NON-NLS-1$
      }
    });
  }

  private void updateLatchIfNecessary(StringBuilder js) {
    if (fPageSize == 0)
      return; //Return if pagination turned off

    /* Latch no longer required - hide */
    if (fViewModel.getVisibleNewsCount() == fViewModel.getNewsCount()) {
      js.append("var latch = ").append(getElementById(Dynamic.PAGE_LATCH.getId())).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
      js.append("if (latch) {"); //$NON-NLS-1$
      js.append("  latch.style.display='none'; "); //$NON-NLS-1$
      js.append("}"); //$NON-NLS-1$
    }

    /* Otherwise update Latch Label */
    else if (getLabelProvider() instanceof NewsBrowserLabelProvider) {
      String updatedLatchName = ((NewsBrowserLabelProvider) getLabelProvider()).getLatchName();
      js.append("var latch = ").append(getElementById(Dynamic.PAGE_LATCH_TEXT.getId())).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
      js.append("if (latch) {"); //$NON-NLS-1$
      js.append("  latch.innerHTML='").append(escapeForInnerHtml(updatedLatchName)).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$
      js.append("}"); //$NON-NLS-1$
    }
  }

  private void delayInUI(Runnable runnable) {
    JobRunner.runInUIThread(0, true, getControl(), runnable);
  }

  private long getId(String query) {
    return Long.parseLong(query);
  }

  private INews getNews(String query) {
    try {
      long id = getId(query);
      return resolve(id);
    } catch (NullPointerException e) {
      return null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private List<INews> getNewsList(String query) {
    List<INews> news = new ArrayList<INews>();

    StringTokenizer tokenizer = new StringTokenizer(query, ","); //$NON-NLS-1$
    while (tokenizer.hasMoreTokens()) {
      String nextElement = tokenizer.nextToken();
      if (StringUtils.isSet(nextElement)) {
        INews item = getNews(nextElement);
        if (item != null)
          news.add(item);
      }
    }

    return news;
  }

  private EntityGroup getEntityGroup(String query) {
    long id = getId(query);

    /* Try to resolve the news from the mapping table */
    List<Long> newsIds = fViewModel.getNewsIds(id);
    if (!newsIds.isEmpty()) {
      List<INews> news = new ArrayList<INews>(newsIds.size());
      for (Long newsId : newsIds) {
        try {
          INews item = resolve(newsId);
          if (item != null)
            news.add(item);
        } catch (NullPointerException e) {
          continue;
        }
      }

      /* Create a temporary new EntityGroup to be used from the context menu */
      EntityGroup group = new EntityGroup(id, NewsGrouping.GROUP_CATEGORY_ID);
      for (INews item : news) {
        new EntityGroupItem(group, item);
      }

      return group;
    }

    return null;
  }

  /*
   * @see org.eclipse.jface.viewers.ContentViewer#setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)
   */
  @Override
  public void setLabelProvider(IBaseLabelProvider labelProvider) {
    fBlockRefresh = true;
    try {
      super.setLabelProvider(labelProvider);
    } finally {
      fBlockRefresh = false;
    }
  }

  /*
   * @see org.eclipse.jface.viewers.ContentViewer#setContentProvider(org.eclipse.jface.viewers.IContentProvider)
   */
  @Override
  public void setContentProvider(IContentProvider contentProvider) {
    fBlockRefresh = true;
    try {
      super.setContentProvider(contentProvider);
    } finally {
      fBlockRefresh = false;
    }
  }

  /*
   * @see org.eclipse.jface.viewers.Viewer#refresh()
   */
  @Override
  public void refresh() {
    if (!fBlockRefresh) {
      fBrowser.refresh();
      onRefresh();
    }
  }

  /**
   * A special way of refreshing this viewer with additional options to control
   * the behavior.
   *
   * @param restoreInput if set to <code>true</code> will restore the initial
   * input that was set to the browser in case the user navigated to a different
   * URL.
   * @param moveToTop if <code>true</code> will scroll the browser to the top
   * position to reveal additional content.
   */
  public void refresh(boolean restoreInput, boolean moveToTop) {

    /* Browser not showing initial input anymore, so restore if asked for */
    if (restoreInput && !ApplicationServer.getDefault().isDisplayOperation(fBrowser.getControl().getUrl()))
      internalSetInput(fInput, true, false);

    /* Otherwise perform the normal refresh */
    else {

      /* Move scroll position to top if set */
      if (moveToTop)
        fBrowser.execute("scroll(0,0);", "refresh"); //$NON-NLS-1$ //$NON-NLS-2$

      /* Refresh */
      refresh();
    }
  }

  /**
   * Method is called whenever the viewer is refreshed. Subclasses my override
   * to do something.
   */
  protected void onRefresh() {
    //Do nothing here.
  }

  /*
   * @see org.eclipse.jface.viewers.ContentViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
   */
  @Override
  protected void handleDispose(DisposeEvent event) {
    fServer.unregister(fId);
    fCurrentSelection = null;
    OwlUI.safeDispose(fNewsContextMenu);
    OwlUI.safeDispose(fAttachmentsContextMenu);
    OwlUI.safeDispose(fLabelsContextMenu);
    OwlUI.safeDispose(fShareNewsContextMenu);
    OwlUI.safeDispose(fFindRelatedContextMenu);
    super.handleDispose(event);
  }

  /*
   * @see org.eclipse.jface.viewers.ContentViewer#setInput(java.lang.Object)
   */
  @Override
  public void setInput(Object input) {
    setInput(input, false);
  }

  /**
   * @param input the input to show in this news browser viewer.
   * @param blockExternalNavigation <code>true</code> to block any potential
   * external navigation when setting the input and <code>false</code> otherwise
   * (default).
   */
  public void setInput(Object input, boolean blockExternalNavigation) {
    internalSetInput(input, false, blockExternalNavigation);
  }

  /**
   * @param newLayout the new {@link Layout} to use in this viewer.
   */
  void onLayoutChanged(Layout newLayout) {
    if (fSite == null)
      return;

    /* Update Page Size */
    if (newLayout == Layout.HEADLINES || newLayout == Layout.NEWSPAPER)
      fPageSize = OwlUI.getPageSize(fSite.getInputPreferences()).getPageSize();
    else
      fPageSize = PageSize.NO_PAGING.getPageSize();

    /* Update "Mark Read on Scrolling" */
    fMarkReadOnScrolling = (newLayout == Layout.NEWSPAPER) && fSite.getInputPreferences().getBoolean(DefaultPreferences.MARK_READ_ON_SCROLLING);
  }

  private void internalSetInput(Object input, boolean force, boolean blockExternalNavigation) {

    /* Ignore this Input if its already set */
    if (!force && sameInput(input))
      return;

    /* Remember Input */
    fInput = input;

    /* Clear Cache of news marked as unread by user */
    fMarkedUnreadByUserCache.clear();

    /* Update Settings based on Input */
    if (fSite != null) {
      IPreferenceScope inputPreferences = fSite.getInputPreferences();
      fMarkReadOnExpand = inputPreferences.getBoolean(DefaultPreferences.MARK_READ_STATE);

      /* Update settings based on Layout */
      Layout layout = OwlUI.getLayout(inputPreferences);
      onLayoutChanged(layout);
    }

    /* Stop any other Website if required */
    String url = fBrowser.getControl().getUrl();
    if (!"".equals(url)) //$NON-NLS-1$
      fBrowser.getControl().stop();

    /* Input is a URL - display it */
    if (input instanceof String) {
      fBrowser.setUrl((String) input, !blockExternalNavigation);
      return;
    }

    /* Set URL if its not already showing and contains a display-operation */
    String inputUrl = fServer.toUrl(fId, input);
    if (fServer.isDisplayOperation(inputUrl) && !inputUrl.equals(url))
      fBrowser.setUrl(inputUrl);

    /* Hide the Browser as soon as the input is set to Null */
    if (input == null && fBrowser.getControl().getVisible())
      fBrowser.getControl().setVisible(false);
  }

  /* Checks wether the given Input is same to the existing one */
  private boolean sameInput(Object input) {
    if (fInput instanceof Object[])
      return input instanceof Object[] && Arrays.equals((Object[]) fInput, (Object[]) input);

    if (fInput != null)
      return fInput.equals(input);

    return false;
  }

  /*
   * @see org.eclipse.jface.viewers.ContentViewer#getInput()
   */
  @Override
  public Object getInput() {
    return fInput;
  }

  /**
   * Adds the given filter to this viewer.
   *
   * @param filter a viewer filter
   */
  public void addFilter(ViewerFilter filter) {
    if (fFilters == null)
      fFilters = new ArrayList<ViewerFilter>();

    fFilters.add(filter);
    if (filter instanceof NewsFilter)
      fNewsFilter = (NewsFilter) filter;
  }

  /**
   * Removes the given filter from this viewer, and triggers refiltering and
   * resorting of the elements if required. Has no effect if the identical
   * filter is not registered.
   *
   * @param filter a viewer filter
   */
  public void removeFilter(ViewerFilter filter) {
    Assert.isNotNull(filter);
    if (fFilters != null) {
      for (Iterator<ViewerFilter> i = fFilters.iterator(); i.hasNext();) {
        Object o = i.next();
        if (o == filter) {
          i.remove();
          refresh();
          if (fFilters.size() == 0)
            fFilters = null;

          return;
        }
      }
    }

    if (filter == fNewsFilter)
      fNewsFilter = null;
  }

  /**
   * @param comparator
   */
  public void setComparator(ViewerComparator comparator) {
    if (fSorter != comparator)
      fSorter = comparator;
  }

  /*
   * @see org.eclipse.jface.viewers.Viewer#getControl()
   */
  @Override
  public Control getControl() {
    return fBrowser.getControl();
  }

  /**
   * @return The wrapped Browser (CBrowser).
   */
  public CBrowser getBrowser() {
    return fBrowser;
  }

  /*
   * @see org.eclipse.jface.viewers.Viewer#getSelection()
   */
  @Override
  public ISelection getSelection() {
    return fCurrentSelection;
  }

  /*
   * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection,
   * boolean)
   */
  @Override
  public void setSelection(ISelection selection, boolean reveal) {
    fCurrentSelection = (IStructuredSelection) selection;
    fireSelectionChanged(new SelectionChangedEvent(this, selection));
  }

  /* Find a news from the selection and scroll it into view using JavaScript */
  void showSelection(ISelection selection) {
    if (!(selection instanceof StructuredSelection) || selection.isEmpty())
      return;

    /* Find the News to Show */
    NewsReference newsToShow = null;
    Object firstElement = ((StructuredSelection) selection).getFirstElement();
    if (firstElement instanceof INews)
      newsToShow = ((INews) firstElement).toReference();
    else if (firstElement instanceof NewsReference)
      newsToShow = (NewsReference) firstElement;

    /* Scroll the News into View if present and expand as necessary */
    if (newsToShow != null) {

      /* First determine if there are hidden elements that need to be expanded */
      Pair<List<Long>, List<Long>> itemsToReveal = fViewModel.revealPage(newsToShow.getId(), fPageSize);
      revealItems(itemsToReveal.getFirst(), itemsToReveal.getSecond(), false);

      /* Headlines Layout */
      if (isHeadlinesLayout()) {
        INews news = resolve(newsToShow.getId());
        if (news != null)
          setNewsExpanded(news, true);
      }

      /* Newspaper Layout */
      else {
        StringBuilder js = new StringBuilder();
        js.append(getElementById(Dynamic.NEWS.getId(newsToShow))).append(".scrollIntoView(true);"); //$NON-NLS-1$
        fBrowser.execute(js.toString(), "showSelection"); //$NON-NLS-1$
      }
    }
  }

  void navigate(boolean next, boolean unread, boolean onInputSet) {

    /* Navigate in Headlines Layout based on expanded element */
    if (isHeadlinesLayout())
      navigateInHeadlines(next, unread);

    /* Navigate in Newspaper Layout based on scroll position */
    else
      navigateInNewspaper(next, unread, onInputSet);
  }

  private void navigateInNewspaper(boolean next, boolean unread, boolean onInputSet) {

    /* Check if the first news is already unread and in this case avoid navigation */
    if (unread && onInputSet && fViewModel.isFirstItemUnread())
      return;

    /* Otherwise need to navigate to a specific unread news */
    StringBuilder js = new StringBuilder();
    if (fBrowser.isIE())
      js.append("var scrollPosY = document.body.scrollTop; "); //$NON-NLS-1$
    else
      js.append("var scrollPosY = window.pageYOffset; "); //$NON-NLS-1$
    js.append("var body = ").append(getElementById(BODY_ELEMENT_ID)).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
    js.append("var divs = body.childNodes; "); //$NON-NLS-1$

    /* Next News (need to fake Y position by some pixels to avoid the same news being selected over and over) */
    if (next) {
      js.append("  for (var i = 1; i < divs.length; i++) { "); //$NON-NLS-1$
      js.append("    if (divs[i].nodeType != 1) { "); //$NON-NLS-1$
      js.append("      continue; "); //$NON-NLS-1$
      js.append("    } "); //$NON-NLS-1$
      js.append("    var divPosY = divs[i].offsetTop; "); //$NON-NLS-1$
      if (unread) {
        js.append("  if (divPosY > scrollPosY + 15 && divs[i].className == \"newsitemUnread\") { "); //$NON-NLS-1$
      } else
        js.append("  if (divPosY > scrollPosY + 15 && (divs[i].className == \"newsitemUnread\" || divs[i].className == \"newsitemRead\")) { "); //$NON-NLS-1$
      js.append("      divs[i].scrollIntoView(true); "); //$NON-NLS-1$
      js.append("      break; "); //$NON-NLS-1$
      js.append("    } "); //$NON-NLS-1$
      js.append("  } "); //$NON-NLS-1$
    }

    /* Previous News (need to fake Y position by some pixels to avoid the same news being selected over and over) */
    else {
      js.append("  for (var i = divs.length - 1; i >= 0; i--) { "); //$NON-NLS-1$
      js.append("    if (divs[i].nodeType != 1) { "); //$NON-NLS-1$
      js.append("      continue; "); //$NON-NLS-1$
      js.append("    } "); //$NON-NLS-1$
      js.append("    var divPosY = divs[i].offsetTop; "); //$NON-NLS-1$
      if (unread) {
        js.append("  if (divPosY < scrollPosY - 15 && divs[i].className == \"newsitemUnread\") { "); //$NON-NLS-1$
      } else
        js.append("  if (divPosY < scrollPosY - 15 && (divs[i].className == \"newsitemUnread\" || divs[i].className == \"newsitemRead\")) { "); //$NON-NLS-1$
      js.append("      divs[i].scrollIntoView(true); "); //$NON-NLS-1$
      js.append("      break; "); //$NON-NLS-1$
      js.append("    } "); //$NON-NLS-1$
      js.append("  } "); //$NON-NLS-1$
    }

    /* See if the Scroll Position Changed at all and handle */
    if (fBrowser.isIE())
      js.append("var newScrollPosY = document.body.scrollTop; "); //$NON-NLS-1$
    else
      js.append("var newScrollPosY = window.pageYOffset; "); //$NON-NLS-1$

    if (unread || fViewModel.hasItems()) { //Workaround for a Bug that would cause endless navigation if viewer contains no news otherwise
      js.append("if (scrollPosY == newScrollPosY) { "); //$NON-NLS-1$
      js.append("  window.location.href = '").append(ILinkHandler.HANDLER_PROTOCOL + getNavigationActionId(next, unread)).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$
      js.append("} "); //$NON-NLS-1$
    }

    /* Execute in Browser */
    fBrowser.execute(js.toString(), "navigateInNewspaper"); //$NON-NLS-1$

    /* If we are at the end of the page, reveal more items if possible */
    onUserInteraction();
  }

  private void navigateInHeadlines(boolean next, boolean unread) {
    long targetNews = -1L;
    long offset = fViewModel.getExpandedNews();

    /* Navigate to Next News */
    if (next)
      targetNews = fViewModel.nextNews(unread, offset);

    /* Navigate to Previous News */
    else
      targetNews = fViewModel.previousNews(unread, offset);

    /* Expand target and the parent group if necessary */
    if (targetNews != -1) {

      /* First determine if there are hidden elements that need to be expanded */
      Pair<List<Long>, List<Long>> itemsToReveal = fViewModel.revealPage(targetNews, fPageSize);
      revealItems(itemsToReveal.getFirst(), itemsToReveal.getSecond(), false);

      /* Group */
      long groupId = fViewModel.findGroup(targetNews);
      if (groupId != -1 && !fViewModel.isGroupExpanded(groupId)) {
        List<Long> newsIds = fViewModel.getNewsIds(groupId);
        if (!newsIds.isEmpty())
          setGroupExpanded(groupId, newsIds, true, false);
      }

      /* News */
      setNewsExpanded(resolve(targetNews), true, false);
      StringBuilder js = new StringBuilder();
      js.append(getElementById(Dynamic.NEWS.getId(targetNews))).append(".scrollIntoView(true); "); //$NON-NLS-1$
      fBrowser.execute(js.toString(), "navigateInHeadlines#0"); //$NON-NLS-1$

      /* If we are at the end of the page, reveal more items if possible */
      onUserInteraction();
    }

    /* If navigation did not find a suitable target, call the outer navigation function */
    else if (unread || fViewModel.hasItems()) { //Workaround for a Bug that would cause endless navigation if viewer contains no news otherwise
      StringBuilder js = new StringBuilder();
      js.append("window.location.href = '").append(ILinkHandler.HANDLER_PROTOCOL + getNavigationActionId(next, unread)).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$
      fBrowser.execute(js.toString(), "navigateInHeadlines#1"); //$NON-NLS-1$
    }
  }

  private String getNavigationActionId(boolean next, boolean unread) {
    if (next) {
      if (unread)
        return NewsBrowserViewer.NEXT_UNREAD_NEWS_HANDLER_ID;

      return NewsBrowserViewer.NEXT_NEWS_HANDLER_ID;
    }

    if (unread)
      return NewsBrowserViewer.PREVIOUS_UNREAD_NEWS_HANDLER_ID;

    return NewsBrowserViewer.PREVIOUS_NEWS_HANDLER_ID;
  }

  /**
   * Shows the intial Input in the Browser.
   */
  public void home() {
    internalSetInput(fInput, true, false);
  }

  /**
   * @return the {@link ViewerComparator} used for sorting news.
   */
  ViewerComparator getComparator() {
    return fSorter;
  }

  private Object[] getSortedChildren(Object parent) {
    Object[] result = getFilteredChildren(parent);
    if (fSorter != null) {

      /* Avoid modifying the original array from the model */
      result = result.clone();
      fSorter.sort(this, result);
    }

    return result;
  }

  private Object[] getFilteredChildren(Object parent) {
    Object[] result = getRawChildren(parent);

    /* Never filter a selected News, thereby return here */
    if (fInput instanceof INews)
      return result;

    /* Run Filters over result */
    if (fFilters != null) {
      for (Object filter : fFilters) {
        ViewerFilter f = (ViewerFilter) filter;
        result = f.filter(this, parent, result);
      }
    }
    return result;
  }

  private Object[] getRawChildren(Object parent) {
    Object[] result = null;
    if (parent != null) {
      IStructuredContentProvider cp = (IStructuredContentProvider) getContentProvider();
      if (cp != null)
        result = cp.getElements(parent);
    }
    return (result != null) ? result : new Object[0];
  }

  /**
   * Asks the NewsViewModel to update based on the given input.
   *
   * @param input the list of elements that becomes visible in the browser
   * viewer.
   */
  public void updateViewModel(Object[] input) {
    fViewModel.setInput(input, fPageSize);
  }

  /**
   * @param input Can either be an Array of Feeds or News
   * @return An flattend array of Objects.
   */
  public Object[] getFlattendChildren(Object input) {
    return getFlattendChildren(input, true);
  }

  /**
   * @param input Can either be an Array of Feeds or News
   * @param withGroups if <code>true</code> also return groups if present.
   * @return An flattend array of Objects.
   */
  public Object[] getFlattendChildren(Object input, boolean withGroups) {

    /* Using NewsContentProvider */
    if (input != null && getContentProvider() instanceof NewsContentProvider) {
      NewsContentProvider cp = (NewsContentProvider) getContentProvider();

      /*
       * Flatten Children since Grouping is Enabled and the Parent is not
       * containing just News (so either Feed or ViewerGroups).
       */
      if (cp.isGroupingEnabled() && !isNews(input)) {
        List<Object> flatList = new ArrayList<Object>();

        /* Wrap into Object-Array */
        if (!(input instanceof Object[]))
          input = new Object[] { input };

        /* For each Group retrieve Children (sorted and filtered) */
        int newsCount = 0;
        Object groups[] = (Object[]) input;
        for (Object group : groups) {

          /* Make sure this child has children */
          if (cp.hasChildren(group)) {
            Object sortedChilds[] = getSortedChildren(group);

            /* Only add if there are Childs */
            if (sortedChilds.length > 0) {

              /* Add the Group itself */
              if (withGroups)
                flatList.add(group);

              /* Store the actual number of news of the group too */
              if (group instanceof EntityGroup)
                ((EntityGroup) group).setSizeHint(sortedChilds.length);

              /* Add childs of group to the list */
              flatList.addAll(Arrays.asList(sortedChilds));
              newsCount += sortedChilds.length;
            }
          }

          /* Otherwise just add */
          else {
            if (withGroups)
              flatList.add(group);
          }
        }

        return fillPagingIfNecessary(flatList.toArray(), newsCount);
      }

      /* Grouping is not enabled, just return sorted Children */
      return fillPagingIfNecessary(getSortedChildren(input));
    }

    /* Structured ContentProvider */
    else if (input != null && getContentProvider() instanceof IStructuredContentProvider)
      return getSortedChildren(input);

    /* No Element to show */
    return new Object[0];
  }

  private Object[] fillPagingIfNecessary(Object[] elements) {
    return fillPagingIfNecessary(elements, elements.length);
  }

  private Object[] fillPagingIfNecessary(Object[] elements, int newsCount) {
    if (fPageSize == 0 || newsCount <= fPageSize)
      return elements;

    Object[] elementsWithPaging = new Object[elements.length + 1];
    System.arraycopy(elements, 0, elementsWithPaging, 0, elements.length);
    elementsWithPaging[elements.length] = new PageLatch();

    return elementsWithPaging;
  }

  /* Returns TRUE if the Input consists only of INews */
  private boolean isNews(Object input) {
    if (input instanceof Object[]) {
      Object elements[] = (Object[]) input;
      for (Object element : elements) {
        if (!(element instanceof INews))
          return false;
      }
    } else if (!(input instanceof INews))
      return false;

    return true;
  }

  /**
   * @param parentElement
   * @param childElements
   */
  public void add(Object parentElement, Object[] childElements) {
    Assert.isNotNull(parentElement);
    assertElementsNotNull(childElements);

    if (childElements.length > 0)
      refresh(); // TODO Optimize
  }

  /**
   * @param news
   */
  public void update(Collection<NewsEvent> news) {

    /*
     * The update-event could have been sent out a lot faster than the Browser
     * having a chance to react. In this case, rather then refreshing a possible
     * blank page (or wrong page), re-set the input.
     */
    String inputUrl = fServer.toUrl(fId, fInput);
    String browserUrl = fBrowser.getControl().getUrl();
    boolean resetInput = browserUrl.length() == 0 || URIUtils.ABOUT_BLANK.equals(browserUrl);
    if (inputUrl.equals(browserUrl)) {
      if (!internalUpdate(news))
        refresh(); // Refresh if dynamic update failed
    } else if (fServer.isDisplayOperation(inputUrl) && resetInput)
      fBrowser.setUrl(inputUrl);
  }

  /**
   * @param objects
   */
  public void remove(Object[] objects) {
    assertElementsNotNull(objects);

    /* Refresh if dynamic removal failed */
    if (!internalRemove(objects))
      refresh();
  }

  /**
   * @param element
   */
  public void remove(Object element) {
    Assert.isNotNull(element);

    /* Refresh if dynamic removal failed */
    if (!internalRemove(new Object[] { element }))
      refresh();
  }

  private boolean internalUpdate(Collection<NewsEvent> newsEvents) {
    boolean toggleJS = fBrowser.shouldDisableScript();
    try {
      if (toggleJS)
        fBrowser.setScriptDisabled(false);

      /* Update for each Event */
      for (NewsEvent newsEvent : newsEvents) {
        INews news = newsEvent.getEntity();
        if (!fViewModel.isNewsVisible(news) || news.getId() == null || !fViewModel.hasNews(news.getId()))
          continue; //Do not update if news not visible at all or not part of the browsers content

        StringBuilder js = new StringBuilder();

        /* State (Bold/Plain Title, Mark Read Tooltip) */
        if (CoreUtils.isStateChange(newsEvent)) {
          String markRead = Messages.NewsBrowserViewer_MARK_READ;
          String markUnread = Messages.NewsBrowserViewer_MARK_UNREAD;

          boolean isRead = (INews.State.READ == news.getState());
          js.append(getElementById(Dynamic.NEWS.getId(news)).append(isRead ? ".className='newsitemRead'; " : ".className='newsitemUnread'; ")); //$NON-NLS-1$ //$NON-NLS-2$
          js.append(getElementById(Dynamic.TITLE_LINK.getId(news)).append(isRead ? ".className='read'; " : ".className='unread'; ")); //$NON-NLS-1$ //$NON-NLS-2$
          js.append(getElementById(Dynamic.TOGGLE_READ_LINK.getId(news)).append(isRead ? ".title='" + markUnread + "'; " : ".title='" + markRead + "'; ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          js.append(getElementById(Dynamic.TOGGLE_READ_IMG.getId(news)).append(isRead ? ".alt='" + markUnread + "'; " : ".alt='" + markRead + "'; ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        /* Sticky (Title Background, Footer Background, Mark Sticky Image) */
        if (CoreUtils.isStickyStateChange(newsEvent)) {
          boolean isSticky = news.isFlagged();
          js.append(getElementById(Dynamic.HEADER.getId(news)).append(isSticky ? ".className='headerSticky'; " : ".className='header'; ")); //$NON-NLS-1$ //$NON-NLS-2$

          js.append("var footer = ").append(getElementById(Dynamic.FOOTER.getId(news))).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
          js.append("if (footer) {"); //$NON-NLS-1$
          js.append("  footer").append(isSticky ? ".className='footerSticky'; " : ".className='footer'; "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          js.append("}"); //$NON-NLS-1$

          String stickyImgUri;
          if (fBrowser.isIE())
            stickyImgUri = isSticky ? OwlUI.getImageUri("/icons/obj16/news_pinned_light.gif", "news_pinned_light.gif") : OwlUI.getImageUri("/icons/obj16/news_pin_light.gif", "news_pin_light.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          else
            stickyImgUri = isSticky ? ApplicationServer.getDefault().toResourceUrl("/icons/obj16/news_pinned_light.gif") : ApplicationServer.getDefault().toResourceUrl("/icons/obj16/news_pin_light.gif"); //$NON-NLS-1$ //$NON-NLS-2$

          js.append(getElementById(Dynamic.TOGGLE_STICKY_IMG.getId(news)).append(".src='").append(stickyImgUri).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /* Label (Title Foreground, Label List) */
        if (CoreUtils.isLabelChange(newsEvent)) {
          Set<ILabel> labels = CoreUtils.getSortedLabels(news);
          String defaultColor = (CoreUtils.getLink(news) != null && !isHeadlinesLayout()) ? "#009" : "rgb(0,0,0)"; //$NON-NLS-1$ //$NON-NLS-2$
          String color = (labels.isEmpty()) ? defaultColor : "rgb(" + OwlUI.toString(OwlUI.getRGB(labels.iterator().next())) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          if ("rgb(0,0,0)".equals(color)) //Don't let black override link color //$NON-NLS-1$
            color = defaultColor;
          js.append(getElementById(Dynamic.TITLE_LINK.getId(news)).append(".style.color='").append(color).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$

          /* Remove Labels */
          if (labels.isEmpty()) {
            js.append(getElementById(Dynamic.LABELS_SEPARATOR.getId(news)).append(".style.display='none'; ")); //$NON-NLS-1$
            js.append(getElementById(Dynamic.LABELS.getId(news)).append(".innerHTML=''; ")); //$NON-NLS-1$
          }

          /* Show Labels */
          else {
            js.append(getElementById(Dynamic.LABELS_SEPARATOR.getId(news)).append(".style.display='inline'; ")); //$NON-NLS-1$

            StringBuilder labelsHtml = new StringBuilder(Messages.NewsBrowserViewer_LABELS);
            labelsHtml.append(" "); //$NON-NLS-1$
            int c = 0;
            for (ILabel label : labels) {
              c++;
              if (c < labels.size())
                span(labelsHtml, StringUtils.htmlEscape(label.getName()) + ", ", label.getColor()); //$NON-NLS-1$
              else
                span(labelsHtml, StringUtils.htmlEscape(label.getName()), label.getColor());
            }

            js.append(getElementById(Dynamic.LABELS.getId(news)).append(".innerHTML='").append(escapeForInnerHtml(labelsHtml.toString())).append("'; ")); //$NON-NLS-1$ //$NON-NLS-2$
          }

          /* Make sure to also update collapsed subtitles if present */
          if (isHeadlinesLayout() && !fViewModel.isNewsExpanded(news)) {
            StringBuilder subtitleContent = new StringBuilder();
            IBaseLabelProvider lp = getLabelProvider();
            if (lp instanceof NewsBrowserLabelProvider)
              ((NewsBrowserLabelProvider) lp).fillSubtitle(subtitleContent, news, labels, false);
            if (subtitleContent.length() > 0)
              js.append(getElementById(Dynamic.SUBTITLE_LINK.getId(news))).append(".innerHTML='").append(escapeForInnerHtml(subtitleContent.toString())).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }

        /* Execute */
        if (js.length() > 0) {
          boolean res = fBrowser.execute(js.toString(), false, "internalUpdate"); //$NON-NLS-1$
          if (!res)
            return false;
        }
      }
    } finally {
      if (toggleJS)
        fBrowser.setScriptDisabled(true);
    }

    return true;
  }

  private void span(StringBuilder builder, String content, String color) {
    builder.append("<span style=\"color: rgb(").append(color).append(");\""); //$NON-NLS-1$ //$NON-NLS-2$
    builder.append(">").append(content).append("</span>"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private StringBuilder getElementById(String id) {
    return new StringBuilder("document.getElementById('" + id + "')"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private boolean internalRemove(Object[] elements) {
    StringBuilder js = new StringBuilder();
    js.append("var body = ").append(getElementById(BODY_ELEMENT_ID)).append("; "); //$NON-NLS-1$ //$NON-NLS-2$

    boolean varDefined = false;
    Set<Long> groupsToUpdate = new HashSet<Long>();
    for (Object element : elements) {
      if (element instanceof INews) {
        INews news = (INews) element;
        if (news.getId() == null || !fViewModel.hasNews(news.getId()))
          continue; //Do not update if news not part of the browsers content at all

        /* Remove from View Model */
        long groupToUpdate = fViewModel.removeNews(news);
        if (groupToUpdate != -1)
          groupsToUpdate.add(groupToUpdate);

        /* Remove News from DOM */
        if (!varDefined) {
          js.append("var "); //$NON-NLS-1$
          varDefined = true;
        }

        js.append("node = ").append(getElementById(Dynamic.NEWS.getId(news))).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        js.append("if (node && node.parentNode == body) { "); //$NON-NLS-1$
        js.append("  body.removeChild(node); "); //$NON-NLS-1$
        js.append("} else if (node) { "); //$NON-NLS-1$
        js.append("  node.className='hidden';"); //$NON-NLS-1$
        js.append("} "); //$NON-NLS-1$

        /* Hide Separator if using headlines layout */
        if (isHeadlinesLayout()) {
          js.append("node = ").append(getElementById(Dynamic.HEADLINE_SEPARATOR.getId(news))).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
          js.append("if (node && node.parentNode == body) { "); //$NON-NLS-1$
          js.append("  body.removeChild(node); "); //$NON-NLS-1$
          js.append("} else if (node) {"); //$NON-NLS-1$
          js.append("  node.className='hidden';"); //$NON-NLS-1$
          js.append("} "); //$NON-NLS-1$
        }
      }
    }

    /* Update Groups */
    IBaseLabelProvider labelProvider = getLabelProvider();
    for (Long groupId : groupsToUpdate) {

      /* Remove Groups from DOM or update it */
      if (!varDefined) {
        js.append("var "); //$NON-NLS-1$
        varDefined = true;
      }

      /* Group is empty now: Remove it from DOM */
      if (!fViewModel.hasGroup(groupId)) {
        js.append("node = ").append(getElementById(Dynamic.GROUP.getId(groupId))).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        js.append("if (node && node.parentNode == body) { "); //$NON-NLS-1$
        js.append("  body.removeChild(node); "); //$NON-NLS-1$
        js.append("} else if (node) { "); //$NON-NLS-1$
        js.append("  node.className='hidden';"); //$NON-NLS-1$
        js.append("} "); //$NON-NLS-1$
      }

      /* Group has a new Element count: Update it */
      else if (labelProvider instanceof NewsBrowserLabelProvider) {
        int count = fViewModel.getGroupSize(groupId);
        NewsBrowserLabelProvider browserLabelProvider = (NewsBrowserLabelProvider) labelProvider;
        String groupNote = browserLabelProvider.getGroupNote(count, count);

        js.append("node = ").append(getElementById(Dynamic.GROUP_NOTE.getId(groupId))).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        js.append("if (node) { "); //$NON-NLS-1$
        js.append("  node.innerHTML = '").append(escapeForInnerHtml(groupNote)).append("'; "); //$NON-NLS-1$ //$NON-NLS-2$
        js.append("} "); //$NON-NLS-1$
      }
    }

    /* Update Latch as necessary */
    updateLatchIfNecessary(js);

    return fBrowser.execute(js.toString(), "internalRemove"); //$NON-NLS-1$
  }

  private void assertElementsNotNull(Object[] elements) {
    Assert.isNotNull(elements);
    for (Object element : elements) {
      Assert.isNotNull(element);
    }
  }

  /**
   * @return Returns a List of Strings that should get highlighted per News that
   * is displayed.
   */
  protected Collection<String> getHighlightedWords() {
    if (getContentProvider() instanceof NewsContentProvider && fPreferences.getBoolean(DefaultPreferences.FV_HIGHLIGHT_SEARCH_RESULTS)) {
      INewsMark mark = ((NewsContentProvider) getContentProvider()).getInput();
      Set<String> extractedWords;

      /* Extract from Conditions if any */
      if (mark instanceof ISearch) {
        List<ISearchCondition> conditions = ((ISearch) mark).getSearchConditions();
        extractedWords = CoreUtils.extractWords(conditions);
      } else
        extractedWords = new HashSet<String>(1);

      /* Fill Pattern if set */
      if (fNewsFilter != null && StringUtils.isSet(fNewsFilter.getPatternString())) {
        String pattern = fNewsFilter.getPatternString();

        /* News Filter sometimes converts to wildcard query */
        if (StringUtils.supportsTrailingWildcards(pattern))
          pattern = pattern + "*"; //$NON-NLS-1$

        /* Extract Words */
        extractedWords.addAll(CoreUtils.extractWords(pattern));
      }

      return extractedWords;
    }

    return Collections.emptyList();
  }

  NewsBrowserViewModel getViewModel() {
    return fViewModel;
  }

  private boolean isGroupingEnabled() {
    IContentProvider cp = getContentProvider();
    if (cp instanceof NewsContentProvider)
      return ((NewsContentProvider) cp).isGroupingEnabled();

    return false;
  }

  private boolean isGroupingByState() {
    IContentProvider cp = getContentProvider();
    if (cp instanceof NewsContentProvider)
      return ((NewsContentProvider) cp).isGroupingByState();

    return false;
  }

  private boolean isHeadlinesLayout() {
    IBaseLabelProvider lp = getLabelProvider();
    if (lp instanceof NewsBrowserLabelProvider)
      return ((NewsBrowserLabelProvider) lp).isHeadlinesOnly();

    return false;
  }

  INews resolve(long newsId) {
    INews news = null;

    /* First Ask ContentProvider Cache */
    if (getContentProvider() instanceof NewsContentProvider)
      news = ((NewsContentProvider) getContentProvider()).obtainFromCache(newsId);

    /* Then fallback to resolve from DB */
    if (news == null)
      return fNewsDao.load(newsId);

    return news;
  }
}
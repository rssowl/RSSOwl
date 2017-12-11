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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IInputProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IPreferenceDAO;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.reference.ModelReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.ITreeNode;
import org.rssowl.core.util.ModelTreeNode;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.TreeTraversal;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.ContextMenuCreator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.ShareProvider;
import org.rssowl.ui.internal.StatusLineUpdater;
import org.rssowl.ui.internal.actions.DeleteTypesAction;
import org.rssowl.ui.internal.actions.EntityPropertyDialogAction;
import org.rssowl.ui.internal.actions.FindAction;
import org.rssowl.ui.internal.actions.NewBookMarkAction;
import org.rssowl.ui.internal.actions.NewFolderAction;
import org.rssowl.ui.internal.actions.NewNewsBinAction;
import org.rssowl.ui.internal.actions.NewSearchMarkAction;
import org.rssowl.ui.internal.actions.OpenAction;
import org.rssowl.ui.internal.actions.OpenInBrowserAction;
import org.rssowl.ui.internal.actions.OpenInNewTabAction;
import org.rssowl.ui.internal.actions.ReloadTypesAction;
import org.rssowl.ui.internal.actions.RetargetActions;
import org.rssowl.ui.internal.actions.SearchInTypeAction;
import org.rssowl.ui.internal.actions.SendLinkAction;
import org.rssowl.ui.internal.dialogs.ManageSetsDialog;
import org.rssowl.ui.internal.dialogs.preferences.SharingPreferencesPage;
import org.rssowl.ui.internal.editors.feed.FeedViewInput;
import org.rssowl.ui.internal.editors.feed.PerformAfterInputSet;
import org.rssowl.ui.internal.undo.UndoStack;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.WidgetTreeNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * TODO The underlying TreeViewer is not yet supporting contributed
 * Label-Decorator, because the used LabelProvider is not extending
 * ILabelProvider. Add this code to provide the decorations from contributions:
 * <code>
 * ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
 * fViewer.setLabelProvider(new DecoratingLabelProvider(new BookMarkLabelProvider(), decorator);
 * </code>
 * </p>
 *
 * @author bpasero
 */
public class BookMarkExplorer extends ViewPart {

  /** ID of this View */
  public static final String VIEW_ID = "org.rssowl.ui.BookMarkExplorer"; //$NON-NLS-1$

  /** IDs of Action: Next Bookmark-Set */
  public static final String NEXT_SET_ACTION = "org.rssowl.ui.internal.views.explorer.NextSetAction"; //$NON-NLS-1$

  /** IDs of Action: Previous Bookmark-Set */
  public static final String PREVIOUS_SET_ACTION = "org.rssowl.ui.internal.views.explorer.PreviousSetAction"; //$NON-NLS-1$

  /* Local Setting Constants */
  private static final String PREF_SELECTED_FOLDER_CHILD = "org.rssowl.ui.internal.views.explorer.SelectedFolderChild"; //$NON-NLS-1$
  private static final String PREF_SELECTED_BOOKMARK_SET = "org.rssowl.ui.internal.views.explorer.SelectedBookMarkSet"; //$NON-NLS-1$
  private static final String PREF_EXPANDED_NODES = "org.rssowl.ui.internal.views.explorer.ExpandedNodes"; //$NON-NLS-1$

  /* Local Actions */
  private static final String GROUP_ACTION = "org.rssowl.ui.internal.views.explorer.GroupAction"; //$NON-NLS-1$
  private static final String FILTER_ACTION = "org.rssowl.ui.internal.views.explorer.FilterAction"; //$NON-NLS-1$

  /* Settings */
  private IPreferenceScope fGlobalPreferences;
  private List<Long> fExpandedNodes;
  private boolean fBeginSearchOnTyping;
  private boolean fAlwaysShowSearch;
  private boolean fSortByName;
  private BookMarkFilter.Type fFilterType;
  private BookMarkGrouping.Type fGroupingType;
  private IFolder fSelectedBookMarkSet;
  private boolean fLinkingEnabled;
  private boolean fFaviconsEnabled;
  private long fLastSelectedFolderChild;

  /* Viewer Classes */
  private TreeViewer fViewer;
  private BookMarkContentProvider fContentProvider;
  private BookMarkLabelProvider fLabelProvider;
  private BookMarkSorter fBookMarkComparator;
  private BookMarkFilter fBookMarkFilter;
  private BookMarkGrouping fBookMarkGrouping;

  /* Widgets */
  private Label fSeparator;
  private Composite fSearchBarContainer;
  private BookMarkSearchbar fSearchBar;
  private IToolBarManager fToolBarManager;

  /* BookMark Sets */
  private Set<IFolder> fRootFolders;

  /* Misc. */
  private IViewSite fViewSite;
  private IStructuredSelection fLastSelection;
  private FolderListener fFolderListener;
  private IPartListener2 fPartListener;
  private IPreferenceDAO fPrefDAO;
  private IPropertyChangeListener fPropertyChangeListener;
  private boolean fBlockSaveState;
  private BookMarkFilter.Type fLastFilterType;
  private BookMarkGrouping.Type fLastGroupType;

  /**
   * Returns the preferences key for the selected bookmark set for the given
   * workbench window.
   *
   * @param window the active workbench window.
   * @return the preferences key for the selected bookmark set for the given
   * workbench window.
   */
  public static String getSelectedBookMarkSetPref(IWorkbenchWindow window) {
    int windowIndex = OwlUI.getWindowIndex(window);
    return PREF_SELECTED_BOOKMARK_SET + windowIndex;
  }

  /*
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {

    /* Update Parent */
    GridLayout layout = new GridLayout(1, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.verticalSpacing = 0;

    parent.setLayout(layout);
    parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    /* Create TreeViewer */
    createViewer(parent);

    /* Restore Expanded Elements */
    restoreExpandedElements();

    /* Restore Selection if linking is disabled */
    if (fLastSelectedFolderChild > 0 && !fLinkingEnabled)
      fViewer.setSelection(new StructuredSelection(new BookMarkReference(fLastSelectedFolderChild)), true);

    /* Hook into Statusline */
    fViewer.addSelectionChangedListener(new StatusLineUpdater(getViewSite().getActionBars().getStatusLineManager()));

    /* Hook into Global Actions */
    hookGlobalActions();

    /* Hook contextual Menu */
    hookContextualMenu();

    /* Hook into Toolbar */
    hookToolBar();

    /* Hook into View Dropdown */
    hookViewMenu();

    /* Register Listeners */
    registerListeners();

    /* Propagate Selection Events */
    fViewSite.setSelectionProvider(fViewer);

    /* Create the Search Bar */
    createSearchBar(parent);

    /* Show Busy when reload occurs */
    IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) fViewSite.getAdapter(IWorkbenchSiteProgressService.class);
    service.showBusyForFamily(Controller.getDefault().getReloadFamily());
  }

  private void createViewer(Composite parent) {

    /* TreeViewer */
    fViewer = new BookMarkViewer(this, parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    fViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fViewer.getControl().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());
    fViewer.getControl().setFont(OwlUI.getThemeFont(OwlUI.BKMRK_EXPLORER_FONT_ID, SWT.NORMAL));

    /* Setup Drag & Drop Support */
    initDragAndDrop();

    /* Add a custom Comparer */
    fViewer.setComparer(getComparer());
    fViewer.setUseHashlookup(true);

    /* Create ContentProvider */
    fContentProvider = new BookMarkContentProvider();
    fViewer.setContentProvider(fContentProvider);

    /* Create LabelProvider */
    fLabelProvider = new BookMarkLabelProvider();
    fLabelProvider.setUseFavicons(fFaviconsEnabled);
    fViewer.setLabelProvider(fLabelProvider);

    /* Apply Sorter */
    fBookMarkComparator = new BookMarkSorter();
    if (fSortByName)
      fBookMarkComparator.setType(BookMarkSorter.Type.SORT_BY_NAME);
    fViewer.setComparator(fBookMarkComparator);

    /* Apply Filter */
    fBookMarkFilter = new BookMarkFilter();
    fBookMarkFilter.setType(fFilterType);
    fViewer.addFilter(fBookMarkFilter);

    /* Create Grouper */
    fBookMarkGrouping = new BookMarkGrouping();
    fBookMarkGrouping.setType(fGroupingType);

    /* Let the ContentProvider know */
    fContentProvider.setBookmarkFilter(fBookMarkFilter);
    fContentProvider.setBookmarkGrouping(fBookMarkGrouping);

    /* Set the initial Input based on selected Bookmark Set */
    fViewer.setInput(fSelectedBookMarkSet);

    /* Enable "Link to FeedView" */
    fViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        onSelectionChanged(event);
      }
    });

    /* Hook Open Support */
    fViewer.addOpenListener(new IOpenListener() {
      @Override
      public void open(OpenEvent event) {
        OwlUI.openInFeedView(fViewSite.getPage(), (IStructuredSelection) fViewer.getSelection());
      }
    });

    /* Custom Owner Drawn for Groups */
    if (!OwlUI.isHighContrast()) {
      fViewer.getControl().addListener(SWT.EraseItem, new Listener() {
        @Override
        public void handleEvent(Event event) {
          Object element = event.item.getData();
          fLabelProvider.erase(event, element);
        }
      });
    }

    /* Update List of Expanded Nodes */
    fViewer.addTreeListener(new ITreeViewerListener() {
      @Override
      public void treeExpanded(TreeExpansionEvent event) {
        onTreeEvent(event.getElement(), true);
      }

      @Override
      public void treeCollapsed(TreeExpansionEvent event) {
        onTreeEvent(event.getElement(), false);
      }
    });

    /* Link if enabled */
    if (fLinkingEnabled) {
      IWorkbenchPart activePart = fViewSite.getPage().getActivePart();
      if (activePart instanceof IEditorPart)
        editorActivated((IEditorPart) activePart);
    }
  }

  private void initDragAndDrop() {
    int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
    Transfer[] dragTransfers = new Transfer[] { LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance(), URLTransfer.getInstance() };
    Transfer[] dropTransfers = new Transfer[] { LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance(), URLTransfer.getInstance(), FileTransfer.getInstance() };
    BookMarkDNDImpl bookmarkDND = new BookMarkDNDImpl(this, fViewer);

    /* Drag Support */
    fViewer.addDragSupport(ops, dragTransfers, bookmarkDND);

    /* Drop Support */
    fViewer.addDropSupport(ops, dropTransfers, bookmarkDND);
  }

  private void onSelectionChanged(SelectionChangedEvent event) {
    fLastSelection = (IStructuredSelection) event.getSelection();
    if (fLinkingEnabled)
      linkToFeedView(fLastSelection);
  }

  private void onTreeEvent(Object element, boolean expanded) {

    /* Element expanded - add to List of expanded Nodes */
    if (expanded) {
      if (element instanceof IFolder)
        fExpandedNodes.add(((IFolder) element).getId());
      else if (element instanceof EntityGroup)
        fExpandedNodes.add(((EntityGroup) element).getId());
    }

    /* Element collapsed - remove from List of expanded Nodes */
    else {
      if (element instanceof IFolder)
        fExpandedNodes.remove(((IFolder) element).getId());
      else if (element instanceof EntityGroup)
        fExpandedNodes.remove(((EntityGroup) element).getId());
    }
  }

  /*
   * This Comparer is used to optimize some operations on the Viewer being used.
   * When deleting Entities, the Delete-Event is providing a reference to the
   * deleted Entity, which can not be resolved anymore. This Comparer will
   * return <code>TRUE</code> for a reference compared with an Entity that has
   * the same ID and is belonging to the same Entity. At any time, it _must_ be
   * avoided to call add, update or refresh with passing in a Reference!
   */
  private IElementComparer getComparer() {
    return new IElementComparer() {
      @Override
      public boolean equals(Object a, Object b) {

        /* Quickyly check this common case */
        if (a == b && a != null)
          return true;

        /* Specially handle this reference */
        if (a instanceof FeedLinkReference || b instanceof FeedLinkReference) {
          FeedLinkReference ref1 = null;
          FeedLinkReference ref2 = null;

          if (a instanceof IBookMark)
            ref1 = ((IBookMark) a).getFeedLinkReference();
          else if (a instanceof FeedLinkReference)
            ref1 = ((FeedLinkReference) a);

          if (b instanceof IBookMark)
            ref2 = ((IBookMark) b).getFeedLinkReference();
          else if (b instanceof FeedLinkReference)
            ref2 = ((FeedLinkReference) b);

          if (ref1 != null)
            return ref1.equals(ref2);

          return false;
        }

        /* Handle Non Feed-Link-Reference */
        long id1 = 0;
        long id2 = 0;

        if (a instanceof IEntity)
          id1 = ((IEntity) a).getId();
        else if (a instanceof ModelReference)
          id1 = ((ModelReference) a).getId();
        else if (a instanceof EntityGroup)
          id1 = ((EntityGroup) a).getId();

        if (b instanceof IEntity)
          id2 = ((IEntity) b).getId();
        else if (b instanceof ModelReference)
          id2 = ((ModelReference) b).getId();
        else if (b instanceof EntityGroup)
          id2 = ((EntityGroup) b).getId();

        return id1 == id2;
      }

      @Override
      public int hashCode(Object element) {
        return element.hashCode();
      }
    };
  }

  private void linkToFeedView(IStructuredSelection selection) {

    /* Only Link if this is the active Part */
    if (this != fViewSite.getPage().getActivePart())
      return;

    /* Only Link for Single Selections */
    if (selection.size() == 1) {
      Object element = selection.getFirstElement();

      /* Find the Editor showing given Selection */
      IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      IEditorReference[] editorReferences = activePage.getEditorReferences();
      IEditorReference reference = EditorUtils.findEditor(editorReferences, element);
      if (reference != null)
        activePage.bringToTop(reference.getPart(true));
    }
  }

  private void editorActivated(IEditorPart part) {
    if (!fLinkingEnabled || part == null)
      return;

    /* Try to select and reveal editor input in the Explorer */
    IEditorInput editorInput = part.getEditorInput();
    if (editorInput instanceof FeedViewInput)
      reveal(((FeedViewInput) editorInput).getMark(), false);
  }

  /**
   * @return <code>true</code> if the explorer is linked to the feed view and
   * <code>false</code> otherwise.
   */
  public boolean isLinkingEnabled() {
    return fLinkingEnabled;
  }

  /**
   * @param element the folder child to make visible and select.
   * @param expand if <code>true</code> expand the element, <code>false</code>
   * otherwise.
   */
  public void reveal(IFolderChild element, boolean expand) {

    /* Return early if hidden */
    if (!fViewSite.getPage().isPartVisible(this))
      return;

    /* Change Set if required */
    IFolderChild child = element;
    while (child.getParent() != null)
      child = child.getParent();

    if (!fSelectedBookMarkSet.equals(child))
      changeSet((IFolder) child);

    /* Set Selection */
    fViewer.setSelection(new StructuredSelection(element), true);

    /* Expand if Set */
    if (expand) {
      fViewer.setExpandedState(element, true);
      fExpandedNodes.add(element.getId());
    }
  }

  void changeSet(IFolder folder) {

    /* Save Expanded Elements */
    saveExpandedElements();

    /* Set new Input */
    fSelectedBookMarkSet = folder;
    fViewer.setInput(fSelectedBookMarkSet);

    /* Restore Expanded Elements */
    fExpandedNodes.clear();
    loadExpandedElements();
    fViewer.getControl().setRedraw(false);
    try {
      restoreExpandedElements();
    } finally {
      fViewer.getControl().setRedraw(true);
    }

    /* Update Set Actions */
    fViewSite.getActionBars().getToolBarManager().find(PREVIOUS_SET_ACTION).update(IAction.ENABLED);
    fViewSite.getActionBars().getToolBarManager().find(NEXT_SET_ACTION).update(IAction.ENABLED);

    /* Save the new selected Set in Preferences */
    IPreference pref = fPrefDAO.loadOrCreate(getSelectedBookMarkSetPref(fViewSite.getWorkbenchWindow()));
    pref.putLongs(fSelectedBookMarkSet.getId());
    fPrefDAO.save(pref);
  }

  private void createSearchBar(final Composite parent) {

    /* Add Separator between Tree and Search Bar */
    fSeparator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    fSeparator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Container for the SearchBar */
    fSearchBarContainer = new Composite(parent, SWT.NONE);
    fSearchBarContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Hide Searchbar in case settings tell so */
    ((GridData) fSeparator.getLayoutData()).exclude = !fAlwaysShowSearch;
    ((GridData) fSearchBarContainer.getLayoutData()).exclude = !fAlwaysShowSearch;

    /* Apply Layout */
    GridLayout searchBarLayout = new GridLayout(1, false);
    searchBarLayout.marginHeight = 2;
    searchBarLayout.marginWidth = 2;
    fSearchBarContainer.setLayout(searchBarLayout);

    /* Create the SearchBar */
    fSearchBar = new BookMarkSearchbar(fViewSite, fSearchBarContainer, fViewer, fBookMarkFilter);

    /* Show the SearchBar on Printable Key pressed */
    fViewer.getControl().addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {

        /* Feature not Used, return */
        if (!fBeginSearchOnTyping)
          return;

        /* Transfer typed key into SearchBar */
        if (e.character > 0x20 && e.character != SWT.DEL) {
          setSearchBarVisible(true);

          fSearchBar.getControl().append(String.valueOf(e.character));
          fSearchBar.getControl().setFocus();

          /* Consume the Event */
          e.doit = false;
        }

        /* Reset any Filter if set */
        else if (e.keyCode == SWT.ESC && fSearchBar.getControl().getText().length() != 0) {
          fSearchBar.setFilterText(""); //$NON-NLS-1$
          setSearchBarVisible(fAlwaysShowSearch);
          setFocus();
        }
      }
    });

    /* Hide SearchBar if search is done */
    fSearchBar.getControl().addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {

        /* Feature not Used, return */
        if (fAlwaysShowSearch)
          return;

        /* Search is Done */
        if (fSearchBar.getControl().getText().length() == 0) {
          setSearchBarVisible(false);
          setFocus();
        }
      }
    });
  }

  void setSearchBarVisible(boolean visible) {

    /* Return if no State Change */
    if (visible != ((GridData) fSeparator.getLayoutData()).exclude)
      return;

    /* Update LayoutData and layout Parent */
    ((GridData) fSeparator.getLayoutData()).exclude = !visible;
    ((GridData) fSearchBarContainer.getLayoutData()).exclude = !visible;
    fSearchBarContainer.getParent().layout();
  }

  private void hookViewMenu() {
    IMenuManager menuManager = fViewSite.getActionBars().getMenuManager();
    menuManager.setRemoveAllWhenShown(true);
    menuManager.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager manager) {

        /* Manage Bookmark Sets */
        IAction manageSets = new Action(Messages.BookMarkExplorer_MANAGE_SETS) {
          @Override
          public void run() {
            ManageSetsDialog instance = ManageSetsDialog.getVisibleInstance();
            if (instance == null) {
              ManageSetsDialog dialog = new ManageSetsDialog(fViewSite.getShell(), fSelectedBookMarkSet);
              dialog.open();
            } else {
              instance.getShell().forceActive();
            }
          }
        };
        manager.add(manageSets);

        /* Available Bookmark Sets */
        manager.add(new Separator());
        for (final IFolder rootFolder : fRootFolders) {
          IAction selectBookMarkSet = new Action(rootFolder.getName(), IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
              if (!fSelectedBookMarkSet.equals(rootFolder) && isChecked())
                changeSet(rootFolder);
            }
          };
          selectBookMarkSet.setImageDescriptor(OwlUI.BOOKMARK_SET);

          if (fSelectedBookMarkSet.equals(rootFolder))
            selectBookMarkSet.setChecked(true);

          manager.add(selectBookMarkSet);
        }

        /* Search Bar */
        manager.add(new Separator());
        MenuManager searchMenu = new MenuManager(Messages.BookMarkExplorer_FIND);
        manager.add(searchMenu);

        /* Search Bar - Always Show Bar */
        IAction alwaysShow = new Action(Messages.BookMarkExplorer_ALWAYS_SHOW, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            fAlwaysShowSearch = !fAlwaysShowSearch;

            /* Only Update if the Filter is not Active */
            if (fSearchBar.getControl().getText().length() == 0)
              setSearchBarVisible(fAlwaysShowSearch);
          }
        };
        alwaysShow.setChecked(fAlwaysShowSearch);
        searchMenu.add(alwaysShow);

        /* Search Bar - Begin Search when Typing */
        IAction beginWhenTyping = new Action(Messages.BookMarkExplorer_BEGIN_WHEN_TYPING, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            fBeginSearchOnTyping = !fBeginSearchOnTyping;
          }
        };
        beginWhenTyping.setChecked(fBeginSearchOnTyping);
        searchMenu.add(beginWhenTyping);

        /* Misc. Settings */
        manager.add(new Separator());
        IAction sortByName = new Action(Messages.BookMarkExplorer_SORT_BY_NAME, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            fSortByName = !fSortByName;
            if (fSortByName)
              fBookMarkComparator.setType(BookMarkSorter.Type.SORT_BY_NAME);
            else
              fBookMarkComparator.setType(BookMarkSorter.Type.DEFAULT_SORTING);
            fViewer.refresh(false);

            /* Save directly to global scope */
            fGlobalPreferences.putBoolean(DefaultPreferences.BE_SORT_BY_NAME, fSortByName);
          }
        };
        sortByName.setChecked(fSortByName);
        manager.add(sortByName);

        IAction showFavicons = new Action(Messages.BookMarkExplorer_SHOW_FAVICONS, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            fFaviconsEnabled = isChecked();

            fLabelProvider.setUseFavicons(fFaviconsEnabled);
            fViewer.getTree().setRedraw(false);
            try {
              fViewer.refresh(true);
            } finally {
              fViewer.getTree().setRedraw(true);
            }
          }
        };
        showFavicons.setChecked(fFaviconsEnabled);
        manager.add(showFavicons);

        /* Allow Contributions */
        manager.add(new Separator());

        IAction linkFeedView = new Action(Messages.BookMarkExplorer_LINKING, IAction.AS_CHECK_BOX) {
          @Override
          public void run() {
            fLinkingEnabled = isChecked();

            /* Link if enabled */
            if (fLinkingEnabled) {
              IEditorPart editor = fViewSite.getPage().getActiveEditor();
              if (editor != null)
                editorActivated(editor);
            }
          }
        };
        linkFeedView.setChecked(fLinkingEnabled);
        linkFeedView.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/synced.gif")); //$NON-NLS-1$
        manager.add(linkFeedView);

        /* Allow Contributions */
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });

    /* Dummy Entry to show Menu in View */
    menuManager.add(new Action("") {}); //$NON-NLS-1$
  }

  private void hookToolBar() {
    fToolBarManager = fViewSite.getActionBars().getToolBarManager();

    /* BookMark Filter */
    final IAction bookmarkFilter = new Action(Messages.BookMarkExplorer_FILTER_ELEMENTS, IAction.AS_DROP_DOWN_MENU) {
      @Override
      public void run() {

        /* Restore Default */
        if (fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_ALL)
          doFilter(BookMarkFilter.Type.SHOW_ALL);

        /* Toggle to Previous */
        else if (fLastFilterType != null)
          doFilter(fLastFilterType);

        /* Show Menu */
        else if (fToolBarManager instanceof ToolBarManager)
          OwlUI.positionDropDownMenu(this, (ToolBarManager) fToolBarManager);
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        if (fBookMarkFilter.getType() == BookMarkFilter.Type.SHOW_ALL)
          return OwlUI.FILTER;

        return OwlUI.getImageDescriptor("icons/etool16/filter_active.gif"); //$NON-NLS-1$
      }
    };
    bookmarkFilter.setId(FILTER_ACTION);

    bookmarkFilter.setMenuCreator(new ContextMenuCreator() {

      @Override
      public Menu createMenu(Control parent) {
        Menu menu = new Menu(parent);

        /* Filter: None */
        final MenuItem showAll = new MenuItem(menu, SWT.RADIO);
        showAll.setText(Messages.BookMarkExplorer_SHOW_ALL);
        showAll.setSelection(BookMarkFilter.Type.SHOW_ALL == fBookMarkFilter.getType());
        menu.setDefaultItem(showAll);
        showAll.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showAll.getSelection() && fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_ALL)
              doFilter(BookMarkFilter.Type.SHOW_ALL);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Filter: New */
        final MenuItem showNew = new MenuItem(menu, SWT.RADIO);
        showNew.setText(Messages.BookMarkExplorer_SHOW_NEW);
        showNew.setSelection(BookMarkFilter.Type.SHOW_NEW == fBookMarkFilter.getType());
        showNew.addSelectionListener(new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showNew.getSelection() && fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_NEW)
              doFilter(BookMarkFilter.Type.SHOW_NEW);
          }
        });

        /* Filter: Unread */
        final MenuItem showUnread = new MenuItem(menu, SWT.RADIO);
        showUnread.setText(Messages.BookMarkExplorer_SHOW_UNREAD);
        showUnread.setSelection(BookMarkFilter.Type.SHOW_UNREAD == fBookMarkFilter.getType());
        showUnread.addSelectionListener(new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showUnread.getSelection() && fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_UNREAD)
              doFilter(BookMarkFilter.Type.SHOW_UNREAD);
          }
        });

        /* Filter: Sticky */
        final MenuItem showSticky = new MenuItem(menu, SWT.RADIO);
        showSticky.setText(Messages.BookMarkExplorer_SHOW_STICKY);
        showSticky.setSelection(BookMarkFilter.Type.SHOW_STICKY == fBookMarkFilter.getType());
        showSticky.addSelectionListener(new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showSticky.getSelection() && fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_STICKY)
              doFilter(BookMarkFilter.Type.SHOW_STICKY);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Filter: Erroneous */
        final MenuItem showErroneous = new MenuItem(menu, SWT.RADIO);
        showErroneous.setText(Messages.BookMarkExplorer_SHOW_ERROR);
        showErroneous.setSelection(BookMarkFilter.Type.SHOW_ERRONEOUS == fBookMarkFilter.getType());
        showErroneous.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showErroneous.getSelection() && fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_ERRONEOUS)
              doFilter(BookMarkFilter.Type.SHOW_ERRONEOUS);
          }
        });

        /* Filter: Never Visited */
        final MenuItem showNeverVisited = new MenuItem(menu, SWT.RADIO);
        showNeverVisited.setText(Messages.BookMarkExplorer_SHOW_NEVER_VISITED);
        showNeverVisited.setSelection(BookMarkFilter.Type.SHOW_NEVER_VISITED == fBookMarkFilter.getType());
        showNeverVisited.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (showNeverVisited.getSelection() && fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_NEVER_VISITED)
              doFilter(BookMarkFilter.Type.SHOW_NEVER_VISITED);
          }
        });

        return menu;
      }
    });

    fToolBarManager.add(bookmarkFilter);

    /* Bookmark Group */
    fToolBarManager.add(new Separator());
    final IAction bookmarkGroup = new Action(Messages.BookMarkExplorer_GROUP_ELEMENTS, IAction.AS_DROP_DOWN_MENU) {
      @Override
      public void run() {

        /* Restore Default */
        if (fBookMarkGrouping.getType() != BookMarkGrouping.Type.NO_GROUPING)
          doGrouping(BookMarkGrouping.Type.NO_GROUPING);

        /* Toggle to previous */
        else if (fLastGroupType != null)
          doGrouping(fLastGroupType);

        /* Show Menu */
        else if (fToolBarManager instanceof ToolBarManager)
          OwlUI.positionDropDownMenu(this, (ToolBarManager) fToolBarManager);
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        if (fBookMarkGrouping.getType() == BookMarkGrouping.Type.NO_GROUPING)
          return OwlUI.getImageDescriptor("icons/etool16/group.gif"); //$NON-NLS-1$

        return OwlUI.getImageDescriptor("icons/etool16/group_active.gif"); //$NON-NLS-1$
      }
    };
    bookmarkGroup.setId(GROUP_ACTION);

    bookmarkGroup.setMenuCreator(new ContextMenuCreator() {

      @Override
      public Menu createMenu(Control parent) {
        Menu menu = new Menu(parent);

        /* Group: None */
        final MenuItem noGrouping = new MenuItem(menu, SWT.RADIO);
        noGrouping.setText(Messages.BookMarkExplorer_NO_GROUPING);
        noGrouping.setSelection(BookMarkGrouping.Type.NO_GROUPING == fBookMarkGrouping.getType());
        menu.setDefaultItem(noGrouping);
        noGrouping.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (noGrouping.getSelection() && fBookMarkGrouping.getType() != BookMarkGrouping.Type.NO_GROUPING)
              doGrouping(BookMarkGrouping.Type.NO_GROUPING);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Group: By Type */
        final MenuItem groupByType = new MenuItem(menu, SWT.RADIO);
        groupByType.setText(Messages.BookMarkExplorer_GROUP_BY_TYPE);
        groupByType.setSelection(BookMarkGrouping.Type.GROUP_BY_TYPE == fBookMarkGrouping.getType());
        groupByType.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByType.getSelection() && fBookMarkGrouping.getType() != BookMarkGrouping.Type.GROUP_BY_TYPE)
              doGrouping(BookMarkGrouping.Type.GROUP_BY_TYPE);
          }
        });

        /* Group: By State */
        final MenuItem groupByState = new MenuItem(menu, SWT.RADIO);
        groupByState.setText(Messages.BookMarkExplorer_GROUP_BY_STATE);
        groupByState.setSelection(BookMarkGrouping.Type.GROUP_BY_STATE == fBookMarkGrouping.getType());
        groupByState.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByState.getSelection() && fBookMarkGrouping.getType() != BookMarkGrouping.Type.GROUP_BY_STATE)
              doGrouping(BookMarkGrouping.Type.GROUP_BY_STATE);
          }
        });

        /* Separator */
        new MenuItem(menu, SWT.SEPARATOR);

        /* Group: By Last Visit */
        final MenuItem groupByLastVisit = new MenuItem(menu, SWT.RADIO);
        groupByLastVisit.setText(Messages.BookMarkExplorer_GROUP_BY_LAST_VISIT);
        groupByLastVisit.setSelection(BookMarkGrouping.Type.GROUP_BY_LAST_VISIT == fBookMarkGrouping.getType());
        groupByLastVisit.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByLastVisit.getSelection() && fBookMarkGrouping.getType() != BookMarkGrouping.Type.GROUP_BY_LAST_VISIT)
              doGrouping(BookMarkGrouping.Type.GROUP_BY_LAST_VISIT);
          }
        });

        /* Group: By Popularity */
        final MenuItem groupByPopularity = new MenuItem(menu, SWT.RADIO);
        groupByPopularity.setText(Messages.BookMarkExplorer_GROUP_BY_POPULARITY);
        groupByPopularity.setSelection(BookMarkGrouping.Type.GROUP_BY_POPULARITY == fBookMarkGrouping.getType());
        groupByPopularity.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (groupByPopularity.getSelection() && fBookMarkGrouping.getType() != BookMarkGrouping.Type.GROUP_BY_POPULARITY)
              doGrouping(BookMarkGrouping.Type.GROUP_BY_POPULARITY);
          }
        });

        return menu;
      }
    });

    fToolBarManager.add(bookmarkGroup);

    /* Collapse All */
    fToolBarManager.add(new Separator());
    IAction collapseAll = new Action(Messages.BookMarkExplorer_COLLAPSE_ALL) {
      @Override
      public void run() {
        fViewer.collapseAll();
        fExpandedNodes.clear();
      }
    };
    collapseAll.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/collapseall.gif")); //$NON-NLS-1$
    fToolBarManager.add(collapseAll);

    /* BookmarkSet Navigation - TODO Consider showing dynamically */
    IAction previousSet = new Action(Messages.BookMarkExplorer_PREVIOUS_SET) {
      @Override
      public void run() {
        int index = getIndexOfRootFolder(fSelectedBookMarkSet);
        changeSet(getRootFolderAt(index - 1));
      }

      @Override
      public boolean isEnabled() {
        int index = getIndexOfRootFolder(fSelectedBookMarkSet);
        return index > 0 && fRootFolders.size() > 1;
      }
    };
    previousSet.setId(PREVIOUS_SET_ACTION);
    previousSet.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/backward.gif")); //$NON-NLS-1$
    previousSet.setDisabledImageDescriptor(OwlUI.getImageDescriptor("icons/dtool16/backward.gif")); //$NON-NLS-1$
    fToolBarManager.add(previousSet);

    IAction nextSet = new Action(Messages.BookMarkExplorer_NEXT_SET) {
      @Override
      public void run() {
        int index = getIndexOfRootFolder(fSelectedBookMarkSet);
        changeSet(getRootFolderAt(index + 1));
      }

      @Override
      public boolean isEnabled() {
        int index = getIndexOfRootFolder(fSelectedBookMarkSet);
        return index < (fRootFolders.size() - 1) && fRootFolders.size() > 1;
      }
    };
    nextSet.setId(NEXT_SET_ACTION);
    nextSet.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/forward.gif")); //$NON-NLS-1$
    nextSet.setDisabledImageDescriptor(OwlUI.getImageDescriptor("icons/dtool16/forward.gif")); //$NON-NLS-1$
    fToolBarManager.add(nextSet);

    /* Allow Contributions */
    fToolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
  }

  private void doFilter(BookMarkFilter.Type type) {

    /* Remember Selection */
    if (type != BookMarkFilter.Type.SHOW_ALL)
      fLastFilterType = type;
    else if (fBookMarkFilter.getType() != BookMarkFilter.Type.SHOW_ALL)
      fLastFilterType = fBookMarkFilter.getType();

    /* Change Filter Type */
    fBookMarkFilter.setType(type);
    fViewer.refresh(false);

    /* Restore Expanded Elements */
    restoreExpandedElements();

    /* Update Image */
    fToolBarManager.find(FILTER_ACTION).update(IAction.IMAGE);
  }

  private void doGrouping(BookMarkGrouping.Type type) {

    /* Remember Selection */
    if (type != BookMarkGrouping.Type.NO_GROUPING)
      fLastGroupType = type;
    else if (fBookMarkGrouping.getType() != BookMarkGrouping.Type.NO_GROUPING)
      fLastGroupType = fBookMarkGrouping.getType();

    /* Temporary change Sorter to reflect grouping */
    if (!fSortByName) {
      if (type.equals(BookMarkGrouping.Type.NO_GROUPING))
        fBookMarkComparator.setType(BookMarkSorter.Type.DEFAULT_SORTING);
      else if (type.equals(BookMarkGrouping.Type.GROUP_BY_LAST_VISIT))
        fBookMarkComparator.setType(BookMarkSorter.Type.SORT_BY_LAST_VISIT_DATE);
      else if (type.equals(BookMarkGrouping.Type.GROUP_BY_POPULARITY))
        fBookMarkComparator.setType(BookMarkSorter.Type.SORT_BY_POPULARITY);
    }

    /* Refresh w/o updating Labels */
    fBookMarkGrouping.setType(type);
    fViewer.refresh(false);

    /* Restore Sorter */
    fBookMarkComparator.setType(fSortByName ? BookMarkSorter.Type.SORT_BY_NAME : BookMarkSorter.Type.DEFAULT_SORTING);

    /* Restore expanded Elements */
    restoreExpandedElements();

    /* Update Image */
    fToolBarManager.find(GROUP_ACTION).update(IAction.IMAGE);
  }

  private void hookContextualMenu() {
    MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager manager) {

        /* New Menu */
        MenuManager newMenu = new MenuManager(Messages.BookMarkExplorer_NEW);
        manager.add(newMenu);

        /* New BookMark */
        Action newBookmarkAction = new Action(Messages.BookMarkExplorer_BOOKMARK) {
          @Override
          public void run() {
            IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
            IFolder parent = getParent(selection);
            IMark position = (IMark) ((selection.getFirstElement() instanceof IMark) ? selection.getFirstElement() : null);
            new NewBookMarkAction(fViewSite.getShell(), parent, position).run(null);
          }

          @Override
          public ImageDescriptor getImageDescriptor() {
            return OwlUI.BOOKMARK;
          }
        };
        newBookmarkAction.setId("org.rssowl.ui.actions.NewBookMark"); //$NON-NLS-1$
        newBookmarkAction.setActionDefinitionId("org.rssowl.ui.actions.NewBookMark"); //$NON-NLS-1$
        newMenu.add(newBookmarkAction);

        /* New NewsBin */
        Action newNewsBinAction = new Action(Messages.BookMarkExplorer_NEWSBIN) {
          @Override
          public void run() {
            IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
            IFolder parent = getParent(selection);
            IMark position = (IMark) ((selection.getFirstElement() instanceof IMark) ? selection.getFirstElement() : null);
            new NewNewsBinAction(fViewSite.getShell(), parent, position).run(null);
          }

          @Override
          public ImageDescriptor getImageDescriptor() {
            return OwlUI.NEWSBIN;
          }
        };
        newNewsBinAction.setId("org.rssowl.ui.actions.NewNewsBin"); //$NON-NLS-1$
        newNewsBinAction.setActionDefinitionId("org.rssowl.ui.actions.NewNewsBin"); //$NON-NLS-1$
        newMenu.add(newNewsBinAction);

        /* New Saved Search */
        Action newSavedSearchAction = new Action(Messages.BookMarkExplorer_SAVED_SEARCH) {
          @Override
          public void run() {
            IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
            IFolder parent = getParent(selection);
            IMark position = (IMark) ((selection.getFirstElement() instanceof IMark) ? selection.getFirstElement() : null);
            new NewSearchMarkAction(fViewSite.getShell(), parent, position).run(null);
          }

          @Override
          public ImageDescriptor getImageDescriptor() {
            return OwlUI.SEARCHMARK;
          }
        };
        newSavedSearchAction.setId("org.rssowl.ui.actions.NewSearchMark"); //$NON-NLS-1$
        newSavedSearchAction.setActionDefinitionId("org.rssowl.ui.actions.NewSearchMark"); //$NON-NLS-1$
        newMenu.add(newSavedSearchAction);

        /* New Folder */
        newMenu.add(new Separator());
        Action newFolderAction = new Action(Messages.BookMarkExplorer_FOLDER) {
          @Override
          public void run() {
            IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
            IFolder parent = getParent(selection);
            IMark position = (IMark) ((selection.getFirstElement() instanceof IMark) ? selection.getFirstElement() : null);
            new NewFolderAction(fViewSite.getShell(), parent, position).run(null);
          }

          @Override
          public ImageDescriptor getImageDescriptor() {
            return OwlUI.FOLDER;
          }
        };
        newFolderAction.setId("org.rssowl.ui.actions.NewFolder"); //$NON-NLS-1$
        newFolderAction.setActionDefinitionId("org.rssowl.ui.actions.NewFolder"); //$NON-NLS-1$
        newMenu.add(newFolderAction);

        manager.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));

        final IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
        IFolder selectedFolder = getFolder(selection);
        IPreferenceScope globalPreferences = Owl.getPreferenceService().getGlobalScope();
        IPreferenceScope eclipsePreferences = Owl.getPreferenceService().getEclipseScope();

        /* Open */
        manager.add(new Separator(OwlUI.M_OPEN));
        if (!selection.isEmpty()) {
          if (!eclipsePreferences.getBoolean(DefaultPreferences.ECLIPSE_SINGLE_CLICK_OPEN))
            manager.add(new OpenAction(fViewSite.getPage(), fViewer));

          /* Tab related Actions */
          if (globalPreferences.getBoolean(DefaultPreferences.ALWAYS_REUSE_FEEDVIEW) && OwlUI.isTabbedBrowsingEnabled()) {

            /* Open in new Tab */
            manager.add(new OpenInNewTabAction(fViewSite.getPage(), fViewer));

            /* Open Feeds of Folder in Tabs */
            if (selectedFolder != null)
              manager.add(new OpenInNewTabAction(fViewSite.getPage(), selectedFolder));
          }
        }

        manager.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));

        /* Mark Read */
        manager.add(new Separator(OwlUI.M_MARK));

        /* Search News */
        manager.add(new Separator());
        manager.add(new SearchInTypeAction(fViewSite.getWorkbenchWindow(), fViewer));
        manager.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));

        /* Share */
        if (getBookMark(selection) != null) {
          manager.add(new Separator("share")); //$NON-NLS-1$
          MenuManager shareMenu = new MenuManager(Messages.BookMarkExplorer_SHARING, OwlUI.SHARE, "sharebookmark"); //$NON-NLS-1$
          manager.add(shareMenu);

          List<ShareProvider> providers = Controller.getDefault().getShareProviders();
          for (final ShareProvider provider : providers) {
            if (provider.isEnabled()) {
              shareMenu.add(new Action(provider.getName()) {
                @Override
                public void run() {
                  if (SendLinkAction.ID.equals(provider.getId())) {
                    IActionDelegate action = new SendLinkAction();
                    action.selectionChanged(null, selection);
                    action.run(null);
                  } else {
                    IBookMark bookmark = getBookMark(selection);
                    if (bookmark != null) {
                      String shareLink = provider.toShareUrl(bookmark);
                      new OpenInBrowserAction(new StructuredSelection(shareLink)).run();
                    }
                  }
                };

                @Override
                public ImageDescriptor getImageDescriptor() {
                  if (StringUtils.isSet(provider.getIconPath()))
                    return OwlUI.getImageDescriptor(provider.getPluginId(), provider.getIconPath());

                  return super.getImageDescriptor();
                };

                @Override
                public boolean isEnabled() {
                  return !selection.isEmpty();
                };

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

          /* Configure Providers */
          shareMenu.add(new Separator());
          shareMenu.add(new Action(Messages.BookMarkExplorer_CONFIGURE) {
            @Override
            public void run() {
              PreferencesUtil.createPreferenceDialogOn(fViewer.getTree().getShell(), SharingPreferencesPage.ID, null, null).open();
            };
          });
        }

        manager.add(new Separator("copy")); //$NON-NLS-1$
        manager.add(new GroupMarker("edit")); //$NON-NLS-1$

        /* Allow Contributions */
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });

    /* Create and Register with Workbench */
    Menu menu = manager.createContextMenu(fViewer.getControl());
    fViewer.getControl().setMenu(menu);
    fViewSite.registerContextMenu(manager, fViewer);
  }

  private IBookMark getBookMark(IStructuredSelection selection) {
    List<?> list = selection.toList();
    for (Object object : list) {
      if (object instanceof IBookMark)
        return (IBookMark) object;
    }

    return null;
  }

  private IFolder getFolder(IStructuredSelection selection) {
    if (selection.size() == 1) {
      Object firstElement = selection.getFirstElement();
      if (firstElement instanceof IFolder)
        return (IFolder) firstElement;
    }

    return null;
  }

  private void registerListeners() {

    /* Listen for Events on Root-Folders */
    fFolderListener = new FolderAdapter() {

      @Override
      public void entitiesAdded(final Set<FolderEvent> events) {
        JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
          @Override
          public void run() {
            for (FolderEvent event : events) {
              if (event.getEntity().getParent() == null) {
                fRootFolders.add(event.getEntity());

                /* Show this Folder in the Explorer */
                changeSet(event.getEntity());
              }
            }
          }
        });
      }

      @Override
      public void entitiesDeleted(final Set<FolderEvent> events) {
        JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
          @Override
          public void run() {
            for (FolderEvent event : events) {
              IFolder deletedFolder = event.getEntity();
              IFolder parentFolder = event.getEntity().getParent();
              if (parentFolder == null) {
                int index = getIndexOfRootFolder(deletedFolder);
                fRootFolders.remove(event.getEntity());

                /* In case this Bookmark set is currently showing in the Explorer */
                if (fSelectedBookMarkSet.equals(deletedFolder)) {
                  if (fRootFolders.size() > index)
                    changeSet(getRootFolderAt(index));
                  else
                    changeSet(getRootFolderAt(index - 1));
                }

                /* Otherwise make sure to update Nav-Buttons */
                else {
                  fViewSite.getActionBars().getToolBarManager().find(PREVIOUS_SET_ACTION).update(IAction.ENABLED);
                  fViewSite.getActionBars().getToolBarManager().find(NEXT_SET_ACTION).update(IAction.ENABLED);
                }
              }
            }
          }
        });
      }
    };
    DynamicDAO.addEntityListener(IFolder.class, fFolderListener);

    /* Listen for Editors activated for the linking Feature */
    fPartListener = new IPartListener2() {
      @Override
      public void partActivated(IWorkbenchPartReference ref) {
        if (ref.getPart(true) instanceof IEditorPart) {

          /* Workaround for Bug 573 */
          JobRunner.runInUIThread(50, fViewer.getTree(), new Runnable() {
            @Override
            public void run() {
              editorActivated(fViewSite.getPage().getActiveEditor());
            }
          });
        }
      }

      @Override
      public void partBroughtToTop(IWorkbenchPartReference ref) {
        if (ref.getPart(true) == BookMarkExplorer.this)
          editorActivated(fViewSite.getPage().getActiveEditor());
      }

      @Override
      public void partOpened(IWorkbenchPartReference ref) {
        if (ref.getPart(true) == BookMarkExplorer.this)
          editorActivated(fViewSite.getPage().getActiveEditor());
      }

      @Override
      public void partVisible(IWorkbenchPartReference ref) {
        if (ref.getPart(true) == BookMarkExplorer.this)
          editorActivated(fViewSite.getPage().getActiveEditor());
      }

      @Override
      public void partClosed(IWorkbenchPartReference ref) {}

      @Override
      public void partDeactivated(IWorkbenchPartReference ref) {}

      @Override
      public void partHidden(IWorkbenchPartReference ref) {}

      @Override
      public void partInputChanged(IWorkbenchPartReference ref) {
        if (ref.getPart(true) instanceof IEditorPart) {

          /* Workaround for Bug 1126 */
          JobRunner.runInUIThread(50, fViewer.getTree(), new Runnable() {
            @Override
            public void run() {
              editorActivated(fViewSite.getPage().getActiveEditor());
            }
          });
        }
      }
    };

    fViewSite.getPage().addPartListener(fPartListener);

    /* Refresh Viewer when Sticky Color Changes */
    fPropertyChangeListener = new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        if (fViewer.getControl().isDisposed())
          return;

        if (OwlUI.STICKY_BG_COLOR_ID.equals(event.getProperty())) {
          fLabelProvider.updateResources();
          fViewer.refresh(true);
        }
      }
    };
    PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(fPropertyChangeListener);
  }

  private void unregisterListeners() {
    DynamicDAO.removeEntityListener(IFolder.class, fFolderListener);
    fViewSite.getPage().removePartListener(fPartListener);
    PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(fPropertyChangeListener);
  }

  private void hookGlobalActions() {

    /* Select All */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), new Action() {
      @Override
      public void run() {
        Control focusControl = fViewer.getControl().getDisplay().getFocusControl();

        /* Select All in Text Widget */
        if (focusControl instanceof Text) {
          ((Text) focusControl).selectAll();
        }

        /* Select All in Tree */
        else {
          ((Tree) fViewer.getControl()).selectAll();
          fViewer.setSelection(fViewer.getSelection());
        }
      }
    });

    /* Delete */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), new Action() {
      @Override
      public void run() {
        fViewer.getControl().getParent().setRedraw(false);
        try {
          new DeleteTypesAction(fViewer.getControl().getShell(), (IStructuredSelection) fViewer.getSelection()).run();
        } finally {
          fViewer.getControl().getParent().setRedraw(true);
        }
      }
    });

    /* Reload */
    fViewSite.getActionBars().setGlobalActionHandler(RetargetActions.RELOAD, new Action() {
      @Override
      public void run() {
        new ReloadTypesAction((IStructuredSelection) fViewer.getSelection(), fViewSite.getShell()).run();
      }
    });

    /* Cut */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(), new Action() {
      @Override
      public void run() {
        Control focusControl = fViewer.getControl().getDisplay().getFocusControl();

        /* Cut in Text Widget */
        if (focusControl instanceof Text)
          ((Text) focusControl).cut();
      }
    });

    /* Copy */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new Action() {
      @Override
      public void run() {
        Control focusControl = fViewer.getControl().getDisplay().getFocusControl();

        /* Copy in Text Widget */
        if (focusControl instanceof Text)
          ((Text) focusControl).copy();
      }
    });

    /* Paste */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), new Action() {
      @Override
      public void run() {
        Control focusControl = fViewer.getControl().getDisplay().getFocusControl();

        /* Paste in Text Widget */
        if (focusControl instanceof Text)
          ((Text) focusControl).paste();
      }
    });

    /* Undo (Eclipse Integration) */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.UNDO.getId(), new Action() {
      @Override
      public void run() {
        UndoStack.getInstance().undo();
      }
    });

    /* Redo (Eclipse Integration) */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.REDO.getId(), new Action() {
      @Override
      public void run() {
        UndoStack.getInstance().redo();
      }
    });

    /* Find (Eclipse Integration) */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.FIND.getId(), new FindAction());

    /* Properties */
    fViewSite.getActionBars().setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), new EntityPropertyDialogAction(fViewSite, fViewer));

    /* Disable some Edit-Actions at first */
    fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.CUT.getId()).setEnabled(false);
    fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId()).setEnabled(false);
    fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.PASTE.getId()).setEnabled(false);
  }

  /**
   * The user performed the "Find" action.
   */
  public void find() {
    setSearchBarVisible(true);
    fSearchBar.getControl().setFocus();
  }

  /*
   * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
   */
  @Override
  public void init(IViewSite site) throws PartInitException {
    super.init(site);
    fViewSite = site;
    fGlobalPreferences = Owl.getPreferenceService().getGlobalScope();
    fPrefDAO = DynamicDAO.getDAO(IPreferenceDAO.class);
    fExpandedNodes = new ArrayList<Long>();

    /* Sort Root-Folders by ID */
    fRootFolders = CoreUtils.loadRootFolders();

    /* Load Settings */
    loadState();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class adapter) {
    if (ISelectionProvider.class.equals(adapter))
      return fViewer;

    if (IInputProvider.class.equals(adapter))
      return fViewer.getInput();

    return super.getAdapter(adapter);
  }

  /*
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    fViewer.getControl().setFocus();
  }

  /*
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
    unregisterListeners();
    saveState();
  }

  /**
   * Navigate to the next/previous read or unread Feed respecting the Marks that
   * are displayed in the Tree-Viewer.
   *
   * @param next If <code>TRUE</code>, move to the next item, or previous if
   * <code>FALSE</code>.
   * @param unread If <code>TRUE</code>, only move to unread items, or ignore if
   * <code>FALSE</code>.
   * @param performOnFeedView If <code>TRUE</code>, this navigation should also
   * invoke a follow up navigation in the opened feed view if a valid target is
   * found and <code>false</code> otherwise.
   * @return Returns <code>TRUE</code> in case navigation found a valid item, or
   * <code>FALSE</code> otherwise.
   */
  public boolean navigate(boolean next, boolean unread, boolean performOnFeedView) {
    Tree explorerTree = fViewer.getTree();

    /* Nothing to Navigate to */
    if (explorerTree.isDisposed())
      return false;

    ITreeNode targetNode = null;

    /* 1.) Navigate in opened Tree */
    targetNode = navigateInTree(explorerTree, next, unread);

    /* 2.) Navigate in BookMark-Sets */
    if (targetNode == null)
      targetNode = navigateInSets(next, unread);

    /* 3.) Finally, wrap in visible Tree if next */
    if (targetNode == null && next) {
      ITreeNode startingNode = new WidgetTreeNode(fViewer.getTree(), fViewer);
      targetNode = navigate(startingNode, next, unread);
    }

    /* Perform navigation if Node was found */
    if (targetNode != null) {
      performNavigation(targetNode, performOnFeedView, unread);
      return true;
    }

    return false;
  }

  private void performNavigation(ITreeNode targetNode, boolean performOnFeedView, boolean unread) {
    INewsMark mark = (INewsMark) targetNode.getData();

    /* Set Selection to Mark */
    IStructuredSelection selection = new StructuredSelection(mark);
    fViewer.setSelection(selection);

    /* Open in FeedView */
    PerformAfterInputSet perform = null;
    if (performOnFeedView && unread)
      perform = PerformAfterInputSet.SELECT_UNREAD_NEWS;
    else if (performOnFeedView)
      perform = PerformAfterInputSet.SELECT_FIRST_NEWS;

    OwlUI.openInFeedView(fViewSite.getPage(), selection, true, false, perform);
  }

  private ITreeNode navigateInTree(Tree tree, boolean next, boolean unread) {
    ITreeNode resultingNode = null;

    /* Selection is Present */
    if (tree.getSelectionCount() > 0) {

      /* Try navigating from Selection */
      ITreeNode startingNode = new WidgetTreeNode(tree.getSelection()[0], fViewer);
      resultingNode = navigate(startingNode, next, unread);
      if (resultingNode != null)
        return resultingNode;
    }

    /* No Selection is Present */
    else {
      ITreeNode startingNode = new WidgetTreeNode(tree, fViewer);
      resultingNode = navigate(startingNode, next, unread);
      if (resultingNode != null)
        return resultingNode;
    }

    return resultingNode;
  }

  private ITreeNode navigateInSets(boolean next, boolean unread) {
    ITreeNode targetNode = null;

    /* Index of current visible Set */
    int index = getIndexOfRootFolder(fSelectedBookMarkSet);

    /* Look in next Sets */
    if (next) {

      /* Sets to the right */
      for (int i = index + 1; i < fRootFolders.size(); i++) {
        targetNode = navigateInSet(getRootFolderAt(i), true, unread);
        if (targetNode != null)
          return targetNode;
      }

      /* Sets to the left */
      for (int i = 0; i < index; i++) {
        targetNode = navigateInSet(getRootFolderAt(i), true, unread);
        if (targetNode != null)
          return targetNode;
      }
    }

    /* Look in previous Sets */
    else {

      /* Sets to the left */
      for (int i = index - 1; i >= 0; i--) {
        targetNode = navigateInSet(getRootFolderAt(i), true, unread);
        if (targetNode != null)
          return targetNode;
      }

      /* Sets to the right */
      for (int i = fRootFolders.size() - 1; i > index; i--) {
        targetNode = navigateInSet(getRootFolderAt(i), true, unread);
        if (targetNode != null)
          return targetNode;
      }
    }

    return targetNode;
  }

  private ITreeNode navigateInSet(IFolder set, boolean next, boolean unread) {
    ITreeNode node = new ModelTreeNode(set);
    ITreeNode targetNode = navigate(node, next, unread);
    if (targetNode != null) {
      changeSet(set);
      return targetNode;
    }

    return null;
  }

  private ITreeNode navigate(ITreeNode startingNode, boolean next, final boolean unread) {

    /* Create Traverse-Helper */
    TreeTraversal traverse = new TreeTraversal(startingNode) {
      @Override
      public boolean select(ITreeNode node) {
        return isValidNavigation(node, unread);
      }
    };

    /* Retrieve and select new Target Node */
    ITreeNode targetNode = (next ? traverse.nextNode() : traverse.previousNode());

    return targetNode;
  }

  private boolean isValidNavigation(ITreeNode node, boolean unread) {
    Object data = node.getData();

    /* Check for Unread news if required */
    if (data instanceof INewsMark) {
      INewsMark newsmark = (INewsMark) data;
      if (unread && newsmark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)) == 0)
        return false;
    }

    /* Folders and Entity Groups are no valid navigation nodes */
    else if (data instanceof IFolder || data instanceof EntityGroup)
      return false;

    return true;
  }

  /**
   * Save all Settings of the Explorer immediately.
   */
  public void saveState() {

    /* Expanded Elements */
    saveExpandedElements();

    /* Selection */
    if (fLastSelection != null && !fLastSelection.isEmpty()) {
      Object element = fLastSelection.getFirstElement();
      if (element instanceof IFolderChild)
        fGlobalPreferences.putLong(PREF_SELECTED_FOLDER_CHILD, ((IFolderChild) element).getId());
      else
        fGlobalPreferences.delete(PREF_SELECTED_FOLDER_CHILD);
    } else
      fGlobalPreferences.delete(PREF_SELECTED_FOLDER_CHILD);

    /* Misc. Settings */
    if (!fBlockSaveState) {
      fGlobalPreferences.putBoolean(DefaultPreferences.BE_BEGIN_SEARCH_ON_TYPING, fBeginSearchOnTyping);
      fGlobalPreferences.putBoolean(DefaultPreferences.BE_ALWAYS_SHOW_SEARCH, fAlwaysShowSearch);
      fGlobalPreferences.putBoolean(DefaultPreferences.BE_SORT_BY_NAME, fSortByName);
      fGlobalPreferences.putBoolean(DefaultPreferences.BE_ENABLE_LINKING, fLinkingEnabled);
      fGlobalPreferences.putBoolean(DefaultPreferences.BE_DISABLE_FAVICONS, !fFaviconsEnabled);
      fGlobalPreferences.putInteger(DefaultPreferences.BE_FILTER_TYPE, fBookMarkFilter.getType().ordinal());
      fGlobalPreferences.putInteger(DefaultPreferences.BE_GROUP_TYPE, fBookMarkGrouping.getType().ordinal());
    }
  }

  private void saveExpandedElements() {
    int i = 0;
    long elements[] = new long[fExpandedNodes.size()];
    for (Object element : fExpandedNodes) {
      elements[i] = (Long) element;
      i++;
    }
    /* Add the ID of the current selected Set to make it Unique */
    String key = PREF_EXPANDED_NODES + fSelectedBookMarkSet.getId();

    IPreference pref = fPrefDAO.loadOrCreate(key);
    pref.putLongs(elements);
    fPrefDAO.save(pref);
  }

  private void loadState() {

    /* Misc. Settings */
    fBeginSearchOnTyping = fGlobalPreferences.getBoolean(DefaultPreferences.BE_BEGIN_SEARCH_ON_TYPING);
    fAlwaysShowSearch = fGlobalPreferences.getBoolean(DefaultPreferences.BE_ALWAYS_SHOW_SEARCH);
    fSortByName = fGlobalPreferences.getBoolean(DefaultPreferences.BE_SORT_BY_NAME);
    fLinkingEnabled = fGlobalPreferences.getBoolean(DefaultPreferences.BE_ENABLE_LINKING);
    fFaviconsEnabled = !fGlobalPreferences.getBoolean(DefaultPreferences.BE_DISABLE_FAVICONS);
    fFilterType = BookMarkFilter.Type.values()[fGlobalPreferences.getInteger(DefaultPreferences.BE_FILTER_TYPE)];
    fGroupingType = BookMarkGrouping.Type.values()[fGlobalPreferences.getInteger(DefaultPreferences.BE_GROUP_TYPE)];

    String selectedBookMarkSetPref = getSelectedBookMarkSetPref(fViewSite.getWorkbenchWindow());
    IPreference pref = fPrefDAO.load(selectedBookMarkSetPref);
    Assert.isTrue(fRootFolders.size() > 0, Messages.BookMarkExplorer_ERROR_NO_SET_FOUND);
    if (pref != null)
      fSelectedBookMarkSet = new FolderReference(pref.getLong().longValue()).resolve();
    else {
      fSelectedBookMarkSet = getRootFolderAt(0);

      /* Save this to make sure subsequent calls succeed */
      pref = Owl.getModelFactory().createPreference(selectedBookMarkSetPref);
      pref.putLongs(fSelectedBookMarkSet.getId());
      fPrefDAO.save(pref);
    }

    /* Expanded Elements */
    loadExpandedElements();

    /* Selected Folder Child */
    fLastSelectedFolderChild = fGlobalPreferences.getLong(PREF_SELECTED_FOLDER_CHILD);
  }

  /* Expanded Elements - Use ID of selected Set to make it Unique */
  private void loadExpandedElements() {
    IPreference pref = fPrefDAO.load(PREF_EXPANDED_NODES + fSelectedBookMarkSet.getId());
    if (pref != null) {
      for (long element : pref.getLongs())
        fExpandedNodes.add(element);
    }
  }

  void restoreExpandedElements() {
    for (Long expandedNodeId : fExpandedNodes) {
      if (fBookMarkGrouping.getType() == BookMarkGrouping.Type.NO_GROUPING)
        fViewer.setExpandedState(new FolderReference(expandedNodeId), true);
      else
        fViewer.setExpandedState(new EntityGroup(expandedNodeId, BookMarkGrouping.GROUP_CATEGORY_ID), true);
    }
  }

  private IFolder getParent(IStructuredSelection selection) {
    if (!selection.isEmpty()) {
      Object obj = selection.getFirstElement();
      if (obj instanceof IFolder)
        return (IFolder) obj;
      else if (obj instanceof IMark)
        return ((IMark) obj).getParent();
    }

    /* Default is selected Set */
    return fSelectedBookMarkSet;
  }

  private IFolder getRootFolderAt(int index) {
    int i = 0;
    for (IFolder rootFolder : fRootFolders) {
      if (i == index)
        return rootFolder;
      i++;
    }

    return null;
  }

  private int getIndexOfRootFolder(IFolder folder) {
    int i = 0;
    for (IFolder rootFolder : fRootFolders) {
      if (rootFolder.equals(folder))
        return i;
      i++;
    }

    return -1;
  }

  boolean isGroupingEnabled() {
    return fBookMarkGrouping.isActive();
  }

  /**
   * @return <code>true</code> if elements are sorted by name,
   * <code>false</code> otherwise.
   */
  public boolean isSortByNameEnabled() {
    return fBookMarkComparator.getType() == BookMarkSorter.Type.SORT_BY_NAME;
  }

  /**
   * Allows to disable saving settings on dispose. Useful if settings have been
   * imported and the application is to restart.
   *
   * @param saveStateOnDispose <code>true</code> to save settings on dispose and
   * <code>false</code> to block this.
   */
  public void saveStateOnDispose(boolean saveStateOnDispose) {
    fBlockSaveState = !saveStateOnDispose;
  }
}
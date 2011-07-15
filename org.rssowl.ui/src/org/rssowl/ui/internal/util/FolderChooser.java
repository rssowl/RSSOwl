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

package org.rssowl.ui.internal.util;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.NewFolderAction;
import org.rssowl.ui.internal.views.explorer.BookMarkLabelProvider;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The <code>FolderChooser</code> allows to select a <code>IFolder</code>. It
 * provides an expandable Tree-Viewer to display all folders which is initially
 * collapsed.
 *
 * @author bpasero
 */
public class FolderChooser extends Composite implements DisposeListener {

  /* Delay in ms before updating Selection on Events */
  private static final int SELECTION_DELAY = 20;

  /* Default Height in Items */
  private static final int DEFAULT_ITEM_HEIGHT = 10;

  private Composite fParent;
  private IFolder fSelectedFolder;
  private ResourceManager fResources;
  private Composite fFolderViewerContainer;
  private ToolItem fToggleItem;
  private TreeViewer fFolderViewer;
  private Label fFolderIcon;
  private Label fFolderName;
  private int fViewerHeight;
  private FolderAdapter fFolderListener;
  private ToolBar fAddFolderBar;
  private List<IFolder> fExcludes;
  private final boolean fExpandable;
  private int fItemHeight;
  private ExpandStrategy fExpandStrategy = ExpandStrategy.RESIZE;

  /** The strategy when expanding the Control */
  public enum ExpandStrategy {

    /** Pack the Parent */
    PACK,

    /** Resize the Parent */
    RESIZE;
  }

  /**
   * @param parent
   * @param initial
   * @param style
   * @param expandable
   */
  public FolderChooser(Composite parent, IFolder initial, int style, boolean expandable) {
    this(parent, initial, null, style, expandable);
  }

  /**
   * @param parent
   * @param initial
   * @param excludes
   * @param style
   * @param expandable
   */
  public FolderChooser(Composite parent, IFolder initial, List<IFolder> excludes, int style, boolean expandable) {
    this(parent, initial, excludes, style, expandable, DEFAULT_ITEM_HEIGHT);
  }

  /**
   * @param parent
   * @param initial
   * @param excludes
   * @param style
   * @param expandable
   * @param itemHeight
   */
  public FolderChooser(Composite parent, IFolder initial, List<IFolder> excludes, int style, boolean expandable, int itemHeight) {
    super(parent, style);
    fParent = parent;
    fSelectedFolder = initial;
    fExcludes = excludes;
    fExpandable = expandable;
    fItemHeight = itemHeight;
    fResources = new LocalResourceManager(JFaceResources.getResources(), parent);

    initComponents();
    addDisposeListener(this);
  }

  /**
   * @return Returns the <code>IFolder</code> that has been selected.
   */
  public IFolder getFolder() {
    return fSelectedFolder;
  }

  /**
   * @param expandStrategy the strategy when expanding the Control (either
   * {@link ExpandStrategy#PACK} or {@link ExpandStrategy#RESIZE}.
   */
  public void setExpandStrategy(ExpandStrategy expandStrategy) {
    fExpandStrategy = expandStrategy;
  }

  /*
   * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
   */
  public void widgetDisposed(DisposeEvent e) {
    unregisterListeners();
  }

  private void registerListeners() {
    fFolderListener = new FolderAdapter() {
      @Override
      public void entitiesUpdated(final Set<FolderEvent> events) {
        if (events.isEmpty())
          return;

        /* Refresh and show added Folder */
        JobRunner.runInUIThread(fFolderViewer.getControl(), new Runnable() {
          public void run() {
            fFolderViewer.refresh();
            FolderEvent event = events.iterator().next();
            expand(event.getEntity());
          }
        });
      }

      @Override
      public void entitiesAdded(final Set<FolderEvent> events) {
        if (events.isEmpty())
          return;

        /* Select added Folder */
        JobRunner.runInUIThread(SELECTION_DELAY, fFolderViewer.getControl(), new Runnable() {
          public void run() {
            FolderEvent event = events.iterator().next();
            fFolderViewer.setSelection(new StructuredSelection(event.getEntity()));
          }
        });
      }
    };

    DynamicDAO.addEntityListener(IFolder.class, fFolderListener);
  }

  private void expand(IFolder folder) {
    IFolder parent = folder.getParent();
    if (parent != null)
      expand(parent);

    fFolderViewer.setExpandedState(folder, true);
  }

  private void unregisterListeners() {
    DynamicDAO.removeEntityListener(IFolder.class, fFolderListener);
  }

  private void initComponents() {
    Composite headerContainer = new Composite(this, SWT.None);
    headerContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0));
    ((GridLayout) headerContainer.getLayout()).marginLeft = 3;
    headerContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    headerContainer.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    if (fExpandable) {
      headerContainer.setCursor(fParent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
      headerContainer.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseDown(MouseEvent e) {
          onToggle();
          notifyListeners(SWT.Selection, new Event());
        }
      });
    }

    fFolderIcon = new Label(headerContainer, SWT.None);
    fFolderIcon.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
    fFolderIcon.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    if (fExpandable) {
      fFolderIcon.setCursor(fParent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
      fFolderIcon.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseDown(MouseEvent e) {
          onToggle();
          notifyListeners(SWT.Selection, new Event());
        }
      });
    }

    fFolderName = new Label(headerContainer, SWT.None);
    fFolderName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    fFolderName.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    if (fExpandable) {
      fFolderName.setCursor(fParent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
      fFolderName.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseDown(MouseEvent e) {
          onToggle();
          notifyListeners(SWT.Selection, new Event());
        }
      });
    }

    Composite toolbarContainer = new Composite(headerContainer, SWT.NONE);
    toolbarContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 1, false));
    toolbarContainer.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
    toolbarContainer.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    fAddFolderBar = new ToolBar(toolbarContainer, SWT.FLAT);
    OwlUI.makeAccessible(fAddFolderBar, Messages.FolderChooser_NEW_FOLDER);
    fAddFolderBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
    fAddFolderBar.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    fAddFolderBar.setCursor(headerContainer.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
    fAddFolderBar.setVisible(!fExpandable);

    ToolItem addFolderItem = new ToolItem(fAddFolderBar, SWT.PUSH);
    addFolderItem.setImage(OwlUI.getImage(fResources, "icons/etool16/add_crop.gif")); //$NON-NLS-1$
    addFolderItem.setToolTipText(Messages.FolderChooser_NEW_FOLDER);
    addFolderItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onNewFolder();
        notifyListeners(SWT.Selection, new Event());
      }
    });

    ToolBar toggleBar = new ToolBar(toolbarContainer, SWT.FLAT);
    OwlUI.makeAccessible(toggleBar, Messages.FolderChooser_SHOW_FOLDERS);
    toggleBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
    ((GridData) toggleBar.getLayoutData()).exclude = !fExpandable;
    toggleBar.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    toggleBar.setCursor(headerContainer.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));

    fToggleItem = new ToolItem(toggleBar, SWT.PUSH);
    fToggleItem.setImage(OwlUI.getImage(fResources, "icons/ovr16/arrow_down.gif")); //$NON-NLS-1$
    fToggleItem.setToolTipText(Messages.FolderChooser_SHOW_FOLDERS);
    fToggleItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onToggle();
        notifyListeners(SWT.Selection, new Event());
      }
    });

    fFolderViewerContainer = new Composite(this, SWT.None);
    fFolderViewerContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 2, 0, false));
    fFolderViewerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fFolderViewerContainer.setBackground(fParent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    Label separator = new Label(fFolderViewerContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fFolderViewer = new TreeViewer(fFolderViewerContainer, SWT.None);
    fFolderViewer.setAutoExpandLevel(2);
    fFolderViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fFolderViewer.getTree().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    fViewerHeight = fFolderViewer.getTree().getItemHeight() * fItemHeight + 12;
    ((GridData) fFolderViewerContainer.getLayoutData()).heightHint = fViewerHeight;
    ((GridData) fFolderViewerContainer.getLayoutData()).exclude = fExpandable;

    /* Sort by Name if set so */
    if (Owl.getPreferenceService().getGlobalScope().getBoolean(DefaultPreferences.BE_SORT_BY_NAME)) {
      fFolderViewer.setComparator(new ViewerComparator() {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
          IFolder f1 = (IFolder) e1;
          IFolder f2 = (IFolder) e2;

          return f1.getName().compareTo(f2.getName());
        }
      });
    }

    /* Filter excluded Folders */
    fFolderViewer.addFilter(new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (fExcludes == null)
          return true;

        return !fExcludes.contains(element) && !fExcludes.contains(parentElement);
      }
    });

    fFolderViewer.setContentProvider(new ITreeContentProvider() {
      public Object[] getElements(Object inputElement) {
        Collection<IFolder> rootFolders = CoreUtils.loadRootFolders();
        return rootFolders.toArray();
      }

      public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IFolder) {
          IFolder folder = (IFolder) parentElement;
          return folder.getFolders().toArray();
        }

        return new Object[0];
      }

      public Object getParent(Object element) {
        if (element instanceof IFolder) {
          IFolder folder = (IFolder) element;
          return folder.getParent();
        }

        return null;
      }

      public boolean hasChildren(Object element) {
        if (element instanceof IFolder) {
          IFolder folder = (IFolder) element;
          return !folder.getFolders().isEmpty();
        }

        return false;
      }

      public void dispose() {}

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    });

    boolean usedInDialog = (fParent.getShell().getParent() != null);
    fFolderViewer.setLabelProvider(new BookMarkLabelProvider(false, usedInDialog));
    fFolderViewer.setInput(new Object());

    fFolderViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty())
          onFolderSelected((IFolder) selection.getFirstElement());
        notifyListeners(SWT.Selection, new Event());
      }
    });

    fFolderViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        IFolder folder = (IFolder) selection.getFirstElement();

        /* Expand / Collapse Folder */
        if (!folder.getFolders().isEmpty()) {
          boolean expandedState = !fFolderViewer.getExpandedState(folder);
          fFolderViewer.setExpandedState(folder, expandedState);
        }

        /* Select Folder and toggle */
        else if (fExpandable) {
          onToggle();
        }
      }
    });

    /* Select the input Folder and expand */
    fFolderViewer.setSelection(new StructuredSelection(fSelectedFolder));
    fFolderViewer.setExpandedState(fSelectedFolder, true);

    /* Add Menu: "New Folder" */
    MenuManager menuManager = new MenuManager();
    menuManager.add(new Action(Messages.FolderChooser_NEW_FOLDER) {
      @Override
      public void run() {
        onNewFolder();
      }
    });

    Menu menu = menuManager.createContextMenu(fFolderViewer.getTree());
    fFolderViewer.getTree().setMenu(menu);

    /* Register Model Listeners */
    registerListeners();
  }

  private void onNewFolder() {

    /* Make sure Folder-List is visible */
    if (((GridData) fFolderViewerContainer.getLayoutData()).exclude && fExpandable)
      onToggle();

    /* Create new Folder */
    IStructuredSelection selection = (IStructuredSelection) fFolderViewer.getSelection();
    NewFolderAction action = new NewFolderAction(fFolderViewer.getTree().getShell(), (IFolder) selection.getFirstElement(), null);
    action.run(null);
  }

  private void onFolderSelected(IFolder folder) {
    fSelectedFolder = folder;
    fFolderIcon.setImage(OwlUI.getImage(fResources, folder.getParent() != null ? OwlUI.FOLDER : OwlUI.BOOKMARK_SET));
    fFolderName.setText(folder.getName());
  }

  private void onToggle() {
    boolean excluded = ((GridData) fFolderViewerContainer.getLayoutData()).exclude;

    fToggleItem.setImage(OwlUI.getImage(fResources, excluded ? "icons/ovr16/arrow_up.gif" : "icons/ovr16/arrow_down.gif")); //$NON-NLS-1$ //$NON-NLS-2$
    fToggleItem.setToolTipText(excluded ? Messages.FolderChooser_HIDE_FOLDERS : Messages.FolderChooser_SHOW_FOLDERS);

    ((GridData) fFolderViewerContainer.getLayoutData()).exclude = !excluded;
    Shell shell = fFolderViewerContainer.getShell();
    shell.layout();

    fAddFolderBar.setVisible(excluded);

    /* Increase Size of Shell to fit Control */
    if (fExpandStrategy == ExpandStrategy.RESIZE) {
      Point size = shell.getSize();
      shell.setSize(size.x, size.y + (excluded ? fViewerHeight : -fViewerHeight));
    }

    /* Pack Shell and expect enough room to fit the Control */
    else {
      int currentWidth = shell.getSize().x;
      Point desiredSize = shell.computeSize(currentWidth, SWT.DEFAULT);
      if (desiredSize.y > shell.getSize().y)
        shell.setSize(currentWidth, desiredSize.y);
      else
        shell.layout(true, true);
    }

    if (excluded)
      fFolderViewer.getTree().setFocus();
    else
      fFolderViewer.getTree().getShell().setFocus();
  }
}
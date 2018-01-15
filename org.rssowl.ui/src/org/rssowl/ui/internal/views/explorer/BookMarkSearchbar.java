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
 **     IBM Corporation - initial API and implementation                     **
 **     RSSOwl Development Team - adapted to work with BookMark Explorer     **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.ui.internal.views.explorer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.progress.WorkbenchJob;
import org.rssowl.ui.internal.ContextMenuCreator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.views.explorer.BookMarkFilter.SearchTarget;

/**
 * A simple control that provides a text widget. The contents of the text widget
 * are used to drive a <code>PatternFilter</code> that is part of a TreeViewer.
 *
 * @author bpasero
 */
class BookMarkSearchbar extends Composite {

  /* Delay before filtering takes place */
  private static final int FILTER_DELAY = 300;

  /* Action Bar */
  private static final String BAR_ACTION_ID = "org.rssowl.ui.internal.views.explorer.BarAction"; //$NON-NLS-1$

  private Composite fFilterComposite;
  private ToolBarManager fFilterToolBar;
  private Object fExpandedElements[];
  private Text fFilterText;
  private String fInitialText = ""; //$NON-NLS-1$
  private BookMarkFilter fPatternFilter;
  private Job fRefreshJob;
  private Object fSelectedElements[];
  private TreeViewer fViewer;
  private IViewSite fViewSite;

  /**
   * Create a new instance of the receiver.
   *
   * @param viewSite A link to the ViewSite this Bar is in.
   * @param parent parent <code>Composite</code>
   * @param viewer The Viewer this Filter is working for.
   * @param patternFilter The PatternFilter to use.
   */
  BookMarkSearchbar(IViewSite viewSite, Composite parent, TreeViewer viewer, BookMarkFilter patternFilter) {
    super(parent, SWT.NONE);

    fViewSite = viewSite;
    fViewer = viewer;
    fPatternFilter = patternFilter;

    createControl(parent);
    createRefreshJob();

    setFont(parent.getFont());
  }

  /* Create Toolbar with "Clear" Button */
  private void createClearText(Composite parent) {
    ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL);
    fFilterToolBar = new ToolBarManager(toolBar);
    fFilterToolBar.getControl().setBackground(parent.getBackground());
    fFilterToolBar.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    /* Initially Hide */
    ((GridData) toolBar.getLayoutData()).exclude = true;
    toolBar.setVisible(false);

    IAction clearTextAction = new Action("", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
      @Override
      public void run() {
        setFilterText(""); //$NON-NLS-1$
      }
    };

    clearTextAction.setToolTipText(Messages.BookMarkSearchbar_CLEAR);
    clearTextAction.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/clear.gif")); //$NON-NLS-1$

    fFilterToolBar.add(clearTextAction);
  }

  /* Creates the Container for the Filter Controls */
  private void createControl(Composite parent) {
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    setLayout(layout);
    setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Container for Filter Controls */
    fFilterComposite = new Composite(this, SWT.NONE);
    GridLayout filterLayout = new GridLayout(OwlUI.needsCancelControl() ? 3 : 2, false);
    filterLayout.marginHeight = 0;
    filterLayout.marginWidth = 0;
    filterLayout.horizontalSpacing = 3;
    fFilterComposite.setLayout(filterLayout);
    fFilterComposite.setFont(parent.getFont());
    fFilterComposite.setBackground(parent.getBackground());

    /* Create Filter Controls */
    createFilterControls(fFilterComposite);
    fFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fFilterComposite.setBackground(parent.getBackground());

    /* Set height as required */
    if (fFilterToolBar != null) {
      int prefContainerHeight = fFilterComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
      int prefToolbarHeight = fFilterToolBar.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
      if (prefToolbarHeight >= prefContainerHeight)
        ((GridData) fFilterComposite.getLayoutData()).heightHint = prefToolbarHeight;
    }
  }

  /* Create Label, Input Field and ToolBar */
  private Composite createFilterControls(Composite parent) {
    createFilterTarget(parent);
    createFilterText(parent);
    if (OwlUI.needsCancelControl())
      createClearText(parent);

    if (fFilterToolBar != null) {
      fFilterToolBar.update(false);
      fFilterToolBar.getControl().setVisible(false);
    }
    return parent;
  }

  /* ToolBar to control SearchTarget */
  private void createFilterTarget(Composite parent) {
    final ToolBarManager filterTargetManager = new ToolBarManager(SWT.FLAT);

    IAction filterTargetAction = new Action("", IAction.AS_DROP_DOWN_MENU) { //$NON-NLS-1$
      @Override
      public void run() {
        OwlUI.positionDropDownMenu(this, filterTargetManager);
      }

      @Override
      public String getId() {
        return BAR_ACTION_ID;
      }
    };

    filterTargetAction.setMenuCreator(new ContextMenuCreator() {

      @Override
      public Menu createMenu(Control parent) {
        Menu menu = new Menu(parent);

        /* Search on: Name */
        final MenuItem searchName = new MenuItem(menu, SWT.RADIO);
        searchName.setText(Messages.BookMarkSearchbar_NAME);
        searchName.setSelection(BookMarkFilter.SearchTarget.NAME == fPatternFilter.getSearchTarget());
        searchName.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchName.getSelection() && fPatternFilter.getSearchTarget() != BookMarkFilter.SearchTarget.NAME)
              doSearch(BookMarkFilter.SearchTarget.NAME);
          }
        });

        /* Search on: Link */
        final MenuItem searchLink = new MenuItem(menu, SWT.RADIO);
        searchLink.setText(Messages.BookMarkSearchbar_LINK);
        searchLink.setSelection(BookMarkFilter.SearchTarget.LINK == fPatternFilter.getSearchTarget());
        searchLink.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (searchLink.getSelection() && fPatternFilter.getSearchTarget() != BookMarkFilter.SearchTarget.LINK)
              doSearch(BookMarkFilter.SearchTarget.LINK);
          }
        });

        return menu;
      }
    });

    filterTargetAction.setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/find.gif")); //$NON-NLS-1$
    filterTargetManager.add(filterTargetAction);

    filterTargetManager.createControl(parent);
    filterTargetManager.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
  }

  private void doSearch(SearchTarget target) {
    fPatternFilter.setSearchTarget(target);
    fFilterText.setFocus();

    /* Set Message */
    if (fPatternFilter.getSearchTarget() == SearchTarget.NAME)
      fFilterText.setMessage(Messages.BookMarkSearchbar_NAME);
    else
      fFilterText.setMessage(Messages.BookMarkSearchbar_LINK);

    if (fFilterText.getText().length() > 0)
      textChanged();
  }

  /* Input Field for typing into the Filter */
  private void createFilterText(Composite parent) {
    fFilterText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
    fFilterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

    /* Set Message */
    if (fPatternFilter.getSearchTarget() == SearchTarget.NAME)
      fFilterText.setMessage(Messages.BookMarkSearchbar_NAME);
    else
      fFilterText.setMessage(Messages.BookMarkSearchbar_LINK);

    /* Register this Input Field to Context Service */
    Controller.getDefault().getContextService().registerInputField(fFilterText);

    /* Override Accessible */
    fFilterText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
      @Override
      public void getName(AccessibleEvent e) {
        String filterTextString = fFilterText.getText();
        if (filterTextString.length() == 0)
          e.result = fInitialText;
        else
          e.result = filterTextString;
      }
    });

    /* Select All on Focus if input matches Initial Text */
    fFilterText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        JobRunner.runInUIThread(0, true, fFilterText, new Runnable() {
          @Override
          public void run() {
            if (fInitialText.equals(fFilterText.getText().trim()))
              fFilterText.selectAll();
          }
        });

        /* Enable some Edit Actions */
        fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.CUT.getId()).setEnabled(true);
        fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId()).setEnabled(true);
        fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.PASTE.getId()).setEnabled(true);
      }

      @Override
      public void focusLost(FocusEvent e) {

        /* Disable some Edit Actions */
        fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.CUT.getId()).setEnabled(false);
        fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId()).setEnabled(false);
        fViewSite.getActionBars().getGlobalActionHandler(ActionFactory.PASTE.getId()).setEnabled(false);
      }
    });

    /* Focus Tree on Arrow Up or Down */
    fFilterText.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {

        /* Pressed ESC */
        if (e.character == SWT.ESC) {
          setFilterText(""); //$NON-NLS-1$
          fViewer.getTree().setFocus();
          return;
        }

        /* Pressed Arrow Down or Up */
        boolean hasItems = fViewer.getTree().getItemCount() > 0;
        if (hasItems && (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP))
          fViewer.getTree().setFocus();
        else if (e.character == SWT.CR)
          return;
      }
    });

    /* Handle the CR Key Pressed */
    fFilterText.addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_RETURN) {
          e.doit = false;

          /* Results available */
          if (fViewer.getTree().getItemCount() > 0) {
            boolean hasFocus = fViewer.getTree().setFocus();
            boolean textChanged = !fInitialText.equals(fFilterText.getText().trim());

            /* Select the first matching Item */
            if (hasFocus && textChanged && fFilterText.getText().trim().length() > 0) {
              TreeItem item = getFirstMatchingItem(fViewer.getTree().getItems());
              if (item != null) {
                fViewer.getTree().setSelection(new TreeItem[] { item });
                ISelection sel = fViewer.getSelection();
                fViewer.setSelection(sel, true);
              }
            }
          }
        }
      }
    });

    /* Update Filter on Modified Input */
    fFilterText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        textChanged();
      }
    });
  }

  /* Create the Job that refreshes the TreeViewer */
  private void createRefreshJob() {
    fRefreshJob = new WorkbenchJob("") {//$NON-NLS-1$
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {

        /* Tree Disposed */
        if (fViewer.getControl().isDisposed())
          return Status.CANCEL_STATUS;

        /* Get the Filter Pattern */
        String text = fFilterText != null ? fFilterText.getText() : null;
        if (text == null)
          return Status.OK_STATUS;

        /* Check if the Initial Text was set */
        boolean initial = fInitialText != null && fInitialText.equals(text);
        if (initial)
          fPatternFilter.setPattern(null);
        else
          fPatternFilter.setPattern(text);

        try {
          fViewer.getControl().getParent().setRedraw(false);

          /* Remember Expanded Elements if not yet done */
          if (fExpandedElements == null)
            fExpandedElements = fViewer.getExpandedElements();

          /* Remember Selected Elements if present */
          IStructuredSelection sel = (IStructuredSelection) fViewer.getSelection();
          if (!sel.isEmpty())
            fSelectedElements = sel.toArray();

          /* Refresh Tree */
          BusyIndicator.showWhile(getDisplay(), new Runnable() {
            @Override
            public void run() {
              fViewer.refresh(false);
            }
          });

          /* Restore Expanded Elements and Selection when Filter is disabled */
          if (text.length() == 0) {

            /* Restore Expansion */
            fViewer.collapseAll();
            for (Object element : fExpandedElements) {
              fViewer.setExpandedState(element, true);
            }

            /* Restore Selection */
            if (fSelectedElements != null)
              fViewer.setSelection(new StructuredSelection(fSelectedElements), true);

            /* Clear Fields */
            fExpandedElements = null;
            fSelectedElements = null;
          }

          /*
           * Expand elements one at a time. After each is expanded, check to see
           * if the filter text has been modified. If it has, then cancel the
           * refresh job so the user doesn't have to endure expansion of all the
           * nodes.
           */
          if (text.length() > 0 && !initial) {
            IStructuredContentProvider provider = (IStructuredContentProvider) fViewer.getContentProvider();
            Object[] elements = provider.getElements(fViewer.getInput());
            for (Object element : elements) {
              if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

              fViewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
            }

            /* Make Sure to show the First Item */
            TreeItem[] items = fViewer.getTree().getItems();
            if (items.length > 0)
              fViewer.getTree().showItem(items[0]);

            /* Enable Toolbar to allow resetting the Filter */
            setToolBarVisible(true);
          }

          /* Disable Toolbar - No Filter is currently activated */
          else {
            setToolBarVisible(false);
          }
        }

        /* Done updating the tree - set redraw back to true */
        finally {
          fViewer.getControl().getParent().setRedraw(true);
        }

        return Status.OK_STATUS;
      }
    };
    fRefreshJob.setSystem(true);

    /* Cancel the Job once the Tree got disposed */
    fViewer.getControl().addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
        fRefreshJob.cancel();
      };
    });
  }

  Text getControl() {
    return fFilterText;
  }

  TreeItem getFirstMatchingItem(TreeItem[] items) {
    for (TreeItem element : items) {
      if (fPatternFilter.isLeafMatch(fViewer, element.getData()) && fPatternFilter.isElementSelectable(element.getData()))
        return element;

      return getFirstMatchingItem(element.getItems());
    }
    return null;
  }

  void setFilterText(String string) {
    if (fFilterText != null) {
      fFilterText.setText(string);
      fFilterText.selectAll();
    }
  }

  void textChanged() {
    fRefreshJob.cancel();
    fRefreshJob.schedule(fFilterText.getText().length() != 0 ? FILTER_DELAY : 0);
  }

  void setToolBarVisible(boolean visible) {
    if (fFilterToolBar != null && ((GridData) fFilterToolBar.getControl().getLayoutData()).exclude == visible) {
      ((GridData) fFilterToolBar.getControl().getLayoutData()).exclude = !visible;
      fFilterToolBar.getControl().setVisible(visible);
      fFilterComposite.layout();
    }
  }
}
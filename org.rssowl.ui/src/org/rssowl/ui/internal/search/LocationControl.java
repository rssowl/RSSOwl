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

package org.rssowl.ui.internal.search;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.NewNewsBinAction;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.ModelUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkLabelProvider;
import org.rssowl.ui.internal.views.explorer.BookMarkSorter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The <code>LocationConditionControl</code> is a <code>Composite</code>
 * providing the UI to define Location-Conditions for a Search.
 *
 * @author bpasero
 */
public class LocationControl extends Composite {
  private Mode fMode = Mode.SEARCH_LOCATION;
  private Link fConditionLabel;
  private List<IFolderChild> fSelection;
  private boolean fModified;

  /** Supported Modes for the Control */
  public enum Mode {

    /** Select a Folder Child to Search in */
    SEARCH_LOCATION,

    /** Select a News Bin */
    SELECT_BIN
  }

  /* A Dialog to select Folders and Childs */
  private class FolderChildChooserDialog extends Dialog {
    private CheckboxTreeViewer fViewer;
    private List<IFolderChild> fCheckedElements;
    private IFolderChild fSelectedElement;
    private Set<IFolderChild> fCheckedElementsCache = new HashSet<IFolderChild>();
    private FilteredTree fFilteredTree;
    private Button fSelectAll;

    FolderChildChooserDialog(Shell parentShell, IFolderChild selectedElement, List<IFolderChild> checkedElements) {
      super(parentShell);
      fSelectedElement = selectedElement;
      fCheckedElements = checkedElements;
    }

    List<IFolderChild> getCheckedElements() {
      return fCheckedElements;
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
      Object[] checkedObjects = fCheckedElementsCache.toArray();
      IStructuredSelection selection = new StructuredSelection(checkedObjects);

      List<IFolderChild> entities = ModelUtils.getFoldersBookMarksBins(selection);

      /* Normalize */
      CoreUtils.normalize(entities);

      fCheckedElements = entities;
      fModified = true;

      super.okPressed();
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(LayoutUtils.createGridLayout(1, 10, 10));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      Label label = new Label(composite, SWT.None);
      label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      switch (fMode) {
        case SEARCH_LOCATION:
          label.setText(Messages.LocationControl_CHOOSE_LOCATION_MSG);
          break;

        case SELECT_BIN:
          label.setText(Messages.LocationControl_CHOOSE_BINS_MSG);
          break;
      }

      /* Filter for Filtered Tree */
      final PatternFilter filter = new PatternFilter() {
        @Override
        protected boolean isLeafMatch(Viewer viewer, Object element) {
          if (fMode == Mode.SELECT_BIN && !(element instanceof INewsBin))
            return false;

          String labelText = ((IFolderChild) element).getName();
          if (labelText == null)
            return false;

          return wordMatches(labelText);
        }
      };

      /* Filtered Tree to make it easier to chose an element */
      fFilteredTree = new FilteredTree(composite, SWT.BORDER, filter) {
        @Override
        protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
          fViewer = new CheckboxTreeViewer(parent, SWT.BORDER) {
            @Override
            public void refresh(boolean updateLabels) {
              super.refresh(updateLabels);

              /* Avoid collapsed Tree */
              expandToLevel(fMode == Mode.SELECT_BIN ? AbstractTreeViewer.ALL_LEVELS : 2);

              /* Restore Checked Elements */
              for (IFolderChild child : fCheckedElementsCache) {
                setParentsExpanded(child);
                fViewer.setChecked(child, true);
                setChildsChecked(child, true, true, false);
              }
            }
          };
          fViewer.setAutoExpandLevel(fMode == Mode.SELECT_BIN ? AbstractTreeViewer.ALL_LEVELS : 2);
          fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
          fViewer.getTree().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());
          return fViewer;
        }

        @Override
        protected void updateToolbar(boolean visible) {
          if (filterToolBar != null)
            filterToolBar.getControl().setEnabled(visible);
        }

        @Override
        protected Composite createFilterControls(Composite parent) {
          Composite filterControls = super.createFilterControls(parent);
          if (filterToolBar != null) {
            filterToolBar.getControl().setVisible(true);
            filterToolBar.getControl().setEnabled(false);
          }
          return filterControls;
        }
      };

      fFilteredTree.setInitialText(""); //$NON-NLS-1$
      if (fMode == Mode.SEARCH_LOCATION) {
        fFilteredTree.getFilterControl().setMessage(Messages.LocationControl_FILTER_LOCATIONS);
        OwlUI.makeAccessible(fFilteredTree.getFilterControl(), Messages.LocationControl_FILTER_LOCATIONS);
      } else {
        fFilteredTree.getFilterControl().setMessage(Messages.LocationControl_FILTER_BINS);
        OwlUI.makeAccessible(fFilteredTree.getFilterControl(), Messages.LocationControl_FILTER_BINS);
      }
      fFilteredTree.getViewer().getControl().setFocus();

      /* Filter when Typing into Tree */
      fFilteredTree.getViewer().getControl().addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.character > 0x20) {
            String character = String.valueOf(e.character);
            Text text = fFilteredTree.getFilterControl();
            text.setFocus();
            text.setText(character);
            text.setSelection(1);
            filter.setPattern(character);

            /* Consume the Event */
            e.doit = false;
          }
        }
      });

      /* Handle some UI changes based on filter enabled or not */
      fFilteredTree.getFilterControl().addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          boolean isFiltered = StringUtils.isSet(fFilteredTree.getFilterControl().getText());

          /* "Select All" enablement */
          if (fSelectAll != null)
            fSelectAll.setEnabled(!isFiltered);

          /* Remove all checked elements when filtering */
          if (isFiltered) {
            OwlUI.setAllChecked(fViewer.getTree(), false);
            cacheAll(false);
          }
        }
      });

      int viewerHeight = fViewer.getTree().getItemHeight() * 20 + 12;
      ((GridData) composite.getLayoutData()).heightHint = viewerHeight;

      /* Sort by Name if set so */
      if (Owl.getPreferenceService().getGlobalScope().getBoolean(DefaultPreferences.BE_SORT_BY_NAME)) {
        BookMarkSorter sorter = new BookMarkSorter();
        sorter.setType(BookMarkSorter.Type.SORT_BY_NAME);
        fViewer.setComparator(sorter);
      }

      fViewer.setContentProvider(new ITreeContentProvider() {
        public Object[] getElements(Object inputElement) {
          Collection<IFolder> rootFolders = CoreUtils.loadRootFolders();
          return rootFolders.toArray();
        }

        public Object[] getChildren(Object parentElement) {
          if (parentElement instanceof IFolder) {
            IFolder folder = (IFolder) parentElement;
            return folder.getChildren().toArray();
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
            return !folder.isEmpty();
          }

          return false;
        }

        public void dispose() {}

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
      });

      fViewer.setLabelProvider(new BookMarkLabelProvider(false, true) {
        @Override
        public void update(ViewerCell cell) {
          super.update(cell);

          if (fMode == Mode.SELECT_BIN) {
            Object element = cell.getElement();
            if (element instanceof IFolder && !OwlUI.isHighContrast())
              cell.setForeground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY));
          }
        }
      });

      /* Filter out any Search Marks */
      fViewer.addFilter(new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
          switch (fMode) {
            case SEARCH_LOCATION:
              return !(element instanceof ISearchMark);

            case SELECT_BIN:
              if (element instanceof IFolder)
                return containsBin(((IFolder) element).getChildren());
              return !(element instanceof ISearchMark || element instanceof IBookMark);
          }

          return true;
        }

        private boolean containsBin(List<IFolderChild> children) {
          for (IFolderChild child : children) {
            if (child instanceof INewsBin)
              return true;
            else if (child instanceof IFolder && containsBin((((IFolder) child).getChildren())))
              return true;
          }

          return false;
        }
      });

      fViewer.addDoubleClickListener(new IDoubleClickListener() {
        public void doubleClick(DoubleClickEvent event) {
          IStructuredSelection selection = (IStructuredSelection) event.getSelection();
          IFolder folder = selection.getFirstElement() instanceof IFolder ? (IFolder) selection.getFirstElement() : null;

          /* Expand / Collapse Folder */
          if (folder != null && !folder.isEmpty()) {
            boolean expandedState = !fViewer.getExpandedState(folder);
            fViewer.setExpandedState(folder, expandedState);

            if (expandedState && fViewer.getChecked(folder))
              setChildsChecked(folder, true, true, false);
          }
        }
      });

      fViewer.setInput(new Object());

      /* Apply checked elements */
      if (fCheckedElements != null) {
        for (IFolderChild child : fCheckedElements) {
          setParentsExpanded(child);
          cache(child, true);
          fViewer.setChecked(child, true);
          setChildsChecked(child, true, true, true);
        }
      }

      /* Update Checks on Selection */
      fViewer.getTree().addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (e.detail == SWT.CHECK) {
            TreeItem item = (TreeItem) e.item;

            /* Disable selection for Folders if SELECT_BIN */
            if (fMode == Mode.SELECT_BIN && item.getData() instanceof IFolder) {
              e.detail = SWT.NONE;
              e.doit = false;
              item.setChecked(false);
            }

            /* Normal selection behavior otherwise */
            else {
              IFolderChild folderChild = (IFolderChild) item.getData();
              setChildsChecked(folderChild, item.getChecked(), false, true);
              cache(folderChild, item.getChecked());

              if (!item.getChecked())
                setParentsChecked(folderChild, false, true);
            }
          }
        }
      });

      /* Update Checks on Expand */
      fViewer.addTreeListener(new ITreeViewerListener() {
        public void treeExpanded(TreeExpansionEvent event) {
          boolean isChecked = fViewer.getChecked(event.getElement());
          if (isChecked)
            setChildsChecked((IFolderChild) event.getElement(), isChecked, false, false);
        }

        public void treeCollapsed(TreeExpansionEvent event) {}
      });

      /* Select and Show Selection */
      if (fSelectedElement != null) {
        fViewer.setSelection(new StructuredSelection(fSelectedElement));
        fViewer.getTree().showSelection();
      }

      /* Buttons */
      Composite buttonContainer = new Composite(composite, SWT.NONE);
      buttonContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
      buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      /* Select All / Deselect All for Location Lookup */
      if (fMode == Mode.SEARCH_LOCATION) {
        fSelectAll = new Button(buttonContainer, SWT.PUSH);
        fSelectAll.setText(Messages.LocationControl_SELECT_ALL);
        Dialog.applyDialogFont(fSelectAll);
        setButtonLayoutData(fSelectAll);
        fSelectAll.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            OwlUI.setAllChecked(fViewer.getTree(), true);
            cacheAll(true);
          }
        });

        Button deselectAll = new Button(buttonContainer, SWT.PUSH);
        deselectAll.setText(Messages.LocationControl_DESELECT_ALL);
        Dialog.applyDialogFont(deselectAll);
        setButtonLayoutData(deselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            OwlUI.setAllChecked(fViewer.getTree(), false);
            cacheAll(false);
          }
        });
      }

      /* Create Bin for Bin Selection */
      else {
        Button createBin = new Button(buttonContainer, SWT.PUSH);
        createBin.setText(Messages.LocationControl_NEW_NEWSBIN);
        Dialog.applyDialogFont(createBin);
        setButtonLayoutData(createBin);
        createBin.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            onCreateNewsBin();
          }
        });
      }

      applyDialogFont(composite);

      return composite;
    }

    private void onCreateNewsBin() {
      IFolderChild selectedFolderChild = null;
      IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
      if (!selection.isEmpty()) {
        Object element = selection.getFirstElement();
        if (element instanceof IFolderChild)
          selectedFolderChild = (IFolderChild) element;
      }

      IFolder folder = (IFolder) ((selectedFolderChild instanceof IFolder) ? selectedFolderChild : null);
      IMark position = null;
      if (folder == null && selectedFolderChild != null && selectedFolderChild instanceof IMark) {
        folder = selectedFolderChild.getParent();
        position = (IMark) selectedFolderChild;
      }

      NewNewsBinAction action = new NewNewsBinAction(getShell(), folder, position);
      action.run(null);
      INewsBin newsbin = action.getNewsbin();
      if (newsbin != null) {
        fFilteredTree.getPatternFilter().setPattern(""); //$NON-NLS-1$
        fFilteredTree.getFilterControl().setText(""); //$NON-NLS-1$
        fViewer.refresh();
        fViewer.expandAll();
        fViewer.setSelection(new StructuredSelection(newsbin), true);
        fViewer.setChecked(newsbin, true);
        fCheckedElementsCache.add(newsbin);
      }
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createButtonBar(Composite parent) {

      /* Separator */
      Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
      sep.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

      return super.createButtonBar(parent);
    }

    private void cache(IFolderChild child, boolean checked) {
      if (checked)
        fCheckedElementsCache.add(child);
      else
        fCheckedElementsCache.remove(child);
    }

    private void cacheAll(boolean checked) {
      Tree tree = fViewer.getTree();
      cacheAll(tree.getItems(), checked);
    }

    private void cacheAll(TreeItem[] items, boolean checked) {
      for (TreeItem item : items) {
        if (item.getData() != null) { //Could not yet be resolved!
          cache((IFolderChild) item.getData(), checked);
          cacheAll(item.getItems(), checked);
        }
      }
    }

    private void setChildsChecked(IFolderChild folderChild, boolean checked, boolean onlyExpanded, boolean cache) {
      if (folderChild instanceof IFolder && (!onlyExpanded || fViewer.getExpandedState(folderChild))) {
        List<IFolderChild> children = ((IFolder) folderChild).getChildren();
        for (IFolderChild child : children) {
          if (cache)
            cache(child, checked);
          fViewer.setChecked(child, checked);
          setChildsChecked(child, checked, onlyExpanded, cache);
        }
      }
    }

    private void setParentsChecked(IFolderChild folderChild, boolean checked, boolean cache) {
      IFolder parent = folderChild.getParent();
      if (parent != null) {
        if (cache)
          cache(parent, checked);
        fViewer.setChecked(parent, checked);
        setParentsChecked(parent, checked, cache);
      }
    }

    private void setParentsExpanded(IFolderChild folderChild) {
      IFolder parent = folderChild.getParent();
      if (parent != null) {
        fViewer.setExpandedState(parent, true);
        setParentsExpanded(parent);
      }
    }

    /*
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      switch (fMode) {
        case SEARCH_LOCATION:
          newShell.setText(Messages.LocationControl_CHOOSE_LOCATION);
          break;
        case SELECT_BIN:
          newShell.setText(Messages.LocationControl_CHOOSE_BINS);
          break;
      }
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
     */
    @Override
    protected void initializeBounds() {
      super.initializeBounds();
      Point bestSize = getShell().computeSize(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT);
      getShell().setSize(bestSize);
      LayoutUtils.positionShell(getShell());
    }
  }

  /**
   * @param parent
   * @param style
   */
  public LocationControl(Composite parent, int style) {
    this(parent, style, Mode.SEARCH_LOCATION);
  }

  /**
   * @param parent
   * @param style
   * @param mode
   */
  public LocationControl(Composite parent, int style, Mode mode) {
    super(parent, style);

    fMode = mode;
    initComponents();
  }

  /**
   * @return the selected locations
   */
  public Long[][] getSelection() {
    return fSelection != null ? ModelUtils.toPrimitive(fSelection) : null;
  }

  /**
   * @param selection
   */
  public void select(Long[][] selection) {
    fSelection = CoreUtils.toEntities(selection);
    fConditionLabel.setText(getLabel(fSelection));
  }

  /**
   * @return <code>true</code> if the location was modified by the user and
   * <code>false</code> otherwise.
   */
  public boolean isModified() {
    return fModified;
  }

  private void initComponents() {

    /* Apply Gridlayout */
    setLayout(LayoutUtils.createGridLayout(1, 5, 1));

    fConditionLabel = new Link(this, SWT.NONE);
    fConditionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    fConditionLabel.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IFolderChild selectedChild = null;
        if (e.text != null && e.text.length() > 0)
          selectedChild = fSelection.get(Integer.valueOf(e.text));

        onChangeCondition(selectedChild);
      }
    });

    fConditionLabel.setText(getLabel(fSelection));
  }

  private void onChangeCondition(IFolderChild selectedChild) {
    FolderChildChooserDialog dialog = new FolderChildChooserDialog(getShell(), selectedChild, fSelection);
    if (dialog.open() == IDialogConstants.OK_ID) {
      List<IFolderChild> checkedElements = dialog.getCheckedElements();
      fSelection = checkedElements;
      fConditionLabel.setText(getLabel(fSelection));
      notifyListeners(SWT.Modify, new Event());

      /* Link might require more space now */
      getShell().layout(true, true);
    }
  }

  /**
   * @return the label to show when no location is selected.
   */
  protected String getDefaultLabel() {
    if (fMode == Mode.SELECT_BIN)
      return Messages.LocationControl_CHOOSE_BINS_LABEL;

    return Messages.LocationControl_CHOOSE_LOCATION_LABEL;
  }

  private String getLabel(List<IFolderChild> entities) {
    if (entities == null || entities.size() == 0) {
      return "<a href=\"\">" + getDefaultLabel() + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    StringBuilder strB = new StringBuilder();
    for (int i = 0; i < entities.size(); i++) {
      strB.append("<a href=\"" + i + "\">").append(entities.get(i).getName()).append("</a>").append(", "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    if (strB.length() > 0)
      strB.delete(strB.length() - 2, strB.length());

    return strB.toString();
  }

  /**
   * @return a {@link ISearchCondition} from the selection of the control or
   * <code>null</code> if none.
   */
  public ISearchCondition toScopeCondition() {
    ISearchCondition condition = null;
    Long[][] selection = getSelection();
    if (selection != null) {
      IModelFactory factory = Owl.getModelFactory();
      ISearchField field = factory.createSearchField(INews.LOCATION, INews.class.getName());
      condition = factory.createSearchCondition(field, SearchSpecifier.SCOPE, selection);
    }

    return condition;
  }
}
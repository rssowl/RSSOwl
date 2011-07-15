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

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.views.explorer.BookMarkLabelProvider;
import org.rssowl.ui.internal.views.explorer.BookMarkSorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link CheckboxTreeViewer} listing all {@link IFolderChild} for selection.
 *
 * @author bpasero
 */
public class FolderChildCheckboxTree {
  private Composite fParent;
  private CheckboxTreeViewer fViewer;
  private boolean fFlat;
  private final boolean fFullLabels;

  /**
   * @param parent the parent for the {@link CheckboxTreeViewer}.
   */
  public FolderChildCheckboxTree(Composite parent) {
    this(parent, false);
  }

  /**
   * @param parent the parent for the {@link CheckboxTreeViewer}.
   * @param fullLabels if <code>true</code> indicate unread count and sticky
   * news.
   */
  public FolderChildCheckboxTree(Composite parent, boolean fullLabels) {
    fParent = parent;
    fFullLabels = fullLabels;
    initComponents();
  }

  /**
   * @param checked <code>true</code> to check all elements and
   * <code>false</code> otherwise.
   */
  public void setAllChecked(boolean checked) {
    OwlUI.setAllChecked(fViewer.getTree(), checked);
  }

  /**
   * @param flat if <code>true</code> will show the {@link INewsMark} in a flat
   * list instead of a hierarchy of {@link IFolder}, false otherwise.
   */
  public void setFlat(boolean flat) {
    fFlat = flat;
    fViewer.refresh();
  }

  /**
   * @return the {@link CheckboxTreeViewer} used for this control.
   */
  public CheckboxTreeViewer getViewer() {
    return fViewer;
  }

  /**
   * @return a {@link List} of the selected {@link IFolderChild} from this
   * control.
   */
  public List<IFolderChild> getCheckedElements() {
    List<IFolderChild> folderChilds = new ArrayList<IFolderChild>();
    Object[] checkedElements = fViewer.getCheckedElements();
    for (Object checkedElement : checkedElements) {
      if (checkedElement instanceof IFolderChild)
        folderChilds.add((IFolderChild) checkedElement);
    }

    return folderChilds;
  }

  private void initComponents() {

    /* Viewer to select particular Folders/Marks */
    fViewer = new CheckboxTreeViewer(fParent, SWT.BORDER | SWT.MULTI);
    fViewer.setAutoExpandLevel(2);
    fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    ((GridData) fViewer.getTree().getLayoutData()).heightHint = 80;
    fViewer.getTree().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    /* Sort by Name if set so */
    if (Owl.getPreferenceService().getGlobalScope().getBoolean(DefaultPreferences.BE_SORT_BY_NAME)) {
      BookMarkSorter sorter = new BookMarkSorter();
      sorter.setType(BookMarkSorter.Type.SORT_BY_NAME);
      fViewer.setComparator(sorter);
    }

    /* ContentProvider */
    fViewer.setContentProvider(new ITreeContentProvider() {
      public Object[] getElements(Object inputElement) {
        if (fFlat)
          return getNewsMarks((Collection<?>) inputElement).toArray();

        return ((Collection<?>) inputElement).toArray();
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

    /* LabelProvider */
    boolean usedInDialog = (fParent.getShell().getParent() != null);
    fViewer.setLabelProvider(new BookMarkLabelProvider(fFullLabels, usedInDialog));

    /* Listen on Doubleclick */
    fViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        onDoubleClick(event);
      }
    });

    /* Update Checks on Selection */
    fViewer.getTree().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onSelect(e);
      }
    });

    /* Update Checks on Expand */
    fViewer.addTreeListener(new ITreeViewerListener() {
      public void treeExpanded(TreeExpansionEvent event) {
        onExpand(event);
      }

      public void treeCollapsed(TreeExpansionEvent event) {}
    });
  }

  private List<INewsMark> getNewsMarks(Collection<?> input) {
    List<INewsMark> childs = new ArrayList<INewsMark>();

    for (Object element : input) {
      if (element instanceof IFolder)
        getNewsMarks(childs, (IFolder) element);
      else if (element instanceof INewsMark)
        childs.add((INewsMark) element);
    }

    return childs;
  }

  private void getNewsMarks(List<INewsMark> childs, IFolder folder) {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {
      if (child instanceof IFolder)
        getNewsMarks(childs, (IFolder) child);
      else if (child instanceof INewsMark)
        childs.add((INewsMark) child);
    }
  }

  private void onSelect(SelectionEvent e) {
    if (e.detail == SWT.CHECK) {
      TreeItem item = (TreeItem) e.item;
      IFolderChild child = (IFolderChild) item.getData();

      /* Update Childs according to Checked State */
      setChildsChecked(child, item.getChecked());

      /* Set Parents un-checked */
      if (!item.getChecked())
        setParentsChecked(child, false);

      /* Check if Parents are checked now */
      else
        updateParentsChecked(child);
    }
  }

  private void onExpand(TreeExpansionEvent event) {
    boolean isChecked = fViewer.getChecked(event.getElement());

    /* Set Childs Checked if Element is Checked */
    if (isChecked)
      setChildsChecked((IFolderChild) event.getElement(), true);
  }

  private void onDoubleClick(DoubleClickEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    IFolder folder = selection.getFirstElement() instanceof IFolder ? (IFolder) selection.getFirstElement() : null;

    /* Expand / Collapse Folder */
    if (folder != null && !folder.isEmpty()) {
      boolean expandedState = !fViewer.getExpandedState(folder);
      fViewer.setExpandedState(folder, expandedState);

      /* Set Childs Checked if Element is Checked */
      if (expandedState && fViewer.getChecked(folder))
        setChildsChecked(folder, true);
    }
  }

  private void setChildsChecked(IFolderChild folderChild, boolean checked) {
    if (folderChild instanceof IFolder) {
      List<IFolderChild> children = ((IFolder) folderChild).getChildren();
      for (IFolderChild child : children) {
        fViewer.setChecked(child, checked);
        setChildsChecked(child, checked);
      }
    }
  }

  private void setParentsChecked(IFolderChild folderChild, boolean checked) {
    IFolder parent = folderChild.getParent();
    if (parent != null) {
      fViewer.setChecked(parent, checked);
      setParentsChecked(parent, checked);
    }
  }

  private void updateParentsChecked(IFolderChild folderChild) {
    IFolder parent = folderChild.getParent();
    if (parent != null) {
      List<IFolderChild> children = parent.getChildren();
      for (IFolderChild child : children) {
        if (!fViewer.getChecked(child) && !isFiltered(child))
          return;
      }

      fViewer.setChecked(parent, true);
      updateParentsChecked(parent);
    }
  }

  private boolean isFiltered(IFolderChild child) {
    ViewerFilter[] filters = fViewer.getFilters();
    for (ViewerFilter filter : filters) {
      if (!filter.select(fViewer, child.getParent(), child))
        return true;

    }
    return false;
  }
}
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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.rssowl.ui.internal.util.TreeItemAdapter;
import org.rssowl.ui.internal.util.ViewerOpenStrategy;

import java.util.List;

/**
 * A Subclass of <code>TreeViewer</code> to display Folders and all kinds of
 * Marks in the "Feeds" view.
 *
 * @author bpasero
 */
public class BookMarkViewer extends TreeViewer {
  private final BookMarkExplorer fExplorer;
  private ListenerList fOpenListeners = new ListenerList();
  private ViewerOpenStrategy fViewerOpenStrategy;

  /**
   * @param explorer
   * @param parent
   * @param style
   */
  public BookMarkViewer(BookMarkExplorer explorer, Composite parent, int style) {
    super(parent, style);
    fExplorer = explorer;
  }

  /*
   * @see org.eclipse.jface.viewers.TreeViewer#createChildren(org.eclipse.swt.widgets.Widget)
   */
  @Override
  public void createChildren(Widget widget) {
    super.createChildren(widget);
  }

  /*
   * @see org.eclipse.jface.viewers.StructuredViewer#refresh(boolean)
   */
  @Override
  public void refresh(boolean updateLabels) {
    getControl().getParent().setRedraw(false);
    try {
      super.refresh(updateLabels);
    } finally {
      getControl().getParent().setRedraw(true);
    }
  }

  /*
   * @see org.eclipse.jface.viewers.ColumnViewer#refresh(java.lang.Object)
   */
  @Override
  public void refresh(Object element) {
    super.refresh(element);

    /* Avoid restoring expanded elements on refresh() */
    if (element == getRoot())
      return;

    /* TODO Revisit later */
    fExplorer.restoreExpandedElements();
  }

  /*
   * @see org.eclipse.jface.viewers.StructuredViewer#refresh(java.lang.Object,
   * boolean)
   */
  @Override
  public void refresh(Object element, boolean updateLabels) {
    super.refresh(element, updateLabels);

    /* TODO Revisit later */
    fExplorer.restoreExpandedElements();
  }

  /*
   * @see org.eclipse.jface.viewers.AbstractTreeViewer#remove(java.lang.Object[])
   */
  @Override
  public void remove(final Object[] elements) {
    updateSelectionAfterDelete(new Runnable() {
      public void run() {
        internalRemove(elements);
      }
    });
  }

  /*
   * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void setSelection(ISelection selection) {
    super.setSelection(selection);
    if (fViewerOpenStrategy != null)
      fViewerOpenStrategy.clearExpandFlag(); // See Bug 164372
  }

  /*
   * @see org.eclipse.jface.viewers.StructuredViewer#setSelection(org.eclipse.jface.viewers.ISelection,
   * boolean)
   */
  @Override
  public void setSelection(ISelection selection, boolean reveal) {
    super.setSelection(selection, reveal);
    if (fViewerOpenStrategy != null)
      fViewerOpenStrategy.clearExpandFlag(); // See Bug 164372
  }

  /*
   * @see org.eclipse.jface.viewers.TreeViewer#setSelection(java.util.List)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void setSelection(List items) {
    super.setSelection(items);
    if (fViewerOpenStrategy != null)
      fViewerOpenStrategy.clearExpandFlag(); // See Bug 164372
  }

  /*
   * @see org.eclipse.jface.viewers.AbstractTreeViewer#setSelectionToWidget(org.eclipse.jface.viewers.ISelection, boolean)
   */
  @Override
  protected void setSelectionToWidget(ISelection selection, boolean reveal) {
    super.setSelectionToWidget(selection, reveal);
    if (fViewerOpenStrategy != null)
      fViewerOpenStrategy.clearExpandFlag(); // See Bug 164372
  }

  /*
   * @see org.eclipse.jface.viewers.AbstractTreeViewer#setSelectionToWidget(java.util.List, boolean)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void setSelectionToWidget(List v, boolean reveal) {
    super.setSelectionToWidget(v, reveal);
    if (fViewerOpenStrategy != null)
      fViewerOpenStrategy.clearExpandFlag(); // See Bug 164372
  }

  /*
   * @see org.eclipse.jface.viewers.TreeViewer#hookControl(org.eclipse.swt.widgets.Control)
   */
  @Override
  protected void hookControl(Control control) {
    super.hookControl(control);

    /* Add a ViewerOpenStrategy */
    fViewerOpenStrategy = new ViewerOpenStrategy(control);
    fViewerOpenStrategy.addOpenListener(new IOpenEventListener() {
      public void handleOpen(SelectionEvent e) {
        internalHandleOpen();
      }
    });
  }

  /*
   * Overrides the open-listener to work with the ViewerOpenStrategy.
   *
   * @see org.eclipse.jface.viewers.StructuredViewer#addOpenListener(org.eclipse.jface.viewers.IOpenListener)
   */
  @Override
  public void addOpenListener(IOpenListener listener) {
    fOpenListeners.add(listener);
  }

  private void internalHandleOpen() {
    Control control = getControl();
    if (control != null && !control.isDisposed()) {
      ISelection selection = getSelection();
      internalFireOpen(new OpenEvent(this, selection));
    }
  }

  private void internalFireOpen(final OpenEvent event) {
    Object[] listeners = fOpenListeners.getListeners();
    for (int i = 0; i < listeners.length; ++i) {
      final IOpenListener listener = (IOpenListener) listeners[i];
      SafeRunnable.run(new SafeRunnable() {
        public void run() {
          listener.open(event);
        }
      });
    }
  }

  private void updateSelectionAfterDelete(Runnable runnable) {
    Tree tree = (Tree) getControl();
    IStructuredSelection selection = (IStructuredSelection) getSelection();

    /* Nothing to do, since no selection */
    if (selection.isEmpty()) {
      runnable.run();
      return;
    }

    /* Look for the minimal Index of all selected Elements */
    int minSelectedIndex = Integer.MAX_VALUE;
    TreeItemAdapter parentOfMinSelected = new TreeItemAdapter(tree);

    /* For each selected Element */
    Object[] selectedElements = selection.toArray();
    for (Object selectedElement : selectedElements) {
      Widget widget = findItem(selectedElement);
      if (widget instanceof TreeItem) {
        TreeItem item = (TreeItem) widget;
        TreeItemAdapter parent = new TreeItemAdapter(item).getParent();

        int index = parent.indexOf(item);
        minSelectedIndex = Math.min(minSelectedIndex, index);
        if (index == minSelectedIndex)
          parentOfMinSelected.setItem(parent.getItem());
      }
    }

    /* Perform Deletion */
    runnable.run();

    Object data = null;

    /* Parent itself has been deleted */
    if (parentOfMinSelected.getItem().isDisposed())
      return;

    /* Restore selection to next Element */
    if (parentOfMinSelected.getItemCount() > minSelectedIndex)
      data = parentOfMinSelected.getItem(minSelectedIndex).getData();

    /* Restore selection to last Element */
    else if (parentOfMinSelected.getItemCount() > 0)
      data = parentOfMinSelected.getItem(parentOfMinSelected.getItemCount() - 1).getData();

    /* Restore selection on actual Element */
    else
      data = parentOfMinSelected.getItem().getData();

    /* Apply selection */
    if (data != null) {
      IStructuredSelection newSelection = new StructuredSelection(data);
      setSelection(newSelection);
    }
  }
}
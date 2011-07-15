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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.rssowl.core.util.ITreeNode;
import org.rssowl.ui.internal.views.explorer.BookMarkViewer;

/**
 * A concrete implementation of <code>ITreeNode</code> working on SWT-Tree and
 * TreeItem.
 * 
 * @author bpasero
 */
public class WidgetTreeNode implements ITreeNode {
  private TreeItem fItem;
  private Tree fTree;
  private TreeViewer fViewer;

  /**
   * Create a new ITreeNode wrapping a Tree.
   * 
   * @param tree The Tree to wrap inside this helper.
   * @param viewer The Viewer that manages the given Tree.
   */
  public WidgetTreeNode(Tree tree, TreeViewer viewer) {
    fTree = tree;
    fViewer = viewer;
  }

  /**
   * Create a new ITreeNode wrapping a TreeItem.
   * 
   * @param item The Item to wrap inside this helper.
   * @param viewer The Viewer that manages the given TreeItem.
   */
  public WidgetTreeNode(TreeItem item, TreeViewer viewer) {
    fItem = item;
    fViewer = viewer;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getFirstChild()
   */
  public ITreeNode getFirstChild() {

    /* Retrieve from Tree */
    if (isSet(fTree) && fTree.getItemCount() > 0)
      return new WidgetTreeNode(fTree.getItem(0), fViewer);

    /* Retrieve from TreeItem */
    if (isSet(fItem) && fItem.getItemCount() > 0)
      return new WidgetTreeNode(fItem.getItem(0), fViewer);

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getLastChild()
   */
  public ITreeNode getLastChild() {

    /* Retrieve from Tree */
    if (isSet(fTree)) {
      int itemCount = fTree.getItemCount();
      if (itemCount > 0)
        return new WidgetTreeNode(fTree.getItem(itemCount - 1), fViewer);
    }

    /* Retrieve from TreeItem */
    if (isSet(fItem)) {
      int itemCount = fItem.getItemCount();
      if (itemCount > 0)
        return new WidgetTreeNode(fItem.getItem(itemCount - 1), fViewer);
    }

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getNextSibling()
   */
  public ITreeNode getNextSibling() {

    /* Require a Tree-Item here */
    if (!isSet(fItem))
      return null;

    TreeItem parent = fItem.getParentItem();

    /* Item is not Root-Leveld */
    if (isSet(parent)) {
      int index = parent.indexOf(fItem);

      if (parent.getItemCount() > index + 1)
        return new WidgetTreeNode(parent.getItem(index + 1), fViewer);

      return null;
    }

    /* Item is Root-Leveld */
    Tree tree = fItem.getParent();
    int index = tree.indexOf(fItem);

    if (tree.getItemCount() > index + 1)
      return new WidgetTreeNode(tree.getItem(index + 1), fViewer);

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getParent()
   */
  public ITreeNode getParent() {
    if (isSet(fTree) || !isSet(fItem))
      return null;

    return new WidgetTreeNode(fItem.getParentItem(), fViewer);
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getPreviousSibling()
   */
  public ITreeNode getPreviousSibling() {

    /* Require a Tree-Item here */
    if (!isSet(fItem))
      return null;

    TreeItem parent = fItem.getParentItem();

    /* Item is not Root-Leveld */
    if (isSet(parent)) {
      int index = parent.indexOf(fItem);

      if (index > 0)
        return new WidgetTreeNode(parent.getItem(index - 1), fViewer);

      return null;
    }

    /* Item is Root-Leveld */
    Tree tree = fItem.getParent();
    int index = tree.indexOf(fItem);

    if (index > 0)
      return new WidgetTreeNode(tree.getItem(index - 1), fViewer);

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#hasChildren()
   */
  public boolean hasChildren() {

    /* Ask Tree */
    if (isSet(fTree))
      return fTree.getItemCount() > 0;

    /* Ask Item */
    if (isSet(fItem)) {

      /* Make sure that children of this branch are populated (TODO Hack!) */
      if (fViewer instanceof BookMarkViewer && !fItem.getExpanded() && fItem.getItemCount() == 1)
        ((BookMarkViewer) fViewer).createChildren(fItem);

      return fItem.getItemCount() > 0;
    }

    return false;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getData()
   */
  public Object getData() {

    /* Ask Tree */
    if (isSet(fTree))
      return fTree.getData();

    /* Ask Item */
    if (isSet(fItem))
      return fItem.getData();

    return null;
  }

  private boolean isSet(Widget item) {
    return item != null && !item.isDisposed();
  }
}
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A wrapper around a Tree or a TreeItem, with support of Methods that can run
 * on both Widgets.
 *
 * @author bpasero
 */
public class TreeItemAdapter {
  private Tree fTree;
  private TreeItem fTreeItem;

  /**
   * @param tree
   */
  public TreeItemAdapter(Tree tree) {
    Assert.isNotNull(tree);
    fTree = tree;
  }

  /**
   * @param item
   */
  public TreeItemAdapter(TreeItem item) {
    Assert.isNotNull(item);
    fTreeItem = item;
  }

  /**
   * @return The number of items contained in this Tree or TreeItem.
   */
  public int getItemCount() {
    if (fTree != null)
      return fTree.getItemCount();

    return fTreeItem.getItemCount();
  }

  /**
   * @param index
   * @return The TreeItem at the given Index.
   */
  public TreeItem getItem(int index) {
    if (fTree != null)
      return fTree.getItem(index);

    return fTreeItem.getItem(index);
  }

  /**
   * @return The Parent of this Adapter.
   */
  public TreeItemAdapter getParent() {
    if (fTree != null)
      return this;

    TreeItem parentItem = fTreeItem.getParentItem();
    if (parentItem != null)
      return new TreeItemAdapter(parentItem);

    return new TreeItemAdapter(fTreeItem.getParent());
  }

  /**
   * @param item
   * @return The Index of the given Item.
   */
  public int indexOf(TreeItem item) {
    if (fTree != null)
      return fTree.indexOf(item);

    return fTreeItem.indexOf(item);
  }

  /**
   * @return The underlying Widget of this Adapter.
   */
  public Widget getItem() {
    if (fTree != null)
      return fTree;

    return fTreeItem;
  }

  /**
   * @param item
   */
  public void setItem(Widget item) {
    if (item instanceof Tree)
      setTree((Tree) item);
    else if (item instanceof TreeItem)
      setTreeItem((TreeItem) item);
  }

  private void setTree(Tree tree) {
    fTree = tree;
    fTreeItem = null;
  }

  private void setTreeItem(TreeItem treeItem) {
    fTreeItem = treeItem;
    fTree = null;
  }
}
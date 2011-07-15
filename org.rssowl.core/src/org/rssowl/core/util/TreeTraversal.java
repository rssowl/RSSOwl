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

package org.rssowl.core.util;

import org.eclipse.core.runtime.Assert;

/**
 * An abstract helper-class to perform navigation in a Tree, specified by a
 * <code>ITreeNode</code> as starting point. Implementors have to override the
 * <code>select(ITreeNode)</code>-Method allowing to filter certain Nodes
 * from the navigation.
 * 
 * @author bpasero
 */
public abstract class TreeTraversal {
  private ITreeNode fNode;

  /**
   * @param startingNode The entry-node of this traversal.
   */
  protected TreeTraversal(ITreeNode startingNode) {
    Assert.isNotNull(startingNode);
    fNode = startingNode;
  }

  /**
   * This method is called during navigation and allows to define, wether a
   * certain Node should be returned as a result or not.
   * 
   * @param node The current Node in the navigation.
   * @return Implementors should return <code>TRUE</code> to respect this Node
   * as a result and <code>FALSE</code> otherwise.
   */
  public abstract boolean select(ITreeNode node);

  /**
   * Returns the next Node starting from the initially given Node using a
   * depth-first-Search or <code>NULL</code> if no next Node.
   * 
   * @return the next Node starting from the initially given Node using a
   * depth-first-Search or <code>NULL</code> if no next Node.
   */
  public ITreeNode nextNode() {

    /* Perform Navigation until finished */
    do {

      /* Branch - Directly take first Child */
      if (fNode.hasChildren()) {
        fNode = fNode.getFirstChild();
      }

      /* Leaf - Find the next sibling */
      else {

        /* Walk up until next sibling found */
        while (fNode != null && fNode.getNextSibling() == null)
          fNode = fNode.getParent();

        /* Go to next Sibling */
        if (fNode != null)
          fNode = fNode.getNextSibling();
      }

      /* Navigation complete */
      if (fNode != null && select(fNode))
        return fNode;
    }

    /* Traverse the Tree */
    while (fNode != null);

    /* No next Node */
    return null;
  }

  /**
   * Returns the previous Node starting from the initially given Node using a
   * depth-first-Search or <code>NULL</code> if no previous Node.
   * 
   * @return the previous Node starting from the initially given Node using a
   * depth-first-Search or <code>NULL</code> if no previous Node.
   */
  public ITreeNode previousNode() {

    /* Perform Navigation until finished */
    do {

      /* Walk up until previous sibling found */
      while (fNode != null && fNode.getPreviousSibling() == null)
        fNode = fNode.getParent();

      /* Go to previous Sibling */
      if (fNode != null) {
        fNode = fNode.getPreviousSibling();

        /* Take last Child of previous Sibling */
        if (fNode != null && fNode.hasChildren()) {
          while (fNode.getLastChild().hasChildren())
            fNode = fNode.getLastChild();

          fNode = fNode.getLastChild();
        }

        /* Navigation complete */
        if (fNode != null && select(fNode))
          return fNode;
      }
    }

    /* Traverse the Tree */
    while (fNode != null);

    /* No previous Node */
    return null;
  }
}
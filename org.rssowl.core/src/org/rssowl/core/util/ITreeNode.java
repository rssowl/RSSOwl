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

/**
 * Implementors of <code>ITreeNode</code> provide methods to retrieve certain
 * items from an underlying Tree-Structure. This Interface can be used in
 * Algorithms that are working on Trees, like the Tree-Traversal-Algorithm.
 * 
 * @author bpasero
 */
public interface ITreeNode {

  /**
   * Get the parent Node of this Node or <code>NULL</code> if no parent at
   * all.
   * 
   * @return Returns the parent Node of this Node or <code>NULL</code> if no
   * parent at all.
   */
  ITreeNode getParent();

  /**
   * Check wether this Node contains any child Nodes.
   * 
   * @return Returns <code>TRUE</code> if this Node contains any child Nodes,
   * <code>FALSE</code> otherwise.
   */
  boolean hasChildren();

  /**
   * Get the first child of this Node or <code>NULL</code> if no childs at
   * all.
   * 
   * @return Returns first child of this Node or <code>NULL</code> if no
   * childs at all.
   */
  ITreeNode getFirstChild();

  /**
   * Get the last child of this Node or <code>NULL</code> if no childs at all.
   * 
   * @return Returns the last child of this Node or <code>NULL</code> if no
   * childs at all.
   */
  ITreeNode getLastChild();

  /**
   * Get the next Sibling of this Node or <code>NULL</code> if no next Sibling
   * at all.
   * 
   * @return Returns the next Sibling of this Mode or <code>NULL</code> if no
   * next Sibling at all.
   */
  ITreeNode getNextSibling();

  /**
   * Get the previous Sibling of this Node or <code>NULL</code> if no previous
   * Sibling at all.
   * 
   * @return Returns the previous Sibling of this Mode or <code>NULL</code> if
   * no previous Sibling at all.
   */
  ITreeNode getPreviousSibling();

  /**
   * Get the Data this Node is wrapping around. Usually this is some kind of
   * Model from the domain.
   * 
   * @return the Data this Node is wrapping around.
   */
  Object getData();
}
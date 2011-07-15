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
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;

import java.util.List;

/**
 * A concrete implementation of <code>ITreeNode</code> working on
 * <code>IFolder</code> and <code>IMark</code>.
 *
 * @author bpasero
 */
public class ModelTreeNode implements ITreeNode {
  private IFolder fFolder;
  private IMark fMark;

  /**
   * @param startFolder The Folder to start from.
   */
  public ModelTreeNode(IFolder startFolder) {
    Assert.isNotNull(startFolder);
    fFolder = startFolder;
  }

  /**
   * @param startMark The Mark to start from.
   */
  public ModelTreeNode(IMark startMark) {
    Assert.isNotNull(startMark);
    fMark = startMark;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getFirstChild()
   */
  public ITreeNode getFirstChild() {

    /* Return from Folder */
    if (fFolder != null) {
      List<IFolder> folders = fFolder.getFolders();
      if (folders.size() > 0)
        return new ModelTreeNode(folders.get(0));

      List<IMark> marks = fFolder.getMarks();
      if (marks.size() > 0)
        return new ModelTreeNode(marks.get(0));
    }

    /* Mark has no Childs */
    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getLastChild()
   */
  public ITreeNode getLastChild() {

    /* Return from Folder */
    if (fFolder != null) {
      List<IFolder> folders = fFolder.getFolders();
      if (folders.size() > 0)
        return new ModelTreeNode(folders.get(folders.size() - 1));

      List<IMark> marks = fFolder.getMarks();
      if (marks.size() > 0)
        return new ModelTreeNode(marks.get(marks.size() - 1));
    }

    /* Mark has no Childs */
    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getNextSibling()
   */
  public ITreeNode getNextSibling() {

    /* Get Parent */
    IFolder parent = (fFolder != null ? fFolder.getParent() : fMark.getParent());

    /* Item is not Root-Leveld */
    if (parent != null) {

      /* Next Sibling of Folder */
      if (fFolder != null) {
        List<IFolder> folders = parent.getFolders();
        int index = folders.indexOf(fFolder);

        if (folders.size() > index + 1)
          return new ModelTreeNode(folders.get(index + 1));

        /* Marks follow Folders, so check for them being available */
        List<IMark> marks = parent.getMarks();
        if (!marks.isEmpty())
          return new ModelTreeNode(marks.get(0));
      }

      /* Next Sibling of Mark */
      else if (fMark != null) {
        List<IMark> marks = parent.getMarks();
        int index = marks.indexOf(fMark);

        if (marks.size() > index + 1)
          return new ModelTreeNode(marks.get(index + 1));
      }
    }

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getParent()
   */
  public ITreeNode getParent() {

    /* Obtain from Folder */
    if (fFolder != null && fFolder.getParent() != null)
      return new ModelTreeNode(fFolder.getParent());

    /* Obtain from Mark */
    if (fMark != null)
      return new ModelTreeNode(fMark.getParent());

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getNextSibling()
   */
  public ITreeNode getPreviousSibling() {

    /* Get Parent */
    IFolder parent = (fFolder != null ? fFolder.getParent() : fMark.getParent());

    /* Item is not Root-Leveld */
    if (parent != null) {

      /* Previous Sibling of Folder */
      if (fFolder != null) {
        List<IFolder> folders = parent.getFolders();
        int index = folders.indexOf(fFolder);

        if (index > 0)
          return new ModelTreeNode(folders.get(index - 1));
      }

      /* Previous Sibling of Mark */
      else if (fMark != null) {
        List<IMark> marks = parent.getMarks();
        int index = marks.indexOf(fMark);

        if (index > 0)
          return new ModelTreeNode(marks.get(index - 1));

        /* Folders preceed Marks, so check for them being available */
        List<IFolder> folders = parent.getFolders();
        if (!folders.isEmpty())
          return new ModelTreeNode(folders.get(folders.size() - 1));
      }
    }

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#hasChildren()
   */
  public boolean hasChildren() {

    /* Ask Folder */
    if (fFolder != null)
      return !fFolder.isEmpty();

    /* Marks dont have Children */
    return false;
  }

  /*
   * @see org.rssowl.ui.internal.util.ITreeNode#getData()
   */
  public Object getData() {
    return fFolder != null ? fFolder : fMark;
  }
}
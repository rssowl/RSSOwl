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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.rssowl.ui.internal.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper around <code>Tree</code> that allows to apply
 * <code>CTreeLayoutData</code> to Columns of the underlying Tree. The Wrapper
 * is making sure to avoid horizontal scrollbars if possible.
 *
 * @author bpasero
 */
public class CTree {

  /* ID to store Layout-Data with TreeColumns */
  private static final String LAYOUT_DATA = "org.rssowl.ui.internal.CTreeLayoutData"; //$NON-NLS-1$

  private Tree fTree;
  private List<TreeColumn> fCols = new ArrayList<TreeColumn>();
  private boolean fIsFlat = true;

  /**
   * @param parent
   * @param style
   */
  public CTree(Composite parent, int style) {
    fTree = new Tree(parent, style);
    parent.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        onTreeResize();
      }
    });
  }

  /**
   * @return Tree
   */
  public Tree getControl() {
    return fTree;
  }

  /**
   * @param isFlat set to <code>true</code> to indicate that this tree is
   * showing a list of elements or <code>false</code> to indicate that its
   * showing entries with child entries.
   */
  public void setFlat(boolean isFlat) {
    fIsFlat = isFlat;
  }

  /**
   * @param col
   * @param layoutData
   * @param text
   * @param tooltip
   * @param image
   * @param moveable
   * @param resizable
   * @return TreeColumn
   */
  public TreeColumn manageColumn(TreeColumn col, CColumnLayoutData layoutData, String text, String tooltip, Image image, boolean moveable, boolean resizable) {
    col.setData(LAYOUT_DATA, layoutData);
    col.setMoveable(moveable);
    col.setResizable(resizable);

    if (text != null)
      col.setText(text);

    if (tooltip != null)
      col.setToolTipText(tooltip);

    if (image != null)
      col.setImage(image);

    fCols.add(col);

    return col;
  }

  /**
   * @param col
   * @param visible
   * @param update
   */
  public void setVisible(TreeColumn col, boolean visible, boolean update) {
    CColumnLayoutData data = (CColumnLayoutData) col.getData(LAYOUT_DATA);
    data.setHidden(!visible);

    if (update)
      onTreeResize();
  }

  /**
   * Force layout of all columns.
   */
  public void update() {
    onTreeResize();
  }

  /**
   * Dispose and clear all columns.
   */
  public void clear() {
    for (TreeColumn cols : fCols) {
      cols.dispose();
    }

    fCols.clear();
  }

  private void onTreeResize() {
    int totalWidth = fTree.getParent().getClientArea().width;
    totalWidth -= fTree.getBorderWidth() * 2;

    ScrollBar verticalBar = fTree.getVerticalBar();
    if (verticalBar != null) {
      int barWidth = verticalBar.getSize().x;
      if (Application.IS_MAC && barWidth == 0)
        barWidth = 16; //Can be 0 on Mac

      totalWidth -= barWidth;
    }

    /* Bug on Mac: Width is too big */
    if (Application.IS_MAC) {
      totalWidth -= 3;

      if (!fIsFlat)
        totalWidth -= 24;
    }

    /* Bug on Linux: Margin from Bar to TableItem not returned */
    else if (Application.IS_LINUX)
      totalWidth -= 3;

    int freeWidth = totalWidth;
    int occupiedWidth = 0;

    /* Foreach TreeColumn */
    int totalFillSum = 0;
    for (int i = 0; i < fCols.size(); i++) {
      TreeColumn column = fCols.get(i);
      CColumnLayoutData data = (CColumnLayoutData) column.getData(LAYOUT_DATA);

      /* Hide Column */
      if (data.isHidden()) {
        column.setWidth(0);
      }

      /* Fixed with Default Width Hint */
      else if (data.getSize() == CColumnLayoutData.Size.FIXED && data.getWidthHint() == CColumnLayoutData.DEFAULT) {
        column.pack();
        int width = column.getWidth();
        freeWidth -= width;
        occupiedWidth += width;
      }

      /* Fixed with actual Width Hint */
      else if (data.getSize() == CColumnLayoutData.Size.FIXED) {
        int widthHint = data.getWidthHint();

        /* Windows: First column in tree always needs extra space for expand/collapse */
        if (Application.IS_WINDOWS && i == 0) {
          if (fIsFlat)
            widthHint += 25;
          else
            widthHint += 45;
        }

        /* Linux: First column in tree always needs extra space for expand/collapse */
        else if (Application.IS_LINUX && i == 0) {
          if (fIsFlat)
            widthHint += 20;
          else
            widthHint += 40;
        }

        /* Mac: First column in tree always needs extra space for expand/collapse */
        else if (Application.IS_MAC && i == 0) {
          widthHint += 20;
        }

        freeWidth -= widthHint;
        occupiedWidth += widthHint;

        /* Only apply if changed */
        if (column.getWidth() != widthHint)
          column.setWidth(widthHint);
      }

      /* Sum up the fill ratios for later use */
      else if (data.getSize() == CColumnLayoutData.Size.FILL) {
        totalFillSum += data.getWidthHint();
      }
    }

    /* Foreach TreeColumn */
    for (TreeColumn column : fCols) {
      CColumnLayoutData data = (CColumnLayoutData) column.getData(LAYOUT_DATA);

      /* Fill available space with ratio */
      if (data.getSize() == CColumnLayoutData.Size.FILL) {
        int colWidth = (freeWidth * data.getWidthHint()) / totalFillSum;

        /* Trim if necessary */
        if (occupiedWidth + colWidth >= totalWidth)
          colWidth = totalWidth - occupiedWidth;

        occupiedWidth += colWidth;

        /* Only apply if changed */
        if (column.getWidth() != colWidth)
          column.setWidth(colWidth);
      }
    }
  }
}
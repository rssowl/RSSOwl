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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.rssowl.ui.internal.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper around <code>Table</code> that allows to apply
 * <code>CTreeLayoutData</code> to Columns of the underlying Table. The Wrapper
 * is making sure to avoid horizontal scrollbars if possible.
 *
 * @author bpasero
 */
public class CTable {

  /* ID to store Layout-Data with TableColumns */
  private static final String LAYOUT_DATA = "org.rssowl.ui.internal.CTreeLayoutData"; //$NON-NLS-1$

  private Table fTable;
  private List<TableColumn> fCols = new ArrayList<TableColumn>();

  /**
   * @param parent
   * @param style
   */
  public CTable(Composite parent, int style) {
    fTable = new Table(parent, style);
    parent.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        onTableResize();
      }
    });
  }

  /**
   * @return Table
   */
  public Table getControl() {
    return fTable;
  }

  /**
   * Force layout of all columns.
   */
  public void update() {
    onTableResize();
  }

  /**
   * Dispose and clear all columns.
   */
  public void clear() {
    for (TableColumn cols : fCols) {
      cols.dispose();
    }

    fCols.clear();
  }

  /**
   * @param col
   * @param layoutData
   * @param text
   * @param tooltip
   * @param image
   * @param moveable
   * @param resizable
   * @return TableColumn
   */
  public TableColumn manageColumn(TableColumn col, CColumnLayoutData layoutData, String text, String tooltip, Image image, boolean moveable, boolean resizable) {
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

  private void onTableResize() {
    int totalWidth = fTable.getParent().getClientArea().width;
    totalWidth -= fTable.getBorderWidth() * 2;

    ScrollBar verticalBar = fTable.getVerticalBar();
    if (verticalBar != null) {
      int barWidth = verticalBar.getSize().x;
      if (Application.IS_MAC && barWidth == 0)
        barWidth= 16; //Can be 0 on Mac

      totalWidth -= barWidth;
    }

    /* Bug on Mac: Width is too big */
    if (Application.IS_MAC) {
      totalWidth -= 3;

      if ((fTable.getStyle() & SWT.CHECK) != 0)
        totalWidth -= 24;
    }

    /* Bug on Linux: Margin from Bar to TableItem not returned */
    else if (Application.IS_LINUX)
      totalWidth -= 3;

    int freeWidth = totalWidth;
    int occupiedWidth = 0;

    /* Foreach TableColumn */
    int totalFillSum = 0;
    for (TableColumn column : fCols) {
      CColumnLayoutData data = (CColumnLayoutData) column.getData(LAYOUT_DATA);

      /* Fixed with Default Width Hint */
      if (data.getSize() == CColumnLayoutData.Size.FIXED && data.getWidthHint() == CColumnLayoutData.DEFAULT) {
        column.pack();
        int width = column.getWidth();
        freeWidth -= width;
        occupiedWidth += width;
      }

      /* Fixed with actual Width Hint */
      else if (data.getSize() == CColumnLayoutData.Size.FIXED) {
        freeWidth -= data.getWidthHint();
        occupiedWidth += data.getWidthHint();

        /* Only apply if changed */
        if (column.getWidth() != data.getWidthHint())
          column.setWidth(data.getWidthHint());
      }

      /* Sum up the fill ratios for later use */
      else if (data.getSize() == CColumnLayoutData.Size.FILL) {
        totalFillSum += data.getWidthHint();
      }
    }

    /* Foreach TableColumn */
    for (TableColumn column : fCols) {
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
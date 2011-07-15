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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * Factory class for some LayoutData concerns in RSSOwl.
 *
 * @author bpasero
 */
public class LayoutUtils {

  /**
   * Create a new FillLayout with the given parameters
   *
   * @param horizontal If TRUE, align Layout horizontally
   * @param marginWidth Margin width in pixel
   * @param marginHeight Margin height in pixel
   * @return FillLayout New FillLayout with the given parameters
   */
  public static FillLayout createFillLayout(boolean horizontal, int marginWidth, int marginHeight) {
    FillLayout f = new FillLayout(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
    f.marginHeight = marginHeight;
    f.marginWidth = marginWidth;
    return f;
  }

  /**
   * Create a new GridLayout with the given parameters
   *
   * @param cols The number of columns
   * @return GridLayout New GridLayout with the given parameters
   */
  public static GridLayout createGridLayout(int cols) {
    return createGridLayout(cols, 5, 5, 5, 5, false);
  }

  /**
   * Create a new GridLayout with the given parameters
   *
   * @param cols The number of columns
   * @param marginWidth Margin width in pixel
   * @return GridLayout New GridLayout with the given parameters
   */
  public static GridLayout createGridLayout(int cols, int marginWidth) {
    return createGridLayout(cols, marginWidth, 5, 5, 5, false);
  }

  /**
   * Create a new GridLayout with the given parameters
   *
   * @param cols The number of columns
   * @param marginWidth Margin width in pixel
   * @param marginHeight Margin height in pixel
   * @return GridLayout New GridLayout with the given parameters
   */
  public static GridLayout createGridLayout(int cols, int marginWidth, int marginHeight) {
    return createGridLayout(cols, marginWidth, marginHeight, 5, 5, false);
  }

  /**
   * Create a new GridLayout with the given parameters
   *
   * @param cols The number of columns
   * @param marginWidth Margin width in pixel
   * @param marginHeight Margin height in pixel
   * @param makeColumnsEqualWidth TRUE if columns should be equals in size
   * @return GridLayout New GridLayout with the given parameters
   */
  public static GridLayout createGridLayout(int cols, int marginWidth, int marginHeight, boolean makeColumnsEqualWidth) {
    return createGridLayout(cols, marginWidth, marginHeight, 5, 5, makeColumnsEqualWidth);
  }

  /**
   * Create a new GridLayout with the given parameters
   *
   * @param cols The number of columns
   * @param marginWidth Margin width in pixel
   * @param marginHeight Margin height in pixel
   * @param verticalSpacing Vertical spacing in pixel
   * @return GridLayout New GridLayout with the given parameters
   */
  public static GridLayout createGridLayout(int cols, int marginWidth, int marginHeight, int verticalSpacing) {
    return createGridLayout(cols, marginWidth, marginHeight, verticalSpacing, 5, false);
  }

  /**
   * Create a new GridLayout with the given parameters
   *
   * @param cols The number of columns
   * @param marginWidth Margin width in pixel
   * @param marginHeight Margin height in pixel
   * @param verticalSpacing Vertical spacing in pixel
   * @param horizontalSpacing Horizontal spacing in pixel
   * @param makeColumnsEqualWidth TRUE if columns should be equals in size
   * @return GridLayout New GridLayout with the given parameters
   */
  public static GridLayout createGridLayout(int cols, int marginWidth, int marginHeight, int verticalSpacing, int horizontalSpacing, boolean makeColumnsEqualWidth) {
    GridLayout g = new GridLayout(cols, makeColumnsEqualWidth);
    g.marginHeight = marginHeight;
    g.marginWidth = marginWidth;
    g.verticalSpacing = verticalSpacing;
    g.horizontalSpacing = horizontalSpacing;
    return g;
  }

  /**
   * Sets the initial location to use for the shell. The default implementation
   * centers the shell horizontally (1/2 of the difference to the left and 1/2
   * to the right) and vertically (1/3 above and 2/3 below) relative to the
   * parent shell
   *
   * @param shell The shell to set the location
   */
  public static void positionShell(Shell shell) {
    Rectangle containerBounds = shell.getParent().getBounds();
    Point initialSize = shell.getSize();
    int x = Math.max(0, containerBounds.x + (containerBounds.width - initialSize.x) / 2);
    int y = Math.max(0, containerBounds.y + (containerBounds.height - initialSize.y) / 3);
    shell.setLocation(x, y);
  }
}
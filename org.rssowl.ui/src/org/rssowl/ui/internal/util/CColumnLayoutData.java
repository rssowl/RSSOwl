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

/**
 * Layout-Information to be used with a <code>CTree</code> or
 * <code>CTable</code>.
 *
 * @author bpasero
 */
public class CColumnLayoutData {

  /** Size hint for Columns */
  public enum Size {

    /** Column should Fill */
    FILL,

    /** Column has fixed Width */
    FIXED;
  }

  /** The Default Width-Hint */
  public static final int DEFAULT = -1;

  private Size fSize;
  private int fWHint;
  private boolean fHidden;

  /**
   * @param size
   * @param wHint
   */
  public CColumnLayoutData(Size size, int wHint) {
    fSize = size;
    fWHint = wHint;
  }

  /**
   * @return Returns the align.
   */
  public Size getSize() {
    return fSize;
  }

  /**
   * @return Returns the wHint.
   */
  public int getWidthHint() {
    return fWHint;
  }

  /**
   * @param hidden TRUE if the column should be hidden.
   */
  public void setHidden(boolean hidden) {
    fHidden = hidden;
  }

  /**
   * @return Returns TRUE if the column should be hidden.
   */
  public boolean isHidden() {
    return fHidden;
  }
}
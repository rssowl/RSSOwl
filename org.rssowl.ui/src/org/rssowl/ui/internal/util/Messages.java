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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.util.messages"; //$NON-NLS-1$
  public static String BrowserUtils_ATTACH_REPORT_ADVISE;
  public static String BrowserUtils_ERROR_LAUNCH_BROWSER;
  public static String BrowserUtils_ERROR_LAUNCH_BROWSER_MSG;
  public static String BrowserUtils_ERROR_LAUNCH_BROWSER_MSG_N;
  public static String CBrowser_BACK;
  public static String CBrowser_ERROR_CREATE_BROWSER;
  public static String CBrowser_ERROR_CREATE_BROWSER_MSG;
  public static String CBrowser_FORWARD;
  public static String CBrowser_LOADING;
  public static String CBrowser_RELOAD;
  public static String CBrowser_STOP;
  public static String ColorPicker_BARBERRY_GREEN;
  public static String ColorPicker_BARN_RED;
  public static String ColorPicker_CHOCOLATE_BROWN;
  public static String ColorPicker_COLOR_LABEL;
  public static String ColorPicker_DRIFTWOOD;
  public static String ColorPicker_FEDERAL_BLUE;
  public static String ColorPicker_LEXINGTON_GREEN;
  public static String ColorPicker_MARIGOLD_YELLOW;
  public static String ColorPicker_MUSTARD;
  public static String ColorPicker_OTHER;
  public static String ColorPicker_PITCH_BLACK;
  public static String ColorPicker_PUMPKIN;
  public static String ColorPicker_SALEM_RED;
  public static String ColorPicker_SALMON;
  public static String ColorPicker_SEA_GREEN;
  public static String ColorPicker_SLATE;
  public static String ColorPicker_SOLIDER_BLUE;
  public static String ColorPicker_TAVERN_GREEN;
  public static String FolderChooser_HIDE_FOLDERS;
  public static String FolderChooser_NEW_FOLDER;
  public static String FolderChooser_SHOW_FOLDERS;
  public static String NewsColumnSelectionControl_ADD;
  public static String NewsColumnSelectionControl_ASCENDING;
  public static String NewsColumnSelectionControl_DESCENDING;
  public static String NewsColumnSelectionControl_MOVE_DOWN;
  public static String NewsColumnSelectionControl_MOVE_UP;
  public static String NewsColumnSelectionControl_REMOVE;
  public static String NewsColumnSelectionControl_SORT_BY;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

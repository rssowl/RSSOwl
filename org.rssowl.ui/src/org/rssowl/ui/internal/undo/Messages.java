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

package org.rssowl.ui.internal.undo;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.undo.messages"; //$NON-NLS-1$
  public static String CopyOperation_COPY_N;
  public static String MoveOperation_ARCHIVE_N;
  public static String MoveOperation_MOVE_N;
  public static String NewsStateOperation_DELETE_N_NEWS;
  public static String NewsStateOperation_MARK_N_NEW_UNREAD;
  public static String NewsStateOperation_MARK_N_READ;
  public static String NewsStateOperation_MARK_N_UNREAD;
  public static String NewsStateOperation_UNSUPPORTED;
  public static String StickyOperation_MARK_N_STICKY;
  public static String StickyOperation_MARK_N_UNSTICKY;
  public static String UndoStack_REDO;
  public static String UndoStack_REDO_N;
  public static String UndoStack_UNDO;
  public static String UndoStack_UNDO_N;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

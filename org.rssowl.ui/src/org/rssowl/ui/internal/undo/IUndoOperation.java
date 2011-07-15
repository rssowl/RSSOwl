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

/**
 * Instances of {@link IUndoOperation} can be added to the {@link UndoStack} and
 * support undo/redo.
 *
 * @author bpasero
 */
public interface IUndoOperation {

  /**
   * @return Returns a displayable name for this operation to show in the UI.
   */
  String getName();

  /**
   * Asks to undo this operation.
   */
  void undo();

  /**
   * Asks to redo this operation.
   */
  void redo();

  /**
   * @return <code>true</code> if the user should be presented with progress
   * while this operation is running and <code>false</code> otherwise.
   */
  boolean isLongRunning();
}
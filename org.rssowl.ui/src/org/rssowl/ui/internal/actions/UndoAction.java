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

package org.rssowl.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.undo.UndoStack;

/**
 * Undo the last operation if possible.
 *
 * @author bpasero
 */
public class UndoAction extends Action {

  /** ID of this Action */
  public static final String ID = "org.rssowl.ui.UndoAction"; //$NON-NLS-1$

  /** Set ID for Key Binding Support */
  public UndoAction() {
    setId(ID);
    setActionDefinitionId(ID);
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    UndoStack.getInstance().undo();
  }

  /*
   * @see org.eclipse.jface.action.Action#isEnabled()
   */
  @Override
  public boolean isEnabled() {
    return UndoStack.getInstance().isUndoSupported();
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    return UndoStack.getInstance().getUndoName();
  }

  /*
   * @see org.eclipse.jface.action.Action#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor() {
    return OwlUI.getImageDescriptor("icons/elcl16/undo_edit.gif"); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
   */
  @Override
  public ImageDescriptor getDisabledImageDescriptor() {
    return OwlUI.getImageDescriptor("icons/dlcl16/undo_edit.gif"); //$NON-NLS-1$
  }
}
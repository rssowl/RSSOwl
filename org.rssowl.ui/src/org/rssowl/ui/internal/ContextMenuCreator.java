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

package org.rssowl.ui.internal;

import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Helper to properly dispose a Menu created from this {@link IMenuCreator} as
 * soon as it is no longer needed or a new one is created.
 *
 * @author bpasero
 */
public abstract class ContextMenuCreator implements IMenuCreator {
  private Menu fMenu;

  /*
   * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
   */
  public Menu getMenu(Control parent) {
    if (fMenu != null)
      OwlUI.safeDispose(fMenu);

    fMenu = createMenu(parent);

    return fMenu;
  }

  /**
   * @param parent the parent control
   * @return the menu, or <code>null</code> if the menu could not be created
   */
  public abstract Menu createMenu(Control parent);

  /*
   * @see org.eclipse.jface.action.IMenuCreator#dispose()
   */
  public void dispose() {
    if (fMenu != null)
      OwlUI.safeDispose(fMenu);
  }

  /*
   * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
   */
  public Menu getMenu(Menu parent) {
    return null;
  }
}
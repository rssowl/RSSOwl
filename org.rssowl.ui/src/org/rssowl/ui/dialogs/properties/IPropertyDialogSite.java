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

package org.rssowl.ui.dialogs.properties;

import org.eclipse.jface.resource.ResourceManager;

/**
 * The <code>IPropertyDialogSite</clode> is used to provide
 * access to some useful methods of the Dialog a contribution
 * is showing.
 *
 * @author bpasero
 */
public interface IPropertyDialogSite {

  /** Defines various message-types */
  enum MessageType {

    /** Constant for an Info-Message type */
    INFO,

    /** Constant for an Warning-Message type */
    WARNING,

    /** Constant for an Error-Message type */
    ERROR
  }

  /**
   * Set's a message to the dialog to show as defined by the given type. Pass
   * <code>NULL</code> to reset any previous set message.
   *
   * @param message The message or <code>NULL</code> to reset any previous
   * message.
   * @param type The type of message (Info, Warning or Error).
   */
  void setMessage(String message, MessageType type);

  /**
   * Asks to select the given Page in the Property-Dialog.
   *
   * @param page the page to select in the Property-Dialog.
   */
  void select(IEntityPropertyPage page);

  /**
   * Returns the shared instance of <code>ResourceManager</code> that can be
   * used to create resources. The manager is disposed automatically when the
   * dialog is closed.
   *
   * @return the shared instance of <code>ResourceManager</code> that can be
   * used to create resources.
   */
  ResourceManager getResourceManager();

  /**
   * Returns the number of pixels corresponding to the given number of
   * horizontal dialog units.
   *
   * @param dlus the number of horizontal dialog units
   * @return the number of pixels
   */
  int getHorizontalPixels(int dlus);

  /**
   * Notify the entity property dialog that the contents of the page have
   * changed so that the dialog can adjust its size properly.
   */
  void contentsChanged();
}
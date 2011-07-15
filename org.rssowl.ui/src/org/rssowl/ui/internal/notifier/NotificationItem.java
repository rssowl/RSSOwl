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

package org.rssowl.ui.internal.notifier;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.RGB;

/**
 * Instances of {@link NotificationItem} can be displayed in the
 * {@link NotificationPopup}.
 *
 * @author bpasero
 */
public abstract class NotificationItem implements Comparable<NotificationItem> {
  private final String fText;
  private final ImageDescriptor fImage;

  /**
   * @param text The text of the item to show in the popup.
   * @param image The image of the item to show in the popup.
   */
  public NotificationItem(String text, ImageDescriptor image) {
    Assert.isNotNull(text);
    Assert.isNotNull(image);

    fText = text;
    fImage = image;
  }

  /**
   * Opens the item. This method will be called when the user clicks on the item
   * in the popup.
   *
   * @param e the mousevent that triggered the opening of the item.
   */
  public abstract void open(MouseEvent e);

  /**
   * @return <code>true</code> if this item supports stickyness and
   * <code>false</code> otherwise.
   */
  public abstract boolean supportsSticky();

  /**
   * @return <code>true</code> if this item supports being marked as read and
   * <code>false</code> otherwise.
   */
  public abstract boolean supportsMarkRead();

  /**
   * @return <code>true</code> if the item is sticky and <code>false</code>
   * otherwise.
   */
  public abstract boolean isSticky();

  /**
   * @param sticky <code>true</code> if the item is made sticky and
   * <code>false</code> otherwise.
   */
  public abstract void setSticky(boolean sticky);

  /**
   * @return <code>true</code> if the item is read, and <code>false</code>
   * otherwise.
   */
  public abstract boolean isRead();

  /**
   * @param read <code>true</code> if the item is made read and
   * <code>false</code> otherwise.
   */
  public abstract void setRead(boolean read);

  /**
   * @param color The foreground color for the News in the notifier or
   * <code>null</code> if none.
   */
  public abstract void setColor(RGB color);

  /**
   * @return The text of the item to show in the popup.
   */
  public String getText() {
    return fText;
  }

  /**
   * @return The image of the item to show in the popup.
   */
  public ImageDescriptor getImage() {
    return fImage;
  }

  /**
   * @return The foreground color for the News in the notifier or
   * <code>null</code> if none.
   */
  public RGB getColor() {
    return null;
  }

  /**
   * @return The description of this item or <code>null</code> if none.
   */
  public String getDescription() {
    return null;
  }

  /**
   * @return The origin of this item or <code>null</code> if none.
   */
  public String getOrigin() {
    return null;
  }
}
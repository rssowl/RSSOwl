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

package org.rssowl.ui.filter;

import org.eclipse.swt.widgets.Composite;
import org.rssowl.core.INewsAction;

import java.util.Properties;

/**
 * Instances of {@link INewsActionPresentation} provide a custom UI presentation
 * for an {@link INewsAction}.
 * <p>
 * Contributed via <code>org.rssowl.ui.NewsActionPresentation</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 */
public interface INewsActionPresentation {

  /**
   * @param parent the parent Composite to create the presentation controls.
   * @param data arbitrary data of the {@link INewsAction} to present.
   */
  void create(Composite parent, Object data);

  /**
   * Asks this presentation to dispose the controls it created.
   */
  void dispose();

  /**
   * @return arbitrary data taken from changes the user made to the
   * presentation. It is strongly recommended to use {@link String} or
   * {@link Properties} as data type to support Import and Export of the news
   * action.
   */
  Object getData();
}
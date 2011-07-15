/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.editors.feed;

import org.eclipse.ui.IEditorSite;
import org.rssowl.core.persist.pref.IPreferenceScope;

/**
 * A more specialized form of an editor site providing direct access to the
 * typical {@link IEditorSite} of the {@link FeedView} as well as to some
 * methods that are only visible on the {@link FeedView} scope but may be
 * required inside {@link IFeedViewPart}.
 */
public interface IFeedViewSite {

  /**
   * @return the {@link IEditorSite} of the {@link FeedView} editor.
   */
  IEditorSite getEditorSite();

  /**
   * @return <code>true</code> if the news table viewer is visible and
   * <code>false</code> otherwise.
   */
  boolean isTableViewerVisible();

  /**
   * @return <code>true</code> if the news browser viewer is visible and
   * <code>false</code> otherwise.
   */
  boolean isBrowserViewerVisible();

  /**
   * @return the {@link IPreferenceScope} to access the preferences of the input
   * that is currently displayed in the editor. Never <code>null</code>. Will
   * fallback to global preferences in any case.
   */
  IPreferenceScope getInputPreferences();
}
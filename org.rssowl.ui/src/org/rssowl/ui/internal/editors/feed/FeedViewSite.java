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
import org.rssowl.core.Owl;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.FolderNewsMark;

/**
 * Implementation of {@link IFeedViewSite} that knows about the {@link FeedView}
 * and can thereby delegate methods to the editor.
 */
public class FeedViewSite implements IFeedViewSite {
  private final FeedView fFeedView;
  private final IEditorSite fSite;

  FeedViewSite(FeedView feedView, IEditorSite site) {
    fFeedView = feedView;
    fSite = site;
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewSite#getEditorSite()
   */
  public IEditorSite getEditorSite() {
    return fSite;
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewSite#isTableViewerVisible()
   */
  public boolean isTableViewerVisible() {
    return fFeedView.isTableViewerVisible();
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewSite#isBrowserViewerVisible()
   */
  public boolean isBrowserViewerVisible() {
    return fFeedView.isBrowserViewerVisible();
  }

  /*
   * @see org.rssowl.ui.internal.editors.feed.IFeedViewSite#getInputPreferences()
   */
  public IPreferenceScope getInputPreferences() {
    FeedViewInput input = (FeedViewInput) fFeedView.getEditorInput();
    if (input != null) {
      INewsMark mark = input.getMark();
      if (mark != null) {
        if (mark instanceof FolderNewsMark)
          return Owl.getPreferenceService().getEntityScope(((FolderNewsMark) mark).getFolder());

        return Owl.getPreferenceService().getEntityScope(mark);
      }
    }

    return Owl.getPreferenceService().getGlobalScope();
  }
}
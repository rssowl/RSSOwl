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

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.editors.feed.FeedViewInput;

import java.util.List;

/**
 * @author bpasero
 */
public class EditorUtils {

  /**
   * @param editorReferences
   * @param input
   * @return IEditorReference
   */
  public static IEditorReference findEditor(IEditorReference[] editorReferences, Object input) {
    for (IEditorReference reference : editorReferences) {
      try {
        IEditorInput editorInput = reference.getEditorInput();
        if (editorInput instanceof FeedViewInput) {
          FeedViewInput feedViewInput = (FeedViewInput) editorInput;
          Object inputObj = feedViewInput.getMark();
          if (inputObj instanceof FolderNewsMark)
            inputObj = ((FolderNewsMark) inputObj).getFolder();

          if (inputObj.equals(input))
            return reference;
        }
      } catch (PartInitException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }

    return null;
  }

  /**
   * @return the number of editors able to be visible at the same time.
   */
  public static int getOpenEditorLimit() {
    IPreferenceScope preferences = Owl.getPreferenceService().getEclipseScope();
    boolean isLimited = preferences.getBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS);
    if (!isLimited)
      return Integer.MAX_VALUE;

    return preferences.getInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD);
  }

  /**
   * Update filter and grouping of all opened feed views.
   */
  public static void updateFilterAndGrouping() {
    updateFilterAndGrouping(null);
  }

  /**
   * Update filter and grouping of all opened feed views except for the provided
   * one.
   * 
   * @param exception the instanceof {@link FeedView} to not update filter and
   * grouping for.
   */
  public static void updateFilterAndGrouping(FeedView exception) {
    List<FeedView> feedViews = OwlUI.getFeedViews();
    for (FeedView feedView : feedViews) {
      if (!feedView.equals(exception))
        feedView.updateFilterAndGrouping(true);
    }
  }

  /**
   * Update the layout of all opened feed views.
   */
  public static void updateLayout() {
    List<FeedView> feedViews = OwlUI.getFeedViews();
    for (FeedView feedView : feedViews) {
      feedView.updateLayout();
    }
  }

  /**
   * Update the columns of all opened feed views.
   */
  public static void updateColumns() {
    List<FeedView> feedViews = OwlUI.getFeedViews();
    for (FeedView feedView : feedViews) {
      feedView.updateColumns();
    }
  }

  /**
   * Update the toolbar visibility of all opened feed views.
   */
  public static void updateToolbarVisibility() {
    List<FeedView> feedViews = OwlUI.getFeedViews();
    for (FeedView feedView : feedViews) {
      feedView.updateToolbarVisibility();
    }
  }
}
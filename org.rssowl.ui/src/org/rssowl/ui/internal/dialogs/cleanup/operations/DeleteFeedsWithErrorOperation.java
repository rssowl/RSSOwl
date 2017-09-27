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

package org.rssowl.ui.internal.dialogs.cleanup.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Delete BookMarks that have Connection Error
 */
public class DeleteFeedsWithErrorOperation extends DeleteFeedsAbstractOperation {
  private boolean isEnabled;

  @SuppressWarnings("unused")
  public DeleteFeedsWithErrorOperation(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void savePreferences(IPreferenceScope preferences) {
    preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_CON_ERROR, isEnabled);
  }

  @Override
  public Collection<IBookMark> filter(Collection<IBookMark> bookmarks, IProgressMonitor monitor) {
    Collection<IBookMark> result = new ArrayList<IBookMark>();

    for (IBookMark mark : bookmarks) {
      if (mark.isErrorLoading())
        result.add(mark);
    }
    return result;
  }

  @Override
  protected SummaryModelTaskGroup createGroup() {
    return new SummaryModelTaskGroup(Messages.CleanUpModel_DELETE_CON_ERROR);
  }
}

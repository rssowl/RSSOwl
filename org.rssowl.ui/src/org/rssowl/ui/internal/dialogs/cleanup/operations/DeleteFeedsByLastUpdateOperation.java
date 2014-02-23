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
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Delete BookMarks that have not updated in X Days
 */
public class DeleteFeedsByLastUpdateOperation extends DeleteFeedsAbstractOperation {
  private boolean isEnabled;
  private int fLastUpdateInDays;

  /* One Day in millis */
  private static final long DAY = 24 * 60 * 60 * 1000;

  public DeleteFeedsByLastUpdateOperation(boolean isEnabled, Object data) {
    this.isEnabled = isEnabled;
    fLastUpdateInDays = (Integer) data;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void savePreferences(IPreferenceScope preferences) {
    preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_LAST_UPDATE_STATE, isEnabled);
    preferences.putInteger(DefaultPreferences.CLEAN_UP_BM_BY_LAST_UPDATE_VALUE, fLastUpdateInDays);
  }

  @Override
  public Collection<IBookMark> filter(Collection<IBookMark> bookmarks, IProgressMonitor monitor) {
    Collection<IBookMark> result = new ArrayList<IBookMark>();

    int days = fLastUpdateInDays;
    long maxLastUpdateDate = DateUtils.getToday().getTimeInMillis() - (days * DAY);

    for (IBookMark mark : bookmarks) {

      Date mostRecentNewsDate = mark.getMostRecentNewsDate();
      Date creationDate = mark.getCreationDate();
      boolean deleteBookMark = false;

      /* Ask for most recent news date if present */
      if (mostRecentNewsDate != null && mostRecentNewsDate.getTime() < maxLastUpdateDate)
        deleteBookMark = true;

      /* Alternatively check for creation date */
      else if (mostRecentNewsDate == null && creationDate.getTime() < maxLastUpdateDate)
        deleteBookMark = true;

      if (deleteBookMark)
        result.add(mark);
    }
    return result;
  }

  @Override
  protected SummaryModelTaskGroup createGroup() {
    return new SummaryModelTaskGroup(NLS.bind(Messages.CleanUpModel_DELETE_BY_UPDATE, fLastUpdateInDays));
  }
}

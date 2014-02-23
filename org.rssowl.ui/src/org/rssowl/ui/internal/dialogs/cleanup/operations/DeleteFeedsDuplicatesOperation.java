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
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Delete Duplicate BookMarks
 * */
public class DeleteFeedsDuplicatesOperation extends DeleteFeedsAbstractOperation {
  private boolean isEnabled;

  @SuppressWarnings("unused")
  public DeleteFeedsDuplicatesOperation(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void savePreferences(IPreferenceScope preferences) {
    preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_DUPLICATES, isEnabled);

  }

  @Override
  public Collection<IBookMark> filter(Collection<IBookMark> bookmarks, IProgressMonitor monitor) {
    Collection<IBookMark> result = new ArrayList<IBookMark>();

    Hashtable<String, ArrayList<IBookMark>> hash = new Hashtable<String, ArrayList<IBookMark>>();

    // creating hash
    for (IBookMark mark : bookmarks) {
      String link = mark.getFeedLinkReference().getLinkAsText();
      if (hash.contains(link)) {
        hash.get(link).add(mark);
      } else {
        ArrayList<IBookMark> arr = new ArrayList<IBookMark>();
        arr.add(mark);
        hash.put(link, arr);
      }
    }

    // finding duplicates
    for (String link : hash.keySet()) {
      ArrayList<IBookMark> arr = hash.get(link);
      if (arr.size() <= 1)
        continue; // no duplicates

      /* Group of BookMark referencing the same Feed sorted by Creation Date */
      Set<IBookMark> sortedBookmarkGroup = new TreeSet<IBookMark>(new Comparator<IBookMark>() {
        public int compare(IBookMark o1, IBookMark o2) {
          if (o1.equals(o2))
            return 0;

          return o1.getCreationDate() == null ? -1 : o1.getCreationDate().compareTo(o2.getCreationDate());
        }
      });

      /* Add Current Bookmark and Duplicates */
      for (IBookMark mark : arr)
        sortedBookmarkGroup.add(mark);

      /* Delete most recent duplicates if any */
      Iterator<IBookMark> iterator = sortedBookmarkGroup.iterator();
      iterator.next(); // Ignore first, oldest one

      while (iterator.hasNext()) {
        result.add(iterator.next());
      }
    }

    return result;
  }

  @Override
  protected SummaryModelTaskGroup createGroup() {
    return new SummaryModelTaskGroup(Messages.CleanUpModel_DELETE_DUPLICATES);
  }
}

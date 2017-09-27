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

import static java.util.Arrays.asList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.Set;

/**
 * Delete Read News
 */
public class DeleteNewsWithStateReadOperation extends DeleteNewsAbstractOperation {
  private boolean isEnabled;

  public DeleteNewsWithStateReadOperation(boolean state) {
    isEnabled = state;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void savePreferences(IPreferenceScope preferences) {
    preferences.putBoolean(DefaultPreferences.CLEAN_UP_READ_NEWS_STATE, isEnabled);
  }

  @Override
  protected SummaryModelTaskGroup createGroup() {
    return new SummaryModelTaskGroup(Messages.CleanUpModel_READ_NEWS);
  }

  @Override
  protected Set<NewsReference> searchNews(IBookMark bookmark, IProgressMonitor monitor) {
    return getSearchResults(searchNews(bookmark, monitor, asList(getNewsStateIsReadCondition())));
  }
}

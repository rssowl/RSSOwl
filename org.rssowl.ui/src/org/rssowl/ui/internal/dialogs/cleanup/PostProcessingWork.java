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

package org.rssowl.ui.internal.dialogs.cleanup;

import org.rssowl.core.persist.IBookMark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostProcessingWork {

  private boolean optimizeSearch;
  private boolean askForRestart;
  private List<IBookMark> bookmarksToDelete;

  public PostProcessingWork() {
    optimizeSearch = false;
    askForRestart = false;
    bookmarksToDelete = new ArrayList<IBookMark>();
  }

  public void setOptimizeSearch(boolean optimizeSearch) {
    this.optimizeSearch = optimizeSearch;
  }

  public void setAskForRestart(boolean askForRestart) {
    this.askForRestart = askForRestart;
  }

  public boolean isOptimizeSearch() {
    return optimizeSearch;
  }

  public boolean needRestart() {
    return askForRestart;
  }

  public void deleteBokmark(IBookMark mark) {
    bookmarksToDelete.add(mark);
  }

  public Collection<IBookMark> bookmarksToDelete() {
    return bookmarksToDelete;
  }

}

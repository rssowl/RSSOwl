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

package org.rssowl.core.internal.newsaction;

import org.rssowl.core.INewsAction;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.util.CoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An instance of {@link INewsAction} to mark a list of news as read.
 *
 * @author bpasero
 */
public class MarkReadNewsAction implements INewsAction {

  /** ID of this Action */
  public static final String ID = "org.rssowl.core.MarkReadNewsAction"; //$NON-NLS-1$

  /*
   * @see org.rssowl.core.INewsAction#run(java.util.List, java.util.Map,
   * java.lang.Object)
   */
  public List<IEntity> run(List<INews> news, Map<INews, INews> replacements, Object data) {

    /* Ensure to Pickup Replaces */
    news = CoreUtils.replace(news, replacements);

    /* Run Filter */
    List<IEntity> entitiesToSave = new ArrayList<IEntity>(news.size());
    for (INews newsitem : news) {
      State state = newsitem.getState();
      if (state == INews.State.NEW || state == INews.State.UNREAD || state == INews.State.UPDATED) {
        newsitem.setState(INews.State.READ);
        entitiesToSave.add(newsitem);
      }
    }

    return entitiesToSave;
  }

  /*
   * @see org.rssowl.core.INewsAction#isConflicting(org.rssowl.core.INewsAction)
   */
  public boolean conflictsWith(INewsAction otherAction) {
    return otherAction instanceof DeleteNewsAction || otherAction instanceof MarkUnreadNewsAction;
  }

  /*
   * @see org.rssowl.core.INewsAction#getLabel(java.lang.Object)
   */
  public String getLabel(Object data) {
    return null;
  }
}
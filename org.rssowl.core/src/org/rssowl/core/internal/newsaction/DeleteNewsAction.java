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
import org.rssowl.core.util.CoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An instance of {@link INewsAction} to delete a list of news.
 *
 * @author bpasero
 */
public class DeleteNewsAction implements INewsAction {

  /** ID of this Action */
  public static final String ID = "org.rssowl.core.DeleteNewsAction"; //$NON-NLS-1$

  /*
   * @see org.rssowl.core.INewsAction#run(java.util.List, java.util.Map,
   * java.lang.Object)
   */
  @Override
  public List<IEntity> run(List<INews> news, Map<INews, INews> replacements, Object data) {

    /* Ensure to Pickup Replaces */
    news = CoreUtils.replace(news, replacements);

    /* Run Filter */
    List<IEntity> entitiesToSave = new ArrayList<IEntity>(news.size());
    for (INews newsitem : news) {
      if (newsitem.isVisible()) {
        newsitem.setState(INews.State.DELETED);
        entitiesToSave.add(newsitem);
      }
    }

    return entitiesToSave;
  }

  /*
   * @see org.rssowl.core.INewsAction#isConflicting(org.rssowl.core.INewsAction)
   */
  @Override
  public boolean conflictsWith(INewsAction otherAction) {
    return otherAction instanceof DeleteNewsAction;
  }

  /*
   * @see org.rssowl.core.INewsAction#getLabel(java.lang.Object)
   */
  @Override
  public String getLabel(Object data) {
    return null;
  }
}
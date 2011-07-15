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

package org.rssowl.core.internal.persist.dao;

import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.event.LabelEvent;
import org.rssowl.core.persist.event.LabelListener;

import java.util.Set;

/**
 * A caching DAO for {@link ILabel}.
 */
public class CachingLabelDAO extends CachingDAO<LabelDAOImpl, ILabel, LabelListener, LabelEvent> implements ILabelDAO {

  public CachingLabelDAO() {
    super(new LabelDAOImpl());
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.CachingDAO#createEntityListener()
   */
  @Override
  protected LabelListener createEntityListener() {
    return new LabelListener() {

      public void entitiesAdded(Set<LabelEvent> events) {
        for (LabelEvent event : events)
          getCache().put(event.getEntity().getId(), event.getEntity());
      }

      public void entitiesDeleted(Set<LabelEvent> events) {
        for (LabelEvent event : events)
          getCache().remove(event.getEntity().getId(), event.getEntity());
      }

      public void entitiesUpdated(Set<LabelEvent> events) {
      /* No action needed */
      }
    };
  }
}
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

package org.rssowl.core.internal.persist.dao;

import org.rssowl.core.internal.persist.SearchCondition;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.dao.ISearchConditionDAO;
import org.rssowl.core.persist.event.SearchConditionEvent;
import org.rssowl.core.persist.event.SearchConditionListener;

/**
 * A data-access-object for <code>ISearchCondition</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class SearchConditionDAOImpl extends AbstractEntityDAO<ISearchCondition, SearchConditionListener, SearchConditionEvent> implements ISearchConditionDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public SearchConditionDAOImpl() {
    super(SearchCondition.class, true);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final SearchConditionEvent createDeleteEventTemplate(ISearchCondition entity) {
    return createSaveEventTemplate(entity);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final SearchConditionEvent createSaveEventTemplate(ISearchCondition entity) {
    return new SearchConditionEvent(entity, true);
  }
}
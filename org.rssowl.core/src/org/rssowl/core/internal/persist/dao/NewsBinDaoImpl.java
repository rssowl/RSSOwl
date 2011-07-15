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

import org.rssowl.core.internal.persist.NewsBin;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.dao.INewsBinDAO;
import org.rssowl.core.persist.event.NewsBinEvent;
import org.rssowl.core.persist.event.NewsBinListener;
import org.rssowl.core.persist.service.PersistenceException;

import com.db4o.ext.Db4oException;

import java.util.Date;

/**
 * A data-access-object for <code>INewsBin</code>s.
 */
public class NewsBinDaoImpl extends AbstractEntityDAO<INewsBin, NewsBinListener, NewsBinEvent> implements INewsBinDAO {

  public NewsBinDaoImpl() {
    super(NewsBin.class, true);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected NewsBinEvent createSaveEventTemplate(INewsBin entity) {
    return new NewsBinEvent(entity, null, true);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected NewsBinEvent createDeleteEventTemplate(INewsBin entity) {
    return createSaveEventTemplate(entity);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.INewsBinDAO#visited(org.rssowl.core.persist
   * .INewsBin)
   */
  public void visited(INewsBin mark) {
    fWriteLock.lock();
    try {
      mark.setLastVisitDate(new Date());
      mark.setPopularity(mark.getPopularity() + 1);
      preSave(mark);
      fDb.ext().set(mark, 1);
      fDb.commit();
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    } finally {
      fWriteLock.unlock();
    }
    DBHelper.cleanUpAndFireEvents();
  }
}
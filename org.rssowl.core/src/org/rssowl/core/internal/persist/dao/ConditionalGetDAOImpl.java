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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.internal.persist.ConditionalGet;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.dao.IConditionalGetDAO;
import org.rssowl.core.persist.service.PersistenceException;

import com.db4o.ext.Db4oException;
import com.db4o.query.Query;

import java.net.URI;

/**
 * A data-access-object for <code>IConditionalGet</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class ConditionalGetDAOImpl extends AbstractPersistableDAO<IConditionalGet> implements IConditionalGetDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public ConditionalGetDAOImpl() {
    super(ConditionalGet.class, true);
  }

  /*
   * @see org.rssowl.core.persist.dao.IConditionalGetDAO#load(java.net.URI)
   */
  @Override
  public IConditionalGet load(URI link) {
    Assert.isNotNull(link, "link cannot be null"); //$NON-NLS-1$
    try {
      Query query = fDb.query();
      query.constrain(fEntityClass);
      query.descend("fLink").constrain(link.toString()); //$NON-NLS-1$

      for (IConditionalGet entity : getList(query)) {
        fDb.activate(entity, Integer.MAX_VALUE);
        return entity;
      }
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }

    return null;
  }
}
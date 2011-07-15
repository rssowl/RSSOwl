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

package org.rssowl.core.persist.dao;

import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.event.PersonEvent;
import org.rssowl.core.persist.event.PersonListener;
import org.rssowl.core.persist.service.PersistenceException;

import java.util.Set;

/**
 * A data-access-object for <code>IPerson</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public interface IPersonDAO extends IEntityDAO<IPerson, PersonListener, PersonEvent> {

  /**
   * Loads a sorted <code>Set</code> of author names or emails for all IPerson
   * instances that are persisted and have either a non-null name or email. The
   * author's name is given preference, but if it's <code>null</code> and the
   * email is not <code>null</code>, the email is used.
   *
   * @return a sorted <code>Set</code> of Strings containing all authors that
   * are persisted in the persistence layer.
   * @throws PersistenceException In case of an error while accessing the
   * persistance layer implementation.
   */
  Set<String> loadAllNames() throws PersistenceException;
}
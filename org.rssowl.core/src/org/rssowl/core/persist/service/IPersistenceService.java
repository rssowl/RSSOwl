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

package org.rssowl.core.persist.service;

import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IEntityDAO;
import org.rssowl.core.util.LongOperationMonitor;

/**
 * Provides access to ther persistence layer of RSSOwl. This layer is
 * contributable via the PersistenceService extension point provided by this
 * plugin. The work that is done by the layer includes:
 * <ul>
 * <li>Controlling the lifecycle of the persistence layer</li>
 * <li>Providing the DAOService that contains DAOs for each persistable entity</li>
 * <li>Providing the model search to perform full-text searching</li>
 * </ul>
 * <p>
 * Contributed via <code>org.rssowl.core.PersistenceService</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 * @see AbstractPersistenceService
 * @see DAOService
 * @see IModelSearch
 */
public interface IPersistenceService {

  /**
   * Startup the persistence layer. In case of a Database, this would be the
   * right place to open the connection.
   *
   * @param emergency if <code>true</code> indicates this startup method is
   * called from an emergency situation like restoring a backup.
   * @param monitor the {@link LongOperationMonitor} is used to react on
   * cancellation and report accurate startup progress.
   * @param forRestore if <code>true</code> will open the restore DB as profile
   * and <code>false</code> to open the default profile location.
   */
  void startup(LongOperationMonitor monitor, boolean emergency, boolean forRestore);

  /**
   * Gets the implementation of <code>DAOService</code> that provides access to
   * all DAOs per entity in RSSOwl. DAOs allow to load and save entities as well
   * as some more advanced operations (e.g. reparenting for folders). The
   * implementation is looked up using the DAOService extension point.
   * <p>
   * Note that for most simple operations like saving or loading entities, using
   * <code>DynamicDAO</code> requires less code.
   * </p>
   *
   * @return Returns the implementation of <code>DAOService</code> that provides
   * access to all DAOs per entity in RSSOwl. DAOs allow to load and save
   * entities as well as some more advanced operations (e.g. reparenting for
   * folders).
   * @see IEntityDAO
   * @see DynamicDAO
   */
  DAOService getDAOService();

  /**
   * Gets the implementation of <code>IDGenerator</code> that generates IDs that
   * have not yet been used by the persistence layer. The implementation is
   * looked up using the IDGenerator extension point.
   *
   * @return An implementation of IDGenerator.
   * @see IDGenerator
   */
  IDGenerator getIDGenerator();

  /**
   * <p>
   * Get the Implementation of <code>IModelSearch</code> that allows to search
   * model types. The implementation is provided using Apache Lucene as
   * full-text-search engine.
   * </p>
   *
   * @return Returns the Implementation of <code>IModelSearch</code> that allows
   * to search model types.
   */
  IModelSearch getModelSearch();

  /**
   * Shutdown the persistence layer. In case of a Database, this would be the
   * right place to save the relations.
   *
   * @param emergency If set to <code>TRUE</code>, this method is called from a
   * shutdown hook that got triggered from a non-normal shutdown (e.g. System
   * Shutdown).
   * @throws PersistenceException In case of an error while starting up the
   * persistence layer.
   */
  void shutdown(boolean emergency) throws PersistenceException;

  /**
   * Instructs the persistence service to schedule an optimization run during
   * the next time the application is started. The actual optimization type is
   * dependent on the persistence system being used and implementors are free to
   * leave this as a no-op in case the the persistence system tunes itself
   * automatically during runtime.
   *
   * @throws PersistenceException in case a problem occurs while trying to
   * schedule this operation.
   */
  void defragmentOnNextStartup() throws PersistenceException;
}
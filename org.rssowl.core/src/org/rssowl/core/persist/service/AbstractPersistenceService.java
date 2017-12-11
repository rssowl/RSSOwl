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

import org.rssowl.core.internal.persist.search.ModelSearchImpl;
import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.util.ExtensionUtils;
import org.rssowl.core.util.LongOperationMonitor;

/**
 * <p>
 * The <code>AbstractPersistenceService</code> is a contributable class that
 * handles addition, update, deletion and search of the various Model Types in
 * the application.
 * </p>
 * <p>
 * Contributors have to implement the Methods startup() and shutdown(), but may
 * chose to leave the others as provided in this abstract class. This default
 * implementation uses extension-points to lookup ModelDAO and PreferencesDAO.
 * </p>
 *
 * @author bpasero
 */
public abstract class AbstractPersistenceService implements IPersistenceService {

  /* ID for ID Generator Contribution */
  private static final String MODEL_ID_GENERATOR_EXTENSION_POINT = "org.rssowl.core.IDGenerator"; //$NON-NLS-1$

  /* ID for DAO Factory Contribution */
  private static final String MODEL_DAO_FACTORY_EXTENSION_POINT = "org.rssowl.core.DAOService"; //$NON-NLS-1$

  private volatile IModelSearch fModelSearch;
  private volatile IDGenerator fIDGenerator;
  private volatile DAOService fDAOService;

  protected AbstractPersistenceService() {}

  /*
   * @see
   * org.rssowl.core.persist.service.IPersistenceService#startup(org.rssowl.
   * core.util.LongOperationMonitor, boolean, boolean)
   */
  @Override
  public void startup(LongOperationMonitor monitor, boolean emergency, boolean forRestore) throws PersistenceException {
    getModelSearch();
    getIDGenerator();
    getDAOService();
  }

  /*
   * @see org.rssowl.core.model.dao.IPersistenceService#getDAOService()
   */
  @Override
  public DAOService getDAOService() {
    if (fDAOService == null)
      fDAOService = (DAOService) ExtensionUtils.loadSingletonExecutableExtension(MODEL_DAO_FACTORY_EXTENSION_POINT);

    return fDAOService;
  }

  /*
   * @see org.rssowl.core.model.dao.IPersistenceService#getIDGenerator()
   */
  @Override
  public IDGenerator getIDGenerator() {
    if (fIDGenerator == null)
      fIDGenerator = (IDGenerator) ExtensionUtils.loadSingletonExecutableExtension(MODEL_ID_GENERATOR_EXTENSION_POINT);

    return fIDGenerator;
  }

  /*
   * @see org.rssowl.core.model.dao.IPersistenceService#getModelSearch()
   */
  @Override
  public IModelSearch getModelSearch() {
    if (fModelSearch == null)
      fModelSearch = new ModelSearchImpl();

    return fModelSearch;
  }
}
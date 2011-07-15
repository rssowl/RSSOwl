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

import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.event.PreferenceEvent;
import org.rssowl.core.persist.event.PreferenceListener;
import org.rssowl.core.persist.service.PersistenceException;

/**
 * The <code>IPreferenceDAO</code> offers methods to store and retrieve
 * Preferences, simply by providing a Key-Value-Pair. The underlying persistance
 * layer is responsible for how the Values are stored.
 *
 * @author bpasero
 */
public interface IPreferenceDAO extends IEntityDAO<IPreference, PreferenceListener, PreferenceEvent> {

  /**
   * If a IPreference with <code>key</code> exists in the persistence system,
   * it is loaded and returned. Otherwise, <code>null</code> is returned.
   * 
   * @param key The key of the required IPreference.
   * @return a IPreference with the given key.
   * @throws PersistenceException If an error occurs while trying to retrieve
   * the object from the persistence system.
   */
  IPreference load(String key) throws PersistenceException;
  
  /**
   * If a IPreference with <code>key</code> exists in the persistence system,
   * loads it and returns it. Otherwise, creates a new IPreference with
   * <code>key</code> and returns it.
   * 
   * @param key The key that uniquely identified the IPreference.
   * @return An existing or newly created IPreference object.
   * @throws PersistenceException If an error occurs while trying to retrieve
   * the object from the persistence system.
   * @see #load(String)
   * @see IModelFactory#createPreference(String)
   */
  IPreference loadOrCreate(String key) throws PersistenceException;
  
  /**
   * If the persistence layer contains a preference with a key that matches
   * <code>key</code>, the preference is deleted and <code>true</code> is
   * returned. Otherwise, no action is taken and <code>false</code> is
   * returned.
   *
   * @param key The key under which the value is stored.
   * @return <code>true</code> if a preference exists with key matching
   * <code>key</code>. <code>false</code> otherwise.
   * @throws PersistenceException In case of an error while accessing the
   * persistance layer.
   */
  boolean delete(String key) throws PersistenceException;
}
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

import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Preference;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.dao.IPreferenceDAO;
import org.rssowl.core.persist.event.PreferenceEvent;
import org.rssowl.core.persist.event.PreferenceListener;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.persist.service.UniqueConstraintException;

import com.db4o.query.Query;

import java.util.List;

/**
 * Default implementation of {@link IPreferenceDAO}.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public class PreferencesDAOImpl extends AbstractEntityDAO<IPreference, PreferenceListener, PreferenceEvent> implements IPreferenceDAO {

  /**
   * Creates an instance of this class.
   */
  public PreferencesDAOImpl() {
    super(Preference.class, true);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#doSave(org.
   * rssowl.core.persist.IPersistable)
   */
  @Override
  protected void doSave(IPreference entity) {
    IPreference pref = load(entity.getKey());
    if (pref != null && pref != entity)
      throw new UniqueConstraintException("key", entity); //$NON-NLS-1$

    super.doSave(entity);
  }

  /*
   * @see org.rssowl.core.persist.dao.IPreferenceDAO#delete(java.lang.String)
   */
  @Override
  public boolean delete(String key) throws PersistenceException {
    List<IPreference> preferences = loadAll(key);
    if (preferences == null || preferences.isEmpty())
      return false;

    /*
     * Rare Bug in RSSOwl 2.0.x: It was possible to happen that more than one
     * preference was stored under the same key. The fix is to make sure that
     * upon delete, all instances of the preference are deleted and not just the
     * first.
     */
    for (IPreference pref : preferences) {
      delete(pref);
    }

    return true;
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected PreferenceEvent createDeleteEventTemplate(IPreference entity) {
    return null;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected PreferenceEvent createSaveEventTemplate(IPreference entity) {
    return null;
  }

  /*
   * @see org.rssowl.core.persist.dao.IPreferenceDAO#load(java.lang.String)
   */
  @Override
  public IPreference load(String key) throws PersistenceException {
    List<IPreference> prefs = loadAll(key);
    if (!prefs.isEmpty())
      return prefs.iterator().next();

    return null;
  }

  private List<IPreference> loadAll(String key) {
    Query query = fDb.query();
    query.constrain(fEntityClass);
    query.descend("fKey").constrain(key); //$NON-NLS-1$
    List<IPreference> prefs = getList(query);
    activateAll(prefs);

    return prefs;
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.IPreferenceDAO#loadOrCreate(java.lang.String)
   */
  @Override
  public IPreference loadOrCreate(String key) throws PersistenceException {
    IPreference pref = load(key);
    if (pref == null)
      return Owl.getModelFactory().createPreference(key);

    return pref;
  }
}
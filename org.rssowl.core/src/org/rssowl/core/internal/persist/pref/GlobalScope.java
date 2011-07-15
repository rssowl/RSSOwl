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

package org.rssowl.core.internal.persist.pref;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.dao.IPreferenceDAO;
import org.rssowl.core.persist.event.PreferenceEvent;
import org.rssowl.core.persist.event.PreferenceListener;
import org.rssowl.core.persist.pref.IPreferenceScope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of <code>IPreferencesScope</code> that asks the
 * <code>IPreferenesDAO</code> of the persistence layer for its Preferences.
 *
 * @author bpasero
 */
public class GlobalScope implements IPreferenceScope {
  private final Map<String, IPreference> fCache;
  private final IPreferenceScope fParent;
  private final IPreferenceDAO fPreferenceDAO;

  /**
   * @param parent the {@link IPreferenceScope} that is being consulted in case
   * this scope is not providing a value.
   */
  public GlobalScope(IPreferenceScope parent) {
    fParent = parent;
    fCache = new HashMap<String, IPreference>();
    fPreferenceDAO = InternalOwl.getDefault().getPersistenceService().getDAOService().getPreferencesDAO();
    registerListeners();
  }

  private void registerListeners() {
    fPreferenceDAO.addEntityListener(new PreferenceListener() {
      public void entitiesAdded(Set<PreferenceEvent> events) {
        synchronized (fCache) {
          for (PreferenceEvent event : events)
            fCache.put(event.getEntity().getKey(), event.getEntity());
        }
      }

      public void entitiesDeleted(Set<PreferenceEvent> events) {
        synchronized (fCache) {
          for (PreferenceEvent event : events)
            fCache.remove(event.getEntity().getKey());
        }
      }

      public void entitiesUpdated(Set<PreferenceEvent> events) {
        synchronized (fCache) {
          for (PreferenceEvent event : events)
            fCache.put(event.getEntity().getKey(), event.getEntity());
        }
      }
    });
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesScope#getParent()
   */
  public IPreferenceScope getParent() {
    return fParent;
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesScope#flush()
   */
  public void flush() {
    //Nothing to do here as the preferences are already saved on each put.
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#delete(java.lang.String
   * )
   */
  public void delete(String key) {
    fPreferenceDAO.delete(key);
  }

  /*
   * @see org.rssowl.core.persist.pref.IPreferenceScope#hasKey(java.lang.String)
   */
  public boolean hasKey(String key) {
    return (fPreferenceDAO.load(key) != null);
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesNode#getBoolean(java.lang
   * .String)
   */
  public boolean getBoolean(String key) {
    synchronized (fCache) {

      /* Consult Cache */
      IPreference cachedPref = fCache.get(key);
      if (cachedPref != null && cachedPref.getBoolean() != null)
        return cachedPref.getBoolean();

      /* Consult the Persistence Layer */
      IPreference pref = load(key);
      if (pref != null && pref.getBoolean() != null) {
        fCache.put(key, pref);
        return pref.getBoolean();
      }

      /* Ask Parent */
      boolean parentValue = fParent.getBoolean(key);

      /* Cache value from parent */
      pref = Owl.getModelFactory().createPreference(key);
      pref.putBooleans(parentValue);
      fCache.put(key, pref);

      return parentValue;
    }
  }

  private IPreference load(String key) {
    return fPreferenceDAO.load(key);
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesNode#getInteger(java.lang
   * .String)
   */
  public int getInteger(String key) {
    synchronized (fCache) {

      /* Consult Cache */
      IPreference cachedPref = fCache.get(key);
      if (cachedPref != null && cachedPref.getInteger() != null)
        return cachedPref.getInteger();

      /* Consult the Persistence Layer */
      IPreference pref = load(key);
      if (pref != null && pref.getInteger() != null) {
        fCache.put(key, pref);
        return pref.getInteger();
      }

      /* Ask Parent */
      int parentValue = fParent.getInteger(key);

      /* Cache value from parent */
      pref = Owl.getModelFactory().createPreference(key);
      pref.putIntegers(parentValue);
      fCache.put(key, pref);

      return parentValue;
    }
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesNode#getIntegers(java.lang
   * .String)
   */
  public int[] getIntegers(String key) {
    synchronized (fCache) {

      /* Consult Cache */
      IPreference cachedPref = fCache.get(key);
      if (cachedPref != null && cachedPref.getIntegers() != null)
        return cachedPref.getIntegers();

      /* Consult the Persistence Layer */
      IPreference pref = load(key);
      if (pref != null && pref.getIntegers() != null) {
        fCache.put(key, pref);
        return pref.getIntegers();
      }

      /* Ask Parent */
      int[] parentValue = fParent.getIntegers(key);

      /* Cache value from parent */
      if (parentValue != null) {
        pref = Owl.getModelFactory().createPreference(key);
        pref.putIntegers(parentValue);
        fCache.put(key, pref);
      }

      return parentValue;
    }
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesNode#getLong(java.lang.String
   * )
   */
  public long getLong(String key) {
    synchronized (fCache) {

      /* Consult Cache */
      IPreference cachedPref = fCache.get(key);
      if (cachedPref != null && cachedPref.getLong() != null)
        return cachedPref.getLong();

      /* Consult the Persistence Layer */
      IPreference pref = load(key);
      if (pref != null && pref.getLong() != null) {
        fCache.put(key, pref);
        return pref.getLong();
      }

      /* Ask Parent */
      long parentValue = fParent.getLong(key);

      /* Cache value from parent */
      pref = Owl.getModelFactory().createPreference(key);
      pref.putLongs(parentValue);
      fCache.put(key, pref);

      return parentValue;
    }
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesNode#getLongs(java.lang.String
   * )
   */
  public long[] getLongs(String key) {
    synchronized (fCache) {

      /* Consult Cache */
      IPreference cachedPref = fCache.get(key);
      if (cachedPref != null && cachedPref.getLongs() != null)
        return cachedPref.getLongs();

      /* Consult the Persistence Layer */
      IPreference pref = load(key);
      if (pref != null && pref.getLongs() != null) {
        fCache.put(key, pref);
        return pref.getLongs();
      }

      /* Ask Parent */
      long[] parentValue = fParent.getLongs(key);

      /* Cache value from parent */
      if (parentValue != null) {
        pref = Owl.getModelFactory().createPreference(key);
        pref.putLongs(parentValue);
        fCache.put(key, pref);
      }

      return parentValue;
    }
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesNode#getString(java.lang.
   * String)
   */
  public String getString(String key) {
    synchronized (fCache) {

      /* Consult Cache */
      IPreference cachedPref = fCache.get(key);
      if (cachedPref != null && cachedPref.getString() != null)
        return cachedPref.getString();

      /* Consult the Persistence Layer */
      IPreference pref = load(key);
      if (pref != null && pref.getString() != null) {
        fCache.put(key, pref);
        return pref.getString();
      }

      /* Ask Parent */
      String parentValue = fParent.getString(key);

      /* Cache value from parent */
      if (parentValue != null) {
        pref = Owl.getModelFactory().createPreference(key);
        pref.putStrings(parentValue);
        fCache.put(key, pref);
      }

      return parentValue;
    }
  }

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesNode#getStrings(java.lang
   * .String)
   */
  public String[] getStrings(String key) {
    synchronized (fCache) {

      /* Consult Cache */
      IPreference cachedPref = fCache.get(key);
      if (cachedPref != null && cachedPref.getStrings() != null)
        return cachedPref.getStrings();

      /* Consult the Persistence Layer */
      IPreference pref = load(key);
      if (pref != null && pref.getStrings() != null) {
        fCache.put(key, pref);
        return pref.getStrings();
      }

      /* Ask Parent */
      String[] parentValue = fParent.getStrings(key);

      /* Cache value from parent */
      if (parentValue != null) {
        pref = Owl.getModelFactory().createPreference(key);
        pref.putStrings(parentValue);
        fCache.put(key, pref);
      }

      return parentValue;
    }
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#putBoolean(java.lang
   * .String, boolean)
   */
  public void putBoolean(String key, boolean value) {

    /* Check if value is already up-to-date */
    if (cached(key, value))
      return;

    /* Delete if matches parent scope */
    if (value == fParent.getBoolean(key)) {
      delete(key);
      return;
    }

    /* Save to DB */
    IPreference pref = fPreferenceDAO.loadOrCreate(key);
    pref.putBooleans(value);
    fPreferenceDAO.save(pref);
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#putInteger(java.lang
   * .String, int)
   */
  public void putInteger(String key, int value) {

    /* Check if value is already up-to-date */
    if (cached(key, value))
      return;

    /* Delete if matches parent scope */
    if (value == fParent.getInteger(key)) {
      delete(key);
      return;
    }

    /* Save to DB */
    IPreference pref = fPreferenceDAO.loadOrCreate(key);
    pref.putIntegers(value);
    fPreferenceDAO.save(pref);
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#putIntegers(java.lang
   * .String, int[])
   */
  public void putIntegers(String key, int[] values) {
    Assert.isNotNull(values);

    /* Check if value is already up-to-date */
    if (cached(key, values))
      return;

    /* Delete if matches parent scope */
    if (Arrays.equals(values, fParent.getIntegers(key))) {
      delete(key);
      return;
    }

    /* Save to DB */
    IPreference pref = fPreferenceDAO.loadOrCreate(key);
    pref.putIntegers(values);
    fPreferenceDAO.save(pref);
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#putLong(java.lang.String
   * , long)
   */
  public void putLong(String key, long value) {
    /* Check if value is already up-to-date */
    if (cached(key, value))
      return;

    /* Delete if matches parent scope */
    if (value == fParent.getLong(key)) {
      delete(key);
      return;
    }

    /* Save to DB */
    IPreference pref = fPreferenceDAO.loadOrCreate(key);
    pref.putLongs(value);
    fPreferenceDAO.save(pref);
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#putLongs(java.lang.
   * String, long[])
   */
  public void putLongs(String key, long[] values) {
    Assert.isNotNull(values);

    /* Check if value is already up-to-date */
    if (cached(key, values))
      return;

    /* Delete if matches parent scope */
    if (Arrays.equals(values, fParent.getLongs(key))) {
      delete(key);
      return;
    }

    /* Save to DB */
    IPreference pref = fPreferenceDAO.loadOrCreate(key);
    pref.putLongs(values);
    fPreferenceDAO.save(pref);
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#putString(java.lang
   * .String, java.lang.String)
   */
  public void putString(String key, String value) {
    Assert.isNotNull(value);

    /* Check if value is already up-to-date */
    if (cached(key, value))
      return;

    /* Delete if matches parent scope */
    if (value.equals(fParent.getString(key))) {
      delete(key);
      return;
    }

    /* Save to DB */
    IPreference pref = fPreferenceDAO.loadOrCreate(key);
    pref.putStrings(value);
    fPreferenceDAO.save(pref);
  }

  /*
   * @see
   * org.rssowl.ui.internal.preferences.IPreferencesNode#putStrings(java.lang
   * .String, java.lang.String[])
   */
  public void putStrings(String key, String[] values) {
    Assert.isNotNull(values);

    /* Check if value is already up-to-date */
    if (cached(key, values))
      return;

    /* Delete if matches parent scope */
    if (Arrays.equals(values, fParent.getStrings(key))) {
      delete(key);
      return;
    }

    /* Save to DB */
    IPreference pref = fPreferenceDAO.loadOrCreate(key);
    pref.putStrings(values);
    fPreferenceDAO.save(pref);
  }

  /* Used from test methods to clear the global scope cache */
  public void clearCache() {
    fCache.clear();
  }

  //TODO Implement this (but see bug #429 for reference)
  private boolean cached(@SuppressWarnings("unused")
  String key, @SuppressWarnings("unused")
  Object value) {
    return false;
    //    synchronized (fCache) {
    //    IPreference cachedRes = fCache.get(key);
    //    }
    //    if (cachedRes == null)
    //      return false;
    //
    //    if (value instanceof Object[])
    //      return Arrays.equals((Object[]) cachedRes, (Object[]) value);
    //
    //    return cachedRes.equals(value);
  }
}
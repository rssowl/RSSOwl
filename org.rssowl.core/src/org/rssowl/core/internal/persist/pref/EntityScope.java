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
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;

import java.util.Arrays;

/**
 * Implementation of <code>IPreferencesScope</code> that asks the given
 * instance of <code>IEntity</code> for its Preferences.
 *
 * @author bpasero
 */
public class EntityScope implements IPreferenceScope {
  private final IEntity fEntity;
  private final IPreferenceScope fParent;

  /**
   * @param entity the {@link IEntity} to obtain preferences from
   * @param parent the {@link IPreferenceScope} that is being consulted in case
   * this scope is not providing a value.
   */
  public EntityScope(IEntity entity, IPreferenceScope parent) {
    Assert.isNotNull(entity, "entity cannot be null"); //$NON-NLS-1$
    fEntity = entity;
    fParent = parent;
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
    DynamicDAO.save(fEntity);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#delete(java.lang.String)
   */
  public void delete(String key) {
    fEntity.removeProperty(key);
  }

  /*
   * @see org.rssowl.core.persist.pref.IPreferenceScope#hasKey(java.lang.String)
   */
  public boolean hasKey(String key) {
    return fEntity.getProperty(key) != null;
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getBoolean(java.lang.String)
   */
  public boolean getBoolean(String key) {

    /* Ask Entity */
    Object prop = fEntity.getProperty(key);
    if (prop != null)
      return (Boolean) prop;

    /* Ask Parent */
    return fParent.getBoolean(key);
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getInteger(java.lang.String)
   */
  public int getInteger(String key) {

    /* Ask Entity */
    Object prop = fEntity.getProperty(key);
    if (prop != null)
      return (Integer) prop;

    /* Ask Parent */
    return fParent.getInteger(key);
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getIntegers(java.lang.String)
   */
  public int[] getIntegers(String key) {

    /* Ask Entity */
    Object prop = fEntity.getProperty(key);
    if (prop != null)
      return (int[]) prop;

    /* Ask Parent */
    return fParent.getIntegers(key);
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getLong(java.lang.String)
   */
  public long getLong(String key) {

    /* Ask Entity */
    Object prop = fEntity.getProperty(key);
    if (prop != null)
      return (Long) prop;

    /* Ask Parent */
    return fParent.getLong(key);
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getLongs(java.lang.String)
   */
  public long[] getLongs(String key) {

    /* Ask Entity */
    Object prop = fEntity.getProperty(key);
    if (prop != null)
      return (long[]) prop;

    /* Ask Parent */
    return fParent.getLongs(key);
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getString(java.lang.String)
   */
  public String getString(String key) {

    /* Ask Entity */
    Object prop = fEntity.getProperty(key);
    if (prop != null)
      return (String) prop;

    /* Ask Parent */
    return fParent.getString(key);
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getStrings(java.lang.String)
   */
  public String[] getStrings(String key) {

    /* Ask Entity */
    Object prop = fEntity.getProperty(key);
    if (prop != null)
      return (String[]) prop;

    /* Ask Parent */
    return fParent.getStrings(key);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putBoolean(java.lang.String,
   * boolean)
   */
  public void putBoolean(String key, boolean value) {
    if (value != fParent.getBoolean(key))
      fEntity.setProperty(key, value);
    else
      delete(key);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putInteger(java.lang.String,
   * int)
   */
  public void putInteger(String key, int value) {
    if (value != fParent.getInteger(key))
      fEntity.setProperty(key, value);
    else
      delete(key);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putIntegers(java.lang.String,
   * int[])
   */
  public void putIntegers(String key, int[] values) {
    Assert.isNotNull(values);

    if (!Arrays.equals(values, fParent.getIntegers(key)))
      fEntity.setProperty(key, values);
    else
      delete(key);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putLong(java.lang.String,
   * long)
   */
  public void putLong(String key, long value) {
    if (value != fParent.getLong(key))
      fEntity.setProperty(key, value);
    else
      delete(key);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putLongs(java.lang.String,
   * long[])
   */
  public void putLongs(String key, long[] values) {
    Assert.isNotNull(values);

    if (!Arrays.equals(values, fParent.getLongs(key)))
      fEntity.setProperty(key, values);
    else
      delete(key);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putString(java.lang.String,
   * java.lang.String)
   */
  public void putString(String key, String value) {
    Assert.isNotNull(value);

    if (!value.equals(fParent.getString(key)))
      fEntity.setProperty(key, value);
    else
      delete(key);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putStrings(java.lang.String,
   * java.lang.String[])
   */
  public void putStrings(String key, String[] values) {
    Assert.isNotNull(values);

    if (!Arrays.equals(values, fParent.getStrings(key)))
      fEntity.setProperty(key, values);
    else
      delete(key);
  }
}
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

import org.rssowl.core.persist.pref.IPreferenceScope;

import java.util.Properties;

/**
 * Implementation of <code>IPreferencesScope</code> that maintains a
 * Property-List of Preferences. These are entirely held in Memory and never
 * stored persistently. The <code>DefaultScope</code> can be used to store
 * initial preferences.
 *
 * @author bpasero
 */
public class DefaultScope implements IPreferenceScope {

  /* Default Values */
  private static final boolean BOOLEAN_DEFAULT = false;
  private static final int INT_DEFAULT = 0;
  private static final long LONG_DEFAULT = 0L;

  private final Properties fDefaults = new Properties();

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesScope#getParent()
   */
  public IPreferenceScope getParent() {
    throw new RuntimeException("There can be no parent of the DefaultScope"); //$NON-NLS-1$
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesScope#flush()
   */
  public void flush() {
  /* Nothing to do here (DefaultScope only kept in Memory) */
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#delete(java.lang.String)
   */
  public void delete(String key) {
    fDefaults.remove(key);
  }

  /*
   * @see org.rssowl.core.persist.pref.IPreferenceScope#hasKey(java.lang.String)
   */
  public boolean hasKey(String key) {
    return fDefaults.containsKey(key);
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getBoolean(java.lang.String)
   */
  public boolean getBoolean(String key) {
    Object res = fDefaults.get(key);
    if (res != null)
      return (Boolean) res;

    return BOOLEAN_DEFAULT;
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getInteger(java.lang.String)
   */
  public int getInteger(String key) {
    Object res = fDefaults.get(key);
    if (res != null)
      return (Integer) res;

    return INT_DEFAULT;
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getIntegers(java.lang.String)
   */
  public int[] getIntegers(String key) {
    Object res = fDefaults.get(key);
    if (res != null)
      return (int[]) res;

    return null;
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getLong(java.lang.String)
   */
  public long getLong(String key) {
    Object res = fDefaults.get(key);
    if (res != null)
      return (Long) res;

    return LONG_DEFAULT;
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getLongs(java.lang.String)
   */
  public long[] getLongs(String key) {
    Object res = fDefaults.get(key);
    if (res != null)
      return (long[]) res;

    return null;
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getString(java.lang.String)
   */
  public String getString(String key) {
    Object res = fDefaults.get(key);
    if (res != null)
      return (String) res;

    return null;
  }

  /*
   * @see org.rssowl.core.model.preferences.IPreferencesNode#getStrings(java.lang.String)
   */
  public String[] getStrings(String key) {
    Object res = fDefaults.get(key);
    if (res != null)
      return (String[]) res;

    return null;
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putBoolean(java.lang.String,
   * boolean)
   */
  public void putBoolean(String key, boolean value) {
    if (value == BOOLEAN_DEFAULT)
      delete(key);
    else
      fDefaults.put(key, value);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putInteger(java.lang.String,
   * int)
   */
  public void putInteger(String key, int value) {
    if (value == INT_DEFAULT)
      delete(key);
    else
      fDefaults.put(key, value);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putIntegers(java.lang.String,
   * int[])
   */
  public void putIntegers(String key, int[] values) {
    if (values == null)
      delete(key);
    else
      fDefaults.put(key, values);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putLong(java.lang.String,
   * long)
   */
  public void putLong(String key, long value) {
    if (value == LONG_DEFAULT)
      delete(key);
    else
      fDefaults.put(key, value);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putLongs(java.lang.String,
   * long[])
   */
  public void putLongs(String key, long[] values) {
    if (values == null)
      delete(key);
    else
      fDefaults.put(key, values);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putString(java.lang.String,
   * java.lang.String)
   */
  public void putString(String key, String value) {
    if (value == null)
      delete(key);
    else
      fDefaults.put(key, value);
  }

  /*
   * @see org.rssowl.ui.internal.preferences.IPreferencesNode#putStrings(java.lang.String,
   * java.lang.String[])
   */
  public void putStrings(String key, String[] values) {
    if (values == null)
      delete(key);
    else
      fDefaults.put(key, values);
  }
}
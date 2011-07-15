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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.persist.pref.IPreferenceScope;

/**
 * Implementation of <code>IPreferencesScope</code> that receives preferences
 * using Eclipse's preference mechanisms.
 *
 * @see IEclipsePreferences
 * @see IPreferencesService
 * @author bpasero
 */
public class EclipseScope implements IPreferenceScope {
  private static final String NODE_SEPARATOR = "/"; //$NON-NLS-1$
  private static final String ROOT_NAME = NODE_SEPARATOR;

  private final IPreferenceScope fParent;
  private final IPreferencesService fPrefService;
  private final IEclipsePreferences fRootNode;

  /**
   * @param parent the {@link IPreferenceScope} that is being consulted in case
   * this scope is not providing a value.
   */
  public EclipseScope(IPreferenceScope parent) {
    fParent = parent;
    fPrefService = Platform.getPreferencesService();
    fRootNode = fPrefService.getRootNode();
  }

  /*
   * @see org.rssowl.core.persist.pref.IPreferenceScope#getParent()
   */
  public IPreferenceScope getParent() {
    return fParent;
  }

  /*
   * @see org.rssowl.core.persist.pref.IPreferenceScope#flush()
   */
  public void flush() {
    try {
      fRootNode.flush();
    } catch (BackingStoreException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }
  }

  /*
   * @see org.rssowl.core.persist.pref.IPreferenceScope#hasKey(java.lang.String)
   */
  public boolean hasKey(String key) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see org.rssowl.core.persist.pref.IPreferenceScope#delete(java.lang.String)
   */
  public void delete(String key) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#getBoolean(java.lang.String)
   */
  public boolean getBoolean(String key) {
    return fPrefService.getBoolean(ROOT_NAME, key, fParent.getBoolean(key), null);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#getInteger(java.lang.String)
   */
  public int getInteger(String key) {
    return fPrefService.getInt(ROOT_NAME, key, fParent.getInteger(key), null);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#getIntegers(java.lang.String)
   */
  public int[] getIntegers(String key) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#getLong(java.lang.String)
   */
  public long getLong(String key) {
    return fPrefService.getLong(ROOT_NAME, key, fParent.getLong(key), null);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#getLongs(java.lang.String)
   */
  public long[] getLongs(String key) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#getString(java.lang.String)
   */
  public String getString(String key) {
    return fPrefService.getString(ROOT_NAME, key, fParent.getString(key), null);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#getStrings(java.lang.String)
   */
  public String[] getStrings(String key) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#putBoolean(java.lang.String,
   * boolean)
   */
  public void putBoolean(String key, boolean value) {
    Assert.isTrue(key.contains(NODE_SEPARATOR), "Invalid Eclipse Preferences Key!"); //$NON-NLS-1$

    String nodePath = getNodePath(key);
    key = key.substring(key.lastIndexOf(NODE_SEPARATOR) + 1);

    Preferences prefNode = fRootNode.node(nodePath);
    prefNode.putBoolean(key, value);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#putInteger(java.lang.String,
   * int)
   */
  public void putInteger(String key, int value) {
    Assert.isTrue(key.contains(NODE_SEPARATOR), "Invalid Eclipse Preferences Key!"); //$NON-NLS-1$

    String nodePath = getNodePath(key);
    key = key.substring(key.lastIndexOf(NODE_SEPARATOR) + 1);

    Preferences prefNode = fRootNode.node(nodePath);
    prefNode.putInt(key, value);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#putIntegers(java.lang.String,
   * int[])
   */
  public void putIntegers(String key, int[] values) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#putLong(java.lang.String,
   * long)
   */
  public void putLong(String key, long value) {
    Assert.isTrue(key.contains(NODE_SEPARATOR), "Invalid Eclipse Preferences Key!"); //$NON-NLS-1$

    String nodePath = getNodePath(key);
    key = key.substring(key.lastIndexOf(NODE_SEPARATOR) + 1);

    Preferences prefNode = fRootNode.node(nodePath);
    prefNode.putLong(key, value);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#putLongs(java.lang.String,
   * long[])
   */
  public void putLongs(String key, long[] values) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#putString(java.lang.String,
   * java.lang.String)
   */
  public void putString(String key, String value) {
    Assert.isTrue(key.contains(NODE_SEPARATOR), "Invalid Eclipse Preferences Key!"); //$NON-NLS-1$

    String nodePath = getNodePath(key);
    key = key.substring(key.lastIndexOf(NODE_SEPARATOR) + 1);

    Preferences prefNode = fRootNode.node(nodePath);
    prefNode.put(key, value);
  }

  /*
   * @see
   * org.rssowl.core.persist.pref.IPreferenceScope#putStrings(java.lang.String,
   * java.lang.String[])
   */
  public void putStrings(String key, String[] values) {
    throw new UnsupportedOperationException();
  }

  private String getNodePath(String key) {
    if (key.startsWith("/")) //$NON-NLS-1$
      return key.substring(0, key.lastIndexOf(NODE_SEPARATOR));

    return ROOT_NAME + key.substring(0, key.lastIndexOf(NODE_SEPARATOR));
  }
}
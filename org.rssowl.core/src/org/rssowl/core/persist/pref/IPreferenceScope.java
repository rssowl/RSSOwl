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

package org.rssowl.core.persist.pref;

/**
 * Instances of <code>IPreferenceScope</code> provide access to Preferences for
 * a certain Scope. In case a Preference is not present in the given Scope, any
 * parent scope is asked for a value if given.
 * <p>
 * Whenever preferences are updated, make sure to call the flush()-Method, to
 * write the changes to the underlying persistence-layer.
 * </p>
 *
 * @author bpasero
 */
public interface IPreferenceScope {

  /** Supported Scopes for Preferences */
  public enum Kind {

    /** Application Global */
    GLOBAL,

    /** Eclipse */
    ECLIPSE,

    /** Entity */
    ENTITY
  }

  /**
   * Returns the parent scope to lookup Preferences in case the actual scope is
   * missing preference-values.
   *
   * @return The parent scope or <code>NULL</code> if none.
   */
  public IPreferenceScope getParent();

  /**
   * Writes the made changes to the underlying persistence layer.
   */
  public void flush();

  /**
   * Deletes a preference from this Scope.
   *
   * @param key The Key under which the value is stored.
   */
  void delete(String key);

  /**
   * Retrieves a <code>Boolean</code> value from this Scope. Asks the parent
   * Scope for the Value in case it is not present at this Scope.
   *
   * @param key The Key under which the value is stored.
   * @return The <code>Boolean</code> value from this Scope or the parent Scope.
   */
  boolean getBoolean(String key);

  /**
   * Retrieves a <code>Integer</code> value from this Scope. Asks the parent
   * Scope for the Value in case it is not present at this Scope.
   *
   * @param key The Key under which the value is stored.
   * @return The <code>Integer</code> value from this Scope or the parent Scope.
   */
  int getInteger(String key);

  /**
   * Retrieves a <code>int</code> array from this Scope. Asks the parent Scope
   * for the Value in case it is not present at this Scope.
   *
   * @param key The Key under which the value is stored.
   * @return The <code>int</code> array from this Scope or the parent Scope.
   */
  int[] getIntegers(String key);

  /**
   * Retrieves a <code>Long</code> value from this Scope. Asks the parent Scope
   * for the Value in case it is not present at this Scope.
   *
   * @param key The Key under which the value is stored.
   * @return The <code>Long</code> value from this Scope or the parent Scope.
   */
  long getLong(String key);

  /**
   * Retrieves a <code>long</code> array from this Scope. Asks the parent Scope
   * for the Value in case it is not present at this Scope.
   * <p>
   * Note: The underlying persistence solution is making sure to keep the order
   * of Items inside the Array when saving and loading.
   * </p>
   *
   * @param key The Key under which the value is stored.
   * @return The <code>long</code> array from this Scope or the parent Scope.
   */
  long[] getLongs(String key);

  /**
   * Retrieves a <code>String</code> value from this Scope. Asks the parent
   * Scope for the Value in case it is not present at this Scope.
   *
   * @param key The Key under which the value is stored.
   * @return The <code>String</code> value from this Scope or the parent Scope.
   */
  String getString(String key);

  /**
   * Retrieves a <code>String</code> array from this Scope. Asks the parent
   * Scope for the Value in case it is not present at this Scope.
   * <p>
   * Note: The underlying persistence solution is making sure to keep the order
   * of Items inside the Array when saving and loading.
   * </p>
   *
   * @param key The Key under which the value is stored.
   * @return The <code>String</code> array from this Scope or the parent Scope.
   */
  String[] getStrings(String key);

  /**
   * Stores a <code>boolean</code> value under the given key into the
   * persistance layer or updates it, if it is already present.
   *
   * @param key The key under which the value is stored.
   * @param value The <code>boolean</code> value that is to be stored.
   */
  void putBoolean(String key, boolean value);

  /**
   * Stores a <code>int</code> value under the given key into the persistance
   * layer or updates it, if it is already present.
   *
   * @param key The key under which the value is stored.
   * @param value The <code>int</code> value that is to be stored.
   */
  void putInteger(String key, int value);

  /**
   * Stores a <code>int</code> array under the given key into the persistance
   * layer or updates it, if it is already present.
   *
   * @param key The key under which the value is stored.
   * @param values The <code>int</code> array that is to be stored.
   */
  void putIntegers(String key, int values[]);

  /**
   * Stores a <code>long</code> value under the given key into the persistance
   * layer or updates it, if it is already present.
   *
   * @param key The key under which the value is stored.
   * @param value The <code>long</code> value that is to be stored.
   */
  void putLong(String key, long value);

  /**
   * Stores a <code>long</code> array under the given key into the persistance
   * layer or updates it, if it is already present.
   * <p>
   * Note: The underlying persistence solution is making sure to keep the order
   * of Items inside the Array when saving and loading.
   * </p>
   *
   * @param key The key under which the value is stored.
   * @param values The <code>long</code> array that is to be stored.
   */
  void putLongs(String key, long values[]);

  /**
   * Stores a <code>String</code> value under the given key into the persistance
   * layer or updates it, if it is already present.
   *
   * @param key The key under which the value is stored.
   * @param value The <code>String</code> value that is to be stored.
   */
  void putString(String key, String value);

  /**
   * Stores a <code>String</code> array under the given key into the persistance
   * layer or updates it, if it is already present.
   * <p>
   * Note: The underlying persistence solution is making sure to keep the order
   * of Items inside the Array when saving and loading.
   * </p>
   *
   * @param key The key under which the value is stored.
   * @param values The <code>String</code> array that is to be stored.
   */
  void putStrings(String key, String values[]);

  /**
   * Returns <code>true</code> if this scope provides a value for the given key
   * or <code>false</code> otherwise.
   *
   * @param key the ID of the key to look for.
   * @return <code>true</code> if this scope provides a value for the given key
   * or <code>false</code> otherwise.
   */
  boolean hasKey(String key);
}
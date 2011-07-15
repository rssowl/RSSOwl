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

package org.rssowl.core.persist;

/**
 * Instances of <code>IPreference</code> are capable of storing a certain
 * preference value (of the Type String, Long, Integer or Boolean) under a
 * certain Key. The value can either be a single one or an array.
 *
 * @author bpasero
 */
public interface IPreference extends IEntity {

  /** The Type of Preference Value */
  public enum Type {

    /** A Boolean Value */
    BOOLEAN,

    /** An Integer Value */
    INTEGER,

    /** A Long Value */
    LONG,

    /** A String Value */
    STRING
  };

  /**
   * @return Returns the Key that maps to the value of this Preference.
   */
  String getKey();

  /**
   * @return Returns the Type of the Value of this Preference.
   */
  Type getType();

  /**
   * @return Returns the <code>Boolean</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#BOOLEAN}.
   */
  Boolean getBoolean();

  /**
   * @return Returns the <code>boolean[]</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#BOOLEAN}.
   */
  boolean[] getBooleans();

  /**
   * @return Returns the <code>Integer</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#INTEGER}.
   */
  Integer getInteger();

  /**
   * @return Returns the <code>int[]</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#INTEGER}.
   */
  int[] getIntegers();

  /**
   * @return Returns the <code>Long</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#LONG}.
   */
  Long getLong();

  /**
   * @return Returns the <code>long[]</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#LONG}.
   */
  long[] getLongs();

  /**
   * @return Returns the <code>String</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#STRING}.
   */
  String getString();

  /**
   * @return Returns the <code>String[]</code> value from this Preference.
   *
   * @throws IllegalArgumentException if {@link #getType()} is not
   * {@linkplain Type#STRING}.
   */
  String[] getStrings();

  /**
   * Stores the given values into this Preference, replacing any previously
   * stores values.
   *
   * @param strings The <code>String</code>s to add as preference value.
   */
  void putStrings(String... strings);

  /**
   * Stores the given values into this Preference, replacing any previously
   * stores values.
   *
   * @param longs The <code>long</code>s to add as preference value.
   */
  void putLongs(long... longs);

  /**
   * Stores the given values into this Preference, replacing any previously
   * stores values.
   *
   * @param integers The <code>int</code>s to add as preference value.
   */
  void putIntegers(int... integers);

  /**
   * Stores the given values into this Preference, replacing any previously
   * stores values.
   *
   * @param booleans The <code>boolean</code>s to add as preference value.
   */
  void putBooleans(boolean... booleans);

  /**
   * Clears the stored value from this preference.
   */
  void clear();
}
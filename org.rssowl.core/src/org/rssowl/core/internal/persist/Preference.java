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

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.reference.ModelReference;

/**
 * Instances of <code>IPreference</code> are capable of storing a certain
 * preference value (of the Type String, Long, or Boolean) under a certain Key.
 *
 * @author bpasero
 */
public final class Preference extends AbstractEntity implements IPreference {
  private String fKey;
  private Type fType;
  private String[] fValues;
  private transient Object fCachedValues;

  /**
   * Provided for deserialization purposes.
   */
  protected Preference() {}

  /**
   * @param key
   */
  public Preference(String key) {
    Assert.isNotNull(key, "key cannot be null"); //$NON-NLS-1$
    this.fKey = key;
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getKey()
   */
  @Override
  public synchronized final String getKey() {
    return fKey;
  }

  /*
   * @see org.rssowl.core.persist.IPreference#getType()
   */
  @Override
  public synchronized final Type getType() {
    return fType;
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getBoolean()
   */
  @Override
  public synchronized final Boolean getBoolean() {
    boolean[] values = getBooleans();
    if (values != null && values.length > 0)
      return values[0];

    return null;
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getBooleans()
   */
  @Override
  public synchronized final boolean[] getBooleans() {
    if (fValues == null)
      return null;
    checkType(Type.BOOLEAN);

    boolean[] cachedValues = (boolean[]) fCachedValues;
    if (fCachedValues != null)
      return copyOf(cachedValues);

    cachedValues = new boolean[fValues.length];
    int index = 0;
    for (String value : fValues) {
      cachedValues[index++] = Boolean.valueOf(value);
    }
    fCachedValues = cachedValues;
    return copyOf(cachedValues);
  }

  private void checkType(Type type) {
    if (fType != type)
      Assert.isLegal(fType == type, "The type of the Preference is not of the expected " + "type. It should be: " + fType + ", but it is: " + type); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  private boolean[] copyOf(boolean[] original) {
    boolean[] copy = new boolean[original.length];
    System.arraycopy(original, 0, copy, 0, original.length);
    return copy;
  }

  private int[] copyOf(int[] original) {
    int[] copy = new int[original.length];
    System.arraycopy(original, 0, copy, 0, original.length);
    return copy;
  }

  private long[] copyOf(long[] original) {
    long[] copy = new long[original.length];
    System.arraycopy(original, 0, copy, 0, original.length);
    return copy;
  }

  private String[] copyOf(String[] original) {
    String[] copy = new String[original.length];
    System.arraycopy(original, 0, copy, 0, original.length);
    return copy;
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getInteger()
   */
  @Override
  public synchronized final Integer getInteger() {
    int[] values = getIntegers();
    if (values != null && values.length > 0)
      return values[0];

    return null;
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getIntegers()
   */
  @Override
  public synchronized final int[] getIntegers() {
    if (fValues == null)
      return null;
    checkType(Type.INTEGER);

    int[] cachedValues = (int[]) fCachedValues;
    if (fCachedValues != null)
      return copyOf(cachedValues);

    cachedValues = new int[fValues.length];
    int index = 0;
    for (String value : fValues) {
      cachedValues[index++] = Integer.valueOf(value);
    }
    fCachedValues = cachedValues;
    return copyOf(cachedValues);
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getLong()
   */
  @Override
  public synchronized final Long getLong() {
    long[] values = getLongs();
    if (values != null && values.length > 0)
      return values[0];

    return null;
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getLongs()
   */
  @Override
  public synchronized final long[] getLongs() {
    if (fValues == null)
      return null;
    checkType(Type.LONG);

    long[] cachedValues = (long[]) fCachedValues;
    if (fCachedValues != null)
      return copyOf(cachedValues);

    cachedValues = new long[fValues.length];
    int index = 0;
    for (String value : fValues) {
      cachedValues[index++] = Long.valueOf(value);
    }
    fCachedValues = cachedValues;
    return copyOf(cachedValues);
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getString()
   */
  @Override
  public synchronized final String getString() {
    String[] values = getStrings();
    if (values != null && values.length > 0)
      return values[0];

    return null;
  }

  /*
   * @see org.rssowl.core.internal.persist.pref.T#getStrings()
   */
  @Override
  public synchronized final String[] getStrings() {
    if (fValues == null)
      return null;
    checkType(Type.STRING);

    return copyOf(fValues);
  }

  /*
   * @see org.rssowl.core.persist.IPreference#putStrings(java.lang.String[])
   */
  @Override
  public synchronized final void putStrings(String... strings) {
    if (strings == null) {
      clear();
      return;
    }
    fType = Type.STRING;
    String[] cachedValues = copyOf(strings);
    fCachedValues = cachedValues;
    fValues = cachedValues;
  }

  /*
   * @see org.rssowl.core.persist.IPreference#putLongs(long[])
   */
  @Override
  public synchronized final void putLongs(long... longs) {
    if (longs == null) {
      clear();
      return;
    }
    fType = Type.LONG;
    long[] cachedValues = copyOf(longs);
    fCachedValues = cachedValues;
    fValues = new String[cachedValues.length];
    int index = 0;
    for (long cachedValue : cachedValues) {
      fValues[index++] = String.valueOf(cachedValue);
    }
  }

  /*
   * @see org.rssowl.core.persist.IPreference#putIntegers(int[])
   */
  @Override
  public synchronized final void putIntegers(int... integers) {
    if (integers == null) {
      clear();
      return;
    }
    fType = Type.INTEGER;
    int[] cachedValues = copyOf(integers);
    fCachedValues = cachedValues;
    fValues = new String[cachedValues.length];
    int index = 0;
    for (int cachedValue : cachedValues) {
      fValues[index++] = String.valueOf(cachedValue);
    }
  }

  /*
   * @see org.rssowl.core.persist.IPreference#putBooleans(boolean[])
   */
  @Override
  public synchronized final void putBooleans(boolean... booleans) {
    if (booleans == null) {
      clear();
      return;
    }
    fType = Type.BOOLEAN;
    boolean[] cachedValues = copyOf(booleans);
    fCachedValues = cachedValues;
    fValues = new String[cachedValues.length];
    int index = 0;
    for (boolean cachedValue : cachedValues) {
      fValues[index++] = String.valueOf(cachedValue);
    }
  }

  /*
   * @see org.rssowl.core.persist.IPreference#clear()
   */
  @Override
  public synchronized final void clear() {
    fValues = null;
    fType = null;
    fCachedValues = null;
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  @Override
  public ModelReference toReference() {
    //TODO We don't have PreferenceReference atm, should probably add them
    //for consistency
    throw new UnsupportedOperationException();
  }
}
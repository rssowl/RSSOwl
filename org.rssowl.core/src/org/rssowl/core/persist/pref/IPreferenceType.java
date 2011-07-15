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
 * An enumeration listing the supported data types of preferences stored via
 * {@link IPreferenceScope}.
 *
 * @author bpasero
 */
public enum IPreferenceType {

  /** Datatype: Boolean */
  BOOLEAN,

  /** Datatype: Integer */
  INTEGER,

  /** Datatype: int[] */
  INTEGERS,

  /** Datatype: Long */
  LONG,

  /** Datatype: long[] */
  LONGS,

  /** Datatype: String */
  STRING,

  /** Datatype: String[] */
  STRINGS;

  /**
   * @param obj the object to infere the {@link IPreferenceType} from.
   * @return the infered {@link IPreferenceType} from the given object.
   */
  public static IPreferenceType getType(Object obj) {
    if (obj instanceof String)
      return STRING;

    if (obj instanceof Long)
      return LONG;

    if (obj instanceof Integer)
      return INTEGER;

    if (obj instanceof Boolean)
      return BOOLEAN;

    if (obj instanceof long[])
      return LONGS;

    if (obj instanceof int[])
      return INTEGERS;

    if (obj instanceof String[])
      return STRINGS;

    return STRING;
  }
}
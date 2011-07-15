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

package org.rssowl.core.internal.persist.migration;

import java.lang.reflect.Field;

/**
 * Some helpers around Migrations.
 */
final class MigrationHelper {

  static Object getFieldValue(Object object, String fieldName) {
    try {
      return getField(object, fieldName).get(object);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }

  static Field getField(Object object, String fieldName) {
    Class<?> klass = object.getClass();
    while (klass != Object.class) {
      for (Field field : klass.getDeclaredFields()) {
        if (field.getName().equals(fieldName)) {
          field.setAccessible(true);
          return field;
        }
      }
      klass = klass.getSuperclass();
    }
    throw new IllegalArgumentException("No field with name: " + fieldName); //$NON-NLS-1$
  }

  static void setField(Object object, String fieldName, Object value) {
    try {
      Field field = getField(object, fieldName);
      field.set(object, value);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
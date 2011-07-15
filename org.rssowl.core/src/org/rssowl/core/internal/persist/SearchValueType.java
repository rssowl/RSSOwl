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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.rssowl.core.persist.ISearchValueType;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Instances of <code>ISearchValueType</code> describe the data-type for a
 * search-field. Most types reflect well known ones as used in relational
 * databases. They are helpful to validate a search-value for a given field and
 * to perform the search in the persistance layer.
 * </p>
 * <p>
 * In case the data-type is <code>ENUM</code>, a call to
 * <code>getEnumValues()</code> has to be used in order to retrieve the valid
 * search-values.
 * </p>
 *
 * @author bpasero
 */
public class SearchValueType implements ISearchValueType {
  private final int fType;
  private List<String> fEnumValues;

  /* Commonly used STRING Type */
  static final SearchValueType STRING = new SearchValueType(ISearchValueType.STRING);

  /* Commonly used INTEGER Type */
  static final SearchValueType INTEGER = new SearchValueType(ISearchValueType.INTEGER);

  /* Commonly used DATETIME Type */
  static final SearchValueType DATETIME = new SearchValueType(ISearchValueType.DATETIME);

  /* Commonly used BOOLEAN Type */
  static final SearchValueType BOOLEAN = new SearchValueType(ISearchValueType.BOOLEAN);

  /* Commonly used LINK Type */
  static final SearchValueType LINK = new SearchValueType(ISearchValueType.LINK);

  /**
   * Instantiates a new SearchValueType that is of any Type <em>not</em>
   * <code>ENUM</code>.
   *
   * @param type One of the constants as defined in
   * <code>ISearchValueType</code>
   */
  public SearchValueType(int type) {
    Assert.isLegal(type != ENUM, "Use the other constructor to supply a list of Enumeration values."); //$NON-NLS-1$
    fType = type;
  }

  /**
   * Instantiates a new SearchValueType that is of the Type <code>ENUM</code>.
   *
   * @param enumValues A List of allowed values as Strings.
   */
  public SearchValueType(List<String> enumValues) {
    Assert.isNotNull(enumValues, "The type SearchValueType of Type ENUM requires a List of Enum-Values that is not NULL"); //$NON-NLS-1$
    fEnumValues = enumValues;
    fType = ENUM;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchValueType#getSearchValueType()
   */
  public synchronized int getId() {
    return fType;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchValueType#getEnumValues()
   */
  public synchronized List<String> getEnumValues() {
    return Collections.unmodifiableList(fEnumValues);
  }

  /**
   * Returns an object which is an instance of the given class associated with
   * this object. Returns <code>null</code> if no such object can be found.
   * <p>
   * This implementation of the method declared by <code>IAdaptable</code>
   * passes the request along to the platform's adapter manager; roughly
   * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
   * Subclasses may override this method (however, if they do so, they should
   * invoke the method on their superclass to ensure that the Platform's adapter
   * manager is consulted).
   * </p>
   *
   * @param adapter the class to adapt to
   * @return the adapted object or <code>null</code>
   * @see IAdaptable#getAdapter(Class)
   * @see Platform#getAdapterManager()
   */
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public synchronized boolean equals(Object obj) {
    if (this == obj)
      return true;

    if ((obj == null) || (obj.getClass() != getClass()))
      return false;

    synchronized (obj) {
      SearchValueType s = (SearchValueType) obj;

      /* Compare Enum of Values if types are ENUM */
      if (fType == ENUM && s.fType == ENUM)
        return fEnumValues.equals(s.fEnumValues);

      /* Compare Type */
      return fType == s.fType;
    }
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public synchronized int hashCode() {
    if (fType != ENUM)
      return ((fType * getClass().hashCode() + 17)) * 37;
    return fEnumValues.hashCode();
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public synchronized String toString() {
    String type;
    switch (fType) {
      case ISearchValueType.BOOLEAN:
        type = "Boolean"; //$NON-NLS-1$
        break;
      case ISearchValueType.DATE:
        type = "Date"; //$NON-NLS-1$
        break;
      case ISearchValueType.DATETIME:
        type = "DateTime"; //$NON-NLS-1$
        break;
      case ISearchValueType.ENUM:
        type = "Enum"; //$NON-NLS-1$
        break;
      case ISearchValueType.INTEGER:
        type = "Integer"; //$NON-NLS-1$
        break;
      case ISearchValueType.NUMBER:
        type = "Number"; //$NON-NLS-1$
        break;
      case ISearchValueType.STRING:
        type = "String"; //$NON-NLS-1$
        break;
      case ISearchValueType.TIME:
        type = "Time"; //$NON-NLS-1$
        break;
      case ISearchValueType.LINK:
        type = "Link"; //$NON-NLS-1$
        break;
      default:
        type = "Unknown"; //$NON-NLS-1$
    }

    if (fType != ENUM)
      return super.toString() + "(Type = " + type + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    return super.toString() + "(Type = " + type + ", Values = " + fEnumValues + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
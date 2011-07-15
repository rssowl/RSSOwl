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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * A search-specifier describes how the search condition is to be performed.
 * More specifically it describes how target types have to match the
 * search-value in order to be matching. Some examples are "is", "is not" and
 * "begins with".
 *
 * @author bpasero
 */
public enum SearchSpecifier implements IPersistable {

  /** Target must match Value */
  IS,

  /** Target must not match Value */
  IS_NOT,

  /** Target is to contain Value (any) */
  CONTAINS,

  /** Target must not contain Value (any) */
  CONTAINS_NOT,

  /** Target is to begin with Value */
  BEGINS_WITH,

  /** Target is to end with Value */
  ENDS_WITH,

  /** Target is to be before Value (Date, Time) */
  IS_BEFORE,

  /** Target is to be after Value (Date, Time) */
  IS_AFTER,

  /** Target is to be greater than Value (Integer, Number) */
  IS_GREATER_THAN,

  /** Target is to be less than Value (Integer, Number) */
  IS_LESS_THAN,

  /** Target is similiar to the Value (String) */
  SIMILIAR_TO,

  /** Target is to contain Value (all) */
  CONTAINS_ALL,

  /** Location Scope */
  SCOPE;

  /**
   * Get a human-readable representation of the specifier to be used in the UI
   * for example.
   *
   * @return Returns a human-readable representation of the specifier.
   */
  public String getName() {
    switch (this) {
      case IS:
        return Messages.SearchSpecifier_IS;
      case IS_NOT:
        return Messages.SearchSpecifier_ISNT;
      case CONTAINS:
        return Messages.SearchSpecifier_CONTAINS_ANY;
      case CONTAINS_ALL:
        return Messages.SearchSpecifier_CONTAINS;
      case CONTAINS_NOT:
        return Messages.SearchSpecifier_DOESNT_CONTAIN;
      case BEGINS_WITH:
        return Messages.SearchSpecifier_BEGINS_WITH;
      case ENDS_WITH:
        return Messages.SearchSpecifier_ENDS_WITH;
      case IS_BEFORE:
        return Messages.SearchSpecifier_IS_BEFORE;
      case IS_AFTER:
        return Messages.SearchSpecifier_IS_AFTER;
      case IS_GREATER_THAN:
        return Messages.SearchSpecifier_IS_GREATHER_THAN;
      case IS_LESS_THAN:
        return Messages.SearchSpecifier_IS_LESS_THAN;
      case SIMILIAR_TO:
        return Messages.SearchSpecifier_IS_SIMILAR_TO;
      case SCOPE:
        return Messages.SearchSpecifier_IS;
      default:
        return super.toString();
    }
  }

  /**
   * Returns <code>TRUE</code> if this specifier is of the type
   * <code>IS_NOT</code> or <code>CONTAINS_NOT</code>.
   *
   * @return Returns <code>TRUE</code> if this specifier is of the type
   * <code>IS_NOT</code> or <code>CONTAINS_NOT</code>.
   */
  public boolean isNegation() {
    return (this == IS_NOT) || (this == CONTAINS_NOT);
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
}
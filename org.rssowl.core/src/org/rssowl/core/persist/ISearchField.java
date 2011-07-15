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
 * <p>
 * Instances of <code>ISearchField</code> describe the target field for a
 * search condition. The field is described by its identifier in the system and
 * a human-readable name, used in the UI.
 * </p>
 * <p>
 * A call to <code>getSearchValueType()</code> will give Information of the
 * data-type the field is using. This information can be used for validating the
 * search-value and to perform the search in the persistance layer.
 * </p>
 *
 * @author bpasero
 */
public interface ISearchField extends IPersistable {

  /**
   * The ID of the search field is uniquely identifying it. It is important that
   * the ID matches the value of the related constant in the affected type.
   * <p>
   * Example: In case the search-field is from the type <code>INews</code> and
   * targeting a News' Title-Field, a call to <code>getFieldID()</code> should
   * return <code>INews.TITLE</code>
   * </p>
   *
   * @return Returns the ID of the search field, uniquely identifying it.
   */
  int getId();

  /**
   * The fully qualified Name of the Entity this <code>ISearchField</code> is
   * referring to.
   *
   * @return The fully qualified Name of the Entity this
   * <code>ISearchField</code> is referring to.
   */
  String getEntityName();

  /**
   * The name of a field is used to represent it in human-readable form inside
   * the UI.
   *
   * @return Returns a human-readable representation of this field.
   */
  String getName();

  /**
   * The search-value-type is dependant on this field. For example some fields
   * require a Date or Time as search-value ("Publish Date"). This information
   * can also be used to perform the search in the persistance layer.
   *
   * @return Returns the search-value-type related to this field.
   */
  ISearchValueType getSearchValueType();
}
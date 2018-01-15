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

import org.rssowl.core.persist.reference.SearchConditionReference;

/**
 * <p>
 * Instances of <code>ISearchCondition</code> are grouped under a single
 * Search. Each condition is connected with the others of the search through the
 * value of <code>isAndSearch()</code> in the SearchMark that contains N
 * conditions, such as:
 * <ul>
 * <li>If TRUE, News have to match all Condition</li>
 * <li>If FALSE, News that dont match this Condition have to match at least one
 * other Condition </li>
 * </ul>
 * </p>
 * <p>
 * The condition contains the affected <code>ISearchField</code>, which maps
 * to a specific Field in the persistance layer.
 * </p>
 * <p>
 * The specififer maps to an Enumeration of possible values. They describe how
 * the Search-Value should be used while searching. Some values are "is", "is
 * not" and "begins with".
 * </p>
 * <p>
 * Last but not least, a call to <code>getValue()</code> returns the value of
 * this condition.
 * </p>
 * <p>
 * Example of a SearchCondition: "Title is'nt 'RSSOwl'"<br>
 * where:
 * <ul>
 * <li>Title belongs to <code>ISearchField</code></li>
 * <li>is'nt belongs to <code>ISearchSpecifier</code></li>
 * <li>'RSSOwl' is returned by <code>getValue()</code></li>
 * </ul>
 * </p>
 * <p>
 * A group of search-conditions may or may not be related to a
 * <code>ISearchMark</code>. If they are related, that basically means that
 * the search is stored in the persistance-layer and is displayed in the List of
 * Marks.
 * </p>
 *
 * @author bpasero
 */
public interface ISearchCondition extends IEntity {

  /**
   * Instances of <code>ISearchField</code> describe the affected field of
   * this search condition. Possible fields are title or description of a News.
   *
   * @return Returns the field this search condition is affecting.
   */
  ISearchField getField();

  /**
   * Instances of <code>ISearchSpecifier</code> describe the logical way, the
   * search condition is to be treated. Possible specifiers are "is", "is not"
   * and "begins with".
   *
   * @return Returns the specifier describing how the search condition is to be
   * treated.
   */
  SearchSpecifier getSpecifier();

  /**
   * The value of this search-condition is the target Query-Value for matching
   * News on the given search field. Depending on the Search-Field, this can be
   * any of:
   * <ul>
   * <li>String</li>
   * <li>Number</li>
   * <li>Boolean</li>
   * <li>Date</li>
   * <li>EnumSet</li>
   * </ul>
   *
   * @return Returns the target Query-Value for matching News on the given
   * search field.
   */
  Object getValue();

  /**
   * @param field Sets the field this search condition is affecting.
   */
  void setField(ISearchField field);

  /**
   * @param specifier Sets the specifier describing how the search condition is
   * to be treated.
   */
  void setSpecifier(SearchSpecifier specifier);

  /**
   * Sets the value of this search-condition. The value is the target
   * Query-Value for matching News on the given search field. Depending on the
   * Search-Field, this can be any of:
   * <ul>
   * <li>String</li>
   * <li>Number</li>
   * <li>Boolean</li>
   * <li>Date</li>
   * <li>EnumSet</li>
   * </ul>
   *
   * @param value Sets the value for matching News on the given search field.
   */
  void setValue(Object value);

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  @Override
  SearchConditionReference toReference();
}
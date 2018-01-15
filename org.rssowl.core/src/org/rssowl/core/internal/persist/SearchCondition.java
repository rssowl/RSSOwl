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
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.reference.SearchConditionReference;

/**
 * <p>
 * Instances of <code>ISearchCondition</code> are grouped under a single
 * Search. Each condition is connected with the others of the search through the
 * value of <code>isAndSearch()</code>, such as:
 * <ul>
 * <li>If TRUE, News have to match this Condition</li>
 * <li>If FALSE, News that don't match this Condition have to match at least any
 * other Condition that returns FALSE, or ALL Conditions that return TRUE on a
 * call to <code>isAndSearch()</code></li>
 * </ul>
 * </p>
 * <p>
 * The condition contains the affected <code>ISearchField</code>, which maps
 * to a specific Field in the persistence layer.
 * </p>
 * <p>
 * The specifier maps to an Enumeration of possible values. They describe how
 * the Search-Value should be used while searching. Some values are "is", "is
 * not" and "begins with".
 * </p>
 * <p>
 * Last but not least, a call to <code>getValue()</code> returns the value of
 * this condition.
 * </p>
 * <p>
 * Example of a SearchCondition: "Title isn't 'RSSOwl'"<br>
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
 * the search is stored in the persistence-layer and is displayed in the List of
 * Marks.
 * </p>
 *
 * @author bpasero
 */
public class SearchCondition extends AbstractEntity implements ISearchCondition {
  private SearchSpecifier fSpecifier;
  private Object fValue;
  private ISearchField fField;

  /**
   * Instantiates a new SearchCondition that is related to a
   * <code>ISearchMark</code>.
   *
   * @param id The ID of this Type or <code>NO_ID if none</code>.
   * @param value The value of this search.
   * @param field The SearchField as described in the SearchFieldEnum.
   * @param specifier The specifier of this search.
   */
  public SearchCondition(Long id, ISearchField field, SearchSpecifier specifier, Object value) {
    super(id);
    Assert.isNotNull(field, "Search-Field for SearchCondition must not be NULL"); //$NON-NLS-1$
    fField = field;
    Assert.isNotNull(specifier, "Search-Specifier for SearchCondition must not be NULL"); //$NON-NLS-1$
    fSpecifier = specifier;
    Assert.isNotNull(value, "Search-Value for SearchCondition must not be NULL"); //$NON-NLS-1$
    fValue = value;
  }

  /**
   * Instantiates a new SearchCondition.
   *
   * @param value The value of this search (will be converted to a String).
   * @param field The SearchField as described in the SearchFieldEnum.
   * @param specifier The specifier of this search.
   */
  public SearchCondition(ISearchField field, SearchSpecifier specifier, Object value) {
    super(null);
    Assert.isNotNull(field, "Search-Field for SearchCondition must not be NULL"); //$NON-NLS-1$
    fField = field;
    Assert.isNotNull(specifier, "Search-Specifier for SearchCondition must not be NULL"); //$NON-NLS-1$
    fSpecifier = specifier;
    Assert.isNotNull(value, "Search-Value for SearchCondition must not be NULL"); //$NON-NLS-1$
    fValue = value;
  }

  /**
   * Default constructor for deserialization
   */
  protected SearchCondition() {
  // As per javadoc
  }

  /*
   * @see org.rssowl.core.model.search.ISearchCondition#getField()
   */
  @Override
  public synchronized ISearchField getField() {
    return fField;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchCondition#getSpecifier()
   */
  @Override
  public synchronized SearchSpecifier getSpecifier() {
    return fSpecifier;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchCondition#getValue()
   */
  @Override
  public synchronized Object getValue() {
    return fValue;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchCondition#setField(org.rssowl.core.model.internal.search.SearchField)
   */
  @Override
  public synchronized void setField(ISearchField field) {
    fField = field;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchCondition#setSpecifier(org.rssowl.core.model.search.SearchSpecifier)
   */
  @Override
  public synchronized void setSpecifier(SearchSpecifier specifier) {
    fSpecifier = specifier;
  }

  /*
   * @see org.rssowl.core.persist.ISearchCondition#setValue(java.lang.Object)
   */
  @Override
  public synchronized void setValue(Object value) {
    fValue = value;
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param searchCondition to be compared.
   * @return whether this object and <code>searchcondition</code> are
   * identical. It compares all the fields.
   */
  public synchronized boolean isIdentical(ISearchCondition searchCondition) {
    if (this == searchCondition)
      return true;

    if (!(searchCondition instanceof SearchCondition))
      return false;

    synchronized (searchCondition) {
      SearchCondition s = (SearchCondition) searchCondition;

      return  (getId() == null ? s.getId() == null : getId().equals(s.getId())) &&
          fField.equals(s.fField) && fSpecifier.equals(s.fSpecifier) &&
          fValue.equals(s.fValue) && (getProperties() == null ? s.getProperties() == null : getProperties().equals(s.getProperties()));
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#toString()
   */
  @Override
  public synchronized String toString() {
    return super.toString() + "Search-Field = " + fField + ", Search-Specifier = " + fSpecifier.name() + ", Search-Value = " + fValue + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  @Override
  public SearchConditionReference toReference() {
    return new SearchConditionReference(getIdAsPrimitive());
  }
}
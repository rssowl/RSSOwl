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
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.reference.ModelReference;
import org.rssowl.core.persist.reference.SearchFilterReference;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link ISearchFilter} to perform a set of operations on
 * entities matching a certain search.
 *
 * @author bpasero
 */
public class SearchFilter extends AbstractEntity implements ISearchFilter {
  private int fOrder;
  private String fName;
  private boolean fEnabled;
  private List<IFilterAction> fActions;
  private ISearch fSearch;
  private boolean fMatchAllNews;

  /**
   * @param id
   * @param search
   * @param name
   */
  public SearchFilter(Long id, ISearch search, String name) {
    super(id);
    Assert.isNotNull(name);
    fSearch = search;
    fMatchAllNews = (fSearch == null);
    fActions = new ArrayList<IFilterAction>(1);
    fName = name;
  }

  /**
   * Default constructor for deserialization
   */
  protected SearchFilter() {
    // As per javadoc
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#getActions()
   */
  @Override
  public synchronized List<IFilterAction> getActions() {
    return new ArrayList<IFilterAction>(fActions);
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#getName()
   */
  @Override
  public synchronized String getName() {
    return fName;
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#getOrder()
   */
  @Override
  public synchronized int getOrder() {
    return fOrder;
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#getSearch()
   */
  @Override
  public synchronized ISearch getSearch() {
    return fSearch;
  }

  /*
   * @see
   * org.rssowl.core.persist.ISearchFilter#setSearch(org.rssowl.core.persist
   * .reference.SearchReference)
   */
  @Override
  public synchronized void setSearch(ISearch search) {
    fSearch = search;
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#isEnabled()
   */
  @Override
  public synchronized boolean isEnabled() {
    return fEnabled;
  }

  /*
   * @see
   * org.rssowl.core.persist.ISearchFilter#addAction(org.rssowl.core.persist
   * .IFilterAction)
   */
  @Override
  public synchronized void addAction(IFilterAction action) {
    fActions.add(action);
  }

  /*
   * @see
   * org.rssowl.core.persist.ISearchFilter#removeAction(org.rssowl.core.persist
   * .IFilterAction)
   */
  @Override
  public synchronized void removeAction(IFilterAction action) {
    fActions.remove(action);
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#setEnabled(boolean)
   */
  @Override
  public synchronized void setEnabled(boolean enabled) {
    fEnabled = enabled;
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#setName(java.lang.String)
   */
  @Override
  public synchronized void setName(String name) {
    fName = name;
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#setOrder(int)
   */
  @Override
  public synchronized void setOrder(int order) {
    fOrder = order;
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#setMatchAllNews(boolean)
   */
  @Override
  public synchronized void setMatchAllNews(boolean matchAllNews) {
    fMatchAllNews = matchAllNews;
  }

  /*
   * @see org.rssowl.core.persist.ISearchFilter#matchAllNews()
   */
  @Override
  public synchronized boolean matchAllNews() {
    return fMatchAllNews;
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  @Override
  public synchronized ModelReference toReference() {
    return new SearchFilterReference(getIdAsPrimitive());
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#toString()
   */
  @Override
  public synchronized String toString() {
    return super.toString() + "Name = " + fName + ", "; //$NON-NLS-1$ //$NON-NLS-2$
  }
}
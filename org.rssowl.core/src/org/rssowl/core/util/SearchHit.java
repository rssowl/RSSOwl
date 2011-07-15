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

package org.rssowl.core.util;

import java.util.Map;

/**
 * Instances of <code>SearchHit</code> are the result of running a query in
 * the <code>IModelSearch</code>. Every hit provides the result identified by
 * <code>T</code>, the relevance score and allows to receive additional data
 * in a generic way.
 *
 * @author ijuma
 * @author bpasero
 * @param <T> The type of Object this Hit provides.
 */
public class SearchHit<T> {

  /** Indicator for an unknown relevance */
  public static final float UNKNOWN_RELEVANCE = -1.0f;

  private final T fResult;
  private final float fRelevance;
  private final Map<?, ?> fData;

  /**
   * @param result a Reference to the Type that is a Hit of the Search
   * @param relevance the relevance of this Search Hit or
   * <code>UNKNOWN_RELEVANCE</code> in case unknown.
   * @param data A Map of data that can be used from the
   * {@link SearchHit#getData(Object)} method.
   */
  public SearchHit(T result, float relevance, Map<?, ?> data) {
    fResult = result;
    fRelevance = relevance;
    fData = data;
  }

  /**
   * @return Returns the relevance of this Search Hit or
   * <code>UNKNOWN_RELEVANCE</code> in case unknown.
   */
  public float getRelevance() {
    return fRelevance;
  }

  /**
   * @return Returns a Reference to the Type that is a Hit of the Search.
   */
  public T getResult() {
    return fResult;
  }

  /**
   * @param key The key to identify the data that is to be retrieved.
   *
   * @return Returns the data associated with the key or <code>NULL</code> if
   * none.
   */
  public Object getData(Object key) {
    return fData != null ? fData.get(key) : null;
  }
}
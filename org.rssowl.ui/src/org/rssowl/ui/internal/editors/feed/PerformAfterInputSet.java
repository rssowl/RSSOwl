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

package org.rssowl.ui.internal.editors.feed;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.reference.NewsReference;

/**
 * The <code>PerformAfterInputSet</code> class is defining an action that is
 * to be executed when the input has been set in the <code>FeedView</code>.
 *
 * @author bpasero
 */
public class PerformAfterInputSet {

  /** Enum of actions that can be performed automatically after input set */
  enum Kind {

    /** Select the first News that is visible */
    SELECT_FIRST_NEWS,

    /** Select the first unread News that is visible */
    SELECT_UNREAD_NEWS,

    /** Select the provided News */
    SELECT_SPECIFIC_NEWS;
  }

  /** Select the first News in the FeedView */
  public static final PerformAfterInputSet SELECT_FIRST_NEWS = new PerformAfterInputSet(PerformAfterInputSet.Kind.SELECT_FIRST_NEWS);

  /** Select the first unread News in the FeedView */
  public static final PerformAfterInputSet SELECT_UNREAD_NEWS = new PerformAfterInputSet(PerformAfterInputSet.Kind.SELECT_UNREAD_NEWS);

  /**
   * Creates a new <code>PerformAfterInputSet</code> that will select the
   * given News in the FeedView.
   *
   * @param reference A reference to the News that is to be selected.
   * @return a new <code>PerformAfterInputSet</code> that will select the
   * given News in the FeedView.
   */
  public static PerformAfterInputSet selectNews(NewsReference reference) {
    Assert.isNotNull(reference);

    return new PerformAfterInputSet(PerformAfterInputSet.Kind.SELECT_SPECIFIC_NEWS, reference);
  }

  private final PerformAfterInputSet.Kind fType;
  private final NewsReference fNewsToSelect;
  private boolean fShouldActivate = true;

  private PerformAfterInputSet(PerformAfterInputSet.Kind type) {
    this(type, null);
  }

  private PerformAfterInputSet(PerformAfterInputSet.Kind type, NewsReference newsToSelect) {
    fType = type;
    fNewsToSelect = newsToSelect;
  }

  PerformAfterInputSet.Kind getType() {
    return fType;
  }

  NewsReference getNewsToSelect() {
    return fNewsToSelect;
  }

  /**
   * @return <code>TRUE</code> to activate the feed-view after this action has
   * been performed, <code>FALSE</code> otherwise.
   */
  public boolean shouldActivate() {
    return fShouldActivate;
  }

  /**
   * @param activate If <code>TRUE</code>, activate the feed-view after this
   * action has been performed, <code>FALSE</code> otherwise.
   */
  public void setActivate(boolean activate) {
    fShouldActivate = activate;
  }
}
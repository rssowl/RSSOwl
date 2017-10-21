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

package org.rssowl.ui.internal.filter;

import org.rssowl.core.INewsAction;

/**
 * A helper to access the values of a {@link INewsAction} contribution.
 *
 * @author bpasero
 */
public class NewsActionDescriptor implements Comparable<NewsActionDescriptor> {
  private final String fActionId;
  private final String fName;
  private final String fSortKey;
  private final INewsAction fNewsAction;
  private final String fDescription;
  private final boolean fIsForcable;

  /**
   * @param actionId the unique ID of the contributed {@link INewsAction}
   * @param newsAction the contributed implementation of {@link INewsAction}
   * @param name the human readable name of the action
   * @param description a description of what the action does nor
   * <code>null</code> if none.
   * @param sortKey the sort key of the action
   * @param isForcable if <code>true</code> allows to be run forced.
   */
  public NewsActionDescriptor(String actionId, INewsAction newsAction, String name, String description, String sortKey, boolean isForcable) {
    fActionId = actionId;
    fNewsAction = newsAction;
    fName = name;
    fDescription = description;
    fSortKey = sortKey;
    fIsForcable = isForcable;
  }

  /**
   * @return the unique ID of the contributed {@link INewsAction}
   */
  public String getActionId() {
    return fActionId;
  }

  /**
   * @return the contributed implementation of {@link INewsAction}
   */
  public INewsAction getNewsAction() {
    return fNewsAction;
  }

  /**
   * @return the human readable name of the action
   */
  public String getName() {
    return fName;
  }

  /**
   * @return a description of what the action does or <code>null</code> if none.
   */
  public String getDescription() {
    return fDescription;
  }

  /**
   * @return the sort key of the action
   */
  public String getSortKey() {
    return fSortKey;
  }

  /**
   * @return <code>true</code> if this action can be forced to run and
   * <code>false</code> otherwise.
   */
  public boolean isForcable() {
    return fIsForcable;
  }

  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(NewsActionDescriptor otherDescriptor) {
    if (equals(otherDescriptor))
      return 0;

    return otherDescriptor.fSortKey.compareTo(fSortKey);
  }
}
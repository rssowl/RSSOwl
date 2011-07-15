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

/**
 * Enumeration used for the {@link NewsTableControl} representing the available
 * columns for a feed to display.
 *
 * @author bpasero
 */
public enum NewsColumn {

  /** Title of the News */
  TITLE(Messages.NewsColumn_TITLE, true, false, true, true, true, true, true),

  /** Date of the News */
  DATE(Messages.NewsColumn_DATE, true, false, false, true, true, true, true),

  /** Publish Date of the News */
  PUBLISHED(Messages.NewsColumn_PUBLISHED, true, false, false, true, true, true, true),

  /** Modified Date of the News */
  MODIFIED(Messages.NewsColumn_MODIFIED, true, false, false, true, true, true, true),

  /** Received Date of the News */
  RECEIVED(Messages.NewsColumn_RECEIVED, true, false, false, true, true, true, true),

  /** Author of the News */
  AUTHOR(Messages.NewsColumn_AUTHOR, true, false, true, true, true, true, true),

  /** Category of the News */
  CATEGORY(Messages.NewsColumn_CATEGORY, true, false, true, true, true, true, true),

  /** Labels */
  LABELS(Messages.NewsColumn_LABEL, true, false, true, true, true, true, true),

  /** Status */
  STATUS(Messages.NewsColumn_STATUS, true, false, true, true, true, true, true),

  /** Sticky-State of the News */
  STICKY(Messages.NewsColumn_STICKY, false, true, false, true, false, true, false),

  /** Attachments */
  ATTACHMENTS(Messages.NewsColumn_ATTACHMENTS, false, true, false, true, false, true, false),

  /** Feed of a News */
  FEED(Messages.NewsColumn_FEED, false, true, false, true, false, true, false),

  /** Relevance of a News (not selectable) */
  RELEVANCE(Messages.NewsColumn_RELEVANCE, false, true, false, false, false, true, false),

  /** Location of a News */
  LOCATION(Messages.NewsColumn_LOCATION, true, false, true, true, true, true, true),

  /** Link of a News */
  LINK(Messages.NewsColumn_LINK, true, false, true, true, true, true, true);

  private final String fName;
  private final boolean fShowName;
  private final boolean fShowTooltip;
  private final boolean fPrefersAscending;
  private final boolean fSelectable;
  private final boolean fResizable;
  private final boolean fMoveable;
  private final boolean fShowSortIndicator;

  NewsColumn(String name, boolean showName, boolean showTooltip, boolean prefersAscending, boolean selectable, boolean resizable, boolean moveable, boolean showSortIndicator) {
    fName = name;
    fShowName = showName;
    fShowTooltip = showTooltip;
    fPrefersAscending = prefersAscending;
    fSelectable = selectable;
    fResizable = resizable;
    fMoveable = moveable;
    fShowSortIndicator = showSortIndicator;
  }

  /**
   * @return the name of the column or <code>null</code> if none.
   */
  public String getName() {
    return fName;
  }

  /**
   * @return <code>true</code> if the column should show its name or
   * <code>false</code> otherwise.
   */
  public boolean showName() {
    return fShowName;
  }

  /**
   * @return <code>true</code> if the column should show its tooltip or
   * <code>false</code> otherwise.
   */
  public boolean showTooltip() {
    return fShowTooltip;
  }

  /**
   * @return Returns <code>TRUE</code> if this Column prefers to be sorted
   * ascending and <code>FALSE</code> otherwise.
   */
  public boolean prefersAscending() {
    return fPrefersAscending;
  }

  /**
   * @return <code>true</code> if this column is selectable for the user and
   * <code>false</code> otherwise.
   */
  public boolean isSelectable() {
    return fSelectable;
  }

  /**
   * @return <code>true</code> if this column should indicate the sort direction
   * and <code>false</code> otherwise.
   */
  public boolean showSortIndicator() {
    return fShowSortIndicator;
  }

  /**
   * @return <code>true</code> if the column is resizable and <code>false</code>
   * otherwise.
   */
  public boolean isResizable() {
    return fResizable;
  }

  /**
   * @return <code>true</code> if the column is moveable and <code>false</code>
   * otherwise.
   */
  public boolean isMoveable() {
    return fMoveable;
  }
}
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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.core.internal.persist.messages"; //$NON-NLS-1$
  public static String SearchField_AGE;
  public static String SearchField_ATTACHMENT;
  public static String SearchField_AUTHOR;
  public static String SearchField_BLOGROLL_LINK;
  public static String SearchField_BOOKMARKS;
  public static String SearchField_CATEGORY;
  public static String SearchField_COLOR;
  public static String SearchField_COMMENTS;
  public static String SearchField_COPYRIGHT;
  public static String SearchField_CREATION_DATE;
  public static String SearchField_DELETED;
  public static String SearchField_DESCRIPTION;
  public static String SearchField_DOCS;
  public static String SearchField_DOMAIN;
  public static String SearchField_EMAIL;
  public static String SearchField_ENTIRE_ATTACHMENT;
  public static String SearchField_ENTIRE_BOOKMARK;
  public static String SearchField_ENTIRE_CATEGORY;
  public static String SearchField_ENTIRE_FOLDER;
  public static String SearchField_ENTIRE_LABEL;
  public static String SearchField_ENTIRE_NEWS;
  public static String SearchField_ENTIRE_PERSON;
  public static String SearchField_ENTIRE_SEARCHMARK;
  public static String SearchField_ERROR_LOADING;
  public static String SearchField_FEED;
  public static String SearchField_FORMAT;
  public static String SearchField_GENERATOR;
  public static String SearchField_GUID;
  public static String SearchField_HAS_ATTACHMENT;
  public static String SearchField_HOMEPAGE;
  public static String SearchField_IMAGE;
  public static String SearchField_IS_STICKY;
  public static String SearchField_LABEL;
  public static String SearchField_LANGUAGE;
  public static String SearchField_LAST_BUILT_DATE;
  public static String SearchField_LAST_MODIFIED_DATE;
  public static String SearchField_LAST_VISIT;
  public static String SearchField_LINK;
  public static String SearchField_LOCATION;
  public static String SearchField_MODIFIED_DATE;
  public static String SearchField_NAME;
  public static String SearchField_NEW;
  public static String SearchField_NUMBER_OF_NEWS;
  public static String SearchField_NUMBER_OF_VISITS;
  public static String SearchField_PUBLISH_DATE;
  public static String SearchField_RATING;
  public static String SearchField_READ;
  public static String SearchField_RECEIVED_DATE;
  public static String SearchField_SIZE;
  public static String SearchField_SOURCE;
  public static String SearchField_STATE_OF_NEWS;
  public static String SearchField_SUB_FOLDERS;
  public static String SearchField_TIME_TO_LIVE;
  public static String SearchField_TITLE;
  public static String SearchField_TYPE;
  public static String SearchField_UNREAD;
  public static String SearchField_UPDATED;
  public static String SearchField_URI;
  public static String SearchField_WEBMASTER;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

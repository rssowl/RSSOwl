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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.filter.messages"; //$NON-NLS-1$
  public static String DownloadAttachmentsNewsAction_DOWNLOAD_TO_N;
  public static String DownloadAttachmentsNewsActionPresentation_SELECT_FOLDER;
  public static String DownloadAttachmentsNewsActionPresentation_TO_N;
  public static String DownloadAttachmentsNewsActionPresentation_TO_SELECT_FOLDER;
  public static String LabelNewsActionPresentation_NEW_LABEL;
  public static String MoveCopyNewsActionPresentation_TO_NEWS_BINS;
  public static String NewsActionList_ADD_ACTION;
  public static String NewsActionList_DELETE_ACTION;
  public static String OpenNewsAction_OPEN_BROWSER_QUEUE;
  public static String PlaySoundActionPresentation_SELECT_SOUND;
  public static String PlaySoundActionPresentation_SELECT_SOUND_TO_PLAY;
  public static String PlaySoundActionPresentation_SOUND_LINK;
  public static String ShowGrowlActionPresentation_SELECT_GROWL_LINK;
  public static String ShowGrowlActionPresentation_SELECT_GROWL_TITLE;
  public static String ShowNotifierNewsActionPresentation_SELECT_COLOR;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

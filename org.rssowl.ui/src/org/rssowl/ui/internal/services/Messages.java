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

package org.rssowl.ui.internal.services;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.services.messages"; //$NON-NLS-1$
  public static String DownloadService_BYTES_OF_BYTES;
  public static String DownloadService_BYTES_OF_UNKNOWN;
  public static String DownloadService_BYTES_PER_SECOND;
  public static String DownloadService_BYTES_REMAINING;
  public static String DownloadService_DOWNLOADING;
  public static String DownloadService_DOWNLOADING_N;
  public static String DownloadService_DOWNLOADING_TITLE;
  public static String DownloadService_ERROR_DOWNLOADING;
  public static String DownloadService_ERROR_DOWNLOADING_N;
  public static String DownloadService_N_OF_M;
  public static String DownloadService_OPEN_FOLDER;
  public static String DownloadService_RE_DOWNLOAD;
  public static String DownloadService_TRY_AGAIN;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

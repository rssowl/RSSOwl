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

package org.rssowl.ui.internal.dialogs.exporter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.dialogs.exporter.messages"; //$NON-NLS-1$
  public static String ExportElementsPage_DESELECT_ALL;
  public static String ExportElementsPage_EXPORT_ELEMENTS;
  public static String ExportElementsPage_SELECT_ALL;
  public static String ExportOptionsPage_EXPORT_FILTERS;
  public static String ExportOptionsPage_EXPORT_LABEL_FILTER_INFO;
  public static String ExportOptionsPage_EXPORT_LABELS;
  public static String ExportOptionsPage_EXPORT_N_FILTERS;
  public static String ExportOptionsPage_EXPORT_N_LABELS;
  public static String ExportOptionsPage_EXPORT_OPTIONS;
  public static String ExportOptionsPage_EXPORT_PREFERENCES;
  public static String ExportOptionsPage_OPTIONS_INFO;
  public static String ExportWizard_CHOOSE_ELEMENTS;
  public static String ExportWizard_EXPORT;
  public static String ExportWizard_EXPORT_FILE;
  public static String ExportWizard_EXPORT_OPTIONS;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

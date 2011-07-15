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

package org.rssowl.core.persist;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.core.persist.messages"; //$NON-NLS-1$
  public static String SearchSpecifier_BEGINS_WITH;
  public static String SearchSpecifier_CONTAINS;
  public static String SearchSpecifier_CONTAINS_ANY;
  public static String SearchSpecifier_DOESNT_CONTAIN;
  public static String SearchSpecifier_ENDS_WITH;
  public static String SearchSpecifier_IS;
  public static String SearchSpecifier_IS_AFTER;
  public static String SearchSpecifier_IS_BEFORE;
  public static String SearchSpecifier_IS_GREATHER_THAN;
  public static String SearchSpecifier_IS_LESS_THAN;
  public static String SearchSpecifier_IS_SIMILAR_TO;
  public static String SearchSpecifier_ISNT;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}

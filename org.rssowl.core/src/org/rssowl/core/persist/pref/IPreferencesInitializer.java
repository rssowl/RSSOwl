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

package org.rssowl.core.persist.pref;

/**
 * Instances of <code>IPreferencesInitializer</code> provide preferences to the
 * default scope. The default scope provides the default-preferences for all
 * other Scopes.
 * <p>
 * Contributed via <code>org.rssowl.core.PreferencesInitializer</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 */
public interface IPreferencesInitializer {

  /**
   * Initializes the given scope of default preferences with values.
   *
   * @param defaultScope The default-scope containing initial preferences. These
   * serve as the default-preferences for all other Scopes.
   */
  void initialize(IPreferenceScope defaultScope);
}
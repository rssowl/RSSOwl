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

package org.rssowl.core.persist.service;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferencesInitializer;

/**
 * <p>
 * Provides access to the scoped preferences service in RSSOwl. There is three
 * levels of preferences: Default, Global and Entity. Any preference that is not
 * set at the one scope will be looked up in the parent scope until the Default
 * scope is reached. This allows to easily override the preferences for all
 * entities without having to define the preferences per entity.
 * </p>
 * <p>
 * You can define default preferences by using the PreferencesInitializer
 * extension point provided by this plugin.
 * </p>
 *
 * @author bpasero
 * @see IPreferenceScope
 * @see IPreferencesInitializer
 */
public interface IPreferenceService {

  /**
   * The default scope can be used to intialize default preferences. It is the
   * most-outer Scope with no parent scope at all. None of the values stored in
   * the default scope is persisted.
   *
   * @return The Default Scope for Preferences.
   */
  IPreferenceScope getDefaultScope();

  /**
   * The global scope stores global preferences. Most entity-scopes will be
   * initialized with the values of the global scope.
   *
   * @return The Global Scope for Preferences.
   */
  IPreferenceScope getGlobalScope();

  /**
   * The eclipse scope can be used to retrieve preferences that are stored in
   * the Eclipse platform via {@link IEclipsePreferences} and
   * {@link IPreferencesService}. It should only be used if the preference is
   * interpreted by other Eclipse plugins. In any other case, use
   * {@link #getGlobalScope()}.
   *
   * @return The Eclipse Scope for Preferences.
   */
  IPreferenceScope getEclipseScope();

  /**
   * The entity scope stores preferences in the given entity itself.
   *
   * @param entity The Entity to be used for the Scope.
   * @return The Entity Scope for Preferences as defined by the given Entity.
   */
  IPreferenceScope getEntityScope(IEntity entity);
}
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

package org.rssowl.core.internal.persist.service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.persist.pref.DefaultScope;
import org.rssowl.core.internal.persist.pref.EclipseScope;
import org.rssowl.core.internal.persist.pref.EntityScope;
import org.rssowl.core.internal.persist.pref.GlobalScope;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferencesInitializer;
import org.rssowl.core.persist.service.IPreferenceService;

/**
 * Provides access to all preference related classes.
 *
 * @author bpasero
 */
public class PreferenceServiceImpl implements IPreferenceService {

  /* Extension Point: Preferences Initializer */
  private static final String PREFERENCES_INITIALIZER_EXTENSION_POINT = "org.rssowl.core.PreferencesInitializer"; //$NON-NLS-1$

  /* Scoped Preferences */
  private final IPreferenceScope fDefaultScope;
  private final IPreferenceScope fGlobalScope;
  private final IPreferenceScope fEclipseScope;

  /** */
  public PreferenceServiceImpl() {
    fDefaultScope = new DefaultScope();
    fGlobalScope = new GlobalScope(fDefaultScope);
    fEclipseScope = new EclipseScope(fDefaultScope);
    initScopedPreferences();
  }

  /*
   * @see
   * org.rssowl.core.model.persist.pref.IPreferencesService#getDefaultScope()
   */
  public IPreferenceScope getDefaultScope() {
    return fDefaultScope;
  }

  /*
   * @see
   * org.rssowl.core.model.persist.pref.IPreferencesService#getGlobalScope()
   */
  public IPreferenceScope getGlobalScope() {
    return fGlobalScope;
  }

  /*
   * @see org.rssowl.core.persist.service.IPreferenceService#getEclipseScope()
   */
  public IPreferenceScope getEclipseScope() {
    return fEclipseScope;
  }

  /*
   * @see
   * org.rssowl.core.model.persist.pref.IPreferencesService#getEntityScope(org
   * .rssowl.core.model.persist.IEntity)
   */
  public IPreferenceScope getEntityScope(IEntity entity) {
    return new EntityScope(entity, fGlobalScope);
  }

  /* Init scoped preferences */
  private void initScopedPreferences() {

    /* Pass Service through Initializers */
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IConfigurationElement elements[] = reg.getConfigurationElementsFor(PREFERENCES_INITIALIZER_EXTENSION_POINT);
    for (IConfigurationElement element : elements) {
      try {
        IPreferencesInitializer initializer = (IPreferencesInitializer) element.createExecutableExtension("class"); //$NON-NLS-1$
        initializer.initialize(fDefaultScope);
      } catch (CoreException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }
  }
}
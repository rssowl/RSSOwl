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

package org.rssowl.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.rssowl.core.internal.Activator;

/**
 * Utility class to work with the <code>IExtensionRegistry</code> of the
 * platform in order to handle contributions.
 *
 * @author bpasero
 */
public class ExtensionUtils {

  /** The RSSOwl Namespace for extension points */
  public static final String RSSOWL_NAMESPACE = "org.rssowl"; //$NON-NLS-1$

  /** The RSSOwl Namespace for extension points */
  public static final String RSSOWL_TESTS_NAMESPACE = "org.rssowl.core.tests"; //$NON-NLS-1$

  /* Attribute for executable extensions */
  private static final String EXECUTABLE_ATTRIBUTE = "class"; //$NON-NLS-1$

  /**
   * Returns the result of creating an executable extension from the "class"
   * attribute of the first contribution that matches the given extension point.
   * Third-Party contributions will be chosen over the default contribution.
   *
   * @param extensionPoint The fully qualified identifier of the extension point
   * to use (e.g. "org.rssowl.core.ApplicationLayer")
   * @return Returns the result of creating an executable extension from the
   * "class" attribute of the first contribution that matches the given
   * extension point. Third-Party contributions will be chosen over the default
   * contribution.
   * @throws IllegalStateException if no contribution was found.
   */
  public static Object loadSingletonExecutableExtension(String extensionPoint) {
    return loadSingletonExecutableExtension(extensionPoint, null);
  }

  /**
   * Returns the result of creating an executable extension from the "class"
   * attribute of the first contribution that matches the given extension point.
   * Third-Party contributions will be chosen over the default contribution. If
   * no contribution is found, the default is returned if provided.
   *
   * @param extensionPoint The fully qualified identifier of the extension point
   * to use (e.g. "org.rssowl.core.ApplicationLayer")
   * @param defaultExecutable The default executable that should be returned if
   * no contribution was found.
   * @return Returns the result of creating an executable extension from the
   * "class" attribute of the first contribution that matches the given
   * extension point. Third-Party contributions will be chosen over the default
   * contribution. If no contribution is found, the default is returned if
   * provided.
   * @throws IllegalStateException if no contribution was found and the default
   * executable is <code>NULL</code>.
   */
  public static Object loadSingletonExecutableExtension(String extensionPoint, Object defaultExecutable) {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IConfigurationElement elements[] = reg.getConfigurationElementsFor(extensionPoint);

    /* More than one contribution - Choose 3d party over our own */
    if (elements.length > 1) {
      for (IConfigurationElement element : elements) {

        /* Let 3d-Party contributions override our contributions */
        if (!element.getNamespaceIdentifier().contains(RSSOWL_NAMESPACE)) {
          try {
            return element.createExecutableExtension(EXECUTABLE_ATTRIBUTE);
          } catch (CoreException e) {
            Activator.getDefault().getLog().log(e.getStatus());
          }
        }
      }
    }

    /* One Contribution or fallback if more than one Contrib matches org.rssowl */
    else if (elements.length == 1) {
      try {
        return elements[0].createExecutableExtension(EXECUTABLE_ATTRIBUTE);
      } catch (CoreException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }

    /* Return default if provided */
    if (defaultExecutable != null)
      return defaultExecutable;

    /* Indicate missing extension with Exception */
    throw new IllegalStateException("Unable to load contributions for " + extensionPoint); //$NON-NLS-1$
  }
}
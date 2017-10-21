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

package org.rssowl.ui.internal.dialogs.properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.rssowl.core.persist.IEntity;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.internal.Activator;

import java.util.List;
import java.util.Set;

/**
 * A Wrapper for instances of <code>IEntityPropertyPage</code> containing
 * additional information taken from the contributing extension-point.
 *
 * @author bpasero
 */
public class EntityPropertyPageWrapper implements Comparable<EntityPropertyPageWrapper> {
  private String fId;
  private IEntityPropertyPage fCachedPage;
  private IConfigurationElement fPageTemplate;
  private String fName;
  private int fOrder;
  private boolean fHandlesMultipleEntities;
  private List<Class<?>> fTargetEntities;

  /**
   * @param id The unique ID of this contributed Entity-Property Page.
   * @param pageTemplate The Property-Page that is wrapped as contributed
   * IConfigurationElement.
   * @param targetEntities The Classes of the Entities this Page is responsible
   * for.
   * @param name The Name of the Property-Page.
   * @param order The Sort-Order of the Property-Page.
   * @param handlesMultipleEntities <code>TRUE</code> in case the wrapped
   * Property-Page is able to handle N Entities, <code>FALSE</code> otherwise.
   */
  public EntityPropertyPageWrapper(String id, IConfigurationElement pageTemplate, List<Class<?>> targetEntities, String name, int order, boolean handlesMultipleEntities) {
    fId = id;
    fPageTemplate = pageTemplate;
    fTargetEntities = targetEntities;
    fName = name;
    fOrder = order;
    fHandlesMultipleEntities = handlesMultipleEntities;
  }

  /**
   * @return The IEntityPropertyPage that is wrapped.
   */
  public IEntityPropertyPage getPage() {
    Assert.isNotNull(fCachedPage, "Call createPage() first!"); //$NON-NLS-1$
    return fCachedPage;
  }

  /** Creates the page and stores it into the cache */
  public void createPage() {
    try {
      fCachedPage = (IEntityPropertyPage) fPageTemplate.createExecutableExtension("class"); //$NON-NLS-1$
    } catch (CoreException e) {
      Activator.getDefault().getLog().log(e.getStatus());
    }
  }

  /**
   * @return The Name of the Property-Page.
   */
  public String getName() {
    return fName;
  }

  /**
   * @return The Classes of the Entities this Page is responsible for.
   */
  public List<Class<?>> getTargetEntities() {
    return fTargetEntities;
  }

  /**
   * @param classes The Entity-Classes to check for
   * @return <code>TRUE</code> in case this Property-Page handles the given List
   * of classes, <code>FALSE</code> otherwise.
   */
  public boolean handles(Set<Class<? extends IEntity>> classes) {
    if (fTargetEntities.size() == 0)
      return false;

    for (Class<? extends IEntity> clazz : classes) {
      if (!handles(clazz))
        return false;
    }

    return true;
  }

  private boolean handles(Class<? extends IEntity> clazz) {
    for (Class<?> containedClass : fTargetEntities) {
      if (containedClass.isAssignableFrom(clazz))
        return true;
    }

    return false;
  }

  /**
   * @return <code>TRUE</code> in case the wrapped Property-Page is able to
   * handle N Entities, <code>FALSE</code> otherwise.
   */
  public boolean isHandlingMultipleEntities() {
    return fHandlesMultipleEntities;
  }

  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(EntityPropertyPageWrapper other) {
    if (fOrder < other.fOrder)
      return -1;
    if (fOrder > other.fOrder)
      return 1;

    return 0;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fId == null) ? 0 : fId.hashCode());
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (getClass() != obj.getClass())
      return false;

    final EntityPropertyPageWrapper other = (EntityPropertyPageWrapper) obj;
    if (fId == null) {
      if (other.fId != null)
        return false;
    } else if (!fId.equals(other.fId))
      return false;

    return true;
  }
}